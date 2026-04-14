package com.mvi.kenny.feature.emulatortoolkit

import androidx.compose.ui.graphics.Color

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// =============================================================
// EmulatorToolkitViewModel — Android Emulator 36.5 多设备 P2P
// 网络测试框架工具包 ViewModel
// =============================================================
// MVI Architecture: processes Intent → updates State + emits Effect
// 处理 Intent → 更新 State → 发送 Effect

/**
 * Emulator Toolkit ViewModel / 模拟器工具包 ViewModel
 *
 * Manages all business logic for the Emulator Toolkit feature.
 * All ADB/device interactions are simulated (API layer pending real AVD).
 *
 * @see EmulatorToolkitState Single source of truth for UI state
 * @see EmulatorToolkitIntent User intentions processed by this ViewModel
 * @see EmulatorToolkitEffect One-time side effects emitted to UI
 */
class EmulatorToolkitViewModel : ViewModel() {

    // ============================================================
    // State — single source of truth for the UI
    // ============================================================
    private val _state = MutableStateFlow(EmulatorToolkitState.Initial)
    val state: StateFlow<EmulatorToolkitState> = _state.asStateFlow()

    // ============================================================
    // Effect — one-time events emitted to UI
    // ============================================================
    private val _effect = MutableSharedFlow<EmulatorToolkitEffect>()
    val effect: SharedFlow<EmulatorToolkitEffect> = _effect.asSharedFlow()

    // ============================================================
    // Device color palette for log aggregation / 设备颜色调色板
    // ============================================================
    private val deviceColors = listOf(
        Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800),
        Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFFE91E63)
    )

    init {
        // Load initial demo data / 加载初始演示数据
        loadDemoData()
    }

    // ============================================================
    // Intent Processing — process user intentions
    // ============================================================
    /**
     * Process user intent / 处理用户意图
     *
     * Called by UI layer when user triggers an action.
     *
     * @param intent User intent to process / 要处理的用户意图
     */
    fun processIntent(intent: EmulatorToolkitIntent) {
        when (intent) {
            is EmulatorToolkitIntent.LoadAVDSessions -> loadAvdSessions()
            is EmulatorToolkitIntent.SelectTab -> selectTab(intent.tab)
            is EmulatorToolkitIntent.StartTopology -> startTopology(intent.template)
            is EmulatorToolkitIntent.StopTopology -> stopTopology()
            is EmulatorToolkitIntent.ApplyNetworkScenario -> applyNetworkScenario(intent.scenario)
            is EmulatorToolkitIntent.ClearNetworkScenario -> clearNetworkScenario()
            is EmulatorToolkitIntent.StartP2pDiscovery -> startP2pDiscovery()
            is EmulatorToolkitIntent.ConnectP2pDevice -> connectP2pDevice(intent.device)
            is EmulatorToolkitIntent.DisconnectP2p -> disconnectP2p()
            is EmulatorToolkitIntent.StartBleEmulator -> startBleEmulator(intent.emulator)
            is EmulatorToolkitIntent.StopBleEmulator -> stopBleEmulator(intent.emulatorId)
            is EmulatorToolkitIntent.StartBleScan -> startBleScan()
            is EmulatorToolkitIntent.StopBleScan -> stopBleScan()
            is EmulatorToolkitIntent.StartBenchmark -> startBenchmark(intent.config)
            is EmulatorToolkitIntent.StopBenchmark -> stopBenchmark()
            is EmulatorToolkitIntent.FilterLogs -> filterLogs(intent.filter)
            is EmulatorToolkitIntent.ClearLogs -> clearLogs()
            is EmulatorToolkitIntent.SaveLogSnapshot -> saveLogSnapshot()
            is EmulatorToolkitIntent.DismissError -> dismissError()
        }
    }

    // ============================================================
    // Tab Selection / Tab 选择
    // ============================================================
    private fun selectTab(tab: ToolTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    // ============================================================
    // AVD Session Loading / AVD 会话加载
    // ============================================================
    /**
     * Load AVD sessions from ADB / 从 ADB 加载 AVD 会话
     *
     * Note: API layer — requires real AVD connection via ADB.
     * adb devices → adb -s <serial> emu avd name
     */
    private fun loadAvdSessions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Simulated data — API layer pending real AVD integration
                // 模拟数据 — API 层待真实 AVD 接入
                delay(800)
                val demoSessions = listOf(
                    AVDSession(
                        serial = "emulator-5554",
                        name = "Pixel8_API34",
                        apiLevel = 34,
                        ipAddress = "192.168.1.101",
                        p2pState = P2PConnectionState.CONNECTED,
                        isRunning = true
                    ),
                    AVDSession(
                        serial = "emulator-5556",
                        name = "Pixel7_API33",
                        apiLevel = 33,
                        ipAddress = "192.168.1.102",
                        p2pState = P2PConnectionState.CONNECTED,
                        isRunning = true
                    ),
                    AVDSession(
                        serial = "emulator-5558",
                        name = "Tablet_API34",
                        apiLevel = 34,
                        ipAddress = "192.168.1.103",
                        p2pState = P2PConnectionState.CONNECTING,
                        isRunning = true
                    )
                )
                val demoLinks = listOf(
                    NetworkLink("emulator-5554", "emulator-5556", true),
                    NetworkLink("emulator-5554", "emulator-5558", true),
                    NetworkLink("emulator-5556", "emulator-5558", false)
                )
                _state.update {
                    it.copy(
                        avdSessions = demoSessions,
                        networkTopology = NetworkTopology(demoSessions, demoLinks),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Failed to load AVD sessions: ${e.message}") }
            }
        }
    }

    // ============================================================
    // Topology Orchestration / 拓扑编排
    // ============================================================
    /**
     * Start topology orchestration / 开始拓扑编排
     *
     * Note: API layer — uses Gradle plugin + CLI to orchestrate AVDs.
     * Demo simulates the orchestration process.
     */
    private fun startTopology(template: TopologyTemplate) {
        viewModelScope.launch {
            _state.update { it.copy(orchestratorStatus = OrchestratorStatus.RUNNING, topologyTemplate = template) }
            try {
                // Simulate topology startup sequence / 模拟拓扑启动序列
                val sequence = template.name.let {
                    listOf(
                        StartupConfig("emulator-5554", 0, null),
                        StartupConfig("emulator-5556", 2, "emulator-5554"),
                        StartupConfig("emulator-5558", 4, "emulator-5556")
                    )
                }
                _state.update { it.copy(startupSequence = sequence) }

                delay(2000)
                _state.update {
                    it.copy(
                        orchestratorStatus = OrchestratorStatus.COMPLETED,
                        avdSessions = it.avdSessions.map { s -> s.copy(p2pState = P2PConnectionState.CONNECTED) }
                    )
                }
                _effect.emit(EmulatorToolkitEffect.OrchestrationComplete)
            } catch (e: Exception) {
                _state.update { it.copy(orchestratorStatus = OrchestratorStatus.FAILED, error = e.message) }
            }
        }
    }

    private fun stopTopology() {
        _state.update { it.copy(orchestratorStatus = OrchestratorStatus.IDLE) }
    }

    // ============================================================
    // Network Simulation / 网络模拟
    // ============================================================
    /**
     * Apply network scenario / 应用网络场景
     *
     * Note: API layer — uses adb shell tc for network impairment injection.
     * Requires root emulator or API 31+ emulators.
     */
    private fun applyNetworkScenario(scenario: NetworkScenario) {
        viewModelScope.launch {
            _state.update { it.copy(activeScenario = scenario) }
            _effect.emit(EmulatorToolkitEffect.ShowSnackbar("Applied: ${scenario.type.label}"))
        }
    }

    private fun clearNetworkScenario() {
        _state.update { it.copy(activeScenario = null) }
    }

    // ============================================================
    // Wi-Fi P2P SDK / Wi-Fi P2P SDK
    // ============================================================
    /**
     * Start P2P device discovery / 开始 P2P 设备发现
     *
     * Note: API layer — uses WifiP2pManager#discoverPeers.
     * Emulator 36.5 supports P2P via adb emu <serial> peer connect <ip>.
     */
    private fun startP2pDiscovery() {
        viewModelScope.launch {
            _state.update { it.copy(p2pConnectionState = P2PConnectionState.CONNECTING) }
            delay(1500)
            val demoDevices = listOf(
                P2pDevice("Pixel8_API34", "02:1A:2B:3C:4D:5E", true, "Connected"),
                P2pDevice("Pixel7_API33", "02:1A:2B:3C:4D:5F", false, "Available"),
                P2pDevice("Tablet_API34", "02:1A:2B:3C:4D:60", false, "Available")
            )
            _state.update {
                it.copy(
                    p2pDevices = demoDevices,
                    p2pConnectionState = P2PConnectionState.CONNECTED
                )
            }
        }
    }

    private fun connectP2pDevice(device: P2pDevice) {
        viewModelScope.launch {
            _state.update { it.copy(p2pConnectionState = P2PConnectionState.CONNECTING) }
            delay(1200)
            _state.update { it.copy(p2pConnectionState = P2PConnectionState.CONNECTED) }
            _effect.emit(EmulatorToolkitEffect.ShowSnackbar("Connected to ${device.deviceName}"))
        }
    }

    private fun disconnectP2p() {
        _state.update { it.copy(p2pConnectionState = P2PConnectionState.DISCONNECTED, p2pDevices = emptyList()) }
    }

    // ============================================================
    // BLE Emulator Framework / BLE 模拟框架
    // ============================================================
    /**
     * Start BLE emulator / 启动 BLE 模拟器
     *
     * Note: API layer — Emulator 36.5 supports Bluetooth HCI snoop log.
     * Use adb bugreport to capture BLE logs.
     */
    private fun startBleEmulator(emulator: BleEmulator) {
        _state.update {
            val updated = it.bleEmulators.map { e ->
                if (e.id == emulator.id) e.copy(isRunning = true) else e
            }.let { list ->
                if (list.none { e -> e.id == emulator.id }) list + emulator.copy(isRunning = true)
                else list
            }
            it.copy(bleEmulators = updated)
        }
    }

    private fun stopBleEmulator(emulatorId: String) {
        _state.update {
            it.copy(bleEmulators = it.bleEmulators.map { e ->
                if (e.id == emulatorId) e.copy(isRunning = false) else e
            })
        }
    }

    private fun startBleScan() {
        viewModelScope.launch {
            delay(1000)
            val demoResults = listOf(
                BleScanResult("BLE_Device_A", -65, byteArrayOf(0x02, 0x01, 0x1A)),
                BleScanResult("BLE_Device_B", -72, byteArrayOf(0x02, 0x01, 0x1A)),
                BleScanResult("BLE_Device_C", -58, byteArrayOf(0x02, 0x01, 0x1A))
            )
            _state.update { it.copy(bleScanResults = demoResults) }
        }
    }

    private fun stopBleScan() {
        // BLE scan stops automatically after demo / BLE 扫描自动停止
    }

    // ============================================================
    // Cross-Device Debugger / 跨设备调试器
    // ============================================================
    /**
     * Filter aggregated logs / 过滤聚合日志
     *
     * Note: API layer — uses adb -s <serial> logcat from multiple devices.
     * Device serial is used to color-code and filter logs.
     */
    private fun filterLogs(filter: LogFilter) {
        _state.update { it.copy(logFilter = filter) }
    }

    private fun clearLogs() {
        _state.update { it.copy(aggregatedLogs = emptyList()) }
    }

    private fun saveLogSnapshot() {
        viewModelScope.launch {
            // Demo: simulate snapshot save / 演示：模拟快照保存
            _effect.emit(EmulatorToolkitEffect.LogSnapshotSaved("/sdcard/logsnapshots/${UUID.randomUUID()}.txt"))
        }
    }

    // ============================================================
    // Performance Benchmark / 性能基准测试
    // ============================================================
    /**
     * Start performance benchmark / 开始性能基准测试
     *
     * Note: API layer — uses adb shell am perf or Gradle Plugin injection.
     */
    private fun startBenchmark(config: BenchmarkConfig) {
        viewModelScope.launch {
            _state.update { it.copy(isRunningBenchmark = true, benchmarkProgress = 0f) }
            try {
                // Simulate benchmark progress / 模拟基准测试进度
                for (i in 1..10) {
                    delay(300)
                    _state.update { it.copy(benchmarkProgress = i * 0.1f) }
                }
                val result = BenchmarkResult(
                    id = UUID.randomUUID().toString(),
                    scenarioName = config.topologyType.title,
                    deviceCount = config.deviceCount,
                    avgLatencyMs = (20..80).random().toDouble(),
                    throughputMbps = (50..150).random().toDouble(),
                    packetLossPercent = (0..5).random().toDouble(),
                    concurrentRequests = config.concurrentRequests,
                    timestamp = System.currentTimeMillis()
                )
                _state.update {
                    it.copy(
                        benchmarkResults = it.benchmarkResults + result,
                        isRunningBenchmark = false,
                        benchmarkProgress = 0f
                    )
                }
                _effect.emit(EmulatorToolkitEffect.BenchmarkComplete(result.id))
            } catch (e: Exception) {
                _state.update { it.copy(isRunningBenchmark = false, error = e.message) }
            }
        }
    }

    private fun stopBenchmark() {
        _state.update { it.copy(isRunningBenchmark = false, benchmarkProgress = 0f) }
    }

    // ============================================================
    // Error Handling / 错误处理
    // ============================================================
    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    // ============================================================
    // Demo Data Initialization / 演示数据初始化
    // ============================================================
    /**
     * Load demo data for preview / 加载演示数据用于预览
     */
    private fun loadDemoData() {
        val demoScenarios = NetworkScenarioType.entries.map { type ->
            NetworkScenario(
                id = "scenario_${type.name.lowercase()}",
                type = type,
                targetSerials = listOf("emulator-5554", "emulator-5556", "emulator-5558"),
                params = mapOf("delayMs" to 100, "lossPercent" to 5)
            )
        }
        val demoBleEmulators = listOf(
            BleEmulator("ble_scanner_1", "Scanner-1", "Scanner", false),
            BleEmulator("ble_server_1", "GATT-Server-1", "Server", false),
            BleEmulator("ble_client_1", "GATT-Client-1", "Client", false)
        )
        _state.update {
            it.copy(
                networkScenarios = demoScenarios,
                bleEmulators = demoBleEmulators
            )
        }
    }
}
