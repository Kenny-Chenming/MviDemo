package com.mvi.kenny.feature.quailldebugtools

// ================================================================
// QuailDebugToolsViewModel — Android Studio Quail 调试/性能工具 ViewModel
// ================================================================
// MVI ViewModel: receives Intent, processes business logic, updates State.
//
// PRD-242: Android Studio Quail 调试/性能工具包
// Design: memory/agency/designs/PRD-242-Android-Studio-Quail-调试-性能工具包.md
//
// Key responsibilities:
//   1. Provide Recomposition Observe Node usage guide
//   2. Provide LeakCanary in Profiler usage guide
//   3. Joint diagnostic workflow coordination
//   4. CI integration best practices
//   5. Quail vs Panda version selection guidance
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ================================================================
// State & Effect Aliases
// ================================================================
typealias QuailDebugToolsUiState = QuailDebugToolsState
typealias QuailDebugToolsUiEffect = QuailDebugToolsEffect

// ================================================================
// ViewModel
// ================================================================

/**
 * ============================================================
 * QuailDebugToolsViewModel — Quail 调试/性能工具 ViewModel
 * ============================================================
 * MVI pattern: State is the single source of truth, Intent drives changes.
 *
 * NOTE: This is a local reference tool. All data is preset.
 * It does NOT connect to Android Studio or scan actual project files.
 */
class QuailDebugToolsViewModel(
    private val initialState: QuailDebugToolsState = QuailDebugToolsState()
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<QuailDebugToolsState> = _state.asStateFlow()

    // ── Effects ───────────────────────────────────────────────
    private val _effect = MutableSharedFlow<QuailDebugToolsEffect>()
    val effect: MutableSharedFlow<QuailDebugToolsEffect> = _effect

    // ==========================================================
    // Public API
    // ==========================================================

    /**
     * Process user intent / 处理用户意图
     * @param intent User action intent
     */
    fun sendIntent(intent: QuailDebugToolsIntent) {
        when (intent) {
            is QuailDebugToolsIntent.SelectTab -> selectTab(intent.index)
            is QuailDebugToolsIntent.SelectStudioVersion -> selectStudioVersion(intent.version)
            is QuailDebugToolsIntent.SelectDiagnosticStep -> selectDiagnosticStep(intent.step)
            is QuailDebugToolsIntent.CopyCode -> copyCode(intent.code)
            is QuailDebugToolsIntent.DismissSnackbar ->
                _state.update { it.copy(snackbarMessage = null) }
            is QuailDebugToolsIntent.ResetAll ->
                _state.update { QuailDebugToolsState.Initial }
        }
    }

    // ==========================================================
    // Tab Selection / Tab 选择
    // ==========================================================

    /**
     * Select a tab and load corresponding data
     * 选择 Tab 并加载对应数据
     */
    private fun selectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
        loadTabData(index)
    }

    /**
     * Load data for the selected tab
     * 加载选中 Tab 的数据
     */
    private fun loadTabData(tabIndex: Int) {
        when (tabIndex) {
            0 -> loadObserveNodeGuide()
            1 -> loadLeakCanaryGuide()
            2 -> loadDiagnosticWorkflow()
            3 -> loadCIIntegration()
            4 -> loadVersionComparison()
        }
    }

    // ==========================================================
    // Studio Version Selection / Studio 版本选择
    // ==========================================================

    private fun selectStudioVersion(version: StudioVersion) {
        _state.update { it.copy(studioVersion = version) }
        loadVersionComparison()
    }

    // ==========================================================
    // Diagnostic Step Selection / 诊断步骤选择
    // ==========================================================

    private fun selectDiagnosticStep(step: DiagnosticStep) {
        _state.update { it.copy(currentDiagnosticStep = step) }
    }

    // ==========================================================
    // Tab 0: Observe Node Guide / Tab 0: Observe Node 指南
    // ==========================================================

    /**
     * Load Observe Node guide items
     * 加载 Observe Node 指南条目
     */
    private fun loadObserveNodeGuide() {
        viewModelScope.launch {
            // Check if Observe Node is available (Quail 1 Canary 4+)
            val observeNodeReady = true // Simulated: in real implementation, would check Studio version
            _state.update {
                it.copy(
                    observeNodeGuideItems = SIMULATED_OBSERVE_NODE_GUIDE_ITEMS,
                    observeNodeReady = observeNodeReady
                )
            }
        }
    }

    // ==========================================================
    // Tab 1: LeakCanary Guide / Tab 1: LeakCanary 指南
    // ==========================================================

    /**
     * Load LeakCanary guide items
     * 加载 LeakCanary 指南条目
     */
    private fun loadLeakCanaryGuide() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    leakcanaryGuideItems = SIMULATED_LEAKCANARY_GUIDE_ITEMS,
                    leakcanaryVsLibraryItems = SIMULATED_LEAKCANARY_VS_LIBRARY_ITEMS
                )
            }
        }
    }

    // ==========================================================
    // Tab 2: Diagnostic Workflow / Tab 2: 诊断工作流
    // ==========================================================

    /**
     * Load diagnostic workflow steps
     * 加载诊断工作流步骤
     */
    private fun loadDiagnosticWorkflow() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    diagnosticWorkflowSteps = SIMULATED_DIAGNOSTIC_WORKFLOW_STEPS,
                    currentDiagnosticStep = DiagnosticStep.COLLECT
                )
            }
        }
    }

    // ==========================================================
    // Tab 3: CI Integration / Tab 3: CI 集成
    // ==========================================================

    /**
     * Load CI integration items
     * 加载 CI 集成项
     */
    private fun loadCIIntegration() {
        viewModelScope.launch {
            _state.update {
                it.copy(ciIntegrationItems = SIMULATED_CI_INTEGRATION_ITEMS)
            }
        }
    }

    // ==========================================================
    // Tab 4: Version Comparison / Tab 4: 版本对比
    // ==========================================================

    /**
     * Load version comparison items
     * 加载版本对比条目
     */
    private fun loadVersionComparison() {
        viewModelScope.launch {
            _state.update {
                it.copy(comparisonItems = SIMULATED_VERSION_COMPARISON_ITEMS)
            }
        }
    }

    // ==========================================================
    // Copy to Clipboard / 复制到剪贴板
    // ==========================================================

    private fun copyCode(code: String) {
        viewModelScope.launch {
            _effect.emit(QuailDebugToolsEffect.CopyToClipboard(code))
            _effect.emit(QuailDebugToolsEffect.ShowSnackbar("Code copied to clipboard!"))
        }
    }
}
