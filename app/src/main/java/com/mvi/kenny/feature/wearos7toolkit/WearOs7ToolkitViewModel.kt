package com.mvi.kenny.feature.wearos7toolkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ============================================================
// WearOs7ToolkitViewModel — MVI ViewModel
// PRD-265: Wear OS 7 AppFunctions API + Wear Workout Tracker Developer Toolkit
// Manages 10 modules (A-J) content navigation and user interactions.
// ============================================================
class WearOs7ToolkitViewModel : ViewModel() {

    private val _state = MutableStateFlow(WearOs7ToolkitState.Initial)
    val state: StateFlow<WearOs7ToolkitState> = _state.asStateFlow()

    // Intent entry point / Intent 入口
    fun sendIntent(intent: WearOs7ToolkitIntent) {
        viewModelScope.launch {
            when (intent) {
                is WearOs7ToolkitIntent.NavigateToModule -> navigateToModule(intent.module)
                is WearOs7ToolkitIntent.SwitchLanguage    -> switchLanguage(intent.lang)
                is WearOs7ToolkitIntent.ToggleSection    -> toggleSection(intent.sectionId)
                is WearOs7ToolkitIntent.CopyCode         -> copyCode(intent.codeId)
                is WearOs7ToolkitIntent.SetDecisionInput -> setDecision(intent.category)
                is WearOs7ToolkitIntent.ClearDecision    -> clearDecision()
                is WearOs7ToolkitIntent.UpdateSearch     -> updateSearch(intent.query)
                is WearOs7ToolkitIntent.ClearError       -> clearError()
            }
        }
    }

    // Initialize all 10 modules on creation / 初始化所有10个模块
    init { loadAllModules() }

    private fun loadAllModules() {
        val modules = mapOf(
            WearModuleType.A_APP_FUNCTIONS  to buildModuleA(),
            WearModuleType.B_WORKOUT_TRACKER to buildModuleB(),
            WearModuleType.C_LIVE_UPDATES   to buildModuleC(),
            WearModuleType.D_WIDGETS        to buildModuleD(),
            WearModuleType.E_WATCH_FACE     to buildModuleE(),
            WearModuleType.F_GEMINI_INTEL   to buildModuleF(),
            WearModuleType.G_DECISION_TREE  to buildModuleG(),
            WearModuleType.H_POWER_OPT      to buildModuleH(),
            WearModuleType.I_CANARY_EMU     to buildModuleI(),
            WearModuleType.J_CI_PLUGIN      to buildModuleJ(),
        )
        _state.value = _state.value.copy(moduleData = modules)
    }

    private fun navigateToModule(module: WearModuleType) {
        _state.value = _state.value.copy(currentModule = module, expandedSections = emptySet())
    }

    private fun switchLanguage(lang: CodeLanguage) {
        _state.value = _state.value.copy(selectedLang = lang)
    }

    private fun toggleSection(sectionId: String) {
        val current = _state.value.expandedSections
        _state.value = _state.value.copy(
            expandedSections = if (sectionId in current) current - sectionId else current + sectionId
        )
    }

    private fun copyCode(codeId: String) {
        _state.value = _state.value.copy(codeCopiedId = codeId)
    }

    private fun setDecision(category: AppCategory) {
        _state.value = _state.value.copy(decisionCategory = category)
    }

    private fun clearDecision() {
        _state.value = _state.value.copy(decisionCategory = null)
    }

    private fun updateSearch(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    private fun clearError() {
        _state.value = _state.value.copy(errorMsg = null)
    }

    // ============================================================
    // Module Content Builders — 10 个模块的内容构建器
    // ============================================================

    // Module A: AppFunctions API Complete Guide / AppFunctions API 完整开发指南
    private fun buildModuleA() = ModuleData(
        module = WearModuleType.A_APP_FUNCTIONS,
        overview = "AppFunctions API 是 Wear OS 7 官方提供的、第三方 App 与 Gemini 深度集成的标准途径。开发者通过在 AndroidManifest 中声明 AppFunction capability，让用户用自然语言唤醒 App 功能。",
        sections = listOf(
            ContentSection("a1", "工作原理", "AppFunctions API 的核心是「能力声明」机制——App 在 AndroidManifest 中用 <meta-data> 声明自己支持的 AppFunction，Gemini 通过 BIIA（Built-in Intent Authority）识别这些能力，用户说「开始追踪我的跑步」即可启动对应 App。"),
            ContentSection("a2", "AndroidManifest 声明格式", "在 <queries> 中声明 AppFunction capability，并在 <meta-data> 中填写 AppFunction 的 JSON 描述。需要精确的 intent-filter 匹配。"),
            ContentSection("a3", "AppFunction 实现示例", "健身 App 通过 AppFunctions 暴露「开始健身」「暂停健身」「结束健身」三个能力，Gemini 识别后驱动 App 状态切换。"),
            ContentSection("a4", "测试方法", "使用 Wear OS 7 Canary Emulator 测试，通过 ADB 触发 AppFunction 模拟。重点验证 intent 路由和参数解析。"),
            ContentSection("a5", "CI 验证", "使用 wearos7-ci-plugin Gradle 插件检测 AppFunctions 声明合规性，在 CI 流水线中自动检测格式错误。"),
        ),
        codeExamples = listOf(
            CodeExample("a_manifest", "AndroidManifest.xml AppFunctions 声明", CodeLanguage.XML,
"""<meta-data
    android:name="com.google.android.wearable.appfunctions.metadata"
    android:resource="@xml/app_functions" />

<queries>
    <intent>
        <action android:name="com.google.android.appfunctions.INTENT_ACTION_TRIGGER" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent>
</queries>"""),
            CodeExample("a_xml", "res/xml/app_functions.xml", CodeLanguage.XML,
"""<AppFunctions xmlns="urn:urgency:appfunctions:v2">
    <AppFunction name="START_WORKOUT">
        <Intent action="com.example.fitness.START_WORKOUT">
            <Parameter name="workoutType" type="string" />
        </Intent>
        <CapabilityBinding>android.hardware.fitness</CapabilityBinding>
        <TriggerPhrase>开始锻炼</TriggerPhrase>
    </AppFunction>
</AppFunctions>"""),
            CodeExample("a_kotlin", "FitnessAppFunction.kt", CodeLanguage.KOTLIN,
"""class FitnessAppFunction : AppFunctionHandler {
    override fun onAppFunctionTriggered(
        intent: Intent,
        params: Bundle
    ): AppFunctionResult {
        return when (intent.action) {
            "com.example.fitness.START_WORKOUT" -> {
                val workoutType = params.getString("workoutType") ?: "running"
                startWorkout(workoutType)
                AppFunctionResult.success("已启动跑步")
            }
            else -> AppFunctionResult.notSupported()
        }
    }
}"""),
        ),
        apiTable = listOf(
            ApiEntry("name", "String", "AppFunction 名称，必须全局唯一", true),
            ApiEntry("action", "String", "触发的 Intent action", true),
            ApiEntry("workoutType", "String", "健身类型参数（running/cycling/swimming）", false),
            ApiEntry("CapabilityBinding", "String", "所需硬件能力声明", false),
            ApiEntry("TriggerPhrase", "String", "触发短语（支持多语言）", false),
        ),
        notes = listOf(
            NoteCallout("warning", "声明格式错误会导致 Gemini 无法识别 AppFunction，建议使用 CI 插件自动检测"),
            NoteCallout("info", "AppFunctions API 依赖 Google Mobile Services (GMS)，部分设备可能不支持"),
        )
    )

    // Module B: Wear Workout Tracker Integration Guide / Wear Workout Tracker 接入指南
    private fun buildModuleB() = ModuleData(
        module = WearModuleType.B_WORKOUT_TRACKER,
        overview = "Wear Workout Tracker 是 Google 提供的原生健身追踪体验，App 通过 Fitness Workouts API 接入后可自动获得心率/配速/距离等数据，无需自建健身套件。",
        sections = listOf(
            ContentSection("b1", "工作原理", "Wear Workout Tracker 通过系统级 WorkoutSession 管理健身状态，App 注册 listener 接收实时数据。数据来源于手表传感器，无需 App 自行读取。"),
            ContentSection("b2", "接入方式对比", "Fitness Workouts API（接入原生 Tracker）vs 自建健身套件。接入原生 Tracker 开发成本低、数据准确，但定制化有限；自建方案灵活性高但工作量大。"),
            ContentSection("b3", "Fitness Workouts API 集成", "声明 Fitness capability，使用 WorkoutSession.Builder 创建会话，注册 WorkoutCallback 接收数据。"),
            ContentSection("b4", "ASICS Runkeeper 集成", "通过 Runkeeper Intent API 桥接到 ASICS 生态，获取更丰富的跑步数据和分析功能。"),
            ContentSection("b5", "决策树入口", "根据 App 类型推荐接入方案（见模块 G 决策树）。"),
        ),
        codeExamples = listOf(
            CodeExample("b_init", "Fitness Workouts API 初始化", CodeLanguage.KOTLIN,
"""val fitnessOptions = FitnessOptions.builder()
    .addDataType(DataType.TYPE_WORKOUT_EXERCISE_SEGMENT)
    .addDataType(DataType.TYPE_HEART_RATE_BPM)
    .build()
val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
Fitness.getConfigClient(context, account)
    .enableWorkoutSessions()
    .addOnSuccessListener { Log.d(TAG, "Workout sessions enabled") }"""),
            CodeExample("b_session", "创建 WorkoutSession", CodeLanguage.KOTLIN,
"""val workoutConfig = WorkoutConfiguration.builder()
    .setExerciseRouteName("Outdoor Run")
    .setFitnessActivities(FitnessActivities.RUNNING)
    .build()
val intent = Workouts.createWorkoutIntentBuilder(context)
    .setInitialIntent(workoutConfig)
    .build(context.activity, 0)
WorkoutSession.start(context, workoutConfig)"""),
        ),
        apiTable = listOf(
            ApiEntry("ExerciseRouteName", "String", "运动路线名称", true),
            ApiEntry("FitnessActivities", "Enum", "运动类型枚举（RUNNING/BICYCLING等）", true),
            ApiEntry("WorkoutCallback", "Interface", "健身数据回调接口", true),
        ),
        notes = listOf(
            NoteCallout("tip", "Wear Workout Tracker 适合需要快速上线健身功能、愿意依赖 Google 生态的 App"),
            NoteCallout("warning", "自建方案适合需要高度定制化或数据控制权的团队"),
        )
    )

    // Module C: Live Updates Data Bridge / Live Updates 手机↔Watch 数据桥接
    private fun buildModuleC() = ModuleData(
        module = WearModuleType.C_LIVE_UPDATES,
        overview = "Live Updates 是 Wear OS 7 的手机→手表实时数据同步机制。手机 App 通过 Play 商店更新桥接将数据同步到手表，开发者需要在手机端声明哪些数据要同步。",
        sections = listOf(
            ContentSection("c1", "同步机制", "手机 App 通过 DataBridge API 写入更新，手表端通过 DataClient 订阅。数据经过 Google Play 服务中转，有秒级延迟（非实时流）。"),
            ContentSection("c2", "数据声明格式", "在手机端 AndroidManifest 中声明 <meta-data android:name=\"com.google.android.wearable.dataBridge\"> 并配置同步数据类型。"),
            ContentSection("c3", "实时性限制", "Live Updates 有 1-5 秒延迟，不适合需要毫秒级同步的场景（如实时心率曲线）。适合天气、日程、导航剩余时间等场景。"),
            ContentSection("c4", "电量管理", "桥接数据同步会触发手表的 WearableListenerService，注意控制频率避免过快刷新导致续航下降。"),
        ),
        codeExamples = listOf(
            CodeExample("c_manifest", "手机端 AndroidManifest 声明", CodeLanguage.XML,
"""<meta-data
    android:name="com.google.android.wearable.dataBridge"
    android:resource="@xml/live_updates_bridge" />

<!-- res/xml/live_updates_bridge.xml -->
<LiveUpdatesBridge>
    <DataType name="weather_current" path="/weather/current" />
    <DataType name="nav_eta"        path="/navigation/eta" />
</LiveUpdatesBridge>"""),
            CodeExample("c_watch", "手表端 DataClient 订阅", CodeLanguage.KOTLIN,
"""val dataClient = Wearable.getDataClient(context)
val putDataRequest = PutDataRequest.create("/weather/current")
dataClient.addListener(dataListener)"""),
        ),
        apiTable = listOf(
            ApiEntry("DataType.name", "String", "数据类型名称，需与手机端匹配", true),
            ApiEntry("DataType.path", "String", "数据路径 URI", true),
            ApiEntry("refreshInterval", "Int", "刷新间隔（秒），建议>=5", false),
        ),
        notes = listOf(
            NoteCallout("warning", "Live Updates 有延迟，不适合实时健身数据同步（用 Wear Workout Tracker 代替）"),
            NoteCallout("info", "数据量建议<1KB，频繁更新会影响手表续航"),
        )
    )

    // Module D: Dual-Size Widget Development / 双尺寸 Widget 开发
    private fun buildModuleD() = ModuleData(
        module = WearModuleType.D_WIDGETS,
        overview = "Wear OS 7 Flexible Widgets 支持 2×1 和 2×2 两种尺寸。Watch Widget 需要同时适配两种尺寸，Gemini 生成的 Widget 会根据场景选择尺寸。",
        sections = listOf(
            ContentSection("d1", "Flexible Widgets 概述", "2×1 适合单行信息（时间、快速操作），2×2 适合多行信息（天气、健身摘要）。两种尺寸必须同时提供。"),
            ContentSection("d2", "布局适配策略", "2×1 使用 LinearLayout（vertical），2×2 使用 ConstraintLayout 支持多行内容。"),
            ContentSection("d3", "Gemini Widget 兼容性", "Gemini 生成的 Widget 默认选择最合适尺寸，App 需要在 widget_info.xml 中声明支持多种尺寸。"),
        ),
        codeExamples = listOf(
            CodeExample("d_info", "widget_info.xml", CodeLanguage.XML,
"""<appwidget-provider
    android:minWidth="110dp"
    android:minHeight="40dp"
    android:minResizeWidth="110dp"
    android:minResizeHeight="40dp"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="widget_complication" />"""),
            CodeExample("d_compose", "Compose Widget 示例", CodeLanguage.KOTLIN,
"""@Composable
fun WeatherWidget2x2(data: WeatherData) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(data.temp, style = MaterialTheme.typography.headlineMedium)
        Text(data.condition, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(4.dp))
        Text("H:${'$'}{data.high} L:${'$'}{data.low}", style = MaterialTheme.typography.caption)
    }
}"""),
        ),
        apiTable = listOf(
            ApiEntry("minWidth", "Dimension", "最小宽度（2x1: 110dp）", true),
            ApiEntry("minHeight", "Dimension", "最小高度（2x1: 40dp, 2x2: 90dp）", true),
            ApiEntry("resizeMode", "Flag", "允许调整方向（horizontal|vertical）", true),
        ),
        notes = listOf(
            NoteCallout("warning", "只提供一种尺寸会导致 Gemini Widget 无法正确展示，必须同时声明 2x1 和 2x2"),
        )
    )

    // Module E: Watch Face Format v5 / Watch Face Format v5 开发指南
    private fun buildModuleE() = ModuleData(
        module = WearModuleType.E_WATCH_FACE,
        overview = "Watch Face Format v5 是 Watch Face 的声明式配置格式（XML），新增 auto-size 和 alignment 选项，无需代码即可实现动态字体大小和精确对齐。",
        sections = listOf(
            ContentSection("e1", "新增字段", "v5 新增 autoSize（字体自适应）和 alignment（精确对齐 left/center/right/top/bottom）两个关键字段。"),
            ContentSection("e2", "Before/After 迁移", "v4 的固定 px 尺寸迁移到 v5 的相对布局，删除所有硬编码的 dp 值，替换为 alignment 表达式。"),
            ContentSection("e3", "兼容性处理", "v5 Watch Face 在 v4 设备上会回退到基础模式，autoSize 功能不可用。"),
        ),
        beforeAfter = BeforeAfter(
            before = """<!-- v4: 硬编码尺寸 -->
<TextLayer ... width="80" height="20" x="60" y="120" />""",
            after = """<!-- v5: 相对布局 + autoSize -->
<PartText x="alignment: CENTER_BOTTOM" y="offset: -30">
    <Format>%d°</Format>
    <autoSize android:maxSize="20" android:minSize="8" />
</PartText>""",
            explanation = "v5 移除了硬编码宽高，改用 alignment + offset + autoSize 实现响应式排版。"
        ),
        codeExamples = listOf(
            CodeExample("e_format", "WatchFace Format v5", CodeLanguage.XML,
"""<WatchFace>
    <Metadata name="preview" />
    <Scene>
        <PartDrawabletext x="alignment: CENTER" y="center_y:40">
            <Format>%d:%02d</Format>
            <autoSize android:maxSize="32" android:minSize="12" />
        </PartDrawabletext>
    </Scene>
</WatchFace>"""),
        ),
        apiTable = listOf(
            ApiEntry("autoSize.maxSize", "Int (sp)", "字体最大字号", true),
            ApiEntry("autoSize.minSize", "Int (sp)", "字体最小字号", true),
            ApiEntry("alignment", "Enum", "对齐方式（CENTER/LEFT/RIGHT/TOP/BOTTOM等）", true),
        ),
        notes = listOf(
            NoteCallout("info", "Watch Face Format v5 是声明式格式，无需 Kotlin/Java 代码"),
            NoteCallout("warning", "autoSize 需要 watch face 渲染器支持，旧设备可能降级"),
        )
    )

    // Module F: Gemini Intelligence / Gemini Intelligence on Wear OS 7
    private fun buildModuleF() = ModuleData(
        module = WearModuleType.F_GEMINI_INTEL,
        overview = "Gemini Intelligence 在 Wear OS 7 上扩展到手表端，Watch App 可以通过 AppFunctions（模块A）接入 Gemini Agentic 能力，实现自然语言驱动的 App 控制。",
        sections = listOf(
            ContentSection("f1", "Gemini on Wear OS 7", "Gemini Intelligence 扩展到部分 2026 年新 watch，支持本地化推理。"),
            ContentSection("f2", "Agentic App 设计模式", "App 暴露能力 -> Gemini 识别意图 -> 生成参数 -> 触发 Intent -> App 执行。"),
            ContentSection("f3", "Watch App AI 能力接入路径", "①通过 AppFunctions API 声明能力（模块A）；②通过 Wear OS ML Kit 集成本地推理；③通过手机端 Gemini Mobile SDK 代理请求。"),
        ),
        codeExamples = listOf(
            CodeExample("f_agentic", "Agentic App 触发流", CodeLanguage.KOTLIN,
"""val agenticIntent = Intent("com.example.fitness.START_WORKOUT").apply {
    putExtra("workoutType", "running")
    putExtra("duration", 30)
    putExtra("goal", "calorie_burn")
}"""),
        ),
        apiTable = listOf(
            ApiEntry("Intent.action", "String", "AppFunction 对应的 action", true),
            ApiEntry("extra params", "Bundle", "Gemini 生成的参数", true),
        ),
        notes = listOf(
            NoteCallout("warning", "Gemini Intelligence 部分 API 尚未完全公开，内容基于 I/O 2026 公布信息"),
        )
    )

    // Module G: Workout Decision Tree / 健身方案决策树
    private fun buildModuleG() = ModuleData(
        module = WearModuleType.G_DECISION_TREE,
        overview = "根据 App 类型和需求，通过决策树推荐健身追踪方案：接入 Wear Workout Tracker（Fitness Workouts API）vs 自建健身套件。",
        sections = listOf(
            ContentSection("g1", "决策树入口", "选择你的 App 类型：健身专项 App / 综合健康 App / 工具类 App / AI 助手类 App"),
            ContentSection("g2", "决策逻辑", "健身专项 -> 推荐接入 Workout Tracker（ASICS Runkeeper）\n综合健康 -> 推荐混合方案\n工具类 -> 推荐 Live Updates 桥接\nAI 助手 -> 推荐 AppFunctions（模块A）"),
        ),
        codeExamples = listOf(
            CodeExample("g_decision", "决策树 Kotlin 实现", CodeLanguage.KOTLIN,
"""fun recommendWorkoutSolution(appType: AppCategory): Recommendation {
    return when (appType) {
        AppCategory.FITNESS -> Recommendation(
            approach = "WEAR_WORKOUT_TRACKER",
            reason = "专用健身 App，推荐接入 Google 原生 Workout Tracker",
            cost = "低（集成成本 1-2周）",
            providers = listOf("Fitness Workouts API", "ASICS Runkeeper")
        )
        AppCategory.HEALTH -> Recommendation(
            approach = "HYBRID",
            reason = "综合健康 App，需要自有数据层 + Tracker 桥接",
            cost = "中（集成成本 2-4周）",
            providers = listOf("Workout Tracker", "Own Data Layer")
        )
        else -> Recommendation(
            approach = "LIVE_UPDATES",
            reason = "非健身 App，通过 Live Updates 同步数据摘要",
            cost = "低（集成成本 3-5天）",
            providers = listOf("Live Updates Bridge")
        )
    }
}
data class Recommendation(
    val approach: String,
    val reason: String,
    val cost: String,
    val providers: List<String>
)"""),
        ),
        apiTable = listOf(
            ApiEntry("AppCategory", "Enum", "App 类型输入", true),
            ApiEntry("Recommendation.approach", "String", "推荐方案", true),
            ApiEntry("Recommendation.cost", "String", "预估接入成本", false),
        ),
        notes = listOf(
            NoteCallout("tip", "决策树输出仅为建议，实际选型需要结合团队技术能力和产品需求综合判断"),
        )
    )

    // Module H: Power Optimization / 功耗优化指南
    private fun buildModuleH() = ModuleData(
        module = WearModuleType.H_POWER_OPT,
        overview = "Wear OS 7 的功耗优化是 Watch App 开发的核心挑战。后台行为受限严重，App 需要主动适配节电模式，否则功能会受限。",
        sections = listOf(
            ContentSection("h1", "后台行为限制", "Wear OS 7 对后台服务有严格限制：屏幕关闭后后台任务被推迟/WakeLock 受限/传感器采样率下降。"),
            ContentSection("h2", "节电模式影响", "节电模式激活时：GPS 采样率降至 1/min、心率传感器降频、屏幕刷新率降至 30Hz、后台网络请求被阻止。"),
            ContentSection("h3", "最佳实践", "①使用 Foreground Service 处理健身追踪；②批量处理网络请求；③使用 WorkManager 延迟非紧急任务；④传感器使用 SENSOR_DELAY_NORMAL 而非 SENSOR_DELAY_FASTEST。"),
        ),
        codeExamples = listOf(
            CodeExample("h_foreground", "ForeGround Service for Workout", CodeLanguage.KOTLIN,
"""class WorkoutForegroundService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("锻炼中")
            .setSmallIcon(R.drawable.ic_run)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }
}"""),
        ),
        apiTable = listOf(
            ApiEntry("SENSOR_DELAY_NORMAL", "Int", "推荐传感器采样间隔（省电）", false),
            ApiEntry("SENSOR_DELAY_FASTEST", "Int", "最高采样率（仅用于实时数据展示）", false),
            ApiEntry("Foreground Service", "Type", "必须用于长时间后台任务（如健身追踪）", true),
        ),
        notes = listOf(
            NoteCallout("warning", "在节电模式下，GPS 采样率降至 1/min，不适合需要精确轨迹的徒步/骑行 App"),
        )
    )

    // Module I: Canary Emulator / Canary Emulator 上手指南
    private fun buildModuleI() = ModuleData(
        module = WearModuleType.I_CANARY_EMU,
        overview = "Wear OS 7 Canary Emulator 是开发者预览版模拟器，支持完整的 Wear OS 7 API。Canary 版本稳定性有限，建议同时准备物理真机调试环境。",
        sections = listOf(
            ContentSection("i1", "模拟器配置", "Android Studio -> AVD Manager -> Wear OS 7 (API 35) Canary -> 创建虚拟设备。"),
            ContentSection("i2", "Gemini 集成调试", "在模拟器中登录 Google 账号，启用 Gemini on Wear。Logcat 中过滤 AppFunction 相关 tag 进行调试。"),
            ContentSection("i3", "AppFunctions 测试工作流", "①安装 App -> ②在手表设置中启用 AppFunction -> ③模拟器内用语音命令测试 -> ④Logcat 检查 Intent 路由。"),
            ContentSection("i4", "Fallback 方案", "Canary 模拟器不稳定时的备选：使用物理 Pixel Watch 3+ 真机调试。"),
        ),
        codeExamples = listOf(
            CodeExample("i_avd", "AVD 配置", CodeLanguage.YAML,
"""avd:
  name: wear-os-7-canary
  api_level: 35
  abi: google_apis/arm64-v8a
  screen: round
  google_apis: true"""),
        ),
        apiTable = listOf(
            ApiEntry("API Level", "Int", "推荐 API 35（Wear OS 7）", true),
            ApiEntry("GMS", "Boolean", "必须启用 Google Mobile Services", true),
        ),
        notes = listOf(
            NoteCallout("warning", "Canary 模拟器可能崩溃，建议同时准备物理真机作为备选调试环境"),
            NoteCallout("tip", "首次启动模拟器后耐心等待 GMS 初始化（约 5 分钟）"),
        )
    )

    // Module J: AppFunctions CI Plugin / AppFunctions CI 验证工具
    private fun buildModuleJ() = ModuleData(
        module = WearModuleType.J_CI_PLUGIN,
        overview = "wearos7-ci-plugin 是 Gradle 插件，在 CI 流水线中自动检测 AppFunctions 声明合规性，防止格式错误的 capability 逃逸到生产环境。",
        sections = listOf(
            ContentSection("j1", "插件安装", "在 build.gradle.kts 中应用插件：id(\"com.wearos7.appfunctions-checker\")"),
            ContentSection("j2", "合规检测规则", "检测 AppFunction name 是否全局唯一、action 是否符合规范、TriggerPhrase 是否包含有效触发词。"),
            ContentSection("j3", "输出报告", "插件输出 JSON 格式的合规报告，包含错误/warning/info 三级，以及修复建议。"),
        ),
        codeExamples = listOf(
            CodeExample("j_build", "build.gradle.kts 配置", CodeLanguage.KOTLIN,
"""wearos7AppFunctions {
    strictMode.set(true)
    reportFormat.set("json")
    excludeTestVariants.set(true)
}"""),
            CodeExample("j_gh", "GitHub Actions 集成", CodeLanguage.YAML,
"""- name: AppFunctions CI Check
  run: ./gradlew checkAppFunctions"""),
        ),
        apiTable = listOf(
            ApiEntry("strictMode", "Boolean", "true 时检测失败导致构建失败", false),
            ApiEntry("reportFormat", "String", "报告格式：json 或 html", false),
            ApiEntry("excludeTestVariants", "Boolean", "是否排除测试变体", false),
        ),
        notes = listOf(
            NoteCallout("tip", "建议在 PR 阶段强制运行 AppFunctions CI check，合规后再合并"),
        )
    )
}
