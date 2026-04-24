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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.gridflexboxkit.*

// MigrationScannerScreen — 迁移扫描器屏幕
// PRD-141 | 旧布局 → Grid/FlexBox 迁移扫描器
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrationScannerScreen(
    onBack: () -> Unit,
    viewModel: com.mvi.kenny.feature.gridflexboxkit.MigrationScannerViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is com.mvi.kenny.feature.gridflexboxkit.MigrationScannerEffect.ShowScanComplete -> {}
                is com.mvi.kenny.feature.gridflexboxkit.MigrationScannerEffect.ShowExportSuccess -> {}
                is com.mvi.kenny.feature.gridflexboxkit.MigrationScannerEffect.ShowError -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("迁移扫描器") },
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
            // 扫描控制面板
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔍 旧布局 → Grid/FlexBox 迁移扫描器",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "检测 Column/Row/LazyGrid 用法，建议迁移到 Grid/FlexBox",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 目标目录输入
                    OutlinedTextField(
                        value = state.targetDirectory,
                        onValueChange = {
                            viewModel.sendIntent(
                                com.mvi.kenny.feature.gridflexboxkit.MigrationScannerIntent.UpdateTargetDirectory(it)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("输入目标目录，例如: app/src/main/java") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Rounded.Folder, contentDescription = null)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 扫描按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isScanning) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("扫描中...", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.sendIntent(
                                        com.mvi.kenny.feature.gridflexboxkit.MigrationScannerIntent.StartScan
                                    )
                                }
                            ) {
                                Icon(Icons.Rounded.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("开始扫描")
                            }
                            if (state.scanResults.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.sendIntent(
                                            com.mvi.kenny.feature.gridflexboxkit.MigrationScannerIntent.ExportReport
                                        )
                                    }
                                ) {
                                    Icon(Icons.Rounded.FileDownload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("导出报告")
                                }
                            }
                        }
                    }

                    // 扫描进度
                    if (state.isScanning) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.scanProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 过滤选项
            if (state.scanResults.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.filterLayoutType == null,
                        onClick = {
                            viewModel.sendIntent(
                                com.mvi.kenny.feature.gridflexboxkit.MigrationScannerIntent.FilterByLayoutType(null)
                            )
                        },
                        label = { Text("全部", style = MaterialTheme.typography.labelSmall) }
                    )
                    com.mvi.kenny.feature.gridflexboxkit.LayoutType.entries.forEach { layout ->
                        FilterChip(
                            selected = state.filterLayoutType == layout,
                            onClick = {
                                viewModel.sendIntent(
                                    com.mvi.kenny.feature.gridflexboxkit.MigrationScannerIntent.FilterByLayoutType(layout)
                                )
                            },
                            label = { Text(layout.displayNameCn, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 扫描结果列表
            if (state.scanResults.isNotEmpty()) {
                val filteredResults = if (state.filterLayoutType != null) {
                    state.scanResults.filter { it.detectedLayout == state.filterLayoutType }
                } else {
                    state.scanResults
                }

                Text(
                    text = "📋 扫描结果（${filteredResults.size} 项）",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredResults) { result ->
                        MigrationResultCard(result = result)
                    }
                }
            } else {
                // 空状态
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (state.isScanning) "正在扫描..." else "点击「开始扫描」检测代码",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MigrationResultCard(result: com.mvi.kenny.feature.gridflexboxkit.MigrationScanResult) {
    val detectedColor = when (result.detectedLayout) {
        com.mvi.kenny.feature.gridflexboxkit.LayoutType.Grid -> Color(0xFF1E88E5)
        com.mvi.kenny.feature.gridflexboxkit.LayoutType.FlexBox -> Color(0xFF8E24AA)
        com.mvi.kenny.feature.gridflexboxkit.LayoutType.LazyGrid -> Color(0xFF43A047)
        com.mvi.kenny.feature.gridflexboxkit.LayoutType.ColumnRow -> Color(0xFF757575)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.filePath,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "第 ${result.lineNumber} 行",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "置信度 ${(result.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 检测到的布局 → 建议迁移
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = detectedColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = result.detectedLayout.displayNameCn,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = detectedColor
                    )
                }
                Icon(
                    Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = result.suggestedMigration.displayNameCn,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 代码片段
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = result.codeSnippet,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
