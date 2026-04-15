package com.mvi.kenny.feature.handoff

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * HandoffScreen — Android 17 Cross-Device Handoff 主屏幕
 * ============================================================
 * 整合所有 Handoff 子页面的主容器。
 *
 * 页面导航：
 * - HandoffDashboardScreen — 仪表盘首页
 * - HandoffApiLibraryScreen — API 封装库
 * - HandoffAnalyzerScreen — 适用性分析器
 * - HandoffSerializationScreen — 序列化框架
 * - HandoffPairingScreen — 设备配对
 * - HandoffUXGuideScreen — UX 设计规范
 * - HandoffDebugScreen — 调试面板
 */
@Composable
fun HandoffScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: HandoffViewModel = viewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    var currentScreen by remember { mutableStateOf(HandoffScreen.DASHBOARD) }

    // Handle navigation effects / 处理导航副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HandoffEffect.NavigateTo -> currentScreen = effect.screen
                is HandoffEffect.NavigateBack -> currentScreen = HandoffScreen.DASHBOARD
                else -> {}
            }
        }
    }

    // Render based on current screen / 根据当前屏幕渲染
    when (currentScreen) {
        HandoffScreen.DASHBOARD -> {
            HandoffDashboardScreen(
                onUpdateTopBar = onUpdateTopBar,
                onNavigateToScreen = { screen -> currentScreen = screen },
                viewModel = viewModel()
            )
        }
        HandoffScreen.API_LIBRARY -> {
            HandoffApiLibraryScreen(
                onNavigateBack = { currentScreen = HandoffScreen.DASHBOARD },
                viewModel = viewModel()
            )
        }
        HandoffScreen.ANALYZER -> {
            HandoffAnalyzerScreen(
                onNavigateBack = { currentScreen = HandoffScreen.DASHBOARD },
                viewModel = viewModel()
            )
        }
        HandoffScreen.SERIALIZATION -> {
            HandoffSerializationScreen(
                onNavigateBack = { currentScreen = HandoffScreen.DASHBOARD },
                viewModel = viewModel()
            )
        }
        HandoffScreen.PAIRING -> {
            HandoffPairingScreen(
                onNavigateBack = { currentScreen = HandoffScreen.DASHBOARD },
                viewModel = viewModel()
            )
        }
        HandoffScreen.UX_GUIDE -> {
            HandoffUXGuideScreen(
                onNavigateBack = { currentScreen = HandoffScreen.DASHBOARD },
                viewModel = viewModel()
            )
        }
        HandoffScreen.DEBUG -> {
            HandoffDebugScreen(
                onNavigateBack = { currentScreen = HandoffScreen.DASHBOARD },
                viewModel = viewModel()
            )
        }
    }
}
