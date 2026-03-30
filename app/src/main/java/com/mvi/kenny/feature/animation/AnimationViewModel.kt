package com.mvi.kenny.feature.animation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================
 * AnimationViewModel — 动画展示页 ViewModel
 * ============================================================
 * 管理 AnimationScreen 的状态和业务逻辑。
 * 使用 MVI 架构，通过 StateFlow 管理状态，SharedFlow 管理副作用。
 *
 * 状态管理流程：
 * 用户操作（Intent）→ ViewModel 处理 → 更新 State（自动触发 UI 重组）
 *                                                        ↓
 *                                              Effect（一次性副作用）
 *
 * 动画驱动：
 * ViewModel 使用 viewModelScope.launch 驱动定时动画更新，
 * 通过 StateFlow 更新动画值，UI 自动响应变化。
 *
 * @param state 当前 UI 状态（不可变）
 * @param effect 副作用流（Toast 等一次性事件）
 *
 * @see AnimationContract 契约定义
 * @see AnimationScreen UI 渲染层
 */
class AnimationViewModel : ViewModel() {

    // ============================================================
    // State — UI 状态（StateFlow 保证线程安全）
    // ============================================================
    private val _state = MutableStateFlow(AnimationState())
    val state: StateFlow<AnimationState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用（SharedFlow 用于一次性事件）
    // ============================================================
    private val _effect = MutableSharedFlow<AnimationEffect>()
    val effect: SharedFlow<AnimationEffect> = _effect.asSharedFlow()

    // ============================================================
    // 动画协程 Job（用于取消/暂停动画）
    // ============================================================
    private var animationJob: Job? = null

    init {
        // 初始化时启动自动动画循环
        startAnimationLoop()
    }

    // ============================================================
    // Intent 处理入口
    // ============================================================
    /**
     * 处理用户意图
     * 所有用户操作通过此方法进入，确保单线程处理。
     *
     * @param intent 用户操作意图
     */
    fun sendIntent(intent: AnimationIntent) {
        when (intent) {
            is AnimationIntent.SelectDemo -> selectDemo(intent.index)
            is AnimationIntent.ToggleAnimation -> toggleAnimation()
            is AnimationIntent.UpdateCounter -> updateCounter(intent.value)
            is AnimationIntent.UpdateScale -> updateScale(intent.value)
            is AnimationIntent.UpdateRotation -> updateRotation(intent.value)
            is AnimationIntent.CycleColor -> cycleColor()
            is AnimationIntent.ResetAnimation -> resetAnimation()
        }
    }

    // ============================================================
    // Intent 处理逻辑
    // ============================================================

    /**
     * 切换到指定演示项
     * 切换时重置该演示的动画状态。
     *
     * @param index 演示项索引（0-4）
     */
    private fun selectDemo(index: Int) {
        if (index !in animationDemos.indices) return
        _state.update {
            it.copy(
                selectedDemo = index,
                counterValue = 0,
                scaleValue = 1f,
                rotationValue = 0f
            )
        }
        // 重启动画循环以适应新演示
        restartAnimationLoop()
    }

    /**
     * 切换动画播放/暂停状态
     * 暂停时保留当前动画状态，恢复时继续播放。
     */
    private fun toggleAnimation() {
        val newIsAnimating = !_state.value.isAnimating
        _state.update { it.copy(isAnimating = newIsAnimating) }

        if (newIsAnimating) {
            restartAnimationLoop()
            emitEffect(AnimationEffect.ShowToast("Animation resumed 动画继续播放"))
        } else {
            animationJob?.cancel()
            emitEffect(AnimationEffect.ShowToast("Animation paused 动画已暂停"))
        }
    }

    /**
     * 更新计数器值
     * 达到最大值后循环归零。
     *
     * @param value 新的计数值
     */
    private fun updateCounter(value: Int) {
        val maxValue = 100
        _state.update { it.copy(counterValue = value % (maxValue + 1)) }
    }

    /**
     * 更新缩放值
     * 缩放在 0.5f - 1.5f 之间循环。
     *
     * @param value 新的缩放值
     */
    private fun updateScale(value: Float) {
        val newScale = when {
            value > 1.5f -> 0.5f  // 从大缩小的最小值
            value < 0.5f -> 1.5f  // 从小放大的最大值
            else -> value
        }
        _state.update { it.copy(scaleValue = newScale) }
    }

    /**
     * 更新旋转角度
     * 角度超过 360° 后归零。
     *
     * @param value 新的旋转角度
     */
    private fun updateRotation(value: Float) {
        _state.update { it.copy(rotationValue = value % 360f) }
    }

    /**
     * 切换到下一个演示颜色
     */
    private fun cycleColor() {
        _state.update {
            it.copy(colorIndex = (it.colorIndex + 1) % demoColors.size)
        }
    }

    /**
     * 重置所有动画到初始状态
     */
    private fun resetAnimation() {
        _state.update {
            it.copy(
                isAnimating = true,
                counterValue = 0,
                scaleValue = 1f,
                rotationValue = 0f,
                colorIndex = 0
            )
        }
        restartAnimationLoop()
        emitEffect(AnimationEffect.ShowToast("Animation reset 动画已重置"))
    }

    // ============================================================
    // 动画循环管理
    // ============================================================

    /**
     * 启动自动动画循环
     * 根据当前选中的演示项，驱动对应的动画更新。
     */
    private fun startAnimationLoop() {
        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            while (true) {
                if (_state.value.isAnimating) {
                    when (_state.value.selectedDemo) {
                        // 缩放动画：每 50ms 变化 0.02f
                        0 -> {
                            val delta = 0.02f
                            val current = _state.value.scaleValue
                            updateScale(current + delta)
                        }
                        // 颜色循环：每 500ms 切换颜色
                        1 -> {
                            cycleColor()
                            delay(500)
                            continue
                        }
                        // 旋转动画：每 16ms（约 60fps）旋转 2°
                        2 -> {
                            updateRotation(_state.value.rotationValue + 2f)
                        }
                        // 抖动效果：在 shakeUpdate 中处理
                        3 -> {
                            // 抖动效果由 Screen 端 InfiniteTransition 处理
                            delay(100)
                            continue
                        }
                        // 计数器：每 100ms 递增 1
                        4 -> {
                            updateCounter(_state.value.counterValue + 1)
                        }
                    }
                }
                delay(when (_state.value.selectedDemo) {
                    0 -> 50L   // Scale: fast
                    1 -> 500L  // Color: medium
                    2 -> 16L   // Rotation: 60fps
                    3 -> 100L  // Shake: medium
                    4 -> 100L  // Counter: medium
                    else -> 50L
                })
            }
        }
    }

    /**
     * 重启动画循环
     * 用于演示切换或恢复播放时重新启动协程。
     */
    private fun restartAnimationLoop() {
        startAnimationLoop()
    }

    // ============================================================
    // 副作用发送
    // ============================================================

    /**
     * 发送副作用事件
     * 用于 Toast、导航等一次性操作。
     *
     * @param effect 副作用事件
     */
    private fun emitEffect(effect: AnimationEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    // ============================================================
    // 生命周期清理
    // ============================================================

    /**
     * ViewModel 销毁时取消动画协程，防止内存泄漏
     */
    override fun onCleared() {
        super.onCleared()
        animationJob?.cancel()
    }
}
