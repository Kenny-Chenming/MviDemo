// ================================================================
// WIUServiceGenerator — WIU Foreground Service 生成器
// While-In-Use Foreground Service Generator
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// 生成带 while-in-use（WIU）能力的 Foreground Service 代码模板。
// Generates Foreground Service code template with WIU capability.
//
// Android 17 要求：
// - android:foregroundServiceType="mediaPlayback"
// - FOREGROUND_SERVICE 权限
// - 正确管理 Service 生命周期
// ================================================================

package com.mvi.kenny.audiobackground.generator

/**
 * 生成的 Service 文件信息
 * Generated Service File Info
 *
 * @param fileName 文件名
 * @param packageName 包名
 * @param content 生成的代码内容
 */
data class GeneratedService(
    val fileName: String,
    val packageName: String,
    val content: String
)

/**
 * WIU Foreground Service 生成器
 * WIUServiceGenerator
 *
 * 生成符合 Android 17 合规的 Foreground Service 代码。
 * Generates Android 17-compliant Foreground Service code.
 */
class WIUServiceGenerator(
    private val packageName: String = "com.example.audioplayback"
) {

    /**
     * 生成 AudioPlaybackService
     * Generate AudioPlaybackService
     *
     * @param serviceName 服务名称（默认 AudioPlaybackService）
     * @return 生成的 Service 代码
     */
    fun generate(serviceName: String = "AudioPlaybackService"): GeneratedService {
        val content = """
package $packageName

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log

/**
 * ================================================================
 * $serviceName — 带 WIU 能力的音频播放前台服务
 * While-In-Use Audio Playback Foreground Service
 * ================================================================
 * 
 * Android 17 合规的前台服务，用于包装音频播放逻辑。
 * 所有后台音频播放必须通过此 Service 进行。
 *
 * Features:
 * - android:foregroundServiceType="mediaPlayback"
 * - AudioFocus 生命周期管理
 * - Android 13 (API 33) 短播通知支持
 * - 正确的 Service 生命周期管理
 *
 * @see AudioFocusManager 音频焦点管理
 */
class $serviceName : Service() {

    companion object {
        private const val TAG = "$serviceName"
        
        // Notification Channel ID / 通知渠道 ID
        const val CHANNEL_ID = "audio_playback_channel"
        const val NOTIFICATION_ID = 1001
        
        // Action 常量
        const val ACTION_PLAY = "${packageName}.action.PLAY"
        const val ACTION_PAUSE = "${packageName}.action.PAUSE"
        const val ACTION_STOP = "${packageName}.action.STOP"
    }

    // ==================== Properties ====================
    // 音频播放器
    private var mediaPlayer: MediaPlayer? = null
    
    // 音频管理器
    private lateinit var audioManager: AudioManager
    
    // 音频焦点请求（Android 26+）
    private var audioFocusRequest: AudioFocusRequest? = null
    
    // 是否正在播放
    private var isPlaying = false
    
    // 当前媒体资源（可扩展）
    private var currentMediaUrl: String? = null
    
    // Binder for local binding / 本地绑定的 Binder
    private val binder = AudioPlaybackBinder()
    
    /**
     * 是否已获得音频焦点
     * Whether audio focus is currently held
     */
    var hasAudioFocus: Boolean = false
        private set

    // ==================== Lifecycle ====================
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Service created")
        
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        
        // 创建通知渠道（Android 8.0+）
        // Create notification channel (Android 8.0+)
        createNotificationChannel()
        
        // 初始化 MediaPlayer
        // Initialize MediaPlayer
        initializeMediaPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: intent=${intent?.action}")
        
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stop()
        }
        
        // START_STICKY：系统杀掉后重启
        // START_STICKY: Restart after system kills
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind: Client bound to service")
        return binder
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: Service destroyed")
        
        // 释放音频焦点
        // Release audio focus
        abandonAudioFocus()
        
        // 释放 MediaPlayer
        // Release MediaPlayer
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        
        isPlaying = false
        super.onDestroy()
    }

    // ==================== Playback Control ====================
    
    /**
     * 开始播放
     * Start playback
     *
     * 必须先获取音频焦点才能播放。
     * Audio focus must be obtained before playback.
     */
    fun play() {
        if (!hasAudioFocus) {
            val focusGranted = requestAudioFocus()
            if (!focusGranted) {
                Log.w(TAG, "play: Audio focus not granted, cannot play")
                return
            }
        }
        
        try {
            mediaPlayer?.apply {
                if (!isPlaying) {
                    start()
                    isPlaying = true
                }
            }
            
            // 启动前台服务并显示通知
            // Start foreground service with notification
            startForegroundServiceWithNotification()
            
            Log.d(TAG, "play: Playback started")
        } catch (e: Exception) {
            Log.e(TAG, "play: Error starting playback", e)
        }
    }

    /**
     * 暂停播放
     * Pause playback
     */
    fun pause() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    pause()
                    isPlaying = false
                }
            }
            
            // 更新通知
            // Update notification
            updateNotification()
            
            Log.d(TAG, "pause: Playback paused")
        } catch (e: Exception) {
            Log.e(TAG, "pause: Error pausing playback", e)
        }
    }

    /**
     * 停止播放
     * Stop playback
     */
    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                isPlaying = false
            }
            
            // 放弃音频焦点
            // Abandon audio focus
            abandonAudioFocus()
            
            // 停止前台服务
            // Stop foreground service
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            
            Log.d(TAG, "stop: Playback stopped")
        } catch (e: Exception) {
            Log.e(TAG, "stop: Error stopping playback", e)
        }
    }

    /**
     * 设置媒体资源
     * Set media resource
     *
     * @param url 媒体 URL 或 URI
     */
    fun setMediaSource(url: String) {
        currentMediaUrl = url
        try {
            mediaPlayer?.apply {
                reset()
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener {
                    Log.d(TAG, "setMediaSource: Media prepared, ready to play")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "setMediaSource: Error setting media source", e)
        }
    }

    /**
     * 跳转到指定位置
     * Seek to position
     *
     * @param position 毫秒
     */
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    /**
     * 获取当前播放位置
     * Get current playback position
     *
     * @return 当前位置（毫秒）
     */
    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    /**
     * 获取媒体时长
     * Get media duration
     *
     * @return 时长（毫秒）
     */
    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    // ==================== Audio Focus ====================
    
    /**
     * 请求音频焦点
     * Request audio focus
     *
     * @return 是否成功获取焦点
     */
    private fun requestAudioFocus(): Boolean {
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
        
        audioFocusRequest = focusRequest
        
        val result = audioManager.requestAudioFocus(focusRequest)
        hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        
        Log.d(TAG, "requestAudioFocus: result=${if (hasAudioFocus) "GRANTED" else "DENIED"}")
        return hasAudioFocus
    }

    /**
     * 放弃音频焦点
     * Abandon audio focus
     */
    private fun abandonAudioFocus() {
        audioFocusRequest?.let { request ->
            audioManager.abandonAudioFocusRequest(request)
        }
        hasAudioFocus = false
        Log.d(TAG, "abandonAudioFocus: Focus abandoned")
    }

    /**
     * 音频焦点变化监听器
     * Audio focus change listener
     *
     * 处理焦点丢失、焦点获得等事件。
     * Handles focus loss, focus gain, etc.
     */
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        Log.d(TAG, "audioFocusChange: focusChange=$focusChange")
        
        when (focusChange) {
            // 获得永久焦点 - 继续播放
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
                mediaPlayer?.setVolume(1.0f, 1.0f)
                if (!isPlaying && currentMediaUrl != null) {
                    play()
                }
            }
            
            // 获得短暂焦点 - 暂停其他并继续
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
                hasAudioFocus = true
                // 降低其他音频的音量
                mediaPlayer?.setVolume(0.5f, 0.5f)
            }
            
            // 失去焦点 - 暂停播放
            AudioManager.AUDIOFOCUS_LOSS -> {
                hasAudioFocus = false
                pause()
            }
            
            // 失去短暂焦点（可能很快恢复）- 降低音量
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                hasAudioFocus = false
                mediaPlayer?.setVolume(0.2f, 0.2f)
            }
            
            // 失去短暂焦点但可以duck - 降低音量
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.2f, 0.2f)
            }
        }
    }

    // ==================== MediaPlayer ====================
    
    /**
     * 初始化 MediaPlayer
     * Initialize MediaPlayer
     */
    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            
            // 播放完成监听
            // Playback completion listener
            setOnCompletionListener {
                Log.d(TAG, "MediaPlayer: Playback completed")
                isPlaying = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                abandonAudioFocus()
            }
            
            // 错误监听
            // Error listener
            setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                isPlaying = false
                true
            }
        }
    }

    // ==================== Notification ====================
    
    /**
     * 创建通知渠道
     * Create notification channel
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Audio playback notification"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 启动前台服务并显示通知
     * Start foreground service with notification
     */
    private fun startForegroundServiceWithNotification() {
        val notification = createNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    /**
     * 创建通知
     * Create notification
     */
    private fun createNotification(): Notification {
        // PendingIntent：点击通知打开 App
        // PendingIntent: Open app when notification is tapped
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Action: 暂停/播放
        // Action: Pause/Play
        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Pause",
                createActionPendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "Play",
                createActionPendingIntent(ACTION_PLAY)
            )
        }
        
        // Action: 停止
        // Action: Stop
        val stopAction = NotificationCompat.Action(
            android.R.drawable.ic_delete,
            "Stop",
            createActionPendingIntent(ACTION_STOP)
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Audio Playback")
            .setContentText(if (isPlaying) "Playing" else "Paused")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentIntent)
            .addAction(playPauseAction)
            .addAction(stopAction)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    /**
     * 创建 Action PendingIntent
     * Create action PendingIntent
     */
    private fun createActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, this::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * 更新通知
     * Update notification
     */
    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    // ==================== Binder ====================
    
    /**
     * AudioPlaybackBinder — 本地绑定 Binder
     * 
     * 提供本地客户端绑定的接口。
     * Provides interface for local client binding.
     */
    inner class AudioPlaybackBinder : Binder() {
        fun getService(): $serviceName = this@$serviceName
    }
}
"""
        return GeneratedService(
            fileName = "${serviceName}.kt",
            packageName = packageName,
            content = content.trimIndent()
        )
    }
}
