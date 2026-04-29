package com.mvi.kenny.feature.lannetwork

// ================================================================
// LanPermissionViewModel — PRD-199 Android 17 Local Network Permission
// 合规检测工具包 ViewModel
// ================================================================
// MVI ViewModel for Android 17 Local Network Permission toolkit.
//
// Responsible for:
//   - Processing user Intents and updating State
//   - Managing side Effects (toast, navigation)
//   - Running business logic in viewModelScope
//
// PRD-199: Android 17 Local Network Permission 合规检测工具包
// Design: memory/agency/designs/PRD-199-Android-17-Local-Network-Permission-合规检测工具包.md
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

// =============================================================
// LanPermissionViewModel — Main ViewModel class
// =============================================================
/**
 * LanPermission ViewModel / 局域网权限工具 ViewModel
 *
 * @param initialState Initial state / 初始状态
 */
class LanPermissionViewModel(
    initialState: LanPermissionState = LanPermissionState.Initial
) : ViewModel() {

    // ── State ────────────────────────────────────────────────
    /** Internal mutable state / 内部可变状态 */
    private val _state = MutableStateFlow(initialState)

    /** Public immutable state flow / 公开不可变状态流 */
    val state: StateFlow<LanPermissionState> = _state.asStateFlow()

    // ── Effects ──────────────────────────────────────────────
    /** Effect channel for one-time events / 副作用通道 */
    private val _effect = Channel<LanPermissionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ── Internal ─────────────────────────────────────────────
    /** Current scan job for cancellation / 当前扫描任务（用于取消） */
    private var scanJob: Job? = null

    // ── Code Templates ────────────────────────────────────────
    /** All available code templates / 所有可用代码模板 */
    private val templates = buildTemplates()

    init {
        // Initialize state with templates / 初始化状态中的模板
        _state.value = _state.value.copy(codeTemplates = templates)
    }

    // ==========================================================
    // Intent Processing — 处理用户意图
    // ==========================================================
    /**
     * Process user intent / 处理用户意图
     *
     * Called from UI layer when user performs an action.
     * Delegates to specific handler methods.
     *
     * @param intent User intent / 用户意图
     */
    fun processIntent(intent: LanPermissionIntent) {
        when (intent) {
            is LanPermissionIntent.SelectModule -> handleSelectModule(intent.module)
            is LanPermissionIntent.NavigateBack -> handleNavigateBack()
            is LanPermissionIntent.StartScan -> handleStartScan()
            is LanPermissionIntent.CancelScan -> handleCancelScan()
            is LanPermissionIntent.FilterByRisk -> handleFilterByRisk(intent.risk)
            is LanPermissionIntent.SelectTemplate -> handleSelectTemplate(intent.template)
            is LanPermissionIntent.UpdateCiConfig -> handleUpdateCiConfig(intent.config)
            is LanPermissionIntent.CopyCode -> handleCopyCode(intent.code)
            is LanPermissionIntent.RunCiCheck -> handleRunCiCheck()
            is LanPermissionIntent.DismissError -> handleDismissError()
            is LanPermissionIntent.ClearResults -> handleClearResults()
        }
    }

    // ==========================================================
    // Intent Handlers — 意图处理器
    // ==========================================================

    /** Handle module selection / 处理模块选择 */
    private fun handleSelectModule(module: ModuleType) {
        _state.value = _state.value.copy(selectedModule = module)
    }

    /** Handle back navigation / 处理返回导航 */
    private fun handleNavigateBack() {
        _state.value = _state.value.copy(selectedModule = null)
    }

    /** Handle start scan / 处理开始扫描 */
    private fun handleStartScan() {
        // Cancel any existing scan / 取消现有扫描
        scanJob?.cancel()

        scanJob = viewModelScope.launch {
            _state.value = _state.value.copy(
                scanProgress = ScanProgress.SCANNING,
                scanResults = emptyList(),
                scanLogs = emptyList(),
                error = null
            )

            // Simulate scan process / 模拟扫描过程
            // In real implementation, this would use AST parsing to scan source files
            val mockResults = mutableListOf<ScanResult>()
            val mockLogs = mutableListOf<String>()

            try {
                // Phase 1: Initialize / 阶段1：初始化
                mockLogs.add("[INFO] Initializing LAN permission scanner...")
                mockLogs.add("[INFO] Target SDK: 37 (Android 17)")
                mockLogs.add("[INFO] Scanning for local network access patterns...")
                _state.value = _state.value.copy(scanLogs = mockLogs.toList())
                delay(300)

                // Phase 2: Scan patterns / 阶段2：扫描模式
                val patterns = listOf(
                    Triple("SocketActivity.kt", "DatagramSocket", "java.net.DatagramSocket"),
                    Triple("NetworkClient.kt", "HttpURLConnection", "java.net.HttpURLConnection"),
                    Triple("DeviceDiscovery.kt", "OkHttp", "okhttp3.OkHttpClient"),
                    Triple("ApiService.kt", "Retrofit", "retrofit2.Retrofit"),
                    Triple("BroadcastReceiver.kt", "BroadcastReceiver", "android.content.BroadcastReceiver"),
                    Triple("InetAddressHelper.kt", "InetAddress", "java.net.InetAddress"),
                    Triple("MulticastSocket.kt", "MulticastSocket", "java.net.MulticastSocket"),
                    Triple("ServerSocket.kt", "ServerSocket", "java.net.ServerSocket")
                )

                patterns.forEachIndexed { index, (file, type, _) ->
                    mockLogs.add("[SCANNING] $file")
                    _state.value = _state.value.copy(scanLogs = mockLogs.toList())
                    delay(200)

                    val riskLevel = when {
                        type in listOf("SocketActivity.kt", "DatagramSocket", "BroadcastReceiver") -> RiskLevel.P0
                        type in listOf("OkHttp", "Retrofit", "HttpURLConnection") -> RiskLevel.P1
                        else -> RiskLevel.P2
                    }

                    val snippet = when (type) {
                        "DatagramSocket" -> """
                            |// ${file}
                            |val socket = DatagramSocket()
                            |socket.send(DatagramPacket(data, len, address))
                        """.trimMargin()
                        "HttpURLConnection" -> """
                            |// ${file}
                            |val url = URL("http://192.168.1.100/api")
                            |val conn = url.openConnection() as HttpURLConnection
                        """.trimMargin()
                        "OkHttp" -> """
                            |// ${file}
                            |val client = OkHttpClient()
                            |val request = Request.Builder().url("http://192.168.0.1").build()
                        """.trimMargin()
                        "Retrofit" -> """
                            |// ${file}
                            |@GET("http://192.168.0.1/device")
                            |fun getDevice(): Call<Device>
                        """.trimMargin()
                        "BroadcastReceiver" -> """
                            |// ${file}
                            |val filter = IntentFilter("android.net.wifi.STATE_CHANGE")
                            |registerReceiver(receiver, filter)
                        """.trimMargin()
                        else -> "// Code snippet for $type in $file"
                    }

                    val isCompliant = Random.nextBoolean() && riskLevel != RiskLevel.P0

                    mockResults.add(
                        ScanResult(
                            id = UUID.randomUUID().toString(),
                            file = "app/src/main/java/com/example/app/$file",
                            line = Random.nextInt(10, 200),
                            method = "${type.removeSuffix(".kt").lowercase()}.connect()",
                            accessType = type.removeSuffix(".kt"),
                            riskLevel = riskLevel,
                            snippet = snippet,
                            suggestion = getSuggestion(type, riskLevel),
                            isCompliant = isCompliant
                        )
                    )
                }

                // Phase 3: Complete / 阶段3：完成
                mockLogs.add("[INFO] Scan completed: ${mockResults.size} issues found")
                mockLogs.add("[INFO] P0: ${mockResults.count { it.riskLevel == RiskLevel.P0 }} | P1: ${mockResults.count { it.riskLevel == RiskLevel.P1 }} | P2: ${mockResults.count { it.riskLevel == RiskLevel.P2 }}")
                _state.value = _state.value.copy(
                    scanProgress = ScanProgress.COMPLETED,
                    scanResults = mockResults.toList(),
                    scanLogs = mockLogs.toList()
                )
                _effect.send(LanPermissionEffect.ScanCompleted)

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    mockLogs.add("[CANCELLED] Scan cancelled by user")
                } else {
                    mockLogs.add("[ERROR] Scan failed: ${e.message}")
                    _state.value = _state.value.copy(
                        scanProgress = ScanProgress.FAILED,
                        error = e.message
                    )
                    _effect.send(LanPermissionEffect.ShowError(e.message ?: "Unknown error"))
                }
            }
        }
    }

    /** Handle cancel scan / 处理取消扫描 */
    private fun handleCancelScan() {
        scanJob?.cancel()
        _state.value = _state.value.copy(
            scanProgress = ScanProgress.IDLE,
            scanLogs = _state.value.scanLogs + "[CANCELLED] Scan cancelled by user"
        )
    }

    /** Handle risk filter change / 处理风险等级筛选变更 */
    private fun handleFilterByRisk(risk: RiskLevel?) {
        _state.value = _state.value.copy(riskFilter = risk)
    }

    /** Handle template tab change / 处理模板标签页变更 */
    private fun handleSelectTemplate(template: TemplateType) {
        _state.value = _state.value.copy(selectedTemplate = template)
    }

    /** Handle CI config update / 处理 CI 配置更新 */
    private fun handleUpdateCiConfig(config: CiConfig) {
        _state.value = _state.value.copy(ciConfig = config)
    }

    /** Handle copy code / 处理复制代码 */
    private fun handleCopyCode(code: String) {
        viewModelScope.launch {
            // In real implementation, copy to clipboard via ClipboardManager
            _effect.send(LanPermissionEffect.CodeCopied)
            _effect.send(LanPermissionEffect.ShowToast("Code copied to clipboard / 代码已复制"))
        }
    }

    /** Handle run CI check / 处理运行 CI 检测 */
    private fun handleRunCiCheck() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val config = _state.value.ciConfig
            val report = buildString {
                appendLine("{")
                appendLine("  \"check_name\": \"lan_permission_compliance\",")
                appendLine("  \"target_sdk\": ${config.targetSdk},")
                appendLine("  \"min_sdk\": ${config.minSdk},")
                appendLine("  \"check_legacy\": ${config.checkLegacy},")
                appendLine("  \"results\": {")
                appendLine("    \"ACCESS_LOCAL_NETWORK\": \"${if (config.targetSdk >= 37) "REQUIRED" else "NOT_REQUIRED"}\",")
                appendLine("    \"NEARBY_WIFI_DEVICES\": \"CONDITIONAL\"")
                appendLine("  },")
                appendLine("  \"recommendation\": \"Add ACCESS_LOCAL_NETWORK runtime permission request if targeting API 37+\",")
                appendLine("  \"compliant\": false")
                appendLine("}")
            }

            _state.value = _state.value.copy(isLoading = false)
            _effect.send(LanPermissionEffect.CiCheckCompleted(report))
            _effect.send(LanPermissionEffect.ShowToast("CI check completed / CI 检测完成"))
        }
    }

    /** Handle dismiss error / 处理关闭错误 */
    private fun handleDismissError() {
        _state.value = _state.value.copy(error = null)
    }

    /** Handle clear results / 处理清除结果 */
    private fun handleClearResults() {
        _state.value = _state.value.copy(
            scanResults = emptyList(),
            scanLogs = emptyList(),
            scanProgress = ScanProgress.IDLE
        )
    }

    // ==========================================================
    // Helper Methods — 辅助方法
    // ==========================================================

    /**
     * Get fix suggestion based on access type and risk level
     * / 根据访问类型和风险等级获取修复建议
     */
    private fun getSuggestion(accessType: String, riskLevel: RiskLevel): String {
        return when (accessType) {
            "DatagramSocket", "MulticastSocket", "ServerSocket" ->
                "🔴 P0: Direct socket access detected. " +
                "Request ACCESS_LOCAL_NETWORK runtime permission. " +
                "建议：申请 ACCESS_LOCAL_NETWORK 运行时权限，或使用系统设备选择器。"
            "HttpURLConnection" ->
                "🟡 P1: HTTP connection to potential LAN IP. " +
                "Consider using NearbyDevices API for privacy-first device discovery. " +
                "建议：考虑使用 NearbyDevices API 进行隐私优先的设备发现。"
            "OkHttp", "Retrofit" ->
                "🟡 P1: Network library detected. " +
                "Verify all LAN endpoints are intentional. " +
                "建议：验证所有局域网端点是否为预期访问。"
            "BroadcastReceiver" ->
                "🔴 P0: Network state BroadcastReceiver detected. " +
                "This pattern may be affected by Android 17 local network protection. " +
                "建议：检查是否涉及局域网发现，可能需要申请权限。"
            "InetAddress" ->
                "🟢 P2: InetAddress resolution. " +
                "Verify if target addresses include private IP ranges. " +
                "建议：确认目标地址是否包含私有 IP 段。"
            else ->
                "ℹ️ Review this access for Android 17 Local Network Protection compliance. " +
                "请检查此访问是否符合 Android 17 局域网保护规范。"
        }
    }

    /**
     * Build all code templates / 构建所有代码模板
     */
    private fun buildTemplates(): Map<ModuleType, List<CodeTemplate>> {
        return mapOf(
            ModuleType.PERMISSION_TEMPLATE to listOf(
                // Kotlin — Runtime Permission Request
                CodeTemplate(
                    templateType = TemplateType.KOTLIN,
                    title = "ACCESS_LOCAL_NETWORK Runtime Permission Request",
                    description = "完整运行时权限请求代码，含 rationale、永久拒绝处理、降级回调",
                    code = """
                        |// ─────────────────────────────────────────────────────────
                        |// ACCESS_LOCAL_NETWORK Runtime Permission Request
                        |// 运行时权限请求 — Android 17 Local Network Protection
                        |// ─────────────────────────────────────────────────────────
                        |
                        |private val lanPermissionLauncher = registerForActivityResult(
                        |    ActivityResultContracts.RequestPermission()
                        |) { isGranted ->
                        |    if (isGranted) {
                        |        // Permission granted — enable LAN features
                        |        // 权限已授予 — 启用局域网功能
                        |        onLanPermissionGranted()
                        |    } else {
                        |        // Permission denied — use fallback
                        |        // 权限被拒绝 — 使用降级方案
                        |        onLanPermissionDenied()
                        |    }
                        |}
                        |
                        |private fun checkAndRequestLanPermission() {
                        |    when {
                        |        // Already granted / 已授予
                        |        ContextCompat.checkSelfPermission(
                        |            this,
                        |            Manifest.permission.ACCESS_LOCAL_NETWORK
                        |        ) == PackageManager.PERMISSION_GRANTED -> {
                        |            onLanPermissionGranted()
                        |        }
                        |        // Show rationale / 显示说明
                        |        shouldShowRequestPermissionRationale(
                        |            Manifest.permission.ACCESS_LOCAL_NETWORK
                        |        ) -> {
                        |            showLanPermissionRationale()
                        |        }
                        |        // First request / 首次请求
                        |        else -> {
                        |            lanPermissionLauncher.launch(
                        |                Manifest.permission.ACCESS_LOCAL_NETWORK
                        |            )
                        |        }
                        |    }
                        |}
                        |
                        |private fun showLanPermissionRationale() {
                        |    AlertDialog.Builder(this)
                        |        .setTitle("局域网权限 / LAN Permission")
                        |        .setMessage(
                        |            "此应用需要访问局域网才能发现并连接本地设备。\n" +
                        |            "This app needs LAN access to discover local devices."
                        |        )
                        |        .setPositiveButton("授权 / Grant") { _, _ ->
                        |            lanPermissionLauncher.launch(
                        |                Manifest.permission.ACCESS_LOCAL_NETWORK
                        |            )
                        |        }
                        |        .setNegativeButton("取消 / Cancel") { _, _ ->
                        |            onLanPermissionDenied()
                        |        }
                        |        .show()
                        |}
                        |
                        |private fun onLanPermissionGranted() {
                        |    // Enable LAN features / 启用局域网功能
                        |    discoveryClient.startDiscovery()
                        |}
                        |
                        |private fun onLanPermissionDenied() {
                        |    // Show fallback UI / 显示降级 UI
                        |    // 提示用户：当前无法发现设备，请在设置中开启局域网权限
                        |    showFallbackMessage()
                        |}
                    """.trimMargin(),
                    parameters = listOf("onLanPermissionGranted", "onLanPermissionDenied", "showFallbackMessage")
                ),
                // XML — Manifest Declaration
                CodeTemplate(
                    templateType = TemplateType.XML,
                    title = "AndroidManifest.xml Permission Declaration",
                    description = "AndroidManifest.xml 权限声明模板",
                    code = """
                        |<!-- ───────────────────────────────────────────────────────── -->
                        |<!-- Android 17 Local Network Protection — Permissions -->
                        |<!-- Android 17 局域网保护 — 权限声明 -->
                        |<!-- ───────────────────────────────────────────────────────── -->
                        |
                        |<!-- ACCESS_LOCAL_NETWORK: Required for API 37+ apps -->
                        |<!-- ACCESS_LOCAL_NETWORK：API 37+ 应用必须声明 -->
                        |<uses-permission
                        |    android:name="android.permission.ACCESS_LOCAL_NETWORK"
                        |    android:maxSdkVersion="36" />
                        |
                        |<!-- NEARBY_WIFI_DEVICES: For Wi-Fi device discovery -->
                        |<!-- NEARBY_WIFI_DEVICES：用于 Wi-Fi 设备发现 -->
                        |<uses-permission
                        |    android:name="android.permission.NEARBY_WIFI_DEVICES"
                        |    android:usesPermissionFlags="neverForLocation" />
                        |
                        |<!-- INTERNET: Always required for any network access -->
                        |<!-- INTERNET：任何网络访问都需要 -->
                        |<uses-permission android:name="android.permission.INTERNET" />
                        |
                        |<!-- ACCESS_WIFI_STATE: For Wi-Fi state monitoring -->
                        |<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
                    """.trimMargin(),
                    parameters = emptyList()
                ),
                // Manifest — Pure Declaration
                CodeTemplate(
                    templateType = TemplateType.MANIFEST,
                    title = "Minimal Manifest (API 37+ Target)",
                    description = "最简 Manifest 声明（targetSdk 37+）",
                    code = """
                        |<!-- PRD-199: Android 17 Local Network Protection -->
                        |<manifest xmlns:android="http://schemas.android.com/apk/res/android">
                        |
                        |    <!-- Required for targeting API 37+ -->
                        |    <uses-permission
                        |        android:name="android.permission.ACCESS_LOCAL_NETWORK" />
                        |
                        |    <!-- For Wi-Fi device discovery (optional but recommended) -->
                        |    <uses-permission
                        |        android:name="android.permission.NEARBY_WIFI_DEVICES"
                        |        android:usesPermissionFlags="neverForLocation" />
                        |
                        |</manifest>
                    """.trimMargin(),
                    parameters = emptyList()
                )
            ),
            ModuleType.DEVICE_PICKER to listOf(
                CodeTemplate(
                    templateType = TemplateType.KOTLIN,
                    title = "NearbyDevices API — Privacy-First Device Picker",
                    description = "系统设备选择器集成 — 隐私保护路径，无需声明 ACCESS_LOCAL_NETWORK",
                    code = """
                        |// ─────────────────────────────────────────────────────────
                        |// NearbyDevices API — Privacy-First Device Picker
                        |// 系统设备选择器 — 隐私保护路径
                        |// ─────────────────────────────────────────────────────────
                        |
                        |// Privacy-first approach: uses system device picker instead of
                        |// requesting ACCESS_LOCAL_NETWORK permission.
                        |// 隐私优先方案：使用系统设备选择器，无需声明权限
                        |
                        |private val devicePickerLauncher = registerForActivityResult(
                        |    ActivityResultContracts.PickNearbyDevices()
                        |) { deviceInfo ->
                        |    when (deviceInfo) {
                        |        is android.nearby.connections.DeviceInfo -> {
                        |            // User selected a device — connect to it
                        |            // 用户选择了设备 — 建立连接
                        |            connectToDevice(deviceInfo.endpointId)
                        |        }
                        |        null -> {
                        |            // User cancelled / 用户取消选择
                        |            showNoDeviceSelectedMessage()
                        |        }
                        |    }
                        |}
                        |
                        |/**
                        | * Launch the system device picker
                        | * / 启动系统设备选择器
                        | *
                        | * This approach:
                        | * ✓ Does NOT require ACCESS_LOCAL_NETWORK permission
                        | * ✓ Does NOT require NEARBY_WIFI_DEVICES permission
                        | * ✓ Privacy-preserving: OS handles device discovery
                        | * ✓ No manual IP scanning needed
                        | */
                        |private fun launchDevicePicker() {
                        |    try {
                        |        devicePickerLauncher.launch(
                        |            android.nearby.connections.PickerIntent()
                        |        )
                        |    } catch (e: Exception) {
                        |        // Fallback to runtime permission approach
                        |        // 降级到运行时权限方案
                        |        checkAndRequestLanPermission()
                        |    }
                        |}
                    """.trimMargin(),
                    parameters = listOf("connectToDevice", "showNoDeviceSelectedMessage")
                ),
                CodeTemplate(
                    templateType = TemplateType.XML,
                    title = "System Picker — Manifest (Minimal)",
                    description = "系统选择器方案 — 最简 Manifest",
                    code = """
                        |<!-- ───────────────────────────────────────────────────────── -->
                        |<!-- Privacy-First Device Picker — Minimal Manifest -->
                        |<!-- 隐私优先设备选择器 — 最简 Manifest -->
                        |<!-- No ACCESS_LOCAL_NETWORK needed when using system picker -->
                        |<!-- 使用系统选择器时无需声明 ACCESS_LOCAL_NETWORK -->
                        |<!-- ───────────────────────────────────────────────────────── -->
                        |
                        |<manifest xmlns:android="http://schemas.android.com/apk/res/android">
                        |
                        |    <!-- INTERNET is always required -->
                        |    <uses-permission android:name="android.permission.INTERNET" />
                        |
                        |    <!-- No ACCESS_LOCAL_NETWORK needed! -->
                        |    <!-- 无需声明 ACCESS_LOCAL_NETWORK！-->
                        |
                        |</manifest>
                    """.trimMargin(),
                    parameters = emptyList()
                )
            ),
            ModuleType.CI_TOOL to listOf(
                CodeTemplate(
                    templateType = TemplateType.KOTLIN,
                    title = "Gradle Plugin Configuration (lan-permission-check)",
                    description = "CI 合规检测 Gradle 插件配置示例",
                    code = """
                        |// ─────────────────────────────────────────────────────────
                        |// lan-permission-check Gradle Plugin Configuration
                        |// CI 合规检测 Gradle 插件配置
                        |// ─────────────────────────────────────────────────────────
                        |
                        |// build.gradle.kts (app module)
                        |plugins {
                        |    id("com.android.lan-permission-check") version "1.0.0"
                        |}
                        |
                        |lanPermissionCheck {
                        |    // Target SDK version / 目标 SDK 版本
                        |    targetSdk.set(37)
                        |
                        |    // Minimum SDK version / 最低 SDK 版本
                        |    minSdk.set(24)
                        |
                        |    // Check legacy compatibility / 检查 Legacy 兼容性
                        |    checkLegacy.set(true)
                        |
                        |    // Fail build on non-compliance / 不合规时阻塞构建
                        |    failOnNonCompliance.set(true)
                        |
                        |    // Report output format / 报告输出格式: JSON | HTML | MARKDOWN
                        |    reportFormat.set("JSON")
                        |
                        |    // Custom rules file (optional) / 自定义规则文件（可选）
                        |    rulesFile.set(file("lan-permission-rules.json"))
                        |
                        |    // Continue after finding all issues / 发现所有问题后继续
                        |    continueOnError.set(true)
                        |}
                        |
                        |// ─────────────────────────────────────────────────────────
                        |// CI Command / CI 命令
                        |// ./gradlew checkLanPermission --continue
                        |// ─────────────────────────────────────────────────────────
                    """.trimMargin(),
                    parameters = listOf("targetSdk", "minSdk", "checkLegacy", "failOnNonCompliance")
                ),
                CodeTemplate(
                    templateType = TemplateType.XML,
                    title = "CI GitHub Actions Workflow",
                    description = "GitHub Actions CI 配置示例",
                    code = """
                        |# ─────────────────────────────────────────────────────────
                        |# LAN Permission CI — GitHub Actions Workflow
                        |# 局域网权限 CI — GitHub Actions 配置
                        |# ─────────────────────────────────────────────────────────
                        |
                        |name: LAN Permission Compliance Check
                        |
                        |on:
                        |  push:
                        |    branches: [main, develop]
                        |  pull_request:
                        |
                        |jobs:
                        |  lan-compliance:
                        |    runs-on: ubuntu-latest
                        |    steps:
                        |      - uses: actions/checkout@v4
                        |
                        |      - name: Setup JDK
                        |        uses: actions/setup-java@v4
                        |        with:
                        |          distribution: 'temurin'
                        |          java-version: '17'
                        |
                        |      - name: Run LAN Permission Check
                        |        run: |
                        |          ./gradlew checkLanPermission \
                        |            --continue \
                        |            -PtargetSdk=37
                        |
                        |      - name: Upload Report
                        |        if: always()
                        |        uses: actions/upload-artifact@v4
                        |        with:
                        |          name: lan-permission-report
                        |          path: build/reports/lan-permission/
                    """.trimMargin(),
                    parameters = emptyList()
                )
            ),
            ModuleType.FALLBACK_TEMPLATE to listOf(
                CodeTemplate(
                    templateType = TemplateType.KOTLIN,
                    title = "Graceful Degradation UX — When Permission Denied",
                    description = "权限拒绝时的优雅降级 UX 模板",
                    code = """
                        |// ─────────────────────────────────────────────────────────
                        |// Fallback Strategy — Permission Denied UX
                        |// 降级策略 — 权限拒绝时的用户体验
                        |// ─────────────────────────────────────────────────────────
                        |
                        |/**
                        | * Show fallback message when LAN permission is denied
                        | * / 局域网权限被拒绝时显示降级提示
                        | *
                        | * Key principles:
                        | * ✓ Never fail silently — user must know why feature is unavailable
                        | * ✓ Provide clear guidance — show how to enable in Settings
                        | * ✓ Offer alternatives — use system device picker as fallback
                        | */
                        |private fun showFallbackMessage() {
                        |    AlertDialog.Builder(context)
                        |        .setTitle("无法发现设备 / Unable to Discover Devices")
                        |        .setMessage(
                        |            "局域网权限被拒绝，无法发现本地设备。\n\n" +
                        |            "您可以：\n" +
                        |            "• 点击「系统选择器」使用隐私保护方案\n" +
                        |            "• 或在「设置」中开启局域网权限\n\n" +
                        |            "LAN permission denied. You can:\n" +
                        |            "• Use System Picker for privacy-first discovery\n" +
                        |            "• Enable in Settings to use full features"
                        |        )
                        |        .setPositiveButton("系统选择器 / System Picker") { _, _ ->
                        |            launchDevicePicker() // Use system picker as fallback
                        |        }
                        |        .setNeutralButton("设置 / Settings") { _, _ ->
                        |            openAppSettings() // Open app settings
                        |        }
                        |        .setNegativeButton("关闭 / Close") { _, _ ->
                        |            // Do nothing — user acknowledged
                        |        }
                        |        .show()
                        |}
                        |
                        |/**
                        | * Open app settings for manual permission grant
                        | * / 打开应用设置页面让用户手动授权
                        | */
                        |private fun openAppSettings() {
                        |    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        |        data = Uri.fromParts("package", packageName, null)
                        |    }
                        |    startActivity(intent)
                        |}
                    """.trimMargin(),
                    parameters = listOf("launchDevicePicker", "openAppSettings")
                )
            ),
            ModuleType.COORDINATION_GUIDE to listOf(
                CodeTemplate(
                    templateType = TemplateType.KOTLIN,
                    title = "ACCESS_LOCAL_NETWORK vs NEARBY_WIFI_DEVICES Decision",
                    description = "两个权限的选择决策树",
                    code = """
                        |// ─────────────────────────────────────────────────────────
                        |// ACCESS_LOCAL_NETWORK vs NEARBY_WIFI_DEVICES
                        |// 权限选择决策树
                        |// ─────────────────────────────────────────────────────────
                        |
                        |/**
                        | * Permission selection decision tree
                        | * / 权限选择决策树
                        | *
                        | * ACCESS_LOCAL_NETWORK:
                        | *   ✓ Covers Wi-Fi AND Ethernet
                        | *   ✓ Full local network access
                        | *   ✓ Your app does device discovery
                        | *   ✗ Requires runtime permission request
                        | *
                        | * NEARBY_WIFI_DEVICES:
                        | *   ✓ Wi-Fi only (not Ethernet)
                        | *   ✓ No permission needed (system handles discovery)
                        | *   ✓ Privacy-preserving
                        | *   ✗ Only for nearby devices
                        | */
                        |
                        |sealed class LanPermissionStrategy(
                        |    val permission: String?,
                        |    val description: String
                        |) {
                        |    /** Privacy-first: use system device picker */
                        |    SYSTEM_PICKER(
                        |        permission = null,
                        |        description = "系统选择器 — 无需权限，隐私优先"
                        |    ),
                        |
                        |    /** Wi-Fi only, no permission needed */
                        |    NEARBY_WIFI_DEVICES(
                        |        permission = "android.permission.NEARBY_WIFI_DEVICES",
                        |        description = "NEARBY_WIFI_DEVICES — Wi-Fi 设备发现，无需申请"
                        |    ),
                        |
                        |    /** Full local network: Wi-Fi + Ethernet */
                        |    ACCESS_LOCAL_NETWORK(
                        |        permission = "android.permission.ACCESS_LOCAL_NETWORK",
                        |        description = "ACCESS_LOCAL_NETWORK — 完整局域网访问"
                        |    )
                        |}
                        |
                        |/**
                        | * Choose the best permission strategy
                        | * / 选择最佳权限策略
                        | */
                        |private fun choosePermissionStrategy(): LanPermissionStrategy {
                        |    return when {
                        |        // Can use system picker / 可用系统选择器
                        |        supportsNearbyDevicesAPI() -> LanPermissionStrategy.SYSTEM_PICKER
                        |
                        |        // Wi-Fi only / 只需要 Wi-Fi
                        |        isWiFiOnlyUseCase() -> LanPermissionStrategy.NEARBY_WIFI_DEVICES
                        |
                        |        // Need full LAN access (Wi-Fi + Ethernet) / 需要完整局域网
                        |        else -> LanPermissionStrategy.ACCESS_LOCAL_NETWORK
                        |    }
                        |}
                    """.trimMargin(),
                    parameters = emptyList()
                )
            ),
            ModuleType.DISCOVERY_GUIDE to listOf(
                CodeTemplate(
                    templateType = TemplateType.KOTLIN,
                    title = "Privacy-First Device Discovery Patterns",
                    description = "隐私优先的设备发现设计模式：mDNS/Bonjour/UPnP 正确用法",
                    code = """
                        |// ─────────────────────────────────────────────────────────
                        |// Privacy-First Device Discovery — mDNS / Bonjour / UPnP
                        |// 隐私优先设备发现 — mDNS/Bonjour/UPnP 正确用法
                        |// ─────────────────────────────────────────────────────────
                        |
                        |/**
                        | * mDNS/Bonjour Discovery (for local network)
                        | * / mDNS/Bonjour 设备发现（用于局域网）
                        | *
                        | * Use NSD (Network Service Discovery) on Android
                        | * Android 上使用 NSD（网络服务发现）
                        | */
                        |private fun startMdnsDiscovery() {
                        |    val nsdManager = getSystemService(NSD_SERVICE) as NsdManager
                        |
                        |    val discoveryListener = object : NsdManager.DiscoveryListener {
                        |        override fun onDiscoveryStarted(regType: String) {
                        |            log("mDNS discovery started")
                        |        }
                        |        override fun onServiceFound(service: NsdServiceInfo) {
                        |            // Found a service / 发现服务
                        |            resolveService(service)
                        |        }
                        |        override fun onServiceLost(service: NsdServiceInfo) {
                        |            // Service lost / 服务丢失
                        |        }
                        |        // ... other callbacks
                        |    }
                        |
                        |    nsdManager.discoverServices(
                        |        "_http._tcp.local.", // Service type
                        |        NsdManager.PROTOCOL_DNS_SD,
                        |        discoveryListener
                        |    )
                        |}
                        |
                        |/**
                        | * UPnP Device Discovery (for router/smart home)
                        | * / UPnP 设备发现（用于路由器/智能家居）
                        | */
                        |private fun startUpnpDiscovery() {
                        |    // UPnP discovery via SSDP (Simple Service Discovery Protocol)
                        |    // Important: UPnP discovery may require ACCESS_LOCAL_NETWORK
                        |    // 重要：UPnP 发现可能需要 ACCESS_LOCAL_NETWORK 权限
                        |    log("UPnP discovery — check ACCESS_LOCAL_NETWORK permission")
                        |}
                    """.trimMargin(),
                    parameters = emptyList()
                )
            )
        )
    }
}
