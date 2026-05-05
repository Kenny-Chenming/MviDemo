package com.mvi.kenny.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.rounded.GridOn
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
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Layers
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
    // PRD-209: Android 开发者验证合规工具包（2026年9月大限）
    data object DevVerifyTool : BottomNavRoute(route = "dev_verify_tool", title = "验证工具", icon = Icons.Default.VerifiedUser)
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
    // PRD-104: Android Studio Panda 4 Gemini 单元测试生成工具包
    data object GeminiTestQuality : BottomNavRoute(route = "gemini_test_quality", title = "Gemini测试", icon = Icons.Default.FactCheck)
    // PRD-106: Android 16 细粒度健康权限迁移检测与合规工具包
    data object HealthPermissions : BottomNavRoute(route = "health_permissions", title = "健康权限", icon = Icons.Default.Shield)
    // PRD-141: Compose Grid + FlexBox 双布局 API 开发工具包
    data object ComposeLayoutsKit : BottomNavRoute(route = "compose_layouts_kit", title = "Grid/FlexBox", icon = Icons.Rounded.GridOn)
    // PRD-136: Android Studio Panda 4 AI 编程助手开发工具包
    data object PandaTools : BottomNavRoute(route = "panda_tools", title = "Panda 4 Tools", icon = Icons.Default.AutoAwesome)
    // PRD-150: Android 17 OnAlarmListener 电池优化与后台任务调度开发工具包
    data object OnAlarm : BottomNavRoute(route = "onalaarmlistener", title = "Battery", icon = Icons.Default.BatteryChargingFull)
    // PRD-151: Android 17 App Memory Limits 内存限制检测与调优开发工具包
    data object MemoryLimits : BottomNavRoute(route = "memory_limits", title = "Memory Limits", icon = Icons.Default.Memory)
    // PRD-155: Android 17 大屏强制适配与 Continuous Canary Release 开发工具包
    data object LargeScreenAdaptation : BottomNavRoute(route = "large_screen_adaptation", title = "大屏适配", icon = Icons.Default.SwapHoriz)
    // PRD-169: Android Agentic AI AppFunctions & UI Automation Framework 开发工具包
    data object AgenticAI : BottomNavRoute(route = "agentic_ai", title = "AgenticAI", icon = Icons.Default.AutoAwesome)
    // PRD-171: Android 17 Key Limit 合规检测与数据重构工具包
    data object KeyVault : BottomNavRoute(route = "key_vault", title = "KeyVault", icon = Icons.Default.VpnKey)
    // PRD-170: Android 17 Desktop Mode 开发工具包
    data object DesktopMode : BottomNavRoute(route = "desktop_mode", title = "Desktop Mode", icon = Icons.Default.DesktopWindows)
    // PRD-178: Android 17 Background Audio Hardening 合规检测工具包
    data object BackgroundAudio : BottomNavRoute(route = "background_audio", title = "Background Audio", icon = Icons.Default.VolumeUp)
    // PRD-160: Android 17 Background Audio Hardening 后台音频加固迁移工具包
    data object BackgroundAudioHardening : BottomNavRoute(route = "background_audio_hardening", title = "Audio加固", icon = Icons.Default.Shield)
    // PRD-183: Android 17 Large Screen Resizability & Orientation Enforcement 合规检测工具包
    data object OrientationEnforcement : BottomNavRoute(route = "orientation_enforcement", title = "大屏方向锁定", icon = Icons.Default.SwapHoriz)
    // PRD-184: Android CLI & Android Skills 工具包
    data object AndroidSkills : BottomNavRoute(route = "android_skills", title = "Android Skills", icon = Icons.Default.AutoAwesome)
    // PRD-185: Android AppFunctions SDK 开发工具包
    data object AppFunctionTest : BottomNavRoute(route = "app_function_test", title = "SDK 测试", icon = Icons.Default.CheckCircle)
    // PRD-186: Android 17 SMS OTP Delay 合规检测与迁移工具包
    data object OtpDelay : BottomNavRoute(route = "otp_delay", title = "OTP Delay", icon = Icons.Default.Shield)
    // PRD-210: Google Play 2026年4月政策三连击合规工具包
    data object Prd210Compliance : BottomNavRoute(route = "prd210_compliance", title = "Play合规", icon = Icons.Default.Policy)
    // PRD-212: Room 3.0 破坏性变更迁移工具包
    data object Room3Migration : BottomNavRoute(route = "room3_migration", title = "Room3迁移", icon = Icons.Default.Layers)
    // PRD-225: Room 3.0 Import 批量迁移工具包（import扫描/SQLiteDriver/suspend/双版本兼容/CI合规）
    data object Room3ImportMigration : BottomNavRoute(route = "room3_import_migration", title = "Import迁移", icon = Icons.Default.SwapHoriz)
    // PRD-213: Android 17 设备 RAM 内存限制适配工具包
    data object Android17Memory : BottomNavRoute(route = "android17_memory", title = "内存限制", icon = Icons.Default.Memory)
    // PRD-214: Android AppFunctions 开发工具包
    data object AppFunctionsTool : BottomNavRoute(route = "appfunctions_tool", title = "AppFunctions", icon = Icons.Default.DeveloperBoard)
    // PRD-220: Android 17 API 37 破坏性变更综合迁移工具包
    data object Android17Api37Tool : BottomNavRoute(route = "android17_api37_tool", title = "API 37工具", icon = Icons.Default.SystemUpdate)
    // PRD-227: Aluminium OS Android App 桌面适配工具包
    data object AluminiumOSDesktop : BottomNavRoute(route = "aluminium_os_desktop", title = "Aluminium OS", icon = Icons.Default.DesktopWindows)
    // PRD-228: Jetpack Compose 1.11 Testing v2 API 迁移工具包
    data object ComposeTestingV2 : BottomNavRoute(route = "compose_testing_v2", title = "Compose v2", icon = Icons.Default.DeveloperBoard)
    // PRD-229: KSP1→KSP2 迁移工具包
    data object KSP2Migration : BottomNavRoute(route = "ksp2_migration", title = "KSP2迁移", icon = Icons.Default.SystemUpdateAlt)
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
    // PRD-209: Android 开发者验证合规工具包
    const val DEV_VERIFY_TOOL = "dev_verify_tool"
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
    // PRD-098: Gemma 4 × Android Studio Agent Mode 本地编码 Agent 工具链
    const val GEMMA4 = "gemma4"
    // PRD-104: Android Studio Panda 4 Gemini 单元测试生成工具包
    const val GEMINI_TEST_QUALITY = "gemini_test_quality"
    // PRD-106: Android 16 细粒度健康权限迁移检测与合规工具包
    const val HEALTH_PERMISSIONS = "health_permissions"
    // PRD-141: Compose Grid + FlexBox 双布局 API 开发工具包
    const val COMPOSE_LAYOUTS_KIT = "compose_layouts_kit"
    // PRD-136: Android Studio Panda 4 AI 编程助手开发工具包
    const val PANDA_TOOLS = "panda_tools"
    // PRD-150: Android 17 OnAlarmListener 电池优化与后台任务调度开发工具包
    const val ON_ALARM = "onalaarmlistener"
    // PRD-151: Android 17 App Memory Limits 内存限制检测与调优开发工具包
    const val MEMORY_LIMITS = "memory_limits"
    // PRD-155: Android 17 大屏强制适配与 Continuous Canary Release 开发工具包
    const val LARGE_SCREEN_ADAPTATION = "large_screen_adaptation"
    // PRD-169: Android Agentic AI AppFunctions & UI Automation Framework 开发工具包
    const val AGENTIC_AI = "agentic_ai"
    // PRD-171: Android 17 Key Limit 合规检测与数据重构工具包
    const val KEY_VAULT = "key_vault"
    // PRD-170: Android 17 Desktop Mode 开发工具包
    const val DESKTOP_MODE = "desktop_mode"
    // PRD-178: Android 17 Background Audio Hardening 合规检测工具包
    const val BACKGROUND_AUDIO = "background_audio"
    // PRD-160: Android 17 Background Audio Hardening 后台音频加固迁移工具包
    const val BACKGROUND_AUDIO_HARDENING = "background_audio_hardening"
    // PRD-183: Android 17 Large Screen Resizability & Orientation Enforcement 合规检测工具包
    const val ORIENTATION_ENFORCEMENT = "orientation_enforcement"
    // PRD-184: Android CLI & Android Skills 工具包
    const val ANDROID_SKILLS = "android_skills"
    // PRD-185: Android AppFunctions SDK 开发工具包
    const val APP_FUNCTION_TEST = "app_function_test"
    // PRD-186: Android 17 SMS OTP Delay 合规检测与迁移工具包
    const val OTP_DELAY = "otp_delay"
    // PRD-210: Google Play 2026年4月政策三连击合规工具包
    const val PRD210_COMPLIANCE = "prd210_compliance"
    // PRD-212: Room 3.0 破坏性变更迁移工具包
    const val ROOM3_MIGRATION = "room3_migration"
    // PRD-213: Android 17 设备 RAM 内存限制适配工具包
    const val ANDROID17_MEMORY = "android17_memory"
    // PRD-214: Android AppFunctions 开发工具包
    const val APP_FUNCTIONS_TOOL = "appfunctions_tool"
    // PRD-220: Android 17 API 37 破坏性变更综合迁移工具包
    const val ANDROID17_API37_TOOL = "android17_api37_tool"
    // PRD-227: Aluminium OS Android App 桌面适配工具包
    const val ALUMINIUM_OS_DESKTOP = "aluminium_os_desktop"
    // PRD-228: Jetpack Compose 1.11 Testing v2 API 迁移工具包
    const val COMPOSE_TESTING_V2 = "compose_testing_v2"
    // PRD-229: KSP1→KSP2 迁移工具包
    const val KSP2_MIGRATION = "ksp2_migration"
}
