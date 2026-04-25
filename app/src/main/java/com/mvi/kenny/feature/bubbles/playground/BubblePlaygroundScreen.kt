package com.mvi.kenny.feature.bubbles.playground

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.bubbles.BubblesColors
import com.mvi.kenny.feature.bubbles.BubblesIntent
import com.mvi.kenny.feature.bubbles.BubblesViewModel
import com.mvi.kenny.feature.bubbles.BubblePlaygroundConfig
import com.mvi.kenny.feature.bubbles.BubbleSize
import kotlin.math.roundToInt

/**
 * ============================================================
 * BubblePlaygroundScreen — Bubble 浮窗体验 Playground
 * ============================================================
 * PRD-148: Android 17 App Bubbles 浮窗开发工具包
 * 交互式示例，可在线编辑 Bubble 配置并预览效果。
 *
 * 功能：
 * - 拖拽调整浮窗位置
 * - 切换显示/隐藏拖动手柄和调整大小手柄
 * - 调节浮窗尺寸
 * - 快速重置位置
 * - 代码预览（生成对应 Compose 代码）
 *
 * @param viewModel BubblesViewModel instance / BubblesViewModel 实例
 * @see BubblePlaygroundConfig Playground 配置数据类
 * @see BubblesIntent.UpdatePlaygroundConfig 更新配置
 */
@Composable
fun BubblePlaygroundScreen(
    viewModel: BubblesViewModel = viewModel()
) {
    // Collect state from ViewModel / 从 ViewModel 收集状态
    val state by viewModel.state.collectAsState()
    val config = state.playgroundConfig

    // Local drag state / 本地拖拽状态
    var bubbleOffset by remember { mutableStateOf(Offset(config.bubblePosition.x, config.bubblePosition.y)) }
    var isDragging by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ============================================================
        // Header / 头部
        // ============================================================
        Text(
            text = "Bubble Playground",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Bubble 浮窗体验场 — 拖拽、调节、预览完整的浮窗交互体验",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Interactive Preview Area / 交互预览区域
        // ============================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A2E)) // 深色屏幕背景
                .border(2.dp, Color(0xFF4A4A6A), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // App content mock / App 内容模拟
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2D2D44)),
                contentAlignment = Alignment.Center
            ) {
                // Draggable Bubble / 可拖拽浮窗
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                bubbleOffset.x.roundToInt() - 64,
                                bubbleOffset.y.roundToInt() - 64
                            )
                        }
                        .size(
                            width = config.customWidth.value.dp,
                            height = config.customHeight.value.dp
                        )
                        .clip(RoundedCornerShape(BubblesColors.BubbleCornerRadius))
                        .background(BubblesColors.BubbleBackground)
                        .border(
                            width = if (isDragging) 2.dp else 1.dp,
                            color = if (isDragging) BubblesColors.Primary else Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(BubblesColors.BubbleCornerRadius)
                        )
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    isDragging = true
                                    viewModel.sendIntent(
                                        BubblesIntent.UpdatePlaygroundConfig(
                                            config.copy(isDragging = true)
                                        )
                                    )
                                },
                                onDragEnd = {
                                    isDragging = false
                                    viewModel.sendIntent(
                                        BubblesIntent.UpdatePlaygroundConfig(
                                            config.copy(isDragging = false)
                                        )
                                    )
                                },
                                onDragCancel = {
                                    isDragging = false
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    bubbleOffset = Offset(
                                        bubbleOffset.x + dragAmount.x,
                                        bubbleOffset.y + dragAmount.y
                                    )
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    PlaygroundBubbleContent(
                        config = config,
                        offset = bubbleOffset
                    )
                }
            }

            // Drag hint / 拖拽提示
            if (!isDragging) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Drag the bubble / 拖拽浮窗",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Position indicator / 位置指示器
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Position: (${bubbleOffset.x.roundToInt()}, ${bubbleOffset.y.roundToInt()})",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Size: ${config.customWidth.value.toInt()}×${config.customHeight.value.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Controls / 控制面板
        // ============================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Controls / 控制面板",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = {
                    bubbleOffset = Offset(100f, 100f)
                    viewModel.sendIntent(
                        BubblesIntent.UpdatePlaygroundConfig(
                            BubblePlaygroundConfig()
                        )
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Size preset chips / 尺寸预设芯片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BubbleSize.entries.forEach { size ->
                FilterChip(
                    selected = config.selectedSize == size,
                    onClick = {
                        viewModel.sendIntent(
                            BubblesIntent.UpdatePlaygroundConfig(
                                config.copy(
                                    selectedSize = size,
                                    customWidth = size.widthDp,
                                    customHeight = size.heightDp
                                )
                            )
                        )
                    },
                    label = {
                        Text(
                            text = size.labelZh,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BubblesColors.Primary.copy(alpha = 0.2f),
                        selectedLabelColor = BubblesColors.Primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Width/Height sliders / 宽高滑块
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Custom Size / 自定义尺寸",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Width / 宽度: ${config.customWidth.value.toInt()} dp",
                    style = MaterialTheme.typography.labelSmall
                )
                Slider(
                    value = config.customWidth.value,
                    onValueChange = { newWidth ->
                        viewModel.sendIntent(
                            BubblesIntent.UpdatePlaygroundConfig(
                                config.copy(
                                    customWidth = newWidth.dp,
                                    selectedSize = BubbleSize.SMALL // Reset preset when customizing
                                )
                            )
                        )
                    },
                    valueRange = 64f..384f,
                    steps = 7
                )

                Text(
                    text = "Height / 高度: ${config.customHeight.value.toInt()} dp",
                    style = MaterialTheme.typography.labelSmall
                )
                Slider(
                    value = config.customHeight.value,
                    onValueChange = { newHeight ->
                        viewModel.sendIntent(
                            BubblesIntent.UpdatePlaygroundConfig(
                                config.copy(
                                    customHeight = newHeight.dp,
                                    selectedSize = BubbleSize.SMALL
                                )
                            )
                        )
                    },
                    valueRange = 64f..384f,
                    steps = 7
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Handle toggles / 手柄开关
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(BubblesColors.Primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⋮⋮",
                                fontSize = 8.sp,
                                color = BubblesColors.Primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Drag Handle / 拖动手柄",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = config.showDragHandle,
                        onCheckedChange = { show ->
                            viewModel.sendIntent(
                                BubblesIntent.UpdatePlaygroundConfig(
                                    config.copy(showDragHandle = show)
                                )
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFBDBDBD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⊡", fontSize = 10.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Resize Handle / 调整大小手柄",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = config.showResizeHandle,
                        onCheckedChange = { show ->
                            viewModel.sendIntent(
                                BubblesIntent.UpdatePlaygroundConfig(
                                    config.copy(showResizeHandle = show)
                                )
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Generated Code Preview / 生成的代码预览
        // ============================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E2E)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Generated Code / 生成的代码",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF888888)
                    )
                    IconButton(
                        onClick = {
                            viewModel.sendIntent(
                                BubblesIntent.CopyCode(generateBubbleCode(config))
                            )
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = Color(0xFF888888),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = generateBubbleCode(config),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF89B4FA)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * ============================================================
 * PlaygroundBubbleContent — Playground 浮窗内容
 * ============================================================
 * 渲染浮窗内部的手柄和内容区域。
 *
 * @param config Playground configuration / Playground 配置
 * @param offset Bubble offset / 浮窗偏移
 */
@Composable
private fun PlaygroundBubbleContent(
    config: BubblePlaygroundConfig,
    offset: Offset
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Drag handle / 拖动手柄
        if (config.showDragHandle) {
            Box(
                modifier = Modifier
                    .width(BubblesColors.DragHandleWidth)
                    .height(BubblesColors.DragHandleHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFBDBDBD))
            )
        } else {
            Spacer(modifier = Modifier.height(BubblesColors.DragHandleHeight))
        }

        // Content area / 内容区
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BubblesColors.Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🥜 Bubble",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BubblesColors.Primary
                )
                Text(
                    text = "${config.customWidth.value.toInt()}×${config.customHeight.value.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Resize handle / 调整大小手柄
        if (config.showResizeHandle) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFBDBDBD)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⊡",
                    fontSize = 8.sp,
                    color = Color.White
                )
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * ============================================================
 * generateBubbleCode — 生成 Bubble 代码
 * ============================================================
 * 根据当前 Playground 配置生成对应的 Jetpack Compose 代码。
 *
 * @param config Playground configuration / Playground 配置
 * @return Generated Kotlin code string / 生成的 Kotlin 代码字符串
 */
private fun generateBubbleCode(config: BubblePlaygroundConfig): String {
    return """
        |BubbleContainer(
        |    modifier = Modifier
        |        .size(
        |            width = ${config.customWidth.value.toInt()}.dp,
        |            height = ${config.customHeight.value.toInt()}.dp
        |        )
        |        .pointerInput(Unit) {
        |            detectDragGestures { _, dragAmount ->
        |                // Handle drag / 处理拖拽
        |            }
        |        },
        |    showDragHandle = ${config.showDragHandle},
        |    showResizeHandle = ${config.showResizeHandle}
        |) {
        |    // Bubble content / 浮窗内容
        |    Text("Your content here")
        |}
    """.trimMargin()
}
