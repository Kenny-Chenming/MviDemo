package com.mvi.kenny.feature.gridflexboxkit.submodules

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.gridflexboxkit.*

// PerformanceAnalysisScreen — 性能分析面板
// PRD-141 | Grid/FlexBox 性能基准测试
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceAnalysisScreen(
    onBack: () -> Unit,
    viewModel: com.mvi.kenny.feature.gridflexboxkit.PerformanceAnalysisViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is com.mvi.kenny.feature.gridflexboxkit.PerformanceAnalysisEffect.ShowBenchmarkProgress -> {}
                is com.mvi.kenny.feature.gridflexboxkit.PerformanceAnalysisEffect.ShowBenchmarkResult -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("性能分析面板") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 控制面板
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚡ 性能基准测试",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 布局选择
                    Text("选择布局类型:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.mvi.kenny.feature.gridflexboxkit.LayoutType.entries.take(4).forEach { layout ->
                            FilterChip(
                                selected = layout in state.selectedLayouts,
                                onClick = {
                                    val newSet = if (layout in state.selectedLayouts) {
                                        state.selectedLayouts - layout
                                    } else {
                                        state.selectedLayouts + layout
                                    }
                                    viewModel.sendIntent(
                                        com.mvi.kenny.feature.gridflexboxkit.PerformanceAnalysisIntent.SelectLayouts(newSet)
                                    )
                                },
                                label = { Text(layout.displayNameCn, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 指标选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.mvi.kenny.feature.gridflexboxkit.PerformanceMetric.entries.forEach { metric ->
                            FilterChip(
                                selected = metric == state.selectedMetric,
                                onClick = {
                                    viewModel.sendIntent(
                                        com.mvi.kenny.feature.gridflexboxkit.PerformanceAnalysisIntent.SelectMetric(metric)
                                    )
                                },
                                label = { Text(metric.displayNameCn, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 运行测试按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text("测试中...", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Button(
                                onClick = {
                                    viewModel.sendIntent(
                                        com.mvi.kenny.feature.gridflexboxkit.PerformanceAnalysisIntent.RunBenchmark
                                    )
                                },
                                enabled = state.selectedLayouts.isNotEmpty()
                            ) {
                                Icon(androidx.compose.material.icons.Icons.Rounded.Speed, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("运行基准测试")
                            }
                            TextButton(
                                onClick = {
                                    viewModel.sendIntent(
                                        com.mvi.kenny.feature.gridflexboxkit.PerformanceAnalysisIntent.ClearResults
                                    )
                                }
                            ) {
                                Text("清除结果")
                            }
                        }
                    }

                    // 进度条
                    if (state.isRunning) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.benchmarkProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 测试结果
            if (state.testResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📊 测试结果（${state.selectedMetric.displayNameCn}）",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.testResults) { result ->
                                PerformanceResultItem(
                                    result = result,
                                    selectedMetric = state.selectedMetric
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 设备信息
                if (state.testResults.isNotEmpty()) {
                    Text(
                        text = "测试设备: ${state.testResults.firstOrNull()?.deviceInfo ?: "Unknown"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                androidx.compose.material.icons.Icons.Rounded.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "点击「运行基准测试」开始性能对比",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚠️ 注意：重组计数仅在 debug build 中可靠",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceResultItem(
    result: com.mvi.kenny.feature.gridflexboxkit.LayoutPerformanceResult,
    selectedMetric: com.mvi.kenny.feature.gridflexboxkit.PerformanceMetric
) {
    val color = when (result.layoutType) {
        com.mvi.kenny.feature.gridflexboxkit.LayoutType.Grid -> Color(0xFF1E88E5)
        com.mvi.kenny.feature.gridflexboxkit.LayoutType.FlexBox -> Color(0xFF8E24AA)
        com.mvi.kenny.feature.gridflexboxkit.LayoutType.LazyGrid -> Color(0xFF43A047)
        com.mvi.kenny.feature.gridflexboxkit.LayoutType.ColumnRow -> Color(0xFF757575)
    }

    val metricValue = when (selectedMetric) {
        com.mvi.kenny.feature.gridflexboxkit.PerformanceMetric.RecompositionCount -> "${result.recompositionCount} 次"
        com.mvi.kenny.feature.gridflexboxkit.PerformanceMetric.RenderTime -> "${result.renderTimeMs} ms"
        com.mvi.kenny.feature.gridflexboxkit.PerformanceMetric.MemoryUsage -> "%.1f MB".format(result.memoryUsageMb)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = color
                ) {
                    Text(
                        text = result.layoutType.displayNameCn,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Text(
                text = metricValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
