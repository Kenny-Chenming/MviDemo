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
import com.mvi.kenny.feature.agentskillstoolkit.AgentSkillsToolkitScreen
import com.mvi.kenny.feature.agentskillstoolkit.AgentSkillsToolkitViewModel
import com.mvi.kenny.feature.android_cli_agent_toolkit.AndroidCLIExternalAgentToolkitScreen
import com.mvi.kenny.feature.android_cli_agent_toolkit.AndroidCLIExternalAgentToolkitViewModel
import com.mvi.kenny.feature.perappmemorylimits.PerAppMemoryLimitsScreen
import com.mvi.kenny.feature.perappmemorylimits.PerAppMemoryLimitsViewModel
import com.mvi.kenny.feature.devverification.ComplianceDashboardScreen
import com.mvi.kenny.feature.devverifytool.DevVerifyToolScreen
import com.mvi.kenny.feature.prd210compliance.Prd210ComplianceScreen
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
import com.mvi.kenny.feature.keyvault.KeyVaultScreen
import com.mvi.kenny.feature.keyvault.KeyVaultViewModel
import com.mvi.kenny.feature.desktopmode.DesktopModeScreen
import com.mvi.kenny.feature.desktopmode.DesktopModeViewModel
import com.mvi.kenny.feature.bgaudio.BackgroundAudioScreen
import com.mvi.kenny.feature.bgaudio.BackgroundAudioViewModel
import com.mvi.kenny.feature.agenticai.AgenticAIScreen
import com.mvi.kenny.feature.agenticai.AgenticAIViewModel
import com.mvi.kenny.feature.backgroundaudiohardening.BackgroundAudioHardeningScreen
import com.mvi.kenny.feature.backgroundaudiohardening.BackgroundAudioHardeningViewModel
import com.mvi.kenny.feature.orientationenforcement.OrientationEnforcementScreen
import com.mvi.kenny.feature.orientationenforcement.OrientationEnforcementViewModel
import com.mvi.kenny.feature.androidskills.AndroidSkillsScreen
import com.mvi.kenny.feature.androidskills.AndroidSkillsViewModel
import com.mvi.kenny.feature.androidskills.AndroidSkillsIntent
import com.mvi.kenny.feature.appfunctionssdk.AppFunctionTestScreen
import com.mvi.kenny.feature.otpdelay.OtpDelayScreen
import com.mvi.kenny.feature.otpdelay.OtpDelayViewModel
import com.mvi.kenny.feature.room3migration.Room3MigrationScreen
import com.mvi.kenny.feature.room3migration.Room3MigrationViewModel
import com.mvi.kenny.feature.room3importmigration.Room3ImportMigrationScreen
import com.mvi.kenny.feature.room3importmigration.Room3ImportMigrationViewModel
import com.mvi.kenny.feature.android17memory.Android17MemoryScreen
import com.mvi.kenny.feature.android17memory.Android17MemoryViewModel
import com.mvi.kenny.feature.android17memory.Android17MemoryViewModelFactory
import com.mvi.kenny.feature.appfunctionstool.AppFunctionsToolScreen
import com.mvi.kenny.feature.appfunctionstool.AppFunctionsToolViewModel
import com.mvi.kenny.feature.android17api37tool.Android17Api37ToolScreen
import com.mvi.kenny.feature.aluminiumosdesktop.AluminiumOSDesktopScreen
import com.mvi.kenny.feature.aluminiumosdesktop.AluminiumOSDesktopViewModel
import com.mvi.kenny.feature.android17api37tool.Android17Api37ToolViewModel
import com.mvi.kenny.feature.contactpicker.ContactPickerScreen
import com.mvi.kenny.feature.contactpicker.ContactPickerViewModel
import com.mvi.kenny.feature.quailldebugtools.QuailDebugToolsScreen
import com.mvi.kenny.feature.quailldebugtools.QuailDebugToolsViewModel
import com.mvi.kenny.feature.appastool.AppAsToolScreen
import com.mvi.kenny.feature.appastool.AppAsToolViewModel
import com.mvi.kenny.feature.verifiedfinancialcalls.VerifiedFinancialCallsScreen
import com.mvi.kenny.feature.verifiedfinancialcalls.VerifiedFinancialCallsViewModel
// PRD-299: KMP 新默认项目结构迁移工具包
import com.mvi.kenny.feature.kmpnewstructuremigration.KMPNewStructureMigrationScreen
// PRD-280: Android Skills 安全扫描工具包
import com.mvi.kenny.feature.skillssecuritytoolkit.SkillsSecurityToolkitScreen
import com.mvi.kenny.feature.skillssecuritytoolkit.SkillsSecurityToolkitViewModel
// PRD-283: Android MDC-Views → Compose 迁移工具包
import com.mvi.kenny.feature.mdcviewscomposemigration.MdcToComposeToolScreen
import com.mvi.kenny.feature.mdcviewscomposemigration.MdcToComposeToolViewModel
// PRD-289: Android XR SDK DP4 开发工具包
import com.mvi.kenny.feature.xrsdkdevkit.XrDevKitScreen
import com.mvi.kenny.feature.xrsdkdevkit.XrDevKitViewModel
// PRD-292: Google ADK for Android 开发工具包
import com.mvi.kenny.feature.adkandroid.AdkAndroidScreen
import com.mvi.kenny.feature.adkandroid.AdkAndroidViewModel
// PRD-294: Room 3.0 KMP 数据库迁移工具包
import com.mvi.kenny.feature.room3kmpmigration.Room3KmpMigrationScreen
import com.mvi.kenny.feature.room3kmpmigration.Room3KmpMigrationViewModel
// PRD-304: Android 开发者身份验证合规批量管理平台
import com.mvi.kenny.feature.devverificationbatch.VerificationDashboardScreen
import com.mvi.kenny.feature.devverificationbatch.VerificationDashboardViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
        // PRD-169: Android Agentic AI AppFunctions & UI Automation Framework 开发工具包
        BottomNavRoute.AgenticAI,
        // PRD-171: Android 17 Key Limit 合规检测与数据重构工具包
        BottomNavRoute.KeyVault,
        // PRD-170: Android 17 Desktop Mode 开发工具包
        BottomNavRoute.DesktopMode,
        // PRD-178: Android 17 Background Audio Hardening 合规检测工具包
        BottomNavRoute.BackgroundAudio,
        // PRD-160: Android 17 Background Audio Hardening 后台音频加固迁移工具包
        BottomNavRoute.BackgroundAudioHardening,
        // PRD-183: Android 17 Large Screen Resizability & Orientation Enforcement 合规检测工具包
        BottomNavRoute.OrientationEnforcement,
        // PRD-185: Android AppFunctions SDK 开发工具包
        BottomNavRoute.AppFunctionTest,
        // PRD-184: Android CLI & Android Skills 工具包
        BottomNavRoute.AndroidSkills,
        // PRD-186: Android 17 SMS OTP Delay 合规检测与迁移工具包
        BottomNavRoute.OtpDelay,
        // PRD-209: Android 开发者验证合规工具包（2026年9月大限）
        BottomNavRoute.DevVerifyTool,
        // PRD-210: Google Play 2026年4月政策三连击合规工具包
        BottomNavRoute.Prd210Compliance,
        // PRD-212: Room 3.0 破坏性变更迁移工具包
        BottomNavRoute.Room3Migration,
        // PRD-225: Room 3.0 Import 批量迁移工具包
        BottomNavRoute.Room3ImportMigration,
        // PRD-213: Android 17 设备 RAM 内存限制适配工具包
        BottomNavRoute.Android17Memory,
        // PRD-214: Android AppFunctions 开发工具包
        BottomNavRoute.AppFunctionsTool,
        // PRD-220: Android 17 API 37 破坏性变更综合迁移工具包
        BottomNavRoute.Android17Api37Tool,
        // PRD-227: Aluminium OS Android App 桌面适配工具包
        BottomNavRoute.AluminiumOSDesktop,
        // PRD-231: KMP × AGP 9.0 不兼容迁移工具包
        BottomNavRoute.KMPAGP90,
        // PRD-230: Jetpack Compose Glimmer AI 眼镜 UI 开发工具包
        BottomNavRoute.GlimmerToolkit,
        // PRD-233: Android Agent Skills 技能库生态工具包
        BottomNavRoute.AgentSkillsToolkit,
        // PRD-234: Google Play Contact Picker 强制迁移工具包
        BottomNavRoute.ContactPicker,
        // PRD-235: Android 17 Per-App 内存限制检测与优化工具包
        BottomNavRoute.PerAppMemoryLimits,
        // PRD-241: Android CLI × External AI Agent 集成工具包
        BottomNavRoute.AndroidCLIExternalAgentToolkit,
        // PRD-242: Android Studio Quail 调试/性能工具包
        BottomNavRoute.QuailDebugTools,
        // PRD-250: Android AppFunctions App-as-Tool 开发工具包
        BottomNavRoute.AppAsTool,
        // PRD-257: Verified Financial Calls API 集成工具包
        BottomNavRoute.VerifiedFinancialCalls,
        // PRD-280: Android Skills 安全扫描工具包
        BottomNavRoute.AndroidSkillsSecurity,
        // PRD-283: Android MDC-Views → Compose 迁移工具包
        BottomNavRoute.MdcToComposeMigration,
        // PRD-289: Android XR SDK DP4 开发工具包
        BottomNavRoute.XrSdkDevKit,
        // PRD-292: Google ADK for Android 开发工具包
        BottomNavRoute.AdkAndroid,
        // PRD-294: Room 3.0 KMP 数据库迁移工具包
        BottomNavRoute.Room3KmpMigration,
        // PRD-299: KMP 新默认项目结构迁移工具包
        BottomNavRoute.KMPNewStructureMigration,
        // PRD-304: Android 开发者身份验证合规批量管理平台
        BottomNavRoute.DevVerificationBatch
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
    // PRD-169: Android Agentic AI AppFunctions & UI Automation Framework 开发工具包
    val agenticAIViewModel = remember { AgenticAIViewModel() }
    var agenticAITopBar by remember { mutableStateOf(TopBarConfig(title = "AgenticAI")) }
    // PRD-171: Android 17 Key Limit 合规检测与数据重构工具包
    val keyVaultViewModel = remember { KeyVaultViewModel() }
    var keyVaultTopBar by remember { mutableStateOf(TopBarConfig(title = "KeyVault")) }
    // PRD-170: Android 17 Desktop Mode 开发工具包
    val desktopModeViewModel = remember { DesktopModeViewModel() }
    var desktopModeTopBar by remember { mutableStateOf(TopBarConfig(title = "Desktop Mode")) }
    // PRD-178: Android 17 Background Audio Hardening 合规检测工具包
    val backgroundAudioViewModel = remember { BackgroundAudioViewModel() }
    var backgroundAudioTopBar by remember { mutableStateOf(TopBarConfig(title = "Background Audio")) }
    // PRD-160: Android 17 Background Audio Hardening 后台音频加固迁移工具包
    val backgroundAudioHardeningViewModel = remember { BackgroundAudioHardeningViewModel() }
    var backgroundAudioHardeningTopBar by remember { mutableStateOf(TopBarConfig(title = "🔊 Audio加固")) }
    // PRD-183: Android 17 Large Screen Resizability & Orientation Enforcement 合规检测工具包
    val orientationEnforcementViewModel = remember { OrientationEnforcementViewModel() }
    var orientationEnforcementTopBar by remember { mutableStateOf(TopBarConfig(title = "大屏方向锁定")) }
    // PRD-184: Android CLI & Android Skills 工具包
    val androidSkillsViewModel = remember { AndroidSkillsViewModel() }
    var androidSkillsTopBar by remember { mutableStateOf(TopBarConfig(title = "Android Skills Toolkit")) }
    // PRD-186: Android 17 SMS OTP Delay 合规检测与迁移工具包
    val otpDelayViewModel = remember { OtpDelayViewModel() }
    var otpDelayTopBar by remember { mutableStateOf(TopBarConfig(title = "OTP Delay 合规检测")) }
    // PRD-209: Android 开发者验证合规工具包（2026年9月大限）
    var devVerifyToolTopBar by remember { mutableStateOf(TopBarConfig(title = "Dev验证合规工具包")) }
    // PRD-212: Room 3.0 破坏性变更迁移工具包
    val room3MigrationViewModel = remember { Room3MigrationViewModel() }
    var room3MigrationTopBar by remember { mutableStateOf(TopBarConfig(title = "Room3迁移")) }
    // PRD-225: Room 3.0 Import 批量迁移工具包
    val room3ImportMigrationViewModel = remember { Room3ImportMigrationViewModel() }
    // PRD-213: Android 17 设备 RAM 内存限制适配工具包
    val android17MemoryViewModel: Android17MemoryViewModel = viewModel(
        factory = Android17MemoryViewModelFactory(LocalContext.current)
    )
    var android17MemoryTopBar by remember { mutableStateOf(TopBarConfig(title = "Memory Limits")) }
    // PRD-214: Android AppFunctions 开发工具包
    val appFunctionsToolViewModel = remember { AppFunctionsToolViewModel() }
    // PRD-220: Android 17 API 37 破坏性变更综合迁移工具包
    val android17Api37ToolViewModel = remember { Android17Api37ToolViewModel() }
    var appFunctionsToolTopBar by remember { mutableStateOf(TopBarConfig(title = "AppFunctions 工具台")) }
    // PRD-220: Android 17 API 37 破坏性变更综合迁移工具包
    var android17Api37ToolTopBar by remember { mutableStateOf(TopBarConfig(title = "Android 17 API 37 迁移工具")) }
    // PRD-227: Aluminium OS Android App 桌面适配工具包
    val aluminiumOSDesktopViewModel = remember { AluminiumOSDesktopViewModel() }
    var aluminiumOSDesktopTopBar by remember { mutableStateOf(TopBarConfig(title = "Aluminium OS 桌面适配工具")) }
    // PRD-233: Android Agent Skills 技能库生态工具包
    val agentSkillsToolkitViewModel = remember { AgentSkillsToolkitViewModel() }
    var agentSkillsToolkitTopBar by remember { mutableStateOf(TopBarConfig(title = "Agent Skills Toolkit")) }
    // PRD-234: Google Play Contact Picker 强制迁移工具包
    val contactPickerViewModel = remember { ContactPickerViewModel() }
    var contactPickerTopBar by remember { mutableStateOf(TopBarConfig(title = "Contact Picker 迁移")) }
    // PRD-235: Android 17 Per-App 内存限制检测与优化工具包
    val perAppMemoryLimitsViewModel = remember { PerAppMemoryLimitsViewModel() }
    var perAppMemoryLimitsTopBar by remember { mutableStateOf(TopBarConfig(title = "Per-App Memory Limits")) }
    // PRD-241: Android CLI × External AI Agent 集成工具包
    val androidCLIExternalAgentToolkitViewModel = remember { AndroidCLIExternalAgentToolkitViewModel() }
    var androidCLIExternalAgentToolkitTopBar by remember { mutableStateOf(TopBarConfig(title = "Android CLI × External AI Agent")) }
    // PRD-242: Android Studio Quail 调试/性能工具包
    val quailDebugToolsViewModel = remember { QuailDebugToolsViewModel() }
    val appAsToolViewModel = remember { AppAsToolViewModel() }
    val verifiedFinancialCallsViewModel = remember { VerifiedFinancialCallsViewModel() }
    var quailDebugToolsTopBar by remember { mutableStateOf(TopBarConfig(title = "Quail 调试/性能工具包")) }
    var verifiedFinancialCallsTopBar by remember { mutableStateOf(TopBarConfig(title = "来电验证 / Verified Calls")) }
    // PRD-280: Android Skills 安全扫描工具包
    val skillsSecurityToolkitViewModel = remember { SkillsSecurityToolkitViewModel() }
    var skillsSecurityToolkitTopBar by remember { mutableStateOf(TopBarConfig(title = "Skills 安全扫描工具包")) }
    // PRD-283: Android MDC-Views → Compose 迁移工具包
    val mdcToComposeToolViewModel = remember { MdcToComposeToolViewModel() }
    var mdcToComposeToolTopBar by remember { mutableStateOf(TopBarConfig(title = "MDC → Compose 迁移工具包")) }
    // PRD-289: Android XR SDK DP4 开发工具包
    val xrSdkDevKitViewModel = remember { XrDevKitViewModel() }
    var xrSdkDevKitTopBar by remember { mutableStateOf(TopBarConfig(title = "XR SDK 开发工具包")) }
    // PRD-292: Google ADK for Android 开发工具包
    val adkAndroidViewModel = remember { AdkAndroidViewModel() }
    var adkAndroidTopBar by remember { mutableStateOf(TopBarConfig(title = "Google ADK for Android")) }
    // PRD-294: Room 3.0 KMP 数据库迁移工具包
    val room3KmpMigrationViewModel = remember { Room3KmpMigrationViewModel() }
    var room3KmpMigrationTopBar by remember { mutableStateOf(TopBarConfig(title = "Room 3.0 KMP 迁移")) }
    // PRD-304: Android 开发者身份验证合规批量管理平台
    val devVerificationBatchViewModel = remember { VerificationDashboardViewModel() }
    var devVerificationBatchTopBar by remember { mutableStateOf(TopBarConfig(title = "Dev批量验证")) }

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
        20 -> onAlarmTopBar
        21 -> memoryLimitsTopBar
        22 -> largeScreenTopBar
        23 -> agenticAITopBar
        24 -> keyVaultTopBar
        25 -> desktopModeTopBar
        26 -> backgroundAudioTopBar
        27 -> backgroundAudioHardeningTopBar
        28 -> orientationEnforcementTopBar
        29 -> androidSkillsTopBar
        30 -> otpDelayTopBar
        31 -> devVerifyToolTopBar
        32 -> room3MigrationTopBar
        33 -> homeTopBar  // pre-existing: DevVerifyTool uses its own TopBar
        34 -> homeTopBar  // pre-existing: Prd210Compliance uses its own TopBar
        35 -> room3MigrationTopBar
        36 -> android17MemoryTopBar
        37 -> appFunctionsToolTopBar
        38 -> android17Api37ToolTopBar
        39 -> aluminiumOSDesktopTopBar
        // PRD-233: Android Agent Skills 技能库生态工具包
        41 -> agentSkillsToolkitTopBar
        // PRD-241: Android CLI × External AI Agent 集成工具包
        43 -> androidCLIExternalAgentToolkitTopBar
        // PRD-242: Android Studio Quail 调试/性能工具包
        44 -> quailDebugToolsTopBar
        // PRD-257: Verified Financial Calls API 集成工具包
        46 -> verifiedFinancialCallsTopBar
        // PRD-280: Android Skills 安全扫描工具包
        47 -> skillsSecurityToolkitTopBar
        // PRD-283: Android MDC-Views → Compose 迁移工具包
        48 -> mdcToComposeToolTopBar
        // PRD-289: Android XR SDK DP4 开发工具包
        49 -> xrSdkDevKitTopBar
        // PRD-292: Google ADK for Android 开发工具包
        50 -> adkAndroidTopBar
        // PRD-294: Room 3.0 KMP 数据库迁移工具包
        51 -> room3KmpMigrationTopBar
        // PRD-304: Android 开发者身份验证合规批量管理平台
        52 -> devVerificationBatchTopBar

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
                    // PRD-169: Android Agentic AI AppFunctions & UI Automation Framework 开发工具包
                    24 -> AgenticAIScreen(
                        state = agenticAIViewModel.state.collectAsState().value,
                        onIntent = agenticAIViewModel::sendIntent,
                        effect = agenticAIViewModel.effect
                    )
                    // PRD-171: Android 17 Key Limit 合规检测与数据重构工具包
                    25 -> KeyVaultScreen(
                        state = keyVaultViewModel.state.collectAsState().value,
                        onIntent = keyVaultViewModel::sendIntent
                    )
                    // PRD-170: Android 17 Desktop Mode 开发工具包
                    26 -> DesktopModeScreen(
                        state = desktopModeViewModel.state.collectAsState().value,
                        onIntent = desktopModeViewModel::sendIntent,
                        effect = desktopModeViewModel.effect
                    )
                    // PRD-178: Android 17 Background Audio Hardening 合规检测工具包
                    27 -> BackgroundAudioScreen(
                        viewModel = backgroundAudioViewModel,
                        onNavigateToRelatedTool = { }
                    )
                    // PRD-160: Android 17 Background Audio Hardening 后台音频加固迁移工具包
                    28 -> BackgroundAudioHardeningScreen(
                        viewModel = backgroundAudioHardeningViewModel,
                        onNavigateBack = { pendingTabToSelect = 0 }
                    )
                    // PRD-183: Android 17 Large Screen Resizability & Orientation Enforcement 合规检测工具包
                    29 -> OrientationEnforcementScreen(
                        viewModel = orientationEnforcementViewModel,
                        onNavigateBack = { pendingTabToSelect = 0 }
                    )
                    // PRD-185: Android AppFunctions SDK 开发工具包
                    30 -> AppFunctionTestScreen(
                        onUpdateTopBar = { }
                    )
                    // PRD-184: Android CLI & Android Skills 工具包
                    31 -> AndroidSkillsScreen(
                        state = androidSkillsViewModel.state.collectAsState().value,
                        onIntent = androidSkillsViewModel::processIntent,
                        effect = androidSkillsViewModel.effect
                    )
                    // PRD-186: Android 17 SMS OTP Delay 合规检测与迁移工具包
                    32 -> OtpDelayScreen(
                        viewModel = otpDelayViewModel,
                        onNavigateToTemplate = { }
                    )
                    // PRD-209: Android 开发者验证合规工具包（2026年9月大限）
                    33 -> DevVerifyToolScreen(
                        onUpdateTopBar = { devVerifyToolTopBar = it }
                    )
                    // PRD-210: Google Play 2026年4月政策三连击合规工具包
                    34 -> Prd210ComplianceScreen(
                        onUpdateTopBar = { }
                    )
                    // PRD-212: Room 3.0 破坏性变更迁移工具包
                    35 -> Room3MigrationScreen(
                        state = room3MigrationViewModel.state.collectAsState().value,
                        onIntent = room3MigrationViewModel::sendIntent
                    )
                    // PRD-225: Room 3.0 Import 批量迁移工具包
                    36 -> Room3ImportMigrationScreen(
                        viewModel = room3ImportMigrationViewModel
                    )
                    // PRD-213: Android 17 设备 RAM 内存限制适配工具包
                    37 -> Android17MemoryScreen(
                        viewModel = android17MemoryViewModel
                    )
                    // PRD-214: Android AppFunctions 开发工具包
                    38 -> AppFunctionsToolScreen(
                        viewModel = appFunctionsToolViewModel,
                        onUpdateTopBar = { appFunctionsToolTopBar = it }
                    )
                    // PRD-220: Android 17 API 37 破坏性变更综合迁移工具包
                    39 -> Android17Api37ToolScreen(
                        state = android17Api37ToolViewModel.state.collectAsState().value,
                        onIntent = android17Api37ToolViewModel::sendIntent,
                        onUpdateTopBar = { android17Api37ToolTopBar = it }
                    )
                    // PRD-227: Aluminium OS Android App 桌面适配工具包
                    40 -> AluminiumOSDesktopScreen(
                        viewModel = aluminiumOSDesktopViewModel,
                        onUpdateTopBar = { aluminiumOSDesktopTopBar = it }
                    )
                    // PRD-233: Android Agent Skills 技能库生态工具包
                    41 -> AgentSkillsToolkitScreen(
                        viewModel = agentSkillsToolkitViewModel,
                        onUpdateTopBar = { agentSkillsToolkitTopBar = it }
                    )
                    // PRD-234: Google Play Contact Picker 强制迁移工具包
                    38 -> ContactPickerScreen(
                        viewModel = contactPickerViewModel
                    )
                    // PRD-235: Android 17 Per-App 内存限制检测与优化工具包
                    42 -> PerAppMemoryLimitsScreen(viewModel = perAppMemoryLimitsViewModel)
                    // PRD-241: Android CLI × External AI Agent 集成工具包
                    43 -> AndroidCLIExternalAgentToolkitScreen(
                        state = androidCLIExternalAgentToolkitViewModel.state.collectAsState().value,
                        viewModel = androidCLIExternalAgentToolkitViewModel,
                        onUpdateTopBar = { androidCLIExternalAgentToolkitTopBar = it }
                    )
                    // PRD-242: Android Studio Quail 调试/性能工具包
                    44 -> QuailDebugToolsScreen(
                        viewModel = quailDebugToolsViewModel
                    )
                    // PRD-250: Android AppFunctions App-as-Tool 开发工具包
                    45 -> AppAsToolScreen(
                        viewModel = appAsToolViewModel,
                        onNavigateBack = { /* no-op: Tab navigation handles back */ }
                    )
                    // PRD-257: Verified Financial Calls API 集成工具包
                    46 -> VerifiedFinancialCallsScreen(
                        viewModel = verifiedFinancialCallsViewModel
                    )
                    // PRD-280: Android Skills 安全扫描工具包
                    47 -> SkillsSecurityToolkitScreen(
                        viewModel = skillsSecurityToolkitViewModel,
                        onUpdateTopBar = { skillsSecurityToolkitTopBar = it }
                    )
                    // PRD-283: Android MDC-Views → Compose 迁移工具包
                    48 -> MdcToComposeToolScreen(
                        viewModel = mdcToComposeToolViewModel
                    )
                    // PRD-289: Android XR SDK DP4 开发工具包
                    49 -> XrDevKitScreen(
                        viewModel = xrSdkDevKitViewModel,
                        onUpdateTopBar = { xrSdkDevKitTopBar = it }
                    )
                    // PRD-292: Google ADK for Android 开发工具包
                    50 -> AdkAndroidScreen(
                        viewModel = adkAndroidViewModel,
                        onNavigateToLab = { /* Tab navigation handled internally */ }
                    )
                    // PRD-294: Room 3.0 KMP 数据库迁移工具包
                    51 -> Room3KmpMigrationScreen(
                        viewModel = room3KmpMigrationViewModel,
                        onUpdateTopBar = { room3KmpMigrationTopBar = it }
                    )
                    // PRD-299: KMP 新默认项目结构迁移工具包
                    52 -> KMPNewStructureMigrationScreen(
                        onNavigateBack = { /* Tab navigation handles back */ }
                    )
                    // PRD-304: Android 开发者身份验证合规批量管理平台
                    53 -> VerificationDashboardScreen(
                        viewModel = devVerificationBatchViewModel,
                        onUpdateTopBar = { devVerificationBatchTopBar = it }
                    )
                }
            }
        }
    }
}
