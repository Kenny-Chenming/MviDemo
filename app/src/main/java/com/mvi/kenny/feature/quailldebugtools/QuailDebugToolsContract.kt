package com.mvi.kenny.feature.quailldebugtools

// ================================================================
// QuailDebugToolsContract — Android Studio Quail 调试/性能工具包 MVI 契约
// ================================================================
// MVI architecture contract for Quail Debug & Performance Toolkit.
//
// PRD-242: Android Studio Quail 调试/性能工具包
// Design: memory/agency/designs/PRD-242-Android-Studio-Quail-调试-性能工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (clipboard, toast), via Channel
// ================================================================
//
// Tab Structure:
//   Tab 0: Recomposition Observe Node — Layout Inspector 内追踪 Composable 状态读取
//   Tab 1: LeakCanary in Profiler — LeakCanary 原生集成进 Android Studio Profiler
//   Tab 2: 联合诊断工作流 — Observe Node + Layout Inspector + Profiler 三工具联动
//   Tab 3: CI 集成方案 — Recomposition/LeakCanary CI 集成最佳实践
//   Tab 4: Quail vs Panda 选型 — Quail vs Panda 4 调试工具选型决策树
// ================================================================
//
// NOTE: This is a local reference tool. It does NOT connect to Android Studio.
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// Tool Type & Status Enums / 工具类型 & 状态枚举
// ================================================================

/**
 * ============================================================
 * ToolType — 工具类型枚举
 * ============================================================
 */
enum class ToolType(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    RECOMPOSITION_OBSERVE_NODE("Recomposition Observe Node", "🔍", "Layout Inspector 内追踪 Composable 状态读取"),
    LEAKCANARY_PROFILER("LeakCanary in Profiler", "🔧", "LeakCanary 原生集成进 Android Studio Profiler"),
    JOINT_DIAGNOSTIC("联合诊断工作流", "🔗", "Observe Node + Layout Inspector + Profiler 三工具联动"),
    CI_INTEGRATION("CI 集成方案", "⚙️", "Recomposition/LeakCanary CI 集成最佳实践"),
    QUAIL_VS_PANDA("Quail vs Panda 选型", "⚖️", "Quail vs Panda 4 调试工具选型决策树")
}

/**
 * ============================================================
 * DiagnosticStep — 诊断步骤枚举
 * ============================================================
 */
enum class DiagnosticStep(
    val stepNumber: Int,
    val displayName: String,
    val emoji: String
) {
    COLLECT(1, "收集", "📊"),
    ANALYZE(2, "分析", "🔬"),
    FIX(3, "修复", "🔧"),
    VERIFY(4, "验证", "✅")
}

/**
 * ============================================================
 * StudioVersion — Android Studio 版本枚举
 * ============================================================
 */
enum class StudioVersion(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    QUAIL_CANARY("Quail 1 Canary 4+", "🆕", "最新功能：Recomposition Observe Node、LeakCanary in Profiler"),
    QUAIL_STABLE("Quail 稳定版", "✅", "功能较 Canary 少，建议等待正式版"),
    PANDA_4("Panda 4.x", "🐼", "无 Recomposition Observe Node，Profiling 功能稳定")
}

// ================================================================
// State / 状态
// ================================================================

/**
 * ============================================================
 * QuailDebugToolsState — Quail 调试/性能工具页面状态（MVI State）
 * ============================================================
 *
 * @param selectedTab 当前 Tab 索引 (0-4)
 *
 * Tab 0 — Recomposition Observe Node
 * @param observeNodeGuideItems Observe Node 指南列表
 * @param observeNodeReady Observe Node 功能是否可用（需要 Quail 1 Canary 4+）
 *
 * Tab 1 — LeakCanary in Profiler
 * @param leakcanaryGuideItems LeakCanary 指南列表
 * @param leakcanaryVsLibraryItems Profiler 版 vs 库版对比列表
 *
 * Tab 2 — 联合诊断工作流
 * @param diagnosticWorkflowSteps 诊断工作流步骤列表
 * @param currentDiagnosticStep 当前诊断步骤
 *
 * Tab 3 — CI 集成
 * @param ciIntegrationItems CI 集成项列表
 *
 * Tab 4 — Quail vs Panda 选型
 * @param studioVersion 当前选中的 Studio 版本
 * @param comparisonItems 版本对比列表
 *
 * @param snackbarMessage Snackbar 消息
 */
data class QuailDebugToolsState(
    // ── 全局状态 ───────────────────────────────────────────────
    val selectedTab: Int = 0,

    // ── Tab 0: Recomposition Observe Node ───────────────────
    val observeNodeGuideItems: List<ObserveNodeGuideItem> = emptyList(),
    val observeNodeReady: Boolean = false,

    // ── Tab 1: LeakCanary in Profiler ───────────────────────
    val leakcanaryGuideItems: List<LeakCanaryGuideItem> = emptyList(),
    val leakcanaryVsLibraryItems: List<LeakCanaryVsLibraryItem> = emptyList(),

    // ── Tab 2: 联合诊断工作流 ───────────────────────────────
    val diagnosticWorkflowSteps: List<DiagnosticWorkflowStep> = emptyList(),
    val currentDiagnosticStep: DiagnosticStep = DiagnosticStep.COLLECT,

    // ── Tab 3: CI 集成方案 ─────────────────────────────────
    val ciIntegrationItems: List<CIIntegrationItem> = emptyList(),

    // ── Tab 4: Quail vs Panda 选型 ────────────────────────
    val studioVersion: StudioVersion = StudioVersion.QUAIL_CANARY,
    val comparisonItems: List<VersionComparisonItem> = emptyList(),

    // ── 全局 ─────────────────────────────────────────────────
    val snackbarMessage: String? = null
) {
    companion object {
        val Initial = QuailDebugToolsState()
    }
}

// ================================================================
// Intent / 用户意图
// ================================================================

/**
 * ============================================================
 * QuailDebugToolsIntent — 用户意图（User Intent）
 * ============================================================
 */
sealed interface QuailDebugToolsIntent {

    /** 用户切换主 Tab
     * @param index Tab 索引 (0-4)
     */
    data class SelectTab(val index: Int) : QuailDebugToolsIntent

    /** 用户选择 Studio 版本（Tab 4）
     * @param version 选中的 Studio 版本
     */
    data class SelectStudioVersion(val version: StudioVersion) : QuailDebugToolsIntent

    /** 用户切换诊断步骤（Tab 2）
     * @param step 选中的诊断步骤
     */
    data class SelectDiagnosticStep(val step: DiagnosticStep) : QuailDebugToolsIntent

    /** 用户复制代码
     * @param code 要复制的代码
     */
    data class CopyCode(val code: String) : QuailDebugToolsIntent

    /** 用户关闭 Snackbar */
    data object DismissSnackbar : QuailDebugToolsIntent

    /** 用户重置所有状态 */
    data object ResetAll : QuailDebugToolsIntent
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * ============================================================
 * QuailDebugToolsEffect — 一次性副作用（Effect）
 * ============================================================
 */
sealed interface QuailDebugToolsEffect {

    /** 复制到剪贴板
     * @param content 要复制的内容
     */
    data class CopyToClipboard(val content: String) : QuailDebugToolsEffect

    /** 显示 Snackbar
     * @param message 消息文本
     */
    data class ShowSnackbar(val message: String) : QuailDebugToolsEffect

    /** 打开外部链接
     * @param url 外部链接 URL
     */
    data class OpenExternalLink(val url: String) : QuailDebugToolsEffect
}

// ================================================================
// Data Models / 数据模型
// ================================================================

/**
 * ============================================================
 * ObserveNodeGuideItem — Recomposition Observe Node 指南条目
 * ============================================================
 *
 * @param id 唯一 ID
 * @param title 标题（中文）
 * @param titleEn 标题（英文）
 * @param description 描述
 * @param steps 操作步骤列表
 * @param stepsCn 中文操作步骤列表
 * @param keyInsight 关键洞察
 * @param screenshotPlaceholder 截图占位符描述
 */
data class ObserveNodeGuideItem(
    val id: String,
    val title: String,
    val titleEn: String,
    val description: String,
    val steps: List<String>,
    val stepsCn: List<String>,
    val keyInsight: String,
    val screenshotPlaceholder: String
)

/**
 * ============================================================
 * LeakCanaryGuideItem — LeakCanary in Profiler 指南条目
 * ============================================================
 *
 * @param id 唯一 ID
 * @param title 标题（中文）
 * @param titleEn 标题（英文）
 * @param description 描述
 * @param detectionScope 检测范围（Activity/Fragment/View/Compose）
 * @param steps 操作步骤列表
 * @param stepsCn 中文操作步骤列表
 * @param limitations 局限性说明
 */
data class LeakCanaryGuideItem(
    val id: String,
    val title: String,
    val titleEn: String,
    val description: String,
    val detectionScope: String,
    val steps: List<String>,
    val stepsCn: List<String>,
    val limitations: String
)

/**
 * ============================================================
 * LeakCanaryVsLibraryItem — Profiler 版 vs 库版对比条目
 * ============================================================
 *
 * @param id 唯一 ID
 * @param feature 特性名称
 * @param profilerCapability Profiler 版能力
 * @param libraryCapability 库版能力
 * @param notes 备注
 */
data class LeakCanaryVsLibraryItem(
    val id: String,
    val feature: String,
    val profilerCapability: String,
    val libraryCapability: String,
    val notes: String
)

/**
 * ============================================================
 * DiagnosticWorkflowStep — 联合诊断工作流步骤
 * ============================================================
 *
 * @param step 诊断步骤枚举
 * @param title 标题
 * @param description 描述
 * @param tools 使用的工具列表
 * @param expectedOutcome 预期结果
 * @param screenshotPlaceholder 截图占位符描述
 */
data class DiagnosticWorkflowStep(
    val step: DiagnosticStep,
    val title: String,
    val description: String,
    val tools: List<String>,
    val expectedOutcome: String,
    val screenshotPlaceholder: String
)

/**
 * ============================================================
 * CIIntegrationItem — CI 集成项
 * ============================================================
 *
 * @param id 唯一 ID
 * @param title 标题
 * @param description 描述
 * @param toolType 工具类型
 * @param ciPlatform CI 平台（GitHub Actions/GitLab CI/Jenkins）
 * @param codeExample CI 配置代码示例
 * @param notes 备注
 */
data class CIIntegrationItem(
    val id: String,
    val title: String,
    val description: String,
    val toolType: ToolType,
    val ciPlatform: String,
    val codeExample: String,
    val notes: String
)

/**
 * ============================================================
 * VersionComparisonItem — 版本对比条目
 * ================================================================
 *
 * @param id 唯一 ID
 * @param feature 特性名称
 * @param quailCapability Quail 能力描述
 * @param pandaCapability Panda 能力描述
 * @param recommendation 推荐说明
 * @param emoji 推荐 emoji
 */
data class VersionComparisonItem(
    val id: String,
    val feature: String,
    val quailCapability: String,
    val pandaCapability: String,
    val recommendation: String,
    val emoji: String
)

// ================================================================
// Simulated Guide Data / 模拟指南数据
// ================================================================

/**
 * ============================================================
 * SIMULATED_OBSERVE_NODE_GUIDE_ITEMS — 模拟 Observe Node 指南
 * ============================================================
 */
val SIMULATED_OBSERVE_NODE_GUIDE_ITEMS = listOf(
    ObserveNodeGuideItem(
        id = "observe-001",
        title = "Observe Node 激活与基本使用",
        titleEn = "Activate Observe Node and Basic Usage",
        description = "Recomposition Observe Node 是 Android Studio Quail 1 Canary 4 在 Layout Inspector 中新增的功能，允许开发者直接追踪 Composable 的状态读取（state reads），无需打日志或使用外部工具。",
        steps = listOf(
            "Open Layout Inspector: View → Tool Windows → Layout Inspector",
            "Select a Composable node in the Component Tree",
            "Click the \"Observe Node\" button (new in Quail)",
            "Interact with the app to trigger recomposition",
            "Observe the right panel for State Reads timeline"
        ),
        stepsCn = listOf(
            "打开 Layout Inspector：View → Tool Windows → Layout Inspector",
            "在组件树中选择目标 Composable 节点",
            "点击「Observe Node」按钮（Quail 新增）",
            "与 App 交互触发 recomposition",
            "观察右侧面板中的 State Reads 时间线"
        ),
        keyInsight = "⚠️ Observe Node 仅在 Composable 函数内有效，非 Composable 代码的 state reads 不会显示。",
        screenshotPlaceholder = "[截图：Layout Inspector Observe Node 界面，显示 State Reads 面板]"
    ),
    ObserveNodeGuideItem(
        id = "observe-002",
        title = "State Reads 面板解读",
        titleEn = "Interpret State Reads Panel",
        description = "State Reads 面板以时间线形式展示每次 recomposition 中读取的状态变量，包含：变量名、读取位置、触发 recompose 的次数。",
        steps = listOf(
            "Locate the State Reads panel (right side of Layout Inspector)",
            "Identify the variable name and read location",
            "Check recompose count for each state read",
            "Filter by minimum recompose count (click filter icon)"
        ),
        stepsCn = listOf(
            "定位 State Reads 面板（Layout Inspector 右侧）",
            "识别变量名和读取位置",
            "检查每个 state read 的 recompose count",
            "按最小 recompose count 筛选（点击筛选图标）"
        ),
        keyInsight = "💡 recompose count 是累积值，需要在测试场景前后对比 delta，而非看绝对值。",
        screenshotPlaceholder = "[截图：State Reads 面板，显示变量名和 recompose count]"
    ),
    ObserveNodeGuideItem(
        id = "observe-003",
        title = "联合分析：recomposition count + state reads",
        titleEn = "Joint Analysis: recomposition count + State Reads",
        description = "将 recomposition count 与 State Reads 联合分析，是定位不必要 recomposition 的核心方法。高 recompose count + 意外的 state reads = 性能问题。",
        steps = listOf(
            "Check recomposition count for target Composable",
            "Activate Observe Node for the same Composable",
            "Cross-reference: which state read triggered each recompose?",
            "Identify unnecessary state reads (derivedStateOf missing, etc.)"
        ),
        stepsCn = listOf(
            "检查目标 Composable 的 recomposition count",
            "为同一 Composable 激活 Observe Node",
            "交叉参照：哪个 state read 触发了每次 recompose？",
            "识别不必要的 state reads（缺失 derivedStateOf 等）"
        ),
        keyInsight = "🔬 多线程场景：Observe Node 追踪主线程 recompose，其他线程需结合 CPU Profiler。",
        screenshotPlaceholder = "[截图：recomposition count + State Reads 联合分析视图]"
    ),
    ObserveNodeGuideItem(
        id = "observe-004",
        title = "常见 recomposition 问题模式",
        titleEn = "Common Recomposition Problem Patterns",
        description = "通过 Observe Node 识别常见的 recomposition 问题模式：不必要的 state 读取、remember 误用、derivedStateOf 缺失。",
        steps = listOf(
            "Pattern 1: Unstable object passed as param → unstable object causes recompose",
            "Pattern 2: Missing derivedStateOf → derived value triggers recompose",
            "Pattern 3: remember { mutableStateOf() } without key → always new reference",
            "Use Observe Node to confirm each pattern"
        ),
        stepsCn = listOf(
            "模式1：传递不稳定对象作为参数 → 不稳定对象导致 recompose",
            "模式2：缺失 derivedStateOf → 派生值触发 recompose",
            "模式3：remember { mutableStateOf() } 无 key → 始终新引用",
            "使用 Observe Node 确认每种模式"
        ),
        keyInsight = "✅ 版本要求：Observe Node 需要 Quail 1 Canary 4+，旧版 Android Studio 无此功能。",
        screenshotPlaceholder = "[截图：常见 recomposition 问题模式示意]"
    ),
    ObserveNodeGuideItem(
        id = "observe-005",
        title = "案例：LazyColumn item 每次 recompose 根因定位",
        titleEn = "Case Study: LazyColumn Item Root Cause Analysis",
        description = "通过一个真实案例展示如何使用 Observe Node 定位 LazyColumn item 每次 recompose 的根因。",
        steps = listOf(
            "Select a LazyColumn item Composable in Layout Inspector",
            "Activate Observe Node",
            "Scroll the list and observe state reads",
            "Identify: is the item receiving a new object reference?",
            "Check parent Composable's state derivation"
        ),
        stepsCn = listOf(
            "在 Layout Inspector 中选择 LazyColumn item Composable",
            "激活 Observe Node",
            "滚动列表观察 state reads",
            "识别：item 是否收到了新的对象引用？",
            "检查父 Composable 的状态派生"
        ),
        keyInsight = "💡 最佳效果需要 Compose 1.7+，旧 Compose 版本功能受限。",
        screenshotPlaceholder = "[截图：LazyColumn item Observe Node 分析]"
    )
)

/**
 * ============================================================
 * SIMULATED_LEAKCANARY_GUIDE_ITEMS — 模拟 LeakCanary 指南
 * ============================================================
 */
val SIMULATED_LEAKCANARY_GUIDE_ITEMS = listOf(
    LeakCanaryGuideItem(
        id = "leak-001",
        title = "LeakCanary in Profiler 启动与基本使用",
        titleEn = "Start and Use LeakCanary in Profiler",
        description = "LeakCanary in Profiler 是 Android Studio Quail 1 首次将 LeakCanary 能力集成进 Android Studio Profiler，无需在项目中添加 LeakCanary 依赖即可使用内存泄漏检测功能。",
        detectionScope = "Activity / Fragment / View / Compose 节点",
        steps = listOf(
            "Open Android Studio Profiler: View → Tool Windows → Profiler",
            "Select target device and process",
            "Click the \"LeakCanary\" tab (new in Quail)",
            "Perform the action you want to test (e.g., Fragment navigation)",
            "Wait 5-10 seconds for GC + leak detection",
            "View detected leaks in the LeakCanary panel"
        ),
        stepsCn = listOf(
            "打开 Android Studio Profiler：View → Tool Windows → Profiler",
            "选择目标设备和进程",
            "点击「LeakCanary」标签（Quail 新增）",
            "执行需要检测的操作（如 Fragment 切换）",
            "等待 5-10 秒（GC + 泄漏检测）",
            "在 LeakCanary 面板中查看检测到的泄漏"
        ),
        limitations = "⚠️ Profiler 版无法检测 native 内存泄漏，库版可以。"
    ),
    LeakCanaryGuideItem(
        id = "leak-002",
        title = "泄漏对象 GC Root 路径解读",
        titleEn = "Interpret GC Root Path of Leaked Objects",
        description = "LeakCanary in Profiler 检测到泄漏后，会显示泄漏对象的 GC Root 路径，帮助开发者理解泄漏链。",
        detectionScope = "Activity / Fragment / View",
        steps = listOf(
            "View the leak list in LeakCanary panel",
            "Click on a leak entry to expand GC Root path",
            "Identify the leak chain: Object → Reference → GC Root",
            "Determine the leak source (who holds the strong reference)"
        ),
        stepsCn = listOf(
            "在 LeakCanary 面板中查看泄漏列表",
            "点击泄漏条目展开 GC Root 路径",
            "识别泄漏链：Object → Reference → GC Root",
            "确定泄漏源（谁持有强引用）"
        ),
        limitations = "💡 支持导出 hprof 文件（50MB+），CI 中建议只导出泄漏对象而非完整 hprof。"
    ),
    LeakCanaryGuideItem(
        id = "leak-003",
        title = "Compose 内存泄漏检测",
        titleEn = "Compose Memory Leak Detection",
        description = "LeakCanary in Profiler 可检测 Compose 节点的内存泄漏，特别是 Fragment-Compose 混合场景中的泄漏。",
        detectionScope = "Compose 节点 / remember / mutableStateOf",
        steps = listOf(
            "Navigate between Compose screens multiple times",
            "Wait for GC after each navigation",
            "Check LeakCanary panel for Compose node leaks",
            "Investigate: is remember scope correctly defined?"
        ),
        stepsCn = listOf(
            "多次在不同 Compose 屏幕间导航",
            "每次导航后等待 GC",
            "检查 LeakCanary 面板中的 Compose 节点泄漏",
            "调查：remember 作用域是否正确定义？"
        ),
        limitations = "⚠️ 老设备兼容性：非 root 设备需要手动触发 GC（Profiler 内置按钮）。"
    ),
    LeakCanaryGuideItem(
        id = "leak-004",
        title = "特定场景泄漏检测案例",
        titleEn = "Specific Scenario Leak Detection Cases",
        description = "Fragment 泄漏、Bitmap 泄漏、Listener 泄漏的检测案例。",
        detectionScope = "Fragment / Bitmap / Listener",
        steps = listOf(
            "Case 1: Fragment Leak — Navigate back and forth, check LeakCanary",
            "Case 2: Bitmap Leak — Load large images, check for Bitmap references",
            "Case 3: Listener Leak — Register/unregister listeners, check for strong refs"
        ),
        stepsCn = listOf(
            "案例1：Fragment 泄漏 — 前后导航，检查 LeakCanary",
            "案例2：Bitmap 泄漏 — 加载大图，检查 Bitmap 引用",
            "案例3：Listener 泄漏 — 注册/取消注册监听器，检查强引用"
        ),
        limitations = "⚠️ 检测延迟：LeakCanary 在 GC 后才检测泄漏，非实时，有操作后等待 5-10 秒。"
    )
)

/**
 * ============================================================
 * SIMULATED_LEAKCANARY_VS_LIBRARY_ITEMS — Profiler vs 库版对比
 * ============================================================
 */
val SIMULATED_LEAKCANARY_VS_LIBRARY_ITEMS = listOf(
    LeakCanaryVsLibraryItem(
        id = "vs-001",
        feature = "依赖库添加",
        profilerCapability = "❌ 无需添加依赖（Profiler 原生集成）",
        libraryCapability = "✅ 需要添加 leakcanary dependency",
        notes = "Profiler 版零配置，库版需 gradle 依赖"
    ),
    LeakCanaryVsLibraryItem(
        id = "vs-002",
        feature = "Native 内存泄漏检测",
        profilerCapability = "❌ 不支持 native 泄漏检测",
        libraryCapability = "✅ 支持 native 泄漏检测",
        notes = "库版 LeakCanary 可以检测 native 内存泄漏，Profiler 版不行"
    ),
    LeakCanaryVsLibraryItem(
        id = "vs-003",
        feature = "CI 集成",
        profilerCapability = "❌ 仅支持手动使用，无法 CI 自动化",
        libraryCapability = "✅ 支持 CI 集成（Gradle 插件方式）",
        notes = "推荐：开发调试用 Profiler 版，CI 自动化用库版"
    ),
    LeakCanaryVsLibraryItem(
        id = "vs-004",
        feature = "检测延迟",
        profilerCapability = "⚠️ 5-10 秒（等待 GC + 检测）",
        libraryCapability = "⚠️ 5-10 秒（等待 GC + 检测）",
        notes = "两者延迟相同，都是非实时检测"
    ),
    LeakCanaryVsLibraryItem(
        id = "vs-005",
        feature = "hprof 导出",
        profilerCapability = "✅ 支持，导出泄漏对象或完整 hprof",
        libraryCapability = "✅ 支持，可配置导出策略",
        notes = "CI 中建议只导出泄漏对象而非完整 hprof（文件较大）"
    ),
    LeakCanaryVsLibraryItem(
        id = "vs-006",
        feature = "Compose 节点检测",
        profilerCapability = "✅ 支持（Quail 新增）",
        libraryCapability = "✅ 支持（LeakCanary 2.x+）",
        notes = "两者都支持 Compose 节点泄漏检测"
    )
)

/**
 * ============================================================
 * SIMULATED_DIAGNOSTIC_WORKFLOW_STEPS — 联合诊断工作流步骤
 * ============================================================
 */
val SIMULATED_DIAGNOSTIC_WORKFLOW_STEPS = listOf(
    DiagnosticWorkflowStep(
        step = DiagnosticStep.COLLECT,
        title = "收集：Recomposition + 内存数据",
        description = "使用 Recomposition Observe Node 收集 recomposition 热点，使用 LeakCanary in Profiler 收集内存泄漏数据。",
        tools = listOf("Recomposition Observe Node", "LeakCanary in Profiler", "Layout Inspector"),
        expectedOutcome = "获取 recomposition count 分布图和内存泄漏列表",
        screenshotPlaceholder = "[截图：Layout Inspector + Profiler 同时打开的状态]"
    ),
    DiagnosticWorkflowStep(
        step = DiagnosticStep.ANALYZE,
        title = "分析：定位根因",
        description = "分析 Observe Node 的 State Reads 追踪结果，结合 LeakCanary 的 GC Root 路径，确定性能问题的根因。",
        tools = listOf("State Reads Panel", "GC Root Path", "recomposition count"),
        expectedOutcome = "确定是 recomposition 问题还是内存泄漏问题，或两者都有",
        screenshotPlaceholder = "[截图：State Reads + GC Root 路径联合分析]"
    ),
    DiagnosticWorkflowStep(
        step = DiagnosticStep.FIX,
        title = "修复：针对性优化",
        description = "根据分析结果修复：优化 state reads 使用 derivedStateOf，修复内存泄漏引用链。",
        tools = listOf("Code Editor", "LeakCanary hprof", "Observe Node"),
        expectedOutcome = "代码变更：derivedStateOf / remember / 引用链修复",
        screenshotPlaceholder = "[截图：代码编辑器中修复 recomposition 问题]"
    ),
    DiagnosticWorkflowStep(
        step = DiagnosticStep.VERIFY,
        title = "验证：确认修复效果",
        description = "使用 Observe Node 重新检查 recomposition count，使用 LeakCanary 重新检测泄漏，确认问题已解决。",
        tools = listOf("Recomposition Observe Node", "LeakCanary in Profiler"),
        expectedOutcome = "recomposition count 下降，内存泄漏消失",
        screenshotPlaceholder = "[截图：修复后 Observe Node 显示正常 recomposition count]"
    )
)

/**
 * ============================================================
 * SIMULATED_CI_INTEGRATION_ITEMS — CI 集成项
 * ============================================================
 */
val SIMULATED_CI_INTEGRATION_ITEMS = listOf(
    CIIntegrationItem(
        id = "ci-001",
        title = "Recomposition Observe Node — CI 集成说明",
        description = "Observe Node 是设计时工具，无法在 CI 中自动化。但可以输出 Layout Inspector 快照到 artifact，供人工 review。",
        toolType = ToolType.RECOMPOSITION_OBSERVE_NODE,
        ciPlatform = "GitHub Actions",
        codeExample = """# ⚠️ Observe Node CI 局限性
# Observe Node 是设计时/调试时工具，无法 CI 自动化

# 推荐做法：导出 Layout Inspector 快照
- name: Export Layout Inspector Snapshot
  run: |
    # Layout Inspector 快照需要手动操作
    # 推荐：在 MR 描述中附上 Observe Node 截图
    echo "Observe Node screenshots should be attached to MR description" """,
        notes = "💡 Observe Node 推荐开发调试阶段使用，不适合 CI 自动化。"
    ),
    CIIntegrationItem(
        id = "ci-002",
        title = "LeakCanary 库版 — CI 集成",
        description = "使用 LeakCanary 库版（而非 Profiler 版）集成到 CI，支持 Gradle 插件方式阻塞未修复泄漏的构建。",
        toolType = ToolType.LEAKCANARY_PROFILER,
        ciPlatform = "GitHub Actions",
        codeExample = """# build.gradle.kts (app)
debugImplementation "com.squareup.leakcanary:leakcanary:2.14"

# build.gradle.kts (root) — LeakCanary Gradle Plugin
plugins {
    id("com.android.leakcanary") version "2.14"
}

# GitHub Actions — 阻塞有泄漏的构建
- name: Run LeakCanary
  run: ./gradlew :app:leakcanaryAnalyzeDebug
  continue-on-error: true

- name: Check LeakCanary Report
  run: |
    if grep -q "LeakCanary: Leak detected" build/reports/leakcanary/; then
      echo "❌ Memory leaks detected! Build blocked."
      exit 1
    fi""",
        notes = "✅ 推荐：Profiler 版用于开发调试，库版用于 CI 自动化。"
    ),
    CIIntegrationItem(
        id = "ci-003",
        title = "GitLab CI + LeakCanary",
        description = "GitLab CI 环境下 LeakCanary CI 集成，配置 artifact 收集泄漏报告。",
        toolType = ToolType.LEAKCANARY_PROFILER,
        ciPlatform = "GitLab CI",
        codeExample = """.leakcanary:
  script:
    - ./gradlew :app:leakcanaryAnalyzeDebug
  artifacts:
    when: always
    paths:
      - app/build/reports/leakcanary/
    expire_in: 1 week

 leak_check:
   stage: test
   extends: .leakcanary
   allow_failure: false""",
        notes = "⚠️ 建议设置 allow_failure: true 避免阻塞发布，allow_failure: false 阻塞构建。"
    ),
    CIIntegrationItem(
        id = "ci-004",
        title = "Jenkins + LeakCanary",
        description = "Jenkins CI 环境下 LeakCanary 集成，配置 HTML 报告展示。",
        toolType = ToolType.LEAKCANARY_PROFILER,
        ciPlatform = "Jenkins",
        codeExample = """// Jenkinsfile
pipeline {
    stages {
        stage('LeakCanary') {
            steps {
                sh './gradlew :app:leakcanaryAnalyzeDebug'
            }
            post {
                always {
                    archive 'app/build/reports/leakcanary/**'
                }
            }
        }
    }
}""",
        notes = "💡 Jenkins 中可以使用 HTML Publisher 插件展示 LeakCanary 报告。"
    )
)

/**
 * ============================================================
 * SIMULATED_VERSION_COMPARISON_ITEMS — 版本对比
 * ============================================================
 */
val SIMULATED_VERSION_COMPARISON_ITEMS = listOf(
    VersionComparisonItem(
        id = "comp-001",
        feature = "Recomposition Observe Node",
        quailCapability = "✅ Quail 独有（Canary 4+）",
        pandaCapability = "❌ 无此功能",
        recommendation = "需要 recomposition 调试 → 使用 Quail",
        emoji = "🔍"
    ),
    VersionComparisonItem(
        id = "comp-002",
        feature = "LeakCanary in Profiler",
        quailCapability = "✅ Quail 独有（首次集成进 Profiler）",
        pandaCapability = "❌ 无此功能",
        recommendation = "需要 IDE 内 LeakCanary → 使用 Quail",
        emoji = "🔧"
    ),
    VersionComparisonItem(
        id = "comp-003",
        feature = "内存 Profiling",
        quailCapability = "✅ Profiler 功能完整（但 Canary 版本可能不稳定）",
        pandaCapability = "✅ Profiler 功能稳定（成熟版本）",
        recommendation = "生产环境调试 → 使用 Panda 稳定版",
        emoji = "🐼"
    ),
    VersionComparisonItem(
        id = "comp-004",
        feature = "CPU Profiling",
        quailCapability = "✅ 完整支持",
        pandaCapability = "✅ 完整支持",
        recommendation = "两者均可",
        emoji = "⚖️"
    ),
    VersionComparisonItem(
        id = "comp-005",
        feature = "Compose Preview Screenshot Testing",
        quailCapability = "✅ Quail 独有（新功能）",
        pandaCapability = "❌ 无此功能",
        recommendation = "需要官方截图测试 → 使用 Quail",
        emoji = "📸"
    ),
    VersionComparisonItem(
        id = "comp-006",
        feature = "AQI Fix with AI",
        quailCapability = "✅ Quail 独有（Gemini Crash 修复）",
        pandaCapability = "⚠️ Otter 版本仅支持 explain，不支持 fix",
        recommendation = "需要 AI Crash 修复 → 使用 Quail",
        emoji = "🤖"
    ),
    VersionComparisonItem(
        id = "comp-007",
        feature = "稳定性",
        quailCapability = "⚠️ Canary 版本，功能新但可能不稳定",
        pandaCapability = "✅ 稳定版，经过充分测试",
        recommendation = "日常开发 → 使用 Panda；尝鲜新功能 → 使用 Quail Canary",
        emoji = "⚖️"
    )
)
