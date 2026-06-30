package com.gcp.agents.cli.audit.log

import java.util.UUID

/**
 * AuditLogEntry — 结构化审计日志条目
 * Structured audit log entry for agent operations.
 *
 * @property id 日志 ID / Log ID
 * @property timestamp 时间戳 / Timestamp
 * @property operator 操作者 / Operator
 * @property agentName Agent 名称 / Agent name
 * @property operation 操作类型 / Operation type
 * @property inputSummary 输入摘要 / Input summary
 * @property outputSummary 输出摘要 / Output summary
 * @property gcpResource 涉及的 GCP 资源 / GCP resources involved
 * @property status 操作状态 / Operation status
 * @property durationMs 操作耗时（毫秒）/ Operation duration in milliseconds
 * @property metadata 额外元数据 / Additional metadata
 */
data class AuditLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val operator: String,
    val agentName: String,
    val operation: OperationType,
    val inputSummary: String,
    val outputSummary: String? = null,
    val gcpResource: String? = null,
    val status: OperationStatus,
    val durationMs: Long? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    /**
     * 操作类型 / Operation types
     */
    enum class OperationType {
        DEPLOY, STOP, DELETE, UPDATE, INVOKE, QUERY_STATUS,
        CREATE_IAM_POLICY, APPLY_IAM_POLICY, SET_QUOTA, VIEW_AUDIT_LOG
    }

    /**
     * 操作状态 / Operation status
     */
    enum class OperationStatus {
        SUCCESS, FAILURE, PARTIAL, PENDING
    }

    /**
     * 转换为 JSON 格式 / Convert to JSON format
     */
    fun toJson(): String {
        return com.google.gson.Gson().toJson(this)
    }

    /**
     * 转换为 CSV 行 / Convert to CSV row
     */
    fun toCsv(): String {
        return listOf(
            id, timestamp.toString(), operator, agentName,
            operation.name, inputSummary, outputSummary ?: "",
            gcpResource ?: "", status.name,
            durationMs?.toString() ?: ""
        ).joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
    }

    companion object {
        /** CSV 表头 / CSV header */
        val CSV_HEADER = "id,timestamp,operator,agentName,operation,inputSummary,outputSummary,gcpResource,status,durationMs"
    }
}

/**
 * StructuredLogEmitter — 结构化日志发射器（到 Cloud Logging）
 * Emits structured logs to Cloud Logging.
 *
 * @property projectId GCP 项目 ID / GCP project ID
 * @property logName 日志名称 / Log name
 */
class StructuredLogEmitter(
    private val projectId: String,
    private val logName: String = "agents-cli-sdk-audit"
) {
    /**
     * 发射日志条目 / Emit log entry
     * @param entry 审计日志条目 / Audit log entry
     */
    fun emit(entry: AuditLogEntry) {
        // In production, this would use Cloud Logging API
        // For now, log to stdout in structured format
        println("[${logName}] ${entry.toJson()}")
    }

    /**
     * 批量发射日志条目 / Emit multiple log entries
     * @param entries 审计日志条目列表 / List of audit log entries
     */
    fun emitBatch(entries: List<AuditLogEntry>) {
        entries.forEach { emit(it) }
    }
}
