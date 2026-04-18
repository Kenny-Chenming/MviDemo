package com.mvi.kenny.feature.wifianalyzer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// ==================== Wi-Fi Analyzer MVI Contract ====================
// Wi-Fi Analyzer MVI 合约 — 用于 Android 17 Wi-Fi API 兼容性检测

// ==================== Tab 枚举 ====================
enum class WifiTab(val title: String) {
    Dashboard("仪表盘"),
    Scanner("API扫描"),
    Alternatives("替代方案"),
    Reports("报告"),
    Settings("设置")
}

// ==================== 影响级别 ====================
enum class Severity(val label: String, val color: Color) {
    P0("P0-严重", Color(0xFFF44336)),
    P1("P1-警告", Color(0xFFFF9800)),
    P2("P2-提示", Color(0xFF2196F3))
}

// ==================== 替代方案类型 ====================
enum class AlternativeType(val title: String) {
    WIFI_RTT("Wi-Fi RTT"),
    NEIGHBOR_REPORTING("Neighbor Reporting"),
    DEGRADATION_STRATEGY("降级策略")
}

// ==================== 导出格式 ====================
enum class ExportFormat(val label: String) {
    PDF("PDF"),
    JSON("JSON"),
    MARKDOWN("Markdown")
}

// ==================== 扫描范围 ====================
enum class ScanScope(val label: String) {
    ALL("全部"),
    FEATURE("功能模块"),
    CORE("核心模块")
}

// ==================== 数据类 ====================
data class AffectedApi(
    val name: String,
    val description: String,
    val minApiLevel: Int
)

data class ScanResult(
    val id: String,
    val apiName: String,
    val severity: Severity,
    val filePath: String,
    val className: String,
    val methodName: String,
    val lineNumber: Int,
    val codeSnippet: String,
    val suggestedFix: String
)

data class RecentScan(
    val moduleName: String,
    val timestamp: Long,
    val score: Int
)

data class ChecklistItem(
    val id: String,
    val description: String,
    val isChecked: Boolean = false
)

data class ImpactReport(
    val id: String,
    val title: String,
    val summary: String,
    val severityCounts: Map<Severity, Int>,
    val affectedFeatures: List<String>,
    val generatedAt: Long
)

data class ScanConfig(
    val scanScope: ScanScope = ScanScope.ALL,
    val targetApiLevel: Int = 17,
    val includeTestSources: Boolean = false
)

// ==================== State ====================
data class WifiAnalyzerState(
    val currentTab: WifiTab = WifiTab.Dashboard,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val selectedModule: String? = null,
    val selectedSeverity: Severity? = null,
    val scanResults: List<ScanResult> = emptyList(),
    val filteredScanResults: List<ScanResult> = emptyList(),
    val affectedApis: List<AffectedApi> = emptyList(),
    val recentScans: List<RecentScan> = emptyList(),
    val healthScore: Int = 0,
    val p0Count: Int = 0,
    val p1Count: Int = 0,
    val p2Count: Int = 0,
    val alternativeType: AlternativeType = AlternativeType.WIFI_RTT,
    val migrationChecklist: List<ChecklistItem> = emptyList(),
    val reports: List<ImpactReport> = emptyList(),
    val isGeneratingReport: Boolean = false,
    val isExporting: Boolean = false,
    val scanConfig: ScanConfig = ScanConfig(),
    val bannerDismissed: Boolean = false
)

// ==================== Intent ====================
sealed class WifiAnalyzerIntent {
    data class SwitchTab(val tab: WifiTab) : WifiAnalyzerIntent()
    data object DismissBanner : WifiAnalyzerIntent()
    data class StartScan(val module: String) : WifiAnalyzerIntent()
    data object CancelScan : WifiAnalyzerIntent()
    data class FilterBySeverity(val severity: Severity?) : WifiAnalyzerIntent()
    data class SelectModule(val module: String) : WifiAnalyzerIntent()
    data class SelectAlternativeType(val type: AlternativeType) : WifiAnalyzerIntent()
    data class ToggleChecklistItem(val id: String) : WifiAnalyzerIntent()
    data object GenerateReport : WifiAnalyzerIntent()
    data class ExportReport(val format: ExportFormat) : WifiAnalyzerIntent()
    data class UpdateScanConfig(val config: ScanConfig) : WifiAnalyzerIntent()
}

// ==================== Effect ====================
sealed class WifiAnalyzerEffect {
    data class ShowToast(val message: String) : WifiAnalyzerEffect()
    data class ReportExported(val file: String) : WifiAnalyzerEffect()
    data object ScanComplete : WifiAnalyzerEffect()
    data class NavigateToCode(val filePath: String, val lineNumber: Int) : WifiAnalyzerEffect()
    data object ShowConfirmDialog : WifiAnalyzerEffect()
}
