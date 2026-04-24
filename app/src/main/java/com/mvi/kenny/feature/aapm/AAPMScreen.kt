package com.mvi.kenny.feature.aapm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.aapm.screens.AAPMStatusScreen
import com.mvi.kenny.feature.aapm.screens.AuditLogScreen
import com.mvi.kenny.feature.aapm.screens.ComplianceReportScreen
import com.mvi.kenny.feature.aapm.screens.MigrationGuideScreen
import com.mvi.kenny.feature.aapm.screens.ServiceImpactScreen

// =============================================================
// AAPMScreen — AAPM 主屏幕容器
// =============================================================
/**
 * AAPM Main Screen / AAPM 主屏幕容器
 *
 * Container for all AAPM (Advanced Protection Manager) dashboard screens.
 * Provides bottom navigation with 5 tabs:
 * - Status: AAPM status overview
 * - Impact: Service impact analysis
 * - Guide: Migration guide
 * - Audit: Audit logs
 * - Report: Compliance report
 *
 * @param viewModel AAPM ViewModel / AAPM ViewModel
 * @param onNavigateToMigration Callback for navigation to migration guide / 导航到迁移指南的回调
 */
@Composable
fun AAPMScreen(
    viewModel: AAPMViewModel,
    onNavigateToMigration: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AAPMDashboardEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is AAPMDashboardEffect.ShowCompliancePassed -> {
                    snackbarHostState.showSnackbar("✅ 合规检查通过 / Compliance check passed")
                }
                is AAPMDashboardEffect.ShowComplianceFailed -> {
                    snackbarHostState.showSnackbar("⚠️ 合规检查失败: ${effect.count} 个问题 / Compliance check failed: ${effect.count} issues")
                }
                is AAPMDashboardEffect.ShowExportSuccess -> {
                    snackbarHostState.showSnackbar("✅ 导出成功: ${effect.path}")
                }
                is AAPMDashboardEffect.ShowScanError -> {
                    snackbarHostState.showSnackbar("❌ 扫描错误: ${effect.message}")
                }
                is AAPMDashboardEffect.NavigateToMigration -> {
                    onNavigateToMigration(effect.serviceName)
                }
            }
        }
    }

    // Initial status check / 初始状态检查
    LaunchedEffect(Unit) {
        viewModel.processIntent(AAPMDashboardIntent.CheckAAPMStatus)
        viewModel.processIntent(AAPMDashboardIntent.ScanAccessibilityServices)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AAPMBottomNavigation(
                activeTab = state.activeTab,
                onTabSelected = { tab ->
                    viewModel.processIntent(AAPMDashboardIntent.SelectTab(tab))
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Content based on active tab / 基于活跃 Tab 的内容
            when (state.activeTab) {
                AAPMTab.STATUS -> {
                    AAPMStatusScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                }
                AAPMTab.IMPACT -> {
                    ServiceImpactScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                }
                AAPMTab.GUIDE -> {
                    MigrationGuideScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                }
                AAPMTab.AUDIT -> {
                    AuditLogScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                }
                AAPMTab.REPORT -> {
                    ComplianceReportScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                }
            }
        }
    }
}

// =============================================================
// AAPMBottomNavigation — AAPM 底部导航栏
// =============================================================
@Composable
private fun AAPMBottomNavigation(
    activeTab: AAPMTab,
    onTabSelected: (AAPMTab) -> Unit
) {
    NavigationBar {
        AAPMTab.values().forEach { tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            AAPMTab.STATUS -> Icons.Rounded.Shield
                            AAPMTab.IMPACT -> Icons.Rounded.Analytics
                            AAPMTab.GUIDE -> Icons.Rounded.MenuBook
                            AAPMTab.AUDIT -> Icons.Rounded.FactCheck
                            AAPMTab.REPORT -> Icons.Rounded.Description
                        },
                        contentDescription = tab.title
                    )
                },
                label = { Text(tab.title) },
                selected = activeTab == tab,
                onClick = { onTabSelected(tab) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
