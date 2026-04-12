package com.mvi.kenny.feature.bubbles.intro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.bubbles.BubblesColors
import com.mvi.kenny.feature.bubbles.BubblesIntent
import com.mvi.kenny.feature.bubbles.BubblesIntroState

/**
 * ============================================================
 * BubblesIntroScreen — Bubbles 接入引导首页
 * ============================================================
 * 功能入口页面，展示 Bubbles 核心能力和快速开始入口。
 *
 * 页面结构：
 * - 顶部：Bubbles 效果动态预览（浮动窗口动画）
 * - 功能说明区：Bubbles 6 大核心能力概览
 * - 快速开始按钮：进入 Gradle 插件引导流程
 * - Bubbles vs PiP 对比卡片
 *
 * @param state Current intro state / 当前引导页状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
fun BubblesIntroScreen(
    state: BubblesIntroState,
    onIntent: (BubblesIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BubblesColors.Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // =============================================================
        // Section 1: Animated Preview / 动态预览区
        // =============================================================
        item {
            BubblesPreviewCard(
                isAnimating = state.isAnimating,
                onToggleAnimation = { onIntent(BubblesIntent.TogglePreviewAnimation) }
            )
        }

        // =============================================================
        // Section 2: Core Capabilities / 核心能力概览
        // =============================================================
        item {
            Text(
                text = "Core Capabilities / 核心能力",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            CoreCapabilitiesGrid()
        }

        // =============================================================
        // Section 3: Quick Start / 快速开始
        // =============================================================
        item {
            QuickStartCard(
                onStartWizard = { onIntent(BubblesIntent.StartGradleWizard("app")) }
            )
        }

        // =============================================================
        // Section 4: Bubbles vs PiP Comparison / vs PiP 对比
        // =============================================================
        item {
            BubblesVsPiPCard(
                expanded = state.showVsPiP,
                onToggle = { onIntent(BubblesIntent.ToggleVsPiP) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =============================================================
// BubblesPreviewCard — 动态预览卡片
// =============================================================
/**
 * Animated Bubbles preview card.
 * 动态 Bubbles 预览卡片。
 *
 * @param isAnimating Whether animation is playing / 动画是否播放
 * @param onToggleAnimation Toggle animation callback / 切换动画回调
 */
@Composable
private fun BubblesPreviewCard(
    isAnimating: Boolean,
    onToggleAnimation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BubblesColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "App Bubbles Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Simulated screen with bubble / 模拟屏幕与浮动窗口
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8DEF8))
                    .clickable { onToggleAnimation() },
                contentAlignment = Alignment.Center
            ) {
                // Main app area / 主应用区域
                Text(
                    text = "Main App Content",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                // Animated bubble / 动画浮动窗口
                AnimatedBubble(isAnimating = isAnimating)
            }

            Text(
                text = "Tap to ${if (isAnimating) "pause" else "play"} animation",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// =============================================================
// AnimatedBubble — 动画浮动窗口
// =============================================================
/**
 * Animated floating bubble component.
 * 动画浮动窗口组件。
 *
 * @param isAnimating Whether bubble is animating / 浮动窗口是否动画中
 */
@Composable
private fun AnimatedBubble(isAnimating: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_animation")

    // Float up and down / 上下浮动
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isAnimating) -20f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubble_offset"
    )

    // Scale pulse / 缩放脉冲
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isAnimating) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubble_scale"
    )

    Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .size((100 * scale).dp, (60 * scale).dp)
            .shadow(8.dp, RoundedCornerShape(BubblesColors.BubbleCornerRadius))
            .background(
                BubblesColors.BubbleBackground,
                RoundedCornerShape(BubblesColors.BubbleCornerRadius)
            )
            .border(1.dp, Color.LightGray, RoundedCornerShape(BubblesColors.BubbleCornerRadius)),
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
                    .background(
                        Color.LightGray,
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🎵",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Music",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// =============================================================
// CoreCapabilitiesGrid — 核心能力网格
// =============================================================
/**
 * Grid of Bubbles core capabilities.
 * Bubbles 核心能力网格。
 */
@Composable
private fun CoreCapabilitiesGrid() {
    val capabilities = listOf(
        CapabilityItem("📱", "Any App", "任意 App 浮动化", "Turn any app into floating bubble"),
        CapabilityItem("🖥️", "Multi-window", "多窗口管理", "Seamless multi-window lifecycle"),
        CapabilityItem("🎨", "Compose UI", "Compose 封装", "First-class Compose support"),
        CapabilityItem("📐", "Adaptive", "自适应尺寸", "Foldable & tablet adaptive"),
        CapabilityItem("🔄", "Sync", "状态同步", "Cross-window data sync"),
        CapabilityItem("🛡️", "Safe", "权限管理", "System overlay permissions")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        capabilities.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { capability ->
                    CapabilityCard(
                        capability = capability,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if odd number / 如果是奇数则填充空白
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Capability item data class.
 * 能力项目数据类。
 */
private data class CapabilityItem(
    val emoji: String,
    val title: String,
    val titleZh: String,
    val description: String
)

/**
 * Individual capability card.
 * 单个能力卡片。
 */
@Composable
private fun CapabilityCard(
    capability: CapabilityItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = BubblesColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = capability.emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = capability.titleZh,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = capability.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =============================================================
// QuickStartCard — 快速开始卡片
// =============================================================
/**
 * Quick start card with CTA button.
 * 含 CTA 按钮的快速开始卡片。
 *
 * @param onStartWizard Start Gradle wizard callback / 开始 Gradle 向导回调
 */
@Composable
private fun QuickStartCard(
    onStartWizard: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = BubblesColors.Primary.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚀 Quick Start / 快速开始",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Integrate App Bubbles into your project in 5 steps",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "5 步完成浮动窗口接入",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartWizard,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BubblesColors.Primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Gradle Wizard")
            }
        }
    }
}

// =============================================================
// BubblesVsPiPCard — Bubbles vs PiP 对比卡片
// =============================================================
/**
 * Expandable Bubbles vs PiP comparison card.
 * 可展开的 Bubbles vs PiP 对比卡片。
 *
 * @param expanded Whether card is expanded / 卡片是否展开
 * @param onToggle Toggle expand callback / 切换展开回调
 */
@Composable
private fun BubblesVsPiPCard(
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
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
                    Icon(
                        imageVector = Icons.Default.PictureInPicture,
                        contentDescription = null,
                        tint = BubblesColors.PiPColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bubbles vs PiP / 决策参考",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (expanded) "▼" else "▶",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(12.dp))

                    // Comparison table / 对比表格
                    ComparisonTable()
                }
            }
        }
    }
}

// =============================================================
// ComparisonTable — 对比表格
// =============================================================
/**
 * Bubbles vs PiP comparison table.
 * Bubbles vs PiP 对比表格。
 */
@Composable
private fun ComparisonTable() {
    val comparisons = listOf(
        ComparisonRow("Use Case", "Interactive panels", "Video playback"),
        ComparisonRow("API Level", "Android 17+", "Android 8+"),
        ComparisonRow("Compose Support", "✅ Full", "⚠️ Partial"),
        ComparisonRow("Drag & Resize", "✅ Yes", "❌ No"),
        ComparisonRow("Multiple Bubbles", "✅ Yes", "❌ No"),
        ComparisonRow("Taskbar Integration", "✅ Yes", "⚠️ Limited"),
        ComparisonRow("Cross-window Sync", "✅ Built-in", "⚠️ Manual")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Header / 表头
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "维度",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Bubbles",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = BubblesColors.BubblesActive,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "PiP",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = BubblesColors.PiPColor,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        HorizontalDivider()

        comparisons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = row.dimension,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = row.bubbles,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = row.pip,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recommendation / 推荐
        Card(
            colors = CardDefaults.cardColors(
                containerColor = BubblesColors.BubblesActive.copy(alpha = 0.1f)
            )
        ) {
            Text(
                text = "💡 推荐：需要交互性、多窗口、Compose 支持 → Bubbles\n" +
                       "      纯视频播放、兼容性优先 → PiP",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/**
 * Comparison row data class.
 * 对比行数据类。
 */
private data class ComparisonRow(
    val dimension: String,
    val bubbles: String,
    val pip: String
)
