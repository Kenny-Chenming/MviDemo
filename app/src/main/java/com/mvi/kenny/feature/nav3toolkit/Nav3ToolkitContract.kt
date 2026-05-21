package com.mvi.kenny.feature.nav3toolkit

// ================================================================
// Nav3ToolkitContract — Jetpack Navigation 3 响应式导航集成工具包 MVI 契约
// ================================================================
// MVI architecture contract for Jetpack Navigation 3 Responsive Navigation Toolkit.
//
// PRD-258: Jetpack Navigation 3 响应式导航集成工具包
// Design Reference: memory/agency/designs/PRD-258-Jetpack-Navigation-3-响应式导航集成工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
//
// Visual Spec:
//   Primary color: Teal #4DB6AC (matches Navigation Component brand)
//   v2.x code label: Gray #757575
//   v3 code label: Teal #4DB6AC
//   Tab active: #4DB6AC | inactive: #757575
//   Card background: SurfaceVariant, cornerRadius 12dp, spacing 12dp
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// 颜色常量 / Color Constants
// ================================================================

/** Teal 主题色 — Navigation Component 品牌色 */
val Nav3Teal = Color(0xFF4DB6AC)

/** 灰色标签色 — v2.x 代码标签 */
val Nav3Gray = Color(0xFF757575)

/** 浅色代码背景 */
val CodeBlockLightBg = Color(0xFFF5F5F5)

/** 深色代码背景 */
val CodeBlockDarkBg = Color(0xFF1E1E1E)

// ================================================================
// Tab 定义 / Tab Definitions
// ================================================================

/**
 * ============================================================
 * Nav3Tab — 工具包 Tab 枚举
 * ============================================================
 *
 * @param index Tab 索引
 * @param title 中文标题
 * @param description 描述
 */
enum class Nav3Tab(val index: Int, val title: String, val description: String) {
    GETTING_STARTED(0, "入门与迁移", "Navigation 3 vs 2.x 完整对比 + 迁移步骤"),
    ADAPTIVE_NAV(1, "自适应导航", "List-Detail / SlidingPaneLayout vs AdaptivePane 选择"),
    FOLDABLE_INTEGRATION(2, "折叠屏集成", "折叠/展开状态同步 + Hinge 位置处理"),
    CROSS_DEVICE_STATE(3, "跨设备状态", "Cross-Device State Preservation + Deep Link"),
    CI_VALIDATION(4, "CI验证工具", "NavGraph XML 合法性检测 + Size Class 配置验证")
}

/**
 * ============================================================
 * DecisionNode — 决策树节点
 * ============================================================
 * 决策树的一个节点（问题或结果）
 *
 * @param id 节点 ID
 * @param type 节点类型：question / result
 * @param content 内容
 * @param options 选项（仅 question 类型有）
 * @param selectedOption 选中的选项 ID
 * @param resultCard 结果卡片信息（仅 result 类型有）
 */
data class DecisionNode(
    val id: String,
    val type: String, // "question" or "result"
    val content: String,
    val options: List<DecisionOption> = emptyList(),
    val selectedOption: String? = null,
    val resultCard: ResultCard? = null
)

/**
 * ============================================================
 * DecisionOption — 决策选项
 * ============================================================
 *
 * @param id 选项 ID
 * @param label 选项标签
 * @param nextNodeId 下一节点 ID
 */
data class DecisionOption(
    val id: String,
    val label: String,
    val nextNodeId: String
)

/**
 * ============================================================
 * ResultCard — 决策结果卡片
 * ============================================================
 *
 * @param title 结果标题
 * @param recommendation 推荐方案
 * @param codeExample 代码示例
 * @param notes 备注
 */
data class ResultCard(
    val title: String,
    val recommendation: String,
    val codeExample: String,
    val notes: String = ""
)

// ================================================================
// State / Intent / Effect
// ================================================================

/**
 * ============================================================
 * Nav3ToolkitState — Navigation 3 工具包页面状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 *
 * @param selectedTab 当前 Tab 索引 (0-4)
 * @param expandedChapterId 展开的章节 ID（用于锚点跳转）
 * @param selectedDecisionPath 已选择的决策路径 Map<questionId, answerId>
 * @param searchQuery 搜索查询字符串
 * @param isLegacy 切换 v2.x / v3 代码视角
 * @param navGraphXml NavGraph XML 示例代码
 * @param isLoading 是否正在加载
 * @param chapters 当前 Tab 内的章节列表
 * @param decisionTreeNodes 决策树节点列表
 * @param activeDecisionNode 当前激活的决策节点
 * @param snackbarMessage Snackbar 消息
 *
 * @see Nav3Tab
 */
data class Nav3ToolkitState(
    // ── Navigation / 全局状态 ────────────────────────────────────
    val selectedTab: Int = 0,
    val expandedChapterId: String? = null,
    val selectedDecisionPath: Map<String, String> = emptyMap(),
    val searchQuery: String = "",
    val isLegacy: Boolean = false, // 切换 v2.x / v3 代码视角
    val navGraphXml: String = "",
    val isLoading: Boolean = false,

    // ── Content ──────────────────────────────────────────────────
    val chapters: List<NavChapter> = emptyList(),
    val decisionTreeNodes: List<DecisionNode> = emptyList(),
    val activeDecisionNode: DecisionNode? = null,

    // ── UI State ────────────────────────────────────────────────
    val snackbarMessage: String? = null,
    val copySuccess: Boolean = false
) {
    companion object {
        /** Initial/empty state */
        val Initial = Nav3ToolkitState()
    }
}

/**
 * ============================================================
 * NavChapter — 章节数据模型
 * ============================================================
 *
 * @param id 章节 ID（锚点）
 * @param title 章节标题
 * @param content 章节内容（富文本描述）
 * @param codeBlocks 代码示例列表
 * @param isExpanded 是否展开
 * @param tabIndex 所属 Tab 索引
 */
data class NavChapter(
    val id: String,
    val title: String,
    val content: String,
    val codeBlocks: List<CodeBlock> = emptyList(),
    val isExpanded: Boolean = false,
    val tabIndex: Int = 0
)

/**
 * ============================================================
 * CodeBlock — 代码块数据模型
 * ============================================================
 *
 * @param language 语言（kotlin / xml）
 * @param label 标签（如 "v2.x" 或 "v3"）
 * @param labelColor 标签颜色（对应版本色）
 * @param code 代码内容
 * @param filename 文件名
 */
data class CodeBlock(
    val language: String = "kotlin",
    val label: String,
    val labelColor: Color = Nav3Teal,
    val code: String,
    val filename: String = ""
)

// ================================================================
// Intent
// ================================================================

/**
 * ============================================================
 * Nav3ToolkitIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see Nav3ToolkitViewModel.sendIntent handles all Intents
 */
sealed interface Nav3ToolkitIntent {

    /** 用户切换主 Tab
     * @param index Tab 索引 (0-4)
     */
    data class SelectTab(val index: Int) : Nav3ToolkitIntent

    /** 用户跳转章节（锚点导航）
     * @param chapterId 章节 ID
     */
    data class JumpToChapter(val chapterId: String) : Nav3ToolkitIntent

    /** 用户选择决策路径
     * @param questionId 问题节点 ID
     * @param answerId 答案选项 ID
     */
    data class SelectDecisionPath(val questionId: String, val answerId: String) : Nav3ToolkitIntent

    /** 用户切换 Legacy 模式（v2.x / v3 代码视角）
     * @param isLegacy 是否为 Legacy 模式
     */
    data class ToggleLegacyMode(val isLegacy: Boolean) : Nav3ToolkitIntent

    /** 用户复制 NavGraph XML */
    data object CopyNavGraph : Nav3ToolkitIntent

    /** 用户搜索章节
     * @param query 搜索查询
     */
    data class Search(val query: String) : Nav3ToolkitIntent

    /** 用户展开/收起章节
     * @param chapterId 章节 ID
     */
    data class ToggleChapter(val chapterId: String) : Nav3ToolkitIntent

    /** 用户关闭 Snackbar */
    data object DismissSnackbar : Nav3ToolkitIntent
}

// ================================================================
// Effect
// ================================================================

/**
 * ============================================================
 * Nav3ToolkitEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see Nav3ToolkitViewModel _effect.send() sends Effects
 */
sealed interface Nav3ToolkitEffect {

    /** 显示复制成功 Toast
     * @param text 已复制的文本
     */
    data class ShowCopiedToast(val text: String) : Nav3ToolkitEffect

    /** 滚动到指定章节
     * @param chapterId 章节 ID
     */
    data class ScrollToChapter(val chapterId: String) : Nav3ToolkitEffect

    /** 显示 Snackbar 消息
     * @param message 消息文本
     */
    data class ShowSnackbar(val message: String) : Nav3ToolkitEffect
}

// ================================================================
// 模拟数据 / Simulation Data
// ================================================================

/**
 * ============================================================
 * SIMULATED_NAV_CHAPTERS — 模拟章节数据
 * ============================================================
 * 各 Tab 的章节内容
 */
val SIMULATED_NAV_CHAPTERS: Map<Int, List<NavChapter>> = mapOf(
    // ── Tab 0: 入门与迁移 ────────────────────────────────────────
    0 to listOf(
        NavChapter(
            id = "ch-0-1",
            title = "1. Navigation 3 简介",
            content = "Jetpack Navigation 3 于 Google I/O 2026 发布，专为「自适应 UI」时代设计。核心改进包括：原生 Window Size Class 支持、Pane API、Cross-Device State Preservation、以及与 Compose Multiplatform 的深度集成。",
            codeBlocks = listOf(
                CodeBlock(
                    language = "kotlin",
                    label = "v3",
                    labelColor = Nav3Teal,
                    code = """// Navigation 3 — 依赖配置
dependencies {
    implementation("androidx.navigation:navigation-compose:3.0.0")
    implementation("androidx.window:window:2.0.0")
}""",
                    filename = "build.gradle.kts"
                ),
                CodeBlock(
                    language = "kotlin",
                    label = "v2.x",
                    labelColor = Nav3Gray,
                    code = """// Navigation 2.x — 旧依赖
dependencies {
    implementation("androidx.navigation:navigation-compose:2.7.7")
}""",
                    filename = "build.gradle.kts (旧)"
                )
            ),
            tabIndex = 0
        ),
        NavChapter(
            id = "ch-0-2",
            title = "2. NavHost 变更",
            content = "Navigation 3 中 NavHost API 发生变化，isEmpty 的默认值从 true 变为 false，并新增 adaptiveNavHost 等 API。",
            codeBlocks = listOf(
                CodeBlock(
                    language = "kotlin",
                    label = "v3",
                    labelColor = Nav3Teal,
                    code = """// Navigation 3 — NavHost
NavHost(
    navController = navController,
    startDestination = "home",
    isEmpty = false  // 默认 false
) {
    composable("home") { HomeScreen() }
    composable("detail/{id}") { DetailScreen() }
}""",
                    filename = "MainNavHost.kt"
                ),
                CodeBlock(
                    language = "kotlin",
                    label = "v2.x",
                    labelColor = Nav3Gray,
                    code = """// Navigation 2.x — NavHost
NavHost(
    navController = navController,
    startDestination = "home"
    // 无 isEmpty 参数
) {
    composable("home") { HomeScreen() }
}""",
                    filename = "MainNavHost.kt (旧)"
                )
            ),
            tabIndex = 0
        ),
        NavChapter(
            id = "ch-0-3",
            title = "3. 迁移检查清单",
            content = "从 Navigation 2.x 迁移到 3.0 的关键步骤：① 升级依赖到 3.0.0；② 检查 NavHost isEmpty 参数；③ 更新 Window Size Class 配置；④ 适配 Pane API；⑤ 更新 Deep Link 配置。",
            codeBlocks = listOf(
                CodeBlock(
                    language = "kotlin",
                    label = "v3",
                    labelColor = Nav3Teal,
                    code = """// Navigation 3 — 迁移检查
val windowSizeClass = calculateWindowSizeClass(this)

// 检查是否为折叠屏/大屏
val isExpanded = windowSizeClass.widthSizeClass ==
    WindowSizeClass.SizeClass.EXPANDED

// 启用自适应导航
if (isExpanded) {
    // 使用 List-Detail 双屏布局
} else {
    // 使用单屏导航
}""",
                    filename = "AdaptiveNavHelper.kt"
                )
            ),
            tabIndex = 0
        )
    ),

    // ── Tab 1: 自适应导航 ────────────────────────────────────────
    1 to listOf(
        NavChapter(
            id = "ch-1-1",
            title = "1. SlidingPaneLayout vs AdaptivePane",
            content = "Navigation 3 推荐使用 AdaptivePane API 处理 List-Detail 自适应布局，替代旧的 SlidingPaneLayout。AdaptivePane 自动响应 WindowSizeClass，无需手动管理 panel 切换逻辑。",
            codeBlocks = listOf(
                CodeBlock(
                    language = "kotlin",
                    label = "v3 (AdaptivePane)",
                    labelColor = Nav3Teal,
                    code = """// Navigation 3 — AdaptivePane
@Composable
fun AdaptiveListDetailScreen() {
    AdaptivePane(
        primaryPane = {
            ListPane(
                onItemClick = { itemId ->
                    detailRoute = "detail/${'$'}itemId"
                }
            )
        },
        secondaryPane = {
            DetailPane(route = detailRoute)
        }
    )
}""",
                    filename = "AdaptiveListDetail.kt"
                ),
                CodeBlock(
                    language = "kotlin",
                    label = "v2.x (SlidingPaneLayout)",
                    labelColor = Nav3Gray,
                    code = """// Navigation 2.x — SlidingPaneLayout
@Composable
fun SlidingListDetailScreen() {
    SlidingPaneLayout(
        firstPane = { ListPane() },
        secondPane = { DetailPane() }
    ).also { layout ->
        if (layout.isOpen) {
            // 展开详情
        } else {
            // 显示列表
        }
    }
}""",
                    filename = "SlidingListDetail.kt (旧)"
                )
            ),
            tabIndex = 1
        ),
        NavChapter(
            id = "ch-1-2",
            title = "2. WindowSizeClass 阈值",
            content = "Navigation 3 使用标准的 WindowSizeClass 阈值：Compact (< 600dp)、Medium (600-840dp)、Expanded (> 840dp)。开发者无需手动计算 pixels，API 直接提供 SizeClass 枚举值。",
            codeBlocks = listOf(
                CodeBlock(
                    language = "kotlin",
                    label = "v3",
                    labelColor = Nav3Teal,
                    code = """// Navigation 3 — WindowSizeClass
val windowSizeClass = calculateWindowSizeClass(this)

when (windowSizeClass.widthSizeClass) {
    WindowSizeClass.SizeClass.COMPACT -> {
        // 手机竖屏：单面板堆叠
        CompactLayout()
    }
    WindowSizeClass.SizeClass.MEDIUM -> {
        // 折叠屏展开/小平板：自适应
        AdaptiveLayout()
    }
    WindowSizeClass.SizeClass.EXPANDED -> {
        // 大屏/Googlebook：双面板并排
        ExpandedLayout()
    }
}""",
                    filename = "WindowSizeClassHelper.kt"
                )
            ),
            tabIndex = 1
        )
    ),

    // ── Tab 2: 折叠屏集成 ───────────────────────────────────────
    2 to listOf(
        NavChapter(
            id = "ch-2-1",
            title = "1. 折叠/展开状态同步",
            content = "Navigation 3 内置折叠状态监听，通过 WindowInfoTracker 自动同步 Navigation 状态。当设备从折叠态切换到展开态时，Navigation 自动恢复之前的页面栈，无需手动处理。",
            codeBlocks = listOf(
                CodeBlock(
                    language = "kotlin",
                    label = "v3",
                    labelColor = Nav3Teal,
                    code = """// Navigation 3 — 折叠状态同步
val foldInfo = windowInfoTracker()
    .windowLayoutInfo(this)
    .collectAsState(initial = null)

val isFolded = foldInfo.value?.displayFeatures
    ?.filterIsInstance<Fold>()
    ?.any { it.state == Fold.State.HALF_OPENED }

// 根据折叠状态调整导航行为
if (isFolded == true) {
    // 折叠状态：显示列表
    navController.navigate("list")
} else {
    // 展开状态：显示双面板
    navController.navigate("list_detail")
}""",
                    filename = "FoldStateSync.kt"
                )
            ),
            tabIndex = 2
        ),
        NavChapter(
            id = "ch-2-2",
            title = "2. Hinge 位置处理",
            content = "Navigation 3 提供 Hinge 位置 API，开发者可以将内容区域智能避让 Hinge 区域，避免 UI 元素被铰链遮挡。配合 Jetpack WindowManager 的 displayFeatures 获取精确位置。",
            codeBlocks = listOf(
                CodeBlock(
                    language = "kotlin",
                    label = "v3",
                    labelColor = Nav3Teal,
                    code = """// Navigation 3 — Hinge 避让
val hingeBounds = windowInfoTracker()
    .windowLayoutInfo(this)
    .value
    ?.displayFeatures
    ?.filterIsInstance<Fold>()
    ?.firstOrNull()
    ?.bounds

// 使用 WindowInsets 避让 Hinge
Box(
    modifier = Modifier.windowInsetsPadding(
        WindowInsets.displayCutout
    )
) {
    // 内容区域自动避让 Hinge
    AdaptivePaneContent()
}""",
                    filename = "HingeAvoidance.kt"
                )
            ),
            tabIndex = 2
        )
    ),

    // ── Tab 3: 跨设备状态 ────────────────────────────────────────
    3 to listOf(
        NavChapter(
            id = "ch-3-1",
            title = "1. Cross-Device State Preservation",
            content = "Navigation 3 的核心特性之一：跨设备状态保持。当用户在手机上的 Navigation 状态（当前路由、页面栈）可以无缝同步到平板或折叠屏，切换设备时自动恢复。",
            codeBlocks = listOf(
                CodeBlock(
                    language = "kotlin",
                    label = "v3",
                    labelColor = Nav3Teal,
                    code = """// Navigation 3 — 跨设备状态保持
val navController = rememberNavController(
    saveState = true,  // 启用状态保存
    restoreState = true  // 启用状态恢复
)

// 当 Activity 重建或跨设备时
// Navigation 自动保存/恢复：
// - 当前路由
// - 页面栈
// - BackStack
navController.navigate("detail/42")""",
                    filename = "CrossDeviceState.kt"
                )
            ),
            tabIndex = 3
        ),
        NavChapter(
            id = "ch-3-2",
            title = "2. Deep Link 多窗口行为",
            content = "在多窗口模式下，Navigation 3 对 Deep Link 行为进行了规范：单窗口模式下 Deep Link 直接导航；多窗口模式下可以指定 launch destination 或使用 newTask flag。",
            codeBlocks = listOf(
                CodeBlock(
                    language = "kotlin",
                    label = "v3",
                    labelColor = Nav3Teal,
                    code = """// Navigation 3 — Deep Link 多窗口
val navGraph = NavHostGraph(
    startDestination = "home",
    route = NavHostGraph.rootRoute
) {
    composable(
        route = "detail/{id}",
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "https://myapp.com/detail/{id}"
                // 多窗口模式下 launch destination
                launchInSingleTop = true
            }
        )
    ) { backStackEntry ->
        DetailScreen(id = backStackEntry.arguments?.getString("id"))
    }
}""",
                    filename = "DeepLinkMultiWindow.kt"
                )
            ),
            tabIndex = 3
        )
    ),

    // ── Tab 4: CI 验证工具 ──────────────────────────────────────
    4 to listOf(
        NavChapter(
            id = "ch-4-1",
            title = "1. NavGraph XML 合法性检测",
            content = "CI 验证工具可以扫描项目中的 nav_graph.xml 文件，检测：① action 缺少 id；② destination 不存在；③ deepLink uriPattern 格式错误；④ arguments 类型不匹配。",
            codeBlocks = listOf(
                CodeBlock(
                    language = "xml",
                    label = "v3 (正确示例)",
                    labelColor = Nav3Teal,
                    code = """<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_graph"
    app:startDestination="@id/homeFragment">

    <fragment
        android:id="@+id/homeFragment"
        android:name="com.example.HomeFragment"
        android:label="Home">
        <action
            android:id="@+id/action_home_to_detail"
            app:destination="@id/detailFragment" />
    </fragment>

    <fragment
        android:id="@+id/detailFragment"
        android:name="com.example.DetailFragment"
        android:label="Detail">
        <argument
            android:name="id"
            app:argType="long" />
        <deepLink
            android:id="@+id/deepLink"
            app:uri="myapp://detail/{id}" />
    </fragment>
</navigation>""",
                    filename = "nav_graph.xml"
                ),
                CodeBlock(
                    language = "xml",
                    label = "v2.x (错误示例)",
                    labelColor = Nav3Gray,
                    code = """<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <!-- ❌ 错误：缺少 action id -->
    <fragment
        android:id="@+id/homeFragment"
        android:name="com.example.HomeFragment">
        <action app:destination="@id/detailFragment" />
    </fragment>

    <!-- ❌ 错误：argument 缺少 name -->
    <fragment
        android:id="@+id/detailFragment"
        android:name="com.example.DetailFragment">
        <argument
            android:name="id"
            app:argType="long" />
    </fragment>
</navigation>""",
                    filename = "nav_graph_old.xml (有错误)"
                )
            ),
            tabIndex = 4
        ),
        NavChapter(
            id = "ch-4-2",
            title = "2. Size Class 配置验证",
            content = "CI 工具还可以验证 WindowSizeClass 配置是否正确适配各设备类型：Compact 是否只有单面板、Expanded 是否强制双面板、是否遗漏了 Medium 的自适应处理。",
            codeBlocks = listOf(
                CodeBlock(
                    language = "kotlin",
                    label = "CI 验证规则",
                    labelColor = Nav3Teal,
                    code = """// CI 验证规则 — SizeClass 检查清单
val rules = listOf(
    SizeClassRule(
        sizeClass = WindowSizeClass.SizeClass.COMPACT,
        mustHave = listOf(Panel.SINGLE),
        mustNotHave = listOf(Panel.LIST_DETAIL)
    ),
    SizeClassRule(
        sizeClass = WindowSizeClass.SizeClass.MEDIUM,
        mustHave = listOf(Panel.ADAPTIVE),
        mustNotHave = emptyList()
    ),
    SizeClassRule(
        sizeClass = WindowSizeClass.SizeClass.EXPANDED,
        mustHave = listOf(Panel.LIST_DETAIL, Panel.DUAL),
        mustNotHave = emptyList()
    )
)

// 运行验证
val results = SizeClassValidator.validate(
    config = currentWindowConfig,
    rules = rules
)

// 输出报告
// 遍历验证结果，如有问题则输出 FAIL
println("验证完成")
""",
                    filename = "SizeClassValidator.kt"
                )
            ),
            tabIndex = 4
        )
    )
)

/**
 * ============================================================
 * SIMULATED_DECISION_TREE — 模拟决策树（SlidingPaneLayout vs AdaptivePane 选择）
 * ============================================================
 */
val SIMULATED_DECISION_TREE = listOf(
    DecisionNode(
        id = "q1",
        type = "question",
        content = "你的项目是否需要支持 Android 16/17？",
        options = listOf(
            DecisionOption("yes", "是（需要最新 API）", "q2"),
            DecisionOption("no", "否（兼容旧版本即可）", "result_slidingpane")
        )
    ),
    DecisionNode(
        id = "q2",
        type = "question",
        content = "你主要面向哪些设备？",
        options = listOf(
            DecisionOption("phone", "手机为主，偶尔折叠屏", "result_slidingpane"),
            DecisionOption("foldable", "折叠屏/Pixel Fold/Samsung Foldable", "result_adaptive"),
            DecisionOption("tablet", "平板/Googlebook 为主", "result_adaptive")
        )
    ),
    DecisionNode(
        id = "result_slidingpane",
        type = "result",
        content = "推荐 SlidingPaneLayout",
        resultCard = ResultCard(
            title = "✅ 推荐：SlidingPaneLayout",
            recommendation = "如果项目以手机为主，只需要轻度折叠屏支持，SlidingPaneLayout 仍然是稳定选择。兼容性好，文档丰富，Navigation 2.x 和 3.x 都支持。",
            codeExample = """// SlidingPaneLayout 集成
dependencies {
    implementation("androidx.slidingpanelayout:slidingpanelayout:1.2.0")
}

// 代码中使用 SlidingPaneLayout
SlidingPaneLayout(
    modifier = Modifier.fillMaxSize(),
    firstPane = { ListPane() },
    secondPane = { DetailPane() }
)""",
            notes = "注意：Google 已宣布 SlidingPaneLayout 进入维护模式，未来 Googlebook 适配可能需要迁移到 AdaptivePane。"
        )
    ),
    DecisionNode(
        id = "result_adaptive",
        type = "result",
        content = "推荐 AdaptivePane（Navigation 3）",
        resultCard = ResultCard(
            title = "✅ 推荐：AdaptivePane（Navigation 3）",
            recommendation = "Navigation 3 原生支持 WindowSizeClass，AdaptivePane 自动响应折叠/展开状态，跨设备体验最佳。是 Googlebook 和大屏 Android 的推荐方案。",
            codeExample = """// Navigation 3 — AdaptivePane
dependencies {
    implementation("androidx.navigation:navigation-compose:3.0.0")
    implementation("androidx.window:window:2.0.0")
}

AdaptivePane(
    primaryPane = { ListPane() },
    secondaryPane = { DetailPane() }
)""",
            notes = "需要 Navigation 3.0+，依赖 Window 2.0+。如果项目仍需支持 Android 7.0+，建议使用 WindowInsetsController 判断屏幕尺寸。"
        )
    )
)

/**
 * ============================================================
 * DEFAULT_NAV_GRAPH_XML — 默认的 NavGraph XML 示例
 * ============================================================
 */
const val DEFAULT_NAV_GRAPH_XML = """<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/nav_graph"
    app:startDestination="@id/homeFragment">

    <!-- 入门页面 -->
    <fragment
        android:id="@+id/homeFragment"
        android:name="com.example.HomeFragment"
        android:label="@string/home"
        tools:layout="@layout/fragment_home">
        <action
            android:id="@+id/action_home_to_list"
            app:destination="@id/listFragment"
            app:enterAnim="@anim/slide_in_right"
            app:exitAnim="@anim/slide_out_left"
            app:popEnterAnim="@anim/slide_in_left"
            app:popExitAnim="@anim/slide_out_right" />
    </fragment>

    <!-- 列表页面 -->
    <fragment
        android:id="@+id/listFragment"
        android:name="com.example.ListFragment"
        android:label="@string/list"
        tools:layout="@layout/fragment_list">
        <action
            android:id="@+id/action_list_to_detail"
            app:destination="@id/detailFragment" />
    </fragment>

    <!-- 详情页面 -->
    <fragment
        android:id="@+id/detailFragment"
        android:name="com.example.DetailFragment"
        android:label="@string/detail"
        tools:layout="@layout/fragment_detail">
        <argument
            android:name="itemId"
            app:argType="long"
            android:defaultValue="0L" />
        <deepLink
            android:id="@+id/deepLink"
            app:uri="myapp://detail/{itemId}" />
    </fragment>

    <!-- 全局 Deep Link -->
    <deepLink
        android:id="@+id/globalDeepLink"
        app:uri="myapp://home" />

</navigation>"""
