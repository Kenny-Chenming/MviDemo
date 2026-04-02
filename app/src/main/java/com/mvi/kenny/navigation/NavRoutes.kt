package com.mvi.kenny.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ============================================================
 * BottomNavRoute — 底部导航 Tab 路由配置
 * ============================================================
 * 定义底部导航栏的每一个 Tab，包含路由名称、显示标题和图标。
 */
sealed class BottomNavRoute(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : BottomNavRoute(route = "home", title = "首页", icon = Icons.Default.Home)
    data object List : BottomNavRoute(route = "list", title = "列表", icon = Icons.AutoMirrored.Filled.List)
    data object Profile : BottomNavRoute(route = "profile", title = "我的", icon = Icons.Default.Person)
    data object Chat : BottomNavRoute(route = "chat", title = "AI 助手", icon = Icons.AutoMirrored.Filled.Chat)
    data object Animation : BottomNavRoute(route = "animation", title = "动画", icon = Icons.Default.Star)
    data object LocationPermission : BottomNavRoute(route = "location_permission", title = "位置权限", icon = Icons.Default.Shield)
    data object MCP : BottomNavRoute(route = "mcp", title = "MCP Server", icon = Icons.Default.DeveloperBoard)
    data object AIAgent : BottomNavRoute(route = "ai_agent", title = "AI Agent", icon = Icons.Default.Memory)
    data object Android17Migration : BottomNavRoute(route = "android17_migration", title = "迁移助手", icon = Icons.Default.SystemUpdate)
    data object QAFramework : BottomNavRoute(route = "qa_framework", title = "QA 框架", icon = Icons.Default.BugReport)
}

/**
 * ============================================================
 * NavRoutes — 导航路由常量
 * ============================================================
 */
object NavRoutes {
    const val HOME = "home"
    const val LIST = "list"
    const val PROFILE = "profile"
    const val CHAT = "chat"
    const val ANIMATION = "animation"
    const val LOCATION_PERMISSION = "location_permission"
    const val MCP = "mcp"
    const val AI_AGENT = "ai_agent"
    const val ANDROID17_MIGRATION = "android17_migration"
    const val QA_FRAMEWORK = "qa_framework"
    const val LOGIN = "login"
}
