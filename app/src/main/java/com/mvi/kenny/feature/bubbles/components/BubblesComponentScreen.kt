package com.mvi.kenny.feature.bubbles.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.bubbles.BubbleComponent
import com.mvi.kenny.feature.bubbles.BubblePlaygroundState
import com.mvi.kenny.feature.bubbles.BubblesColors
import com.mvi.kenny.feature.bubbles.BubblesComponentsState
import com.mvi.kenny.feature.bubbles.BubblesIntent
import com.mvi.kenny.feature.bubbles.ComponentCategory
import kotlin.math.roundToInt

/**
 * ============================================================
 * BubblesComponentScreen — Compose Bubbles 组件库
 * ============================================================
 * 展示所有封装的 Bubbles Compose 组件。
 *
 * 功能区域：
 * - 组件列表：按分类展示所有封装组件
 * - 组件详情：每个组件的 API 文档 + 代码示例
 * - Playground：实时预览各组件的渲染效果
 *
 * @param state Current components state / 当前组件库状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
fun BubblesComponentScreen(
    state: BubblesComponentsState,
    onIntent: (BubblesIntent) -> Unit
) {
    // Determine if showing detail view / 判断是否显示详情视图
    if (state.selectedComponent != null) {
        ComponentDetailView(
            component = state.selectedComponent,
            playgroundState = state.playgroundState,
            onBack = { onIntent(BubblesIntent.ClearComponentSelection) },
            onPlaygroundIntent = onIntent
        )
    } else {
        ComponentListView(
            state = state,
            onIntent = onIntent
        )
    }
}

// =============================================================
// ComponentListView — 组件列表视图
// =============================================================
/**
 * Component list view with category filter.
 * 带分类过滤的组件列表视图。
 *
 * @param state Current components state / 当前组件库状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
private fun ComponentListView(
    state: BubblesComponentsState,
    onIntent: (BubblesIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BubblesColors.Background)
    ) {
        // Category filter chips / 分类过滤 Chip
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All filter / 全部过滤
            item {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { /* Reset filter */ },
                    label = { Text("全部") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BubblesColors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }

            // Category filters / 分类过滤
            items(ComponentCategory.entries) { category ->
                val isSelected = state.selectedCategory == category
                val chipColor by animateColorAsState(
                    targetValue = when {
                        !isSelected -> BubblesColors.Surface
                        category == ComponentCategory.CONTAINER -> BubblesColors.Primary
                        category == ComponentCategory.STATE -> BubblesColors.BubblesActive
                        category == ComponentCategory.LIFECYCLE -> BubblesColors.PiPColor
                        category == ComponentCategory.PERMISSION -> Color(0xFFFF9800)
                        else -> BubblesColors.Secondary
                    },
                    label = "chip_color"
                )

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onIntent(BubblesIntent.SelectComponent(
                            state.components.find { it.category == category } ?: state.components.first()
                        ))
                    },
                    label = { Text(category.labelZh) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = chipColor,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Component list / 组件列表
        val filteredComponents = if (state.selectedCategory == null) {
            state.components
        } else {
            state.components.filter { it.category == state.selectedCategory }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredComponents) { component ->
                ComponentListCard(
                    component = component,
                    onClick = { onIntent(BubblesIntent.SelectComponent(component)) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Component list card.
 * 组件列表卡片。
 */
@Composable
private fun ComponentListCard(
    component: BubbleComponent,
    onClick: () -> Unit
) {
    val categoryColor = when (component.category) {
        ComponentCategory.CONTAINER -> BubblesColors.Primary
        ComponentCategory.STATE -> BubblesColors.BubblesActive
        ComponentCategory.LIFECYCLE -> BubblesColors.PiPColor
        ComponentCategory.PERMISSION -> Color(0xFFFF9800)
        ComponentCategory.UI -> BubblesColors.Secondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BubblesColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category badge / 分类标签
                    Box(
                        modifier = Modifier
                            .background(categoryColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = component.category.labelZh,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = component.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Preview",
                    tint = BubblesColors.Primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = component.nameZh,
                style = MaterialTheme.typography.bodyMedium,
                color = BubblesColors.Primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = component.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = component.descriptionZh,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
    }
}

// =============================================================
// ComponentDetailView — 组件详情视图
// =============================================================
/**
 * Component detail view with API docs and playground.
 * 组件详情视图，含 API 文档和 Playground。
 *
 * @param component Component to display / 要显示的组件
 * @param playgroundState Current playground state / 当前 Playground 状态
 * @param onBack Back navigation callback / 返回导航回调
 * @param onPlaygroundIntent Playground intent handler / Playground 意图处理器
 */
@Composable
private fun ComponentDetailView(
    component: BubbleComponent,
    playgroundState: BubblePlaygroundState,
    onBack: () -> Unit,
    onPlaygroundIntent: (BubblesIntent) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: API, 1: Playground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BubblesColors.Background)
    ) {
        // Header with back button / 带返回按钮的头部
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BubblesColors.Surface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Column {
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = component.nameZh,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Tab row / Tab 行
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BubblesColors.Surface,
            contentColor = BubblesColors.Primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("API 文档") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Playground") }
            )
        }

        // Content / 内容
        when (selectedTab) {
            0 -> ComponentApiDocs(component = component)
            1 -> ComponentPlayground(
                component = component,
                state = playgroundState,
                onIntent = onPlaygroundIntent
            )
        }
    }
}

// =============================================================
// ComponentApiDocs — API 文档
// =============================================================
/**
 * Component API documentation view.
 * 组件 API 文档视图。
 *
 * @param component Component to document / 要文档化的组件
 */
@Composable
private fun ComponentApiDocs(component: BubbleComponent) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Overview / 概述
        Text(
            text = "Overview / 概述",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = component.description,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = component.descriptionZh,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Usage / 使用方法
        Text(
            text = "Usage / 使用方法",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Code example / 代码示例
        val codeExample = getCodeExample(component.name)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${component.name}.kt",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = codeExample,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color(0xFF9CDCFE)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Parameters / 参数
        Text(
            text = "Parameters / 参数",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val params = getComponentParams(component.name)
        params.forEach { param ->
            ParamRow(param = param)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Parameter row.
 * 参数行。
 */
@Composable
private fun ParamRow(param: ParamInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BubblesColors.Surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
                Text(
                    text = param.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = ": ${param.type}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BubblesColors.Primary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = param.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = param.descriptionZh,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Parameter info data class.
 * 参数信息数据类。
 */
private data class ParamInfo(
    val name: String,
    val type: String,
    val description: String,
    val descriptionZh: String
)

/**
 * Get code example for component.
 * 获取组件的代码示例。
 */
private fun getCodeExample(componentName: String): String {
    return when (componentName) {
        "BubbleContainer" -> """
val bubbleState = rememberBubbleState()
val bubbleLifecycleOwner = rememberBubbleLifecycleOwner()

BubbleContainer(
    state = bubbleState,
    lifecycleOwner = bubbleLifecycleOwner,
    modifier = Modifier.fillMaxSize()
) {
    // Bubble content here
    BubbleDragHandle()
    BubbleContent()
}
        """.trimIndent()

        "BubbleState" -> """
val bubbleState = rememberBubbleState(
    initialSize = DpSize(250.dp, 150.dp),
    initialPosition = Offset(100f, 200f)
)

// Update bubble size
bubbleState.updateSize(DpSize(300.dp, 200.dp))

// Minimize bubble
bubbleState.minimize()
        """.trimIndent()

        "BubbleLifecycleOwner" -> """
val lifecycleOwner = rememberBubbleLifecycleOwner()

LaunchedEffect(lifecycleOwner) {
    lifecycleOwner.lifecycle.collect { event ->
        when (event) {
            Lifecycle.Event.ONResume -> { /* Resume */ }
            Lifecycle.Event.ON_PAUSE -> { /* Pause */ }
            Lifecycle.Event.ON_DESTROY -> { /* Destroy */ }
        }
    }
}
        """.trimIndent()

        "BubblePermissionHandler" -> """
val permissionHandler = rememberBubblePermissionHandler()

Button(onClick = {
    permissionHandler.requestOverlayPermission()
}) {
    Text("Grant Permission")
}

if (permissionHandler.hasOverlayPermission) {
    BubbleContainer { /* ... */ }
}
        """.trimIndent()

        "BubbleDragHandle" -> """
BubbleDragHandle(
    onDragStart = { /* Drag started */ },
    onDragEnd = { /* Drag ended */ },
    modifier = Modifier
        .width(32.dp)
        .height(4.dp)
)
        """.trimIndent()

        "BubbleResizeHandle" -> """
BubbleResizeHandle(
    onResize = { newSize ->
        // Handle size change
    },
    corner = Corner.BOTTOM_RIGHT
)
        """.trimIndent()

        else -> "// No example available"
    }
}

/**
 * Get component parameters.
 * 获取组件参数。
 */
private fun getComponentParams(componentName: String): List<ParamInfo> {
    return when (componentName) {
        "BubbleContainer" -> listOf(
            ParamInfo("state", "BubbleState", "Bubble state management", "浮动窗口状态管理"),
            ParamInfo("lifecycleOwner", "BubbleLifecycleOwner", "Lifecycle owner for bubble", "浮动窗口生命周期所有者"),
            ParamInfo("modifier", "Modifier", "Composable modifier", "Compose 修饰符"),
            ParamInfo("content", "@Composable () -> Unit", "Bubble content", "浮动窗口内容")
        )
        "BubbleState" -> listOf(
            ParamInfo("initialSize", "DpSize", "Initial bubble size", "初始浮动窗口尺寸"),
            ParamInfo("initialPosition", "Offset", "Initial bubble position", "初始浮动窗口位置")
        )
        "BubbleLifecycleOwner" -> listOf(
            ParamInfo("lifecycle", "Lifecycle", "Parent lifecycle", "父级生命周期"),
            ParamInfo("onLifecycleEvent", "(Event) -> Unit", "Lifecycle event callback", "生命周期事件回调")
        )
        "BubblePermissionHandler" -> listOf(
            ParamInfo("context", "Context", "Android context", "Android Context"),
            ParamInfo("onPermissionGranted", "() -> Unit", "Permission granted callback", "权限授予回调")
        )
        "BubbleDragHandle" -> listOf(
            ParamInfo("onDragStart", "() -> Unit", "Drag start callback", "拖动开始回调"),
            ParamInfo("onDragEnd", "() -> Unit", "Drag end callback", "拖动结束回调"),
            ParamInfo("modifier", "Modifier", "Composable modifier", "Compose 修饰符")
        )
        "BubbleResizeHandle" -> listOf(
            ParamInfo("onResize", "(DpSize) -> Unit", "Resize callback", "调整大小回调"),
            ParamInfo("corner", "Corner", "Resize handle corner position", "调整手柄位置")
        )
        else -> emptyList()
    }
}

// =============================================================
// ComponentPlayground — 组件 Playground
// =============================================================
/**
 * Interactive component playground.
 * 交互式组件 Playground。
 *
 * @param component Component to preview / 要预览的组件
 * @param state Current playground state / 当前 Playground 状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
private fun ComponentPlayground(
    component: BubbleComponent,
    state: BubblePlaygroundState,
    onIntent: (BubblesIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Preview area / 预览区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFE8DEF8))
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Draggable bubble preview / 可拖动浮动窗口预览
            DraggableBubblePreview(
                state = state,
                componentName = component.name,
                onDragChange = { isDragging ->
                    onIntent(BubblesIntent.UpdatePlaygroundDrag(isDragging))
                },
                onPositionChange = { position ->
                    onIntent(BubblesIntent.UpdatePlaygroundPosition(position))
                }
            )
        }

        // Controls / 控制面板
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BubblesColors.Surface),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Preview Controls / 预览控制",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            onIntent(BubblesIntent.UpdatePlaygroundSize(
                                DpSize(200.dp, 100.dp)
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BubblesColors.Primary)
                    ) {
                        Text("Small")
                    }
                    Button(
                        onClick = {
                            onIntent(BubblesIntent.UpdatePlaygroundSize(
                                DpSize(300.dp, 180.dp)
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BubblesColors.BubblesActive)
                    ) {
                        Text("Medium")
                    }
                    Button(
                        onClick = {
                            onIntent(BubblesIntent.UpdatePlaygroundSize(
                                DpSize(400.dp, 250.dp)
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BubblesColors.PiPColor)
                    ) {
                        Text("Large")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Position: (${state.windowPosition.x.toInt()}, ${state.windowPosition.y.toInt()})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Size: ${state.windowSize.width.value.toInt()}dp × ${state.windowSize.height.value.toInt()}dp",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// =============================================================
// DraggableBubblePreview — 可拖动浮动窗口预览
// =============================================================
/**
 * Draggable bubble preview component.
 * 可拖动浮动窗口预览组件。
 *
 * @param state Current playground state / 当前 Playground 状态
 * @param componentName Component name to preview / 要预览的组件名
 * @param onDragChange Drag state change callback / 拖动状态变化回调
 * @param onPositionChange Position change callback / 位置变化回调
 */
@Composable
private fun DraggableBubblePreview(
    state: BubblePlaygroundState,
    componentName: String,
    onDragChange: (Boolean) -> Unit,
    onPositionChange: (Offset) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(state.windowPosition.x) }
    var offsetY by remember { mutableFloatStateOf(state.windowPosition.y) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(state.windowSize.width, state.windowSize.height)
            .shadow(BubblesColors.BubbleElevation, RoundedCornerShape(BubblesColors.BubbleCornerRadius))
            .background(
                BubblesColors.BubbleBackground,
                RoundedCornerShape(BubblesColors.BubbleCornerRadius)
            )
            .border(1.dp, Color.LightGray, RoundedCornerShape(BubblesColors.BubbleCornerRadius))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragChange(true) },
                    onDragEnd = { onDragChange(false) },
                    onDragCancel = { onDragChange(false) },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        onPositionChange(Offset(offsetX, offsetY))
                    }
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle / 拖动手柄
            Box(
                modifier = Modifier
                    .width(BubblesColors.DragHandleWidth)
                    .height(BubblesColors.DragHandleHeight)
                    .background(Color.LightGray, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Component preview content / 组件预览内容
            Text(
                text = getComponentEmoji(componentName),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = componentName,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Resize handle indicator / 调整大小手柄指示器
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.End)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.LightGray.copy(alpha = 0.5f),
                            RoundedCornerShape(bottomStart = 8.dp)
                        )
                )
            }
        }
    }
}

/**
 * Get component emoji.
 * 获取组件 Emoji。
 */
private fun getComponentEmoji(componentName: String): String {
    return when (componentName) {
        "BubbleContainer" -> "📦"
        "BubbleState" -> "🔢"
        "BubbleLifecycleOwner" -> "🔄"
        "BubblePermissionHandler" -> "🔐"
        "BubbleDragHandle" -> "✋"
        "BubbleResizeHandle" -> "📐"
        else -> "🫧"
    }
}
