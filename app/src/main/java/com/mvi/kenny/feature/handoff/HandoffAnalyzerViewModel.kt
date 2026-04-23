package com.mvi.kenny.feature.handoff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * HandoffAnalyzerViewModel — 适用性分析器 ViewModel
 * ============================================================
 */
class HandoffAnalyzerViewModel : ViewModel() {

    private val _state = MutableStateFlow(HandoffAnalyzerState.Initial)
    val state: StateFlow<HandoffAnalyzerState> = _state.asStateFlow()

    private val _effect = Channel<HandoffAnalyzerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun sendIntent(intent: HandoffAnalyzerIntent) {
        when (intent) {
            is HandoffAnalyzerIntent.UpdateInputPath -> _state.value = _state.value.copy(inputPath = intent.path)
            is HandoffAnalyzerIntent.StartScan -> startScan()
            is HandoffAnalyzerIntent.CancelScan -> _state.value = _state.value.copy(scanStatus = ScanStatus.IDLE)
            is HandoffAnalyzerIntent.SelectActivity -> _state.value = _state.value.copy(selectedActivity = intent.activity)
            is HandoffAnalyzerIntent.ClearSelection -> _state.value = _state.value.copy(selectedActivity = null)
        }
    }

    private fun startScan() {
        _state.value = _state.value.copy(scanStatus = ScanStatus.SCANNING)
        viewModelScope.launch {
            delay(3000)
            val results = listOf(
                ActivityHandoffScore("com.example.app.MainActivity", "MainActivity", 95, "✅ 非常适合 Handoff", true, listOf("独立状态，可序列化", "无复杂依赖"), listOf("com.example.app.SplashActivity")),
                ActivityHandoffScore("com.example.app.ComposeActivity", "ComposeActivity", 88, "✅ 适合 Handoff", true, listOf("状态为 Compose State", "可逆向恢复"), emptyList()),
                ActivityHandoffScore("com.example.app.EditorActivity", "EditorActivity", 62, "⚠️ 需要优化", true, listOf("包含未序列化的大对象", "部分状态需本地保留"), listOf("com.example.app.DraftManager")),
                ActivityHandoffScore("com.example.app.GameActivity", "GameActivity", 15, "❌ 不建议 Handoff", false, listOf("游戏状态无法序列化", "OpenGL 上下文无法传输"), listOf("com.example.engine.GameEngine")),
                ActivityHandoffScore("com.example.app.CameraActivity", "CameraActivity", 30, "⚠️ 部分场景可用", true, listOf("Camera 状态无法传输", "但配置参数可传递"), emptyList())
            )
            _state.value = _state.value.copy(
                scanStatus = ScanStatus.DONE,
                analyzedActivities = results,
                dependencyTree = ActivityDependencyTree("com.example.app.MainActivity", true, 95, listOf(
                    ActivityDependencyTree("com.example.app.ComposeActivity", handoffScore = 88),
                    ActivityDependencyTree("com.example.app.EditorActivity", handoffScore = 62)
                ))
            )
            val handoffableCount = results.count { it.isHandoffable }
            _effect.send(HandoffAnalyzerEffect.ScanCompleted(results.size, handoffableCount))
        }
    }
}
