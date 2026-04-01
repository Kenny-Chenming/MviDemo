package com.mvi.kenny.feature.animation

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.R
import com.mvi.kenny.base.TopBarActions
import com.mvi.kenny.base.TopBarConfig
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * ============================================================
 * AnimationScreen — 动画展示Composable
 * ============================================================
 * 展示 Compose 各种动画能力的演示页面。
 *
 * 布局结构：
 * ┌─────────────────────────────────────────────┐
 * │  TopBar（播放/暂停 + 重置按钮）              │
 * ├─────────────────────────────────────────────┤
 * │  [Chip 1] [Chip 2] [Chip 3] [Chip 4] [Chip 5]│  ← 演示选择器
 * ├─────────────────────────────────────────────┤
 * │                                             │
 * │              动画演示区域                    │
 * │         （根据 selectedDemo 切换）           │
 * │                                             │
 * ├─────────────────────────────────────────────┤
 * │  描述文字 + 参数显示                        │
 * └─────────────────────────────────────────────┘
 *
 * 动画类型：
 * 0 - Fade & Scale（AnimatedVisibility + animateFloatAsState）
 * 1 - Color Cycle（rememberInfiniteTransition）
 * 2 - Rotation（animateFloatAsState）
 * 3 - Shake（InfiniteTransition + offset）
 * 4 - Counter（animateIntAsState）
 *
 * @param onUpdateTopBar 向 MainScreen 上报 TopBar 配置
 */
@Composable
fun AnimationScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val viewModel: AnimationViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // ============================================================
    // TopBar 配置（播放/暂停 + 重置按钮）
    // ============================================================
    // 注意：TopBarConfig 的创建在 Composable 上下文中执行（LaunchedEffect 外部），
    // 以支持 stringResource 等 @Composable 调用。
    // LaunchedEffect 仅负责在 isAnimating 变化时触发更新。
    val topBarConfig = TopBarConfig(
        title = stringResource(R.string.animation_title),
        actions = listOf(
            TopBarActions.animationPlayPause(
                isPlaying = state.isAnimating,
                onClick = { viewModel.sendIntent(AnimationIntent.ToggleAnimation) }
            ),
            TopBarActions.animationReset(
                onClick = { viewModel.sendIntent(AnimationIntent.ResetAnimation) }
            )
        )
    )
    LaunchedEffect(state.isAnimating) {
        onUpdateTopBar(topBarConfig)
    }

    // ============================================================
    // Effect 收集（Toast）
    // ============================================================
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AnimationEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ============================================================
    // 页面主体
    // ============================================================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // —— 演示选择器（Chips） —— //
        Text(
            text = "Animation Demos 动画演示",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            animationDemos.forEachIndexed { index, demo ->
                FilterChip(
                    selected = state.selectedDemo == index,
                    onClick = { viewModel.sendIntent(AnimationIntent.SelectDemo(index)) },
                    label = {
                        Text(
                            text = "${index + 1}",
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // —— 动画演示区域 —— //
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // 根据选中演示切换动画内容
                AnimatedContent(
                    targetState = state.selectedDemo,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f))
                            .togetherWith(fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f))
                    },
                    label = "demo_transition"
                ) { demoIndex ->
                    when (demoIndex) {
                        0 -> ScaleDemo(state = state)
                        1 -> ColorCycleDemo(state = state)
                        2 -> RotationDemo(state = state)
                        3 -> ShakeDemo(state = state)
                        4 -> CounterDemo(state = state)
                        else -> Text("Unknown")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // —— 当前演示信息卡片 —— //
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = animationDemos.getOrNull(state.selectedDemo)?.title ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = animationDemos.getOrNull(state.selectedDemo)?.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildString {
                        append("Scale: ${String.format("%.2f", state.scaleValue)} | ")
                        append("Rotation: ${state.rotationValue.toInt()}° | ")
                        append("Counter: ${state.counterValue}")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ============================================================
// 动画演示组件 1: Scale Demo（缩放动画）
// ============================================================

/**
 * 缩放动画演示
 * 使用 animateFloatAsState 控制 Box 的 scale。
 *
 * @param state 当前动画状态
 */
@Composable
private fun ScaleDemo(state: AnimationState) {
    // animateFloatAsState 在值变化时自动驱动插值动画
    val animatedScale by animateFloatAsState(
        targetValue = state.scaleValue,
        animationSpec = tween(durationMillis = 100),
        label = "scale_animation"
    )

    // 淡入淡出动画
    val visible = state.isAnimating

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.3f),
        exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.3f)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Scale",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================
// 动画演示组件 2: Color Cycle Demo（颜色循环）
// ============================================================

/**
 * 颜色循环动画演示
 * 使用 rememberInfiniteTransition 实现无限循环的颜色渐变。
 *
 * @param state 当前动画状态
 */
@Composable
private fun ColorCycleDemo(state: AnimationState) {
    val infiniteTransition = rememberInfiniteTransition(label = "color_cycle")

    // 使用 animateFloat 在两个颜色之间无限循环
    val animatedFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color_fraction"
    )

    // 当前目标颜色（从 state.colorIndex 获取）
    val currentColor = demoColors.getOrElse(state.colorIndex) { demoColors[0] }
    val nextColor = demoColors.getOrElse((state.colorIndex + 1) % demoColors.size) { demoColors[0] }

    // 插值计算当前显示颜色
    val displayColor = lerp(currentColor, nextColor, animatedFraction)

    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(displayColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Color",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cycle",
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * 两个 Color 之间的线性插值
 *
 * @param start 起始颜色
 * @param end 目标颜色
 * @param fraction 插值因子（0f - 1f）
 * @return 插值后的颜色
 */
@Composable
private fun lerp(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = start.alpha + (end.alpha - start.alpha) * fraction
    )
}

// ============================================================
// 动画演示组件 3: Rotation Demo（旋转动画）
// ============================================================

/**
 * 旋转动画演示
 * 使用 animateFloatAsState 控制 Box 的 rotate。
 *
 * @param state 当前动画状态
 */
@Composable
private fun RotationDemo(state: AnimationState) {
    val animatedRotation by animateFloatAsState(
        targetValue = state.rotationValue,
        animationSpec = tween(durationMillis = 16), // 约 60fps
        label = "rotation_animation"
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .rotate(animatedRotation)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.tertiary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${animatedRotation.toInt()}°",
            color = MaterialTheme.colorScheme.onTertiary,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// 动画演示组件 4: Shake Demo（抖动动画）
// ============================================================

/**
 * 抖动动画演示
 * 使用 rememberInfiniteTransition 控制 offset 实现左右抖动。
 *
 * @param state 当前动画状态（isAnimating 控制是否播放）
 */
@Composable
private fun ShakeDemo(state: AnimationState) {
    val infiniteTransition = rememberInfiniteTransition(label = "shake")

    // X 方向偏移：0 → 10 → 0 → -10 → 0（循环）
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake_x"
    )

    // Y 方向偏移：轻微上下抖动
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 80, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake_y"
    )

    val displayOffset = if (state.isAnimating) IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) else IntOffset.Zero

    Box(
        modifier = Modifier
            .size(100.dp)
            .offset { displayOffset }
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Shake!",
            color = MaterialTheme.colorScheme.onError,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// 动画演示组件 5: Counter Demo（数字递增动画）
// ============================================================

/**
 * 数字递增动画演示
 * 使用 animateIntAsState 控制数字变化时的动画过渡。
 *
 * @param state 当前动画状态
 */
@Composable
private fun CounterDemo(state: AnimationState) {
    val animatedCounter by animateIntAsState(
        targetValue = state.counterValue,
        animationSpec = tween(durationMillis = 200),
        label = "counter_animation"
    )

    // 动态字体大小（数字越大，字号略微增大）
    val fontSize = (24 + (animatedCounter % 20)).sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = animatedCounter.toString(),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "/ 100",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 进度条
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedCounter / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
