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

// FlexBoxDebugPanelScreen — FlexBox 调试可视化面板
// PRD-141 | Compose Grid + FlexBox 双布局 API 开发工具包
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlexBoxDebugPanelScreen(onBack: () -> Unit) {
    var direction by remember { mutableStateOf(FlexDirection.Row) }
    var itemCount by remember { mutableIntStateOf(5) }
    var showGrow by remember { mutableStateOf(true) }
    var showShrink by remember { mutableStateOf(true) }
    var showBasis by remember { mutableStateOf(true) }

    // Flex items 参数 / Flex items parameters
    var flexGrowValues by remember { mutableStateOf(listOf(0f, 1f, 2f, 1f, 0f)) }
    var flexShrinkValues by remember { mutableStateOf(listOf(0f, 1f, 2f, 1f, 0f)) }
    var flexBasisValues by remember { mutableStateOf(listOf(0, 60, 120, 60, 0)) }

    val growColor = Color(0xFF8E24AA)   // 紫色 - Flex Grow
    val shrinkColor = Color(0xFFFF6F00) // 橙色 - Flex Shrink
    val basisColor = Color(0xFF00897B)  // 青色 - Flex Basis

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FlexBox 调试面板") },
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
            // 参数控制面板
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚙️ FlexBox 参数调节",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 方向选择 / Direction selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("方向:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(60.dp))
                        FlexDirection.entries.forEach { dir ->
                            FilterChip(
                                selected = direction == dir,
                                onClick = { direction = dir },
                                label = { Text(dir.displayName, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Item 数量 / Item count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Item 数量:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(80.dp))
                        Slider(
                            value = itemCount.toFloat(),
                            onValueChange = {
                                itemCount = it.toInt().coerceIn(1, 8)
                                // 调整数组长度 / Adjust array length
                                flexGrowValues = (0 until itemCount).map { flexGrowValues.getOrElse(it) { 1f } }
                                flexShrinkValues = (0 until itemCount).map { flexShrinkValues.getOrElse(it) { 1f } }
                                flexBasisValues = (0 until itemCount).map { flexBasisValues.getOrElse(it) { 60 } }
                            },
                            valueRange = 1f..8f,
                            steps = 6,
                            modifier = Modifier.weight(1f)
                        )
                        Text("$itemCount", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(24.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "🔥 Flex 属性热力图 / Flex Heatmap",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DebugToggleFlex("Grow (紫色)", showGrow, growColor) { showGrow = it }
                    DebugToggleFlex("Shrink (橙色)", showShrink, shrinkColor) { showShrink = it }
                    DebugToggleFlex("Basis (青色)", showBasis, basisColor) { showBasis = it }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FlexBox 可视化展示区
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    FlexBoxDebugVisualization(
                        direction = direction,
                        flexGrowValues = flexGrowValues.take(itemCount),
                        flexShrinkValues = flexShrinkValues.take(itemCount),
                        flexBasisValues = flexBasisValues.take(itemCount),
                        showGrow = showGrow,
                        showShrink = showShrink,
                        showBasis = showBasis,
                        growColor = growColor,
                        shrinkColor = shrinkColor,
                        basisColor = basisColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 颜色图例
            FlexBoxDebugLegend(growColor = growColor, shrinkColor = shrinkColor, basisColor = basisColor)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlexBoxDebugVisualization(
    direction: FlexDirection,
    flexGrowValues: List<Float>,
    flexShrinkValues: List<Float>,
    flexBasisValues: List<Int>,
    showGrow: Boolean,
    showShrink: Boolean,
    showBasis: Boolean,
    growColor: Color,
    shrinkColor: Color,
    basisColor: Color
) {
    val isRow = direction == FlexDirection.Row || direction == FlexDirection.RowReverse

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        // 方向标签 / Direction label
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = direction.displayName,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Flex items 容器 / Flex items container
        if (isRow) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                flexGrowValues.forEachIndexed { index, grow ->
                    FlexItemDebugBox(
                        index = index,
                        grow = grow,
                        shrink = flexShrinkValues.getOrElse(index) { 1f },
                        basis = flexBasisValues.getOrElse(index) { 0 },
                        showGrow = showGrow,
                        showShrink = showShrink,
                        showBasis = showBasis,
                        growColor = growColor,
                        shrinkColor = shrinkColor,
                        basisColor = basisColor,
                        modifier = Modifier
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                flexGrowValues.forEachIndexed { index, grow ->
                    FlexItemDebugBox(
                        index = index,
                        grow = grow,
                        shrink = flexShrinkValues.getOrElse(index) { 1f },
                        basis = flexBasisValues.getOrElse(index) { 0 },
                        showGrow = showGrow,
                        showShrink = showShrink,
                        showBasis = showBasis,
                        growColor = growColor,
                        shrinkColor = shrinkColor,
                        basisColor = basisColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun FlexItemDebugBox(
    index: Int,
    grow: Float,
    shrink: Float,
    basis: Int,
    showGrow: Boolean,
    showShrink: Boolean,
    showBasis: Boolean,
    growColor: Color,
    shrinkColor: Color,
    basisColor: Color,
    modifier: Modifier = Modifier
) {
    // 计算颜色强度 / Calculate color intensity (0.0 ~ 1.0)
    val growIntensity = (grow / 3f).coerceIn(0f, 1f)
    val shrinkIntensity = (shrink / 3f).coerceIn(0f, 1f)
    val basisIntensity = (basis / 200f).coerceIn(0f, 1f)

    val backgroundColor = when {
        showGrow && growIntensity > 0 -> growColor.copy(alpha = 0.1f + growIntensity * 0.5f)
        showShrink && shrinkIntensity > 0 -> shrinkColor.copy(alpha = 0.1f + shrinkIntensity * 0.5f)
        showBasis && basisIntensity > 0 -> basisColor.copy(alpha = 0.1f + basisIntensity * 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = when {
        showGrow && growIntensity > 0 -> growColor.copy(alpha = growIntensity)
        showShrink && shrinkIntensity > 0 -> shrinkColor.copy(alpha = shrinkIntensity)
        showBasis && basisIntensity > 0 -> basisColor.copy(alpha = basisIntensity)
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$index",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (showGrow) {
                Text(
                    text = "⬆️$grow",
                    style = MaterialTheme.typography.labelSmall,
                    color = growColor
                )
            }
            if (showShrink) {
                Text(
                    text = "⬇️$shrink",
                    style = MaterialTheme.typography.labelSmall,
                    color = shrinkColor
                )
            }
            if (showBasis) {
                Text(
                    text = "📏$basis",
                    style = MaterialTheme.typography.labelSmall,
                    color = basisColor
                )
            }
        }
    }
}

@Composable
private fun DebugToggleFlex(
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
private fun FlexBoxDebugLegend(
    growColor: Color,
    shrinkColor: Color,
    basisColor: Color
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
            FlexLegendItem(color = growColor, label = "Grow (紫色)")
            FlexLegendItem(color = shrinkColor, label = "Shrink (橙色)")
            FlexLegendItem(color = basisColor, label = "Basis (青色)")
        }
    }
}

@Composable
private fun FlexLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(12.dp),
            shape = RoundedCornerShape(2.dp),
            color = color
        ) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
