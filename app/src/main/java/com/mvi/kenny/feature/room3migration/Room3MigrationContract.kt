package com.mvi.kenny.feature.room3migration

// ================================================================
// Room3MigrationContract — Room 3.0 KMP 现代化迁移检测与自动化工具包 MVI 契约
// ================================================================
// MVI architecture contract for Room 3.0 KMP modernization migration toolkit.
//
// PRD-118: Room 3.0 KMP 现代化迁移检测与自动化工具包
// Design Reference: memory/agency/designs/PRD-118-Room-3-0-KMP-现代化迁移检测与自动化工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

/**
 * ============================================================
 * MigrationModule — 迁移模块枚举
 * ================================================================
 * Represents each migration module tracked by the toolkit.
 *
 * @property displayName Module display name for UI
 * @property description Module description
 */
enum class MigrationModule(val displayName: String, val description: String) {
    PACKAGE_NAMESPACE("包命名空间", "androidx.room → androidx.room3 包迁移"),
    KSP_SWITCH("KSP 编译器", "KAPT → KSP 配置迁移"),
    SQLITE_DRIVER("SQLite Driver", "SupportSQLite → SQLiteDriver 迁移"),
    KOTLIN_COMPILER("Kotlin 编译器", "Kotlin 编译器合规检测"),
    SUSPEND_API("Suspend API", "全协程化 API 迁移"),
    FLOW_INVALIDATION("Flow Invalidation", "InvalidationTracker → Flow API"),
    REGRESSION_TEST("回归测试", "迁移后行为一致性验证")
}

/**
 * ============================================================
 * ModuleState — 模块迁移状态枚举
 * ================================================================
 * Represents the migration state of each module.
 */
enum class ModuleState {
    /** 未开始 */
    NOT_STARTED,
    /** 进行中 */
    IN_PROGRESS,
    /** 已完成 */
    COMPLETED,
    /** 阻塞 */
    BLOCKED
}

/**
 * ============================================================
 * MigrationTab — Tab 页枚举
 * ================================================================
 * Navigation tabs within the Room 3.0 migration toolkit.
 *
 * @param title Tab display title
 * @param iconName Material icon name for the tab
 */
enum class MigrationTab(val title: String, val iconName: String) {
    DASHBOARD("仪表盘", "Dashboard"),
    PACKAGE_MIGRATOR("包迁移", "PackageMigrator"),
    KSP_SWITCHER("KSP 切换", "KSPSwitcher"),
    SQLITE_DRIVER("SQLite Driver", "SQLiteDriver"),
    KOTLIN_COMPILER("编译器检测", "KotlinCompiler"),
    SUSPEND_API("Suspend API", "SuspendApi"),
    FLOW_INVALIDATION("Flow 订阅", "FlowInvalidation"),
    REGRESSION_TEST("回归测试", "RegressionTest"),
    SETTINGS("设置", "Settings")
}

// ================================================================
// Data Models / 数据模型
// ================================================================

/**
 * ============================================================
 * ScanResults — 扫描结果汇总
 * ================================================================
 * Aggregated scan results from all migration modules.
 *
 * @param packageIssues Found import/declaration issues for package migration
 * @param kaptConfigs Found KAPT configurations that need migration
 * @param sqliteUsages Found SupportSQLite API usages
 * @param kotlinCompilerIssues Kotlin compiler compliance issues
 * @param suspendApiIssues Suspend API migration issues
 * @param invalidationIssues InvalidationTracker → Flow migration issues
 * @param testCoverage Report test coverage status
 */
data class ScanResults(
    val packageIssues: List<PackageIssue> = emptyList(),
    val kaptConfigs: List<KaptConfig> = emptyList(),
    val sqliteUsages: List<SqliteUsage> = emptyList(),
    val kotlinCompilerIssues: List<KotlinCompilerIssue> = emptyList(),
    val suspendApiIssues: List<SuspendApiIssue> = emptyList(),
    val invalidationIssues: List<InvalidationIssue> = emptyList(),
    val testCoverage: TestCoverage = TestCoverage()
) {
    /** Total issues across all modules / 所有模块问题总数 */
    val totalIssues: Int
        get() = packageIssues.size + kaptConfigs.size + sqliteUsages.size +
                kotlinCompilerIssues.size + suspendApiIssues.size + invalidationIssues.size

    /** Whether scan found any issues / 是否发现任何问题 */
    val hasIssues: Boolean
        get() = totalIssues > 0
}

/**
 * ============================================================
 * ImportReplacement — Import 替换项
 * ================================================================
 * Represents a single import statement replacement.
 *
 * @param id Unique identifier
 * @param filePath File where the import appears
 * @param lineNumber Line number in file
 * @param oldImport Original import statement
 * @param newImport Replacement import statement
 * @param isSelected Whether this replacement is selected for execution
 * @param isExecuted Whether this replacement has been applied
 */
data class ImportReplacement(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val oldImport: String,
    val newImport: String,
    val isSelected: Boolean = true,
    val isExecuted: Boolean = false
)

/**
 * ============================================================
 * PackageIssue — 包命名空间问题
 * ================================================================
 * Found package namespace issue in source files.
 *
 * @param filePath Source file path
 * @param lineNumber Line number
 * @param content Code snippet
 * @param issueType Type of issue (import/dependency/usage)
 * @param fixAvailable Whether automated fix is available
 */
data class PackageIssue(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val content: String,
    val issueType: String, // "import" / "dependency" / "usage"
    val fixAvailable: Boolean = true
)

/**
 * ============================================================
 * KaptConfig — KAPT 配置问题
 * ================================================================
 * Found KAPT configuration that needs to be migrated to KSP.
 *
 * @param filePath build.gradle.kts file path
 * @param lineNumber Line number of kapt block
 * @param configContent The kapt configuration content
 * @param suggestedKspConfig Suggested KSP equivalent configuration
 * @param hasConflict Whether this KAPT conflicts with existing KSP
 */
data class KaptConfig(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val configContent: String,
    val suggestedKspConfig: String = "",
    val hasConflict: Boolean = false
)

/**
 * ============================================================
 * SqliteUsage — SupportSQLite API 使用
 * ================================================================
 * Found SupportSQLite API usage that needs migration to SQLiteDriver.
 *
 * @param filePath Source file path
 * @param lineNumber Line number
 * @param content Code snippet
 * @param suggestedReplacement Suggested SQLiteDriver replacement code
 * @param needsWrapperDependency Whether room3-sqlite-wrapper dependency is needed
 */
data class SqliteUsage(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val content: String,
    val suggestedReplacement: String = "",
    val needsWrapperDependency: Boolean = false
)

/**
 * ============================================================
 * KotlinCompilerIssue — Kotlin 编译器合规问题
 * ================================================================
 * Kotlin compiler compliance issue found in project.
 *
 * @param filePath Configuration file path
 * @param lineNumber Line number
 * @param content Configuration snippet
 * @param issueType Type of compliance issue
 * @param recommendation Recommended fix
 */
data class KotlinCompilerIssue(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val content: String,
    val issueType: String,
    val recommendation: String
)

/**
 * ============================================================
 * SuspendApiIssue — Suspend API 迁移问题
 * ================================================================
 * Synchronous API that needs migration to suspend function.
 *
 * @param filePath Source file path
 * @param lineNumber Line number
 * @param content Code snippet showing sync usage
 * @param suggestedSuspendReplacement Suggested suspend function replacement
 * @param callerCount Number of call sites that need updating
 */
data class SuspendApiIssue(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val content: String,
    val suggestedSuspendReplacement: String = "",
    val callerCount: Int = 0
)

/**
 * ============================================================
 * InvalidationIssue — InvalidationTracker → Flow API 问题
 * ================================================================
 * InvalidationTracker usage that needs migration to Flow API.
 *
 * @param filePath Source file path
 * @param lineNumber Line number
 * @param content Code snippet
 * @param suggestedFlowReplacement Suggested Flow-based replacement
 * @param isInDao Whether this is in a DAO file
 */
data class InvalidationIssue(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val content: String,
    val suggestedFlowReplacement: String = "",
    val isInDao: Boolean = false
)

/**
 * ============================================================
 * TestCoverage — 回归测试覆盖情况
 * ================================================================
 * Test coverage status for migrated code.
 *
 * @param totalDaos Total DAO count in project
 * @param testedDaos Number of DAOs with regression tests
 * @param untestedDaos List of DAOs without tests
 * @param generatedTests List of auto-generated test cases
 */
data class TestCoverage(
    val totalDaos: Int = 0,
    val testedDaos: Int = 0,
    val untestedDaos: List<String> = emptyList(),
    val generatedTests: List<GeneratedTest> = emptyList()
) {
    val coveragePercent: Float
        get() = if (totalDaos == 0) 0f else testedDaos.toFloat() / totalDaos
}

/**
 * ============================================================
 * GeneratedTest — 生成的回归测试
 * ================================================================
 * Auto-generated regression test case.
 *
 * @param daoName Target DAO name
 * @param testContent Generated test code
 * @param testFilePath Where the test will be saved
 * @param isGenerated Whether test has been written to file
 */
data class GeneratedTest(
    val id: String,
    val daoName: String,
    val testContent: String,
    val testFilePath: String,
    val isGenerated: Boolean = false
)

/**
 * ============================================================
 * BackupInfo — 备份信息
 * ================================================================
 * Backup created before migration operations.
 *
 * @param backupPath Path to backup directory
 * @param createdAt Timestamp when backup was created
 * @param fileCount Number of files backed up
 * @param description Backup description
 */
data class BackupInfo(
    val backupPath: String,
    val createdAt: String,
    val fileCount: Int,
    val description: String
)

// ================================================================
// State / 状态
// ================================================================

/**
 * ============================================================
 * Room3MigrationState — Room 3.0 迁移工具页面状态
 * ================================================================
 * Immutable UI state — single source of truth for the migration toolkit.
 *
 * @param isScanning Whether a scan is in progress
 * @param scanResults Aggregated scan results from all modules
 * @param healthScore Overall migration health score (0.0 ~ 1.0)
 * @param moduleStates Map of each module to its current migration state
 * @param selectedTab Currently selected tab
 * @param pendingReplacements List of import replacements awaiting execution
 * @param backupInfo Current backup information if any
 * @param rollbackAvailable Whether rollback is available
 * @param isLoading Whether a long-running operation is in progress
 * @param error Error message if any operation failed
 * @param showRollbackDialog Whether rollback confirmation dialog is visible
 */
data class Room3MigrationState(
    val isScanning: Boolean = false,
    val scanResults: ScanResults = ScanResults(),
    val healthScore: Float = 0f, // 0.0 ~ 1.0
    val moduleStates: Map<MigrationModule, ModuleState> = MigrationModule.entries
        .associateWith { ModuleState.NOT_STARTED },
    val selectedTab: MigrationTab = MigrationTab.DASHBOARD,
    val pendingReplacements: List<ImportReplacement> = emptyList(),
    val backupInfo: BackupInfo? = null,
    val rollbackAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showRollbackDialog: Boolean = false,
    // ─────────────────────────────────────────────────────────
    // Dashboard-specific state / 仪表盘专用状态
    // ─────────────────────────────────────────────────────────
    val radarAxisLabels: List<String> = listOf(
        "包命名空间", "KSP切换", "SQLite Driver",
        "编译器合规", "Suspend API", "Flow订阅", "回归测试"
    ),
    val radarAxisScores: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    // ─────────────────────────────────────────────────────────
    // Package Migrator state / 包迁移器状态
    // ─────────────────────────────────────────────────────────
    val selectedReplacements: Set<String> = emptySet(),
    val allReplacementsSelected: Boolean = true,
    // ─────────────────────────────────────────────────────────
    // KSP Switcher state / KSP 切换器状态
    // ─────────────────────────────────────────────────────────
    val kaptConfigs: List<KaptConfig> = emptyList(),
    val selectedKaptConfigs: Set<String> = emptySet(),
    // ─────────────────────────────────────────────────────────
    // SQLite Driver state / SQLite Driver 状态
    // ─────────────────────────────────────────────────────────
    val sqliteUsages: List<SqliteUsage> = emptyList(),
    // ─────────────────────────────────────────────────────────
    // Kotlin Compiler state / Kotlin 编译器状态
    // ─────────────────────────────────────────────────────────
    val kotlinCompilerIssues: List<KotlinCompilerIssue> = emptyList(),
    // ─────────────────────────────────────────────────────────
    // Suspend API state / Suspend API 状态
    // ─────────────────────────────────────────────────────────
    val suspendApiIssues: List<SuspendApiIssue> = emptyList(),
    // ─────────────────────────────────────────────────────────
    // Flow Invalidation state / Flow 订阅状态
    // ─────────────────────────────────────────────────────────
    val invalidationIssues: List<InvalidationIssue> = emptyList(),
    // ─────────────────────────────────────────────────────────
    // Regression Test state / 回归测试状态
    // ─────────────────────────────────────────────────────────
    val testCoverage: TestCoverage = TestCoverage(),
    val generatedTests: List<GeneratedTest> = emptyList()
) {
    /** Number of completed modules / 已完成模块数 */
    val completedModulesCount: Int
        get() = moduleStates.count { it.value == ModuleState.COMPLETED }

    /** Overall migration progress percentage / 整体迁移进度百分比 */
    val migrationProgress: Float
        get() = if (MigrationModule.entries.isEmpty()) 0f
                else completedModulesCount.toFloat() / MigrationModule.entries.size

    /** Whether any module is in progress / 是否有模块正在进行 */
    val hasModuleInProgress: Boolean
        get() = moduleStates.any { it.value == ModuleState.IN_PROGRESS }
}

// ================================================================
// Intent / 意图
// ================================================================

/**
 * ============================================================
 * Room3MigrationIntent — 用户操作意图
 * ================================================================
 * Sealed class representing all possible user intentions.
 * ViewModel receives intents via sendIntent() and processes them.
 */
sealed class Room3MigrationIntent {
    // ─────────────────────────────────────────────────────────
    // Scanning / 扫描
    // ─────────────────────────────────────────────────────────
    /** 开始扫描项目中的 Room 3.0 迁移问题 */
    data object StartScan : Room3MigrationIntent()

    /** 刷新扫描结果 */
    data object RefreshScan : Room3MigrationIntent()

    // ─────────────────────────────────────────────────────────
    // Tab Navigation / Tab 导航
    // ─────────────────────────────────────────────────────────
    /** 切换 Tab 页 */
    data class SelectTab(val tab: MigrationTab) : Room3MigrationIntent()

    // ─────────────────────────────────────────────────────────
    // Package Migration / 包迁移
    // ─────────────────────────────────────────────────────────
    /** 预览 Import 替换列表 */
    data class PreviewReplacements(val replacements: List<ImportReplacement>) : Room3MigrationIntent()

    /** 切换单个替换项的选择状态 */
    data class ToggleReplacementSelection(val id: String) : Room3MigrationIntent()

    /** 全选/取消全选所有替换项 */
    data object ToggleSelectAllReplacements : Room3MigrationIntent()

    /** 执行单个替换 */
    data class ExecuteReplacement(val replacement: ImportReplacement) : Room3MigrationIntent()

    /** 执行所有选中的替换 */
    data object ExecuteSelectedReplacements : Room3MigrationIntent()

    // ─────────────────────────────────────────────────────────
    // KSP Migration / KSP 迁移
    // ─────────────────────────────────────────────────────────
    /** 将选中的 KAPT 配置转换为 KSP */
    data class ConvertKaptToKsp(val configIds: Set<String>) : Room3MigrationIntent()

    /** 转换所有 KAPT 配置 */
    data object ConvertAllKaptToKsp : Room3MigrationIntent()

    // ─────────────────────────────────────────────────────────
    // SQLite Driver Migration / SQLite Driver 迁移
    // ─────────────────────────────────────────────────────────
    data object MigrateSqliteDriver : Room3MigrationIntent()

    // ─────────────────────────────────────────────────────────
    // Kotlin Compiler / Kotlin 编译器
    // ─────────────────────────────────────────────────────────
    data object CheckKotlinCompiler : Room3MigrationIntent()

    // ─────────────────────────────────────────────────────────
    // Suspend API / Suspend API
    // ─────────────────────────────────────────────────────────
    data object MigrateSuspendApi : Room3MigrationIntent()

    // ─────────────────────────────────────────────────────────
    // Flow Invalidation / Flow 订阅
    // ─────────────────────────────────────────────────────────
    data object MigrateFlowInvalidation : Room3MigrationIntent()

    // ─────────────────────────────────────────────────────────
    // Regression Test / 回归测试
    // ─────────────────────────────────────────────────────────
    /** 生成回归测试用例 */
    data object GenerateRegressionTests : Room3MigrationIntent()

    /** 保存生成的测试用例 */
    data class SaveGeneratedTests(val testIds: Set<String>) : Room3MigrationIntent()

    // ─────────────────────────────────────────────────────────
    // Backup & Rollback / 备份与回滚
    // ─────────────────────────────────────────────────────────
    /** 创建备份 */
    data object CreateBackup : Room3MigrationIntent()

    /** 显示回滚确认对话框 */
    data object ShowRollbackDialog : Room3MigrationIntent()

    /** 确认执行回滚 */
    data object ConfirmRollback : Room3MigrationIntent()

    /** 取消回滚 */
    data object CancelRollback : Room3MigrationIntent()
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * ============================================================
 * Room3MigrationEffect — 一次性副作用
 * ================================================================
 * One-time side effects triggered by ViewModel.
 * Delivered via Channel<UiEffect> and consumed by the UI layer.
 */
sealed class Room3MigrationEffect {
    /** 显示 Toast 消息 */
    data class ShowToast(val message: String) : Room3MigrationEffect()

    /** 导航到指定文件并高亮行号 */
    data class NavigateToFile(val filePath: String, val line: Int) : Room3MigrationEffect()

    /** 备份已创建通知 */
    data object BackupCreated : Room3MigrationEffect()

    /** 回滚完成通知 */
    data object RollbackCompleted : Room3MigrationEffect()

    /** 模块迁移完成通知 */
    data class MigrationCompleted(val module: MigrationModule) : Room3MigrationEffect()

    /** 扫描完成通知 */
    data class ScanCompleted(
        val totalIssues: Int,
        val criticalModules: List<MigrationModule>
    ) : Room3MigrationEffect()

    /** 复制到剪贴板成功 */
    data object CopiedToClipboard : Room3MigrationEffect()

    /** 显示错误消息 */
    data class ShowError(val message: String) : Room3MigrationEffect()
}
