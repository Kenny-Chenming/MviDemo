package com.mvi.kenny.feature.aapmmonitor

/**
 * ============================================================
 * AAPMonitorContract.kt — MVI 合约定义
 * AAPMonitorContract.kt — MVI Contract Definition
 * ============================================================
 * 定义 AAPMonitor 模块的 State、Intent、Effect
 * Defines State, Intent, and Effect for AAPMonitor module
 */

// ============================================================
// AAPMonitorState — 界面状态
// AAPMonitorState — UI State
// ============================================================

/**
 * AAPMonitor 主状态
 * AAPMonitor main state
 *
 * @property scanStatus 扫描状态（IDLE/SCANNING/DONE/ERROR）
 * @property scanResults 扫描结果列表
 * @property filterOptions 筛选选项
 * @property aapmStatus AAPM 激活状态
 * @property selectedIssue 选中的问题项
 * @property complianceStep 合规步骤（1-4）
 * @property knowledgeEntries 知识库条目列表
 * @property isLoading 是否正在加载
 * @property error 错误信息
 * @property scanProgress 扫描进度（0.0-1.0）
 * @property deviceInfo 设备信息
 * @property isBottomSheetExpanded BottomSheet 是否展开
 * @property searchQuery 知识库搜索关键词
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
    /**
     * 过滤后的问题列表
     * Filtered issue list based on current filter options
     */
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

    /**
     * 按严重程度分组的问题
     * Issues grouped by severity
     */
    val groupedBySeverity: Map<Severity, List<AccessibilityIssue>>
        get() = filteredResults.groupBy { it.severity }

    /**
     * P0 问题数
     */
    val p0Count: Int get() = scanResults.count { it.severity == Severity.P0 && !it.isResolved }

    /**
     * P1 问题数
     */
    val p1Count: Int get() = scanResults.count { it.severity == Severity.P1 && !it.isResolved }

    /**
     * P2 问题数
     */
    val p2Count: Int get() = scanResults.count { it.severity == Severity.P2 && !it.isResolved }

    /**
     * 未解决的问题数
     */
    val unresolvedCount: Int get() = scanResults.count { !it.isResolved }

    /**
     * 已解决的问题数
     */
    val resolvedCount: Int get() = scanResults.count { it.isResolved }
}

// ============================================================
// AAPMonitorIntent — 用户意图（用户动作）
// AAPMonitorIntent — User Intents (User Actions)
// ============================================================

/**
 * AAPMonitor 用户意图 sealed class
 * AAPMonitor user intent sealed class
 */
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

// ============================================================
// AAPMonitorEffect — 副作用（一次性事件）
// AAPMonitorEffect — Side Effects (One-time Events)
// ============================================================

/**
 * AAPMonitor 副作用 sealed class
 * AAPMonitor side effects sealed class
 */
sealed class AAPMonitorEffect {
    data class NavigateToDetail(val issue: AccessibilityIssue) : AAPMonitorEffect()
    object ScanCompleted : AAPMonitorEffect()
    data class ShowError(val message: String) : AAPMonitorEffect()
    data class ShareReport(val path: String) : AAPMonitorEffect()
    data class ShowToast(val message: String) : AAPMonitorEffect()
    object TriggerHapticFeedback : AAPMonitorEffect()
}
