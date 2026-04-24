package com.mvi.kenny.feature.ranging

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =============================================================
// RangingViewModel — Android 17 UWB Ranging API 开发工具包
// ViewModel 实现（MVI 架构）
// =============================================================
/**
 * Ranging ViewModel / 测距 ViewModel
 *
 * Manages all state transitions and business logic for the UWB Ranging Tool.
 * Follows MVI (Model-View-Intent) architecture pattern.
 *
 * Responsibilities:
 * - Device discovery lifecycle management
 * - Ranging session lifecycle management
 * - Configuration management
 * - Error handling and user feedback
 *
 * @param context Application context for UWB API access / 应用上下文
 *
 * @see RangingDashboardState UI state model
 * @see RangingIntent User intentions
 * @see RangingEffect Side effects
 */
class RangingViewModel(
    private val context: Context
) : ViewModel() {

    // ============================================================
    // State — UI 状态（single source of truth）
    // ============================================================
    private val _state = MutableStateFlow(RangingDashboardState.Initial)
    val state: StateFlow<RangingDashboardState> = _state.asStateFlow()

    // ============================================================
    // Effects — 副作用 Channel（one-time events）
    // ============================================================
    private val _effect = MutableSharedFlow<RangingEffect>()
    val effect: MutableSharedFlow<RangingEffect> = _effect

    // ============================================================
    // Internal State — 内部状态
    // ============================================================
    private var discoveryJob: Job? = null
    private val rangingJobs = mutableMapOf<String, Job>()
    private var resultSimulationJob: Job? = null

    // ============================================================
    // Initialization — 初始化
    // ============================================================
    init {
        // Check UWB availability on init / 初始化时检测 UWB 可用性
        sendIntent(RangingIntent.CheckUwbAvailability)
        // Load tech comparisons / 加载技术对比数据
        loadTechComparisons()
        // Load power estimates / 加载功耗估算数据
        loadPowerEstimates()
    }

    // ============================================================
    // sendIntent — 接收用户意图，处理业务逻辑
    // ============================================================
    /**
     * Send user intent to be processed / 发送用户意图进行处理
     *
     * @param intent User intent / 用户意图
     */
    fun sendIntent(intent: RangingIntent) {
        when (intent) {
            is RangingIntent.SelectTab -> handleSelectTab(intent.tab)
            is RangingIntent.StartDiscovery -> handleStartDiscovery()
            is RangingIntent.StopDiscovery -> handleStopDiscovery()
            is RangingIntent.StartRanging -> handleStartRanging(intent.deviceAddress)
            is RangingIntent.StopRanging -> handleStopRanging(intent.deviceAddress)
            is RangingIntent.StopAllRanging -> handleStopAllRanging()
            is RangingIntent.SelectDevice -> handleSelectDevice(intent.deviceAddress)
            is RangingIntent.ClearSelection -> handleClearSelection()
            is RangingIntent.UpdateConfig -> handleUpdateConfig(intent.config)
            is RangingIntent.DismissError -> handleDismissError()
            is RangingIntent.CheckUwbAvailability -> handleCheckUwbAvailability()
        }
    }

    // ============================================================
    // Intent Handlers — 意图处理器
    // ============================================================

    /**
     * Handle tab selection / 处理 Tab 选择
     */
    private fun handleSelectTab(tab: RangingTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    /**
     * Handle start device discovery / 处理开始设备发现
     */
    private fun handleStartDiscovery() {
        if (!_state.value.isUwbAvailable) {
            viewModelScope.launch {
                _effect.emit(RangingEffect.UwbNotAvailable)
            }
            return
        }

        discoveryJob?.cancel()
        discoveryJob = viewModelScope.launch {
            _state.update { it.copy(isDiscovering = true, error = null) }

            // Simulate device discovery with demo devices
            // 模拟设备发现流程（演示用，实际对接 android.ranging API）
            val demoDevices = listOf(
                UwbDevice(
                    address = "AA:BB:CC:DD:EE:01",
                    displayName = "UWB Tag #1",
                    rssi = -65,
                    firstSeenTimestamp = System.currentTimeMillis()
                ),
                UwbDevice(
                    address = "AA:BB:CC:DD:EE:02",
                    displayName = "UWB Beacon #2",
                    rssi = -72,
                    firstSeenTimestamp = System.currentTimeMillis()
                ),
                UwbDevice(
                    address = "AA:BB:CC:DD:EE:03",
                    displayName = "UWB Hub #3",
                    rssi = -80,
                    firstSeenTimestamp = System.currentTimeMillis()
                ),
                UwbDevice(
                    address = "AA:BB:CC:DD:EE:04",
                    displayName = "UWB Sensor #4",
                    rssi = -88,
                    firstSeenTimestamp = System.currentTimeMillis()
                )
            )

            // Stagger device discovery for visual effect
            // 交错发现设备以产生视觉效果
            for (device in demoDevices) {
                _state.update { state ->
                    val newDevices = if (state.devices.none { it.address == device.address }) {
                        state.devices + device
                    } else {
                        state.devices
                    }
                    state.copy(devices = newDevices)
                }
                delay(300)
            }

            _state.update { it.copy(isDiscovering = false) }
            _effect.emit(RangingEffect.ShowSnackbar("发现 ${demoDevices.size} 个 UWB 设备 / Discovered ${demoDevices.size} UWB devices"))
        }
    }

    /**
     * Handle stop device discovery / 处理停止设备发现
     */
    private fun handleStopDiscovery() {
        discoveryJob?.cancel()
        _state.update { it.copy(isDiscovering = false) }
    }

    /**
     * Handle start ranging for a device / 处理开始测距
     */
    private fun handleStartRanging(deviceAddress: String) {
        if (!_state.value.isUwbAvailable) {
            viewModelScope.launch {
                _effect.emit(RangingEffect.UwbNotAvailable)
            }
            return
        }

        // Check max concurrent ranging limit (typically 8 on Android 17)
        // 检查最大并发测距限制（Android 17 通常为 8 个）
        val currentRangingCount = _state.value.activeRangings.count { it.value == RangingStatus.RANGING }
        if (currentRangingCount >= MAX_CONCURRENT_RANGING) {
            viewModelScope.launch {
                _effect.emit(RangingEffect.ShowSnackbar("已达到最大并发测距数 $MAX_CONCURRENT_RANGING / Max concurrent ranging reached", isError = true))
            }
            return
        }

        // Cancel existing job for this device if any
        // 如果设备已有测距任务则取消
        rangingJobs[deviceAddress]?.cancel()

        rangingJobs[deviceAddress] = viewModelScope.launch {
            // Update status to ranging / 更新状态为测距中
            _state.update { state ->
                state.copy(
                    activeRangings = state.activeRangings + (deviceAddress to RangingStatus.RANGING),
                    devices = state.devices.map { device ->
                        if (device.address == deviceAddress) device.copy(isConnected = true)
                        else device
                    }
                )
            }

            _effect.emit(RangingEffect.RangingStarted(deviceAddress))

            // Start result simulation for demo
            // 开始结果模拟（演示用，实际对接 RangingSession）
            startResultSimulation(deviceAddress)
        }
    }

    /**
     * Start simulated ranging results for demo purposes
     * 启动模拟测距结果（演示用，实际对接 RangingSession.read())
     */
    private fun startResultSimulation(deviceAddress: String) {
        resultSimulationJob?.cancel()
        resultSimulationJob = viewModelScope.launch {
            var baseDistance = (1..10).random().toFloat()
            var trend = if (kotlin.random.Random.nextBoolean()) 0.1f else -0.1f

            while (true) {
                // Simulate distance fluctuation
                // 模拟距离波动
                baseDistance += trend + (kotlin.random.Random.nextFloat() - 0.5f) * 0.2f
                baseDistance = baseDistance.coerceIn(0.5f, 25.0f)

                if (kotlin.random.Random.nextFloat() > 0.9f) {
                    trend = -trend // Random direction change / 随机方向变化
                }

                val result = RangingResult(
                    deviceAddress = deviceAddress,
                    distanceMeters = baseDistance,
                    azimuth = kotlin.random.Random.nextFloat() * 360f,
                    elevation = (kotlin.random.Random.nextFloat() * 180f) - 90f,
                    rssi = (-50..-95).random(),
                    timestamp = System.currentTimeMillis()
                )

                _state.update { state ->
                    val history = state.rangingHistory[deviceAddress]?.toMutableList() ?: mutableListOf()
                    history.add(result)
                    // Keep last 60 seconds of data (at 1s interval = 60 records)
                    // 保留最近 60 秒数据（按 1s 间隔 = 60 条记录）
                    if (history.size > 60) {
                        history.removeAt(0)
                    }

                    val updatedDevices = state.devices.map { device ->
                        if (device.address == deviceAddress) {
                            device.copy(
                                distanceMeters = result.distanceMeters,
                                azimuth = result.azimuth,
                                elevation = result.elevation
                            )
                        } else device
                    }

                    state.copy(
                        rangingHistory = state.rangingHistory + (deviceAddress to history),
                        devices = updatedDevices
                    )
                }

                delay(_state.value.config.updateIntervalMs)
            }
        }
    }

    /**
     * Handle stop ranging for a device / 处理停止测距
     */
    private fun handleStopRanging(deviceAddress: String) {
        rangingJobs[deviceAddress]?.cancel()
        rangingJobs.remove(deviceAddress)

        _state.update { state ->
            state.copy(
                activeRangings = state.activeRangings + (deviceAddress to RangingStatus.IDLE),
                devices = state.devices.map { device ->
                    if (device.address == deviceAddress) {
                        device.copy(isConnected = false, distanceMeters = null)
                    } else device
                }
            )
        }

        viewModelScope.launch {
            _effect.emit(RangingEffect.RangingStopped(deviceAddress))
        }
    }

    /**
     * Handle stop all ranging / 处理停止所有测距
     */
    private fun handleStopAllRanging() {
        rangingJobs.forEach { (_, job) -> job.cancel() }
        rangingJobs.clear()
        resultSimulationJob?.cancel()

        _state.update { state ->
            state.copy(
                activeRangings = state.activeRangings.mapValues { RangingStatus.IDLE },
                devices = state.devices.map { it.copy(isConnected = false, distanceMeters = null) },
                rangingHistory = emptyMap()
            )
        }

        viewModelScope.launch {
            _effect.emit(RangingEffect.ShowSnackbar("已停止所有测距 / All ranging stopped"))
        }
    }

    /**
     * Handle device selection / 处理设备选择
     */
    private fun handleSelectDevice(deviceAddress: String) {
        _state.update { it.copy(selectedDeviceAddress = deviceAddress) }
        viewModelScope.launch {
            _effect.emit(RangingEffect.NavigateToDetail(deviceAddress))
        }
    }

    /**
     * Handle clear selection / 处理清除选择
     */
    private fun handleClearSelection() {
        _state.update { it.copy(selectedDeviceAddress = null) }
    }

    /**
     * Handle update config / 处理更新配置
     */
    private fun handleUpdateConfig(config: RangingConfig) {
        _state.update { it.copy(config = config) }
    }

    /**
     * Handle dismiss error / 处理关闭错误
     */
    private fun handleDismissError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Handle check UWB availability / 处理检测 UWB 可用性
     */
    private fun handleCheckUwbAvailability() {
        // Check if device supports UWB (Android 17+ with UWB hardware)
        // 检测设备是否支持 UWB（Android 17+ 且有 UWB 硬件）
        val isUwbAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                context.packageManager.hasSystemFeature("android.hardware.uwb")

        _state.update { it.copy(isUwbAvailable = isUwbAvailable) }

        if (!isUwbAvailable) {
            viewModelScope.launch {
                _effect.emit(RangingEffect.UwbNotAvailable)
            }
        }
    }

    // ============================================================
    // Tech Comparisons Data — 技术对比数据
    // ============================================================
    /**
     * Load technology comparison data / 加载技术对比数据
     */
    private fun loadTechComparisons() {
        val comparisons = listOf(
            TechComparison(
                name = "UWB (Ranging API)",
                accuracyRange = "±10 cm",
                typicalRange = "0 ~ 50m",
                powerConsumption = PowerLevel.HIGH,
                cost = CostLevel.HIGH,
                compatibility = "Android 17+ (部分设备)",
                bestUseCases = listOf("附近的朋友发现", "精准室内定位", "AR 锚点", "无钥匙进入"),
                pros = listOf("厘米级精度", "抗干扰能力强", "支持角度检测"),
                cons = listOf("硬件要求高", "功耗较大", "设备覆盖率低")
            ),
            TechComparison(
                name = "BLE 蓝牙",
                accuracyRange = "±1 ~ 5m",
                typicalRange = "0 ~ 30m",
                powerConsumption = PowerLevel.LOW,
                cost = CostLevel.LOW,
                compatibility = "Android 4.3+",
                bestUseCases = listOf("防丢器", "健康追踪", "资产追踪"),
                pros = listOf("设备普及率高", "功耗低", "成本低"),
                cons = listOf("精度较差", "受环境干扰大", "无法测角")
            ),
            TechComparison(
                name = "WiFi RTT (IEEE 802.11mc)",
                accuracyRange = "±1 ~ 2m",
                typicalRange = "0 ~ 100m",
                powerConsumption = PowerLevel.MEDIUM,
                cost = CostLevel.MEDIUM,
                compatibility = "Android 9+ (部分设备)",
                bestUseCases = listOf("室内定位", "商场导航", "办公楼定位"),
                pros = listOf("无需额外硬件", "覆盖范围广", "精度尚可"),
                cons = listOf("需要 WiFi AP 支持", "精度不如 UWB", "功耗高于 BLE")
            )
        )
        _state.update { it.copy(techComparisons = comparisons) }
    }

    // ============================================================
    // Power Estimates Data — 功耗估算数据
    // ============================================================
    /**
     * Load power consumption estimates / 加载功耗估算数据
     */
    private fun loadPowerEstimates() {
        val estimates = listOf(
            PowerEstimate(
                technology = "UWB 测距",
                hourlyMah = 45f,
                dailyMah = 360f, // 8h active
                batteryImpactDays = 2.5f,
                description = "UWB 测距功耗较高，主要来自 RF 发射和接收"
            ),
            PowerEstimate(
                technology = "BLE 扫描",
                hourlyMah = 12f,
                dailyMah = 96f, // 8h active
                batteryImpactDays = 0.6f,
                description = "BLE 扫描功耗较低，适合长时间运行的追踪器"
            ),
            PowerEstimate(
                technology = "WiFi RTT",
                hourlyMah = 25f,
                dailyMah = 200f, // 8h active
                batteryImpactDays = 1.3f,
                description = "WiFi RTT 功耗中等，需要 WiFi 芯片持续工作"
            )
        )
        _state.update { it.copy(powerEstimates = estimates) }
    }

    // ============================================================
    // Constants — 常量
    // ============================================================
    companion object {
        /** Maximum concurrent ranging sessions allowed by Android 17 / Android 17 最大并发测距数 */
        const val MAX_CONCURRENT_RANGING = 8
    }

    // ============================================================
    // Cleanup — 清理资源
    // ============================================================
    override fun onCleared() {
        super.onCleared()
        // Clean up all ranging sessions on ViewModel clear
        // ViewModel 清除时清理所有测距会话，防止资源泄漏
        rangingJobs.forEach { (_, job) -> job.cancel() }
        rangingJobs.clear()
        discoveryJob?.cancel()
        resultSimulationJob?.cancel()
    }
}
