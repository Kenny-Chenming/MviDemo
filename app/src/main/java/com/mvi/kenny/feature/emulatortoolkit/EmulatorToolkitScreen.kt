package com.mvi.kenny.feature.emulatortoolkit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.SelectionContainer
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig

// =============================================================
// EmulatorToolkitScreen — Android Emulator 36.5 多设备 P2P
// 网络测试框架工具包主界面
// =============================================================
/**
 * Emulator Toolkit Screen / 模拟器工具包主界面
 *
 * Main dashboard with 6 feature tabs for Android Emulator 36.5 P2P network testing.
 * Layout: TopAppBar + AVDSessionManagerCard + TabRow + HorizontalPager
 *
 * @param viewModel ViewModel for state management / 状态管理的 ViewModel
 * @param onUpdateTopBar TopBar configuration callback / TopBar 配置回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmulatorToolkitScreen(
    viewModel: EmulatorToolkitViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Update TopBar config / 更新 TopBar 配置
    LaunchedEffect(state.activeTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Emulator 36.5 Toolkit",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Refresh / 刷新",
                        onClick = { viewModel.processIntent(EmulatorToolkitIntent.LoadAVDSessions) }
                    )
                )
            )
        )
    }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is EmulatorToolkitEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is EmulatorToolkitEffect.BenchmarkComplete -> {
                    snackbarHostState.showSnackbar("Benchmark complete! / 基准测试完成！")
                }
                is EmulatorToolkitEffect.LogSnapshotSaved -> {
                    snackbarHostState.showSnackbar("Snapshot saved: ${effect.filePath}")
                }
                is EmulatorToolkitEffect.OrchestrationComplete -> {
                    snackbarHostState.showSnackbar("Topology orchestration complete! / 拓扑编排完成！")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // AVDSessionManagerCard — 多 AVD 会话管理卡片
            AVDSessionManagerCard(
                sessions = state.avdSessions,
                networkTopology = state.networkTopology,
                modifier = Modifier.padding(16.dp)
            )

            HorizontalDivider()

            // Tab Row — 6 功能 Tab
            val tabs = ToolTab.entries
            val pagerState = rememberPagerState(pageCount = { tabs.size })

            LaunchedEffect(pagerState.currentPage) {
                viewModel.processIntent(EmulatorToolkitIntent.SelectTab(tabs[pagerState.currentPage]))
            }

            androidx.compose.material3.ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = getTabIcon(tab),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tab.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    )
                }
            }

            // HorizontalPager — Tab 内容
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { page ->
                when (tabs[page]) {
                    ToolTab.TOPOLOGY_ORCHESTRATION -> TopologyOrchestrationTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    ToolTab.NETWORK_SIMULATION -> NetworkSimulationTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    ToolTab.WIFI_P2P_SDK -> WifiP2pSDKTap(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    ToolTab.BLE_EMULATOR -> BleEmulatorFrameworkTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    ToolTab.CROSS_DEVICE_DEBUGGER -> CrossDeviceDebuggerTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    ToolTab.PERFORMANCE_BENCHMARK -> PerformanceBenchmarkTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                }
            }
        }
    }
}

// =============================================================
// AVDSessionManagerCard — AVD 会话管理卡片（主卡）
// =============================================================
/**
 * AVD Session Manager Card / AVD 会话管理卡片
 *
 * Displays all running AVD instances with network topology visualization.
 */
@Composable
fun AVDSessionManagerCard(
    sessions: List<AVDSession>,
    networkTopology: NetworkTopology,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header / 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AVD Session Manager / AVD 会话管理",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${sessions.size} nodes / ${networkTopology.links.count { it.isActive }} links",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Topology Canvas / 拓扑画布
            TopologyCanvas(
                topology = networkTopology,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Session list / 会话列表
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions) { session ->
                    AVDSessionChip(session = session)
                }
            }
        }
    }
}

// =============================================================
// AVDSessionChip — AVD 会话信息芯片
// =============================================================
@Composable
fun AVDSessionChip(session: AVDSession) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (session.p2pState) {
                P2PConnectionState.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
                P2PConnectionState.CONNECTING -> MaterialTheme.colorScheme.tertiaryContainer
                P2PConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
                P2PConnectionState.ERROR -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = session.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = session.serial,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = session.p2pState.label,
                style = MaterialTheme.typography.labelSmall,
                color = session.p2pState.color
            )
        }
    }
}

// =============================================================
// TopologyCanvas — 网络拓扑画布（自定义绘制）
// =============================================================
/**
 * Topology Canvas / 网络拓扑画布
 *
 * Custom Canvas drawing showing AVD nodes and P2P links.
 * Nodes are circles with device name, links are arrows.
 */
@Composable
fun TopologyCanvas(
    topology: NetworkTopology,
    modifier: Modifier = Modifier
) {
    val nodePositions = remember(topology.nodes) {
        if (topology.nodes.isEmpty()) emptyMap()
        else {
            val positions = mutableMapOf<String, Offset>()
            val count = topology.nodes.size
            val angleStep = 360.0 / count
            topology.nodes.forEachIndexed { index, node ->
                val angle = Math.toRadians(angleStep * index - 90)
                val cx = 0.5f + 0.35f * kotlin.math.cos(angle).toFloat()
                val cy = 0.5f + 0.35f * kotlin.math.sin(angle).toFloat()
                positions[node.serial] = Offset(cx, cy)
            }
            positions
        }
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Draw links / 绘制链路
        topology.links.forEach { link ->
            val from = nodePositions[link.fromSerial]
            val to = nodePositions[link.toSerial]
            if (from != null && to != null) {
                val startX = from.x * canvasWidth
                val startY = from.y * canvasHeight
                val endX = to.x * canvasWidth
                val endY = to.y * canvasHeight

                drawLine(
                    color = if (link.isActive) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = if (!link.isActive) PathEffect.dashPathEffect(floatArrayOf(8f, 4f)) else null
                )
            }
        }

        // Draw nodes / 绘制节点
        topology.nodes.forEach { node ->
            val pos = nodePositions[node.serial] ?: return@forEach
            val cx = pos.x * canvasWidth
            val cy = pos.y * canvasHeight
            val radius = 24.dp.toPx()

            drawCircle(
                color = when (node.p2pState) {
                    P2PConnectionState.CONNECTED -> Color(0xFF4CAF50)
                    P2PConnectionState.CONNECTING -> Color(0xFFFFC107)
                    else -> Color(0xFF9E9E9E)
                },
                radius = radius,
                center = Offset(cx, cy)
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = radius * 0.6f,
                center = Offset(cx, cy)
            )
        }
    }
}

// =============================================================
// Tab Icon Mapping / Tab 图标映射
// =============================================================
private fun getTabIcon(tab: ToolTab): ImageVector {
    return when (tab) {
        ToolTab.TOPOLOGY_ORCHESTRATION -> Icons.Default.Router
        ToolTab.NETWORK_SIMULATION -> Icons.Default.NetworkCheck
        ToolTab.WIFI_P2P_SDK -> Icons.Default.Wifi
        ToolTab.BLE_EMULATOR -> Icons.Default.Bluetooth
        ToolTab.CROSS_DEVICE_DEBUGGER -> Icons.Default.BugReport
        ToolTab.PERFORMANCE_BENCHMARK -> Icons.Default.Speed
    }
}

// =============================================================
// Tab Content: TopologyOrchestration / 拓扑编排 Tab
// =============================================================
@Composable
fun TopologyOrchestrationTab(
    state: EmulatorToolkitState,
    onIntent: (EmulatorToolkitIntent) -> Unit
) {
    var expandedTemplateMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Topology Template Selector / 拓扑模板选择器
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Topology Template / 拓扑模板",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box {
                        OutlinedTextField(
                            value = state.topologyTemplate.title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Template / 选择模板") },
                            trailingIcon = {
                                IconButton(onClick = { expandedTemplateMenu = true }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Expand")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedTemplateMenu = true }
                        )
                        DropdownMenu(
                            expanded = expandedTemplateMenu,
                            onDismissRequest = { expandedTemplateMenu = false }
                        ) {
                            TopologyTemplate.entries.forEach { template ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(template.title, fontWeight = FontWeight.Bold)
                                            Text(
                                                template.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        onIntent(EmulatorToolkitIntent.StartTopology(template))
                                        expandedTemplateMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Required devices / 所需设备:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${state.topologyTemplate.deviceCount}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Orchestrator Controls / 编排器控制
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Orchestrator / 编排器",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (state.orchestratorStatus) {
                                            OrchestratorStatus.IDLE -> Color(0xFF9E9E9E)
                                            OrchestratorStatus.RUNNING -> Color(0xFFFFC107)
                                            OrchestratorStatus.COMPLETED -> Color(0xFF4CAF50)
                                            OrchestratorStatus.FAILED -> Color(0xFFF44336)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.orchestratorStatus.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row {
                            Button(
                                onClick = { onIntent(EmulatorToolkitIntent.StartTopology(state.topologyTemplate)) },
                                enabled = state.orchestratorStatus != OrchestratorStatus.RUNNING
                            ) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start / 启动")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onIntent(EmulatorToolkitIntent.StopTopology) },
                                enabled = state.orchestratorStatus == OrchestratorStatus.RUNNING,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Stop, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop / 停止")
                            }
                        }
                    }

                    if (state.startupSequence.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Startup Sequence / 启动序列",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        state.startupSequence.forEachIndexed { index, config ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(20.dp)
                                )
                                Text(
                                    text = config.serial,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = if (config.dependsOn != null) "+${config.delaySeconds}s after ${config.dependsOn}" else "+${config.delaySeconds}s",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // API Layer Note / API 层说明
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "⚠️ API Layer Note / API 层说明\n" +
                            "This panel uses demo data. Real AVD integration requires:\n" +
                            "- Gradle Plugin + CLI for orchestration\n" +
                            "- adb devices → adb emulator <serial> avd name\n" +
                            "此面板使用演示数据。真实 AVD 接入需要 Gradle Plugin + CLI。",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

// =============================================================
// Tab Content: NetworkSimulation / 网络模拟 Tab
// =============================================================
@Composable
fun NetworkSimulationTab(
    state: EmulatorToolkitState,
    onIntent: (EmulatorToolkitIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Network Scenario Presets / 网络场景预设
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Network Scenarios / 网络场景",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.networkScenarios) { scenario ->
                            FilterChip(
                                selected = state.activeScenario?.id == scenario.id,
                                onClick = { onIntent(EmulatorToolkitIntent.ApplyNetworkScenario(scenario)) },
                                label = { Text(scenario.type.label) }
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = state.activeScenario != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        state.activeScenario?.let { scenario ->
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Active: ${scenario.type.label}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = scenario.type.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Targets: ${scenario.targetSerials.joinToString()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(
                                    onClick = { onIntent(EmulatorToolkitIntent.ClearNetworkScenario) }
                                ) {
                                    Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp))
                                    Text("Clear / 清除")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Per-AVD Network Config / 每个 AVD 的网络配置
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Per-AVD Network Config / 每个 AVD 网络配置",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.avdSessions.isEmpty()) {
                        Text(
                            text = "No AVD sessions loaded. Tap refresh to load demo data. / 无 AVD 会话，点击刷新加载演示数据。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        state.avdSessions.forEach { session ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(session.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        session.ipAddress,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = session.p2pState == P2PConnectionState.CONNECTED,
                                    onCheckedChange = { }
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "⚠️ API Layer Note / API 层说明\n" +
                            "Network impairment injection uses adb shell tc command.\n" +
                            "Requires root emulator or API 31+ emulators.\n" +
                            "网络损伤注入使用 adb shell tc 命令，需要 root 或 API 31+。",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

// =============================================================
// Tab Content: WifiP2pSDK / Wi-Fi P2P SDK Tab
// =============================================================
@Composable
fun WifiP2pSDKTap(
    state: EmulatorToolkitState,
    onIntent: (EmulatorToolkitIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // P2P Status Card / P2P 状态卡
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wi-Fi P2P Status / Wi-Fi P2P 状态",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(state.p2pConnectionState.color)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.p2pConnectionState.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = state.p2pConnectionState.color
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onIntent(EmulatorToolkitIntent.StartP2pDiscovery) },
                            enabled = state.p2pConnectionState != P2PConnectionState.CONNECTING
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Text("Discover / 发现")
                        }
                        Button(
                            onClick = { onIntent(EmulatorToolkitIntent.DisconnectP2p) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Disconnect / 断开")
                        }
                    }
                }
            }
        }

        // P2P Device List / P2P 设备列表
        if (state.p2pDevices.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Discovered Devices / 发现的设备",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        state.p2pDevices.forEach { device ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onIntent(EmulatorToolkitIntent.ConnectP2pDevice(device)) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(device.deviceName, fontWeight = FontWeight.Bold)
                                    Text(
                                        device.deviceAddress,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        if (device.isGroupOwner) "Group Owner" else "Peer",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    device.status,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (device.status == "Connected") Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        // SDK API Code Sample / SDK API 代码示例
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WifiP2pManager API Sample / SDK 代码示例",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        SelectionContainer {
                            Text(
                                text = """
                                    |// Initialize WifiP2pManager
                                    |val channel = IntentFilter().apply {
                                    |    addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
                                    |    addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
                                    |    addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
                                    |}
                                    |
                                    |// Discover peers / 发现对等节点
                                    |manager.discoverPeers(channel) { ... }
                                    |
                                    |// Connect to device / 连接到设备
                                    |val config = WifiP2pConfig().apply {
                                    |    deviceAddress = peerAddress
                                    |}
                                    |manager.connect(channel, config) { ... }
                                """.trimMargin(),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// Tab Content: BleEmulatorFramework / BLE 模拟框架 Tab
// =============================================================
@Composable
fun BleEmulatorFrameworkTab(
    state: EmulatorToolkitState,
    onIntent: (EmulatorToolkitIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // BLE Scanner Controls / BLE 扫描控制
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BLE Scanner / BLE 扫描器",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onIntent(EmulatorToolkitIntent.StartBleScan) }) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Text("Start Scan / 开始扫描")
                        }
                        Button(
                            onClick = { onIntent(EmulatorToolkitIntent.StopBleScan) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Stop / 停止")
                        }
                    }
                }
            }
        }

        // BLE Scan Results / BLE 扫描结果
        if (state.bleScanResults.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Scan Results / 扫描结果",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        state.bleScanResults.forEach { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(result.deviceName, fontWeight = FontWeight.Bold)
                                Text(
                                    "${result.rssi} dBm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // BLE Emulator Components / BLE 模拟器组件
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BLE Emulators / BLE 模拟器",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.bleEmulators.isEmpty()) {
                        Text(
                            text = "No BLE emulators configured. / 暂无 BLE 模拟器配置。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        state.bleEmulators.forEach { emulator ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(emulator.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Role: ${emulator.role}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "GATT: ${emulator.gattProfile}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (emulator.isRunning) Color(0xFF4CAF50) else Color(0xFF9E9E9E))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (emulator.isRunning) "Running" else "Stopped",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = emulator.isRunning,
                                        onCheckedChange = {
                                            if (emulator.isRunning) {
                                                onIntent(EmulatorToolkitIntent.StopBleEmulator(emulator.id))
                                            } else {
                                                onIntent(EmulatorToolkitIntent.StartBleEmulator(emulator))
                                            }
                                        }
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        // Emulator vs Real Device / 模拟器 vs 真机对比
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Emulator vs Real Device / 模拟器 vs 真机对比",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val comparisons = listOf(
                        "Feature" to "Emulator" to "Real Device",
                        "BLE Scanning" to "Supported" to "Supported",
                        "GATT Server" to "Limited" to "Full",
                        "HCI Snoop Log" to "via adb bugreport" to "via Developer Options",
                        "Power Management" to "Simulated" to "Real"
                    )

                    comparisons.forEach { (emulator, real) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "$emulator",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$real",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// Tab Content: CrossDeviceDebugger / 跨设备调试器 Tab
// =============================================================
/**
 * Cross-Device Debugger Tab / 跨设备调试器 Tab
 *
 * Multi-device Logcat aggregation, event timeline, log filtering.
 */
@Composable
fun CrossDeviceDebuggerTab(
    state: EmulatorToolkitState,
    onIntent: (EmulatorToolkitIntent) -> Unit
) {
    var keywordFilter by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Log Filter Bar / 日志过滤栏
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aggregated Logs / 聚合日志",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(
                                onClick = { onIntent(EmulatorToolkitIntent.SaveLogSnapshot) }
                            ) {
                                Icon(Icons.Default.Save, "Save snapshot / 保存快照")
                            }
                            IconButton(
                                onClick = { onIntent(EmulatorToolkitIntent.ClearLogs) }
                            ) {
                                Icon(Icons.Default.Clear, "Clear logs / 清除日志")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = keywordFilter,
                        onValueChange = {
                            keywordFilter = it
                            onIntent(EmulatorToolkitIntent.FilterLogs(LogFilter(keyword = it)))
                        },
                        label = { Text("Filter keyword / 过滤关键词") },
                        placeholder = { Text("tag:xxx or message content / 标签或消息内容") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Demo log entries / 演示日志条目
                    if (state.filteredLogs.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No logs yet. Run adb -s <serial> logcat to stream device logs. / 暂无日志。运行 adb -s <serial> logcat 拉取设备日志。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Log entries / 日志条目
        items(state.filteredLogs.take(50)) { entry ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = entry.deviceColor.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(entry.deviceColor)
                            .align(Alignment.Top)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "[${entry.deviceSerial}]",
                                style = MaterialTheme.typography.labelSmall,
                                color = entry.deviceColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = entry.level,
                                style = MaterialTheme.typography.labelSmall,
                                color = when (entry.level) {
                                    "E" -> Color(0xFFF44336)
                                    "W" -> Color(0xFFFF9800)
                                    "I" -> Color(0xFF2196F3)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = entry.tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = entry.message,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// Tab Content: PerformanceBenchmark / 性能基准测试 Tab
// =============================================================
/**
 * Performance Benchmark Tab / 性能基准测试 Tab
 *
 * Benchmark config, execution, result charts and history.
 */
@Composable
fun PerformanceBenchmarkTab(
    state: EmulatorToolkitState,
    onIntent: (EmulatorToolkitIntent) -> Unit
) {
    var deviceCount by remember { mutableIntStateOf(2) }
    var concurrentRequests by remember { mutableIntStateOf(10) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Benchmark Config / 基准测试配置
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Benchmark Config / 基准测试配置",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = deviceCount.toString(),
                            onValueChange = { deviceCount = it.toIntOrNull() ?: 2 },
                            label = { Text("Devices / 设备数") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = concurrentRequests.toString(),
                            onValueChange = { concurrentRequests = it.toIntOrNull() ?: 10 },
                            label = { Text("Concurrent / 并发") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Topology: ${state.topologyTemplate.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress indicator / 进度指示器
                    AnimatedVisibility(
                        visible = state.isRunningBenchmark,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            val progress by animateFloatAsState(
                                targetValue = state.benchmarkProgress,
                                animationSpec = tween(300),
                                label = "benchmark_progress"
                            )
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(progress * 100).toInt()}% — Running benchmark... / 运行基准测试中...",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onIntent(
                                    EmulatorToolkitIntent.StartBenchmark(
                                        BenchmarkConfig(
                                            deviceCount = deviceCount,
                                            concurrentRequests = concurrentRequests
                                        )
                                    )
                                )
                            },
                            enabled = !state.isRunningBenchmark
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run Benchmark / 运行基准测试")
                        }
                        Button(
                            onClick = { onIntent(EmulatorToolkitIntent.StopBenchmark) },
                            enabled = state.isRunningBenchmark,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Stop, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop / 停止")
                        }
                    }
                }
            }
        }

        // Benchmark Chart / 基准测试图表
        if (state.benchmarkResults.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Results Chart / 结果图表",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BenchmarkChart(
                            results = state.benchmarkResults.takeLast(5),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }
        }

        // Historical Results / 历史结果
        if (state.benchmarkResults.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "History / 历史记录",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        state.benchmarkResults.takeLast(5).reversed().forEach { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(result.scenarioName, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${result.deviceCount} devices / ${result.concurrentRequests ?: 0} concurrent",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "%.1f ms".format(result.avgLatencyMs),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "%.1f Mbps".format(result.throughputMbps),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// BenchmarkChart — 性能对比柱状图（自定义绘制）
// =============================================================
/**
 * Benchmark Chart / 性能对比柱状图
 *
 * Custom Canvas drawing for benchmark comparison visualization.
 * Shows latency, throughput, and packet loss for each benchmark run.
 *
 * @param results List of benchmark results / 基准测试结果列表
 * @param modifier Compose modifier / Compose 修饰符
 */
@Composable
fun BenchmarkChart(
    results: List<BenchmarkResult>,
    modifier: Modifier = Modifier
) {
    if (results.isEmpty()) return

    val maxLatency = results.maxOfOrNull { it.avgLatencyMs } ?: 100.0
    val maxThroughput = results.maxOfOrNull { it.throughputMbps } ?: 100.0

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barGroupWidth = canvasWidth / results.size
        val barWidth = barGroupWidth * 0.3f
        val spacing = barGroupWidth * 0.05f

        results.forEachIndexed { index, result ->
            val groupX = index * barGroupWidth

            // Latency bar (normalized to max) / 延迟柱（归一化到最大值）
            val latencyHeight = (result.avgLatencyMs / maxLatency * canvasHeight * 0.8f).toFloat()
            drawRect(
                color = Color(0xFF2196F3),
                topLeft = Offset(groupX + spacing, canvasHeight - latencyHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, latencyHeight)
            )

            // Throughput bar / 吞吐量柱
            val throughputHeight = (result.throughputMbps / maxThroughput * canvasHeight * 0.8f).toFloat()
            drawRect(
                color = Color(0xFF4CAF50),
                topLeft = Offset(groupX + spacing + barWidth + spacing, canvasHeight - throughputHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, throughputHeight)
            )

            // Baseline / 基准线
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(0f, canvasHeight - latencyHeight),
                end = Offset(canvasWidth, canvasHeight - latencyHeight),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
            )
        }
    }

    // Legend / 图例
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF2196F3), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Latency / 延迟 (ms)", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF4CAF50), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Throughput / 吞吐量 (Mbps)", style = MaterialTheme.typography.labelSmall)
        }
    }
}
