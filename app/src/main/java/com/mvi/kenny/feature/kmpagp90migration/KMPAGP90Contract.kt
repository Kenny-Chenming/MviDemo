package com.mvi.kenny.feature.kmpagp90migration

/**
 * PRD-231 | KMP × AGP 9.0 不兼容迁移工具包
 * MVI Contract — Defines State, Intent, and Effect
 *
 * Design Doc: memory/agency/designs/PRD-231-KMP-AGP-9-0-不兼容迁移工具包.md
 * Status: 设计完成，待移交开发 | Completed: 2026-05-05
 */

// ============ Data Models ============
// ============ 数据模型 ============

/**
 * Urgency level for migration issues
 * 迁移问题紧迫性级别
 */
enum class UrgencyLevel { LOW, MEDIUM, HIGH, CRITICAL }

/**
 * Scan status for project analysis
 * 项目扫描状态
 */
enum class ScanStatus { IDLE, SCANNING, COMPLETED, ERROR }

/**
 * Migration order recommendation
 * 迁移顺序建议
 */
enum class MigrationOrder {
    KSP2_FIRST,
    AGP90_FIRST,
    PARALLEL
}

/**
 * CI compliance status
 * CI 合规状态
 */
enum class ComplianceStatus { UNKNOWN, COMPLIANT, NON_COMPLIANT, PARTIAL }

/**
 * Incompatible plugin detection result
 * 不兼容插件检测结果
 */
data class IncompatiblePlugin(
    val moduleName: String,
    val modulePath: String,
    val currentPlugins: List<String>,
    val incompatiblePair: String,
    val urgency: UrgencyLevel,
    val suggestion: String,
    val migrationStep: String
)

/**
 * KMP module structure for split preview
 * KMP 模块结构（拆分预览用）
 */
data class ModuleStructure(
    val moduleName: String,
    val modulePath: String,
    val hasAndroidApp: Boolean,
    val hasComposeApp: Boolean,
    val hasShared: Boolean,
    val files: List<String>
)

/**
 * Module split diff result
 * 模块拆分差异结果
 */
data class ModuleSplitDiff(
    val originalModule: String,
    val newAndroidApp: String,
    val newComposeApp: String,
    val movedFiles: List<String>,
    val settingsChanges: String
)

/**
 * Plugin migration step
 * 插件迁移步骤
 */
data class MigrationStep(
    val order: Int,
    val title: String,
    val titleCn: String,
    val description: String,
    val descriptionCn: String,
    val codeBefore: String,
    val codeAfter: String,
    val file: String
)

/**
 * Before/After diff for plugin migration
 * 插件迁移前后对比
 */
data class BeforeAfterDiff(
    val pluginBlockBefore: String,
    val pluginBlockAfter: String,
    val androidBlockBefore: String,
    val androidBlockAfter: String,
    val file: String
)

/**
 * Gradle breaking change item
 * Gradle breaking change 条目
 */
data class GradleBreakingChange(
    val version: String,
    val change: String,
    val changeCn: String,
    val impact: String,
    val impactCn: String,
    val severity: UrgencyLevel
)

/**
 * Countdown warning for AGP 10.0
 * AGP 10.0 倒计时警告
 */
data class CountdownWarning(
    val daysRemaining: Int,
    val isUrgent: Boolean,
    val message: String,
    val messageCn: String
)

// ============ State ============
// ============ 状态定义 ============

/**
 * Overall UI state for KMP × AGP 9.0 Migration Toolkit
 * KMP × AGP 9.0 不兼容迁移工具包的整体 UI 状态
 */
data class KMPAGP90State(
    // Tab navigation / Tab 导航
    val selectedTab: Int = 0,

    // Tab 1: 不兼容检测扫描器
    val projectPath: String = "",
    val isScanning: Boolean = false,
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanResults: List<IncompatiblePlugin> = emptyList(),
    val scanUrgency: UrgencyLevel = UrgencyLevel.MEDIUM,
    val scanError: String? = null,

    // Tab 2: KMP 模块拆分规划
    val currentStructure: ModuleStructure? = null,
    val splitDiff: ModuleSplitDiff? = null,
    val isAnalyzing: Boolean = false,

    // Tab 3: Android KMP Library Plugin 迁移指南
    val pluginMigrationSteps: List<MigrationStep> = emptyList(),
    val beforeAfterDiff: BeforeAfterDiff? = null,
    val selectedStep: Int = 0,

    // Tab 4: Built-in Kotlin + Gradle 9.0 迁移
    val builtInKotlinDetected: Boolean = false,
    val kotlinPluginExplicit: Boolean = false,
    val currentGradleVersion: String = "",
    val gradleBreakingChanges: List<GradleBreakingChange> = emptyList(),
    val recommendedMigrationOrder: MigrationOrder = MigrationOrder.KSP2_FIRST,

    // Tab 5: AGP 10.0 倒计时 + CI 合规
    val builtInKotlinUsed: Boolean = false,
    val countdownWarning: CountdownWarning? = null,
    val ciComplianceStatus: ComplianceStatus = ComplianceStatus.UNKNOWN,
    val ciMigrationProgress: Int = 0,
    val migrationComplete: Boolean = false,

    // Shared UI State
    val expandedCardId: String? = null
)

// ============ Intent ============
// ============ 用户意图 ============

sealed class KMPAGP90Intent {
    data class SelectTab(val index: Int) : KMPAGP90Intent()
    data class UpdateProjectPath(val path: String) : KMPAGP90Intent()
    object StartScan : KMPAGP90Intent()
    object CancelScan : KMPAGP90Intent()
    data class ToggleResultExpand(val id: String) : KMPAGP90Intent()
    object AnalyzeStructure : KMPAGP90Intent()
    object GenerateSplitDiff : KMPAGP90Intent()
    data class SelectMigrationStep(val index: Int) : KMPAGP90Intent()
    object ShowBeforeAfterDiff : KMPAGP90Intent()
    object DetectBuiltInKotlin : KMPAGP90Intent()
    data class SetMigrationOrder(val order: MigrationOrder) : KMPAGP90Intent()
    object ValidateCICompliance : KMPAGP90Intent()
    object MarkMigrationComplete : KMPAGP90Intent()
}

// ============ Effect ============
// ============ 副作用（一次性事件）===========

sealed class KMPAGP90Effect {
    data class ShowError(val message: String) : KMPAGP90Effect()
    data class ShowToast(val message: String) : KMPAGP90Effect()
    object NavigateToTab : KMPAGP90Effect()
    data class OpenExternalLink(val url: String) : KMPAGP90Effect()
    object ScanCompleted : KMPAGP90Effect()
    object ComplianceCheckPassed : KMPAGP90Effect()
}
