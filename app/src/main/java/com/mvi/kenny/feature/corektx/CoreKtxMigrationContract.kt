package com.mvi.kenny.feature.corektx

// ================================================================
// CoreKtxMigrationContract — AndroidX core-ktx 迁移工具 MVI 契约
// ================================================================
// MVI architecture contract for AndroidX core-ktx → core migration toolkit.
//
// PRD-115: AndroidX core-ktx 合并至 core 历史性迁移检测与自动化工具包
// Design Reference: memory/agency/designs/PRD-115-AndroidX-core-ktx-历史性迁移检测与自动化工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View            — Composable function, consumes State, renders UI
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
// ================================================================

/**
 * ============================================================
 * ScanState — 扫描状态枚举
 * ================================================================
 * Represents the current scanning/processing status.
 */
enum class ScanState {
    /** 空闲状态（初始状态） */
    Idle,
    /** 扫描中 */
    Scanning,
    /** 扫描完成 */
    Completed,
    /** 扫描失败 */
    Failed
}

/**
 * ============================================================
 * Severity — 违规严重程度枚举
 * ================================================================
 * Severity levels for migration issues detected.
 *
 * @param order 优先级顺序（数字越小越严重）
 */
enum class Severity(val order: Int) {
    /** 直接崩溃风险 — 必须立即修复 */
    P0(0),
    /** 警告 — 可能导致运行时问题 */
    P1(1),
    /** 建议 — 可优化但不影响功能 */
    P2(2)
}

/**
 * ============================================================
 * ValidationStatus — 验证状态枚举
 * ================================================================
 * Represents the result of import validation.
 */
enum class ValidationStatus {
    Pass,  // 通过
    Warn,  // 警告
    Fail   // 失败
}

/**
 * ============================================================
 * ComplianceLevel — 合规级别枚举
 * ================================================================
 * CI/CD compliance check result levels.
 */
enum class ComplianceLevel {
    Compliant,   // ✅ 合规
    Warning,     // ⚠️ 警告
    Violation    // ❌ 违规
}

// ================================================================
// Data Models / 数据模型
// ================================================================

/**
 * ============================================================
 * ScanResult — 单个扫描结果项
 * ================================================================
 * Represents a single core-ktx dependency or usage detected.
 *
 * @param id Unique identifier for this result item
 * @param filePath File path where the issue was found
 * @param lineNumber Line number in the file
 * @param content Code snippet showing the issue
 * @param severity Issue severity level
 * @param fixAvailable Whether an automated fix is available
 * @param fixSuggestion Suggested fix for this issue
 * @param isFixed Whether this item has been fixed
 */
data class ScanResult(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val content: String,
    val severity: Severity,
    val fixAvailable: Boolean,
    val fixSuggestion: String,
    val isFixed: Boolean = false
)

/**
 * ============================================================
 * ValidationResult — Import 验证结果
 * ================================================================
 * Result of validating Kotlin extension function imports.
 *
 * @param filePath File path that was validated
 * @param importStatement The import statement found
 * @param isCompatible Whether the import is compatible after migration
 * @param status Validation status (Pass/Warn/Fail)
 * @param message Additional message or warning
 */
data class ValidationResult(
    val filePath: String,
    val importStatement: String,
    val isCompatible: Boolean,
    val status: ValidationStatus,
    val message: String = ""
)

/**
 * ============================================================
 * ThirdPartyImpact — 第三方库影响分析
 * ================================================================
 * Represents the impact of core-ktx on a third-party dependency.
 *
 * @param dependencyName Name of the third-party library
 * @param version Library version
 * @param hasCoreKtx Whether this library directly depends on core-ktx
 * @param impactLevel Severity of the impact (High/Medium/Low)
 * @param transitiveDeps List of transitive dependencies that pull in core-ktx
 * @param recommendation Migration recommendation for this library
 */
data class ThirdPartyImpact(
    val dependencyName: String,
    val version: String,
    val hasCoreKtx: Boolean,
    val impactLevel: String,  // "High" / "Medium" / "Low"
    val transitiveDeps: List<String> = emptyList(),
    val recommendation: String
)

/**
 * ============================================================
 * ComplianceRule — 合规检查规则
 * ================================================================
 * Represents a CI/CD compliance check rule.
 *
 * @param id Rule unique identifier
 * @param name Rule display name
 * @param description Rule description
 * @param enabled Whether this rule is currently enabled
 * @param level Compliance level result
 * @param lastChecked Last check timestamp
 * @param curlCommand Example curl command for CI/CD integration
 */
data class ComplianceRule(
    val id: String,
    val name: String,
    val description: String,
    val enabled: Boolean = true,
    val level: ComplianceLevel = ComplianceLevel.Compliant,
    val lastChecked: String = "",
    val curlCommand: String = ""
)

/**
 * ============================================================
 * MigrationSettings — 迁移设置
 * ================================================================
 * Configuration settings for the migration tool.
 *
 * @param scanIncludeModules List of modules to include in scan (empty = all)
 * @param scanExcludePaths List of paths to exclude from scan
 * @param autoFixEnabled Whether to allow automated fixes
 * @param backupEnabled Whether to create backups before fixing
 * @param minSeverityForFix Minimum severity level for auto-fix
 * @param enableR8RulesCheck Whether to check R8/Proguard rules
 */
data class MigrationSettings(
    val scanIncludeModules: List<String> = emptyList(),
    val scanExcludePaths: List<String> = listOf("build/", ".gradle/", "*/.gradle/"),
    val autoFixEnabled: Boolean = true,
    val backupEnabled: Boolean = true,
    val minSeverityForFix: Severity = Severity.P1,
    val enableR8RulesCheck: Boolean = true
)

// ================================================================
// State / 状态
// ================================================================

/**
 * ============================================================
 * CoreKtxMigrationState — AndroidX core-ktx 迁移工具页面状态
 * ================================================================
 * Immutable UI state — single source of truth for the migration toolkit.
 *
 * @param scanState Current scanning status
 * @param scanProgress Scanning progress (0.0–1.0)
 * @param scanResults List of detected core-ktx issues
 * @param healthScore Overall migration health score (0–100)
 * @param totalDependencies Total dependencies scanned
 * @param affectedDependencies Number of dependencies with core-ktx issues
 * @param selectedTab Currently selected tab index
 * @param filterSeverity Set of severity levels to display in scanner
 * @param validatorInput User input for import validation path
 * @param validatorResults List of validation results
 * @param thirdPartyImpact List of third-party library impacts
 * @param complianceRules List of CI/CD compliance rules
 * @param settings Migration tool settings
 * @param isLoading Whether a long-running operation is in progress
 * @param error Error message if any operation failed
 * @param showFixDialog Whether the fix confirmation dialog is visible
 * @param pendingFixItem The item pending fix confirmation
 */
data class CoreKtxMigrationState(
    val scanState: ScanState = ScanState.Idle,
    val scanProgress: Float = 0f,
    val scanResults: List<ScanResult> = emptyList(),
    val healthScore: Int = 100,
    val totalDependencies: Int = 0,
    val affectedDependencies: Int = 0,
    val selectedTab: Int = 0,
    val filterSeverity: Set<Severity> = Severity.entries.toSet(),
    val validatorInput: String = "",
    val validatorResults: List<ValidationResult> = emptyList(),
    val thirdPartyImpact: List<ThirdPartyImpact> = emptyList(),
    val complianceRules: List<ComplianceRule> = emptyList(),
    val settings: MigrationSettings = MigrationSettings(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showFixDialog: Boolean = false,
    val pendingFixItem: ScanResult? = null
) {
    /** Filtered scan results based on selected severity filters / 按严重程度过滤的扫描结果 */
    val filteredResults: List<ScanResult>
        get() = scanResults.filter { it.severity in filterSeverity }

    /** P0 (critical) count / P0 级别问题数量 */
    val criticalCount: Int
        get() = scanResults.count { it.severity == Severity.P0 }

    /** P1 (warning) count / P1 级别问题数量 */
    val warningCount: Int
        get() = scanResults.count { it.severity == Severity.P1 }

    /** P2 (suggestion) count / P2 级别问题数量 */
    val suggestionCount: Int
        get() = scanResults.count { it.severity == Severity.P2 }

    /** Number of fixed items / 已修复项目数量 */
    val fixedCount: Int
        get() = scanResults.count { it.isFixed }

    /** Overall migration progress percentage / 整体迁移进度百分比 */
    val migrationProgress: Float
        get() = if (scanResults.isEmpty()) 0f else fixedCount.toFloat() / scanResults.size
}

// ================================================================
// Intent / 意图
// ================================================================

/**
 * ============================================================
 * CoreKtxMigrationIntent — 用户操作意图
 * ================================================================
 * Sealed class representing all possible user intentions.
 * ViewModel receives intents via sendIntent() and processes them.
 */
sealed class CoreKtxMigrationIntent {
    // ─────────────────────────────────────────────────────────
    // Scanning / 扫描
    // ─────────────────────────────────────────────────────────
    /** 开始扫描项目中的 core-ktx 使用情况 */
    data object StartScan : CoreKtxMigrationIntent()

    /** 刷新扫描结果 */
    data object RefreshScan : CoreKtxMigrationIntent()

    // ─────────────────────────────────────────────────────────
    // Filtering / 过滤
    // ─────────────────────────────────────────────────────────
    /** 按严重程度过滤扫描结果 */
    data class FilterBySeverity(val severity: Set<Severity>) : CoreKtxMigrationIntent()

    // ─────────────────────────────────────────────────────────
    // Fixing / 修复
    // ─────────────────────────────────────────────────────────
    /** 修复单个问题项 */
    data class FixItem(val itemId: String) : CoreKtxMigrationIntent()

    /** 确认执行修复操作 */
    data object ConfirmFix : CoreKtxMigrationIntent()

    /** 取消修复操作 */
    data object CancelFix : CoreKtxMigrationIntent()

    /** 修复所有符合条件的问题项 */
    data object FixAll : CoreKtxMigrationIntent()

    // ─────────────────────────────────────────────────────────
    // Validation / 验证
    // ─────────────────────────────────────────────────────────
    /** 验证指定路径的 Kotlin 扩展函数 import 兼容性 */
    data class ValidateImports(val path: String) : CoreKtxMigrationIntent()

    /** 清空验证结果 */
    data object ClearValidation : CoreKtxMigrationIntent()

    // ─────────────────────────────────────────────────────────
    // Settings / 设置
    // ─────────────────────────────────────────────────────────
    /** 更新迁移工具设置 */
    data class UpdateSettings(val settings: MigrationSettings) : CoreKtxMigrationIntent()

    // ─────────────────────────────────────────────────────────
    // Navigation / 导航
    // ─────────────────────────────────────────────────────────
    /** 切换 Tab 页 */
    data class SelectTab(val tabIndex: Int) : CoreKtxMigrationIntent()
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * ============================================================
 * CoreKtxMigrationEffect — 一次性副作用
 * ================================================================
 * One-time side effects triggered by ViewModel.
 * Delivered via Channel<UiEffect> and consumed by the UI layer.
 */
sealed class CoreKtxMigrationEffect {
    /** 显示 Toast 消息 */
    data class ShowToast(val message: String) : CoreKtxMigrationEffect()

    /** 显示修复确认对话框 */
    data class ShowFixConfirmation(
        val item: ScanResult,
        val onConfirm: () -> Unit,
        val onDismiss: () -> Unit
    ) : CoreKtxMigrationEffect()

    /** 扫描完成通知 */
    data class ScanCompleted(
        val totalFound: Int,
        val criticalCount: Int
    ) : CoreKtxMigrationEffect()

    /** 修复完成通知 */
    data class FixCompleted(val fixedCount: Int) : CoreKtxMigrationEffect()

    /** 复制到剪贴板成功 */
    data object CopiedToClipboard : CoreKtxMigrationEffect()

    /** 导航到报告详情 */
    data class NavigateToReport(val reportPath: String) : CoreKtxMigrationEffect()

    /** 显示错误消息 */
    data class ShowError(val message: String) : CoreKtxMigrationEffect()
}
