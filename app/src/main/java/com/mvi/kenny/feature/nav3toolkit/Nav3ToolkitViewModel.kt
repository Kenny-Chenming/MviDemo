package com.mvi.kenny.feature.nav3toolkit

// ================================================================
// Nav3ToolkitViewModel — Jetpack Navigation 3 响应式导航集成工具包 MVI ViewModel
// ================================================================
// ViewModel for Jetpack Navigation 3 Responsive Navigation Toolkit.
//
// PRD-258: Jetpack Navigation 3 响应式导航集成工具包
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Manage tab navigation (5 tabs)
//   - Handle chapter expansion and anchor navigation
//   - Process decision tree interactions (RadioGroup + result card)
//   - Toggle between v2.x / v3 code perspective
//   - Copy NavGraph XML to clipboard
//   - Search/filter chapters
//   - Expose one-time Effects (toast, scroll)
//
// MVI Flow:
//   Intent (user action) → ViewModel.process() → State update + Effect emit
//   UI observes State via StateFlow, collects Effect via Channel/Flow
//
// @see Nav3ToolkitState
// @see Nav3ToolkitIntent
// @see Nav3ToolkitEffect
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================
 * Nav3ToolkitViewModel — Navigation 3 工具包 ViewModel
 * ============================================================
 * Manages Nav3ToolkitState and processes Nav3ToolkitIntent.
 *
 * 5 Tabs:
 *   Tab 0: 入门与迁移 (Getting Started & Migration)
 *   Tab 1: 自适应导航 (Adaptive Navigation)
 *   Tab 2: 折叠屏集成 (Foldable Integration)
 *   Tab 3: 跨设备状态 (Cross-Device State)
 *   Tab 4: CI 验证工具 (CI Validation Tools)
 *
 * Architecture:
 *   Intent → handle*() methods → _state.update{} → _effect.send{}
 *
 * @see Nav3ToolkitState
 * @see Nav3ToolkitIntent
 * @see Nav3ToolkitEffect
 */
class Nav3ToolkitViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(Nav3ToolkitState.Initial)
    val state: StateFlow<Nav3ToolkitState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via SharedFlow
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<Nav3ToolkitEffect>()
    val effect = _effect.asSharedFlow()

    init {
        // Initialize with Tab 0 chapters
        loadChaptersForTab(0)
        // Initialize decision tree
        _state.update { it.copy(decisionTreeNodes = SIMULATED_DECISION_TREE) }
        // Set default NavGraph XML
        _state.update { it.copy(navGraphXml = DEFAULT_NAV_GRAPH_XML) }
    }

    // ─────────────────────────────────────────────────────────────
    // Public API for UI to send intents
    // ─────────────────────────────────────────────────────────────

    /**
     * 处理用户 Intent
     * @param intent 用户意图
     */
    fun sendIntent(intent: Nav3ToolkitIntent) {
        viewModelScope.launch {
            when (intent) {
                is Nav3ToolkitIntent.SelectTab -> handleSelectTab(intent.index)
                is Nav3ToolkitIntent.JumpToChapter -> handleJumpToChapter(intent.chapterId)
                is Nav3ToolkitIntent.SelectDecisionPath -> handleSelectDecisionPath(intent.questionId, intent.answerId)
                is Nav3ToolkitIntent.ToggleLegacyMode -> handleToggleLegacyMode(intent.isLegacy)
                is Nav3ToolkitIntent.CopyNavGraph -> handleCopyNavGraph()
                is Nav3ToolkitIntent.Search -> handleSearch(intent.query)
                is Nav3ToolkitIntent.ToggleChapter -> handleToggleChapter(intent.chapterId)
                is Nav3ToolkitIntent.DismissSnackbar -> handleDismissSnackbar()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Intent Handlers
    // ─────────────────────────────────────────────────────────────

    /**
     * 处理 Tab 切换
     * @param index 新 Tab 索引 (0-4)
     */
    private fun handleSelectTab(index: Int) {
        _state.update { it.copy(selectedTab = index, searchQuery = "") }
        loadChaptersForTab(index)
    }

    /**
     * 加载指定 Tab 的章节内容
     * @param tabIndex Tab 索引
     */
    private fun loadChaptersForTab(tabIndex: Int) {
        val chapters = SIMULATED_NAV_CHAPTERS[tabIndex] ?: emptyList()
        _state.update { it.copy(chapters = chapters, expandedChapterId = null) }
    }

    /**
     * 处理章节锚点跳转
     * @param chapterId 目标章节 ID
     */
    private fun handleJumpToChapter(chapterId: String) {
        viewModelScope.launch {
            _state.update { it.copy(expandedChapterId = chapterId) }
            _effect.emit(Nav3ToolkitEffect.ScrollToChapter(chapterId))
        }
    }

    /**
     * 处理决策树选项选择
     * @param questionId 问题节点 ID
     * @param answerId 答案选项 ID
     */
    private fun handleSelectDecisionPath(questionId: String, answerId: String) {
        val currentPath = _state.value.selectedDecisionPath.toMutableMap()
        currentPath[questionId] = answerId
        _state.update { it.copy(selectedDecisionPath = currentPath) }

        // Find the selected option and navigate to next node
        val nodes = _state.value.decisionTreeNodes
        val questionNode = nodes.find { it.id == questionId }
        val selectedOption = questionNode?.options?.find { it.id == answerId }

        if (selectedOption != null) {
            val nextNode = nodes.find { it.id == selectedOption.nextNodeId }
            _state.update { it.copy(activeDecisionNode = nextNode) }
        }
    }

    /**
     * 处理 Legacy 模式切换（v2.x / v3 代码视角）
     * @param isLegacy 是否为 Legacy 模式
     */
    private fun handleToggleLegacyMode(isLegacy: Boolean) {
        _state.update { it.copy(isLegacy = isLegacy) }
    }

    /**
     * 处理复制 NavGraph XML
     */
    private fun handleCopyNavGraph() {
        viewModelScope.launch {
            val xml = _state.value.navGraphXml.ifEmpty { DEFAULT_NAV_GRAPH_XML }
            _state.update { it.copy(copySuccess = true) }
            _effect.emit(Nav3ToolkitEffect.ShowCopiedToast(xml.take(50) + "..."))
        }
    }

    /**
     * 处理搜索
     * @param query 搜索查询
     */
    private fun handleSearch(query: String) {
        _state.update { it.copy(searchQuery = query) }

        if (query.isBlank()) {
            // Reset to all chapters for current tab
            loadChaptersForTab(_state.value.selectedTab)
        } else {
            // Filter chapters by query (title or content)
            val allChapters = SIMULATED_NAV_CHAPTERS[_state.value.selectedTab] ?: emptyList()
            val filtered = allChapters.filter { chapter ->
                chapter.title.contains(query, ignoreCase = true) ||
                chapter.content.contains(query, ignoreCase = true)
            }
            _state.update { it.copy(chapters = filtered) }
        }
    }

    /**
     * 处理章节展开/收起
     * @param chapterId 章节 ID
     */
    private fun handleToggleChapter(chapterId: String) {
        val currentExpanded = _state.value.expandedChapterId
        _state.update {
            it.copy(
                expandedChapterId = if (currentExpanded == chapterId) null else chapterId
            )
        }
    }

    /**
     * 关闭 Snackbar
     */
    private fun handleDismissSnackbar() {
        _state.update { it.copy(snackbarMessage = null, copySuccess = false) }
    }
}
