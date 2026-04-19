package com.mvi.kenny.feature.localnetworkpermission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * ViewModel for Android 17 Local Network Permission Toolkit
 * Android 17 本地网络权限迁移检测工具包的 ViewModel
 *
 * Design: Implements MVI pattern — State is the single source of truth,
 * Intent represents user actions, Effect represents one-time side effects.
 * 设计: MVI 架构 — State 是唯一数据源, Intent 表示用户动作, Effect 表示一次性副作用
 */
class LocalNetworkPermissionViewModel : ViewModel() {

    private val _state = MutableStateFlow(LocalNetworkPermissionState())
    val state: StateFlow<LocalNetworkPermissionState> = _state.asStateFlow()

    private val _effect = Channel<LocalNetworkPermissionEffect>(Channel.BUFFERED)
    val effect: Flow<LocalNetworkPermissionEffect> = _effect.receiveAsFlow()

    // Mock affected APIs for simulation
    // 模拟受影响的 API 数据
    private val simulatedAffectedApis = listOf(
        AffectedApi(
            apiName = "WifiP2pManager.discoverServices()",
            filePath = "app/src/main/java/com/example/app/WifiP2pActivity.kt",
            lineNumber = 42,
            severity = Severity.P0,
            suggestion = "需要申请 NEARBY_WIFI_DEVICES 权限，或切换到 ACCESS_LOCAL_NETWORK",
            alternativeApi = "WifiP2pManager.discoverServices() with permission"
        ),
        AffectedApi(
            apiName = "Network.getSocketFactory()",
            filePath = "app/src/main/java/com/example/app/NetworkActivity.kt",
            lineNumber = 88,
            severity = Severity.P0,
            suggestion = "本地网络需要申请 ACCESS_LOCAL_NETWORK 权限",
            alternativeApi = "ConnectivityManager.getNetworkCapabilities()"
        ),
        AffectedApi(
            apiName = "WifiManager.startScan()",
            filePath = "app/src/main/java/com/example/app/WifiScanActivity.kt",
            lineNumber = 134,
            severity = Severity.P1,
            suggestion = "Wi-Fi 扫描在 Android 17 需要 NEARBY_WIFI_DEVICES 权限",
            alternativeApi = "WifiManager.startScan() with NEARBY_WIFI_DEVICES"
        ),
        AffectedApi(
            apiName = "NsdManager.discoverServices()",
            filePath = "app/src/main/java/com/example/app/NsdDiscoveryActivity.kt",
            lineNumber = 56,
            severity = Severity.P1,
            suggestion = "服务发现需要申请 NEARBY_WIFI_DEVICES 权限",
            alternativeApi = "NsdManager.discoverServices() with permission"
        ),
        AffectedApi(
            apiName = "InetAddress.getAllByName()",
            filePath = "app/src/main/java/com/example/app/NetworkUtil.kt",
            lineNumber = 23,
            severity = Severity.P2,
            suggestion = "建议检查是否需要本地网络访问权限",
            alternativeApi = "InetAddress.getByName() with proper error handling"
        ),
        AffectedApi(
            apiName = "Socket.connect()",
            filePath = "app/src/main/java/com/example/app/TcpConnection.kt",
            lineNumber = 67,
            severity = Severity.P0,
            suggestion = "Socket 连接需要申请 ACCESS_LOCAL_NETWORK 权限",
            alternativeApi = "Socket.connect() with ACCESS_LOCAL_NETWORK"
        ),
        AffectedApi(
            apiName = "WifiP2pManager.requestPeers()",
            filePath = "app/src/main/java/com/example/app/P2pActivity.kt",
            lineNumber = 91,
            severity = Severity.P1,
            suggestion = "请求对等节点列表需要 NEARBY_WIFI_DEVICES 权限",
            alternativeApi = "WifiP2pManager.requestPeers() with permission"
        ),
        AffectedApi(
            apiName = "HttpURLConnection.connect()",
            filePath = "app/src/main/java/com/example/app/HttpClient.kt",
            lineNumber = 45,
            severity = Severity.P2,
            suggestion = "HTTP 连接建议添加网络错误处理和超时配置",
            alternativeApi = "OkHttpClient with proper timeout"
        )
    )

    // Mock radar scores based on scan results
    // 基于扫描结果的模拟雷达图分数
    private val simulatedRadarScores = RadarScores(
        permissionDecl = RadarScore("Permission Declaration / 权限声明", 45),
        runtimeRequest = RadarScore("Runtime Request / 运行时请求", 30),
        apiUsage = RadarScore("API Usage Pattern / API 使用模式", 55),
        fallbackHandling = RadarScore("Fallback Handling / 降级处理", 40),
        manifestConfig = RadarScore("Manifest Config / Manifest 配置", 60),
        testCoverage = RadarScore("Test Coverage / 测试覆盖", 25)
    )

    // Migration guides
    // 迁移指南
    private val migrationGuides = listOf(
        MigrationGuide(
            permission = PermissionState.NEARBY_WIFI_DEVICES,
            title = "NEARBY_WIFI_DEVICES 权限路径",
            description = "适用于 Wi-Fi P2P、投屏、打印等场景，不需要完整网络访问",
            steps = listOf(
                "1. 在 AndroidManifest.xml 中声明 NEARBY_WIFI_DEVICES 权限",
                "2. 在运行时使用 ActivityResultContracts.RequestPermission 请求权限",
                "3. 检查权限是否已授予后再调用 Wi-Fi P2P API",
                "4. 提供降级方案：无权限时提示用户或禁用相关功能"
            ),
            codeSnippet = """
                <!-- AndroidManifest.xml -->
                <uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES" />

                // Kotlin
                val permission = android.Manifest.permission.NEARBY_WIFI_DEVICES
                if (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED) {
                    // 调用 Wi-Fi P2P API
                } else {
                    requestPermissionLauncher.launch(permission)
                }
            """.trimIndent(),
            priority = 1
        ),
        MigrationGuide(
            permission = PermissionState.ACCESS_LOCAL_NETWORK,
            title = "ACCESS_LOCAL_NETWORK 权限路径",
            description = "适用于需要访问本地网络所有设备的场景，如智能家居控制",
            steps = listOf(
                "1. 在 AndroidManifest.xml 中声明 ACCESS_LOCAL_NETWORK 权限",
                "2. 在运行时请求权限，需要用户明确授权",
                "3. 仅在实际需要时请求权限，提供清晰的用途说明",
                "4. 处理用户拒绝权限的情况，提供应用内说明"
            ),
            codeSnippet = """
                <!-- AndroidManifest.xml -->
                <uses-permission android:name="android.permission.ACCESS_LOCAL_NETWORK" />

                // Kotlin
                val permission = android.Manifest.permission.ACCESS_LOCAL_NETWORK
                if (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED) {
                    // 访问本地网络设备
                } else {
                    requestPermissionLauncher.launch(permission)
                }
            """.trimIndent(),
            priority = 2
        ),
        MigrationGuide(
            permission = PermissionState.DISABLED,
            title = "Printer API 权限路径（无需权限）",
            description = "Android 17 推荐使用 PrinterService API，无需申请网络权限",
            steps = listOf(
                "1. 移除所有 Wi-Fi P2P 和网络相关 API 调用",
                "2. 使用 Android PrintService framework 实现打印功能",
                "3. 实现 PrintService 并在 AndroidManifest.xml 中声明",
                "4. 使用 PrintManager 获取打印框架支持"
            ),
            codeSnippet = """
                // 使用 Android PrintService
                class MyPrintService : PrintService() {
                    override fun onPrintJobQueued(printJob: PrintJob) {
                        // 处理打印任务
                    }
                }

                <!-- AndroidManifest.xml -->
                <service
                    android:name=".MyPrintService"
                    android:permission="android.permission.BIND_PRINT_SERVICE">
                    <intent-filter>
                        <action android:name="android.printservice.PrintService" />
                    </intent-filter>
                </service>
            """.trimIndent(),
            priority = 3
        )
    )

    /**
     * Process user intents and update state accordingly
     * 处理用户意图并相应更新状态
     */
    fun processIntent(intent: LocalNetworkPermissionIntent) {
        when (intent) {
            is LocalNetworkPermissionIntent.SelectTab -> selectTab(intent.tab)
            is LocalNetworkPermissionIntent.StartScan -> startScan()
            is LocalNetworkPermissionIntent.CancelScan -> cancelScan()
            is LocalNetworkPermissionIntent.SetSeverityFilter -> setSeverityFilter(intent.severity)
            is LocalNetworkPermissionIntent.SetPermissionState -> setPermissionState(intent.state)
            is LocalNetworkPermissionIntent.GenerateTestSuite -> generateTestSuite()
            is LocalNetworkPermissionIntent.DismissError -> dismissError()
        }
    }

    // ===== Tab Navigation / 标签页导航 =====

    private fun selectTab(tab: LocalNetworkTab) {
        _state.update { it.copy(currentTab = tab) }
    }

    // ===== Scan Logic / 扫描逻辑 =====

    private fun startScan() {
        if (_state.value.isScanning) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    scanStatus = ScanStatus.SCANNING,
                    isScanning = true,
                    scanProgress = 0f,
                    errorMessage = null
                )
            }

            // Simulate scanning progress from 0 to 100%
            // 模拟从 0% 到 100% 的扫描进度
            val steps = 20
            for (step in 1..steps) {
                delay(80L)  // Simulate work
                val progress = step.toFloat() / steps
                _state.update { it.copy(scanProgress = progress) }
            }

            // Complete scan with results
            // 完成扫描并显示结果
            _state.update {
                it.copy(
                    scanStatus = ScanStatus.COMPLETED,
                    isScanning = false,
                    scanProgress = 1f,
                    affectedApis = simulatedAffectedApis,
                    radarScores = simulatedRadarScores,
                    migrationGuides = migrationGuides
                )
            }

            _effect.send(LocalNetworkPermissionEffect.ShowSnackbar(
                "Scan completed — Found ${simulatedAffectedApis.size} affected APIs"
            ))
        }
    }

    private fun cancelScan() {
        viewModelScope.coroutineContext.cancelChildren()
        _state.update {
            it.copy(
                scanStatus = ScanStatus.IDLE,
                isScanning = false,
                scanProgress = 0f
            )
        }
    }

    // ===== Severity Filter / 严重级别过滤 =====

    private fun setSeverityFilter(severity: Severity?) {
        _state.update { it.copy(severityFilter = severity) }
    }

    // ===== Settings / 设置 =====

    private fun setPermissionState(state: PermissionState) {
        _state.update { it.copy(selectedPermissionState = state) }
    }

    private fun generateTestSuite() {
        viewModelScope.launch {
            _effect.send(LocalNetworkPermissionEffect.ShowSnackbar("Generating test suite..."))
            delay(500L)  // Simulate generation
            val filePath = "/test/local_network_permission_test_${System.currentTimeMillis()}.kt"
            _effect.send(LocalNetworkPermissionEffect.TestSuiteGenerated(filePath))
            _effect.send(LocalNetworkPermissionEffect.ShowSnackbar("Test suite generated: $filePath"))
        }
    }

    // ===== Error Handling / 错误处理 =====

    private fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.coroutineContext.cancelChildren()
    }
}
