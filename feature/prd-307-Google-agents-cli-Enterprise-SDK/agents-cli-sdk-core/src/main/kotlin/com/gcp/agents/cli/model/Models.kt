package com.gcp.agents.cli.model

/**
 * Agent 部署请求对象 / Type-safe agent deployment request
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

data class AgentConfig(
    val version: String,
    val runtime: String,
    val resources: ResourceAllocation,
    val environmentVariables: Map<String, String>
)

data class ResourceAllocation(
    val cpu: Int,
    val memoryGb: Int,
    val diskGb: Int
)

data class DeploymentResult(
    val success: Boolean,
    val agentInstance: AgentInstance?,
    val logUrl: String?,
    val errorMessage: String?
)

data class AgentOperationResult(
    val success: Boolean,
    val output: String,
    val errorMessage: String?
)

enum class AgentDeploymentStatus {
    PENDING, DEPLOYING, RUNNING, STOPPING, STOPPED, FAILED, UPDATING
}

sealed class AgentOperationStatus {
    object Idle : AgentOperationStatus()
    data class InProgress(val operationId: String, val progress: Float) : AgentOperationStatus()
    data class Failed(val error: String) : AgentOperationStatus()
    data class Succeeded(val result: Any?) : AgentOperationStatus()
}

sealed class AgentSource {
    data class GitHubSource(val repo: String, val branch: String = "main") : AgentSource()
    data class LocalSource(val path: String) : AgentSource()
    data class GcsSource(val bucket: String, val objectKey: String) : AgentSource()
}

sealed class GcpTargetService {
    data class VertexAiEndpoint(val endpoint: String, val project: String? = null) : GcpTargetService()
    data class CloudRunService(val serviceName: String, val region: String) : GcpTargetService()
    data class GkeCluster(val cluster: String, val namespace: String = "default") : GcpTargetService()
}

data class GcpIamPolicy(
    val allowedResources: List<String> = emptyList(),
    val quotaPerHour: Int = 10000,
    val serviceAccountEmail: String? = null
)

object SdkErrorCodes {
    const val AGENT_001 = "AGENT_001"
    const val AGENT_002 = "AGENT_002"
    const val AGENT_003 = "AGENT_003"
    const val AGENT_004 = "AGENT_004"
    const val AGENT_005 = "AGENT_005"
    const val AUDIT_001 = "AUDIT_001"
}
