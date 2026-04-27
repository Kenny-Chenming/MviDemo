package com.mvi.kenny.feature.backgroundaudiohardening

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

/**
 * ============================================================
 * BackgroundAudioHardeningViewModel — Background Audio Hardening MVI ViewModel
 * ============================================================
 * Processes user intents, manages state, delivers effects.
 *
 * Architecture: MVI (Model-View-Intent)
 * - Receives Intent from UI
 * - Updates State via StateFlow
 * - Emits one-time Effects via Channel
 *
 * @see BackgroundAudioHardeningState Page state
 * @see BackgroundAudioHardeningIntent User actions
 * @see BackgroundAudioHardeningEffect Side effects
 */

// ============================================================
// Sample Data — 示例数据（演示用，实际扫描从项目文件读取）
// ============================================================

private val sampleIssues = listOf(
    ComplianceIssue(
        id = UUID.randomUUID().toString(),
        type = ViolationType.MediaPlayer_NoForeground,
        severity = IssueSeverity.Critical,
        filePath = "app/src/main/java/com/example/musicplayer/MusicService.kt",
        lineNumber = 87,
        codeSnippet = "mediaPlayer.start() // called in background service without foreground lifecycle",
        description = "MediaPlayer.start() 在后台 Service 中调用，缺少有效的前台生命周期状态。Android 17 会静默失败。",
        fixSuggestion = "将音频播放与 MediaSession 关联，确保在有效前台生命周期状态下调用 start()。",
        docsUrl = "https://developer.android.com/about/versions/17/changes/bg-audio"
    ),
    ComplianceIssue(
        id = UUID.randomUUID().toString(),
        type = ViolationType.AudioFocusRequest_InBackground,
        severity = IssueSeverity.Critical,
        filePath = "app/src/main/java/com/example/podcast/PodcastPlayer.kt",
        lineNumber = 134,
        codeSnippet = "audioManager.requestAudioFocus(focusRequest) // in background without Activity",
        description = "在后台（无有效 Activity）请求音频焦点，Android 17 会静默拒绝请求。",
        fixSuggestion = "在请求音频焦点前检查前台状态，或使用 MediaSession 管理音频焦点。",
        docsUrl = "https://developer.android.com/about/versions/17/changes/bg-audio"
    ),
    ComplianceIssue(
        id = UUID.randomUUID().toString(),
        type = ViolationType.ForegroundService_MissingMediaType,
        severity = IssueSeverity.Warning,
        filePath = "app/src/main/AndroidManifest.xml",
        lineNumber = 42,
        codeSnippet = "<service android:name=\".AudioPlaybackService\" /> <!-- missing foregroundServiceType=\"mediaPlayback\" -->",
        description = "音频播放 Service 未声明 foregroundServiceType=\"mediaPlayback\"，不符合 Android 17 要求。",
        fixSuggestion = "在 AndroidManifest.xml 中为 Service 添加 android:foregroundServiceType=\"mediaPlayback\"。",
        docsUrl = "https://developer.android.com/guide/topics/manifest/service-element"
    ),
    ComplianceIssue(
        id = UUID.randomUUID().toString(),
        type = ViolationType.MediaSession_NoToken,
        severity = IssueSeverity.Warning,
        filePath = "app/src/main/java/com/example/musicplayer/MusicService.kt",
        lineNumber = 55,
        codeSnippet = "mediaSession = MediaSessionCompat(this, \"MusicService\") // setSessionToken never called",
        description = "MediaSession 创建后未调用 setSessionToken()，音频控制功能无法正常工作。",
        fixSuggestion = "在 MediaSession 初始化后调用 val token = mediaSession.sessionToken 并将其传递给 AudioManager。",
        docsUrl = "https://developer.android.com/guide/topics/media-apps/audio-focus"
    ),
    ComplianceIssue(
        id = UUID.randomUUID().toString(),
        type = ViolationType.ExoPlayer_NoMediaSession,
        severity = IssueSeverity.Suggestion,
        filePath = "app/src/main/java/com/example/radio/RadioService.kt",
        lineNumber = 92,
        codeSnippet = "exoPlayer.play() // no MediaSession attached",
        description = "ExoPlayer 未关联 MediaSession，建议关联以获得系统音频控制支持。",
        fixSuggestion = "创建 MediaSession 并使用 MediaSessionConnector 将 ExoPlayer 连接到 session。",
        docsUrl = "https://developer.android.com/guide/topics/media-apps/media-session-overview"
    )
)

// ============================================================
// ViewModel
// ============================================================

class BackgroundAudioHardeningViewModel : ViewModel() {

    // State — 状态流，唯一真相来源
    private val _state = MutableStateFlow(BackgroundAudioHardeningState.Initial)
    val state: StateFlow<BackgroundAudioHardeningState> = _state.asStateFlow()

    // Effect — 一次性副作用通道
    private val _effect = Channel<BackgroundAudioHardeningEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // Initialize with sample data for demonstration
        viewModelScope.launch {
            val demoResult = AuditResult(
                score = 68,
                totalIssues = 5,
                criticalCount = 2,
                warningCount = 2,
                suggestionCount = 1,
                issues = sampleIssues,
                scanDurationMs = 2350
            )
            _state.value = _state.value.copy(
                auditResult = demoResult,
                auditStatus = AuditStatus.Completed
            )
        }
    }

    // ============================================================
    // Intent Processing — 意图处理
    // ============================================================

    /**
     * Process user intent and update state accordingly.
     * 处理用户意图，执行业务逻辑，更新状态。
     *
     * @param intent User action / 用户动作
     */
    fun processIntent(intent: BackgroundAudioHardeningIntent) {
        when (intent) {
            is BackgroundAudioHardeningIntent.SwitchTab -> handleSwitchTab(intent.tab)
            is BackgroundAudioHardeningIntent.StartAudit -> handleStartAudit(intent.projectPath, intent.targetSdk)
            is BackgroundAudioHardeningIntent.CancelAudit -> handleCancelAudit()
            is BackgroundAudioHardeningIntent.SelectIssue -> handleSelectIssue(intent.issue)
            is BackgroundAudioHardeningIntent.GenerateTemplate -> handleGenerateTemplate(intent.templateType)
            is BackgroundAudioHardeningIntent.GenerateFGSConfig -> handleGenerateFGSConfig(intent.serviceClassName)
            is BackgroundAudioHardeningIntent.UpdateCIConfig -> handleUpdateCIConfig(intent.config, intent.failOnError)
            is BackgroundAudioHardeningIntent.CopyCode -> handleCopyCode(intent.code)
            is BackgroundAudioHardeningIntent.ExportReport -> handleExportReport()
            is BackgroundAudioHardeningIntent.Refresh -> handleRefresh()
        }
    }

    // ============================================================
    // Tab Switching — 标签页切换
    // ============================================================

    private fun handleSwitchTab(tab: AudioHardeningTab) {
        _state.value = _state.value.copy(activeTab = tab)
    }

    // ============================================================
    // Audit Scanning — 合规扫描
    // ============================================================

    private fun handleStartAudit(projectPath: String, targetSdk: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                auditStatus = AuditStatus.Scanning,
                scanProgress = 0,
                projectPath = projectPath,
                targetSdk = targetSdk,
                auditResult = null
            )

            // Simulate scanning progress / 模拟扫描进度
            withContext(Dispatchers.Default) {
                for (progress in listOf(10, 25, 40, 55, 70, 85, 95)) {
                    delay(Random.nextLong(200, 500))
                    _state.value = _state.value.copy(scanProgress = progress)
                }

                // Generate sample result / 生成示例结果
                delay(500)
                val result = AuditResult(
                    score = Random.nextInt(55, 85),
                    totalIssues = sampleIssues.size,
                    criticalCount = sampleIssues.count { it.severity == IssueSeverity.Critical },
                    warningCount = sampleIssues.count { it.severity == IssueSeverity.Warning },
                    suggestionCount = sampleIssues.count { it.severity == IssueSeverity.Suggestion },
                    issues = sampleIssues,
                    scanDurationMs = Random.nextLong(1500, 4000)
                )

                _state.value = _state.value.copy(
                    auditStatus = AuditStatus.Completed,
                    auditResult = result,
                    scanProgress = 100
                )
            }

            _effect.send(BackgroundAudioHardeningEffect.ShowToast("扫描完成，合规分数: ${_state.value.auditResult?.score}"))
        }
    }

    private fun handleCancelAudit() {
        _state.value = _state.value.copy(
            auditStatus = AuditStatus.Idle,
            scanProgress = 0
        )
    }

    // ============================================================
    // Issue Selection — 问题选中
    // ============================================================

    private fun handleSelectIssue(issue: ComplianceIssue?) {
        _state.value = _state.value.copy(selectedIssue = issue)
    }

    // ============================================================
    // Template Generation — 模板生成
    // ============================================================

    private fun handleGenerateTemplate(templateType: TemplateType) {
        viewModelScope.launch {
            val code = when (templateType) {
                TemplateType.MediaSessionInit -> mediaSessionInitTemplate()
                TemplateType.MediaSessionCallback -> mediaSessionCallbackTemplate()
                TemplateType.PlaybackService -> playbackServiceTemplate()
                TemplateType.AudioFocusHandling -> audioFocusTemplate()
                TemplateType.LifecycleSync -> lifecycleSyncTemplate()
            }

            _state.value = _state.value.copy(
                generatedCode = code,
                generatedTemplateType = templateType
            )

            _effect.send(BackgroundAudioHardeningEffect.ShowToast("模板已生成: ${templateType.labelCN}"))
        }
    }

    private fun mediaSessionInitTemplate(): String = """
        |/**
        | * MediaSession 初始化模板 / MediaSession Initialization Template
        | * Android 17 Background Audio Hardening 合规
        | */
        |import android.content.Context
        |import androidx.media.session.MediaSessionCompat
        |
        |class MediaSessionHelper(context: Context) {
        |
        |    private val mediaSession = MediaSessionCompat(context, "MyMusicService").apply {
        |        // 设置回调 / Set callback
        |        setCallback(mediaSessionCallback)
        |
        |        // 关联到前台 Service / Connect to foreground service
        |        isActive = true
        |    }
        |
        |    /**
        |     * 获取 SessionToken — 必须调用以激活 MediaSession
        |     * Get SessionToken — must be called to activate MediaSession
        |     */
        |    val sessionToken: android.media.session.MediaSession.Token
        |        get() = mediaSession.sessionToken
        |
        |    /**
        |     * 释放资源 / Release resources
        |     */
        |    fun release() {
        |        mediaSession.release()
        |    }
        |}
    """.trimMargin()

    private fun mediaSessionCallbackTemplate(): String = """
        |/**
        | * MediaSessionCallback 实现模板 / MediaSessionCallback Implementation Template
        | * 处理音频播放控制命令 / Handle audio playback control commands
        | */
        |import androidx.media.session.MediaSessionCompat
        |import android.content.Intent
        |import android.os.Bundle
        |
        |private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        |
        |    override fun onPlay() {
        |        // 确保在前台生命周期有效时调用 / Ensure valid foreground lifecycle before calling
        |        super.onPlay()
        |    }
        |
        |    override fun onPause() {
        |        super.onPause()
        |    }
        |
        |    override fun onStop() {
        |        // 停止播放并移除前台通知 / Stop playback and remove foreground notification
        |        stopForeground(STOP_FOREGROUND_REMOVE)
        |        super.onStop()
        |    }
        |
        |    override fun onSkipToNext() {
        |        // 跳到下一首 / Skip to next track
        |        super.onSkipToNext()
        |    }
        |
        |    override fun onSkipToPrevious() {
        |        // 跳到上一首 / Skip to previous track
        |        super.onSkipToPrevious()
        |    }
        |
        |    override fun onSeekTo(pos: Long) {
        |        // 跳到指定位置 / Seek to position
        |        super.onSeekTo(pos)
        |    }
        |
        |    override fun onCustomAction(action: String, extras: Bundle?) {
        |        // 处理自定义动作 / Handle custom actions
        |        when (action) {
        |            "CHANGE_SPEED" -> { /* 处理倍速 */ }
        |        }
        |    }
        |}
    """.trimMargin()

    private fun playbackServiceTemplate(): String = """
        |/**
        | * PlaybackService 完整模板 / PlaybackService Complete Template
        | * Android 17 Background Audio Hardening 合规实现
        | */
        |import android.app.Notification
        |import android.app.NotificationChannel
        |import android.app.NotificationManager
        |import android.app.PendingIntent
        |import android.app.Service
        |import android.content.Intent
        |import android.os.Build
        |import android.os.IBinder
        |import androidx.core.app.NotificationCompat
        |import androidx.media.app.NotificationCompat.MediaStyle
        |import androidx.media.session.MediaSessionCompat
        |
        |class PlaybackService : Service() {
        |
        |    companion object {
        |        const val CHANNEL_ID = "music_playback_channel"
        |        const val NOTIFICATION_ID = 1
        |    }
        |
        |    private lateinit var mediaSession: MediaSessionCompat
        |    private lateinit var notificationManager: NotificationManager
        |
        |    override fun onCreate() {
        |        super.onCreate()
        |        createNotificationChannel()
        |        initMediaSession()
        |    }
        |
        |    private fun createNotificationChannel() {
        |        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        |            val channel = NotificationChannel(
        |                CHANNEL_ID,
        |                "Music Playback",
        |                NotificationManager.IMPORTANCE_LOW
        |            ).apply {
        |                description = "Music playback controls"
        |            }
        |            notificationManager.createNotificationChannel(channel)
        |        }
        |    }
        |
        |    private fun initMediaSession() {
        |        mediaSession = MediaSessionCompat(this, "PlaybackService").apply {
        |            setCallback(mediaSessionCallback)
        |            setSessionToken(sessionToken) // 必须调用 / Must be called
        |            isActive = true
        |        }
        |    }
        |
        |    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        |        // 启动前台服务并关联 MediaSession
        |        startForeground(NOTIFICATION_ID, createNotification())
        |        return START_NOT_STICKY
        |    }
        |
        |    private fun createNotification(): Notification {
        |        val contentIntent = PendingIntent.getActivity(
        |            this, 0,
        |            packageManager.getLaunchIntentForPackage(packageName),
        |            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        |        )
        |
        |        return NotificationCompat.Builder(this, CHANNEL_ID)
        |            .setContentTitle("Music Playing")
        |            .setContentText("Artist - Track")
        |            .setSmallIcon(R.drawable.ic_notification)
        |            .setContentIntent(contentIntent)
        |            .setStyle(
        |                MediaStyle()
        |                    .setMediaSession(mediaSession.sessionToken)
        |                    .setShowActionsInCompactView(0, 1, 2)
        |            )
        |            .addAction(R.drawable.ic_previous, "Previous", null)
        |            .addAction(R.drawable.ic_pause, "Pause", null)
        |            .addAction(R.drawable.ic_next, "Next", null)
        |            .setPriority(NotificationCompat.PRIORITY_LOW)
        |            .setOngoing(true)
        |            .build()
        |    }
        |
        |    override fun onBind(intent: Intent?): IBinder? = null
        |
        |    override fun onDestroy() {
        |        mediaSession.release()
        |        super.onDestroy()
        |    }
        |}
    """.trimMargin()

    private fun audioFocusTemplate(): String = """
        |/**
        | * 音频焦点处理模板 / Audio Focus Handling Template
        | * Android 17 Background Audio Hardening 合规
        | */
        |import android.content.Context
        |import android.media.AudioAttributes
        |import android.media.AudioFocusRequest
        |import android.media.AudioManager
        |
        |class AudioFocusHelper(private val context: Context) {
        |
        |    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        |
        |    private val audioAttributes = AudioAttributes.Builder()
        |        .setUsage(AudioAttributes.USAGE_MEDIA)
        |        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        |        .build()
        |
        |    private var focusRequest: AudioFocusRequest? = null
        |
        |    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        |        when (focusChange) {
        |            AudioManager.AUDIOFOCUS_GAIN -> {
        |                // 获得焦点，恢复播放 / Gain focus, resume playback
        |            }
        |            AudioManager.AUDIOFOCUS_LOSS -> {
        |                // 永久失去焦点，停止播放 / Permanently lost focus, stop playback
        |            }
        |            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
        |                // 暂时失去焦点，暂停播放 / Temporarily lost focus, pause
        |            }
        |            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
        |                // 暂时失去焦点但可duck，降低音量 / Duck volume
        |            }
        |        }
        |    }
        |
        |    /**
        |     * 请求音频焦点 — 在播放前调用
        |     * Request audio focus — call before playback
        |     * @return true if focus granted, false otherwise
        |     */
        |    fun requestAudioFocus(): Boolean {
        |        // Android 17: 确保在有效前台生命周期状态下请求焦点
        |        // Android 17: Ensure valid foreground lifecycle before requesting focus
        |        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        |            .setAudioAttributes(audioAttributes)
        |            .setAcceptsDelayedFocusGain(true)
        |            .setOnAudioFocusChangeListener(focusChangeListener)
        |            .build()
        |
        |        val result = audioManager.requestAudioFocus(focusRequest!!)
        |        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        |    }
        |
        |    /**
        |     * 释放音频焦点 — 停止播放时调用
        |     * Abandon audio focus — call when stopping playback
        |     */
        |    fun abandonAudioFocus() {
        |        focusRequest?.let {
        |            audioManager.abandonAudioFocusRequest(it)
        |        }
        |    }
        |}
    """.trimMargin()

    private fun lifecycleSyncTemplate(): String = """
        |/**
        | * 生命周期同步模板 / Lifecycle Synchronization Template
        | * 确保 MediaSession 与 Activity/Service 生命周期同步
        | * Ensures MediaSession syncs with Activity/Service lifecycle
        | */
        |import androidx.lifecycle.Lifecycle
        |import androidx.lifecycle.LifecycleEventObserver
        |import androidx.lifecycle.LifecycleOwner
        |import androidx.media.session.MediaSessionCompat
        |
        |class MediaSessionLifecycleObserver(
        |    private val mediaSession: MediaSessionCompat
        |) : LifecycleEventObserver {
        |
        |    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        |        when (event) {
        |            Lifecycle.Event.ON_START -> {
        |                // Activity 可见，激活 MediaSession
        |                // Activity visible, activate MediaSession
        |                mediaSession.isActive = true
        |            }
        |            Lifecycle.Event.ON_STOP -> {
        |                // Activity 不可见，根据业务决定是否保持 MediaSession
        |                // Activity not visible, decide whether to keep MediaSession active
        |                // 如果是后台播放，保持 active；否则停止
        |            }
        |            Lifecycle.Event.ON_DESTROY -> {
        |                // Activity 销毁，释放 MediaSession
        |                // Activity destroyed, release MediaSession
        |                mediaSession.isActive = false
        |            }
        |            else -> { /* ignore other events */ }
        |        }
        |    }
        |}
        |
        |// 使用方式 / Usage:
        |// lifecycle.addObserver(MediaSessionLifecycleObserver(mediaSession))
    """.trimMargin()

    // ============================================================
    // FGS Configuration Generation — FGS 配置生成
    // ============================================================

    private fun handleGenerateFGSConfig(serviceClassName: String) {
        viewModelScope.launch {
            val code = fgsConfigTemplate(serviceClassName)
            _state.value = _state.value.copy(
                generatedCode = code,
                activeTab = AudioHardeningTab.FGSConfig
            )
            _effect.send(BackgroundAudioHardeningEffect.ShowToast("FGS 配置已生成"))
        }
    }

    private fun fgsConfigTemplate(serviceClassName: String): String = """
        |<!--
        | * Foreground Service 配置模板 / Foreground Service Configuration Template
        | * Android 17 Background Audio Hardening 合规
        -->
        |<service
        |    android:name=".$serviceClassName"
        |    android:foregroundServiceType="mediaPlayback"
        |    android:exported="false">
        |
        |    <!--
        |     * 如果需要设备策略控制器 (例如 WorkManager)，添加:
        |     * If device policy controller needed (e.g. WorkManager), add:
        |     -->
        |    <property
        |        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        |        android:value="mediaPlayback"/>
        |
        |</service>
        |
        |<!--
        | * 所需权限 / Required permissions:
        | * <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
        | * <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
        | * <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/> (Android 13+)
        | -->
    """.trimMargin()

    // ============================================================
    // CI Configuration — CI 配置
    // ============================================================

    private fun handleUpdateCIConfig(config: String, failOnError: Boolean) {
        _state.value = _state.value.copy(
            ciConfig = config,
            failOnError = failOnError
        )
    }

    // ============================================================
    // Copy Code — 复制代码
    // ============================================================

    private fun handleCopyCode(code: String) {
        viewModelScope.launch {
            _effect.send(BackgroundAudioHardeningEffect.CopyToClipboard(code))
            _effect.send(BackgroundAudioHardeningEffect.ShowToast("代码已复制到剪贴板"))
        }
    }

    // ============================================================
    // Report Export — 报告导出
    // ============================================================

    private fun handleExportReport() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isGeneratingReport = true)

            // Simulate report generation / 模拟报告生成
            delay(1500)

            _state.value = _state.value.copy(isGeneratingReport = false)
            _effect.send(BackgroundAudioHardeningEffect.ReportExported("audio-compliance-report.html"))
            _effect.send(BackgroundAudioHardeningEffect.ShowToast("报告已导出"))
        }
    }

    // ============================================================
    // Refresh — 刷新
    // ============================================================

    private fun handleRefresh() {
        viewModelScope.launch {
            _effect.send(BackgroundAudioHardeningEffect.ShowToast("状态已刷新"))
        }
    }
}
