package com.mvi.kenny.feature.androidxraiglasses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvi.kenny.feature.androidxraiglasses.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * ============================================================
 * AndroidXRViewModel — MVI ViewModel 实现
 * ============================================================
 * PRD-168 / Android XR AI Glasses Development Toolkit
 * Handles business logic and state management
 */
class AndroidXRViewModel : ViewModel() {

    // MVI State / 状态流
    private val _state = MutableStateFlow(AndroidXRState())
    val state: StateFlow<AndroidXRState> = _state.asStateFlow()

    // MVI Effect / 副作用流
    private val _effect = MutableSharedFlow<AndroidXREffect>()
    val effect: SharedFlow<AndroidXREffect> = _effect.asSharedFlow()

    init {
        // Initialize with default recommended path / 初始化默认推荐路径
        _state.update { it.copy(recommendedPath = getDefaultRecommendedPath()) }
    }

    /**
     * Process Intent / 处理 Intent
     */
    fun sendIntent(intent: AndroidXRIntent) {
        when (intent) {
            is AndroidXRIntent.SwitchTab -> handleSwitchTab(intent.tab)
            is AndroidXRIntent.SetAppType -> handleSetAppType(intent.appType)
            is AndroidXRIntent.SelectGlassesType -> handleSelectGlassesType(intent.glassesType)
            is AndroidXRIntent.LoadGlimmerGuide -> loadGlimmerGuide()
            is AndroidXRIntent.LoadProjectedGuide -> loadProjectedGuide()
            is AndroidXRIntent.LoadARCoreGuide -> loadARCoreGuide()
            is AndroidXRIntent.LoadUXRules -> loadUXRules()
            is AndroidXRIntent.LoadDualDeviceTemplate -> loadDualDeviceTemplate()
            is AndroidXRIntent.LoadEmulatorGuide -> loadEmulatorGuide()
            is AndroidXRIntent.LoadCITemplate -> loadCITemplate()
            is AndroidXRIntent.CopyCode -> handleCopyCode(intent.code)
            is AndroidXRIntent.SelectTemplate -> handleSelectTemplate(intent.template)
        }
    }

    // Tab switching / Tab 切换
    private fun handleSwitchTab(tab: XRTab) {
        _state.update { it.copy(currentTab = tab) }
        viewModelScope.launch { _effect.emit(AndroidXREffect.ScrollToTop) }
    }

    // App type selection / 应用类型选择
    private fun handleSetAppType(appType: AppType) {
        _state.update { it.copy(appType = appType, recommendedPath = getRecommendedPathForAppType(appType)) }
    }

    // Glasses type selection / 眼镜类型选择
    private fun handleSelectGlassesType(glassesType: GlassesType) {
        _state.update { it.copy(selectedGlassesType = glassesType) }
    }

    // Load Glimmer Guide / 加载 Glimmer 指南
    private fun loadGlimmerGuide() {
        _state.update { it.copy(glimmerGuide = createGlimmerGuide()) }
    }

    // Load Projected Library Guide / 加载 Projected Library 指南
    private fun loadProjectedGuide() {
        _state.update { it.copy(projectedGuide = createProjectedGuide()) }
    }

    // Load ARCore Guide / 加载 ARCore 指南
    private fun loadARCoreGuide() {
        _state.update { it.copy(arcoreGuide = createARCoreGuide()) }
    }

    // Load UX Rules / 加载 UX 规范
    private fun loadUXRules() {
        _state.update { it.copy(uxRules = createUXRules()) }
    }

    // Load Dual Device Template / 加载双端模板
    private fun loadDualDeviceTemplate() {
        _state.update { it.copy(dualDeviceTemplate = createDualDeviceTemplate()) }
    }

    // Load Emulator Guide / 加载模拟器指南
    private fun loadEmulatorGuide() {
        _state.update { it.copy(emulatorGuide = createEmulatorGuide()) }
    }

    // Load CI Template / 加载 CI 模板
    private fun loadCITemplate() {
        _state.update { it.copy(ciTemplate = createCITemplate()) }
    }

    // Copy code to clipboard / 复制代码
    private fun handleCopyCode(code: String) {
        viewModelScope.launch {
            _effect.emit(AndroidXREffect.ShowToast("Code copied to clipboard! / 代码已复制!"))
        }
    }

    // Select template / 选择模板
    private fun handleSelectTemplate(template: GlimmerTemplate) {
        _state.update { it.copy(selectedTemplate = template) }
    }

    // Calculate days until I/O / 计算距离 I/O 的天数
    private fun calculateDaysUntilIO(): Int {
        val ioDate = LocalDate.of(2026, 5, 19)
        val today = LocalDate.now()
        return ChronoUnit.DAYS.between(today, ioDate).toInt().coerceAtLeast(0)
    }

    // Get default recommended path / 获取默认推荐路径
    private fun getDefaultRecommendedPath(): List<XRModule> = listOf(
        XRModule("1", "Glimmer 基础", "学习 Compose Glimmer 的基本概念和组件", true),
        XRModule("2", "眼镜 UI 设计", "掌握 XR 特有的 UX 规范和设计原则", true),
        XRModule("3", "Projected Library", "实现手机与眼镜的协同通信", false),
        XRModule("4", "ARCore 集成", "在眼镜应用中集成 AR 能力", false),
        XRModule("5", "测试与部署", "使用 XR Emulator 测试和 CI 流水线构建", false)
    )

    // Get recommended path for app type / 根据应用类型获取推荐路径
    private fun getRecommendedPathForAppType(appType: AppType): List<XRModule> {
        return when (appType) {
            AppType.EXISTING -> listOf(
                XRModule("1", "Glimmer 迁移评估", "评估现有 App 的 Glimmer 迁移可行性", true),
                XRModule("2", "Projected 集成", "在现有 App 中集成 Projected Library", true),
                XRModule("3", "XR Emulator 测试", "使用模拟器验证兼容性", false),
                XRModule("4", "ARCore 功能扩展", "为现有 AR 功能添加眼镜支持", false)
            )
            AppType.NEW_GLASSES -> listOf(
                XRModule("1", "Glimmer 入门", "从头学习 Glimmer 开发范式", true),
                XRModule("2", "眼镜 UI 模板", "使用模板快速构建 UI", true),
                XRModule("3", "UX 规范实践", "遵循 XR 设计原则", false),
                XRModule("4", "Projected 通信", "实现手机控制功能", false),
                XRModule("5", "发布准备", "打包和发布眼镜应用", false)
            )
            AppType.NEW_PHONE -> listOf(
                XRModule("1", "Projected 架构", "设计手机-眼镜协同架构", true),
                XRModule("2", "数据同步策略", "实现可靠的双端数据同步", true),
                XRModule("3", "UI 模板集成", "使用 Glimmer UI 模板", false),
                XRModule("4", "CI 流水线", "配置自动化构建和测试", false)
            )
        }
    }

    // Create Glimmer Guide / 创建 Glimmer 指南
    private fun createGlimmerGuide(): GlimmerGuide = GlimmerGuide(
        comparisonTable = listOf(
            GlimmerComparisonItem("布局容器", "Column/Row/Box", "GlimmerColumn/GlimmerRow/DepthStack", "Glimmer 版本针对眼镜屏幕优化，支持深度层次"),
            GlimmerComparisonItem("文本组件", "Text", "GlimmerText", "自动优化阅读距离，支持注视点渲染"),
            GlimmerComparisonItem("卡片组件", "Card", "GlimmerCard", "浮动效果更强，适合 AR 叠加"),
            GlimmerComparisonItem("列表组件", "LazyColumn/LazyRow", "GlimmerList/GlimmerHorizontalList", "虚拟化优化，减少眼镜渲染负担"),
            GlimmerComparisonItem("手势支持", "clickable/tap", "GlimmerTouchTarget", "更大的触控目标，适合眼镜交互"),
            GlimmerComparisonItem("3D 场景", "-", "GlimmerScene", "Glimmer 特有，支持 3D 场景渲染")
        ),
        decisionTree = listOf(
            DecisionTreeNode("你的应用需要显示 3D 内容吗?", "GlimmerScene + ARCore", "继续", "如果只需要 2D UI，继续下一步"),
            DecisionTreeNode("你的应用需要 AR 叠加吗?", "使用 GlimmerCard + DepthStack", "使用标准 Card", "AR 叠加需要 Glimmer 的深度堆叠能力"),
            DecisionTreeNode("你的用户主要通过语音交互吗?", "GlimmerVoiceUI 组件", "GlimmerTouchTarget", "语音为主时减少触控目标数量"),
            DecisionTreeNode("你需要支持哪些眼镜设备?", "Glamor 0.9+", "标准 Glimmer", "多设备支持需要更高版本的 Glamor")
        ),
        componentList = listOf(
            GlimmerComponent("GlimmerText", "XR 优化的文本组件", "Glimmer 1.0+"),
            GlimmerComponent("GlimmerCard", "浮动卡片组件", "Glimmer 1.0+"),
            GlimmerComponent("GlimmerColumn", "垂直布局容器", "Glimmer 1.0+"),
            GlimmerComponent("GlimmerRow", "水平布局容器", "Glimmer 1.0+"),
            GlimmerComponent("DepthStack", "深度堆叠容器", "Glimmer 1.0+", true),
            GlimmerComponent("GlimmerList", "虚拟化垂直列表", "Glimmer 1.1+"),
            GlimmerComponent("GlimmerHorizontalList", "虚拟化水平列表", "Glimmer 1.1+"),
            GlimmerComponent("GlimmerTouchTarget", "增强触控目标", "Glimmer 1.0+"),
            GlimmerComponent("GlimmerScene", "3D 场景组件", "Glimmer 1.2+", true),
            GlimmerComponent("GlimmerVoiceUI", "语音 UI 组件", "Glimmer 1.2+", true)
        ),
        migrationSteps = listOf(
            MigrationStep(1, "添加 Glimmer 依赖", "在 build.gradle 中添加 Compose Glimmer 依赖", "implementation(\"androidx.compose.glimmer:glimmer:1.0.0\")"),
            MigrationStep(2, "替换布局容器", "将 Column 替换为 GlimmerColumn", "// Before\nColumn { ... }\n// After\nGlimmerColumn { ... }"),
            MigrationStep(3, "更新文本组件", "将 Text 替换为 GlimmerText", "// Before\nText(\"Hello\")\n// After\nGlimmerText(\"Hello\", GlimmerTypography.Body)"),
            MigrationStep(4, "调整卡片设计", "使用 GlimmerCard 替换 Card", "// Before\nCard { Text(\"Content\") }\n// After\nGlimmerCard { GlimmerText(\"Content\", GlimmerTypography.Body) }"),
            MigrationStep(5, "测试眼镜渲染", "在 XR Emulator 中验证 UI 效果", "使用 GlassesScreenPreview 组件预览效果")
        ),
        codeDiff = """// Before (Standard Compose)
@Composable
fun NotificationCard(title: String, content: String) {
    Card {
        Column {
            Text(title, fontWeight = FontWeight.Bold)
            Text(content)
        }
    }
}

// After (Compose Glimmer)
@Composable
fun NotificationCard(title: String, content: String) {
    GlimmerCard {
        GlimmerColumn {
            GlimmerText(title, GlimmerTypography.Heading)
            GlimmerText(content, GlimmerTypography.Body)
        }
    }
}"""
    )

    // Create Projected Guide / 创建 Projected Library 指南
    private fun createProjectedGuide(): ProjectedGuide = ProjectedGuide(
        conceptExplanation = "Projected Library 是 Google 推出的用于实现手机与 XR 眼镜协同通信的库。手机作为主机，眼镜作为第二屏幕，数据通过本地网络实时同步。适用于需要手机控制、数据处理或复杂交互的 XR 应用场景。",
        apiSteps = listOf(
            ProjectedApiStep(1, "添加依赖", "在手机和眼镜 App 中添加 Projected Library 依赖", "implementation(\"com.google.android.gms:play-services-projected:1.0.0\")"),
            ProjectedApiStep(2, "初始化 ProjectedManager", "在手机端创建 ProjectedManager 实例", "val projectedManager = ProjectedManager(context)"),
            ProjectedApiStep(3, "发现眼镜设备", "搜索可连接的 XR 眼镜设备", "projectedManager.discoverDevices { devices -> ... }"),
            ProjectedApiStep(4, "建立连接", "与选定的眼镜设备建立投影会话", "val session = projectedManager.createSession(deviceId)"),
            ProjectedApiStep(5, "发送数据", "通过 Session 发送数据到眼镜", "session.sendData(dataChannel, dataBuffer)"),
            ProjectedApiStep(6, "接收数据", "监听来自眼镜的返回数据", "session.receiveData(dataChannel) { data -> ... }")
        ),
        scenarioTemplates = listOf(
            ProjectedScenario("导航场景", "手机计算路线，实时同步到眼镜显示", "手机 GPS → 计算路线 → 发送导航数据 → 眼镜 AR 叠加"),
            ProjectedScenario("拍照场景", "眼镜摄像头拍照，手机处理并返回预览", "眼镜触发拍照 → 传输图像 → 手机 AI 处理 → 返回缩略图"),
            ProjectedScenario("通知同步", "手机通知推送到眼镜显示", "手机接收通知 → 格式化 → 发送到眼镜 → GlimmerCard 显示")
        ),
        permissionConfig = listOf(
            PermissionItem("android.permission.INTERNET", "网络通信必需", true),
            PermissionItem("android.permission.ACCESS_WIFI_STATE", "发现附近设备", true),
            PermissionItem("android.permission.CHANGE_WIFI_STATE", "建立连接", true),
            PermissionItem("com.google.android.gms.permission.PROJECTED_DEVICE", "Projected Library 权限", true),
            PermissionItem("android.permission.CAMERA", "拍照场景需要", false),
            PermissionItem("android.permission.ACCESS_FINE_LOCATION", "导航场景需要", false)
        )
    )

    // Create ARCore Guide / 创建 ARCore 指南
    private fun createARCoreGuide(): ARCoreGuide = ARCoreGuide(
        differencesFromStandard = listOf(
            "眼镜端 ARCore 不支持环境光照估计",
            "手势识别改为头部注视 + 语音命令",
            "平面检测结果直接用于 GlimmerScene 渲染",
            "不支持 ARCore 的某些深度 API",
            "性能优化更关键，眼镜算力有限"
        ),
        glimmerArCapabilities = listOf(
            "GlimmerScene 支持将 3D 对象锚定到现实世界坐标",
            "DepthStack 容器支持 AR 叠加层管理",
            "集成的平面检测结果直接对接 Glimmer 渲染",
            "支持 ARCore 的注视点追踪",
            "优化的渲染管线减少延迟"
        ),
        developmentSteps = listOf(
            ARCoreStep(1, "配置 ARCore", "在 AndroidManifest 中声明 ARCore 要求", "需要支持 ARCore 的眼镜设备"),
            ARCoreStep(2, "初始化 ARCore Session", "创建 ARCore Session 实例", "眼镜设备需要 Google Play Services for XR"),
            ARCoreStep(3, "实现 GlimmerScene", "使用 GlimmerScene 组件渲染 AR 内容", "与标准 ARCore 渲染分开，Glimmer 优化过"),
            ARCoreStep(4, "处理注视点", "实现头部追踪和注视点检测", "ARCore 提供注视点但不提供手势"),
            ARCoreStep(5, "测试部署", "在真机上测试 AR 功能", "XR Emulator 不支持 AR，请使用真机")
        ),
        sampleCode = """// ARCore for AI Glasses 示例
@Composable
fun ARNavigationOverlay(direction: String, distance: Float) {
    GlimmerScene(
        modifier = Modifier.fillMaxSize(),
        arSession = arCoreSession
    ) {
        // AR 叠加层
        DepthStack {
            GlimmerCard(
                modifier = Modifier.align(Alignment.TopCenter)
                    .padding(top = 48.dp)
            ) {
                GlimmerText(
                    text = direction,
                    typography = GlimmerTypography.Headline,
                    color = XRColors.Accent
                )
                GlimmerText(
                    text = "\${'$'}{distance}m",
                    typography = GlimmerTypography.Body
                )
            }
        }
    }
}"""
    )

    // Create UX Rules / 创建 UX 规范
    private fun createUXRules(): UXRules = UXRules(
        principles = listOf(
            UXPrinciple("🔒", "隐私优先", "眼镜设备容易被他人看到，避免显示敏感信息"),
            UXPrinciple("👁️", "视线安全", "避免频繁的视觉焦点移动，单次注视不超过 3 秒"),
            UXPrinciple("👆", "手势简洁", "眼镜端手势有限，优先语音交互"),
            UXPrinciple("🎤", "语音协同", "关键操作支持语音确认，减少触控依赖"),
            UXPrinciple("⚡", "响应及时", "所有交互反馈不超过 200ms"),
            UXPrinciple("🌙", "夜间友好", "AR 叠加亮度可调，避免刺眼")
        ),
        doList = listOf(
            "使用高对比度色彩确保户外可见性",
            "重要信息放在视野中心区域",
            "每次只显示 3-5 个关键信息",
            "使用图标 + 文字组合提高识别度",
            "提供清晰的语音反馈确认操作",
            "支持手势和语音两种交互方式",
            "内容自动隐藏以保护隐私"
        ),
        dontList = listOf(
            "显示银行密码、验证码等敏感信息",
            "要求用户长时间注视小文字",
            "复杂的二级菜单或深层导航",
            "自动播放声音或频繁震动",
            "一次性显示大量文字信息",
            "依赖精确的手势操作",
            "忽略低光环境下的显示效果"
        ),
        typographySpec = TypographySpec(
            fontFamily = "Google Sans / Noto Sans SC",
            headingSize = "24-32sp (眼镜等效距离)",
            bodySize = "16-20sp",
            captionSize = "12-14sp"
        )
    )

    // Create Dual Device Template / 创建双端模板
    private fun createDualDeviceTemplate(): DualDeviceTemplate = DualDeviceTemplate(
        architectureDiagram = """┌─────────────────┐     ┌─────────────────┐
│    Phone App     │     │   Glasses App   │
│   (Controller)   │     │   (Display)     │
├─────────────────┤     ├─────────────────┤
│ ProjectedManager│────▶│  GlimmerScene   │
│     Session     │◀────│   Receiver      │
└────────┬────────┘     └────────▲────────┘
         │                       │
         │    Local Network       │
         └───────────────────────┘""",
        dataSyncStrategy = "采用事件驱动架构，手机端变更通过 Projected Library 实时同步到眼镜端。支持三种模式：1) 实时同步：导航、游戏等低延迟场景；2) 批量同步：文件传输等高带宽场景；3) 延迟同步：非关键数据，降低网络依赖。所有同步协议内置重试机制和网络波动容忍。",
        codeTemplates = listOf(
            CodeTemplateItem(
                "Phone → Glasses 数据发送",
                "手机端发送数据到眼镜端",
                """// Phone side
val session = projectedManager.createSession(deviceId)
val dataChannel = session.getDataChannel("navigation")
val dataBuffer = DataBuffer.wrap(navData)
dataChannel.send(dataBuffer)"""
            ),
            CodeTemplateItem(
                "Glasses → Phone 状态回传",
                "眼镜端回传用户交互状态",
                """// Glasses side
val session = LocalProjectedSession.getInstance()
val stateChannel = session.getDataChannel("state")
stateChannel.setReceiveListener { buffer ->
    val state = buffer.toString()
    handleStateUpdate(state)
}"""
            ),
            CodeTemplateItem(
                "连接状态管理",
                "处理连接断开和恢复",
                """// Connection state observer
session.addConnectionStateListener { state ->
    when (state) {
        ConnectionState.CONNECTED -> { ... }
        ConnectionState.DISCONNECTED -> { ... }
        ConnectionState.RECONNECTING -> { ... }
    }
}"""
            )
        ),
        testingMethods = listOf(
            "单元测试：使用 FakeProjectedManager 模拟眼镜设备",
            "集成测试：在同一 Wi-Fi 下测试真实设备对",
            "网络波动测试：使用 network emulator 模拟不稳定网络",
            "长时间运行测试：验证连接稳定性和内存泄漏",
            "错误恢复测试：验证断网重连逻辑"
        )
    )

    // Create Emulator Guide / 创建模拟器指南
    private fun createEmulatorGuide(): EmulatorGuide = EmulatorGuide(
        downloadUrl = "Android Studio > SDK Manager > SDK Tools > Android XR Emulator",
        setupSteps = listOf(
            EmulatorStep(1, "下载 XR Emulator", "通过 Android Studio SDK Manager 下载 XR Emulator 系统镜像"),
            EmulatorStep(2, "创建 AVD", "在 AVD Manager 中创建 XR 虚拟设备，选择 XR API 34+ 系统镜像"),
            EmulatorStep(3, "配置 FoV", "在设备属性中设置 Field of View，建议 60-90 度"),
            EmulatorStep(4, "配置分辨率", "根据目标眼镜设置屏幕分辨率，如 360x360 或 640x360"),
            EmulatorStep(5, "启动测试", "运行应用并使用 XR Emulator 的调试工具验证 UI 效果")
        ),
        fovSetting = "建议 60°（日常）到 90°（沉浸游戏）",
        resolutionSetting = "Galaxy XR 风格: 360×360px / Meta Quest 风格: 640×360px",
        dpiSetting = "建议 425 DPI (与真实设备一致)",
        debuggingMethods = listOf(
            "使用 adb logcat 过滤 XR 相关日志",
            "启用 XR Debug Overlay 显示性能指标",
            "使用 GlassesScreenPreview 组件实时预览",
            "截图和录屏功能辅助 UI 调试",
            "连接真机进行最终验证"
        ),
        faq = listOf(
            FaqItem("XR Emulator 支持 AR 功能吗?", "不支持。AR 功能需要真实 ARCore 兼容设备和 Google Play Services for XR。请使用真机测试 AR 相关功能。"),
            FaqItem("模拟器性能较慢?", "XR Emulator 需要较多系统资源，建议 16GB+ RAM，并关闭不必要的后台应用。也可使用 Release 模式提升性能。"),
            FaqItem("如何模拟眼镜设备旋转?", "在 Emulator 的 Extended Controls > XR Settings 中可以模拟头部运动和手势操作。"),
            FaqItem("UI 在模拟器上显示正常但真机异常?", "真机屏幕密度可能不同，建议使用真实设备进行最终测试。同时检查 GPU 渲染差异。")
        )
    )

    // Create CI Template / 创建 CI 模板
    private fun createCITemplate(): CITemplate = CITemplate(
        githubActionsTemplate = """name: Android XR CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        device: [
          { name: 'Pixel 8 Pro', api: 34, abi: 'arm64-v8a', xr: false },
          { name: 'XR Emulator', api: 34, abi: 'x86_64', xr: true }
        ]
    steps:
      - uses: actions/checkout@v4
      - name: Setup JDK
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: ~/.gradle/caches
          key: ${'$'}{{ runner.os }}-gradle-${'$'}{{ hashFiles('**/*.gradle*') }}
      - name: Build XR Debug APK
        run: ./gradlew assembleDebug
      - name: Run XR Unit Tests
        run: ./gradlew testDebugUnitTest""",
        gitlabCiTemplate = """stages:
  - build
  - test
  - deploy

variables:
  ANDROID_COMPILE_SDK: "34"
  ANDROID_BUILD_TOOLS: "34.0.0"

build:xr:
  stage: build
  image: registry.example.com/android-xr:latest
  script:
    - ./gradlew assembleDebug
  artifacts:
    paths:
      - app/build/outputs/apk/debug/""",
        deviceMatrix = listOf(
            DeviceConfig("Pixel 8 Pro", 34, "arm64-v8a", false),
            DeviceConfig("Pixel 7 Pro", 33, "arm64-v8a", false),
            DeviceConfig("Samsung Galaxy S24", 34, "arm64-v8a", false),
            DeviceConfig("XR Emulator (360x360)", 34, "x86_64", true),
            DeviceConfig("XR Emulator (640x360)", 34, "x86_64", true)
        ),
        prBotConfig = """PR Bot 配置示例:
- 自动化检查: 代码风格、单元测试覆盖率
- XR 专项检查: Glimmer 组件使用规范、UX 规范合规
- 设备兼容性: 在多个设备配置上运行Instrumented Test
- 预览生成: 自动生成 XR UI 截图"""
    )
}
