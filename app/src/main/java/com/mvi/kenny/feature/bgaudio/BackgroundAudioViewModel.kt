package com.mvi.kenny.feature.bgaudio

// ================================================================
// BackgroundAudioViewModel — Android 17 Background Audio Hardening MVI ViewModel
// ================================================================
// ViewModel for Android 17 Background Audio Hardening compliance toolkit.
//
// PRD-178: Android 17 Background Audio Hardening 合规检测工具包
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Display dashboard with 9 tool cards and overall compliance score
//   - Route to tool detail screens on card click
//   - Run tool-specific analysis/simulation/generation
//   - Generate formatted reports (Markdown/JSON)
//   - Expose one-time Effects (navigation, toast, errors)
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================
 * BackgroundAudioViewModel — Background Audio 工具 ViewModel
 * ============================================================
 * Manages the BackgroundAudioState and processes BackgroundAudioIntent.
 *
 * In a real implementation, this would:
 *   - Parse AndroidManifest.xml for FGS configuration
 *   - Use static analysis to detect AudioFocus API usage
 *   - Parse adb logcat output for silent failure detection
 *   - Generate CI simulation scenarios
 *
 * Current implementation uses simulated data for demonstration.
 *
 * @see BackgroundAudioState
 * @see BackgroundAudioIntent
 * @see BackgroundAudioEffect
 */
class BackgroundAudioViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(BackgroundAudioState.Initial)
    val state: StateFlow<BackgroundAudioState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via SharedFlow
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<BackgroundAudioEffect>()
    val effect = _effect.asSharedFlow()

    // ─────────────────────────────────────────────────────────────
    // Internal state
    // ─────────────────────────────────────────────────────────────
    private var toolJobs: MutableMap<String, Job> = mutableMapOf()

    init {
        // Initialize dashboard with default tools
        _update {
            copy(
                toolStatuses = defaultToolStatuses,
                toolDetailStates = createInitialDetailStates()
            )
        }
        // Simulate initial scan to get mock compliance score
        simulateInitialScan()
    }

    /**
     * ============================================================
     * sendIntent — Intent 分发入口
     * ============================================================
     * All user intents flow through this single entry point.
     * ViewModel processes the intent and updates State or sends Effect.
     *
     * @param intent The intent to process
     */
    fun sendIntent(intent: BackgroundAudioIntent) {
        when (intent) {
            is BackgroundAudioIntent.RefreshDashboard -> handleRefreshDashboard()
            is BackgroundAudioIntent.SelectTool -> handleSelectTool(intent.toolId)
            is BackgroundAudioIntent.BackToDashboard -> handleBackToDashboard()
            is BackgroundAudioIntent.RunTool -> handleRunTool(intent.toolId, intent.config)
            is BackgroundAudioIntent.StopTool -> handleStopTool(intent.toolId)
            is BackgroundAudioIntent.ExportReport -> handleExportReport(intent.toolId, intent.format)
            is BackgroundAudioIntent.SelectCIScenarios -> handleSelectCIScenarios(intent.scenarios)
            is BackgroundAudioIntent.SelectDegradationTemplate -> handleSelectDegradationTemplate(intent.template)
            is BackgroundAudioIntent.ClearError -> _update { copy(error = null) }
        }
    }

    // ============================================================
    // Intent Handlers
    // ============================================================

    /**
     * Handle dashboard refresh — re-runs all tool scans.
     */
    private fun handleRefreshDashboard() {
        _update { copy(isRefreshing = true) }
        viewModelScope.launch {
            try {
                delay(1500) // Simulate scanning
                // Re-run all tool scans with simulated results
                val updatedStatuses = defaultToolStatuses.map { tool ->
                    tool.copy(status = simulateToolStatus(tool.toolId))
                }
                val passingCount = updatedStatuses.count { it.status == ComplianceStatus.PASS }
                val score = (passingCount * 100) / 9

                _update {
                    copy(
                        toolStatuses = updatedStatuses,
                        isRefreshing = false,
                        complianceScore = score,
                        failingTools = updatedStatuses.count {
                            it.status == ComplianceStatus.FAIL || it.status == ComplianceStatus.WARNING
                        }
                    )
                }
                _effect.emit(BackgroundAudioEffect.ShowToast("刷新完成，合规评分: $score/100"))
            } catch (e: Exception) {
                _update { copy(isRefreshing = false, error = e.message) }
            }
        }
    }

    /**
     * Handle tool card click — navigate to tool detail screen.
     *
     * @param toolId Selected tool ID
     */
    private fun handleSelectTool(toolId: String) {
        _update { copy(selectedToolId = toolId) }
    }

    /**
     * Handle back navigation from tool detail to dashboard.
     */
    private fun handleBackToDashboard() {
        _update { copy(selectedToolId = null) }
    }

    /**
     * Handle tool execution request.
     *
     * @param toolId Tool ID to run
     * @param config Tool-specific configuration
     */
    private fun handleRunTool(toolId: String, config: Map<String, Any>) {
        toolJobs[toolId]?.cancel()
        toolJobs[toolId] = viewModelScope.launch {
            _updateToolState(toolId) {
                copy(isRunning = true, progress = 0f, report = null, error = null)
            }
            try {
                when (toolId) {
                    ToolId.SILENT_FAILURE_DETECTOR -> runSilentFailureDetector()
                    ToolId.FGS_CONFIG_VALIDATOR -> runFGSValidator()
                    ToolId.AUDIO_FOCUS_CHECKER -> runAudioFocusChecker()
                    ToolId.CI_SCENARIO_SIMULATOR -> runCISimulator()
                    ToolId.DEGRADATION_STRATEGY -> runDegradationStrategy()
                    ToolId.BOOT_AUDIO_MIGRATION -> runBootAudioMigration()
                    ToolId.BLUETOOTH_AUDIO_GUIDE -> runBluetoothAudioGuide()
                    ToolId.AUDIO_ATTRIBUTES_GUIDE -> runAudioAttributesGuide()
                    ToolId.FOCUS_LOSS_MONITOR -> runFocusLossMonitor()
                }
            } catch (e: Exception) {
                _updateToolState(toolId) {
                    copy(isRunning = false, error = e.message)
                }
            }
        }
    }

    /**
     * Stop a running tool.
     *
     * @param toolId Tool ID to stop
     */
    private fun handleStopTool(toolId: String) {
        toolJobs[toolId]?.cancel()
        toolJobs.remove(toolId)
        _updateToolState(toolId) {
            copy(isRunning = false, progress = 0f)
        }
    }

    /**
     * Export tool report in specified format.
     *
     * @param toolId Tool ID
     * @param format Export format: "markdown" or "json"
     */
    private fun handleExportReport(toolId: String, format: String) {
        viewModelScope.launch {
            val toolState = _state.value.toolDetailStates[toolId]
            val report = toolState?.report
            if (report == null) {
                _effect.emit(BackgroundAudioEffect.ShowToast("请先生成报告"))
                return@launch
            }
            val content = formatReport(report, format)
            _effect.emit(BackgroundAudioEffect.CopyToClipboard(content))
            _effect.emit(BackgroundAudioEffect.ShowToast("报告已复制到剪贴板"))
        }
    }

    /**
     * Handle CI scenario selection.
     *
     * @param scenarios Selected scenarios
     */
    private fun handleSelectCIScenarios(scenarios: Set<CIScenario>) {
        _state.value.toolDetailStates[ToolId.CI_SCENARIO_SIMULATOR]?.let { toolState ->
            val updatedState = toolState.copy(
                config = toolState.config + mapOf("selectedScenarios" to scenarios),
                selectedScenarios = scenarios
            )
            _update {
                copy(toolDetailStates = toolDetailStates + (ToolId.CI_SCENARIO_SIMULATOR to updatedState))
            }
        }
    }

    /**
     * Handle degradation template selection.
     *
     * @param template Selected template
     */
    private fun handleSelectDegradationTemplate(template: DegradationStrategyTemplate) {
        _updateToolState(ToolId.DEGRADATION_STRATEGY) {
            copy(selectedStrategy = template, generatedCode = template.codeTemplate)
        }
    }

    // ============================================================
    // Tool Runners
    // ============================================================

    /**
     * Run Silent Failure Detector tool.
     * Simulates detecting silent audio failures from logcat analysis.
     */
    private suspend fun runSilentFailureDetector() {
        val toolId = ToolId.SILENT_FAILURE_DETECTOR
        repeat(10) { step ->
            _updateToolState(toolId) { copy(progress = (step + 1) / 10f) }
            delay(200)
        }
        val events = listOf(
            SilentFailureEvent(
                timestamp = System.currentTimeMillis() - 3600000,
                apiName = "AudioTrack.start()",
                reason = "App lacks visible activity — silent failure",
                stackTrace = "android.media.AudioTrack.checkStateAndPermission()\n  → AUDIOFOCUS_REQUEST_FAILED"
            ),
            SilentFailureEvent(
                timestamp = System.currentTimeMillis() - 7200000,
                apiName = "AudioManager.requestAudioFocus()",
                reason = "Background audio focus request silently denied",
                stackTrace = "android.media.AudioManager.dispatchFocusChange()\n  → AUDIOFOCUS_NONE"
            )
        )
        val report = ToolReport(
            toolId = toolId,
            title = "Silent Audio Failure Detection Report",
            summary = "检测到 2 个静默失败事件。AudioTrack 和 AudioFocus 在后台场景下被系统静默拦截。",
            status = ComplianceStatus.FAIL,
            findings = listOf(
                Finding(
                    severity = "HIGH",
                    title = "AudioTrack.start() 静默失败",
                    description = "App 在后台调用 AudioTrack.start() 时，系统静默拦截，未抛出异常",
                    location = "com.example.app.AudioPlayer.kt:234",
                    fixSuggestion = "确保调用 AudioTrack.start() 前 App 处于前台可见状态，或使用 while-in-use FGS"
                ),
                Finding(
                    severity = "HIGH",
                    title = "AudioFocus 请求静默失败",
                    description = "后台 AudioFocus 请求被系统静默拒绝",
                    location = "com.example.app.AudioManager.kt:89",
                    fixSuggestion = "在请求 AudioFocus 前检查 App 生命周期状态"
                )
            ),
            recommendations = listOf(
                "在音频播放前检查 Lifecycle.State 为 RESUMED 或可见状态",
                "使用 MediaSession 配合 FGS 确保音频在后台正确播放",
                "在 CI 中添加 logcat 解析检测静默失败"
            )
        )
        _updateToolState(toolId) {
            copy(isRunning = false, progress = 1f, report = report, silentFailureEvents = events)
        }
        updateToolDashboardStatus(toolId, ComplianceStatus.FAIL)
    }

    /**
     * Run FGS Config Validator tool.
     */
    private suspend fun runFGSValidator() {
        val toolId = ToolId.FGS_CONFIG_VALIDATOR
        repeat(10) { step ->
            _updateToolState(toolId) { copy(progress = (step + 1) / 10f) }
            delay(150)
        }
        val items = listOf(
            FGSConfigItem(
                itemName = "foregroundServiceType 声明",
                description = "检测 AndroidManifest.xml 是否声明 mediaPlayback 类型",
                isCompliant = true,
                currentValue = "foregroundServiceType=\"mediaPlayback\"",
                expectedValue = "foregroundServiceType=\"mediaPlayback\"",
                fixSuggestion = "无需修改"
            ),
            FGSConfigItem(
                itemName = "startForeground 调用时机",
                description = "检测 startForeground() 是否在音频开始播放前调用",
                isCompliant = false,
                currentValue = "startForeground() 在 onCreate() 中调用",
                expectedValue = "startForeground() 应在用户可感知音频时调用",
                fixSuggestion = "将 startForeground() 调用移动到音频实际开始播放的时机"
            ),
            FGSConfigItem(
                itemName = "WHILE_IN_USE foreground service 权限",
                description = "检测 Android 17 while-in-use FGS 权限配置",
                isCompliant = true,
                currentValue = "FOREGROUND_SERVICE_MEDIA_PLAYING 权限已声明",
                expectedValue = "同 foregroundServiceType=\"mediaPlayback\"",
                fixSuggestion = "无需修改"
            ),
            FGSConfigItem(
                itemName = "POST_NOTIFICATIONS 权限 (Android 13+)",
                description = "检测通知权限是否正确申请",
                isCompliant = true,
                currentValue = "POST_NOTIFICATIONS 权限已申请",
                expectedValue = "Android 13+ 必须申请",
                fixSuggestion = "无需修改"
            )
        )
        val failingCount = items.count { !it.isCompliant }
        val report = ToolReport(
            toolId = toolId,
            title = "FGS Configuration Compliance Report",
            summary = "共 4 项检查，${items.size - failingCount} 项通过，$failingCount 项不合规",
            status = if (failingCount > 0) ComplianceStatus.WARNING else ComplianceStatus.PASS,
            findings = items.filter { !it.isCompliant }.map { item ->
                Finding(
                    severity = "HIGH",
                    title = "FGS 配置不合规: ${item.itemName}",
                    description = item.description,
                    location = "AndroidManifest.xml",
                    fixSuggestion = item.fixSuggestion
                )
            },
            recommendations = items.filter { !it.isCompliant }.map { it.fixSuggestion }
        )
        _updateToolState(toolId) {
            copy(isRunning = false, progress = 1f, report = report, fgsConfigItems = items)
        }
        updateToolDashboardStatus(toolId, report.status)
    }

    /**
     * Run Audio Focus Checker tool.
     */
    private suspend fun runAudioFocusChecker() {
        val toolId = ToolId.AUDIO_FOCUS_CHECKER
        repeat(10) { step ->
            _updateToolState(toolId) { copy(progress = (step + 1) / 10f) }
            delay(180)
        }
        val results = listOf(
            AudioFocusResult(
                apiCall = "audioManager.requestAudioFocus(focusChangeListener, STREAM_MUSIC, AUDIOFOCUS_GAIN)",
                context = "前台 Activity",
                willSucceedInBackground = false,
                recommendation = "在请求 AudioFocus 前检查 App 是否处于前台可见状态"
            ),
            AudioFocusResult(
                apiCall = "audioManager.abandonAudioFocus(focusChangeListener)",
                context = "Service.onDestroy()",
                willSucceedInBackground = true,
                recommendation = "正确实现在 Service 销毁时放弃音频焦点"
            )
        )
        val report = ToolReport(
            toolId = toolId,
            title = "Audio Focus Compliance Report",
            summary = "检测到 1 个 AudioFocus 后台场景警告",
            status = ComplianceStatus.WARNING,
            findings = listOf(
                Finding(
                    severity = "MEDIUM",
                    title = "AudioFocus 后台请求失败",
                    description = "AudioFocus 在后台请求会被静默拒绝",
                    location = "com.example.app.AudioService.kt:156",
                    fixSuggestion = "添加 App 生命周期检查，确保仅在前台时请求音频焦点"
                )
            ),
            recommendations = listOf(
                "在 requestAudioFocus() 前检查 Lifecycle.State",
                "添加 AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK 用于短暂中断场景",
                "正确实现 OnAudioFocusChangeListener 处理焦点丢失"
            )
        )
        _updateToolState(toolId) {
            copy(isRunning = false, progress = 1f, report = report, audioFocusResults = results)
        }
        updateToolDashboardStatus(toolId, ComplianceStatus.WARNING)
    }

    /**
     * Run CI Scenario Simulator tool.
     */
    private suspend fun runCISimulator() {
        val toolId = ToolId.CI_SCENARIO_SIMULATOR
        repeat(10) { step ->
            _updateToolState(toolId) { copy(progress = (step + 1) / 10f) }
            delay(200)
        }
        val results = listOf(
            CITestResult(
                scenario = CIScenario.ENTER_BACKGROUND,
                passed = false,
                audioStateSnapshot = "AudioTrack state: IDLE (silently stopped)",
                notes = "音频在后台被静默停止，未调用 stop()"
            ),
            CITestResult(
                scenario = CIScenario.BLUETOOTH_DISCONNECT,
                passed = false,
                audioStateSnapshot = "AudioFocus: LOST",
                notes = "蓝牙断开后音频焦点丢失，但 App 未正确处理"
            ),
            CITestResult(
                scenario = CIScenario.BOOT_COMPLETED,
                passed = false,
                audioStateSnapshot = "AudioTrack.start() returned but no sound",
                notes = "BOOT_COMPLETED 触发的 FGS 无法播放音频（Android 17 行为）"
            )
        )
        val report = ToolReport(
            toolId = toolId,
            title = "CI Scenario Simulation Report",
            summary = "测试 3 个场景，0 个通过，3 个失败",
            status = ComplianceStatus.FAIL,
            findings = results.filter { !it.passed }.map { result ->
                Finding(
                    severity = "HIGH",
                    title = "场景失败: ${result.scenario.displayName}",
                    description = result.notes,
                    location = "CI Pipeline",
                    fixSuggestion = "参考对应工具的修复建议"
                )
            },
            recommendations = listOf(
                "在 App 进入后台时保存音频状态并在返回前台时恢复",
                "添加蓝牙断开时的 AudioFocus 丢失处理",
                "BOOT_COMPLETED 场景需要用户主动触发才能播放音频"
            )
        )
        _updateToolState(toolId) {
            copy(isRunning = false, progress = 1f, report = report, ciTestResults = results)
        }
        updateToolDashboardStatus(toolId, ComplianceStatus.FAIL)
    }

    /**
     * Run Degradation Strategy template generator.
     */
    private suspend fun runDegradationStrategy() {
        val toolId = ToolId.DEGRADATION_STRATEGY
        repeat(10) { step ->
            _updateToolState(toolId) { copy(progress = (step + 1) / 10f) }
            delay(150)
        }
        val template = defaultDegradationTemplates.first()
        val report = ToolReport(
            toolId = toolId,
            title = "Audio Degradation Strategy Generated",
            summary = "已生成「${template.title}」代码模板",
            status = ComplianceStatus.PASS,
            findings = emptyList(),
            recommendations = listOf(
                "将模板代码集成到项目的 AudioService",
                "配置 NotificationChannel (API 26+)",
                "在 CI 中验证降级策略"
            )
        )
        _updateToolState(toolId) {
            copy(
                isRunning = false,
                progress = 1f,
                report = report,
                selectedStrategy = template,
                generatedCode = template.codeTemplate
            )
        }
        updateToolDashboardStatus(toolId, ComplianceStatus.PASS)
    }

    /**
     * Run BOOT Audio Migration guide generator.
     */
    private suspend fun runBootAudioMigration() {
        val toolId = ToolId.BOOT_AUDIO_MIGRATION
        repeat(10) { step ->
            _updateToolState(toolId) { copy(progress = (step + 1) / 10f) }
            delay(180)
        }
        val migrationDiff = """
// BOOT_COMPLETED + Audio Migration Diff
// From: Direct FGS audio playback after boot
// To: User-gated while-in-use FGS

// BEFORE (Android 17 broken behavior)
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // ❌ This will silently fail on Android 17+
            val serviceIntent = Intent(context, AudioService::class.java)
            context.startForegroundService(serviceIntent)
            // AudioService.startPlayback() called immediately
            // → Silent failure, no audio, no exception
        }
    }
}

// AFTER (Android 17 compatible)
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // ✅ Show notification prompting user to start playback
            val pendingIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("AUTO_PLAY", true)
                },
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = Notification.Builder(context, "boot_audio")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Audio Ready")
                .setContentText("Tap to start audio playback")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.notify(NOTIFICATION_ID, notification)
        }
    }
}
        """.trimIndent()
        val report = ToolReport(
            toolId = toolId,
            title = "BOOT_COMPLETED + Audio Migration Guide",
            summary = "已生成从 BOOT_COMPLETED 直接启动音频到用户触发的迁移 Diff",
            status = ComplianceStatus.PASS,
            findings = listOf(
                Finding(
                    severity = "HIGH",
                    title = "BOOT_COMPLETED FGS 音频静默失败",
                    description = "Android 17 通过 BOOT_COMPLETED 启动的 FGS 无法播放音频",
                    location = "BootReceiver.kt",
                    fixSuggestion = "迁移到用户触发的 while-in-use FGS 模式"
                )
            ),
            recommendations = listOf(
                "移除 BOOT_COMPLETED 中的直接音频播放逻辑",
                "改为显示通知等待用户主动触发",
                "在 MainActivity 接收 AUTO_PLAY 参数后开始播放"
            )
        )
        _updateToolState(toolId) {
            copy(
                isRunning = false,
                progress = 1f,
                report = report,
                bootCompletedCode = migrationDiff,
                migrationDiff = migrationDiff
            )
        }
        updateToolDashboardStatus(toolId, ComplianceStatus.PASS)
    }

    /**
     * Run Bluetooth Audio Guide.
     */
    private suspend fun runBluetoothAudioGuide() {
        val toolId = ToolId.BLUETOOTH_AUDIO_GUIDE
        repeat(10) { step ->
            _updateToolState(toolId) { copy(progress = (step + 1) / 10f) }
            delay(150)
        }
        val status = BluetoothPermissionStatus(
            hasBluetoothConnectPermission = false,
            hasBluetoothScanPermission = true,
            hasAudioPlaybackService = true,
            autoPlayOnConnect = true
        )
        val report = ToolReport(
            toolId = toolId,
            title = "Bluetooth Audio Compliance Report",
            summary = "检测到 1 个不合规项：缺少 BLUETOOTH_CONNECT 权限",
            status = ComplianceStatus.WARNING,
            findings = listOf(
                Finding(
                    severity = "HIGH",
                    title = "缺少 BLUETOOTH_CONNECT 权限",
                    description = "Android 12+ 蓝牙扫描需要 BLUETOOTH_CONNECT 权限",
                    location = "AndroidManifest.xml",
                    fixSuggestion = "<uses-permission android:name=\"android.permission.BLUETOOTH_CONNECT\" />"
                )
            ),
            recommendations = listOf(
                "添加 BLUETOOTH_CONNECT 权限声明",
                "在蓝牙设备连接时请求 BLUETOOTH_CONNECT 运行时权限",
                "在 AndroidManifest.xml 中添加 <queries> 声明蓝牙意图"
            )
        )
        _updateToolState(toolId) {
            copy(
                isRunning = false,
                progress = 1f,
                report = report,
                bluetoothPermissionStatus = status
            )
        }
        updateToolDashboardStatus(toolId, ComplianceStatus.WARNING)
    }

    /**
     * Run AudioAttributes Guide.
     */
    private suspend fun runAudioAttributesGuide() {
        val toolId = ToolId.AUDIO_ATTRIBUTES_GUIDE
        repeat(10) { step ->
            _updateToolState(toolId) { copy(progress = (step + 1) / 10f) }
            delay(150)
        }
        val items = listOf(
            AudioAttributesItem(
                attributeKey = "USAGE_MEDIA",
                contentType = "AUDIO_CONTENT_TYPE_MUSIC",
                flags = "none",
                isCompliant = true,
                android17Change = "无显著变化",
                recommendation = "继续使用"
            ),
            AudioAttributesItem(
                attributeKey = "USAGE_ALARM",
                contentType = "AUDIO_CONTENT_TYPE_ALARM",
                flags = "FLAG_AUDIBLE_ENFORCED",
                isCompliant = true,
                android17Change = "alarm audio 在 Android 17 Beta 4 获得豁免",
                recommendation = "闹钟音频继续可用"
            ),
            AudioAttributesItem(
                attributeKey = "USAGE_VOICE_COMMUNICATION",
                contentType = "AUDIO_CONTENT_TYPE_SPEECH",
                flags = "none",
                isCompliant = false,
                android17Change = "语音通信在后台被更严格限制",
                recommendation = "确保语音通话在可见前台状态进行"
            )
        )
        val report = ToolReport(
            toolId = toolId,
            title = "AudioAttributes Android 17 Compliance Report",
            summary = "检测到 1 个 AudioAttributes 配置问题",
            status = ComplianceStatus.WARNING,
            findings = items.filter { !it.isCompliant }.map { item ->
                Finding(
                    severity = "MEDIUM",
                    title = "${item.attributeKey} 在 Android 17 有变更",
                    description = item.android17Change,
                    location = "AudioAttributes configuration",
                    fixSuggestion = item.recommendation
                )
            },
            recommendations = items.filter { !it.isCompliant }.map { it.recommendation }
        )
        _updateToolState(toolId) {
            copy(
                isRunning = false,
                progress = 1f,
                report = report,
                audioAttributesItems = items
            )
        }
        updateToolDashboardStatus(toolId, report.status)
    }

    /**
     * Run Focus Loss Callback Monitor.
     */
    private suspend fun runFocusLossMonitor() {
        val toolId = ToolId.FOCUS_LOSS_MONITOR
        repeat(10) { step ->
            _updateToolState(toolId) { copy(progress = (step + 1) / 10f) }
            delay(150)
        }
        val callbacks = listOf(
            FocusLossCallback(
                listenerClass = "com.example.app.AudioFocusManager",
                callbackMethods = listOf("onAudioFocusLoss()", "onAudioFocusLossTransient()"),
                isComplete = false,
                missingHandlers = listOf("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK", "AUDIOFOCUS_GAIN")
            ),
            FocusLossCallback(
                listenerClass = "com.example.app.MediaSessionManager",
                callbackMethods = listOf("onAudioFocusChange(int)", "onAudioFocusLoss()", "onAudioFocusGain()"),
                isComplete = true,
                missingHandlers = emptyList()
            )
        )
        val report = ToolReport(
            toolId = toolId,
            title = "Focus Loss Callback Coverage Report",
            summary = "检测到 1 个不完整的焦点丢失处理",
            status = ComplianceStatus.WARNING,
            findings = callbacks.filter { !it.isComplete }.map { cb ->
                Finding(
                    severity = "HIGH",
                    title = "${cb.listenerClass} 缺少焦点丢失处理",
                    description = "缺少处理: ${cb.missingHandlers.joinToString()}",
                    location = cb.listenerClass,
                    fixSuggestion = "实现完整的 OnAudioFocusChangeListener"
                )
            },
            recommendations = listOf(
                "确保所有 AUDIOFOCUS_LOSS 类型都被正确处理",
                "实现 AUDIOFOCUS_GAIN 恢复逻辑",
                "在丢失焦点时暂停播放并在恢复时继续"
            )
        )
        _updateToolState(toolId) {
            copy(
                isRunning = false,
                progress = 1f,
                report = report,
                focusLossCallbacks = callbacks
            )
        }
        updateToolDashboardStatus(toolId, ComplianceStatus.WARNING)
    }

    // ============================================================
    // Helper Functions
    // ============================================================

    /**
     * Simulate initial scan on dashboard load.
     */
    private fun simulateInitialScan() {
        viewModelScope.launch {
            delay(800)
            val simulatedStatuses = defaultToolStatuses.map { tool ->
                tool.copy(status = simulateToolStatus(tool.toolId))
            }
            val passingCount = simulatedStatuses.count { it.status == ComplianceStatus.PASS }
            val score = (passingCount * 100) / 9
            _update {
                copy(
                    toolStatuses = simulatedStatuses,
                    complianceScore = score,
                    failingTools = simulatedStatuses.count {
                        it.status == ComplianceStatus.FAIL || it.status == ComplianceStatus.WARNING
                    }
                )
            }
        }
    }

    /**
     * Simulate tool status based on tool ID.
     */
    private fun simulateToolStatus(toolId: String): ComplianceStatus {
        // For demonstration, cycle through statuses based on tool ID
        return when (toolId.hashCode() % 4) {
            0 -> ComplianceStatus.PASS
            1 -> ComplianceStatus.WARNING
            2 -> ComplianceStatus.FAIL
            else -> ComplianceStatus.UNKNOWN
        }
    }

    /**
     * Update tool dashboard status after tool run completes.
     */
    private fun updateToolDashboardStatus(toolId: String, status: ComplianceStatus) {
        _update {
            val updatedStatuses = toolStatuses.map { tool ->
                if (tool.toolId == toolId) tool.copy(status = status) else tool
            }
            val passingCount = updatedStatuses.count { it.status == ComplianceStatus.PASS }
            val score = (passingCount * 100) / 9.coerceAtLeast(1)
            copy(
                toolStatuses = updatedStatuses,
                complianceScore = score,
                failingTools = updatedStatuses.count {
                    it.status == ComplianceStatus.FAIL || it.status == ComplianceStatus.WARNING
                }
            )
        }
    }

    /**
     * Format report as Markdown or JSON.
     */
    private fun formatReport(report: ToolReport, format: String): String {
        return if (format == "json") {
            buildString {
                appendLine("{")
                appendLine("  \"toolId\": \"${report.toolId}\",")
                appendLine("  \"title\": \"${report.title}\",")
                appendLine("  \"summary\": \"${report.summary}\",")
                appendLine("  \"status\": \"${report.status.name}\",")
                appendLine("  \"findings\": [")
                report.findings.forEachIndexed { index, f ->
                    appendLine("    {")
                    appendLine("      \"severity\": \"${f.severity}\",")
                    appendLine("      \"title\": \"${f.title}\",")
                    appendLine("      \"description\": \"${f.description}\",")
                    appendLine("      \"location\": \"${f.location}\",")
                    appendLine("      \"fixSuggestion\": \"${f.fixSuggestion}\"")
                    appendLine("    }${if (index < report.findings.size - 1) "," else ""}")
                }
                appendLine("  ],")
                appendLine("  \"recommendations\": [")
                report.recommendations.forEachIndexed { index, r ->
                    appendLine("    \"$r\"${if (index < report.recommendations.size - 1) "," else ""}")
                }
                appendLine("  ]")
                appendLine("}")
            }
        } else {
            buildString {
                appendLine("# ${report.title}")
                appendLine()
                appendLine("**Status:** ${report.status.emoji} ${report.status.displayName}")
                appendLine()
                appendLine("## Summary")
                appendLine(report.summary)
                appendLine()
                if (report.findings.isNotEmpty()) {
                    appendLine("## Findings (${report.findings.size})")
                    appendLine()
                    report.findings.forEach { f ->
                        appendLine("### ${f.severity}: ${f.title}")
                        appendLine("- **Location:** `${f.location}`")
                        appendLine("- **Description:** ${f.description}")
                        appendLine("- **Fix:** ${f.fixSuggestion}")
                        appendLine()
                    }
                }
                if (report.recommendations.isNotEmpty()) {
                    appendLine("## Recommendations")
                    report.recommendations.forEach { r ->
                        appendLine("- $r")
                    }
                }
                appendLine()
                appendLine("---")
                appendLine("*Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(report.generatedAt))}*")
            }
        }
    }

    /**
     * Create initial detail states for all tools.
     */
    private fun createInitialDetailStates(): Map<String, ToolDetailState> {
        return defaultToolStatuses.associate { tool ->
            tool.toolId to ToolDetailState(
                toolId = tool.toolId,
                toolName = tool.toolName,
                toolDescription = tool.toolDescription
            )
        }
    }

    /**
     * Helper to update state with a tool-specific state transformation.
     */
    private inline fun _update(transform: BackgroundAudioState.() -> BackgroundAudioState) {
        _state.update { it.transform() }
    }

    /**
     * Helper to update a specific tool's detail state.
     */
    private inline fun _updateToolState(toolId: String, transform: ToolDetailState.() -> ToolDetailState) {
        _state.update { state ->
            val currentToolState = state.toolDetailStates[toolId]
                ?: ToolDetailState(toolId = toolId, toolName = "", toolDescription = "")
            state.copy(
                toolDetailStates = state.toolDetailStates + (toolId to currentToolState.transform())
            )
        }
    }
}
