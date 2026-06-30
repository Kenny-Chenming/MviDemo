// ================================================================
// Android17AudioInterceptor — Android 17 音频拦截器
// Android 17 Audio Interceptor for Compatibility Testing
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// 插桩版本的音频 API 拦截器，用于验证迁移效果。
// 在测试环境中模拟 Android 17 的后台音频拦截行为。
//
// Instrumented audio API interceptor for compatibility testing.
// Simulates Android 17 background audio interception behavior in test environment.
// ================================================================

package com.mvi.kenny.audiobackground.validation

/**
 * 拦截结果
 * Interception Result
 *
 * @param wasBlocked 是否被拦截
 * @param reason 拦截原因（如被拦截）
 * @param fallbackAction 建议的替代操作
 */
data class InterceptionResult(
    val wasBlocked: Boolean,
    val reason: String? = null,
    val fallbackAction: String? = null
)

/**
 * 音频 API 类型
 * Audio API Type
 */
enum class AudioApiType {
    MEDIA_PLAYER_START,
    MEDIA_PLAYER_PAUSE,
    MEDIA_PLAYER_STOP,
    AUDIO_MANAGER_REQUEST_FOCUS,
    AUDIO_MANAGER_ABANDON_FOCUS,
    AUDIO_TRACK_START,
    AUDIO_TRACK_STOP,
    SOUND_POOL_PLAY,
    EXO_PLAYER_PLAY,
    VOLUME_CHANGE
}

/**
 * 调用上下文
 * Call Context
 *
 * @param hasVisibleActivity 是否有可见 Activity
 * @param hasWiuService 是否有 WIU Foreground Service
 * @param isInForegroundProcess 是否在前景进程
 * @param apiLevel 当前 API 等级
 */
data class CallContext(
    val hasVisibleActivity: Boolean = false,
    val hasWiuService: Boolean = false,
    val isInForegroundProcess: Boolean = false,
    val apiLevel: Int = 17  // 默认为 Android 17 行为
)

/**
 * Android 17 音频拦截器
 * Android17AudioInterceptor
 *
 * 模拟 Android 17 对后台音频 API 的拦截行为。
 * Simulates Android 17 background audio API interception behavior.
 *
 * 拦截规则：
 * - 无可见 Activity + 无 WIU Service → 拦截所有音频 API
 * - 有 WIU Service → 允许播放
 * - 有可见 Activity → 允许播放
 */
class Android17AudioInterceptor {

    companion object {
        /** API 17+ 的拦截阈值 */
        const val TARGET_API_LEVEL = 17
    }

    /**
     * 检查音频 API 调用是否会被拦截
     * Check if audio API call will be intercepted
     *
     * @param apiType 音频 API 类型
     * @param context 调用上下文
     * @return 拦截结果
     */
    fun checkInterception(
        apiType: AudioApiType,
        context: CallContext
    ): InterceptionResult {
        // Android 17 以下不拦截
        if (context.apiLevel < TARGET_API_LEVEL) {
            return InterceptionResult(wasBlocked = false)
        }

        // Android 17+ 后台限制规则
        if (context.hasWiuService) {
            return InterceptionResult(
                wasBlocked = false,
                reason = "WIU Foreground Service active, allowing playback"
            )
        }

        if (context.hasVisibleActivity) {
            return InterceptionResult(
                wasBlocked = false,
                reason = "Visible Activity present, allowing playback"
            )
        }

        if (context.isInForegroundProcess) {
            return InterceptionResult(
                wasBlocked = false,
                reason = "Process in foreground, allowing playback"
            )
        }

        // 后台调用被拦截
        return InterceptionResult(
            wasBlocked = true,
            reason = "Background audio API call blocked by Android 17",
            fallbackAction = getFallbackAction(apiType)
        )
    }

    /**
     * 获取替代操作建议
     * Get fallback action suggestion
     */
    private fun getFallbackAction(apiType: AudioApiType): String {
        return when (apiType) {
            AudioApiType.MEDIA_PLAYER_START,
            AudioApiType.EXO_PLAYER_PLAY -> 
                "Use Foreground Service with foregroundServiceType='mediaPlayback'"
            
            AudioApiType.MEDIA_PLAYER_PAUSE,
            AudioApiType.MEDIA_PLAYER_STOP -> 
                "Ensure audio focus is properly managed via AudioFocusManager"
            
            AudioApiType.AUDIO_MANAGER_REQUEST_FOCUS -> 
                "Request focus within WIU Service context"
            
            AudioApiType.AUDIO_MANAGER_ABANDON_FOCUS -> 
                "Abandon focus in Service.onDestroy() or after playback ends"
            
            AudioApiType.AUDIO_TRACK_START -> 
                "Use WIU Foreground Service to wrap AudioTrack"
            
            AudioApiType.AUDIO_TRACK_STOP -> 
                "Release AudioTrack resources in all exit paths"
            
            AudioApiType.SOUND_POOL_PLAY -> 
                "Short sound effects may be exempt, but prefer WIU Service for extended playback"
            
            AudioApiType.VOLUME_CHANGE -> 
                "User-initiated volume changes are allowed"
        }
    }

    /**
     * 批量检查多个 API 调用
     * Check multiple API calls
     *
     * @param apiCalls API 调用列表
     * @param context 调用上下文
     * @return 每个 API 的拦截结果
     */
    fun checkMultiple(
        apiCalls: List<AudioApiType>,
        context: CallContext
    ): List<Pair<AudioApiType, InterceptionResult>> {
        return apiCalls.map { apiType ->
            apiType to checkInterception(apiType, context)
        }
    }

    /**
     * 模拟 Android 17 行为测试
     * Simulate Android 17 behavior test
     *
     * @param scenario 测试场景描述
     * @param context 调用上下文
     * @return 测试结果
     */
    fun simulateTest(
        scenario: String,
        context: CallContext
    ): SimulationResult {
        val allApis = AudioApiType.entries.toList()
        val results = checkMultiple(allApis, context)
        
        val blocked = results.filter { it.second.wasBlocked }
        val allowed = results.filter { !it.second.wasBlocked }

        return SimulationResult(
            scenario = scenario,
            context = context,
            totalApis = allApis.size,
            blockedCount = blocked.size,
            allowedCount = allowed.size,
            blockedApis = blocked.map { it.first },
            allowedApis = allowed.map { it.first },
            complianceScore = calculateComplianceScore(allowed.size, allApis.size)
        )
    }

    /**
     * 计算合规分数
     * Calculate compliance score
     *
     * @param allowedCount 允许的 API 数量
     * @param totalCount 总 API 数量
     * @return 合规分数（0-100）
     */
    private fun calculateComplianceScore(allowedCount: Int, totalCount: Int): Int {
        if (totalCount == 0) return 100
        return (allowedCount * 100) / totalCount
    }
}

/**
 * 模拟测试结果
 * Simulation Result
 */
data class SimulationResult(
    val scenario: String,
    val context: CallContext,
    val totalApis: Int,
    val blockedCount: Int,
    val allowedCount: Int,
    val blockedApis: List<AudioApiType>,
    val allowedApis: List<AudioApiType>,
    val complianceScore: Int  // 0-100
)
