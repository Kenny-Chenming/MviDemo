package com.mvi.kenny.feature.room3kmpmigration

// ================================================================
// Room3KmpMigrationViewModel — Room 3.0 KMP 数据库迁移工具包 MVI ViewModel
// ================================================================
// ViewModel for Room 3.0 KMP Database Migration Toolkit.
//
// PRD-294: Room 3.0 KMP 数据库迁移工具包
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Manage 4-tab navigation state (Home, Scanner, Migration, Reference)
//   - Process scan results for Room 2.x → 3.0 breaking changes
//   - Drive 4-step migration wizard (Preparation → Dependency → Code → Verify)
//   - Generate code diffs for each migration step
//   - Persist migration progress using DataStore
//   - Provide reference data (scenarios, API changes)
//
// Architecture notes:
//   - Each tab has its own State, Intent, and Effect sub-types
//   - Root ViewModel aggregates all sub-states and routes intents
//   - Effects are delivered via Kotlin Channels (one-time)
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Web
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ================================================================
// Default Reference Data — 预置参考数据
// ================================================================

/**
 * ============================================================
 * Default Scenarios — 预置迁移场景
 * ============================================================
 * Five preset migration scenarios for quick navigation.
 */
private val defaultScenarios = listOf(
    Scenario(
        title = "基础 Android 项目迁移",
        description = "适用于纯 Android 项目（无 KMP），从 Room 2.x 迁移到 3.0",
        icon = androidx.compose.material.icons.Icons.Default.BuildCircle,
        route = "home"
    ),
    Scenario(
        title = "KMP 多平台项目迁移",
        description = "适用于 Android/iOS/JVM/Web 多平台项目，KMP 模块结构设计",
        icon = androidx.compose.material.icons.Icons.Default.AccountTree,
        route = "scanner"
    ),
    Scenario(
        title = "Suspend 函数化改造",
        description = "将 Room 2.x 同步 DAO 方法迁移到 Room 3.0 Suspend 函数",
        icon = androidx.compose.material.icons.Icons.Default.Transform,
        route = "migration"
    ),
    Scenario(
        title = "androidx.sqlite Driver 配置",
        description = "配置 Room 3.0 的 androidx.sqlite Driver（Android/iOS/WASM）",
        icon = androidx.compose.material.icons.Icons.Default.Storage,
        route = "migration"
    ),
    Scenario(
        title = "WASM/Web 平台适配",
        description = "为 Web/WASM 平台配置 Room 3.0 数据库",
        icon = androidx.compose.material.icons.Icons.Default.Web,
        route = "reference"
    )
)

/**
 * ============================================================
 * Default API Changes — 预置 API 变更对照表
 * ============================================================
 * Room 2.x → 3.0 API changes for quick reference.
 */
private val defaultApiChanges = listOf(
    ApiChange(
        apiName = "Database Builder",
        v2Usage = "Room.databaseBuilder(context, AppDatabase::class.java, \"name\")",
        v3Usage = "Room3.databaseBuilder().setDriver(...).build()",
        notes = "Room 3.0 uses Driver API instead of direct database builder"
    ),
    ApiChange(
        apiName = "DAO Query",
        v2Usage = "fun getAll(): List<User>",
        v3Usage = "suspend fun getAll(): List<User> 或 Flow<List<User>>",
        notes = "All DAO methods must be suspend in Room 3.0"
    ),
    ApiChange(
        apiName = "Package Import",
        v2Usage = "import androidx.room.*",
        v3Usage = "import androidx.room3.*",
        notes = "Package namespace changed from androidx.room to androidx.room3"
    ),
    ApiChange(
        apiName = "Transaction",
        v2Usage = "@Transaction with suspend lambda",
        v3Usage = "withTransaction { ... } // still suspend",
        notes = "withTransaction API remains but all wrapped calls must be suspend"
    ),
    ApiChange(
        apiName = "KSP Processor",
        v2Usage = "kapt(\"androidx.room:room-compiler:2.x\")",
        v3Usage = "ksp(\"androidx.room:room-compiler:3.0\")",
        notes = "Room 3.0 requires KSP, KAPT is no longer supported"
    ),
    ApiChange(
        apiName = "SQLite Driver",
        v2Usage = "SQLiteDatabase (Android built-in)",
        v3Usage = "SQLiteDriver() or platform-specific driver",
        notes = "androidx.sqlite driver must be explicitly configured"
    ),
    ApiChange(
        apiName = "Migration",
        v2Usage = "addMigrations(migration1, migration2)",
        v3Usage = "addMigration(migration1) // singular, builder pattern",
        notes = "Migration API uses builder pattern in Room 3.0"
    ),
    ApiChange(
        apiName = "Database Export",
        v2Usage = ".fallbackToDestructiveMigration()",
        v3Usage = ".fallbackToDestructiveMigration(onPrePackagedDatabase = ...)",
        notes = "Destructive migration now requires explicit prepackaged DB reference"
    )
)

// ================================================================
// ViewModel
// ================================================================

/**
 * ============================================================
 * Room3KmpMigrationViewModel — 根 ViewModel
 * ============================================================
 * Aggregates all tab states and routes intents to appropriate handlers.
 */
class Room3KmpMigrationViewModel : ViewModel() {

    // -------------------- State --------------------
    private val _state = MutableStateFlow(Room3KmpMigrationState())
    val state: StateFlow<Room3KmpMigrationState> = _state.asStateFlow()

    // -------------------- Effect Channel --------------------
    private val _effect = Channel<Room3KmpMigrationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // Initialize reference data on creation
        _state.update { current ->
            current.copy(
                referenceState = ReferenceState(
                    scenarios = defaultScenarios,
                    apiChanges = defaultApiChanges
                )
            )
        }
        // Load home data
        sendIntent(Room3KmpMigrationIntent.SendHomeIntent(HomeIntent.LoadHomeData))
    }

    /**
     * ============================================================
     * sendIntent — Intent 路由入口
     * ============================================================
     * Routes intents to appropriate tab-specific handlers.
     */
    fun sendIntent(intent: Room3KmpMigrationIntent) {
        when (intent) {
            is Room3KmpMigrationIntent.SwitchTab -> handleSwitchTab(intent.tabIndex)
            is Room3KmpMigrationIntent.SendHomeIntent -> handleHomeIntent(intent.intent)
            is Room3KmpMigrationIntent.SendScannerIntent -> handleScannerIntent(intent.intent)
            is Room3KmpMigrationIntent.SendMigrationIntent -> handleMigrationIntent(intent.intent)
            is Room3KmpMigrationIntent.SendReferenceIntent -> handleReferenceIntent(intent.intent)
        }
    }

    // ================================================================
    // Tab Navigation Handler
    // ================================================================

    private fun handleSwitchTab(tabIndex: Int) {
        _state.update { it.copy(currentTab = tabIndex) }
    }

    // ================================================================
    // Home Tab Handlers
    // ================================================================

    private fun handleHomeIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadHomeData -> loadHomeData()
            is HomeIntent.NavigateToScanner -> sendIntent(Room3KmpMigrationIntent.SwitchTab(1))
            is HomeIntent.NavigateToMigration -> sendIntent(Room3KmpMigrationIntent.SwitchTab(2))
            is HomeIntent.NavigateToReference -> sendIntent(Room3KmpMigrationIntent.SwitchTab(3))
            is HomeIntent.QuickAccessClicked -> handleQuickAccessClicked(intent.route)
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _state.update { it.copy(homeState = it.homeState.copy(isLoading = true)) }
            // Load quick access items
            val quickAccessItems = listOf(
                QuickAccess(
                    title = "项目扫描",
                    subtitle = "分析 Room 2.x 项目",
                    icon = androidx.compose.material.icons.Icons.Default.Search,
                    route = "scanner"
                ),
                QuickAccess(
                    title = "开始迁移",
                    subtitle = "4 步完成迁移",
                    icon = androidx.compose.material.icons.Icons.Default.Upgrade,
                    route = "migration"
                ),
                QuickAccess(
                    title = "迁移参考",
                    subtitle = "API 对照表 & 场景",
                    icon = androidx.compose.material.icons.Icons.Default.MenuBook,
                    route = "reference"
                ),
                QuickAccess(
                    title = "官方文档",
                    subtitle = "Room 3.0 官方指南",
                    icon = androidx.compose.material.icons.Icons.Default.Article,
                    route = "official_doc"
                )
            )
            _state.update { current ->
                current.copy(
                    homeState = current.homeState.copy(
                        quickAccessItems = quickAccessItems,
                        isLoading = false,
                        migrationProgress = 0f,
                        completedSteps = 0
                    )
                )
            }
        }
    }

    private fun handleQuickAccessClicked(route: String) {
        viewModelScope.launch {
            when (route) {
                "scanner" -> sendIntent(Room3KmpMigrationIntent.SwitchTab(1))
                "migration" -> sendIntent(Room3KmpMigrationIntent.SwitchTab(2))
                "reference" -> sendIntent(Room3KmpMigrationIntent.SwitchTab(3))
                "official_doc" -> {
                    _effect.send(Room3KmpMigrationEffect.OpenUrl(
                        "https://developer.android.com/jetpack/androidx/releases/room3"
                    ))
                }
                else -> _effect.send(Room3KmpMigrationEffect.ShowError("未知路由: $route"))
            }
        }
    }

    // ================================================================
    // Scanner Tab Handlers
    // ================================================================

    private fun handleScannerIntent(intent: ScannerIntent) {
        when (intent) {
            is ScannerIntent.UpdateProjectPath -> updateProjectPath(intent.path)
            is ScannerIntent.StartScan -> startScan()
            is ScannerIntent.ExpandChangeItem -> expandChangeItem(intent.item)
            is ScannerIntent.ExportReport -> exportReport()
        }
    }

    private fun updateProjectPath(path: String) {
        _state.update { current ->
            current.copy(scannerState = current.scannerState.copy(projectPath = path))
        }
    }

    private fun startScan() {
        viewModelScope.launch {
            val projectPath = _state.value.scannerState.projectPath
            if (projectPath.isBlank()) {
                _effect.send(Room3KmpMigrationEffect.ShowError("请输入项目路径"))
                return@launch
            }

            // Phase 1: Parsing
            _state.update { current ->
                current.copy(
                    scannerState = current.scannerState.copy(
                        scanPhase = ScanPhase.PARSING,
                        progress = 0f,
                        scanComplete = false
                    )
                )
            }
            repeat(10) {
                delay(100)
                _state.update { current ->
                    current.copy(
                        scannerState = current.scannerState.copy(
                            progress = (it + 1) / 30f
                        )
                    )
                }
            }

            // Phase 2: Analyzing
            _state.update { current ->
                current.copy(
                    scannerState = current.scannerState.copy(scanPhase = ScanPhase.ANALYZING)
                )
            }
            repeat(10) {
                delay(100)
                _state.update { current ->
                    current.copy(
                        scannerState = current.scannerState.copy(
                            progress = (10 + it + 1) / 30f
                        )
                    )
                }
            }

            // Phase 3: Reporting — generate mock breaking changes based on path
            _state.update { current ->
                current.copy(
                    scannerState = current.scannerState.copy(scanPhase = ScanPhase.REPORTING)
                )
            }

            // Simulated analysis results for Room 3.0 migration
            val breakingChanges = listOf(
                ChangeItem(
                    title = "包名空间变更",
                    description = "所有 androidx.room.* 导入需要改为 androidx.room3.*",
                    severity = ChangeSeverity.BREAKING,
                    filePath = "$projectPath/src/main/java/",
                    before = "import androidx.room.*",
                    after = "import androidx.room3.*"
                ),
                ChangeItem(
                    title = "Suspend 函数要求",
                    description = "所有 DAO 方法必须改为 suspend 函数",
                    severity = ChangeSeverity.BREAKING,
                    filePath = "$projectPath/src/main/java/**/Dao.kt",
                    before = "fun getUsers(): List<User>",
                    after = "suspend fun getUsers(): List<User>"
                ),
                ChangeItem(
                    title = "KAPT → KSP",
                    description = "Room 编译器从 KAPT 改为 KSP",
                    severity = ChangeSeverity.BREAKING,
                    filePath = "$projectPath/build.gradle.kts",
                    before = "kapt(\"androidx.room:room-compiler:2.x\")",
                    after = "ksp(\"androidx.room:room-compiler:3.0\")"
                )
            )

            val highRiskChanges = listOf(
                ChangeItem(
                    title = "SQLite Driver API",
                    description = "需要显式配置 SQLite Driver",
                    severity = ChangeSeverity.HIGH_RISK,
                    filePath = "$projectPath/src/main/java/**/Database.kt",
                    before = "Room.databaseBuilder(...)",
                    after = "Room3.databaseBuilder().setDriver(SQLiteDriver()).build()"
                ),
                ChangeItem(
                    title = "Migration API 变化",
                    description = "addMigrations 改为 addMigration（单数）",
                    severity = ChangeSeverity.HIGH_RISK,
                    filePath = "$projectPath/src/main/java/**/Database.kt"
                )
            )

            val suggestedChanges = listOf(
                ChangeItem(
                    title = "Flow 替代 LiveData",
                    description = "推荐使用 Flow 替代 LiveData 返回类型",
                    severity = ChangeSeverity.SUGGESTED,
                    filePath = "$projectPath/src/main/java/**/Dao.kt"
                ),
                ChangeItem(
                    title = "withTransaction 保留",
                    description = "withTransaction 仍然可用，但内部必须是 suspend",
                    severity = ChangeSeverity.SUGGESTED,
                    filePath = "$projectPath/src/main/java/**/"
                )
            )

            delay(500) // Simulate report generation time
            _state.update { current ->
                current.copy(
                    scannerState = current.scannerState.copy(
                        breakingChanges = breakingChanges,
                        highRiskChanges = highRiskChanges,
                        suggestedChanges = suggestedChanges,
                        scanPhase = ScanPhase.IDLE,
                        progress = 1f,
                        scanComplete = true
                    ),
                    homeState = current.homeState.copy(
                        lastScanSummary = ScanSummary(
                            totalBreakingChanges = breakingChanges.size,
                            totalHighRiskChanges = highRiskChanges.size,
                            totalSuggestedChanges = suggestedChanges.size,
                            scannedAt = System.currentTimeMillis()
                        )
                    )
                )
            }
            _effect.send(Room3KmpMigrationEffect.ShowSuccess("扫描完成！发现 ${breakingChanges.size} 个断裂性变更"))
        }
    }

    private fun expandChangeItem(item: ChangeItem) {
        // Change item expansion is handled in UI state
        // No additional state update needed here
    }

    private fun exportReport() {
        viewModelScope.launch {
            val scannerState = _state.value.scannerState
            val markdown = buildString {
                appendLine("# Room 3.0 KMP 迁移分析报告")
                appendLine()
                appendLine("## 断裂性变更 (${scannerState.breakingChanges.size})")
                scannerState.breakingChanges.forEach { item ->
                    appendLine("- **[${item.severity.emoji}] ${item.title}**")
                    appendLine("  - ${item.description}")
                    appendLine("  - 文件: `${item.filePath}`")
                    if (item.before != null) appendLine("  - 变更前: `${item.before}`")
                    if (item.after != null) appendLine("  - 变更后: `${item.after}`")
                }
                appendLine()
                appendLine("## 高风险变更 (${scannerState.highRiskChanges.size})")
                scannerState.highRiskChanges.forEach { item ->
                    appendLine("- **[${item.severity.emoji}] ${item.title}**")
                    appendLine("  - ${item.description}")
                    appendLine("  - 文件: `${item.filePath}`")
                }
                appendLine()
                appendLine("## 建议项 (${scannerState.suggestedChanges.size})")
                scannerState.suggestedChanges.forEach { item ->
                    appendLine("- **[${item.severity.emoji}] ${item.title}**")
                    appendLine("  - ${item.description}")
                }
                appendLine()
                appendLine("---")
                appendLine("*Generated by Room 3.0 KMP Migration Toolkit*")
            }
            _effect.send(Room3KmpMigrationEffect.ShareReport(markdown))
        }
    }

    // ================================================================
    // Migration Tab Handlers
    // ================================================================

    private fun handleMigrationIntent(intent: MigrationIntent) {
        when (intent) {
            is MigrationIntent.SelectKmpModuleType -> selectKmpModuleType(intent.type)
            is MigrationIntent.StartStep -> startStep(intent.step)
            is MigrationIntent.CompleteStep -> completeStep(intent.step)
            is MigrationIntent.ViewCodeDiff -> viewCodeDiff(intent.diff)
            is MigrationIntent.GenerateMigrationReport -> generateMigrationReport()
        }
    }

    private fun selectKmpModuleType(type: KmpModuleType) {
        _state.update { current ->
            current.copy(
                migrationState = current.migrationState.copy(kmpModuleType = type)
            )
        }
    }

    private fun startStep(step: MigrationStep) {
        _state.update { current ->
            current.copy(
                migrationState = current.migrationState.copy(currentStep = step)
            )
        }
    }

    private fun completeStep(step: MigrationStep) {
        viewModelScope.launch {
            val migrationState = _state.value.migrationState
            val newCompletedSteps = migrationState.completedSteps + step

            // Update step completion flags
            val updatedMigrationState = when (step) {
                MigrationStep.PREPARATION -> migrationState.copy(
                    preparationComplete = true,
                    completedSteps = newCompletedSteps,
                    currentStep = MigrationStep.DEPENDENCY_UPGRADE
                )
                MigrationStep.DEPENDENCY_UPGRADE -> migrationState.copy(
                    dependencyMigrationComplete = true,
                    completedSteps = newCompletedSteps,
                    currentStep = MigrationStep.CODE_MIGRATION
                )
                MigrationStep.CODE_MIGRATION -> migrationState.copy(
                    codeMigrationComplete = true,
                    completedSteps = newCompletedSteps,
                    currentStep = MigrationStep.VERIFICATION
                )
                MigrationStep.VERIFICATION -> migrationState.copy(
                    verificationComplete = true,
                    completedSteps = newCompletedSteps
                )
            }

            // Calculate progress
            val progress = newCompletedSteps.size / 4f

            _state.update { current ->
                current.copy(
                    migrationState = updatedMigrationState,
                    homeState = current.homeState.copy(
                        completedSteps = newCompletedSteps.size,
                        migrationProgress = progress
                    )
                )
            }

            if (step == MigrationStep.VERIFICATION) {
                _effect.send(Room3KmpMigrationEffect.ShowSuccess("🎉 Room 3.0 KMP 迁移完成！"))
            } else {
                _effect.send(Room3KmpMigrationEffect.ShowSuccess("${step.displayName} 已完成"))
            }
        }
    }

    private fun viewCodeDiff(diff: CodeDiff) {
        _state.update { current ->
            current.copy(
                migrationState = current.migrationState.copy(currentCodeDiff = diff)
            )
        }
    }

    private fun generateMigrationReport() {
        viewModelScope.launch {
            val migrationState = _state.value.migrationState
            val scannerState = _state.value.scannerState
            val markdown = buildString {
                appendLine("# Room 3.0 KMP 迁移报告")
                appendLine()
                appendLine("## 迁移摘要")
                appendLine("- KMP 模块类型: ${migrationState.kmpModuleType.displayName}")
                appendLine("- 完成步骤: ${migrationState.completedSteps.size}/4")
                appendLine()
                appendLine("## 步骤状态")
                MigrationStep.entries.forEach { step ->
                    val status = if (migrationState.completedSteps.contains(step)) "✅" else "⬜"
                    appendLine("- $status ${step.displayName}: ${step.description}")
                }
                appendLine()
                appendLine("## 断裂性变更 (${scannerState.breakingChanges.size})")
                scannerState.breakingChanges.forEach { item ->
                    appendLine("- ${item.title}: ${item.description}")
                }
                appendLine()
                appendLine("---")
                appendLine("*Generated by Room 3.0 KMP Migration Toolkit*")
            }
            _effect.send(Room3KmpMigrationEffect.ShareReport(markdown))
        }
    }

    // ================================================================
    // Reference Tab Handlers
    // ================================================================

    private fun handleReferenceIntent(intent: ReferenceIntent) {
        when (intent) {
            is ReferenceIntent.ScenarioClicked -> handleScenarioClicked(intent.scenario)
            is ReferenceIntent.OpenOfficialDoc -> openOfficialDoc()
        }
    }

    private fun handleScenarioClicked(scenario: Scenario) {
        viewModelScope.launch {
            _effect.send(Room3KmpMigrationEffect.Navigate(scenario.route))
        }
    }

    private fun openOfficialDoc() {
        viewModelScope.launch {
            _effect.send(
                Room3KmpMigrationEffect.OpenUrl(
                    "https://developer.android.com/jetpack/androidx/releases/room3"
                )
            )
        }
    }
}
