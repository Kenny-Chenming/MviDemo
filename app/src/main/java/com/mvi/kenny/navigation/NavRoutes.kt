package com.mvi.kenny.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
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
    data object AppFuncDesignTool : BottomNavRoute(route = "app_func_design_tool", title = "AppFunctions", icon = Icons.Default.DeveloperBoard)
    data object Nav3Tool : BottomNavRoute(route = "nav3_tool", title = "Nav3迁移", icon = Icons.AutoMirrored.Filled.List)
    data object Page16Kb : BottomNavRoute(route = "page16kb", title = "16KB迁移", icon = Icons.Default.Memory)
    data object WearOs64Bit : BottomNavRoute(route = "wearos64bit", title = "WearOS 64位", icon = Icons.Default.Watch)
    data object SwiftPMMigration : BottomNavRoute(route = "swiftpm_migration", title = "SwiftPM迁移", icon = Icons.AutoMirrored.Filled.ArrowForward)
    // PRD-078: Android Developer Verification Compliance Toolkit
    data object DevVerification : BottomNavRoute(route = "dev_verification", title = "Dev验证", icon = Icons.Default.VerifiedUser)
    // PRD-081: Android 17 Location Button Jetpack Library
    data object LocationButton : BottomNavRoute(route = "location_button", title = "Location Button", icon = Icons.Default.LocationOn)
    // PRD-082: AGP 9.0 KMP NDK/C++ Migration Toolkit
    data object AGP9Migration : BottomNavRoute(route = "agp9_migration", title = "AGP9迁移", icon = Icons.Default.SystemUpdateAlt)
    // PRD-080: Android Auto Car App Library API Level 3 Vehicle Data Toolkit
    data object CarData : BottomNavRoute(route = "car_data", title = "车辆数据", icon = Icons.Default.DirectionsCar)
    // PRD-092: Android 17 App Bubbles Floating Window Toolkit
    data object AppBubbles : BottomNavRoute(route = "app_bubbles", title = "App Bubbles", icon = Icons.Default.Widgets)
    // PRD-091: Android 17 Cross-Device Handoff API 开发者接入工具包
    data object Handoff : BottomNavRoute(route = "handoff", title = "Handoff", icon = Icons.Default.Share)
    // PRD-098: Gemma 4 × Android Studio Agent Mode 本地编码 Agent 工具链
    data object Gemma4 : BottomNavRoute(route = "gemma4", title = "Gemma 4", icon = Icons.Default.AutoAwesome)
    // PRD-115: AndroidX core-ktx 历史性迁移检测与自动化工具包
    data object CoreKtx : BottomNavRoute(route = "core_ktx", title = "core-ktx迁移", icon = Icons.Default.Shield)
    // PRD-099: Journeys E2E Testing Toolkit
    data object Journeys : BottomNavRoute(route = "journeys", title = "Journeys", icon = Icons.Default.BugReport)
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
    const val APP_FUNC_DESIGN_TOOL = "app_func_design_tool"
    const val NAV3_TOOL = "nav3_tool"
    const val PAGE16KB = "page16kb"
    const val WEAROS64BIT = "wearos64bit"
    const val SWIFTPM_MIGRATION = "swiftpm_migration"
    const val LOGIN = "login"
    // PRD-078: Android Developer Verification Compliance Toolkit
    const val DEV_VERIFICATION = "dev_verification"
    const val DEV_VERIFICATION_WIZARD = "dev_verification_wizard"
    const val DEV_VERIFICATION_MDM = "dev_verification_mdm"
    const val DEV_VERIFICATION_SETTINGS = "dev_verification_settings"
    // PRD-081: Android 17 Location Button Jetpack Library
    const val LOCATION_BUTTON = "location_button"
    // PRD-082: AGP 9.0 KMP NDK/C++ Migration Toolkit
    const val AGP9_MIGRATION = "agp9_migration"
    // PRD-080: Android Auto Car App Library API Level 3 Vehicle Data Toolkit
    const val CAR_DATA = "car_data"
    // PRD-092: Android 17 App Bubbles Floating Window Toolkit
    const val APP_BUBBLES = "app_bubbles"
    // PRD-091: Android 17 Cross-Device Handoff API 开发者接入工具包
    const val HANDOFF = "handoff"
    // PRD-115: AndroidX core-ktx 历史性迁移检测与自动化工具包
    const val CORE_KTX = "core_ktx"
    // PRD-098: Gemma 4 × Android Studio Agent Mode 本地编码 Agent 工具链
    const val GEMMA4 = "gemma4"
    // PRD-099: Journeys E2E Testing Toolkit
    const val JOURNEYS = "journeys"
    const val JOURNEY_EDITOR = "journey_editor"
    const val JOURNEY_RESULT = "journey_result"
    const val JOURNEY_TEMPLATE_LIBRARY = "journey_template_library"
    const val JOURNEY_CI_CONFIG = "journey_ci_config"
    const val JOURNEY_FRAMEWORK_COMPARISON = "journey_framework_comparison"
}
