package com.mvi.kenny.feature.webmcp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =============================================================
// WebMcpViewModel — WebMCP Android WebView Agent 集成工具包 ViewModel
// PRD-262 | WebMCP Android WebView Agent 集成工具包
// =============================================================
// MVI Architecture: ViewModel receives Intent, updates State, emits Effect
// MVI 架构: ViewModel 接收 Intent，更新 State，发送 Effect

/**
 * WebMCP Toolkit ViewModel / WebMCP 工具包 ViewModel
 *
 * Manages UI state and business logic for the WebMCP Toolkit.
 * Follows MVI pattern: receives UserIntent → processes → updates State
 *
 * @see WebMcpToolkitState UI state
 * @see WebMcpIntent User actions
 * @see WebMcpEffect Side effects
 */
class WebMcpViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────
    // State — UI 状态 (single source of truth)
    // ─────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(WebMcpToolkitState.Initial)
    val state: StateFlow<WebMcpToolkitState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────
    // Effect — 副作用通道 (one-time events)
    // ─────────────────────────────────────────────────────────

    private val _effect = MutableSharedFlow<WebMcpEffect>()
    val effect = _effect.asSharedFlow()

    // ─────────────────────────────────────────────────────────
    // Intent Processing — 用户意图处理
    // ─────────────────────────────────────────────────────────

    /**
     * Process user intent / 处理用户意图
     *
     * Entry point for all user actions. Maps Intent → business logic.
     *
     * @param intent User action intent / 用户动作意图
     */
    fun processIntent(intent: WebMcpIntent) {
        when (intent) {
            is WebMcpIntent.NavigateTo -> navigateTo(intent.page)
            is WebMcpIntent.NavigateToDashboard -> navigateTo(ToolkitPage.DASHBOARD)
            is WebMcpIntent.StartDetection -> startDetection(intent.url)
            is WebMcpIntent.ClearDetection -> clearDetection()
            is WebMcpIntent.GoToMigrationStep -> goToMigrationStep(intent.step)
            is WebMcpIntent.CompleteMigrationStep -> completeMigrationStep(intent.step)
            is WebMcpIntent.AnswerDecisionTree -> answerDecisionTree(intent.questionIndex, intent.answer)
            is WebMcpIntent.ResetDecisionTree -> resetDecisionTree()
            is WebMcpIntent.SelectTool -> selectTool(intent.tool)
            is WebMcpIntent.ClearSelectedTool -> clearSelectedTool()
            is WebMcpIntent.CopyCode -> copyCode(intent.code)
            is WebMcpIntent.OpenExternalUrl -> openExternalUrl(intent.url)
            is WebMcpIntent.DismissError -> dismissError()
        }
    }

    // ─────────────────────────────────────────────────────────
    // Navigation / 导航
    // ─────────────────────────────────────────────────────────

    /**
     * Navigate to specified page / 导航到指定页面
     */
    private fun navigateTo(page: ToolkitPage) {
        _state.value = _state.value.copy(
            currentPage = page,
            error = null
        )
    }

    // ─────────────────────────────────────────────────────────
    // Compliance Detection / 合规检测
    // ─────────────────────────────────────────────────────────

    /**
     * Start WebMCP compliance detection / 开始 WebMCP 合规检测
     *
     * Simulates a static analysis scan of the given URL.
     * In production, this would analyze HTML/JS content.
     *
     * @param url URL to scan / 要扫描的 URL
     */
    private fun startDetection(url: String) {
        if (url.isBlank()) {
            _state.value = _state.value.copy(
                error = "请输入有效的 URL"
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                detectionState = DetectionState.SCANNING,
                detectionUrl = url,
                isLoading = true,
                error = null
            )

            // Simulate scanning delay / 模拟扫描延迟
            delay(2500)

            // Perform static analysis / 执行静态分析
            val result = performStaticAnalysis(url)

            _state.value = _state.value.copy(
                detectionState = when (result.level) {
                    ComplianceLevel.COMPLIANT -> DetectionState.COMPLIANT
                    ComplianceLevel.PARTIALLY_COMPLIANT -> DetectionState.PARTIALLY_COMPLIANT
                    ComplianceLevel.NON_COMPLIANT -> DetectionState.NON_COMPLIANT
                },
                detectionResult = result,
                isLoading = false
            )
        }
    }

    /**
     * Perform static analysis on URL / 对 URL 执行静态分析
     *
     * Simulates detection logic. In production, this would:
     * 1. Fetch the HTML/JS content
     * 2. Check for window.webMCP existence
     * 3. Validate exposeTools() calls
     * 4. Check for Origin Trial tokens
     *
     * @param url URL to analyze / 要分析的 URL
     * @return Detection result / 检测结果
     */
    private fun performStaticAnalysis(url: String): DetectionResult {
        // Simulated logic based on URL patterns / 基于 URL 模式的模拟逻辑
        // In production, this would analyze actual HTML/JS content

        val findings = mutableListOf<ComplianceFinding>()

        // Check 1: window.webMCP global / window.webMCP 全局对象
        val hasWebMcp = url.contains("agent-ready") || url.contains("webmcp-demo")
        findings.add(
            ComplianceFinding(
                type = FindingType.WEB_MCP_GLOBAL,
                severity = if (hasWebMcp) Severity.LOW else Severity.CRITICAL,
                description = if (hasWebMcp)
                    "✅ 检测到 window.webMCP 全局对象"
                else
                    "❌ 未检测到 window.webMCP 全局对象",
                codeLocation = "window.webMCP",
                suggestion = if (hasWebMcp) "无需操作"
                    else "在页面加载时初始化 window.webMCP = window.webMCP || {}"
            )
        )

        // Check 2: exposeTools() call / exposeTools() 调用
        val hasExposeTools = url.contains("agent-ready") || url.contains("toolkit")
        findings.add(
            ComplianceFinding(
                type = FindingType.EXPOSE_TOOLS_CALL,
                severity = if (hasExposeTools) Severity.LOW else Severity.HIGH,
                description = if (hasExposeTools)
                    "✅ 检测到 exposeTools() 调用"
                else
                    "⚠️ 未检测到 exposeTools() 调用",
                codeLocation = "window.webMCP.exposeTools()",
                suggestion = if (hasExposeTools) "无需操作"
                    else "调用 window.webMCP.exposeTools({ tools: [...] }) 声明工具"
            )
        )

        // Check 3: Tool Schema / 工具 Schema
        val hasSchema = url.contains("agent-ready")
        findings.add(
            ComplianceFinding(
                type = FindingType.TOOL_SCHEMA,
                severity = if (hasSchema) Severity.LOW else Severity.MEDIUM,
                description = if (hasSchema)
                    "✅ 工具 Schema 完整"
                else
                    "⚠️ 工具 Schema 可能不完整，建议检查参数定义",
                suggestion = if (hasSchema) "无需操作"
                    else "确保每个工具都有完整的 parameters schema 定义"
            )
        )

        // Check 4: Origin Trial Token / Origin Trial Token
        val hasOriginTrial = url.contains("ot-demo") || url.contains("origin-trial")
        findings.add(
            ComplianceFinding(
                type = FindingType.ORIGIN_TRIAL_TOKEN,
                severity = Severity.MEDIUM,
                description = if (hasOriginTrial)
                    "✅ 检测到 Origin Trial Token"
                else
                    "ℹ️ 未检测到 Origin Trial Token（可选，生产环境建议配置）",
                codeLocation = "<meta http-equiv=\"origin-trial\">",
                suggestion = if (hasOriginTrial) "无需操作"
                    else "如需在生产环境使用 WebMCP，请申请 Chrome Origin Trial Token"
            )
        )

        // Check 5: JavaScript Enabled / JavaScript 启用
        findings.add(
            ComplianceFinding(
                type = FindingType.JAVASCRIPT_ENABLED,
                severity = Severity.CRITICAL,
                description = "⚠️ 无法在静态分析中检测 JavaScript 启用状态",
                suggestion = "请在 WebView 配置中确保 settings.javaScriptEnabled = true"
            )
        )

        // Calculate overall compliance level / 计算总体合规等级
        val criticalCount = findings.count { it.severity == Severity.CRITICAL }
        val highCount = findings.count { it.severity == Severity.HIGH }
        val mediumCount = findings.count { it.severity == Severity.MEDIUM }

        val level = when {
            criticalCount > 0 -> ComplianceLevel.NON_COMPLIANT
            highCount > 0 -> ComplianceLevel.PARTIALLY_COMPLIANT
            mediumCount > 0 -> ComplianceLevel.PARTIALLY_COMPLIANT
            else -> ComplianceLevel.COMPLIANT
        }

        val toolsFound = if (hasExposeTools) 3 else 0
        val recommendations = buildRecommendations(findings)

        return DetectionResult(
            url = url,
            level = level,
            toolsFound = toolsFound,
            totalTools = 5,
            findings = findings.sortedBy { it.severity.priority },
            recommendations = recommendations,
            isStaticAnalysis = true
        )
    }

    /**
     * Build fix recommendations from findings / 从发现构建修复建议
     */
    private fun buildRecommendations(findings: List<ComplianceFinding>): List<String> {
        return findings
            .filter { it.severity != Severity.LOW }
            .mapIndexed { index, finding ->
                "${index + 1}. ${finding.suggestion}"
            }
    }

    /**
     * Clear detection result / 清除检测结果
     */
    private fun clearDetection() {
        _state.value = _state.value.copy(
            detectionState = DetectionState.IDLE,
            detectionUrl = "",
            detectionResult = null,
            error = null
        )
    }

    // ─────────────────────────────────────────────────────────
    // Migration Steps / 迁移步骤
    // ─────────────────────────────────────────────────────────

    /**
     * Navigate to specific migration step / 导航到指定迁移步骤
     */
    private fun goToMigrationStep(step: Int) {
        val maxStep = _state.value.migrationSteps.size - 1
        val targetStep = step.coerceIn(0, maxStep)
        _state.value = _state.value.copy(
            currentMigrationStep = targetStep
        )
    }

    /**
     * Mark migration step as completed / 标记迁移步骤完成
     */
    private fun completeMigrationStep(step: Int) {
        val updatedSteps = _state.value.migrationSteps.mapIndexed { index, migrationStep ->
            if (index == step) migrationStep.copy(isCompleted = true)
            else migrationStep
        }
        _state.value = _state.value.copy(migrationSteps = updatedSteps)

        // Auto-advance to next step / 自动进入下一步
        if (step < _state.value.migrationSteps.size - 1) {
            _state.value = _state.value.copy(currentMigrationStep = step + 1)
        }
    }

    // ─────────────────────────────────────────────────────────
    // Decision Tree / 决策树
    // ─────────────────────────────────────────────────────────

    /**
     * Answer decision tree question / 回答决策树问题
     */
    private fun answerDecisionTree(questionIndex: Int, answer: Boolean) {
        val currentNode = _state.value.decisionTreeCurrentNode
        if (currentNode == null) return

        val selectedOption = currentNode.options.getOrNull(if (answer) 0 else 1) ?: return

        val newAnswers = _state.value.decisionTreeAnswers + answer

        if (selectedOption.resultIfChosen != null) {
            // Leaf node reached / 到达叶子节点
            _state.value = _state.value.copy(
                decisionTreeAnswers = newAnswers,
                decisionTreeResult = selectedOption.resultIfChosen
            )
        } else if (selectedOption.nextNodeId != null) {
            // Continue to next node / 继续下一节点
            _state.value = _state.value.copy(
                decisionTreeCurrentNodeId = selectedOption.nextNodeId,
                decisionTreeAnswers = newAnswers,
                decisionTreeResult = null
            )
        }
    }

    /**
     * Reset decision tree / 重置决策树
     */
    private fun resetDecisionTree() {
        _state.value = _state.value.copy(
            decisionTreeCurrentNodeId = "root",
            decisionTreeAnswers = emptyList(),
            decisionTreeResult = null
        )
    }

    // ─────────────────────────────────────────────────────────
    // Tool Selection / 工具选择
    // ─────────────────────────────────────────────────────────

    /**
     * Select tool for detail view / 选中工具查看详情
     */
    private fun selectTool(tool: WebMcpTool) {
        _state.value = _state.value.copy(selectedTool = tool)
    }

    /**
     * Clear selected tool / 清除选中的工具
     */
    private fun clearSelectedTool() {
        _state.value = _state.value.copy(selectedTool = null)
    }

    // ─────────────────────────────────────────────────────────
    // Clipboard / 剪贴板
    // ─────────────────────────────────────────────────────────

    /**
     * Copy code to clipboard / 复制代码到剪贴板
     */
    private fun copyCode(code: String) {
        viewModelScope.launch {
            _effect.emit(WebMcpEffect.CopyToClipboard(code))
            _effect.emit(WebMcpEffect.ShowSnackbar("代码已复制到剪贴板 / Code copied to clipboard"))
        }
    }

    // ─────────────────────────────────────────────────────────
    // External URLs / 外部链接
    // ─────────────────────────────────────────────────────────

    /**
     * Open external URL / 打开外部链接
     */
    private fun openExternalUrl(url: String) {
        viewModelScope.launch {
            _effect.emit(WebMcpEffect.OpenExternalUrl(url))
        }
    }

    // ─────────────────────────────────────────────────────────
    // Error Handling / 错误处理
    // ─────────────────────────────────────────────────────────

    /**
     * Dismiss error message / 关闭错误信息
     */
    private fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }
}
