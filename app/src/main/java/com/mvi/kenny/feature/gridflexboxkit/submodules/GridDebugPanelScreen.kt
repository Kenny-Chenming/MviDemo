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

// GridDebugPanelScreen — Grid 调试可视化面板
// PRD-141 | Compose Grid + FlexBox 双布局 API 开发工具包
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GridDebugPanelScreen(onBack: () -> Unit) {
    var columnCount by remember { mutableIntStateOf(3) }
    var rowCount by remember { mutableIntStateOf(3) }
    var columnGap by remember { mutableIntStateOf(8) }
    var rowGap by remember { mutableIntStateOf(8) }
    var showGridLines by remember { mutableStateOf(true) }
    var showTracks by remember { mutableStateOf(true) }
    var showCells by remember { mutableStateOf(true) }
    var showGaps by remember { mutableStateOf(true) }

    // 调试颜色常量 / Debug color constants
    val gridLineColor = Color(0xFFE53935)  // 红色 - Grid lines
    val gridTrackColor = Color(0xFF1E88E5) // 蓝色 - Grid tracks
    val gridCellColor = Color(0xFF43A047)  // 绿色 - Grid cells
    val gridGapColor = Color(0xFF757575)   // 灰色 - Grid gaps

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grid 调试面板") },
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
            // 参数控制面板 / Parameter control panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚙️ Grid 参数调节",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 列数 / 行数 / Gap 滑块
                    GridParamSlider("列数 (Columns)", columnCount, 1..6) { columnCount = it }
                    GridParamSlider("行数 (Rows)", rowCount, 1..6) { rowCount = it }
                    GridParamSlider("列间距 (Column Gap)", columnGap, 0..32) { columnGap = it }
                    GridParamSlider("行间距 (Row Gap)", rowGap, 0..32) { rowGap = it }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // 调试覆盖层开关 / Debug overlay toggles
                    Text(
                        text = "🔍 调试覆盖层 / Debug Overlays",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DebugToggle("Grid Lines (红色)", showGridLines, gridLineColor) { showGridLines = it }
                    DebugToggle("Grid Tracks (蓝色)", showTracks, gridTrackColor) { showTracks = it }
                    DebugToggle("Grid Cells (绿色)", showCells, gridCellColor) { showCells = it }
                    DebugToggle("Grid Gaps (灰色)", showGaps, gridGapColor) { showGaps = it }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid 可视化展示区 / Grid visualization area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 使用 Grid API 展示网格（自身即是对 Grid API 的展示）
                    // Using Grid API to display grid layout (showcasing itself)
                    GridDebugVisualization(
                        columnCount = columnCount,
                        rowCount = rowCount,
                        columnGap = columnGap.dp,
                        rowGap = rowGap.dp,
                        showGridLines = showGridLines,
                        showTracks = showTracks,
                        showCells = showCells,
                        showGaps = showGaps,
                        gridLineColor = gridLineColor,
                        gridTrackColor = gridTrackColor,
                        gridCellColor = gridCellColor,
                        gridGapColor = gridGapColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 颜色图例 / Color legend
            GridDebugLegend(
                gridLineColor = gridLineColor,
                gridTrackColor = gridTrackColor,
                gridCellColor = gridCellColor,
                gridGapColor = gridGapColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GridDebugVisualization(
    columnCount: Int,
    rowCount: Int,
    columnGap: androidx.compose.ui.unit.Dp,
    rowGap: androidx.compose.ui.unit.Dp,
    showGridLines: Boolean,
    showTracks: Boolean,
    showCells: Boolean,
    showGaps: Boolean,
    gridLineColor: Color,
    gridTrackColor: Color,
    gridCellColor: Color,
    gridGapColor: Color
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        // 计算 Grid 尺寸 / Calculate Grid dimensions
        val totalColumnGap = columnGap * (columnCount - 1)
        val totalRowGap = rowGap * (rowCount - 1)
        val cellWidth = (totalWidth - totalColumnGap) / columnCount
        val cellHeight = (totalHeight - totalRowGap) / rowCount

        // 绘制 Grid 布局 + 调试覆盖层
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // 绘制 Grid gaps（作为背景）
            if (showGaps) {
                for (row in 0 until rowCount) {
                    for (col in 0 until columnCount) {
                        val x = col.toFloat() * (cellWidth.toPx() + columnGap.toPx())
                        val y = row.toFloat() * (cellHeight.toPx() + rowGap.toPx())
                        drawRect(
                            color = gridGapColor.copy(alpha = 0.3f),
                            topLeft = androidx.compose.ui.geometry.Offset(x, y),
                            size = androidx.compose.ui.geometry.Size(
                                cellWidth.toPx() + columnGap.toPx(),
                                cellHeight.toPx() + rowGap.toPx()
                            )
                        )
                    }
                }
            }
        }

        // Grid cells with content
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(rowGap)
        ) {
            repeat(rowCount) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(columnGap)
                ) {
                    repeat(columnCount) { col ->
                        val cellIndex = row * columnCount + col
                        GridCellDebugItem(
                            cellIndex = cellIndex,
                            showGridLines = showGridLines,
                            showTracks = showTracks,
                            showCells = showCells,
                            gridLineColor = gridLineColor,
                            gridTrackColor = gridTrackColor,
                            gridCellColor = gridCellColor,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridCellDebugItem(
    cellIndex: Int,
    showGridLines: Boolean,
    showTracks: Boolean,
    showCells: Boolean,
    gridLineColor: Color,
    gridTrackColor: Color,
    gridCellColor: Color,
    modifier: Modifier = Modifier
) {
    val borderModifier = if (showGridLines) {
        Modifier.border(
            width = 2.dp,
            color = gridLineColor.copy(alpha = 0.8f),
            shape = RoundedCornerShape(4.dp)
        )
    } else {
        Modifier
    }

    val backgroundModifier = if (showCells) {
        Modifier.background(
            color = gridCellColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp)
        )
    } else {
        Modifier
    }

    Surface(
        modifier = modifier.then(borderModifier).then(backgroundModifier),
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (showTracks) {
                // 绘制 track 索引标记 / Draw track index markers
                Text(
                    text = "[$cellIndex]",
                    style = MaterialTheme.typography.labelSmall,
                    color = gridTrackColor,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "$cellIndex",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GridParamSlider(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(140.dp)
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$value",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun DebugToggle(
    label: String,
    checked: Boolean,
    color: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = color,
                checkedTrackColor = color.copy(alpha = 0.5f)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = color.copy(alpha = 0.2f)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun GridDebugLegend(
    gridLineColor: Color,
    gridTrackColor: Color,
    gridCellColor: Color,
    gridGapColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = gridLineColor, label = "Grid Lines")
            LegendItem(color = gridTrackColor, label = "Grid Tracks")
            LegendItem(color = gridCellColor, label = "Grid Cells")
            LegendItem(color = gridGapColor, label = "Grid Gaps")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            shape = RoundedCornerShape(2.dp),
            color = color
        ) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
