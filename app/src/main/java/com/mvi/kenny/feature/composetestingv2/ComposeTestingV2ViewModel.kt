package com.mvi.kenny.feature.composetestingv2

// ================================================================
// ComposeTestingV2ViewModel — Compose Testing v2 迁移工具 ViewModel
// ================================================================
// MVI ViewModel: receives Intent, processes business logic, updates State.
//
// PRD-228: Jetpack Compose 1.11 Testing v2 API 迁移工具包
// Design: memory/agency/designs/PRD-228-Jetpack-Compose-Testing-v2-API-迁移工具包.md
//
// Key responsibilities:
//   1. Scan project files for composeTestRule/runTest/UnconfinedTestDispatcher usage
//   2. Provide Dispatcher behavior comparison guide
//   3. Guide Espresso-Coordination migration
//   4. CI compliance checklist management
//   5. KMP Testing v2 migration guide
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ================================================================
// State & Effect Aliases
// ================================================================
typealias ComposeTestingV2UiState = ComposeTestingV2State
typealias ComposeTestingV2UiEffect = ComposeTestingV2Effect

// ================================================================
// ViewModel
// ================================================================

/**
 * ============================================================
 * ComposeTestingV2ViewModel — Compose Testing v2 迁移工具 ViewModel
 * ============================================================
 * MVI pattern: State is the single source of truth, Intent drives changes.
 *
 * NOTE: This is a local simulation tool. All scans use preset data.
 * Real file scanning requires access to the actual project directory.
 */
class ComposeTestingV2ViewModel(
    private val initialState: ComposeTestingV2State = ComposeTestingV2State()
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<ComposeTestingV2State> = _state.asStateFlow()

    // ── Effects ───────────────────────────────────────────────
    private val _effect = MutableSharedFlow<ComposeTestingV2Effect>()
    val effect: MutableSharedFlow<ComposeTestingV2Effect> = _effect

    // ==========================================================
    // Public API
    // ==========================================================

    /**
     * Process user intent / 处理用户意图
     * @param intent User action intent
     */
    fun sendIntent(intent: ComposeTestingV2Intent) {
        when (intent) {
            is ComposeTestingV2Intent.SelectTab ->
                _state.update { it.copy(selectedTab = intent.index) }

            is ComposeTestingV2Intent.RunV2Scan ->
                runV2Scan(simulate = intent.simulate)

            is ComposeTestingV2Intent.CopyFixCode ->
                copyFixCode(intent.code)

            is ComposeTestingV2Intent.ToggleChecklistItem ->
                toggleChecklistItem(intent.itemId, intent.checked)

            is ComposeTestingV2Intent.DismissSnackbar ->
                _state.update { it.copy(snackbarMessage = null) }

            is ComposeTestingV2Intent.ResetAll ->
                _state.update { ComposeTestingV2State.Initial }
        }
    }

    // ==========================================================
    // v2 Scan Logic / v2 扫描逻辑
    // ==========================================================

    /**
     * Run v2 API scan — scan project for deprecated Testing v1 APIs
     * 运行 v2 API 扫描 — 扫描项目中废弃的 Testing v1 API
     *
     * NOTE: This implementation uses simulated data.
     * Real scanning would use ProcessBuilder + grep on project files.
     */
    private fun runV2Scan(simulate: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    scanProgress = 0f,
                    scanProgressText = "Initializing Compose Testing v2 scan..."
                )
            }

            if (simulate) {
                runSimulatedScan()
            } else {
                // Real scan not implemented — fallback to simulation
                runSimulatedScan()
            }
        }
    }

    /**
     * Simulated v2 scan with preset data
     * 模拟 v2 扫描（使用预设数据）
     */
    private suspend fun runSimulatedScan() {
        val phases = listOf(
            "Scanning project files..." to 0.10f,
            "Detecting composeTestRule usage..." to 0.30f,
            "Detecting UnconfinedTestDispatcher..." to 0.50f,
            "Analyzing runTest coroutine behavior..." to 0.70f,
            "Categorizing issues by risk level..." to 0.90f,
            "Complete!" to 1.0f
        )

        for ((text, progress) in phases) {
            _state.update { it.copy(scanProgressText = text, scanProgress = progress) }
            delay(350)
        }

        val results = SIMULATED_SCAN_RESULTS
        val criticalCount = results.count { it.riskLevel == RiskLevel.P0_CRITICAL }
        val status = when {
            criticalCount > 0 -> CompatibilityStatus.INCOMPATIBLE
            results.isNotEmpty() -> CompatibilityStatus.NEEDS_MIGRATION
            else -> CompatibilityStatus.COMPATIBLE
        }

        _state.update {
            it.copy(
                isScanning = false,
                scanProgress = 1f,
                scanProgressText = "Scan complete!",
                scanResults = results,
                overallStatus = status,
                snackbarMessage = "Found ${results.size} issues (${criticalCount} P0 critical)"
            )
        }

        _effect.emit(ComposeTestingV2Effect.ShowSnackbar(
            "Found ${results.size} issues (${criticalCount} P0 critical)"
        ))
    }

    // ==========================================================
    // Load Guide Data / 加载指南数据
    // ==========================================================

    /**
     * Load Dispatcher guide items
     * 加载 Dispatcher 指南条目
     */
    private fun loadDispatcherGuide() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    scanProgressText = "Loading Dispatcher behavior guide..."
                )
            }
            delay(500)
            _state.update {
                it.copy(
                    isScanning = false,
                    dispatcherGuideItems = SIMULATED_DISPATCHER_GUIDE_ITEMS
                )
            }
        }
    }

    /**
     * Load Espresso coordination guide items
     * 加载 Espresso 协调指南条目
     */
    private fun loadEspressoGuide() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    scanProgressText = "Loading Espresso coordination guide..."
                )
            }
            delay(500)
            _state.update {
                it.copy(
                    isScanning = false,
                    espressoGuideItems = SIMULATED_ESPRESSO_GUIDE_ITEMS
                )
            }
        }
    }

    /**
     * Load CI checklist items
     * 加载 CI 检查清单
     */
    private fun loadCIChecklist() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    scanProgressText = "Loading CI compliance checklist..."
                )
            }
            delay(400)
            val items = SIMULATED_CI_CHECKLIST
            val passed = items.count { it.isChecked }
            val failed = items.count { !it.isChecked && it.riskLevel == RiskLevel.P0_CRITICAL }
            val warning = items.count { !it.isChecked && it.riskLevel != RiskLevel.P0_CRITICAL }

            val overallRisk = when {
                failed > 0 -> RiskLevel.P0_CRITICAL
                warning > 0 -> RiskLevel.P1_HIGH
                passed == items.size -> RiskLevel.P3_LOW
                else -> RiskLevel.UNKNOWN
            }

            _state.update {
                it.copy(
                    isScanning = false,
                    ciChecklistItems = items,
                    ciCompliancePassed = passed,
                    ciComplianceFailed = failed,
                    ciComplianceWarning = warning,
                    overallRiskLevel = overallRisk
                )
            }
        }
    }

    /**
     * Load KMP guide items
     * 加载 KMP 指南条目
     */
    private fun loadKMPGuide() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    scanProgressText = "Loading KMP Testing v2 guide..."
                )
            }
            delay(400)
            _state.update {
                it.copy(
                    isScanning = false,
                    kmpGuideItems = SIMULATED_KMP_GUIDE_ITEMS,
                    migrationPathItems = SIMULATED_MIGRATION_PATH_ITEMS
                )
            }
        }
    }

    // ==========================================================
    // Copy to Clipboard / 复制到剪贴板
    // ==========================================================

    private fun copyFixCode(code: String) {
        viewModelScope.launch {
            _effect.emit(ComposeTestingV2Effect.CopyToClipboard(code))
            _effect.emit(ComposeTestingV2Effect.ShowSnackbar("Code copied to clipboard!"))
        }
    }

    // ==========================================================
    // Checklist Toggle / 检查清单切换
    // ==========================================================

    private fun toggleChecklistItem(itemId: String, checked: Boolean) {
        _state.update { currentState ->
            val updatedItems = currentState.ciChecklistItems.map { item ->
                if (item.id == itemId) item.copy(isChecked = checked) else item
            }
            val passed = updatedItems.count { it.isChecked }
            val failed = updatedItems.count { !it.isChecked && it.riskLevel == RiskLevel.P0_CRITICAL }
            val warning = updatedItems.count { !it.isChecked && it.riskLevel != RiskLevel.P0_CRITICAL }

            val overallRisk = when {
                failed > 0 -> RiskLevel.P0_CRITICAL
                warning > 0 -> RiskLevel.P1_HIGH
                passed == updatedItems.size -> RiskLevel.P3_LOW
                else -> RiskLevel.UNKNOWN
            }

            currentState.copy(
                ciChecklistItems = updatedItems,
                ciCompliancePassed = passed,
                ciComplianceFailed = failed,
                ciComplianceWarning = warning,
                overallRiskLevel = overallRisk
            )
        }
    }

    // ==========================================================
    // Tab Loading Logic / Tab 加载逻辑
    // ==========================================================

    /**
     * Load data for the selected tab
     * 加载选中 Tab 的数据
     */
    fun loadTabData(tabIndex: Int) {
        when (tabIndex) {
            0 -> runV2Scan(simulate = true)
            1 -> loadDispatcherGuide()
            2 -> loadEspressoGuide()
            3 -> loadCIChecklist()
            4 -> loadKMPGuide()
        }
    }
}

// ================================================================
// Simulated Migration Path Items / 模拟迁移路径条目
// ================================================================

/**
 * ============================================================
 * SIMULATED_MIGRATION_PATH_ITEMS — 完整迁移路径
 * ============================================================
 */
val SIMULATED_MIGRATION_PATH_ITEMS = listOf(
    MigrationPathItem(
        id = "path-001",
        stepNumber = 1,
        title = "Audit Test Suite — 审计现有测试套件",
        description = "使用本工具的 v2 扫描器功能，扫描项目中所有 Testing v1 API 使用情况（composeTestRule/UnconfinedTestDispatcher/runTest）。统计 P0/P1/P2 问题数量，制定迁移计划。",
        codeExample = """// 在本工具 Tab 0 点击「开始扫描」获取完整问题清单
// 问题按风险等级分类:
// - P0: 必须立即迁移（composeTestRule废弃、UnconfinedTestDispatcher行为变化）
// - P1: 需要迁移（runTest时序变化、Espresso同步）
// - P2: 建议迁移（过渡期配置、flakiness）""",
        estimatedTime = "30 分钟",
        riskLevel = RiskLevel.P0_CRITICAL
    ),
    MigrationPathItem(
        id = "path-002",
        stepNumber = 2,
        title = "Migrate composeTestRule → createComposeRule()",
        description = "将所有 @get:Rule val composeTestRule = composeTestRule 替换为 createComposeRule()。这是最直接的 Breaking Change，composeTestRule 在 Compose 1.11 Testing v2 中已完全废弃。",
        codeExample = """// ❌ Before (v1)
import androidx.compose.ui.test.junit4.composeTestRule

@get:Rule
val composeTestRule = composeTestRule

// ✅ After (v2)
import androidx.compose.ui.test.createComposeRule

@get:Rule
val composeTestRule = createComposeRule()""",
        estimatedTime = "1-2 小时（取决于测试文件数量）",
        riskLevel = RiskLevel.P0_CRITICAL
    ),
    MigrationPathItem(
        id = "path-003",
        stepNumber = 3,
        title = "Migrate UnconfinedTestDispatcher → StandardTestDispatcher",
        description = "将所有 UnconfinedTestDispatcher 替换为 StandardTestDispatcher，并添加 testScheduler.advanceTimeBy()/runCurrent() 显式调度。",
        codeExample = """// ❌ Before (v1) — fire-and-forget
val dispatcher = UnconfinedTestDispatcher()
viewModel.loadData()
// v1: 可能已经执行完毕

// ✅ After (v2) — queued + explicit scheduling
val dispatcher = StandardTestDispatcher(testScheduler)
viewModel.loadData()
testScheduler.runCurrent() // 显式执行队列任务
// v2: 确定任务已执行""",
        estimatedTime = "2-4 小时",
        riskLevel = RiskLevel.P0_CRITICAL
    ),
    MigrationPathItem(
        id = "path-004",
        stepNumber = 4,
        title = "Update runTest {} Block Coroutine Scheduling",
        description = "在所有 runTest {} 块内，排查直接调用协程后立即断言的代码模式，改用 testScheduler.runCurrent() 等待任务完成。",
        codeExample = """// ❌ v1 fire-and-forget pattern (flaky in v2)
@Test
fun test() = runTest {
    viewModel.loadData()
    // v1: 可能已完成
    assert(viewModel.state.value.isLoaded)
}

// ✅ v2 explicit scheduling
@Test
fun test() = runTest {
    viewModel.loadData()
    testScheduler.runCurrent() // 确保任务完成
    assert(viewModel.state.value.isLoaded)
}""",
        estimatedTime = "1-3 小时",
        riskLevel = RiskLevel.P1_HIGH
    ),
    MigrationPathItem(
        id = "path-005",
        stepNumber = 5,
        title = "Synchronize Compose + Espresso Tests",
        description = "在混合测试（Compose + Espresso）中，每次 UI 操作后添加 runCurrent()，确保 Compose 任务在 Espresso 断言前完成。",
        codeExample = """@Test
fun mixedTest() {
    composeTestRule.setContent { MyScreen() }
    composeTestRule.mainTestScheduler.runCurrent() // 等待 Compose 任务

    composeTestRule.onNodeWithText("Submit").performClick()
    composeTestRule.mainTestScheduler.runCurrent() // 等待点击任务

    // Espresso 断言
    composeTestRule.mainTestScheduler.runCurrent()
    Espresso.onView(withId(R.id.result)).check(matches(isDisplayed()))
}""",
        estimatedTime = "1-2 小时",
        riskLevel = RiskLevel.P1_HIGH
    ),
    MigrationPathItem(
        id = "path-006",
        stepNumber = 6,
        title = "Update KMP Test Configuration (if applicable)",
        description = "对于 Kotlin Multiplatform 项目，验证非 Android 平台（iOS/JS/Desktop）的 Testing v2 配置。",
        codeExample = """// KMP 项目各平台配置
// Android (androidAndroidTest):
// ✅ 使用 StandardTestDispatcher

// iOS (iosX64Test/iosArm64Test):
// ⚠️ 使用 XCTest，原生 iOS 测试框架

// JS/WASM (jsTest):
// ⚠️ 使用 Kotlin/JS test runner，Kotlin Coroutines Test 库

// Desktop (jvmTest):
// ✅ 与 Android 相同配置""",
        estimatedTime = "1-2 小时",
        riskLevel = RiskLevel.P1_HIGH
    ),
    MigrationPathItem(
        id = "path-007",
        stepNumber = 7,
        title = "Integrate CI Gradle Plugin",
        description = "集成 compose-testing-v2-check Gradle 插件到 CI 流程，阻塞未完全迁移的构建。",
        codeExample = """// build.gradle.kts (project root)
plugins {
    id("com.android.compose-testing-v2-check") version "1.0.0"
}

// CI 合规插件会在构建时检测:
// ✅ 所有测试使用 createComposeRule()
// ✅ 无 UnconfinedTestDispatcher
// ✅ runTest 内协程已显式调度
// ❌ 不合规 → 构建失败，输出详细报告""",
        estimatedTime = "30 分钟",
        riskLevel = RiskLevel.P2_MEDIUM
    ),
    MigrationPathItem(
        id = "path-008",
        stepNumber = 8,
        title = "Run Full Test Suite & Fix Flakiness",
        description = "运行完整测试套件，修复所有 flaky tests。v2 迁移后测试应全部稳定，不应有任何偶发性失败。",
        codeExample = """// 运行测试套件
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedAndroidTest

// 统计 flaky tests（v2 迁移后应为 0）
// 如有 flaky failures，检查是否还有:
// 1. 未替换的 UnconfinedTestDispatcher
// 2. runTest {} 内缺少 runCurrent()
// 3. Compose + Espresso 混合测试缺少同步""",
        estimatedTime = "2-4 小时",
        riskLevel = RiskLevel.P1_HIGH
    )
)
