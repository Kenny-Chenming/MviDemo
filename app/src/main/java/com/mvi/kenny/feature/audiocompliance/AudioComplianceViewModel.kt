package com.mvi.kenny.feature.audiocompliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================
 * AudioComplianceViewModel — 后台音频限制检测 ViewModel
 * ============================================================
 * PRD-120 | Android 17 后台音频限制检测与合规迁移工具包
 *
 * 职责：
 * - 管理 AudioComplianceState 状态
 * - 处理 AudioComplianceIntent 用户意图
 * - 发送 AudioComplianceEffect 副作用
 *
 * @see AudioComplianceContract
 * @see AudioComplianceScreen
 */
class AudioComplianceViewModel : ViewModel() {

    // ================================================================
    // State / 状态
    // ================================================================
    private val _state = MutableStateFlow(AudioComplianceState.Initial)
    val state: StateFlow<AudioComplianceState> = _state.asStateFlow()

    // ================================================================
    // Effect Channel / 副作用通道
    // ================================================================
    private val _effect = Channel<AudioComplianceEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ================================================================
    // Predefined data / 预定义数据
    // ================================================================

    /** 预定义音频场景 / Predefined audio scenarios */
    private val predefinedScenarios = listOf(
        AudioScenario(
            id = "music_player",
            name = "Music Player",
            description = "后台音乐播放器，屏幕关闭后继续播放",
            affectedApis = listOf("AudioTrack.start()", "MediaPlayer.start()")
        ),
        AudioScenario(
            id = "navigation",
            name = "Navigation",
            description = "导航 App 后台播报转向指令",
            affectedApis = listOf("AudioTrack.start()", "AudioFocusRequest")
        ),
        AudioScenario(
            id = "voice_assistant",
            name = "Voice Assistant",
            description = "语音助手后台音频处理",
            affectedApis = listOf("MediaPlayer.start()", "AudioFocusRequest")
        ),
        AudioScenario(
            id = "podcast_auto",
            name = "Podcast Auto-Play",
            description = "播客下载完成后自动播放下一集",
            affectedApis = listOf("MediaPlayer.start()")
        )
    )

    /** 预定义替代方案 / Predefined fallback solutions */
    private val predefinedFallbacks = listOf(
        FallbackSolution(
            id = "media_session",
            name = "通知媒体样式 + MediaSession",
            applicableScenarios = listOf("music_player", "podcast_auto"),
            implementationDifficulty = "MEDIUM",
            effectDescription = "使用 MediaSession 在通知栏显示播放控件，同时保持 FGS 保护",
            steps = listOf(
                "1. 创建 MediaSession 并设置 Callback",
                "2. 在 Service 的 onStartCommand 中建立 MediaSession",
                "3. 配置 MediaStyle 通知，添加 MediaStyle.FLAG_FOREGROUND_SERVICE",
                "4. 在 onPlay/onPause 中处理 AudioFocus",
                "5. 在 AndroidManifest.xml 中声明 FOREGROUND_SERVICE_MEDIA_PLAYBACK"
            )
        ),
        FallbackSolution(
            id = "fgs_notification",
            name = "必应通知前台服务",
            applicableScenarios = listOf("navigation", "voice_assistant"),
            implementationDifficulty = "EASY",
            effectDescription = "使用必应通知样式启动 FGS，在通知栏显示持续运行状态",
            steps = listOf(
                "1. 在 AndroidManifest.xml 中声明 FOREGROUND_SERVICE",
                "2. 创建 NotificationChannel（IMPORTANCE_LOW）",
                "3. 使用 NotificationCompat.Builder 构建必应通知",
                "4. 在 Service.onCreate 中调用 startForeground(NOTIFICATION_ID, notification)",
                "5. 处理 onDestroy 中调用 stopForeground(STOP_FOREGROUND_REMOVE)"
            )
        ),
        FallbackSolution(
            id = "minimal_notification",
            name = "简约通知策略",
            applicableScenarios = listOf("voice_assistant"),
            implementationDifficulty = "EASY",
            effectDescription = "最小化通知打扰，同时保持 FGS 保护",
            steps = listOf(
                "1. 使用 PRIORITY_MIN 设置 notification priority",
                "2. 设置 flag FLAG_ONGOING_EVENT 替代 FOREGROUND_SERVICE",
                "3. 配合 setOnlyAlertOnce() 减少通知打扰",
                "4. 在音频停止时立即调用 stopForeground"
            )
        )
    )

    /** 预定义模拟扫描结果 / Predefined scan results for demo */
    private val demoScanResults = listOf(
        AudioApiCall(
            id = 1,
            apiName = "AudioTrack.start()",
            filePath = "app/src/main/java/com/mvi/kenny/player/AudioPlayerService.kt",
            lineNumber = 142,
            severity = AudioSeverity.BLOCKED,
            description = "在后台 Service 中直接调用 AudioTrack.start()，无 FGS 保护",
            fixSuggestion = "使用 MediaSession + FGS，或将 AudioTrack 替换为 MediaPlayer 并声明 FOREGROUND_SERVICE_MEDIA_PLAYBACK"
        ),
        AudioApiCall(
            id = 2,
            apiName = "MediaPlayer.start()",
            filePath = "app/src/main/java/com/mvi/kenny/player/MusicPlaybackService.kt",
            lineNumber = 87,
            severity = AudioSeverity.BLOCKED,
            description = "在 onDestroy 延迟消息中调用 MediaPlayer.start()，无生命周期保护",
            fixSuggestion = "添加 Lifecycle.State 检查，或使用 MediaSession 替代直接 MediaPlayer 调用"
        ),
        AudioApiCall(
            id = 3,
            apiName = "AudioFocusRequest",
            filePath = "app/src/main/java/com/mvi/kenny/player/AudioFocusManager.kt",
            lineNumber = 55,
            severity = AudioSeverity.DEGRADED,
            description = "AudioFocusRequest 声明了 AUDIOFOCUS_GAIN_TRANSIENT 但未处理焦点丢失恢复",
            fixSuggestion = "实现 AudioFocusRequest.OnAudioFocusChangeListener，在 onAudioFocusChange 中正确处理 AUDIOFOCUS_LOSS"
        ),
        AudioApiCall(
            id = 4,
            apiName = "AudioTrack.start()",
            filePath = "app/src/main/java/com/mvi/kenny/player/EffectsManager.kt",
            lineNumber = 203,
            severity = AudioSeverity.SAFE,
            description = "在 Lifecycle.State.STARTED 保护下调用 AudioTrack.start()",
            fixSuggestion = "无需修改，当前实现在 Android 17 下可正常工作"
        )
    )

    /** 预定义测试用例 / Predefined test cases */
    private val demoTestCases = listOf(
        AudioTestCase(
            id = "test_bg_music_stop",
            name = "后台音乐停止测试",
            scenario = "屏幕关闭后音乐播放器在后台的播放行为",
            expectedBehavior = "音乐停止（符合 Android 17 后台音频限制）",
            verificationSteps = listOf(
                "1. 启动音乐播放器，播放歌曲",
                "2. 关闭屏幕",
                "3. 等待 30 秒",
                "4. 检查音频是否已停止"
            ),
            lastResult = TestResultStatus.PASS
        ),
        AudioTestCase(
            id = "test_nav_announcement",
            name = "导航后台播报测试",
            scenario = "导航 App 在后台时的语音播报行为",
            expectedBehavior = "导航播报被阻断或降级",
            verificationSteps = listOf(
                "1. 启动导航 App，开始导航",
                "2. 切换到其他 App",
                "3. 等待下一个转向提示",
                "4. 检查转向提示是否正常播报"
            ),
            lastResult = TestResultStatus.FAIL
        ),
        AudioTestCase(
            id = "test_podcast_auto_next",
            name = "播客自动播放测试",
            scenario = "播客 App 下载完成后自动播放下一集",
            expectedBehavior = "自动播放失败或需要用户确认",
            verificationSteps = listOf(
                "1. 播客 App 设置为下载完成后自动播放",
                "2. 下载一集播客",
                "3. 等待下载完成",
                "4. 检查是否自动开始播放"
            ),
            lastResult = TestResultStatus.PENDING
        )
    )

    init {
        // Initialize with demo data / 使用演示数据初始化
        initializeDemoData()
    }

    // ================================================================
    // Initialize demo data / 初始化演示数据
    // ================================================================
    private fun initializeDemoData() {
        _state.update { current ->
            current.copy(
                healthScore = 65,
                affectedApiCount = 4,
                blockedCount = 2,
                degradedCount = 1,
                safeCount = 1,
                scanResults = demoScanResults,
                reportData = AudioComplianceReport(
                    totalApis = 4,
                    blockedCount = 2,
                    degradedCount = 1,
                    safeCount = 1,
                    healthScore = 65,
                    summary = "检测到 4 处音频 API 调用，其中 2 处会受到 Android 17 后台音频限制的影响",
                    moduleBreakdown = mapOf(
                        "player" to demoScanResults.take(2),
                        "audio" to listOf(demoScanResults[2]),
                        "effects" to listOf(demoScanResults[3])
                    )
                ),
                fallbackSolutions = predefinedFallbacks,
                testCases = demoTestCases,
                currentFgsConfig = FgsConfig(
                    hasMediaPlaybackService = false,
                    hasMediaProcessingService = false,
                    hasOtherFgs = false,
                    isCompliant = false,
                    missingTypes = listOf("FOREGROUND_SERVICE_MEDIA_PLAYBACK", "FOREGROUND_SERVICE_MEDIA_PROCESSING")
                ),
                fgsRecommendation = "建议添加 FOREGROUND_SERVICE_MEDIA_PLAYBACK 前台服务，并在 AndroidManifest.xml 中声明"
            )
        }
    }

    // ================================================================
    // sendIntent — Intent 处理器入口
    // ================================================================
    /**
     * 处理用户意图
     *
     * @param intent 用户意图
     */
    fun sendIntent(intent: AudioComplianceIntent) {
        when (intent) {
            is AudioComplianceIntent.SelectTab -> handleSelectTab(intent.index)
            is AudioComplianceIntent.StartScan -> handleStartScan()
            is AudioComplianceIntent.FilterBySeverity -> handleFilterBySeverity(intent.severity)
            is AudioComplianceIntent.SelectScanResult -> { /* Expand detail handled in UI */ }
            is AudioComplianceIntent.SelectScenario -> handleSelectScenario(intent.scenario)
            is AudioComplianceIntent.RunSimulation -> handleRunSimulation()
            is AudioComplianceIntent.DetectFgsConfig -> handleDetectFgsConfig()
            is AudioComplianceIntent.GenerateFgsTemplate -> handleGenerateFgsTemplate()
            is AudioComplianceIntent.ToggleFallbackDetail -> handleToggleFallbackDetail(intent.id)
            is AudioComplianceIntent.RunTestSuite -> handleRunTestSuite()
            is AudioComplianceIntent.AddTestCase -> handleAddTestCase(intent.testCase)
            is AudioComplianceIntent.SetAddTestDialogVisible -> handleSetAddTestDialogVisible(intent.visible)
        }
    }

    // ================================================================
    // Intent handlers / 意图处理器
    // ================================================================

    /** Select tab / 切换 Tab */
    private fun handleSelectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    /** Start scan / 开始扫描 */
    private fun handleStartScan() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, scanProgress = 0f, scanResults = emptyList()) }

            // Simulate scanning progress / 模拟扫描进度
            repeat(10) { step ->
                delay(150)
                _state.update { it.copy(scanProgress = (step + 1) * 0.1f) }
            }

            // Load demo results / 加载演示结果
            _state.update { current ->
                current.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    scanResults = demoScanResults,
                    affectedApiCount = demoScanResults.size,
                    blockedCount = demoScanResults.count { it.severity == AudioSeverity.BLOCKED },
                    degradedCount = demoScanResults.count { it.severity == AudioSeverity.DEGRADED },
                    safeCount = demoScanResults.count { it.severity == AudioSeverity.SAFE },
                    healthScore = calculateHealthScore(demoScanResults)
                )
            }

            _effect.send(AudioComplianceEffect.ShowToast("扫描完成，共检测到 ${demoScanResults.size} 处音频 API 调用"))
        }
    }

    /** Filter by severity / 按严重程度过滤 */
    private fun handleFilterBySeverity(severity: AudioSeverity?) {
        _state.update { it.copy(severityFilter = severity) }
    }

    /** Select scenario / 选择音频场景 */
    private fun handleSelectScenario(scenario: AudioScenario) {
        _state.update { it.copy(selectedScenario = scenario) }
    }

    /** Run simulation / 运行模拟 */
    private fun handleRunSimulation() {
        viewModelScope.launch {
            val scenario = _state.value.selectedScenario ?: return@launch
            _state.update { it.copy(simulationLogs = emptyList(), simulationResults = emptyList()) }

            // Simulate simulation logs / 模拟仿真日志
            val logs = mutableListOf(
                "[INFO] Initializing audio framework simulation...",
                "[INFO] Target SDK: 37 (Android 17)",
                "[INFO] Loading scenario: ${scenario.name}",
                "[INFO] API permissions: INTERNET, WAKE_LOCK",
                "[INFO] Checking AudioTrack configuration...",
                "[INFO] AudioTrack mode: MODE_STREAM, sampleRate: 44100",
                "[INFO] AudioAttributes: CONTENT_TYPE_MUSIC, usage: USAGE_MEDIA"
            )

            logs.forEach { log ->
                delay(200)
                _state.update { it.copy(simulationLogs = it.simulationLogs + log) }
            }

            // Determine behavior based on scenario / 根据场景确定行为
            val behavior = when (scenario.id) {
                "music_player" -> SimulationBehavior.BLOCKED
                "navigation" -> SimulationBehavior.DEGRADED
                "voice_assistant" -> SimulationBehavior.DEGRADED
                "podcast_auto" -> SimulationBehavior.BLOCKED
                else -> SimulationBehavior.NORMAL
            }

            val detail = when (behavior) {
                SimulationBehavior.BLOCKED -> "Android 17 后台音频限制：音频播放被系统强制阻断。系统检测到 AudioTrack/MediaPlayer 在非前台上下文调用，触发 TRIGGER_TYPE_AUDIO playback blocking。"
                SimulationBehavior.DEGRADED -> "音频行为降级：AudioFocusRequest 被授予但未正确处理焦点丢失。系统降低了音频会话优先级，可能导致音量减小或音频被其他应用打断。"
                SimulationBehavior.NORMAL -> "音频行为正常：在前台 Lifecycle 状态下，音频 API 调用不受限制。"
            }

            logs.addAll(listOf(
                "[INFO] Simulation completed.",
                "[RESULT] Expected behavior: ${behavior.name}",
                "[DETAIL] $detail"
            ))

            logs.forEach { log ->
                delay(100)
                _state.update { it.copy(simulationLogs = it.simulationLogs + log) }
            }

            _state.update { it.copy(
                simulationResults = it.simulationResults + SimulationResult(
                    scenarioId = scenario.id,
                    scenarioName = scenario.name,
                    behavior = behavior,
                    detail = detail
                )
            ) }
        }
    }

    /** Detect FGS config / 检测 FGS 配置 */
    private fun handleDetectFgsConfig() {
        _state.update { it.copy(
            currentFgsConfig = FgsConfig(
                hasMediaPlaybackService = false,
                hasMediaProcessingService = false,
                hasOtherFgs = false,
                isCompliant = false,
                missingTypes = listOf("FOREGROUND_SERVICE_MEDIA_PLAYBACK")
            ),
            fgsRecommendation = "当前 App 未检测到媒体播放前台服务。强烈建议添加 FOREGROUND_SERVICE_MEDIA_PLAYBACK，并在 AndroidManifest.xml 的 <application> 节点下声明。"
        ) }
    }

    /** Generate FGS template / 生成 FGS 模板 */
    private fun handleGenerateFgsTemplate() {
        val template = """<!-- AndroidManifest.xml 片段 -->
<service
    android:name=".AudioPlaybackService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="mediaPlayback" />

<!-- 权限声明 -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />

<!-- AudioPlaybackService.kt -->
class AudioPlaybackService : Service() {
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("正在播放")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    companion object {
        private const val CHANNEL_ID = "audio_playback_channel"
        private const val NOTIFICATION_ID = 1
    }
}"""

        _state.update { it.copy(generatedFgsTemplate = template) }
    }

    /** Toggle fallback detail / 展开/折叠降级方案详情 */
    private fun handleToggleFallbackDetail(id: String) {
        _state.update { current ->
            current.copy(
                expandedFallbackId = if (current.expandedFallbackId == id) null else id
            )
        }
    }

    /** Run test suite / 运行测试套件 */
    private fun handleRunTestSuite() {
        viewModelScope.launch {
            _state.update { it.copy(isRunningTests = true) }

            delay(2000) // Simulate test execution / 模拟测试执行

            val results = _state.value.testCases.map { testCase ->
                val status = when (testCase.id) {
                    "test_bg_music_stop" -> TestResultStatus.PASS
                    "test_nav_announcement" -> TestResultStatus.FAIL
                    "test_podcast_auto_next" -> TestResultStatus.PASS
                    else -> TestResultStatus.PASS
                }
                TestResult(
                    caseId = testCase.id,
                    status = status,
                    executedAt = System.currentTimeMillis(),
                    message = if (status == TestResultStatus.PASS) "测试通过" else "测试失败：音频在后台被阻断"
                )
            }

            _state.update { it.copy(
                isRunningTests = false,
                testResults = results,
                testCases = it.testCases.map { tc ->
                    val result = results.find { it.caseId == tc.id }
                    tc.copy(lastResult = result?.status ?: TestResultStatus.PENDING)
                }
            ) }

            val passCount = results.count { it.status == TestResultStatus.PASS }
            _effect.send(AudioComplianceEffect.ShowToast("测试完成：$passCount/${results.size} 通过"))
        }
    }

    /** Add test case / 新增测试用例 */
    private fun handleAddTestCase(testCase: AudioTestCase) {
        _state.update { it.copy(
            testCases = it.testCases + testCase,
            isAddTestDialogVisible = false
        ) }
    }

    /** Set add test dialog visible / 设置新增对话框可见性 */
    private fun handleSetAddTestDialogVisible(visible: Boolean) {
        _state.update { it.copy(isAddTestDialogVisible = visible) }
    }

    // ================================================================
    // Helper functions / 辅助函数
    // ================================================================

    /**
     * Calculate health score based on scan results
     * 根据扫描结果计算健康度评分
     *
     * @param results 扫描结果列表
     * @return 健康度评分 (0-100)
     */
    private fun calculateHealthScore(results: List<AudioApiCall>): Int {
        if (results.isEmpty()) return 100
        val blocked = results.count { it.severity == AudioSeverity.BLOCKED }
        val degraded = results.count { it.severity == AudioSeverity.DEGRADED }
        val total = results.size
        // 权重：BLOCKED -30分, DEGRADED -15分
        val score = 100 - (blocked * 30) - (degraded * 15)
        return score.coerceIn(0, 100)
    }
}
