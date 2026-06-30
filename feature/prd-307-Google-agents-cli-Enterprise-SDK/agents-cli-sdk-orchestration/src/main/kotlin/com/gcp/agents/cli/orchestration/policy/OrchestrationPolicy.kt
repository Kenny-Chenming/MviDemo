package com.gcp.agents.cli.orchestration.policy

/**
 * OrchestrationPolicy — 部署策略定义
 * Deployment policy definition for orchestration.
 *
 * @property parallelism 并行度（0 = 无限制）/ Parallelism (0 = unlimited)
 * @property failureStrategy 失败策略 / Failure strategy
 * @property maxRetries 最大重试次数 / Max retry attempts
 * @property continueOnFailure 失败后是否继续 / Continue on failure
 */
data class OrchestrationPolicy(
    val parallelism: Int = 1,
    val failureStrategy: FailureStrategy = FailureStrategy.STOP,
    val maxRetries: Int = 0,
    val continueOnFailure: Boolean = false,
    val timeoutMinutes: Long = 60
) {
    /**
     * 失败策略 / Failure strategy
     */
    enum class FailureStrategy {
        /** 立即停止 / Stop immediately */
        STOP,
        /** 等待后重试 / Wait and retry */
        RETRY,
        /** 降级到备用 Agent / Fallback to alternative agent */
        FALLBACK
    }

    companion object {
        /** 默认策略 / Default policy */
        val DEFAULT = OrchestrationPolicy()

        /** 并行策略 / Parallel policy */
        val PARALLEL = OrchestrationPolicy(parallelism = 0)

        /** 串行策略 / Sequential policy */
        val SEQUENTIAL = OrchestrationPolicy(parallelism = 1)
    }
}
