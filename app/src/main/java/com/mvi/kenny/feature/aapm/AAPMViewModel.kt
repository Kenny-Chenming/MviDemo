package com.mvi.kenny.feature.aapm

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// =============================================================
// AAPMViewModel — AAPM 工具面板 ViewModel
// =============================================================
/**
 * ViewModel for AAPM Dashboard / AAPM 工具面板 ViewModel
 *
 * Manages all state transitions following MVI pattern.
 * ViewModelScope is used for all coroutine operations.
 *
 * Key responsibilities:
 * - Detect and report AAPM (Advanced Protection Mode) status
 * - Scan Accessibility Services for AAPM compliance
 * - Generate compliance reports
 * - Provide migration guidance for affected services
 *
 * @see AAPMContract For State, Intent, Effect definitions
 * @see AAPMScreen For UI implementation
 */
class AAPMViewModel : ViewModel() {

    // ---------------------------------------------------------
    // State — single source of truth for UI
    // ---------------------------------------------------------
    private val _state = MutableStateFlow(AAPMDashboardState.Initial)
    val state: StateFlow<AAPMDashboardState> = _state.asStateFlow()

    // ---------------------------------------------------------
    // Effect — one-time events for UI
    // ---------------------------------------------------------
    private val _effect = MutableSharedFlow<AAPMDashboardEffect>()
    val effect: SharedFlow<AAPMDashboardEffect> = _effect.asSharedFlow()

    // ---------------------------------------------------------
    // Internal state
    // ---------------------------------------------------------
    private var scanJob: Job? = null

    // ================================================================
    // Intent processing — process user actions
    // ================================================================
    /**
     * Process user intent / 处理用户意图
     *
     * Called from UI layer when user performs an action.
     * Each when branch handles one Intent type.
     */
    fun processIntent(intent: AAPMDashboardIntent) {
        when (intent) {
            is AAPMDashboardIntent.SelectTab -> handleSelectTab(intent.tab)
            is AAPMDashboardIntent.CheckAAPMStatus -> handleCheckAAPMStatus()
            is AAPMDashboardIntent.ScanAccessibilityServices -> handleScanAccessibilityServices()
            is AAPMDashboardIntent.GenerateComplianceReport -> handleGenerateComplianceReport()
            is AAPMDashboardIntent.SelectService -> handleSelectService(intent.service)
            is AAPMDashboardIntent.ClearSelectedService -> handleClearSelectedService()
            is AAPMDashboardIntent.NavigateToMigration -> handleNavigateToMigration(intent.serviceName)
            is AAPMDashboardIntent.UpdateServiceResponse -> handleUpdateServiceResponse(intent.serviceName, intent.strategy)
            is AAPMDashboardIntent.ExportAuditLogs -> handleExportAuditLogs(intent.format)
            is AAPMDashboardIntent.DismissError -> handleDismissError()
            is AAPMDashboardIntent.ClearReport -> handleClearReport()
        }
    }

    // ================================================================
    // Intent handlers
    // ================================================================

    /**
     * Handle tab selection / 处理 Tab 选择
     *
     * Switches between dashboard tabs (Status/Impact/Guide/Audit/Report).
     */
    private fun handleSelectTab(tab: AAPMTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    /**
     * Handle AAPM status check / 处理 AAPM 状态检查
     *
     * Checks if Advanced Protection Mode is available and active.
     * Note: AdvancedProtectionManager API requires Android 17.2+
     */
    private fun handleCheckAAPMStatus() {
        viewModelScope.launch {
            try {
                val aapmState = withContext(Dispatchers.Default) {
                    detectAAPMState()
                }
                _state.update { it.copy(aapmState = aapmState) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to check AAPM status: ${e.message}") }
                _effect.emit(AAPMDashboardEffect.ShowScanError(e.message ?: "Unknown error"))
            }
        }
    }

    /**
     * Detect AAPM state / 检测 AAPM 状态
     *
     * @return Current AAPM state / 当前 AAPM 状态
     */
    private fun detectAAPMState(): AAPMState {
        // AdvancedProtectionManager available only on Android 17.2+ (API 35+)
        // Android 17 = API 34, Android 17.2 likely = API 35
        return if (Build.VERSION.SDK_INT < 35) {
            // Android < 17.2, not supported
            AAPMState.NotSupported
        } else {
            // Simulate AAPM state detection
            // In production: use AdvancedProtectionManager API
            // Note: This requires QUERY_ALL_PACKAGES permission
            AAPMState.Enabled(
                isUserEnrolled = false, // Simulated
                affectedServices = emptyList() // Simulated
            )
        }
    }

    /**
     * Handle Accessibility Services scan / 处理 Accessibility Services 扫描
     *
     * Scans the app for AccessibilityService declarations and assesses risk.
     */
    private fun handleScanAccessibilityServices() {
        scanJob?.cancel()

        _state.update {
            it.copy(
                isScanning = true,
                error = null
            )
        }

        scanJob = viewModelScope.launch {
            try {
                val services = withContext(Dispatchers.Default) {
                    simulateServiceScan()
                }

                _state.update {
                    it.copy(
                        isScanning = false,
                        services = services
                    )
                }

                _effect.emit(AAPMDashboardEffect.ShowSnackbar(
                    "Scan complete: ${services.size} Accessibility Services found"
                ))

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    _state.update { it.copy(isScanning = false) }
                } else {
                    _state.update {
                        it.copy(
                            isScanning = false,
                            error = "Scan failed: ${e.message}"
                        )
                    }
                    _effect.emit(AAPMDashboardEffect.ShowScanError(e.message ?: "Unknown error"))
                }
            }
        }
    }

    /**
     * Simulate Accessibility Services scan / 模拟 Accessibility Services 扫描
     *
     * In production, this would parse AndroidManifest.xml and source files.
     * Simulates 2-second scan with progress updates.
     *
     * @return List of detected Accessibility Services / 检测到的 Accessibility Services 列表
     */
    private suspend fun simulateServiceScan(): List<AccessibilityServiceInfo> {
        // Simulate scan progress / 模拟扫描进度
        repeat(10) { step ->
            delay(200)
        }

        // Return simulated service list / 返回模拟的服务列表
        return listOf(
            AccessibilityServiceInfo(
                name = "MyAccessibilityService",
                className = "com.example.app.service.MyAccessibilityService",
                riskLevel = RiskLevel.HIGH,
                recommendedResponse = ResponseStrategy.GRACEFUL_DEGRADE,
                isEnabled = true,
                description = "Main accessibility service for auto-fill functionality / 用于自动填充功能的主要辅助功能服务"
            ),
            AccessibilityServiceInfo(
                name = "ScreenReaderService",
                className = "com.example.app.service.ScreenReaderService",
                riskLevel = RiskLevel.MEDIUM,
                recommendedResponse = ResponseStrategy.USER_GUIDANCE,
                isEnabled = true,
                description = "Screen reader for visually impaired users / 为视障用户提供的屏幕阅读器"
            ),
            AccessibilityServiceInfo(
                name = "AutomationService",
                className = "com.example.app.service.AutomationService",
                riskLevel = RiskLevel.HIGH,
                recommendedResponse = ResponseStrategy.DISABLE,
                isEnabled = false,
                description = "Task automation service / 任务自动化服务"
            ),
            AccessibilityServiceInfo(
                name = "PasswordManagerService",
                className = "com.example.app.service.PasswordManagerService",
                riskLevel = RiskLevel.MEDIUM,
                recommendedResponse = ResponseStrategy.GRACEFUL_DEGRADE,
                isEnabled = true,
                description = "Auto-fill password service / 自动填充密码服务"
            ),
            AccessibilityServiceInfo(
                name = "AnalyticsService",
                className = "com.example.app.service.AnalyticsService",
                riskLevel = RiskLevel.LOW,
                recommendedResponse = ResponseStrategy.NONE,
                isEnabled = false,
                description = "Usage analytics collection / 使用情况分析收集"
            )
        )
    }

    /**
     * Handle compliance report generation / 处理合规报告生成
     */
    private fun handleGenerateComplianceReport() {
        viewModelScope.launch {
            _state.update { it.copy(isGeneratingReport = true, error = null) }

            try {
                val report = withContext(Dispatchers.Default) {
                    generateComplianceReport()
                }

                _state.update {
                    it.copy(
                        complianceReport = report,
                        isGeneratingReport = false
                    )
                }

                if (report.isCompliant) {
                    _effect.emit(AAPMDashboardEffect.ShowCompliancePassed)
                } else {
                    _effect.emit(AAPMDashboardEffect.ShowComplianceFailed(report.nonCompliantServices))
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isGeneratingReport = false,
                        error = "Report generation failed: ${e.message}"
                    )
                }
                _effect.emit(AAPMDashboardEffect.ShowSnackbar(
                    "Report generation failed: ${e.message}",
                    isError = true
                ))
            }
        }
    }

    /**
     * Generate compliance report / 生成合规报告
     *
     * @return Generated compliance report / 生成的合规报告
     */
    private fun generateComplianceReport(): ComplianceReport {
        val services = _state.value.services
        val nonCompliantServices = services.filter {
            it.riskLevel == RiskLevel.HIGH && it.isEnabled
        }

        val issues = nonCompliantServices.map { service ->
            ComplianceIssue(
                serviceName = service.name,
                description = buildString {
                    append("Service '${service.name}' is enabled and at HIGH risk. ")
                    append("AAPM may revoke accessibility permissions automatically.")
                },
                severity = service.riskLevel,
                suggestion = when (service.recommendedResponse) {
                    ResponseStrategy.DISABLE -> "Consider disabling this service until AAPM compliance is achieved."
                    ResponseStrategy.GRACEFUL_DEGRADE -> "Implement alternative functionality without Accessibility API."
                    ResponseStrategy.USER_GUIDANCE -> "Provide clear user guidance when AAPM affects this service."
                    ResponseStrategy.NONE -> "No action required for this service."
                }
            )
        }

        return ComplianceReport(
            isCompliant = issues.isEmpty(),
            totalServices = services.size,
            compliantServices = services.size - nonCompliantServices.size,
            nonCompliantServices = nonCompliantServices.size,
            issues = issues,
            generatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Handle service selection / 处理服务选择
     *
     * @param service Selected service / 选中的服务
     */
    private fun handleSelectService(service: AccessibilityServiceInfo) {
        _state.update { it.copy(selectedService = service) }
    }

    /**
     * Handle clearing selected service / 处理清除选中的服务
     */
    private fun handleClearSelectedService() {
        _state.update { it.copy(selectedService = null) }
    }

    /**
     * Handle navigation to migration guide / 处理导航到迁移指南
     *
     * @param serviceName Service name to navigate to / 要导航到的服务名称
     */
    private fun handleNavigateToMigration(serviceName: String) {
        viewModelScope.launch {
            // Switch to GUIDE tab and select the service
            _state.update { currentState ->
                currentState.copy(
                    activeTab = AAPMTab.GUIDE,
                    services = currentState.services.map { service ->
                        if (service.name == serviceName) service else service
                    }
                )
            }
            _effect.emit(AAPMDashboardEffect.ShowSnackbar(
                "已跳转到 ${serviceName} 迁移指南"
            ))
        }
    }

    /**
     * Handle service response strategy update / 处理服务响应策略更新
     *
     * @param serviceName Service name / 服务名称
     * @param strategy New strategy / 新策略
     */
    private fun handleUpdateServiceResponse(serviceName: String, strategy: ResponseStrategy) {
        _state.update { state ->
            state.copy(
                services = state.services.map { service ->
                    if (service.name == serviceName) {
                        service.copy(recommendedResponse = strategy)
                    } else {
                        service
                    }
                }
            )
        }

        viewModelScope.launch {
            _effect.emit(AAPMDashboardEffect.ShowSnackbar(
                "Strategy updated for $serviceName"
            ))
        }
    }

    /**
     * Handle audit log export / 处理审计日志导出
     *
     * @param format Export format / 导出格式
     */
    private fun handleExportAuditLogs(format: ExportFormat) {
        viewModelScope.launch {
            try {
                val filePath = withContext(Dispatchers.IO) {
                    exportAuditLogs(format)
                }

                _effect.emit(AAPMDashboardEffect.ShowExportSuccess(filePath))
                _effect.emit(AAPMDashboardEffect.ShowSnackbar(
                    "Audit logs exported to: $filePath"
                ))

            } catch (e: Exception) {
                _effect.emit(AAPMDashboardEffect.ShowSnackbar(
                    "Export failed: ${e.message}",
                    isError = true
                ))
            }
        }
    }

    /**
     * Export audit logs to file / 导出审计日志到文件
     *
     * @param format Export format / 导出格式
     * @return Exported file path / 导出文件路径
     */
    private fun exportAuditLogs(format: ExportFormat): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "aapm_audit_logs_$timestamp.${format.extension}"
        val filePath = "/tmp/$fileName"

        val logs = _state.value.auditLogs

        val content = when (format) {
            ExportFormat.JSON -> buildJsonExport(logs)
            ExportFormat.CSV -> buildCsvExport(logs)
        }

        File(filePath).apply {
            parentFile?.mkdirs()
            writeText(content)
        }

        return filePath
    }

    /**
     * Build JSON export content / 构建 JSON 导出内容
     */
    private fun buildJsonExport(logs: List<AuditLogEntry>): String {
        return buildString {
            appendLine("{")
            appendLine("  \"exportedAt\": ${System.currentTimeMillis()},")
            appendLine("  \"totalEntries\": ${logs.size},")
            appendLine("  \"logs\": [")
            logs.forEachIndexed { index, log ->
                appendLine("    {")
                appendLine("      \"id\": \"${log.id}\",")
                appendLine("      \"timestamp\": ${log.timestamp},")
                appendLine("      \"targetApp\": \"${log.targetApp}\",")
                appendLine("      \"operationType\": \"${log.operationType}\",")
                appendLine("      \"serviceName\": \"${log.serviceName}\",")
                appendLine("      \"isCompliant\": ${log.isCompliant}")
                append("    }")
                if (index < logs.size - 1) appendLine(",") else appendLine()
            }
            appendLine("  ]")
            appendLine("}")
        }
    }

    /**
     * Build CSV export content / 构建 CSV 导出内容
     */
    private fun buildCsvExport(logs: List<AuditLogEntry>): String {
        return buildString {
            appendLine("ID,Timestamp,Target App,Operation Type,Service Name,Compliant")
            logs.forEach { log ->
                appendLine("${log.id},${log.timestamp},${log.targetApp},${log.operationType},${log.serviceName},${log.isCompliant}")
            }
        }
    }

    /**
     * Handle error dismissal / 处理错误关闭
     */
    private fun handleDismissError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Handle report clearing / 处理报告清除
     */
    private fun handleClearReport() {
        _state.update { it.copy(complianceReport = null) }
    }

    // ================================================================
    // Lifecycle
    // ================================================================

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}
