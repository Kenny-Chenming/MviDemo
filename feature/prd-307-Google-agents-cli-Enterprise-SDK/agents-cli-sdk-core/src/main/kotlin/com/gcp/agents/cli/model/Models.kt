package com.gcp.agents.cli.model

/**
 * Agent 部署请求对象 / Type-safe agent deployment request
 * @property agentName Agent 名称 / Agent name
 * @property agentSource Agent 源代码来源 / Agent source location
 * @property targetGcpService 目标 GCP 服务 / Target GCP service
 * @property iamPolicy IAM 策略（可选）/ IAM policy (optional)
 * @property labels 标签映射 / Label key-value pairs
 */
data class AgentDeploymentRequest(
    val agentName: String,
    val agentSource: AgentSource,
    val targetGcpService: GcpTargetService,
    val iamPolicy: GcpIamPolicy? = null,
    val labels: Map<String, String> = emptyMap()
)

/**
 * Agent 部署响应对象 / Agent deployment response
 * @property agentName Agent 名称 / Agent name
 * @property instanceId 实例 ID / Instance ID
 * @property status 部署状态 / Deployment status
 * @property endpoint 访问端点 / Access endpoint
 * @property createdAt 创建时间戳 / Creation timestamp
 */
data class AgentDeploymentResponse(
    val agentName: String,
    val instanceId: String,
    val status: AgentDeploymentStatus,
    val endpoint: String?,
    val createdAt: Long
)

/**
 * 已部署 Agent 的运行时对象 / Runtime object for deployed agent
 * @property name Agent 名称 / Agent name
 * @property instanceId 实例 ID / Instance ID
 * @property status 当前状态 / Current status
 * @property endpoint 访问端点 / Access endpoint
 * @property config 配置信息 / Configuration
 * @property gcpProjectId GCP 项目 ID / GCP project ID
 * @property region GCP 区域 / GCP region
 * @property createdAt 创建时间 / Creation time
 * @property lastHeartbeat 最后心跳时间 / Last heartbeat time
 */
data class AgentInstance(
    val name: String,
    val instanceId: String,
    val status: AgentDeploymentStatus,
    val endpoint: String?,
    val config: AgentConfig,
    val gcpProjectId: String,
    val region: String,
    val createdAt: Long,
    val lastHeartbeat: Long
)

/**
 * Agent 配置信息 / Agent configuration
 * @property version Agent 版本 / Agent version
 * @property runtime 运行时环境 / Runtime environment
 * @property resources 资源配置 / Resource allocation
 * @property environmentVariables 环境变量 / Environment variables
 */
data class AgentConfig(
    val version: String,
    val runtime: String,
    val resources: ResourceAllocation,
    val environmentVariables: Map<String, String>
)

/**
 * 资源配置 / Resource allocation
 * @property cpu CPU 核心数 / CPU cores
 * @property memoryGb 内存（GB）/ Memory in GB
 * @property diskGb 磁盘（GB）/ Disk in GB
 */
data class ResourceAllocation(
    val cpu: Int,
    val memoryGb: Int,
    val diskGb: Int
)

/**
 * 部署结果 / Deployment result with success/failure details
 * @property success 是否成功 / Success flag
 * @property agentInstance 部署的 Agent 实例（成功时）/ Deployed agent instance (on success)
 * @property logUrl 日志 URL（成功时）/ Log URL (on success)
 * @property errorMessage 错误信息（失败时）/ Error message (on failure)
 */
data class DeploymentResult(
    val success: Boolean,
    val agentInstance: AgentInstance?,
    val logUrl: String?,
    val errorMessage: String?
)

/**
 * Agent 操作结果 / Agent operation result
 * @property success 是否成功 / Success flag
 * @property output 输出内容 / Output content
 * @property errorMessage 错误信息（失败时）/ Error message (on failure)
 */
data class AgentOperationResult(
    val success: Boolean,
    val output: String,
    val errorMessage: String?
)

/**
 * Agent 部署状态枚举 / Agent deployment status enum
 */
enum class AgentDeploymentStatus {
    /** 排队中 / Queued */
    PENDING,
    /** 部署中 / Deploying */
    DEPLOYING,
    /** 运行中 / Running */
    RUNNING,
    /** 停止中 / Stopping */
    STOPPING,
    /** 已停止 / Stopped */
    STOPPED,
    /** 部署/运行失败 / Deployment/runtime failed */
    FAILED,
    /** 更新中 / Updating */
    UPDATING
}

/**
 * Agent 操作状态 sealed class / Agent operation status
 */
sealed class AgentOperationStatus {
    /** 空闲 / Idle */
    object Idle : AgentOperationStatus()
    /** 进行中 / In progress */
    data class InProgress(val operationId: String, val progress: Float) : AgentOperationStatus()
    /** 失败 / Failed */
    data class Failed(val error: String) : AgentOperationStatus()
    /** 成功 / Succeeded */
    data class Succeeded(val result: Any?) : AgentOperationStatus()
}

/**
 * Agent 源代码来源 / Agent source location types
 */
sealed class AgentSource {
    /** GitHub 源 / GitHub source */
    data class GitHubSource(val repo: String, val branch: String = "main") : AgentSource()
    /** 本地源 / Local source */
    data class LocalSource(val path: String) : AgentSource()
    /** GCS 源 / GCS source */
    data class GcsSource(val bucket: String, val objectKey: String) : AgentSource()
}

/**
 * GCP 目标服务 / GCP target service types
 */
sealed class GcpTargetService {
    /** Vertex AI 端点 / Vertex AI endpoint */
    data class VertexAiEndpoint(val endpoint: String, val project: String? = null) : GcpTargetService()
    /** Cloud Run 服务 / Cloud Run service */
    data class CloudRunService(val serviceName: String, val region: String) : GcpTargetService()
    /** GKE 集群 / GKE cluster */
    data class GkeCluster(val cluster: String, val namespace: String = "default") : GcpTargetService()
}

/**
 * GCP IAM 策略 / GCP IAM policy definition
 * @property allowedResources 允许访问的 GCP 资源 / Allowed GCP resources
 * @property quotaPerHour 每小时配额上限 / Hourly quota limit
 * @property serviceAccountEmail Service Account 邮箱 / Service account email
 */
data class GcpIamPolicy(
    val allowedResources: List<String> = emptyList(),
    val quotaPerHour: Int = 10000,
    val serviceAccountEmail: String? = null
)

/**
 * SDK 错误码 / SDK error codes
 */
object SdkErrorCodes {
    const val AGENT_001 = "AGENT_001"  // Agent 部署失败
    const val AGENT_002 = "AGENT_002"  // Agent 实例不存在
    const val AGENT_003 = "AGENT_003"  // Quota 超限
    const val AGENT_004 = "AGENT_004"  // GCP IAM 权限不足
    const val AGENT_005 = "AGENT_005"  // Workflow step 执行失败
    const val AUDIT_001 = "AUDIT_001"  // 审计日志写入失败
}
