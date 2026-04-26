package com.mvi.kenny.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.feature.home.HomeScreen
import com.mvi.kenny.feature.list.ListScreen
import com.mvi.kenny.feature.profile.ProfileScreen
import com.mvi.kenny.feature.mcp.McpScreen
import com.mvi.kenny.feature.android17migration.MigrationDashboardScreen
import com.mvi.kenny.feature.aiagent.AIAgentScreen
import com.mvi.kenny.feature.qaframework.QAFrameworkScreen
import com.mvi.kenny.feature.appfunctions.AppFuncDesignToolScreen
import com.mvi.kenny.feature.nav3tool.NavToolScreen
import com.mvi.kenny.feature.page16kb.Page16KbScreen
import com.mvi.kenny.feature.wearos64bit.WearOs64BitScreen
import com.mvi.kenny.feature.swiftpmmigration.SwiftPMMigrationScreen
import com.mvi.kenny.feature.devverification.ComplianceDashboardScreen
import com.mvi.kenny.feature.locationbutton.LocationButtonScreen
import com.mvi.kenny.feature.agp9migration.AGP9MigrationScreen
import com.mvi.kenny.feature.cardatal.CarDataScreen
import com.mvi.kenny.feature.bubbles.BubblesMainScreen
import com.mvi.kenny.feature.gemma4.Gemma4Screen
import com.mvi.kenny.feature.geminitest.GeminiTestQualityScreen
import com.mvi.kenny.feature.healthpermissions.HealthPermissionsScreen
import com.mvi.kenny.feature.gridflexboxkit.ComposeLayoutsKitScreen
import com.mvi.kenny.feature.gridflexboxkit.ComposeLayoutsKitViewModel
import com.mvi.kenny.feature.pandatools.PandaToolsScreen
import com.mvi.kenny.feature.pandatools.PandaToolsViewModel
import com.mvi.kenny.feature.onalaarmlistener.OnAlarmScreen
import com.mvi.kenny.feature.onalaarmlistener.OnAlarmViewModel
import com.mvi.kenny.feature.memorylimits.MemoryLimitsScreen
import com.mvi.kenny.feature.memorylimits.MemoryLimitsViewModel
import com.mvi.kenny.feature.largescreen.LargeScreenScreen
import com.mvi.kenny.feature.largescreen.LargeScreenViewModel
import com.mvi.kenny.navigation.BottomNavRoute

/**
 * ============================================================
 * MainScreen — App 主界面容器
 * ============================================================
 * App 的唯一入口页面（MainActivity 渲染此组件）。
 *
 * 架构设计：
 * —————————————————————————————————————————————————————
 * 整个页面采用"共享 TopAppBar + HorizontalPager + BottomNavigationBar"的结构。
 *
 *                  ┌──────────────────────────────────┐
 *                  │        TopAppBar（共享）          │
 *                  │  标题随 Tab 切换 + action 按钮    │
 *                  └──────────────────────────────────┘
 *                  ┌──────────────────────────────────┐
 *                  │                                  │
 *                  │       HorizontalPager             │
 *                  │   (Page 0)  HomeScreen           │
 *                  │   (Page 1)  ListScreen          │
 *                  │   (Page 2)  ProfileScreen       │
 *                  │                                  │
 *                  └──────────────────────────────────┘
 *                  ┌──────────────────────────────────┐
 *                  │       BottomNavigationBar        │
 *                  │   [首页]  [列表]  [我的]        │
 *                  └──────────────────────────────────┘
 *
 * 为什么不用 Navigation Compose NavHost？
 * —————————————————————————————————————————————————————
 * NavHost 适合页面间有层级关系（push/pop）的导航，
 * 但 Tab 间滑动切换用 HorizontalPager 更流畅，
 * 且三个 Tab 同时存在于内存中，切换时不会有重新创建的开销。
 *
 * TopBar 动态更新原理：
 * 每个子页面（HomeScreen 等）通过 onUpdateTopBar 回调，
 * 把自己的 TopBarConfig 传给 MainScreen，
 * MainScreen 在 Pager 切换时渲染对应页面的配置。
 *
 * @param onNavigateToLogin 跳转到登录页的回调（Profile 退出登录时触发）
 *
 * @see HomeScreen 首页页面
 * @see ListScreen 列表页面
 * @see ProfileScreen 个人中心页面
 * @see TopBarConfig 顶部导航栏配置
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit
) {
    // 定义底部导航 Tab（按顺序排列，共 14 个）
    val bottomNavItems = listOf(
        BottomNavRoute.Home,
        BottomNavRoute.List,
        BottomNavRoute.Profile,
        BottomNavRoute.AIAgent,
        BottomNavRoute.QAFramework,
        BottomNavRoute.MCP,
        BottomNavRoute.AppFuncDesignTool,
        BottomNavRoute.Nav3Tool,
        BottomNavRoute.Page16Kb,
        BottomNavRoute.WearOs64Bit,
        BottomNavRoute.SwiftPMMigration,
        BottomNavRoute.DevVerification,
        BottomNavRoute.LocationButton,
        BottomNavRoute.AGP9Migration,
        BottomNavRoute.CarData,
        BottomNavRoute.AppBubbles,
        BottomNavRoute.Gemma4,
        BottomNavRoute.GeminiTestQuality,
        BottomNavRoute.HealthPermissions,
        BottomNavRoute.ComposeLayoutsKit,
        BottomNavRoute.PandaTools,
        // PRD-150: Android 17 OnAlarmListener 电池优化与后台任务调度开发工具包
        BottomNavRoute.OnAlarm,
        // PRD-151: Android 17 App Memory Limits 内存限制检测与调优开发工具包
        BottomNavRoute.MemoryLimits,
        // PRD-155: Android 17 大屏强制适配与 Continuous Canary Release 开发工具包
        BottomNavRoute.LargeScreenAdaptation,
        // PRD-153: Handoff 待修复: BottomNavRoute.Handoff 已加入 NavRoutes，但 MainScreen 集成缺失（Bug #2），待修复后启用
        // PRD-161: Compose 1.11 Layout & Style APIs 开发工具包
        BottomNavRoute.Compose11Layouts
    )

    // Pager 状态，管理当前是第几页
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })

    // ============================================================
    // 各页面的 TopBar 配置（通过子组件的 onUpdateTopBar 回调设置）
    // ============================================================
    var homeTopBar by remember { mutableStateOf(TopBarConfig(title = "Home")) }
    var listTopBar by remember { mutableStateOf(TopBarConfig(title = "List")) }
    var profileTopBar by remember { mutableStateOf(TopBarConfig(title = "Profile")) }
    var aiAgentTopBar by remember { mutableStateOf(TopBarConfig(title = "AI Agent")) }
    var qaFrameworkTopBar by remember { mutableStateOf(TopBarConfig(title = "QA 框架")) }
    var mcpTopBar by remember { mutableStateOf(TopBarConfig(title = "MCP Server")) }
    var appFuncTopBar by remember { mutableStateOf(TopBarConfig(title = "AppFunctions 工具台")) }
    var nav3ToolTopBar by remember { mutableStateOf(TopBarConfig(title = "Nav3 迁移工具")) }
    var page16KbTopBar by remember { mutableStateOf(TopBarConfig(title = "16KB 迁移助手")) }
    var wearOs64BitTopBar by remember { mutableStateOf(TopBarConfig(title = "Wear OS 64位合规")) }
    var swiftPMMigrationTopBar by remember { mutableStateOf(TopBarConfig(title = "SwiftPM 迁移助手")) }
    var devVerificationTopBar by remember { mutableStateOf(TopBarConfig(title = "Dev Verification")) }
    var locationButtonTopBar by remember { mutableStateOf(TopBarConfig(title = "Location Button")) }
    var agp9MigrationTopBar by remember { mutableStateOf(TopBarConfig(title = "AGP 9.0 迁移")) }
    var carDataTopBar by remember { mutableStateOf(TopBarConfig(title = "车辆数据 / Car Data")) }
    var appBubblesTopBar by remember { mutableStateOf(TopBarConfig(title = "App Bubbles · 接入引导")) }
    var gemma4TopBar by remember { mutableStateOf(TopBarConfig(title = "Gemma 4 Agent Toolkit")) }
    var geminiTestQualityTopBar by remember { mutableStateOf(TopBarConfig(title = "Gemini 测试质量中心")) }
    var healthPermissionsTopBar by remember { mutableStateOf(TopBarConfig(title = "健康权限合规")) }
    // PRD-141: Compose Grid + FlexBox 双布局 API 开发工具包
    val composeLayoutsKitViewModel = remember { ComposeLayoutsKitViewModel() }
    var composeLayoutsKitTopBar by remember { mutableStateOf(TopBarConfig(title = "ComposeLayoutsKit")) }
    // PRD-136: Android Studio Panda 4 AI 编程助手开发工具包
    val pandaToolsViewModel = remember { PandaToolsViewModel() }
    var pandaToolsTopBar by remember { mutableStateOf(TopBarConfig(title = "Panda 4 AI Tools")) }
    // PRD-150: Android 17 OnAlarmListener 电池优化与后台任务调度开发工具包
    val onAlarmViewModel = remember { OnAlarmViewModel() }
    var onAlarmTopBar by remember { mutableStateOf(TopBarConfig(title = "Battery / 电池优化")) }
    // PRD-151: Android 17 App Memory Limits 内存限制检测与调优开发工具包
    val memoryLimitsViewModel = remember { MemoryLimitsViewModel() }
    var memoryLimitsTopBar by remember { mutableStateOf(TopBarConfig(title = "Memory Limits")) }
    // PRD-155: Android 17 大屏强制适配与 Continuous Canary Release 开发工具包
    val largeScreenViewModel = remember { LargeScreenViewModel() }
    var largeScreenTopBar by remember { mutableStateOf(TopBarConfig(title = "大屏适配")) }
    // PRD-161: Compose 1.11 Layout & Style APIs 开发工具包
    val compose11LayoutsViewModel = remember { com.mvi.kenny.feature.compose11layouts.Compose11LayoutsViewModel() }
    var compose11LayoutsTopBar by remember { mutableStateOf(TopBarConfig(title = "Compose 1.11 布局 API")) }

    // 根据当前页码决定显示哪个 TopBar 配置
    val currentTopBar = when (pagerState.currentPage) {
        0 -> homeTopBar
        1 -> listTopBar
        2 -> profileTopBar
        3 -> aiAgentTopBar
        4 -> qaFrameworkTopBar
        5 -> mcpTopBar
        6 -> appFuncTopBar
        7 -> nav3ToolTopBar
        8 -> page16KbTopBar
        9 -> wearOs64BitTopBar
        10 -> swiftPMMigrationTopBar
        11 -> devVerificationTopBar
        12 -> locationButtonTopBar
        13 -> agp9MigrationTopBar
        14 -> carDataTopBar
        15 -> appBubblesTopBar
        16 -> gemma4TopBar
        17 -> geminiTestQualityTopBar
        18 -> healthPermissionsTopBar
        19 -> composeLayoutsKitTopBar
        20 -> pandaToolsTopBar
        21 -> onAlarmTopBar
        22 -> memoryLimitsTopBar
        23 -> largeScreenTopBar
        24 -> compose11LayoutsTopBar
        else -> homeTopBar
    }

    // ============================================================
    // 底部 Tab 点击 → 驱动 Pager 切换
    // ============================================================
    var pendingTabToSelect by remember { mutableIntStateOf(-1) }

    LaunchedEffect(pendingTabToSelect) {
        if (pendingTabToSelect >= 0) {
            pagerState.animateScrollToPage(pendingTabToSelect)
            pendingTabToSelect = -1
        }
    }

    // ============================================================
    // 页面结构：Scaffold（TopAppBar + BottomNavigation + Content）
    // ============================================================
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTopBar.title) },
                actions = {
                    currentTopBar.actions.forEach { action ->
                        IconButton(onClick = action.onClick) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.contentDescription,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = pagerState.currentPage == index,
                        onClick = { pendingTabToSelect = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> HomeScreen(onUpdateTopBar = { homeTopBar = it })
                    1 -> ListScreen(onUpdateTopBar = { listTopBar = it })
                    2 -> ProfileScreen(
                        onNavigateToLogin = onNavigateToLogin,
                        onUpdateTopBar = { profileTopBar = it }
                    )
                    3 -> AIAgentScreen(onUpdateTopBar = { aiAgentTopBar = it })
                    4 -> QAFrameworkScreen(onUpdateTopBar = { qaFrameworkTopBar = it })
                    5 -> McpScreen(onUpdateTopBar = { mcpTopBar = it })
                    6 -> AppFuncDesignToolScreen(onUpdateTopBar = { appFuncTopBar = it })
                    7 -> NavToolScreen(onNavigateTo = { /* Feature internal navigation */ })
                    8 -> Page16KbScreen(onNavigateBack = { pendingTabToSelect = 0 })
                    9 -> WearOs64BitScreen(
                        onUpdateTopBar = { wearOs64BitTopBar = it },
                        onNavigateBack = { pendingTabToSelect = 0 }
                    )
                    10 -> SwiftPMMigrationScreen(onNavigateTo = { /* Feature internal navigation */ })
                    11 -> ComplianceDashboardScreen(
                        onNavigateToWizard = { /* internal state nav */ },
                        onNavigateToMDM = { /* internal state nav */ },
                        onNavigateToSettings = { /* internal state nav */ }
                    )
                    12 -> LocationButtonScreen(onUpdateTopBar = { locationButtonTopBar = it })
                    13 -> AGP9MigrationScreen()
                    14 -> CarDataScreen(
                        onUpdateTopBar = { carDataTopBar = it }
                    )
                    15 -> BubblesMainScreen(
                        onUpdateTopBar = { appBubblesTopBar = it }
                    )
                    16 -> Gemma4Screen(
                        onUpdateTopBar = { gemma4TopBar = it }
                    )
                    17 -> GeminiTestQualityScreen(
                        onUpdateTopBar = { geminiTestQualityTopBar = it }
                    )
                    18 -> HealthPermissionsScreen(
                        onUpdateTopBar = { healthPermissionsTopBar = it }
                    )
                    19 -> ComposeLayoutsKitScreen(
                        state = composeLayoutsKitViewModel.state.collectAsState().value,
                        onIntent = composeLayoutsKitViewModel::sendIntent,
                        onNavigateToSubmodule = { }
                    )
                    20 -> PandaToolsScreen(
                        viewModel = pandaToolsViewModel,
                        onNavigateBack = { pendingTabToSelect = 0 }
                    )
                    // PRD-150: Android 17 OnAlarmListener 电池优化与后台任务调度开发工具包
                    21 -> OnAlarmScreen(
                        viewModel = onAlarmViewModel,
                        onNavigateToScanner = { },
                        onNavigateToAnalysis = { }
                    )
                    // PRD-151: Android 17 App Memory Limits 内存限制检测与调优开发工具包
                    22 -> MemoryLimitsScreen(
                        state = memoryLimitsViewModel.state.collectAsState().value,
                        onIntent = memoryLimitsViewModel::sendIntent
                    )
                    // PRD-155: Android 17 大屏强制适配与 Continuous Canary Release 开发工具包
                    23 -> LargeScreenScreen(
                        viewModel = largeScreenViewModel,
                        onUpdateTopBar = { largeScreenTopBar = it },
                        onSnackbar = { }
                    )
                    // PRD-161: Compose 1.11 Layout & Style APIs 开发工具包
                    24 -> com.mvi.kenny.feature.compose11layouts.Compose11LayoutsScreen(
                        state = compose11LayoutsViewModel.state.collectAsState().value,
                        onIntent = compose11LayoutsViewModel::sendIntent
                    )
                }
            }
        }
    }
}
