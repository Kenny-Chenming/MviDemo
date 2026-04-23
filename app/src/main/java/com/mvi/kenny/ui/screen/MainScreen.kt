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
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.mvi.kenny.feature.swiftpmmigration.SwiftPMMigrationScreen
import com.mvi.kenny.feature.aapmmonitor.AAPMonitorScreen
import com.mvi.kenny.feature.aapmmonitor.AAPMonitorViewModel
import com.mvi.kenny.feature.paging35.PagingToolHostScreen
import com.mvi.kenny.feature.compose111.Compose111Screen
import com.mvi.kenny.navigation.BottomNavRoute

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit
) {
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
        BottomNavRoute.SwiftPMMigration,
        BottomNavRoute.AAPMonitor,
        BottomNavRoute.Paging35,
        BottomNavRoute.Compose111
    )

    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })

    var homeTopBar by remember { mutableStateOf(TopBarConfig(title = "Home")) }
    var listTopBar by remember { mutableStateOf(TopBarConfig(title = "List")) }
    var profileTopBar by remember { mutableStateOf(TopBarConfig(title = "Profile")) }
    var aiAgentTopBar by remember { mutableStateOf(TopBarConfig(title = "AI Agent")) }
    var qaFrameworkTopBar by remember { mutableStateOf(TopBarConfig(title = "QA 框架")) }
    var mcpTopBar by remember { mutableStateOf(TopBarConfig(title = "MCP Server")) }
    var appFuncTopBar by remember { mutableStateOf(TopBarConfig(title = "AppFunctions 工具台")) }
    var nav3ToolTopBar by remember { mutableStateOf(TopBarConfig(title = "Navigation 3 迁移工具")) }
    var page16KbTopBar by remember { mutableStateOf(TopBarConfig(title = "16KB 迁移助手")) }
    var swiftPMMigrationTopBar by remember { mutableStateOf(TopBarConfig(title = "SwiftPM 迁移助手")) }
    var aapmMonitorTopBar by remember { mutableStateOf(TopBarConfig(title = "AAPM 检测工具")) }
    var paging35TopBar by remember { mutableStateOf(TopBarConfig(title = "Paging 3.5 工具包")) }
    var compose111TopBar by remember { mutableStateOf(TopBarConfig(title = "Compose 1.11 变更检测")) }

    val aapmViewModel: AAPMonitorViewModel = viewModel()

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
        9 -> swiftPMMigrationTopBar
        10 -> aapmMonitorTopBar
        11 -> paging35TopBar
        12 -> compose111TopBar
        else -> homeTopBar
    }

    var pendingTabToSelect by remember { mutableIntStateOf(-1) }

    LaunchedEffect(pendingTabToSelect) {
        if (pendingTabToSelect >= 0) {
            pagerState.animateScrollToPage(pendingTabToSelect)
            pendingTabToSelect = -1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTopBar.title) },
                actions = {
                    currentTopBar.actions.forEach { action ->
                        IconButton(onClick = action.onClick) {
                            Icon(imageVector = action.icon, contentDescription = action.contentDescription)
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
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> HomeScreen(onUpdateTopBar = { homeTopBar = it })
                    1 -> ListScreen(onUpdateTopBar = { listTopBar = it })
                    2 -> ProfileScreen(onNavigateToLogin = onNavigateToLogin, onUpdateTopBar = { profileTopBar = it })
                    3 -> AIAgentScreen(onUpdateTopBar = { aiAgentTopBar = it })
                    4 -> QAFrameworkScreen(onUpdateTopBar = { qaFrameworkTopBar = it })
                    5 -> McpScreen(onUpdateTopBar = { mcpTopBar = it })
                    6 -> AppFuncDesignToolScreen(onUpdateTopBar = { appFuncTopBar = it })
                    7 -> NavToolScreen(onNavigateTo = { })
                    8 -> Page16KbScreen(onNavigateBack = { pendingTabToSelect = 0 })
                    9 -> SwiftPMMigrationScreen(onNavigateTo = { })
                    10 -> AAPMonitorScreen(
                        state = aapmViewModel.state.collectAsState().value,
                        onIntent = aapmViewModel::processIntent,
                        modifier = Modifier.fillMaxSize()
                    )
                    11 -> PagingToolHostScreen(
                        onNavigateToTool = { }
                    )
                    12 -> Compose111Screen(
                        onUpdateTopBar = { compose111TopBar = it }
                    )
                }
            }
        }
    }
}
