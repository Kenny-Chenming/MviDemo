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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.feature.animation.AnimationScreen
import com.mvi.kenny.feature.chat.ChatScreen
import com.mvi.kenny.feature.home.HomeScreen
import com.mvi.kenny.feature.list.ListScreen
import com.mvi.kenny.feature.locationpermission.LocationPermissionScreen
import com.mvi.kenny.feature.mcp.McpScreen
import com.mvi.kenny.feature.aiagent.AIAgentScreen
import com.mvi.kenny.feature.profile.ProfileScreen
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
 *                  │   [首页]  [列表]  [我的]          │
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
    // 定义七个 Tab 的路由配置
    val bottomNavItems = listOf(
        BottomNavRoute.Home,
        BottomNavRoute.List,
        BottomNavRoute.Profile,
        BottomNavRoute.Chat,
        BottomNavRoute.Animation,
        BottomNavRoute.LocationPermission,
        BottomNavRoute.MCP,
        BottomNavRoute.AIAgent
    )

    // Pager 状态，管理当前是第几页
    // pageCount 是 LazyListState 的参数，返回总页数
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })

    // ============================================================
    // 各页面的 TopBar 配置（通过子组件的 onUpdateTopBar 回调设置）
    // ============================================================
    // 初始值，避免在子组件未挂载前 TopAppBar 空白
    var homeTopBar by remember { mutableStateOf(TopBarConfig(title = "Home")) }
    var listTopBar by remember { mutableStateOf(TopBarConfig(title = "List")) }
    var profileTopBar by remember { mutableStateOf(TopBarConfig(title = "Profile")) }
    var chatTopBar by remember { mutableStateOf(TopBarConfig(title = "AI Chat")) }
    var animationTopBar by remember { mutableStateOf(TopBarConfig(title = "Animation")) }
    var locationPermissionTopBar by remember { mutableStateOf(TopBarConfig(title = "位置权限")) }
    var mcpTopBar by remember { mutableStateOf(TopBarConfig(title = "MCP Server")) }
    var aiAgentTopBar by remember { mutableStateOf(TopBarConfig(title = "AI Agent")) }

    // 根据当前页码决定显示哪个 TopBar 配置
    val currentTopBar = when (pagerState.currentPage) {
        0 -> homeTopBar
        1 -> listTopBar
        2 -> profileTopBar
        3 -> chatTopBar
        4 -> animationTopBar
        5 -> locationPermissionTopBar
        6 -> mcpTopBar
        else -> aiAgentTopBar
    }

    // ============================================================
    // 底部 Tab 点击 → 驱动 Pager 切换
    // ============================================================
    // pendingTabToSelect 作为中间状态，LaunchedEffect 监听其变化后执行切换
    // 这样避免在 recomposition 期间直接调用 animateScrollToPage
    var pendingTabToSelect by remember { mutableIntStateOf(-1) }

    // LaunchedEffect 监听 pendingTabToSelect 状态，
    // 值为 >= 0 时执行页面切换动画，然后重置为 -1
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
        // 顶部导航栏：标题 + 右侧 action 按钮
        topBar = {
            TopAppBar(
                title = { Text(currentTopBar.title) },
                actions = {
                    // 遍历当前页面的 action 按钮列表，渲染 IconButton
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

        // 底部导航栏：三个 Tab（首页 / 列表 / 我的）
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        // 当前选中的 Tab 高亮（与 Pager 当前页同步）
                        selected = pagerState.currentPage == index,
                        onClick = { pendingTabToSelect = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        // 内容区域：HorizontalPager（支持左右滑动）
        Box(modifier = Modifier.padding(innerPadding)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                // 根据页码渲染对应的子页面
                when (page) {
                    0 -> HomeScreen(
                        // 首页传递 TopBar 更新回调（首页需要设置按钮）
                        onUpdateTopBar = { homeTopBar = it }
                    )
                    1 -> ListScreen(
                        // 列表页传递 TopBar 更新回调（列表页需要刷新按钮）
                        onUpdateTopBar = { listTopBar = it }
                    )
                    2 -> ProfileScreen(
                        onNavigateToLogin = onNavigateToLogin,
                        onUpdateTopBar = { profileTopBar = it }
                    )
                    3 -> ChatScreen(
                        onUpdateTopBar = { chatTopBar = it }
                    )
                    4 -> AnimationScreen(
                        onUpdateTopBar = { animationTopBar = it }
                    )
                    5 -> LocationPermissionScreen(
                        onUpdateTopBar = { locationPermissionTopBar = it }
                    )
                    6 -> McpScreen(
                        onUpdateTopBar = { mcpTopBar = it }
                    )
                    7 -> AIAgentScreen(
                        onUpdateTopBar = { aiAgentTopBar = it }
                    )
                }
            }
        }
    }
}
