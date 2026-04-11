package com.mvi.kenny.feature.alarmmigration

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mvi.kenny.feature.android17migration.Severity

/**
 * ============================================================
 * AlarmMigrationContract — AlarmManager Listener Mode 迁移工具 MVI 契约
 * ============================================================
 * MVI (Model-View-Intent) Architecture Pattern.
 *
 * 三层架构：
 * - Model (State): 不可变数据类，UI 的单一数据源
 * - View: Composable 函数，消费 State 并渲染 UI
 * - Intent: 用户意图（用户操作），ViewModel 处理并更新 State
 *
 * Effect: 一次性副作用（导航、Toast），通过 Channel 投递
 *
 * @see AlarmMigrationViewModel 状态管理
 * @see AlarmMigrationScreen 主界面
 * @see HtmlReportPreview HTML 报告预览组件
 */

// =============================================================
// CLI Color Palette — CLI 配色方案
// =============================================================
/**
 * CLI color constants for terminal-style output.
 * CLI 终端输出的颜色常量
 *
 * Used by ScanResultRenderer for colored CLI output.
 *
 * @see AlarmMigrationScreen CLI output panel
 */
object CliColors {
    val CRITICAL = Color(0xFFFF4444)   // #FF4444 — Critical severity
    val WARNING  = Color(0xFFFFB300)   // #FFB300 — Warning severity
    val INFO     = Color(0xFF42A5F5)   // #42A5F5 — Info severity
    val SUCCESS  = Color(0xFF66BB6A)    // #66BB6A — Success
    val RESET    = Color.Unspecified    // Reset to default
}

// =============================================================
// AlarmSeverity — 告警严重程度
// =============================================================
/**
 * AlarmManager issue severity level.
 * 告警管理器问题严重程度等级
 *
 * @param label Display label (English) / 显示标签（英文）
 * @param labelZh Display label (Chinese) / 显示标签（中文）
 * @param color Severity color (Compose Color) / 严重程度颜色
 * @param cliColor Terminal color / 终端颜色
 */
enum class AlarmSeverity(
    val label: String,
    val labelZh: String,
    val color: Color,
    val cliColor: Color
) {
    CRITICAL("Critical", "严重", Color(0xFFFF4444), CliColors.CRITICAL),
    WARNING("Warning",  "警告", Color(0xFFFFB300), CliColors.WARNING),
    INFO("Info",        "提示", Color(0xFF42A5F5), CliColors.INFO),
    SUCCESS("Success",  "成功", Color(0xFF66BB6A), CliColors.SUCCESS)
}

// =============================================================
// ScanStatus — 扫描状态
// =============================================================
/**
 * Project scan status / 项目扫描状态
 *
 * @property label Display label / 显示标签
 */
enum class AlarmScanStatus(val label: String) {
    IDLE("Idle / 待机"),
    SCANNING("Scanning... / 扫描中"),
    COMPLETED("Completed / 完成"),
    ERROR("Error / 错误")
}

// =============================================================
// MigrationStatus — 迁移状态
// =============================================================
/**
 * Migration operation status / 迁移操作状态
 *
 * @property label Display label / 显示标签
 */
enum class AlarmMigrationStatus(val label: String) {
    PREVIEW("Preview / 预览"),
    MIGRATING("Migrating... / 迁移中"),
    COMPLETED("Completed / 完成"),
    ROLLED_BACK("Rolled Back / 已回滚"),
    ERROR("Error / 错误")
}

// =============================================================
// AlarmApiType — AlarmManager API 类型
// =============================================================
/**
 * AlarmManager API type that triggered the issue.
 * 触发问题的 AlarmManager API 类型
 *
 * @property oldApi Old deprecated API / 旧的废弃 API
 * @property newApi New recommended API / 新的推荐 API
 */
enum class AlarmApiType(
    val oldApi: String,
    val newApi: String
) {
    SET_EXACT("setExact() + PendingIntent", "setExactAndUntilWhileIdle() + OnAlarmListener"),
    SET_REPEATING("setRepeating() + PendingIntent", "WorkManager.enqueueUniquePeriodicWork()"),
    SET("set() + PendingIntent", "setAndUntilIdle() + OnAlarmListener"),
    SET_AND_WHILE_IDLE("setAndWhileIdleUntil() + PendingIntent", "setAndUntilIdle() + OnAlarmListener")
}

// =============================================================
// AlarmUsageIssue — 告警使用问题
// =============================================================
/**
 * AlarmManager PendingIntent usage issue.
 * AlarmManager PendingIntent 使用问题
 *
 * Represents a single incompatibility found during AST scan.
 * 描述在 AST 扫描中发现的单个不兼容问题。
 *
 * @param id Unique issue ID / 唯一问题ID
 * @param filePath Source file path / 源文件路径
 * @param lineNumber Line number in file / 文件行号
 * @param methodName Method containing the issue / 包含问题的的方法名
 * @param apiType AlarmManager API type / API 类型
 * @param severity Issue severity / 严重程度
 * @param pendingIntentType PendingIntent type (getService/getBroadcast/getActivity) / PendingIntent 类型
 * @param codeSnippet Original code snippet / 原始代码片段
 * @param recommendedFix Recommended fix code / 推荐修复代码
 * @param isFixed Whether this issue has been migrated / 是否已迁移
 * @param isIgnored Whether this issue is marked as ignored / 是否标记为忽略
 */
data class AlarmUsageIssue(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val methodName: String,
    val apiType: AlarmApiType,
    val severity: AlarmSeverity,
    val pendingIntentType: String, // "getService" / "getBroadcast" / "getActivity"
    val codeSnippet: String,
    val recommendedFix: String,
    val isFixed: Boolean = false,
    val isIgnored: Boolean = false
)

// =============================================================
// FilterCriteria — 过滤条件
// =============================================================
/**
 * Scan result filter criteria / 扫描结果过滤条件
 *
 * @param severity Severity filter, null means all / 严重程度过滤，null表示全部
 * @param filePathFilter File path filter (contains) / 文件路径过滤（包含）
 * @param showFixed Whether to show fixed issues / 是否显示已修复问题
 * @param showIgnored Whether to show ignored issues / 是否显示已忽略问题
 */
data class FilterCriteria(
    val severity: AlarmSeverity? = null,
    val filePathFilter: String? = null,
    val showFixed: Boolean = false,
    val showIgnored: Boolean = false
)

// =============================================================
// ScanSummary — 扫描摘要
// =============================================================
/**
 * Scan result summary / 扫描结果摘要
 *
 * @param total Total issues found / 发现问题总数
 * @param critical Critical issues count / 严重问题数
 * @param warning Warning issues count / 警告问题数
 * @param info Info issues count / 提示问题数
 * @param fixed Fixed issues count / 已修复问题数
 * @param ignored Ignored issues count / 已忽略问题数
 * @param filesScanned Number of files scanned / 已扫描文件数
 * @param scanDurationMs Scan duration in milliseconds / 扫描耗时（毫秒）
 */
data class ScanSummary(
    val total: Int = 0,
    val critical: Int = 0,
    val warning: Int = 0,
    val info: Int = 0,
    val fixed: Int = 0,
    val ignored: Int = 0,
    val filesScanned: Int = 0,
    val scanDurationMs: Long = 0L
)

// =============================================================
// PendingChange — 待处理变更
// =============================================================
/**
 * Pending migration change / 待处理迁移变更
 *
 * Represents a single file change that will be applied during migration.
 * 描述迁移期间将应用的单个文件变更。
 *
 * @param issueId Related issue ID / 关联问题ID
 * @param filePath File to be modified / 将被修改的文件路径
 * @param originalCode Original code snippet / 原始代码
 * @param migratedCode Migrated code snippet / 迁移后代码
 * @param backupComment Backup comment for rollback / 回滚用的备份注释
 */
data class PendingChange(
    val issueId: String,
    val filePath: String,
    val originalCode: String,
    val migratedCode: String,
    val backupComment: String // e.g. "// KAIFU_BACKUP: <original>"
)

// =============================================================
// BlockedFile — 阻塞文件
// =============================================================
/**
 * File blocked from migration / 被阻塞迁移的文件
 *
 * @param filePath Blocked file path / 被阻塞的文件路径
 * @param reason Reason for blocking / 阻塞原因
 * @param suggestedAction Suggested action to resolve / 建议的解决操作
 */
data class BlockedFile(
    val filePath: String,
    val reason: String,
    val suggestedAction: String
)

// =============================================================
// AlarmScanState — 扫描页面状态
// =============================================================
/**
 * Alarm Migration Tool State — Single source of truth for UI.
 * 告警迁移工具状态 — UI 的单一数据源
 *
 * @param scanStatus Current scan status / 当前扫描状态
 * @param modulePath Selected module path / 已选模块路径
 * @param results List of all detected issues / 所有检测到的问题列表
 * @param summary Scan summary statistics / 扫描摘要统计
 * @param filterCriteria Current filter criteria / 当前过滤条件
 * @param scanProgress Scan progress 0.0~1.0 / 扫描进度
 * @param error Error message if any / 错误信息
 */
data class AlarmScanState(
    val scanStatus: AlarmScanStatus = AlarmScanStatus.IDLE,
    val modulePath: String = "",
    val results: List<AlarmUsageIssue> = emptyList(),
    val summary: ScanSummary = ScanSummary(),
    val filterCriteria: FilterCriteria = FilterCriteria(),
    val scanProgress: Float = 0f,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = AlarmScanState()
    }

    /**
     * Filtered results based on current FilterCriteria.
     * 根据当前过滤条件筛选后的结果
     */
    val filteredResults: List<AlarmUsageIssue>
        get() = results.filter { issue ->
            val severityMatch = filterCriteria.severity == null || issue.severity == filterCriteria.severity
            val fileMatch = filterCriteria.filePathFilter == null || issue.filePath.contains(filterCriteria.filePathFilter)
            val fixedMatch = !issue.isFixed || filterCriteria.showFixed
            val ignoredMatch = !issue.isIgnored || filterCriteria.showIgnored
            severityMatch && fileMatch && fixedMatch && ignoredMatch
        }
}

// =============================================================
// AlarmMigrationState — 迁移页面状态
// =============================================================
/**
 * Alarm Migration State / 告警迁移状态
 *
 * @param migrationStatus Current migration status / 当前迁移状态
 * @param pendingChanges List of pending changes for dry-run preview / 预览模式的待处理变更列表
 * @param completedFiles List of successfully migrated files / 已成功迁移的文件列表
 * @param blockedFiles List of blocked files with reasons / 被阻塞的文件列表
 * @param rollbackAvailable Whether rollback is available / 是否可回滚
 * @param lastBackupPaths Map of file path to backup content / 文件路径到备份内容的映射
 * @param migrationProgress Migration progress 0.0~1.0 / 迁移进度
 * @param error Error message if any / 错误信息
 */
data class AlarmMigrationState(
    val migrationStatus: AlarmMigrationStatus = AlarmMigrationStatus.PREVIEW,
    val pendingChanges: List<PendingChange> = emptyList(),
    val completedFiles: List<String> = emptyList(),
    val blockedFiles: List<BlockedFile> = emptyList(),
    val rollbackAvailable: Boolean = false,
    val lastBackupPaths: Map<String, String> = emptyMap(),
    val migrationProgress: Float = 0f,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = AlarmMigrationState()
    }
}

// =============================================================
// AlarmReportState — 报告状态
// =============================================================
/**
 * HTML Report Generation State / HTML 报告生成状态
 *
 * @param reportStatus Report generation status / 报告生成状态
 * @param reportPath Generated report file path / 生成的报告文件路径
 * @param isPreview Whether report is in preview mode / 是否为预览模式
 */
data class AlarmReportState(
    val reportStatus: AlarmScanStatus = AlarmScanStatus.IDLE,
    val reportPath: String? = null,
    val isPreview: Boolean = false
)

// =============================================================
// AlarmMigrationIntent — 用户意图
// =============================================================
/**
 * Alarm Migration Tool User Intents / 告警迁移工具用户意图
 *
 * Every user action in the UI corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 *
 * @see AlarmMigrationViewModel.sendIntent Process all intents
 */
sealed interface AlarmMigrationIntent {
    /** Set module path for scan / 设置扫描模块路径
     * @param path Module root path / 模块根路径
     */
    data class SetModulePath(val path: String) : AlarmMigrationIntent

    /** Start alarm usage scan / 开始告警使用扫描 */
    data object StartScan : AlarmMigrationIntent

    /** Cancel ongoing scan / 取消正在进行的扫描 */
    data object CancelScan : AlarmMigrationIntent

    /** Set severity filter / 设置严重程度过滤
     * @param severity Severity to filter, null means all / 过滤的严重程度，null表示全部
     */
    data class SetSeverityFilter(val severity: AlarmSeverity?) : AlarmMigrationIntent

    /** Set file path filter / 设置文件路径过滤
     * @param filter File path contains filter, null means no filter / 文件路径包含过滤，null表示不过滤
     */
    data class SetFilePathFilter(val filter: String?) : AlarmMigrationIntent

    /** Toggle show fixed issues / 切换显示已修复问题
     * @param show Whether to show fixed issues / 是否显示已修复问题
     */
    data class ToggleShowFixed(val show: Boolean) : AlarmMigrationIntent

    /** Toggle show ignored issues / 切换显示已忽略问题
     * @param show Whether to show ignored issues / 是否显示已忽略问题
     */
    data class ToggleShowIgnored(val show: Boolean) : AlarmMigrationIntent

    /** Generate preview of pending migration changes / 生成待迁移变更预览
     * @param issues Issues to migrate / 要迁移的问题列表
     */
    data class GeneratePreview(val issues: List<AlarmUsageIssue>) : AlarmMigrationIntent

    /** Execute migration (after preview confirmation) / 执行迁移（预览确认后）
     * @param changes Changes to apply / 要应用的变更列表
     */
    data class ExecuteMigration(val changes: List<PendingChange>) : AlarmMigrationIntent

    /** Rollback last migration / 回滚上次迁移 */
    data object Rollback : AlarmMigrationIntent

    /** Ignore a specific issue / 忽略指定问题
     * @param issueId Issue ID to ignore / 待忽略问题ID
     */
    data class IgnoreIssue(val issueId: String) : AlarmMigrationIntent

    /** Un-ignore an issue / 取消忽略问题
     * @param issueId Issue ID to un-ignore / 取消忽略的问题ID
     */
    data class UnignoreIssue(val issueId: String) : AlarmMigrationIntent

    /** Generate HTML report / 生成 HTML 报告
     * @param scanState Current scan state to include in report / 报告中包含的当前扫描状态
     */
    data class GenerateReport(val scanState: AlarmScanState) : AlarmMigrationIntent

    /** Open HTML report in browser / 在浏览器中打开 HTML 报告
     * @param reportPath Report file path / 报告文件路径
     */
    data class OpenReport(val reportPath: String) : AlarmMigrationIntent

    /** Compare two scan results / 对比两次扫描结果
     * @param beforePath Path to before scan JSON / 前一次扫描 JSON 路径
     * @param afterPath Path to after scan JSON / 后一次扫描 JSON 路径
     */
    data class DiffScans(val beforePath: String, val afterPath: String) : AlarmMigrationIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : AlarmMigrationIntent
}

// =============================================================
// AlarmMigrationEffect — 副作用
// =============================================================
/**
 * Alarm Migration Tool Side Effects / 告警迁移工具副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see AlarmMigrationViewModel Send via _effect.send()
 */
sealed interface AlarmMigrationEffect {
    /** Show toast message / 显示 Toast 消息
     * @param message Toast text / Toast 文本
     */
    data class ShowToast(val message: String) : AlarmMigrationEffect

    /** Show error message / 显示错误消息
     * @param message Error description / 错误描述
     */
    data class ShowError(val message: String) : AlarmMigrationEffect

    /** Scan complete notification / 扫描完成通知 */
    data object ScanComplete : AlarmMigrationEffect

    /** Migration complete notification / 迁移完成通知 */
    data object MigrationComplete : AlarmMigrationEffect

    /** Rollback complete notification / 回滚完成通知 */
    data object RollbackComplete : AlarmMigrationEffect

    /** Open file in IDE / 在 IDE 中打开文件
     * @param filePath File path / 文件路径
     * @param lineNumber Line number / 行号
     */
    data class OpenInIDE(val filePath: String, val lineNumber: Int) : AlarmMigrationEffect

    /** Share generated report / 分享生成的报告
     * @param reportPath Report file path / 报告文件路径
     */
    data class ShareReport(val reportPath: String) : AlarmMigrationEffect

    /** Open HTML report in browser / 在浏览器中打开 HTML 报告
     * @param reportPath Report file path / 报告文件路径
     */
    data class OpenInBrowser(val reportPath: String) : AlarmMigrationEffect
}
