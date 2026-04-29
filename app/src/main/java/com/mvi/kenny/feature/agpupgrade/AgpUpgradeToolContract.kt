package com.mvi.kenny.feature.agpupgrade

/**
 * ============================================================
 * PRD-200 | Compose 1.12.0 compileSdk 37 & AGP 9.2 强制升级工具包
 * MVI Contract — Defines State, Intent, and Effect
 * ============================================================
 *
 * Design Doc: memory/agency/designs/PRD-200-Compose-1.12.0-AGP-9.2-升级工具包.md
 * Status: 设计完成，待移交开发 | Completed: 2026-04-29
 *
 * 功能：AGP 版本现状扫描器、AGP 9.2 升级路径生成器、AGP 9.x 破坏性变更检查清单、
 *       AGP/Gradle 兼容性验证工具、Compose 1.12.0 CI 合规检测工具、AGP 升级失败诊断工具、
 *       AGP 9.2 降级回退策略、compileSdk 36→37 行为变更指南
 */

// ============ Enums & Data Models ============
// ============ 枚举和数据模型 ============

/**
 * Tool Tab — 工具的 5 个主要模块 Tab
 * Tool Tab — 5 main modules of the toolkit
 */
enum class ToolTab(val title: String, val titleZh: String) {
    Scanner("Scanner", "扫描器"),
    Wizard("Upgrade Wizard", "升级向导"),
    Changes("Breaking Changes", "变更清单"),
    Diagnostics("Diagnostic Tool", "诊断工具"),
    Compliance("Compliance Check", "合规检测")
}

/**
 * Wizard Step — 升级向导的分步流程
 * Wizard Step — step-by-step upgrade wizard flow
 */
enum class WizardStep(val step: Int, val title: String, val titleZh: String) {
    SelectCurrent(1, "Select Current Version", "选择当前版本"),
    ShowPath(2, "Upgrade Path", "显示升级路径"),
    ShowConfig(3, "Generated Config", "生成配置"),
    CICommands(4, "CI Commands", "CI 命令")
}

/**
 * Risk Level — 紧迫性/风险等级
 * Risk Level — urgency/risk classification
 */
enum class RiskLevel(val label: String, val labelZh: String) {
    P0("🔴 P0 BLOCKER", "🔴 P0 阻断"),
    P1("🟡 P1 Important", "🟡 P1 重要"),
    P2("🟢 P2 Notice", "🟢 P2 注意"),
    Pass("✅ Up to Date", "✅ 已是最新")
}

/**
 * Compliance Status — 合规检测结果
 * Compliance Status — compliance check result
 */
enum class ComplianceStatus {
    Compliant,      // 完全合规
    Partial,        // 部分合规
    NonCompliant    // 不合规
}

// ============ Scan Result Models ============
// ============ 扫描结果模型 ============

/**
 * Project version info extracted from build.gradle / Gradle files
 * 从 build.gradle/Gradle 文件提取的项目版本信息
 */
data class ProjectVersionInfo(
    val agpVersion: String = "Unknown",
    val gradleVersion: String = "Unknown",
    val kotlinVersion: String = "Unknown",
    val composeVersion: String = "Unknown",
    val compileSdk: Int = 0,
    val targetSdk: Int = 0,
    val minSdk: Int = 0
)

/**
 * Scan result with risk assessment
 * 含风险评估的扫描结果
 */
data class ScanResult(
    val projectPath: String,
    val versionInfo: ProjectVersionInfo,
    val riskLevel: RiskLevel,
    val upgradeRecommendations: List<String>,
    val blockingIssues: List<String>,
    val compatibleVersions: CompatibleVersions?
)

/**
 * Recommended compatible version combination
 * 推荐的兼容版本组合
 */
data class CompatibleVersions(
    val recommendedAGP: String,
    val recommendedGradle: String,
    val recommendedKotlin: String? = null
)

// ============ Upgrade Path Models ============
// ============ 升级路径模型 ============

/**
 * Single step in the upgrade path
 * 升级路径中的单步
 */
data class UpgradeStep(
    val fromVersion: String,
    val toVersion: String,
    val breakingChanges: List<String>,
    val riskLevel: RiskLevel,
    val keyNotes: String
)

/**
 * Upgrade wizard state
 * 升级向导状态
 */
data class WizardState(
    val currentStep: WizardStep = WizardStep.SelectCurrent,
    val currentAGPVersion: String = "",
    val targetAGPVersion: String = "9.2.0",
    val upgradePath: List<UpgradeStep> = emptyList(),
    val generatedGradleWrapper: String = "",
    val generatedBuildGradle: String = "",
    val generatedCICommands: String = ""
)

// ============ Breaking Change Models ============
// ============ 破坏性变更模型 ============

/**
 * Single breaking change item
 * 单条破坏性变更
 */
data class BreakingChangeItem(
    val id: String,
    val category: String,           // "AGP 9.0" / "AGP 9.1" / "AGP 9.2" / "compileSdk 37"
    val title: String,
    val titleZh: String,
    val description: String,
    val descriptionZh: String,
    val codeBefore: String? = null,
    val codeAfter: String? = null,
    val riskLevel: RiskLevel,
    val affectedScope: String,
    val migrationGuide: String? = null
)

// ============ Diagnostic Models ============
// ============ 诊断模型 ============

/**
 * Diagnostic error category
 * 诊断错误分类
 */
enum class DiagnosticCategory {
    AGP_VERSION_INCOMPATIBLE,      // AGP 版本不兼容
    GRADLE_VERSION_MISMATCH,       // Gradle 版本不匹配
    KOTLIN_VERSION_ISSUE,         // Kotlin 版本问题
    COMPOSE_DEPENDENCY_CONFLICT,   // Compose 依赖冲突
    COMPILE_SDK_TOO_LOW,           // compileSdk 版本过低
    UNKNOWN                         // 未知错误
}

/**
 * Diagnostic result
 * 诊断结果
 */
data class DiagnosisResult(
    val category: DiagnosticCategory,
    val summary: String,
    val summaryZh: String,
    val rootCause: String,
    val rootCauseZh: String,
    val fixSuggestions: List<String>,
    val fixSuggestionsZh: List<String>,
    val referenceLinks: List<String>
)

// ============ Compliance Models ============
// ============ 合规检测模型 ============

/**
 * Single compliance check item
 * 单条合规检测项
 */
data class ComplianceCheckItem(
    val name: String,
    val nameZh: String,
    val checkRule: String,
    val actualValue: String,
    val expectedValue: String,
    val status: ComplianceStatus,
    val fixSuggestion: String,
    val fixSuggestionZh: String
)

/**
 * Full compliance report
 * 完整合规报告
 */
data class ComplianceReport(
    val projectPath: String,
    val overallStatus: ComplianceStatus,
    val checkItems: List<ComplianceCheckItem>,
    val reportTimestamp: Long = System.currentTimeMillis(),
    val isCI: Boolean = false
)

// ============ Scanner State ============
// ============ 扫描器状态 ============

data class ScannerState(
    val projectPath: String = "",
    val isScanning: Boolean = false,
    val scanResult: ScanResult? = null,
    val error: String? = null
)

// ============ Diagnostics State ============
// ============ 诊断工具状态 ============

data class DiagnosticsState(
    val errorLog: String = "",
    val isAnalyzing: Boolean = false,
    val diagnosisResult: DiagnosisResult? = null,
    val error: String? = null
)

// ============ Compliance State ============
// ============ 合规检测状态 ============

data class ComplianceState(
    val projectPath: String = "",
    val isChecking: Boolean = false,
    val report: ComplianceReport? = null,
    val logOutput: String = "",
    val error: String? = null
)

// ============ Changes/Filter State ============
// ============ 变更清单/筛选状态 ============

data class ChangesState(
    val breakingChanges: List<BreakingChangeItem> = emptyList(),
    val filteredChanges: List<BreakingChangeItem> = emptyList(),
    val searchQuery: String = "",
    val selectedRiskFilter: RiskLevel? = null,
    val selectedCategoryFilter: String? = null,
    val expandedChangeId: String? = null
)

// ============ Main State ============
// ============ 主状态 ============

/**
 * Overall UI state for AGP Upgrade Tool
 * AGP 升级工具的整体 UI 状态
 */
data class AgpUpgradeToolState(
    val currentTab: ToolTab = ToolTab.Scanner,
    val scanner: ScannerState = ScannerState(),
    val wizard: WizardState = WizardState(),
    val diagnostics: DiagnosticsState = DiagnosticsState(),
    val compliance: ComplianceState = ComplianceState(),
    val changes: ChangesState = ChangesState()
)

// ============ Intent ============
// ============ 用户意图 ============

sealed class AgpUpgradeToolIntent {
    // Tab navigation / Tab 切换
    data class SwitchTab(val tab: ToolTab) : AgpUpgradeToolIntent()

    // ---- Scanner ----
    data class UpdateScannerPath(val path: String) : AgpUpgradeToolIntent()
    data object StartScan : AgpUpgradeToolIntent()
    data object ClearScanResult : AgpUpgradeToolIntent()

    // ---- Wizard ----
    data class SelectCurrentVersion(val version: String) : AgpUpgradeToolIntent()
    data object NextWizardStep : AgpUpgradeToolIntent()
    data object PrevWizardStep : AgpUpgradeToolIntent()
    data object GenerateConfig : AgpUpgradeToolIntent()
    data class UpdateWizardPath(val path: String) : AgpUpgradeToolIntent()

    // ---- Breaking Changes ----
    data class UpdateSearchQuery(val query: String) : AgpUpgradeToolIntent()
    data class FilterByRisk(val risk: RiskLevel?) : AgpUpgradeToolIntent()
    data class FilterByCategory(val category: String?) : AgpUpgradeToolIntent()
    data class ToggleChangeExpanded(val id: String) : AgpUpgradeToolIntent()

    // ---- Diagnostics ----
    data class UpdateErrorLog(val log: String) : AgpUpgradeToolIntent()
    data object AnalyzeLog : AgpUpgradeToolIntent()
    data object ClearDiagnosis : AgpUpgradeToolIntent()

    // ---- Compliance ----
    data class UpdateCompliancePath(val path: String) : AgpUpgradeToolIntent()
    data object RunComplianceCheck : AgpUpgradeToolIntent()
    data object ExportComplianceReport : AgpUpgradeToolIntent()
    data object ClearComplianceReport : AgpUpgradeToolIntent()
}

// ============ Effect ============
// ============ 副作用（一次性事件）===========

sealed class AgpUpgradeToolEffect {
    data class ShowToast(val message: String) : AgpUpgradeToolEffect()
    data class CopyToClipboard(val text: String) : AgpUpgradeToolEffect()
    data class OpenExternalLink(val url: String) : AgpUpgradeToolEffect()
    data class ExportReport(val content: String, val filename: String) : AgpUpgradeToolEffect()
}
