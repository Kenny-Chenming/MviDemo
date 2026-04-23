package com.mvi.kenny.feature.paging35

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

/**
 * ============================================================
 * PagingDebugScreen — PagingSource 调试面板屏幕
 * ============================================================
 * PRD-093 | Paging 3.5 `asState` 操作符开发工具包
 *
 * 功能：
 * - 实时状态区：LoadState / 页码 / 已加载条目数
 * - 模拟触发区：模拟加载下一页/失败/刷新
 * - 调用链追踪：最近 10 次 LoadState 变化时序图
 *
 * @param viewModel PagingDebugViewModel 实例
 * @param onNavigateBack 返回上一级回调
 *
 * @author 开心果 🥜
 */
@Composable
fun PagingDebugScreen(
    viewModel: PagingDebugViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PagingDebugPanelEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PagingSource 调试面板") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.processIntent(PagingDebugPanelIntent.ClearHistory) }
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "清除历史")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ============================================================
            // 实时状态区
            // ============================================================

            item {
                RealTimeStatusCard(state = state)
            }

            // ============================================================
            // PagingData 流向可视化
            // ============================================================

            item {
                val debugLoadState = when (state.loadState) {
                    LoadStateDebug.LOADING, LoadStateDebug.REFRESHING, LoadStateDebug.APPENDING -> LoadState.LOADING
                    LoadStateDebug.ERROR -> LoadState.ERROR
                    else -> LoadState.SUCCESS
                }
                PagingFlowDiagram(
                    currentPage = state.currentPage,
                    totalItems = state.totalItems,
                    loadState = debugLoadState
                )
            }

            // ============================================================
            // 模拟触发区
            // ============================================================

            item {
                SimulationControlCard(
                    isSimulating = state.isSimulating,
                    onSimulateNextPage = { viewModel.processIntent(PagingDebugPanelIntent.SimulateLoadNextPage) },
                    onSimulateError = { viewModel.processIntent(PagingDebugPanelIntent.SimulateLoadError) },
                    onSimulateRefresh = { viewModel.processIntent(PagingDebugPanelIntent.SimulateRefresh) },
                    onReset = { viewModel.processIntent(PagingDebugPanelIntent.ResetState) }
                )
            }

            // ============================================================
            // 调用链追踪（时序图）
            // ============================================================

            item {
                Text(
                    text = "调用链追踪",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.loadStateHistory.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Timeline,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "点击上方按钮模拟操作",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "LoadState 变化将显示在这里",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = state.loadStateHistory,
                    key = { _, event -> event.id }
                ) { index, event ->
                    LoadStateTimelineItem(
                        event = event,
                        isLatest = index == 0,
                        totalEvents = state.loadStateHistory.size
                    )
                }
            }
        }
    }
}

// ============================================================
// Sub-components / 子组件
// ============================================================

/**
 * 实时状态卡片
 *
 * @param state 当前调试状态
 *
 * @author 开心果 🥜
 */
@Composable
private fun RealTimeStatusCard(state: PagingDebugPanelState) {
    val stateColor = when (state.loadState) {
        LoadStateDebug.LOADING, LoadStateDebug.APPENDING -> Color(0xFFFF9800)
        LoadStateDebug.ERROR -> Color(0xFFF44336)
        LoadStateDebug.SUCCESS, LoadStateDebug.IDLE -> Color(0xFF4CAF50)
        LoadStateDebug.REFRESHING -> Color(0xFF2196F3)
    }

    // Pulse animation for active states / 活跃状态脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val displayAlpha = if (state.loadState in listOf(
            LoadStateDebug.LOADING,
            LoadStateDebug.REFRESHING,
            LoadStateDebug.APPENDING
        )
    ) pulseAlpha else 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "实时状态",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // State indicator with pulse / 带脉冲的状态指示
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(stateColor.copy(alpha = displayAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.loadState.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = stateColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row / 统计行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DebugStatItem(
                    label = "当前页码",
                    value = "${state.currentPage}",
                    icon = Icons.Default.Numbers
                )
                DebugStatItem(
                    label = "已加载条目",
                    value = "${state.totalItems}",
                    icon = Icons.Default.Inventory
                )
                DebugStatItem(
                    label = "历史事件",
                    value = "${state.loadStateHistory.size}",
                    icon = Icons.Default.History
                )
            }

            // Error message / 错误信息
            state.lastError?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 模拟控制卡片
 *
 * @param isSimulating 是否正在模拟
 * @param onSimulateNextPage 模拟下一页
 * @param onSimulateError 模拟失败
 * @param onSimulateRefresh 模拟刷新
 * @param onReset 重置状态
 *
 * @author 开心果 🥜
 */
@Composable
private fun SimulationControlCard(
    isSimulating: Boolean,
    onSimulateNextPage: () -> Unit,
    onSimulateError: () -> Unit,
    onSimulateRefresh: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "模拟触发区",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "点击按钮模拟 PagingSource 的各种加载场景",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons / 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSimulateNextPage,
                    modifier = Modifier.weight(1f),
                    enabled = !isSimulating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    if (isSimulating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.NavigateNext, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("下一页", fontSize = 13.sp)
                }

                Button(
                    onClick = onSimulateError,
                    modifier = Modifier.weight(1f),
                    enabled = !isSimulating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("失败", fontSize = 13.sp)
                }

                Button(
                    onClick = onSimulateRefresh,
                    modifier = Modifier.weight(1f),
                    enabled = !isSimulating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("刷新", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSimulating
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("重置状态", fontSize = 13.sp)
            }
        }
    }
}

/**
 * LoadState 时序图条目
 *
 * @param event LoadState 事件
 * @param isLatest 是否最新事件
 * @param totalEvents 总事件数
 *
 * @author 开心果 🥜
 */
@Composable
private fun LoadStateTimelineItem(
    event: LoadStateEvent,
    isLatest: Boolean,
    totalEvents: Int
) {
    val eventColor = when (event.loadState) {
        LoadStateDebug.LOADING, LoadStateDebug.REFRESHING, LoadStateDebug.APPENDING -> Color(0xFFFF9800)
        LoadStateDebug.ERROR -> Color(0xFFF44336)
        LoadStateDebug.SUCCESS -> Color(0xFF4CAF50)
        LoadStateDebug.IDLE -> Color(0xFF9E9E9E)
    }

    val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        .format(Date(event.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLatest) eventColor.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timeline dot / 时间线点
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Event indicator / 事件指示器
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(eventColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Event info / 事件信息
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = event.loadState.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isLatest) FontWeight.Bold else FontWeight.Normal,
                        color = eventColor
                    )
                    Text(
                        text = "页: ${event.page} | 条目: ${event.totalItemsLoaded}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
