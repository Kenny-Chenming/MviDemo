package com.mvi.kenny.feature.panda4workflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================
 * Panda4WorkflowViewModel — Android Studio Panda 4 AI 工作流工具包 ViewModel
 * ============================================================
 *
 * MVI Architecture: ViewModel handles Intent → executes business logic → updates State.
 *
 * State management:
 * - _state: MutableStateFlow holding current UI state
 * - state: Public immutable StateFlow exposed to UI layer
 *
 * Effect management:
 * - _effect: Channel for one-time side effects (Toast, Clipboard)
 * - effect: Public receiveAsFlow for UI to collect
 *
 * @see Panda4WorkflowState
 * @see Panda4WorkflowIntent
 * @see Panda4WorkflowEffect
 */
class Panda4WorkflowViewModel : ViewModel() {

    // ============================================================
    // State — UI 状态
    // ============================================================

    private val _state = MutableStateFlow(Panda4WorkflowState.Initial)
    val state: StateFlow<Panda4WorkflowState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用通道
    // ============================================================

    private val _effect = Channel<Panda4WorkflowEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent Processing — 意图处理
    // ============================================================

    /**
     * Process user intention
     * 处理用户意图
     *
     * Entry point for all user interactions. Called from UI layer via:
     *   viewModel.sendIntent(Panda4WorkflowIntent.xxx)
     *
     * @param intent User intention / 用户意图
     */
    fun sendIntent(intent: Panda4WorkflowIntent) {
        when (intent) {
            is Panda4WorkflowIntent.SelectTab -> handleSelectTab(intent.index)
            is Panda4WorkflowIntent.ToggleCard -> handleToggleCard(intent.cardId)
            is Panda4WorkflowIntent.ToggleScenario -> handleToggleScenario(intent.scenarioId)
            is Panda4WorkflowIntent.RunComparison -> handleRunComparison()
            is Panda4WorkflowIntent.CopyToClipboard -> handleCopyToClipboard(intent.text)
        }
    }

    // ============================================================
    // Tab Navigation / Tab 切换
    // ============================================================

    /**
     * Handle tab selection
     * 处理 Tab 切换
     *
     * @param index Selected tab index / 选中的 Tab 索引
     */
    private fun handleSelectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    // ============================================================
    // Expand/Collapse Handlers / 展开/收起处理
    // ============================================================

    /**
     * Toggle card expansion state
     * 切换卡片展开/收起状态
     *
     * @param cardId Card identifier / 卡片标识
     */
    private fun handleToggleCard(cardId: Int) {
        _state.update { currentState ->
            val newExpanded = if (cardId in currentState.expandedCards) {
                currentState.expandedCards - cardId
            } else {
                currentState.expandedCards + cardId
            }
            currentState.copy(expandedCards = newExpanded)
        }
    }

    // ============================================================
    // Comparison Tool / 竞品对比工具
    // ============================================================

    /**
     * Toggle comparison scenario selection
     * 切换竞品对比场景选中状态
     *
     * @param scenarioId Scenario identifier / 场景标识
     */
    private fun handleToggleScenario(scenarioId: Int) {
        _state.update { currentState ->
            val newScenarios = if (scenarioId in currentState.selectedScenarios) {
                currentState.selectedScenarios - scenarioId
            } else {
                currentState.selectedScenarios + scenarioId
            }
            currentState.copy(selectedScenarios = newScenarios)
        }
    }

    /**
     * Handle run comparison action
     * 处理运行竞品对比操作
     *
     * Generates recommendation based on selected scenarios
     */
    private fun handleRunComparison() {
        val selected = _state.value.selectedScenarios

        // Android Studio Panda 4 specific comparison logic
        // Android Studio Panda 4 特定对比逻辑
        val result = when {
            selected.isEmpty() -> ComparisonResult(
                recommended = "Android Studio Panda 4",
                reasoning = "No scenarios selected. Android Studio Panda 4 is the recommended starting point for all Android AI-assisted development scenarios.",
                alternatives = listOf("GitHub Copilot", "Claude Code", "Cursor")
            )
            // Planning Mode scenarios
            // Planning Mode 场景
            selected.contains(0) && selected.size == 1 -> ComparisonResult(
                recommended = "Android Studio Panda 4 (Planning Mode)",
                reasoning = "Planning Mode excels at breaking down complex multi-file implementation tasks. Native Android Studio integration provides context-aware plan generation.",
                alternatives = listOf("Claude Code (use /android-plan)", "GitHub Copilot Chat")
            )
            // Code editing speed scenarios
            // 代码编辑速度场景
            selected.contains(1) && selected.size == 1 -> ComparisonResult(
                recommended = "Cursor",
                reasoning = "Cursor's NEP implementation provides faster single-file editing throughput. However, Panda 4 NEP offers better project-wide context understanding.",
                alternatives = listOf("Android Studio Panda 4 NEP", "GitHub Copilot")
            )
            // Documentation lookup scenarios
            // 文档查找场景
            selected.contains(2) && selected.size == 1 -> ComparisonResult(
                recommended = "Android Studio Panda 4 (Agent Web Search)",
                reasoning = "Agent Web Search is built into the IDE and provides real-time library documentation. No context switching required.",
                alternatives = listOf("Claude (web search)", "Browser + IDE dual monitor")
            )
            // Multi-tool orchestration scenarios
            // 多工具编排场景
            selected.contains(3) && selected.size == 1 -> ComparisonResult(
                recommended = "Claude Code",
                reasoning = "Claude Code offers superior multi-tool orchestration with shell/tools integration. Better for complex CI/CD and build pipeline tasks.",
                alternatives = listOf("Android Studio Panda 4 (Skills)", "GitHub Copilot Workspace")
            )
            // Enterprise security scenarios
            // 企业安全场景
            selected.contains(4) && selected.size == 1 -> ComparisonResult(
                recommended = "Android Studio Panda 4",
                reasoning = "Panda 4 NEP processes code locally. No code is uploaded to external servers for editing prediction, addressing enterprise security concerns.",
                alternatives = listOf("GitHub Copilot (with Enterprise security mode)")
            )
            // Combined scenarios
            // 组合场景
            selected.size >= 3 -> ComparisonResult(
                recommended = "Android Studio Panda 4",
                reasoning = "For Android-specific development, Panda 4's native IDE integration, Android Skills ecosystem, and Planning Mode provide the most comprehensive workflow coverage.",
                alternatives = listOf("Claude Code + Android Studio", "GitHub Copilot + Custom Scripts")
            )
            else -> ComparisonResult(
                recommended = "Android Studio Panda 4",
                reasoning = "Android Studio Panda 4 is the recommended tool for Android development with its native Planning Mode, NEP, and Agent Web Search features.",
                alternatives = listOf("Claude Code", "GitHub Copilot", "Cursor")
            )
        }

        _state.update { it.copy(comparisonResult = result) }
    }

    // ============================================================
    // Clipboard / 剪贴板
    // ============================================================

    /**
     * Handle copy to clipboard action
     * 处理复制到剪贴板操作
     *
     * @param text Text to copy / 要复制的文本
     */
    private fun handleCopyToClipboard(text: String) {
        viewModelScope.launch {
            _effect.send(Panda4WorkflowEffect.CopyToClipboard(text))
            _effect.send(Panda4WorkflowEffect.ShowToast("已复制到剪贴板 / Copied to clipboard"))
        }
    }
}
