package com.mvi.kenny.feature.remotetoolkit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.remotetoolkit.sub_screens.DashboardScreen
import com.mvi.kenny.feature.remotetoolkit.sub_screens.CompatibilityScannerScreen
import com.mvi.kenny.feature.remotetoolkit.sub_screens.ServerSDKScreen
import com.mvi.kenny.feature.remotetoolkit.sub_screens.DebugPanelScreen
import com.mvi.kenny.feature.remotetoolkit.sub_screens.RPCFrameworkScreen
import com.mvi.kenny.feature.remotetoolkit.sub_screens.SecurityLayerScreen
import com.mvi.kenny.feature.remotetoolkit.sub_screens.VersionManagerScreen
import com.mvi.kenny.feature.remotetoolkit.sub_screens.PerformanceAnalyzerScreen
import com.mvi.kenny.feature.remotetoolkit.sub_screens.AIPipelineScreen
import com.mvi.kenny.feature.remotetoolkit.sub_screens.ABTestingScreen
import kotlinx.coroutines.flow.collectLatest

// =============================================================
// RemoteToolkitScreen — Compose Remote Toolkit 主界面
// Main screen with NavigationRail + content area layout
// 左侧导航 + 右侧内容区 Dashboard 架构
// =============================================================

@Composable
fun RemoteToolkitScreen(
    viewModel: RemoteToolkitViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Update parent TopBar when active page changes / 页面切换时更新父 TopBar
    LaunchedEffect(state.activePage) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Compose Remote Toolkit",
                actions = emptyList()
            )
        )
    }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is RemoteToolkitEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is RemoteToolkitEffect.ScanComplete -> {
                    snackbarHostState.showSnackbar("Scan complete! Score: ${state.scanScore}%")
                }
                is RemoteToolkitEffect.ServerStarted -> {
                    snackbarHostState.showSnackbar("Debug server started on port ${effect.port}")
                }
                is RemoteToolkitEffect.ServerError -> {
                    snackbarHostState.showSnackbar("Server error: ${effect.message}")
                }
                is RemoteToolkitEffect.ConfigSaved -> {
                    snackbarHostState.showSnackbar("Configuration saved successfully")
                }
                is RemoteToolkitEffect.ConnectionTestResult -> {
                    snackbarHostState.showSnackbar(
                        if (effect.success) "Connection test: PASS ✅" else "Connection test: FAILED ❌"
                    )
                }
                is RemoteToolkitEffect.VersionDeployed -> {
                    snackbarHostState.showSnackbar("New version deployed successfully!")
                }
                is RemoteToolkitEffect.RollbackComplete -> {
                    snackbarHostState.showSnackbar("Rollback complete")
                }
                is RemoteToolkitEffect.TrafficSplitUpdated -> {
                    snackbarHostState.showSnackbar("Traffic split updated")
                }
                is RemoteToolkitEffect.BenchmarkComplete -> {
                    snackbarHostState.showSnackbar("Benchmark complete!")
                }
                is RemoteToolkitEffect.ReportExported -> {
                    snackbarHostState.showSnackbar("Report exported successfully")
                }
                is RemoteToolkitEffect.CodeCopied -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is RemoteToolkitEffect.DeploymentStarted -> {
                    snackbarHostState.showSnackbar("UI deployment started...")
                }
                is RemoteToolkitEffect.WinnerDeclared -> {
                    snackbarHostState.showSnackbar("Winner declared!")
                }
                is RemoteToolkitEffect.ScrollToBottom -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(RemoteToolkitColors.Background)
        ) {
            // ============================================================
            // Left: Navigation Rail / 左侧：导航栏
            // ============================================================
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = RemoteToolkitColors.Surface
            ) {
                RemoteToolkitPage.entries.forEachIndexed { index, page ->
                    NavigationRailItem(
                        icon = {
                            Icon(
                                imageVector = page.icon,
                                contentDescription = page.title
                            )
                        },
                        label = { Text(page.title, style = MaterialTheme.typography.labelSmall) },
                        selected = state.activePage == page,
                        onClick = { viewModel.processIntent(RemoteToolkitIntent.SelectPage(page)) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = RemoteToolkitColors.Purple,
                            selectedTextColor = RemoteToolkitColors.Purple,
                            indicatorColor = RemoteToolkitColors.PurpleContainer,
                            unselectedIconColor = RemoteToolkitColors.OnSurfaceVariant,
                            unselectedTextColor = RemoteToolkitColors.OnSurfaceVariant
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight(),
                color = RemoteToolkitColors.Outline
            )

            // ============================================================
            // Right: Content Area / 右侧：内容区
            // ============================================================
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = RemoteToolkitColors.Background
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    when (state.activePage) {
                        RemoteToolkitPage.DASHBOARD -> DashboardScreen(state, viewModel::processIntent)
                        RemoteToolkitPage.SCANNER -> CompatibilityScannerScreen(state, viewModel::processIntent)
                        RemoteToolkitPage.SDK -> ServerSDKScreen(state, viewModel::processIntent)
                        RemoteToolkitPage.DEBUG -> DebugPanelScreen(state, viewModel::processIntent)
                        RemoteToolkitPage.RPC -> RPCFrameworkScreen(state, viewModel::processIntent)
                        RemoteToolkitPage.SECURITY -> SecurityLayerScreen(state, viewModel::processIntent)
                        RemoteToolkitPage.VERSION -> VersionManagerScreen(state, viewModel::processIntent)
                        RemoteToolkitPage.PERFORMANCE -> PerformanceAnalyzerScreen(state, viewModel::processIntent)
                        RemoteToolkitPage.AI_PIPELINE -> AIPipelineScreen(state, viewModel::processIntent)
                        RemoteToolkitPage.AB_TESTING -> ABTestingScreen(state, viewModel::processIntent)
                    }
                }
            }
        }
    }
}


