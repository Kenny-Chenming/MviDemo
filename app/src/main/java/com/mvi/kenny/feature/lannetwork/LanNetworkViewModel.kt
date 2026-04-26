package com.mvi.kenny.feature.lannetwork

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import kotlin.random.Random

// ================================================================
// LanNetworkViewModel — Android 17 ACCESS_LOCAL_NETWORK 状态管理
// ================================================================
// Inherits ViewModel, holds LanNetworkState and LanNetworkEffect.
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
//   本实现为 Demo 模式，基于代码模式匹配模拟 AST 扫描。
//   真实实现需要接入 Kotlin Compiler Plugin 或 Android Gradle Plugin
//   进行字节码级别的静态分析。
//
// @see LanNetworkContract MVI contract definition
// @see LanNetworkScreen Main UI
// @see LanNetworkDetailSheet Path detail bottom sheet
// ================================================================

class LanNetworkViewModel : ViewModel() {

    // =============================================================
    // State
    // =============================================================
    /** Page state (StateFlow, UI read-only) / 页面状态 */
    private val _state = MutableStateFlow(LanNetworkState.Initial)
    val state: StateFlow<LanNetworkState> = _state.asStateFlow()

    /** Current state snapshot / 当前状态快照 */
    val currentState: LanNetworkState get() = _state.value

    // =============================================================
    // Effect
    // =============================================================
    /** Effect Channel / 副作用通道 */
    private val _effect = Channel<LanNetworkEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // =============================================================
    // Intent Processing
    // =============================================================
    /**
     * Process user intent / 处理用户意图
     *
     * @param intent User intent / 用户意图
     */
    fun sendIntent(intent: LanNetworkIntent) {
        viewModelScope.launch {
            when (intent) {
                is LanNetworkIntent.SelectProject -> handleSelectProject(intent.path)
                is LanNetworkIntent.StartScan -> handleStartScan()
                is LanNetworkIntent.CancelScan -> handleCancelScan()
                is LanNetworkIntent.SelectPath -> handleSelectPath(intent.pathId)
                is LanNetworkIntent.ClearSelectedPath -> handleClearSelectedPath()
                is LanNetworkIntent.GenerateManifestDiff -> handleGenerateManifestDiff()
                is LanNetworkIntent.ApplyManifestDiff -> handleApplyManifestDiff()
                is LanNetworkIntent.SetReportFormat -> handleSetReportFormat(intent.format)
                is LanNetworkIntent.ExportReport -> handleExportReport()
                is LanNetworkIntent.SetCiMode -> handleSetCiMode(intent.enabled)
                is LanNetworkIntent.RunCiCheck -> handleRunCiCheck()
                is LanNetworkIntent.DismissError -> handleDismissError()
            }
        }
    }

    // =============================================================
    // Intent Handlers
    // =============================================================

    /**
     * Handle project selection / 处理项目选择
     */
    private suspend fun handleSelectProject(path: String) {
        _state.value = _state.value.copy(
            selectedProjectPath = path,
            scanStatus = ScanStatus.IDLE,
            accessPaths = emptyList(),
            report = null,
            generateStatus = GenerateStatus.IDLE,
            pendingDiff = emptyList()
        )
        _effect.send(LanNetworkEffect.ShowToast("已选择项目: $path"))
    }

    /**
     * Handle scan start / 处理开始扫描
     *
     * Simulation: Generates mock data to demonstrate UI.
     * 真实实现需接入 Gradle 插件或 CLI 工具进行 AST 分析。
     */
    private suspend fun handleStartScan() {
        val projectPath = _state.value.selectedProjectPath
        if (projectPath == null) {
            _effect.send(LanNetworkEffect.ShowError("请先选择项目路径"))
            return
        }

        _state.value = _state.value.copy(
            scanStatus = ScanStatus.SCANNING,
            scanProgress = 0f,
            scannedFilesCount = 0,
            totalFilesCount = 0,
            accessPaths = emptyList(),
            report = null,
            error = null
        )

        // ============================================================
        // Simulation: Mock scan progress
        // 真实场景：Gradle Task `appAnalyzeLocalNetworkPermissions` 执行
        // ============================================================
        val mockFiles = listOf(
            "app/src/main/java/com/mvi/kenny/MyApp.kt",
            "app/src/main/java/com/mvi/kenny/network/ApiClient.kt",
            "app/src/main/java/com/mvi/kenny/network/SocketManager.kt",
            "app/src/main/java/com/mvi/kenny/discovery/DiscoveryService.kt",
            "app/src/main/java/com/mvi/kenny/discovery/DeviceScanner.kt"
        )
        val totalFiles = mockFiles.size

        for ((index, file) in mockFiles.withIndex()) {
            delay(300) // Simulate scan latency / 模拟扫描延迟
            _state.value = _state.value.copy(
                scanProgress = (index + 1).toFloat() / totalFiles,
                scannedFilesCount = index + 1,
                totalFilesCount = totalFiles
            )
        }

        // Generate mock scan results / 生成模拟扫描结果
        val mockPaths = generateMockAccessPaths()
        val report = buildComplianceReport(mockPaths)

        _state.value = _state.value.copy(
            scanStatus = ScanStatus.DONE,
            scanProgress = 1f,
            accessPaths = mockPaths,
            report = report
        )

        _effect.send(LanNetworkEffect.ScanComplete)
    }

    /**
     * Generate mock access paths for demonstration / 生成模拟访问路径数据
     *
     * In production, this data comes from AST analysis via Gradle plugin.
     */
    private fun generateMockAccessPaths(): List<LanAccessPath> {
        return listOf(
            LanAccessPath(
                id = UUID.randomUUID().toString(),
                file = "app/src/main/java/com/mvi/kenny/network/SocketManager.kt",
                line = 42,
                method = "InetAddress.getByName",
                targetIpRange = "192.168.1.0/24",
                accessType = AccessType.INET_ADDRESS,
                hasPermission = false,
                snippet = """
                    |// Connect to local smart hub
                    |val address = InetAddress.getByName("192.168.1.100")
                    |val socket = Socket(address, 8080)
                """.trimMargin(),
                suggestion = "添加 ACCESS_LOCAL_NETWORK 权限声明，并在运行时请求该权限"
            ),
            LanAccessPath(
                id = UUID.randomUUID().toString(),
                file = "app/src/main/java/com/mvi/kenny/network/ApiClient.kt",
                line = 87,
                method = "HttpUrlConnection.connect",
                targetIpRange = "192.168.0.0/16",
                accessType = AccessType.HTTP_URL_CONNECTION,
                hasPermission = true,
                snippet = """
                    |// HTTP request to local server
                    |val url = URL("http://192.168.1.50/api/status")
                    |val conn = url.openConnection() as HttpURLConnection
                """.trimMargin(),
                suggestion = "权限已声明，合规"
            ),
            LanAccessPath(
                id = UUID.randomUUID().toString(),
                file = "app/src/main/java/com/mvi/kenny/discovery/DeviceScanner.kt",
                line = 115,
                method = "OkHttpClient.newCall",
                targetIpRange = "192.168.1.0/24",
                accessType = AccessType.OKHTTP,
                hasPermission = false,
                snippet = """
                    |// Scan local network for devices
                    |val request = Request.Builder()
                    |    .url("http://192.168.1.1:80/discover")
                    |    .build()
                    |client.newCall(request).execute()
                """.trimMargin(),
                suggestion = "OkHttp 访问局域网需要在 AndroidManifest 中声明 ACCESS_LOCAL_NETWORK 权限"
            ),
            LanAccessPath(
                id = UUID.randomUUID().toString(),
                file = "app/src/main/java/com/mvi/kenny/discovery/DiscoveryService.kt",
                line = 63,
                method = "Retrofit.create",
                targetIpRange = "10.0.0.0/8",
                accessType = AccessType.RETROFIT,
                hasPermission = null,
                snippet = """
                    |// Enterprise VPN API
                    |val api = Retrofit.Builder()
                    |    .baseUrl("http://10.0.5.20:8080/")
                    |    .build()
                """.trimMargin(),
                suggestion = "Retrofit baseUrl 包含内网地址，请确认是否访问局域网资源"
            ),
            LanAccessPath(
                id = UUID.randomUUID().toString(),
                file = "app/src/main/java/com/mvi/kenny/network/SocketManager.kt",
                line = 98,
                method = "Socket.connect",
                targetIpRange = "192.168.1.0/24",
                accessType = AccessType.SOCKET,
                hasPermission = false,
                snippet = """
                    |// Direct socket to local media server
                    |val socket = Socket("192.168.1.200", 9000)
                    |val out = socket.getOutputStream()
                """.trimMargin(),
                suggestion = "Socket 直连局域网设备需要 ACCESS_LOCAL_NETWORK 权限"
            )
        )
    }

    /**
     * Build compliance report from scan results / 根据扫描结果构建合规报告
     */
    private fun buildComplianceReport(paths: List<LanAccessPath>): ComplianceReport {
        val compliant = paths.count { it.hasPermission == true }
        val nonCompliant = paths.count { it.hasPermission == false }
        val unknown = paths.count { it.hasPermission == null }

        // 模拟：假设 targetSDK = 37（需要声明权限）
        val targetSdk = 37
        val requiresPermission = targetSdk >= 37

        val missingPermissions = if (nonCompliant > 0 && requiresPermission) {
            listOf("android.permission.ACCESS_LOCAL_NETWORK")
        } else {
            emptyList()
        }

        return ComplianceReport(
            totalPaths = paths.size,
            compliantPaths = compliant,
            nonCompliantPaths = nonCompliant,
            unknownPaths = unknown,
            targetSdk = targetSdk,
            requiresPermission = requiresPermission,
            missingPermissions = missingPermissions
        )
    }

    /**
     * Handle scan cancellation / 处理取消扫描
     */
    private fun handleCancelScan() {
        _state.value = _state.value.copy(
            scanStatus = ScanStatus.IDLE,
            scanProgress = 0f
        )
    }

    /**
     * Handle path selection / 处理选择路径查看详情
     */
    private suspend fun handleSelectPath(pathId: String) {
        _state.value = _state.value.copy(selectedPathId = pathId)
        _effect.send(LanNetworkEffect.NavigateToDetail(pathId))
    }

    /**
     * Handle clear selected path / 处理清除选中路径
     */
    private suspend fun handleClearSelectedPath() {
        _state.value = _state.value.copy(selectedPathId = null)
        _effect.send(LanNetworkEffect.NavigateBack)
    }

    /**
     * Handle manifest diff generation / 处理生成 Manifest Diff
     */
    private suspend fun handleGenerateManifestDiff() {
        val report = _state.value.report
        if (report == null) {
            _effect.send(LanNetworkEffect.ShowError("请先完成扫描"))
            return
        }

        val diffs = mutableListOf<ManifestDiff>()

        if (report.requiresPermission && report.missingPermissions.isNotEmpty()) {
            for (permission in report.missingPermissions) {
                diffs.add(
                    ManifestDiff(
                        permission = permission,
                        action = ManifestDiff.DiffAction.ADD,
                        reason = "Android 17 targetSDK >= 37 访问局域网必须声明"
                    )
                )
            }
        }

        if (diffs.isEmpty()) {
            _effect.send(LanNetworkEffect.ShowToast("无需生成 Diff，所有权限已合规"))
            return
        }

        _state.value = _state.value.copy(
            generateStatus = GenerateStatus.READY,
            pendingDiff = diffs
        )
        _effect.send(LanNetworkEffect.ShowToast("已生成 ${diffs.size} 项变更"))
    }

    /**
     * Handle manifest diff application / 处理应用 Manifest Diff
     */
    private suspend fun handleApplyManifestDiff() {
        val diffs = _state.value.pendingDiff
        if (diffs.isEmpty()) {
            _effect.send(LanNetworkEffect.ShowError("没有待应用的变更"))
            return
        }

        // 模拟：写入 AndroidManifest.xml（真实场景由 Gradle Task 完成）
        delay(500)

        _state.value = _state.value.copy(
            generateStatus = GenerateStatus.APPLIED,
            pendingDiff = emptyList()
        )

        _effect.send(LanNetworkEffect.DiffApplied)
        _effect.send(LanNetworkEffect.ShowToast("Manifest 已更新"))
    }

    /**
     * Handle report format change / 处理报告格式切换
     */
    private fun handleSetReportFormat(format: ReportFormat) {
        _state.value = _state.value.copy(reportFormat = format)
    }

    /**
     * Handle report export / 处理导出报告
     */
    private suspend fun handleExportReport() {
        val report = _state.value.report
        if (report == null) {
            _effect.send(LanNetworkEffect.ShowError("请先完成扫描"))
            return
        }

        _state.value = _state.value.copy(isExporting = true)

        // Simulate export / 模拟导出
        delay(800)

        val outputPath = "/tmp/lannetwork_report.${_state.value.reportFormat.extension}"
        // In real implementation: write report to file

        _state.value = _state.value.copy(isExporting = false)
        _effect.send(LanNetworkEffect.ShareFile(outputPath))
    }

    /**
     * Handle CI mode toggle / 处理 CI 模式切换
     */
    private fun handleSetCiMode(enabled: Boolean) {
        _state.value = _state.value.copy(ciMode = enabled)
    }

    /**
     * Handle CI compliance check / 处理 CI 合规检测
     *
     * CI 模式：无 UI，扫描完成后直接输出报告到 stdout/logcat
     */
    private suspend fun handleRunCiCheck() {
        val projectPath = _state.value.selectedProjectPath
        if (projectPath == null) {
            _effect.send(LanNetworkEffect.ShowError("CI 模式需要指定项目路径"))
            return
        }

        _state.value = _state.value.copy(ciMode = true)
        handleStartScan()
    }

    /**
     * Handle error dismissal / 处理关闭错误
     */
    private fun handleDismissError() {
        _state.value = _state.value.copy(error = null)
    }
}
