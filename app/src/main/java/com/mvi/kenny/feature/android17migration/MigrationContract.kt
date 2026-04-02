package com.mvi.kenny.feature.android17migration

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * MigrationContract — Android 17 迁移助手 MVI 契约
 * ============================================================
 * MVI (Model-View-Intent) Architecture Pattern.
 *
 * Three pillars:
 * - Model (State): Immutable data class, single source of truth for UI
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions (user actions), ViewModel processes and updates State
 *
 * Effect: One-time side effects (navigation, toast), delivered via Channel
 *
 * @see MigrationViewModel State management
 * @see MigrationDashboardScreen Main dashboard
 * @see IssueDetailScreen Issue detail page
 * @see WizardScreen Fix wizard
 */

// =============================================================
// Severity — 问题严重程度枚举
// =============================================================
/**
 * Issue severity level for Android 17 migration issues.
 * 迁移问题严重程度等级
 *
 * @param label Display label / 显示标签
 * @param color Severity color (Compose Color) / 严重程度颜色
 */
enum class Severity(
    val label: String,
    val labelZh: String,
    val color: Color
) {
    /** Critical: Breaking change, must fix before Android 17 / 重大变更，必须修复 */
    CRITICAL("Critical", "严重", Color(0xFFE53935)),

    /** Warning: Deprecated API, recommended to fix / API 已废弃，建议修复 */
    WARNING("Warning", "警告", Color(0xFFFB8C00)),

    /** Info: Informational, no immediate action needed / 信息性，暂不需处理 */
    INFO("Info", "提示", Color(0xFF42A5F5))
}

// =============================================================
// ExportFormat — 报告导出格式
// =============================================================
/** Report export format / 报告导出格式 */
enum class ExportFormat {
    MARKDOWN,
    PDF
}

// =============================================================
// ScanStatus — 扫描状态
// =============================================================
/**
 * Project scan status / 项目扫描状态
 *
 * @property label Display label / 显示标签
 */
enum class ScanStatus(val label: String) {
    IDLE("待命"),
    SCANNING("扫描中"),
    DONE("完成"),
    ERROR("错误")
}

// =============================================================
// MigrationIssue — 迁移问题数据模型
// =============================================================
/**
 * Android 17 migration issue / Android 17 迁移问题
 *
 * Represents a single incompatibility issue found during project scan.
 * 描述在项目扫描中发现的不兼容问题。
 *
 * @param id Unique issue ID / 唯一问题ID
 * @param title Issue title (English) / 问题标题（英文）
 * @param titleZh Issue title (Chinese) / 问题标题（中文）
 * @param description Issue description / 问题描述
 * @param descriptionZh Issue description (Chinese) / 问题描述（中文）
 * @param severity Issue severity / 严重程度
 * @param affectedFiles Number of affected files / 受影响文件数
 * @param affectedMethods Number of affected methods / 受影响方法数
 * @param filePaths List of affected file paths / 受影响文件路径列表
 * @param fixSuggestion Fix suggestion code snippet / 修复建议代码片段
 * @param isFixed Whether this issue has been fixed / 是否已修复
 * @param isIgnored Whether this issue is marked as ignored / 是否标记为忽略
 * @param wizardSteps Number of wizard steps to fix / 修复所需向导步骤数
 */
data class MigrationIssue(
    val id: String,
    val title: String,
    val titleZh: String,
    val description: String,
    val descriptionZh: String,
    val severity: Severity,
    val affectedFiles: Int,
    val affectedMethods: Int,
    val filePaths: List<String>,
    val fixSuggestion: String,
    val isFixed: Boolean = false,
    val isIgnored: Boolean = false,
    val wizardSteps: Int = 1
)

// =============================================================
// MigrationState — 页面状态
// =============================================================
/**
 * Migration Dashboard State / 迁移助手页面状态
 *
 * This is the single source of truth for the entire migration UI.
 * All UI state is derived from this data class.
 *
 * @param selectedProjectPath Selected project path, null if none / 已选项目路径
 * @param scanStatus Current scan status / 当前扫描状态
 * @param scanProgress Scan progress 0.0~1.0 / 扫描进度
 * @param scannedFilesCount Number of files scanned so far / 已扫描文件数
 * @param totalFilesCount Total number of files to scan / 待扫描文件总数
 * @param totalIssues Total issues found / 发现问题总数
 * @param criticalCount Critical issues count / 严重问题数
 * @param warningCount Warning issues count / 警告问题数
 * @param infoCount Info issues count / 提示问题数
 * @param fixedCount Fixed issues count / 已修复问题数
 * @param ignoredCount Ignored issues count / 已忽略问题数
 * @param issues List of all migration issues / 所有迁移问题列表
 * @param filteredIssues Issues after applying filter / 过滤后的问题列表
 * @param currentFilter Current severity filter, null means all / 当前严重程度过滤
 * @param selectedIssueId Selected issue for detail view / 选中查看详情的问题ID
 * @param currentWizardStep Current wizard step (0-indexed) / 当前向导步骤
 * @param isExporting Whether report is being exported / 是否正在导出报告
 * @param exportFormat Current export format selection / 当前导出格式
 * @param error Error message if any / 错误信息
 */
data class MigrationState(
    val selectedProjectPath: String? = null,
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanProgress: Float = 0f,
    val scannedFilesCount: Int = 0,
    val totalFilesCount: Int = 0,
    val totalIssues: Int = 0,
    val criticalCount: Int = 0,
    val warningCount: Int = 0,
    val infoCount: Int = 0,
    val fixedCount: Int = 0,
    val ignoredCount: Int = 0,
    val issues: List<MigrationIssue> = emptyList(),
    val filteredIssues: List<MigrationIssue> = emptyList(),
    val currentFilter: Severity? = null,
    val selectedIssueId: String? = null,
    val currentWizardStep: Int = 0,
    val isExporting: Boolean = false,
    val exportFormat: ExportFormat = ExportFormat.MARKDOWN,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = MigrationState()
    }

    /**
     * Overall migration progress (0.0~1.0) / 总体迁移进度
     */
    val migrationProgress: Float
        get() = if (totalIssues == 0) 0f
                else (fixedCount.toFloat() / totalIssues).coerceIn(0f, 1f)

    /**
     * Progress percentage string / 进度百分比字符串
     */
    val progressPercent: String
        get() = "${(migrationProgress * 100).toInt()}%"

    /**
     * Pending issues count (not fixed, not ignored) / 待处理问题数
     */
    val pendingCount: Int
        get() = totalIssues - fixedCount - ignoredCount
}

// =============================================================
// MigrationIntent — 用户意图
// =============================================================
/**
 * Migration Dashboard User Intents / 迁移助手用户意图
 *
 * Every user action in the UI corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 *
 * @see MigrationViewModel.sendIntent Process all intents
 */
sealed interface MigrationIntent {
    /** Select project folder / 选择项目文件夹
     * @param path Project root path / 项目根路径
     */
    data class SelectProject(val path: String) : MigrationIntent

    /** Start project scan / 开始项目扫描 */
    data object StartScan : MigrationIntent

    /** Cancel ongoing scan / 取消正在进行的扫描 */
    data object CancelScan : MigrationIntent

    /** Fix a specific issue / 修复指定问题
     * @param issueId Issue ID to fix / 待修复问题ID
     */
    data class FixIssue(val issueId: String) : MigrationIntent

    /** Run auto-fix for all applicable issues / 自动修复所有适用问题 */
    data object RunAutoFix : MigrationIntent

    /** Ignore a specific issue / 忽略指定问题
     * @param issueId Issue ID to ignore / 待忽略问题ID
     */
    data class IgnoreIssue(val issueId: String) : MigrationIntent

    /** Un-ignore an issue / 取消忽略问题
     * @param issueId Issue ID to un-ignore / 取消忽略的问题ID
     */
    data class UnignoreIssue(val issueId: String) : MigrationIntent

    /** Export migration report / 导出迁移报告
     * @param format Export format (Markdown/PDF) / 导出格式
     */
    data class ExportReport(val format: ExportFormat) : MigrationIntent

    /** Set severity filter / 设置严重程度过滤
     * @param severity Severity to filter, null means show all / 过滤的严重程度，null表示显示全部
     */
    data class SetSeverityFilter(val severity: Severity?) : MigrationIntent

    /** Select an issue to view detail / 选择查看详情的问题
     * @param issueId Issue ID / 问题ID
     */
    data class SelectIssue(val issueId: String) : MigrationIntent

    /** Clear selected issue (go back from detail) / 清除选中问题（从详情页返回） */
    data object ClearSelectedIssue : MigrationIntent

    /** Navigate to wizard to fix an issue / 进入向导修复问题
     * @param issueId Issue ID to fix / 待修复问题ID
     */
    data class NavigateToWizard(val issueId: String) : MigrationIntent

    /** Move to next wizard step / 进入下一步向导
     * @param currentStep Current step index / 当前步骤索引
     */
    data class NextWizardStep(val currentStep: Int) : MigrationIntent

    /** Move to previous wizard step / 返回上一步向导
     * @param currentStep Current step index / 当前步骤索引
     */
    data class PrevWizardStep(val currentStep: Int) : MigrationIntent

    /** Complete wizard and apply fix / 完成向导并应用修复
     * @param issueId Issue ID / 问题ID
     */
    data class CompleteWizard(val issueId: String) : MigrationIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : MigrationIntent
}

// =============================================================
// MigrationEffect — 副作用
// =============================================================
/**
 * Migration Dashboard Side Effects / 迁移助手副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see MigrationViewModel Send via _effect.send()
 */
sealed interface MigrationEffect {
    /** Show toast message / 显示Toast消息
     * @param message Toast text / Toast文本
     */
    data class ShowToast(val message: String) : MigrationEffect

    /** Show error message / 显示错误消息
     * @param message Error description / 错误描述
     */
    data class ShowError(val message: String) : MigrationEffect

    /** Share exported file / 分享导出文件
     * @param filePath Exported file path / 导出文件路径
     */
    data class ShareFile(val filePath: String) : MigrationEffect

    /** Scan complete notification / 扫描完成通知 */
    data object ScanComplete : MigrationEffect

    /** Navigate to wizard page / 导航到向导页
     * @param issueId Issue ID to fix / 待修复问题ID
     */
    data class NavigateToWizard(val issueId: String) : MigrationEffect

    /** Navigate to issue detail page / 导航到问题详情页
     * @param issueId Issue ID / 问题ID
     */
    data class NavigateToDetail(val issueId: String) : MigrationEffect

    /** Navigate back from detail page / 从详情页返回仪表盘 */
    data object NavigateBack : MigrationEffect

    /** Open file picker / 打开文件选择器 */
    data object OpenFilePicker : MigrationEffect
}
