// ================================================================
// ViolationClassifier — 违规分类器
// Violation Classifier for Audio API Violations
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// 将检测到的音频 API 违规按严重等级、使用场景分类，
// 并生成优先级排序的建议。
//
// Classifies detected audio API violations by severity and usage,
// generating prioritized fix suggestions.
// ================================================================

package com.mvi.kenny.audiobackground.engine

/**
 * 违规使用场景分类
 * Violation Usage Context Classification
 *
 * @property displayName 显示名称
 * @property description 场景描述
 */
enum class ViolationContext(val displayName: String, val description: String) {
    /** 后台 Service 中调用 */
    IN_SERVICE("Service 中调用", "在 Service.onStartCommand 等生命周期中调用"),
    /** 后台线程/Handler 中调用 */
    IN_BACKGROUND_THREAD("后台线程调用", "在 Handler/ExecutorService/Coroutines 中调用"),
    /** BroadcastReceiver 中调用 */
    IN_BROADCAST_RECEIVER("BroadcastReceiver 中调用", "在 BroadcastReceiver.onReceive 中调用"),
    /** WorkManager 中调用 */
    IN_WORK_MANAGER("WorkManager 中调用", "在 Worker.doWork 中调用"),
    /** Application.onCreate 中调用 */
    IN_APPLICATION("Application 中调用", "在 Application.onCreate 中初始化音频"),
    /** ViewModel/Activity 中调用（正常） */
    IN_UI_LAYER("UI 层调用", "在 Activity/ViewModel 中调用（正常）"),
    /** 未知场景 */
    UNKNOWN("未知场景", "无法确定调用上下文")
}

/**
 * 修复优先级
 * Fix Priority
 *
 * @property priority 数值越小优先级越高
 */
enum class FixPriority(val priority: Int, val displayName: String) {
    P0_CRITICAL(0, "P0 - 必须立即修复"),
    P1_HIGH(1, "P1 - 高优先级"),
    P2_MEDIUM(2, "P2 - 中优先级"),
    P3_LOW(3, "P3 - 低优先级"),
    P4_INFO(4, "P4 - 仅作参考")
}

/**
 * 带优先级的违规记录
 * Violation with priority
 *
 * @param violation 原始违规记录
 * @param context 使用场景分类
 * @param priority 修复优先级
 * @param estimatedFixEffort 预估修复工作量（小时）
 */
data class PrioritizedViolation(
    val violation: AudioViolation,
    val context: ViolationContext,
    val priority: FixPriority,
    val estimatedFixEffort: Int  // 小时
)

/**
 * 违规分类器
 * Violation Classifier
 *
 * 负责：
 * 1. 分析违规的上下文场景
 * 2. 计算修复优先级
 * 3. 对违规列表排序
 */
class ViolationClassifier {

    /**
     * 分类并排序违规列表
     * Classify and sort violation list
     *
     * @param violations 原始违规列表
     * @return 按优先级排序的带优先级违规列表
     */
    fun classifyAndSort(violations: List<AudioViolation>): List<AudioViolation> {
        val prioritized = violations.map { classifySingle(it) }
        return prioritized
            .sortedWith(
                compareBy(
                    { it.priority.priority },
                    { it.violation.severity.priority }
                )
            )
            .map { it.violation }
    }

    /**
     * 分类单个违规
     * Classify single violation
     */
    private fun classifySingle(violation: AudioViolation): PrioritizedViolation {
        val context = classifyContext(violation)
        val priority = calculatePriority(violation, context)
        val effort = estimateFixEffort(violation, context)

        return PrioritizedViolation(
            violation = violation,
            context = context,
            priority = priority,
            estimatedFixEffort = effort
        )
    }

    /**
     * 分类违规使用场景
     * Classify violation usage context
     *
     * 分析调用栈和文件路径，确定违规发生的场景。
     * Analyzes call stack and file path to determine where violation occurred.
     */
    private fun classifyContext(violation: AudioViolation): ViolationContext {
        val filePath = violation.file.lowercase()
        val callStack = violation.callStack.joinToString("\n").lowercase()

        return when {
            // Service 相关
            filePath.contains("service") ||
            filePath.contains("foregroundservice") ||
            callStack.contains("onstartcommand") ||
            callStack.contains("onservicestart") -> ViolationContext.IN_SERVICE

            // 后台线程相关
            callStack.contains("handler") ||
            callStack.contains("executorservice") ||
            callStack.contains("coroutine") ||
            callStack.contains("launch") ||
            callStack.contains("async") ||
            callStack.contains("background") -> ViolationContext.IN_BACKGROUND_THREAD

            // BroadcastReceiver
            filePath.contains("broadcastreceiver") ||
            filePath.contains("receiver") ||
            callStack.contains("onreceive") -> ViolationContext.IN_BROADCAST_RECEIVER

            // WorkManager
            filePath.contains("worker") ||
            filePath.contains("workmanager") ||
            callStack.contains("dowork") -> ViolationContext.IN_WORK_MANAGER

            // Application
            filePath.contains("application") ||
            callStack.contains("oncreate") -> ViolationContext.IN_APPLICATION

            // UI Layer
            filePath.contains("activity") ||
            filePath.contains("viewmodel") ||
            filePath.contains("screen") ||
            filePath.contains("fragment") -> ViolationContext.IN_UI_LAYER

            else -> ViolationContext.UNKNOWN
        }
    }

    /**
     * 计算修复优先级
     * Calculate fix priority
     */
    private fun calculatePriority(
        violation: AudioViolation,
        context: ViolationContext
    ): FixPriority {
        // CRITICAL 严重性直接提升到 P0/P1
        if (violation.severity == AudioApiSeverity.CRITICAL) {
            return when (context) {
                ViolationContext.IN_SERVICE,
                ViolationContext.IN_WORK_MANAGER,
                ViolationContext.IN_BROADCAST_RECEIVER -> FixPriority.P0_CRITICAL

                ViolationContext.IN_BACKGROUND_THREAD,
                ViolationContext.IN_APPLICATION -> FixPriority.P1_HIGH

                else -> FixPriority.P2_MEDIUM
            }
        }

        // WARNING 等级
        if (violation.severity == AudioApiSeverity.WARNING) {
            return when (context) {
                ViolationContext.IN_SERVICE,
                ViolationContext.IN_WORK_MANAGER -> FixPriority.P1_HIGH
                ViolationContext.IN_APPLICATION -> FixPriority.P2_MEDIUM
                else -> FixPriority.P3_LOW
            }
        }

        // INFO 等级
        return FixPriority.P4_INFO
    }

    /**
     * 预估修复工作量
     * Estimate fix effort
     *
     * @return 预估小时数
     */
    private fun estimateFixEffort(
        violation: AudioViolation,
        context: ViolationContext
    ): Int {
        val baseEffort = when (violation.severity) {
            AudioApiSeverity.CRITICAL -> 4   // 至少 4 小时
            AudioApiSeverity.WARNING -> 2    // 至少 2 小时
            AudioApiSeverity.INFO -> 1       // 1 小时
        }

        val contextMultiplier = when (context) {
            ViolationContext.IN_SERVICE -> 1.5  // Service 改造更复杂
            ViolationContext.IN_WORK_MANAGER -> 1.3
            ViolationContext.IN_APPLICATION -> 1.2
            else -> 1.0
        }

        return (baseEffort * contextMultiplier).toInt()
    }

    /**
     * 生成违规报告摘要
     * Generate violation report summary
     *
     * @param violations 违规列表
     * @return 按场景分组的报告
     */
    fun generateContextReport(violations: List<AudioViolation>): Map<ViolationContext, List<AudioViolation>> {
        val classified = violations.map { violation ->
            violation to classifyContext(violation)
        }

        return classified.groupBy(
            keySelector = { it.second },
            valueTransform = { it.first }
        )
    }
}
