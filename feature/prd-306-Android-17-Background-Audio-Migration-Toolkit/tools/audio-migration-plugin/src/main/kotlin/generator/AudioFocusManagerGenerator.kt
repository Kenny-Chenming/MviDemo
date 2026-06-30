// ================================================================
// AudioFocusManagerGenerator — AudioFocus 管理器生成器
// AudioFocus Manager Generator
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// 生成封装了 AudioFocus 申请/释放逻辑的管理器类。
// Generates manager class that encapsulates AudioFocus request/release logic.
//
// AudioFocus 状态机：
// - REQUEST → GAIN / LOSS
// - 必须配对：每次 requestAudioFocus 都必须有对应的 abandonAudioFocus
// - 所有退出路径（正常返回、异常、onDestroy）都必须释放焦点
// ================================================================

package com.mvi.kenny.audiobackground.generator

/**
 * 生成的 AudioFocusManager 文件信息
 * Generated AudioFocusManager File Info
 */
data class GeneratedAudioFocusManager(
    val fileName: String,
    val packageName: String,
    val content: String
)

/**
 * AudioFocus 管理器生成器
 * AudioFocusManagerGenerator
 *
 * 生成符合 Android 17 合规的 AudioFocus 封装类。
 * Generates Android 17-compliant AudioFocus encapsulation class.
 */
class AudioFocusManagerGenerator(
    private val packageName: String = "com.example.audioplayback"
) {

    /**
     * 生成 AudioFocusManager
     * Generate AudioFocusManager
     *
     * @param managerName 管理器名称（默认 AudioFocusManager）
     * @return 生成的代码
     */
    fun generate(managerName: String = "AudioFocusManager"): GeneratedAudioFocusManager {
        val content = """
package $packageName

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log

/**
 * ================================================================
 * $managerName — 音频焦点管理器
 * AudioFocus Manager
 * ================================================================
 * 
 * 封装 AudioFocus 的申请、释放、监听逻辑。
 * Encapsulates AudioFocus request, release, and listener logic.
 *
 * Android AudioFocus 状态机：
 * - audioFocusRequest() 申请焦点
 * - abandonAudioFocus() 释放焦点
 * - OnAudioFocusChangeListener 监听焦点变化
 *
 * 所有退出路径都必须调用 abandonAudioFocus()：
 * - 正常返回
 * - onPause / onStop
 * - onDestroy
 * - 异常捕获
 *
 * Usage / 使用方式:
 * ```kotlin
 * val audioFocusManager = AudioFocusManager(context) { focusChange ->
 *     when (focusChange) {
 *         AudioManager.AUDIOFOCUS_GAIN -> resumePlayback()
 *         AudioManager.AUDIOFOCUS_LOSS -> pausePlayback()
 *         AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> duckOrPause()
 *     }
 * }
 *
 * // Request focus before playback / 播放前申请焦点
 * if (audioFocusManager.requestFocus()) {
 *     player.start()
 * }
 *
 * // Release on pause / 暂停时释放
 * audioFocusManager.abandonFocus()
 * ```
 */
class $managerName(context: Context) {

    companion object {
        private const val TAG = "$managerName"
        
        /** 焦点请求生成时的播放属性 */
        val AUDIO_ATTRIBUTES = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }

    // ==================== Properties ====================
    
    private val audioManager: AudioManager = 
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    private var audioFocusRequest: AudioFocusRequest? = null
    
    /** 是否持有音频焦点 */
    var hasFocus: Boolean = false
        private set
    
    /** 音频焦点变化回调 */
    private var focusChangeListener: ((Int) -> Unit)? = null
    
    /** 上次焦点变化原因 */
    var lastFocusChange: Int = 0
        private set

    // ==================== Initialization ====================
    
    init {
        Log.d(TAG, "init: AudioFocusManager initialized")
    }

    // ==================== Public API ====================
    
    /**
     * 设置焦点变化监听器
     * Set focus change listener
     *
     * @param listener 焦点变化回调，参数为 AudioManager.AUDIOFOCUS_* 常量
     */
    fun setFocusChangeListener(listener: (Int) -> Unit) {
        focusChangeListener = listener
    }

    /**
     * 请求音频焦点
     * Request audio focus
     *
     * 使用 AUDIOFOCUS_GAIN 请求永久焦点。
     * Use AUDIOFOCUS_GAIN for permanent focus.
     *
     * @return 是否成功获取焦点
     */
    fun requestFocus(): Boolean {
        if (hasFocus) {
            Log.d(TAG, "requestFocus: Already have focus")
            return true
        }
        
        // 构建焦点请求
        // Build focus request
        val requestBuilder = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AUDIO_ATTRIBUTES)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { focusChange ->
                lastFocusChange = focusChange
                handleFocusChange(focusChange)
            }
        
        // Android 26+ 支持 delayed focus
        // Android 26+ supports delayed focus
        audioFocusRequest = requestBuilder.build()
        
        val result = audioManager.requestAudioFocus(audioFocusRequest!!)
        hasFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        
        Log.d(TAG, "requestFocus: result=${if (hasFocus) "GRANTED" else "DENIED"}")
        return hasFocus
    }

    /**
     * 请求短暂音频焦点
     * Request transient (short-term) audio focus
     *
     * 用于短音效播放等场景。
     * Used for short sound effects, etc.
     *
     * @return 是否成功获取焦点
     */
    fun requestTransientFocus(): Boolean {
        if (hasFocus) return true
        
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(AUDIO_ATTRIBUTES)
            .setOnAudioFocusChangeListener { focusChange ->
                lastFocusChange = focusChange
                handleFocusChange(focusChange)
            }
            .build()
        
        audioFocusRequest = request
        
        val result = audioManager.requestAudioFocus(request)
        hasFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        
        Log.d(TAG, "requestTransientFocus: result=${if (hasFocus) "GRANTED" else "DENIED"}")
        return hasFocus
    }

    /**
     * 请求可混音的短暂焦点
     * Request duckable transient focus
     *
     * 用于导航语音等场景，可以与其他音频混音。
     * Used for navigation prompts, etc. Can mix with other audio.
     *
     * @return 是否成功获取焦点
     */
    fun requestDuckableFocus(): Boolean {
        if (hasFocus) return true
        
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(AUDIO_ATTRIBUTES)
            .setOnAudioFocusChangeListener { focusChange ->
                lastFocusChange = focusChange
                handleFocusChange(focusChange)
            }
            .build()
        
        audioFocusRequest = request
        
        val result = audioManager.requestAudioFocus(request)
        hasFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        
        Log.d(TAG, "requestDuckableFocus: result=${if (hasFocus) "GRANTED" else "DENIED"}")
        return hasFocus
    }

    /**
     * 放弃音频焦点
     * Abandon audio focus
     *
     * ⚠️ 重要：所有退出路径都必须调用此方法！
     * 所有暂停、停止、销毁时都必须放弃焦点。
     *
     * ⚠️ Important: All exit paths must call this method!
     * All pause, stop, and destroy paths must abandon focus.
     *
     * @return 是否成功放弃焦点
     */
    fun abandonFocus(): Boolean {
        if (!hasFocus) {
            Log.d(TAG, "abandonFocus: Don't have focus to abandon")
            return true
        }
        
        audioFocusRequest?.let { request ->
            val result = audioManager.abandonAudioFocusRequest(request)
            val success = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            
            if (success) {
                hasFocus = false
                audioFocusRequest = null
                Log.d(TAG, "abandonFocus: Focus abandoned successfully")
            } else {
                Log.w(TAG, "abandonFocus: Failed to abandon focus")
            }
            
            return success
        }
        
        // 即使没有 request 也标记为无焦点
        // Still mark as no focus even without request
        hasFocus = false
        return true
    }

    /**
     * 带保护的焦点申请（自动处理异常）
     * Protected focus request (handles exceptions automatically)
     *
     * 推荐在 try-catch 块中使用，或在 Service 生命周期中使用。
     * Recommended for use in try-catch blocks or Service lifecycle.
     *
     * @return 是否成功获取焦点
     */
    fun requestFocusSafely(): Boolean {
        return try {
            requestFocus()
        } catch (e: Exception) {
            Log.e(TAG, "requestFocusSafely: Error requesting focus", e)
            false
        }
    }

    /**
     * 带保护的焦点放弃（自动处理异常）
     * Protected focus abandonment (handles exceptions automatically)
     *
     * 推荐在所有退出路径（try-finally、finally）中使用。
     * Recommended for all exit paths (try-finally, finally).
     *
     * @return 是否成功放弃焦点
     */
    fun abandonFocusSafely(): Boolean {
        return try {
            abandonFocus()
        } catch (e: Exception) {
            Log.e(TAG, "abandonFocusSafely: Error abandoning focus", e)
            // 即使异常也要标记为无焦点
            // Mark as no focus even on exception
            hasFocus = false
            true
        }
    }

    // ==================== Focus Change Handler ====================
    
    /**
     * 处理焦点变化
     * Handle focus change
     *
     * @param focusChange AudioManager.AUDIOFOCUS_* 常量
     */
    private fun handleFocusChange(focusChange: Int) {
        Log.d(TAG, "handleFocusChange: focusChange=$focusChange")
        
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // 获得永久焦点 - 可以完全恢复播放
                hasFocus = true
                focusChangeListener?.invoke(focusChange)
            }
            
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
                // 获得短暂焦点 - 暂停其他音频
                hasFocus = true
                focusChangeListener?.invoke(focusChange)
            }
            
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> {
                // 可混音的短暂焦点 - 降低音量
                hasFocus = true
                focusChangeListener?.invoke(focusChange)
            }
            
            AudioManager.AUDIOFOCUS_LOSS -> {
                // 永久失去焦点 - 必须停止播放
                hasFocus = false
                focusChangeListener?.invoke(focusChange)
            }
            
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // 短暂失去焦点 - 暂停播放，可能稍后恢复
                hasFocus = false
                focusChangeListener?.invoke(focusChange)
            }
            
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // 可以继续播放但需要降低音量
                focusChangeListener?.invoke(focusChange)
            }
        }
    }

    // ==================== Utility ====================
    
    /**
     * 检查是否持有焦点
     * Check if holding focus
     */
    fun isHoldingFocus(): Boolean = hasFocus

    /**
     * 重置状态
     * Reset state
     *
     * 用于重新初始化或清理状态。
     * Used for re-initialization or cleanup.
     */
    fun reset() {
        abandonFocus()
        lastFocusChange = 0
        Log.d(TAG, "reset: State reset")
    }
}

/**
 * ================================================================
 * 扩展函数 / Extension Functions
 * ================================================================
 */

/**
 * 包装播放操作的焦点管理
 * Wrap playback operations with focus management
 *
 * 确保在播放期间持有焦点，播放结束后自动释放。
 * Ensures focus is held during playback, auto-releases after playback ends.
 *
 * Usage:
 * ```kotlin
 * audioFocusManager.withFocus {
 *     player.start()
 *     // ... playback code
 * }
 * ```
 */
inline fun AudioFocusManager.withFocus(block: () -> Unit) {
    if (requestFocus()) {
        try {
            block()
        } finally {
            abandonFocus()
        }
    } else {
        Log.w(TAG, "withFocus: Could not obtain focus, skipping playback")
    }
}
"""
        return GeneratedAudioFocusManager(
            fileName = "${managerName}.kt",
            packageName = packageName,
            content = content.trimIndent()
        )
    }

    /**
     * 生成迁移指南文档
     * Generate migration guide document
     *
     * @param violations 检测到的违规列表（可选）
     * @return Markdown 格式的迁移指南
     */
    fun generateMigrationGuide(violations: List<String> = emptyList()): String {
        return """
# Android 17 后台音频迁移指南

## 概述

Android 17 对后台音频实施严格限制（Background Audio Hardening）。
所有后台音频 API 调用必须有可见 Activity 或带 `while-in-use`（WIU）能力的
Foreground Service 支撑。

## 迁移步骤

### 1. 添加必要权限

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK" />
```

### 2. 声明 Foreground Service

```xml
<!-- AndroidManifest.xml -->
<service
    android:name=".AudioPlaybackService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="mediaPlayback" />
```

### 3. 使用生成的 Service

#### 方式一：绑定 Service 播放

```kotlin
val intent = Intent(context, AudioPlaybackService::class.java)
context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

// 通过 Binder 控制播放
binder.getService().play()
```

#### 方式二：通过 Intent Action 控制

```kotlin
val intent = Intent(context, AudioPlaybackService::class.java).apply {
    action = AudioPlaybackService.ACTION_PLAY
}
context.startForegroundService(intent)
```

### 4. 管理 AudioFocus

所有退出路径都必须释放 AudioFocus：

```kotlin
try {
    if (audioFocusManager.requestFocus()) {
        player.start()
    }
} finally {
    audioFocusManager.abandonFocus()
}
```

## 检测到的需要迁移的代码路径

${if (violations.isEmpty()) "（暂无违规代码）" else violations.joinToString("\n") { "- $it" }}

## 注意事项

1. **Service 生命周期**：确保在 `onDestroy` 中释放所有资源
2. **AudioFocus 配对**：每次 `requestAudioFocus` 都必须有对应的 `abandonAudioFocus`
3. **通知要求**：WIU Service 必须显示前台通知
4. **Android 13+**：短播通知需要 `POST_NOTIFICATIONS` 权限

## 参考链接

- [Android 17 Background Audio Changes](https://developer.android.com)
- [Foreground Service Documentation](https://developer.android.com/guide/topics/manifest/service-element)
- [Audio Focus Best Practices](https://developer.android.com/guide/topics/media/audiofocus)
""".trimIndent()
    }
}
