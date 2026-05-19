package com.mvi.kenny.feature.createmywidget

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
 * CreateMyWidgetViewModel — Android 17 Create My Widget ViewModel
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
 * @see CreateMyWidgetState
 * @see CreateMyWidgetIntent
 * @see CreateMyWidgetEffect
 */
class CreateMyWidgetViewModel : ViewModel() {

    // ============================================================
    // State — UI 状态
    // ============================================================

    private val _state = MutableStateFlow(CreateMyWidgetState.Initial)
    val state: StateFlow<CreateMyWidgetState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用通道
    // ============================================================

    private val _effect = Channel<CreateMyWidgetEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent Processing — 意图处理
    // ============================================================

    /**
     * Process user intention
     * 处理用户意图
     *
     * Entry point for all user interactions. Called from UI layer via:
     *   viewModel.sendIntent(CreateMyWidgetIntent.xxx)
     *
     * @param intent User intention / 用户意图
     */
    fun sendIntent(intent: CreateMyWidgetIntent) {
        when (intent) {
            is CreateMyWidgetIntent.LoadTools -> handleLoadTools()
            is CreateMyWidgetIntent.SelectTool -> handleSelectTool(intent.toolId)
            is CreateMyWidgetIntent.NavigateBack -> handleNavigateBack()
            is CreateMyWidgetIntent.SearchTools -> handleSearchTools(intent.query)
            is CreateMyWidgetIntent.CopyCode -> handleCopyCode(intent.code, intent.label)
            is CreateMyWidgetIntent.ClearError -> handleClearError()
        }
    }

    // ============================================================
    // Load Tools / 加载工具
    // ============================================================

    /**
     * Handle load tools action
     * 处理加载工具列表操作
     *
     * Loads all 8 tools on overview page
     */
    private fun handleLoadTools() {
        _state.update { currentState ->
            currentState.copy(
                tools = defaultTools,
                filteredTools = defaultTools,
                isLoading = false
            )
        }
    }

    // ============================================================
    // Tool Selection / 工具选择
    // ============================================================

    /**
     * Handle tool selection
     * 处理工具选中操作
     *
     * @param toolId Selected tool ID / 选中的工具 ID
     */
    private fun handleSelectTool(toolId: String) {
        _state.update { currentState ->
            currentState.copy(overviewSelectedTool = toolId)
        }
        viewModelScope.launch {
            _effect.send(CreateMyWidgetEffect.NavigateToDetail(toolId))
        }
    }

    // ============================================================
    // Navigation / 导航
    // ============================================================

    /**
     * Handle back navigation
     * 处理返回导航
     */
    private fun handleNavigateBack() {
        _state.update { currentState ->
            currentState.copy(
                overviewSelectedTool = null,
                searchQuery = "",
                filteredTools = currentState.tools
            )
        }
        viewModelScope.launch {
            _effect.send(CreateMyWidgetEffect.NavigateToOverview)
        }
    }

    // ============================================================
    // Search / 搜索
    // ============================================================

    /**
     * Handle search action
     * 处理搜索操作
     *
     * Filters tools by name, description, or category
     *
     * @param query Search query / 搜索关键词
     */
    private fun handleSearchTools(query: String) {
        _state.update { currentState ->
            val filtered = if (query.isBlank()) {
                currentState.tools
            } else {
                currentState.tools.filter { tool ->
                    tool.name.contains(query, ignoreCase = true) ||
                    tool.nameEn.contains(query, ignoreCase = true) ||
                    tool.description.contains(query, ignoreCase = true) ||
                    tool.category.label.contains(query, ignoreCase = true)
                }
            }
            currentState.copy(
                searchQuery = query,
                filteredTools = filtered,
                isSearching = query.isNotBlank()
            )
        }
    }

    // ============================================================
    // Clipboard / 剪贴板
    // ============================================================

    /**
     * Handle copy code action
     * 处理复制代码操作
     *
     * @param code Code to copy / 要复制的代码
     * @param label Label for toast / Toast 标签
     */
    private fun handleCopyCode(code: String, label: String) {
        viewModelScope.launch {
            _effect.send(CreateMyWidgetEffect.CopyToClipboard(code, label))
            _effect.send(CreateMyWidgetEffect.ShowToast("$label 已复制到剪贴板"))
        }
    }

    // ============================================================
    // Error / 错误处理
    // ============================================================

    /**
     * Handle clear error action
     * 处理清除错误操作
     */
    private fun handleClearError() {
        _state.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }
}
