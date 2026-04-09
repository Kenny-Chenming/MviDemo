package com.mvi.kenny.feature.adaptive17

// =============================================================
// AdaptiveScanState — 自适应布局扫描页面状态
// =============================================================
/**
 * Adaptive layout scan state / 自适应布局扫描页面状态
 *
 * Single source of truth for the AdaptiveScan screen.
 *
 * @param isScanning Whether scan is in progress / 是否正在扫描
 * @param progress Scan progress 0.0~1.0 / 扫描进度
 * @param violations List of detected violations / 检测到的违规列表
 * @param reportUrl Path to generated HTML report / 生成的 HTML 报告路径
 * @param error Error message if any / 错误信息
 * @param scanReport Last completed scan report / 上次完成的扫描报告
 */
data class AdaptiveScanState(
    val isScanning: Boolean = false,
    val progress: Float = 0f,
    val violations: List<Violation> = emptyList(),
    val reportUrl: String? = null,
    val error: String? = null,
    val scanReport: ScanReport? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = AdaptiveScanState()
    }

    /**
     * Number of CRITICAL violations / 严重违规数
     */
    val criticalCount: Int
        get() = violations.count { it.severity == ViolationSeverity.CRITICAL }

    /**
     * Number of WARNING violations / 中等违规数
     */
    val warningCount: Int
        get() = violations.count { it.severity == ViolationSeverity.WARNING }

    /**
     * Number of LOW violations / 低级违规数
     */
    val lowCount: Int
        get() = violations.count { it.severity == ViolationSeverity.LOW }

    /**
     * Total violation count / 违规总数
     */
    val totalCount: Int
        get() = violations.size
}

// =============================================================
// TemplateState — 模板生成器状态
// =============================================================
/**
 * Template generator state / 模板生成器状态
 *
 * @param selectedActivity Selected activity name for template generation / 选择的 Activity 名称
 * @param selectedScenario Selected layout scenario / 选择的布局场景
 * @param generatedCode Generated template code / 生成的模板代码
 * @param isGenerating Whether template generation is in progress / 是否正在生成模板
 * @param templateResult Last generated template result / 上次生成的模板结果
 */
data class TemplateState(
    val selectedActivity: String = "",
    val selectedScenario: LayoutScenario = LayoutScenario.GENERIC,
    val generatedCode: String? = null,
    val isGenerating: Boolean = false,
    val templateResult: TemplateResult? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = TemplateState()
    }
}

// =============================================================
// ScanIntent — 扫描用户意图
// =============================================================
/**
 * Adaptive scan user intents / 自适应布局扫描用户意图
 */
sealed interface ScanIntent {
    /** Start scanning for adaptive layout violations / 开始扫描自适应布局违规 */
    data object StartScan : ScanIntent

    /** Export scan report as HTML / 导出 HTML 报告 */
    data object ExportHtml : ScanIntent

    /** Apply fix to a specific violation / 应用修复到特定违规
     * @param violationId ID of the violation to fix / 要修复的违规 ID
     */
    data class ApplyFix(val violationId: String) : ScanIntent

    /** Clear error message / 清除错误信息 */
    data object DismissError : ScanIntent

    /** Select a violation to view detail / 选择查看违规详情
     * @param violation The violation to view / 要查看的违规
     */
    data class SelectViolation(val violation: Violation) : ScanIntent

    /** Clear selected violation / 清除选中的违规 */
    data object ClearSelectedViolation : ScanIntent
}

// =============================================================
// TemplateIntent — 模板生成器用户意图
// =============================================================
/**
 * Template generator user intents / 模板生成器用户意图
 */
sealed interface TemplateIntent {
    /** Select a layout scenario / 选择布局场景
     * @param scenario Selected layout scenario / 选择的布局场景
     */
    data class SelectScenario(val scenario: LayoutScenario) : TemplateIntent

    /** Generate template code / 生成模板代码 */
    data object GenerateTemplate : TemplateIntent

    /** Copy generated code to clipboard / 复制生成的代码到剪贴板 */
    data object CopyToClipboard : TemplateIntent

    /** Update selected activity name / 更新选择的 Activity 名称
     * @param activityName Activity name / Activity 名称
     */
    data class UpdateActivityName(val activityName: String) : TemplateIntent
}

// =============================================================
// ScanEffect — 扫描副作用
// =============================================================
/**
 * Adaptive scan side effects / 自适应布局扫描副作用
 *
 * One-time events, consumed only once by UI layer.
 */
sealed interface ScanEffect {
    /** Show violation detail / 显示违规详情
     * @param violation The violation to show / 要显示的违规
     */
    data class ShowViolationDetail(val violation: Violation) : ScanEffect

    /** Navigate to template generator / 导航到模板生成器
     * @param activityName Activity name to pre-fill / 预填充的 Activity 名称
     */
    data class NavigateToTemplateGenerator(val activityName: String) : ScanEffect

    /** Copy fix code to clipboard / 复制修复代码到剪贴板
     * @param code The fix code to copy / 要复制的修复代码
     */
    data class CopyFixCode(val code: String) : ScanEffect

    /** Show toast message / 显示 Toast 消息
     * @param message Toast message / Toast 消息
     */
    data class ShowToast(val message: String) : ScanEffect

    /** Show error message / 显示错误消息
     * @param message Error message / 错误信息
     */
    data class ShowError(val message: String) : ScanEffect
}

// =============================================================
// TemplateEffect — 模板生成器副作用
// =============================================================
/**
 * Template generator side effects / 模板生成器副作用
 */
sealed interface TemplateEffect {
    /** Copy code to clipboard success / 复制到剪贴板成功
     * @param message Success message / 成功消息
     */
    data class CopySuccess(val message: String) : TemplateEffect

    /** Show error message / 显示错误消息
     * @param message Error message / 错误信息
     */
    data class ShowError(val message: String) : TemplateEffect

    /** Show toast message / 显示 Toast 消息
     * @param message Toast message / Toast 消息
     */
    data class ShowToast(val message: String) : TemplateEffect
}
