package com.mvi.kenny.feature.cardatal

/**
 * ============================================================
 * CarDataContract — Android Auto Car App Library API Level 3 MVI 契约
 * ============================================================
 * MVI architecture contract for CarData (Vehicle Data) feature.
 * PRD-080: Android Auto Car App Library API Level 3 车辆数据集成工具包
 *
 * MVI 三要素 / Three pillars:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable function, consumes State, renders UI
 * - Intent: User intentions, ViewModel processes and updates State
 * Effect: One-time side effects (navigation, toast), delivered via Channel
 * —————————————————————————————————————————————————————
 *
 * Design Reference: memory/agency/designs/PRD-080-Android-Auto-Car-App-Library-API-Level-3-车辆数据集成工具包.md
 */

/**
 * ============================================================
 * FuelType — 燃油/能源类型枚举
 * ============================================================
 *
 * @param displayName 显示名称
 */
enum class FuelType(val displayName: String) {
    GASOLINE("燃油"),
    ELECTRIC("电动"),
    HYBRID("混动")
}

/**
 * ============================================================
 * CarConnectionStatus — 车辆连接状态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 */
enum class CarConnectionStatus(val displayName: String) {
    CONNECTED("已连接"),
    DISCONNECTED("未连接"),
    API_NOT_SUPPORTED("API 不支持"),
    PERMISSION_DENIED("权限被拒绝")
}

/**
 * ============================================================
 * DrivingScenario — 驾驶场景枚举（模拟器）
 * ============================================================
 *
 * @param displayName 显示名称
 * @param description 场景描述
 */
enum class DrivingScenario(val displayName: String, val description: String) {
    CITY_DRIVING("城市驾驶", "低速，频繁启停"),
    HIGHWAY_CRUISE("高速巡航", "稳定中速"),
    HEAVY_TRAFFIC("拥堵路况", "怠速，低速"),
    HARD_ACCELERATION("急加速", "高速，油耗高峰"),
    COLD_START("冷启动", "发动机升温过程"),
    ELECTRIC_ECONOMY("经济驾驶", "电动车最优能耗")
}

/**
 * ============================================================
 * FuelStation — 加油站/充电站数据类
 * ============================================================
 *
 * @param id Station unique ID
 * @param name Station name
 * @param distanceKm Distance from current location in km
 * @param fuelType Supported fuel/energy type
 * @param price Price per unit (nullable, no real API)
 */
data class FuelStation(
    val id: String,
    val name: String,
    val distanceKm: Float,
    val fuelType: FuelType,
    val price: Float? = null
)

/**
 * ============================================================
 * VehicleSpeed — 车速数据类
 * ============================================================
 *
 * @param speedMps Speed in meters per second
 * @param speedMph Speed in miles per hour
 * @param timestamp Timestamp in milliseconds
 */
data class VehicleSpeed(
    val speedMps: Float,
    val speedMph: Float,
    val timestamp: Long
)

/**
 * ============================================================
 * FuelLevel — 油量/电量数据类
 * ============================================================
 *
 * @param fuelPercent Fuel level percentage (0-100)
 * @param rangeKm Estimated remaining range in km
 * @param fuelType Fuel type
 */
data class FuelLevel(
    val fuelPercent: Float,
    val rangeKm: Float,
    val fuelType: FuelType
)

/**
 * ============================================================
 * EngineTemperature — 发动机温度数据类
 * ============================================================
 *
 * @param temperatureCelsius Temperature in Celsius
 * @param isOverheating Whether engine is overheating
 */
data class EngineTemperature(
    val temperatureCelsius: Float,
    val isOverheating: Boolean
)

/**
 * ============================================================
 * HvacState — 空调/暖通状态数据类
 * ============================================================
 *
 * @param cabinTemperatureCelsius Cabin temperature in Celsius
 * @param fanSpeed Fan speed level (0-7)
 * @param acEnabled Whether AC is enabled
 * @param defrostEnabled Whether defrost is enabled
 */
data class HvacState(
    val cabinTemperatureCelsius: Float,
    val fanSpeed: Int,
    val acEnabled: Boolean,
    val defrostEnabled: Boolean
)

/**
 * ============================================================
 * CarDataState — 车辆数据监控页面状态
 * ============================================================
 *
 * @param connectionStatus Current car connection status
 * @param isApiLevel3Supported Whether vehicle supports Car API Level 3
 * @param currentSpeed Current vehicle speed (null if disconnected)
 * @param fuelLevel Current fuel/electricity level (null if disconnected)
 * @param engineTemperature Current engine temperature (null if disconnected)
 * @param hvacState Current HVAC state (null if disconnected)
 * @param estimatedRangeKm Estimated driving range in km (null if unavailable)
 * @param nearbyFuelStations List of nearby fuel/charging stations
 * @param degradationMessage Degradation message when API not supported
 * @param isSimulatorActive Whether simulator is currently active
 * @param currentScenario Current driving scenario (if simulator active)
 * @param isLoadingNearbyStations Whether nearby stations are being loaded
 */
data class CarDataState(
    val connectionStatus: CarConnectionStatus = CarConnectionStatus.DISCONNECTED,
    val isApiLevel3Supported: Boolean = false,
    val currentSpeed: VehicleSpeed? = null,
    val fuelLevel: FuelLevel? = null,
    val engineTemperature: EngineTemperature? = null,
    val hvacState: HvacState? = null,
    val estimatedRangeKm: Float? = null,
    val nearbyFuelStations: List<FuelStation> = emptyList(),
    val degradationMessage: String? = null,
    val isSimulatorActive: Boolean = false,
    val currentScenario: DrivingScenario? = null,
    val isLoadingNearbyStations: Boolean = false
) {
    companion object {
        /** Initial/empty state */
        val Initial = CarDataState()
    }
}

/**
 * ============================================================
 * CarDataIntent — 用户意图（User Intent）
 * —————————————————————————————————————————————————————
 * Every user action on the page corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see CarDataViewModel.sendIntent handles all Intents
 */
sealed interface CarDataIntent {

    /** 用户点击"连接车辆"按钮 */
    data object Connect : CarDataIntent

    /** 用户点击"断开连接"按钮 */
    data object Disconnect : CarDataIntent

    /** 用户选择并启动模拟器场景
     * @param scenario Selected driving scenario
     */
    data class StartSimulator(val scenario: DrivingScenario) : CarDataIntent

    /** 用户停止模拟器 */
    data object StopSimulator : CarDataIntent

    /** 用户请求搜索附近加油站/充电站
     * @param radiusKm Search radius in kilometers
     */
    data class RequestNearbyStations(val radiusKm: Float) : CarDataIntent

    /** 用户点击"申请权限"按钮 */
    data object RequestCarPermission : CarDataIntent

    /** 用户点击附近加油站卡片
     * @param stationId Selected station ID
     */
    data class SelectFuelStation(val stationId: String) : CarDataIntent
}

/**
 * ============================================================
 * CarDataEffect — 一次性副作用（Effect）
 * —————————————————————————————————————————————————————
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see CarDataViewModel _effect.send() sends Effects
 */
sealed interface CarDataEffect {

    /** 显示错误消息
     * @param message Error message
     */
    data class ShowError(val message: String) : CarDataEffect

    /** 导航到加油站详情
     * @param stationId Station ID to navigate to
     */
    data class NavigateToFuelStation(val stationId: String) : CarDataEffect

    /** 请求车辆权限（跳转系统权限页面） */
    data object RequestCarPermission : CarDataEffect

    /** 连接成功提示 */
    data object ConnectionSuccess : CarDataEffect

    /** 断开连接提示 */
    data object DisconnectionSuccess : CarDataEffect
}
