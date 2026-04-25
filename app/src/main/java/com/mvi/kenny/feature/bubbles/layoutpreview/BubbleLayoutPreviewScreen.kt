package com.mvi.kenny.feature.bubbles.layoutpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.bubbles.BubblesColors
import com.mvi.kenny.feature.bubbles.BubblesIntent
import com.mvi.kenny.feature.bubbles.BubblesViewModel
import com.mvi.kenny.feature.bubbles.BubbleSize

/**
 * ============================================================
 * BubbleLayoutPreviewScreen — 浮窗自适应布局预览
 * ============================================================
 * PRD-148: Android 17 App Bubbles 浮窗开发工具包
 * 在不同浮窗尺寸下模拟 App 渲染，检测布局崩溃/截断/溢出。
 *
 * 功能：
 * - 多种 BubbleSize 切换预览（SMALL/MEDIUM/LARGE/WIDE/TALL）
 * - 实时宽度/高度滑块调节
 * - 预览区域尺寸标注
 * - 模拟不同目标屏幕（Phone/Foldable/Tablet）
 *
 * @param viewModel BubblesViewModel instance / BubblesViewModel 实例
 * @see BubbleSize 浮窗尺寸枚举
 * @see BubblesIntent.UpdateLayoutBubbleSize 更新浮窗尺寸
 */
@Composable
fun BubbleLayoutPreviewScreen(
    viewModel: BubblesViewModel = viewModel()
) {
    // Collect state from ViewModel / 从 ViewModel 收集状态
    val state by viewModel.state.collectAsState()

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
            text = "Bubble Layout Preview",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "浮窗自适应布局预览 — 在不同 Bubble 尺寸下验证 App 布局表现",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Bubble Size Selector / 浮窗尺寸选择器
        // ============================================================
        Text(
            text = "Bubble Size / 浮窗尺寸",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BubbleSize.entries) { size ->
                FilterChip(
                    selected = state.layoutPreviewSize == size,
                    onClick = { viewModel.sendIntent(BubblesIntent.UpdateLayoutBubbleSize(size)) },
                    label = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = size.labelZh,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "${size.widthDp.value.toInt()}×${size.heightDp.value.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BubblesColors.Primary.copy(alpha = 0.2f),
                        selectedLabelColor = BubblesColors.Primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Custom Size Sliders / 自定义尺寸滑块
        // ============================================================
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
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Width slider / 宽度滑块
                Text(
                    text = "Width / 宽度: ${state.playgroundConfig.customWidth.value.toInt()} dp",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = state.playgroundConfig.customWidth.value,
                    onValueChange = { newWidth ->
                        viewModel.sendIntent(
                            BubblesIntent.UpdatePlaygroundConfig(
                                state.playgroundConfig.copy(customWidth = newWidth.dp)
                            )
                        )
                    },
                    valueRange = 64f..384f,
                    steps = 7
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Height slider / 高度滑块
                Text(
                    text = "Height / 高度: ${state.playgroundConfig.customHeight.value.toInt()} dp",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = state.playgroundConfig.customHeight.value,
                    onValueChange = { newHeight ->
                        viewModel.sendIntent(
                            BubblesIntent.UpdatePlaygroundConfig(
                                state.playgroundConfig.copy(customHeight = newHeight.dp)
                            )
                        )
                    },
                    valueRange = 64f..384f,
                    steps = 7
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Layout Preview Area / 布局预览区域
        // ============================================================
        Text(
            text = "Preview / 预览",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Target screen frame / 目标屏幕边框
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A2E)) // 深色背景模拟屏幕
                .border(2.dp, Color(0xFF4A4A6A), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // App content area / App 内容区域
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2D2D44)),
                contentAlignment = Alignment.TopCenter
            ) {
                // Bubble preview / 浮窗预览
                BubblePreviewBox(
                    width = state.playgroundConfig.customWidth,
                    height = state.playgroundConfig.customHeight
                )
            }
        }

        // Preview size label / 预览尺寸标签
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Preview: ${state.playgroundConfig.customWidth.value.toInt()} × ${state.playgroundConfig.customHeight.value.toInt()} dp",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Size Comparison Grid / 尺寸对比网格
        // ============================================================
        Text(
            text = "Size Reference / 尺寸参考",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BubbleSize.entries.take(3).forEach { size ->
                SizeReferenceItem(
                    size = size,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BubbleSize.entries.drop(3).forEach { size ->
                SizeReferenceItem(
                    size = size,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Layout Tips / 布局提示
        // ============================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BubblesColors.Primary.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Layout Tips / 布局建议",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = BubblesColors.Primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LayoutTipItem("SMALL (128×128): 适合单图标 + 简短文字，如 Badge 提示")
                LayoutTipItem("MEDIUM (192×192): 适合简单控件组合，如播放器控制")
                LayoutTipItem("LARGE (256×256): 适合列表或网格，如通讯录快捷")
                LayoutTipItem("WIDE (256×160): 适合横屏内容，如视频缩略图")
                LayoutTipItem("TALL (160×256): 适合长文本或表单，如翻译结果")
            }
        }
    }
}

/**
 * ============================================================
 * BubblePreviewBox — 浮窗预览框
 * ============================================================
 * 在目标屏幕中渲染一个模拟的 Bubble 浮窗。
 *
 * @param width Bubble width / 浮窗宽度
 * @param height Bubble height / 浮窗高度
 */
@Composable
private fun BubblePreviewBox(width: Dp, height: Dp) {
    Box(
        modifier = Modifier
            .padding(top = 20.dp)
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(BubblesColors.BubbleCornerRadius))
            .background(BubblesColors.BubbleBackground)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(BubblesColors.BubbleCornerRadius))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Drag handle / 拖动手柄
            Box(
                modifier = Modifier
                    .width(BubblesColors.DragHandleWidth)
                    .height(BubblesColors.DragHandleHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFBDBDBD))
            )

            // Mock content / 模拟内容
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BubblesColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${width.value.toInt()}×${height.value.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    color = BubblesColors.Primary
                )
            }

            // Resize handle / 调整大小手柄
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
        }
    }
}

/**
 * ============================================================
 * SizeReferenceItem — 尺寸参考项
 * ============================================================
 *
 * @param size Bubble size / 浮窗尺寸
 * @param modifier Modifier / 修饰符
 */
@Composable
private fun SizeReferenceItem(size: BubbleSize, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(size.widthDp.value.dp / 4, size.heightDp.value.dp / 4)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BubblesColors.Primary.copy(alpha = 0.3f))
                    .border(1.dp, BubblesColors.Primary.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = size.labelZh,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${size.widthDp.value.toInt()}×${size.heightDp.value.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * ============================================================
 * LayoutTipItem — 布局提示项
 * ============================================================
 *
 * @param text Tip text / 提示文本
 */
@Composable
private fun LayoutTipItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            color = BubblesColors.Primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
