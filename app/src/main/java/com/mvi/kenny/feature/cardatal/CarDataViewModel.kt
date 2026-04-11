package com.mvi.kenny.feature.cardatal

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
 * CarDataViewModel — 车辆数据监控 ViewModel
 * ============================================================
 * MVI architecture ViewModel for CarData feature.
 * PRD-080: Android Auto Car App Library API Level 3 车辆数据集成工具包
 *
 * Key behaviors:
 * 1. Manages car connection lifecycle (connect/disconnect)
 * 2. Provides vehicle data simulation via VehicleSimulator
 * 3. Generates mock nearby fuel/charging stations based on fuel level
 * 4. Handles graceful degradation when Car API Level 3 is unavailable
 * 5. Exposes data as MVI StateFlow + Effect Channel
 *
 * Design Reference: memory/agency/designs/PRD-080-Android-Auto-Car-App-Library-API-Level-3-车辆数据集成工具包.md
 * —————————————————————————————————————————————————————
 */
class CarDataViewModel : ViewModel() {

    // MVI State — single source of truth using StateFlow
    private val _state = MutableStateFlow(CarDataState.Initial)
    val state: StateFlow<CarDataState> = _state.asStateFlow()

    // MVI Effect — one-time side effects via Channel
    private val _effect = Channel<CarDataEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // Vehicle data simulator instance
    private val simulator = VehicleSimulator()

    init {
        // Check API Level 3 support on startup
        checkApiSupport()
    }

    /**
     * Checks whether the vehicle supports Car App Library API Level 3.
     * In a real implementation, this would query CarPropertyManager.
     * Here we simulate: connected only when simulator is active.
     */
    private fun checkApiSupport() {
        // Real implementation would use CarPropertyManager to check API level
        // For demo: always report as supported (simulator will provide data)
        _state.value = _state.value.copy(
            isApiLevel3Supported = true,
            degradationMessage = null
        )
    }

    /**
     * Processes incoming user Intents and updates state accordingly.
     *
     * @param intent User intent from the UI layer
     * @see CarDataIntent All available user intents
     */
    fun sendIntent(intent: CarDataIntent) {
        when (intent) {
            is CarDataIntent.Connect -> connect()
            is CarDataIntent.Disconnect -> disconnect()
            is CarDataIntent.StartSimulator -> startSimulator(intent.scenario)
            is CarDataIntent.StopSimulator -> stopSimulator()
            is CarDataIntent.RequestNearbyStations -> loadNearbyStations(intent.radiusKm)
            is CarDataIntent.RequestCarPermission -> requestPermission()
            is CarDataIntent.SelectFuelStation -> selectFuelStation(intent.stationId)
        }
    }

    /**
     * Initiates connection to the vehicle.
     * In a real implementation, this would establish CarAppService connection.
     */
    private fun connect() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                connectionStatus = CarConnectionStatus.CONNECTED,
                isApiLevel3Supported = true,
                degradationMessage = null
            )
            _effect.send(CarDataEffect.ConnectionSuccess)

            // Auto-start simulator to provide demo data
            // In production, real vehicle data would flow from CarSensorManager
            startSimulatorDataFlow()
        }
    }

    /**
     * Disconnects from the vehicle and clears data.
     */
    private fun disconnect() {
        viewModelScope.launch {
            stopSimulatorDataFlow()
            _state.value = CarDataState(
                isApiLevel3Supported = _state.value.isApiLevel3Supported,
                connectionStatus = CarConnectionStatus.DISCONNECTED
            )
            _effect.send(CarDataEffect.DisconnectionSuccess)
        }
    }

    /**
     * Starts the vehicle data simulator with the given scenario.
     *
     * @param scenario Selected driving scenario
     */
    private fun startSimulator(scenario: DrivingScenario) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSimulatorActive = true,
                currentScenario = scenario,
                connectionStatus = CarConnectionStatus.CONNECTED
            )
            simulator.startSimulation(scenario)
            startSimulatorDataFlow()
        }
    }

    /**
     * Stops the vehicle data simulator.
     */
    private fun stopSimulator() {
        viewModelScope.launch {
            stopSimulatorDataFlow()
            _state.value = _state.value.copy(
                isSimulatorActive = false,
                currentScenario = null,
                currentSpeed = null,
                fuelLevel = null,
                engineTemperature = null,
                hvacState = null,
                estimatedRangeKm = null
            )
        }
    }

    /**
     * Starts the simulator data flow — generates mock vehicle data periodically.
     * This simulates real-time data from CarSensorManager.
     */
    private fun startSimulatorDataFlow() {
        viewModelScope.launch {
            while (_state.value.isSimulatorActive && _state.value.connectionStatus == CarConnectionStatus.CONNECTED) {
                val scenario = _state.value.currentScenario ?: DrivingScenario.CITY_DRIVING
                val data = simulator.generateData(scenario)

                _state.value = _state.value.copy(
                    currentSpeed = data.speed,
                    fuelLevel = data.fuelLevel,
                    engineTemperature = data.engineTemperature,
                    hvacState = data.hvacState,
                    estimatedRangeKm = data.fuelLevel?.rangeKm
                )

                delay(1000L) // Update every second
            }
        }
    }

    /**
     * Stops the simulator data flow coroutine.
     */
    private fun stopSimulatorDataFlow() {
        simulator.stopSimulation()
    }

    /**
     * Loads nearby fuel/charging stations based on current fuel level.
     * Generates mock data; real implementation would use Places API.
     *
     * @param radiusKm Search radius in kilometers
     */
    private fun loadNearbyStations(radiusKm: Float) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingNearbyStations = true)

            // Simulate network delay
            delay(800L)

            val fuelPercent = _state.value.fuelLevel?.fuelPercent ?: 50f
            val fuelType = _state.value.fuelLevel?.fuelType ?: FuelType.GASOLINE

            // Generate mock stations when fuel is below threshold
            val stations = if (fuelPercent < 40f) {
                generateMockStations(fuelType, radiusKm)
            } else {
                emptyList()
            }

            _state.value = _state.value.copy(
                nearbyFuelStations = stations,
                isLoadingNearbyStations = false
            )
        }
    }

    /**
     * Requests car API permission.
     * In a real app, this would trigger the car permission flow.
     */
    private fun requestPermission() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                connectionStatus = CarConnectionStatus.PERMISSION_DENIED
            )
            _effect.send(CarDataEffect.RequestCarPermission)
        }
    }

    /**
     * Handles fuel station selection.
     *
     * @param stationId Selected station ID
     */
    private fun selectFuelStation(stationId: String) {
        viewModelScope.launch {
            _effect.send(CarDataEffect.NavigateToFuelStation(stationId))
        }
    }

    /**
     * Generates mock fuel/charging stations for demo purposes.
     *
     * @param fuelType Fuel type
     * @param radiusKm Search radius
     * @return List of mock fuel stations
     */
    private fun generateMockStations(fuelType: FuelType, radiusKm: Float): List<FuelStation> {
        val stationNames = if (fuelType == FuelType.ELECTRIC) {
            listOf("特来电充电站" to 1.2f, "国家电网充电站" to 2.5f, "星星充电站" to 3.8f)
        } else {
            listOf("中石化加油站" to 1.5f, "中石油加油站" to 2.8f, "壳牌加油站" to 4.2f)
        }

        return stationNames.mapIndexed { index, (name, distance) ->
            FuelStation(
                id = "station_$index",
                name = name,
                distanceKm = distance * (radiusKm / 5f).coerceAtMost(3f),
                fuelType = fuelType,
                price = if (fuelType == FuelType.ELECTRIC) (0.8f + index * 0.15f) else (6.5f + index * 0.3f)
            )
        }
    }
}

/**
 * ============================================================
 * VehicleSimulator — 车辆数据模拟器
 * ============================================================
 * Generates realistic mock vehicle data for development without real car.
 * Data format is identical to real CarSensorManager output for seamless switching.
 *
 * @see CarDataManager Real implementation interface
 */
class VehicleSimulator {

    private var isRunning = false
    private var currentScenario: DrivingScenario = DrivingScenario.CITY_DRIVING

    // Internal state for generating realistic variations
    private var speedBase: Float = 15f
    private var fuelBase: Float = 65f
    private var tempBase: Float = 85f

    /**
     * Starts simulation with the given scenario.
     *
     * @param scenario Driving scenario
     */
    fun startSimulation(scenario: DrivingScenario) {
        isRunning = true
        currentScenario = scenario
        resetForScenario(scenario)
    }

    /**
     * Stops the simulation.
     */
    fun stopSimulation() {
        isRunning = false
    }

    /**
     * Generates a single frame of vehicle data.
     * Called every second by the ViewModel.
     *
     * @param scenario Current driving scenario
     * @return SimulatedVehicleData
     */
    fun generateData(scenario: DrivingScenario): SimulatedVehicleData {
        if (!isRunning) return SimulatedVehicleData()

        // Update internal state with slight random variations for realism
        updateState(scenario)

        val timestamp = System.currentTimeMillis()

        return SimulatedVehicleData(
            speed = VehicleSpeed(
                speedMps = speedBase,
                speedMph = speedBase * 2.237f,
                timestamp = timestamp
            ),
            fuelLevel = FuelLevel(
                fuelPercent = fuelBase.coerceIn(0f, 100f),
                rangeKm = (fuelBase * 6.5f), // ~6.5km per 1% fuel
                fuelType = FuelType.GASOLINE
            ),
            engineTemperature = EngineTemperature(
                temperatureCelsius = tempBase,
                isOverheating = tempBase > 105f
            ),
            hvacState = HvacState(
                cabinTemperatureCelsius = 22f + (tempBase - 80f) / 10f,
                fanSpeed = when {
                    speedBase > 30f -> 3
                    speedBase > 15f -> 2
                    else -> 1
                },
                acEnabled = tempBase > 90f,
                defrostEnabled = false
            )
        )
    }

    /**
     * Resets simulator state for a new scenario.
     */
    private fun resetForScenario(scenario: DrivingScenario) {
        when (scenario) {
            DrivingScenario.CITY_DRIVING -> {
                speedBase = 12f
                fuelBase = 55f
                tempBase = 82f
            }
            DrivingScenario.HIGHWAY_CRUISE -> {
                speedBase = 28f
                fuelBase = 50f
                tempBase = 88f
            }
            DrivingScenario.HEAVY_TRAFFIC -> {
                speedBase = 3f
                fuelBase = 48f
                tempBase = 78f
            }
            DrivingScenario.HARD_ACCELERATION -> {
                speedBase = 35f
                fuelBase = 42f
                tempBase = 98f
            }
            DrivingScenario.COLD_START -> {
                speedBase = 0f
                fuelBase = 70f
                tempBase = 25f
            }
            DrivingScenario.ELECTRIC_ECONOMY -> {
                speedBase = 18f
                fuelBase = 80f
                tempBase = 75f
            }
        }
    }

    /**
     * Updates internal state with realistic drift.
     */
    private fun updateState(scenario: DrivingScenario) {
        // Speed variation based on scenario
        val speedDelta = when (scenario) {
            DrivingScenario.CITY_DRIVING -> (Math.random() * 6 - 3).toFloat()
            DrivingScenario.HIGHWAY_CRUISE -> (Math.random() * 4 - 2).toFloat()
            DrivingScenario.HEAVY_TRAFFIC -> (Math.random() * 2 - 1).toFloat()
            DrivingScenario.HARD_ACCELERATION -> (Math.random() * 8).toFloat()
            DrivingScenario.COLD_START -> 0f
            DrivingScenario.ELECTRIC_ECONOMY -> (Math.random() * 3 - 1.5f).toFloat()
        }
        speedBase = (speedBase + speedDelta).coerceAtLeast(0f)

        // Fuel consumption
        val fuelConsumption = when (scenario) {
            DrivingScenario.HARD_ACCELERATION -> 0.15f
            DrivingScenario.HIGHWAY_CRUISE -> 0.08f
            DrivingScenario.HEAVY_TRAFFIC -> 0.05f
            DrivingScenario.CITY_DRIVING -> 0.06f
            DrivingScenario.COLD_START -> 0.02f
            DrivingScenario.ELECTRIC_ECONOMY -> 0.03f
        }
        fuelBase = (fuelBase - fuelConsumption).coerceAtLeast(5f)

        // Engine temperature
        val tempDelta = when {
            tempBase < 80f -> 0.5f // Warming up
            tempBase > 95f -> -(Math.random() * 2).toFloat() // Cooling down
            else -> (Math.random() * 3 - 1.5f).toFloat()
        }
        tempBase = (tempBase + tempDelta).coerceIn(20f, 110f)
    }
}

/**
 * ============================================================
 * SimulatedVehicleData — 模拟数据容器
 * ============================================================
 *
 * @param speed Vehicle speed
 * @param fuelLevel Fuel level
 * @param engineTemperature Engine temperature
 * @param hvacState HVAC state
 */
data class SimulatedVehicleData(
    val speed: VehicleSpeed? = null,
    val fuelLevel: FuelLevel? = null,
    val engineTemperature: EngineTemperature? = null,
    val hvacState: HvacState? = null
)
