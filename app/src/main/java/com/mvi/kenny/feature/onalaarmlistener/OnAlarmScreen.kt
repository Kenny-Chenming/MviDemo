package com.mvi.kenny.feature.onalaarmlistener

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel

// =============================================================
// OnAlarmScreen — Android 17 OnAlarmListener Battery
// Optimization Tool 主界面
// =============================================================
/**
 * Main container screen for OnAlarmListener Battery Optimization Tool / OnAlarmListener 电池优化工具主界面
 *
 * Provides bottom tab navigation between 4 tool modules:
 * - Dashboard: Compliance overview / 合规概览
 * - Scanner: Guided scan flow / 引导扫描流程
 * - Analysis: Detailed task list / 详细任务列表
 * - Settings: CI config and thresholds / CI 配置和阈值
 *
 * @param viewModel ViewModel instance / ViewModel 实例
 * @param onNavigateToScanner Callback when scanner needs to be shown / 需要显示扫描器时的回调
 * @param onNavigateToAnalysis Callback when analysis needs to be shown / 需要显示分析时的回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnAlarmScreen(
    viewModel: OnAlarmViewModel = viewModel(),
    onNavigateToScanner: (() -> Unit)? = null,
    onNavigateToAnalysis: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()

    // Handle navigation effects / 处理导航副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OnAlarmEffect.NavigateToScanner -> onNavigateToScanner?.invoke()
                is OnAlarmEffect.NavigateToAnalysis -> onNavigateToAnalysis?.invoke()
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "OnAlarmListener / 电池优化工具",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    // Quick scan button / 快速扫描按钮
                    if (state.activeTab != ToolTab.SCANNER) {
                        IconButton(
                            onClick = { viewModel.processIntent(OnAlarmIntent.StartQuickScan) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = "Quick Scan / 快速扫描"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                ToolTab.entries.forEach { tab ->
                    val selected = state.activeTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            viewModel.processIntent(OnAlarmIntent.SelectTab(tab))
                        },
                        icon = {
                            Icon(
                                imageVector = getTabIcon(tab, selected),
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = getTabLabel(tab),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Crossfade(
                targetState = state.activeTab,
                label = "TabCrossfade"
            ) { tab ->
                when (tab) {
                    ToolTab.DASHBOARD -> DashboardScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    ToolTab.SCANNER -> ScannerScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    ToolTab.ANALYSIS -> AnalysisScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    ToolTab.SETTINGS -> SettingsScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                }
            }
        }
    }
}

/**
 * Get icon for tab / 获取 Tab 图标
 *
 * @param tab Tool tab / 工具 Tab
 * @param selected Whether tab is selected / Tab 是否被选中
 * @return ImageVector icon / 图标
 */
private fun getTabIcon(tab: ToolTab, selected: Boolean): ImageVector {
    return when (tab) {
        ToolTab.DASHBOARD -> if (selected) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
        ToolTab.SCANNER -> if (selected) Icons.Filled.Radar else Icons.Outlined.Radar
        ToolTab.ANALYSIS -> if (selected) Icons.Filled.Analytics else Icons.Outlined.Analytics
        ToolTab.SETTINGS -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
    }
}

/**
 * Get short label for tab / 获取 Tab 简短标签
 */
private fun getTabLabel(tab: ToolTab): String {
    return when (tab) {
        ToolTab.DASHBOARD -> "Dashboard"
        ToolTab.SCANNER -> "Scanner"
        ToolTab.ANALYSIS -> "Analysis"
        ToolTab.SETTINGS -> "Settings"
    }
}
