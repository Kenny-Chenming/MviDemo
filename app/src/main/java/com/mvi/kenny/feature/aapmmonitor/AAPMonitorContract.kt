package com.mvi.kenny.feature.aapmmonitor

/**
 * ============================================================
 * AAPMonitorContract — AAPM 状态/意图/副作用定义
 * AAPMonitorContract — AAPM State/Intent/Effect Definitions
 * ============================================================
 * PRD-053 | Android 17 AdvancedProtectionManager Accessibility API 迁移检测
 * PRD-053 | Android 17 AdvancedProtectionManager Accessibility API Migration Detection
 *
 * MVI 三要素（Three pillars of MVI）:
 * — State: 页面状态的唯一真相来源（Single source of truth），Immutable data class
 * — Intent: 用户意图（User intent），ViewModel receives Intent and executes business logic
 * — Effect: 一次性副作用（One-time side effects），delivered via Channel
 * —————————————————————————————————————————————————————
 */

// ================================================================
// Scan Status — 扫描状态
// ================================================================

/**
 * 扫描状态
 * Scan status for AAPM detection
 */
sealed class ScanStatus {
    /** 空闲状态（Idle state） */
    data object IDLE : ScanStatus()
    /** 扫描中（Currently scanning） */
    data object SCANNING : ScanStatus()
    /** 扫描完成（Scan completed successfully） */
    data object DONE : ScanStatus()
    /** 扫描出错（Scan encountered an error） */
    data object ERROR : ScanStatus()
}

// ================================================================
// Severity — 问题严重程度
// ================================================================

/**
 * 问题严重程度枚举
 * Issue severity levels
 * — P0: 阻断性问题（Critical - blocks app functionality）
 * — P1: 高风险问题（High risk - security/policy concern）
 * — P2: 低风险问题（Low risk - best practice improvement）
 */
enum class Severity {
    P0,  // 阻断性 / Critical
    P1,  // 高风险 / High risk
    P2   // 低风险 / Low risk
}

// ================================================================
// Accessibility Issue — 可访问性问题条目
// ================================================================

/**
 * 可访问性问题条目
 * Accessibility issue detected during scan
 *
 * @param id 唯一标识符（Unique identifier）
 * @param serviceName 问题服务名称（Service name causing the issue）
 * @param filePath 涉及的文件路径（File path containing the issue）
 * @param severity 严重程度（Severity level）
 * @param description 问题描述（Issue description）
 * @param suggestedFix 建议修复方案（Suggested fix）
 * @param callChain 调用链（API call chain leading to the issue）
 * @param isResolved 是否已解决（Whether the issue has been resolved）
 */
data class AccessibilityIssue(
    val id: String,
    val serviceName: String,
    val filePath: String,
    val severity: Severity,
    val description: String,
    val suggestedFix: String,
    val callChain: List<String> = emptyList(),
    val isResolved: Boolean = false
)

// ================================================================
// Filter Options — 筛选选项
// ================================================================

/**
 * 扫描结果筛选选项
 * Filter options for scan results
 *
 * @param showResolved 是否显示已解决问题（Show resolved issues）
 * @param severities 筛选的严重程度（Severity levels to include）
 * @param searchText 搜索关键词（Search text filter）
 * @param module 按模块筛选（如 "P0" / "P1" / "P2" / null 表示全部）
 */
data class FilterOptions(
    val showResolved: Boolean = false,
    val severities: Set<Severity> = Severity.entries.toSet(),
    val searchText: String = "",
    val module: String? = null
)

// ================================================================
// AAPM Status — AAPM 状态
// ================================================================

/**
 * Android Advanced Protection Mode (AAPM) 状态
 * Android Advanced Protection Mode status
 */
enum class AapmStatus {
    /** AAPM 已激活（AAPM is active on the device） */
    ACTIVE,
    /** AAPM 未激活（AAPM is not active） */
    INACTIVE,
    /** 状态未知（Status unknown） */
    UNKNOWN
}

// ================================================================
// Device AAPM Info — 设备 AAPM 信息
// ================================================================

/**
 * 设备 AAPM 信息
 * Device AAPM information
 *
 * @param sdkVersion Android SDK 版本（Android SDK version）
 * @param isRooted 设备是否 Root（Whether device is rooted）
 * @param manufacturer 设备制造商（Device manufacturer）
 * @param model 设备型号（Device model）
 */
data class DeviceAapmInfo(
    val sdkVersion: Int,
    val isRooted: Boolean,
    val manufacturer: String,
    val model: String
)

// ================================================================
// Scoped API Entry — API 条目
// ================================================================

/**
 * API 作用域条目（知识库用）
 * Scoped API entry for knowledge base
 *
 * @param name API 名称（API name）
 * @param description API 描述（API description）
 * @param alternative 替代方案（Alternative API）
 * @param applicableScenario 适用场景（Applicable scenario）
 */
data class ScopedApiEntry(
    val name: String,
    val description: String,
    val alternative: String,
    val applicableScenario: String
)

// ================================================================
// Compliance Step — 合规步骤
// ================================================================

/**
 * 合规引导步骤
 * Compliance guide step
 *
 * @param step 步骤编号（Step number, 1-4）
 * @param title 步骤标题（Step title）
 * @param description 步骤描述（Step description）
 * @param isCompleted 是否已完成（Whether step is completed）
 */
data class ComplianceStep(
    val step: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)

// ================================================================
// Scan Report — 扫描报告
// ================================================================

/**
 * 扫描报告
 * Scan report
 *
 * @param generatedAt 报告生成时间戳（Report generation timestamp）
 * @param totalIssues 总体问题数（Total number of issues）
 * @param p0Count P0 问题数（P0 issue count）
 * @param p1Count P1 问题数（P1 issue count）
 * @param p2Count P2 问题数（P2 issue count）
 * @param issues 问题列表（List of issues）
 */
data class ScanReport(
    val generatedAt: Long,
    val totalIssues: Int,
    val p0Count: Int,
    val p1Count: Int,
    val p2Count: Int,
    val issues: List<AccessibilityIssue>
)

// ================================================================
// AAPMonitorState — 主状态
// ================================================================

/**
 * AAPMonitor 主状态
 * Main state for AAPM monitoring screen
 *
 * @param scanStatus 当前扫描状态（Current scan status）
 * @param scanProgress 扫描进度 0.0-1.0（Scan progress 0.0-1.0）
 * @param scanResults 扫描结果列表（List of scan results）
 * @param selectedIssue 选中的问题（Currently selected issue）
 * @param filterOptions 筛选选项（Filter options）
 * @param isLoading 是否加载中（Whether loading）
 * @param aapmStatus AAPM 状态（AAPM status）
 * @param deviceInfo 设备信息（Device information）
 * @param complianceStep 当前合规步骤（Current compliance step, 1-4）
 * @param isBottomSheetExpanded BottomSheet 是否展开（Whether bottom sheet is expanded）
 * @param searchQuery 搜索关键词（Search query）
 * @param knowledgeEntries 知识库条目（Knowledge base entries）
 * @param error 错误信息（Error message if any）
 */
data class AAPMonitorState(
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanProgress: Float = 0f,
    val scanResults: List<AccessibilityIssue> = emptyList(),
    val selectedIssue: AccessibilityIssue? = null,
    val filterOptions: FilterOptions = FilterOptions(),
    val isLoading: Boolean = false,
    val aapmStatus: AapmStatus = AapmStatus.UNKNOWN,
    val deviceInfo: DeviceAapmInfo? = null,
    val complianceStep: Int = 1,
    val isBottomSheetExpanded: Boolean = false,
    val searchQuery: String = "",
    val knowledgeEntries: List<ScopedApiEntry> = emptyList(),
    val error: String? = null
) {
    /**
     * 根据 filterOptions 过滤后的扫描结果
     * Filtered scan results based on current filter options
     */
    val filteredResults: List<AccessibilityIssue>
        get() = scanResults.filter { issue ->
            val severityMatch = when (filterOptions.module) {
                "P0" -> issue.severity == Severity.P0
                "P1" -> issue.severity == Severity.P1
                "P2" -> issue.severity == Severity.P2
                else -> true
            }
            val resolvedMatch = filterOptions.showResolved || !issue.isResolved
            val searchMatch = filterOptions.searchText.isEmpty() ||
                issue.serviceName.contains(filterOptions.searchText, ignoreCase = true) ||
                issue.description.contains(filterOptions.searchText, ignoreCase = true)
            severityMatch && resolvedMatch && searchMatch
        }

    /** 未解决的问题数量（Number of unresolved issues） */
    val unresolvedCount: Int get() = scanResults.count { !it.isResolved }

    /** P0 阻断性问题数量（P0 critical issue count） */
    val p0Count: Int get() = scanResults.count { it.severity == Severity.P0 }

    /** P1 高风险问题数量（P1 high risk issue count） */
    val p1Count: Int get() = scanResults.count { it.severity == Severity.P1 }

    /** P2 低风险问题数量（P2 low risk issue count） */
    val p2Count: Int get() = scanResults.count { it.severity == Severity.P2 }
}

// ================================================================
// AAPMonitorIntent — 用户意图
// ================================================================

/**
 * AAPMonitor 用户意图
 * User intents for AAPM monitoring
 */
sealed interface AAPMonitorIntent {
    /** 开始扫描（Start the AAPM scan） */
    data object StartScan : AAPMonitorIntent
    /** 取消扫描（Cancel ongoing scan） */
    data object CancelScan : AAPMonitorIntent
    /** 选中问题（Select an issue for detail view） */
    data class SelectIssue(val issue: AccessibilityIssue) : AAPMonitorIntent
    /** 设置筛选选项（Set filter options） */
    data class SetFilter(val options: FilterOptions) : AAPMonitorIntent
    /** 刷新 AAPM 状态（Refresh AAPM status） */
    data object RefreshAapmStatus : AAPMonitorIntent
    /** 标记问题已解决（Mark an issue as resolved） */
    data class MarkIssueResolved(val issue: AccessibilityIssue) : AAPMonitorIntent
    /** 设置合规步骤（Set compliance guide step） */
    data class SetComplianceStep(val step: Int) : AAPMonitorIntent
    /** 导出报告（Export scan report） */
    data object ExportReport : AAPMonitorIntent
    /** 设置 BottomSheet 展开状态（Set bottom sheet expanded state） */
    data class SetBottomSheetExpanded(val expanded: Boolean) : AAPMonitorIntent
    /** 设置搜索关键词（Set search query） */
    data class SetSearchQuery(val query: String) : AAPMonitorIntent
    /** 加载知识库（Load knowledge base entries） */
    data object LoadKnowledgeBase : AAPMonitorIntent
    /** 关闭错误提示（Dismiss error message） */
    data object DismissError : AAPMonitorIntent
}

// ================================================================
// AAPMonitorEffect — 副作用
// ================================================================

/**
 * AAPMonitor 副作用
 * One-time side effects for AAPM monitoring
 */
sealed interface AAPMonitorEffect {
    /** 显示 Toast 消息（Show toast message） */
    data class ShowToast(val message: String) : AAPMonitorEffect
    /** 扫描完成（Scan completed successfully） */
    data object ScanCompleted : AAPMonitorEffect
    /** 触发触觉反馈（Trigger haptic feedback） */
    data object TriggerHapticFeedback : AAPMonitorEffect
    /** 显示错误（Show error message） */
    data class ShowError(val message: String) : AAPMonitorEffect
    /** 导航到详情页（Navigate to issue detail page） */
    data class NavigateToDetail(val issue: AccessibilityIssue) : AAPMonitorEffect
    /** 分享报告（Share generated report） */
    data class ShareReport(val filePath: String) : AAPMonitorEffect
}
