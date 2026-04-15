package com.mvi.kenny.feature.paging35

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ============================================================
 * Paging35SubScreens — Paging 3.5 子界面
 * ============================================================
 * Contains Migration Report, Debug Panel, and Troubleshooting screens.
 */

// =============================================================
// MigrationReportScreen — 迁移检测报告
// =============================================================

/**
 * Migration Report Screen / 迁移检测报告
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrationReportScreen(
    viewModel: Paging35ViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.migrationState.collectAsState()

    // Generate report on first load / 首次加载时生成报告
    LaunchedEffect(Unit) {
        if (state.filesToMigrate.isEmpty()) {
            viewModel.processMigrationIntent(MigrationReportIntent.GenerateReport)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Migration Report / 迁移报告") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.processMigrationIntent(MigrationReportIntent.ExportMarkdown) }) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Overview Section / 概览区
            item {
                MigrationOverviewCard(
                    totalFiles = state.totalFiles,
                    directReplaceCount = state.directReplaceCount,
                    needsAdjustmentCount = state.needsAdjustmentCount,
                    needsRefactorCount = state.needsRefactorCount,
                    workloadEstimate = state.workloadEstimate
                )
            }

            // Filter Chips / 过滤芯片
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.filterCompatibility == null,
                        onClick = { viewModel.processMigrationIntent(MigrationReportIntent.FilterByCompatibility(null)) },
                        label = { Text("All / 全部") }
                    )
                    FilterChip(
                        selected = state.filterCompatibility == CompatibilityLevel.DIRECT_REPLACE,
                        onClick = { viewModel.processMigrationIntent(MigrationReportIntent.FilterByCompatibility(CompatibilityLevel.DIRECT_REPLACE)) },
                        label = { Text("🟢 Direct") },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                        )
                    )
                    FilterChip(
                        selected = state.filterCompatibility == CompatibilityLevel.NEEDS_ADJUSTMENT,
                        onClick = { viewModel.processMigrationIntent(MigrationReportIntent.FilterByCompatibility(CompatibilityLevel.NEEDS_ADJUSTMENT)) },
                        label = { Text("🟡 Adjust") },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF9800).copy(alpha = 0.2f)
                        )
                    )
                    FilterChip(
                        selected = state.filterCompatibility == CompatibilityLevel.NEEDS_REFACTOR,
                        onClick = { viewModel.processMigrationIntent(MigrationReportIntent.FilterByCompatibility(CompatibilityLevel.NEEDS_REFACTOR)) },
                        label = { Text("🔴 Refactor") },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF44336).copy(alpha = 0.2f)
                        )
                    )
                }
            }

            // Migration File Cards / 迁移文件卡片
            if (state.isGeneratingReport) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(state.filteredFiles) { file ->
                    MigrationFileCard(
                        file = file,
                        onIgnore = { viewModel.processMigrationIntent(MigrationReportIntent.IgnoreFile(file.filePath)) },
                        onSelect = { viewModel.processMigrationIntent(MigrationReportIntent.SelectFile(file)) }
                    )
                }
            }
        }
    }
}

/**
 * Migration Overview Card / 迁移概览卡片
 */
@Composable
private fun MigrationOverviewCard(
    totalFiles: Int,
    directReplaceCount: Int,
    needsAdjustmentCount: Int,
    needsRefactorCount: Int,
    workloadEstimate: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Overview / 概览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewItem(
                    emoji = "📁",
                    value = totalFiles.toString(),
                    label = "Total / 总数"
                )
                OverviewItem(
                    emoji = "🟢",
                    value = directReplaceCount.toString(),
                    label = "Direct / 直接"
                )
                OverviewItem(
                    emoji = "🟡",
                    value = needsAdjustmentCount.toString(),
                    label = "Adjust / 调整"
                )
                OverviewItem(
                    emoji = "🔴",
                    value = needsRefactorCount.toString(),
                    label = "Refactor / 重构"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workload Estimate / 工作量估算:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = workloadEstimate,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        needsRefactorCount > 3 -> Color(0xFFF44336)
                        needsAdjustmentCount > 5 -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                )
            }
        }
    }
}

/**
 * Overview Item / 概览项
 */
@Composable
private fun OverviewItem(
    emoji: String,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 24.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Migration File Card / 迁移文件卡片
 */
@Composable
private fun MigrationFileCard(
    file: MigrationFile,
    onIgnore: () -> Unit,
    onSelect: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Compatibility Level Indicator / 兼容性等级指示器
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(file.compatibilityLevel.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = file.compatibilityLevel.emoji,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.filePath.substringAfterLast("/"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = file.filePath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                SeverityChip(level = file.compatibilityLevel)
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // Current API / 当前 API
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Current / 当前:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.width(80.dp)
                    )
                    Text(
                        text = file.currentApi,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFFF44336)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Suggested API / 建议 API
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Suggested / 建议:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.width(80.dp)
                    )
                    Text(
                        text = file.suggestedApi,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFF4CAF50)
                    )
                }

                // Issues / 问题
                if (file.issues.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Issues / 问题:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    file.issues.forEach { issue ->
                        Text(
                            text = "• $issue",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions / 操作
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onIgnore) {
                        Text("Ignore / 忽略")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSelect) {
                        Text("View Details / 查看详情")
                    }
                }
            }
        }
    }
}

/**
 * Severity Chip / 严重程度芯片
 */
@Composable
private fun SeverityChip(level: CompatibilityLevel) {
    Box(
        modifier = Modifier
            .background(
                level.color.copy(alpha = 0.2f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${level.emoji} ${level.labelZh}",
            style = MaterialTheme.typography.labelSmall,
            color = level.color
        )
    }
}

// =============================================================
// PagingDebugPanelScreen — PagingSource 调试面板
// =============================================================

/**
 * Paging Debug Panel Screen / PagingSource 调试面板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagingDebugPanelScreen(
    viewModel: Paging35ViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.debugState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PagingSource Debug / 调试面板") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Real-time Status / 实时状态
            item {
                RealTimeStatusCard(state = state)
            }

            // PagingData Flow Diagram / PagingData 流向图
            item {
                PagingFlowDiagram(state = state)
            }

            // Simulation Buttons / 模拟触发按钮
            item {
                SimulationButtonsPanel(
                    isSimulating = state.isSimulating,
                    onSimulateLoadNextPage = { viewModel.processDebugIntent(PagingDebugPanelIntent.SimulateLoadNextPage) },
                    onSimulateLoadError = { viewModel.processDebugIntent(PagingDebugPanelIntent.SimulateLoadError) },
                    onSimulateRefresh = { viewModel.processDebugIntent(PagingDebugPanelIntent.SimulateRefresh) },
                    onResetState = { viewModel.processDebugIntent(PagingDebugPanelIntent.ResetState) }
                )
            }

            // Load State Timeline / 调用链追踪
            item {
                LoadStateTimelineChart(history = state.loadStateHistory)
            }
        }
    }
}

/**
 * Real-time Status Card / 实时状态卡片
 */
@Composable
private fun RealTimeStatusCard(state: PagingDebugPanelState) {
    val animatedColor by animateColorAsState(
        targetValue = state.loadState.color,
        animationSpec = tween(durationMillis = 200),
        label = "load_state_color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Real-time Status / 实时状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Load State Indicator / LoadState 指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(animatedColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (state.loadState) {
                            LoadState.LOADING -> Icons.Default.Refresh
                            LoadState.ERROR -> Icons.Default.Error
                            LoadState.SUCCESS -> Icons.Default.Check
                            LoadState.IDLE -> Icons.Default.BugReport
                        },
                        contentDescription = null,
                        tint = animatedColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = state.loadState.label,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = animatedColor
                    )
                    Text(
                        text = when (state.loadState) {
                            LoadState.LOADING -> "Loading... / 加载中..."
                            LoadState.ERROR -> "Error occurred / 发生错误"
                            LoadState.SUCCESS -> "Success / 成功"
                            LoadState.IDLE -> "Idle / 空闲"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row / 统计行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DebugStatItem(
                    label = "Current Page / 当前页",
                    value = state.currentPage.toString()
                )
                DebugStatItem(
                    label = "Total Items / 总 Item",
                    value = state.totalItems.toString()
                )
                DebugStatItem(
                    label = "Refresh Count / 刷新次数",
                    value = state.refreshTriggerTimes.toString()
                )
            }
        }
    }
}

/**
 * Debug Stat Item / 调试统计项
 */
@Composable
private fun DebugStatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Paging Flow Diagram / PagingData 流向可视化
 */
@Composable
private fun PagingFlowDiagram(state: PagingDebugPanelState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "PagingData Flow / PagingData 流向",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Flow diagram visualization / 流向图可视化
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowNode(
                    label = "PagingSource",
                    subLabel = "数据源",
                    isActive = state.loadState == LoadState.LOADING,
                    activeColor = state.loadState.color
                )

                FlowArrow(isActive = state.loadState != LoadState.IDLE)

                FlowNode(
                    label = "PagingData",
                    subLabel = "分页数据",
                    isActive = state.loadState == LoadState.SUCCESS,
                    activeColor = Color(0xFF4CAF50)
                )

                FlowArrow(isActive = state.loadState == LoadState.SUCCESS)

                FlowNode(
                    label = "UI State",
                    subLabel = "UI 状态",
                    isActive = state.loadState == LoadState.SUCCESS,
                    activeColor = Color(0xFF4CAF50)
                )
            }
        }
    }
}

/**
 * Flow Node / 流向节点
 */
@Composable
private fun FlowNode(
    label: String,
    subLabel: String,
    isActive: Boolean,
    activeColor: Color
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isActive) activeColor else Color.Gray,
        animationSpec = tween(durationMillis = 200),
        label = "flow_node_color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(animatedColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (isActive) 50.dp else 40.dp)
                    .clip(CircleShape)
                    .background(animatedColor)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = subLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

/**
 * Flow Arrow / 流向箭头
 */
@Composable
private fun FlowArrow(isActive: Boolean) {
    val animatedColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF4CAF50) else Color.Gray,
        animationSpec = tween(durationMillis = 200),
        label = "flow_arrow_color"
    )

    Canvas(modifier = Modifier.size(40.dp, 20.dp)) {
        drawLine(
            color = animatedColor,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 3f
        )
        // Arrow head / 箭头
        drawLine(
            color = animatedColor,
            start = Offset(size.width - 10f, size.height / 2 - 8f),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 3f
        )
        drawLine(
            color = animatedColor,
            start = Offset(size.width - 10f, size.height / 2 + 8f),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 3f
        )
    }
}

/**
 * Simulation Buttons Panel / 模拟触发区
 */
@Composable
private fun SimulationButtonsPanel(
    isSimulating: Boolean,
    onSimulateLoadNextPage: () -> Unit,
    onSimulateLoadError: () -> Unit,
    onSimulateRefresh: () -> Unit,
    onResetState: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Simulation / 模拟触发",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSimulateLoadNextPage,
                    enabled = !isSimulating,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Next Page")
                }

                Button(
                    onClick = onSimulateLoadError,
                    enabled = !isSimulating,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Icon(Icons.Default.Error, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Error")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSimulateRefresh,
                    enabled = !isSimulating,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh")
                }

                OutlinedButton(
                    onClick = onResetState,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }
            }

            if (isSimulating) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

/**
 * Load State Timeline Chart / 调用链追踪时序图
 */
@Composable
private fun LoadStateTimelineChart(history: List<LoadStateEvent>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "LoadState History (Last 10) / LoadState 历史 (最近10次)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (history.isEmpty()) {
                Text(
                    text = "No events yet. Trigger a simulation to see history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "暂无事件。触发模拟操作以查看历史。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                history.reversed().forEach { event ->
                    LoadStateEventItem(event = event)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Load State Event Item / LoadState 事件项
 */
@Composable
private fun LoadStateEventItem(event: LoadStateEvent) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(event.loadState.color)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = event.loadState.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = event.loadState.color,
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = "Page: ${event.page} | Items: ${event.itemCount}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = dateFormat.format(Date(event.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// =============================================================
// PagingTroubleshootingScreen — 踩坑排查工具
// =============================================================

/**
 * Paging Troubleshooting Screen / 踩坑排查工具
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagingTroubleshootingScreen(
    viewModel: Paging35ViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.troubleshootingState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Troubleshooting / 踩坑排查") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tab Row / Tab 行
            item {
                TabRow(
                    selectedTabIndex = state.selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    state.tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = state.selectedTabIndex == index,
                            onClick = { viewModel.processTroubleshootingIntent(PagingTroubleshootingIntent.SelectTab(index)) },
                            text = {
                                Text(
                                    text = title.substringBefore(" /"),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }

            // Auto Scan Toggle / 自动扫描开关
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Auto Scan / 自动扫描",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Scan project for issues / 扫描项目问题",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = state.isAutoScanEnabled,
                            onCheckedChange = { viewModel.processTroubleshootingIntent(PagingTroubleshootingIntent.ToggleAutoScan(it)) }
                        )
                    }

                    if (state.isScanning) {
                        LinearProgressIndicator(
                            progress = { state.scanProgress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Problem Cards / 问题诊断卡片
            items(state.currentTabProblems) { problem ->
                TroubleshootingCard(
                    problem = problem,
                    isExpanded = state.expandedCardId == problem.id,
                    onExpand = { viewModel.processTroubleshootingIntent(PagingTroubleshootingIntent.ExpandCard(problem.id)) },
                    onCollapse = { viewModel.processTroubleshootingIntent(PagingTroubleshootingIntent.CollapseCard) }
                )
            }

            // Scan Results / 扫描结果
            if (state.scanResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Scan Results / 扫描结果",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(state.scanResults) { result ->
                    ScanResultCard(result = result)
                }
            }
        }
    }
}

/**
 * Troubleshooting Card / 踩坑诊断卡片
 */
@Composable
private fun TroubleshootingCard(
    problem: TroubleshootingProblem,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isExpanded) onCollapse() else onExpand() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = problem.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = problem.titleZh,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                // Symptoms / 症状
                Text(
                    text = "Symptoms / 症状",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
                problem.symptoms.forEachIndexed { index, symptom ->
                    Text(
                        text = "• $symptom",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (index < problem.symptomsZh.size) {
                        Text(
                            text = "  ${problem.symptomsZh[index]}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Possible Causes / 可能原因
                Text(
                    text = "Possible Causes / 可能原因",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
                problem.possibleCauses.forEachIndexed { index, cause ->
                    Text(
                        text = "• $cause",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (index < problem.possibleCausesZh.size) {
                        Text(
                            text = "  ${problem.possibleCausesZh[index]}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Solutions / 解决方案
                Text(
                    text = "Solutions / 解决方案",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                problem.solutions.forEachIndexed { index, solution ->
                    Text(
                        text = "• $solution",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (index < problem.solutionsZh.size) {
                        Text(
                            text = "  ${problem.solutionsZh[index]}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Scan Result Card / 扫描结果卡片
 */
@Composable
private fun ScanResultCard(result: ScanResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = result.severity.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.severity.emoji,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.filePath.substringAfterLast("/"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Line ${result.lineNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.issueDescription,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = result.issueDescriptionZh,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}