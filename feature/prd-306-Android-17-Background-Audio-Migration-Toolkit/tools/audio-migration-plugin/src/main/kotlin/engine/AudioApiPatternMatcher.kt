// ================================================================
// AudioApiPatternMatcher — 音频 API 模式匹配器
// Audio API Pattern Matcher for Background Audio Detection
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// 负责匹配 Android Framework 中的音频相关 API 调用，
// 覆盖 android.media、android.app 等包中的所有音频类。
//
// Matches audio-related API calls in Android Framework,
// covering all audio classes in android.media, android.app packages.
// ================================================================

package com.mvi.kenny.audiobackground.engine

/**
 * 音频 API 严重等级
 * Audio API Severity Level
 *
 * @property priority 数值越小等级越高（用于排序）
 */
enum class AudioApiSeverity(val priority: Int) {
    CRITICAL(1),  // 后台直接调用会导致 Android 17 拦截
    WARNING(2),   // 潜在风险，需要审查
    INFO(3)       // 仅供参考
}

/**
 * 音频 API 模式信息
 * Audio API Pattern Info
 *
 * @param className 音频类名（如 MediaPlayer、AudioManager）
 * @param methodPattern 方法名或正则表达式
 * @param severity 严重等级
 * @param description 描述
 * @param suggestion 修复建议
 * @param androidVersionIntroduced 引入该限制的 Android 版本
 */
data class AudioApiPattern(
    val className: String,
    val methodPattern: String,
    val severity: AudioApiSeverity,
    val description: String,
    val suggestion: String,
    val androidVersionIntroduced: Int = 17  // Android 17 开始严格限制
)

/**
 * 音频 API 模式匹配器（单例）
 * Audio API Pattern Matcher
 *
 * 维护所有已知的后台音频违规 API 模式列表。
 * Maintains a list of all known background audio violation API patterns.
 */
object AudioApiPatternMatcher {

    /**
     * 所有音频 API 模式列表
     * All audio API patterns
     *
     * 覆盖：MediaPlayer、AudioManager、AudioTrack、AudioAttributes、
     *      MediaRecorder、SoundPool、VolumeProvider 等
     */
    val allPatterns: List<AudioApiPattern> = listOf(
        // ==================== MediaPlayer ====================
        AudioApiPattern(
            className = "MediaPlayer",
            methodPattern = "start\\(\\)",
            severity = AudioApiSeverity.CRITICAL,
            description = "MediaPlayer.start() 在后台调用",
            suggestion = "使用带 WIU 的 Foreground Service 包装，或检查是否有可见 Activity"
        ),
        AudioApiPattern(
            className = "MediaPlayer",
            methodPattern = "pause\\(\\)",
            severity = AudioApiSeverity.CRITICAL,
            description = "MediaPlayer.pause() 在后台调用",
            suggestion = "确保在 pause 前已获取 AudioFocus，使用 WIU Service"
        ),
        AudioApiPattern(
            className = "MediaPlayer",
            methodPattern = "stop\\(\\)",
            severity = AudioApiSeverity.CRITICAL,
            description = "MediaPlayer.stop() 在后台调用",
            suggestion = "确保在 stop 时释放 AudioFocus"
        ),
        AudioApiPattern(
            className = "MediaPlayer",
            methodPattern = "seekTo\\(\\)",
            severity = AudioApiSeverity.WARNING,
            description = "MediaPlayer.seekTo() 在后台调用",
            suggestion = "检查调用链，确保在用户可见状态下调用"
        ),
        AudioApiPattern(
            className = "MediaPlayer",
            methodPattern = "setVolume\\(",
            severity = AudioApiSeverity.INFO,
            description = "MediaPlayer.setVolume() 调用",
            suggestion = "检查是否为用户主动调整音量"
        ),

        // ==================== AudioManager ====================
        AudioApiPattern(
            className = "AudioManager",
            methodPattern = "requestAudioFocus\\(",
            severity = AudioApiSeverity.CRITICAL,
            description = "AudioManager.requestAudioFocus() 在后台调用",
            suggestion = "确保在获取焦点前有可见 Activity 或 WIU Service"
        ),
        AudioApiPattern(
            className = "AudioManager",
            methodPattern = "abandonAudioFocus\\(",
            severity = AudioApiSeverity.WARNING,
            description = "AudioManager.abandonAudioFocus() 在后台调用",
            suggestion = "确保在 abandon 时有对应的 requestAudioFocus 配对"
        ),
        AudioApiPattern(
            className = "AudioManager",
            methodPattern = "requestAudioFocusAndDuck\\(",
            severity = AudioApiSeverity.CRITICAL,
            description = "AudioManager.requestAudioFocusAndDuck() 在后台调用",
            suggestion = "Android 17 废弃，使用 requestAudioFocus 替代"
        ),
        AudioApiPattern(
            className = "AudioManager",
            methodPattern = "adjustVolume\\(",
            severity = AudioApiSeverity.WARNING,
            description = "AudioManager.adjustVolume() 在后台调用",
            suggestion = "用户主动音量调整除外，检查调用链"
        ),
        AudioApiPattern(
            className = "AudioManager",
            methodPattern = "setStreamVolume\\(",
            severity = AudioApiSeverity.WARNING,
            description = "AudioManager.setStreamVolume() 在后台调用",
            suggestion = "用户主动音量调整除外，检查调用链"
        ),

        // ==================== AudioTrack ====================
        AudioApiPattern(
            className = "AudioTrack",
            methodPattern = "play\\(\\)",
            severity = AudioApiSeverity.CRITICAL,
            description = "AudioTrack.play() 在后台调用",
            suggestion = "使用带 WIU 的 Foreground Service 包装"
        ),
        AudioApiPattern(
            className = "AudioTrack",
            methodPattern = "pause\\(\\)",
            severity = AudioApiSeverity.WARNING,
            description = "AudioTrack.pause() 在后台调用",
            suggestion = "确保在 pause 前有 WIU Service"
        ),
        AudioApiPattern(
            className = "AudioTrack",
            methodPattern = "stop\\(\\)",
            severity = AudioApiSeverity.WARNING,
            description = "AudioTrack.stop() 在后台调用",
            suggestion = "确保正确释放 AudioTrack 资源"
        ),
        AudioApiPattern(
            className = "AudioTrack",
            methodPattern = "setVolume\\(",
            severity = AudioApiSeverity.INFO,
            description = "AudioTrack.setVolume() 调用",
            suggestion = "检查是否为后台音量调整"
        ),

        // ==================== MediaRecorder ====================
        AudioApiPattern(
            className = "MediaRecorder",
            methodPattern = "start\\(\\)",
            severity = AudioApiSeverity.CRITICAL,
            description = "MediaRecorder.start() 在后台调用",
            suggestion = "录音需要 foregroundServiceType='microphone'"
        ),
        AudioApiPattern(
            className = "MediaRecorder",
            methodPattern = "stop\\(\\)",
            severity = AudioApiSeverity.WARNING,
            description = "MediaRecorder.stop() 在后台调用",
            suggestion = "确保在正确的生命周期中停止"
        ),

        // ==================== SoundPool ====================
        AudioApiPattern(
            className = "SoundPool",
            methodPattern = "play\\(",
            severity = AudioApiSeverity.WARNING,
            description = "SoundPool.play() 在后台调用",
            suggestion = "短音效通常允许，检查是否持续播放"
        ),

        // ==================== AudioAttributes ====================
        AudioApiPattern(
            className = "AudioAttributes",
            methodPattern = "Builder\\.setUsage\\(",
            severity = AudioApiSeverity.INFO,
            description = "AudioAttributes.Usage 设置",
            suggestion = "使用 CONTENT_TYPE_MUSIC / USAGE_MEDIA 而非 USAGE_GAME"
        ),

        // ==================== ExoPlayer / Media3 ====================
        AudioApiPattern(
            className = "ExoPlayer",
            methodPattern = "play\\(\\)",
            severity = AudioApiSeverity.CRITICAL,
            description = "ExoPlayer.play() 在后台调用",
            suggestion = "使用 MediaSessionService 或带 WIU 的 Service"
        ),
        AudioApiPattern(
            className = "Player",
            methodPattern = "play\\(\\)",
            severity = AudioApiSeverity.CRITICAL,
            description = "Player.play() 在后台调用（Media3 Player 接口）",
            suggestion = "使用 MediaSessionService 或带 WIU 的 Service"
        )
    )

    /**
     * 根据类名筛选模式
     * Filter patterns by class name
     */
    fun byClass(className: String): List<AudioApiPattern> =
        allPatterns.filter { it.className == className }

    /**
     * 根据严重等级筛选模式
     * Filter patterns by severity
     */
    fun bySeverity(severity: AudioApiSeverity): List<AudioApiPattern> =
        allPatterns.filter { it.severity == severity }

    /**
     * 获取所有 CRITICAL 模式
     * Get all CRITICAL patterns
     */
    fun criticalPatterns(): List<AudioApiPattern> = bySeverity(AudioApiSeverity.CRITICAL)
}
