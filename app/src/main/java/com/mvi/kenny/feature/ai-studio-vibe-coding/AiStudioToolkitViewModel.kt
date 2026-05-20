package com.mvi.kenny.feature.ai_studio_vibe_coding

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
 * AiStudioToolkitViewModel — Google AI Studio Android Vibe Coding 开发工具包 ViewModel
 * ============================================================
 *
 * MVI Architecture: ViewModel handles Intent → executes business logic → updates State.
 *
 * State management:
 * - _state: MutableStateFlow holding current UI state
 * - state: Public immutable StateFlow exposed to UI layer
 *
 * Effect management:
 * - _effect: Channel for one-time side effects (Toast, Clipboard, Navigation)
 * - effect: Public receiveAsFlow for UI to collect
 *
 * @see AiStudioState
 * @see AiStudioIntent
 * @see AiStudioEffect
 */
class AiStudioToolkitViewModel : ViewModel() {

    // ============================================================
    // State — UI 状态
    // ============================================================

    private val _state = MutableStateFlow(AiStudioState.Initial)
    val state: StateFlow<AiStudioState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用通道
    // ============================================================

    private val _effect = Channel<AiStudioEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Initialization — 初始化质量检查和安全检查数据
    // ============================================================

    init {
        initializeQualityChecks()
        initializeSecurityChecks()
    }

    /**
     * Initialize quality check items
     * 初始化质量检查项
     */
    private fun initializeQualityChecks() {
        val checks = listOf(
            // API Version / API 版本
            QualityCheckItem(1, "API 版本", "检查是否使用最新 Jetpack Compose API", "warning"),
            QualityCheckItem(2, "API 版本", "检查是否使用过时 API (如 findViewById)", "error"),
            QualityCheckItem(3, "API 版本", "检查 minSdk/targetSdk 版本配置", "warning"),
            // Code Quality / 代码质量
            QualityCheckItem(4, "代码质量", "检查 Compose 函数命名规范 (@Composable)", "info"),
            QualityCheckItem(5, "代码质量", "检查 State 管理方式 (remember/mutableStateOf)", "warning"),
            QualityCheckItem(6, "代码质量", "检查是否缺少必要的 @Preview 注释", "info"),
            // Lint Compliance / Lint 规范
            QualityCheckItem(7, "Lint 规范", "检查硬编码字符串资源", "warning"),
            QualityCheckItem(8, "Lint 规范", "检查未使用的导入", "info"),
            QualityCheckItem(9, "Lint 规范", "检查 Compose 性能警告 (lambda in SlotTable)", "error"),
            // Permissions / 权限
            QualityCheckItem(10, "权限审计", "检查是否声明了非必要权限", "error"),
            QualityCheckItem(11, "权限审计", "检查运行时权限申请流程", "warning"),
            QualityCheckItem(12, "权限审计", "检查权限用途描述 (usesPermissionFlags)", "warning"),
            // Dependency / 依赖
            QualityCheckItem(13, "依赖管理", "检查依赖库版本是否最新", "info"),
            QualityCheckItem(14, "依赖管理", "检查是否存在版本冲突", "error"),
            QualityCheckItem(15, "依赖管理", "检查是否使用过时库 (如 AndroidX AppCompat 而非 Compose)", "warning")
        )
        _state.update { it.copy(qualityChecks = checks) }
    }

    /**
     * Initialize security compliance check items
     * 初始化安全合规检查项
     */
    private fun initializeSecurityChecks() {
        val checks = listOf(
            SecurityCheckItem(1, "隐私合规", "隐私政策声明", "检查 AndroidManifest.xml 中是否包含隐私政策链接"),
            SecurityCheckItem(2, "隐私合规", "数据收集披露", "检查是否声明了数据收集行为（Firebase Analytics 等）"),
            SecurityCheckItem(3, "隐私合规", "第三方 SDK 披露", "检查是否披露了所有第三方 SDK 的数据处理"),
            SecurityCheckItem(4, "权限最小化", "非必要权限移除", "检查是否移除了所有非必要权限"),
            SecurityCheckItem(5, "权限最小化", "敏感权限用途", "检查所有敏感权限是否有明确用途"),
            SecurityCheckItem(6, "网络安全", "明文流量检查", "检查是否允许明文 HTTP 流量（usesCleartextTraffic）"),
            SecurityCheckItem(7, "网络安全", "SSL/TLS 配置", "检查网络安全配置是否正确"),
            SecurityCheckItem(8, "代码安全", "ProGuard/R8 混淆", "检查是否启用了代码混淆"),
            SecurityCheckItem(9, "代码安全", "调试标志关闭", "检查 release 版本是否关闭了调试标志"),
            SecurityCheckItem(10, "Play Store 合规", "内容分级", "检查是否完成内容分级问卷"),
            SecurityCheckItem(11, "Play Store 合规", "广告 ID 政策", "检查是否遵守 Google Play 广告 ID 政策"),
            SecurityCheckItem(12, "Play Store 合规", "目标用户", "检查目标用户群体设置是否正确")
        )
        _state.update { it.copy(securityChecks = checks) }
    }

    // ============================================================
    // Intent Processing — 意图处理
    // ============================================================

    /**
     * Process user intention
     * 处理用户意图
     *
     * Entry point for all user interactions. Called from UI layer via:
     *   viewModel.sendIntent(AiStudioIntent.xxx)
     *
     * @param intent User intention / 用户意图
     */
    fun sendIntent(intent: AiStudioIntent) {
        when (intent) {
            is AiStudioIntent.SelectTab -> handleSelectTab(intent.index)
            is AiStudioIntent.ToggleQuickStartStep -> handleToggleQuickStartStep(intent.stepIndex)
            is AiStudioIntent.ToggleQualityCheck -> handleToggleQualityCheck(intent.checkId)
            is AiStudioIntent.ToggleHandoffStep -> handleToggleHandoffStep(intent.stepId)
            is AiStudioIntent.UpdatePromptInput -> handleUpdatePromptInput(intent.prompt)
            is AiStudioIntent.SelectComparisonTab -> handleSelectComparisonTab(intent.tabIndex)
            is AiStudioIntent.ToggleSecurityCheck -> handleToggleSecurityCheck(intent.checkId)
            is AiStudioIntent.CopyCode -> handleCopyCode(intent.code, intent.blockId)
            is AiStudioIntent.ClearCopiedState -> handleClearCopiedState()
            is AiStudioIntent.RunQualityAssessment -> handleRunQualityAssessment(intent.prompt)
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
        viewModelScope.launch {
            _effect.send(AiStudioEffect.ScrollToTop)
        }
        _state.update { it.copy(selectedTab = index) }
    }

    // ============================================================
    // Quick Start Guide / 上手指南
    // ============================================================

    /**
     * Toggle quick start step expansion
     * 切换快速开始步骤展开/收起
     *
     * @param stepIndex Step index / 步骤索引
     */
    private fun handleToggleQuickStartStep(stepIndex: Int) {
        _state.update { currentState ->
            val newExpanded = if (stepIndex in currentState.expandedQuickStartSteps) {
                currentState.expandedQuickStartSteps - stepIndex
            } else {
                currentState.expandedQuickStartSteps + stepIndex
            }
            currentState.copy(expandedQuickStartSteps = newExpanded)
        }
    }

    // ============================================================
    // Quality Assessment / 质量评估
    // ============================================================

    /**
     * Toggle quality check item
     * 切换质量检查项勾选状态
     *
     * @param checkId Check item ID / 检查项 ID
     */
    private fun handleToggleQualityCheck(checkId: Int) {
        _state.update { currentState ->
            val updatedChecks = currentState.qualityChecks.map { check ->
                if (check.id == checkId) {
                    check.copy(isChecked = !check.isChecked)
                } else {
                    check
                }
            }
            currentState.copy(qualityChecks = updatedChecks)
        }
    }

    /**
     * Handle prompt input update
     * 处理提示词输入更新
     *
     * @param prompt Prompt text / 提示词文本
     */
    private fun handleUpdatePromptInput(prompt: String) {
        _state.update { it.copy(promptInput = prompt) }
    }

    /**
     * Run quality assessment on prompt
     * 对提示词运行质量评估
     *
     * @param prompt User's prompt / 用户的提示词
     */
    private fun handleRunQualityAssessment(prompt: String) {
        if (prompt.isBlank()) {
            viewModelScope.launch {
                _effect.send(AiStudioEffect.ShowToast("请输入提示词 / Please enter a prompt"))
            }
            return
        }
        // Simulate assessment based on prompt length and specificity
        // 根据提示词长度和具体性模拟评估
        val quality = when {
            prompt.length < 50 -> "基础 - 建议添加更多细节"
            prompt.length < 150 -> "中等 - 可以更具体"
            else -> "良好 - 提示词足够具体"
        }
        val complexity = when {
            prompt.contains("数据库") || prompt.contains("network") || prompt.contains("API") -> "高复杂度"
            prompt.contains("列表") || prompt.contains("form") || prompt.contains("表单") -> "中等复杂度"
            else -> "低复杂度"
        }
        val report = "提示词质量评估结果：\n" +
                "- 详细程度: $quality\n" +
                "- 预估复杂度: $complexity\n" +
                "- 建议: 请明确说明 UI 布局、数据来源和交互行为"
        viewModelScope.launch {
            _effect.send(AiStudioEffect.ShowQualityReport(report))
        }
    }

    // ============================================================
    // Handoff Workflow / 移交工作流
    // ============================================================

    /**
     * Toggle handoff step expansion
     * 切换移交步骤展开/收起
     *
     * @param stepId Step ID / 步骤 ID
     */
    private fun handleToggleHandoffStep(stepId: Int) {
        _state.update { currentState ->
            val newExpanded = if (stepId in currentState.expandedHandoffSteps) {
                currentState.expandedHandoffSteps - stepId
            } else {
                currentState.expandedHandoffSteps + stepId
            }
            currentState.copy(expandedHandoffSteps = newExpanded)
        }
    }

    // ============================================================
    // Tool Comparison / 工具对比
    // ============================================================

    /**
     * Handle comparison tab selection
     * 处理对比 Tab 选择
     *
     * @param tabIndex Tab index / Tab 索引
     */
    private fun handleSelectComparisonTab(tabIndex: Int) {
        _state.update { it.copy(selectedComparisonTab = tabIndex) }
    }

    // ============================================================
    // Security & Compliance / 安全合规
    // ============================================================

    /**
     * Toggle security check item
     * 切换安全检查项勾选状态
     *
     * @param checkId Check item ID / 检查项 ID
     */
    private fun handleToggleSecurityCheck(checkId: Int) {
        _state.update { currentState ->
            val updatedChecks = currentState.securityChecks.map { check ->
                if (check.id == checkId) {
                    check.copy(isPassed = !check.isPassed)
                } else {
                    check
                }
            }
            currentState.copy(securityChecks = updatedChecks)
        }
    }

    // ============================================================
    // Clipboard Operations / 剪贴板操作
    // ============================================================

    /**
     * Handle copy code action
     * 处理复制代码操作
     *
     * @param code Code text to copy / 要复制的代码文本
     * @param blockId Block identifier / 代码块标识
     */
    private fun handleCopyCode(code: String, blockId: String) {
        viewModelScope.launch {
            _effect.send(AiStudioEffect.CopyToClipboard(code, blockId))
            _effect.send(AiStudioEffect.ShowToast("已复制到剪贴板 / Copied to clipboard"))
            _state.update { it.copy(copiedCodeBlockId = blockId) }
        }
    }

    /**
     * Handle clear copied state
     * 清除已复制状态
     */
    private fun handleClearCopiedState() {
        _state.update { it.copy(copiedCodeBlockId = null) }
    }
}
