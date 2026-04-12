package com.mvi.kenny.feature.bubbles.sizing

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.bubbles.BubblePreset
import com.mvi.kenny.feature.bubbles.BubbleSizeSimulatorState
import com.mvi.kenny.feature.bubbles.BubblesColors
import com.mvi.kenny.feature.bubbles.BubblesIntent
import com.mvi.kenny.feature.bubbles.BubblesTemplatesState
import com.mvi.kenny.feature.bubbles.ScreenSize

/**
 * ============================================================
 * BubblesSizeConfigScreen — 尺寸配置模板 & 场景模板
 * ============================================================
 * 尺寸配置模板：预设模板 + 自定义配置 + 实时模拟器
 * 场景模板：6 种常见场景 Demo
 *
 * 布局：Tab 式（尺寸配置 / 场景模板）
 *
 * @param simulatorState Current size simulator state / 当前尺寸模拟器状态
 * @param templatesState Current templates state / 当前场景模板状态
 * @param onSimulatorIntent Size simulator intent handler / 尺寸模拟器意图处理器
 * @param onTemplatesIntent Templates intent handler / 场景模板意图处理器
 */
@Composable
fun BubblesSizeConfigScreen(
    simulatorState: BubbleSizeSimulatorState,
    templatesState: BubblesTemplatesState,
    onSimulatorIntent: (BubblesIntent) -> Unit,
    onTemplatesIntent: (BubblesIntent) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Size Config, 1: Templates

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BubblesColors.Background)
    ) {
        // Tab row / Tab 行
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BubblesColors.Surface,
            contentColor = BubblesColors.Primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("尺寸配置") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("场景模板") }
            )
        }

        // Content / 内容
        when (selectedTab) {
            0 -> SizeConfigTab(
                state = simulatorState,
                onIntent = onSimulatorIntent
            )
            1 -> TemplatesTab(
                state = templatesState,
                onIntent = onTemplatesIntent
            )
        }
    }
}

// =============================================================
// SizeConfigTab — 尺寸配置 Tab
// =============================================================
/**
 * Size configuration tab with presets and simulator.
 * 含预设和模拟器的尺寸配置 Tab。
 *
 * @param state Current simulator state / 当前模拟器状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
private fun SizeConfigTab(
    state: BubbleSizeSimulatorState,
    onIntent: (BubblesIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // =============================================================
        // Section 1: Presets / 预设区
        // =============================================================
        item {
            Text(
                text = "Presets / 预设模板",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(BubblePreset.entries) { preset ->
                    PresetChip(
                        preset = preset,
                        isSelected = state.selectedPreset == preset,
                        onClick = { onIntent(BubblesIntent.SelectSizePreset(preset)) }
                    )
                }
            }
        }

        // =============================================================
        // Section 2: Custom Size Sliders / 自定义尺寸滑块
        // =============================================================
        item {
            Text(
                text = "Custom Size / 自定义尺寸",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SizeSliderCard(
                currentWidth = state.customWidth,
                currentHeight = state.customHeight,
                onWidthChange = { width ->
                    onIntent(BubblesIntent.UpdateCustomSize(width, state.customHeight))
                },
                onHeightChange = { height ->
                    onIntent(BubblesIntent.UpdateCustomSize(state.customWidth, height))
                }
            )
        }

        // =============================================================
        // Section 3: Target Screen Size / 目标屏幕尺寸
        // =============================================================
        item {
            Text(
                text = "Target Screen / 目标屏幕",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ScreenSize.entries) { screenSize ->
                    ScreenSizeChip(
                        screenSize = screenSize,
                        isSelected = state.targetScreenSize == screenSize,
                        onClick = { onIntent(BubblesIntent.UpdateTargetScreenSize(screenSize)) }
                    )
                }
            }
        }

        // =============================================================
        // Section 4: Live Simulator / 实时模拟器
        // =============================================================
        item {
            Text(
                text = "Live Simulator / 实时模拟",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SizeSimulatorCard(
                bubbleWidth = state.customWidth,
                bubbleHeight = state.customHeight,
                screenSize = state.targetScreenSize
            )
        }

        // =============================================================
        // Section 5: Foldable Adaptation / 折叠屏适配
        // =============================================================
        item {
            Text(
                text = "Foldable Adaptation / 折叠屏适配",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            FoldableAdaptationCard(currentPreset = state.selectedPreset)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Preset selection chip.
 * 预设选择 Chip。
 */
@Composable
private fun PresetChip(
    preset: BubblePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = getPresetEmoji(preset),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = preset.labelZh,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BubblesColors.Primary,
            selectedLabelColor = Color.White
        )
    )
}

/**
 * Screen size selection chip.
 * 屏幕尺寸选择 Chip。
 */
@Composable
private fun ScreenSizeChip(
    screenSize: ScreenSize,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = screenSize.label,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "${screenSize.widthDp.value.toInt()}×${screenSize.heightDp.value.toInt()}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BubblesColors.PiPColor,
            selectedLabelColor = Color.White
        )
    )
}

/**
 * Size slider card.
 * 尺寸滑块卡片。
 */
@Composable
private fun SizeSliderCard(
    currentWidth: Dp,
    currentHeight: Dp,
    onWidthChange: (Dp) -> Unit,
    onHeightChange: (Dp) -> Unit
) {
    var sliderWidth by remember { mutableFloatStateOf(currentWidth.value) }
    var sliderHeight by remember { mutableFloatStateOf(currentHeight.value) }

    Card(
        colors = CardDefaults.cardColors(containerColor = BubblesColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Width slider / 宽度滑块
            Text(
                text = "Width / 宽度: ${sliderWidth.toInt()}dp",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = sliderWidth,
                onValueChange = { sliderWidth = it },
                onValueChangeFinished = { onWidthChange(sliderWidth.dp) },
                valueRange = 100f..500f,
                colors = SliderDefaults.colors(
                    thumbColor = BubblesColors.Primary,
                    activeTrackColor = BubblesColors.Primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Height slider / 高度滑块
            Text(
                text = "Height / 高度: ${sliderHeight.toInt()}dp",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = sliderHeight,
                onValueChange = { sliderHeight = it },
                onValueChangeFinished = { onHeightChange(sliderHeight.dp) },
                valueRange = 50f..500f,
                colors = SliderDefaults.colors(
                    thumbColor = BubblesColors.BubblesActive,
                    activeTrackColor = BubblesColors.BubblesActive
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Corner radius info / 圆角信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Corner Radius / 圆角: 16dp",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Elevation / 阴影: 8dp",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Size simulator card showing bubble in screen context.
 * 显示浮动窗口在屏幕上下文中的尺寸模拟器卡片。
 */
@Composable
private fun SizeSimulatorCard(
    bubbleWidth: Dp,
    bubbleHeight: Dp,
    screenSize: ScreenSize
) {
    val animatedWidth by animateDpAsState(targetValue = bubbleWidth, label = "bubble_width")
    val animatedHeight by animateDpAsState(targetValue = bubbleHeight, label = "bubble_height")

    Card(
        colors = CardDefaults.cardColors(containerColor = BubblesColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simulated screen / 模拟屏幕
            Box(
                modifier = Modifier
                    .width(screenSize.widthDp * 0.6f)
                    .height(screenSize.heightDp * 0.3f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0))
                    .border(2.dp, Color.Gray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Bubble / 浮动窗口
                Box(
                    modifier = Modifier
                        .size(animatedWidth * 0.6f, animatedHeight * 0.6f)
                        .shadow(8.dp, RoundedCornerShape(BubblesColors.BubbleCornerRadius))
                        .background(
                            BubblesColors.BubbleBackground,
                            RoundedCornerShape(BubblesColors.BubbleCornerRadius)
                        )
                        .border(1.dp, Color.LightGray, RoundedCornerShape(BubblesColors.BubbleCornerRadius)),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(3.dp)
                                .background(Color.LightGray, RoundedCornerShape(1.dp))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${bubbleWidth.value.toInt()}×${bubbleHeight.value.toInt()}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Screen: ${screenSize.label} (${screenSize.widthDp.value.toInt()}×${screenSize.heightDp.value.toInt()})",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Text(
                text = "Bubble occupies ${(bubbleWidth.value / screenSize.widthDp.value * 100).toInt()}% width",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

/**
 * Foldable adaptation card.
 * 折叠屏适配卡片。
 */
@Composable
private fun FoldableAdaptationCard(currentPreset: BubblePreset) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BubblesColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Foldable Size Adaptation / 折叠屏尺寸适配",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "When foldable device switches between folded/unfolded state,",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = "bubble will auto-adjust using WindowInfoTracker.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Adaptation table / 适配表
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Folded →",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentPreset.defaultWidth.value.toInt() * 0.8f}×${currentPreset.defaultHeight.value.toInt() * 0.8f}dp",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "← Unfolded",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentPreset.defaultWidth.value.toInt()}×${currentPreset.defaultHeight.value.toInt()}dp",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// =============================================================
// TemplatesTab — 场景模板 Tab
// =============================================================
/**
 * Scenario templates tab.
 * 场景模板 Tab。
 *
 * @param state Current templates state / 当前模板状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
private fun TemplatesTab(
    state: BubblesTemplatesState,
    onIntent: (BubblesIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Scenario Demos / 场景 Demo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose a scenario to see implementation details",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        items(state.scenarios) { scenario ->
            ScenarioCard(
                scenario = scenario,
                onClick = { onIntent(BubblesIntent.SelectScenario(scenario)) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Scenario demo card.
 * 场景 Demo 卡片。
 */
@Composable
private fun ScenarioCard(
    scenario: com.mvi.kenny.feature.bubbles.BubbleScenario,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BubblesColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scenario icon / 场景图标
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(4.dp, RoundedCornerShape(BubblesColors.BubbleCornerRadius))
                    .background(
                        BubblesColors.BubbleBackground,
                        RoundedCornerShape(BubblesColors.BubbleCornerRadius)
                    )
                    .border(1.dp, Color.LightGray, RoundedCornerShape(BubblesColors.BubbleCornerRadius)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getScenarioEmoji(scenario.id),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.nameZh,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = scenario.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = BubblesColors.Primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scenario.descriptionZh,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = scenario.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "View",
                tint = BubblesColors.Primary
            )
        }
    }
}

// =============================================================
// Helper Functions / 辅助函数
// =============================================================
/**
 * Get preset emoji.
 * 获取预设 Emoji。
 */
private fun getPresetEmoji(preset: BubblePreset): String {
    return when (preset) {
        BubblePreset.MUSIC -> "🎵"
        BubblePreset.NAVIGATION -> "🗺️"
        BubblePreset.NOTES -> "📝"
        BubblePreset.TRANSLATION -> "🌐"
        BubblePreset.VIDEO -> "📺"
        BubblePreset.CUSTOM -> "⚙️"
    }
}

/**
 * Get scenario emoji.
 * 获取场景 Emoji。
 */
private fun getScenarioEmoji(scenarioId: String): String {
    return when (scenarioId) {
        "music" -> "🎵"
        "navigation" -> "🗺️"
        "notes" -> "📝"
        "translation" -> "🌐"
        "video" -> "📺"
        "call" -> "📞"
        else -> "🫧"
    }
}
