package com.mvi.kenny.feature.animation

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * AnimationContract — 动画展示 MVI 契约
 * ============================================================
 * 定义 AnimationScreen 的 MVI 三要素：State、Intent、Effect。
 *
 * MVI 架构遵循单向数据流：
 * Intent（用户意图）→ ViewModel 处理 → State（UI 状态）→ UI 自动重组
 *
 * 设计原则：
 * - State 是不可变数据类（Immutable），所有字段 val
 * - Intent 是 sealed class，便于穷举所有用户操作
 * - Effect 是用于一次性副作用（导航、Toast、震动等）
 *
 * @see AnimationViewModel 状态管理逻辑
 * @see AnimationScreen UI 渲染层
 */

/**
 * ============================================================
 * AnimationState — 动画页UI状态
 * ============================================================
 * 包含所有需要驱动 UI 渲染的状态字段。
 *
 * @param selectedDemo 当前选中的动画演示项（0-4）
 * @param isAnimating 当前动画是否正在播放
 * @param counterValue 计数器动画的当前值
 * @param scaleValue 缩放动画的当前值
 * @param rotationValue 旋转动画的当前角度
 * @param colorIndex 颜色动画当前索引（用于循环颜色列表）
 */
data class AnimationState(
    val selectedDemo: Int = 0,
    val isAnimating: Boolean = true,
    val counterValue: Int = 0,
    val scaleValue: Float = 1f,
    val rotationValue: Float = 0f,
    val colorIndex: Int = 0
) {
    /**
     * 当前演示项名称（中英文）
     */
    val currentDemoName: String
        get() = when (selectedDemo) {
            0 -> "Fade & Scale 淡入淡出与缩放"
            1 -> "Color Cycle 颜色循环"
            2 -> "Rotation 旋转动画"
            3 -> "Shake 抖动效果"
            4 -> "Counter 数字递增"
            else -> "Unknown"
        }
}

/**
 * ============================================================
 * AnimationIntent — 用户操作意图
 * ============================================================
 * 用户在动画页面产生的所有操作。
 *
 * @param SelectDemo 切换到指定演示项
 * @param ToggleAnimation 切换动画播放/暂停状态
 * @param UpdateCounter 更新计数器值（自动触发）
 * @param UpdateScale 更新缩放值（自动触发）
 * @param UpdateRotation 更新旋转角度（自动触发）
 * @param CycleColor 切换到下一个颜色
 * @param ResetAnimation 重置所有动画到初始状态
 */
sealed class AnimationIntent {
    data class SelectDemo(val index: Int) : AnimationIntent()
    data object ToggleAnimation : AnimationIntent()
    data class UpdateCounter(val value: Int) : AnimationIntent()
    data class UpdateScale(val value: Float) : AnimationIntent()
    data class UpdateRotation(val value: Float) : AnimationIntent()
    data object CycleColor : AnimationIntent()
    data object ResetAnimation : AnimationIntent()
}

/**
 * ============================================================
 * AnimationEffect — 一次性副作用
 * ============================================================
 * 用于在 Intent 处理后触发非 UI 状态变更的一次性操作。
 *
 * @param ShowToast 显示提示信息
 */
sealed class AnimationEffect {
    data class ShowToast(val message: String) : AnimationEffect()
}

/**
 * ============================================================
 * AnimationDemo — 动画演示配置
 * ============================================================
 * 定义每种动画演示的名称和描述。
 *
 * @param title 演示标题（双语：中文 + 英文）
 * @param description 演示描述
 */
data class AnimationDemo(
    val title: String,
    val description: String
)

/**
 * 预定义的动画演示列表
 */
val animationDemos = listOf(
    AnimationDemo(
        title = "Fade & Scale 淡入淡出与缩放",
        description = "AnimatedVisibility + animateFloatAsState 组合演示"
    ),
    AnimationDemo(
        title = "Color Cycle 颜色循环",
        description = "rememberInfiniteTransition 颜色渐变动画"
    ),
    AnimationDemo(
        title = "Rotation 旋转动画",
        description = "animateFloatAsState 控制旋转角度"
    ),
    AnimationDemo(
        title = "Shake 抖动效果",
        description = "InfiniteTransition + offset 实现抖动"
    ),
    AnimationDemo(
        title = "Counter 数字递增",
        description = "animateIntAsState 整数动画"
    )
)

/**
 * 演示颜色列表（用于颜色循环动画）
 */
val demoColors = listOf(
    Color(0xFF6750A4), // Primary Purple
    Color(0xFF03DAC6), // Teal
    Color(0xFFFF6B6B), // Coral Red
    Color(0xFFFFD93D), // Yellow
    Color(0xFF6BCB77), // Green
    Color(0xFF4D96FF)  // Blue
)
