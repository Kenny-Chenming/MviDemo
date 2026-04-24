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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.gridflexboxkit.*

// PlaygroundScreen — 交互式 Playground 屏幕
// PRD-141 | Compose Grid + FlexBox 双布局 API 开发工具包
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlaygroundScreen(
    onBack: () -> Unit,
    initialTemplateId: String? = null,
) {
    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.mvi.kenny.feature.gridflexboxkit.PlaygroundViewModel>()
    val state by viewModel.state.collectAsState()

    // 初始化加载模板 / Load initial template if provided
    androidx.compose.runtime.LaunchedEffect(initialTemplateId) {
        if (!initialTemplateId.isNullOrEmpty()) {
            viewModel.sendIntent(com.mvi.kenny.feature.gridflexboxkit.PlaygroundIntent.LoadTemplate(initialTemplateId))
        } else if (state.templates.isNotEmpty() && state.selectedTemplate == null) {
            // 默认加载第一个模板 / Default: load first template
            viewModel.sendIntent(com.mvi.kenny.feature.gridflexboxkit.PlaygroundIntent.LoadTemplate(state.templates.first().id))
        }
    }

    // 收集副作用 / Collect side effects
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is com.mvi.kenny.feature.gridflexboxkit.PlaygroundEffect.ShowPreviewError -> {
                    // Error is shown via state.previewError
                }
                is com.mvi.kenny.feature.gridflexboxkit.PlaygroundEffect.ShowSaveSuccess -> {
                    // Save success feedback
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Playground",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "交互式 Grid / FlexBox 预览",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // 实时预览开关 / Live preview toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "实时预览",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = state.isLivePreviewEnabled,
                            onCheckedChange = {
                                viewModel.sendIntent(com.mvi.kenny.feature.gridflexboxkit.PlaygroundIntent.ToggleLivePreview)
                            },
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab 切换：Grid / FlexBox
            val tabs = com.mvi.kenny.feature.gridflexboxkit.PlaygroundTab.entries
            TabRow(
                selectedTabIndex = tabs.indexOf(state.activeTab),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = state.activeTab == tab,
                        onClick = {
                            viewModel.sendIntent(com.mvi.kenny.feature.gridflexboxkit.PlaygroundIntent.SelectTab(tab))
                        },
                        text = { Text(tab.displayName) }
                    )
                }
            }

            // 主内容区：代码编辑 + 预览
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // 左侧：代码编辑器 + 模板选择
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(12.dp)
                ) {
                    // 模板选择器 / Template selector
                    Text(
                        text = "模板",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    var templateExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = templateExpanded,
                        onExpandedChange = { templateExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = state.selectedTemplate?.nameCn ?: "选择模板...",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = templateExpanded,
                            onDismissRequest = { templateExpanded = false }
                        ) {
                            state.templates.forEach { template ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(template.nameCn, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                template.nameEn,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.sendIntent(
                                            com.mvi.kenny.feature.gridflexboxkit.PlaygroundIntent.LoadTemplate(template.id)
                                        )
                                        templateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 代码编辑器标签 / Code editor label
                    Text(
                        text = "代码预览",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // 代码展示区 / Code display area
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // 生成代码展示 / Generate code display
                            val generatedCode = remember(state.selectedTemplate, state.gridParams, state.flexBoxParams) {
                                generatePlaygroundCode(state)
                            }
                            Text(
                                text = generatedCode,
                                style = androidx.compose.ui.text.font.FontFamily.Monospace.let {
                                    androidx.compose.ui.text.TextStyle(
                                        fontFamily = it,
                                        fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
                                    )
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 分隔线 / Divider
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // 右侧：实时预览区
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "实时预览",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // 预览缩放控制 / Preview scale control
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "缩放: %.0f%%".format(state.previewScale * 100),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = state.previewScale,
                            onValueChange = {
                                viewModel.sendIntent(
                                    com.mvi.kenny.feature.gridflexboxkit.PlaygroundIntent.UpdatePreviewScale(it)
                                )
                            },
                            valueRange = 0.25f..2f,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 预览区域 / Preview area
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = state.previewScale
                                    scaleY = state.previewScale
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.previewError != null) {
                                // 错误展示 / Error display
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Rounded.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = state.previewError!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else {
                                // Grid / FlexBox 预览 / Grid / FlexBox preview
                                when (state.activeTab) {
                                    com.mvi.kenny.feature.gridflexboxkit.PlaygroundTab.Grid -> {
                                        GridPlaygroundPreview(
                                            gridParams = state.gridParams,
                                            itemCount = state.selectedTemplate?.itemCount ?: 6
                                        )
                                    }
                                    com.mvi.kenny.feature.gridflexboxkit.PlaygroundTab.FlexBox -> {
                                        FlexBoxPlaygroundPreview(
                                            flexBoxParams = state.flexBoxParams,
                                            itemCount = state.selectedTemplate?.itemCount ?: 5
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 底部参数调节面板 / Bottom parameter adjustment panel
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "参数调节",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    when (state.activeTab) {
                        com.mvi.kenny.feature.gridflexboxkit.PlaygroundTab.Grid -> {
                            GridParamsPanel(
                                gridParams = state.gridParams,
                                onParamsChange = { newParams ->
                                    viewModel.sendIntent(
                                        com.mvi.kenny.feature.gridflexboxkit.PlaygroundIntent.UpdateGridParams(newParams)
                                    )
                                }
                            )
                        }
                        com.mvi.kenny.feature.gridflexboxkit.PlaygroundTab.FlexBox -> {
                            FlexBoxParamsPanel(
                                flexBoxParams = state.flexBoxParams,
                                onParamsChange = { newParams ->
                                    viewModel.sendIntent(
                                        com.mvi.kenny.feature.gridflexboxkit.PlaygroundIntent.UpdateFlexParams(newParams)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Grid 实时预览 / Grid live preview
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GridPlaygroundPreview(
    gridParams: com.mvi.kenny.feature.gridflexboxkit.GridParams,
    itemCount: Int,
) {
    val colors = listOf(
        Color(0xFF1E88E5),
        Color(0xFF43A047),
        Color(0xFFE53935),
        Color(0xFF8E24AA),
        Color(0xFFFF6F00),
        Color(0xFF00897B),
        Color(0xFF5E35B1),
        Color(0xFFD81B60),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(gridParams.rowGap.dp)
    ) {
        repeat((itemCount / gridParams.columns).coerceAtLeast(1)) { rowIndex ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(gridParams.columnGap.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(gridParams.columns) { colIndex ->
                    val itemIndex = rowIndex * gridParams.columns + colIndex
                    if (itemIndex < itemCount) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = colors[itemIndex % colors.size].copy(alpha = 0.8f),
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${itemIndex + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// FlexBox 实时预览 / FlexBox live preview
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlexBoxPlaygroundPreview(
    flexBoxParams: com.mvi.kenny.feature.gridflexboxkit.FlexBoxParams,
    itemCount: Int,
) {
    val colors = listOf(
        Color(0xFF8E24AA),  // 紫色 - FlexBox Grow
        Color(0xFFFF6F00),  // 橙色 - FlexBox Shrink
        Color(0xFF00897B),  // 青色 - FlexBox Basis
        Color(0xFF1E88E5),  // 蓝色
        Color(0xFF43A047),  // 绿色
    )

    val flexGrowValues = if (flexBoxParams.flexGrow.size >= itemCount) {
        flexBoxParams.flexGrow.take(itemCount)
    } else {
        flexBoxParams.flexGrow + List(itemCount - flexBoxParams.flexGrow.size) { 1f }
    }

    val flexShrinkValues = if (flexBoxParams.flexShrink.size >= itemCount) {
        flexBoxParams.flexShrink.take(itemCount)
    } else {
        flexBoxParams.flexShrink + List(itemCount - flexBoxParams.flexShrink.size) { 1f }
    }

    val flexBasisValues = if (flexBoxParams.flexBasis.size >= itemCount) {
        flexBoxParams.flexBasis.take(itemCount)
    } else {
        flexBoxParams.flexBasis + List(itemCount - flexBoxParams.flexBasis.size) { 0 }
    }

    val isRow = flexBoxParams.direction == com.mvi.kenny.feature.gridflexboxkit.FlexDirection.Row ||
            flexBoxParams.direction == com.mvi.kenny.feature.gridflexboxkit.FlexDirection.RowReverse

    if (isRow) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(flexBoxParams.mainAxisSpacing.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(itemCount) { index ->
                val grow = flexGrowValues.getOrElse(index) { 1f }
                val basis = flexBasisValues.getOrElse(index) { 0 }
                FlexItem(
                    flexGrow = grow,
                    flexBasis = basis,
                    color = colors[index % colors.size],
                    label = "${index + 1}",
                    shrink = flexShrinkValues.getOrElse(index) { 1f }
                )
            }
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(flexBoxParams.mainAxisSpacing.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(itemCount) { index ->
                val grow = flexGrowValues.getOrElse(index) { 1f }
                val shrink = flexShrinkValues.getOrElse(index) { 1f }
                FlexItem(
                    flexGrow = grow,
                    flexBasis = 0,
                    modifier = Modifier.fillMaxWidth(),
                    color = colors[index % colors.size],
                    label = "${index + 1}",
                    shrink = shrink
                )
            }
        }
    }
}

@Composable
private fun FlexItem(
    flexGrow: Float,
    flexBasis: Int,
    modifier: Modifier = Modifier,
    color: Color,
    label: String,
    shrink: Float,
) {
    val actualModifier = if (flexBasis > 0) {
        modifier.height(48.dp).width(flexBasis.dp)
    } else {
        modifier.height(48.dp)
    }
    Surface(
        modifier = actualModifier,
        shape = RoundedCornerShape(6.dp),
        color = color,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "g:${"%.1f".format(flexGrow)} s:${"%.1f".format(shrink)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// Grid 参数调节面板 / Grid parameter adjustment panel
@Composable
private fun GridParamsPanel(
    gridParams: com.mvi.kenny.feature.gridflexboxkit.GridParams,
    onParamsChange: (com.mvi.kenny.feature.gridflexboxkit.GridParams) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ParamSlider(
            label = "列数",
            value = gridParams.columns.toFloat(),
            valueRange = 1f..8f,
            onValueChange = {
                onParamsChange(gridParams.copy(columns = it.toInt()))
            },
            modifier = Modifier.weight(1f)
        )
        ParamSlider(
            label = "行数",
            value = gridParams.rows.toFloat(),
            valueRange = 1f..6f,
            onValueChange = {
                onParamsChange(gridParams.copy(rows = it.toInt()))
            },
            modifier = Modifier.weight(1f)
        )
        ParamSlider(
            label = "列间距",
            value = gridParams.columnGap.toFloat(),
            valueRange = 0f..32f,
            onValueChange = {
                onParamsChange(gridParams.copy(columnGap = it.toInt()))
            },
            modifier = Modifier.weight(1f)
        )
        ParamSlider(
            label = "行间距",
            value = gridParams.rowGap.toFloat(),
            valueRange = 0f..32f,
            onValueChange = {
                onParamsChange(gridParams.copy(rowGap = it.toInt()))
            },
            modifier = Modifier.weight(1f)
        )
    }
}

// FlexBox 参数调节面板 / FlexBox parameter adjustment panel
@Composable
private fun FlexBoxParamsPanel(
    flexBoxParams: com.mvi.kenny.feature.gridflexboxkit.FlexBoxParams,
    onParamsChange: (com.mvi.kenny.feature.gridflexboxkit.FlexBoxParams) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Direction selector
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "方向",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                com.mvi.kenny.feature.gridflexboxkit.FlexDirection.entries.forEach { direction ->
                    FilterChip(
                        selected = flexBoxParams.direction == direction,
                        onClick = { onParamsChange(flexBoxParams.copy(direction = direction)) },
                        label = { Text(direction.displayName, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        ParamSlider(
            label = "主轴间距",
            value = flexBoxParams.mainAxisSpacing.toFloat(),
            valueRange = 0f..32f,
            onValueChange = {
                onParamsChange(flexBoxParams.copy(mainAxisSpacing = it.toInt()))
            },
            modifier = Modifier.weight(1f)
        )
        ParamSlider(
            label = "交叉轴间距",
            value = flexBoxParams.crossAxisSpacing.toFloat(),
            valueRange = 0f..32f,
            onValueChange = {
                onParamsChange(flexBoxParams.copy(crossAxisSpacing = it.toInt()))
            },
            modifier = Modifier.weight(1f)
        )
    }
}

// 参数滑块通用组件 / Common parameter slider component
@Composable
private fun ParamSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (value == value.toInt().toFloat()) {
                    value.toInt().toString()
                } else {
                    "%.1f".format(value)
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier
        )
    }
}

// 代码生成器 / Code generator
private fun generatePlaygroundCode(state: com.mvi.kenny.feature.gridflexboxkit.PlaygroundState): String {
    return when (state.activeTab) {
        com.mvi.kenny.feature.gridflexboxkit.PlaygroundTab.Grid -> {
            buildString {
                appendLine("@OptIn(ExperimentalLayoutApi::class)")
                appendLine("@Composable")
                appendLine("fun GridPreview() {")
                appendLine("    Column(")
                appendLine("        modifier = Modifier")
                appendLine("            .fillMaxWidth()")
                appendLine("            .padding(16.dp),")
                appendLine("        verticalArrangement = Arrangement")
                appendLine("            .spacedBy(${state.gridParams.rowGap}.dp)")
                appendLine("    ) {")
                appendLine("        repeat(${state.gridParams.rows}) { row ->")
                appendLine("            Row(")
                appendLine("                horizontalArrangement = Arrangement")
                appendLine("                    .spacedBy(${state.gridParams.columnGap}.dp),")
                appendLine("                modifier = Modifier.fillMaxWidth()")
                appendLine("            ) {")
                appendLine("                repeat(${state.gridParams.columns}) { col ->")
                appendLine("                    Card(")
                appendLine("                        modifier = Modifier")
                appendLine("                            .weight(1f)")
                appendLine("                            .aspectRatio(1f),")
                appendLine("                        /* item ${'$'}{(row * cols + col + 1)} */")
                appendLine("                    ) {}")
                appendLine("                }")
                appendLine("            }")
                appendLine("        }")
                appendLine("    }")
                appendLine("}")
            }
        }
        com.mvi.kenny.feature.gridflexboxkit.PlaygroundTab.FlexBox -> {
            val direction = when (state.flexBoxParams.direction) {
                com.mvi.kenny.feature.gridflexboxkit.FlexDirection.Row -> "Row"
                com.mvi.kenny.feature.gridflexboxkit.FlexDirection.Column -> "Column"
                com.mvi.kenny.feature.gridflexboxkit.FlexDirection.RowReverse -> "RowReverse"
                com.mvi.kenny.feature.gridflexboxkit.FlexDirection.ColumnReverse -> "ColumnReverse"
            }
            buildString {
                appendLine("@OptIn(ExperimentalLayoutApi::class)")
                appendLine("@Composable")
                appendLine("fun FlexBoxPreview() {")
                appendLine("    ${direction}(")
                appendLine("        modifier = Modifier")
                appendLine("            .fillMaxWidth()")
                appendLine("            .padding(16.dp),")
                appendLine("        horizontalArrangement = Arrangement")
                appendLine("            .spacedBy(${state.flexBoxParams.mainAxisSpacing}.dp),")
                appendLine("        verticalAlignment = Alignment.CenterVertically")
                appendLine("    ) {")
                appendLine("        // Flex items with flexGrow/flexShrink")
                state.flexBoxParams.flexGrow.take(5).forEachIndexed { index, grow ->
                    appendLine("        FlexItem(")
                    appendLine("            modifier = Modifier.flexible(flexGrow = $grow)")
                    appendLine("        ) { Text(\"Item ${index + 1}\") }")
                }
                appendLine("    }")
                appendLine("}")
            }
        }
    }
}

// Scroll state remember helper
@Composable
private fun rememberScrollState() = androidx.compose.foundation.rememberScrollState()
