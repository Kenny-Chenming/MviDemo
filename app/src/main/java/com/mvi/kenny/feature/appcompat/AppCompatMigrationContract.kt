package com.mvi.kenny.feature.appcompat

// AppCompatMigrationContract — PRD-114
enum class ScanState { Idle, Scanning, Completed, Failed }
enum class ViolationSeverity(val order: Int) { P0(0), P1(1), P2(2) }
enum class ReportType { MinSdkImpact, JSpecifyCompliance }
enum class ExportFormat { PDF, JSON, HTML }

data class ModuleMinSdkImpact(val moduleName: String, val currentMinSdk: Int, val affectedByAppCompat: Boolean, val suggestedMinSdk: Int = 23, val dependencyChain: List<String> = emptyList(), val activityVersion: String = "")
data class JSpecifyViolation(val id: String, val filePath: String, val line: Int, val currentAnnotation: String, val suggestedAnnotation: String, val severity: ViolationSeverity, val codeSnippet: String = "", val suggestion: String = "")
data class KGPVersionStatus(val kotlinVersion: String, val kgpVersion: String, val isCompatible: Boolean, val upgradePath: String? = null, val isKotlin2Required: Boolean = false)
data class ComplianceReport(val scanTimestamp: String, val module: String, val totalFiles: Int, val violations: List<JSpecifyViolation>, val coverage: Float, val recommendation: String)
data class AppCompatMigrationSettings(val autoFixEnabled: Boolean = true, val backupEnabled: Boolean = true, val minSeverityForFix: ViolationSeverity = ViolationSeverity.P1, val enableActivityCheck: Boolean = true, val jspecifyAnnotationsStrict: Boolean = false, val scanExcludePaths: List<String> = listOf("build/", ".gradle/"))
data class AppCompatMigrationState(val scanState: ScanState = ScanState.Idle, val healthScore: Int = 100, val selectedTab: Int = 0, val minSdkAnalysis: List<ModuleMinSdkImpact> = emptyList(), val jspecifyViolations: List<JSpecifyViolation> = emptyList(), val kgpVersionStatus: KGPVersionStatus = KGPVersionStatus("Unknown", "Unknown", false), val complianceReport: ComplianceReport? = null, val settings: AppCompatMigrationSettings = AppCompatMigrationSettings(), val isLoading: Boolean = false, val error: String? = null, val selectedViolation: JSpecifyViolation? = null, val showViolationDialog: Boolean = false, val selectedModule: ModuleMinSdkImpact? = null, val showModuleSheet: Boolean = false, val filterSeveritySet: Set<ViolationSeverity> = ViolationSeverity.entries.toSet(), val jspecifyCoverage: Float = 1.0f, val totalModulesAffected: Int = 0) {
    val filteredViolations get() = jspecifyViolations.filter { it.severity in filterSeveritySet }
    val p0Count get() = jspecifyViolations.count { it.severity == ViolationSeverity.P0 }
    val p1Count get() = jspecifyViolations.count { it.severity == ViolationSeverity.P1 }
    val p2Count get() = jspecifyViolations.count { it.severity == ViolationSeverity.P2 }
    val affectedModulesCount get() = minSdkAnalysis.count { it.affectedByAppCompat && it.currentMinSdk < 23 }
}
sealed class AppCompatMigrationIntent {
    data object StartFullScan : AppCompatMigrationIntent()
    data object RefreshScan : AppCompatMigrationIntent()
    data class AnalyzeMinSdkImpact(val moduleName: String) : AppCompatMigrationIntent()
    data class ShowModuleDetail(val module: ModuleMinSdkImpact) : AppCompatMigrationIntent()
    data object DismissModuleSheet : AppCompatMigrationIntent()
    data class ScanJSpecifyViolations(val packageName: String) : AppCompatMigrationIntent()
    data class FilterBySeverity(val severity: Set<ViolationSeverity>) : AppCompatMigrationIntent()
    data class ShowViolationDetail(val violation: JSpecifyViolation) : AppCompatMigrationIntent()
    data object DismissViolationDialog : AppCompatMigrationIntent()
    data object CheckKGPVersion : AppCompatMigrationIntent()
    data class ExportReport(val reportType: ReportType, val format: ExportFormat) : AppCompatMigrationIntent()
    data class UpdateSettings(val settings: AppCompatMigrationSettings) : AppCompatMigrationIntent()
    data class SelectTab(val tabIndex: Int) : AppCompatMigrationIntent()
}
sealed class AppCompatMigrationEffect {
    data class ShowToast(val message: String) : AppCompatMigrationEffect()
    data class ShowViolationDetail(val violation: JSpecifyViolation) : AppCompatMigrationEffect()
    data class ReportExported(val filePath: String) : AppCompatMigrationEffect()
    data object ScanCompleted : AppCompatMigrationEffect()
    data class ShowError(val message: String) : AppCompatMigrationEffect()
    data object CopiedToClipboard : AppCompatMigrationEffect()
}