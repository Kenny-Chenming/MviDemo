package com.gcp.agents.cli.orchestration.executor

import com.gcp.agents.cli.model.*
import com.gcp.agents.cli.orchestration.context.OrchestrationContext
import com.gcp.agents.cli.orchestration.policy.OrchestrationPolicy
import com.gcp.agents.cli.orchestration.workflow.WorkflowDefinition
import com.gcp.agents.cli.orchestration.workflow.WorkflowGraph
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * WorkflowExecutor — 执行工作流，跟踪每个 step 的状态
 * Executes workflow and tracks each step's status.
 *
 * @param sdk SDK 实例 / SDK instance
 * @param context 编排上下文 / Orchestration context
 */
class WorkflowExecutor(
    private val sdk: com.gcp.agents.cli.AgentsCliSdk,
    private val context: OrchestrationContext
) {
    private val _executionState = MutableStateFlow<WorkflowExecutionState?>(null)
    val executionState: StateFlow<WorkflowExecutionState?> = _executionState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 执行工作流 / Execute workflow
     * @param workflow 工作流定义 / Workflow definition
     * @param policy 编排策略 / Orchestration policy
     * @return 工作流执行状态 / Workflow execution state
     */
    suspend fun execute(
        workflow: WorkflowDefinition,
        policy: OrchestrationPolicy = workflow.defaultPolicy
    ): WorkflowExecutionState = withContext(Dispatchers.IO) {
        require(workflow.validate()) { "Workflow validation failed: circular dependency detected" }

        val executionId = UUID.randomUUID().toString()
        val stepStates = mutableMapOf<String, StepStatus>()

        workflow.steps.keys.forEach { stepName ->
            stepStates[stepName] = StepStatus(stepName, StepStatus.State.PENDING, null, 0f)
        }

        var currentState = WorkflowExecutionState(
            executionId = executionId,
            workflowName = workflow.name,
            status = WorkflowStatus.RUNNING,
            stepStates = stepStates,
            startTime = System.currentTimeMillis(),
            auditLog = emptyList()
        )
        _executionState.value = currentState

        val graph = workflow.toDAG()
        val parallelGroups = graph.getParallelGroups()

        for (group in parallelGroups) {
            if (currentState.status == WorkflowStatus.CANCELLED) break

            val jobs = group.map { stepName ->
                async {
                    executeStep(workflow.steps[stepNameName]!!, policy, currentState)
                }
            }

            val results = jobs.awaitAll()
            val failedSteps = results.filter { !it.success }

            if (failedSteps.isNotEmpty() && !policy.continueOnFailure) {
                currentState = currentState.copy(
                    status = WorkflowStatus.FAILED,
                    stepStates = currentState.stepStates + results.associate {
                        it.stepName to StepStatus(it.stepName, StepStatus.State.FAILED, it.error, 1f)
                    }
                )
                _executionState.value = currentState
                break
            }

            currentState = currentState.copy(
                status = if (failedSteps.isEmpty()) WorkflowStatus.RUNNING else WorkflowStatus.FAILED,
                stepStates = currentState.stepStates + results.associate {
                    it.stepName to StepStatus(it.stepName, if (it.success) StepStatus.State.COMPLETED else StepStatus.State.FAILED, it.error, 1f)
                }
            )
            _executionState.value = currentState
        }

        val finalState = if (currentState.status == WorkflowStatus.RUNNING) {
            currentState.copy(status = WorkflowStatus.COMPLETED, endTime = System.currentTimeMillis())
        } else {
            currentState.copy(endTime = System.currentTimeMillis())
        }
        _executionState.value = finalState
        return@withContext finalState
    }

    private var stepNameName: String = ""

    /**
     * 执行单个步骤 / Execute single step
     */
    private suspend fun executeStep(
        step: com.gcp.agents.cli.orchestration.workflow.WorkflowStep,
        policy: OrchestrationPolicy,
        currentState: WorkflowExecutionState
    ): StepExecutionResult {
        updateStepState(step.name, StepStatus.State.IN_PROGRESS, null, 0.5f)

        var retries = 0
        var lastError: String? = null

        while (retries <= policy.maxRetries) {
            try {
                val agent = context.getAgent(step.agent)
                    ?: throw IllegalStateException("Agent '${step.agent}' not found in context")

                // Simulate step execution (实际调用 SDK 部署/操作 Agent)
                val result = sdk.deploy {
                    this.agentName = step.agent
                    this.agentSource = AgentSource.LocalSource("/tmp/${step.agent}")
                    this.targetGcpService = GcpTargetService.VertexAiEndpoint("us-central1-aiplatform.googleapis.com")
                }

                if (result.success) {
                    updateStepState(step.name, StepStatus.State.COMPLETED, null, 1f)
                    return StepExecutionResult(step.name, true, null)
                } else {
                    lastError = result.errorMessage
                    retries++
                }
            } catch (e: Exception) {
                lastError = e.message
                retries++
            }

            if (retries <= policy.maxRetries) {
                delay(1000L * retries) // Exponential backoff
            }
        }

        updateStepState(step.name, StepStatus.State.FAILED, lastError, 1f)
        return StepExecutionResult(step.name, false, lastError)
    }

    private fun updateStepState(stepName: String, state: StepStatus.State, error: String?, progress: Float) {
        val current = _executionState.value ?: return
        _executionState.value = current.copy(
            stepStates = current.stepStates + (stepName to StepStatus(stepName, state, error, progress))
        )
    }

    /**
     * 取消工作流执行 / Cancel workflow execution
     */
    fun cancel() {
        val current = _executionState.value ?: return
        _executionState.value = current.copy(status = WorkflowStatus.CANCELLED, endTime = System.currentTimeMillis())
    }
}

/**
 * StepExecutionResult — 步骤执行结果 / Step execution result
 */
data class StepExecutionResult(
    val stepName: String,
    val success: Boolean,
    val error: String?
)

/**
 * WorkflowExecutionState — 工作流执行状态
 * Workflow execution state.
 *
 * @property executionId 执行 ID / Execution ID
 * @property workflowName 工作流名称 / Workflow name
 * @property status 工作流状态 / Workflow status
 * @property stepStates 各步骤状态 / Step states
 * @property startTime 开始时间 / Start time
 * @property endTime 结束时间 / End time
 * @property auditLog 审计日志 / Audit log
 */
data class WorkflowExecutionState(
    val executionId: String,
    val workflowName: String,
    val status: WorkflowStatus,
    val stepStates: Map<String, StepStatus>,
    val startTime: Long,
    val endTime: Long? = null,
    val auditLog: List<AuditLogEntry>
)

/**
 * WorkflowStatus — 工作流状态枚举
 */
enum class WorkflowStatus {
    PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
}

/**
 * StepStatus — 步骤状态
 *
 * @property stepName 步骤名称 / Step name
 * @property state 状态 / State
 * @property error 错误信息 / Error message
 * @property progress 进度（0.0 - 1.0）/ Progress (0.0 - 1.0)
 */
data class StepStatus(
    val stepName: String,
    val state: State,
    val error: String?,
    val progress: Float
) {
    enum class State {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED
    }
}

/**
 * AuditLogEntry — 审计日志条目（来自 audit 模块）
 * Re-exported from audit module.
 */
typealias AuditLogEntry = com.gcp.agents.cli.audit.log.AuditLogEntry
