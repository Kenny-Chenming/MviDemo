package com.mvi.kenny.feature.lannetwork

// ================================================================
// LanPermissionContract — PRD-199 Android 17 Local Network Permission
// 合规检测工具包 MVI 契约
// ================================================================
// MVI Architecture Contract for Android 17 Local Network Permission
// compliance detection & migration toolkit.
//
// PRD-199: Android 17 Local Network Permission 合规检测工具包
// Design: memory/agency/designs/PRD-199-Android-17-Local-Network-Permission-合规检测工具包.md
//
// MVI 三要素 / Three Pillars:
//   State  — Immutable page state, single source of truth
//   Intent — User intentions, ViewModel processes and updates State
//   Effect — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

// =============================================================
// ModuleType — 工具模块类型枚举（首页 8 卡片入口）
// =============================================================
/**
 * Tool module type / 工具模块类型枚举
 *
 * Each card on the dashboard corresponds to one module.
 *
 * @param titleZh Chinese title / 中文标题
 * @param titleEn English title / 英文标题
 * @param description Module description / 模块描述
 * @param priority Priority level / 优先级
 * @param iconEmoji Module icon emoji / 模块图标
 */
enum class ModuleType(
    val titleZh: String,
    val titleEn: String,
    val description: String,
    val priority: String,
    val iconEmoji: String
) {
    /** 🔍 LAN 权限影响扫描器 — 扫描 App 代码中所有局域网访问模式 */
    SCANNER(
        titleZh = "LAN 权限影响扫描器",
        titleEn = "LAN Permission Impact Scanner",
        description = "Scans all local network access patterns: socket, DatagramSocket, BroadcastReceiver, HttpURLConnection, OkHttp, Retrofit",
        priority = "P0",
        iconEmoji = "🔍"
    ),
    /** 📝 运行时权限集成模板 — ACCESS_LOCAL_NETWORK 完整权限请求代码 */
    PERMISSION_TEMPLATE(
        titleZh = "运行时权限集成模板",
        titleEn = "Runtime Permission Template",
        description = "Complete permission request code with rationale UI, permanent denial handling, and graceful degradation",
        priority = "P1",
        iconEmoji = "📝"
    ),
    /** 🎯 系统设备选择器集成 — 隐私保护路径的 NearbyDevice API */
    DEVICE_PICKER(
        titleZh = "系统设备选择器集成",
        titleEn = "System Device Picker Integration",
        description = "Privacy-preserving device picker using NearbyDevice API as alternative to runtime permission",
        priority = "P1",
        iconEmoji = "🎯"
    ),
    /** ⚙️ CI 合规检测工具 — Gradle 插件验证 App 是否已适配 Local Network Protection */
    CI_TOOL(
        titleZh = "CI 合规检测工具",
        titleEn = "CI Compliance Detection Tool",
        description = "Gradle plugin (lan-permission-check) to validate Local Network Protection compliance in CI",
        priority = "P1",
        iconEmoji = "⚙️"
    ),
    /** 🔄 降级策略模板 — 权限拒绝时的优雅降级 UX */
    FALLBACK_TEMPLATE(
        titleZh = "降级策略模板",
        titleEn = "Fallback Strategy Template",
        description = "Graceful degradation UX for casting/file transfer/smart home apps when permission is denied",
        priority = "P2",
        iconEmoji = "🔄"
    ),
    /** 📡 LAN × NEARBY_WIFI_DEVICES 协同指南 — 两个权限的适用场景分析 */
    COORDINATION_GUIDE(
        titleZh = "LAN × Wi-Fi 协同指南",
        titleEn = "LAN × NEARBY_WIFI_DEVICES Guide",
        description = "Scenario analysis for ACCESS_LOCAL_NETWORK and NEARBY_WIFI_DEVICES combination usage",
        priority = "P2",
        iconEmoji = "📡"
    ),
    /** 📋 Legacy App 兼容性评估 — targetSdk <37 升级到 37+ 的权限影响 */
    LEGACY_EVAL(
        titleZh = "Legacy App 兼容性评估",
        titleEn = "Legacy App Compatibility Eval",
        description = "Evaluates permission impact when upgrading App from targetSdk <37 to 37+",
        priority = "P2",
        iconEmoji = "📋"
    ),
    /** 🌐 设备发现新模式指南 — mDNS/Bonjour/UPnP 正确用法 */
    DISCOVERY_GUIDE(
        titleZh = "设备发现新模式指南",
        titleEn = "Device Discovery New Pattern Guide",
        description = "Privacy-first device discovery design patterns: mDNS, Bonjour, UPnP correct usage",
        priority = "P2",
        iconEmoji = "🌐"
    )
}

// =============================================================
// RiskLevel — 风险等级
// =============================================================
/**
 * Risk level for scan results / 扫描结果风险等级
 *
 * @param label Risk level label / 风险等级标签
 * @param color Risk level color / 风险等级颜色
 */
enum class RiskLevel(val label: String, val color: Color) {
    P0("P0 — Critical", Color(0xFFEF5350)),
    P1("P1 — High", Color(0xFFFFB74D)),
    P2("P2 — Medium", Color(0xFF4FC3F7))
}

// =============================================================
// ScanProgress — 扫描进度状态
// =============================================================
/**
 * Scan progress status / 扫描进度状态
 *
 * @param label Status label / 状态标签
 */
enum class ScanProgress(val label: String) {
    IDLE("Ready / 待命"),
    SCANNING("Scanning... / 扫描中"),
    COMPLETED("Completed / 完成"),
    FAILED("Failed / 失败")
}

// =============================================================
// ScanResult — 单条扫描结果
// =============================================================
/**
 * Single scan result item / 单条扫描结果
 *
 * @param id Unique result ID / 唯一结果 ID
 * @param file File path relative to project / 文件路径（相对项目）
 * @param line Code line number / 代码行号
 * @param method Method or API call / 方法或 API 调用
 * @param accessType Access type / 访问类型
 * @param riskLevel Risk level / 风险等级
 * @param snippet Code snippet (3 lines) / 代码片段（3 行）
 * @param suggestion Fix suggestion / 修复建议
 * @param isCompliant Whether this access is compliant / 是否已合规
 */
data class ScanResult(
    val id: String,
    val file: String,
    val line: Int,
    val method: String,
    val accessType: String,
    val riskLevel: RiskLevel,
    val snippet: String,
    val suggestion: String,
    val isCompliant: Boolean
)

// =============================================================
// TemplateType — 代码模板类型
// =============================================================
/**
 * Code template type / 代码模板类型
 *
 * @param label Template display name / 模板显示名
 */
enum class TemplateType(val label: String) {
    KOTLIN("Kotlin"),
    XML("XML / Manifest"),
    MANIFEST("AndroidManifest.xml")
}

// =============================================================
// CiConfig — CI 配置数据类
// =============================================================
/**
 * CI compliance check configuration / CI 合规检测配置
 *
 * @param projectPath Project path to scan / 要扫描的项目路径
 * @param minSdk Minimum SDK version / 最低 SDK 版本
 * @param targetSdk Target SDK version / 目标 SDK 版本
 * @param checkLegacy Whether to check legacy compatibility / 是否检查 Legacy 兼容性
 * @param reportFormat Report output format / 报告输出格式
 */
data class CiConfig(
    val projectPath: String = "",
    val minSdk: Int = 24,
    val targetSdk: Int = 37,
    val checkLegacy: Boolean = true,
    val reportFormat: String = "JSON"
)

// =============================================================
// CodeTemplate — 代码模板数据类
// =============================================================
/**
 * Code template content / 代码模板内容
 *
 * @param templateType Template type / 模板类型
 * @param title Template title / 模板标题
 * @param description Template description / 模板描述
 * @param code Template code content / 模板代码内容
 * @param parameters Template parameters that need filling / 需要填写的模板参数
 */
data class CodeTemplate(
    val templateType: TemplateType,
    val title: String,
    val description: String,
    val code: String,
    val parameters: List<String> = emptyList()
)

// =============================================================
// DecisionNode — 决策树节点
// =============================================================
/**
 * Decision tree node for permission path selection / 权限路径选择决策树节点
 *
 * @param question Question text / 问题文本
 * @param options Available options / 可选答案
 * @param recommendation Recommended path / 推荐路径
 */
data class DecisionNode(
    val question: String,
    val options: List<String>,
    val recommendation: String
)

// =============================================================
// LanPermissionState — 页面状态（MVI State）
// =============================================================
/**
 * LanPermission page state (MVI State) / 页面状态（MVI State）
 *
 * Single source of truth for the entire LAN Permission Dashboard.
 *
 * @param selectedModule Currently selected module / 当前选中的模块
 * @param scanProgress Scan progress status / 扫描进度状态
 * @param scanResults Scan results list / 扫描结果列表
 * @param riskFilter Current risk level filter / 当前风险等级筛选
 * @param selectedTemplate Currently selected code template / 当前选中的代码模板
 * @param ciConfig CI configuration / CI 配置
 * @param codeTemplates All available code templates / 所有可用代码模板
 * @param isLoading Whether an operation is in progress / 是否有操作进行中
 * @param scanLogs Real-time scan logs / 实时扫描日志
 * @param error Error message if any / 错误信息
 */
data class LanPermissionState(
    val selectedModule: ModuleType? = null,
    val scanProgress: ScanProgress = ScanProgress.IDLE,
    val scanResults: List<ScanResult> = emptyList(),
    val riskFilter: RiskLevel? = null,
    val selectedTemplate: TemplateType = TemplateType.KOTLIN,
    val ciConfig: CiConfig = CiConfig(),
    val codeTemplates: Map<ModuleType, List<CodeTemplate>> = emptyMap(),
    val isLoading: Boolean = false,
    val scanLogs: List<String> = emptyList(),
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = LanPermissionState()
    }

    /** Filtered scan results based on risk level / 按风险等级筛选后的扫描结果 */
    val filteredResults: List<ScanResult>
        get() = when (riskFilter) {
            null -> scanResults
            else -> scanResults.filter { it.riskLevel == riskFilter }
        }

    /** Whether scanner has results / 扫描器是否有结果 */
    val hasResults: Boolean
        get() = scanResults.isNotEmpty()

    /** Summary: P0 count / 汇总：P0 数量 */
    val p0Count: Int
        get() = scanResults.count { it.riskLevel == RiskLevel.P0 }

    /** Summary: P1 count / 汇总：P1 数量 */
    val p1Count: Int
        get() = scanResults.count { it.riskLevel == RiskLevel.P1 }

    /** Summary: P2 count / 汇总：P2 数量 */
    val p2Count: Int
        get() = scanResults.count { it.riskLevel == RiskLevel.P2 }

    /** Summary: compliant count / 汇总：已合规数量 */
    val compliantCount: Int
        get() = scanResults.count { it.isCompliant }
}

// =============================================================
// LanPermissionIntent — 用户意图（MVI Intent）
// =============================================================
/**
 * LanPermission user intentions (MVI Intent) / 用户意图（MVI Intent）
 *
 * All user actions correspond to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface LanPermissionIntent {
    /** Navigate to module / 导航到指定模块 */
    data class SelectModule(val module: ModuleType) : LanPermissionIntent

    /** Navigate back to dashboard / 返回仪表盘 */
    data object NavigateBack : LanPermissionIntent

    /** Start project scan / 开始项目扫描 */
    data object StartScan : LanPermissionIntent

    /** Cancel ongoing scan / 取消正在进行的扫描 */
    data object CancelScan : LanPermissionIntent

    /** Filter scan results by risk level / 按风险等级筛选扫描结果 */
    data class FilterByRisk(val risk: RiskLevel?) : LanPermissionIntent

    /** Select code template tab / 选择代码模板标签页 */
    data class SelectTemplate(val template: TemplateType) : LanPermissionIntent

    /** Update CI configuration / 更新 CI 配置 */
    data class UpdateCiConfig(val config: CiConfig) : LanPermissionIntent

    /** Copy code to clipboard / 复制代码到剪贴板 */
    data class CopyCode(val code: String) : LanPermissionIntent

    /** Run CI compliance check / 运行 CI 合规检测 */
    data object RunCiCheck : LanPermissionIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : LanPermissionIntent

    /** Clear scan results / 清除扫描结果 */
    data object ClearResults : LanPermissionIntent
}

// =============================================================
// LanPermissionEffect — 副作用（MVI Effect）
// =============================================================
/**
 * LanPermission side effects (MVI Effect) / 副作用（MVI Effect）
 *
 * One-time side effects, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface LanPermissionEffect {
    /** Show toast message / 显示 Toast */
    data class ShowToast(val message: String) : LanPermissionEffect

    /** Code copied to clipboard / 代码已复制到剪贴板 */
    data object CodeCopied : LanPermissionEffect

    /** Navigate to source code location / 导航到源代码位置 */
    data class NavigateToCode(val filePath: String, val line: Int) : LanPermissionEffect

    /** Show error message / 显示错误信息 */
    data class ShowError(val message: String) : LanPermissionEffect

    /** CI check completed / CI 检测完成 */
    data class CiCheckCompleted(val report: String) : LanPermissionEffect

    /** Scan completed / 扫描完成 */
    data object ScanCompleted : LanPermissionEffect
}
