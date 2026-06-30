package com.gcp.agents.cli.orchestration.policy

/**
 * OrchestrationPolicy — 部署策略定义
 */
data class OrchestrationPolicy(
    val parallelism: Int = 1,
    val failureStrategy: FailureStrategy = FailureStrategy.STOP,
    val maxRetries: Int = 0,
    val continueOnFailure: Boolean = false,
    val timeoutMinutes: Long = 60
) {
    enum class FailureStrategy { STOP, RETRY, FALLBACK }

    companion object {
        val DEFAULT = OrchestrationPolicy()
        val PARALLEL = OrchestrationPolicy(parallelism = 0)
        val SEQUENTIAL = OrchestrationPolicy(parallelism = 1)
    }
}
