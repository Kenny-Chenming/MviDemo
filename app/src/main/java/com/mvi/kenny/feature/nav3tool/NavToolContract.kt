package com.mvi.kenny.feature.nav3tool

import androidx.compose.ui.graphics.Color

// =============================================================
// NavToolContract — Jetpack Navigation 3 迁移工具包 MVI 契约
// PRD-238 | Jetpack Navigation 3 迁移工具包
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class, single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, clipboard, etc.)
//
// @see NavToolViewModel State management
// @see NavToolScreen Main screen

// =============================================================
// ToolId — 工具编号枚举 (10 tools)
// =============================================================
/**
 * Navigation 3 migration tool ID / Navigation 3 迁移工具编号
 *
 * Each tool has a unique ID for routing and state management.
 *
 * @param title Tool display title / 工具显示标题
 * @param subtitle Short description / 简短描述
 * @param iconName Material icon name / Material 图标名称
 * @param isCore Whether this is a core (essential) tool / 是否为核心工具
 */
enum class ToolId(
    val title: String,
    val subtitle: String,
    val iconName: String,
    val isCore: Boolean
) {
    SCANNER("Nav2→Nav3 迁移扫描器", "扫描项目中 Nav2 API 使用，评估迁移复杂度", "radar", true),
    API_MAPPING("API 对照指南", "Nav2 → Nav3 API 完整对照表", "compare_arrows", true),
    BOTTOM_NAV("BottomNavigation 迁移 Diff", "多 Tab 应用的完整导航迁移代码 Diff", "table_chart", true),
    STATE_MANAGEMENT("Navigation 3 状态管理架构指南", "SnapshotStateList + entryProvider 完整用法", "account_tree", true),
    TYPE_SAFE_ARGS("NavArgs 类型安全方案", "sealed class routes + key-based routing 最佳实践", "lock", false),
    LIST_DETAIL("List-Detail 自适应布局模板", "Nav3 原生支持的多 Pane 布局完整代码", "view_column", false),
    DEEP_LINK("Deep Link 迁移指南", "Nav2 → Nav3 URI-based navigation 迁移", "link", false),
    CI_COMPLIANCE("CI 合规检测工具", "Gradle 插件，阻塞未迁移 CI", "verified", false),
    ANIMATION("动画转场配置指南", "SpatialTransitions + 转场动画配置", "animation", false),
    DECISION_TREE("Nav2 vs Nav3 决策树", "何时迁移 Nav3，何时继续用 Nav2", "help", false)
}

// =============================================================
// ScanStatus — 扫描状态枚举
// =============================================================
/**
 * Scan status / 扫描状态
 *
 * @param label Display label / 显示标签
 */
enum class ScanStatus(val label: String) {
    IDLE("Idle / 空闲"),
    SCANNING("Scanning / 扫描中"),
    COMPLETED("Completed / 完成"),
    ERROR("Error / 错误")
}

// =============================================================
// Severity — 问题严重程度
// =============================================================
/**
 * Migration issue severity / 迁移问题严重程度
 *
 * @param label Display label / 显示标签
 * @param priority Sort priority (lower = more urgent) / 排序优先级
 * @param color Badge color / 徽章颜色
 */
enum class Severity(val label: String, val priority: Int, val color: Color) {
    P0_BLOCKER("P0 — 阻塞级", 0, Color(0xFFB3261E)),    // Red — must migrate
    P1_HIGH("P1 — 高", 1, Color(0xFFF57C00)),           // Orange — strongly recommended
    P2_MEDIUM("P2 — 中", 2, Color(0xFFF9A825)),         // Yellow — recommended
    P3_LOW("P3 — 低", 3, Color(0xFF4CAF50))             // Green — optional
}

// =============================================================
// Nav2ApiUsage — 扫描发现的 Nav2 API 使用点
// =============================================================
/**
 * Nav2 API usage discovered by scanner / 扫描发现的 Nav2 API 使用点
 *
 * @param id Unique identifier / 唯一标识符
 * @param filePath Source file path / 源文件路径
 * @param lineNumber Line number in source / 源文件行号
 * @param apiName Nav2 API name / Nav2 API 名称
 * @param codeSnippet Surrounding code / 周围代码
 * @param severity Issue severity / 问题严重程度
 * @param migrationHint Migration suggestion / 迁移建议
 * @param estimatedMinutes Estimated migration time in minutes / 预估迁移时间（分钟）
 */
data class Nav2ApiUsage(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val apiName: String,
    val codeSnippet: String,
    val severity: Severity,
    val migrationHint: String,
    val estimatedMinutes: Int
)

// =============================================================
// ScanResult — 扫描结果汇总
// =============================================================
/**
 * Complete scan result summary / 完整扫描结果汇总
 *
 * @param totalFilesScanned Total files scanned / 扫描文件总数
 * @param totalIssuesFound Total Nav2 API usages found / 发现的 Nav2 API 使用总数
 * @param issuesBySeverity Breakdown by severity / 按严重程度分类
 * @param affectedFiles List of affected source files / 受影响的源文件列表
 * @param estimatedTotalMinutes Total estimated migration time / 总预估迁移时间
 * @param nav2Version Current navigation-compose version / 当前 navigation-compose 版本
 * @param recommendation Overall migration recommendation / 总体迁移建议
 */
data class ScanResult(
    val totalFilesScanned: Int,
    val totalIssuesFound: Int,
    val issuesBySeverity: Map<Severity, Int>,
    val affectedFiles: List<String>,
    val estimatedTotalMinutes: Int,
    val nav2Version: String?,
    val recommendation: String
)

// =============================================================
// ApiMappingItem — API 对照条目
// =============================================================
/**
 * Nav2 → Nav3 API mapping item / Nav2 → Nav3 API 对照条目
 *
 * @param category API category / API 分类
 * @param nav2Api Nav2 API signature / Nav2 API 签名
 * @param nav3Api Nav3 API signature / Nav3 API 签名
 * @param behaviorChange Description of behavior change / 行为变化描述
 * @param migrationStep Migration step / 迁移步骤
 */
data class ApiMappingItem(
    val category: String,
    val nav2Api: String,
    val nav3Api: String,
    val behaviorChange: String,
    val migrationStep: String
)

// =============================================================
// DiffEntry — 代码 Diff 条目
// =============================================================
/**
 * Code diff entry / 代码 Diff 条目
 *
 * @param lineNumber Line number / 行号
 * @param changeType ADD / REMOVE / CONTEXT / 修改类型
 * @param oldLine Old code line (null for ADD) / 旧代码行（ADD 时为 null）
 * @param newLine New code line (null for REMOVE) / 新代码行（REMOVE 时为 null）
 * @param explanation Line explanation / 行说明
 */
data class DiffEntry(
    val lineNumber: Int,
    val changeType: ChangeType,
    val oldLine: String?,
    val newLine: String?,
    val explanation: String
)

/** Diff change type / Diff 修改类型 */
enum class ChangeType { ADD, REMOVE, CONTEXT }

// =============================================================
// CodeTemplate — 代码模板
// =============================================================
/**
 * Navigation 3 code template / Navigation 3 代码模板
 *
 * @param id Template ID / 模板 ID
 * @param title Template title / 模板标题
 * @param description Template description / 模板描述
 * @param code Template code / 模板代码
 * @param language Code language / 代码语言
 * @param tags Search tags / 搜索标签
 */
data class CodeTemplate(
    val id: String,
    val title: String,
    val description: String,
    val code: String,
    val language: String = "kotlin",
    val tags: List<String>
)

// =============================================================
// DecisionTreeNode — 决策树节点
// =============================================================
/**
 * Decision tree node / 决策树节点
 *
 * @param question Question text / 问题文本
 * @param options List of answer options / 答案选项列表
 */
data class DecisionTreeNode(
    val question: String,
    val options: List<DecisionOption>
)

/**
 * Decision tree option / 决策树选项
 *
 * @param answer Answer text / 答案文本
 * @param nextNodeId Next node ID (null if this is a leaf) / 下一节点 ID（叶子节点为 null）
 * @param resultIfChosen Final result if this option is chosen / 选择此项时的最终结果
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
 * @param reason Detailed reason / 详细理由
 * @param riskLevel Risk level: HIGH / MEDIUM / LOW / 风险等级
 * @param nextSteps Suggested next steps / 建议的后续步骤
 */
data class DecisionResult(
    val verdict: String,
    val reason: String,
    val riskLevel: RiskLevel,
    val nextSteps: List<String>
)

/** Risk level / 风险等级 */
enum class RiskLevel(val label: String, val color: Color) {
    HIGH("高风险", Color(0xFFB3261E)),
    MEDIUM("中等风险", Color(0xFFF9A825)),
    LOW("低风险", Color(0xFF4CAF50))
}

// =============================================================
// NavToolState — 页面状态
// =============================================================
/**
 * Navigation 3 Migration Tool State / Navigation 3 迁移工具页面状态
 *
 * Single source of truth for the entire Nav3 Migration Tool UI.
 * All UI state is derived from this data class.
 *
 * @param activeTool Selected tool ID / 当前选中的工具 ID
 * @param isHome Whether to show home (tool grid) / 是否显示首页（工具网格）
 * @param scanStatus Current scan status / 当前扫描状态
 * @param scanProgress Scan progress 0.0~1.0 / 扫描进度
 * @param scanResult Scan result, null if not scanned yet / 扫描结果
 * @param selectedIssue Selected issue for detail view / 选中的问题详情
 * @param apiMappingFilter Currently selected API category filter / 当前 API 分类过滤
 * @param apiMappingItems All API mapping items / 所有 API 对照条目
 * @param bottomNavDiffEntries Current BottomNav diff entries / 当前 BottomNav Diff 条目
 * @param selectedTemplate Selected code template / 选中的代码模板
 * @param decisionTreeCurrentNodeId Current decision tree node ID / 当前决策树节点 ID
 * @param decisionTreeAnswers User's answers so far / 用户已选择的答案
 * @param isLoading Whether any background operation is in progress / 是否有后台操作进行中
 * @param error Error message if any / 错误信息
 */
data class NavToolState(
    val activeTool: ToolId = ToolId.SCANNER,
    val isHome: Boolean = true,
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanProgress: Float = 0f,
    val scanResult: ScanResult? = null,
    val selectedIssue: Nav2ApiUsage? = null,
    val apiMappingFilter: String = "全部",
    val apiMappingItems: List<ApiMappingItem> = emptyList(),
    val bottomNavDiffEntries: List<DiffEntry> = emptyList(),
    val selectedTemplate: CodeTemplate? = null,
    val decisionTreeCurrentNodeId: String = "root",
    val decisionTreeAnswers: List<String> = emptyList(),
    val decisionTreeResult: DecisionResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = NavToolState(apiMappingItems = getDefaultApiMappings())
    }

    /**
     * Filtered API mapping items by category / 按分类过滤的 API 对照条目
     */
    val filteredApiMappings: List<ApiMappingItem>
        get() = if (apiMappingFilter == "全部") apiMappingItems
                else apiMappingItems.filter { it.category == apiMappingFilter }

    /**
     * All available API categories / 所有可用 API 分类
     */
    val apiCategories: List<String>
        get() = listOf("全部") + apiMappingItems.map { it.category }.distinct()

    /**
     * Decision tree current node / 决策树当前节点
     */
    val decisionTreeCurrentNode: DecisionTreeNode?
        get() = DECISION_TREE_NODES[decisionTreeCurrentNodeId]

    /**
     * P0 count from scan result / 扫描结果中 P0 数量
     */
    val p0Count: Int
        get() = scanResult?.issuesBySeverity?.get(Severity.P0_BLOCKER) ?: 0

    /**
     * P1 count from scan result / 扫描结果中 P1 数量
     */
    val p1Count: Int
        get() = scanResult?.issuesBySeverity?.get(Severity.P1_HIGH) ?: 0

    /**
     * Scan percentage / 扫描完成百分比
     */
    val scanPercentage: Int
        get() = (scanProgress * 100).toInt()
}

// =============================================================
// Default API Mappings Data / 默认 API 对照数据
// =============================================================
/**
 * Default Nav2 → Nav3 API mappings / 默认 Nav2 → Nav3 API 对照数据
 */
private fun getDefaultApiMappings(): List<ApiMappingItem> = listOf(
    // Core Navigation API
    ApiMappingItem(
        category = "核心导航",
        nav2Api = "navController.navigate(route)",
        nav3Api = "navigator.navigateTo(routeKey)",
        behaviorChange = "Nav2: 直接路由字符串；Nav3: 使用 Nav3RouteKey 替代字符串路由",
        migrationStep = "将所有 navigate(route) 调用改为 navigateTo(RouteKey)"
    ),
    ApiMappingItem(
        category = "核心导航",
        nav2Api = "NavHost(navController = navController)",
        nav3Api = "NavDisplay(navigationState = navState)",
        behaviorChange = "NavHost + NavController 组合被 NavDisplay + NavigationState 替代",
        migrationStep = "重构 NavHost 为 NavDisplay，传入 NavigationState"
    ),
    ApiMappingItem(
        category = "核心导航",
        nav2Api = "navController.popBackStack()",
        nav3Api = "navigator.popBackStack()",
        behaviorChange = "API 名称一致，但内部实现完全不同",
        migrationStep = "调用方代码不变，navigator 实例需重新获取"
    ),
    ApiMappingItem(
        category = "Back Stack",
        nav2Api = "NavBackStackEntry",
        nav3Api = "String (route key)",
        behaviorChange = "Nav2: NavBackStackEntry 是包含路由和参数的复杂对象；Nav3: 退化为字符串 route key",
        migrationStep = "移除所有 NavBackStackEntry 引用，用 route key 字符串替代"
    ),
    ApiMappingItem(
        category = "Back Stack",
        nav2Api = "navController.currentBackStackEntryAsState()",
        nav3Api = "navigationState.currentEntryProvider",
        behaviorChange = "Flow<NavBackStackEntry> → StateFlow<Nav3Entry>，数据结构完全不同",
        migrationStep = "改用 navigationState.currentEntryProvider 获取当前 entry"
    ),
    // Bottom Navigation
    ApiMappingItem(
        category = "底部导航",
        nav2Api = "BottomNavigation + NavController",
        nav3Api = "NavigationBar + SnapshotStateList<Nav3RouteKey>",
        behaviorChange = "Nav2 多 back stack 需要手动管理；Nav3 原生支持多 tab stacks",
        migrationStep = "将 BottomNavigation 的 eachTabNavController 改为统一的 navigator"
    ),
    ApiMappingItem(
        category = "底部导航",
        nav2Api = "navController.navigate(TabRoute) { launchSingleTop = true }",
        nav3Api = "navigator.navigateTo(tabRouteKey) { launchSingleTop = true }",
        behaviorChange = "API 签名相同，但底层的 stack 管理逻辑完全不同",
        migrationStep = "navigator 实例替换，调用签名保持一致"
    ),
    // State Management
    ApiMappingItem(
        category = "状态管理",
        nav2Api = "navController.getBackStackEntry(route).savedStateHandle",
        nav3Api = "navigationState.snapshotEntry(routeKey).savedStateHandle",
        behaviorChange = "从 NavBackStackEntry 获取 → 从 NavigationState snapshot 获取",
        migrationStep = "通过 navigationState.snapshotEntry(key) 访问 savedStateHandle"
    ),
    ApiMappingItem(
        category = "状态管理",
        nav2Api = "NavGraph includes = listOf(...)",
        nav3Api = "NavDisplay content = { entryProvider { ... } }",
        behaviorChange = "Nav2: 声明式图构建；Nav3: DSL 形式注册路由",
        migrationStep = "将静态 NavGraph includes 改写为 entryProvider DSL"
    ),
    // Deep Link
    ApiMappingItem(
        category = "Deep Link",
        nav2Api = "navArgument { uriPatterns = listOf(\"https://example.com/{id}\") }",
        nav3Api = "navArgument(key = \"id\", uris = listOf(Uri(\"https://example.com/{id}\")))",
        behaviorChange = "uriPatterns → uris，API 名称变化，结构略有调整",
        migrationStep = "将 navArgument 的 uriPatterns 参数改为 uris 参数"
    ),
    ApiMappingItem(
        category = "Deep Link",
        nav2Api = "navController.handleDeepLink(intent)",
        nav3Api = "navigator.handleDeepLink(intent)",
        behaviorChange = "API 一致，内部处理逻辑完全重写",
        migrationStep = "navigator 实例替换"
    ),
    // NavArgs / Type Safety
    ApiMappingItem(
        category = "NavArgs",
        nav2Api = "@NavHostDsl @Serializable data class Args(val id: Int)",
        nav3Api = "sealed class RouteKey { data object Home : RouteKey(\"home\") }",
        behaviorChange = "Nav2: KSP 生成 Args 类；Nav3: sealed class + key string，无代码生成",
        migrationStep = "将所有 @Serializable NavArgs 替换为 sealed class RouteKey"
    ),
    ApiMappingItem(
        category = "NavArgs",
        nav2Api = "navController.navigate(Home(id = 123))",
        nav3Api = "navigator.navigateTo(HomeKey(id = 123))",
        behaviorChange = "类型安全的导航 → key-based 导航，API 相似但语义不同",
        migrationStep = "用 data class RouteKey 替代 case class Args"
    ),
    // Animation / Transitions
    ApiMappingItem(
        category = "动画转场",
        nav2Api = "composeOptions { enterTransition = fadeIn() }",
        nav3Api = "SpatialNavigatingTransition + enterSpatial()",
        behaviorChange = "Nav2: 简单 fade/slide；Nav3: SpatialTransitions API，完全不同的动画系统",
        migrationStep = "使用 Nav3 的 SpatialNavigatingTransition API 重写所有转场动画"
    ),
    ApiMappingItem(
        category = "动画转场",
        nav2Api = "popEnterTransition / popExitTransition",
        nav3Api = "popEnterTransition / popExitTransition (semantics preserved)",
        behaviorChange = "API 签名基本不变，但底层实现基于 SpatialTransitions",
        migrationStep = "保留 API 调用，更新依赖的动画库版本"
    ),
    // KSP / Code Generation
    ApiMappingItem(
        category = "KSP 代码生成",
        nav2Api = "Safe Args: navValues generateNavHost()",
        nav3Api = "无需生成 — 全部运行时 key string",
        behaviorChange = "Nav2 依赖 KSP 生成 NavArgs；Nav3 完全无需代码生成",
        migrationStep = "移除 Safe Args Gradle Plugin 和所有生成的 Args 类"
    ),
    // List-Detail Layout
    ApiMappingItem(
        category = "自适应布局",
        nav2Api = "SlidingPaneLayout + NavController",
        nav3Api = "NavDisplay + pane-adaptive layout (原生支持)",
        behaviorChange = "Nav2 需要第三方 SlidingPaneLayout；Nav3 原生支持 list-detail",
        migrationStep = "使用 Nav3 的自适应布局 API 替代 SlidingPaneLayout"
    )
)

// =============================================================
// Default Decision Tree Data / 默认决策树数据
// =============================================================
private val DECISION_TREE_NODES = mapOf(
    "root" to DecisionTreeNode(
        question = "你的项目当前使用的 Navigation Compose 版本是多少？",
        options = listOf(
            DecisionOption("navigation-compose < 2.4.0", nextNodeId = "q_old"),
            DecisionOption("navigation-compose 2.4.0 ~ 2.8.x", nextNodeId = "q_scope"),
            DecisionOption("navigation-compose >= 2.9.0 (已接近 Nav3)", nextNodeId = "q_migrate_now")
        )
    ),
    "q_old" to DecisionTreeNode(
        question = "你的项目规模是？",
        options = listOf(
            DecisionOption("小型（< 10 个 NavHost / 路由 < 30）", nextNodeId = "q_team"),
            DecisionOption("中型（10~50 个 NavHost / 路由 30~100）", nextNodeId = "q_team"),
            DecisionOption("大型（> 50 个 NavHost / 路由 > 100）", resultIfChosen = DecisionResult(
                verdict = "建议暂缓，等待生态成熟",
                reason = "大型项目迁移复杂度高，Nav3 工具链（KSP 替代方案、生态库）尚未完全成熟，建议等待 3-6 个月",
                riskLevel = RiskLevel.HIGH,
                nextSteps = listOf("监控 Navigation 3 官方更新", "关注社区工具发布", "建立 Nav3 技术储备")
            ))
        )
    ),
    "q_scope" to DecisionTreeNode(
        question = "Navigation 在你的项目中有多复杂？",
        options = listOf(
            DecisionOption("简单导航（1~2 个 NavHost，线性 back stack）", resultIfChosen = DecisionResult(
                verdict = "建议迁移 — 收益大于成本",
                reason = "简单项目迁移成本低，可快速获得 Nav3 的 type-safety 改进和性能提升",
                riskLevel = RiskLevel.LOW,
                nextSteps = listOf("使用 Nav2→Nav3 迁移扫描器评估", "制定 1-2 周迁移计划", "优先迁移 BottomNavigation 部分")
            )),
            DecisionOption("中等复杂度（有 BottomNavigation，多 tab）", nextNodeId = "q_migrate_now"),
            DecisionOption("复杂（多 NavHost、嵌套导航、Custom Navigator）", resultIfChosen = DecisionResult(
                verdict = "建议分阶段迁移，优先迁移新功能",
                reason = "复杂项目不适合全量迁移，建议新功能用 Nav3，老功能逐步迁移",
                riskLevel = RiskLevel.MEDIUM,
                nextSteps = listOf("新功能页面使用 Nav3", "建立 Nav2/Nav3 双轨制", "制定 6 个月迁移路线图")
            ))
        )
    ),
    "q_migrate_now" to DecisionTreeNode(
        question = "你的团队对 Kotlin 和 Compose 熟悉程度？",
        options = listOf(
            DecisionOption("团队熟悉 Kotlin/Compose，有架构设计经验", resultIfChosen = DecisionResult(
                verdict = "强烈建议立即迁移 Nav3",
                reason = "Nav3 是 Compose-First 设计，熟悉的团队可以快速上手并受益于新架构",
                riskLevel = RiskLevel.LOW,
                nextSteps = listOf("使用迁移扫描器评估范围", "从非核心页面开始迁移", "建立 Nav3 代码审查规范")
            )),
            DecisionOption("团队有 Kotlin 基础，Compose 经验有限", resultIfChosen = DecisionResult(
                verdict = "建议等待 + 培训优先",
                reason = "Nav3 的新范式（NavigationState + entryProvider）对 Compose 理解要求高，建议先提升团队能力",
                riskLevel = RiskLevel.MEDIUM,
                nextSteps = listOf("组织 Nav3 技术培训", "搭建 Nav3 Demo 环境", "3 个月后再评估迁移")
            ))
        )
    ),
    "q_team" to DecisionTreeNode(
        question = "你的项目发布节奏是？",
        options = listOf(
            DecisionOption("快速迭代（< 4 周一个版本）", resultIfChosen = DecisionResult(
                verdict = "建议暂缓，等下一个版本周期",
                reason = "Nav3 迁移需要一定时间验证，快速迭代团队应避免在迁移期间引入风险",
                riskLevel = RiskLevel.MEDIUM,
                nextSteps = listOf("在下个季度规划中预留 2-4 周迁移时间", "关注 Nav3 1.0 稳定版发布")
            )),
            DecisionOption("正常节奏（4~12 周一个版本）", resultIfChosen = DecisionResult(
                verdict = "可以在下一个版本中规划 Nav3 迁移",
                reason = "有足够时间做 Nav3 迁移验证，不影响正常发布节奏",
                riskLevel = RiskLevel.LOW,
                nextSteps = listOf("下一个版本规划迁移冲刺", "使用本工具包扫描和制定计划", "预留 20% buffer 时间应对意外")
            ))
        )
    )
)

// =============================================================
// NavToolIntent — 用户意图
// =============================================================
/**
 * Navigation 3 Migration Tool User Intents / Navigation 3 迁移工具用户意图
 *
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface NavToolIntent {

    /** Navigate to tool detail / 导航到工具详情
     * @param tool Tool to navigate to / 要导航到的工具
     */
    data class SelectTool(val tool: ToolId) : NavToolIntent

    /** Navigate back to home / 导航回首页 */
    data object NavigateHome : NavToolIntent

    /** Start Nav2 API scan / 开始 Nav2 API 扫描
     * @param projectPath Project path to scan / 要扫描的项目路径
     */
    data class StartScan(val projectPath: String) : NavToolIntent

    /** Cancel ongoing scan / 取消正在进行的扫描 */
    data object CancelScan : NavToolIntent

    /** Select an issue for detail view / 选中问题查看详情
     * @param issue Issue to select / 要选中的问题
     */
    data class SelectIssue(val issue: Nav2ApiUsage) : NavToolIntent

    /** Clear selected issue / 清除选中的问题 */
    data object ClearIssue : NavToolIntent

    /** Filter API mappings by category / 按分类过滤 API 对照表
     * @param category Category to filter by / 要过滤的分类
     */
    data class FilterApiMappings(val category: String) : NavToolIntent

    /** Copy code template to clipboard / 复制代码模板到剪贴板
     * @param template Template to copy / 要复制的模板
     */
    data class CopyTemplate(val template: CodeTemplate) : NavToolIntent

    /** Answer decision tree question / 回答决策树问题
     * @param answer Selected answer / 选中的答案
     * @param nextNodeId Next node ID / 下一节点 ID
     */
    data class AnswerDecisionTree(val answer: String, val nextNodeId: String) : NavToolIntent

    /** Reset decision tree to beginning / 重置决策树到起始节点 */
    data object ResetDecisionTree : NavToolIntent

    /** Export scan report / 导出扫描报告
     * @param format Export format (json/markdown) / 导出格式
     */
    data class ExportScanReport(val format: String) : NavToolIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : NavToolIntent
}

// =============================================================
// NavToolEffect — 副作用
// =============================================================
/**
 * Navigation 3 Migration Tool Side Effects / Navigation 3 迁移工具副作用
 *
 * One-time events, consumed only once by UI layer.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface NavToolEffect {

    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error message / 是否为错误消息
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : NavToolEffect

    /** Copy text to clipboard / 复制文本到剪贴板
     * @param text Text to copy / 要复制的文本
     * @param label Label for clipboard / 剪贴板标签
     */
    data class CopyToClipboard(val text: String, val label: String) : NavToolEffect

    /** Export scan report / 导出扫描报告
     * @param reportPath Path where report was saved / 报告保存路径
     */
    data class ExportReport(val reportPath: String) : NavToolEffect

    /** Open file at specific line (IDE integration) / 在 IDE 中打开文件
     * @param filePath File path / 文件路径
     * @param lineNumber Line number / 行号
     */
    data class OpenFile(val filePath: String, val lineNumber: Int) : NavToolEffect
}
