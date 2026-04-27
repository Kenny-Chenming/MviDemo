package com.mvi.kenny.feature.bgaudio

// ================================================================
// BackgroundAudioContract — Android 17 Background Audio Hardening MVI Contract
// ================================================================
// MVI architecture contract for Android 17 Background Audio Hardening compliance toolkit.
//
// PRD-178: Android 17 Background Audio Hardening 合规检测工具包
// Design Reference: memory/agency/designs/PRD-178-Android-17-Background-Audio-Hardening-合规检测工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * ComplianceStatus — 合规状态枚举
 * ============================================================
 * Represents the compliance status of an audio tool check.
 *
 * @param displayName Chinese display name
 * @param emoji Emoji representation
 * @param color Status color
 */
enum class ComplianceStatus(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    PASS("合规", "✅", Color(0xFF3FB950)),
    WARNING("警告", "⚠️", Color(0xFFFFD700)),
    FAIL("不合规", "❌", Color(0xFFF85149)),
    UNKNOWN("未检测", "❓", Color(0xFF8B949E))
}

/**
 * ============================================================
 * ToolId — 工具 ID 常量
 * ============================================================
 */
object ToolId {
    const val SILENT_FAILURE_DETECTOR = "silent_failure_detector"
    const val FGS_CONFIG_VALIDATOR = "fgs_config_validator"
    const val AUDIO_FOCUS_CHECKER = "audio_focus_checker"
    const val CI_SCENARIO_SIMULATOR = "ci_scenario_simulator"
    const val DEGRADATION_STRATEGY = "degradation_strategy"
    const val BOOT_AUDIO_MIGRATION = "boot_audio_migration"
    const val BLUETOOTH_AUDIO_GUIDE = "bluetooth_audio_guide"
    const val AUDIO_ATTRIBUTES_GUIDE = "audio_attributes_guide"
    const val FOCUS_LOSS_MONITOR = "focus_loss_monitor"
}

/**
 * ============================================================
 * BackgroundAudioState — 主状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 *
 * @param complianceScore Overall compliance score (0-100)
 * @param totalTools Total number of tools (9)
 * @param failingTools Number of non-compliant tools
 * @param toolStatuses Status of each tool
 * @param isRefreshing Whether the dashboard is refreshing
 * @param error Error message if any
 * @param selectedToolId Currently selected tool ID (null = dashboard)
 * @param toolDetailStates Map of tool-specific detail states
 */
data class BackgroundAudioState(
    val complianceScore: Int = 0,
    val totalTools: Int = 9,
    val failingTools: Int = 0,
    val toolStatuses: List<ToolStatus> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedToolId: String? = null,
    val toolDetailStates: Map<String, ToolDetailState> = emptyMap()
) {
    companion object {
        val Initial = BackgroundAudioState()
    }
}

/**
 * ============================================================
 * ToolStatus — 工具状态
 * ============================================================
 *
 * @param toolId Tool ID
 * @param toolName Tool display name (Chinese)
 * @param toolNameEn Tool display name (English)
 * @param toolDescription Brief description
 * @param status Compliance status
 * @param iconEmoji Tool icon emoji
 */
data class ToolStatus(
    val toolId: String,
    val toolName: String,
    val toolNameEn: String,
    val toolDescription: String,
    val status: ComplianceStatus,
    val iconEmoji: String
)

/**
 * ============================================================
 * ToolDetailState — 工具详情状态（通用模板）
 * ============================================================
 *
 * @param toolId Tool ID
 * @param toolName Tool display name
 * @param toolDescription Detailed description
 * @param config Tool-specific configuration parameters
 * @param isRunning Whether the tool is currently running
 * @param progress Progress (0f-1f)
 * @param report Generated report (if any)
 * @param error Error message
 */
data class ToolDetailState(
    val toolId: String,
    val toolName: String,
    val toolDescription: String,
    val config: Map<String, Any> = emptyMap(),
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val report: ToolReport? = null,
    val error: String? = null,
    // Silent Failure Detector specific
    val silentFailureEvents: List<SilentFailureEvent> = emptyList(),
    // FGS Config Validator specific
    val fgsConfigItems: List<FGSConfigItem> = emptyList(),
    // Audio Focus Checker specific
    val audioFocusResults: List<AudioFocusResult> = emptyList(),
    // CI Simulator specific
    val selectedScenarios: Set<CIScenario> = emptySet(),
    val ciTestResults: List<CITestResult> = emptyList(),
    // Degradation Strategy specific
    val selectedStrategy: DegradationStrategyTemplate? = null,
    val generatedCode: String? = null,
    // BOOT Audio Migration specific
    val bootCompletedCode: String? = null,
    val migrationDiff: String? = null,
    // Bluetooth Audio Guide specific
    val bluetoothPermissionStatus: BluetoothPermissionStatus? = null,
    // AudioAttributes Guide specific
    val audioAttributesItems: List<AudioAttributesItem> = emptyList(),
    // Focus Loss Monitor specific
    val focusLossCallbacks: List<FocusLossCallback> = emptyList()
)

/**
 * ============================================================
 * ToolReport — 工具报告
 * ============================================================
 *
 * @param toolId Tool ID
 * @param title Report title
 * @param summary Report summary
 * @param status Overall status
 * @param findings List of findings
 * @param recommendations List of recommendations
 * @param generatedAt Timestamp
 * @param format Report format: "markdown" or "json"
 */
data class ToolReport(
    val toolId: String,
    val title: String,
    val summary: String,
    val status: ComplianceStatus,
    val findings: List<Finding> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis(),
    val format: String = "markdown"
)

/**
 * ============================================================
 * Finding — 检测发现
 * ============================================================
 *
 * @param severity Severity: HIGH / MEDIUM / LOW
 * @param title Finding title
 * @param description Finding description
 * @param location Code location
 * @param fixSuggestion Suggested fix
 */
data class Finding(
    val severity: String,
    val title: String,
    val description: String,
    val location: String,
    val fixSuggestion: String
)

/**
 * ============================================================
 * SilentFailureEvent — 静默失败事件
 * ============================================================
 *
 * @param timestamp Event timestamp
 * @param apiName API name (e.g., "AudioTrack.start()")
 * @param reason Failure reason
 * @param stackTrace Stack trace or log snippet
 */
data class SilentFailureEvent(
    val timestamp: Long,
    val apiName: String,
    val reason: String,
    val stackTrace: String
)

/**
 * ============================================================
 * FGSConfigItem — FGS 配置检查项
 * ============================================================
 *
 * @param itemName Check item name
 * @param description Description
 * @param isCompliant Whether compliant
 * @param currentValue Current value in the project
 * @param expectedValue Expected value
 * @param fixSuggestion Fix suggestion
 */
data class FGSConfigItem(
    val itemName: String,
    val description: String,
    val isCompliant: Boolean,
    val currentValue: String,
    val expectedValue: String,
    val fixSuggestion: String
)

/**
 * ============================================================
 * AudioFocusResult — 音频焦点检测结果
 * ============================================================
 *
 * @param apiCall AudioFocus API call location
 * @param context Usage context (foreground/background)
 * @param willSucceedInBackground Whether it will succeed in background
 * @param recommendation Recommendation
 */
data class AudioFocusResult(
    val apiCall: String,
    val context: String,
    val willSucceedInBackground: Boolean,
    val recommendation: String
)

/**
 * ============================================================
 * CIScenario — CI 场景枚举
 * ============================================================
 *
 * @param id Scenario ID
 * @param name Scenario name
 * @param description Scenario description
 */
enum class CIScenario(
    val id: String,
    val displayName: String,
    val displayNameEn: String,
    val description: String
) {
    ENTER_BACKGROUND("enter_background", "App 进入后台", "App Enters Background",
        "Simulate App entering background and verify audio behavior"),
    RETURN_FOREGROUND("return_foreground", "App 返回前台", "App Returns to Foreground",
        "Simulate App returning to foreground after background"),
    BLUETOOTH_DISCONNECT("bluetooth_disconnect", "蓝牙断开", "Bluetooth Disconnect",
        "Simulate Bluetooth device disconnection during audio playback"),
    BOOT_COMPLETED("boot_completed", "BOOT_COMPLETED 启动", "BOOT_COMPLETED Boot",
        "Simulate BOOT_COMPLETED triggered background FGS audio attempt")
}

/**
 * ============================================================
 * CITestResult — CI 测试结果
 * ============================================================
 *
 * @param scenario The tested scenario
 * @param passed Whether the test passed
 * @param audioStateSnapshot Audio state at test completion
 * @param notes Test notes
 */
data class CITestResult(
    val scenario: CIScenario,
    val passed: Boolean,
    val audioStateSnapshot: String,
    val notes: String
)

/**
 * ============================================================
 * DegradationStrategyTemplate — 降级策略模板
 * ============================================================
 *
 * @param id Template ID
 * @param title Strategy title
 * @param description Strategy description
 * @param codeTemplate Kotlin code template
 * @param integrationSteps Integration steps
 */
data class DegradationStrategyTemplate(
    val id: String,
    val title: String,
    val description: String,
    val codeTemplate: String,
    val integrationSteps: String
)

/**
 * ============================================================
 * BluetoothPermissionStatus — 蓝牙权限状态
 * ============================================================
 *
 * @param hasBluetoothConnectPermission Whether BLUETOOTH_CONNECT is declared
 * @param hasBluetoothScanPermission Whether BLUETOOTH_SCAN is declared
 * @param hasAudioPlaybackService Whether audio playback service is declared
 * @param autoPlayOnConnect Whether auto-play on connect is implemented
 */
data class BluetoothPermissionStatus(
    val hasBluetoothConnectPermission: Boolean,
    val hasBluetoothScanPermission: Boolean,
    val hasAudioPlaybackService: Boolean,
    val autoPlayOnConnect: Boolean
)

/**
 * ============================================================
 * AudioAttributesItem — AudioAttributes 检查项
 * ============================================================
 *
 * @param attributeKey AudioAttributes key (e.g., "USAGE_MEDIA")
 * @param contentType Content type
 * @param flags Flags
 * @param isCompliant Whether compliant with Android 17
 * @param android17Change Description of Android 17 changes
 * @param recommendation Recommendation
 */
data class AudioAttributesItem(
    val attributeKey: String,
    val contentType: String,
    val flags: String,
    val isCompliant: Boolean,
    val android17Change: String,
    val recommendation: String
)

/**
 * ============================================================
 * FocusLossCallback — 焦点丢失回调检查
 * ============================================================
 *
 * @param listenerClass The class implementing OnAudioFocusChangeListener
 * val callbackMethods Methods that handle focus loss (AUDIOFOCUS_LOSS, etc.)
 * @param isComplete Whether all focus loss scenarios are handled
 * @param missingHandlers List of unhandled focus loss types
 */
data class FocusLossCallback(
    val listenerClass: String,
    val callbackMethods: List<String>,
    val isComplete: Boolean,
    val missingHandlers: List<String>
)

// ================================================================
// BackgroundAudioIntent — 用户意图（User Intent）
// ================================================================

/**
 * ============================================================
 * BackgroundAudioIntent — 用户意图
 * ============================================================
 * Every user action corresponds to an Intent.
 *
 * @see BackgroundAudioViewModel.sendIntent handles all Intents
 */
sealed interface BackgroundAudioIntent {

    /** 用户点击刷新仪表盘 */
    data object RefreshDashboard : BackgroundAudioIntent

    /** 用户点击工具卡片
     * @param toolId Tool ID
     */
    data class SelectTool(val toolId: String) : BackgroundAudioIntent

    /** 用户从工具详情页返回仪表盘 */
    data object BackToDashboard : BackgroundAudioIntent

    /** 用户在工具详情页配置参数后点击运行
     * @param toolId Tool ID
     * @param config Configuration parameters
     */
    data class RunTool(val toolId: String, val config: Map<String, Any>) : BackgroundAudioIntent

    /** 用户停止正在运行的工具
     * @param toolId Tool ID
     */
    data class StopTool(val toolId: String) : BackgroundAudioIntent

    /** 用户导出报告
     * @param toolId Tool ID
     * @param format Export format: "markdown" or "json"
     */
    data class ExportReport(val toolId: String, val format: String) : BackgroundAudioIntent

    /** 用户选择 CI 场景
     * @param scenarios Selected scenarios
     */
    data class SelectCIScenarios(val scenarios: Set<CIScenario>) : BackgroundAudioIntent

    /** 用户选择降级策略模板
     * @param template Selected template
     */
    data class SelectDegradationTemplate(val template: DegradationStrategyTemplate) : BackgroundAudioIntent

    /** 用户清除错误 */
    data object ClearError : BackgroundAudioIntent
}

// ================================================================
// BackgroundAudioEffect — 一次性副作用（Effect）
// ================================================================

/**
 * ============================================================
 * BackgroundAudioEffect — 一次性副作用
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 *
 * @see BackgroundAudioViewModel _effect.send() sends Effects
 */
sealed interface BackgroundAudioEffect {

    /** 显示 Toast 消息
     * @param message Toast text
     */
    data class ShowToast(val message: String) : BackgroundAudioEffect

    /** 复制内容到剪贴板
     * @param content Content to copy
     */
    data class CopyToClipboard(val content: String) : BackgroundAudioEffect

    /** 导航到相关工具
     * @param toolId Target tool ID
     */
    data class NavigateToRelatedTool(val toolId: String) : BackgroundAudioEffect

    /** 分享报告
     * @param content Report content
     * @param title Report title
     */
    data class ShareReport(val content: String, val title: String) : BackgroundAudioEffect
}

// ================================================================
// 默认数据 / Default Data
// ================================================================

/**
 * Default tool list for the dashboard.
 * Each tool has a unique ID, Chinese/English names, description, and initial status.
 */
val defaultToolStatuses = listOf(
    ToolStatus(
        toolId = ToolId.SILENT_FAILURE_DETECTOR,
        toolName = "静默失败检测器",
        toolNameEn = "Silent Failure Detector",
        toolDescription = "监控 App 音频操作被 Android 17 静默拦截的情况，解析系统日志输出诊断报告",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "🔊"
    ),
    ToolStatus(
        toolId = ToolId.FGS_CONFIG_VALIDATOR,
        toolName = "FGS 配置验证器",
        toolNameEn = "FGS Config Validator",
        toolDescription = "检测 App 的 foregroundServiceType=\"mediaPlayback\" 配置是否满足 Android 17 while-in-use 要求",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "⏱️"
    ),
    ToolStatus(
        toolId = ToolId.AUDIO_FOCUS_CHECKER,
        toolName = "音频焦点合规检测",
        toolNameEn = "Audio Focus Compliance Checker",
        toolDescription = "检测 App 的 AudioFocus 请求在后台场景下是否会被静默拒绝",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "🎯"
    ),
    ToolStatus(
        toolId = ToolId.CI_SCENARIO_SIMULATOR,
        toolName = "CI 场景模拟器",
        toolNameEn = "CI Scenario Simulator",
        toolDescription = "模拟 App 进入后台/返回前台/蓝牙连接断开等场景，验证音频行为正确性",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "🔄"
    ),
    ToolStatus(
        toolId = ToolId.DEGRADATION_STRATEGY,
        toolName = "降级策略模板",
        toolNameEn = "Degradation Strategy Template",
        toolDescription = "App 无法播放音频时的优雅降级 UX：状态保留/通知提示/恢复引导",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "📉"
    ),
    ToolStatus(
        toolId = ToolId.BOOT_AUDIO_MIGRATION,
        toolName = "BOOT+音频迁移指南",
        toolNameEn = "BOOT + Audio Migration Guide",
        toolDescription = "从后台启动到 while-in-use FGS 的迁移路径和代码 Diff",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "🔌"
    ),
    ToolStatus(
        toolId = ToolId.BLUETOOTH_AUDIO_GUIDE,
        toolName = "蓝牙自动播放指南",
        toolNameEn = "Bluetooth Auto-Play Guide",
        toolDescription = "蓝牙连接/断开时的音频行为配置，正确声明 BLUETOOTH_CONNECT 权限",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "🔗"
    ),
    ToolStatus(
        toolId = ToolId.AUDIO_ATTRIBUTES_GUIDE,
        toolName = "AudioAttributes 解读",
        toolNameEn = "AudioAttributes Guide",
        toolDescription = "Android 17 下 AudioAttributes 的配置变化和最佳实践",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "🎨"
    ),
    ToolStatus(
        toolId = ToolId.FOCUS_LOSS_MONITOR,
        toolName = "焦点丢失回调监控",
        toolNameEn = "Focus Loss Callback Monitor",
        toolDescription = "验证 App 的 OnAudioFocusChangeListener 是否正确处理焦点丢失场景",
        status = ComplianceStatus.UNKNOWN,
        iconEmoji = "🎤"
    )
)

/**
 * Default degradation strategy templates.
 */
val defaultDegradationTemplates = listOf(
    DegradationStrategyTemplate(
        id = "deg_001",
        title = "状态保留 + 通知提示",
        description = "音频无法播放时，保留当前播放状态，弹出通知引导用户返回 App",
        codeTemplate = """
// Audio Degradation Strategy: State Preservation + Notification
class AudioDegradationManager(private val context: Context) {

    private var suspendedAudioState: AudioState? = null
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    /**
     * Called when audio playback fails silently in background.
     * Suspends playback, preserves state, shows notification.
     */
    fun onAudioSilentlyFailed(reason: String) {
        // Step 1: Preserve current state
        suspendedAudioState = captureCurrentAudioState()
        
        // Step 2: Show notification with action to restore
        showRestorationNotification()
        
        // Step 3: Log for diagnostics
        android.util.Log.w("AudioDegrade", "Audio silently failed: ${'$'}reason")
    }

    private fun captureCurrentAudioState(): AudioState {
        // Capture: trackId, position, volume, etc.
        return AudioState(/* current playback info */)
    }

    private fun showRestorationNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("RESTORE_AUDIO", true)
        }
        val notification = Notification.Builder(context, "audio_restore")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Audio Paused")
            .setContentText("Tap to restore playback")
            .setContentIntent(PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
            ))
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Restore audio when user returns to foreground.
     */
    fun restoreAudio() {
        suspendedAudioState?.let { state ->
            resumePlayback(state)
            suspendedAudioState = null
        }
    }
}
        """.trimIndent(),
        integrationSteps = """
集成步骤：
1. 在 Application 或 AudioService 中初始化 AudioDegradationManager
2. 在所有音频 API 调用处添加 try-catch，失败时调用 onAudioSilentlyFailed()
3. 在 App onResume() 中调用 restoreAudio()
4. 创建 "audio_restore" NotificationChannel (API 26+)
        """
    ),
    DegradationStrategyTemplate(
        id = "deg_002",
        title = "while-in-use FGS 降级",
        description = "当 FGS 无法获取 while-in-use 状态时，优雅降级到前台通知模式",
        codeTemplate = """
// while-in-use FGS Degradation Strategy
class WhileInUseDegradation {

    /**
     * Check if device supports while-in-use FGS.
     * If not, fall back to standard foreground service.
     */
    fun startMediaPlaybackWithFallback(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 17+: Try while-in-use FGS
            startWhileInUseFGS(context)
        } else {
            // Fallback: Standard FGS with notification
            startStandardFGS(context)
        }
    }

    private fun startWhileInUseFGS(context: Context) {
        val serviceIntent = Intent(context, MediaPlaybackService::class.java)
        context.startForegroundService(serviceIntent)
        // Note: Audio will silently fail if App lacks visible activity
    }

    private fun startStandardFGS(context: Context) {
        val serviceIntent = Intent(context, MediaPlaybackService::class.java)
        context.startForegroundService(serviceIntent)
        // Show persistent notification to maintain foreground state
    }
}
        """.trimIndent(),
        integrationSteps = """
集成步骤：
1. 创建 MediaPlaybackService 继承 Service
2. 在 startForeground() 前检查 Android 版本
3. 配置 AndroidManifest: foregroundServiceType="mediaPlayback"
4. 添加 POST_NOTIFICATIONS 权限 (Android 13+)
        """
    )
)
