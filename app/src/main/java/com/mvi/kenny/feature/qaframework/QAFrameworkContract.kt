package com.mvi.kenny.feature.qaframework

/**
 * ============================================================
 * QAFrameworkContract — AI驱动QA框架 MVI 契约
 * ============================================================
 * MVI (Model-View-Intent) Architecture:
 * - Model (State): Immutable data class, single source of truth
 * - View: Composable functions consuming State, rendering UI
 * - Intent: User intentions (user actions), processed by ViewModel
 *
 * Effect: One-time side effects (navigation, Toast), delivered via Channel
 * —————————————————————————————————————————————————————
 */

// ================================================================
// AI Model & Export Format Enums
// ================================================================

/**
 * AI 模型选择枚举
 *
 * @param LOCAL 本地模型（如 Google ML Kit / 本地 Llama）
 * @param CLOUD 云端模型（如 OpenAI Vision API）
 */
enum class AIModel {
    LOCAL,
    CLOUD
}

/**
 * 报告导出格式枚举
 *
 * @param PDF PDF 格式导出
 * @param JSON JSON 格式导出
 */
enum class ExportFormat {
    PDF,
    JSON
}

/**
 * 扫描任务状态枚举
 *
 * @param IDLE 空闲状态，未开始
 * @param RUNNING 扫描进行中
 * @param PAUSED 扫描暂停
 * @param COMPLETED 扫描完成
 * @param FAILED 扫描失败
 */
enum class ScanTaskStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED
}

/**
 * QA 问题严重程度枚举
 *
 * @param ERROR 严重错误（红色 #B3261E）
 * @param WARNING 警告（橙色 #F9A825）
 * @param INFO 信息提示（绿色 #2E7D32）
 */
enum class IssueSeverity {
    ERROR,
    WARNING,
    INFO
}

// ================================================================
// Data Models
// ================================================================

/**
 * 扫描任务数据模型
 *
 * @param id 任务唯一标识
 * @param packageName 目标 App 包名
 * @param deviceId 设备 ID
 * @param status 任务状态
 * @param screenCount 扫描屏幕数
 * @param issueCount 发现问题数
 * @param createdAt 创建时间（毫秒时间戳）
 */
data class ScanTask(
    val id: String,
    val packageName: String,
    val deviceId: String,
    val status: ScanTaskStatus = ScanTaskStatus.IDLE,
    val screenCount: Int = 0,
    val issueCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 连接的设备数据模型
 *
 * @param id 设备唯一标识（ADB device ID）
 * @param name 设备名称（如 "Pixel 6 Pro"）
 * @param androidVersion Android 版本号（如 "Android 13"）
 * @param isConnected 是否已连接
 */
data class ConnectedDevice(
    val id: String,
    val name: String,
    val androidVersion: String,
    val isConnected: Boolean = true
)

/**
 * QA 问题数据模型
 *
 * @param id 问题唯一标识
 * @param screenIndex 所属屏幕序号（0-based）
 * @param screenshotPath 截图文件路径
 * @param description AI 分析描述
 * @param severity 严重程度
 * @param suggestions 修复建议列表
 */
data class QAIssue(
    val id: String,
    val screenIndex: Int,
    val screenshotPath: String,
    val description: String,
    val severity: IssueSeverity,
    val suggestions: List<String> = emptyList()
)

/**
 * 报告摘要数据模型
 *
 * @param totalScreens 扫描总屏幕数
 * @param passedScreens 通过屏幕数
 * @param errorCount 严重错误数
 * @param warningCount 警告数
 * @param infoCount 信息提示数
 */
data class ReportSummary(
    val totalScreens: Int,
    val passedScreens: Int,
    val errorCount: Int,
    val warningCount: Int,
    val infoCount: Int
)

// ================================================================
// State Classes
// ================================================================

/**
 * 主界面状态（MainScreen）
 *
 * @param packageName 输入的包名
 * @param recentTasks 最近扫描任务列表
 * @param availableDevices 可用设备列表
 * @param selectedDeviceId 选中的设备 ID（null 表示未选）
 * @param isLoading 是否加载中
 * @param error 错误信息（null 表示无错误）
 * @param showDeviceSheet 是否显示设备选择 BottomSheet
 */
data class MainState(
    val packageName: String = "",
    val recentTasks: List<ScanTask> = emptyList(),
    val availableDevices: List<ConnectedDevice> = emptyList(),
    val selectedDeviceId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeviceSheet: Boolean = false
) {
    companion object {
        /** 初始状态 / Initial state */
        val Initial = MainState()
    }
}

/**
 * 扫描进度状态（ScanProgressScreen）
 *
 * @param taskId 当前任务 ID
 * @param currentScreen 当前屏幕序号（0-based）
 * @param totalScreens 屏幕总数
 * @param currentScreenshotPath 当前截图路径
 * @param aiAnalysisResult AI 分析结果文本
 * @param isAnalyzing 是否正在 AI 分析中
 * @param isPaused 是否暂停
 * @param isCancelled 是否已取消
 * @param issuesFound 已发现的问题列表
 */
data class ScanProgressState(
    val taskId: String = "",
    val currentScreen: Int = 0,
    val totalScreens: Int = 0,
    val currentScreenshotPath: String? = null,
    val aiAnalysisResult: String? = null,
    val isAnalyzing: Boolean = false,
    val isPaused: Boolean = false,
    val isCancelled: Boolean = false,
    val issuesFound: List<QAIssue> = emptyList()
) {
    companion object {
        /** 初始状态 / Initial state */
        val Initial = ScanProgressState()
    }

    /** 进度百分比（0.0 ~ 1.0） */
    val progress: Float
        get() = if (totalScreens > 0) currentScreen.toFloat() / totalScreens else 0f
}

/**
 * 报告状态（ReportScreen / ReportDetailScreen）
 *
 * @param taskId 任务 ID
 * @param summary 报告摘要
 * @param issues 问题列表
 * @param isLoading 是否加载中
 * @param isExporting 是否正在导出
 * @param error 错误信息
 */
data class ReportState(
    val taskId: String = "",
    val summary: ReportSummary? = null,
    val issues: List<QAIssue> = emptyList(),
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** 初始状态 / Initial state */
        val Initial = ReportState()
    }
}

/**
 * 设置页面状态（SettingsScreen）
 *
 * @param cdpHost CDP 连接主机地址（默认 127.0.0.1）
 * @param cdpPort CDP 连接端口（默认 9222）
 * @param aiModel AI 模型选择
 * @param parallelDevices 并行设备数（1-5）
 * @param screenshotQuality 截图质量（10-100）
 * @param exportFormat 导出格式偏好
 */
data class SettingsState(
    val cdpHost: String = "127.0.0.1",
    val cdpPort: Int = 9222,
    val aiModel: AIModel = AIModel.LOCAL,
    val parallelDevices: Int = 1,
    val screenshotQuality: Int = 80,
    val exportFormat: ExportFormat = ExportFormat.PDF
) {
    companion object {
        /** 初始状态 / Initial state */
        val Initial = SettingsState()
    }
}

// ================================================================
// Intent Classes
// ================================================================

/**
 * 主界面用户意图（MainIntent）
 * —————————————————————————————————————————————————————
 * MainScreen 上的用户操作对应的事件。
 */
sealed interface MainIntent {
    /** 更新包名输入
     * @param packageName 包名
     */
    data class UpdatePackageName(val packageName: String) : MainIntent

    /** 加载最近任务列表
     * @param limit 最多加载数量
     */
    data class LoadRecentTasks(val limit: Int = 10) : MainIntent

    /** 删除指定任务
     * @param taskId 任务 ID
     */
    data class DeleteTask(val taskId: String) : MainIntent

    /** 开始扫描
     * @param packageName 包名
     * @param deviceId 设备 ID
     */
    data class StartScan(val packageName: String, val deviceId: String) : MainIntent

    /** 显示设备选择 BottomSheet */
    data object ShowDeviceSheet : MainIntent

    /** 隐藏设备选择 BottomSheet */
    data object HideDeviceSheet : MainIntent

    /** 选择设备
     * @param deviceId 设备 ID
     */
    data class SelectDevice(val deviceId: String) : MainIntent

    /** 刷新设备列表 */
    data object RefreshDevices : MainIntent
}

/**
 * 扫描进度页面用户意图（ScanIntent）
 * —————————————————————————————————————————————————————
 * ScanProgressScreen 上的用户操作。
 */
sealed interface ScanIntent {
    /** 暂停扫描 */
    data object PauseScan : ScanIntent

    /** 继续扫描 */
    data object ResumeScan : ScanIntent

    /** 取消扫描 */
    data object CancelScan : ScanIntent

    /** 开始扫描（内部初始化用）
     * @param taskId 任务 ID
     * @param packageName 包名
     * @param deviceId 设备 ID
     */
    data class InitScan(val taskId: String, val packageName: String, val deviceId: String) : ScanIntent
}

/**
 * 报告页面用户意图（ReportIntent）
 * —————————————————————————————————————————————————————
 * ReportScreen / ReportDetailScreen 上的用户操作。
 */
sealed interface ReportIntent {
    /** 加载报告
     * @param taskId 任务 ID
     */
    data class LoadReport(val taskId: String) : ReportIntent

    /** 导出报告
     * @param format 导出格式
     */
    data class ExportReport(val format: ExportFormat) : ReportIntent

    /** 分享报告 */
    data class ShareReport(val taskId: String) : ReportIntent
}

/**
 * 设置页面用户意图（SettingsIntent）
 * —————————————————————————————————————————————————————
 * SettingsScreen 上的用户操作。
 */
sealed interface SettingsIntent {
    /** 更新 CDP 主机地址
     * @param host 主机地址
     */
    data class UpdateCdpHost(val host: String) : SettingsIntent

    /** 更新 CDP 端口
     * @param port 端口号
     */
    data class UpdateCdpPort(val port: Int) : SettingsIntent

    /** 更新 AI 模型选择
     * @param model AI 模型类型
     */
    data class UpdateAIModel(val model: AIModel) : SettingsIntent

    /** 更新并行设备数
     * @param count 并行设备数
     */
    data class UpdateParallelDevices(val count: Int) : SettingsIntent

    /** 更新截图质量
     * @param quality 质量值（10-100）
     */
    data class UpdateScreenshotQuality(val quality: Int) : SettingsIntent

    /** 更新导出格式
     * @param format 导出格式
     */
    data class UpdateExportFormat(val format: ExportFormat) : SettingsIntent
}

// ================================================================
// Effect Classes
// ================================================================

/**
 * 主界面副作用（MainEffect）
 * —————————————————————————————————————————————————————
 * MainScreen 上的一次性副作用事件。
 */
sealed interface MainEffect {
    /** 显示 Snackbar 错误消息
     * @param message 错误信息
     */
    data class ShowError(val message: String) : MainEffect

    /** 显示 Snackbar 成功消息
     * @param message 成功信息
     */
    data class ShowSuccess(val message: String) : MainEffect

    /** 导航到扫描进度页面
     * @param taskId 任务 ID
     */
    data class NavigateToScan(val taskId: String) : MainEffect

    /** 导航到报告页面
     * @param taskId 任务 ID
     */
    data class NavigateToReport(val taskId: String) : MainEffect
}

/**
 * 扫描进度页面副作用（ScanEffect）
 * —————————————————————————————————————————————————————
 * ScanProgressScreen 上的一次性副作用事件。
 */
sealed interface ScanEffect {
    /** 扫描完成
     * @param taskId 任务 ID
     */
    data class ScanCompleted(val taskId: String) : ScanEffect

    /** 设备断开连接
     * @param deviceId 设备 ID
     */
    data class DeviceDisconnected(val deviceId: String) : ScanEffect

    /** 显示通知
     * @param title 通知标题
     * @param body 通知内容
     */
    data class ShowNotification(val title: String, val body: String) : ScanEffect
}

/**
 * 报告页面副作用（ReportEffect）
 * —————————————————————————————————————————————————————
 * ReportScreen / ReportDetailScreen 上的一次性副作用事件。
 */
sealed interface ReportEffect {
    /** 导出完成
     * @param filePath 导出文件路径
     */
    data class ExportCompleted(val filePath: String) : ReportEffect

    /** 导出失败
     * @param error 错误信息
     */
    data class ExportFailed(val error: String) : ReportEffect

    /** 分享报告（触发系统分享面板） */
    data object TriggerShare : ReportEffect
}
