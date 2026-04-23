package com.mvi.kenny.feature.aapmmonitor

/**
 * ============================================================
 * AAPMonitorContract.kt — MVI 合约定义
 * ============================================================
 */

data class AAPMonitorState(
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanResults: List<AccessibilityIssue> = emptyList(),
    val filterOptions: FilterOptions = FilterOptions(),
    val aapmStatus: AapmStatus = AapmStatus.UNKNOWN,
    val selectedIssue: AccessibilityIssue? = null,
    val complianceStep: Int = 1,
    val knowledgeEntries: List<ScopedApiEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val scanProgress: Float = 0f,
    val deviceInfo: DeviceAapmInfo? = null,
    val isBottomSheetExpanded: Boolean = false,
    val searchQuery: String = ""
) {
    val filteredResults: List<AccessibilityIssue>
        get() = scanResults.filter { issue ->
            val moduleMatch = filterOptions.module?.let {
                issue.filePath.contains(it, ignoreCase = true)
            } ?: true
            val apiTypeMatch = filterOptions.apiType?.let {
                issue.description.contains(it, ignoreCase = true)
            } ?: true
            val filePathMatch = filterOptions.filePath?.let {
                issue.filePath.contains(it, ignoreCase = true)
            } ?: true
            moduleMatch && apiTypeMatch && filePathMatch
        }

    val p0Count: Int get() = scanResults.count { it.severity == Severity.P0 && !it.isResolved }
    val p1Count: Int get() = scanResults.count { it.severity == Severity.P1 && !it.isResolved }
    val p2Count: Int get() = scanResults.count { it.severity == Severity.P2 && !it.isResolved }
    val unresolvedCount: Int get() = scanResults.count { !it.isResolved }
    val resolvedCount: Int get() = scanResults.count { it.isResolved }
}

sealed class AAPMonitorIntent {
    object StartScan : AAPMonitorIntent()
    object CancelScan : AAPMonitorIntent()
    data class SelectIssue(val issue: AccessibilityIssue) : AAPMonitorIntent()
    data class SetFilter(val options: FilterOptions) : AAPMonitorIntent()
    object RefreshAapmStatus : AAPMonitorIntent()
    data class MarkIssueResolved(val issue: AccessibilityIssue) : AAPMonitorIntent()
    data class SetComplianceStep(val step: Int) : AAPMonitorIntent()
    object ExportReport : AAPMonitorIntent()
    data class SetBottomSheetExpanded(val expanded: Boolean) : AAPMonitorIntent()
    data class SetSearchQuery(val query: String) : AAPMonitorIntent()
    object LoadKnowledgeBase : AAPMonitorIntent()
    object DismissError : AAPMonitorIntent()
}

sealed class AAPMonitorEffect {
    data class NavigateToDetail(val issue: AccessibilityIssue) : AAPMonitorEffect()
    object ScanCompleted : AAPMonitorEffect()
    data class ShowError(val message: String) : AAPMonitorEffect()
    data class ShareReport(val path: String) : AAPMonitorEffect()
    data class ShowToast(val message: String) : AAPMonitorEffect()
    object TriggerHapticFeedback : AAPMonitorEffect()
}
