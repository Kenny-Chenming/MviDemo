package com.mvi.kenny.feature.desktopmode

// ================================================================
// DesktopModeViewModel — Android 17 Desktop Mode MVI ViewModel
// ================================================================
// ViewModel for Android 17 Desktop Mode development toolkit.
//
// PRD-170: Android 17 Desktop Mode 开发工具包
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Run CI scan for Desktop Mode compliance
//   - Check AI Reflect Layer adaptation status
//   - Test zRAM hibernation/wake behavior
//   - Generate quality radar scores
//   - Provide Desktop Mode vs Large Screen decision guidance
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ============================================================
 * DesktopModeViewModel — Desktop Mode 工具 ViewModel
 * ============================================================
 *
 * @see DesktopModeState
 * @see DesktopModeIntent
 * @see DesktopModeEffect
 */
class DesktopModeViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(DesktopModeState.Initial)
    val state: StateFlow<DesktopModeState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via SharedFlow
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<DesktopModeEffect>()
    val effect = _effect.asSharedFlow()

    // ─────────────────────────────────────────────────────────────
    // Internal state
    // ─────────────────────────────────────────────────────────────
    private var scanJob: Job? = null
    private var zramJob: Job? = null
    private var reflectJob: Job? = null

    init {
        // Initialize with demo data
        _update {
            copy(
                qualityScore = 63,
                radarScores = defaultRadarScores(),
                checklist = defaultChecklist,
                ciResults = createMockCIResults(),
                reflectStatus = ReflectStatus.UNKNOWN
            )
        }
    }

    /**
     * ============================================================
     * sendIntent — Intent 处理入口
     * ============================================================
     *
     * @param intent User intent
     */
    fun sendIntent(intent: DesktopModeIntent) {
        when (intent) {
            is DesktopModeIntent.StartCIScan -> startCIScan()
            is DesktopModeIntent.StartReflectCheck -> startReflectCheck()
            is DesktopModeIntent.TestZRAMHibernation -> testZRAMHibernation()
            is DesktopModeIntent.SetDeviceRAM -> setDeviceRAM(intent.ramMB)
            is DesktopModeIntent.SelectTab -> selectTab(intent.tab)
            is DesktopModeIntent.ExportReport -> exportReport()
            is DesktopModeIntent.ToggleCheckItem -> toggleCheckItem(intent.itemId)
            is DesktopModeIntent.ClearError -> clearError()
            is DesktopModeIntent.RunDecisionAnalysis -> runDecisionAnalysis()
        }
    }

    // ================================================================
    // Intent Handlers
    // ================================================================

    /**
     * StartCIScan — 开始 CI 合规扫描
     *
     * Simulates Gradle plugin scan for Desktop Mode issues:
     *   - Window layout truncation in floating windows
     *   - Overflow issues at various window sizes
     *   - Crash when resizing
     */
    private fun startCIScan() {
        if (_state.value.isScanning) return

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _update {
                copy(
                    isScanning = true,
                    scanProgress = 0f,
                    ciResults = emptyList(),
                    errorMessage = null
                )
            }

            try {
                // Simulate scan progress
                val mockResults = mutableListOf<CIResult>()
                val totalSteps = 10

                for (step in 1..totalSteps) {
                    delay(300)
                    _update { copy(scanProgress = step.toFloat() / totalSteps) }

                    // Generate results incrementally
                    if (step == 3) {
                        mockResults.add(
                            CIResult(
                                id = "ci_001",
                                issueType = "LAYOUT_TRUNCATION",
                                severity = "ERROR",
                                description = "TextView 在窗口宽度 < 300dp 时文字被截断",
                                filePath = "app/src/main/res/layout/activity_main.xml",
                                lineNumber = 42,
                                suggestion = "使用 minWidth 和 ellipsize 属性，或改用自适应布局"
                            )
                        )
                    }
                    if (step == 5) {
                        mockResults.add(
                            CIResult(
                                id = "ci_002",
                                issueType = "OVERFLOW",
                                severity = "WARNING",
                                description = "Button 在小窗口模式下超出容器边界",
                                filePath = "app/src/main/res/layout/dialog_settings.xml",
                                lineNumber = 28,
                                suggestion = "使用 wrap_content 而非固定宽度，添加 horizontalScroll"
                            )
                        )
                    }
                    if (step == 7) {
                        mockResults.add(
                            CIResult(
                                id = "ci_003",
                                issueType = "CRASH",
                                severity = "ERROR",
                                description = "窗口尺寸快速变化时出现 WindowManager BadTokenException",
                                filePath = "app/src/main/java/com/example/MainActivity.kt",
                                lineNumber = 87,
                                suggestion = "在 onConfigurationChanged 中延迟操作，或使用 Lifecycle-aware 回调"
                            )
                        )
                    }
                    _update { copy(ciResults = mockResults.toList()) }
                }

                // Finalize scan
                delay(200)
                _update { copy(isScanning = false, scanProgress = 1f) }
                _effect.emit(DesktopModeEffect.ShowToast("CI 扫描完成，发现 ${mockResults.size} 个问题"))

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _update { copy(isScanning = false, errorMessage = e.message) }
                _effect.emit(DesktopModeEffect.ShowError("扫描异常: ${e.message}"))
            }
        }
    }

    /**
     * StartReflectCheck — 开始 AI Reflect Layer 检测
     *
     * Simulates Reflect adaptation status check:
     *   - Checks for fixed pixel dimensions
     *   - Identifies layout structures that may confuse Reflect
     *   - Provides adaptation recommendations
     */
    private fun startReflectCheck() {
        if (_state.value.isReflectChecking) return

        reflectJob?.cancel()
        reflectJob = viewModelScope.launch {
            _update {
                copy(
                    isReflectChecking = true,
                    reflectStatus = ReflectStatus.REFLECTING,
                    errorMessage = null
                )
            }

            try {
                delay(2000)

                // Simulate Reflect analysis
                val hasFixedPixels = _state.value.ciResults.any { it.issueType == "LAYOUT_TRUNCATION" }
                val status = if (hasFixedPixels) ReflectStatus.FAILED else ReflectStatus.ADAPTED

                _update { copy(isReflectChecking = false, reflectStatus = status) }

                when (status) {
                    ReflectStatus.ADAPTED -> _effect.emit(DesktopModeEffect.ShowToast("Reflect 检测通过：布局已适配"))
                    ReflectStatus.FAILED -> _effect.emit(DesktopModeEffect.ShowToast("Reflect 检测：发现布局问题，请修复后重试"))
                    else -> {}
                }

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _update { copy(isReflectChecking = false, reflectStatus = ReflectStatus.UNKNOWN, errorMessage = e.message) }
                _effect.emit(DesktopModeEffect.ShowError("Reflect 检测异常: ${e.message}"))
            }
        }
    }

    /**
     * TestZRAMHibernation — 测试 zRAM 休眠/唤醒行为
     *
     * Simulates zRAM hibernation test:
     *   - Freeze app state to zRAM
     *   - Measure wake recovery time
     *   - Verify state consistency after wake
     */
    private fun testZRAMHibernation() {
        if (_state.value.isZramTesting) return

        zramJob?.cancel()
        zramJob = viewModelScope.launch {
            _update {
                copy(
                    isZramTesting = true,
                    zramTestResult = null,
                    errorMessage = null
                )
            }

            try {
                // Phase 1: Hibernation
                delay(1500)
                _effect.emit(DesktopModeEffect.ShowToast("正在冻结 App 状态到 zRAM..."))
                delay(1000)

                // Phase 2: Wake
                _effect.emit(DesktopModeEffect.ShowToast("正在唤醒 App..."))
                delay(2000)

                // Generate test result
                val testResult = ZRAMTestResult(
                    id = "zram_${System.currentTimeMillis()}",
                    timestamp = System.currentTimeMillis(),
                    hibernationDurationMs = (1500L..3000L).random(),
                    wakeDurationMs = (100L..500L).random(),
                    stateConsistent = _state.value.checklist.filter { it.category == "zRAM" }.count { it.isChecked } >= 2,
                    memoryRestoredBytes = (50L..200L).random() * 1024 * 1024,
                    issues = if (_state.value.checklist.filter { it.category == "zRAM" }.count { it.isChecked } < 2) {
                        listOf("状态保存不完整，部分 in-memory 数据丢失", "建议实现 onSaveInstanceState 和 WorkManager")
                    } else {
                        emptyList()
                    }
                )

                _update { copy(isZramTesting = false, zramTestResult = testResult) }
                _effect.emit(DesktopModeEffect.ShowToast("zRAM 测试完成"))

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _update { copy(isZramTesting = false, errorMessage = e.message) }
                _effect.emit(DesktopModeEffect.ShowError("zRAM 测试异常: ${e.message}"))
            }
        }
    }

    /**
     * SetDeviceRAM — 设置模拟设备 RAM
     *
     * @param ramMB RAM 大小（MB）
     */
    private fun setDeviceRAM(ramMB: Int) {
        _update { copy(deviceRAM = ramMB) }
    }

    /**
     * SelectTab — 切换 Tab
     *
     * @param tab Target tab
     */
    private fun selectTab(tab: DesktopTab) {
        _update { copy(activeTab = tab) }
    }

    /**
     * ExportReport — 导出 Desktop Mode 质量报告
     */
    private fun exportReport() {
        viewModelScope.launch {
            try {
                delay(500)
                val filePath = "/tmp/desktop_mode_quality_report_${System.currentTimeMillis()}.json"
                _effect.emit(DesktopModeEffect.ReportReady(filePath))
                _effect.emit(DesktopModeEffect.ShowToast("报告已导出至 $filePath"))
            } catch (e: Exception) {
                _effect.emit(DesktopModeEffect.ShowError("导出失败: ${e.message}"))
            }
        }
    }

    /**
     * ToggleCheckItem — 切换 Checklist 条目
     *
     * @param itemId Checklist 条目 ID
     */
    private fun toggleCheckItem(itemId: String) {
        _update {
            copy(
                checklist = checklist.map { item ->
                    if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
                }
            )
        }
        recalculateQualityScore()
    }

    /**
     * ClearError — 清除错误消息
     */
    private fun clearError() {
        _update { copy(errorMessage = null) }
    }

    /**
     * RunDecisionAnalysis — 运行适配决策分析
     */
    private fun runDecisionAnalysis() {
        viewModelScope.launch {
            try {
                delay(800)

                // Analyze based on checklist completion
                val checkedCount = _state.value.checklist.count { it.isChecked }
                val totalCount = _state.value.checklist.size
                val completionRatio = checkedCount.toFloat() / totalCount

                val result = when {
                    completionRatio >= 0.8f -> DecisionResult(
                        recommendedApproach = "Quartz Compositor 原生适配（推荐）",
                        priority = "HIGH",
                        reasoning = "App 已完成 80%+ Desktop Mode 适配检查项，建议投入资源完成 Quartz 原生适配，获得最佳桌面体验",
                        effortEstimate = "约 16-24 人时"
                    )
                    completionRatio >= 0.5f -> DecisionResult(
                        recommendedApproach = "AI Reflect Layer + 基础适配（推荐）",
                        priority = "MEDIUM",
                        reasoning = "App 已完成 50%+ 检查项，建议先依赖 AI Reflect Layer 保证基本可用，同步推进关键项适配",
                        effortEstimate = "约 8-16 人时"
                    )
                    else -> DecisionResult(
                        recommendedApproach = "优先适配 Desktop Mode 基本要求",
                        priority = "LOW",
                        reasoning = "当前适配度较低（<50%），建议优先处理高优先级检查项（zRAM 状态恢复、键鼠交互），再考虑深度适配",
                        effortEstimate = "约 24-40 人时"
                    )
                }

                _update { copy(decisionResult = result) }
                _effect.emit(DesktopModeEffect.ShowToast("决策分析完成"))

            } catch (e: Exception) {
                _effect.emit(DesktopModeEffect.ShowError("分析失败: ${e.message}"))
            }
        }
    }

    // ================================================================
    // Private Helpers
    // ================================================================

    /**
     * update — State update helper using immutable copy
     */
    private inline fun _update(update: DesktopModeState.() -> DesktopModeState) {
        _state.update { it.update() }
    }

    /**
     * recalculateQualityScore — 根据 Checklist 完成度重新计算质量分
     */
    private fun recalculateQualityScore() {
        val checklist = _state.value.checklist
        if (checklist.isEmpty()) return

        val windowChecked = checklist.filter { it.category == "WindowAPI" }.count { it.isChecked }
        val windowTotal = checklist.count { it.category == "WindowAPI" }
        val reflectChecked = checklist.filter { it.category == "Reflect" }.count { it.isChecked }
        val reflectTotal = checklist.count { it.category == "Reflect" }
        val zramChecked = checklist.filter { it.category == "zRAM" }.count { it.isChecked }
        val zramTotal = checklist.count { it.category == "zRAM" }
        val inputChecked = checklist.filter { it.category == "Input" }.count { it.isChecked }
        val inputTotal = checklist.count { it.category == "Input" }
        val layoutChecked = checklist.filter { it.category == "Layout" }.count { it.isChecked }
        val layoutTotal = checklist.count { it.category == "Layout" }

        val multiWindow = if (windowTotal > 0) (windowChecked * 100 / windowTotal) else 0
        val stateRecovery = if (zramTotal > 0) (zramChecked * 100 / zramTotal) else 0
        val inputAdaptation = if (inputTotal > 0) (inputChecked * 100 / inputTotal) else 0
        val visualLayout = if (layoutTotal > 0) (layoutChecked * 100 / layoutTotal) else 0

        val overall = (multiWindow + stateRecovery + inputAdaptation + visualLayout) / 4

        _update {
            copy(
                qualityScore = overall,
                radarScores = RadarScores(multiWindow, stateRecovery, inputAdaptation, visualLayout)
            )
        }
    }

    // ================================================================
    // Mock Data Generators (演示用)
    // ================================================================

    /**
     * createMockCIResults — 创建模拟 CI 检测结果
     */
    private fun createMockCIResults(): List<CIResult> {
        return listOf(
            CIResult(
                id = "ci_001",
                issueType = "LAYOUT_TRUNCATION",
                severity = "ERROR",
                description = "TextView 在窗口宽度 < 300dp 时文字被截断",
                filePath = "app/src/main/res/layout/activity_main.xml",
                lineNumber = 42,
                suggestion = "使用 minWidth 和 ellipsize 属性，或改用自适应布局"
            ),
            CIResult(
                id = "ci_002",
                issueType = "OVERFLOW",
                severity = "WARNING",
                description = "Button 在小窗口模式下超出容器边界",
                filePath = "app/src/main/res/layout/dialog_settings.xml",
                lineNumber = 28,
                suggestion = "使用 wrap_content 而非固定宽度，添加 horizontalScroll"
            ),
            CIResult(
                id = "ci_003",
                issueType = "CRASH",
                severity = "ERROR",
                description = "窗口尺寸快速变化时出现 WindowManager BadTokenException",
                filePath = "app/src/main/java/com/example/MainActivity.kt",
                lineNumber = 87,
                suggestion = "在 onConfigurationChanged 中延迟操作，或使用 Lifecycle-aware 回调"
            ),
            CIResult(
                id = "ci_004",
                issueType = "KEYBOARD_SUPPORT",
                severity = "WARNING",
                description = "SearchView 缺少键盘回车确认监听",
                filePath = "app/src/main/java/com/example/ui/SearchFragment.kt",
                lineNumber = 55,
                suggestion = "添加 imeOptions=\"actionSearch\" 和 setOnEditorActionListener"
            ),
            CIResult(
                id = "ci_005",
                issueType = "STATE_PERSISTENCE",
                severity = "WARNING",
                description = "Fragment 未在 onSaveInstanceState 中保存 scroll position",
                filePath = "app/src/main/java/com/example/ui/ListFragment.kt",
                lineNumber = 103,
                suggestion = "在 onSaveInstanceState 中保存 RecyclerView scroll position"
            )
        )
    }
}
