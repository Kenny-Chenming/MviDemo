package com.mvi.kenny.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ============================================================
 * BottomNavRoute — 底部导航 Tab 路由配置
 * ============================================================
 * 定义底部导航栏的每一个 Tab，包含路由名称、显示标题和图标。
 *
 * @param route NavHost 中对应的路由标识符（字符串）
 * @param title Tab 显示的中文标题
 * @param icon Tab 图标（ImageVector）
 *
 * @see BottomNavRoute.Home 首页 Tab
 * @see BottomNavRoute.List 列表页 Tab
 * @see BottomNavRoute.Profile 个人中心 Tab
 */
sealed class BottomNavRoute(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    /** 首页 Tab */
    data object Home : BottomNavRoute(
        route = "home",
        title = "首页",
        icon = Icons.Default.Home
    )

    /** 列表页 Tab */
    data object List : BottomNavRoute(
        route = "list",
        title = "列表",
        icon = Icons.AutoMirrored.Filled.List
    )

    /** 个人中心 Tab */
    data object Profile : BottomNavRoute(
        route = "profile",
        title = "我的",
        icon = Icons.Default.Person
    )

    /** AI 聊天 Tab */
    data object Chat : BottomNavRoute(
        route = "chat",
        title = "AI 助手",
        icon = Icons.AutoMirrored.Filled.Chat
    )

    /** 动画展示 Tab */
    data object Animation : BottomNavRoute(
        route = "animation",
        title = "动画",
        icon = Icons.Default.Star
    )

    /** 位置权限管控 Tab (PRD-008) */
    data object LocationPermission : BottomNavRoute(
        route = "location_permission",
        title = "位置权限",
        icon = Icons.Default.Shield
    )
}

/**
 * ============================================================
 * NavRoutes — 导航路由常量
 * ============================================================
 * 集中管理所有路由的字符串常量，方便在 NavHost 和 NavigationBarItem 中引用。
 *
 * 注意：
 * - 这里的路由字符串必须与 HorizontalPager 的 page index 对应
 * - 0 = home, 1 = list, 2 = profile, 3 = chat（见 MainScreen.kt）
 *
 * @see com.mvi.kenny.ui.screen.MainScreen 中的 Pager 实现
 */
object NavRoutes {
    /** 首页路由 */
    const val HOME = "home"

    /** 列表页路由 */
    const val LIST = "list"

    /** 个人中心路由 */
    const val PROFILE = "profile"

    /** AI 聊天页路由 */
    const val CHAT = "chat"

    /** 动画展示页路由 */
    const val ANIMATION = "animation"

    /** 位置权限管控页路由 (PRD-008) */
    const val LOCATION_PERMISSION = "location_permission"

    /** 登录页路由（用于 Intent 跳转） */
    const val LOGIN = "login"
}
