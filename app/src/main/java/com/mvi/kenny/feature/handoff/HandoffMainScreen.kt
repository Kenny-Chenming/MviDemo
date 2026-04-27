package com.mvi.kenny.feature.handoff

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

// =============================================================
// HandoffMainScreen — Android 17 Handoff API 跨设备连续性开发工具包
// 主屏幕 / Main Screen
// =============================================================
// PRD-153 | Android 17 Handoff API Cross-Device Continuity Dev Toolkit
//
// Layout: Tab-based navigation with 8 tool modules.
// 布局：基于 Tab 的导航，8个工具模块。
//
// Tool tabs / 工具标签页:
// 1. INTEGRATE_TEMPLATE  — 集成模板（onHandoffActivityRequested 集成）
// 2. INTENT_BUILDER      — Intent 构建器
// 3. DECISION_ENGINE      — 适用性决策引擎
// 4. PRIVACY_COMPLIANCE  — 隐私合规检测
// 5. DEBUG_PANEL         — 跨设备调试面板
// 6. SYNC_TEMPLATE       — 多设备同步框架集成
// 7. APP_BUNDLE_GUIDE   — App Bundle 联动
// 8. FALLBACK_STRATEGY  — 降级策略
//
// @param onUpdateTopBar Update parent TopBar callback / 更新父 TopBar 回调
// @param viewModel HandoffViewModel instance / HandoffViewModel 实例
//
// @see HandoffContract MVI contract definitions
// @see HandoffViewModel State management

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoffMainScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: HandoffViewModel = viewModel()
) {
    // Collect state from ViewModel / 从 ViewModel 收集状态
    val state by viewModel.state.collectAsState()

    // Update TopBar when tool tab changes / Tab 变化时更新 TopBar
    LaunchedEffect(state.currentTool) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Handoff · ${state.currentTool.titleZh}",
                actions = emptyList()
            )
        )
    }

    // Collect effects for one-time UI actions / 收集副作用用于一次性UI操作
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            // Handle side effects / 处理副作用
            // ( Toast, Snackbar, Navigation handled by parent if needed )
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab navigation / Tab 导航
            HandoffTabRow(
                selectedTool = state.currentTool,
                onToolSelected = { viewModel.sendIntent(HandoffIntent.SelectTool(it)) }
            )

            // Tool content area / 工具内容区域
            when (state.currentTool) {
                HandoffTool.INTEGRATE_TEMPLATE -> HandoffIntegrateTemplateScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                HandoffTool.INTENT_BUILDER -> HandoffIntentBuilderScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                HandoffTool.DECISION_ENGINE -> HandoffDecisionEngineScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                HandoffTool.PRIVACY_COMPLIANCE -> HandoffPrivacyComplianceScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                HandoffTool.DEBUG_PANEL -> HandoffDebugPanelScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                HandoffTool.SYNC_TEMPLATE -> HandoffSyncTemplateScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                HandoffTool.APP_BUNDLE_GUIDE -> HandoffAppBundleGuideScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                HandoffTool.FALLBACK_STRATEGY -> HandoffFallbackStrategyScreen(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
            }
        }
    }
}

// =============================================================
// HandoffTabRow — Tab navigation row
// =============================================================
/**
 * Tab navigation row for Handoff tool modules.
 * Handoff 工具模块的 Tab 导航行。
 *
 * Horizontal scrollable tabs for all 8 tools.
 *
 * @param selectedTool Currently selected tool / 当前选中的工具
 * @param onToolSelected Callback when a tool tab is selected / 工具 Tab 选中时的回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandoffTabRow(
    selectedTool: HandoffTool,
    onToolSelected: (HandoffTool) -> Unit
) {
    TabRow(
        selectedTabIndex = HandoffTool.entries.indexOf(selectedTool),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        HandoffTool.entries.forEach { tool ->
            Tab(
                selected = selectedTool == tool,
                onClick = { onToolSelected(tool) },
                text = {
                    Text(
                        text = tool.titleZh,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
