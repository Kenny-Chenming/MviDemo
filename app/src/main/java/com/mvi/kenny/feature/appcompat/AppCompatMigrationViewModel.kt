package com.mvi.kenny.feature.appcompat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AppCompatMigrationViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppCompatMigrationState())
    val state: StateFlow<AppCompatMigrationState> = _state.asStateFlow()
    private val _effect = Channel<AppCompatMigrationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

    fun sendIntent(intent: AppCompatMigrationIntent) {
        when (intent) {
            is AppCompatMigrationIntent.StartFullScan -> startFullScan()
            is AppCompatMigrationIntent.RefreshScan -> startFullScan()
            is AppCompatMigrationIntent.AnalyzeMinSdkImpact -> analyzeMinSdkImpact(intent.moduleName)
            is AppCompatMigrationIntent.ShowModuleDetail -> showModuleDetail(intent.module)
            is AppCompatMigrationIntent.DismissModuleSheet -> dismissModuleSheet()
            is AppCompatMigrationIntent.ScanJSpecifyViolations -> scanJSpecifyViolations(intent.packageName)
            is AppCompatMigrationIntent.FilterBySeverity -> filterBySeverity(intent.severity)
            is AppCompatMigrationIntent.ShowViolationDetail -> showViolationDetail(intent.violation)
            is AppCompatMigrationIntent.DismissViolationDialog -> dismissViolationDialog()
            is AppCompatMigrationIntent.CheckKGPVersion -> checkKGPVersion()
            is AppCompatMigrationIntent.ExportReport -> exportReport(intent.reportType, intent.format)
            is AppCompatMigrationIntent.UpdateSettings -> updateSettings(intent.settings)
            is AppCompatMigrationIntent.SelectTab -> selectTab(intent.tabIndex)
        }
    }

    private fun startFullScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(scanState = ScanState.Scanning, isLoading = true, error = null)
            try {
                delay(300); val mockMinSdkAnalysis = generateMockMinSdkAnalysis()
                delay(300); val mockViolations = generateMockJSpecifyViolations()
                delay(300); val mockKGPStatus = generateMockKGPStatus()
                delay(200)
                val healthScore = calculateHealthScore(mockViolations.count { it.severity == ViolationSeverity.P0 }, mockViolations.count { it.severity == ViolationSeverity.P1 }, mockMinSdkAnalysis.count { it.affectedByAppCompat && it.currentMinSdk < 23 }, mockKGPStatus.isCompatible)
                val complianceReport = ComplianceReport(scanTimestamp = isoDateFormat.format(Date()), module = "app", totalFiles = 150, violations = mockViolations, coverage = 1.0f - (mockViolations.size.toFloat() / 150f), recommendation = "建议使用 JSpecify 注解并启用 -Xjspecify-annotations=strict")
                _state.value = _state.value.copy(scanState = ScanState.Completed, minSdkAnalysis = mockMinSdkAnalysis, jspecifyViolations = mockViolations, kgpVersionStatus = mockKGPStatus, healthScore = healthScore, complianceReport = complianceReport, jspecifyCoverage = complianceReport.coverage, totalModulesAffected = mockMinSdkAnalysis.count { it.affectedByAppCompat }, isLoading = false)
                _effect.send(AppCompatMigrationEffect.ScanCompleted)
            } catch (e: Exception) {
                _state.value = _state.value.copy(scanState = ScanState.Failed, isLoading = false, error = e.message)
                _effect.send(AppCompatMigrationEffect.ShowError(e.message ?: "Error"))
            }
        }
    }

    private fun analyzeMinSdkImpact(moduleName: String) {
        viewModelScope.launch { _state.value = _state.value.copy(isLoading = true); delay(500); val mockAnalysis = generateMockMinSdkAnalysis().filter { it.moduleName == moduleName }; _state.value = _state.value.copy(minSdkAnalysis = mockAnalysis, isLoading = false) }
    }
    private fun showModuleDetail(module: ModuleMinSdkImpact) { _state.value = _state.value.copy(selectedModule = module, showModuleSheet = true) }
    private fun dismissModuleSheet() { _state.value = _state.value.copy(selectedModule = null, showModuleSheet = false) }
    private fun scanJSpecifyViolations(packageName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(scanState = ScanState.Scanning, isLoading = true); delay(800)
            val mockViolations = generateMockJSpecifyViolations()
            val healthScore = calculateHealthScore(mockViolations.count { it.severity == ViolationSeverity.P0 }, mockViolations.count { it.severity == ViolationSeverity.P1 }, _state.value.minSdkAnalysis.count { it.affectedByAppCompat }, _state.value.kgpVersionStatus.isCompatible)
            _state.value = _state.value.copy(scanState = ScanState.Completed, jspecifyViolations = mockViolations, healthScore = healthScore, jspecifyCoverage = 1.0f - (mockViolations.size.toFloat() / 150f), isLoading = false)
            _effect.send(AppCompatMigrationEffect.ShowToast("扫描完成: ${mockViolations.size} 违规"))
        }
    }
    private fun filterBySeverity(severity: Set<ViolationSeverity>) { _state.value = _state.value.copy(filterSeveritySet = severity) }
    private fun showViolationDetail(violation: JSpecifyViolation) { _state.value = _state.value.copy(selectedViolation = violation, showViolationDialog = true); viewModelScope.launch { _effect.send(AppCompatMigrationEffect.ShowViolationDetail(violation)) } }
    private fun dismissViolationDialog() { _state.value = _state.value.copy(selectedViolation = null, showViolationDialog = false) }
    private fun checkKGPVersion() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true); delay(500)
            val kgpStatus = generateMockKGPStatus()
            val healthScore = calculateHealthScore(_state.value.jspecifyViolations.count { it.severity == ViolationSeverity.P0 }, _state.value.jspecifyViolations.count { it.severity == ViolationSeverity.P1 }, _state.value.minSdkAnalysis.count { it.affectedByAppCompat }, kgpStatus.isCompatible)
            _state.value = _state.value.copy(kgpVersionStatus = kgpStatus, healthScore = healthScore, isLoading = false)
            _effect.send(AppCompatMigrationEffect.ShowToast(if (kgpStatus.isCompatible) "KGP 版本兼容" else "KGP 版本不兼容: ${kgpStatus.upgradePath}"))
        }
    }
    private fun exportReport(reportType: ReportType, format: ExportFormat) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true); delay(600)
            val fileName = when (reportType) { ReportType.MinSdkImpact -> "minSdk-impact-report"; ReportType.JSpecifyCompliance -> "jspecify-compliance-report" }
            val ext = when (format) { ExportFormat.PDF -> "pdf"; ExportFormat.JSON -> "json"; ExportFormat.HTML -> "html" }
            _state.value = _state.value.copy(isLoading = false)
            _effect.send(AppCompatMigrationEffect.ReportExported("/tmp/$fileName.$ext"))
            _effect.send(AppCompatMigrationEffect.ShowToast("报告已导出: /tmp/$fileName.$ext"))
        }
    }
    private fun updateSettings(settings: AppCompatMigrationSettings) { _state.value = _state.value.copy(settings = settings); viewModelScope.launch { _effect.send(AppCompatMigrationEffect.ShowToast("设置已保存")) } }
    private fun selectTab(tabIndex: Int) { _state.value = _state.value.copy(selectedTab = tabIndex) }
    private fun calculateHealthScore(p0: Int, p1: Int, affected: Int, isKGPCompatible: Boolean): Int { var score = 100; score -= minOf(p0 * 15, 45); score -= minOf(p1 * 5, 25); score -= minOf(affected * 3, 15); if (!isKGPCompatible) score -= 15; return maxOf(0, minOf(100, score)) }
    private fun generateMockMinSdkAnalysis(): List<ModuleMinSdkImpact> = listOf(ModuleMinSdkImpact("app", 21, true, 23, listOf("app -> androidx.appcompat:appcompat:1.8.0-alpha01", "appcompat -> androidx.activity:activity:1.8.0"), "1.8.0 (需要升级)"), ModuleMinSdkImpact("feature:home", 21, true, 23, listOf("feature:home -> app"), "1.8.0 (需要升级)"), ModuleMinSdkImpact("feature:profile", 23, true, 23, listOf("feature:profile -> app"), "1.8.0"), ModuleMinSdkImpact("feature:list", 21, false, 23, emptyList(), "1.6.0"), ModuleMinSdkImpact("feature:mcp", 24, true, 23, listOf("feature:mcp -> app"), "1.8.0"), ModuleMinSdkImpact("feature:gemma4", 21, true, 23, listOf("feature:gemma4 -> app"), "1.7.0 (需要升级)"))
    private fun generateMockJSpecifyViolations(): List<JSpecifyViolation> = listOf(JSpecifyViolation(UUID.randomUUID().toString(), "app/src/main/java/com/mvi/kenny/feature/home/HomeViewModel.kt", 42, "@androidx.annotation.Nullable", "@org.jspecify.annotations.Nullable", ViolationSeverity.P0, "@androidx.annotation.Nullable val name: String?", "替换为 @org.jspecify.annotations.Nullable"), JSpecifyViolation(UUID.randomUUID().toString(), "app/src/main/java/com/mvi/kenny/feature/list/ListAdapter.kt", 87, "@androidx.annotation.NonNull", "@org.jspecify.annotations.NonNull", ViolationSeverity.P0, "@androidx.annotation.NonNull fun getItem(position: Int): Item", "替换为 @org.jspecify.annotations.NonNull"), JSpecifyViolation(UUID.randomUUID().toString(), "feature/profile/src/main/java/com/mvi/kenny/ProfileFragment.kt", 35, "@androidx.annotation.Nullable", "@org.jspecify.annotations.Nullable", ViolationSeverity.P1, "@androidx.annotation.Nullable var userName: String?", "JSpecify 要求使用 @org.jspecify.annotations.Nullable"), JSpecifyViolation(UUID.randomUUID().toString(), "feature/mcp/src/main/java/com/mvi/kenny/McpClient.kt", 112, "@androidx.annotation.Nullable", "@org.jspecify.annotations.Nullable", ViolationSeverity.P1, "@androidx.annotation.Nullable val config: Config?", "启用 -Xjspecify-annotations=strict"), JSpecifyViolation(UUID.randomUUID().toString(), "app/src/main/java/com/mvi/kenny/feature/gemma4/Gemma4Screen.kt", 56, "@androidx.annotation.NonNull", "@org.jspecify.annotations.NonNull", ViolationSeverity.P2, "@androidx.annotation.NonNull data class Message(", "建议迁移到 JSpecify 注解"), JSpecifyViolation(UUID.randomUUID().toString(), "feature/home/src/main/java/com/mvi/kenny/HomeScreen.kt", 78, "@androidx.annotation.Nullable", "@org.jspecify.annotations.Nullable", ViolationSeverity.P2, "@androidx.annotation.Nullable var subtitle: String?", "建议使用 JSpecify 注解"))
    private fun generateMockKGPStatus(): KGPVersionStatus = KGPVersionStatus(kotlinVersion = "1.9.24", kgpVersion = "1.9.24", isCompatible = false, upgradePath = "Kotlin 1.9.24 -> Kotlin 2.0.0 + KGP 2.0.0", isKotlin2Required = true)
}