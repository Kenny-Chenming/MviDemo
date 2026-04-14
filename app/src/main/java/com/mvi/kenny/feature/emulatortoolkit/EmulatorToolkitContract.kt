package com.mvi.kenny.feature.emulatortoolkit

import androidx.compose.ui.graphics.Color


// =============================================================
// EmulatorToolkitContract — Android Emulator 36.5 多设备 P2P
// 网络测试框架工具包 MVI 契约
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class, single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, etc.)

// =============================================================
// P2PConnectionState — P2P 连接状态枚举
// =============================================================
/**
 * P2P connection state / P2P 连接状态
 *
 * @param label Display label / 显示标签
 * @param color State color / 状态颜色
 */
enum class P2PConnectionState(val label: String, val color: Color) {
    CONNECTED("Connected / 已连接", Color(0xFF4CAF50)),
    CONNECTING("Connecting / 连接中", Color(0xFFFFC107)),
    DISCONNECTED("Disconnected / 未连接", Color(0xFF9E9E9E)),
    ERROR("Error / 错误", Color(0xFFF44336))
}

// =============================================================
// OrchestratorStatus — 编排器状态枚举
// =============================================================
/**
 * Topology orchestrator status / 拓扑编排器状态
 *
 * @param label Display label / 显示标签
 */
enum class OrchestratorStatus(val label: String) {
    IDLE("Idle / 空闲"),
    RUNNING("Running / 运行中"),
    COMPLETED("Completed / 已完成"),
    FAILED("Failed / 失败")
}

// =============================================================
// TopologyTemplate — 拓扑模板枚举
// =============================================================
/**
 * AVD topology template / AVD 拓扑模板
 *
 * @param title Display title / 显示标题
 * @param description Template description / 模板描述
 * @param deviceCount Number of AVD devices required / 所需 AVD 设备数量
 */
enum class TopologyTemplate(
    val title: String,
    val description: String,
    val deviceCount: Int
) {
    GAME_TWO_PLAYER("双人游戏 / Two-Player Game", "Two AVDs connected via P2P / 两个 AVD 通过 P2P 连接", 2),
    CROSS_SCREEN("跨屏协同 / Cross-Screen", "Phone + Tablet + TV / 手机 + 平板 + TV", 3),
    IOT_SIMULATION("IoT 模拟 / IoT Simulation", "Hub + 3 sensor nodes / Hub + 3 个传感器节点", 4),
    MULTI_PLAYER("多人游戏 / Multi-Player", "1 server + 4 clients / 1 服务器 + 4 客户端", 5)
}

// =============================================================
// NetworkScenarioType — 网络分区场景类型
// =============================================================
/**
 * Network partition scenario type / 网络分区场景类型
 *
 * @param label Display label / 显示标签
 * @param description Scenario description / 场景描述
 */
enum class NetworkScenarioType(val label: String, val description: String) {
    NORMAL("Normal / 正常", "No network impairment / 无网络损伤"),
    LATENCY("Latency / 延迟", "Add 100-500ms round-trip delay / 添加 100-500ms 往返延迟"),
    PACKET_LOSS("Packet Loss / 丢包", "Random 5-20% packet loss / 随机 5-20% 丢包"),
    INTERMITTENT("Intermittent / 间歇断网", "Periodic disconnection every 30s / 每 30s 周期性断连"),
    PARTITION("Partition / 网络分区", "Split brain between node groups / 节点组之间脑裂")
}

// =============================================================
// AVDSession — AVD 会话数据模型
// =============================================================
/**
 * AVD session / AVD 会话数据模型
 *
 * Represents a running Android Virtual Device (AVD) instance.
 *
 * @param serial AVD serial number (e.g., emulator-5554) / AVD 序列号
 * @param name AVD name / AVD 名称
 * @param apiLevel Android API level / Android API 级别
 * @param ipAddress P2P IP address / P2P IP 地址
 * @param p2pState P2P connection state / P2P 连接状态
 * @param isRunning Whether the AVD is currently running / AVD 是否正在运行
 */
data class AVDSession(
    val serial: String,
    val name: String,
    val apiLevel: Int,
    val ipAddress: String,
    val p2pState: P2PConnectionState = P2PConnectionState.DISCONNECTED,
    val isRunning: Boolean = false
)

// =============================================================
// NetworkLink — 网络链路数据模型
// =============================================================
/**
 * Network link between AVD nodes / AVD 节点之间的网络链路
 *
 * @param fromSerial Source AVD serial / 源 AVD 序列号
 * @param toSerial Target AVD serial / 目标 AVD 序列号
 * @param isActive Whether link is active / 链路是否活跃
 */
data class NetworkLink(
    val fromSerial: String,
    val toSerial: String,
    val isActive: Boolean = false
)

// =============================================================
// NetworkTopology — 网络拓扑数据模型
// =============================================================
/**
 * Network topology / 网络拓扑数据模型
 *
 * Contains all AVD nodes and their P2P connection links.
 *
 * @param nodes List of AVD sessions / AVD 会话列表
 * @param links List of network links / 网络链路列表
 */
data class NetworkTopology(
    val nodes: List<AVDSession> = emptyList(),
    val links: List<NetworkLink> = emptyList()
)

// =============================================================
// StartupConfig — AVD 启动配置
// =============================================================
/**
 * AVD startup configuration / AVD 启动配置
 *
 * @param serial AVD serial / AVD 序列号
 * @param delaySeconds Startup delay in seconds / 启动延迟（秒）
 * @param dependsOn Serial of AVD this depends on / 依赖的 AVD 序列号
 */
data class StartupConfig(
    val serial: String,
    val delaySeconds: Int = 0,
    val dependsOn: String? = null
)

// =============================================================
// NetworkScenario — 网络分区场景配置
// =============================================================
/**
 * Network partition scenario / 网络分区场景配置
 *
 * @param id Scenario unique ID / 场景唯一 ID
 * @param type Scenario type / 场景类型
 * @param targetSerials Target AVD serials / 目标 AVD 序列号列表
 * @param params Scenario parameters (e.g., delay ms, loss %) / 场景参数
 */
data class NetworkScenario(
    val id: String,
    val type: NetworkScenarioType,
    val targetSerials: List<String>,
    val params: Map<String, Any> = emptyMap()
)

// =============================================================
// P2pDevice — P2P 设备数据模型
// =============================================================
/**
 * Wi-Fi P2P device / Wi-Fi P2P 设备数据模型
 *
 * @param deviceName Device display name / 设备显示名称
 * @param deviceAddress MAC address / MAC 地址
 * @param isGroupOwner Whether this device is group owner / 是否为群主
 * @param status Device status / 设备状态
 */
data class P2pDevice(
    val deviceName: String,
    val deviceAddress: String,
    val isGroupOwner: Boolean = false,
    val status: String = "Available"
)

// =============================================================
// BleEmulator — BLE 模拟器数据模型
// =============================================================
/**
 * BLE emulator instance / BLE 模拟器实例
 *
 * @param id Emulator unique ID / 模拟器唯一 ID
 * @param name Emulator display name / 模拟器显示名称
 * @param role BLE role (Scanner/Server/Client) / BLE 角色
 * @param isRunning Whether emulator is running / 模拟器是否运行中
 * @param gattProfile GATT profile UUID / GATT Profile UUID
 */
data class BleEmulator(
    val id: String,
    val name: String,
    val role: String,
    val isRunning: Boolean = false,
    val gattProfile: String = "0000FFE0-0000-1000-8000-00805F9B34FB"
)

// =============================================================
// BleScanResult — BLE 扫描结果数据模型
// =============================================================
/**
 * BLE scan result / BLE 扫描结果
 *
 * @param deviceName Scanned device name / 扫描到的设备名
 * @param rssi Signal strength / 信号强度
 * @param advertisementData Advertisement data bytes / 广播数据
 */
data class BleScanResult(
    val deviceName: String,
    val rssi: Int,
    val advertisementData: ByteArray = byteArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BleScanResult
        return deviceName == other.deviceName && rssi == other.rssi
    }
    override fun hashCode(): Int = deviceName.hashCode() * 31 + rssi
}

// =============================================================
// DeviceLogEntry — 设备日志条目
// =============================================================
/**
 * Device log entry / 设备日志条目
 *
 * Represents a single log line from an AVD device.
 *
 * @param id Unique entry ID / 唯一条目 ID
 * @param timestamp Log timestamp / 日志时间戳
 * @param deviceSerial Source AVD serial / 源 AVD 序列号
 * @param level Log level (V/D/I/W/E) / 日志级别
 * @param tag Log tag / 日志标签
 * @param message Log message / 日志消息
 * @param deviceColor Color for this device in the UI / UI 中该设备的颜色
 */
data class DeviceLogEntry(
    val id: String,
    val timestamp: Long,
    val deviceSerial: String,
    val level: String,
    val tag: String,
    val message: String,
    val deviceColor: Color = Color(0xFF2196F3)
)

// =============================================================
// LogFilter — 日志过滤器配置
// =============================================================
/**
 * Log filter configuration / 日志过滤器配置
 *
 * @param level Minimum log level to show / 显示的最低日志级别
 * @param keyword Keyword filter (empty = show all) / 关键词过滤
 * @param deviceSerials Filter by device serials (empty = all) / 按设备序列号过滤
 */
data class LogFilter(
    val level: String = "V",
    val keyword: String = "",
    val deviceSerials: List<String> = emptyList()
)

// =============================================================
// BenchmarkResult — 性能基准测试结果
// =============================================================
/**
 * Performance benchmark result / 性能基准测试结果
 *
 * @param id Result unique ID / 结果唯一 ID
 * @param scenarioName Scenario name / 场景名称
 * @param deviceCount Number of devices used / 使用的设备数量
 * @param avgLatencyMs Average latency in milliseconds / 平均延迟（毫秒）
 * @param throughputMbps Throughput in Mbps / 吞吐量（Mbps）
 * @param packetLossPercent Packet loss percentage / 丢包率（%）
 * @param timestamp Test timestamp / 测试时间戳
 */
data class BenchmarkResult(
    val id: String,
    val scenarioName: String,
    val deviceCount: Int,
    val avgLatencyMs: Double,
    val throughputMbps: Double,
    val packetLossPercent: Double,
    val concurrentRequests: Int = 0,
    val timestamp: Long
)

// =============================================================
// BenchmarkConfig — 基准测试配置
// =============================================================
/**
 * Benchmark configuration / 基准测试配置
 *
 * @param deviceCount Number of AVD devices / AVD 设备数量
 * @param topologyType Network topology type / 网络拓扑类型
 * @param concurrentRequests Number of concurrent requests / 并发请求数
 * @param durationSeconds Test duration in seconds / 测试持续时间（秒）
 */
data class BenchmarkConfig(
    val deviceCount: Int = 2,
    val topologyType: TopologyTemplate = TopologyTemplate.GAME_TWO_PLAYER,
    val concurrentRequests: Int = 10,
    val durationSeconds: Int = 60
)

// =============================================================
// ToolTab — 工具 Tab 枚举
// =============================================================
/**
 * Emulator toolkit feature tab / 模拟器工具包功能 Tab
 *
 * @param title Display title / 显示标题
 * @param iconName Material icon name / Material 图标名称
 */
enum class ToolTab(val title: String, val iconName: String) {
    TOPOLOGY_ORCHESTRATION("拓扑编排 / Topology", "router"),
    NETWORK_SIMULATION("网络模拟 / Network", "network_check"),
    WIFI_P2P_SDK("Wi-Fi P2P SDK", "wifi"),
    BLE_EMULATOR("BLE 模拟 / BLE", "bluetooth"),
    CROSS_DEVICE_DEBUGGER("跨设备调试 / Debugger", "bug_report"),
    PERFORMANCE_BENCHMARK("性能基准 / Benchmark", "speed")
}

// =============================================================
// EmulatorToolkitState — 页面状态
// =============================================================
/**
 * Emulator Toolkit State / 模拟器工具包页面状态
 *
 * Single source of truth for the entire Emulator Toolkit UI.
 *
 * @param avdSessions List of running AVD sessions / 运行中的 AVD 会话列表
 * @param networkTopology Current network topology / 当前网络拓扑
 * @param activeTab Currently active tool tab / 当前活跃的工具 Tab
 * @param topologyTemplate Selected topology template / 选中的拓扑模板
 * @param startupSequence AVD startup sequence config / AVD 启动序列配置
 * @param orchestratorStatus Orchestrator status / 编排器状态
 * @param networkScenarios Available network scenarios / 可用网络场景列表
 * @param activeScenario Currently active network scenario / 当前活跃网络场景
 * @param p2pDevices Discovered P2P devices / 发现的 P2P 设备列表
 * @param p2pConnectionState Current P2P connection state / 当前 P2P 连接状态
 * @param bleEmulators BLE emulator instances / BLE 模拟器实例列表
 * @param bleScanResults BLE scan results / BLE 扫描结果列表
 * @param aggregatedLogs Aggregated device logs / 聚合设备日志
 * @param logFilter Current log filter / 当前日志过滤器
 * @param benchmarkResults Historical benchmark results / 历史基准测试结果
 * @param isRunningBenchmark Whether benchmark is currently running / 是否正在运行基准测试
 * @param benchmarkProgress Benchmark progress 0.0~1.0 / 基准测试进度
 * @param isLoading Whether any background operation is in progress / 是否有后台操作进行中
 * @param error Error message if any / 错误信息
 */
data class EmulatorToolkitState(
    val avdSessions: List<AVDSession> = emptyList(),
    val networkTopology: NetworkTopology = NetworkTopology(),
    val activeTab: ToolTab = ToolTab.TOPOLOGY_ORCHESTRATION,
    // Topology Orchestration
    val topologyTemplate: TopologyTemplate = TopologyTemplate.GAME_TWO_PLAYER,
    val startupSequence: List<StartupConfig> = emptyList(),
    val orchestratorStatus: OrchestratorStatus = OrchestratorStatus.IDLE,
    // Network Simulation
    val networkScenarios: List<NetworkScenario> = emptyList(),
    val activeScenario: NetworkScenario? = null,
    // Wi-Fi P2P
    val p2pDevices: List<P2pDevice> = emptyList(),
    val p2pConnectionState: P2PConnectionState = P2PConnectionState.DISCONNECTED,
    // BLE
    val bleEmulators: List<BleEmulator> = emptyList(),
    val bleScanResults: List<BleScanResult> = emptyList(),
    // Cross-Device Debug
    val aggregatedLogs: List<DeviceLogEntry> = emptyList(),
    val logFilter: LogFilter = LogFilter(),
    // Performance Benchmark
    val benchmarkResults: List<BenchmarkResult> = emptyList(),
    val isRunningBenchmark: Boolean = false,
    val benchmarkProgress: Float = 0f,
    // UI
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = EmulatorToolkitState()
    }

    /** Connected AVD count / 已连接的 AVD 数量 */
    val connectedAvdCount: Int
        get() = avdSessions.count { it.p2pState == P2PConnectionState.CONNECTED }

    /** Active P2P link count / 活跃 P2P 链路数量 */
    val activeLinkCount: Int
        get() = networkTopology.links.count { it.isActive }

    /** Filtered logs based on current filter / 根据当前过滤器过滤后的日志 */
    val filteredLogs: List<DeviceLogEntry>
        get() {
            var logs = aggregatedLogs
            if (logFilter.keyword.isNotBlank()) {
                logs = logs.filter {
                    it.message.contains(logFilter.keyword, ignoreCase = true) ||
                    it.tag.contains(logFilter.keyword, ignoreCase = true)
                }
            }
            if (logFilter.deviceSerials.isNotEmpty()) {
                logs = logs.filter { it.deviceSerial in logFilter.deviceSerials }
            }
            return logs
        }
}

// =============================================================
// EmulatorToolkitIntent — 用户意图
// =============================================================
/**
 * Emulator Toolkit User Intents / 模拟器工具包用户意图
 *
 * Every user action corresponds to an Intent.
 */
sealed interface EmulatorToolkitIntent {
    /** Load AVD sessions from ADB / 从 ADB 加载 AVD 会话
     * Note: API layer — real AVD integration pending / API 层待真实 AVD 接入
     */
    data object LoadAVDSessions : EmulatorToolkitIntent

    /** Select a tool tab / 选择工具 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: ToolTab) : EmulatorToolkitIntent

    /** Start topology orchestration / 开始拓扑编排
     * @param template Topology template to use / 要使用的拓扑模板
     */
    data class StartTopology(val template: TopologyTemplate) : EmulatorToolkitIntent

    /** Stop topology orchestration / 停止拓扑编排 */
    data object StopTopology : EmulatorToolkitIntent

    /** Apply a network scenario / 应用网络场景
     * @param scenario Network scenario to apply / 要应用的网络场景
     */
    data class ApplyNetworkScenario(val scenario: NetworkScenario) : EmulatorToolkitIntent

    /** Clear active network scenario / 清除活跃网络场景 */
    data object ClearNetworkScenario : EmulatorToolkitIntent

    /** Start P2P device discovery / 开始 P2P 设备发现 */
    data object StartP2pDiscovery : EmulatorToolkitIntent

    /** Connect to a P2P device / 连接到 P2P 设备
     * @param device P2P device to connect to / 要连接的 P2P 设备
     */
    data class ConnectP2pDevice(val device: P2pDevice) : EmulatorToolkitIntent

    /** Disconnect P2P connection / 断开 P2P 连接 */
    data object DisconnectP2p : EmulatorToolkitIntent

    /** Start BLE emulator / 启动 BLE 模拟器
     * @param emulator Emulator config / 模拟器配置
     */
    data class StartBleEmulator(val emulator: BleEmulator) : EmulatorToolkitIntent

    /** Stop BLE emulator / 停止 BLE 模拟器
     * @param emulatorId Emulator ID to stop / 要停止的模拟器 ID
     */
    data class StopBleEmulator(val emulatorId: String) : EmulatorToolkitIntent

    /** Start BLE scan / 开始 BLE 扫描 */
    data object StartBleScan : EmulatorToolkitIntent

    /** Stop BLE scan / 停止 BLE 扫描 */
    data object StopBleScan : EmulatorToolkitIntent

    /** Start performance benchmark / 开始性能基准测试
     * @param config Benchmark configuration / 基准测试配置
     */
    data class StartBenchmark(val config: BenchmarkConfig) : EmulatorToolkitIntent

    /** Stop performance benchmark / 停止性能基准测试 */
    data object StopBenchmark : EmulatorToolkitIntent

    /** Filter aggregated logs / 过滤聚合日志
     * @param filter Log filter to apply / 要应用的日志过滤器
     */
    data class FilterLogs(val filter: LogFilter) : EmulatorToolkitIntent

    /** Clear all logs / 清除所有日志 */
    data object ClearLogs : EmulatorToolkitIntent

    /** Save device log snapshot / 保存设备日志快照 */
    data object SaveLogSnapshot : EmulatorToolkitIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : EmulatorToolkitIntent
}

// =============================================================
// EmulatorToolkitEffect — 副作用
// =============================================================
/**
 * Emulator Toolkit Side Effects / 模拟器工具包副作用
 *
 * One-time events consumed only once by the UI layer.
 */
sealed interface EmulatorToolkitEffect {
    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error / 是否为错误
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : EmulatorToolkitEffect

    /** Benchmark complete event / 基准测试完成事件
     * @param resultId Result ID / 结果 ID
     */
    data class BenchmarkComplete(val resultId: String) : EmulatorToolkitEffect

    /** Log snapshot saved event / 日志快照保存事件
     * @param filePath Snapshot file path / 快照文件路径
     */
    data class LogSnapshotSaved(val filePath: String) : EmulatorToolkitEffect

    /** Orchestration complete event / 编排完成事件 */
    data object OrchestrationComplete : EmulatorToolkitEffect
}
