// ================================================================
// AudioMigrationToolViewModel — 音频迁移工具 ViewModel
// Audio Migration Tool ViewModel
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// 继承 ViewModel，持有 AudioMigrationToolState（页面状态）和
// AudioMigrationToolEffect（副作用）。
//
// 状态管理：
// - _state：私有 MutableStateFlow，ViewModel 内部写入
// - state：公开 StateFlow，供 UI 层订阅
//
// 副作用管理：
// - _effect：Channel（热流），缓冲区大小 BUFFERED
// - effect：receiveAsFlow，UI 层通过 collect{} 监听
// ================================================================

package com.mvi.kenny.feature.audiobackgroundtool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * AudioMigrationToolViewModel — 音频迁移工具状态管理
 *
 * @see AudioMigrationToolState 页面状态定义
 * @see AudioMigrationToolIntent 用户意图
 * @see AudioMigrationToolEffect 副作用
 */
class AudioMigrationToolViewModel : ViewModel() {

    // ==================== State ====================

    /** 页面状态（StateFlow，UI 只读） */
    private val _state = MutableStateFlow(AudioMigrationToolState.Initial)
    val state: StateFlow<AudioMigrationToolState> = _state.asStateFlow()

    /** 当前状态的快照（用于 lambda 表达式内部访问） */
    val currentState: AudioMigrationToolState get() = _state.value

    // ==================== Effect ====================

    /** 副作用 Channel */
    private val _effect = Channel<AudioMigrationToolEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ==================== Intent Handling ====================

    /**
     * 接收并处理用户意图
     * Entry point, UI layer calls via viewModel.sendIntent(intent)
     *
     * @param intent 用户意图（非空）
     */
    fun sendIntent(intent: AudioMigrationToolIntent) {
        when (intent) {
            is AudioMigrationToolIntent.StartScan -> startScan()
            is AudioMigrationToolIntent.FilterBySeverity -> filterBySeverity(intent.severity)
            is AudioMigrationToolIntent.ToggleViolationSelection -> toggleSelection(intent.violationId)
            is AudioMigrationToolIntent.SelectAllViolations -> selectAll()
            is AudioMigrationToolIntent.DeselectAllViolations -> deselectAll()
            is AudioMigrationToolIntent.GenerateScaffolding -> generateScaffolding(intent.violationIds)
            is AudioMigrationToolIntent.RunCompatibilityTest -> runCompatibilityTest()
            is AudioMigrationToolIntent.SwitchTab -> switchTab(intent.tabIndex)
            is AudioMigrationToolIntent.ClearError -> clearError()
            is AudioMigrationToolIntent.LoadPreviousReport -> loadPreviousReport()
        }
    }

    // ==================== Scan ====================

    /**
     * 开始扫描
     * Start background audio scan
     *
     * 模拟异步扫描过程，更新 scanProgress。
     * Simulates async scanning process, updates scanProgress.
     */
    private fun startScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isScanning = true,
                scanProgress = 0f,
                errorMessage = null
            )

            try {
                // 模拟扫描过程
                // Simulate scanning process
                for (i in 1..10) {
                    delay(200)
                    _state.value = _state.value.copy(scanProgress = i / 10f)
                }

                // 生成模拟扫描结果
                // Generate mock scan results
                val mockViolations = generateMockViolations()
                val mockSummary = AuditSummaryUi(
                    total = mockViolations.size,
                    critical = mockViolations.count { it.severity == "CRITICAL" },
                    warning = mockViolations.count { it.severity == "WARNING" },
                    info = mockViolations.count { it.severity == "INFO" },
                    affectedModules = listOf("app", "feature-a")
                )

                _state.value = _state.value.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    violations = mockViolations,
                    summary = mockSummary
                )

                _effect.send(AudioMigrationToolEffect.ShowSuccess("扫描完成：发现 ${mockSummary.total} 个违规"))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isScanning = false,
                    errorMessage = "扫描失败: ${e.message}"
                )
                _effect.send(AudioMigrationToolEffect.ShowError("扫描失败: ${e.message}"))
            }
        }
    }

    /**
     * 生成模拟违规数据
     * Generate mock violation data
     *
     * 用于演示和测试。在真实环境中，数据来自 Gradle 任务扫描结果。
     * Used for demo and testing. In real environment, data comes from Gradle task scan results.
     */
    private fun generateMockViolations(): List<AudioViolationUi> {
        return listOf(
            AudioViolationUi(
                id = "VIO-001",
                severity = "CRITICAL",
                apiName = "MediaPlayer.start()",
                filePath = "app/src/main/kotlin/com/example/player/PlaybackService.kt",
                line = 42,
                suggestedFix = "使用带 WIU 的 Foreground Service 包装播放逻辑"
            ),
            AudioViolationUi(
                id = "VIO-002",
                severity = "CRITICAL",
                apiName = "AudioManager.requestAudioFocus()",
                filePath = "app/src/main/kotlin/com/example/player/AudioManager.kt",
                line = 78,
                suggestedFix = "在 WIU Service 中申请音频焦点"
            ),
            AudioViolationUi(
                id = "VIO-003",
                severity = "WARNING",
                apiName = "MediaPlayer.pause()",
                filePath = "app/src/main/kotlin/com/example/player/PlayerActivity.kt",
                line = 115,
                suggestedFix = "确保 pause 时有可见 Activity 或 WIU Service"
            ),
            AudioViolationUi(
                id = "VIO-004",
                severity = "WARNING",
                apiName = "AudioTrack.play()",
                filePath = "app/src/main/kotlin/com/example/audio/AudioRenderer.kt",
                line = 56,
                suggestedFix = "在后台线程中使用 AudioTrack 需要 WIU Service"
            ),
            AudioViolationUi(
                id = "VIO-005",
                severity = "INFO",
                apiName = "AudioAttributes.setUsage()",
                filePath = "app/src/main/kotlin/com/example/player/AudioConfig.kt",
                line = 23,
                suggestedFix = "建议使用 CONTENT_TYPE_MUSIC 和 USAGE_MEDIA"
            ),
            AudioViolationUi(
                id = "VIO-006",
                severity = "CRITICAL",
                apiName = "ExoPlayer.play()",
                filePath = "feature-a/src/main/kotlin/com/example/featurea/MediaPlayer.kt",
                line = 89,
                suggestedFix = "使用 MediaSessionService 或带 WIU 的 Service"
            )
        )
    }

    /**
     * 按严重等级筛选
     * Filter violations by severity
     */
    private fun filterBySeverity(severity: String?) {
        // 实际筛选逻辑在 UI 层通过 state.violations.filter{} 实现
        // Real filtering logic is implemented in UI layer
        viewModelScope.launch {
            _effect.send(AudioMigrationToolEffect.ShowToast("筛选: ${severity ?: "全部"}"))
        }
    }

    // ==================== Selection ====================

    /**
     * 切换违规选中状态
     * Toggle violation selection
     */
    private fun toggleSelection(violationId: String) {
        val current = _state.value.selectedViolationIds.toMutableSet()
        if (current.contains(violationId)) {
            current.remove(violationId)
        } else {
            current.add(violationId)
        }
        _state.value = _state.value.copy(selectedViolationIds = current)
    }

    /**
     * 全选所有违规
     * Select all violations
     */
    private fun selectAll() {
        val allIds = _state.value.violations.map { it.id }.toSet()
        _state.value = _state.value.copy(selectedViolationIds = allIds)
        viewModelScope.launch {
            _effect.send(AudioMigrationToolEffect.ShowToast("已选择 ${allIds.size} 个违规"))
        }
    }

    /**
     * 取消全选
     * Deselect all violations
     */
    private fun deselectAll() {
        _state.value = _state.value.copy(selectedViolationIds = emptySet())
        viewModelScope.launch {
            _effect.send(AudioMigrationToolEffect.ShowToast("已取消选择"))
        }
    }

    // ==================== Scaffolding Generation ====================

    /**
     * 生成选中的脚手架
     * Generate scaffolding for selected violations
     *
     * @param violationIds 要生成脚手架的违规 ID
     */
    private fun generateScaffolding(violationIds: List<String>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true, errorMessage = null)

            try {
                // 模拟生成过程
                // Simulate generation process
                delay(1500)

                val generatedFiles = listOf(
                    "AudioPlaybackService.kt",
                    "AudioFocusManager.kt",
                    "AudioMigrationGuide.md"
                )

                _state.value = _state.value.copy(
                    isGenerating = false,
                    generatedFiles = generatedFiles
                )

                _effect.send(AudioMigrationToolEffect.ScaffoldGenerated(generatedFiles))
                _effect.send(AudioMigrationToolEffect.ShowSuccess("脚手架生成完成"))

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isGenerating = false,
                    errorMessage = "生成失败: ${e.message}"
                )
                _effect.send(AudioMigrationToolEffect.ShowError("生成失败: ${e.message}"))
            }
        }
    }

    // ==================== Compatibility Test ====================

    /**
     * 运行兼容性测试
     * Run compatibility test
     */
    private fun runCompatibilityTest() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isVerifying = true, errorMessage = null)

            try {
                // 模拟测试过程
                // Simulate test process
                delay(2000)

                // 模拟测试结果
                val passed = true
                val score = 85

                _state.value = _state.value.copy(
                    isVerifying = false,
                    testPassed = passed,
                    testScore = score
                )

                _effect.send(AudioMigrationToolEffect.TestCompleted(passed, score))

                if (passed) {
                    _effect.send(AudioMigrationToolEffect.ShowSuccess("兼容性测试通过：$score%"))
                } else {
                    _effect.send(AudioMigrationToolEffect.ShowError("兼容性测试失败：$score%"))
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isVerifying = false,
                    errorMessage = "测试失败: ${e.message}"
                )
                _effect.send(AudioMigrationToolEffect.ShowError("测试失败: ${e.message}"))
            }
        }
    }

    // ==================== Navigation ====================

    /**
     * 切换标签页
     * Switch tab
     */
    private fun switchTab(tabIndex: Int) {
        _state.value = _state.value.copy(activeTab = tabIndex)
    }

    // ==================== Error ====================

    /**
     * 清除错误消息
     * Clear error message
     */
    private fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    /**
     * 加载历史报告
     * Load previous report
     */
    private fun loadPreviousReport() {
        viewModelScope.launch {
            _effect.send(AudioMigrationToolEffect.ShowToast("加载历史报告..."))
            // 实际实现从 build/reports 目录加载历史 JSON 报告
            // Real implementation loads historical JSON reports from build/reports
        }
    }
}
