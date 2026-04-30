package com.mvi.kenny.feature.devverifytool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * ============================================================
 * DevVerifyToolViewModel — PRD-209 Android 开发者验证合规工具包
 * ============================================================
 * ViewModel — Manages DevVerifyToolState and processes DevVerifyToolIntent
 *
 * MVI Pattern:
 * - State: DevVerifyToolState (single source of truth)
 * - Intent: DevVerifyToolIntent (user actions)
 * - Effect: DevVerifyToolEffect (one-time side effects via Channel)
 *
 * Design: memory/agency/designs/PRD-209-Android-开发者验证合规工具包.md
 * Bilingual comments: CN + EN
 */
class DevVerifyToolViewModel : ViewModel() {

    // ============ State ============
    // ============ 状态 ============
    private val _state = MutableStateFlow(DevVerifyToolState.Initial)
    val state: StateFlow<DevVerifyToolState> = _state.asStateFlow()

    // ============ Effect Channel ============
    // ============ 副作用频道 ============
    private val _effects = Channel<DevVerifyToolEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        // Initialize region deadline items on ViewModel creation
        // 初始化地区截止日期项
        initializeRegionDeadlines()
    }

    // ============ Intent Processing ============
    // ============ 意图处理 ============
    /**
     * Process user intent / 处理用户意图
     * @param intent User action intent / 用户操作意图
     */
    fun sendIntent(intent: DevVerifyToolIntent) {
        viewModelScope.launch {
            when (intent) {
                is DevVerifyToolIntent.SelectTab -> handleSelectTab(intent.tab)
                is DevVerifyToolIntent.StartScan -> handleStartScan()
                is DevVerifyToolIntent.CancelScan -> handleCancelScan()
                is DevVerifyToolIntent.ImportCsv -> handleImportCsv(intent.path)
                is DevVerifyToolIntent.ExecuteBatchRegister -> handleExecuteBatchRegister()
                is DevVerifyToolIntent.ToggleRegion -> handleToggleRegion(intent.region)
                is DevVerifyToolIntent.RefreshDashboard -> handleRefreshDashboard()
                is DevVerifyToolIntent.DismissError -> handleDismissError()
                is DevVerifyToolIntent.SelectApp -> handleSelectApp(intent.packageName)
                is DevVerifyToolIntent.ExportReport -> handleExportReport(intent.format)
            }
        }
    }

    // ============ Tab Selection ============
    // ============ Tab 选择 ============
    private suspend fun handleSelectTab(tab: VerificationTab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    // ============ Scan Logic ============
    // ============ 扫描逻辑 ============
    private suspend fun handleStartScan() {
        _state.value = _state.value.copy(
            isScanning = true,
            scanProgress = 0f,
            errorMessage = null,
            scanResults = emptyList()
        )

        // Simulate Gradle plugin scan with progress
        // 模拟 Gradle 插件扫描进度
        repeat(10) { step ->
            delay(300)
            _state.value = _state.value.copy(scanProgress = (step + 1) / 10f)
        }

        // Mock scan results — replace with actual Android Developer API / Gradle plugin JSON parsing
        // 模拟扫描结果 — 实际使用时替换为 Android Developer API / Gradle 插件 JSON 解析
        val mockResults = listOf(
            AppComplianceItem(
                id = UUID.randomUUID().toString(),
                packageName = "com.example.myapp",
                appName = "My App",
                status = AppComplianceStatus.VERIFIED,
                signingKeyType = KeyType.GOOGLE_MANAGED,
                keyFingerprint = "SHA256:AA:BB:CC:DD:EE:FF:11:22:33:44:55:66:77:88:99:00",
                verificationDate = LocalDate.now().minusDays(30),
                regionCovered = listOf(DevRegion.BRAZIL, DevRegion.SINGAPORE, DevRegion.THAILAND)
            ),
            AppComplianceItem(
                id = UUID.randomUUID().toString(),
                packageName = "com.example.oldapp",
                appName = "Old App",
                status = AppComplianceStatus.NOT_VERIFIED,
                signingKeyType = KeyType.SELF_SIGNED,
                keyFingerprint = "SHA256:11:22:33:44:55:66:77:88:99:00:AA:BB:CC:DD:EE:FF"
            ),
            AppComplianceItem(
                id = UUID.randomUUID().toString(),
                packageName = "com.example.newapp",
                appName = "New App",
                status = AppComplianceStatus.EXPIRING_SOON,
                signingKeyType = KeyType.UPLOAD_KEY,
                keyFingerprint = "SHA256:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90",
                verificationDate = LocalDate.now().minusDays(150),
                regionCovered = listOf(DevRegion.INDONESIA)
            ),
            AppComplianceItem(
                id = UUID.randomUUID().toString(),
                packageName = "com.example.enterprise",
                appName = "Enterprise App",
                status = AppComplianceStatus.VERIFIED,
                signingKeyType = KeyType.GOOGLE_MANAGED,
                keyFingerprint = "SHA256:FE:DC:BA:98:76:54:32:10:FE:DC:BA:98:76:54:32:10",
                verificationDate = LocalDate.now().minusDays(7),
                regionCovered = DevRegion.entries.toList()
            ),
            AppComplianceItem(
                id = UUID.randomUUID().toString(),
                packageName = "com.example.oss",
                appName = "FOSS App",
                status = AppComplianceStatus.UNKNOWN,
                signingKeyType = null,
                keyFingerprint = null
            )
        )

        _state.value = _state.value.copy(
            isScanning = false,
            scanProgress = 1f,
            scanResults = mockResults
        )

        // Update dashboard stats simultaneously / 同时更新仪表盘统计
        updateDashboardStats(mockResults)

        // Add operation log / 添加操作日志
        addOperationLog("Scan completed", "${mockResults.size} apps scanned", true)

        _effects.send(DevVerifyToolEffect.ShowSnackbar("Scan complete: ${mockResults.size} apps found"))
    }

    private fun handleCancelScan() {
        _state.value = _state.value.copy(
            isScanning = false,
            scanProgress = 0f
        )
    }

    // ============ CSV Import ============
    // ============ CSV 导入 ============
    private suspend fun handleImportCsv(path: String) {
        _state.value = _state.value.copy(
            csvPath = path,
            batchRegisterOutput = "[INFO] CSV imported: $path\n[INFO] Ready to register batch.\n"
        )
        _effects.send(DevVerifyToolEffect.ShowSnackbar("CSV imported: $path"))
    }

    // ============ Batch Register ============
    // ============ 批量注册 ============
    private suspend fun handleExecuteBatchRegister() {
        val csvPath = _state.value.csvPath
        if (csvPath == null) {
            _state.value = _state.value.copy(errorMessage = "No CSV file imported")
            _effects.send(DevVerifyToolEffect.ShowError("Please import a CSV file first"))
            return
        }

        _state.value = _state.value.copy(
            isRegistering = true,
            batchRegisterOutput = "[INFO] Starting batch registration...\n[INFO] Reading CSV: $csvPath\n"
        )

        // Simulate batch registration with terminal output
        // 模拟批量注册终端输出
        val apps = listOf("com.example.app1", "com.example.app2", "com.example.app3")
        apps.forEachIndexed { index, app ->
            delay(500)
            val output = _state.value.batchRegisterOutput +
                    "[${index + 1}/${apps.size}] Registering $app... " +
                    if ((0..1).random() == 1) "✅ SUCCESS\n" else "⚠️ RATE LIMITED, RETRY...\n"
            _state.value = _state.value.copy(batchRegisterOutput = output)
        }

        _state.value = _state.value.copy(
            isRegistering = false,
            batchRegisterOutput = _state.value.batchRegisterOutput +
                    "\n[INFO] Batch registration complete. 3 apps processed.\n" +
                    "[INFO] Check Dashboard for updated status.\n"
        )

        addOperationLog("Batch register", csvPath, true)
        _effects.send(DevVerifyToolEffect.RegistrationComplete)
    }

    // ============ Region Toggle ============
    // ============ 地区切换 ============
    private suspend fun handleToggleRegion(region: DevRegion) {
        val current = _state.value.selectedRegions.toMutableSet()
        if (current.contains(region)) {
            current.remove(region)
        } else {
            current.add(region)
        }
        _state.value = _state.value.copy(selectedRegions = current)
        updateRegionDeadlines()
    }

    // ============ Dashboard Refresh ============
    // ============ 仪表盘刷新 ============
    private suspend fun handleRefreshDashboard() {
        val results = _state.value.scanResults
        updateDashboardStats(results)
        _effects.send(DevVerifyToolEffect.ShowSnackbar("Dashboard refreshed"))
    }

    // ============ Error Dismiss ============
    // ============ 错误关闭 ============
    private fun handleDismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    // ============ App Selection ============
    // ============ App 选择 ============
    private suspend fun handleSelectApp(packageName: String) {
        _effects.send(DevVerifyToolEffect.ShowSnackbar("Selected: $packageName"))
    }

    // ============ Export Report ============
    // ============ 导出报告 ============
    private suspend fun handleExportReport(format: String) {
        val state = _state.value
        val stats = state.dashboardStats

        val reportJson = """
        {
            "reportType": "DevVerificationCompliance",
            "generatedAt": "${LocalDate.now()}",
            "summary": {
                "totalApps": ${stats.totalApps},
                "verified": ${stats.verifiedCount},
                "notVerified": ${stats.notVerifiedCount},
                "expiringSoon": ${stats.expiringSoonCount},
                "complianceRate": ${stats.complianceRate}
            },
            "scanResults": ${state.scanResults.size},
            "keyItems": ${state.keyItems.size}
        }
        """.trimIndent()

        _effects.send(DevVerifyToolEffect.ExportReportReady(reportJson))
        addOperationLog("Export report", format, true)
    }

    // ============ Helper: Update Dashboard Stats ============
    // ============ 辅助：更新仪表盘统计 ============
    private fun updateDashboardStats(results: List<AppComplianceItem>) {
        val total = results.size
        val verified = results.count { it.status == AppComplianceStatus.VERIFIED }
        val notVerified = results.count { it.status == AppComplianceStatus.NOT_VERIFIED }
        val expiringSoon = results.count { it.status == AppComplianceStatus.EXPIRING_SOON }
        val complianceRate = if (total > 0) verified.toFloat() / total else 0f

        _state.value = _state.value.copy(
            dashboardStats = DashboardStats(
                totalApps = total,
                verifiedCount = verified,
                notVerifiedCount = notVerified,
                expiringSoonCount = expiringSoon,
                complianceRate = complianceRate
            )
        )
    }

    // ============ Helper: Initialize Region Deadlines ============
    // ============ 辅助：初始化地区截止日期 ============
    private fun initializeRegionDeadlines() {
        val items = DevRegion.entries.map { region ->
            val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), region.deadline).coerceAtLeast(0)
            val totalDays = region.deadline.toEpochDay() - LocalDate.now().minusDays(180).toEpochDay()
            val progress = 1f - (daysRemaining.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
            RegionDeadlineItem(
                region = region,
                daysRemaining = daysRemaining,
                isSelected = true,
                progress = progress
            )
        }.sortedBy { it.daysRemaining }

        _state.value = _state.value.copy(regionDeadlineItems = items)
    }

    // ============ Helper: Update Region Deadlines ============
    // ============ 辅助：更新地区截止日期 ============
    private fun updateRegionDeadlines() {
        val selected = _state.value.selectedRegions
        val items = _state.value.regionDeadlineItems.map { item ->
            item.copy(isSelected = selected.contains(item.region))
        }
        _state.value = _state.value.copy(regionDeadlineItems = items)
    }

    // ============ Helper: Add Operation Log ============
    // ============ 辅助：添加操作日志 ============
    private fun addOperationLog(action: String, target: String, isSuccess: Boolean) {
        val logs = _state.value.operationLogs.toMutableList()
        logs.add(
            0, // Add to top / 添加到顶部
            OperationLog(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                action = action,
                target = target,
                isSuccess = isSuccess
            )
        )
        // Keep only last 50 logs / 只保留最近 50 条日志
        val trimmedLogs = logs.take(50)
        _state.value = _state.value.copy(operationLogs = trimmedLogs)
    }
}
