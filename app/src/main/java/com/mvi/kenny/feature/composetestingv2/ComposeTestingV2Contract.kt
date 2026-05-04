package com.mvi.kenny.feature.composetestingv2

// ================================================================
// ComposeTestingV2Contract — Jetpack Compose 1.11 Testing v2 API 迁移工具包 MVI 契约
// ================================================================
// MVI architecture contract for Compose Testing v2 Migration Toolkit.
//
// PRD-228: Jetpack Compose 1.11 Testing v2 API 迁移工具包
// Design: memory/agency/designs/PRD-228-Jetpack-Compose-Testing-v2-API-迁移工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (clipboard, toast), via Channel
// ================================================================
//
// Tab Structure:
//   Tab 0: v2 扫描器 — Detect composeTestRule/runTest/UnconfinedTestDispatcher usage
//   Tab 1: Dispatcher 指南 — UnconfinedTestDispatcher vs StandardTestDispatcher behavior diff
//   Tab 2: Espresso 协调 — launchFragmentInContainer + FragmentComposeRule migration
//   Tab 3: CI 合规检测 — Gradle plugin compliance + transition compat + audit checklist
//   Tab 4: KMP + 迁移指南 — Compose Multiplatform v2 + composeTestRule → createComposeRule
// ================================================================
//
// NOTE: This is a local simulation tool. It does NOT scan the actual project.
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// Risk Level & Status Enums / 风险等级 & 状态枚举
// ================================================================

/**
 * ============================================================
 * RiskLevel — 风险等级枚举
 * ============================================================
 */
enum class RiskLevel(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    P0_CRITICAL("P0 严重", "🔴", Color(0xFFF85149)),
    P1_HIGH("P1 高风险", "🟠", Color(0xFFD29922)),
    P2_MEDIUM("P2 中风险", "🟡", Color(0xFF3FB950)),
    P3_LOW("P3 低风险", "🟢", Color(0xFF6BCF7F)),
    UNKNOWN("未知", "⚪", Color(0xFF8B949E))
}

/**
 * ============================================================
 * CompatibilityStatus — 兼容性状态枚举
 * ============================================================
 */
enum class CompatibilityStatus(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    COMPATIBLE("已兼容 v2", "✅", Color(0xFF3FB950)),
    NEEDS_MIGRATION("需迁移", "⚠️", Color(0xFFD29922)),
    INCOMPATIBLE("不兼容", "🚨", Color(0xFFF85149)),
    UNKNOWN("未检测", "❓", Color(0xFF8B949E))
}

/**
 * ============================================================
 * Platform — 多平台目标枚举
 * ============================================================
 */
enum class Platform(val displayName: String, val icon: String) {
    ANDROID("Android", "🤖"),
    IOS("iOS", "🍎"),
    JS("JavaScript/WASM", "🌐"),
    DESKTOP("Desktop JVM", "🖥️")
}

// ================================================================
// State / 状态
// ================================================================

/**
 * ============================================================
 * ComposeTestingV2State — Compose Testing v2 迁移工具页面状态（MVI State）
 * ============================================================
 *
 * @param selectedTab 当前 Tab 索引 (0-4)
 * @param isScanning 是否正在扫描
 * @param scanProgress 扫描进度 0.0~1.0
 * @param scanProgressText 扫描进度文本
 *
 * Tab 0 — v2 扫描器
 * @param scanResults v2 API 扫描结果列表
 * @param overallStatus 整体兼容性状态
 *
 * Tab 1 — Dispatcher 指南
 * @param dispatcherGuideItems Dispatcher 行为对比指南列表
 *
 * Tab 2 — Espresso 协调
 * @param espressoGuideItems Espresso 协调指南列表
 *
 * Tab 3 — CI 合规检测
 * @param ciChecklistItems CI 检查清单列表
 * @param ciCompliancePassed 已通过项数
 * @param ciComplianceFailed 未通过项数（仅 P0）
 * @param ciComplianceWarning 警告项数（非 P0 未通过）
 * @param overallRiskLevel 整体风险等级
 *
 * Tab 4 — KMP + 迁移指南
 * @param kmpGuideItems KMP 指南列表
 * @param migrationPathItems 迁移路径列表
 *
 * @param snackbarMessage Snackbar 消息
 */
data class ComposeTestingV2State(
    // ── 全局状态 ───────────────────────────────────────────────
    val selectedTab: Int = 0,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanProgressText: String = "",

    // ── Tab 0: v2 扫描器 ───────────────────────────────────
    val scanResults: List<ScanIssue> = emptyList(),
    val overallStatus: CompatibilityStatus = CompatibilityStatus.UNKNOWN,

    // ── Tab 1: Dispatcher 指南 ──────────────────────────────
    val dispatcherGuideItems: List<DispatcherGuideItem> = emptyList(),

    // ── Tab 2: Espresso 协调 ─────────────────────────────────
    val espressoGuideItems: List<EspressoGuideItem> = emptyList(),

    // ── Tab 3: CI 合规检测 ──────────────────────────────────
    val ciChecklistItems: List<CIChecklistItem> = emptyList(),
    val ciCompliancePassed: Int = 0,
    val ciComplianceFailed: Int = 0,
    val ciComplianceWarning: Int = 0,
    val overallRiskLevel: RiskLevel = RiskLevel.UNKNOWN,

    // ── Tab 4: KMP + 迁移指南 ────────────────────────────────
    val kmpGuideItems: List<KMPGuideItem> = emptyList(),
    val migrationPathItems: List<MigrationPathItem> = emptyList(),

    // ── 全局 ─────────────────────────────────────────────────
    val snackbarMessage: String? = null
) {
    companion object {
        val Initial = ComposeTestingV2State()
    }
}

// ================================================================
// Intent / 用户意图
// ================================================================

/**
 * ============================================================
 * ComposeTestingV2Intent — 用户意图（User Intent）
 * ============================================================
 */
sealed interface ComposeTestingV2Intent {

    /** 用户切换主 Tab
     * @param index Tab 索引 (0-4)
     */
    data class SelectTab(val index: Int) : ComposeTestingV2Intent

    /** 用户触发 v2 API 扫描
     * @param simulate 是否使用模拟数据
     */
    data class RunV2Scan(val simulate: Boolean = false) : ComposeTestingV2Intent

    /** 用户复制修复后代码
     * @param code 要复制的代码
     */
    data class CopyFixCode(val code: String) : ComposeTestingV2Intent

    /** 用户切换 Checklist 条目状态
     * @param itemId 条目 ID
     * @param checked 是否勾选
     */
    data class ToggleChecklistItem(val itemId: String, val checked: Boolean) : ComposeTestingV2Intent

    /** 用户关闭 Snackbar */
    data object DismissSnackbar : ComposeTestingV2Intent

    /** 用户重置所有状态 */
    data object ResetAll : ComposeTestingV2Intent
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * ============================================================
 * ComposeTestingV2Effect — 一次性副作用（Effect）
 * ============================================================
 */
sealed interface ComposeTestingV2Effect {

    /** 复制到剪贴板
     * @param content 要复制的内容
     */
    data class CopyToClipboard(val content: String) : ComposeTestingV2Effect

    /** 显示 Snackbar
     * @param message 消息文本
     */
    data class ShowSnackbar(val message: String) : ComposeTestingV2Effect
}

// ================================================================
// Data Models / 数据模型
// ================================================================

/**
 * ============================================================
 * ScanIssue — v2 API 扫描问题条目
 * ============================================================
 *
 * @param id 唯一 ID
 * @param filePath 文件路径
 * @param lineNumber 行号
 * @param issueType 问题类型（composeTestRule/runTest/UnconfinedTestDispatcher）
 * @param codeSnippet 问题代码片段
 * @param fixSnippet 修复后代码片段
 * @param riskLevel 风险等级
 * @param status 兼容性状态
 */
data class ScanIssue(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val issueType: String,
    val description: String,
    val codeSnippet: String,
    val fixSnippet: String,
    val riskLevel: RiskLevel,
    val status: CompatibilityStatus
)

/**
 * ============================================================
 * DispatcherGuideItem — Dispatcher 行为对比指南条目
 * ============================================================
 *
 * @param id 唯一 ID
 * @param title 标题（中文）
 * @param titleEn 标题（英文）
 * @param description 描述
 * @param v1Behavior v1 行为（UnconfinedTestDispatcher）
 * @param v2Behavior v2 行为（StandardTestDispatcher）
 * @param timingDiagramV1 v1 时序图描述
 * @param timingDiagramV2 v2 时序图描述
 * @param failureScenario 典型失败场景
 * @param fixGuidance 修复指导
 * @param riskLevel 风险等级
 */
data class DispatcherGuideItem(
    val id: String,
    val title: String,
    val titleEn: String,
    val description: String,
    val v1Behavior: String,
    val v2Behavior: String,
    val timingDiagramV1: String,
    val timingDiagramV2: String,
    val failureScenario: String,
    val fixGuidance: String,
    val riskLevel: RiskLevel = RiskLevel.P1_HIGH
)

/**
 * ============================================================
 * EspressoGuideItem — Espresso 协调指南条目
 * ============================================================
 *
 * @param id 唯一 ID
 * @param title 标题（中文）
 * @param titleEn 标题（英文）
 * @param description 描述
 * @param beforeCode v1 代码示例
 * @param afterCode v2 代码示例
 * @param steps 迁移步骤列表
 * @param stepsCn 中文迁移步骤列表
 * @param notes 注意事项
 * @param riskLevel 风险等级
 */
data class EspressoGuideItem(
    val id: String,
    val title: String,
    val titleEn: String,
    val description: String,
    val beforeCode: String,
    val afterCode: String,
    val steps: List<String>,
    val stepsCn: List<String>,
    val notes: String = "",
    val riskLevel: RiskLevel = RiskLevel.P1_HIGH
)

/**
 * ============================================================
 * CIChecklistItem — CI 合规检查清单条目
 * ============================================================
 *
 * @param id 条目 ID
 * @param title 检查项标题
 * @param description 检查项描述
 * @param riskLevel 风险等级
 * @param isChecked 是否已勾选
 * @param category 类别（Scanner/Dispatcher/Espresso/KMP/Plugin）
 */
data class CIChecklistItem(
    val id: String,
    val title: String,
    val description: String,
    val riskLevel: RiskLevel,
    val isChecked: Boolean = false,
    val category: String
)

/**
 * ============================================================
 * KMPGuideItem — Compose Multiplatform v2 指南条目
 * ============================================================
 *
 * @param id 唯一 ID
 * @param title 标题（中文）
 * @param titleEn 标题（英文）
 * @param description 描述
 * @param platform 目标平台
 * @param specialNote 平台特殊注意事项
 * @param codeExample 代码示例
 * @param riskLevel 风险等级
 */
data class KMPGuideItem(
    val id: String,
    val title: String,
    val titleEn: String,
    val description: String,
    val platform: Platform,
    val specialNote: String,
    val codeExample: String,
    val riskLevel: RiskLevel = RiskLevel.P2_MEDIUM
)

/**
 * ============================================================
 * MigrationPathItem — 完整迁移路径条目
 * ================================================================
 *
 * @param id 唯一 ID
 * @param stepNumber 步骤编号
 * @param title 标题
 * @param description 描述
 * @param codeExample 代码示例（可选）
 * @param estimatedTime 预估耗时
 * @param riskLevel 风险等级
 */
data class MigrationPathItem(
    val id: String,
    val stepNumber: Int,
    val title: String,
    val description: String,
    val codeExample: String = "",
    val estimatedTime: String,
    val riskLevel: RiskLevel = RiskLevel.P1_HIGH
)

// ================================================================
// Simulated Scan Results / 模拟扫描结果
// ================================================================

/**
 * ============================================================
 * SIMULATED_SCAN_RESULTS — 模拟 v2 API 扫描结果
 * ============================================================
 */
val SIMULATED_SCAN_RESULTS = listOf(
    ScanIssue(
        id = "scan-001",
        filePath = "app/src/androidTest/java/com/example/app/compose/LoginScreenTest.kt",
        lineNumber = 24,
        issueType = "composeTestRule",
        description = "composeTestRule 已废弃，需迁移到 createComposeRule()",
        codeSnippet = """@get:Rule
val composeTestRule = composeTestRule

@Test
fun loginScreen_displaysFields() {
    composeTestRule.setContent { LoginScreen() }
    composeTestRule.onNodeWithText("Login").assertIsDisplayed()
}""",
        fixSnippet = """import androidx.compose.ui.test.createComposeRule

@get:Rule
val composeTestRule = createComposeRule()

@Test
fun loginScreen_displaysFields() {
    composeTestRule.setContent { LoginScreen() }
    composeTestRule.onNodeWithText("Login").assertIsDisplayed()
}""",
        riskLevel = RiskLevel.P0_CRITICAL,
        status = CompatibilityStatus.NEEDS_MIGRATION
    ),
    ScanIssue(
        id = "scan-002",
        filePath = "app/src/androidTest/java/com/example/app/compose/UserProfileTest.kt",
        lineNumber = 31,
        issueType = "UnconfinedTestDispatcher",
        description = "UnconfinedTestDispatcher 在 v2 中被 StandardTestDispatcher 替代，行为改变",
        codeSnippet = """@OptIn(ExperimentalCoroutinesApi::class)
@Test
fun userProfile_loadsData() = runTest {
    val dispatcher = UnconfinedTestDispatcher()
    val viewModel = UserViewModel(dispatcher)
    // fire-and-forget in v1: task executes immediately
    viewModel.loadUser("user123")
    // v1: task already done here
    assert(viewModel.uiState.value.user != null)
}""",
        fixSnippet = """@OptIn(ExperimentalTestApi::class)
@Test
fun userProfile_loadsData() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val viewModel = UserViewModel(dispatcher)
    viewModel.loadUser("user123")
    // v2: task is queued, advanceClock to process
    dispatcher.testScheduler.advanceTimeBy(1000)
    dispatcher.testScheduler.runCurrent()
    assert(viewModel.uiState.value.user != null)
}""",
        riskLevel = RiskLevel.P0_CRITICAL,
        status = CompatibilityStatus.NEEDS_MIGRATION
    ),
    ScanIssue(
        id = "scan-003",
        filePath = "app/src/androidTest/java/com/example/app/compose/ProductListTest.kt",
        lineNumber = 18,
        issueType = "runTest",
        description = "runTest 块内协程行为改变：v1 立即执行，v2 队列等待调度",
        codeSnippet = """@Test
fun productList_showsItems() = runTest {
    // v1: fire-and-forget — launches coroutine immediately
    val job = launch { viewModel.loadProducts() }
    // v1: job may have already completed here
    // This assertion might flakily fail in v2
    assert(productList.isNotEmpty())
    job.cancel()
}""",
        fixSnippet = """@Test
fun productList_showsItems() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    // Explicitly use StandardTestDispatcher
    val viewModel = ProductViewModel(testDispatcher)
    testDispatcher.testScheduler.advanceTimeBy(100)
    testDispatcher.testScheduler.runCurrent()
    assert(productList.isNotEmpty())
}""",
        riskLevel = RiskLevel.P1_HIGH,
        status = CompatibilityStatus.NEEDS_MIGRATION
    ),
    ScanIssue(
        id = "scan-004",
        filePath = "app/src/androidTest/java/com/example/app/compose/NavigationTest.kt",
        lineNumber = 42,
        issueType = "composeTestRule",
        description = "composeTestRule.waitForAtLeast (v1 特有) 在 v2 中行为不同",
        codeSnippet = """@Test
fun navigation_navigatesToDetail() {
    composeTestRule.setContent { AppNavHost() }
    composeTestRule.onNodeWithText("Settings").performClick()
    // v1: waitForAtLeast waits for condition
    composeTestRule.waitForAtLeast(500)
    composeTestRule.onNodeWithText("Settings Detail").assertExists()
}""",
        fixSnippet = """@Test
fun navigation_navigatesToDetail() {
    composeTestRule.setContent { AppNavHost() }
    composeTestRule.onNodeWithText("Settings").performClick()
    // v2: use runCurrent() to process queued tasks
    composeTestRule.mainTestScheduler.runCurrent()
    composeTestRule.onNodeWithText("Settings Detail").assertExists()
}""",
        riskLevel = RiskLevel.P1_HIGH,
        status = CompatibilityStatus.NEEDS_MIGRATION
    ),
    ScanIssue(
        id = "scan-005",
        filePath = "app/src/androidTest/java/com/example/app/compose/ThemeTest.kt",
        lineNumber = 15,
        issueType = "runTest (legacy)",
        description = "旧版 runTest 用法，在 Compose 1.11 Testing v2 中需要更新",
        codeSnippet = """// Legacy runTest — works in v1, may cause flakiness in v2
@Test
fun theme_appliesDarkMode() {
    runTest {
        val vm = ThemeViewModel()
        vm.toggleDarkMode()
        // v1: coroutine fires immediately
        // v2: queued, may not execute before assertion
    }
}""",
        fixSnippet = """// v2 runTest with StandardTestDispatcher
@OptIn(ExperimentalTestApi::class)
@Test
fun theme_appliesDarkMode() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val vm = ThemeViewModel(testDispatcher)
    vm.toggleDarkMode()
    testDispatcher.testScheduler.runCurrent()
    // Now the assertion is deterministic
    assert(vm.isDarkMode.value)
}""",
        riskLevel = RiskLevel.P2_MEDIUM,
        status = CompatibilityStatus.NEEDS_MIGRATION
    )
)

/**
 * ============================================================
 * SIMULATED_DISPATCHER_GUIDE_ITEMS — 模拟 Dispatcher 指南
 * ============================================================
 */
val SIMULATED_DISPATCHER_GUIDE_ITEMS = listOf(
    DispatcherGuideItem(
        id = "disp-001",
        title = "协程执行时序：立即执行 vs 队列调度",
        titleEn = "Coroutine Execution Timing: Immediate vs Queued",
        description = "v1 UnconfinedTestDispatcher：协程任务立即执行（fire-and-forget）。v2 StandardTestDispatcher：任务进入队列，等待显式调度（advanceTimeBy/runCurrent）。这是导致测试 flaky 的根本原因。",
        v1Behavior = "UnconfinedTestDispatcher — 协程任务立即执行，不受 testScheduler 控制",
        v2Behavior = "StandardTestDispatcher — 任务进入队列，等待 testScheduler.advanceTimeBy() 或 runCurrent() 调度",
        timingDiagramV1 = "[Test Thread] ── launch{} ──立即执行──> [Task completes instantly]",
        timingDiagramV2 = "[Test Thread] ── launch{} ──进入队列──> [waiting] ──advanceTimeBy──> [Task executes]",
        failureScenario = "基于 v1 时序的测试（如 `viewModel.loadData(); assert(data != null)`）在 v2 下会因任务未完成而 flaky 失败。",
        fixGuidance = "使用 StandardTestDispatcher + advanceTimeBy/runCurrent 控制任务执行时机，或在 ViewModel 注入 TestDispatcher。",
        riskLevel = RiskLevel.P0_CRITICAL
    ),
    DispatcherGuideItem(
        id = "disp-002",
        title = "Test Scheduler 控制权",
        titleEn = "Test Scheduler Control",
        description = "v1 下 UnconfinedTestDispatcher 不受 testScheduler 控制，测试时间完全依赖实际执行速度。v2 下 StandardTestDispatcher 由 testScheduler 统一调度，测试时间完全可控。",
        v1Behavior = "UnconfinedTestDispatcher — testScheduler 无法控制任务执行时间",
        v2Behavior = "StandardTestDispatcher — testScheduler.advanceTimeBy() 控制虚拟时间流逝",
        timingDiagramV1 = "[real time] ── task runs at CPU speed ── unpredictable",
        timingDiagramV2 = "[virtual time] ── advanceTimeBy(1000) ── virtual clock = 1000ms ── tasks scheduled at 1000ms execute",
        failureScenario = "依赖 Thread.sleep() 或 real-time waits 的测试在 v2 下完全失效（v2 使用虚拟时间）。",
        fixGuidance = "用 testScheduler.advanceTimeBy(milliseconds) 推进虚拟时钟，替代 Thread.sleep()。",
        riskLevel = RiskLevel.P1_HIGH
    ),
    DispatcherGuideItem(
        id = "disp-003",
        title = "ViewModel 协程作用域",
        titleEn = "ViewModel Coroutine Scope",
        description = "ViewModel.viewModelScope 在测试中使用 testDispatcher 时，所有 viewModelScope.launch{} 任务都通过该 dispatcher 调度。v1/v2 行为差异直接影响 ViewModel 内部协程的执行时机。",
        v1Behavior = "viewModelScope.launch {} 在 UnconfinedTestDispatcher 下立即启动，立即执行",
        v2Behavior = "viewModelScope.launch {} 在 StandardTestDispatcher 下进入队列，等待 testScheduler 调度",
        timingDiagramV1 = "viewModelScope.launch ──立即执行──> [suspend function body]",
        timingDiagramV2 = "viewModelScope.launch ──进入队列──> [waiting for scheduler] ──runCurrent()──> [suspend function body]",
        failureScenario = "测试中直接调用 viewModel 方法后立即断言结果，但 v2 下协程尚未执行完毕，导致 assertion fails。",
        fixGuidance = "在 ViewModel 中注入 TestDispatcher（依赖注入），测试时传入 StandardTestDispatcher，并在断言前调用 runCurrent()。",
        riskLevel = RiskLevel.P0_CRITICAL
    ),
    DispatcherGuideItem(
        id = "disp-004",
        title = "Flow 收集时序",
        titleEn = "Flow Collection Timing",
        description = "collect{} 作为 suspend 函数，在 v1 和 v2 下的调度行为不同。v1 UnconfinedTestDispatcher 立即执行，v2 StandardTestDispatcher 进入队列。",
        v1Behavior = "UnconfinedTestDispatcher — Flow collect{} 立即开始收集，无调度延迟",
        v2Behavior = "StandardTestDispatcher — Flow collect{} 进入队列，等待调度",
        timingDiagramV1 = "[collect{}] ──立即执行──> [emits flow values immediately]",
        timingDiagramV2 = "[collect{}] ──进入队列──> [waiting] ──runCurrent()──> [emits flow values]",
        failureScenario = "测试中 `viewModel.stateFlow.collect {}` 后立即断言 state 变化，但 v2 下 collect{} 尚未执行导致 assertion fails。",
        fixGuidance = "在 collect{} 之前调用 testScheduler.runCurrent() 确保队列中任务已执行。",
        riskLevel = RiskLevel.P1_HIGH
    ),
    DispatcherGuideItem(
        id = "disp-005",
        title = "过渡期兼容配置",
        titleEn = "Transition Period Compatibility",
        description = "如果迁移窗口期内需要同时支持 v1 和 v2，可以使用 ExperimentalTestApi 配置临时回退到 v1 behavior，但不推荐长期使用。",
        v1Behavior = "UnconfinedTestDispatcher (v1 默认) — 立即执行，不等待",
        v2Behavior = "StandardTestDispatcher (v2 默认) — 队列调度，可控",
        timingDiagramV1 = "[configure { v1Timeout() }] ──回退到 v1 行为──>",
        timingDiagramV2 = "[StandardTestDispatcher 默认] ──队列调度──>",
        failureScenario = "使用 configure{} 回退 v1 behavior 会使测试套件无法检测 v2 不兼容问题，不推荐。",
        fixGuidance = "短期过渡：使用 @OptIn(ExperimentalTestApi::class) 配置 v1 behavior；长期：完全迁移到 v2 StandardTestDispatcher。",
        riskLevel = RiskLevel.P2_MEDIUM
    )
)

/**
 * ============================================================
 * SIMULATED_ESPRESSO_GUIDE_ITEMS — 模拟 Espresso 协调指南
 * ============================================================
 */
val SIMULATED_ESPRESSO_GUIDE_ITEMS = listOf(
    EspressoGuideItem(
        id = "espresso-001",
        title = "launchFragmentInContainer 迁移",
        titleEn = "launchFragmentInContainer Migration",
        description = "Compose + Espresso 混合测试中，launchFragmentInContainer 在 Testing v2 下的配置需要更新，以协调 Compose 和 Fragment 生命周期。",
        beforeCode = """// ❌ v1 写法
@get:Rule
val composeTestRule = composeTestRule

@OptIn(ExperimentalCoroutinesApi::class)
@Test
fun fragment_inCompose_showsContent() {
    launchFragmentInContainer<MyFragment>(
        fragmentArgs = bundleOf("userId" to "123")
    )
    composeTestRule.setContent {
        MyComposeContent()
    }
    // v1: compose and espresso run concurrently
    Espresso.onView(withId(R.id.fragment_view))
        .check(matches(isDisplayed()))
}""",
        afterCode = """// ✅ v2 写法 — 需要协调 Compose TestRule 和 Fragment
@get:Rule
val composeTestRule = createComposeRule()

@OptIn(ExperimentalTestApi::class)
@Test
fun fragment_inCompose_showsContent() {
    // v2: 使用 TestActivity 托管 Fragment
    val scenario = launchFragmentInContainer<MyFragment>(
        fragmentArgs = bundleOf("userId" to "123"),
        factory = FragmentFactory()
    )

    // v2: 需要显式同步 Compose 和 Fragment
    scenario.onFragment { fragment ->
        composeTestRule.setContent {
            MaterialTheme {
                fragment.requireView()
            }
        }
    }

    // v2: 使用 runCurrent() 等待 Compose 任务完成
    composeTestRule.mainTestScheduler.runCurrent()
    Espresso.onView(withId(R.id.fragment_view))
        .check(matches(isDisplayed()))
}""",
        steps = listOf(
            "Replace composeTestRule with createComposeRule()",
            "Update launchFragmentInContainer to use TestActivity",
            "Synchronize Fragment lifecycle with composeTestRule.setContent",
            "Use mainTestScheduler.runCurrent() to flush pending Compose tasks",
            "Replace waitForAtLeast with runCurrent()"
        ),
        stepsCn = listOf(
            "将 composeTestRule 替换为 createComposeRule()",
            "更新 launchFragmentInContainer 使用 TestActivity",
            "将 Fragment 生命周期与 composeTestRule.setContent 同步",
            "使用 mainTestScheduler.runCurrent() 清空待处理的 Compose 任务",
            "将 waitForAtLeast 替换为 runCurrent()"
        ),
        notes = "v2 下 Compose 和 Espresso 任务都在同一 TestScheduler 上调度，需要显式同步。",
        riskLevel = RiskLevel.P1_HIGH
    ),
    EspressoGuideItem(
        id = "espresso-002",
        title = "FragmentComposeRule 迁移",
        titleEn = "FragmentComposeRule Migration",
        description = "FragmentComposeRule 在 Compose 1.11 Testing v2 中行为变化，需要使用 createComposeRule() 替代，并显式处理 Fragment-Compose 协调。",
        beforeCode = """// ❌ v1 FragmentComposeRule
@get:Rule
val composeTestRule = composeTestRule

// v1: composeTestRule provides Fragment integration
@Test
fun test() {
    composeTestRule.launchFragmentInContainer<MyComposeFragment>()
    composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
}""",
        afterCode = """// ✅ v2 FragmentComposeRule → createComposeRule + 手动协调
@get:Rule
val composeTestRule = createComposeRule()

@OptIn(ExperimentalTestApi::class)
@Test
fun test() {
    val scenario = composeTestRule.launchFragmentInContainer<MyComposeFragment>(
        factory = MyFragmentFactory()
    )

    // v2: 手动协调 Fragment 和 Compose 状态
    scenario.onFragment { fragment ->
        composeTestRule.setContent {
            ComposeView(fragment.requireView().findViewById(...))
        }
    }

    composeTestRule.mainTestScheduler.runCurrent()
    composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
}""",
        steps = listOf(
            "Replace FragmentComposeRule with createComposeRule()",
            "Use launchFragmentInContainer with explicit FragmentFactory",
            "In onFragment callback, call composeTestRule.setContent with ComposeView",
            "Call mainTestScheduler.runCurrent() to flush pending work",
            "Update assertions to use runCurrent() instead of waitForAtLeast"
        ),
        stepsCn = listOf(
            "将 FragmentComposeRule 替换为 createComposeRule()",
            "使用 launchFragmentInContainer 配合显式 FragmentFactory",
            "在 onFragment 回调中调用 composeTestRule.setContent 传入 ComposeView",
            "调用 mainTestScheduler.runCurrent() 清空待处理任务",
            "将断言中的 waitForAtLeast 替换为 runCurrent()"
        ),
        notes = "v2 下不再有自动的 Fragment-Compose 同步，需要手动协调。",
        riskLevel = RiskLevel.P1_HIGH
    ),
    EspressoGuideItem(
        id = "espresso-003",
        title = "同步时序问题（composeTestRule + Espresso）",
        titleEn = "Timing Synchronization (composeTestRule + Espresso)",
        description = "v1 下 Compose 和 Espresso 测试可以独立运行。v2 下所有任务共享同一 TestScheduler，需要显式同步否则 Espresso 会在 Compose UI 就绪前执行断言。",
        beforeCode = """// ❌ v1 时序：独立运行，可能偶发 flakiness
@Test
fun mixedTest() {
    composeTestRule.setContent { MyScreen() }
    composeTestRule.onNodeWithText("Submit").performClick()
    // v1: no explicit sync needed
    Espresso.onView(withId(R.id.result_text))
        .check(matches(withText("Success")))
}""",
        afterCode = """// ✅ v2 时序：显式同步
@Test
fun mixedTest() {
    composeTestRule.setContent { MyScreen() }
    composeTestRule.mainTestScheduler.runCurrent()
    composeTestRule.onNodeWithText("Submit").performClick()
    // v2: 必须等待 Compose 任务完成后再调用 Espresso
    composeTestRule.mainTestScheduler.runCurrent()
    Espresso.onView(withId(R.id.result_text))
        .check(matches(withText("Success")))
}""",
        steps = listOf(
            "After composeTestRule.setContent, call runCurrent()",
            "After each performClick/composed action, call runCurrent()",
            "Before Espresso assertions, call runCurrent() to flush Compose tasks",
            "Use advanceTimeBy() for async operations with delays"
        ),
        stepsCn = listOf(
            "在 composeTestRule.setContent 后调用 runCurrent()",
            "在每次 performClick/composed 操作后调用 runCurrent()",
            "在 Espresso 断言前调用 runCurrent() 清空 Compose 任务",
            "对于有延迟的异步操作，使用 advanceTimeBy()"
        ),
        notes = "v2 下每次 UI 交互后都需要 runCurrent()，否则下一个任务/断言可能在 UI 更新前执行。",
        riskLevel = RiskLevel.P0_CRITICAL
    )
)

/**
 * ============================================================
 * SIMULATED_CI_CHECKLIST — CI 合规检查清单
 * ============================================================
 */
val SIMULATED_CI_CHECKLIST = listOf(
    CIChecklistItem(
        id = "ci-001",
        title = "composeTestRule → createComposeRule() 迁移",
        description = "检测测试文件中是否已从 composeTestRule 迁移到 createComposeRule()",
        riskLevel = RiskLevel.P0_CRITICAL,
        category = "Scanner"
    ),
    CIChecklistItem(
        id = "ci-002",
        title = "UnconfinedTestDispatcher → StandardTestDispatcher 迁移",
        description = "检测是否使用 UnconfinedTestDispatcher（v1），应替换为 StandardTestDispatcher（v2）",
        riskLevel = RiskLevel.P0_CRITICAL,
        category = "Dispatcher"
    ),
    CIChecklistItem(
        id = "ci-003",
        title = "runTest 内协程显式调度",
        description = "确认 runTest {} 块内所有协程都通过 testScheduler 显式调度（advanceTimeBy/runCurrent）",
        riskLevel = RiskLevel.P0_CRITICAL,
        category = "Dispatcher"
    ),
    CIChecklistItem(
        id = "ci-004",
        title = "Espresso-Compose 同步（runCurrent）",
        description = "确认混合测试（Compose + Espresso）中每次 UI 操作后调用 runCurrent()",
        riskLevel = RiskLevel.P1_HIGH,
        category = "Espresso"
    ),
    CIChecklistItem(
        id = "ci-005",
        title = "launchFragmentInContainer v2 配置",
        description = "确认 launchFragmentInContainer 测试使用 v2 配置（createComposeRule + onFragment 同步）",
        riskLevel = RiskLevel.P1_HIGH,
        category = "Espresso"
    ),
    CIChecklistItem(
        id = "ci-006",
        title = "FragmentComposeRule v2 迁移",
        description = "确认 FragmentComposeRule 已迁移到 createComposeRule() + 手动协调模式",
        riskLevel = RiskLevel.P1_HIGH,
        category = "Espresso"
    ),
    CIChecklistItem(
        id = "ci-007",
        title = "KMP 项目 Testing v2 平台特定配置",
        description = "KMP 项目需验证非 Android 平台（iOS/JS/Desktop）已配置对应的 TestRunner",
        riskLevel = RiskLevel.P1_HIGH,
        category = "KMP"
    ),
    CIChecklistItem(
        id = "ci-008",
        title = "CI Gradle Plugin 合规",
        description = "确认 CI 已集成 compose-testing-v2-check Gradle 插件，阻塞未完全迁移的构建",
        riskLevel = RiskLevel.P2_MEDIUM,
        category = "Plugin"
    ),
    CIChecklistItem(
        id = "ci-009",
        title = "过渡期兼容配置清理",
        description = "确认已移除 configure { v1Timeout() } 等临时回退配置",
        riskLevel = RiskLevel.P2_MEDIUM,
        category = "Plugin"
    ),
    CIChecklistItem(
        id = "ci-010",
        title = "测试 flakiness 审计",
        description = "运行测试套件，确认无 flaky failures（v2 迁移后应全部稳定）",
        riskLevel = RiskLevel.P2_MEDIUM,
        category = "Scanner"
    )
)

/**
 * ============================================================
 * SIMULATED_KMP_GUIDE_ITEMS — 模拟 KMP v2 指南
 * ============================================================
 */
val SIMULATED_KMP_GUIDE_ITEMS = listOf(
    KMPGuideItem(
        id = "kmp-001",
        title = "非 Android 平台 Testing v2 支持",
        titleEn = "Non-Android Platform Testing v2 Support",
        description = "Compose Multiplatform 1.11.0-beta03 开始支持 Testing v2 API，但各平台配置不同。Android 使用 StandardTestDispatcher，iOS/JS/Desktop 有各自测试运行机制。",
        platform = Platform.ANDROID,
        specialNote = "Android: StandardTestDispatcher + TestScheduler; iOS/JS: 使用平台原生 TestRunner",
        codeExample = """// ✅ KMP 共享测试代码
// src/commonTest/kotlin/
@OptIn(ExperimentalTestApi::class)
@Test
fun sharedTest() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    launch {
        viewModel.loadData()
        testScheduler.runCurrent()
    }
    testScheduler.advanceTimeBy(1000)
    assert(viewModel.data != null)
}
""",
        riskLevel = RiskLevel.P1_HIGH
    ),
    KMPGuideItem(
        id = "kmp-002",
        title = "KMP iOS 测试配置",
        titleEn = "KMP iOS Test Configuration",
        description = "Compose Multiplatform iOS 测试使用 XCTest，与 Android Testing v2 API 不同。KMP 共享测试代码需要平台特定配置。",
        platform = Platform.IOS,
        specialNote = "iOS 使用 XCTest，不支持 StandardTestDispatcher；共享测试代码需要 @SkipOnPlatform(Platform.IOS) 注解",
        codeExample = """// ✅ KMP iOS 测试
// src/iosX64Test/kotlin/
class IosComposeTest {
    @Test
    fun testComposeList() {
        // iOS 上使用 XCTest，与 Android Testing v2 不同
        XCTAssertTrue(true)
    }
}

// ⚠️ KMP 共享测试代码示例
@SkipOnPlatform(Platform.IOS)
@Test
fun sharedTest() = runTest {
    // 仅在非 iOS 平台运行
}""",
        riskLevel = RiskLevel.P2_MEDIUM
    ),
    KMPGuideItem(
        id = "kmp-003",
        title = "KMP JS/WASM 测试配置",
        titleEn = "KMP JS/WASM Test Configuration",
        description = "Compose Multiplatform JS/WASM 测试使用 Kotlin/JS test runner，需要 kotlin-test 库配置。",
        platform = Platform.JS,
        specialNote = "JS/WASM 使用 kotlin-test-js，StandardTestDispatcher 不可用；使用 waitFor 替代 advanceTimeBy",
        codeExample = """// ✅ KMP JS 测试
// src/jsTest/kotlin/
@SkippableTest
@Test
fun jsComposeTest() {
    // JS 上使用 Kotlin/JS test runner
    assertEquals(true, true)
}

// ⚠️ KMP 共享测试中避免 StandardTestDispatcher
@SkipOnPlatform(Platform.JS)
@Test
fun sharedComposeTest() = runTest {
    // 仅在 Android/JVM 平台运行
}""",
        riskLevel = RiskLevel.P2_MEDIUM
    )
)

/**
 * ============================================================
 * SIMULATED_MIGRATION_PATH_ITEMS — 模拟迁移路径（参考 ViewModel 中的完整定义）
 * ============================================================
 */
// Note: Migration path items are defined in ComposeTestingV2ViewModel.kt
// This is a placeholder reference to avoid circular dependency issues.
typealias MigrationPathItemPlaceholder = Unit
