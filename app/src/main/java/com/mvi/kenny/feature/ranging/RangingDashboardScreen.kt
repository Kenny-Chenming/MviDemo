package com.mvi.kenny.feature.ranging

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.DeviceHub
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

// =============================================================
// RangingDashboardScreen — UWB 测距工具主界面
// PRD-147: Android 17 UWB Ranging API 跨设备测距开发工具包
// =============================================================
/**
 * Ranging Dashboard Screen / 测距工具主界面
 *
 * Main container for the UWB Ranging API development toolkit.
 * Provides bottom navigation for all 5 screens:
 * 1. Device Discovery / 设备发现
 * 2. Single Device Ranging / 单设备测距
 * 3. Multi-Device Ranging / 多设备测距
 * 4. Technology Comparison / 技术对比
 * 5. Power Analyzer / 功耗分析
 *
 * Features:
 * - Material 3 dark theme / Material 3 深色主题
 * - Bottom navigation with 5 tabs / 5 Tab 底部导航
 * - MVI architecture / MVI 架构
 * - Bilingual UI (Chinese + English) / 双语界面
 *
 * @param context Application context / 应用上下文
 * @param onNavigateToDetail Navigate to detail handler / 导航到详情处理
 *
 * @see RangingViewModel ViewModel for state management
 * @see DeviceDiscoveryScreen Device discovery screen
 * @see RangingDetailScreen Single device ranging detail
 * @see MultiRangingScreen Multi-device ranging
 * @see TechCompareScreen Technology comparison
 * @see PowerAnalyzerScreen Power analysis
 */
@Composable
fun RangingDashboardScreen(
    onNavigateToDetail: () -> Unit = {}
) {
    // ============================================================
    // ViewModel / ViewModel
    // ============================================================
    val context = LocalContext.current
    val viewModel: RangingViewModel = viewModel {
        RangingViewModel(context)
    }
    val state by viewModel.state.collectAsState()

    // ============================================================
    // Snackbar for effects / 副作用 Snackbar
    // ============================================================
    val snackbarHostState = remember { SnackbarHostState() }

    // ============================================================
    // Handle effects / 处理副作用
    // ============================================================
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is RangingEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is RangingEffect.NavigateToDetail -> {
                    onNavigateToDetail()
                }
                is RangingEffect.RangingStarted -> {
                    snackbarHostState.showSnackbar("测距开始 / Ranging started: ${effect.deviceAddress.takeLast(8)}")
                }
                is RangingEffect.RangingStopped -> {
                    snackbarHostState.showSnackbar("测距停止 / Ranging stopped: ${effect.deviceAddress.takeLast(8)}")
                }
                is RangingEffect.UwbNotAvailable -> {
                    snackbarHostState.showSnackbar("UWB 不可用 / UWB not available on this device")
                }
                is RangingEffect.CopyAddress -> {
                    snackbarHostState.showSnackbar("地址已复制 / Address copied: ${effect.address.takeLast(8)}")
                }
            }
        }
    }

    // ============================================================
    // Scaffold with bottom navigation / 带底部导航的 Scaffold
    // ============================================================
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            RangingBottomNavigation(
                activeTab = state.activeTab,
                onTabSelected = { tab ->
                    viewModel.sendIntent(RangingIntent.SelectTab(tab))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ============================================================
            // Active screen content / 当前屏幕内容
            // ============================================================
            when (state.activeTab) {
                RangingTab.DEVICE_DISCOVERY -> {
                    DeviceDiscoveryScreen(
                        state = state,
                        onIntent = viewModel::sendIntent
                    )
                }
                RangingTab.SINGLE_RANGING -> {
                    RangingDetailScreen(
                        state = state,
                        onIntent = viewModel::sendIntent,
                        onNavigateBack = {
                            viewModel.sendIntent(RangingIntent.ClearSelection)
                        }
                    )
                }
                RangingTab.MULTI_RANGING -> {
                    MultiRangingScreen(
                        state = state,
                        onIntent = viewModel::sendIntent
                    )
                }
                RangingTab.TECH_COMPARE -> {
                    TechCompareScreen(
                        state = state,
                        onIntent = viewModel::sendIntent
                    )
                }
                RangingTab.POWER_ANALYZER -> {
                    PowerAnalyzerScreen(
                        state = state,
                        onIntent = viewModel::sendIntent
                    )
                }
            }
        }
    }
}

// =============================================================
// RangingBottomNavigation — 测距工具底部导航
// =============================================================
/**
 * Bottom navigation bar for ranging tool / 测距工具底部导航栏
 *
 * @param activeTab Currently active tab / 当前活跃 Tab
 * @param onTabSelected Tab selection handler / Tab 选择处理
 */
@Composable
private fun RangingBottomNavigation(
    activeTab: RangingTab,
    onTabSelected: (RangingTab) -> Unit
) {
    val items = listOf(
        BottomNavItem(
            tab = RangingTab.DEVICE_DISCOVERY,
            title = "设备发现",
            titleEn = "Discovery",
            icon = Icons.Rounded.DeviceHub
        ),
        BottomNavItem(
            tab = RangingTab.SINGLE_RANGING,
            title = "单设备",
            titleEn = "Single",
            icon = Icons.Rounded.NearMe
        ),
        BottomNavItem(
            tab = RangingTab.MULTI_RANGING,
            title = "多设备",
            titleEn = "Multi",
            icon = Icons.Rounded.Sensors
        ),
        BottomNavItem(
            tab = RangingTab.TECH_COMPARE,
            title = "技术对比",
            titleEn = "Compare",
            icon = Icons.Rounded.CompareArrows
        ),
        BottomNavItem(
            tab = RangingTab.POWER_ANALYZER,
            title = "功耗分析",
            titleEn = "Power",
            icon = Icons.Rounded.Analytics
        )
    )

    NavigationBar(
        containerColor = Color(0xFF1E1E1E)
    ) {
        items.forEach { item ->
            val isSelected = activeTab == item.tab
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) Color(0xFF4FC3F7) else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (isSelected) Color(0xFF4FC3F7) else Color.Gray
                    )
                },
                selected = isSelected,
                onClick = { onTabSelected(item.tab) }
            )
        }
    }
}

/**
 * Bottom navigation item data / 底部导航项数据
 */
private data class BottomNavItem(
    val tab: RangingTab,
    val title: String,
    val titleEn: String,
    val icon: ImageVector
)
