package com.mvi.kenny.feature.cardatal

import androidx.compose.foundation.background
import com.mvi.kenny.base.TopBarConfig
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * CarDataScreen — Android Auto Car App Library API Level 3 监控界面
 * ============================================================
 * Composable screen for vehicle data monitoring (Car App Library API Level 3).
 * PRD-080: Android Auto Car App Library API Level 3 车辆数据集成工具包
 *
 * Features:
 * - Real-time vehicle data display (speed, fuel, temperature, HVAC)
 * - Vehicle data simulator with multiple driving scenarios
 * - Nearby fuel/charging station recommendations
 * - Graceful degradation when API not supported
 *
 * Design Reference: memory/agency/designs/PRD-080-Android-Auto-Car-App-Library-API-Level-3-车辆数据集成工具包.md
 * —————————————————————————————————————————————————————
 */

// Design spec colors / 设计规范颜色
private val GoogleBlue = Color(0xFF1A73E8)
private val EnergyGreen = Color(0xFF34A853)
private val WarningYellow = Color(0xFFFBBC04)
private val ErrorRed = Color(0xFFEA4335)
private val DarkSurface = Color(0xFF121212)
private val DarkCard = Color(0xFF1E1E1E)

/**
 * ============================================================
 * CarDataScreen — 主入口 Composable
 * ============================================================
 *
 * @param viewModel CarDataViewModel instance
 * @param onUpdateTopBar Callback to update the TopAppBar title
 * @param onShowSnackbar Callback for showing snackbar messages
 */
@Composable
fun CarDataScreen(
    viewModel: CarDataViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {},
    onShowSnackbar: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    // Update TopAppBar title on screen load
    LaunchedEffect(Unit) {
        onUpdateTopBar(TopBarConfig(title = "车辆数据 / Car Data"))
    }

    // Handle side effects from ViewModel
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CarDataEffect.ShowError -> onShowSnackbar(effect.message)
                is CarDataEffect.NavigateToFuelStation -> onShowSnackbar("导航到: ${effect.stationId}")
                is CarDataEffect.RequestCarPermission -> onShowSnackbar("请求车辆权限")
                is CarDataEffect.ConnectionSuccess -> onShowSnackbar("车辆连接成功")
                is CarDataEffect.DisconnectionSuccess -> onShowSnackbar("车辆已断开")
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkSurface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connection Status Bar
            item {
                CarConnectionStatusBar(
                    status = state.connectionStatus,
                    isApiLevel3Supported = state.isApiLevel3Supported,
                    degradationMessage = state.degradationMessage
                )
            }

            // Connect/Disconnect Button
            item {
                ConnectionButton(
                    isConnected = state.connectionStatus == CarConnectionStatus.CONNECTED,
                    onConnect = { viewModel.sendIntent(CarDataIntent.Connect) },
                    onDisconnect = { viewModel.sendIntent(CarDataIntent.Disconnect) }
                )
            }

            // Simulator Panel
            item {
                SimulatorPanel(
                    isActive = state.isSimulatorActive,
                    currentScenario = state.currentScenario,
                    onStartSimulator = { scenario ->
                        viewModel.sendIntent(CarDataIntent.StartSimulator(scenario))
                    },
                    onStopSimulator = { viewModel.sendIntent(CarDataIntent.StopSimulator) }
                )
            }

            // Vehicle Data Monitor (Speed, Fuel, Temp, HVAC)
            if (state.connectionStatus == CarConnectionStatus.CONNECTED || state.isSimulatorActive) {
                item {
                    Text(
                        text = "实时数据 / Live Data",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Speed Card
                state.currentSpeed?.let { speed ->
                    item {
                        VehicleDataCard(
                            title = "车速 / Speed",
                            icon = Icons.Default.Speed,
                            iconColor = GoogleBlue
                        ) {
                            SpeedGauge(speedMps = speed.speedMps)
                        }
                    }
                }

                // Fuel Card
                state.fuelLevel?.let { fuel ->
                    item {
                        VehicleDataCard(
                            title = "油量/电量 / Fuel",
                            icon = if (fuel.fuelType == FuelType.ELECTRIC)
                                Icons.Default.BatteryChargingFull else Icons.Default.LocalGasStation,
                            iconColor = when {
                                fuel.fuelPercent < 15f -> ErrorRed
                                fuel.fuelPercent < 30f -> WarningYellow
                                else -> EnergyGreen
                            }
                        ) {
                            FuelGauge(
                                fuelPercent = fuel.fuelPercent,
                                rangeKm = fuel.rangeKm,
                                fuelType = fuel.fuelType
                            )
                        }
                    }
                }

                // Engine Temperature Card
                state.engineTemperature?.let { temp ->
                    item {
                        VehicleDataCard(
                            title = "发动机温度 / Engine Temp",
                            icon = Icons.Default.Thermostat,
                            iconColor = if (temp.isOverheating) ErrorRed else GoogleBlue
                        ) {
                            TemperatureDisplay(
                                temperatureCelsius = temp.temperatureCelsius,
                                isOverheating = temp.isOverheating
                            )
                        }
                    }
                }

                // HVAC Card
                state.hvacState?.let { hvac ->
                    item {
                        VehicleDataCard(
                            title = "空调状态 / HVAC",
                            icon = Icons.Default.Build,
                            iconColor = GoogleBlue
                        ) {
                            HvacDisplay(hvacState = hvac)
                        }
                    }
                }

                // Range Prediction Banner
                state.estimatedRangeKm?.let { range ->
                    item {
                        RangePredictionBanner(rangeKm = range)
                    }
                }

                // Nearby Stations
                item {
                    NearbyStationsSection(
                        stations = state.nearbyFuelStations,
                        isLoading = state.isLoadingNearbyStations,
                        currentFuelPercent = state.fuelLevel?.fuelPercent,
                        onRequestStations = { radius ->
                            viewModel.sendIntent(CarDataIntent.RequestNearbyStations(radius))
                        },
                        onStationClick = { stationId ->
                            viewModel.sendIntent(CarDataIntent.SelectFuelStation(stationId))
                        }
                    )
                }
            }

            // Empty state when not connected
            if (state.connectionStatus == CarConnectionStatus.DISCONNECTED && !state.isSimulatorActive) {
                item {
                    EmptyStatePanel()
                }
            }

            // Bottom padding
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

/**
 * ============================================================
 * CarConnectionStatusBar — 车辆连接状态栏
 * ============================================================
 */
@Composable
private fun CarConnectionStatusBar(
    status: CarConnectionStatus,
    isApiLevel3Supported: Boolean,
    degradationMessage: String?
) {
    val (statusColor, statusText) = when (status) {
        CarConnectionStatus.CONNECTED -> ErrorRed to "已连接 / Connected"
        CarConnectionStatus.DISCONNECTED -> Color.Gray to "未连接 / Disconnected"
        CarConnectionStatus.API_NOT_SUPPORTED -> WarningYellow to "API 不支持 / Not Supported"
        CarConnectionStatus.PERMISSION_DENIED -> WarningYellow to "权限被拒绝 / Permission Denied"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = statusText,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (!isApiLevel3Supported || degradationMessage != null) {
                    Text(
                        text = degradationMessage ?: "车辆不支持 Car API Level 3",
                        color = WarningYellow,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * ============================================================
 * ConnectionButton — 连接/断开按钮
 * ============================================================
 */
@Composable
private fun ConnectionButton(
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Button(
        onClick = { if (isConnected) onDisconnect() else onConnect() },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isConnected) ErrorRed else GoogleBlue
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = if (isConnected) Icons.Default.Stop else Icons.Default.DirectionsCar,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isConnected) "断开连接 / Disconnect" else "连接车辆 / Connect",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * ============================================================
 * SimulatorPanel — 模拟器控制面板
 * ============================================================
 */
@Composable
private fun SimulatorPanel(
    isActive: Boolean,
    currentScenario: DrivingScenario?,
    onStartSimulator: (DrivingScenario) -> Unit,
    onStopSimulator: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = GoogleBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "车辆模拟器 / Vehicle Simulator",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isActive && currentScenario != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = EnergyGreen,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = currentScenario.displayName,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isActive) {
                Text(
                    text = "选择驾驶场景启动模拟器：",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DrivingScenario.entries) { scenario ->
                        FilterChip(
                            selected = false,
                            onClick = { onStartSimulator(scenario) },
                            label = { Text(scenario.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkSurface,
                                labelColor = Color.White
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            } else {
                Button(
                    onClick = onStopSimulator,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("停止模拟器 / Stop Simulator")
                }
            }
        }
    }
}

/**
 * ============================================================
 * VehicleDataCard — 单项车辆数据卡片
 * ============================================================
 */
@Composable
private fun VehicleDataCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * ============================================================
 * SpeedGauge — 速度仪表盘
 * ============================================================
 */
@Composable
private fun SpeedGauge(speedMps: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "%.1f".format(speedMps),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "m/s",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "%.1f".format(speedMps * 2.237f),
                color = GoogleBlue,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "mph",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * ============================================================
 * FuelGauge — 油量/电量仪表盘
 * ============================================================
 */
@Composable
private fun FuelGauge(
    fuelPercent: Float,
    rangeKm: Float,
    fuelType: FuelType
) {
    val gaugeColor = when {
        fuelPercent < 15f -> ErrorRed
        fuelPercent < 30f -> WarningYellow
        else -> EnergyGreen
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "%.0f%%".format(fuelPercent),
                color = gaugeColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.0f km".format(rangeKm),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "剩余续航 / Range",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (fuelPercent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = gaugeColor,
            trackColor = Color.DarkGray
        )
    }
}

/**
 * ============================================================
 * TemperatureDisplay — 温度显示
 * ============================================================
 */
@Composable
private fun TemperatureDisplay(
    temperatureCelsius: Float,
    isOverheating: Boolean
) {
    val tempColor = if (isOverheating) ErrorRed else GoogleBlue

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "%.1f°C".format(temperatureCelsius),
            color = tempColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        if (isOverheating) {
            Surface(
                color = ErrorRed,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "⚠️ 过热 / Overheating",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        } else {
            Text(
                text = "正常 / Normal",
                color = EnergyGreen,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * ============================================================
 * HvacDisplay — 空调状态显示
 * ============================================================
 */
@Composable
private fun HvacDisplay(hvacState: HvacState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "%.0f°C".format(hvacState.cabinTemperatureCelsius),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "车内温度 / Cabin",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HvacBadge(
                    label = "A/C",
                    isActive = hvacState.acEnabled,
                    activeColor = GoogleBlue
                )
                HvacBadge(
                    label = "Def",
                    isActive = hvacState.defrostEnabled,
                    activeColor = WarningYellow
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "风扇 / Fan: ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            repeat(7) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index < hvacState.fanSpeed) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < hvacState.fanSpeed) GoogleBlue else Color.DarkGray
                        )
                )
                if (index < 6) Spacer(modifier = Modifier.width(2.dp))
            }
            Text(
                text = " ${hvacState.fanSpeed}/7",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * ============================================================
 * HvacBadge — 空调标签徽章
 * ============================================================
 */
@Composable
private fun HvacBadge(label: String, isActive: Boolean, activeColor: Color) {
    Surface(
        color = if (isActive) activeColor else Color.DarkGray,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * ============================================================
 * RangePredictionBanner — 续航预测横幅
 * ============================================================
 */
@Composable
private fun RangePredictionBanner(rangeKm: Float) {
    val bannerColor = when {
        rangeKm < 30f -> ErrorRed
        rangeKm < 80f -> WarningYellow
        else -> EnergyGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bannerColor.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = bannerColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "预计续航 / Est. Range",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = "%.0f km".format(rangeKm),
                    color = bannerColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * ============================================================
 * NearbyStationsSection — 附近加油站/充电站
 * ============================================================
 */
@Composable
private fun NearbyStationsSection(
    stations: List<FuelStation>,
    isLoading: Boolean,
    currentFuelPercent: Float?,
    onRequestStations: (Float) -> Unit,
    onStationClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = EnergyGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "附近加油站 / Nearby Stations",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { onRequestStations(5f) },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("搜索 / Search", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GoogleBlue)
                    }
                }
                stations.isEmpty() -> {
                    val fuelOk = (currentFuelPercent ?: 100f) >= 40f
                    Text(
                        text = if (fuelOk) "油量充足，暂不需要加油" else "加载中...",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                else -> {
                    stations.forEach { station ->
                        FuelStationCard(
                            station = station,
                            onClick = { onStationClick(station.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * ============================================================
 * FuelStationCard — 加油站卡片
 * ============================================================
 */
@Composable
private fun FuelStationCard(
    station: FuelStation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (station.fuelType == FuelType.ELECTRIC)
                        Icons.Default.BatteryChargingFull else Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = EnergyGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = station.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "%.1f km".format(station.distanceKm),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            station.price?.let { price ->
                Text(
                    text = "¥%.1f".format(price),
                    color = GoogleBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * ============================================================
 * EmptyStatePanel — 空状态面板
 * ============================================================
 */
@Composable
private fun EmptyStatePanel() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "未连接车辆 / Not Connected",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击上方「连接车辆」按钮，或启动模拟器获取车辆数据",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
