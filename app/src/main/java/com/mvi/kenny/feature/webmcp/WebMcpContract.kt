package com.mvi.kenny.feature.webmcp

import androidx.compose.ui.graphics.Color

// =============================================================
// WebMcpContract — WebMCP Android WebView Agent 集成工具包 MVI 契约
// PRD-262 | WebMCP Android WebView Agent 集成工具包
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class, single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, clipboard, etc.)

// =============================================================
// ToolkitPage — 工具包页面枚举
// =============================================================
/**
 * WebMCP Toolkit page / WebMCP 工具包页面
 *
 * Defines all pages/screens within the WebMCP toolkit.
 *
 * @param title Page title / 页面标题
 * @param subtitle Short description / 简短描述
 * @param iconName Material icon name / Material 图标名称
 */
enum class ToolkitPage(
    val title: String,
    val subtitle: String,
    val iconName: String
) {
    DASHBOARD("Dashboard", "工具包主页 / 概览", "dashboard"),
    COMPATIBILITY("兼容性检测", "WebView 版本与 WebMCP 支持检测", "verified_user"),
    API_GUIDE("API 指南", "window.webMCP.exposeTools() 完整用法", "code"),
    MIGRATION("迁移工具", "WebView → Agent-Ready WebView", "transform"),
    SECURITY("安全配置", "Origin 限制 / Deep Link vs WebMCP 决策树", "security")
}

// =============================================================
// DetectionState — 合规检测状态
// =============================================================
/**
 * Compliance detection state / 合规检测状态
 *
 * @param label Display label / 显示标签
 */
enum class DetectionState(val label: String) {
    IDLE("待检测"),
    SCANNING("检测中"),
    COMPLIANT("✅ 合规"),
    PARTIALLY_COMPLIANT("⚠️ 部分合规"),
    NON_COMPLIANT("❌ 不合规"),
    ERROR("检测失败")
}

// =============================================================
// ComplianceLevel — 合规等级
// =============================================================
/**
 * WebMCP compliance level / WebMCP 合规等级
 *
 * @param label Display label / 显示标签
 * @param color Status color / 状态颜色
 * @param description Level description / 等级描述
 */
enum class ComplianceLevel(
    val label: String,
    val color: Color,
    val description: String
) {
    COMPLIANT(
        label = "✅ 合规",
        color = Color(0xFF4CAF50),
        description = "WebMCP 工具已正确声明，可被 AI Agent 正常调用"
    ),
    PARTIALLY_COMPLIANT(
        label = "⚠️ 部分合规",
        color = Color(0xFFFF9800),
        description = "部分 WebMCP 工具已声明，建议补充缺失部分"
    ),
    NON_COMPLIANT(
        label = "❌ 不合规",
        color = Color(0xFFF44336),
        description = "未检测到 WebMCP 工具声明，Agent 无法结构化交互"
    )
}

// =============================================================
// DetectionResult — 合规检测结果
// =============================================================
/**
 * WebMCP compliance detection result / WebMCP 合规检测结果
 *
 * @param url URL scanned / 扫描的 URL
 * @param level Compliance level / 合规等级
 * @param toolsFound Number of WebMCP tools found / 发现的 WebMCP 工具数量
 * @param totalTools Expected total tools / 预期工具总数
 * @param findings Detailed findings / 详细发现
 * @param recommendations Fix recommendations / 修复建议
 * @param isStaticAnalysis Whether this is static analysis only / 是否仅为静态分析
 */
data class DetectionResult(
    val url: String,
    val level: ComplianceLevel,
    val toolsFound: Int,
    val totalTools: Int,
    val findings: List<ComplianceFinding>,
    val recommendations: List<String>,
    val isStaticAnalysis: Boolean = true
)

/**
 * Individual compliance finding / 单个合规发现
 *
 * @param type Finding type / 发现类型
 * @param severity Severity level / 严重程度
 * @param description Finding description / 发现描述
 * @param codeLocation Code location if applicable / 代码位置
 * @param suggestion Fix suggestion / 修复建议
 */
data class ComplianceFinding(
    val type: FindingType,
    val severity: Severity,
    val description: String,
    val codeLocation: String? = null,
    val suggestion: String
)

/**
 * Finding type / 发现类型
 */
enum class FindingType {
    WEB_MCP_GLOBAL,        // window.webMCP 是否存在
    EXPOSE_TOOLS_CALL,     // exposeTools() 是否被调用
    TOOL_SCHEMA,           // 工具 Schema 是否完整
    ORIGIN_TRIAL_TOKEN,    // Origin Trial Token 是否配置
    JAVASCRIPT_ENABLED,    // JavaScript 是否启用
    SECURITY_HEADERS       // 安全头是否配置
}

/**
 * Finding severity / 发现严重程度
 *
 * @param label Display label / 显示标签
 * @param priority Sort priority / 排序优先级
 * @param color Badge color / 徽章颜色
 */
enum class Severity(val label: String, val priority: Int, val color: Color) {
    CRITICAL("P0 — 阻塞", 0, Color(0xFFB3261E)),
    HIGH("P1 — 高", 1, Color(0xFFF57C00)),
    MEDIUM("P2 — 中", 2, Color(0xFFF9A825)),
    LOW("P3 — 低", 3, Color(0xFF4CAF50))
}

// =============================================================
// WebMcpTool — WebMCP 工具声明模板
// =============================================================
/**
 * WebMCP tool declaration template / WebMCP 工具声明模板
 *
 * @param id Tool ID / 工具 ID
 * @param name Tool name / 工具名称
 * @param description Tool description / 工具描述
 * @param parameters Tool parameters / 工具参数
 * @param codeTemplate JavaScript code template / JavaScript 代码模板
 * @param category Tool category / 工具分类
 */
data class WebMcpTool(
    val id: String,
    val name: String,
    val description: String,
    val parameters: List<ToolParameter>,
    val codeTemplate: String,
    val category: String
)

/**
 * Tool parameter / 工具参数
 *
 * @param name Parameter name / 参数名称
 * @param type Parameter type / 参数类型
 * @param description Parameter description / 参数描述
 * @param required Whether required / 是否必需
 */
data class ToolParameter(
    val name: String,
    val type: String,
    val description: String,
    val required: Boolean = true
)

// =============================================================
// MigrationStep — 迁移步骤
// =============================================================
/**
 * WebView → Agent-Ready WebView migration step / 迁移步骤
 *
 * @param stepNumber Step number / 步骤编号
 * @param title Step title / 步骤标题
 * @param description Step description / 步骤描述
 * @param codeTemplate Step code template / 步骤代码模板
 * @param isCompleted Whether step is completed / 是否已完成
 */
data class MigrationStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val codeTemplate: String,
    val isCompleted: Boolean = false
)

// =============================================================
// DecisionTreeNode — 决策树节点
// =============================================================
/**
 * Deep Link vs WebMCP decision tree node / Deep Link vs WebMCP 决策树节点
 *
 * @param question Question text / 问题文本
 * @param options Answer options / 答案选项
 */
data class DecisionTreeNode(
    val question: String,
    val options: List<DecisionOption>
)

/**
 * Decision tree option / 决策树选项
 *
 * @param answer Answer text / 答案文本
 * @param nextNodeId Next node ID / 下一节点 ID
 * @param resultIfChosen Final result if chosen / 选择此答案时的最终结果
 */
data class DecisionOption(
    val answer: String,
    val nextNodeId: String? = null,
    val resultIfChosen: DecisionResult? = null
)

/**
 * Final decision result / 最终决策结果
 *
 * @param verdict Decision verdict / 决策结论
 * @param recommendation Primary recommendation / 主要建议
 * @param secondaryRecommendation Secondary recommendation / 次要建议
 * @param reason Detailed reason / 详细理由
 * @param useWebMcp Whether to use WebMCP / 是否使用 WebMCP
 */
data class DecisionResult(
    val verdict: String,
    val recommendation: String,
    val secondaryRecommendation: String?,
    val reason: String,
    val useWebMcp: Boolean
)

// =============================================================
// WebMcpToolkitState — 页面状态
// =============================================================
/**
 * WebMCP Toolkit State / WebMCP 工具包页面状态
 *
 * Single source of truth for the entire WebMCP Toolkit UI.
 *
 * @param currentPage Current page / 当前页面
 * @param detectionState Detection state / 检测状态
 * @param detectionUrl URL being detected / 正在检测的 URL
 * @param detectionResult Detection result / 检测结果
 * @param migrationSteps All migration steps / 所有迁移步骤
 * @param currentMigrationStep Current migration step index / 当前迁移步骤索引
 * @param decisionTreeCurrentNodeId Current decision tree node ID / 当前决策树节点 ID
 * @param decisionTreeAnswers User's answers to decision tree questions / 用户决策树答案
 * @param decisionTreeResult Final decision tree result / 决策树最终结果
 * @param selectedTool Selected WebMCP tool for detail view / 选中的工具详情
 * @param isLoading Whether any background operation is in progress / 是否有后台操作
 * @param error Error message if any / 错误信息
 */
data class WebMcpToolkitState(
    val currentPage: ToolkitPage = ToolkitPage.DASHBOARD,
    val detectionState: DetectionState = DetectionState.IDLE,
    val detectionUrl: String = "",
    val detectionResult: DetectionResult? = null,
    val migrationSteps: List<MigrationStep> = emptyList(),
    val currentMigrationStep: Int = 0,
    val decisionTreeCurrentNodeId: String = "root",
    val decisionTreeAnswers: List<Boolean> = emptyList(),
    val decisionTreeResult: DecisionResult? = null,
    val selectedTool: WebMcpTool? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = WebMcpToolkitState(
            migrationSteps = getDefaultMigrationSteps()
        )
    }

    /**
     * Current decision tree node / 决策树当前节点
     */
    val decisionTreeCurrentNode: DecisionTreeNode?
        get() = DECISION_TREE_NODES[decisionTreeCurrentNodeId]

    /**
     * Progress percentage for current migration step / 当前迁移步骤进度百分比
     */
    val migrationProgress: Int
        get() = if (migrationSteps.isEmpty()) 0
                else ((currentMigrationStep + 1) * 100) / migrationSteps.size

    /**
     * Completed migration steps count / 已完成迁移步骤数量
     */
    val completedStepsCount: Int
        get() = migrationSteps.count { it.isCompleted }
}

// =============================================================
// Default Migration Steps Data / 默认迁移步骤数据
// =============================================================
/**
 * Default WebView → Agent-Ready WebView migration steps /
 * 默认 WebView → Agent-Ready WebView 迁移步骤
 */
private fun getDefaultMigrationSteps(): List<MigrationStep> = listOf(
    MigrationStep(
        stepNumber = 1,
        title = "启用 JavaScript",
        description = "确保 WebView 已启用 JavaScript，这是 WebMCP 的前提条件",
        codeTemplate = """
// Kotlin: 启用 JavaScript
webView.settings.javaScriptEnabled = true
webView.settings.domStorageEnabled = true
        """.trimIndent()
    ),
    MigrationStep(
        stepNumber = 2,
        title = "添加 WebMCP 工具声明",
        description = "在页面 HTML 中添加 window.webMCP.exposeTools() 调用",
        codeTemplate = """
// JavaScript: WebMCP 工具声明示例
window.webMCP = window.webMCP || {};
window.webMCP.exposeTools({
  tools: [{
    name: 'queryProducts',
    description: 'Query products by category',
    parameters: {
      type: 'object',
      properties: {
        category: { type: 'string', description: 'Product category' },
        limit: { type: 'number', description: 'Max results' }
      },
      required: ['category']
    }
  }]
});
        """.trimIndent()
    ),
    MigrationStep(
        stepNumber = 3,
        title = "配置 Origin Trial Token（可选）",
        description = "如需在生产环境使用 WebMCP，需申请 Chrome Origin Trial Token",
        codeTemplate = """
<!-- HTML: 添加 Origin Trial Token -->
<head>
  <meta http-equiv="origin-trial"
        content="YOUR_TOKEN_HERE">
</head>
        """.trimIndent()
    ),
    MigrationStep(
        stepNumber = 4,
        title = "Android 端通信接口",
        description = "通过 addJavascriptInterface 或 evaluateJavascript 实现 Android ↔ WebMCP 通信",
        codeTemplate = """
// Kotlin: addJavascriptInterface 示例
webView.addJavascriptInterface(
    WebMcpInterface(),
    "AndroidBridge"
)

// Kotlin: evaluateJavascript 示例
webView.evaluateJavascript(
    "window.webMCP.getTools()",
    { value -> /* handle tools list */ }
)
        """.trimIndent()
    ),
    MigrationStep(
        stepNumber = 5,
        title = "验证 WebMCP 工具可用",
        description = "在 Chrome DevTools 中验证工具声明是否正确",
        codeTemplate = """
// Chrome DevTools Console 验证命令
window.webMCP.getTools()
// 预期输出: [{ name: 'queryProducts', ... }]
        """.trimIndent()
    )
)

// =============================================================
// Default WebMCP Tool Templates / 默认 WebMCP 工具模板
// =============================================================
/**
 * Default WebMCP tool templates / 默认 WebMCP 工具模板
 */
private val DEFAULT_WEB_MCP_TOOLS = listOf(
    WebMcpTool(
        id = "product_query",
        name = "商品查询",
        description = "按分类查询商品列表",
        parameters = listOf(
            ToolParameter("category", "string", "商品分类", true),
            ToolParameter("limit", "number", "最大结果数", false)
        ),
        codeTemplate = """
window.webMCP.exposeTools({
  tools: [{
    name: 'queryProducts',
    description: 'Query products by category',
    parameters: {
      type: 'object',
      properties: {
        category: { type: 'string', description: 'Product category' },
        limit: { type: 'number', description: 'Max results', default: 10 }
      },
      required: ['category']
    }
  }]
});
        """.trimIndent(),
        category = "电商"
    ),
    WebMcpTool(
        id = "add_to_cart",
        name = "加入购物车",
        description = "将商品加入购物车",
        parameters = listOf(
            ToolParameter("productId", "string", "商品 ID", true),
            ToolParameter("quantity", "number", "数量", true)
        ),
        codeTemplate = """
window.webMCP.exposeTools({
  tools: [{
    name: 'addToCart',
    description: 'Add product to shopping cart',
    parameters: {
      type: 'object',
      properties: {
        productId: { type: 'string', description: 'Product ID' },
        quantity: { type: 'number', description: 'Quantity', default: 1 }
      },
      required: ['productId']
    }
  }]
});
        """.trimIndent(),
        category = "电商"
    ),
    WebMcpTool(
        id = "form_submit",
        name = "表单提交",
        description = "提交表单数据",
        parameters = listOf(
            ToolParameter("formId", "string", "表单 ID", true),
            ToolParameter("data", "object", "表单数据", true)
        ),
        codeTemplate = """
window.webMCP.exposeTools({
  tools: [{
    name: 'submitForm',
    description: 'Submit form data',
    parameters: {
      type: 'object',
      properties: {
        formId: { type: 'string', description: 'Form ID' },
        data: { type: 'object', description: 'Form data as key-value pairs' }
      },
      required: ['formId', 'data']
    }
  }]
});
        """.trimIndent(),
        category = "通用"
    )
)

// =============================================================
// Default Decision Tree Data / 默认决策树数据
// =============================================================
private val DECISION_TREE_NODES = mapOf(
    "root" to DecisionTreeNode(
        question = "你的 App 需要 AI Agent 自动化交互吗？",
        options = listOf(
            DecisionOption("是，Agent 需要自动操作 App 内容", nextNodeId = "q_agent_type"),
            DecisionOption("否，主要用户手动操作", nextNodeId = "q_deep_link")
        )
    ),
    "q_agent_type" to DecisionTreeNode(
        question = "Agent 交互的复杂度如何？",
        options = listOf(
            DecisionOption("简单（单一操作，如查询、提交）", nextNodeId = "q_agents"),
            DecisionOption("复杂（多步骤流程，如结账、预订）", nextNodeId = "q_security")
        )
    ),
    "q_agents" to DecisionTreeNode(
        question = "你的目标 Agent 是？",
        options = listOf(
            DecisionOption("Claude Code / Codex / Antigravity 等主流 Agent", resultIfChosen = DecisionResult(
                verdict = "强烈推荐使用 WebMCP",
                recommendation = "立即集成 WebMCP",
                secondaryRecommendation = "同时保留 Deep Link 作为用户入口",
                reason = "主流 Agent 已支持 WebMCP，WebMCP 可让 Agent 以结构化方式操作你的 WebView，无需屏幕抓取",
                useWebMcp = true
            )),
            DecisionOption("企业内部定制 Agent", nextNodeId = "q_security")
        )
    ),
    "q_security" to DecisionTreeNode(
        question = "你的 App 对安全性有何要求？",
        options = listOf(
            DecisionOption("高安全要求（金融、医疗等敏感数据）", resultIfChosen = DecisionResult(
                verdict = "WebMCP + 严格 Origin 限制",
                recommendation = "使用 WebMCP 并配置严格的 Origin 白名单",
                secondaryRecommendation = "同时使用 Deep Link 作为备用用户入口",
                reason = "WebMCP 的 Origin 限制机制可以确保只有授权的 Agent 才能调用工具",
                useWebMcp = true
            )),
            DecisionOption("一般安全要求", nextNodeId = "q_standalone")
        )
    ),
    "q_standalone" to DecisionTreeNode(
        question = "你的 App 是否需要独立于浏览器运行？",
        options = listOf(
            DecisionOption("是，App 独立运行，不依赖浏览器", resultIfChosen = DecisionResult(
                verdict = "WebMCP 适用",
                recommendation = "WebMCP 适用于独立 WebView App",
                secondaryRecommendation = "Deep Link 作为辅助方案",
                reason = "Android WebView 基于 Chromium，WebMCP 在 WebView 中可用",
                useWebMcp = true
            )),
            DecisionOption("否，可以在浏览器中运行", nextNodeId = "q_both")
        )
    ),
    "q_both" to DecisionTreeNode(
        question = "是否需要同时支持移动端和 Web 端？",
        options = listOf(
            DecisionOption("是，需要跨平台统一体验", resultIfChosen = DecisionResult(
                verdict = "WebMCP + Deep Link 双轨并行",
                recommendation = "WebMCP 用于 Agent 自动化，Deep Link 用于用户直接打开",
                secondaryRecommendation = "两者互补，缺一不可",
                reason = "WebMCP 处理 Agent 场景，Deep Link 处理用户手动触发场景",
                useWebMcp = true
            )),
            DecisionOption("否，仅移动端", resultIfChosen = DecisionResult(
                verdict = "推荐使用 WebMCP",
                recommendation = "优先使用 WebMCP",
                secondaryRecommendation = "Deep Link 作为用户入口补充",
                reason = "移动端 WebView App 适合使用 WebMCP 实现 Agent 交互",
                useWebMcp = true
            ))
        )
    ),
    "q_deep_link" to DecisionTreeNode(
        question = "你的 App 是否需要从外部链接唤起？",
        options = listOf(
            DecisionOption("是，需要从链接直接打开特定页面", resultIfChosen = DecisionResult(
                verdict = "使用 Deep Link",
                recommendation = "使用 Deep Link / App Link",
                secondaryRecommendation = "未来可考虑添加 WebMCP 支持 Agent 场景",
                reason = "Deep Link 是用户从外部链接唤起 App 的标准方式",
                useWebMcp = false
            )),
            DecisionOption("否，仅 App 内部导航", resultIfChosen = DecisionResult(
                verdict = "暂不需要 Deep Link 或 WebMCP",
                recommendation = "保持现有导航架构",
                secondaryRecommendation = null,
                reason = "如果既不需要外部唤起也不需要 Agent 交互，则 WebMCP 和 Deep Link 都不必需",
                useWebMcp = false
            ))
        )
    )
)

// =============================================================
// WebMcpIntent — 用户意图
// =============================================================
/**
 * WebMCP Toolkit User Intents / WebMCP 工具包用户意图
 *
 * Every user action corresponds to an Intent.
 */
sealed interface WebMcpIntent {

    /** Navigate to a specific page / 导航到指定页面
     * @param page Target page / 目标页面
     */
    data class NavigateTo(val page: ToolkitPage) : WebMcpIntent

    /** Navigate back to dashboard / 导航回主页 */
    data object NavigateToDashboard : WebMcpIntent

    /** Start compliance detection / 开始合规检测
     * @param url URL to scan / 要检测的 URL
     */
    data class StartDetection(val url: String) : WebMcpIntent

    /** Clear detection result / 清除检测结果 */
    data object ClearDetection : WebMcpIntent

    /** Go to next migration step / 进入下一步迁移
     * @param step Step number / 步骤编号
     */
    data class GoToMigrationStep(val step: Int) : WebMcpIntent

    /** Mark migration step as completed / 标记迁移步骤完成
     * @param step Step number / 步骤编号
     */
    data class CompleteMigrationStep(val step: Int) : WebMcpIntent

    /** Answer decision tree question / 回答决策树问题
     * @param questionIndex Question index / 问题索引
     * @param answer Answer / 答案
     */
    data class AnswerDecisionTree(val questionIndex: Int, val answer: Boolean) : WebMcpIntent

    /** Reset decision tree / 重置决策树 */
    data object ResetDecisionTree : WebMcpIntent

    /** Select a WebMCP tool for detail view / 选中工具查看详情
     * @param tool Tool to select / 要选中的工具
     */
    data class SelectTool(val tool: WebMcpTool) : WebMcpIntent

    /** Clear selected tool / 清除选中的工具 */
    data object ClearSelectedTool : WebMcpIntent

    /** Copy code template to clipboard / 复制代码模板
     * @param code Code to copy / 要复制的代码
     */
    data class CopyCode(val code: String) : WebMcpIntent

    /** Open external URL / 打开外部链接
     * @param url URL to open / 要打开的链接
     */
    data class OpenExternalUrl(val url: String) : WebMcpIntent

    /** Dismiss error / 关闭错误信息 */
    data object DismissError : WebMcpIntent
}

// =============================================================
// WebMcpEffect — 副作用
// =============================================================
/**
 * WebMCP Toolkit Side Effects / WebMCP 工具包副作用
 *
 * One-time events consumed only once by UI layer.
 */
sealed interface WebMcpEffect {

    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error / 是否为错误
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : WebMcpEffect

    /** Copy text to clipboard / 复制文本到剪贴板
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : WebMcpEffect

    /** Open external URL / 打开外部链接
     * @param url URL to open / 要打开的链接
     */
    data class OpenExternalUrl(val url: String) : WebMcpEffect
}
