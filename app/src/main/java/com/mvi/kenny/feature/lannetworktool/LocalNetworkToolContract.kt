package com.mvi.kenny.feature.lannetworktool

import androidx.compose.ui.graphics.Color

// =============================================================
// LocalNetworkToolContract — Android 17 ACCESS_LOCAL_NETWORK
// 权限合规检测与迁移工具包 MVI 契约
// =============================================================
// PRD-159: Android 17 ACCESS_LOCAL_NETWORK 权限合规检测与迁移工具包
//
// Architecture Pattern: MVI (Model-View-Intent)
// —————————————————————————————————————————————————————————
// • Model (State): Immutable data class — UI 的单一数据源
// • View: 消费 State 并渲染 UI 的 Composable 函数
// • Intent: 用户意图，ViewModel 处理并更新 State
// • Effect: 一次性副作用（导航、Toast 等）
//
// Android 17 引入 ACCESS_LOCAL_NETWORK 运行时权限：
// — 属于 NEARBY_DEVICES 权限组
// — targetSDK 37+ 访问局域网（RFC1918 私有地址段）必须显式声明
// — 权限被撤销后局域网访问静默失败，不抛异常
//
// @see LocalNetworkToolViewModel 状态管理
// @see LocalNetworkToolScreen 主屏幕
// =============================================================

// =============================================================
// AccessType — 局域网访问类型
// =============================================================
/**
 * LAN access type / 局域网访问类型
 *
 * @param label Display label / 显示标签
 * @param description Description of this access type / 此访问类型的描述
 * @param exampleMethods Example method names / 示例方法名
 */
enum class AccessType(
    val label: String,
    val description: String,
    val exampleMethods: List<String>
) {
    /**
     * Direct socket connection / 直连 Socket 连接
     * e.g., Socket(), ServerSocket, DatagramSocket
     */
    SOCKET("Socket", "Direct socket connection / 直连 Socket", listOf("Socket()", "ServerSocket", "DatagramSocket")),

    /**
     * HttpURLConnection based access / 基于 HttpURLConnection 的访问
     * e.g., HttpURLConnection, HttpsURLConnection
     */
    HTTP_URL_CONNECTION("HttpURLConnection", "HttpURLConnection based / 基于 HttpURLConnection", listOf("HttpURLConnection", "HttpsURLConnection", "URL.openConnection()")),

    /**
     * OkHttp client access / OkHttp 客户端访问
     * e.g., OkHttpClient.newCall(), interceptors
     */
    OKHTTP("OkHttp", "OkHttp client / OkHttp 客户端", listOf("OkHttpClient", "newCall()", "addInterceptor()")),

    /**
     * Retrofit service access / Retrofit 服务访问
     * e.g., Retrofit.create(), @GET/@POST annotations
     */
    RETROFIT("Retrofit", "Retrofit service / Retrofit 服务", listOf("Retrofit.create()", "@GET", "@POST")),

    /**
     * InetAddress lookup / InetAddress 查询
     * e.g., InetAddress.getByName(), Inet4Address.getByName()
     */
    INET_ADDRESS("InetAddress", "IP address lookup / IP 地址查询", listOf("InetAddress.getByName()", "InetAddress.getAllByName()"))
}

// =============================================================
// IpRange — 私有 IP 地址段（RFC1918）
// =============================================================
/**
 * RFC1918 private IP ranges / RFC1918 私有 IP 地址段
 *
 * These ranges require ACCESS_LOCAL_NETWORK permission on Android 17 (targetSDK 37+).
 * 10.0.0.0/8      — Class A private / A 类私有
 * 172.16.0.0/12   — Class B private / B 类私有
 * 192.168.0.0/16  — Class C private / C 类私有
 * 169.254.0.0/16  — Link-local / 链路本地
 * 224.0.0.0/4     — Multicast / 多播
 */
enum class IpRange(
    val cidr: String,
    val description: String,
    val examples: List<String>
) {
    CLASS_A("10.0.0.0/8", "Class A private / A 类私有", listOf("10.0.0.1", "10.255.255.255")),
    CLASS_B("172.16.0.0/12", "Class B private / B 类私有", listOf("172.16.0.1", "172.31.255.255")),
    CLASS_C("192.168.0.0/16", "Class C private / C 类私有", listOf("192.168.0.1", "192.168.255.255")),
    LINK_LOCAL("169.254.0.0/16", "Link-local / 链路本地", listOf("169.254.0.1")),
    MULTICAST("224.0.0.0/4", "Multicast / 多播", listOf("224.0.0.1")),
    ALL_PRIVATE("All RFC1918", "All private ranges / 所有私有地址", listOf("10.x.x.x", "172.16.x.x", "192.168.x.x"))
}

// =============================================================
// ScanStep — 扫描步骤
// =============================================================
/**
 * Scan step in the guided scanner flow / 引导式扫描流程中的扫描步骤
 */
enum class ScanStep(val stepNumber: Int, val title: String) {
    PROJECT_SELECTION(1, "Project Selection / 项目选择"),
    SCANNING(2, "Scanning / 扫描中"),
    RESULTS(3, "Results / 扫描结果"),
    COMPLIANCE(4, "Compliance / 合规评估"),
    MIGRATION(5, "Migration / 迁移方案")
}

// =============================================================
// PermissionStatus — 权限声明状态
// =============================================================
/**
 * Permission declaration status / 权限声明状态
 *
 * @param label Display label / 显示标签
 * @param color Status color / 状态颜色
 * @param description Description / 描述
 */
enum class PermissionStatus(
    val label: String,
    val color: Color,
    val description: String
) {
    DECLARED("Declared / 已声明", Color(0xFF4CAF50), "Permission is declared in AndroidManifest.xml / 权限已在 AndroidManifest.xml 中声明"),
    MISSING("Missing / 缺失", Color(0xFFF44336), "Permission is NOT declared — non-compliant on targetSDK 37+ / 权限未声明 — targetSDK 37+ 不合规"),
    UNCERTAIN("Uncertain / 不确定", Color(0xFFFF9800), "Cannot determine — needs manual review / 无法确定 — 需人工审查"),
    NOT_NEEDED("Not Needed / 不需要", Color(0xFF9E9E9E), "Access does not require ACCESS_LOCAL_NETWORK / 此访问不需要 ACCESS_LOCAL_NETWORK")
}

// =============================================================
// LanAccessPath — 检测到的局域网访问路径
// =============================================================
/**
 * Detected LAN access path / 检测到的局域网访问路径
 *
 * Represents a single code location where the app accesses the local network.
 * 表示应用访问局域网的单个代码位置。
 *
 * @param id Unique identifier / 唯一标识符
 * @param file Source file path / 源文件路径
 * @param line Line number in file / 文件中的行号
 * @param method Method or API called / 被调用的方法或 API
 * @param targetIpRange Target IP range accessed / 访问的目标 IP 地址段
 * @param accessType Type of LAN access / 局域网访问类型
 * @param permissionStatus Current permission declaration status / 当前权限声明状态
 * @param suggestedPermission Suggested permission to add / 建议添加的权限
 * @param codeSnippet The relevant code snippet / 相关代码片段
 */
data class LanAccessPath(
    val id: String,
    val file: String,
    val line: Int,
    val method: String,
    val targetIpRange: String,
    val accessType: AccessType,
    val permissionStatus: PermissionStatus,
    val suggestedPermission: String = "android.permission.ACCESS_LOCAL_NETWORK",
    val codeSnippet: String = ""
)

// =============================================================
// ManifestDiff — AndroidManifest.xml 变更
// =============================================================
/**
 * AndroidManifest.xml modification diff / AndroidManifest.xml 修改差异
 *
 * @param original Original manifest content / 原始清单内容
 * @param modified Modified manifest content / 修改后的清单内容
 * @param addedPermissions Permissions added / 添加的权限
 * @param conflicts Existing conflicts / 现有冲突
 */
data class ManifestDiff(
    val original: String,
    val modified: String,
    val addedPermissions: List<String>,
    val conflicts: List<String> = emptyList()
)

// =============================================================
// ComplianceReport — 合规报告
// =============================================================
/**
 * LAN permission compliance report / 局域网权限合规报告
 *
 * @param totalPaths Total LAN access paths found / 发现的总访问路径数
 * @param compliantPaths Paths with proper permission / 有正确权限的路径数
 * @param nonCompliantPaths Paths missing permission / 缺失权限的路径数
 * @param uncertainPaths Paths requiring manual review / 需人工审查的路径数
 * @param accessPaths Detailed access path list / 详细访问路径列表
 * @param targetSdk Target SDK version / 目标 SDK 版本
 * @param requiresPermission Whether ACCESS_LOCAL_NETWORK is required / 是否需要 ACCESS_LOCAL_NETWORK
 * @param generatedAt Report generation timestamp / 报告生成时间戳
 */
data class ComplianceReport(
    val totalPaths: Int,
    val compliantPaths: Int,
    val nonCompliantPaths: Int,
    val uncertainPaths: Int,
    val accessPaths: List<LanAccessPath>,
    val targetSdk: Int,
    val requiresPermission: Boolean,
    val generatedAt: Long = System.currentTimeMillis()
) {
    /** Compliance score 0-100 / 合规评分 0-100 */
    val complianceScore: Int
        get() = if (totalPaths == 0) 100 else ((compliantPaths.toFloat() / totalPaths) * 100).toInt()

    /** Overall compliance level / 总体合规等级 */
    val complianceLevel: ComplianceLevel
        get() = when {
            complianceScore >= 90 -> ComplianceLevel.FULL
            complianceScore >= 60 -> ComplianceLevel.PARTIAL
            else -> ComplianceLevel.NON_COMPLIANT
        }

    /** Summary text / 摘要文本 */
    val summaryText: String
        get() = when (complianceLevel) {
            ComplianceLevel.FULL -> "✅ Fully Compliant / 完全合规 — ACCESS_LOCAL_NETWORK 权限声明完整"
            ComplianceLevel.PARTIAL -> "⚠️ Partially Compliant / 部分合规 — 建议补充缺失的权限声明"
            ComplianceLevel.NON_COMPLIANT -> "🚨 Non-Compliant / 不合规 — targetSDK 37+ 必须声明 ACCESS_LOCAL_NETWORK"
        }
}

// =============================================================
// ComplianceLevel — 合规等级
// =============================================================
/**
 * Compliance level / 合规等级
 *
 * @param label Display label / 显示标签
 * @param color Level color / 等级颜色
 * @param description Description / 描述
 */
enum class ComplianceLevel(
    val label: String,
    val color: Color
) {
    FULL("Fully Compliant / 完全合规", Color(0xFF4CAF50)),
    PARTIAL("Partially Compliant / 部分合规", Color(0xFFFF9800)),
    NON_COMPLIANT("Non-Compliant / 不合规", Color(0xFFF44336))
}

// =============================================================
// ScanLogEntry — 扫描日志条目
// =============================================================
/**
 * Scan log entry / 扫描日志条目
 *
 * @param timestamp Log timestamp / 日志时间戳
 * @param level Log level (info/warning/error/success) / 日志级别
 * @param message Log message / 日志消息
 * @param details Additional details / 附加详情
 */
data class ScanLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val message: String,
    val details: String = ""
)

/**
 * Log level / 日志级别
 */
enum class LogLevel(val color: Color, val prefix: String) {
    INFO(Color(0xFF2196F3), "ℹ️"),
    WARNING(Color(0xFFFF9800), "⚠️"),
    ERROR(Color(0xFFF44336), "❌"),
    SUCCESS(Color(0xFF4CAF50), "✅"),
    SCANNING(Color(0xFF9E9E9E), "🔍")
}

// =============================================================
// ReportFormat — 报告导出格式
// =============================================================
/**
 * Compliance report export format / 合规报告导出格式
 */
enum class ReportFormat(val label: String, val extension: String) {
    JSON("JSON", "json"),
    HTML("HTML", "html"),
    MARKDOWN("Markdown", "md")
}

// =============================================================
// PermissionTemplate — 权限请求代码模板
// =============================================================
/**
 * Runtime permission request code template / 运行时权限请求代码模板
 *
 * @param templateName Template display name / 模板显示名称
 * @param templateCode Template code content / 模板代码内容
 * @param description Template description / 模板描述
 * @param language Template language / 模板语言
 */
data class PermissionTemplate(
    val templateName: String,
    val templateCode: String,
    val description: String,
    val language: String = "Kotlin"
)

// =============================================================
// LocalNetworkToolState — 页面状态
// =============================================================
/**
 * Local Network Tool State / 局域网权限工具页面状态
 *
 * Single source of truth for the entire Local Network Tool UI.
 * 整个局域网权限工具 UI 的单一数据源。
 *
 * @param activeTab Currently active main tab / 当前活跃的主 Tab
 * @param scanStep Current scanner guided step / 当前扫描引导步骤
 * @param isLoading Whether any background operation is in progress / 是否有任何后台操作进行中
 * @param error Error message if any / 错误信息
 *
 * // Scanner state
 * @param projectPath Selected project path / 选中的项目路径
 * @param scanLogs Scan operation logs / 扫描操作日志
 * @param isScanning Whether scan is in progress / 扫描是否进行中
 * @param scanProgress Scan progress 0.0~1.0 / 扫描进度
 * @param accessPaths Detected LAN access paths / 检测到的局域网访问路径
 *
 * // Report state
 * @param complianceReport Generated compliance report / 生成的合规报告
 * @param selectedAccessPath Selected access path for detail view / 选中查看详情的访问路径
 *
 * // Migration state
 * @param manifestDiff Generated manifest diff / 生成的清单差异
 * @param selectedTemplate Selected permission request template / 选中的权限请求模板
 * @param generatedPermissionCode Generated permission request code / 生成的权限请求代码
 *
 * // Settings state
 * @param selectedFormats Selected report export formats / 选中的报告导出格式
 * @param targetSdkOverride Target SDK override for simulation / 模拟用的目标 SDK 覆盖
 */
data class LocalNetworkToolState(
    // General
    val activeTab: MainTab = MainTab.DASHBOARD,
    val scanStep: ScanStep = ScanStep.PROJECT_SELECTION,
    val isLoading: Boolean = false,
    val error: String? = null,

    // Scanner
    val projectPath: String = "",
    val scanLogs: List<ScanLogEntry> = emptyList(),
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val accessPaths: List<LanAccessPath> = emptyList(),

    // Report
    val complianceReport: ComplianceReport? = null,
    val selectedAccessPath: LanAccessPath? = null,

    // Migration
    val manifestDiff: ManifestDiff? = null,
    val selectedTemplate: PermissionTemplate? = null,
    val generatedPermissionCode: String = "",

    // Settings
    val selectedFormats: List<ReportFormat> = listOf(ReportFormat.JSON),
    val targetSdkOverride: Int? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = LocalNetworkToolState()
    }

    /** Non-compliant paths count / 不合规路径数量 */
    val nonCompliantCount: Int
        get() = accessPaths.count { it.permissionStatus == PermissionStatus.MISSING }

    /** Compliant paths count / 合规路径数量 */
    val compliantCount: Int
        get() = accessPaths.count { it.permissionStatus == PermissionStatus.DECLARED }

    /** Scanner progress text / 扫描进度文本 */
    val scanProgressText: String
        get() = "${(scanProgress * 100).toInt()}%"

    /** Whether scan is complete / 扫描是否完成 */
    val isScanComplete: Boolean
        get() = scanProgress >= 1f && !isScanning
}

// =============================================================
// MainTab — 主 Tab
// =============================================================
/**
 * Main tab in Local Network Tool / 局域网工具主 Tab
 */
enum class MainTab(val title: String, val iconName: String) {
    DASHBOARD("Dashboard / 总览", "dashboard"),
    SCANNER("Scanner / 扫描器", "search"),
    REPORT("Report / 报告", "description"),
    MIGRATION("Migration / 迁移", "build")
}

// =============================================================
// LocalNetworkToolIntent — 用户意图
// =============================================================
/**
 * Local Network Tool User Intents / 局域网权限工具用户意图
 *
 * Every user action corresponds to an Intent.
 * 每个用户操作对应一个 Intent。
 */
sealed interface LocalNetworkToolIntent {
    // General
    /** Select a main tab / 选择主 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: MainTab) : LocalNetworkToolIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : LocalNetworkToolIntent

    // Scanner
    /** Set project path / 设置项目路径
     * @param path Project directory path / 项目目录路径
     */
    data class SetProjectPath(val path: String) : LocalNetworkToolIntent

    /** Start LAN permission scan / 开始局域网权限扫描 */
    data object StartScan : LocalNetworkToolIntent

    /** Cancel ongoing scan / 取消正在进行的扫描 */
    data object CancelScan : LocalNetworkToolIntent

    /** Move to next scan step / 移动到下一个扫描步骤
     * @param step Next step / 下一步骤
     */
    data class SetScanStep(val step: ScanStep) : LocalNetworkToolIntent

    /** Select an access path for detail view / 选择访问路径查看详情
     * @param path Access path to select / 要选择的访问路径
     */
    data class SelectAccessPath(val path: LanAccessPath) : LocalNetworkToolIntent

    /** Clear scan results / 清除扫描结果 */
    data object ClearResults : LocalNetworkToolIntent

    // Report
    /** Generate compliance report / 生成合规报告 */
    data object GenerateReport : LocalNetworkToolIntent

    /** Export compliance report / 导出合规报告
     * @param format Report format to export / 要导出的报告格式
     */
    data class ExportReport(val format: ReportFormat) : LocalNetworkToolIntent

    // Migration
    /** Generate manifest diff / 生成清单差异 */
    data object GenerateManifestDiff : LocalNetworkToolIntent

    /** Apply manifest diff to project / 将清单差异应用到项目
     * @param applyToProject Whether to apply to project or just preview / 是否应用到项目或仅预览
     */
    data class ApplyManifestDiff(val applyToProject: Boolean) : LocalNetworkToolIntent

    /** Select permission request template / 选择权限请求模板
     * @param template Template to select / 要选择的模板
     */
    data class SelectTemplate(val template: PermissionTemplate) : LocalNetworkToolIntent

    /** Generate permission request code / 生成权限请求代码 */
    data object GeneratePermissionCode : LocalNetworkToolIntent

    /** Copy permission code to clipboard / 复制权限代码到剪贴板 */
    data object CopyPermissionCode : LocalNetworkToolIntent

    // Settings
    /** Toggle report format selection / 切换报告格式选择
     * @param format Format to toggle / 要切换的格式
     */
    data class ToggleReportFormat(val format: ReportFormat) : LocalNetworkToolIntent

    /** Set target SDK override / 设置目标 SDK 覆盖
     * @param sdk SDK version to simulate / 要模拟的 SDK 版本
     */
    data class SetTargetSdkOverride(val sdk: Int?) : LocalNetworkToolIntent
}

// =============================================================
// LocalNetworkToolEffect — 副作用
// =============================================================
/**
 * Local Network Tool Side Effects / 局域网权限工具副作用
 *
 * One-time events, consumed only once by UI layer.
 * UI 层消费的一次性事件。
 */
sealed interface LocalNetworkToolEffect {
    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error message / 是否为错误消息
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : LocalNetworkToolEffect

    /** Report exported successfully / 报告导出成功
     * @param path Exported file path / 导出文件路径
     * @param format Export format / 导出格式
     */
    data class ReportExported(val path: String, val format: ReportFormat) : LocalNetworkToolEffect

    /** Manifest diff applied / 清单差异已应用
     * @param path Path to modified manifest / 修改后的清单路径
     */
    data class ManifestDiffApplied(val path: String) : LocalNetworkToolEffect

    /** Code copied to clipboard / 代码已复制到剪贴板
     * @param code Copied code / 已复制的代码
     */
    data class CodeCopied(val code: String) : LocalNetworkToolEffect

    /** Scan completed / 扫描完成
     * @param totalPaths Total paths found / 发现的路径总数
     * @param nonCompliant Non-compliant paths / 不合规路径数
     */
    data class ScanCompleted(val totalPaths: Int, val nonCompliant: Int) : LocalNetworkToolEffect
}
