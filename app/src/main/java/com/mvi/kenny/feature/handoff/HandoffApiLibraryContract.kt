package com.mvi.kenny.feature.handoff

/**
 * ============================================================
 * HandoffApiLibraryContract — API Library MVI 契约
 * ============================================================
 */

data class HandoffApiLibraryState(
    val selectedTab: ApiLibraryTab = ApiLibraryTab.QUICK_START,
    val quickStartCode: String = "",
    val apiUsageExamples: List<ApiExample> = emptyList(),
    val handoffConfig: HandoffConfig = HandoffConfig(),
    val copiedItem: String? = null,
    val error: String? = null
) {
    companion object { val Initial = HandoffApiLibraryState() }
}

sealed interface HandoffApiLibraryIntent {
    data class SwitchTab(val tab: ApiLibraryTab) : HandoffApiLibraryIntent
    data class CopyCode(val code: String, val label: String) : HandoffApiLibraryIntent
    data class UpdateConfig(val config: HandoffConfig) : HandoffApiLibraryIntent
    data object ClearCopyFeedback : HandoffApiLibraryIntent
}

sealed interface HandoffApiLibraryEffect {
    data class CodeCopied(val label: String) : HandoffApiLibraryEffect
    data class ShowSnackbar(val message: String) : HandoffApiLibraryEffect
}
