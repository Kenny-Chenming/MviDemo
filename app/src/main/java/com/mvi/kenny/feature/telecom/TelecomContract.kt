package com.mvi.kenny.feature.telecom

import androidx.compose.ui.graphics.Color

// =============================================================
// TelecomContract — Jetpack Telecom v1.1.0 VoIP Native Visibility
// 集成工具包 MVI 契约
// =============================================================
// PRD-254 | Jetpack Telecom v1.1.0 VoIP Native Visibility Integration Toolkit
// 2026-05-14 Google 官博发布 | Android 16.1 SDK 36.1 Alpha
//
// MVI Architecture Pattern / MVI 架构模式
// - Model (State): Immutable data class — single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, etc.) via Channel
//
// 6 Tool Modules / 6个工具模块:
// 1. Overview         — 功能总览首页
// 2. CallLogIntegration — 通话日志接入系统拨号器
// 3. CallbackFromDialer — 系统拨号器回拨 VoIP
// 4. CallLogExclusion   — 通话日志排除（隐私）
// 5. AllowlistApply     — Secure Package Allowlist 申请
// 6. CITool             — CI 验证工具

// =============================================================
// TelecomTab — 工具Tab枚举
// =============================================================
/**
 * Telecom toolkit module tab / Telecom 工具包模块 Tab
 *
 * @param titleZh Chinese title / 中文标题
 * @param titleEn English title / 英文标题
 */
enum class TelecomTab(val titleZh: String, val titleEn: String) {
    OVERVIEW("总览", "Overview"),
    CALL_LOG_INTEGRATION("通话日志接入", "Call Log Integration"),
    CALLBACK_FROM_DIALER("拨号器回拨", "Callback from Dialer"),
    CALL_LOG_EXCLUSION("通话日志排除", "Call Log Exclusion"),
    ALLOWLIST("白名单申请", "Secure Package Allowlist"),
    CI_TOOL("CI 验证工具", "CI Validation Tool")
}

// =============================================================
// CallLogEntry — VoIP 通话日志条目
// =============================================================
/**
 * VoIP call log entry / VoIP 通话日志条目
 *
 * Represents a single VoIP call that can be submitted to the system call log.
 *
 * @param id Unique identifier / 唯一标识符
 * @param contactName Contact display name / 联系人显示名
 * @param phoneNumber Contact phone number / 联系人电话号码
 * @param timestamp Call timestamp in millis / 通话时间戳（毫秒）
 * @param duration Call duration in seconds / 通话时长（秒）
 * @param callType Call type (incoming/outgoing/missed) / 通话类型
 * @param isExcluded Whether this call is excluded from system call log / 是否已排除系统通话日志
 * @param accountHandle VoIP account handle / VoIP 账户句柄
 */
data class CallLogEntry(
    val id: String,
    val contactName: String,
    val phoneNumber: String,
    val timestamp: Long,
    val duration: Int,
    val callType: CallType,
    val isExcluded: Boolean = false,
    val accountHandle: String = ""
)

/**
 * Call type / 通话类型
 */
enum class CallType(val labelZh: String, val labelEn: String) {
    INCOMING("呼入", "Incoming"),
    OUTGOING("呼出", "Outgoing"),
    MISSED("未接", "Missed")
}

// =============================================================
// ConnectionServiceConfig — ConnectionService 配置
// =============================================================
/**
 * ConnectionService configuration for VoIP integration /
 * 用于 VoIP 集成的 ConnectionService 配置
 *
 * @param serviceClassName Fully qualified service class name / 服务类全名
 * @param label User-visible label / 用户可见标签
 * @param iconUri App icon URI for system dialer display / 系统拨号器显示的应用图标
 * @param isSupported Whether TelecomManager.isSupported() returns true / 功能是否可用
 * @param supportsCallback Whether dialer callback is supported / 是否支持拨号器回拨
 */
data class ConnectionServiceConfig(
    val serviceClassName: String = "",
    val label: String = "",
    val iconUri: String = "",
    val isSupported: Boolean = false,
    val supportsCallback: Boolean = false
)

// =============================================================
// AllowlistStatus — 白名单申请状态
// =============================================================
/**
 * Secure Package Allowlist application status /
 * Secure Package Allowlist 申请状态
 *
 * @param labelZh Chinese label / 中文标签
 * @param labelEn English label / 英文标签
 * @param color Status indicator color / 状态指示色
 */
enum class AllowlistStatus(val labelZh: String, val labelEn: String, val color: Color) {
    NOT_APPLIED("未申请", "Not Applied", Color(0xFF9E9E9E)),
    IN_REVIEW("审核中", "In Review", Color(0xFF2196F3)),
    APPROVED("已通过", "Approved", Color(0xFF4CAF50)),
    REJECTED("已拒绝", "Rejected", Color(0xFFF44336))
}

// =============================================================
// AllowlistApplication — 白名单申请记录
// =============================================================
/**
 * Secure Package Allowlist application record /
 * Secure Package Allowlist 申请记录
 *
 * @param packageName VoIP app package name / VoIP 应用包名
 * @param appName VoIP app name / VoIP 应用名
 * @param website App website URL / 应用官网
 * @param description App description / 应用描述
 * @param expectedMonthlyUsers Expected monthly active users / 预期月活用户
 * @param status Current application status / 当前申请状态
 * @param submittedAt Submission timestamp / 提交时间
 */
data class AllowlistApplication(
    val packageName: String,
    val appName: String,
    val website: String,
    val description: String,
    val expectedMonthlyUsers: String,
    val status: AllowlistStatus = AllowlistStatus.NOT_APPLIED,
    val submittedAt: Long = 0L
)

// =============================================================
// CIValidationResult — CI 验证结果
// =============================================================
/**
 * CI validation result / CI 验证结果
 *
 * @param checkName Check name / 检查项名称
 * @param passed Whether the check passed / 是否通过
 * @param message Result message / 结果消息
 * @param filePath Source file path if applicable / 源文件路径（如适用）
 * @param lineNumber Line number if applicable / 行号（如适用）
 */
data class CIValidationResult(
    val checkName: String,
    val passed: Boolean,
    val message: String,
    val filePath: String? = null,
    val lineNumber: Int? = null
)

/**
 * CI validation report / CI 验证报告
 *
 * @param totalChecks Total number of checks performed / 执行的总检查数
 * @param passedChecks Number of checks passed / 通过的检查数
 * @param failedChecks Number of checks failed / 失败的检查数
 * @param warningChecks Number of warnings / 警告数
 * @param results Individual check results / 各检查项结果
 * @param overallPassed Whether all checks passed / 所有检查是否通过
 * @param reportContent Formatted report content / 格式化的报告内容
 */
data class CIValidationReport(
    val totalChecks: Int,
    val passedChecks: Int,
    val failedChecks: Int,
    val warningChecks: Int,
    val results: List<CIValidationResult>,
    val overallPassed: Boolean,
    val reportContent: String = ""
)

// =============================================================
// TelecomState — 页面状态
// =============================================================
/**
 * Telecom Toolkit State / Telecom 工具包页面状态
 *
 * Single source of truth for the entire Telecom Toolkit UI.
 * All UI state is derived from this data class.
 *
 * @param selectedTab Currently active tab index / 当前活跃的 Tab 索引
 * @param isDarkTheme Whether dark theme is enabled / 深色模式是否启用
 * @param copiedCodeBlock ID of the most recently copied code block /
 *  最近一次复制的代码块 ID
 * @param callLogEntries Simulated VoIP call log entries / 模拟的 VoIP 通话记录列表
 * @param selectedCallLogEntry Currently selected call log entry for detail view /
 *  选中的通话记录条目
 * @param connectionServiceConfig ConnectionService configuration /
 *  ConnectionService 配置
 * @param callLogIntegrationGuideExpanded Which guide section is expanded /
 *  展开的通话日志接入指南章节
 * @param callbackGuideExpanded Which callback guide section is expanded /
 *  展开的回拨指南章节
 * @param exclusionGuideExpanded Which exclusion guide section is expanded /
 *  展开的排除指南章节
 * @param allowlistApplication Current allowlist application data /
 *  当前白名单申请数据
 * @param ciValidationReport Most recent CI validation report /
 *  最近一次 CI 验证报告
 * @param isRunningCIValidation Whether CI validation is in progress /
 *  CI 验证是否进行中
 * @param isLoading Whether any background operation is in progress /
 *  是否有后台操作进行中
 * @param error Error message if any / 错误信息
 */
data class TelecomState(
    val selectedTab: TelecomTab = TelecomTab.OVERVIEW,
    val isDarkTheme: Boolean = false,
    val copiedCodeBlock: String? = null,
    val callLogEntries: List<CallLogEntry> = emptyList(),
    val selectedCallLogEntry: CallLogEntry? = null,
    val connectionServiceConfig: ConnectionServiceConfig = ConnectionServiceConfig(),
    val callLogIntegrationGuideExpanded: Set<String> = emptySet(),
    val callbackGuideExpanded: Set<String> = emptySet(),
    val exclusionGuideExpanded: Set<String> = emptySet(),
    val allowlistApplication: AllowlistApplication = AllowlistApplication(
        packageName = "com.example.voip",
        appName = "Example VoIP",
        website = "https://example.com",
        description = "",
        expectedMonthlyUsers = ""
    ),
    val ciValidationReport: CIValidationReport? = null,
    val isRunningCIValidation: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = TelecomState()
    }

    /**
     * Get the tab index from TelecomTab enum / 通过 TelecomTab 获取索引
     */
    val selectedTabIndex: Int
        get() = TelecomTab.entries.indexOf(selectedTab)

    /**
     * Count of non-excluded call log entries / 未排除的通话记录数量
     */
    val nonExcludedCallLogCount: Int
        get() = callLogEntries.count { !it.isExcluded }

    /**
     * Count of excluded call log entries / 已排除的通话记录数量
     */
    val excludedCallLogCount: Int
        get() = callLogEntries.count { it.isExcluded }

    /**
     * Whether TelecomManager is supported on current device /
     * 当前设备是否支持 TelecomManager
     */
    val isTelecomSupported: Boolean
        get() = connectionServiceConfig.isSupported
}

// =============================================================
// TelecomIntent — 用户意图
// =============================================================
/**
 * Telecom Toolkit User Intents / Telecom 工具包用户意图
 *
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface TelecomIntent {
    /** Select a tab / 选择 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: TelecomTab) : TelecomIntent

    /** Toggle dark theme / 切换深色模式 */
    data object ToggleTheme : TelecomIntent

    /** Copy code block to clipboard / 复制代码块到剪贴板
     * @param codeBlockId Code block identifier / 代码块标识符
     * @param code Code content / 代码内容
     */
    data class CopyCodeBlock(val codeBlockId: String, val code: String) : TelecomIntent

    /** Select a call log entry for detail view /
     *  选择通话记录查看详情
     * @param entry Call log entry to select / 要选择的通话记录
     */
    data class SelectCallLogEntry(val entry: CallLogEntry) : TelecomIntent

    /** Clear selected call log entry / 清除选中的通话记录 */
    data object ClearCallLogEntry : TelecomIntent

    /** Toggle call log exclusion status / 切换通话记录排除状态
     * @param entryId Call log entry ID / 通话记录 ID
     */
    data class ToggleCallLogExclusion(val entryId: String) : TelecomIntent

    /** Expand/collapse a call log integration guide section /
     *  展开/收起通话日志接入指南章节
     * @param sectionId Section identifier / 章节标识符
     */
    data class ToggleCallLogIntegrationGuide(val sectionId: String) : TelecomIntent

    /** Expand/collapse a callback guide section /
     *  展开/收起回拨指南章节
     * @param sectionId Section identifier / 章节标识符
     */
    data class ToggleCallbackGuide(val sectionId: String) : TelecomIntent

    /** Expand/collapse an exclusion guide section /
     *  展开/收起排除指南章节
     * @param sectionId Section identifier / 章节标识符
     */
    data class ToggleExclusionGuide(val sectionId: String) : TelecomIntent

    /** Update allowlist application data /
     *  更新白名单申请数据
     * @param application Updated application data / 更新后的申请数据
     */
    data class UpdateAllowlistApplication(val application: AllowlistApplication) : TelecomIntent

    /** Submit allowlist application / 提交白名单申请 */
    data object SubmitAllowlistApplication : TelecomIntent

    /** Run CI validation / 运行 CI 验证 */
    data object RunCIValidation : TelecomIntent

    /** Check device Telecom support / 检测设备 Telecom 支持情况 */
    data object CheckTelecomSupport : TelecomIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : TelecomIntent

    /** Clear CI validation report / 清除 CI 验证报告 */
    data object ClearCIReport : TelecomIntent
}

// =============================================================
// TelecomEffect — 副作用
// =============================================================
/**
 * Telecom Toolkit Side Effects / Telecom 工具包副作用
 *
 * One-time events, consumed only once by UI layer.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface TelecomEffect {
    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error message / 是否为错误消息
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : TelecomEffect

    /** Copy code to clipboard event / 复制代码到剪贴板事件
     * @param code Code that was copied / 被复制的代码
     */
    data class CodeCopied(val code: String) : TelecomEffect

    /** Allowlist application submitted / 白名单申请已提交
     * @param appName Applied app name / 申请的应用名
     */
    data class AllowlistSubmitted(val appName: String) : TelecomEffect

    /** Open external link / 打开外部链接
     * @param url URL to open / 要打开的 URL
     */
    data class OpenExternalLink(val url: String) : TelecomEffect

    /** CI validation complete / CI 验证完成
     * @param passed Whether all checks passed / 所有检查是否通过
     */
    data class CIValidationComplete(val passed: Boolean) : TelecomEffect
}
