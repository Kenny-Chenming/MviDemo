package com.mvi.kenny.feature.appastool

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
 * AppAsToolViewModel — App-as-Tool 开发者工具包 ViewModel
 * ============================================================
 *
 * MVI Architecture: ViewModel handles Intent → executes business logic → updates State.
 *
 * State management:
 * - _state: MutableStateFlow holding current UI state
 * - state: Public immutable StateFlow exposed to UI layer
 *
 * Effect management:
 * - _effect: Channel for one-time side effects (Toast, Clipboard, File Export)
 * - effect: Public receiveAsFlow for UI to collect
 *
 * @see AppAsToolState
 * @see AppAsToolIntent
 * @see AppAsToolEffect
 */
class AppAsToolViewModel : ViewModel() {

    // ============================================================
    // State — UI 状态
    // ============================================================

    private val _state = MutableStateFlow(AppAsToolState.Initial)
    val state: StateFlow<AppAsToolState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用通道
    // ============================================================

    private val _effect = Channel<AppAsToolEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent Processing — 意图处理
    // ============================================================

    /**
     * Process user intention
     * 处理用户意图
     *
     * Entry point for all user interactions. Called from UI layer via:
     *   viewModel.sendIntent(AppAsToolIntent.xxx)
     *
     * @param intent User intention / 用户意图
     */
    fun sendIntent(intent: AppAsToolIntent) {
        when (intent) {
            is AppAsToolIntent.SelectTab -> handleSelectTab(intent.index)
            is AppAsToolIntent.SelectSchemaTemplate -> handleSelectSchemaTemplate(intent.template)
            is AppAsToolIntent.UpdateSchemaName -> handleUpdateSchemaName(intent.name)
            is AppAsToolIntent.UpdateSchemaDescription -> handleUpdateSchemaDescription(intent.description)
            is AppAsToolIntent.UpdateSchemaParameters -> handleUpdateSchemaParameters(intent.parameters)
            is AppAsToolIntent.UpdateSchemaReturns -> handleUpdateSchemaReturns(intent.returns)
            is AppAsToolIntent.ToggleCard -> handleToggleCard(intent.cardId)
            is AppAsToolIntent.ToggleSection -> handleToggleSection(intent.sectionId)
            is AppAsToolIntent.CopyToClipboard -> handleCopyToClipboard(intent.text)
            is AppAsToolIntent.ExportSchema -> handleExportSchema(intent.json)
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
    // Schema Creation Tool (Tab 2) / Schema 创作工具
    // ============================================================

    /**
     * Handle schema template selection
     * 处理 Schema 模板选择
     *
     * @param template Selected template / 选中的模板
     */
    private fun handleSelectSchemaTemplate(template: SchemaTemplate) {
        _state.update { currentState ->
            // Pre-fill form fields based on template selection
            // 根据模板类型预填充表单字段
            val (schemaName, schemaDesc, schemaParams, schemaReturns) = when (template) {
                SchemaTemplate.BASIC -> listOf("", "", "", "")
                SchemaTemplate.ADVANCED -> listOf("", "", "", "")
                SchemaTemplate.WITH_PARAMS -> listOf("", "", """[{"name":"param1","type":"String","required":true}]""", "")
                SchemaTemplate.WITH_RETURN -> listOf("", "", "", "{}")
            }
            currentState.copy(
                schemaTemplate = template,
                schemaName = schemaName,
                schemaDescription = schemaDesc,
                schemaParameters = schemaParams,
                schemaReturns = schemaReturns
            )
        }
    }

    /**
     * Handle schema name update
     * 处理 Schema 名称更新
     */
    private fun handleUpdateSchemaName(name: String) {
        _state.update { it.copy(schemaName = name) }
    }

    /**
     * Handle schema description update
     * 处理 Schema 描述更新
     */
    private fun handleUpdateSchemaDescription(description: String) {
        _state.update { it.copy(schemaDescription = description) }
    }

    /**
     * Handle schema parameters update
     * 处理 Schema 参数更新
     */
    private fun handleUpdateSchemaParameters(parameters: String) {
        _state.update { it.copy(schemaParameters = parameters) }
    }

    /**
     * Handle schema returns update
     * 处理 Schema 返回值更新
     */
    private fun handleUpdateSchemaReturns(returns: String) {
        _state.update { it.copy(schemaReturns = returns) }
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

    /**
     * Toggle intent section expansion state
     * 切换 Intent 段落展开/收起状态
     *
     * @param sectionId Section identifier / 段落标识
     */
    private fun handleToggleSection(sectionId: Int) {
        _state.update { currentState ->
            val newExpanded = if (sectionId in currentState.expandedSections) {
                currentState.expandedSections - sectionId
            } else {
                currentState.expandedSections + sectionId
            }
            currentState.copy(expandedSections = newExpanded)
        }
    }

    // ============================================================
    // Clipboard & Export / 剪贴板与导出
    // ============================================================

    /**
     * Handle copy to clipboard action
     * 处理复制到剪贴板操作
     *
     * @param text Text to copy / 要复制的文本
     */
    private fun handleCopyToClipboard(text: String) {
        viewModelScope.launch {
            _effect.send(AppAsToolEffect.CopyToClipboard(text))
            _effect.send(AppAsToolEffect.ShowToast("已复制到剪贴板 / Copied to clipboard"))
        }
    }

    /**
     * Handle schema export action
     * 处理 Schema 导出操作
     *
     * @param json JSON content to export / 要导出的 JSON 内容
     */
    private fun handleExportSchema(json: String) {
        viewModelScope.launch {
            _effect.send(AppAsToolEffect.ExportSchemaFile(json))
            _effect.send(AppAsToolEffect.ShowToast("Schema 已导出 / Schema exported"))
        }
    }
}
