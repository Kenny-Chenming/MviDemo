package com.mvi.kenny.feature.onalaarmlistener

import androidx.compose.ui.graphics.Color

// =============================================================
// OnAlarmContract — Android 17 OnAlarmListener Battery
// Optimization & Background Task Scheduling Dev Toolkit
// MVI Contract / MVI 契约
// =============================================================
// PRD-150 | Android 17 OnAlarmListener 电池优化与后台任务调度开发工具包
//
// MVI Architecture Pattern / MVI 架构模式
// - Model (State): Immutable data class — single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, etc.)
//
// @see OnAlarmViewModel State management
// @see OnAlarmScreen Main screen with bottom tab navigation

// =============================================================
// RiskLevel — 风险等级
// =============================================================
/**
 * Battery risk level / 电池风险等级
 *
 * Indicates the compliance status of the app's battery usage.
 *
 * @param label Display label / 显示标签
 * @param color Risk level color / 颜色
 */
enum class RiskLevel(val label: String, val color: Color) {
    GREEN("🟢 合规 / Compliant", Color(0xFF4CAF50)),
    YELLOW("🟡 警告 / Warning", Color(0xFFFF9800)),
    RED("🔴 高风险 / High Risk", Color(0xFFF44336)),
    UNKNOWN("⚪ 未知 / Unknown", Color(0xFF9E9E9E))
}

// =============================================================
// ScanStep — 扫描步骤枚举
// =============================================================
/**
 * Scanner step in the guided flow / 引导流程中的扫描步骤
 *
 * @param stepNumber Step number (1-5) / 步骤编号
 * @param title Step title / 步骤标题
 */
enum class ScanStep(val stepNumber: Int, val title: String) {
    PROJECT_SELECTION(1, "项目选择 / Project Selection"),
    WAKELOCK_SCAN(2, "WakeLock 扫描 / WakeLock Scan"),
    ALARM_WORK_SCAN(3, "Alarm/WorkManager 扫描 / Alarm/WorkManager Scan"),
    COMPLIANCE_EVALUATION(4, "合规评估 / Compliance Evaluation"),
    DIFF_GENERATION(5, "Diff 生成 / Diff Generation")
}

// =============================================================
// TaskType — 任务类型
// =============================================================
/**
 * Background task type / 后台任务类型
 *
 * @param label Display label / 显示标签
 * @param description Task type description / 任务类型描述
 */
enum class TaskType(val label: String, val description: String) {
    WAKELOCK("WakeLock", "PowerManager WakeLock / PowerManager WakeLock"),
    ALARM("Alarm", "AlarmManager scheduled task / AlarmManager 计划任务"),
    WORK_MANAGER("WorkManager", "Jetpack WorkManager task / WorkManager 任务"),
    EXPEDITED("Expedited", "Expedited Task / 加急任务"),
    USER_INITIATED("UserInitiated", "UserInitiatedDataTransfer / 用户发起的数据传输")
}

// =============================================================
// ElectricityContribution — 耗电贡献
// =============================================================
/**
 * Electricity/battery consumption contribution / 耗电贡献
 *
 * @param taskName Task name / 任务名称
 * @param taskType Task type / 任务类型
 * @param contributionPercent Contribution percentage / 贡献百分比
 * @param estimatedMah Estimated mAh consumption / 预估耗电量（mAh）
 */
data class ElectricityContribution(
    val taskName: String,
    val taskType: TaskType,
    val contributionPercent: Float,
    val estimatedMah: Float
)

// =============================================================
// AffectedTask — 受影响的任务
// =============================================================
/**
 * Affected background task / 受影响的后台任务
 *
 * Represents a detected task that may impact battery compliance.
 *
 * @param id Unique identifier / 唯一标识符
 * @param taskName Task name / 任务名称
 * @param taskType Task type / 任务类型
 * @param filePath Source file path / 源文件路径
 * @param lineNumber Line number in source file / 源文件行号
 * @param methodName Method name / 方法名
 * @param className Class name / 类名
 * @param estimatedDurationMinutes Estimated wake lock duration in minutes / 预估 WakeLock 时长（分钟）
 * @param estimatedFrequency Daily trigger frequency / 预估每日触发频率
 * @param migrationSuggestion Migration suggestion / 迁移建议
 * @param isMigrated Whether this task has been migrated / 是否已迁移
 */
data class AffectedTask(
    val id: String,
    val taskName: String,
    val taskType: TaskType,
    val filePath: String,
    val lineNumber: Int,
    val methodName: String,
    val className: String,
    val estimatedDurationMinutes: Int,
    val estimatedFrequency: Int,
    val migrationSuggestion: String,
    val isMigrated: Boolean = false
) {
    /** Estimated battery impact level / 预估电池影响等级 */
    val impactLevel: ImpactLevel
        get() = when {
            estimatedDurationMinutes > 30 || estimatedFrequency > 50 -> ImpactLevel.HIGH
            estimatedDurationMinutes > 10 || estimatedFrequency > 20 -> ImpactLevel.MEDIUM
            else -> ImpactLevel.LOW
        }
}

/** Impact level / 影响等级 */
enum class ImpactLevel(val label: String, val color: Color) {
    HIGH("高 / High", Color(0xFFF44336)),
    MEDIUM("中 / Medium", Color(0xFFFF9800)),
    LOW("低 / Low", Color(0xFF4CAF50))
}

// =============================================================
// ScanLogEntry — 扫描日志条目
// =============================================================
/**
 * Scan log entry / 扫描日志条目
 *
 * @param timestamp Log timestamp / 日志时间戳
 * @param level Log level (INFO/WARN/ERROR) / 日志级别
 * @param message Log message / 日志消息
 */
data class ScanLogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val message: String
)

/** Log level / 日志级别 */
enum class LogLevel(val symbol: String) {
    INFO("[INF]"),
    WARN("[WRN]"),
    ERROR("[ERR]"),
    SUCCESS("[OK ]")
}

// =============================================================
// WakeLockFinding — WakeLock 检测结果
// =============================================================
/**
 * WakeLock detection result / WakeLock 检测结果
 *
 * @param id Unique identifier / 唯一标识符
 * @param filePath Source file path / 源文件路径
 * @param lineNumber Line number / 行号
 * @param className Class name / 类名
 * @param methodName Method name / 方法名
 * @param callChain Full call chain / 完整调用链
 * @param estimatedDurationMinutes Estimated hold duration / 预估持有时长（分钟）
 * @param suggestion Migration suggestion / 迁移建议
 */
data class WakeLockFinding(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val className: String,
    val methodName: String,
    val callChain: String,
    val estimatedDurationMinutes: Int,
    val suggestion: String
)

// =============================================================
// AlarmFinding — Alarm 检测结果
// =============================================================
/**
 * Alarm detection result / Alarm 检测结果
 *
 * @param id Unique identifier / 唯一标识符
 * @param filePath Source file path / 源文件路径
 * @param lineNumber Line number / 行号
 * @param className Class name / 类名
 * @param methodName Method name / 方法名
 * @param alarmType Alarm type (ELAPSED_REALTIME/RTC/etc.) / Alarm 类型
 * @param estimatedTriggerFrequency Daily trigger frequency / 预估每日触发频率
 * @param suggestion Migration suggestion / 迁移建议
 */
data class AlarmFinding(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val className: String,
    val methodName: String,
    val alarmType: String,
    val estimatedTriggerFrequency: Int,
    val suggestion: String
)

// =============================================================
// CiTemplate — CI 模板类型
// =============================================================
/**
 * CI template type / CI 模板类型
 *
 * @param label Display label / 显示标签
 * @param description Template description / 模板描述
 */
enum class CiTemplate(val label: String, val description: String) {
    GITHUB_ACTIONS("GitHub Actions", "GitHub Actions YAML workflow / GitHub Actions 工作流"),
    GITLAB_CI("GitLab CI", "GitLab CI YAML pipeline / GitLab CI 流水线")
}

// =============================================================
// ExportFormat — Diff 导出格式
// =============================================================
/**
 * Diff export format / Diff 导出格式
 */
enum class ExportFormat(val label: String) {
    UNIFIED_DIFF("Unified Diff (.diff)"),
    GIT_PATCH("Git Patch (.patch)"),
    JSON("JSON")
}

// =============================================================
// ReportFormat — 报告导出格式
// =============================================================
/**
 * Compliance report export format / 合规报告导出格式
 */
enum class ReportFormat(val label: String) {
    PDF("PDF"),
    JSON("JSON"),
    MARKDOWN("Markdown")
}

// =============================================================
// OnAlarmState — 页面状态
// =============================================================
/**
 * OnAlarmListener Tool State / OnAlarmListener 工具页面状态
 *
 * Single source of truth for the entire tool UI.
 * All UI state is derived from this data class.
 *
 * @param activeTab Currently active bottom tab / 当前活跃的底部 Tab
 *   - Dashboard: Compliance overview / 合规概览
 *   - Scanner: Guided scan flow / 引导扫描流程
 *   - Analysis: Detailed task list / 详细任务列表
 *   - Settings: CI config and thresholds / CI 配置和阈值
 * @param overallScore Overall compliance score 0-100 / 综合合规评分
 * @param batteryRiskLevel Current battery risk level / 当前电池风险等级
 * @param affectedTasks List of affected background tasks / 受影响的后台任务列表
 * @param isScanning Whether scan is in progress / 扫描是否进行中
 * @param scanProgress Scan progress 0.0~1.0 / 扫描进度
 * @param lastScanTime Last scan timestamp / 上次扫描时间戳
 *
 * --- Scanner-specific state ---
 * @param scannerCurrentStep Current scanner step / 当前扫描步骤
 * @param scanLogs List of scan log entries / 扫描日志列表
 * @param wakeLockResults WakeLock scan results / WakeLock 扫描结果
 * @param alarmResults Alarm scan results / Alarm 扫描结果
 * @param generatedDiff Generated migration diff / 生成的迁移 Diff
 *
 * --- Analysis-specific state ---
 * @param taskGroups Tasks grouped by type / 按类型分组的任务
 * @param selectedTask Currently selected task / 当前选中的任务
 * @param electricityData Electricity contribution data / 耗电数据
 *
 * --- Settings-specific state ---
 * @param wakeLockThresholdMinutes WakeLock duration threshold in minutes / WakeLock 时长阈值（分钟）
 * @param ciTemplateType Selected CI template type / 选中的 CI 模板类型
 * @param notificationsEnabled Whether notifications are enabled / 通知是否启用
 *
 * @param error Error message if any / 错误信息
 */
data class OnAlarmState(
    // ─────────────────────────────────────────────────────────
    // Tab navigation / Tab 导航
    // ─────────────────────────────────────────────────────────
    val activeTab: ToolTab = ToolTab.DASHBOARD,

    // ─────────────────────────────────────────────────────────
    // Dashboard state / 仪表盘状态
    // ─────────────────────────────────────────────────────────
    val overallScore: Int = 0,
    val batteryRiskLevel: RiskLevel = RiskLevel.UNKNOWN,
    val affectedTasks: List<AffectedTask> = emptyList(),
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val lastScanTime: Long? = null,

    // ─────────────────────────────────────────────────────────
    // Scanner state / 扫描器状态
    // ─────────────────────────────────────────────────────────
    val scannerCurrentStep: ScanStep = ScanStep.PROJECT_SELECTION,
    val scanLogs: List<ScanLogEntry> = emptyList(),
    val wakeLockResults: List<WakeLockFinding> = emptyList(),
    val alarmResults: List<AlarmFinding> = emptyList(),
    val generatedDiff: String? = null,
    val isExportingDiff: Boolean = false,

    // ─────────────────────────────────────────────────────────
    // Analysis state / 分析状态
    // ─────────────────────────────────────────────────────────
    val taskGroups: Map<TaskType, List<AffectedTask>> = emptyMap(),
    val selectedTask: AffectedTask? = null,
    val electricityData: List<ElectricityContribution> = emptyList(),

    // ─────────────────────────────────────────────────────────
    // Settings state / 设置状态
    // ─────────────────────────────────────────────────────────
    val wakeLockThresholdMinutes: Int = 4,
    val ciTemplateType: CiTemplate = CiTemplate.GITHUB_ACTIONS,
    val notificationsEnabled: Boolean = true,

    // ─────────────────────────────────────────────────────────
    // Common state / 通用状态
    // ─────────────────────────────────────────────────────────
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = OnAlarmState()
    }

    /**
     * Count of affected tasks by type / 按类型统计受影响任务数
     */
    val taskCountByType: Map<TaskType, Int>
        get() = affectedTasks.groupBy { it.taskType }.mapValues { it.value.size }

    /**
     * Count of high impact tasks / 高影响任务数量
     */
    val highImpactCount: Int
        get() = affectedTasks.count { it.impactLevel == ImpactLevel.HIGH }

    /**
     * Count of migrated tasks / 已迁移任务数量
     */
    val migratedCount: Int
        get() = affectedTasks.count { it.isMigrated }

    /**
     * Scan percentage as integer / 扫描完成百分比（整数）
     */
    val scanPercentage: Int
        get() = (scanProgress * 100).toInt()
}

// =============================================================
// ToolTab — 功能 Tab
// =============================================================
/**
 * Tool module tab / 功能模块 Tab
 *
 * @param title Display title / 显示标题
 * @param iconName Icon name / 图标名称
 */
enum class ToolTab(val title: String, val iconName: String) {
    DASHBOARD("Dashboard / 仪表盘", "dashboard"),
    SCANNER("Scanner / 扫描器", "radar"),
    ANALYSIS("Analysis / 分析", "analytics"),
    SETTINGS("Settings / 设置", "settings")
}

// =============================================================
// OnAlarmIntent — 用户意图
// =============================================================
/**
 * OnAlarmListener Tool User Intents / OnAlarmListener 工具用户意图
 *
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface OnAlarmIntent {

    // ─────────────────────────────────────────────────────────
    // Tab navigation / Tab 导航
    // ─────────────────────────────────────────────────────────
    /** Select a tool tab / 选择工具 Tab */
    data class SelectTab(val tab: ToolTab) : OnAlarmIntent

    // ─────────────────────────────────────────────────────────
    // Dashboard intents / 仪表盘意图
    // ─────────────────────────────────────────────────────────
    /** Start quick scan from dashboard / 从仪表盘开始快速扫描 */
    data object StartQuickScan : OnAlarmIntent

    /** Refresh dashboard / 刷新仪表盘 */
    data object RefreshDashboard : OnAlarmIntent

    /** Navigate to specific task / 导航到指定任务
     * @param task Task to navigate to / 要导航到的任务
     */
    data class NavigateToTask(val task: AffectedTask) : OnAlarmIntent

    // ─────────────────────────────────────────────────────────
    // Scanner intents / 扫描器意图
    // ─────────────────────────────────────────────────────────
    /** Select project path / 选择项目路径
     * @param path Project or module path / 项目或模块路径
     */
    data class SelectProject(val path: String) : OnAlarmIntent

    /** Move to next scanner step / 进入下一个扫描步骤 */
    data object NextStep : OnAlarmIntent

    /** Move to previous scanner step / 返回上一个扫描步骤 */
    data object PreviousStep : OnAlarmIntent

    /** Start the scan process / 开始扫描流程 */
    data object StartScan : OnAlarmIntent

    /** Generate migration diff / 生成迁移 Diff */
    data object GenerateDiff : OnAlarmIntent

    /** Export diff to file / 导出 Diff 到文件
     * @param format Export format / 导出格式
     */
    data class ExportDiff(val format: ExportFormat) : OnAlarmIntent

    // ─────────────────────────────────────────────────────────
    // Analysis intents / 分析意图
    // ─────────────────────────────────────────────────────────
    /** Select a task for detail view / 选择任务查看详情
     * @param task Task to select / 要选择的任务
     */
    data class SelectTask(val task: AffectedTask) : OnAlarmIntent

    /** Clear selected task / 清除选中的任务 */
    data object ClearSelectedTask : OnAlarmIntent

    /** Mark task as migrated / 标记任务已迁移
     * @param taskId ID of the task / 任务 ID
     */
    data class MarkAsMigrated(val taskId: String) : OnAlarmIntent

    /** Export compliance report / 导出合规报告
     * @param format Report format / 报告格式
     */
    data class ExportReport(val format: ReportFormat) : OnAlarmIntent

    // ─────────────────────────────────────────────────────────
    // Settings intents / 设置意图
    // ─────────────────────────────────────────────────────────
    /** Update WakeLock threshold / 更新 WakeLock 阈值
     * @param minutes Threshold in minutes / 阈值（分钟）
     */
    data class UpdateWakeLockThreshold(val minutes: Int) : OnAlarmIntent

    /** Update CI template type / 更新 CI 模板类型
     * @param template CI template type / CI 模板类型
     */
    data class UpdateCiTemplate(val template: CiTemplate) : OnAlarmIntent

    /** Toggle notifications / 切换通知设置 */
    data object ToggleNotifications : OnAlarmIntent

    /** Download CI template / 下载 CI 模板 */
    data object DownloadCiTemplate : OnAlarmIntent

    // ─────────────────────────────────────────────────────────
    // Common intents / 通用意图
    // ─────────────────────────────────────────────────────────
    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : OnAlarmIntent
}

// =============================================================
// OnAlarmEffect — 副作用
// =============================================================
/**
 * OnAlarmListener Tool Side Effects / OnAlarmListener 工具副作用
 *
 * One-time events, consumed only once by UI layer.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface OnAlarmEffect {

    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error message / 是否为错误消息
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : OnAlarmEffect

    /** Scan complete event / 扫描完成事件
     * @param score Final compliance score / 最终合规评分
     */
    data class ScanComplete(val score: Int) : OnAlarmEffect

    /** Diff generated event / Diff 生成完成事件
     * @param diff Generated diff content / 生成的 Diff 内容
     */
    data class DiffGenerated(val diff: String) : OnAlarmEffect

    /** Report exported event / 报告导出完成事件
     * @param filePath Exported file path / 导出文件路径
     */
    data class ReportExported(val filePath: String) : OnAlarmEffect

    /** Task marked as migrated event / 任务已迁移事件
     * @param taskId ID of the migrated task / 已迁移任务的 ID
     */
    data class TaskMarkedMigrated(val taskId: String) : OnAlarmEffect

    /** Copy text to clipboard / 复制文本到剪贴板
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : OnAlarmEffect

    /** Navigate to scanner tab / 导航到扫描器 Tab */
    data object NavigateToScanner : OnAlarmEffect

    /** Navigate to analysis tab / 导航到分析 Tab */
    data object NavigateToAnalysis : OnAlarmEffect
}
