package com.mvi.kenny.feature.exoplayermedia3migration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

// =============================================================
// ExoPlayer2Media3MigrationToolViewModel — MVI ViewModel
// =============================================================
/**
 * ViewModel for ExoPlayer 2 → Media3 Migration Tool / ExoPlayer 2 → Media3 迁移工具 ViewModel
 *
 * Processes user intents, manages state, delivers effects.
 * Architecture: MVI (Model-View-Intent)
 *
 * @see ExoPlayer2Media3MigrationToolContract For state/intent/effect definitions
 */
class ExoPlayer2Media3MigrationToolViewModel : ViewModel() {

    // ============================================================
    // State — UI 状态（唯一真实数据源）
    // State — UI state (single source of truth)
    // ============================================================
    private val _state = MutableStateFlow(ExoPlayer2Media3MigrationToolState.Initial)
    val state: StateFlow<ExoPlayer2Media3MigrationToolState> = _state.asStateFlow()

    // ============================================================
    // Effect — 一次性副作用（导航、Toast 等）
    // Effect — One-time side effects (navigation, toast, etc.)
    // ============================================================
    private val _effect = Channel<ExoPlayer2Media3MigrationToolEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // sendIntent — 接收 UI Intent 并处理
    // sendIntent — Receive UI Intent and process
    // ============================================================
    /**
     * Send user intent to ViewModel / 将用户意图发送到 ViewModel
     *
     * @param intent User intent / 用户意图
     */
    fun sendIntent(intent: ExoPlayer2Media3MigrationToolIntent) {
        viewModelScope.launch {
            when (intent) {
                is ExoPlayer2Media3MigrationToolIntent.SelectTab -> handleSelectTab(intent.tab)
                is ExoPlayer2Media3MigrationToolIntent.StartScan -> handleStartScan(intent.modulePath)
                is ExoPlayer2Media3MigrationToolIntent.CancelScan -> handleCancelScan()
                is ExoPlayer2Media3MigrationToolIntent.SelectScanResult -> handleSelectScanResult(intent.result)
                is ExoPlayer2Media3MigrationToolIntent.ClearScanResult -> handleClearScanResult()
                is ExoPlayer2Media3MigrationToolIntent.SelectStep -> handleSelectStep(intent.step)
                is ExoPlayer2Media3MigrationToolIntent.ApplyStep -> handleApplyStep(intent.stepIndex)
                is ExoPlayer2Media3MigrationToolIntent.NextStep -> handleNextStep()
                is ExoPlayer2Media3MigrationToolIntent.PreviousStep -> handlePreviousStep()
                is ExoPlayer2Media3MigrationToolIntent.SelectLibraryTab -> handleSelectLibraryTab(intent.tab)
                is ExoPlayer2Media3MigrationToolIntent.FilterApiCategory -> handleFilterApiCategory(intent.category)
                is ExoPlayer2Media3MigrationToolIntent.GenerateReport -> handleGenerateReport()
                is ExoPlayer2Media3MigrationToolIntent.NavigateToMigration -> handleNavigateToMigration()
                is ExoPlayer2Media3MigrationToolIntent.NavigateToReport -> handleNavigateToReport()
                is ExoPlayer2Media3MigrationToolIntent.StartQuickScan -> handleStartScan(null)
                is ExoPlayer2Media3MigrationToolIntent.DismissError -> handleDismissError()
                is ExoPlayer2Media3MigrationToolIntent.CopyCode -> handleCopyCode(intent.code)
            }
        }
    }

    // ============================================================
    // Intent Handlers — 意图处理器
    // ============================================================

    /**
     * Handle tab selection / 处理 Tab 选择
     */
    private suspend fun handleSelectTab(tab: MigrationTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    /**
     * Handle scan start / 处理扫描开始
     * Simulates scanning ExoPlayer 2 usage in codebase / 模拟扫描代码库中的 ExoPlayer 2 使用
     */
    private suspend fun handleStartScan(modulePath: String?) {
        if (_state.value.scanStatus == ScanStatus.SCANNING) return

        _state.update {
            it.copy(
                scanStatus = ScanStatus.SCANNING,
                scanProgress = 0f,
                scanResults = emptyList(),
                isLoading = true
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Simulate scan progress / 模拟扫描进度
                // In production, this would scan actual project files
                // 生产环境中，这里会扫描实际的项目文件
                repeat(10) { step ->
                    delay(300)
                    _state.update { state ->
                        state.copy(scanProgress = (step + 1) / 10f)
                    }
                }

                // Sample scan results / 示例扫描结果
                // Replace with actual project scan results in production
                val sampleResults = generateSampleScanResults()

                _state.update {
                    it.copy(
                        scanStatus = ScanStatus.COMPLETED,
                        scanProgress = 1f,
                        scanResults = sampleResults,
                        detectedPlayerInstances = sampleResults.size,
                        lastScanTime = System.currentTimeMillis(),
                        isLoading = false,
                        isCompliant = sampleResults.isEmpty()
                    )
                }

                _effect.send(ExoPlayer2Media3MigrationToolEffect.ScanComplete)
                _effect.send(ExoPlayer2Media3MigrationToolEffect.ShowSnackbar("扫描完成，发现 ${sampleResults.size} 处 ExoPlayer 2 使用"))
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        scanStatus = ScanStatus.ERROR,
                        isLoading = false,
                        error = "扫描失败: ${e.message}"
                    )
                }
                _effect.send(ExoPlayer2Media3MigrationToolEffect.ShowSnackbar("扫描失败: ${e.message}", isError = true))
            }
        }
    }

    /**
     * Handle scan cancellation / 处理扫描取消
     */
    private fun handleCancelScan() {
        _state.update {
            it.copy(
                scanStatus = ScanStatus.IDLE,
                scanProgress = 0f,
                isLoading = false
            )
        }
    }

    /**
     * Handle scan result selection / 处理扫描结果选择
     */
    private fun handleSelectScanResult(result: ScanResult) {
        _state.update { it.copy(selectedScanResult = result) }
    }

    /**
     * Handle clear scan result / 处理清除扫描结果
     */
    private fun handleClearScanResult() {
        _state.update { it.copy(selectedScanResult = null) }
    }

    /**
     * Handle migration step selection / 处理迁移步骤选择
     */
    private fun handleSelectStep(step: MigrationStep) {
        _state.update { it.copy(selectedStep = step) }
    }

    /**
     * Handle apply migration step / 处理应用迁移步骤
     */
    private suspend fun handleApplyStep(stepIndex: Int) {
        _state.update { it.copy(isApplyingStep = true) }

        delay(500) // Simulate apply operation / 模拟应用操作

        _state.update { state ->
            val updatedSteps = state.migrationSteps.map { step ->
                if (step.index == stepIndex) step.copy(isCompleted = true)
                else step
            }
            val completedCount = updatedSteps.count { it.isCompleted }
            val progress = completedCount.toFloat() / updatedSteps.size

            state.copy(
                migrationSteps = updatedSteps,
                currentStep = minOf(stepIndex + 1, updatedSteps.size - 1),
                migrationProgress = progress,
                isApplyingStep = false
            )
        }

        _effect.send(ExoPlayer2Media3MigrationToolEffect.StepApplied(stepIndex))
        _effect.send(ExoPlayer2Media3MigrationToolEffect.ShowSnackbar("步骤 ${stepIndex + 1} 应用成功"))
    }

    /**
     * Handle next migration step / 处理下一步迁移
     */
    private fun handleNextStep() {
        _state.update { state ->
            val nextStep = minOf(state.currentStep + 1, state.migrationSteps.size - 1)
            state.copy(currentStep = nextStep)
        }
    }

    /**
     * Handle previous migration step / 处理上一步迁移
     */
    private fun handlePreviousStep() {
        _state.update { state ->
            val prevStep = maxOf(state.currentStep - 1, 0)
            state.copy(currentStep = prevStep)
        }
    }

    /**
     * Handle library tab selection / 处理参考库 Tab 选择
     */
    private suspend fun handleSelectLibraryTab(tab: LibraryTab) {
        _state.update { it.copy(libraryTab = tab) }

        // Load API comparisons if switching to API_TABLE tab
        // 切换到 API_TABLE Tab 时加载 API 对照
        if (tab == LibraryTab.API_TABLE && _state.value.apiComparisons.isEmpty()) {
            loadApiComparisons()
        }
    }

    /**
     * Handle API category filter / 处理 API 类别过滤
     */
    private fun handleFilterApiCategory(category: ApiCategory?) {
        _state.update { it.copy(selectedApiCategory = category) }
    }

    /**
     * Handle report generation / 处理报告生成
     */
    private suspend fun handleGenerateReport() {
        _state.update { it.copy(isGeneratingReport = true) }

        delay(1000) // Simulate report generation / 模拟报告生成

        val currentState = _state.value
        val report = ComplianceReport(
            score = calculateComplianceScore(currentState),
            level = determineComplianceLevel(currentState),
            totalIssues = currentState.scanResults.size,
            criticalCount = currentState.criticalCount,
            warningCount = currentState.warningCount,
            infoCount = currentState.infoCount,
            migratedCount = currentState.migratedCount,
            remainingIssues = currentState.scanResults.filter { !it.isMigrated },
            generatedAt = System.currentTimeMillis(),
            lastScanTime = currentState.lastScanTime ?: System.currentTimeMillis()
        )

        _state.update {
            it.copy(
                complianceReport = report,
                isGeneratingReport = false
            )
        }

        _effect.send(ExoPlayer2Media3MigrationToolEffect.ReportGenerated(report.score))
        _effect.send(ExoPlayer2Media3MigrationToolEffect.ShowSnackbar("合规报告生成完成，评分: ${report.score}/100"))
    }

    /**
     * Handle navigate to migration / 处理导航到迁移
     */
    private suspend fun handleNavigateToMigration() {
        _state.update { it.copy(activeTab = MigrationTab.MIGRATION) }
    }

    /**
     * Handle navigate to report / 处理导航到报告
     */
    private suspend fun handleNavigateToReport() {
        _state.update { it.copy(activeTab = MigrationTab.REPORT) }
        handleGenerateReport()
    }

    /**
     * Handle dismiss error / 处理关闭错误
     */
    private fun handleDismissError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Handle copy code / 处理复制代码
     */
    private suspend fun handleCopyCode(code: String) {
        _effect.send(ExoPlayer2Media3MigrationToolEffect.CopyToClipboard("代码已复制到剪贴板"))
        _effect.send(ExoPlayer2Media3MigrationToolEffect.ShowSnackbar("代码已复制到剪贴板"))
    }

    // ============================================================
    // Helper Functions — 辅助函数
    // ============================================================

    /**
     * Load API comparisons / 加载 API 对照
     */
    private suspend fun loadApiComparisons() {
        withContext(Dispatchers.IO) {
            val comparisons = listOf(
                // Player API comparisons / Player API 对照
                ApiComparisonItem(
                    category = ApiCategory.PLAYER,
                    exoPlayer2Api = "ExoPlayer.Builder(context).build()",
                    media3Api = "ExoPlayer.Builder(context).build()",
                    notes = "API unchanged, just different package / API 不变，仅包名不同"
                ),
                ApiComparisonItem(
                    category = ApiCategory.PLAYER,
                    exoPlayer2Api = "player.playWhenReady = true",
                    media3Api = "player.playWhenReady = true",
                    notes = "API unchanged / API 不变"
                ),
                ApiComparisonItem(
                    category = ApiCategory.PLAYER,
                    exoPlayer2Api = "player.seekTo(currentWindow, playbackPosition)",
                    media3Api = "player.seekTo(currentWindow, playbackPosition)",
                    notes = "API unchanged / API 不变"
                ),
                ApiComparisonItem(
                    category = ApiCategory.PLAYER,
                    exoPlayer2Api = "player.addListener(playerListener)",
                    media3Api = "player.addListener(playerListener)",
                    notes = "Player.Listener interface unchanged / Player.Listener 接口不变"
                ),
                ApiComparisonItem(
                    category = ApiCategory.PLAYER,
                    exoPlayer2Api = "MediaSource.createProgressiveMediaSource(...)",
                    media3Api = "MediaItem.fromUri(uri) + player.setMediaItem()",
                    notes = "MediaSource creation changed to MediaItem / MediaSource 创建改为 MediaItem"
                ),
                ApiComparisonItem(
                    category = ApiCategory.PLAYER,
                    exoPlayer2Api = "C.CONTENT_TYPE_MUSIC",
                    media3Api = "C.USAGE_MEDIA",
                    notes = "Content type renamed / 内容类型重命名"
                ),
                // MediaSession comparisons / MediaSession 对照
                ApiComparisonItem(
                    category = ApiCategory.SESSION,
                    exoPlayer2Api = "MediaSessionCompat(context, tag)",
                    media3Api = "MediaSession.Builder(context, player).build()",
                    notes = "Builder pattern replaces constructor / Builder 模式替代构造函数"
                ),
                ApiComparisonItem(
                    category = ApiCategory.SESSION,
                    exoPlayer2Api = "MediaSessionCompat.setActive(true)",
                    media3Api = "Automatic when player is active / 随 player 自动激活",
                    notes = "No explicit setActive in Media3 / Media3 无需显式 setActive"
                ),
                ApiComparisonItem(
                    category = ApiCategory.SESSION,
                    exoPlayer2Api = "MediaSessionCompat.setCallback(callback)",
                    media3Api = "MediaSession.Builder(...).setCallback(callback).build()",
                    notes = "Callback set via Builder / 通过 Builder 设置 callback"
                ),
                ApiComparisonItem(
                    category = ApiCategory.SESSION,
                    exoPlayer2Api = "MediaSessionCompat.release()",
                    media3Api = "MediaSession.release()",
                    notes = "Same method name, different package / 方法名相同，包名不同"
                ),
                // AudioFocus comparisons / AudioFocus 对照
                ApiComparisonItem(
                    category = ApiCategory.AUDIO_FOCUS,
                    exoPlayer2Api = "AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)...build()",
                    media3Api = "Player.AudioAttributes + handleAudioFocus=true in ExoPlayer.Builder",
                    notes = "Media3 handles audio focus automatically / Media3 自动处理音频焦点"
                ),
                ApiComparisonItem(
                    category = ApiCategory.AUDIO_FOCUS,
                    exoPlayer2Api = "audioManager.requestAudioFocus(focusRequest)",
                    media3Api = "Not needed with Media3 / 使用 Media3 无需手动请求",
                    notes = "Media3 AudioFocusMember handles it / Media3 AudioFocusMember 自动处理"
                ),
                // Notification comparisons / Notification 对照
                ApiComparisonItem(
                    category = ApiCategory.NOTIFICATION,
                    exoPlayer2Api = "MediaStyle notification with MediaSessionCompat.Token",
                    media3Api = "Media3 MediaSession handles notification automatically / Media3 MediaSession 自动处理通知",
                    notes = "Notification managed by MediaSession / 通知由 MediaSession 管理"
                ),
                ApiComparisonItem(
                    category = ApiCategory.NOTIFICATION,
                    exoPlayer2Api = "NotificationCompat.Builder(context, channelId)...",
                    media3Api = "MediaStyleNotification provided by Media3 / Media3 提供 MediaStyleNotification",
                    notes = "No manual notification building needed / 无需手动构建通知"
                )
            )
            _state.update { it.copy(apiComparisons = comparisons) }
        }
    }

    /**
     * Calculate compliance score / 计算合规评分
     */
    private fun calculateComplianceScore(state: ExoPlayer2Media3MigrationToolState): Int {
        if (state.scanResults.isEmpty()) return 100
        val total = state.scanResults.size
        val migrated = state.migratedCount
        val completedSteps = state.migrationSteps.count { it.isCompleted }
        val stepWeight = completedSteps * 10 // Each completed step adds 10 points / 每个完成步骤加 10 分
        val migratedWeight = (migrated * 100) / total
        return minOf(100, stepWeight + migratedWeight)
    }

    /**
     * Determine compliance level / 确定合规等级
     */
    private fun determineComplianceLevel(state: ExoPlayer2Media3MigrationToolState): ComplianceLevel {
        val score = calculateComplianceScore(state)
        return when {
            score >= 90 && state.criticalCount == 0 -> ComplianceLevel.COMPLIANT
            score >= 50 -> ComplianceLevel.PARTIAL
            else -> ComplianceLevel.NON_COMPLIANT
        }
    }

    /**
     * Generate sample scan results for demo / 生成示例扫描结果（演示用）
     */
    private fun generateSampleScanResults(): List<ScanResult> = listOf(
        ScanResult(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/musicplayer/MusicPlayerActivity.kt",
            lineNumber = 87,
            className = "MusicPlayerActivity",
            methodName = "onCreate",
            usageType = UsageType.PLAYER,
            severity = IssueSeverity.CRITICAL,
            codeSnippet = "val player = ExoPlayer.Builder(this).build()",
            description = "使用已废弃的 ExoPlayer 2 Player API",
            fixSuggestion = "替换为 androidx.media3.exoplayer.ExoPlayer"
        ),
        ScanResult(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/musicplayer/MusicPlayerActivity.kt",
            lineNumber = 95,
            className = "MusicPlayerActivity",
            methodName = "setupPlayer",
            usageType = UsageType.MEDIA_SESSION,
            severity = IssueSeverity.CRITICAL,
            codeSnippet = "val mediaSession = MediaSessionCompat(this, \"MusicService\")",
            description = "使用已废弃的 MediaSessionCompat",
            fixSuggestion = "替换为 androidx.media3.session.MediaSession"
        ),
        ScanResult(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/podcast/PodcastPlayer.kt",
            lineNumber = 134,
            className = "PodcastPlayer",
            methodName = "requestAudioFocus",
            usageType = UsageType.AUDIO_FOCUS,
            severity = IssueSeverity.WARNING,
            codeSnippet = "audioManager.requestAudioFocus(focusRequest)",
            description = "使用手动音频焦点请求",
            fixSuggestion = "使用 Media3 内置音频焦点处理"
        ),
        ScanResult(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/AndroidManifest.xml",
            lineNumber = 42,
            className = "AudioPlaybackService",
            methodName = "onCreate",
            usageType = UsageType.MEDIA_SESSION,
            severity = IssueSeverity.CRITICAL,
            codeSnippet = "<service android:name=\".AudioPlaybackService\" />",
            description = "Service 缺少 foregroundServiceType=\"mediaPlayback\"",
            fixSuggestion = "添加 android:foregroundServiceType=\"mediaPlayback\""
        ),
        ScanResult(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/musicplayer/TrackSelectorHelper.kt",
            lineNumber = 55,
            className = "TrackSelectorHelper",
            methodName = "buildTrackSelector",
            usageType = UsageType.TRACK_SELECTOR,
            severity = IssueSeverity.WARNING,
            codeSnippet = "val trackSelector = DefaultTrackSelector(context)",
            description = "ExoPlayer 2 TrackSelector API 在 Media3 中有变化",
            fixSuggestion = "使用 Media3 的 TrackSelector 配置方式"
        ),
        ScanResult(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/musicplayer/DataSourceHelper.kt",
            lineNumber = 23,
            className = "DataSourceHelper",
            methodName = "buildDataSource",
            usageType = UsageType.DATA_SOURCE,
            severity = IssueSeverity.INFO,
            codeSnippet = "val dataSourceFactory = DefaultDataSourceFactory(context, userAgent)",
            description = "ExoPlayer 2 DataSource API 在 Media3 中有变化",
            fixSuggestion = "使用 androidx.media3.datasource.DefaultDataSource"
        )
    )
}
