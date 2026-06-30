package com.gcp.agents.cli.audit.review

import com.gcp.agents.cli.audit.log.AuditLogEntry
import com.gcp.agents.cli.model.AgentInstance
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * AccessReviewGenerator — 定期生成访问权限审查报告
 * Generates periodic access review reports.
 *
 * @param projectId GCP 项目 ID / GCP project ID
 */
class AccessReviewGenerator(
    private val projectId: String
) {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    /**
     * 生成访问权限审查报告 / Generate access review report
     * @param agents 已部署的 Agent 列表 / List of deployed agents
     * @param auditEntries 审计日志条目 / Audit log entries
     * @param reviewPeriodDays 审查周期（天）/ Review period in days
     * @return 访问审查报告 / Access review report
     */
    fun generateAccessReview(
        agents: List<AgentInstance>,
        auditEntries: List<AuditLogEntry>,
        reviewPeriodDays: Int = 30
    ): AccessReviewReport {
        val now = System.currentTimeMillis()
        val periodStart = now - (reviewPeriodDays * 24 * 3600 * 1000L)

        val relevantEntries = auditEntries.filter { it.timestamp >= periodStart }

        val agentAccessReviews = agents.map { agent ->
            AgentAccessReview(
                agentName = agent.name,
                instanceId = agent.instanceId,
                serviceAccount = agent.config.environmentVariables["GCP_SERVICE_ACCOUNT"] ?: "default",
                lastAccessed = relevantEntries
                    .filter { it.agentName == agent.name }
                    .maxOfOrNull { it.timestamp }
                    ?: agent.createdAt,
                totalOperations = relevantEntries.count { it.agentName == agent.name },
                failedOperations = relevantEntries.count {
                    it.agentName == agent.name && it.status == AuditLogEntry.OperationStatus.FAILURE
                },
                accessedResources = relevantEntries
                    .filter { it.agentName == agent.name }
                    .mapNotNull { it.gcpResource }
                    .distinct(),
                riskLevel = calculateRiskLevel(agent, relevantEntries),
                recommendation = generateRecommendation(agent, relevantEntries)
            )
        }

        val summary = AccessReviewSummary(
            totalAgents = agents.size,
            highRiskAgents = agentAccessReviews.count { it.riskLevel == RiskLevel.HIGH },
            mediumRiskAgents = agentAccessReviews.count { it.riskLevel == RiskLevel.MEDIUM },
            lowRiskAgents = agentAccessReviews.count { it.riskLevel == RiskLevel.LOW },
            unusedAgents = agentAccessReviews.count { it.totalOperations == 0 },
            reviewDate = formatter.format(Instant.now()),
            periodStart = formatter.format(Instant.ofEpochMilli(periodStart)),
            periodEnd = formatter.format(Instant.ofEpochMilli(now))
        )

        return AccessReviewReport(
            projectId = projectId,
            summary = summary,
            agentReviews = agentAccessReviews,
            generatedAt = System.currentTimeMillis()
        )
    }

    /**
     * 计算风险级别 / Calculate risk level
     */
    private fun calculateRiskLevel(agent: AgentInstance, entries: List<AuditLogEntry>): RiskLevel {
        val agentEntries = entries.filter { it.agentName == agent.name }

        // High risk: high failure rate or sensitive resources
        val failureRate = if (agentEntries.isNotEmpty()) {
            agentEntries.count { it.status == AuditLogEntry.OperationStatus.FAILURE }.toFloat() / agentEntries.size
        } else 0f

        val hasSensitiveResources = agentEntries.any {
            it.gcpResource?.contains("secret", ignoreCase = true) == true ||
            it.gcpResource?.contains("pii", ignoreCase = true) == true
        }

        return when {
            failureRate > 0.3 -> RiskLevel.HIGH
            hasSensitiveResources && failureRate > 0.1 -> RiskLevel.HIGH
            failureRate > 0.1 -> RiskLevel.MEDIUM
            agentEntries.isEmpty() -> RiskLevel.LOW
            else -> RiskLevel.LOW
        }
    }

    /**
     * 生成建议 / Generate recommendation
     */
    private fun generateRecommendation(agent: AgentInstance, entries: List<AuditLogEntry>): String {
        val agentEntries = entries.filter { it.agentName == agent.name }
        val failureRate = if (agentEntries.isNotEmpty()) {
            agentEntries.count { it.status == AuditLogEntry.OperationStatus.FAILURE }.toFloat() / agentEntries.size
        } else 0f

        return when {
            agentEntries.isEmpty() -> "Agent has not been used in the review period. Consider removing if no longer needed."
            failureRate > 0.3 -> "High failure rate detected. Review agent configuration and permissions."
            failureRate > 0.1 -> "Some operations failed. Monitor and optimize agent performance."
            else -> "Agent is operating within normal parameters."
        }
    }

    /**
     * 导出报告为 JSON / Export report as JSON
     */
    fun exportAsJson(report: AccessReviewReport): String {
        return com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report)
    }
}

/**
 * AccessReviewReport — 访问审查报告
 * Access review report.
 *
 * @property projectId GCP 项目 ID / GCP project ID
 * @property summary 审查摘要 / Review summary
 * @property agentReviews 各 Agent 的审查结果 / Per-agent review results
 * @property generatedAt 报告生成时间 / Report generation time
 */
data class AccessReviewReport(
    val projectId: String,
    val summary: AccessReviewSummary,
    val agentReviews: List<AgentAccessReview>,
    val generatedAt: Long
)

/**
 * AccessReviewSummary — 访问审查摘要
 * Access review summary.
 */
data class AccessReviewSummary(
    val totalAgents: Int,
    val highRiskAgents: Int,
    val mediumRiskAgents: Int,
    val lowRiskAgents: Int,
    val unusedAgents: Int,
    val reviewDate: String,
    val periodStart: String,
    val periodEnd: String
)

/**
 * AgentAccessReview — 单个 Agent 的访问审查结果
 * Access review result for a single agent.
 *
 * @property agentName Agent 名称 / Agent name
 * @property instanceId 实例 ID / Instance ID
 * @property serviceAccount 关联的 Service Account / Associated service account
 * @property lastAccessed 最后访问时间 / Last accessed time
 * @property totalOperations 总操作数 / Total operations
 * @property failedOperations 失败操作数 / Failed operations
 * @property accessedResources 访问过的资源 / Accessed resources
 * @property riskLevel 风险级别 / Risk level
 * @property recommendation 建议 / Recommendation
 */
data class AgentAccessReview(
    val agentName: String,
    val instanceId: String,
    val serviceAccount: String,
    val lastAccessed: Long,
    val totalOperations: Int,
    val failedOperations: Int,
    val accessedResources: List<String>,
    val riskLevel: RiskLevel,
    val recommendation: String
)

/**
 * RiskLevel — 风险级别
 */
enum class RiskLevel {
    LOW, MEDIUM, HIGH
}
