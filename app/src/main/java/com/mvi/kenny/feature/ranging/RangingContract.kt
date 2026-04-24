package com.mvi.kenny.feature.ranging

import androidx.compose.ui.graphics.Color

// =============================================================
// RangingContract — Android 17 UWB Ranging API 开发工具包
// MVI 架构状态、意图与副作用定义
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class, single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, etc.)
//
// @see RangingViewModel State management
// @see DeviceDiscoveryScreen Main screen

// =============================================================
// RangingTab — 功能 Tab 枚举（底部导航栏）
// =============================================================
/**
 * Ranging tool module tab / 功能模块 Tab（底部导航栏）
 *
 * @param title Display title / 显示标题
 * @param route Nav route string / 导航路由字符串
 */
enum class RangingTab(val title: String, val route: String) {
    DEVICE_DISCOVERY("设备发现", "ranging_device_discovery"),
    SINGLE_RANGING("单设备测距", "ranging_single"),
    MULTI_RANGING("多设备测距", "ranging_multi"),
    TECH_COMPARE("技术对比", "ranging_compare"),
    POWER_ANALYZER("功耗分析", "ranging_power")
}

// =============================================================
// RangingMode — 测距模式枚举
// =============================================================
/**
 * UWB Ranging mode / UWB 测距模式
 *
 * DL_TDOA: Downlink Time Difference of Arrival — 适合基础设施场景
 * PRIVACY: 隐私模式 — 适合社交 App（不暴露精确位置）
 */
enum class RangingMode(val label: String, val description: String) {
    DL_TDOA("DL-TDoA", "Downlink TDoA — 基础设施场景 / Infrastructure mode"),
    PRIVACY("Privacy", "隐私模式 — 社交 App / Social app mode")
}

// =============================================================
// RangingStatus — 测距状态枚举
// =============================================================
/**
 * Ranging session status / 测距会话状态
 *
 * @param label Display label / 显示标签
 * @param color Status indicator color / 状态指示器颜色
 */
enum class RangingStatus(val label: String, val color: Color) {
    IDLE("空闲 / Idle", Color(0xFF757575)),
    DISCOVERING("发现中 / Discovering", Color(0xFFFFD54F)),
    RANGING("测距中 / Ranging", Color(0xFF69F0AE)),
    ERROR("错误 / Error", Color(0xFFFF6B6B))
}

// =============================================================
// UwbDevice — UWB 设备数据模型
// =============================================================
/**
 * UWB Device / UWB 设备
 *
 * Represents a discovered UWB device in the vicinity.
 *
 * @param address Device unique address (MAC-like) / 设备唯一地址
 * @param displayName Optional human-readable name / 可读名称
 * @param rssi Signal strength (dBm) / 信号强度
 * @param firstSeenTimestamp First discovery timestamp (ms) / 首次发现时间戳
 * @param isConnected Whether actively ranging / 是否正在测距
 * @param distanceMeters Current distance (if ranging) / 当前距离（如正在测距）
 * @param azimuth Azimuth angle (if available) / 方位角（如可用）
 * @param elevation Elevation angle (if available) / 仰角（如可用）
 */
data class UwbDevice(
    val address: String,
    val displayName: String? = null,
    val rssi: Int,
    val firstSeenTimestamp: Long,
    val isConnected: Boolean = false,
    val distanceMeters: Float? = null,
    val azimuth: Float? = null,
    val elevation: Float? = null
) {
    /** Display name or truncated address / 显示名称或截断地址 */
    val displayInfo: String
        get() = displayName ?: address.takeLast(8)

    /** Signal strength level / 信号强度等级 */
    val signalLevel: SignalLevel
        get() = when {
            rssi >= -60 -> SignalLevel.EXCELLENT
            rssi >= -75 -> SignalLevel.GOOD
            rssi >= -90 -> SignalLevel.FAIR
            else -> SignalLevel.POOR
        }
}

/**
 * Signal strength level / 信号强度等级
 */
enum class SignalLevel(val label: String, val color: Color) {
    EXCELLENT("极强 / Excellent", Color(0xFF69F0AE)),
    GOOD("良好 / Good", Color(0xFF4FC3F7)),
    FAIR("一般 / Fair", Color(0xFFFFD54F)),
    POOR("较弱 / Poor", Color(0xFFFF6B6B))
}

// =============================================================
// RangingConfig — 测距配置
// =============================================================
/**
 * Ranging configuration / 测距配置
 *
 * @param rangingMode Ranging mode (DL_TDOA / PRIVACY) / 测距模式
 * @param updateIntervalMs Update interval in milliseconds / 更新间隔（毫秒）
 * @param timeoutMs Ranging timeout in milliseconds / 测距超时（毫秒）
 * @param enableSensorFusion Whether to enable motion sensor fusion / 是否启用运动传感器融合
 */
data class RangingConfig(
    val rangingMode: RangingMode = RangingMode.DL_TDOA,
    val updateIntervalMs: Long = 1000L,
    val timeoutMs: Long = 30000L,
    val enableSensorFusion: Boolean = false
) {
    companion object {
        /** Default configuration / 默认配置 */
        val Default = RangingConfig()
    }
}

// =============================================================
// RangingResult — 单次测距结果
// =============================================================
/**
 * Ranging result / 测距结果
 *
 * @param deviceAddress Target device address / 目标设备地址
 * @param distanceMeters Distance in meters / 距离（米）
 * @param azimuth Azimuth angle (degrees, if available) / 方位角（如可用）
 * @param elevation Elevation angle (degrees, if available) / 仰角（如可用）
 * @param rssi Signal strength (dBm) / 信号强度
 * @param timestamp Measurement timestamp (ms) / 测量时间戳
 */
data class RangingResult(
    val deviceAddress: String,
    val distanceMeters: Float,
    val azimuth: Float? = null,
    val elevation: Float? = null,
    val rssi: Int,
    val timestamp: Long
)

// =============================================================
// TechComparison — 技术对比数据
// =============================================================
/**
 * Technology comparison item / 技术对比项
 *
 * @param name Technology name / 技术名称
 * @param accuracyRange Accuracy range description / 精度范围描述
 * @param typicalRange Typical operational range / 典型工作范围
 * @param powerConsumption Power consumption level / 功耗等级
 * @param cost Cost level / 成本等级
 * @param compatibility Compatibility description / 兼容性描述
 * @param bestUseCases Best-fit use cases / 最佳适用场景
 * @param pros Advantages / 优势
 * @param cons Disadvantages / 劣势
 */
data class TechComparison(
    val name: String,
    val accuracyRange: String,
    val typicalRange: String,
    val powerConsumption: PowerLevel,
    val cost: CostLevel,
    val compatibility: String,
    val bestUseCases: List<String>,
    val pros: List<String>,
    val cons: List<String>
)

enum class PowerLevel(val label: String, val bars: Int) {
    LOW("低 / Low", 1),
    MEDIUM("中 / Medium", 2),
    HIGH("高 / High", 3),
    VERY_HIGH("极高 / Very High", 4)
}

enum class CostLevel(val label: String) {
    LOW("低成本"),
    MEDIUM("中等成本"),
    HIGH("高成本")
}

// =============================================================
// PowerEstimate — 功耗估算结果
// =============================================================
/**
 * Power consumption estimate / 功耗估算结果
 *
 * @param technology Technology name / 技术名称
 * @param hourlyMah milliamp-hour per hour / 每小时毫安时
 * @param dailyMah Daily consumption (8h active) / 每日消耗（按8小时活跃计算）
 * @param batteryImpactDays Estimated days of battery life reduction / 电池续航影响天数
 * @param description Description / 描述
 */
data class PowerEstimate(
    val technology: String,
    val hourlyMah: Float,
    val dailyMah: Float,
    val batteryImpactDays: Float,
    val description: String
)

// =============================================================
// RangingDashboardState — 页面状态（MVI Model）
// =============================================================
/**
 * Ranging Dashboard State / 测距工具页面状态
 *
 * Single source of truth for the entire UWB Ranging Tool UI.
 * All UI state is derived from this data class.
 *
 * @param activeTab Currently active tab / 当前活跃 Tab
 * @param devices Discovered UWB devices / 发现的 UWB 设备列表
 * @param activeRangings Map of device address to ranging status / 活跃测距映射
 * @param rangingHistory Map of device address to ranging result history / 测距历史
 * @param selectedDeviceAddress Selected device for detail view / 选中的设备地址
 * @param isUwbAvailable Whether UWB hardware is available / UWB 硬件是否可用
 * @param isDiscovering Whether device discovery is in progress / 是否正在发现设备
 * @param config Ranging configuration / 测距配置
 * @param error Error message if any / 错误信息
 * @param techComparisons Technology comparison data / 技术对比数据
 * @param powerEstimates Power consumption estimates / 功耗估算数据
 */
data class RangingDashboardState(
    val activeTab: RangingTab = RangingTab.DEVICE_DISCOVERY,
    val devices: List<UwbDevice> = emptyList(),
    val activeRangings: Map<String, RangingStatus> = emptyMap(),
    val rangingHistory: Map<String, List<RangingResult>> = emptyMap(),
    val selectedDeviceAddress: String? = null,
    val isUwbAvailable: Boolean = false,
    val isDiscovering: Boolean = false,
    val config: RangingConfig = RangingConfig.Default,
    val error: String? = null,
    val techComparisons: List<TechComparison> = emptyList(),
    val powerEstimates: List<PowerEstimate> = emptyList()
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = RangingDashboardState()
    }

    /** Selected device object / 选中的设备对象 */
    val selectedDevice: UwbDevice?
        get() = devices.find { it.address == selectedDeviceAddress }

    /** Devices being actively ranged / 正在测距的设备 */
    val rangingDevices: List<UwbDevice>
        get() = devices.filter { activeRangings[it.address] == RangingStatus.RANGING }

    /** Discovery state for a specific device / 特定设备的发现状态 */
    fun deviceStatus(address: String): RangingStatus =
        activeRangings[address] ?: RangingStatus.IDLE

    /** History for selected device / 选中设备的测距历史 */
    val selectedDeviceHistory: List<RangingResult>
        get() = selectedDeviceAddress?.let { rangingHistory[it] } ?: emptyList()

    /** Check if a device is being ranged / 检查设备是否正在测距 */
    fun isRanging(address: String): Boolean =
        activeRangings[address] == RangingStatus.RANGING
}

// =============================================================
// RangingIntent — 用户意图（MVI Intent）
// =============================================================
/**
 * Ranging Tool User Intents / 测距工具用户意图
 *
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface RangingIntent {
    /** Select a tab / 选择 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: RangingTab) : RangingIntent

    /** Start device discovery / 开始设备发现
     *
     * Initiates UWB device discovery and starts monitoring for new devices.
     */
    data object StartDiscovery : RangingIntent

    /** Stop device discovery / 停止设备发现 */
    data object StopDiscovery : RangingIntent

    /** Start ranging for a specific device / 对指定设备开始测距
     * @param deviceAddress Device address to range / 要测距的设备地址
     */
    data class StartRanging(val deviceAddress: String) : RangingIntent

    /** Stop ranging for a specific device / 停止对指定设备测距
     * @param deviceAddress Device address to stop ranging / 要停止测距的设备地址
     */
    data class StopRanging(val deviceAddress: String) : RangingIntent

    /** Stop all active ranging sessions / 停止所有测距 */
    data object StopAllRanging : RangingIntent

    /** Select a device for detail view / 选择设备查看详情
     * @param deviceAddress Device address to select / 要选择的设备地址
     */
    data class SelectDevice(val deviceAddress: String) : RangingIntent

    /** Clear device selection / 清除设备选择 */
    data object ClearSelection : RangingIntent

    /** Update ranging configuration / 更新测距配置
     * @param config New ranging configuration / 新的测距配置
     */
    data class UpdateConfig(val config: RangingConfig) : RangingIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : RangingIntent

    /** Check UWB hardware availability / 检测 UWB 硬件可用性 */
    data object CheckUwbAvailability : RangingIntent
}

// =============================================================
// RangingEffect — 副作用（MVI Effect）
// =============================================================
/**
 * Ranging Tool Side Effects / 测距工具副作用
 *
 * One-time events, consumed only once by UI layer.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface RangingEffect {
    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error message / 是否为错误消息
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : RangingEffect

    /** Navigate to ranging detail / 导航到测距详情
     * @param deviceAddress Device address to show detail for / 要显示详情的设备地址
     */
    data class NavigateToDetail(val deviceAddress: String) : RangingEffect

    /** Ranging started event / 测距开始事件
     * @param deviceAddress Device address that started ranging / 开始测距的设备地址
     */
    data class RangingStarted(val deviceAddress: String) : RangingEffect

    /** Ranging stopped event / 测距停止事件
     * @param deviceAddress Device address that stopped ranging / 停止测距的设备地址
     */
    data class RangingStopped(val deviceAddress: String) : RangingEffect

    /** UWB not available warning / UWB 不可用警告 */
    data object UwbNotAvailable : RangingEffect

    /** Copy device address to clipboard / 复制设备地址到剪贴板
     * @param address Device address to copy / 要复制的设备地址
     */
    data class CopyAddress(val address: String) : RangingEffect
}
