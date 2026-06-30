package com.gcp.agents.cli

import com.gcp.agents.cli.auth.GcpAuthenticator
import com.gcp.agents.cli.executor.AgentsCliProcessExecutor
import com.gcp.agents.cli.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AgentsCliSdk — Google agents-cli 企业级 SDK 主入口
 * Main entry point for Google agents-cli Enterprise SDK.
 *
 * @property projectId GCP 项目 ID / GCP project ID
 * @property region GCP 区域 / GCP region
 * @property authenticator GCP 认证器 / GCP authenticator
 *
 * 用法示例 / Usage example:
 * ```
 * val sdk = AgentsCliSdk.init {
 *     projectId = "my-gcp-project"
 *     credentialsPath = "/path/to/service-account.json"
 * }
 * val result = sdk.deploy(
 *     agentName = "my-android-agent",
 *     agentSource = GitHubSource("owner/repo", "main"),
 *     targetGcpService = VertexAiEndpoint("us-central1-aiplatform.googleapis.com")
 * )
 * ```
 */
class AgentsCliSdk private constructor(
    private val projectId: String,
    private val region: String,
    private val authenticator: GcpAuthenticator,
    private val agentsCliPath: String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val executor = AgentsCliProcessExecutor(agentsCliPath)

    // SDK 状态流 / SDK state flow
    private val _sdkState = MutableStateFlow(SdkConfigState(projectId, region, CredentialsState.Initialized, 10000, true, emptyList()))
    val sdkState: StateFlow<SdkConfigState> = _sdkState.asStateFlow()

    // SDK 通知事件流 / SDK notification event flow
    private val _notificationEvents = MutableStateFlow<List<NotificationEvent>>(emptyList())
    val notificationEvents: StateFlow<List<NotificationEvent>> = _notificationEvents.asStateFlow()

    /**
     * 部署单个 Agent / Deploy a single agent
     * @param agentName Agent 名称 / Agent name
     * @param agentSource Agent 源代码来源 / Agent source location
     * @param targetGcpService 目标 GCP 服务 / Target GCP service
     * @param iamPolicy IAM 策略（可选）/ IAM policy (optional)
     * @param labels 标签映射（可选）/ Label key-value pairs (optional)
     * @return 部署结果 / Deployment result
     */
    suspend fun deploy(
        agentName: String,
        agentSource: AgentSource,
        targetGcpService: GcpTargetService,
        iamPolicy: GcpIamPolicy? = null,
        labels: Map<String, String> = emptyMap()
    ): DeploymentResult {
        val request = AgentDeploymentRequest(agentName, agentSource, targetGcpService, iamPolicy, labels)
        val result = executor.deploy(request)
        if (result.success) {
            emitNotification(NotificationEvent.DeploymentSucceeded(agentName, result.agentInstance?.endpoint ?: ""))
        } else {
            emitNotification(NotificationEvent.DeploymentFailed(agentName, result.errorMessage ?: "Unknown error"))
        }
        return result
    }

    /**
     * 停止 Agent / Stop an agent
     * @param agentName Agent 名称 / Agent name
     * @param instanceId 实例 ID / Instance ID
     */
    suspend fun stop(agentName: String, instanceId: String): AgentOperationResult {
        return executor.stop(agentName, instanceId)
    }

    /**
     * 查询 Agent 状态 / Query agent status
     * @param agentName Agent 名称 / Agent name
     */
    suspend fun getStatus(agentName: String): AgentDeploymentStatus {
        return executor.getStatus(agentName)
    }

    /**
     * 删除 Agent / Delete an agent
     * @param agentName Agent 名称 / Agent name
     * @param instanceId 实例 ID / Instance ID
     */
    suspend fun delete(agentName: String, instanceId: String): AgentOperationResult {
        return executor.delete(agentName, instanceId)
    }

    /**
     * 列出所有 Agent / List all agents
     */
    suspend fun listAgents(): List<AgentInstance> {
        return executor.listAgents()
    }

    /**
     * 更新 SDK 配置 / Update SDK configuration
     * @param config 更新后的配置 / Updated configuration
     */
    fun updateConfig(config: SdkConfigState) {
        _sdkState.value = config
    }

    /**
     * 发送通知事件 / Emit notification event
     */
    private fun emitNotification(event: NotificationEvent) {
        _notificationEvents.value = _notificationEvents.value + event
    }

    /**
     * SDK 配置状态 / SDK configuration state
     */
    data class SdkConfigState(
        val projectId: String,
        val region: String,
        val credentials: CredentialsState,
        val defaultQuota: Int,
        val auditEnabled: Boolean,
        val notificationChannels: List<NotificationChannel>
    )

    /**
     * 凭证状态 / Credentials state
     */
    sealed class CredentialsState {
        object NotInitialized : CredentialsState()
        object Initialized : CredentialsState()
        data class Error(val message: String) : CredentialsState()
    }

    /**
     * 通知渠道 / Notification channel
     * @property type 渠道类型 / Channel type
     * @property config 渠道配置 / Channel configuration
     */
    data class NotificationChannel(
        val type: ChannelType,
        val config: Map<String, String>
    )

    enum class ChannelType {
        PUBSUB, SLACK, FEISHU, DINGTALK, EMAIL
    }

    /**
     * SDK 初始化器 / SDK initializer
     */
    class Initializer {
        private var projectId: String = ""
        private var region: String = "us-central1"
        private var credentialsPath: String? = null
        private var agentsCliPath: String = "agents-cli"
        private var authenticator: GcpAuthenticator? = null

        fun projectId(projectId: String) = apply { this.projectId = projectId }
        fun region(region: String) = apply { this.region = region }
        fun credentialsPath(path: String) = apply { this.credentialsPath = path }
        fun agentsCliPath(path: String) = apply { this.agentsCliPath = path }
        fun authenticator(auth: GcpAuthenticator) = apply { this.authenticator = auth }

        fun build(): AgentsCliSdk {
            val auth = authenticator ?: GcpAuthenticator.create().apply {
                when {
                    credentialsPath != null -> withKeyFile(credentialsPath!!)
                    else -> withADC()
                }
            }
            return AgentsCliSdk(projectId, region, auth, agentsCliPath)
        }
    }

    companion object {
        /**
         * 初始化 SDK / Initialize SDK
         * @param block 初始化配置块 / Initialization configuration block
         * @return AgentsCliSdk 实例 / AgentsCliSdk instance
         */
        fun init(block: Initializer.() -> Unit): AgentsCliSdk {
            return Initializer().apply(block).build()
        }
    }
}

/**
 * 通知事件 sealed class / Notification event
 */
sealed class NotificationEvent {
    data class DeploymentSucceeded(val agentName: String, val endpoint: String) : NotificationEvent()
    data class DeploymentFailed(val agentName: String, val reason: String) : NotificationEvent()
    data class QuotaThresholdReached(val agentName: String, val used: Int, val limit: Int) : NotificationEvent()
    data class CostAlert(val amount: Double, val currency: String) : NotificationEvent()
}
