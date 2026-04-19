package com.mvi.kenny.feature.localnetworkpermission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * LocalNetworkPermissionViewModel — 本地网络权限状态管理
 * ============================================================
 * PRD-122 | Android 17 ACCESS_LOCAL_NETWORK 运行时权限迁移检测工具包
 *
 * @see LocalNetworkPermissionState 页面状态定义
 * @see LocalNetworkPermissionIntent 用户意图
 * @see LocalNetworkPermissionEffect 副作用
 */
class LocalNetworkPermissionViewModel : ViewModel() {

    // ================================================================
    // State / 状态
    // ================================================================
    private val _state = MutableStateFlow(LocalNetworkPermissionState.Initial)
    val state: StateFlow<LocalNetworkPermissionState> = _state.asStateFlow()

    val currentState: LocalNetworkPermissionState get() = _state.value

    // ================================================================
    // Effect / 副作用
    // ================================================================
    private val _effect = Channel<LocalNetworkPermissionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // 初始化时自动加载模拟数据
        loadMockData()
    }

    // ================================================================
    // Intent 处理入口 / Intent entry point
    // ================================================================
    fun sendIntent(intent: LocalNetworkPermissionIntent) {
        when (intent) {
            is LocalNetworkPermissionIntent.SelectTab -> selectTab(intent.index)
            is LocalNetworkPermissionIntent.StartScan -> startScan()
            is LocalNetworkPermissionIntent.SetSeverityFilter -> setSeverityFilter(intent.severity)
            is LocalNetworkPermissionIntent.SelectPermissionState -> selectPermissionState(intent.state)
            is LocalNetworkPermissionIntent.ToggleApiExpansion -> toggleApiExpansion(intent.index)
            is LocalNetworkPermissionIntent.ApplyMigration -> applyMigration(intent.path)
            is LocalNetworkPermissionIntent.GenerateTestSuite -> generateTestSuite()
            is LocalNetworkPermissionIntent.ExportReport -> exportReport()
            is LocalNetworkPermissionIntent.CopyCode -> copyCode(intent.code)
        }
    }

    // ================================================================
    // Tab 切换 / Tab switching
    // ================================================================
    /**
     * 切换 Tab
     * @param index Tab 索引
     */
    private fun selectTab(index: Int) {
        _state.value = _state.value.copy(currentTab = index)
    }

    // ================================================================
    // 扫描逻辑 / Scan logic
    // ================================================================
    /**
     * 开始代码扫描
     * 模拟 Gradle 插件扫描逻辑：
     * - 扫描 WifiManager / Socket / NetworkInterface / InetAddress / DatagramSocket 等 API
     * - 正则匹配典型调用模式
     * - 输出受影响文件路径 + 行号 + 代码片段
     */
    private fun startScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, scanProgress = 0f)

            // 模拟扫描进度 0% -> 100%
            for (i in 1..20) {
                delay(100)
                _state.value = _state.value.copy(scanProgress = i / 20f)
            }

            // 模拟扫描结果
            val affectedApis = listOf(
                AffectedApi(
                    apiName = "WifiManager.startScan()",
                    filePath = "app/src/main/java/com/mvi/kenny/feature/wifi/WifiScanner.kt",
                    lineNumber = 42,
                    severity = Severity.P0,
                    codeContext = """
                        |// Lines 39-45
                        |private fun discoverDevices() {
                        |    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        |    wifiManager.startScan()  // ⚠️ Requires ACCESS_LOCAL_NETWORK on API 37+
                        |    val results = wifiManager.scanResults
                    """.trimMargin(),
                    suggestedFix = "Add <uses-permission android:name=\"android.permission.ACCESS_LOCAL_NETWORK\"/> and request at runtime"
                ),
                AffectedApi(
                    apiName = "Socket.connect()",
                    filePath = "app/src/main/java/com/mvi/kenny/feature/network/NetworkClient.kt",
                    lineNumber = 87,
                    severity = Severity.P0,
                    codeContext = """
                        |// Lines 84-90
                        |private fun connectToDevice(host: String, port: Int) {
                        |    val socket = Socket()
                        |    socket.connect(InetSocketAddress(host, port), 3000)
                        |    // ⚠️ Socket access requires ACCESS_LOCAL_NETWORK on API 37+
                    """.trimMargin(),
                    suggestedFix = "Request ACCESS_LOCAL_NETWORK runtime permission before Socket.connect()"
                ),
                AffectedApi(
                    apiName = "NetworkInterface.getNetworkInterfaces()",
                    filePath = "app/src/main/java/com/mvi/kenny/feature/network/InterfaceScanner.kt",
                    lineNumber = 23,
                    severity = Severity.P1,
                    codeContext = """
                        |// Lines 20-26
                        |fun getLocalInterfaces(): List<NetworkInterface> {
                        |    return Collections.list(
                        |        NetworkInterface.getNetworkInterfaces()  // ⚠️ May need ACCESS_LOCAL_NETWORK
                        |    ).filter { it.isUp && !it.isLoopback }
                        |}
                    """.trimMargin(),
                    suggestedFix = "Add ACCESS_LOCAL_NETWORK permission and handle denial gracefully"
                ),
                AffectedApi(
                    apiName = "InetAddress.getAllByName()",
                    filePath = "app/src/main/java/com/mvi/kenny/feature/discovery/DeviceDiscovery.kt",
                    lineNumber = 56,
                    severity = Severity.P1,
                    codeContext = """
                        |// Lines 53-59
                        |private suspend fun resolveHostname(hostname: String): InetAddress {
                        |    return withContext(Dispatchers.IO) {
                        |        InetAddress.getAllByName(hostname)  // ⚠️ Check if local network
                        |    }
                        |}
                    """.trimMargin(),
                    suggestedFix = "Verify target is not local network or request ACCESS_LOCAL_NETWORK"
                ),
                AffectedApi(
                    apiName = "DatagramSocket.send()",
                    filePath = "app/src/main/java/com/mvi/kenny/feature/broadcast/BroadcastSender.kt",
                    lineNumber = 34,
                    severity = Severity.P2,
                    codeContext = """
                        |// Lines 31-37
                        |private fun broadcastPacket(data: ByteArray) {
                        |    val ds = DatagramSocket()
                        |    ds.send(DatagramPacket(data, data.size, groupAddress, 9999))
                        |    // ⚠️ UDP broadcast may trigger permission check on API 37+
                    """.trimMargin(),
                    suggestedFix = "Consider using NEARBY_WIFI_DEVICES for short-range discovery"
                )
            )

            _state.value = _state.value.copy(
                isScanning = false,
                scanProgress = 1f,
                affectedApis = affectedApis,
                radarScores = RadarScores(
                    manifestDeclaration = 40,
                    permissionRequestPath = 25,
                    gracefulDegradation = 60,
                    testCoverage = 15,
                    dependencyLibraries = 80,
                    apiCalls = 35
                )
            )
            _effect.send(LocalNetworkPermissionEffect.ShowSnackbar("扫描完成，发现 ${affectedApis.size} 处受影响 API"))
        }
    }

    // ================================================================
    // 风险过滤 / Severity filter
    // ================================================================
    /**
     * 设置风险级别过滤
     * @param severity 风险级别
     */
    private fun setSeverityFilter(severity: Severity) {
        _state.value = _state.value.copy(severityFilter = severity)
    }

    // ================================================================
    // 权限状态选择 / Permission state selection
    // ================================================================
    /**
     * 选择权限状态并更新雷达图评分
     * @param state 权限状态
     */
    private fun selectPermissionState(state: PermissionState) {
        _state.value = _state.value.copy(selectedPermissionState = state)
    }

    // ================================================================
    // API 条目展开/折叠 / Toggle API expansion
    // ================================================================
    /**
     * 展开/折叠 API 条目
     * @param index 条目索引
     */
    private fun toggleApiExpansion(index: Int) {
        _state.value = _state.value.copy(
            expandedApiIndex = if (_state.value.expandedApiIndex == index) -1 else index
        )
    }

    // ================================================================
    // 迁移指南 / Migration guide
    // ================================================================
    /**
     * 应用迁移方案
     * 生成对应权限请求路径的代码修改建议
     * @param path 权限请求路径
     */
    private fun applyMigration(path: PermissionRequestPath) {
        val guide = when (path) {
            PermissionRequestPath.NEARBY_WIFI_DEVICES -> MigrationGuide(
                path = path,
                beforeCode = """
                    |// No permission handling (Android < 17)
                    |val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
                    |wifiManager.startScan()
                """.trimMargin(),
                afterCode = """
                    |// Android 17+: Use NEARBY_WIFI_DEVICES (temporary, 90 days)
                    |if (checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
                    |    != PackageManager.PERMISSION_GRANTED) {
                    |    requestPermissions(
                    |        arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES),
                    |        REQUEST_CODE_NEARBY_WIFI
                    |    )
                    |} else {
                    |    val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
                    |    wifiManager.startScan()
                    |}
                """.trimMargin(),
                manifestDiff = """
                    |+ <uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES"
                    |+     android:usesPermissionFlags="neverForLocation"
                    |+     tools:targetApi="tiramisu" />
                """.trimMargin()
            )
            PermissionRequestPath.ACCESS_LOCAL_NETWORK -> MigrationGuide(
                path = path,
                beforeCode = """
                    |// No permission handling (Android < 17)
                    |val socket = Socket()
                    |socket.connect(InetSocketAddress(host, port))
                """.trimMargin(),
                afterCode = """
                    |// Android 17+: Use ACCESS_LOCAL_NETWORK (persistent)
                    |val permission = Manifest.permission.ACCESS_LOCAL_NETWORK
                    |if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    |    if (shouldShowRequestPermissionRationale(permission)) {
                    |        // Show rationale, then request
                    |    }
                    |    requestPermissions(arrayOf(permission), REQUEST_CODE_LOCAL_NETWORK)
                    |} else {
                    |    val socket = Socket()
                    |    socket.connect(InetSocketAddress(host, port))
                    |}
                """.trimMargin(),
                manifestDiff = """
                    |+ <uses-permission android:name="android.permission.ACCESS_LOCAL_NETWORK" />
                """.trimMargin()
            )
            PermissionRequestPath.PRINTER_API -> MigrationGuide(
                path = path,
                beforeCode = """
                    |// Legacy print API without Android 17 considerations
                    |fun printDocument(doc: Document) { ... }
                """.trimMargin(),
                afterCode = """
                    |// Android 17+: Use PRINT permission + system print dialog
                    |// No ACCESS_LOCAL_NETWORK needed for system print workflow
                    |private fun printDocument(context: Context, doc: Document) {
                    |    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    |    printManager.print("MyDoc", doc.adapter, null)
                    |}
                """.trimMargin(),
                manifestDiff = """
                    |+ <uses-permission android:name="android.permission.INTERNET" />
                    |+ <!-- ACCESS_LOCAL_NETWORK not needed for system print workflow -->
                """.trimMargin()
            )
        }
        _state.value = _state.value.copy(migrationGuide = guide)
    }

    // ================================================================
    // 测试套件生成 / Test suite generation
    // ================================================================
    /**
     * 生成针对当前权限状态的测试用例代码
     */
    private fun generateTestSuite() {
        viewModelScope.launch {
            val state = _state.value.selectedPermissionState
            val testCode = when (state) {
                PermissionState.GRANTED -> """
                    |package com.example.app.test
                    |
                    |import android.Manifest
                    |import androidx.test.ext.junit.runners.AndroidJUnit4
                    |import androidx.test.platform.app.InstrumentationRegistry
                    |import org.junit.Test
                    |import org.junit.runner.RunWith
                    |
                    |@RunWith(AndroidJUnit4::class)
                    |class LocalNetworkPermissionTest {
                    |
                    |    @Test
                    |    fun testLocalNetworkAccess_granted() {
                    |        // Permission granted - socket connection should succeed
                    |        val socket = Socket()
                    |        socket.connect(InetSocketAddress("192.168.1.1", 8080), 3000)
                    |        assert(socket.isConnected)
                    |    }
                    |}
                """.trimMargin()
                PermissionState.DENIED -> """
                    |package com.example.app.test
                    |
                    |import android.Manifest
                    |import androidx.test.ext.junit.runners.AndroidJUnit4
                    |import org.junit.Test
                    |import org.junit.runner.RunWith
                    |
                    |@RunWith(AndroidJUnit4::class)
                    |class LocalNetworkPermissionTest {
                    |
                    |    @Test(expected = SecurityException::class)
                    |    fun testLocalNetworkAccess_denied() {
                    |        // Permission denied - should throw SecurityException
                    |        grantPermission(Manifest.permission.ACCESS_LOCAL_NETWORK)
                    |        denyPermission(Manifest.permission.ACCESS_LOCAL_NETWORK)
                    |        val socket = Socket()  // Throws SecurityException
                    |    }
                    |}
                """.trimMargin()
                PermissionState.PERMANENTLY_DENIED -> """
                    |package com.example.app.test
                    |
                    |import android.Manifest
                    |import androidx.test.ext.junit.runners.AndroidJUnit4
                    |import org.junit.Test
                    |import org.junit.runner.RunWith
                    |
                    |@RunWith(AndroidJUnit4::class)
                    |class LocalNetworkPermissionTest {
                    |
                    |    @Test
                    |    fun testLocalNetworkAccess_permanentlyDenied() {
                    |        // Must navigate to Settings to re-enable
                    |        val shouldShowSettings = shouldShowRequestPermissionRationale(
                    |            Manifest.permission.ACCESS_LOCAL_NETWORK
                    |        )
                    |        assert(!shouldShowSettings)  // Should direct to Settings
                    |    }
                    |}
                """.trimMargin()
                PermissionState.NOT_ASKED -> """
                    |package com.example.app.test
                    |
                    |import android.Manifest
                    |import androidx.test.ext.junit.runners.AndroidJUnit4
                    |import org.junit.Test
                    |import org.junit.runner.RunWith
                    |
                    |@RunWith(AndroidJUnit4::class)
                    |class LocalNetworkPermissionTest {
                    |
                    |    @Test
                    |    fun testLocalNetworkAccess_notAsked() {
                    |        // Should request permission before accessing local network
                    |        val notAsked = checkSelfPermission(
                    |            Manifest.permission.ACCESS_LOCAL_NETWORK
                    |        ) != PackageManager.PERMISSION_GRANTED
                    |        assert(notAsked)
                    |    }
                    |}
                """.trimMargin()
            }
            _state.value = _state.value.copy(testSuiteCode = testCode)
            _effect.send(LocalNetworkPermissionEffect.ShowSnackbar("测试用例已生成"))
        }
    }

    // ================================================================
    // 报告导出 / Report export
    // ================================================================
    /**
     * 导出完整迁移报告为 Markdown
     */
    private fun exportReport() {
        viewModelScope.launch {
            val s = _state.value
            val radar = s.radarScores
            val report = buildString {
                appendLine("# Android 17 ACCESS_LOCAL_NETWORK 迁移检测报告")
                appendLine()
                appendLine("## 健康度评分")
                appendLine()
                appendLine("| 维度 | 评分 |")
                appendLine("|------|------|")
                appendLine("| Manifest 声明 | ${radar.manifestDeclaration}/100 |")
                appendLine("| 权限请求路径 | ${radar.permissionRequestPath}/100 |")
                appendLine("| 降级策略 | ${radar.gracefulDegradation}/100 |")
                appendLine("| 测试覆盖 | ${radar.testCoverage}/100 |")
                appendLine("| 依赖库兼容性 | ${radar.dependencyLibraries}/100 |")
                appendLine("| API 调用合规 | ${radar.apiCalls}/100 |")
                appendLine()
                appendLine("## 受影响 API (${s.affectedApis.size})")
                appendLine()
                s.affectedApis.forEach { api ->
                    appendLine("### ${api.apiName}")
                    appendLine("- 文件: `${api.filePath}:${api.lineNumber}`")
                    appendLine("- 风险: **${api.severity}**")
                    appendLine("- 建议: ${api.suggestedFix}")
                    appendLine()
                }
                appendLine("## 迁移建议")
                appendLine()
                appendLine("1. 在 AndroidManifest.xml 中声明 ACCESS_LOCAL_NETWORK 或 NEARBY_WIFI_DEVICES")
                appendLine("2. 在访问本地网络前检查运行时权限")
                appendLine("3. 实现权限被拒后的降级策略（引导至系统设置）")
                appendLine("4. 添加权限状态的回归测试")
            }
            _state.value = _state.value.copy(exportedReport = report)
            _effect.send(LocalNetworkPermissionEffect.ShowSnackbar("报告已生成"))
        }
    }

    // ================================================================
    // 代码复制 / Code copy
    // ================================================================
    /**
     * 复制代码到剪贴板
     * @param code 要复制的代码
     */
    private fun copyCode(code: String) {
        viewModelScope.launch {
            _effect.send(LocalNetworkPermissionEffect.CopyToClipboard(code))
            _effect.send(LocalNetworkPermissionEffect.ShowSnackbar("已复制到剪贴板"))
        }
    }

    // ================================================================
    // 模拟数据 / Mock data
    // ================================================================
    /**
     * 加载模拟数据用于 Dashboard 展示
     * 真实场景中由 Gradle 插件扫描后注入
     */
    private fun loadMockData() {
        _state.value = _state.value.copy(
            radarScores = RadarScores(
                manifestDeclaration = 40,
                permissionRequestPath = 25,
                gracefulDegradation = 60,
                testCoverage = 15,
                dependencyLibraries = 80,
                apiCalls = 35
            ),
            affectedApis = listOf(
                AffectedApi(
                    apiName = "WifiManager.startScan()",
                    filePath = "app/src/main/java/com/mvi/kenny/feature/wifi/WifiScanner.kt",
                    lineNumber = 42,
                    severity = Severity.P0,
                    codeContext = "wifiManager.startScan()  // ⚠️ API 37+ requires permission",
                    suggestedFix = "Add ACCESS_LOCAL_NETWORK permission + runtime request"
                ),
                AffectedApi(
                    apiName = "Socket.connect()",
                    filePath = "app/src/main/java/com/mvi/kenny/feature/network/NetworkClient.kt",
                    lineNumber = 87,
                    severity = Severity.P0,
                    codeContext = "socket.connect(InetSocketAddress(host, port))  // ⚠️ Needs permission",
                    suggestedFix = "Request ACCESS_LOCAL_NETWORK before Socket.connect()"
                ),
                AffectedApi(
                    apiName = "NetworkInterface.getNetworkInterfaces()",
                    filePath = "app/src/main/java/com/mvi/kenny/feature/network/InterfaceScanner.kt",
                    lineNumber = 23,
                    severity = Severity.P1,
                    codeContext = "NetworkInterface.getNetworkInterfaces()  // ⚠️ Local network access",
                    suggestedFix = "Add ACCESS_LOCAL_NETWORK permission and handle denial"
                ),
                AffectedApi(
                    apiName = "InetAddress.getAllByName()",
                    filePath = "app/src/main/java/com/mvi/kenny/feature/discovery/DeviceDiscovery.kt",
                    lineNumber = 56,
                    severity = Severity.P1,
                    codeContext = "InetAddress.getAllByName(hostname)  // ⚠️ May need permission",
                    suggestedFix = "Verify target is not local or request permission"
                ),
                AffectedApi(
                    apiName = "DatagramSocket.send()",
                    filePath = "app/src/main/java/com/mvi/kenny/feature/broadcast/BroadcastSender.kt",
                    lineNumber = 34,
                    severity = Severity.P2,
                    codeContext = "ds.send(DatagramPacket(...))  // ⚠️ UDP broadcast",
                    suggestedFix = "Consider NEARBY_WIFI_DEVICES for short-range discovery"
                )
            )
        )
    }
}
