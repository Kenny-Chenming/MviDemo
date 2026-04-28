package com.mvi.kenny.feature.remotecompose

// ================================================================
// RemoteComposeViewModel — AndroidX Remote Compose 服务器驱动 UI 开发工具包 MVI ViewModel
// ================================================================
// ViewModel for Remote Compose developer toolkit MVI architecture.
//
// PRD-191: AndroidX Remote Compose 服务器驱动 UI 开发工具包
// Design Reference: memory/agency/designs/PRD-191-AndroidX-Remote-Compose-开发工具包.md
//
// ViewModel responsibilities:
//   1. Process incoming Intents and update State
//   2. Emit one-time Effects via Channel
//   3. Coordinate with repositories/data sources
//   4. Manage coroutine scope for async operations
//
// MVI Flow:
//   Intent → ViewModel.processIntent() → State update → Screen recomposes
//                                    → Effect → Screen handles (toast, navigation)
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================
 * RemoteComposeViewModel — MVI ViewModel
 * ============================================================
 * ViewModel for the Remote Compose toolkit screen.
 * Manages state transitions and side effects following MVI architecture.
 *
 * @see RemoteComposeState for state definition
 * @see RemoteComposeIntent for intent definitions
 * @see RemoteComposeEffect for effect definitions
 */
class RemoteComposeViewModel : ViewModel() {

    // ── State ──────────────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(RemoteComposeState())
    val state: StateFlow<RemoteComposeState> = _state.asStateFlow()

    // ── Effects ───────────────────────────────────────────────────────────────

    private val _effects = MutableSharedFlow<RemoteComposeEffect>()
    val effects = _effects.asSharedFlow()

    // ── Internal state ────────────────────────────────────────────────────────

    // Tracks which tool IDs are bookmarked (in-memory for this session)
    // 跟踪收藏的工具 ID（内存存储，仅当前会话有效）
    private val _bookmarkedTools = MutableStateFlow<Set<String>>(emptySet())

    // Tracks active payload download simulation jobs
    // 跟踪活跃的 Payload 下载模拟任务
    private var payloadDownloadJob: Job? = null

    // ── Intent Processing ─────────────────────────────────────────────────────

    /**
     * Process incoming intent and update state accordingly.
     * 处理用户意图并更新状态。
     *
     * @param intent The intent to process
     */
    fun processIntent(intent: RemoteComposeIntent) {
        viewModelScope.launch {
            when (intent) {
                is RemoteComposeIntent.SelectSection -> handleSelectSection(intent.section)
                is RemoteComposeIntent.ToggleSection -> handleToggleSection(intent.section)
                is RemoteComposeIntent.SelectTool -> handleSelectTool(intent.tool)
                is RemoteComposeIntent.CloseCodeViewer -> handleCloseCodeViewer()
                is RemoteComposeIntent.SwitchTab -> handleSwitchTab(intent.tab)
                is RemoteComposeIntent.CopyCode -> handleCopyCode(intent.code)
                is RemoteComposeIntent.UpdateSearch -> handleUpdateSearch(intent.query)
                is RemoteComposeIntent.ToggleBookmark -> handleToggleBookmark(intent.toolId)
                is RemoteComposeIntent.SimulatePayloadDownload -> handleSimulatePayloadDownload(intent.url)
                is RemoteComposeIntent.DismissError -> handleDismissError()
            }
        }
    }

    // ── Intent Handlers ──────────────────────────────────────────────────────

    /**
     * Select a toolkit section and load its tools.
     * 选择模块并加载其工具列表。
     */
    private suspend fun handleSelectSection(section: RemoteComposeSection) {
        _state.update { currentState ->
            currentState.copy(
                selectedSection = section,
                // 自动展开选中的 section
                expandedSections = currentState.expandedSections + section,
                // 清空当前选中的工具
                selectedTool = null,
                // 清空代码查看器
                codeViewerContent = "",
                codeViewerLanguage = CodeLanguage.KOTLIN
            )
        }

        // 记录分析事件
        _effects.emit(RemoteComposeEffect.LogAnalytics(
            event = "section_selected",
            params = mapOf("section" to section.name)
        ))
    }

    /**
     * Toggle section expansion.
     * 展开/折叠模块。
     */
    private fun handleToggleSection(section: RemoteComposeSection) {
        _state.update { currentState ->
            val newExpanded = if (section in currentState.expandedSections) {
                currentState.expandedSections - section
            } else {
                currentState.expandedSections + section
            }
            currentState.copy(expandedSections = newExpanded)
        }
    }

    /**
     * Select a tool and open the code viewer.
     * 选择工具并打开代码查看器。
     */
    private suspend fun handleSelectTool(tool: RemoteComposeTool) {
        _state.update { currentState ->
            currentState.copy(
                selectedTool = tool,
                codeViewerContent = tool.codeTemplate,
                codeViewerLanguage = tool.language
            )
        }

        // 记录分析事件
        _effects.emit(RemoteComposeEffect.LogAnalytics(
            event = "tool_selected",
            params = mapOf(
                "tool_id" to tool.id,
                "section" to _state.value.selectedSection.name
            )
        ))
    }

    /**
     * Close the code viewer.
     * 关闭代码查看器。
     */
    private fun handleCloseCodeViewer() {
        _state.update { currentState ->
            currentState.copy(
                selectedTool = null,
                codeViewerContent = "",
                codeViewerLanguage = CodeLanguage.KOTLIN
            )
        }
    }

    /**
     * Switch main tab.
     * 切换主 Tab。
     */
    private suspend fun handleSwitchTab(tab: MainTab) {
        _state.update { currentState ->
            currentState.copy(activeTab = tab)
        }
        _effects.emit(RemoteComposeEffect.LogAnalytics(
            event = "tab_switched",
            params = mapOf("tab" to tab.name)
        ))
    }

    /**
     * Copy code to clipboard (simulated — emits effect for Screen to handle).
     * 复制代码到剪贴板（模拟 — 发射 Effect 由 Screen 处理）。
     */
    private suspend fun handleCopyCode(code: String) {
        _effects.emit(RemoteComposeEffect.CodeCopied)
        _effects.emit(RemoteComposeEffect.ShowToast(
            message = "代码已复制到剪贴板 / Code copied to clipboard",
            isSuccess = true
        ))
    }

    /**
     * Update search query and filter tools.
     * 更新搜索查询并过滤工具。
     */
    private fun handleUpdateSearch(query: String) {
        _state.update { currentState ->
            currentState.copy(searchQuery = query)
        }
    }

    /**
     * Toggle bookmark status for a tool.
     * 收藏/取消收藏工具。
     */
    private suspend fun handleToggleBookmark(toolId: String) {
        _bookmarkedTools.update { current ->
            if (toolId in current) current - toolId else current + toolId
        }

        val isNowBookmarked = toolId in _bookmarkedTools.value
        _effects.emit(RemoteComposeEffect.ShowToast(
            message = if (isNowBookmarked) {
                "已收藏工具 / Tool bookmarked"
            } else {
                "已取消收藏 / Bookmark removed"
            },
            isSuccess = true
        ))
    }

    /**
     * Simulate payload download for demonstration purposes.
     * 模拟 Payload 下载（演示用）。
     *
     * Shows the loading → ready/error flow for the RemoteComposePlayer demo.
     * 展示 RemoteComposePlayer 的加载→就绪/错误流程。
     */
    private suspend fun handleSimulatePayloadDownload(url: String) {
        // Cancel any existing download simulation
        payloadDownloadJob?.cancel()

        // Phase 1: Set downloading state
        _state.update { currentState ->
            currentState.copy(payloadStatus = PayloadStatus.DOWNLOADING)
        }

        _effects.emit(RemoteComposeEffect.LogAnalytics(
            event = "payload_download_started",
            params = mapOf("url" to url)
        ))

        // Simulate network delay (2-4 seconds)
        delay(2500L)

        // Simulate success (80%) or error (20%)
        val success = (0..4).random() < 4

        if (success) {
            _state.update { currentState ->
                currentState.copy(payloadStatus = PayloadStatus.READY)
            }
            _effects.emit(RemoteComposeEffect.ShowToast(
                message = "Payload 下载成功 / Payload downloaded successfully",
                isSuccess = true
            ))
        } else {
            _state.update { currentState ->
                currentState.copy(payloadStatus = PayloadStatus.ERROR)
            }
            _effects.emit(RemoteComposeEffect.ShowToast(
                message = "Payload 下载失败，请重试 / Download failed, please retry",
                isSuccess = false
            ))
        }
    }

    /**
     * Dismiss error message.
     * 重置错误消息。
     */
    private fun handleDismissError() {
        _state.update { currentState ->
            currentState.copy(
                errorMessage = null,
                payloadStatus = PayloadStatus.IDLE
            )
        }
    }

    /**
     * Get all tools across all sections, optionally filtered by search query.
     * 获取所有模块的工具列表，可按搜索查询过滤。
     */
    fun getAllToolsFiltered(): List<RemoteComposeTool> {
        val query = _state.value.searchQuery.lowercase()
        if (query.isEmpty()) {
            return RemoteComposeSection.entries.flatMap { section ->
                getToolsForSection(section)
            }
        }
        // Filter by search query matching name or description
        return RemoteComposeSection.entries.flatMap { section ->
            getToolsForSection(section).filter { tool ->
                tool.nameCn.contains(query, ignoreCase = true) ||
                tool.nameEn.contains(query, ignoreCase = true) ||
                tool.id.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * Check if a tool is bookmarked.
     * 检查工具是否已收藏。
     */
    fun isToolBookmarked(toolId: String): Boolean = toolId in _bookmarkedTools.value

    /**
     * Get count of bookmarked tools.
     * 获取收藏工具数量。
     */
    fun getBookmarkedCount(): Int = _bookmarkedTools.value.size
}
