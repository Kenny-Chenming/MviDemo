package com.mvi.kenny.feature.handoff

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// =============================================================
// HandoffContract — Android 17 Handoff API 跨设备连续性开发工具包
// MVI Contract / MVI 契约
// =============================================================
// PRD-153 | Android 17 Handoff API Cross-Device Continuity Dev Toolkit
//
// MVI Architecture Pattern / MVI 架构模式
// - Model (State): Immutable data class — single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, etc.) via Channel
//
// 8 Tool Modules / 8个工具模块:
// 1. HandoffActivityDataBuilder  — 状态打包配置
// 2. HandoffIntentBuilder        — Intent 构建预览
// 3. DecisionEngine              — 适用性决策引擎
// 4. PrivacyCompliance          — 隐私合规检测
// 5. DebugPanel                  — 跨设备调试面板
// 6. SyncTemplate               — 多设备同步框架集成
// 7. AppBundleGuide             — App Bundle 联动
// 8. FallbackStrategy           — 降级策略

// =============================================================
// HandoffTool — 工具Tab枚举
// =============================================================
/**
 * Handoff tool module / Handoff 工具模块
 *
 * Each tab represents an independent tool in the Handoff development suite.
 *
 * @param titleZh Chinese title / 中文标题
 * @param titleEn English title / 英文标题
 * @param description Tool description / 工具描述
 */
enum class HandoffTool(
    val titleZh: String,
    val titleEn: String,
    val description: String
) {
    INTEGRATE_TEMPLATE("集成模板", "Integrate Template", "onHandoffActivityRequested() 完整集成模板"),
    INTENT_BUILDER("Intent构建", "Intent Builder", "Handoff Intent 构建预览与代码生成"),
    DECISION_ENGINE("决策引擎", "Decision Engine", "Activity Handoff 适用性分析"),
    PRIVACY_COMPLIANCE("隐私合规", "Privacy Compliance", "GDPR/CCPA 跨设备合规检测"),
    DEBUG_PANEL("调试面板", "Debug Panel", "跨设备 Handoff 流程模拟调试"),
    SYNC_TEMPLATE("同步模板", "Sync Template", "多设备同步框架集成模板"),
    APP_BUNDLE_GUIDE("Bundle联动", "App Bundle Guide", "Play Install API 引导安装"),
    FALLBACK_STRATEGY("降级策略", "Fallback Strategy", "Handoff 不可用时的降级方案")
}

// =============================================================
// DeviceType — 设备类型
// =============================================================
/**
 * Android device type / Android 设备类型
 *
 * @param label Display label / 显示标签
 * @param icon Material icon name / 图标名称
 */
enum class DeviceType(val label: String) {
    PHONE("手机 / Phone"),
    TABLET("平板 / Tablet"),
    AUTOMOTIVE("车机 / Automotive"),
    TV("电视 / TV"),
    WATCH("手表 / Watch")
}

// =============================================================
// ActivityInput — Activity 输入
// =============================================================
/**
 * Activity input for decision engine analysis / 决策引擎分析的 Activity 输入
 *
 * @param name Activity class name / Activity 名称
 * @param packageName Package name / 包名
 * @param hasUserData Whether it contains user data / 是否包含用户数据
 * @param hasNetworkData Whether it requires network / 是否需要网络
 * @param dataSensitivity Data sensitivity level / 数据敏感级别
 */
data class ActivityInput(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val packageName: String = "",
    val hasUserData: Boolean = false,
    val hasNetworkData: Boolean = false,
    val dataSensitivity: DataSensitivity = DataSensitivity.LOW
)

/**
 * Data sensitivity level / 数据敏感级别
 */
enum class DataSensitivity(val label: String, val color: Color) {
    LOW("低 / Low", Color(0xFF4CAF50)),
    MEDIUM("中 / Medium", Color(0xFFFF9800)),
    HIGH("高 / High", Color(0xFFF44336)),
    CRITICAL("极高 / Critical", Color(0xFF9C27B0))
}

// =============================================================
// DecisionResult — 决策结果
// =============================================================
/**
 * Handoff suitability decision result / Handoff 适用性决策结果
 *
 * @param activityId Activity identifier / Activity 标识
 * @param activityName Activity name / Activity 名称
 * @param suitable Whether suitable for Handoff / 是否适合 Handoff
 * @param confidence Confidence score (0.0-1.0) / 置信度
 * @param reasons Analysis reasons / 分析原因
 * @param risks Risk items / 风险项
 */
data class DecisionResult(
    val activityId: String,
    val activityName: String,
    val suitable: Boolean,
    val confidence: Float,
    val reasons: List<String> = emptyList(),
    val risks: List<String> = emptyList()
)

// =============================================================
// PrivacyReport — 隐私合规报告
// =============================================================
/**
 * Privacy compliance report / 隐私合规报告
 *
 * @param overallStatus Overall compliance status / 整体合规状态
 * @param gdprCompliant GDPR compliance / GDPR 合规
 * @param ccpCompliant CCPA compliance / CCPA 合规
 * @param crossDeviceDataItems Data items transferred across devices / 跨设备传输数据项
 * @param violations Violation items / 违规项
 * @param suggestions Compliance improvement suggestions / 改进建议
 */
data class PrivacyReport(
    val overallStatus: ComplianceStatus,
    val gdprCompliant: Boolean,
    val ccpCompliant: Boolean,
    val crossDeviceDataItems: List<CrossDeviceDataItem> = emptyList(),
    val violations: List<PrivacyViolation> = emptyList(),
    val suggestions: List<String> = emptyList()
)

/**
 * Compliance status / 合规状态
 */
enum class ComplianceStatus(val label: String, val color: Color) {
    PASS("通过 / Pass", Color(0xFF146B3A)),
    WARN("警告 / Warn", Color(0xFFF5A623)),
    FAIL("不通过 / Fail", Color(0xFFB3261E))
}

/**
 * Data item that may be transferred across devices / 可能跨设备传输的数据项
 *
 * @param name Data item name / 数据项名称
 * @param sensitivity Data sensitivity / 数据敏感度
 * @param requiresEncryption Whether encryption is required / 是否需要加密
 */
data class CrossDeviceDataItem(
    val name: String,
    val sensitivity: DataSensitivity,
    val requiresEncryption: Boolean
)

/**
 * Privacy violation item / 隐私违规项
 *
 * @param regulation Regulation name / 法规名称
 * @param description Violation description / 违规描述
 * @param severity Severity level / 严重程度
 */
data class PrivacyViolation(
    val regulation: String,
    val description: String,
    val severity: DataSensitivity
)

// =============================================================
// DebugSession — 调试会话
// =============================================================
/**
 * Handoff debug session / Handoff 调试会话
 *
 * @param id Session ID / 会话ID
 * @param sourceDevice Source device type / 源设备类型
 * @param targetDevice Target device type / 目标设备类型
 * @param status Session status / 会话状态
 * @param steps Session steps / 会话步骤
 * @param resultState Resulting activity state / 结果状态
 */
data class DebugSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sourceDevice: DeviceType,
    val targetDevice: DeviceType,
    val status: SessionStatus,
    val steps: List<HandoffStep> = emptyList(),
    val resultState: String = ""
)

/**
 * Session status / 会话状态
 */
enum class SessionStatus(val label: String) {
    PENDING("待执行 / Pending"),
    RUNNING("执行中 / Running"),
    SUCCESS("成功 / Success"),
    FAILED("失败 / Failed")
}

/**
 * Handoff flow step / Handoff 流程步骤
 *
 * @param stepNumber Step number / 步骤编号
 * @param title Step title / 步骤标题
 * @param description Step description / 步骤描述
 * @param status Step status / 步骤状态
 */
data class HandoffStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val status: StepStatus
)

/**
 * Step status / 步骤状态
 */
enum class StepStatus { PENDING, IN_PROGRESS, DONE, ERROR }

// =============================================================
// SyncConfig — 同步配置
// =============================================================
/**
 * Multi-device sync configuration / 多设备同步配置
 *
 * @param syncMethod Sync method / 同步方法
 * @param conflictResolution Conflict resolution strategy / 冲突解决策略
 * @param enabled Whether sync is enabled / 是否启用
 */
data class SyncConfig(
    val syncMethod: SyncMethod = SyncMethod.LAST_WRITE_WINS,
    val conflictResolution: ConflictResolution = ConflictResolution.LAST_WRITE_WINS,
    val enabled: Boolean = true
)

/**
 * Sync method / 同步方法
 */
enum class SyncMethod(val label: String) {
    LAST_WRITE_WINS("最后写入优先 / Last Write Wins"),
    MERGE("合并 / Merge"),
    USER_CONFIRMED("用户确认 / User Confirmed")
}

/**
 * Conflict resolution strategy / 冲突解决策略
 */
enum class ConflictResolution(val label: String) {
    LAST_WRITE_WINS("最后写入优先 / Last Write Wins"),
    PRIORITY_BASED("优先级优先 / Priority Based"),
    USER_CONFIRMED("用户确认 / User Confirmed"),
    KEEP_BOTH("保留双方 / Keep Both")
}

// =============================================================
// AppBundleConfig — App Bundle 配置
// =============================================================
/**
 * App Bundle install guide configuration / App Bundle 安装引导配置
 *
 * @param packageName App package name / App 包名
 * @param hasInstalled Whether app is installed on target / 目标设备是否已安装
 * @param installUrl Play Store install URL / Play Store 安装链接
 */
data class AppBundleConfig(
    val packageName: String = "",
    val hasInstalled: Boolean = false,
    val installUrl: String = ""
)

// =============================================================
// FallbackOption — 降级选项
// =============================================================
/**
 * Fallback option when Handoff is unavailable / Handoff 不可用时的降级选项
 *
 * @param type Fallback type / 降级类型
 * @param label Display label / 显示标签
 * @param description Fallback description / 降级描述
 * @param isRecommended Whether recommended / 是否推荐
 */
data class FallbackOption(
    val type: FallbackType,
    val label: String,
    val description: String,
    val isRecommended: Boolean = false
)

/**
 * Fallback type / 降级类型
 */
enum class FallbackType(val label: String) {
    DEEP_LINK("深链接 / Deep Link", "Open content via deep link on target device"),
    WEB_FALLBACK("Web降级 / Web Fallback", "Open web version as fallback"),
    NOTIFICATION("推送通知 / Push Notification", "Send push notification to continue"),
    QR_CODE("二维码 / QR Code", "Generate QR code for manual transfer"),
    NONE("无可用降级 / No Fallback", "Handoff is required, no alternative")
}

// =============================================================
// HandoffCodeTemplate — 代码模板
// =============================================================
/**
 * Handoff integration code template / Handoff 集成代码模板
 *
 * @param language Programming language / 编程语言
 * @param code The generated code / 生成的代码
 * @param description Template description / 模板描述
 */
data class HandoffCodeTemplate(
    val language: CodeLanguage,
    val code: String,
    val description: String
)

/**
 * Programming language / 编程语言
 */
enum class CodeLanguage(val label: String) {
    KOTLIN("Kotlin"),
    JAVA("Java")
}

// =============================================================
// HandoffState — MVI State
// =============================================================
/**
 * Handoff MVI State / Handoff MVI 状态
 *
 * Single source of truth for the entire Handoff toolkit UI.
 * All 8 tool modules share this state, with currentTool indicating
 * which module is currently active.
 *
 * @param currentTool Currently active tool tab / 当前工具Tab
 * @param isLoading Loading state / 加载状态
 * @param error Error message / 错误信息
 *
 * // INTEGRATE_TEMPLATE state
 * @param activityDataFields Serialized Activity data fields / 序列化的Activity数据字段
 * @param generatedCodeTemplates Generated integration code / 生成的集成代码
 *
 * // INTENT_BUILDER state
 * @param intentPackageName Intent package name / Intent 包名
 * @param intentClassName Intent class name / Intent 类名
 *
 * // DECISION_ENGINE state
 * @param activityInputs List of activities to analyze / 待分析的Activity列表
 * @param decisionResults Decision results / 决策结果
 * @param analyzingActivityId Currently analyzing activity ID / 当前分析的Activity ID
 *
 * // PRIVACY_COMPLIANCE state
 * @param privacyReport Privacy compliance report / 隐私合规报告
 * @param dataTypesInput Input data types / 输入数据类型
 *
 * // DEBUG_PANEL state
 * @param debugSessions Debug session history / 调试会话历史
 * @param activeSession Currently active debug session / 当前活动的调试会话
 * @param selectedSourceDevice Selected source device type / 选中的源设备类型
 * @param selectedTargetDevice Selected target device type / 选中的目标设备类型
 *
 * // SYNC_TEMPLATE state
 * @param syncConfig Sync configuration / 同步配置
 *
 * // APP_BUNDLE state
 * @param appBundleConfig App Bundle configuration / App Bundle配置
 *
 * // FALLBACK_STRATEGY state
 * @param fallbackOptions Available fallback options / 可用降级选项
 * @param selectedFallback Selected fallback option / 选中的降级选项
 */
data class HandoffState(
    val currentTool: HandoffTool = HandoffTool.INTEGRATE_TEMPLATE,
    val isLoading: Boolean = false,
    val error: String? = null,

    // INTEGRATE_TEMPLATE
    val activityDataFields: List<DataField> = emptyList(),
    val generatedCodeTemplates: List<HandoffCodeTemplate> = emptyList(),

    // INTENT_BUILDER
    val intentPackageName: String = "",
    val intentClassName: String = "",

    // DECISION_ENGINE
    val activityInputs: List<ActivityInput> = listOf(ActivityInput()),
    val decisionResults: List<DecisionResult> = emptyList(),
    val analyzingActivityId: String? = null,

    // PRIVACY_COMPLIANCE
    val privacyReport: PrivacyReport? = null,
    val dataTypesInput: List<String> = emptyList(),

    // DEBUG_PANEL
    val debugSessions: List<DebugSession> = emptyList(),
    val activeSession: DebugSession? = null,
    val selectedSourceDevice: DeviceType = DeviceType.PHONE,
    val selectedTargetDevice: DeviceType = DeviceType.TABLET,

    // SYNC_TEMPLATE
    val syncConfig: SyncConfig = SyncConfig(),

    // APP_BUNDLE
    val appBundleConfig: AppBundleConfig = AppBundleConfig(),

    // FALLBACK_STRATEGY
    val fallbackOptions: List<FallbackOption> = FallbackType.entries.map {
        FallbackOption(
            type = it,
            label = it.label,
            description = when (it) {
                FallbackType.DEEP_LINK -> "在目标设备上通过深链接打开对应内容 / Open content via deep link"
                FallbackType.WEB_FALLBACK -> "在目标设备上打开网页版作为降级方案 / Use web version as fallback"
                FallbackType.NOTIFICATION -> "发送推送通知引导用户继续 / Send push to guide user"
                FallbackType.QR_CODE -> "生成二维码手动传输数据 / Generate QR code for manual transfer"
                FallbackType.NONE -> "无可用降级方案，Handoff 是唯一方式 / No fallback available"
            },
            isRecommended = it == FallbackType.DEEP_LINK
        )
    },
    val selectedFallback: FallbackType = FallbackType.DEEP_LINK
)

/**
 * Data field for HandoffActivityData serialization / HandoffActivityData 序列化数据字段
 */
data class DataField(
    val name: String,
    val type: String,
    val isRequired: Boolean,
    val isEncrypted: Boolean
)

// =============================================================
// HandoffIntent — MVI Intent
// =============================================================
/**
 * Handoff MVI Intent / Handoff MVI 意图
 *
 * User intentions that trigger state changes in the ViewModel.
 */
sealed class HandoffIntent {
    // Tool navigation / 工具导航
    data class SelectTool(val tool: HandoffTool) : HandoffIntent()

    // INTEGRATE_TEMPLATE intents
    data class AddDataField(val field: DataField) : HandoffIntent()
    data class RemoveDataField(val index: Int) : HandoffIntent()
    data class UpdateDataField(val index: Int, val field: DataField) : HandoffIntent()
    object GenerateCode : HandoffIntent()

    // INTENT_BUILDER intents
    data class UpdateIntentPackage(val packageName: String) : HandoffIntent()
    data class UpdateIntentClass(val className: String) : HandoffIntent()

    // DECISION_ENGINE intents
    data class AddActivityInput(val input: ActivityInput) : HandoffIntent()
    data class RemoveActivityInput(val id: String) : HandoffIntent()
    data class UpdateActivityInput(val input: ActivityInput) : HandoffIntent()
    object AnalyzeActivities : HandoffIntent()

    // PRIVACY_COMPLIANCE intents
    data class AddDataType(val dataType: String) : HandoffIntent()
    data class RemoveDataType(val dataType: String) : HandoffIntent()
    data class GeneratePrivacyReport(val dataTypes: List<String>) : HandoffIntent()

    // DEBUG_PANEL intents
    data class SelectSourceDevice(val device: DeviceType) : HandoffIntent()
    data class SelectTargetDevice(val device: DeviceType) : HandoffIntent()
    object StartDebugSession : HandoffIntent()
    data class SelectDebugSession(val session: DebugSession) : HandoffIntent()

    // SYNC_TEMPLATE intents
    data class UpdateSyncConfig(val config: SyncConfig) : HandoffIntent()

    // APP_BUNDLE intents
    data class UpdateAppBundleConfig(val config: AppBundleConfig) : HandoffIntent()

    // FALLBACK_STRATEGY intents
    data class SelectFallback(val type: FallbackType) : HandoffIntent()

    // Common intents
    object ClearError : HandoffIntent()
    object DismissLoading : HandoffIntent()
}

// =============================================================
// HandoffEffect — MVI Effect
// =============================================================
/**
 * Handoff MVI Effect / Handoff MVI 副作用
 *
 * One-time side effects emitted via Channel.
 * These are consumed by the View composable for one-time actions.
 */
sealed class HandoffEffect {
    data class ShowToast(val message: String) : HandoffEffect()
    data class ShowSnackbar(val message: String, val action: String? = null) : HandoffEffect()
    data class NavigateTo(val tool: HandoffTool) : HandoffEffect()
    data class CopyToClipboard(val text: String, val label: String) : HandoffEffect()
    data class ExportReport(val content: String, val filename: String) : HandoffEffect()
    data class DebugSessionCompleted(val session: DebugSession) : HandoffEffect()
    data class PrivacyReportGenerated(val report: PrivacyReport) : HandoffEffect()
    data class DecisionAnalysisCompleted(val results: List<DecisionResult>) : HandoffEffect()
}
