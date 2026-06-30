package com.gcp.agents.cli.orchestration.executor

import com.gcp.agents.cli.AgentsCliSdk
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
 */
class WorkflowExecutor(
    private val sdk: AgentsCliSdk,
    private val context: OrchestrationContext
) {
    private val _executionState = MutableStateFlow<WorkflowExecutionState?>(null)
    val executionState: StateFlow<WorkflowExecutionState?> = _executionState.asStateFlow()

    suspend fun execute(
        workflow: WorkflowDefinition,
        policy: OrchestrationPolicy = workflow.defaultPolicy
    ): WorkflowExecutionState = withContext(Dispatchers.IO) {
        require(workflow.validate()) { "Workflow validation failed: circular dependency" }

        val executionId = UUID.randomUUID().toString()
        val stepStates = mutableMapOf<String, StepStatus>()

        workflow.steps.keys.forEach { stepName ->
            stepStates[stepName] = StepStatus(stepName, StepStatus.State.PENDING, null, 0f)
        }

        var currentState = WorkflowExecutionState(
            executionId, workflow.name, WorkflowStatus.RUNNING, stepStates, System.currentTimeMillis(), null, emptyList()
        )
        _executionState.value = currentState

        val graph = workflow.toDAG()
        val parallelGroups = graph.getParallelGroups()

        for (group in parallelGroups) {
            if (currentState.status == WorkflowStatus.CANCELLED) break

            val jobs = group.map { stepName ->
                async { executeStep(workflow.steps[stepName]!!, policy, currentState) }
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
        finalState
    }

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
                val result = sdk.deploy(
                    agentName = step.agent,
                    agentSource = AgentSource.LocalSource("/tmp/${step.agent}"),
                    targetGcpService = GcpTargetService.VertexAiEndpoint("us-central1-aiplatform.googleapis.com")
                )
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
            if (retries <= policy.maxRetries) delay(1000L * retries)
        }
        updateStepState(step.name, StepStatus.State.FAILED, lastError, 1f)
        return StepExecutionResult(step.name, false, lastError)
    }

    private fun updateStepState(stepName: String, state: StepStatus.State, error: String?, progress: Float) {
        val current = _executionState.value ?: return
        _executionState.value = current.copy(stepStates = current.stepStates + (stepName to StepStatus(stepName, state, error, progress)))
    }

    fun cancel() {
        val current = _executionState.value ?: return
        _executionState.value = current.copy(status = WorkflowStatus.CANCELLED, endTime = System.currentTimeMillis())
    }
}

data class StepExecutionResult(val stepName: String, val success: Boolean, val error: String?)

data class WorkflowExecutionState(
    val executionId: String,
    val workflowName: String,
    val status: WorkflowStatus,
    val stepStates: Map<String, StepStatus>,
    val startTime: Long,
    val endTime: Long?,
    val auditLog: List<AuditLogEntry>
)

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
    enum class OperationType { DEPLOY, STOP, DELETE, UPDATE, INVOKE, QUERY_STATUS }
    enum class OperationStatus { SUCCESS, FAILURE, PARTIAL, PENDING }
}

enum class WorkflowStatus { PENDING, RUNNING, COMPLETED, FAILED, CANCELLED }

data class StepStatus(
    val stepName: String,
    val state: State,
    val error: String?,
    val progress: Float
) {
    enum class State { PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED }
}
