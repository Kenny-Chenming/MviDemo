package com.gcp.agents.cli.iam.quota

import com.gcp.agents.cli.AgentsCliSdk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * QuotaController — Token 用量配额管理，支持配置配额上限和告警阈值
 * Token quota management with configurable limits and alert thresholds.
 *
 * @param defaultQuota 默认每小时配额 / Default hourly quota
 * @param warningThreshold 告警阈值（百分比）/ Warning threshold (percentage)
 */
class QuotaController(
    private val defaultQuota: Int = 10000,
    private val warningThreshold: Float = 0.8f
) {
    private val _quotas = MutableStateFlow<Map<String, AgentQuota>>(emptyMap())
    val quotas: StateFlow<Map<String, AgentQuota>> = _quotas.asStateFlow()

    private val usageTracker = ConcurrentHashMap<String, UsageTracker>()

    /**
     * 为 Agent 设置配额 / Set quota for an agent
     * @param agentName Agent 名称 / Agent name
     * @param hourlyLimit 每小时限制 / Hourly limit
     * @param dailyLimit 每日限制（可选）/ Daily limit (optional)
     */
    fun setQuota(agentName: String, hourlyLimit: Int, dailyLimit: Int? = null) {
        val current = _quotas.value.toMutableMap()
        current[agentName] = AgentQuota(agentName, hourlyLimit, dailyLimit)
        _quotas.value = current
    }

    /**
     * 检查是否可以执行请求 / Check if request can be executed
     * @param agentName Agent 名称 / Agent name
     * @param tokensRequested 请求的 Token 数 / Requested tokens
     * @return 是否允许 / Whether allowed
     */
    fun canExecute(agentName: String, tokensRequested: Int): QuotaCheckResult {
        val quota = _quotas.value[agentName] ?: AgentQuota(agentName, defaultQuota, null)
        val tracker = usageTracker.computeIfAbsent(agentName) { UsageTracker() }

        val hourlyUsed = tracker.getHourlyUsage()
        val dailyUsed = tracker.getDailyUsage()

        val hourlyAllowed = hourlyUsed + tokensRequested <= quota.hourlyLimit
        val dailyAllowed = quota.dailyLimit?.let { dailyUsed + tokensRequested <= it } ?: true

        return when {
            !hourlyAllowed -> QuotaCheckResult.Denied("Hourly quota exceeded: ${hourlyUsed}/${quota.hourlyLimit}")
            !dailyAllowed -> QuotaCheckResult.Denied("Daily quota exceeded: ${dailyUsed}/${quota.dailyLimit}")
            hourlyUsed >= (quota.hourlyLimit * warningThreshold).toInt() -> {
                QuotaCheckResult.Warning("Hourly quota warning: ${hourlyUsed}/${quota.hourlyLimit}")
            }
            else -> QuotaCheckResult.Allowed
        }
    }

    /**
     * 记录 Token 使用量 / Record token usage
     * @param agentName Agent 名称 / Agent name
     * @param tokensUsed 使用的 Token 数 / Tokens used
     */
    fun recordUsage(agentName: String, tokensUsed: Int) {
        val tracker = usageTracker.computeIfAbsent(agentName) { UsageTracker() }
        tracker.record(tokensUsed)

        // Emit warning if threshold reached
        val quota = _quotas.value[agentName] ?: AgentQuota(agentName, defaultQuota, null)
        val hourlyUsed = tracker.getHourlyUsage()
        if (hourlyUsed >= (quota.hourlyLimit * warningThreshold).toInt()) {
            //通知逻辑会由调用方处理
        }
    }

    /**
     * 获取 Agent 的当前使用量 / Get current usage for an agent
     * @param agentName Agent 名称 / Agent name
     * @return 使用量信息 / Usage info
     */
    fun getUsage(agentName: String): QuotaUsage {
        val tracker = usageTracker[agentName] ?: UsageTracker()
        val quota = _quotas.value[agentName] ?: AgentQuota(agentName, defaultQuota, null)
        return QuotaUsage(
            agentName = agentName,
            hourlyUsed = tracker.getHourlyUsage(),
            hourlyLimit = quota.hourlyLimit,
            dailyUsed = tracker.getDailyUsage(),
            dailyLimit = quota.dailyLimit
        )
    }

    /**
     * 重置指定 Agent 的使用量 / Reset usage for an agent
     * @param agentName Agent 名称 / Agent name
     */
    fun resetUsage(agentName: String) {
        usageTracker.remove(agentName)
    }
}

/**
 * AgentQuota — Agent 配额配置
 * Agent quota configuration.
 *
 * @property agentName Agent 名称 / Agent name
 * @property hourlyLimit 每小时限制 / Hourly limit
 * @property dailyLimit 每日限制（可选）/ Daily limit (optional)
 */
data class AgentQuota(
    val agentName: String,
    val hourlyLimit: Int,
    val dailyLimit: Int?
)

/**
 * QuotaCheckResult — 配额检查结果
 * Quota check result.
 */
sealed class QuotaCheckResult {
    object Allowed : QuotaCheckResult()
    data class Warning(val message: String) : QuotaCheckResult()
    data class Denied(val reason: String) : QuotaCheckResult()
}

/**
 * QuotaUsage — 配额使用量信息
 * Quota usage information.
 *
 * @property agentName Agent 名称 / Agent name
 * @property hourlyUsed 每小时已用 / Hourly used
 * @property hourlyLimit 每小时限制 / Hourly limit
 * @property dailyUsed 每日已用 / Daily used
 * @property dailyLimit 每日限制 / Daily limit
 */
data class QuotaUsage(
    val agentName: String,
    val hourlyUsed: Int,
    val hourlyLimit: Int,
    val dailyUsed: Int,
    val dailyLimit: Int?
)

/**
 * UsageTracker — 使用量追踪器
 * Usage tracker with hourly and daily windows.
 */
class UsageTracker {
    private val hourlyUsage = mutableListOf<Pair<Long, Int>>() // (timestamp, tokens)
    private val dailyUsage = mutableListOf<Pair<Long, Int>>() // (timestamp, tokens)

    fun record(tokens: Int) {
        val now = System.currentTimeMillis()
        hourlyUsage.add(now to tokens)
        dailyUsage.add(now to tokens)
        cleanup()
    }

    fun getHourlyUsage(): Int {
        cleanup()
        val hourAgo = System.currentTimeMillis() - 3600000
        return hourlyUsage.filter { it.first >= hourAgo }.sumOf { it.second }
    }

    fun getDailyUsage(): Int {
        cleanup()
        val dayAgo = System.currentTimeMillis() - 86400000
        return dailyUsage.filter { it.first >= dayAgo }.sumOf { it.second }
    }

    private fun cleanup() {
        val hourAgo = System.currentTimeMillis() - 3600000
        val dayAgo = System.currentTimeMillis() - 86400000
        hourlyUsage.removeAll { it.first < hourAgo }
        dailyUsage.removeAll { it.first < dayAgo }
    }
}
