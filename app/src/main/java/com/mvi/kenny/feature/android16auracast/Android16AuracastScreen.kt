package com.mvi.kenny.feature.android16auracast

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.android16auracast.Android16AuracastContract.Colors as Colors
import com.mvi.kenny.feature.android16auracast.Android16AuracastContract.Spacing as Spacing
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * Android16AuracastScreen — Android 16 Auracast 开发工具包主屏幕
 * ============================================================
 * PRD-045 | Android 16 Beta 3 Platform Stability 迁移与 Auracast 开发工具包
 *
 * 6 功能模块 Tab：
 * - AURACAST_BROADCAST → LE Audio 广播管理（发送端）
 * - AURACAST_RECEIVER  → LE Audio 接收管理（接收端）
 * - OUTLINE_TEXT       → Android 16 无障碍 Outline Text API 组件
 * - LOCAL_NETWORK      → Local Network 权限引导 UI
 * - BEHAVIOR_CHANGE    → Android 16 行为变更速查面板
 * - MIGRATION          → 迁移检测与 Gradle 报告面板
 *
 * @param viewModel Android16AuracastViewModel 实例
 * @param onNavigateTo 路由导航回调（可选）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Android16AuracastScreen(
    viewModel: Android16AuracastViewModel = viewModel(),
    onNavigateTo: ((String) -> Unit)? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // 权限申请 Launcher（Local Network）
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        viewModel.processIntent(
            Android16AuracastIntent.UpdatePermissionState(
                if (granted) PermissionState.GRANTED else PermissionState.DENIED
            )
        )
    }

    // 监听 Effect / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Android16AuracastEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is Android16AuracastEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is Android16AuracastEffect.NavigateTo -> {
                    onNavigateTo?.invoke(effect.route)
                }
                is Android16AuracastEffect.OpenSystemSettings -> {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", context.packageName, null)
                    })
                }
                is Android16AuracastEffect.ExportReport -> {
                    // Handled by ShowSnackbar
                }
                is Android16AuracastEffect.ShowError -> {
                    snackbarHostState.showSnackbar("${effect.title}: ${effect.message}")
                }
                is Android16AuracastEffect.BroadcastStarted -> {
                    // Already handled by snackbar in ViewModel
                }
                is Android16AuracastEffect.SourceLost -> {
                    Toast.makeText(context, "音频源丢失: ${effect.sourceId}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Android 16 开发工具",
                        fontWeight = FontWeight.Medium
                    )
                },
                actions = {
                    // Outline Text API 支持状态指示器
                    if (!viewModel.isOutlineTextSupported) {
                        AssistChip(
                            onClick = { },
                            label = { Text("预览模式", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Colors.P1Color.copy(alpha = 0.15f),
                                labelColor = Colors.P1Color,
                                leadingIconContentColor = Colors.P1Color
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    IconButton(onClick = {
                        viewModel.processIntent(Android16AuracastIntent.ExportMigrationReport)
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "导出报告")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row / Tab 切换栏
            Android16TabRow(
                selectedTab = state.currentTab,
                onTabSelected = { viewModel.processIntent(Android16AuracastIntent.SwitchTab(it)) }
            )

            // Tab Content / Tab 内容区
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (state.currentTab) {
                    Android16Tab.AURACAST_BROADCAST -> AuracastBroadcastTab(state, viewModel::processIntent)
                    Android16Tab.AURACAST_RECEIVER -> AuracastReceiverTab(state, viewModel::processIntent)
                    Android16Tab.OUTLINE_TEXT -> OutlineTextTab(state, viewModel::processIntent, viewModel.isOutlineTextSupported)
                    Android16Tab.LOCAL_NETWORK -> LocalNetworkTab(state, viewModel::processIntent, permissionLauncher)
                    Android16Tab.BEHAVIOR_CHANGE -> BehaviorChangeTab(state, viewModel::processIntent, viewModel.filteredBehaviorChanges)
                    Android16Tab.MIGRATION -> MigrationTab(state, viewModel::processIntent)
                }
            }
        }
    }
}

// ============================================================
// Tab Row / Tab 切换栏
// ============================================================

@Composable
private fun Android16TabRow(
    selectedTab: Android16Tab,
    onTabSelected: (Android16Tab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = Android16Tab.entries.indexOf(selectedTab),
        edgePadding = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Android16Tab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                icon = {
                    Icon(
                        imageVector = getAndroid16TabIcon(tab),
                        contentDescription = tab.title,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

private fun getAndroid16TabIcon(tab: Android16Tab): ImageVector = when (tab) {
    Android16Tab.AURACAST_BROADCAST -> Icons.Default.Radio
    Android16Tab.AURACAST_RECEIVER -> Icons.Default.Headphones
    Android16Tab.OUTLINE_TEXT -> Icons.Default.TextFields
    Android16Tab.LOCAL_NETWORK -> Icons.Default.Wifi
    Android16Tab.BEHAVIOR_CHANGE -> Icons.Default.Warning
    Android16Tab.MIGRATION -> Icons.Default.MoveToInbox
}

// ============================================================
// Auracast Broadcast Tab / 广播发送模块
// ============================================================

@Composable
private fun AuracastBroadcastTab(
    state: Android16AuracastState,
    onIntent: (Android16AuracastIntent) -> Unit
) {
    val broadcastState = state.auracastBroadcastState

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // 设备兼容性提示
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Colors.P1Color.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Colors.P1Color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Auracast 广播需要物理 Android 16+ 设备，并开启蓝牙和位置权限",
                    style = MaterialTheme.typography.bodySmall,
                    color = Colors.P1Color
                )
            }
        }

        // 广播名称输入
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "广播设置",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = broadcastState.broadcastName,
                    onValueChange = { onIntent(Android16AuracastIntent.UpdateBroadcastName(it)) },
                    label = { Text("广播名称") },
                    placeholder = { Text("例如：我的会议室音响") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !broadcastState.isBroadcasting
                )
            }
        }

        // 广播状态卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (broadcastState.status) {
                    BroadcastStatus.BROADCASTING -> Color(0xFF1B5E20).copy(alpha = 0.1f)
                    BroadcastStatus.STARTING -> Color(0xFF1565C0).copy(alpha = 0.1f)
                    BroadcastStatus.PAUSED -> Color(0xFFE65100).copy(alpha = 0.1f)
                    BroadcastStatus.ERROR -> Color(0xFFB71C1C).copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "广播状态",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    StatusBadge(status = broadcastState.status)
                }

                Spacer(modifier = Modifier.height(12.dp))

                broadcastState.broadcastSession?.let { session ->
                    BroadcastSessionInfo(session = session)
                } ?: Text(
                    text = "当前无活跃广播会话",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (broadcastState.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = broadcastState.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = Colors.P0Color
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 控制按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!broadcastState.isBroadcasting) {
                        Button(
                            onClick = { onIntent(Android16AuracastIntent.StartBroadcast) },
                            modifier = Modifier.weight(1f),
                            enabled = broadcastState.status != BroadcastStatus.STARTING
                        ) {
                            if (broadcastState.status == BroadcastStatus.STARTING) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Icon(Icons.Default.Radio, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (broadcastState.status == BroadcastStatus.STARTING) "启动中..." else "开始广播")
                        }
                    } else {
                        // 广播中控制按钮
                        OutlinedButton(
                            onClick = {
                                if (broadcastState.status == BroadcastStatus.PAUSED) {
                                    onIntent(Android16AuracastIntent.ResumeBroadcast)
                                } else {
                                    onIntent(Android16AuracastIntent.PauseBroadcast)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (broadcastState.status == BroadcastStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (broadcastState.status == BroadcastStatus.PAUSED) "恢复" else "暂停")
                        }
                        Button(
                            onClick = { onIntent(Android16AuracastIntent.StopBroadcast) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Colors.P0Color)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("停止")
                        }
                    }
                }
            }
        }

        // 广播动画（仅在广播中显示）
        AnimatedVisibility(
            visible = broadcastState.isBroadcasting && broadcastState.status == BroadcastStatus.BROADCASTING,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            BroadcastAnimationCard()
        }
    }
}

@Composable
private fun StatusBadge(status: BroadcastStatus) {
    val (color, text) = when (status) {
        BroadcastStatus.IDLE -> Colors.P2Color to "空闲"
        BroadcastStatus.STARTING -> Colors.P1Color to "启动中"
        BroadcastStatus.BROADCASTING -> Color(0xFF2E7D32) to "广播中"
        BroadcastStatus.PAUSED -> Colors.P1Color to "已暂停"
        BroadcastStatus.ERROR -> Colors.P0Color to "错误"
    }

    Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.15f)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun BroadcastSessionInfo(session: BroadcastSession) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        InfoRow("会话 ID", session.sessionId.take(16) + "...")
        InfoRow("广播名称", session.name)
        InfoRow("广播 ID", session.broadcastId)
        InfoRow("开始时间", java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(session.startedAt)))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BroadcastAnimationCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "broadcast")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                // 扩散波纹
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.3f))
                )
                // 麦克风图标
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "正在广播音频...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

// ============================================================
// Auracast Receiver Tab / 广播接收模块
// ============================================================

@Composable
private fun AuracastReceiverTab(
    state: Android16AuracastState,
    onIntent: (Android16AuracastIntent) -> Unit
) {
    val receiverState = state.auracastReceiverState

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // 扫描控制卡片
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Auracast 广播接收",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "扫描附近的 LE Audio Auracast 广播源并接收音频流",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (receiverState.isScanning) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "正在扫描...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { onIntent(Android16AuracastIntent.StopScanning) }) {
                            Text("停止")
                        }
                    }
                } else {
                    Button(
                        onClick = { onIntent(Android16AuracastIntent.StartScanning) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始扫描")
                    }
                }
            }
        }

        // 已连接的源
        val connectedSource = receiverState.availableSources.find { it.isConnected }
        if (connectedSource != null) {
            ConnectedSourceCard(
                source = connectedSource,
                onDisconnect = { onIntent(Android16AuracastIntent.DisconnectSource) }
            )
        }

        // 可用源列表
        if (receiverState.availableSources.isNotEmpty()) {
            Text(
                text = "可用广播源（${receiverState.availableSources.size}）",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            receiverState.availableSources.forEach { source ->
                SourceCard(
                    source = source,
                    isConnected = source.isConnected,
                    onConnect = { onIntent(Android16AuracastIntent.ConnectToSource(source.sourceId)) }
                )
            }
        } else if (!receiverState.isScanning) {
            EmptySourcesCard()
        }
    }
}

@Composable
private fun SourceCard(
    source: AuracastSource,
    isConnected: Boolean,
    onConnect: () -> Unit
) {
    val signalBars = when {
        source.signalStrength >= -50 -> 4
        source.signalStrength >= -60 -> 3
        source.signalStrength >= -70 -> 2
        else -> 1
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 信号强度图标
            Icon(
                imageVector = when (signalBars) {
                    4 -> Icons.Default.SignalCellular4Bar
                    3 -> Icons.Default.NetworkCell
                    2 -> Icons.Default.NetworkCell
                    else -> Icons.Default.SignalCellularAlt
                },
                contentDescription = null,
                tint = when (signalBars) {
                    4 -> Color(0xFF4CAF50)
                    3 -> Color(0xFF8BC34A)
                    2 -> Colors.P1Color
                    else -> Colors.P2Color
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = source.deviceName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isConnected) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "已连接",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color(0xFF2E7D32),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onConnect,
                    enabled = !source.isConnected
                ) {
                    Text("连接", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ConnectedSourceCard(
    source: AuracastSource,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Headphones,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "正在接收",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
                TextButton(onClick = onDisconnect) {
                    Text("断开", color = Colors.P0Color)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = source.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = source.deviceName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptySourcesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "点击「开始扫描」发现附近的 Auracast 源",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============================================================
// Outline Text Tab / Outline Text 无障碍 API 模块
// ============================================================

@Composable
private fun OutlineTextTab(
    state: Android16AuracastState,
    onIntent: (Android16AuracastIntent) -> Unit,
    isSupported: Boolean
) {
    val outlineTextState = state.outlineTextState

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        if (!isSupported) {
            // Android 16 以下显示版本提示
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Colors.P1Color.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Colors.P1Color,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Outline Text API 需要 Android 16（VanillaIceCream）。当前设备：Android ${Build.VERSION.SDK_INT}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Colors.P1Color
                    )
                }
            }
        }

        // 样式选择器
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Outline Text 样式",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlineTextStyle.entries.forEach { style ->
                        FilterChip(
                            selected = outlineTextState.currentStyle == style,
                            onClick = { onIntent(Android16AuracastIntent.UpdateOutlineStyle(style)) },
                            label = {
                                Text(
                                    when (style) {
                                        OutlineTextStyle.Default -> "Default"
                                        OutlineTextStyle.Highlight -> "Highlight"
                                        OutlineTextStyle.Underline -> "Underline"
                                    },
                                    fontSize = 12.sp
                                )
                            },
                            enabled = isSupported
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "自动无障碍检测",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = outlineTextState.autoAccessible,
                        onCheckedChange = { onIntent(Android16AuracastIntent.ToggleAutoAccessible(it)) },
                        enabled = isSupported
                    )
                }
            }
        }

        // 文本输入
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "文本内容",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = outlineTextState.textInput,
                    onValueChange = { onIntent(Android16AuracastIntent.UpdateOutlineText(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("输入要展示的文字...") },
                    maxLines = 3,
                    enabled = isSupported
                )
            }
        }

        // 预览卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "预览",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Outline Text Composable 演示
                OutlineTextDemo(
                    text = outlineTextState.textInput.ifBlank { "Hello Android 16" },
                    style = outlineTextState.currentStyle,
                    autoAccessible = outlineTextState.autoAccessible,
                    isSupported = isSupported
                )
            }
        }

        // API 说明
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "API 说明",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val codeExample = """
                    |@Composable
                    |fun OutlineTextDemo() {
                    |    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    |        OutlineText(
                    |            text = "Important Label",
                    |            style = OutlineTextStyle.Default,
                    |            autoAccessibility = true
                    |        )
                    |    }
                    |}
                """.trimMargin()

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E1E1E)
                ) {
                    Text(
                        text = codeExample,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Color(0xFFD4D4D4)
                    )
                }
            }
        }
    }
}

/**
 * OutlineText — Android 16 无障碍文字组件
 *
 * Android 16 引入的药丸形背景 + 文字样式，用于提高低视力用户的阅读体验。
 * 带版本守卫，仅在 VANILLA_ICE_CREAM 及以上版本可用。
 *
 * @param text 显示的文本
 * @param style 样式（Default / Highlight / Underline）
 * @param autoAccessible 是否自动检测高对比度需求
 * @param modifier Compose Modifier
 */
@Composable
private fun OutlineTextDemo(
    text: String,
    style: OutlineTextStyle,
    autoAccessible: Boolean,
    isSupported: Boolean
) {
    if (!isSupported) {
        // 版本不支持时显示降级版本
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    // 版本守卫：Build.VERSION_CODES.VANILLA_ICE_CREAM = 36
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    // Android 16+ 完整实现
    val backgroundColor = when (style) {
        OutlineTextStyle.Default -> Colors.OutlineTextBg
        OutlineTextStyle.Highlight -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        OutlineTextStyle.Underline -> Color.Transparent
    }

    val textColor = when (style) {
        OutlineTextStyle.Default -> MaterialTheme.colorScheme.onSurface
        OutlineTextStyle.Highlight -> MaterialTheme.colorScheme.primary
        OutlineTextStyle.Underline -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (style) {
            OutlineTextStyle.Default -> {
                // 药丸形背景，文字下方（Android 16 默认样式）
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor
                    )
                }
            }
            OutlineTextStyle.Highlight -> {
                // 纯色高亮
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = backgroundColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = text,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
            }
            OutlineTextStyle.Underline -> {
                // 下划线强调
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(3.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }

        if (autoAccessible) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    Icons.Default.Accessibility,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "自动无障碍已启用",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

// ============================================================
// Local Network Permission Tab / Local Network 权限引导模块
// ============================================================

@Composable
private fun LocalNetworkTab(
    state: Android16AuracastState,
    onIntent: (Android16AuracastIntent) -> Unit,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    val localNetworkState = state.localNetworkState

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        when {
            localNetworkState.showIntro -> {
                // 权限说明页
                PermissionIntroPage(
                    onDismiss = { onIntent(Android16AuracastIntent.DismissPermissionIntro) },
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_WIFI_STATE
                            )
                        )
                        onIntent(Android16AuracastIntent.RequestLocalNetworkPermission)
                    }
                )
            }
            localNetworkState.showDeniedPage -> {
                // 拒绝后的引导恢复页
                PermissionDeniedPage(
                    onOpenSettings = { onIntent(Android16AuracastIntent.OpenAppSettings) }
                )
            }
            else -> {
                // 主权限状态页
                LocalNetworkPermissionGate(
                    permissionState = localNetworkState.permissionState,
                    onShowIntro = { onIntent(Android16AuracastIntent.ShowPermissionIntro) },
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_WIFI_STATE
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun LocalNetworkPermissionGate(
    permissionState: PermissionState,
    onShowIntro: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Local Network 权限状态",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val (stateColor, stateText, stateIcon) = when (permissionState) {
                    PermissionState.NOT_DETERMINED -> Triple(Colors.P2Color, "未确定（未申请）", Icons.Default.HourglassEmpty)
                    PermissionState.GRANTED -> Triple(Color(0xFF2E7D32), "已授权", Icons.Default.CheckCircle)
                    PermissionState.DENIED -> Triple(Colors.P1Color, "被拒绝", Icons.Default.Cancel)
                    PermissionState.DENIED_FOREVER -> Triple(Colors.P0Color, "永久拒绝", Icons.Default.Block)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(stateIcon, contentDescription = null, tint = stateColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = stateColor,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = when (permissionState) {
                                PermissionState.NOT_DETERMINED -> "应用尚未请求 Local Network 权限"
                                PermissionState.GRANTED -> "应用可以访问本地网络设备"
                                PermissionState.DENIED -> "用户拒绝了权限，可以重试"
                                PermissionState.DENIED_FOREVER -> "需手动到系统设置开启"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (permissionState) {
                    PermissionState.NOT_DETERMINED -> {
                        Button(onClick = onShowIntro, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("申请权限")
                        }
                    }
                    PermissionState.GRANTED -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Local Network 权限已就绪，可以开始网络发现",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                    PermissionState.DENIED, PermissionState.DENIED_FOREVER -> {
                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("重试")
                        }
                    }
                }
            }
        }

        // 说明文档
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "为什么需要 Local Network 权限？",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Android 16 强制执行 Local Network 权限要求。任何在本地网络上进行网络活动的应用都必须声明并获取 ACCESS_FINE_LOCATION 权限。这是保护用户隐私的重要变更。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "涉及场景：DLNA/UPnP 媒体发现、SmartConfig、IoT 设备配网、局域网文件传输、Auracast 接收",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PermissionIntroPage(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Wifi,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "需要 Local Network 权限",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "为了发现并连接附近的音频设备（如 Auracast 扬声器、智能家居设备），我们需要访问你的本地网络。\n\n此权限仅用于局域网设备发现，不会访问互联网。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("取消")
                }
                Button(onClick = onRequestPermission, modifier = Modifier.weight(1f)) {
                    Text("授权")
                }
            }
        }
    }
}

@Composable
private fun PermissionDeniedPage(onOpenSettings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Colors.P0Color.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Block,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Colors.P0Color
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "权限被拒绝",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Colors.P0Color
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Local Network 权限被拒绝。部分功能可能无法正常使用。\n\n如果你希望启用此功能，请在系统设置中手动开启权限。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Colors.P0Color)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("打开系统设置")
            }
        }
    }
}

// ============================================================
// Behavior Change Tab / 行为变更速查面板
// ============================================================

@Composable
private fun BehaviorChangeTab(
    state: Android16AuracastState,
    onIntent: (Android16AuracastIntent) -> Unit,
    filteredChanges: List<BehaviorChange>
) {
    val behaviorChangeState = state.behaviorChangeState

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // 版本 Tab 切换
        VersionTabRow(
            selectedVersion = behaviorChangeState.selectedVersionTab,
            onSelectVersion = { onIntent(Android16AuracastIntent.SelectVersionTab(it)) }
        )

        // 搜索栏
        OutlinedTextField(
            value = behaviorChangeState.searchQuery,
            onValueChange = { onIntent(Android16AuracastIntent.UpdateSearchQuery(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索行为变更...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (behaviorChangeState.searchQuery.isNotBlank()) {
                    IconButton(onClick = { onIntent(Android16AuracastIntent.UpdateSearchQuery("")) }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // 严重程度过滤
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SeverityBadge(
                label = "全部",
                color = Colors.P2Color,
                isSelected = behaviorChangeState.selectedSeverity == null,
                onClick = { onIntent(Android16AuracastIntent.FilterBySeverity(null)) }
            )
            Severity.entries.forEach { severity ->
                SeverityBadge(
                    label = severity.name,
                    color = Color(severity.color),
                    isSelected = behaviorChangeState.selectedSeverity == severity,
                    onClick = { onIntent(Android16AuracastIntent.FilterBySeverity(severity)) }
                )
            }
        }

        // 统计摘要
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "共 ${filteredChanges.size} 条变更",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (behaviorChangeState.expandedChangeId != null) {
                TextButton(onClick = { onIntent(Android16AuracastIntent.CollapseAllChanges) }) {
                    Text("收起全部", fontSize = 12.sp)
                }
            }
        }

        // 变更列表
        filteredChanges.forEach { change ->
            BehaviorChangeCard(
                change = change,
                isExpanded = behaviorChangeState.expandedChangeId == change.id,
                onExpand = { onIntent(Android16AuracastIntent.ExpandChange(change.id)) }
            )
        }

        if (filteredChanges.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "没有找到匹配的行为变更",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionTabRow(
    selectedVersion: String,
    onSelectVersion: (String) -> Unit
) {
    val versions = listOf("Android 16", "Android 15", "Android 14")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        versions.forEach { version ->
            FilterChip(
                selected = selectedVersion == version,
                onClick = { onSelectVersion(version) },
                label = { Text(version, fontSize = 12.sp) },
                leadingIcon = if (selectedVersion == version) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun SeverityBadge(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
        border = if (!isSelected) BorderStroke(1.dp, color.copy(alpha = 0.5f)) else null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun BehaviorChangeCard(
    change: BehaviorChange,
    isExpanded: Boolean,
    onExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 顶部行：严重程度 + 类别
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SeverityBadge(
                        label = change.severity.name,
                        color = Color(change.severity.color),
                        isSelected = true,
                        onClick = { }
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = change.category.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 标题
            Text(
                text = change.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 描述
            Text(
                text = change.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            // 展开内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // 迁移指南
                    if (change.migrationGuide.isNotBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = change.migrationGuide,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 受影响文件
                    if (change.affectedFiles.isNotEmpty()) {
                        Text(
                            text = "受影响文件",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        change.affectedFiles.forEach { file ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = file,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 文档链接
                    if (change.docUrl.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = change.docUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Migration Tab / 迁移检测模块
// ============================================================

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MigrationTab(
    state: Android16AuracastState,
    onIntent: (Android16AuracastIntent) -> Unit
) {
    val migrationState = state.migrationState

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // 项目路径配置卡片
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "迁移检测配置",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = migrationState.projectPath,
                    onValueChange = { onIntent(Android16AuracastIntent.UpdateProjectPath(it)) },
                    label = { Text("项目路径") },
                    placeholder = { Text("/path/to/your/android/project") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !migrationState.isScanning
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 目标版本选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "目标版本",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { if (!migrationState.isScanning) expanded = it }
                    ) {
                        OutlinedTextField(
                            value = migrationState.targetVersion,
                            onValueChange = { },
                            readOnly = true,
                            modifier = Modifier.menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            enabled = !migrationState.isScanning
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("Android 16", "Android 15", "Android 14").forEach { version ->
                                DropdownMenuItem(
                                    text = { Text(version) },
                                    onClick = {
                                        onIntent(Android16AuracastIntent.UpdateTargetVersion(version))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 扫描进度卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (migrationState.isScanning)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "扫描进度",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (migrationState.isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (migrationState.isScanning) {
                    LinearProgressIndicator(
                        progress = { migrationState.scanProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "正在扫描: ${migrationState.currentFile}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { onIntent(Android16AuracastIntent.CancelMigrationScan) }) {
                        Text("取消扫描")
                    }
                } else {
                    Button(
                        onClick = { onIntent(Android16AuracastIntent.StartMigrationScan) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = migrationState.projectPath.isNotBlank()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始扫描")
                    }
                }

                if (migrationState.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = migrationState.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = Colors.P0Color
                    )
                }
            }
        }

        // 扫描结果汇总
        if (migrationState.behaviorChanges.isNotEmpty()) {
            val p0Count = migrationState.behaviorChanges.count { it.severity == Severity.P0 }
            val p1Count = migrationState.behaviorChanges.count { it.severity == Severity.P1 }
            val p2Count = migrationState.behaviorChanges.count { it.severity == Severity.P2 }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "扫描结果汇总",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SeverityCountChip("P0", p0Count, Colors.P0Color)
                        SeverityCountChip("P1", p1Count, Colors.P1Color)
                        SeverityCountChip("P2", p2Count, Colors.P2Color)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onIntent(Android16AuracastIntent.ExportMigrationReport) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("导出迁移报告")
                    }
                }
            }

            // 扫描发现的行为变更列表
            Text(
                text = "扫描发现的行为变更",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            migrationState.behaviorChanges.forEach { change ->
                BehaviorChangeCard(
                    change = change,
                    isExpanded = false,
                    onExpand = { }
                )
            }
        }
    }
}

@Composable
private fun SeverityCountChip(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.15f)) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================
// End of Android16AuracastScreen
// ============================================================
