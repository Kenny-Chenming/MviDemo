package com.mvi.kenny.feature.wifianalyzer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// ==================== Wi-Fi Analyzer ViewModel ====================
// Wi-Fi Analyzer ViewModel — 处理所有业务逻辑和状态管理

class WifiAnalyzerViewModel : ViewModel() {

    // ==================== State ====================
    private val _state = MutableStateFlow(WifiAnalyzerState())
    val state: StateFlow<WifiAnalyzerState> = _state.asStateFlow()

    // ==================== Effect ====================
    private val _effect = MutableSharedFlow<WifiAnalyzerEffect>()
    val effect = _effect.asSharedFlow()

    init {
        // 初始化默认受影响 API 列表 / Initialize default affected APIs
        _state.update {
            it.copy(
                affectedApis = listOf(
                    AffectedApi("WifiManager.startScan()", "Wi-Fi 扫描 — Android 17 返回空列表", 17),
                    AffectedApi("WifiInfo.getRssi()", "信号强度 — 精度已降低", 17),
                    AffectedApi("WifiInfo.getSSID()", "SSID 获取 — 需要精确定位权限", 17),
                    AffectedApi("WifiP2pManager.discoverPeers()", "P2P 发现 — 限制更严格", 17)
                ),
                recentScans = listOf(
                    RecentScan("feature/wifi", System.currentTimeMillis() - 3600000, 45),
                    RecentScan("core/network", System.currentTimeMillis() - 7200000, 62)
                ),
                healthScore = 53
            )
        }
    }

    // ==================== Process Intent ====================
    fun processIntent(intent: WifiAnalyzerIntent) {
        when (intent) {
            is WifiAnalyzerIntent.SwitchTab -> switchTab(intent.tab)
            is WifiAnalyzerIntent.DismissBanner -> dismissBanner()
            is WifiAnalyzerIntent.StartScan -> startScan(intent.module)
            is WifiAnalyzerIntent.CancelScan -> cancelScan()
            is WifiAnalyzerIntent.FilterBySeverity -> filterBySeverity(intent.severity)
            is WifiAnalyzerIntent.SelectModule -> selectModule(intent.module)
            is WifiAnalyzerIntent.SelectAlternativeType -> selectAlternativeType(intent.type)
            is WifiAnalyzerIntent.ToggleChecklistItem -> toggleChecklistItem(intent.id)
            is WifiAnalyzerIntent.GenerateReport -> generateReport()
            is WifiAnalyzerIntent.ExportReport -> exportReport(intent.format)
            is WifiAnalyzerIntent.UpdateScanConfig -> updateScanConfig(intent.config)
        }
    }

    // ==================== Tab Switching ====================
    private fun switchTab(tab: WifiTab) {
        _state.update { it.copy(currentTab = tab) }
    }

    // ==================== Banner ====================
    private fun dismissBanner() {
        _state.update { it.copy(bannerDismissed = true) }
    }

    // ==================== Module Selection ====================
    private fun selectModule(module: String) {
        _state.update { it.copy(selectedModule = module) }
    }

    // ==================== Severity Filter ====================
    private fun filterBySeverity(severity: Severity?) {
        _state.update { state ->
            val filtered = if (severity == null) {
                state.scanResults
            } else {
                state.scanResults.filter { it.severity == severity }
            }
            state.copy(selectedSeverity = severity, filteredScanResults = filtered)
        }
    }

    // ==================== Scan ====================
    private fun startScan(module: String) {
        if (_state.value.isScanning) return
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, scanProgress = 0f) }
            try {
                // 模拟扫描进度 / Simulate scan progress
                for (i in 1..10) {
                    if (!_state.value.isScanning) break
                    delay(300)
                    _state.update { it.copy(scanProgress = i / 10f) }
                }
                // 生成模拟扫描结果 / Generate mock scan results
                val results = generateMockScanResults()
                val p0 = results.count { it.severity == Severity.P0 }
                val p1 = results.count { it.severity == Severity.P1 }
                val p2 = results.count { it.severity == Severity.P2 }
                val score = calculateHealthScore(p0, p1, p2)
                val recentScan = RecentScan(module, System.currentTimeMillis(), score)
                _state.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 1f,
                        scanResults = results,
                        filteredScanResults = results,
                        p0Count = p0, p1Count = p1, p2Count = p2,
                        healthScore = score,
                        recentScans = listOf(recentScan) + it.recentScans.take(4)
                    )
                }
                _effect.emit(WifiAnalyzerEffect.ScanComplete)
            } catch (e: Exception) {
                _state.update { it.copy(isScanning = false, scanProgress = 0f) }
                _effect.emit(WifiAnalyzerEffect.ShowToast("扫描失败: ${e.message}"))
            }
        }
    }

    private fun cancelScan() {
        _state.update { it.copy(isScanning = false, scanProgress = 0f) }
    }

    // ==================== Mock Data Generation ====================
    private fun generateMockScanResults(): List<ScanResult> {
        val apis = listOf(
            Triple("WifiManager.startScan()", Severity.P0, "使用 WifiRttManager.startRanging() 替代"),
            Triple("WifiInfo.getRssi()", Severity.P1, "使用 RangingResult.distanceMm 获取精确距离"),
            Triple("WifiManager.getScanResults()", Severity.P0, "结果集受限，考虑降级策略"),
            Triple("WifiInfo.getSSID()", Severity.P1, "需要 ACCESS_FINE_LOCATION 权限"),
            Triple("WifiP2pManager.discoverPeers()", Severity.P2, "限制更严格，建议添加错误处理"),
            Triple("WifiManager.reassociate()", Severity.P2, "建议使用 WifiNetworkSuggestion"),
            Triple("WifiInfo.getFrequency()", Severity.P1, "5GHz/6GHz 支持请使用 RttService"),
            Triple("WifiManager.addNetwork()", Severity.P2, "建议使用 WifiNetworkSpecifier")
        )
        val files = listOf(
            Triple("WifiHelper.kt", "com.app.utils", "checkWifiStatus"),
            Triple("NetworkManager.kt", "com.app.data", "connectToWifi"),
            Triple("WifiScanner.kt", "com.app.feature.wifi", "scanNearbyNetworks"),
            Triple("WifiP2PHelper.kt", "com.app.feature.p2p", "findPeers"),
            Triple("WifiConfigStore.kt", "com.app.data.local", "saveWifiConfig")
        )
        val snippets = listOf(
            "wifiManager.startScan()",
            "val rssi = wifiInfo.rssi",
            "val scanResults = wifiManager.scanResults",
            "val ssid = wifiInfo.ssid",
            "wifiP2pManager.discoverPeers(channel, ...)"
        )
        return apis.mapIndexed { idx, (api, severity, fix) ->
            val fileIdx = idx % files.size
            val (file, cls, method) = files[fileIdx]
            ScanResult(
                id = UUID.randomUUID().toString(),
                apiName = api,
                severity = severity,
                filePath = "app/src/main/java/com/mvi/kenny/$file",
                className = cls,
                methodName = method,
                lineNumber = 40 + idx * 12,
                codeSnippet = snippets[idx % snippets.size],
                suggestedFix = fix
            )
        }
    }

    private fun calculateHealthScore(p0: Int, p1: Int, p2: Int): Int {
        val total = maxOf(p0 + p1 + p2, 1)
        return maxOf(0, 100 - (p0 * 25 + p1 * 10 + p2 * 3))
    }

    // ==================== Alternative Type ====================
    private fun selectAlternativeType(type: AlternativeType) {
        _state.update { it.copy(alternativeType = type) }
    }

    // ==================== Checklist ====================
    private fun toggleChecklistItem(id: String) {
        _state.update { state ->
            val updated = state.migrationChecklist.map {
                if (it.id == id) it.copy(isChecked = !it.isChecked) else it
            }.ifEmpty {
                listOf(
                    ChecklistItem("check_1", "检查项目中所有 WifiManager.startScan() 调用点"),
                    ChecklistItem("check_2", "评估 Wi-Fi RTT 设备支持情况"),
                    ChecklistItem("check_3", "设计降级策略：无可用 Wi-Fi 数据时提示用户"),
                    ChecklistItem("check_4", "实现 WifiRttManager.isAvailable() 设备能力检测"),
                    ChecklistItem("check_5", "添加精确定位权限申请逻辑"),
                    ChecklistItem("check_6", "审查 Wi-Fi 数据使用隐私政策披露"),
                    ChecklistItem("check_7", "生成 Android 17 回归测试用例"),
                    ChecklistItem("check_8", "在 Android 17 设备上进行真机测试")
                ).map { item -> if (item.id == id) item.copy(isChecked = true) else item }
            }
            state.copy(migrationChecklist = updated)
        }
    }

    // ==================== Report ====================
    private fun generateReport() {
        viewModelScope.launch {
            _state.update { it.copy(isGeneratingReport = true) }
            delay(2000)
            val currentState = _state.value
            val report = ImpactReport(
                id = UUID.randomUUID().toString(),
                title = "Android 17 Wi-Fi API 影响分析报告",
                summary = "检测到 ${currentState.p0Count} 个严重问题，${currentState.p1Count} 个警告，${currentState.p2Count} 个提示",
                severityCounts = mapOf(Severity.P0 to currentState.p0Count, Severity.P1 to currentState.p1Count, Severity.P2 to currentState.p2Count),
                affectedFeatures = listOf("Wi-Fi 扫描功能", "P2P 发现", "网络自动切换", "位置定位服务"),
                generatedAt = System.currentTimeMillis()
            )
            _state.update { it.copy(isGeneratingReport = false, reports = listOf(report) + it.reports) }
            _effect.emit(WifiAnalyzerEffect.ShowToast("报告已生成 / Report generated"))
        }
    }

    private fun exportReport(format: ExportFormat) {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            delay(1500)
            val latestReport = _state.value.reports.firstOrNull()
            val fileName = latestReport?.let { "wifi_report_${it.id.take(8)}.${format.name.lowercase()}" } ?: "wifi_report.${format.name.lowercase()}"
            _state.update { it.copy(isExporting = false) }
            _effect.emit(WifiAnalyzerEffect.ReportExported(fileName))
        }
    }

    // ==================== Scan Config ====================
    private fun updateScanConfig(config: ScanConfig) {
        _state.update { it.copy(scanConfig = config) }
    }
}
