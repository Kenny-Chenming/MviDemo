package com.mvi.kenny.feature.verifiedfinancialcalls

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
 * VerifiedFinancialCallsViewModel — Verified Financial Calls API 工具包 ViewModel
 * ============================================================
 *
 * MVI Architecture: ViewModel handles Intent → executes business logic → updates State.
 *
 * State management:
 * - _state: MutableStateFlow holding current UI state
 * - state: Public immutable StateFlow exposed to UI layer
 *
 * Effect management:
 * - _effect: Channel for one-time side effects (Snackbar, Clipboard)
 * - effect: Public receiveAsFlow for UI to collect
 *
 * @see VerifiedFinancialCallsState
 * @see VerifiedFinancialCallsIntent
 * @see VerifiedFinancialCallsEffect
 */
class VerifiedFinancialCallsViewModel : ViewModel() {

    // ============================================================
    // State — UI 状态
    // ============================================================

    private val _state = MutableStateFlow(VerifiedFinancialCallsState.Initial)
    val state: StateFlow<VerifiedFinancialCallsState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用通道
    // ============================================================

    private val _effect = Channel<VerifiedFinancialCallsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent Processing — 意图处理
    // ============================================================

    /**
     * Process user intention
     * 处理用户意图
     *
     * Entry point for all user interactions. Called from UI layer via:
     *   viewModel.sendIntent(VerifiedFinancialCallsIntent.xxx)
     *
     * @param intent User intention / 用户意图
     */
    fun sendIntent(intent: VerifiedFinancialCallsIntent) {
        when (intent) {
            is VerifiedFinancialCallsIntent.SelectTab -> handleSelectTab(intent.index)
            is VerifiedFinancialCallsIntent.ToggleComplianceItem -> handleToggleComplianceItem(intent.itemId)
            is VerifiedFinancialCallsIntent.CopyCodeBlock -> handleCopyCodeBlock(intent.blockId, intent.content)
            is VerifiedFinancialCallsIntent.ToggleCard -> handleToggleCard(intent.cardId)
            is VerifiedFinancialCallsIntent.DismissError -> handleDismissError()
        }
    }

    // ============================================================
    // Tab Navigation / Tab 切换
    // ============================================================

    /**
     * Handle tab selection
     * 处理 Tab 切换
     *
     * @param index Selected tab index (0-4) / 选中的 Tab 索引
     */
    private fun handleSelectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    // ============================================================
    // Compliance Checklist / 合规清单
    // ============================================================

    /**
     * Toggle compliance item check state
     * 切换合规清单项勾选状态
     *
     * Saves checked state to DataStore (via remember) to persist developer progress.
     * 将勾选状态保存到 DataStore 以持久化开发者合规进度。
     *
     * @param itemId Item unique identifier / 清单项唯一标识
     */
    private fun handleToggleComplianceItem(itemId: Int) {
        _state.update { currentState ->
            val newCheckedItems = if (itemId in currentState.complianceCheckedItems) {
                currentState.complianceCheckedItems - itemId
            } else {
                currentState.complianceCheckedItems + itemId
            }
            currentState.copy(complianceCheckedItems = newCheckedItems)
        }
    }

    // ============================================================
    // Code Block Copy / 代码块复制
    // ============================================================

    /**
     * Handle code block copy to clipboard
     * 处理代码块复制到剪贴板
     *
     * Updates copiedBlockId for visual feedback (button icon change).
     * 触发 CopyToClipboard effect 并显示 Snackbar 反馈。
     *
     * @param blockId Code block identifier / 代码块标识
     * @param content Code content to copy / 要复制的代码内容
     */
    private fun handleCopyCodeBlock(blockId: String, content: String) {
        viewModelScope.launch {
            // Update UI feedback state / 更新 UI 反馈状态
            _state.update { it.copy(copiedBlockId = blockId) }

            // Send clipboard effect / 发送剪贴板副作用
            _effect.send(VerifiedFinancialCallsEffect.CopyToClipboard(content))
            _effect.send(VerifiedFinancialCallsEffect.ShowSnackbar("已复制 / Copied to clipboard"))

            // Reset copied state after delay / 延迟重置复制状态
            kotlinx.coroutines.delay(2000)
            _state.update { it.copy(copiedBlockId = null) }
        }
    }

    // ============================================================
    // Card Expansion / 卡片展开
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
    // Error Handling / 错误处理
    // ============================================================

    /**
     * Dismiss error message
     * 关闭错误提示
     */
    private fun handleDismissError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
