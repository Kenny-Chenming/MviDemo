package com.mvi.kenny.feature.android17compliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

// ================================================================
// Android17ComplianceViewModel — Android 17 合规工具状态管理
// ================================================================
// Inherits ViewModel, holds Android17ComplianceState and Android17ComplianceEffect.
//
// State Management:
//   _state: Private MutableStateFlow, written internally by ViewModel
//   state: Public StateFlow, for UI layer subscription (collectAsState)
//
// Effect Management:
//   _effect: Channel (hot flow), buffer size BUFFERED
//   effect: receiveAsFlow, UI layer listens via collect{}
//
// Simulation Note:
//   本实现为 Demo 模式，模拟扫描结果和配置生成。
//   真实实现需要接入 Gradle Plugin 进行 Manifest/源码扫描。
//
// @see Android17ComplianceContract MVI contract definition
// @see Android17ComplianceScreen Main UI
// ================================================================

class Android17ComplianceViewModel : ViewModel() {

    // =============================================================
    // State
    // =============================================================
    /** Page state (StateFlow, UI read-only) / 页面状态 */
    private val _state = MutableStateFlow(Android17ComplianceState.Initial)
    val state: StateFlow<Android17ComplianceState> = _state.asStateFlow()

    /** Current state snapshot / 当前状态快照 */
    val currentState: Android17ComplianceState get() = _state.value

    // =============================================================
    // Effect
    // =============================================================
    /** Effect Channel / 副作用通道 */
    private val _effect = Channel<Android17ComplianceEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // =============================================================
    // Intent Processing
    // =============================================================
    /**
     * Process user intent / 处理用户意图
     *
     * @param intent User intent / 用户意图
     */
    fun sendIntent(intent: Android17ComplianceIntent) {
        viewModelScope.launch {
            when (intent) {
                is Android17ComplianceIntent.SelectTab -> handleSelectTab(intent.tabIndex)
                is Android17ComplianceIntent.StartScreenScan -> handleStartScreenScan()
                is Android17ComplianceIntent.StartNetworkScan -> handleStartNetworkScan()
                is Android17ComplianceIntent.AddDomain -> handleAddDomain(intent.domain)
                is Android17ComplianceIntent.RemoveDomain -> handleRemoveDomain(intent.domain)
                is Android17ComplianceIntent.SetConfigMode -> handleSetConfigMode(intent.mode)
                is Android17ComplianceIntent.GenerateConfig -> handleGenerateConfig()
                is Android17ComplianceIntent.CopyConfig -> handleCopyConfig()
                is Android17ComplianceIntent.SetMigrationStep -> handleSetMigrationStep(intent.step)
                is Android17ComplianceIntent.CompleteMigrationStep -> handleCompleteMigrationStep(intent.step)
                is Android17ComplianceIntent.RunFullComplianceCheck -> handleRunFullComplianceCheck()
                is Android17ComplianceIntent.ExportReport -> handleExportReport()
                is Android17ComplianceIntent.ClearSelection -> handleClearSelection()
            }
        }
    }

    // =============================================================
    // Tab Selection / Tab 选择
    // =============================================================
    private fun handleSelectTab(tabIndex: Int) {
        _state.value = _state.value.copy(selectedTab = tabIndex)
    }

    // =============================================================
    // Screen Scan / 大屏扫描
    // =============================================================
    private suspend fun handleStartScreenScan() {
        _state.value = _state.value.copy(
            screenScanState = ScanState.SCANNING,
            screenScanProgress = 0f,
            screenRisks = emptyList()
        )

        // Simulate scan progress / 模拟扫描进度
        val mockRisks = listOf(
            ScreenRisk(
                id = UUID.randomUUID().toString(),
                file = "app/src/main/AndroidManifest.xml",
                line = 12,
                riskType = "screenOrientation",
                riskLevel = RiskLevel.P0,
                description = "Activity 锁定竖屏，在大屏设备上无法旋转",
                snippet = """<activity
    android:name=".MainActivity"
    android:screenOrientation="portrait" />""",
                suggestion = "移除 android:screenOrientation 属性，启用完全自适应"
            ),
            ScreenRisk(
                id = UUID.randomUUID().toString(),
                file = "app/src/main/AndroidManifest.xml",
                line = 18,
                riskType = "resizeableActivity",
                riskLevel = RiskLevel.P0,
                description = "resizeableActivity=false 强制禁用多窗口，在 Android 17 大屏设备上将被忽略",
                snippet = """<activity
    android:name=".SettingsActivity"
    android:resizeableActivity="false"
    android:maxAspectRatio="1.86" />""",
                suggestion = "移除 resizeableActivity=false，测试各种宽高比"
            ),
            ScreenRisk(
                id = UUID.randomUUID().toString(),
                file = "app/src/main/AndroidManifest.xml",
                line = 25,
                riskType = "maxAspectRatio",
                riskLevel = RiskLevel.P1,
                description = "限制了最大宽高比，折叠屏/平板展开时可能出现裁剪",
                snippet = """<activity
    android:name=".VideoPlayerActivity"
    android:maxAspectRatio="2.33"
    android:minAspectRatio="1.33" />""",
                suggestion = "移除 maxAspectRatio/minAspectRatio 限制"
            )
        )

        for (i in 1..10) {
            delay(100)
            _state.value = _state.value.copy(screenScanProgress = i / 10f)
        }

        _state.value = _state.value.copy(
            screenScanState = ScanState.SUCCESS,
            screenScanProgress = 1f,
            screenRisks = mockRisks
        )
        _effect.send(Android17ComplianceEffect.ShowSnackbar("大屏扫描完成，发现 ${mockRisks.size} 个风险项"))
    }

    // =============================================================
    // Network Scan / 明文流量扫描
    // =============================================================
    private suspend fun handleStartNetworkScan() {
        _state.value = _state.value.copy(
            networkScanState = ScanState.SCANNING,
            networkScanProgress = 0f,
            networkRisks = emptyList()
        )

        val mockRisks = listOf(
            NetworkRisk(
                id = UUID.randomUUID().toString(),
                file = "app/src/main/java/com/example/app/network/ApiClient.kt",
                line = 23,
                url = "http://api.example.com/v1/data",
                library = "OkHttp",
                riskLevel = RiskLevel.P0,
                snippet = """private val client = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .url("http://api.example.com/v1/data")
            .build()
        chain.proceed(request)
    }
    .build()""",
                suggestion = "将 http:// 改为 https://，并配置证书验证"
            ),
            NetworkRisk(
                id = UUID.randomUUID().toString(),
                file = "app/src/main/java/com/example/app/data/RetrofitService.kt",
                line = 15,
                url = "http://config.internal.local/api/settings",
                library = "Retrofit",
                riskLevel = RiskLevel.P0,
                snippet = """@GET("http://config.internal.local/api/settings")
suspend fun getSettings(): Response<Settings>""",
                suggestion = "配置 Network Security Config 域名放行，或迁移到 HTTPS"
            ),
            NetworkRisk(
                id = UUID.randomUUID().toString(),
                file = "app/src/main/java/com/example/app/utils/NetworkUtil.kt",
                line = 8,
                url = "http://192.168.1.100:8080/status",
                library = "HttpUrlConnection",
                riskLevel = RiskLevel.P1,
                snippet = """fun checkServerStatus(): Boolean {
    val url = URL("http://192.168.1.100:8080/status")
    val connection = url.openConnection() as HttpURLConnection
    return connection.responseCode == 200
}""",
                suggestion = "明文流量将被系统拦截，需配置 Network Security Config 或迁移到 HTTPS"
            )
        )

        for (i in 1..10) {
            delay(120)
            _state.value = _state.value.copy(networkScanProgress = i / 10f)
        }

        _state.value = _state.value.copy(
            networkScanState = ScanState.SUCCESS,
            networkScanProgress = 1f,
            networkRisks = mockRisks
        )
        _effect.send(Android17ComplianceEffect.ShowSnackbar("明文流量扫描完成，发现 ${mockRisks.size} 个风险项"))
    }

    // =============================================================
    // Config Generation / 配置生成
    // =============================================================
    private fun handleAddDomain(domain: String) {
        val trimmed = domain.trim()
        if (trimmed.isEmpty()) return
        val current = _state.value.configDomains
        if (!current.contains(trimmed)) {
            _state.value = _state.value.copy(configDomains = current + trimmed)
        }
    }

    private fun handleRemoveDomain(domain: String) {
        _state.value = _state.value.copy(
            configDomains = _state.value.configDomains.filter { it != domain }
        )
    }

    private fun handleSetConfigMode(mode: ConfigMode) {
        _state.value = _state.value.copy(configMode = mode)
    }

    private fun handleGenerateConfig() {
        val state = _state.value
        val config = buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            appendLine("<network-security-config>")
            when (state.configMode) {
                ConfigMode.DOMAIN_LEVEL -> {
                    if (state.configDomains.isEmpty()) {
                        appendLine("    <!-- No domains added yet. Add domains in Config Generator tab. -->")
                    } else {
                        state.configDomains.forEach { domain ->
                            appendLine("    <domain-config cleartextTrafficPermitted=\"false\">")
                            appendLine("        <domain includeSubdomains=\"true\">$domain</domain>")
                            appendLine("        <trust-anchors>")
                            appendLine("            <certificates src=\"system\" />")
                            appendLine("        </trust-anchors>")
                            appendLine("    </domain-config>")
                        }
                    }
                }
                ConfigMode.GLOBAL -> {
                    appendLine("    <base-config cleartextTrafficPermitted=\"false\">")
                    appendLine("        <trust-anchors>")
                    appendLine("            <certificates src=\"system\" />")
                    appendLine("        </trust-anchors>")
                    appendLine("    </base-config>")
                }
            }
            appendLine("</network-security-config>")
        }
        _state.value = _state.value.copy(generatedConfig = config)
    }

    private suspend fun handleCopyConfig() {
        _effect.send(Android17ComplianceEffect.ConfigCopied)
        _effect.send(Android17ComplianceEffect.ShowSnackbar("配置已复制到剪贴板"))
    }

    // =============================================================
    // Migration Wizard / 迁移向导
    // =============================================================
    private fun handleSetMigrationStep(step: Int) {
        val maxStep = _state.value.migrationSteps.size - 1
        _state.value = _state.value.copy(migrationStep = step.coerceIn(0, maxStep))
    }

    private fun handleCompleteMigrationStep(step: Int) {
        val steps = _state.value.migrationSteps.toMutableList()
        if (step in steps.indices) {
            steps[step] = steps[step].copy(isCompleted = true)
            _state.value = _state.value.copy(migrationSteps = steps)
        }
    }

    // =============================================================
    // Full Compliance Check / 完整合规检测
    // =============================================================
    private suspend fun handleRunFullComplianceCheck() {
        handleStartScreenScan()
        delay(1500)
        handleStartNetworkScan()
        delay(1500)

        val state = _state.value
        val summary = ComplianceSummary(
            screenRisksCount = state.screenRisks.size,
            screenRisksFixed = 0,
            networkRisksCount = state.networkRisks.size,
            networkRisksFixed = 0
        )
        _state.value = _state.value.copy(complianceSummary = summary)
        _effect.send(Android17ComplianceEffect.ShowSnackbar("合规检测完成，请查看报告"))
    }

    // =============================================================
    // Export Report / 导出报告
    // =============================================================
    private suspend fun handleExportReport() {
        val state = _state.value
        val summary = state.complianceSummary
        val report = buildString {
            appendLine("# Android 17 合规检测报告")
            appendLine()
            appendLine("生成时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
            appendLine()
            appendLine("## 合规状态摘要")
            appendLine()
            if (summary != null) {
                appendLine("- 大屏适配: ${summary.screenRisksFixed}/${summary.screenRisksCount} 已修复")
                appendLine("- 明文流量: ${summary.networkRisksFixed}/${summary.networkRisksCount} 已修复")
                appendLine("- 总体状态: ${if (summary.isCompliant) "✅ 合规" else "❌ 不合规"}")
            } else {
                appendLine("- 请先运行完整合规检测")
            }
            appendLine()
            if (state.screenRisks.isNotEmpty()) {
                appendLine("## 大屏适配风险")
                state.screenRisks.forEach { risk ->
                    appendLine("- [${risk.riskLevel.emoji}] ${risk.riskType}: ${risk.description}")
                    appendLine("  文件: ${risk.file}:${risk.line}")
                }
                appendLine()
            }
            if (state.networkRisks.isNotEmpty()) {
                appendLine("## 明文流量风险")
                state.networkRisks.forEach { risk ->
                    appendLine("- [${risk.riskLevel.emoji}] ${risk.url} (${risk.library})")
                    appendLine("  文件: ${risk.file}:${risk.line}")
                }
            }
        }
        _effect.send(Android17ComplianceEffect.ReportExported(report))
    }

    // =============================================================
    // Clear Selection / 清除选中
    // =============================================================
    private fun handleClearSelection() {
        // Currently no selection state; reserved for future expansion
    }
}
