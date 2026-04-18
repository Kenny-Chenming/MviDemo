package com.mvi.kenny.feature.wifianalyzer

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// =============================================================
// WifiAnalyzerViewModel — Android 17 Wi-Fi Analyzer ViewModel
// =============================================================
class WifiAnalyzerViewModel : ViewModel() {

    private val _state = MutableStateFlow(WifiAnalyzerState.Initial)
    val state: StateFlow<WifiAnalyzerState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<WifiAnalyzerEffect>()
    val effect: SharedFlow<WifiAnalyzerEffect> = _effect.asSharedFlow()

    private var scanJob: Job? = null

    private val defaultAffectedApis = listOf(
        AffectedApi("WifiManager.startScan()", "Wi-Fi 扫描请求在 Android 17 中需要精确定位权限或完全受限", 17),
        AffectedApi("WifiInfo.getSSID()", "返回 <unknown ssid> 或需要精确定位权限", 17),
        AffectedApi("WifiInfo.getRssi()", "信号强度获取受限，需使用 RTT API", 17),
        AffectedApi("WifiP2pManager.discoverPeers()", "P2P 发现功能在部分设备上受限", 17)
    )

    fun processIntent(intent: WifiAnalyzerIntent) {
        when (intent) {
            is WifiAnalyzerIntent.SwitchTab -> _state.update { it.copy(currentTab = intent.tab) }
            is WifiAnalyzerIntent.StartScan -> handleStartScan(intent.modulePath)
            is WifiAnalyzerIntent.CancelScan -> handleCancelScan()
            is WifiAnalyzerIntent.FilterBySeverity -> _state.update { it.copy(selectedSeverity = intent.severity) }
            is WifiAnalyzerIntent.SelectModule -> _state.update { it.copy(selectedModule = intent.module) }
            is WifiAnalyzerIntent.SelectAlternativeType -> _state.update { it.copy(alternativeType = intent.type) }
            is WifiAnalyzerIntent.ToggleChecklistItem -> handleToggleChecklistItem(intent.itemId)
            is WifiAnalyzerIntent.GenerateReport -> handleGenerateReport()
            is WifiAnalyzerIntent.ExportReport -> handleExportReport(intent.format)
            is WifiAnalyzerIntent.DismissBanner -> _state.update { it.copy(bannerDismissed = true) }
            is WifiAnalyzerIntent.UpdateScanConfig -> _state.update { it.copy(scanConfig = intent.config) }
            is WifiAnalyzerIntent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun handleStartScan(modulePath: String) {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _state.update { it.copy(isScanning = true, scanProgress = 0f, scanResults = emptyList(), error = null) }
            try {
                val mockResults = withContext(Dispatchers.IO) { simulateScan() }
                val healthScore = calculateHealthScore(mockResults)
                val newScan = RecentScan(id = UUID.randomUUID().toString(), timestamp = System.currentTimeMillis(), score = healthScore, moduleName = modulePath)
                _state.update {
                    it.copy(isScanning = false, scanProgress = 1f, scanResults = mockResults, healthScore = healthScore,
                        recentScans = listOf(newScan) + it.recentScans.take(9), affectedApis = defaultAffectedApis)
                }
                _effect.emit(WifiAnalyzerEffect.ScanComplete)
            } catch (e: Exception) {
                _state.update { it.copy(isScanning = false, error = "扫描失败: ${e.message}") }
            }
        }
    }

    private suspend fun simulateScan(): List<ScanResult> = withContext(Dispatchers.IO) {
        val totalSteps = 10
        val mockFiles = listOf(
            Triple("com/example/app/WifiHelper.kt", "MainActivity.kt", "checkWifiStatus"),
            Triple("com/example/app/NetworkScanner.kt", "NetworkScanner.kt", "scanNetworks"),
            Triple("com/example/app/WifiAnalyzer.kt", "WifiAnalyzer.kt", "getSignalStrength"),
            Triple("com/example/app/P2pConnection.kt", "P2pConnection.kt", "discoverPeers"),
            Triple("com/example/app/SettingsFragment.kt", "SettingsFragment.kt", "onResume"),
            Triple("com/example/app/WifiService.kt", "WifiService.kt", "startBackgroundScan"),
            Triple("com/example/util/WifiUtils.kt", "WifiUtils.kt", "getAvailableNetworks")
        )
        val results = mutableListOf<ScanResult>()
        val apiNames = listOf("WifiManager.startScan()" to Severity.P0, "WifiInfo.getSSID()" to Severity.P1, "WifiInfo.getRssi()" to Severity.P1, "WifiP2pManager.discoverPeers()" to Severity.P2)
        mockFiles.forEachIndexed { index, (filePath, className, methodName) ->
            delay(300)
            _state.update { it.copy(scanProgress = (index + 1).toFloat() / totalSteps) }
            val (apiName, severity) = apiNames[index % apiNames.size]
            results.add(ScanResult(id = UUID.randomUUID().toString(), filePath = "src/main/java/$filePath", lineNumber = (50..200).random(),
                methodName = methodName, className = className, apiName = apiName, severity = severity,
                codeSnippet = generateMockCodeSnippet(apiName), suggestedFix = generateMockFix(apiName)))
        }
        results
    }

    private fun generateMockCodeSnippet(apiName: String): String = when {
        apiName.contains("startScan") -> "wifiManager.startScan()\nToast.makeText(context, \"扫描中...\", Toast.LENGTH_SHORT).show()"
        apiName.contains("getSSID") -> "val ssid = wifiInfo.ssid\nif (ssid == \"<unknown ssid>\") { /* 需要精确定位权限 */ }"
        apiName.contains("getRssi") -> "val rssi = wifiInfo.rssi\nval level = WifiManager.calculateSignalLevel(rssi, 5)"
        else -> "wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener { override fun onSuccess() { } override fun onFailure(reason: Int) { } })"
    }

    private fun generateMockFix(apiName: String): String = when {
        apiName.contains("startScan") -> "改用 WifiRttManager.startRanging() 或实现降级策略"
        apiName.contains("getSSID") -> "使用 RTT 定位 API 或向用户请求精确定位权限"
        apiName.contains("getRssi") -> "使用 WifiRttManager 进行精确定位 (±1-2米精度)"
        else -> "实现设备能力检测，优雅降级到手动输入模式"
    }

    private fun calculateHealthScore(results: List<ScanResult>): Int {
        if (results.isEmpty()) return 100
        return maxOf(0, 100 - results.count { it.severity == Severity.P0 } * 25 - results.count { it.severity == Severity.P1 } * 10 - results.count { it.severity == Severity.P2 } * 5)
    }

    private fun handleCancelScan() {
        scanJob?.cancel()
        _state.update { it.copy(isScanning = false, scanProgress = 0f, error = null) }
        viewModelScope.launch { _effect.emit(WifiAnalyzerEffect.ShowToast("扫描已取消 / Scan cancelled")) }
    }

    private fun handleToggleChecklistItem(itemId: String) {
        _state.update { state ->
            state.copy(migrationChecklist = state.migrationChecklist.map { if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it })
        }
    }

    private fun handleGenerateReport() {
        viewModelScope.launch {
            _state.update { it.copy(isGeneratingReport = true) }
            try {
                delay(1500)
                val currentState = _state.value
                val newReport = ImpactReport(id = UUID.randomUUID().toString(), moduleName = currentState.selectedModule ?: "app",
                    generatedAt = System.currentTimeMillis(), p0Count = currentState.p0Count, p1Count = currentState.p1Count, p2Count = currentState.p2Count,
                    affectedFeatures = currentState.scanResults.map { "${it.className}.${it.methodName}" }.distinct().take(10),
                    reportContent = generateReportContent(currentState))
                _state.update { it.copy(isGeneratingReport = false, reports = listOf(newReport) + it.reports) }
                _effect.emit(WifiAnalyzerEffect.ShowToast("报告生成成功 / Report generated"))
            } catch (e: Exception) {
                _state.update { it.copy(isGeneratingReport = false, error = "报告生成失败: ${e.message}") }
            }
        }
    }

    private fun generateReportContent(state: WifiAnalyzerState): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return buildString {
            appendLine("# Android 17 Wi-Fi 兼容性影响报告")
            appendLine("**生成时间**: ${dateFormat.format(Date())}")
            appendLine("**模块**: ${state.selectedModule ?: "app"}")
            appendLine("**健康度评分**: ${state.healthScore}/100")
            appendLine("## 影响摘要")
            appendLine("| 严重程度 | 数量 |")
            appendLine("|----------|------|")
            appendLine("| P0 严重  | ${state.p0Count} |")
            appendLine("| P1 警告  | ${state.p1Count} |")
            appendLine("| P2 提示  | ${state.p2Count} |")
        }
    }

    private fun handleExportReport(format: ExportFormat) {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportFormat = format) }
            try {
                delay(1000)
                val latestReport = _state.value.reports.firstOrNull()
                if (latestReport != null) {
                    _effect.emit(WifiAnalyzerEffect.ReportExported("wifi_report_${System.currentTimeMillis()}.${format.name.lowercase()}"))
                    _effect.emit(WifiAnalyzerEffect.ShowToast("导出成功 / Export successful"))
                } else {
                    _effect.emit(WifiAnalyzerEffect.ShowToast("请先生成报告 / Please generate report first"))
                }
            } catch (e: Exception) {
                _effect.emit(WifiAnalyzerEffect.ShowToast("导出失败: ${e.message}"))
            } finally {
                _state.update { it.copy(isExporting = false) }
            }
        }
    }

    override fun onCleared() { super.onCleared(); scanJob?.cancel() }
}
