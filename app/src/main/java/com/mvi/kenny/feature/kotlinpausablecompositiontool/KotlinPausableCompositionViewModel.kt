package com.mvi.kenny.feature.kotlinpausablecompositiontool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================
 * KotlinPausableCompositionViewModel — Kotlin 2.2 + Pausable Composition Tool ViewModel
 * ============================================================
 * Processes user intents and manages UI state for the Kotlin 2.2 Context Parameters
 * and Compose Pausable Composition developer adaptation toolkit.
 *
 * Architecture: MVI (Model-View-Intent)
 * - Receives intents via sendIntent()
 * - Updates state via StateFlow<MainState>
 * - Emits side effects via SharedFlow<MainEffect>
 *
 * @param initialState Initial UI state / 初始 UI 状态
 */
class KotlinPausableCompositionViewModel(
    private val initialState: MainState = MainState.Initial
) : ViewModel() {

    // =============================================================
    // State — UI 状态流
    // =============================================================
    /** Current UI state / 当前 UI 状态 */
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<MainState> = _state.asStateFlow()

    // =============================================================
    // Effect — 副作用流
    // =============================================================
    /** One-time side effects (snackbar, etc.) / 一次性副作用 */
    private val _effect = MutableSharedFlow<MainEffect>()
    val effect: SharedFlow<MainEffect> = _effect.asSharedFlow()

    // =============================================================
    // sendIntent — 处理用户意图
    // =============================================================
    /**
     * Process user intent and update state accordingly.
     * 处理用户意图并相应更新状态。
     *
     * @param intent User intent from UI / 来自 UI 的用户意图
     */
    fun sendIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.TabSelected -> handleTabSelected(intent.index)
            is MainIntent.CopyCode -> handleCopyCode(intent.code, intent.codeId)
            is MainIntent.StartScan -> handleStartScan()
            is MainIntent.ToggleCodeExpand -> handleToggleCodeExpand(intent.codeId)
            is MainIntent.DismissCopiedSnackbar -> handleDismissSnackbar()
        }
    }

    // =============================================================
    // Intent Handlers — 意图处理器
    // =============================================================

    /**
     * Handle tab selection / 处理 Tab 选择
     * @param index Selected tab index / 选中的 Tab 索引
     */
    private fun handleTabSelected(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    /**
     * Handle code copy action / 处理代码复制操作
     * Shows snackbar confirmation and triggers clipboard copy.
     *
     * @param code Code text to copy / 要复制的代码文本
     * @param codeId Unique ID of the code block / 代码块唯一 ID
     */
    private fun handleCopyCode(code: String, codeId: String) {
        _state.update {
            it.copy(
                codeCopied = true,
                codeCopiedMessage = "已复制到剪贴板 / Copied to clipboard"
            )
        }
        viewModelScope.launch {
            _effect.emit(MainEffect.ShowCopiedSnackbar("已复制到剪贴板 / Copied to clipboard"))
            delay(100) // Brief delay to ensure effect is collected
        }
    }

    /**
     * Handle scan start action / 处理扫描开始操作
     * Simulates a 2-second scan with animated progress.
     * 模拟 2 秒扫描动画。
     */
    private fun handleStartScan() {
        _state.update { it.copy(scanInProgress = true, scanResults = emptyList()) }

        viewModelScope.launch {
            // Simulate scan delay / 模拟扫描延迟
            delay(2000)

            // Mock scan results / 模拟扫描结果
            val mockResults = listOf(
                ComposableInfo(
                    id = "scan_1",
                    name = "UserProfileCard",
                    optimizationBenefit = BenefitLevel.HIGH,
                    suggestion = "建议添加 rememberUserData 预计算逻辑，将数据转换移至后台线程，避免在 composition 中执行耗时操作。"
                ),
                ComposableInfo(
                    id = "scan_2",
                    name = "ProductListItem",
                    optimizationBenefit = BenefitLevel.HIGH,
                    suggestion = "推荐使用 key {} 包裹 LazyColumn item content，确保 item 类型稳定，减少不必要的重组。"
                ),
                ComposableInfo(
                    id = "scan_3",
                    name = "DashboardHeader",
                    optimizationBenefit = BenefitLevel.MEDIUM,
                    suggestion = "可考虑将 Header 的复杂布局拆分，减少单次 composition 的节点数量。"
                ),
                ComposableInfo(
                    id = "scan_4",
                    name = "SettingsToggle",
                    optimizationBenefit = BenefitLevel.MEDIUM,
                    suggestion = "Toggle 状态变更频繁，建议使用 derivedStateOf 避免每次重组都重新计算。"
                ),
                ComposableInfo(
                    id = "scan_5",
                    name = "SimpleTextLabel",
                    optimizationBenefit = BenefitLevel.LOW,
                    suggestion = "当前组件足够轻量，优化收益较低，无需特殊处理。"
                ),
                ComposableInfo(
                    id = "scan_6",
                    name = "ImageGalleryItem",
                    optimizationBenefit = BenefitLevel.HIGH,
                    suggestion = "图片加载和解码建议移至预计算阶段，使用 Interceptor 或自定义 LayoutManager 实现。"
                ),
                ComposableInfo(
                    id = "scan_7",
                    name = "CommentBubble",
                    optimizationBenefit = BenefitLevel.LOW,
                    suggestion = "组件较简单，保持现有实现即可。"
                )
            )

            _state.update {
                it.copy(
                    scanInProgress = false,
                    scanResults = mockResults
                )
            }
        }
    }

    /**
     * Handle code block expand/collapse toggle / 处理代码块展开/折叠切换
     * @param codeId Unique ID of the code block / 代码块唯一 ID
     */
    private fun handleToggleCodeExpand(codeId: String) {
        _state.update {
            it.copy(
                expandedCodeId = if (it.expandedCodeId == codeId) null else codeId
            )
        }
    }

    /**
     * Handle snackbar dismiss / 处理 Snackbar 关闭
     */
    private fun handleDismissSnackbar() {
        _state.update { it.copy(codeCopied = false) }
    }
}
