package com.mvi.kenny.feature.audiohardening

// ================================================================
// AudioHardeningViewModel — Android 17 Background Audio Hardening MVI ViewModel
// ================================================================
// ViewModel for the Audio Hardening toolkit, handling Dashboard and all 9 tool pages.
//
// PRD-145: Android 17 Background Audio Hardening 合规检测与 Foreground Service 迁移工具包
//
// MVI Pattern:
//   Intent  → ViewModel processes → updates State → UI recomposes
//   Effect  → One-time events (navigation, toast) sent via Channel
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ================================================================
// AudioHardeningViewModel — 主 ViewModel（Dashboard + 工具协调）
// ================================================================

/**
 * ============================================================
 * AudioHardeningViewModel — Dashboard 主 ViewModel
 * ============================================================
 * 协调 Dashboard 状态和所有 9 个工具的导航。
 * 实际的工具详情页各自有独立的 ViewModel（下面分别定义）。
 *
 * @see AudioHardeningDashboardState
 * @see AudioHardeningDashboardIntent
 * @see AudioHardeningDashboardEffect
 */
class AudioHardeningViewModel : ViewModel() {

    // ────────────────────────────────────────────────────────────
    // State
    // ────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(AudioHardeningDashboardState.Initial)
    val state: StateFlow<AudioHardeningDashboardState> = _state.asStateFlow()

    // ────────────────────────────────────────────────────────────
    // Effect Channel（一次性副作用）
    // ────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<AudioHardeningDashboardEffect>()
    val effect: kotlinx.coroutines.flow.SharedFlow<AudioHardeningDashboardEffect> = _effect

    init {
        // 初始化工具状态（全部为待检测）
        initializeToolStatuses()
    }

    /**
     * 处理用户意图
     * @param intent 用户意图
     */
    fun sendIntent(intent: AudioHardeningDashboardIntent) {
        when (intent) {
            is AudioHardeningDashboardIntent.RunFullScan -> runFullScan()
            is AudioHardeningDashboardIntent.OpenTool -> openTool(intent.toolId)
            is AudioHardeningDashboardIntent.ExportReport -> exportReport()
            is AudioHardeningDashboardIntent.DismissError -> dismissError()
            is AudioHardeningDashboardIntent.SelectTool -> selectTool(intent.toolId)
        }
    }

    private fun initializeToolStatuses() {
        val statuses = AudioToolId.entries.associateWith { ToolStatus.NotChecked }
        _state.value = _state.value.copy(toolStatuses = statuses)
        updateComplianceSummary()
    }

    private fun runFullScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, scanProgress = 0f)

            // 模拟扫描进度
            for (i in 1..10) {
                delay(200)
                _state.value = _state.value.copy(scanProgress = i / 10f)
            }

            // 模拟扫描结果
            val mockStatuses = AudioToolId.entries.associateWith {
                when {
                    kotlin.random.Random.nextFloat() > 0.7f -> ToolStatus.Warning
                    kotlin.random.Random.nextFloat() > 0.3f -> ToolStatus.Pass
                    else -> ToolStatus.NotChecked
                }
            }

            _state.value = _state.value.copy(
                isScanning = false,
                scanProgress = 1f,
                toolStatuses = mockStatuses
            )
            updateComplianceSummary()
            _effect.emit(AudioHardeningDashboardEffect.ShowSnackbar("全量扫描完成"))
        }
    }

    private fun openTool(toolId: AudioToolId) {
        viewModelScope.launch {
            _effect.emit(AudioHardeningDashboardEffect.NavigateToTool(toolId))
        }
    }

    private fun selectTool(toolId: AudioToolId) {
        _state.value = _state.value.copy(selectedTool = toolId)
    }

    private fun exportReport() {
        viewModelScope.launch {
            _effect.emit(AudioHardeningDashboardEffect.ShowSnackbar("报告生成中…"))
            delay(500)
            _effect.emit(AudioHardeningDashboardEffect.ShareReport("/tmp/audio_hardening_report.html"))
        }
    }

    private fun dismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private fun updateComplianceSummary() {
        val statuses = _state.value.toolStatuses
        val passCount = statuses.values.count { it == ToolStatus.Pass }
        val warningCount = statuses.values.count { it == ToolStatus.Warning }
        val notCheckedCount = statuses.values.count { it == ToolStatus.NotChecked }

        val overallStatus = when {
            warningCount > 0 -> ToolStatus.Warning
            notCheckedCount == AudioToolId.entries.size -> ToolStatus.NotChecked
            else -> ToolStatus.Pass
        }

        _state.value = _state.value.copy(
            complianceSummary = ComplianceSummary(
                passCount = passCount,
                warningCount = warningCount,
                notCheckedCount = notCheckedCount,
                overallStatus = overallStatus
            )
        )
    }
}

// ================================================================
// 工具详情页 ViewModel — AudioComplianceScanner
// ================================================================

/**
 * ============================================================
 * AudioComplianceScannerViewModel — 自动化检测器 ViewModel
 * ============================================================
 */
class AudioComplianceScannerViewModel : ViewModel() {

    private val _state = MutableStateFlow(AudioComplianceScannerState())
    val state: StateFlow<AudioComplianceScannerState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AudioComplianceScannerEffect>()
    val effect: kotlinx.coroutines.flow.SharedFlow<AudioComplianceScannerEffect> = _effect

    fun sendIntent(intent: AudioComplianceScannerIntent) {
        when (intent) {
            is AudioComplianceScannerIntent.SetScanScope -> setScanScope(intent.scope)
            is AudioComplianceScannerIntent.StartScan -> startScan()
            is AudioComplianceScannerIntent.CancelScan -> cancelScan()
            is AudioComplianceScannerIntent.ToggleResultExpand -> toggleExpand(intent.resultId)
            is AudioComplianceScannerIntent.SetRiskFilter -> setRiskFilter(intent.riskLevel)
            is AudioComplianceScannerIntent.GenerateFixDiff -> generateFixDiff()
        }
    }

    private fun setScanScope(scope: ScanScope) {
        _state.value = _state.value.copy(scanScope = scope)
    }

    private fun startScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, scanState = ScanState.Scanning, progress = 0f)
            for (i in 1..10) {
                delay(300)
                _state.value = _state.value.copy(progress = i / 10f)
            }

            // 模拟扫描结果
            val results = listOf(
                ScanResult("1", "MainActivity.kt", 42, "MediaPlayer.start()", RiskLevel.P0, "后台调用 MediaPlayer.start() 会静默失败", "使用 MediaSession + MediaController 控制"),
                ScanResult("2", "AudioService.kt", 87, "audioManager.requestAudioFocus()", RiskLevel.P1, "音频焦点请求未检查返回值", "检查 AUDIOFOCUS_REQUEST_GRANTED"),
                ScanResult("3", "PlaybackFragment.kt", 115, "volumeControlStream = AudioManager.STREAM_MUSIC", RiskLevel.P2, "建议显式指定音频流类型", "保持当前实现或明确声明流类型")
            )
            _state.value = _state.value.copy(isScanning = false, scanState = ScanState.Success, progress = 1f, scanResults = results)
            _effect.emit(AudioComplianceScannerEffect.ShowSnackbar("扫描完成，发现 ${results.size} 个问题"))
        }
    }

    private fun cancelScan() {
        _state.value = _state.value.copy(isScanning = false, scanState = ScanState.Idle, progress = 0f)
    }

    private fun toggleExpand(resultId: String) {
        _state.value = _state.value.copy(
            expandedResultId = if (_state.value.expandedResultId == resultId) null else resultId
        )
    }

    private fun setRiskFilter(riskLevel: RiskLevel?) {
        _state.value = _state.value.copy(selectedFilter = riskLevel)
    }

    private fun generateFixDiff() {
        viewModelScope.launch {
            val diff = """
                --- a/app/src/main/java/com/mvi/kenny/MainActivity.kt
                +++ b/app/src/main/java/com/mvi/kenny/MainActivity.kt
                @@ -40,5 +40,7 @@ class MainActivity : Activity() {
                -    mediaPlayer.start()
                +    val session = MediaSessionCompat(this, "AudioService")
                +    session.setCallback(AudioServiceCallback())
                +    session.isActive = true
                +    // 使用 MediaSession 控制音频播放
                 }
            """.trimIndent()
            _effect.emit(AudioComplianceScannerEffect.ShareDiff(diff))
        }
    }
}

// ================================================================
// 工具详情页 ViewModel — SilentFailureMonitor
// ================================================================

/**
 * ============================================================
 * SilentFailureMonitorViewModel — 静默失败监控面板 ViewModel
 * ============================================================
 */
class SilentFailureMonitorViewModel : ViewModel() {

    private val _state = MutableStateFlow(SilentFailureMonitorState())
    val state: StateFlow<SilentFailureMonitorState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SilentFailureMonitorEffect>()
    val effect: kotlinx.coroutines.flow.SharedFlow<SilentFailureMonitorEffect> = _effect

    fun sendIntent(intent: SilentFailureMonitorIntent) {
        when (intent) {
            is SilentFailureMonitorIntent.ToggleMonitoring -> toggleMonitoring()
            is SilentFailureMonitorIntent.SetApiFilter -> setApiFilter(intent.apiType)
            is SilentFailureMonitorIntent.ClearEvents -> clearEvents()
        }
    }

    private fun toggleMonitoring() {
        viewModelScope.launch {
            val newMonitoring = !_state.value.isMonitoring
            _state.value = _state.value.copy(isMonitoring = newMonitoring)
            if (newMonitoring) {
                _effect.emit(SilentFailureMonitorEffect.ShowSnackbar("监控已启动"))
                startMockEvents()
            } else {
                _effect.emit(SilentFailureMonitorEffect.ShowSnackbar("监控已停止"))
            }
        }
    }

    private fun startMockEvents() {
        viewModelScope.launch {
            val apiTypes = listOf("MediaPlayer", "AudioManager", "AudioFocusRequest", "ExoPlayer")
            repeat(5) { i ->
                delay(1000)
                if (!_state.value.isMonitoring) return@launch
                val event = AudioAPIEvent(
                    id = "event_$i",
                    timestamp = System.currentTimeMillis(),
                    apiType = apiTypes[i % apiTypes.size],
                    callingThread = "main",
                    errorCode = -i * 2,
                    stackTrace = "at MediaPlayer.start(Native Method)\nat AudioService.play(AudioService.kt:87)"
                )
                val newEvents = (_state.value.events + event).takeLast(20)
                val newStats = newEvents.groupingBy { it.apiType }.eachCount()
                _state.value = _state.value.copy(events = newEvents, statistics = newStats)
            }
        }
    }

    private fun setApiFilter(apiType: String?) {
        _state.value = _state.value.copy(selectedFilter = apiType)
    }

    private fun clearEvents() {
        _state.value = _state.value.copy(events = emptyList(), statistics = emptyMap())
    }
}

// ================================================================
// 工具详情页 ViewModel — FGSConfigGenerator
// ================================================================

/**
 * ============================================================
 * FGSConfigGeneratorViewModel — FGS 配置生成器 ViewModel
 * ============================================================
 */
class FGSConfigGeneratorViewModel : ViewModel() {

    private val _state = MutableStateFlow(FGSConfigGeneratorState())
    val state: StateFlow<FGSConfigGeneratorState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<FGSConfigGeneratorEffect>()
    val effect: kotlinx.coroutines.flow.SharedFlow<FGSConfigGeneratorEffect> = _effect

    init { generateConfig(FGSUseCase.MusicPlayer) }

    fun sendIntent(intent: FGSConfigGeneratorIntent) {
        when (intent) {
            is FGSConfigGeneratorIntent.SelectUseCase -> generateConfig(intent.useCase)
            is FGSConfigGeneratorIntent.CopyManifest -> copyManifest()
            is FGSConfigGeneratorIntent.ApplyToProject -> applyToProject()
        }
    }

    private fun generateConfig(useCase: FGSUseCase) {
        val manifest = when (useCase) {
            FGSUseCase.MusicPlayer -> """
                <service
                    android:name=".AudioPlaybackService"
                    android:foregroundServiceType="mediaPlayback"
                    android:exported="false">
                    <property
                        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
                        android:value="media_playback" />
                </service>
            """.trimIndent()
            FGSUseCase.Navigation -> """
                <service
                    android:name=".NavigationAudioService"
                    android:foregroundServiceType="mediaPlayback"
                    android:exported="false">
                    <property
                        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
                        android:value="navigation_audio" />
                </service>
            """.trimIndent()
            FGSUseCase.Audiobook -> """
                <service
                    android:name=".AudiobookPlaybackService"
                    android:foregroundServiceType="mediaPlayback"
                    android:exported="false">
                    <property
                        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
                        android:value="audiobook_playback" />
                </service>
            """.trimIndent()
            FGSUseCase.Fitness -> """
                <service
                    android:name=".FitnessAudioService"
                    android:foregroundServiceType="mediaPlayback"
                    android:exported="false">
                    <property
                        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
                        android:value="fitness_audio" />
                </service>
            """.trimIndent()
            FGSUseCase.AIAgent -> """
                <service
                    android:name=".AIAudioService"
                    android:foregroundServiceType="mediaPlayback"
                    android:exported="false">
                    <property
                        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
                        android:value="ai_voice_assistant" />
                </service>
            """.trimIndent()
            FGSUseCase.Custom -> """
                <service
                    android:name=".CustomAudioService"
                    android:foregroundServiceType="mediaPlayback"
                    android:exported="false" />
            """.trimIndent()
        }

        val permissions = listOf(
            "android.permission.FOREGROUND_SERVICE",
            "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK",
            "android.permission.POST_NOTIFICATIONS"
        )

        val capabilities = listOf(
            "android:while-in-use capabilities: mediaPlayback"
        )

        val tips = listOf(
            "Android 17 要求 mediaPlayback 类型 FGS 必须声明 while-in-use capability",
            "在 AndroidManifest.xml 中添加 tools:targetApi 属性",
            "音频播放时调用 startForeground() 展示通知",
            "使用 MediaSession 管理音频会话并关联到 FGS"
        )

        _state.value = _state.value.copy(
            selectedUseCase = useCase,
            generatedManifest = manifest,
            generatedPermissions = permissions,
            generatedCapabilities = capabilities,
            bestPracticeTips = tips
        )
    }

    private fun copyManifest() {
        viewModelScope.launch {
            _effect.emit(FGSConfigGeneratorEffect.ShowSnackbar("Manifest 配置已复制到剪贴板"))
        }
    }

    private fun applyToProject() {
        viewModelScope.launch {
            _effect.emit(FGSConfigGeneratorEffect.ShowSnackbar("配置已准备，请在 AndroidManifest.xml 中粘贴"))
        }
    }
}

// ================================================================
// 工具详情页 ViewModel — MediaSessionBinder
// ================================================================

/**
 * ============================================================
 * MediaSessionBinderViewModel — MediaSession 绑定引导器 ViewModel
 * ============================================================
 */
class MediaSessionBinderViewModel : ViewModel() {

    private val _state = MutableStateFlow(MediaSessionBinderState())
    val state: StateFlow<MediaSessionBinderState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<MediaSessionBinderEffect>()
    val effect: kotlinx.coroutines.flow.SharedFlow<MediaSessionBinderEffect> = _effect

    private val codeTemplates = listOf(
        // Step 1
        """
        // Step 1: 创建 MediaSessionCompat.Callback 子类
        class AudioServiceCallback : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                // 处理播放命令
                mediaPlayer.prepare()
                mediaPlayer.start()
            }

            override fun onPause() {
                super.onPause()
                mediaPlayer.pause()
            }

            override fun onStop() {
                super.onStop()
                mediaPlayer.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                // 切换到下一首
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                // 切换到上一首
            }
        }
        """.trimIndent(),
        // Step 2
        """
        // Step 2: 在 Service 中初始化 MediaSession
        class AudioPlaybackService : Service() {

            private lateinit var mediaSession: MediaSessionCompat

            override fun onCreate() {
                super.onCreate()
                mediaSession = MediaSessionCompat(this, "AudioService").apply {
                    setCallback(AudioServiceCallback())
                    isActive = true
                }
            }

            override fun onGetSession(controllerInfo: MediaSessionManager.SessionInfo): IBinder? {
                return mediaSession.getSessionToken()
            }
        }
        """.trimIndent(),
        // Step 3
        """
        // Step 3: 绑定音频播放到 MediaSession
        class AudioPlaybackService : Service() {

            private lateinit var mediaSession: MediaSessionCompat
            private lateinit var mediaPlayer: MediaPlayer

            private val callback = object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.start()
                        updatePlaybackState(PlaybackState.STATE_PLAYING)
                    }
                }

                override fun onPause() {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        updatePlaybackState(PlaybackState.STATE_PAUSED)
                    }
                }

                private fun updatePlaybackState(state: Int) {
                    val playbackState = PlaybackState.Builder()
                        .setState(state, mediaPlayer.currentPosition.toLong(), 1f)
                        .build()
                    mediaSession.setPlaybackState(playbackState)
                }
            }
        }
        """.trimIndent()
    )

    init {
        _state.value = _state.value.copy(codeTemplates = codeTemplates)
    }

    fun sendIntent(intent: MediaSessionBinderIntent) {
        when (intent) {
            is MediaSessionBinderIntent.NextStep -> nextStep()
            is MediaSessionBinderIntent.PreviousStep -> previousStep()
            is MediaSessionBinderIntent.CopyAllCode -> copyAllCode()
        }
    }

    private fun nextStep() {
        val current = _state.value.currentStep
        if (current < _state.value.totalSteps - 1) {
            _state.value = _state.value.copy(currentStep = current + 1)
        }
    }

    private fun previousStep() {
        val current = _state.value.currentStep
        if (current > 0) {
            _state.value = _state.value.copy(currentStep = current - 1)
        }
    }

    private fun copyAllCode() {
        viewModelScope.launch {
            _effect.emit(MediaSessionBinderEffect.ShowSnackbar("全部代码模板已复制到剪贴板"))
        }
    }
}

// ================================================================
// 工具详情页 ViewModel — AudioComplianceCI
// ================================================================

/**
 * ============================================================
 * AudioComplianceCIViewModel — 合规 CI 检测工具 ViewModel
 * ================================================================
 */
class AudioComplianceCIViewModel : ViewModel() {

    private val _state = MutableStateFlow(AudioComplianceCIState())
    val state: StateFlow<AudioComplianceCIState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AudioComplianceCIEffect>()
    val effect: kotlinx.coroutines.flow.SharedFlow<AudioComplianceCIEffect> = _effect

    init { generateCIReport(37) }

    fun sendIntent(intent: AudioComplianceCIIntent) {
        when (intent) {
            is AudioComplianceCIIntent.SelectTargetSdk -> generateCIReport(intent.version)
            is AudioComplianceCIIntent.GenerateGitHubAction -> generateGitHubAction()
            is AudioComplianceCIIntent.GenerateGitLabCI -> generateGitLabCI()
            is AudioComplianceCIIntent.ExportReport -> exportReport(intent.format)
        }
    }

    private fun generateCIReport(targetSdk: Int) {
        val compliance = listOf(
            CIComplianceItem("ci_1", "targetSDK 声明正确", "build.gradle 中 targetSdk 已设置", ToolStatus.Pass),
            CIComplianceItem("ci_2", "FOREGROUND_SERVICE 权限", "AndroidManifest.xml 已声明", ToolStatus.Pass),
            CIComplianceItem("ci_3", "POST_NOTIFICATIONS 权限", "Android 13+ 需要运行时请求", ToolStatus.Pass)
        )
        val warnings = listOf(
            CIComplianceItem("ci_4", "while-in-use capability", "mediaPlayback FGS 缺少 while-in-use 声明", ToolStatus.Warning),
            CIComplianceItem("ci_5", "MediaSession 绑定", "后台音频未绑定 MediaSession", ToolStatus.Warning)
        )
        val violations = listOf(
            CIComplianceItem("ci_6", "后台 startForeground() 调用", "在后台线程直接调用 startForeground()", ToolStatus.Pass)
        )
        _state.value = _state.value.copy(
            selectedTargetSdk = targetSdk,
            complianceItems = compliance,
            warningItems = warnings,
            violationItems = violations
        )
    }

    private fun generateGitHubAction() {
        viewModelScope.launch {
            val yaml = """
                name: Audio Compliance Check
                on: [push, pull_request]
                jobs:
                  audio-compliance:
                    runs-on: ubuntu-latest
                    steps:
                      - uses: actions/checkout@v4
                      - name: Run Audio Hardening Scan
                        run: |
                          ./gradlew audioHardeningScan --json > audio-compliance-report.json
                      - name: Upload Report
                        uses: actions/upload-artifact@v4
                        with:
                          name: audio-compliance-report
                          path: audio-compliance-report.json
            """.trimIndent()
            _effect.emit(AudioComplianceCIEffect.ShareYAML(yaml, "audio-compliance.yml"))
        }
    }

    private fun generateGitLabCI() {
        viewModelScope.launch {
            val yaml = """
                audio_compliance:
                  stage: test
                  script:
                    - ./gradlew audioHardeningScan --json > audio-compliance-report.json
                  artifacts:
                    reports:
                      json: audio-compliance-report.json
            """.trimIndent()
            _effect.emit(AudioComplianceCIEffect.ShareYAML(yaml, ".gitlab-ci.yml"))
        }
    }

    private fun exportReport(format: String) {
        viewModelScope.launch {
            _effect.emit(AudioComplianceCIEffect.ShowSnackbar("报告已导出为 $format"))
        }
    }
}

// ================================================================
// 工具详情页 ViewModel — AudioFocusDegradation
// ================================================================

/**
 * ============================================================
 * AudioFocusDegradationViewModel — Audio Focus 降级策略 ViewModel
 * ================================================================
 */
class AudioFocusDegradationViewModel : ViewModel() {

    private val _state = MutableStateFlow(AudioFocusDegradationState())
    val state: StateFlow<AudioFocusDegradationState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AudioFocusDegradationEffect>()
    val effect: kotlinx.coroutines.flow.SharedFlow<AudioFocusDegradationEffect> = _effect

    init { updateStrategy(AudioFocusStrategy.PauseAndWait) }

    fun sendIntent(intent: AudioFocusDegradationIntent) {
        when (intent) {
            is AudioFocusDegradationIntent.SelectStrategy -> updateStrategy(intent.strategy)
            is AudioFocusDegradationIntent.UpdateCustomCode -> updateCustomCode(intent.code)
            is AudioFocusDegradationIntent.CopyCode -> copyCode()
        }
    }

    private fun updateStrategy(strategy: AudioFocusStrategy) {
        val codeTemplate = when (strategy) {
            AudioFocusStrategy.PauseAndWait -> """
                // 暂停播放，等待焦点恢复
                private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener { focusChange ->
                        when (focusChange) {
                            AudioManager.AUDIOFOCUS_LOSS -> {
                                mediaPlayer.pause()
                                mediaPlayer.seekTo(0)
                            }
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                                mediaPlayer.pause()
                            }
                            AudioManager.AUDIOFOCUS_GAIN -> {
                                mediaPlayer.start()
                            }
                        }
                    }
                    .build()
            """.trimIndent()
            AudioFocusStrategy.DuckAndRestore -> """
                // 降低音量（duck），恢复后复原
                private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setOnAudioFocusChangeListener { focusChange ->
                        when (focusChange) {
                            AudioManager.AUDIOFOCUS_GAIN -> {
                                mediaPlayer.setVolume(1f, 1f)
                                if (!mediaPlayer.isPlaying) mediaPlayer.start()
                            }
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                                mediaPlayer.setVolume(0.2f, 0.2f)
                            }
                        }
                    }
                    .build()
            """.trimIndent()
            AudioFocusStrategy.DuckAndPause -> """
                // 混合模式：降低音量后暂停
                private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener { focusChange ->
                        when (focusChange) {
                            AudioManager.AUDIOFOCUS_LOSS -> {
                                mediaPlayer.pause()
                                mediaPlayer.setVolume(1f, 1f)
                            }
                            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                                mediaPlayer.setVolume(0.1f, 0.1f)
                                // 延迟暂停
                                handler.postDelayed({ mediaPlayer.pause() }, 1000)
                            }
                            AudioManager.AUDIOFOCUS_GAIN -> {
                                mediaPlayer.setVolume(1f, 1f)
                                mediaPlayer.start()
                            }
                        }
                    }
                    .build()
            """.trimIndent()
        }

        val behaviors = mapOf(
            "AUDIOFOCUS_GAIN" to "获得焦点：完全恢复播放",
            "AUDIOFOCUS_LOSS" to "永久失去焦点：暂停并重置",
            "AUDIOFOCUS_LOSS_TRANSIENT" to "临时失去焦点：${strategy.titleCn}",
            "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK" to "可duck时：降低音量继续播放"
        )

        _state.value = _state.value.copy(selectedStrategy = strategy, previewBehaviors = behaviors)
    }

    private fun updateCustomCode(code: String) {
        _state.value = _state.value.copy(customStrategyCode = code)
    }

    private fun copyCode() {
        viewModelScope.launch {
            _effect.emit(AudioFocusDegradationEffect.ShowSnackbar("代码已复制到剪贴板"))
        }
    }
}

// ================================================================
// 工具详情页 ViewModel — AudioRegressionTest
// ================================================================

/**
 * ============================================================
 * AudioRegressionTestViewModel — 回归测试框架 ViewModel
 * ================================================================
 */
class AudioRegressionTestViewModel : ViewModel() {

    private val _state = MutableStateFlow(AudioRegressionTestState())
    val state: StateFlow<AudioRegressionTestState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AudioRegressionTestEffect>()
    val effect: kotlinx.coroutines.flow.SharedFlow<AudioRegressionTestEffect> = _effect

    fun sendIntent(intent: AudioRegressionTestIntent) {
        when (intent) {
            is AudioRegressionTestIntent.ToggleVersion -> toggleVersion(intent.version)
            is AudioRegressionTestIntent.ToggleScenario -> toggleScenario(intent.scenario)
            is AudioRegressionTestIntent.RunTests -> runTests()
            is AudioRegressionTestIntent.ViewCIIntegration -> viewCIIntegration()
        }
    }

    private fun toggleVersion(version: Int) {
        val current = _state.value.selectedVersions.toMutableSet()
        if (current.contains(version)) current.remove(version) else current.add(version)
        _state.value = _state.value.copy(selectedVersions = current)
    }

    private fun toggleScenario(scenario: RegressionScenario) {
        val current = _state.value.selectedScenarios.toMutableSet()
        if (current.contains(scenario)) current.remove(scenario) else current.add(scenario)
        _state.value = _state.value.copy(selectedScenarios = current)
    }

    private fun runTests() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRunning = true)
            delay(2000)
            val results = mutableListOf<TestResult>()
            _state.value.selectedVersions.forEach { version ->
                _state.value.selectedScenarios.forEach { scenario ->
                    results.add(TestResult(scenario, version, kotlin.random.Random.nextBoolean(), "Test log for $scenario on Android $version"))
                }
            }
            _state.value = _state.value.copy(isRunning = false, testResults = results)
            _effect.emit(AudioRegressionTestEffect.ShowSnackbar("测试完成，共 ${results.size} 个场景"))
        }
    }

    private fun viewCIIntegration() {
        val hint = """
            # GitHub Actions 集成
            name: Audio Regression Tests
            on: [push, pull_request]
            jobs:
              regression:
                runs-on: ubuntu-latest
                steps:
                  - uses: actions/checkout@v4
                  - name: Run Audio Regression
                    run: |
                      ./gradlew audioRegressionTest --versions=16,17
        """.trimIndent()
        _state.value = _state.value.copy(ciIntegrationHint = hint)
    }
}

// ================================================================
// 工具详情页 ViewModel — AudioHardeningDebugPanel
// ================================================================

/**
 * ============================================================
 * AudioHardeningDebugPanelViewModel — 可视化调试工具 ViewModel
 * ================================================================
 */
class AudioHardeningDebugPanelViewModel : ViewModel() {

    private val _state = MutableStateFlow(AudioHardeningDebugPanelState())
    val state: StateFlow<AudioHardeningDebugPanelState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AudioHardeningDebugPanelEffect>()
    val effect: kotlinx.coroutines.flow.SharedFlow<AudioHardeningDebugPanelEffect> = _effect

    init { loadMockData() }

    private fun loadMockData() {
        val nodes = listOf(
            APINode("n1", "MediaPlayer.start()", "uri: content://music/track1", "void", System.currentTimeMillis() - 3000, NodeStatus.Success),
            APINode("n2", "AudioManager.requestAudioFocus()", "AUDIOFOCUS_GAIN", "GRANTED", System.currentTimeMillis() - 2000, NodeStatus.Success),
            APINode("n3", "AudioManager.setVolume()", "stream: MUSIC, volume: 0.8", "void", System.currentTimeMillis() - 1000, NodeStatus.Failed),
            APINode("n4", "MediaSession.setPlaybackState()", "STATE_PLAYING", "void", System.currentTimeMillis(), NodeStatus.Pending)
        )
        val timeline = nodes.map { node ->
            TimelineEvent(node.id, node.methodName, node.timestamp, 500L, node.status)
        }
        _state.value = _state.value.copy(apiNodes = nodes, timelineEvents = timeline)
    }

    fun sendIntent(intent: AudioHardeningDebugPanelIntent) {
        when (intent) {
            is AudioHardeningDebugPanelIntent.SetNodeFilter -> setFilter(intent.filter)
            is AudioHardeningDebugPanelIntent.SelectNode -> selectNode(intent.nodeId)
            is AudioHardeningDebugPanelIntent.ExportAsHAR -> exportAsHAR()
            is AudioHardeningDebugPanelIntent.ExportAsJSON -> exportAsJSON()
        }
    }

    private fun setFilter(filter: NodeFilter) {
        _state.value = _state.value.copy(selectedFilter = filter)
    }

    private fun selectNode(nodeId: String) {
        // 节点选择逻辑（展开详情）
    }

    private fun exportAsHAR() {
        viewModelScope.launch {
            _effect.emit(AudioHardeningDebugPanelEffect.ShowSnackbar("HAR 导出功能开发中"))
        }
    }

    private fun exportAsJSON() {
        viewModelScope.launch {
            val json = _state.value.apiNodes.joinToString("\n") { node ->
                """{"id":"${node.id}","method":"${node.methodName}","status":"${node.status}"}"""
            }
            _effect.emit(AudioHardeningDebugPanelEffect.ShareFile("[$json]", "audio_debug.json"))
        }
    }
}

// ================================================================
// 工具详情页 ViewModel — AudioFallbackPathDetector
// ================================================================

/**
 * ============================================================
 * AudioFallbackPathDetectorViewModel — 降级路径检测 ViewModel
 * ================================================================
 */
class AudioFallbackPathDetectorViewModel : ViewModel() {

    private val _state = MutableStateFlow(AudioFallbackPathDetectorState())
    val state: StateFlow<AudioFallbackPathDetectorState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AudioFallbackPathDetectorEffect>()
    val effect: kotlinx.coroutines.flow.SharedFlow<AudioFallbackPathDetectorEffect> = _effect

    fun sendIntent(intent: AudioFallbackPathDetectorIntent) {
        when (intent) {
            is AudioFallbackPathDetectorIntent.SetDeviceState -> setDeviceState(intent.state)
            is AudioFallbackPathDetectorIntent.RunDetection -> runDetection()
            is AudioFallbackPathDetectorIntent.ApplyEnhancements -> applyEnhancements()
        }
    }

    private fun setDeviceState(state: DeviceConnectionState) {
        _state.value = _state.value.copy(deviceState = state)
    }

    private fun runDetection() {
        viewModelScope.launch {
            val paths = listOf(
                FallbackPath(
                    id = "fp1",
                    fromState = "BluetoothA2DP",
                    toState = "Speaker",
                    hasFallbackLogic = true,
                    codeSnippet = "audioManager.mode = AudioManager.MODE_NORMAL",
                    suggestion = "已存在降级逻辑，建议增强：增加用户确认弹窗"
                ),
                FallbackPath(
                    id = "fp2",
                    fromState = "WiredHeadphones",
                    toState = "Speaker",
                    hasFallbackLogic = false,
                    codeSnippet = "// 无降级逻辑",
                    suggestion = "建议添加：检测耳机拔出时自动切换到扬声器并暂停播放"
                ),
                FallbackPath(
                    id = "fp3",
                    fromState = "Speaker",
                    toState = "BluetoothA2DP",
                    hasFallbackLogic = true,
                    codeSnippet = "bluetoothA2dp.connect(deviceAddress)",
                    suggestion = "已存在降级逻辑，可优化连接超时处理"
                )
            )
            val suggestions = paths.filter { !it.hasFallbackLogic }.map { it.suggestion }
            _state.value = _state.value.copy(detectedPaths = paths, suggestedEnhancements = suggestions)
            _effect.emit(AudioFallbackPathDetectorEffect.ShowSnackbar("检测完成，发现 ${paths.size} 条路径"))
        }
    }

    private fun applyEnhancements() {
        viewModelScope.launch {
            _effect.emit(AudioFallbackPathDetectorEffect.ShowSnackbar("增强方案已准备"))
        }
    }
}
