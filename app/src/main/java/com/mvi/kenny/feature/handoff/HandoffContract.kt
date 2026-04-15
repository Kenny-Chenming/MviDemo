package com.mvi.kenny.feature.handoff

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * HandoffContract — Android 17 Cross-Device Handoff API MVI 契约
 * ============================================================
 * MVI (Model-View-Intent) Architecture Pattern for Cross-Device Handoff.
 * Android 17 跨设备 Handoff API 开发者接入工具包
 *
 * 三要素：
 * - Model (State): 不可变数据类，UI 的单一数据源
 * - View: 消费 State 并渲染 UI 的 Composable 函数
 * - Intent: 用户意图（用户操作），ViewModel 处理并更新 State
 *
 * Effect: 一次性副作用（导航、Toast），通过 Channel 传递
 */

// =============================================================
// HandoffScreen — 子页面枚举
// =============================================================
enum class HandoffScreen(val title: String, val titleZh: String, val description: String) {
    DASHBOARD("Dashboard", "仪表盘", "Handoff 能力概览与功能入口"),
    API_LIBRARY("API Library", "API 封装库", "HandoffActivity 与 HandoffManager 核心用法"),
    ANALYZER("Analyzer", "适用性分析器", "Activity Handoff 适配评分"),
    SERIALIZATION("Serialization", "序列化框架", "状态传输与安全过滤配置"),
    PAIRING("Pairing", "设备配对", "设备发现与配对流程"),
    UX_GUIDE("UX Guide", "UX 设计规范", "通知样式与场景化设计指南"),
    DEBUG("Debug", "调试面板", "模拟发送接收与状态冲突")
}

// =============================================================
// HandoffFeature — 功能入口卡片
// =============================================================
data class HandoffFeature(
    val id: String,
    val screen: HandoffScreen,
    val iconName: String,
    val title: String,
    val titleZh: String,
    val description: String,
    val isAvailable: Boolean = true
)

// =============================================================
// PairedDevice — 已配对设备
// =============================================================
data class PairedDevice(
    val deviceId: String,
    val deviceName: String,
    val deviceType: DeviceType,
    val pairedAt: Long,
    val lastHandoffAt: Long? = null,
    val isOnline: Boolean = false
)

enum class DeviceType(val label: String, val labelZh: String) {
    PHONE("Phone", "手机"),
    TABLET("Tablet", "平板"),
    FOLDABLE("Foldable", "折叠屏"),
    TV("Android TV", "电视"),
    WATCH("Wear OS", "手表"),
    CAR("Android Auto", "车载"),
    UNKNOWN("Unknown", "未知")
}

// =============================================================
// HandoffRecord — Handoff 历史记录
// =============================================================
data class HandoffRecord(
    val id: String,
    val activityName: String,
    val targetDevice: String,
    val timestamp: Long,
    val status: HandoffStatus
)

enum class HandoffStatus(val label: String, val labelZh: String) {
    SUCCESS("Success", "成功"),
    FAILED("Failed", "失败"),
    PENDING("Pending", "待处理"),
    CANCELLED("Cancelled", "已取消")
}

// =============================================================
// ApiLibraryTab — API 库 Tab
// =============================================================
enum class ApiLibraryTab(val title: String, val titleZh: String) {
    QUICK_START("Quick Start", "快速接入"),
    API_USAGE("API Usage", "API 用法"),
    CONFIG("Configuration", "参数配置")
}

// =============================================================
// ApiExample — API 示例
// =============================================================
data class ApiExample(
    val title: String,
    val titleZh: String,
    val description: String,
    val descriptionZh: String,
    val code: String,
    val language: String = "kotlin"
)

// =============================================================
// HandoffConfig — Handoff 配置
// =============================================================
data class HandoffConfig(
    val activityName: String = "",
    val intentFilters: List<String> = emptyList(),
    val supportsLargeScreen: Boolean = true,
    val supportsKeyboard: Boolean = true,
    val webFallbackUrl: String = ""
)

// =============================================================
// ScanStatus — 扫描状态
// =============================================================
enum class ScanStatus(val label: String, val labelZh: String) {
    IDLE("Idle", "空闲"),
    SCANNING("Scanning", "扫描中"),
    DONE("Done", "完成"),
    ERROR("Error", "错误")
}

// =============================================================
// ActivityHandoffScore — Activity Handoff 评分
// =============================================================
data class ActivityHandoffScore(
    val activityName: String,
    val score: Int,
    val priority: HandoffPriority,
    val reasons: List<String>,
    val warnings: List<String>,
    val dependencies: List<String> = emptyList()
)

enum class HandoffPriority(val label: String, val labelZh: String) {
    HIGH("High", "高"),
    MEDIUM("Medium", "中"),
    LOW("Low", "低"),
    NOT_SUITABLE("Not Suitable", "不适合")
}

// =============================================================
// ActivityDependencyTree — Activity 依赖树
// =============================================================
data class ActivityDependencyTree(
    val rootActivities: List<ActivityNode>,
    val allActivities: List<ActivityHandoffScore>
)

data class ActivityNode(
    val activityName: String,
    val children: List<ActivityNode>,
    val score: Int
)

// =============================================================
// SerializationStrategy — 序列化策略
// =============================================================
enum class SerializationStrategy(val label: String, val labelZh: String) {
    LWW("Last Write Wins", "最后写入优先"),
    MERGE("Merge", "合并"),
    MANUAL("Manual Confirm", "手动确认")
}

// =============================================================
// SecurityFilter — 安全过滤规则
// =============================================================
data class SecurityFilter(
    val fieldName: String,
    val filterType: SecurityFilterType,
    val description: String
)

enum class SecurityFilterType(val label: String) {
    REDACT("Redact"),
    EXCLUDE("Exclude"),
    ENCRYPT("Encrypt")
}

// =============================================================
// PairingStep — 配对步骤
// =============================================================
enum class PairingStep(val stepNumber: Int, val title: String, val titleZh: String) {
    DISCOVER(1, "Discover", "发现设备"),
    PAIR(2, "Pair", "配对"),
    CONFIRM(3, "Confirm", "确认")
}

// =============================================================
// UXCategory & UXScenario — UX 指南
// =============================================================
enum class UXCategory(val label: String, val labelZh: String) {
    NOTIFICATIONS("Notifications", "通知样式"),
    LAUNCHER("Launcher", "启动器集成"),
    TASKBAR("Taskbar", "任务栏集成"),
    LARGE_SCREEN("Large Screen", "大屏适配")
}

data class UXScenario(
    val id: String,
    val type: AppType,
    val title: String,
    val titleZh: String,
    val description: String,
    val descriptionZh: String,
    val recommendations: List<String>
)

enum class AppType(val label: String, val labelZh: String) {
    EMAIL("Email", "邮件"),
    DOCUMENT("Document", "文档"),
    NOTES("Notes", "笔记"),
    BROWSER("Browser", "浏览器"),
    MEDIA("Media", "媒体"),
    NAVIGATION("Navigation", "导航")
}

// =============================================================
// DebugLogEntry — 调试日志
// =============================================================
data class DebugLogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val message: String,
    val data: String? = null
)

enum class LogLevel(val label: String, val color: Color) {
    INFO("Info", Color(0xFF2196F3)),
    SUCCESS("Success", Color(0xFF4CAF50)),
    WARNING("Warning", Color(0xFFFF9800)),
    ERROR("Error", Color(0xFFB3261E))
}

// =============================================================
// States — 状态定义
// =============================================================
data class HandoffDashboardState(
    val isHandoffAvailable: Boolean = true,
    val pairedDevices: List<PairedDevice> = emptyList(),
    val recentHandoffs: List<HandoffRecord> = emptyList(),
    val allFeatures: List<HandoffFeature> = getDefaultFeatures(),
    val isScanning: Boolean = false,
    val selectedFeature: HandoffFeature? = null,
    val error: String? = null
)

data class HandoffApiLibraryState(
    val selectedTab: ApiLibraryTab = ApiLibraryTab.QUICK_START,
    val quickStartCode: String = getDefaultQuickStartCode(),
    val apiUsageExamples: List<ApiExample> = getDefaultApiExamples(),
    val handoffConfig: HandoffConfig = HandoffConfig(),
    val copiedItem: String? = null
)

data class HandoffAnalyzerState(
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val analyzedActivities: List<ActivityHandoffScore> = emptyList(),
    val selectedActivity: ActivityHandoffScore? = null,
    val dependencyTree: ActivityDependencyTree? = null,
    val apkPath: String = "",
    val scanProgress: Int = 0,
    val error: String? = null
)

data class HandoffSerializationState(
    val codeTemplate: String = getDefaultSerializationTemplate(),
    val selectedStrategy: SerializationStrategy = SerializationStrategy.LWW,
    val securityFilters: List<SecurityFilter> = getDefaultSecurityFilters(),
    val sampleJson: String = getDefaultSampleJson(),
    val isGenerating: Boolean = false
)

data class HandoffPairingState(
    val pairedDevices: List<PairedDevice> = getMockPairedDevices(),
    val discoveredDevices: List<PairedDevice> = emptyList(),
    val currentStep: PairingStep = PairingStep.DISCOVER,
    val isDiscovering: Boolean = false,
    val webFallbackUrl: String = "",
    val selectedDevice: PairedDevice? = null,
    val pairingProgress: Int = 0
)

data class HandoffUXGuideState(
    val selectedCategory: UXCategory = UXCategory.NOTIFICATIONS,
    val selectedScenario: UXScenario? = null,
    val showNotificationPreview: Boolean = true
)

data class HandoffDebugState(
    val isSimulatingSend: Boolean = false,
    val isSimulatingReceive: Boolean = false,
    val serializedJson: String = "",
    val deserializedData: String = "",
    val simulatedConflict: String = "",
    val isConflictResolved: Boolean = false,
    val selectedResolution: SerializationStrategy = SerializationStrategy.LWW,
    val debugLogs: List<DebugLogEntry> = emptyList()
)

// =============================================================
// Intent — 用户意图
// =============================================================
sealed interface HandoffIntent {
    data object StartDeviceScan : HandoffIntent
    data object StopDeviceScan : HandoffIntent
    data class NavigateToFeature(val feature: HandoffFeature) : HandoffIntent
    data class RemovePairedDevice(val deviceId: String) : HandoffIntent
    data object RefreshHandoffStatus : HandoffIntent
    data class SelectApiTab(val tab: ApiLibraryTab) : HandoffIntent
    data class CopyCode(val code: String, val itemId: String) : HandoffIntent
    data class UpdateHandoffConfig(val config: HandoffConfig) : HandoffIntent
    data class StartScan(val apkPath: String) : HandoffIntent
    data class SelectActivityDetail(val activity: ActivityHandoffScore) : HandoffIntent
    data object ClearActivitySelection : HandoffIntent
    data class SelectStrategy(val strategy: SerializationStrategy) : HandoffIntent
    data class AddSecurityFilter(val filter: SecurityFilter) : HandoffIntent
    data class RemoveSecurityFilter(val fieldName: String) : HandoffIntent
    data object GenerateSerializationCode : HandoffIntent
    data object StartDiscovery : HandoffIntent
    data object StopDiscovery : HandoffIntent
    data class SelectDeviceForPairing(val device: PairedDevice) : HandoffIntent
    data class ConfirmPairing(val device: PairedDevice) : HandoffIntent
    data object CancelPairing : HandoffIntent
    data class UpdateWebFallbackUrl(val url: String) : HandoffIntent
    data class SelectUXCategory(val category: UXCategory) : HandoffIntent
    data class SelectUXScenario(val scenario: UXScenario) : HandoffIntent
    data object ToggleNotificationPreview : HandoffIntent
    data object SimulateSend : HandoffIntent
    data object SimulateReceive : HandoffIntent
    data object SimulateConflict : HandoffIntent
    data class ResolveConflict(val strategy: SerializationStrategy) : HandoffIntent
    data object ClearDebugLogs : HandoffIntent
}

// =============================================================
// Effect — 副作用
// =============================================================
sealed interface HandoffEffect {
    data class ShowToast(val message: String) : HandoffEffect
    data class ShowError(val throwable: Throwable) : HandoffEffect
    data class NavigateTo(val screen: HandoffScreen) : HandoffEffect
    data object NavigateBack : HandoffEffect
    data class CopyToClipboardSuccess(val label: String) : HandoffEffect
    data class OpenExternalLink(val url: String) : HandoffEffect
    data class PairingSuccess(val deviceName: String) : HandoffEffect
}

// =============================================================
// Default Data Helpers / 默认数据辅助函数
// =============================================================
private fun getDefaultFeatures(): List<HandoffFeature> = listOf(
    HandoffFeature("api_library", HandoffScreen.API_LIBRARY, "Share", "Handoff API 封装库", "API Library", "HandoffActivity 与 HandoffManager 核心用法，快速接入模板"),
    HandoffFeature("analyzer", HandoffScreen.ANALYZER, "Analytics", "适用性分析器", "Suitability Analyzer", "分析 APK/Activity 的 Handoff 适配评分与优先级"),
    HandoffFeature("serialization", HandoffScreen.SERIALIZATION, "DataObject", "序列化框架", "Serialization Framework", "状态传输配置、安全过滤、冲突解决策略"),
    HandoffFeature("pairing", HandoffScreen.PAIRING, "Devices", "设备配对", "Device Pairing", "设备发现、配对流程、Web 降级配置"),
    HandoffFeature("ux_guide", HandoffScreen.UX_GUIDE, "DesignServices", "UX 设计规范", "UX Guide", "通知样式、启动器集成、大屏适配指南"),
    HandoffFeature("debug", HandoffScreen.DEBUG, "BugReport", "调试面板", "Debug Panel", "模拟发送接收、JSON 查看、冲突调试")
)

private fun getMockPairedDevices(): List<PairedDevice> = listOf(
    PairedDevice("device_1", "Pixel 8 Pro", DeviceType.PHONE, System.currentTimeMillis() - 86400000, System.currentTimeMillis() - 3600000, true),
    PairedDevice("device_2", "Samsung Tab S9", DeviceType.TABLET, System.currentTimeMillis() - 172800000, null, false),
    PairedDevice("device_3", "Pixel Fold", DeviceType.FOLDABLE, System.currentTimeMillis() - 259200000, System.currentTimeMillis() - 7200000, true)
)

private fun getDefaultQuickStartCode(): String = """
// HandoffActivity.kt - 快速接入跨设备 Handoff
@RequiresApi(17)
class MainActivity : AppCompatActivity() {

    private val handoffManager by lazy { HandoffManager(this) }

    override fun onHandoffActivityRequested(request: HandoffActivityRequest) {
        super.onHandoffActivityRequested(request)
        val targetActivity = request.targetActivity
        val extras = request.extras
        startActivity(Intent(this, targetActivity).apply {
            putExtras(extras)
        })
    }

    private fun sendHandoffToDevice(targetDevice: String) {
        handoffManager.createHandoffActivityRequest(
            sourceActivity = this::class.java,
            targetDeviceId = targetDevice,
            extras = bundleOf("key" to "value")
        )
    }
}
""".trimIndent()

private fun getDefaultApiExamples(): List<ApiExample> = listOf(
    ApiExample(
        "HandoffActivity.onHandoffActivityRequested()",
        "处理 Handoff 请求",
        "当用户从其他设备发起 Handoff 时，系统会调用此方法",
        "Called when user initiates Handoff from another device",
        """
@RequiresApi(17)
override fun onHandoffActivityRequested(request: HandoffActivityRequest) {
    super.onHandoffActivityRequested(request)
    val sourceDevice = request.sourceDevice
    val targetActivity = request.targetActivity
    val extras = request.extras
    handleHandoffRequest(sourceDevice, targetActivity, extras)
}
        """.trimIndent()
    ),
    ApiExample(
        "HandoffManager API",
        "HandoffManager 封装",
        "使用 HandoffManager 管理设备发现与 Handoff 请求",
        "Use HandoffManager for device discovery and Handoff requests",
        """
class HandoffManager(private val context: Context) {
    suspend fun discoverDevices(): List<DeviceInfo> = withContext(Dispatchers.IO) {
        BluetoothAdapter.getDefaultAdapter().bondedDevices.map { btDevice ->
            DeviceInfo(id = btDevice.address, name = btDevice.name, type = inferDeviceType(btDevice))
        }
    }

    suspend fun createHandoffRequest(
        sourceActivity: Class<*>,
        targetDeviceId: String,
        extras: Bundle
    ): HandoffActivityRequest = withContext(Dispatchers.IO) {
        HandoffActivityRequest(
            sourceDevice = getCurrentDeviceInfo(),
            targetDeviceId = targetDeviceId,
            targetActivity = sourceActivity,
            extras = sanitizeExtras(extras)
        )
    }
}
        """.trimIndent()
    ),
    ApiExample(
        "Large Screen 联动",
        "Large Screen Integration",
        "结合 WindowManager 实现大屏设备上的 Handoff 优化",
        "Combine with WindowManager for Large Screen optimization",
        """
@RequiresApi(17)
class HandoffAwareActivity : AppCompatActivity(), WindowSizeClass.OnWindowSizeChangeListener {
    override fun onWindowSizeChanged(windowSizeClass: WindowSizeClass) {
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.COMPACT -> configureCompactHandoff()
            WindowWidthSizeClass.MEDIUM -> configureDualPaneHandoff()
            WindowWidthSizeClass.EXPANDED -> configureDesktopHandoff()
        }
    }
}
        """.trimIndent()
    )
)

private fun getDefaultSerializationTemplate(): String = """
// HandoffData.kt - 使用 @Serializable 注解标记需要传输的数据类
@Serializable
data class HandoffData(
    val activityName: String,
    val state: Map<String, String>,
    val timestamp: Long,
    val deviceId: String,
    val version: Int = 1
)

// 安全过滤：在序列化前调用
object HandoffSanitizer {
    val SENSITIVE_KEYS = listOf("password", "token", "apiKey", "secret", "creditCard", "ssn")
    fun sanitize(data: HandoffData): HandoffData {
        val sanitizedState = data.state.filterKeys { it.lowercase() !in SENSITIVE_KEYS.map { k -> k.lowercase() } }
        return data.copy(state = sanitizedState)
    }
}

// 冲突解决策略
sealed class ConflictResolution {
    data class LWW(val localTimestamp: Long, val remoteTimestamp: Long) : ConflictResolution()
    data class Merge(val localState: Map<String, String>, val remoteState: Map<String, String>) : ConflictResolution()
    data class Manual(val conflictData: ConflictData) : ConflictResolution()
}
""".trimIndent()

private fun getDefaultSecurityFilters(): List<SecurityFilter> = listOf(
    SecurityFilter("password", SecurityFilterType.EXCLUDE, "密码字段不传输"),
    SecurityFilter("token", SecurityFilterType.EXCLUDE, "Token 不传输"),
    SecurityFilter("apiKey", SecurityFilterType.EXCLUDE, "API Key 不传输"),
    SecurityFilter("creditCard", SecurityFilterType.REDACT, "信用卡号打码处理"),
    SecurityFilter("ssn", SecurityFilterType.REDACT, "社保号打码处理")
)

private fun getDefaultSampleJson(): String = """
{
    "activityName": "com.example.app.MainActivity",
    "state": {
        "documentId": "doc_12345",
        "scrollPosition": "0.35",
        "selectedTab": "inbox"
    },
    "timestamp": 1713254400000,
    "deviceId": "device_abc123",
    "version": 1
}
""".trimIndent()

/**
 * Handoff Feature Color Palette / Handoff 功能配色
 */
object HandoffColors {
    val Primary = Color(0xFF6750A4)
    val Secondary = Color(0xFF625B71)
    val Tertiary = Color(0xFF7D5260)
    val HandoffActive = Color(0xFF4CAF50)
    val HandoffInactive = Color(0xFF9E9E9E)
    val Error = Color(0xFFB3261E)
    val Surface = Color(0xFFFFFBFE)
    val Background = Color(0xFFFFFBFE)
}
