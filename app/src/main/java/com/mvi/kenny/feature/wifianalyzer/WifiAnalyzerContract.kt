package com.mvi.kenny.feature.wifianalyzer

import androidx.compose.ui.graphics.Color

// =============================================================
// WifiAnalyzerContract — Android 17 Wi-Fi Analyzer MVI Contract
// =============================================================

/** Tool module tab / 功能模块 Tab */
enum class WifiTab(val title: String) {
    Dashboard("Dashboard"), Scanner("Scanner"), Alternatives("Alternatives"), Reports("Reports"), Settings("Settings")
}

/** Issue severity / 问题严重程度 */
enum class Severity(val label: String, val priority: Int, val color: Color) {
    P0("P0 — 严重", 0, Color(0xFFB3261E)),
    P1("P1 — 警告", 1, Color(0xFFF57C00)),
    P2("P2 — 提示", 2, Color(0xFFF1C40F))
}

/** Alternative solution type / 替代方案类型 */
enum class AlternativeType(val title: String) {
    WIFI_RTT("Wi-Fi RTT"), NEIGHBOR_REPORTING("Neighbor Reporting"), DEGRADATION_STRATEGY("降级策略")
}

/** Report export format / 报告导出格式 */
enum class ExportFormat(val label: String) { PDF("PDF"), JSON("JSON"), MARKDOWN("Markdown") }

/** Affected Wi-Fi API */
data class AffectedApi(val name: String, val description: String, val minApiLevel: Int)

/** Wi-Fi API scan result */
data class ScanResult(val id: String, val filePath: String, val lineNumber: Int, val methodName: String, val className: String, val apiName: String, val severity: Severity, val codeSnippet: String, val suggestedFix: String)

/** Recent scan record */
data class RecentScan(val id: String, val timestamp: Long, val score: Int, val moduleName: String)

/** Migration checklist item */
data class ChecklistItem(val id: String, val description: String, val isChecked: Boolean = false)

/** Impact level report */
data class ImpactReport(val id: String, val moduleName: String, val generatedAt: Long, val p0Count: Int, val p1Count: Int, val p2Count: Int, val affectedFeatures: List<String>, val reportContent: String)

/** Scan configuration */
data class ScanConfig(val scanScope: ScanScope = ScanScope.MAIN_ONLY, val includeDependencies: Boolean = false, val targetApiLevel: Int = 17)

/** Scan scope */
enum class ScanScope(val label: String) { MAIN_ONLY("仅主模块 / Main Only"), WITH_DEPENDENCIES("包含依赖 / With Dependencies"), FULL("全量 / Full") }

/** Wi-Fi Analyzer State / Wi-Fi Analyzer 工具页面状态 */
data class WifiAnalyzerState(
    val currentTab: WifiTab = WifiTab.Dashboard, val healthScore: Int = 0, val affectedApis: List<AffectedApi> = emptyList(),
    val scanResults: List<ScanResult> = emptyList(), val scanProgress: Float = 0f, val isScanning: Boolean = false,
    val selectedSeverity: Severity? = null, val selectedModule: String? = null, val alternativeType: AlternativeType = AlternativeType.WIFI_RTT,
    val migrationChecklist: List<ChecklistItem> = emptyList(), val reports: List<ImpactReport> = emptyList(),
    val isGeneratingReport: Boolean = false, val exportFormat: ExportFormat = ExportFormat.PDF, val scanConfig: ScanConfig = ScanConfig(),
    val bannerDismissed: Boolean = false, val recentScans: List<RecentScan> = emptyList(), val isExporting: Boolean = false, val error: String? = null
) {
    companion object { val Initial = WifiAnalyzerState() }
    val filteredScanResults: List<ScanResult> get() = scanResults.filter { selectedSeverity == null || it.severity == selectedSeverity }.sortedBy { it.severity.priority }
    val p0Count: Int get() = scanResults.count { it.severity == Severity.P0 }
    val p1Count: Int get() = scanResults.count { it.severity == Severity.P1 }
    val p2Count: Int get() = scanResults.count { it.severity == Severity.P2 }
    val scanPercentage: Int get() = (scanProgress * 100).toInt()
}

/** Wi-Fi Analyzer User Intents */
sealed interface WifiAnalyzerIntent {
    data class SwitchTab(val tab: WifiTab) : WifiAnalyzerIntent
    data class StartScan(val modulePath: String) : WifiAnalyzerIntent
    data object CancelScan : WifiAnalyzerIntent
    data class FilterBySeverity(val severity: Severity?) : WifiAnalyzerIntent
    data class SelectModule(val module: String) : WifiAnalyzerIntent
    data class SelectAlternativeType(val type: AlternativeType) : WifiAnalyzerIntent
    data class ToggleChecklistItem(val itemId: String) : WifiAnalyzerIntent
    data object GenerateReport : WifiAnalyzerIntent
    data class ExportReport(val format: ExportFormat) : WifiAnalyzerIntent
    data object DismissBanner : WifiAnalyzerIntent
    data class UpdateScanConfig(val config: ScanConfig) : WifiAnalyzerIntent
    data object DismissError : WifiAnalyzerIntent
}

/** Wi-Fi Analyzer Side Effects */
sealed interface WifiAnalyzerEffect {
    data class ShowToast(val message: String) : WifiAnalyzerEffect
    data class ReportExported(val file: String) : WifiAnalyzerEffect
    data object ScanComplete : WifiAnalyzerEffect
    data class NavigateToCode(val filePath: String, val lineNumber: Int) : WifiAnalyzerEffect
    data class ShowConfirmDialog(val title: String, val message: String, val onConfirm: () -> Unit) : WifiAnalyzerEffect
}
