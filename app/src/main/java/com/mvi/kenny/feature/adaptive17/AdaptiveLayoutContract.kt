package com.mvi.kenny.feature.adaptive17

// =============================================================
// AdaptiveScanState — 自适应布局扫描页面状态
// =============================================================
data class AdaptiveScanState(
    val isScanning: Boolean = false,
    val progress: Float = 0f,
    val violations: List<Violation> = emptyList(),
    val reportUrl: String? = null,
    val error: String? = null,
    val scanReport: ScanReport? = null
) {
    companion object {
        val Initial = AdaptiveScanState()
    }

    val criticalCount: Int
        get() = violations.count { it.severity == ViolationSeverity.CRITICAL }

    val warningCount: Int
        get() = violations.count { it.severity == ViolationSeverity.WARNING }

    val lowCount: Int
        get() = violations.count { it.severity == ViolationSeverity.LOW }

    val totalCount: Int
        get() = violations.size
}

// =============================================================
// TemplateState — 模板生成器状态
// =============================================================
data class TemplateState(
    val selectedActivity: String = "",
    val selectedScenario: LayoutScenario = LayoutScenario.GENERIC,
    val generatedCode: String? = null,
    val isGenerating: Boolean = false,
    val templateResult: TemplateResult? = null
) {
    companion object {
        val Initial = TemplateState()
    }
}

// =============================================================
// ScanIntent — 扫描用户意图
// =============================================================
sealed interface ScanIntent {
    data object StartScan : ScanIntent
    data object ExportHtml : ScanIntent
    data class ApplyFix(val violationId: String) : ScanIntent
    data object DismissError : ScanIntent
    data class SelectViolation(val violation: Violation) : ScanIntent
    data object ClearSelectedViolation : ScanIntent
}

// =============================================================
// TemplateIntent — 模板生成器用户意图
// =============================================================
sealed interface TemplateIntent {
    data class SelectScenario(val scenario: LayoutScenario) : TemplateIntent
    data object GenerateTemplate : TemplateIntent
    data object CopyToClipboard : TemplateIntent
    data class UpdateActivityName(val activityName: String) : TemplateIntent
}

// =============================================================
// ScanEffect — 扫描副作用
// =============================================================
sealed interface ScanEffect {
    data class ShowViolationDetail(val violation: Violation) : ScanEffect
    data class NavigateToTemplateGenerator(val activityName: String) : ScanEffect
    data class CopyFixCode(val code: String) : ScanEffect
    data class ShowToast(val message: String) : ScanEffect
    data class ShowError(val message: String) : ScanEffect
}

// =============================================================
// TemplateEffect — 模板生成器副作用
// =============================================================
sealed interface TemplateEffect {
    data class CopySuccess(val message: String) : TemplateEffect
    data class ShowError(val message: String) : TemplateEffect
    data class ShowToast(val message: String) : TemplateEffect
}
