package com.mvi.kenny.feature.lannetwork

// ================================================================
// LanNetworkContract — Android 17 ACCESS_LOCAL_NETWORK MVI 契约
// ================================================================
// MVI architecture contract for Android 17 ACCESS_LOCAL_NETWORK permission
// compliance detection & migration toolkit.
//
// PRD-159: Android 17 ACCESS_LOCAL_NETWORK 权限合规检测与迁移工具包
// Design Reference: memory/agency/designs/PRD-159-Android-17-ACCESS-LOCAL-NETWORK-权限合规检测工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

// =============================================================
// AccessType — 局域网访问类型枚举
// =============================================================
/**
 * LanAccessPath access type / 局域网访问路径类型
 *
 * @param displayName 显示名称（中英双语）
 */
enum class AccessType(val displayName: String) {
    /** 直接 Socket 连接 */
    SOCKET("Socket 连接"),
    /** HttpUrlConnection HTTP 请求 */
    HTTP_URL_CONNECTION("HttpUrlConnection"),
    /** OkHttp Client 请求 */
    OKHTTP("OkHttp"),
    /** Retrofit 请求 */
    RETROFIT("Retrofit"),
    /** InetAddress 主机名解析 */
    INET_ADDRESS("InetAddress")
}

// =============================================================
// IpRange — 常见私有地址段
// =============================================================
/**
 * Common RFC 1918 private IP ranges / 常见私有地址段
 *
 * @param label 地址段显示名
 * @param range CIDR notation / 网段 CIDR 表示
 * @param description 用途说明
 */
enum class IpRange(val label: String, val range: String, val description: String) {
    /** 10.0.0.0/8 — 大型内网 */
    CLASS_A("Class A", "10.0.0.0/8", "大型内网，企业网络"),
    /** 172.16.0.0/12 — 中型内网 */
    CLASS_B("Class B", "172.16.0.0/12", "中型内网，ISP 分配"),
    /** 192.168.0.0/16 — 家庭/小型网络 */
    CLASS_C("Class C", "192.168.0.0/16", "家庭/小型网络，最常见"),
    /** 127.0.0.0/8 — 本地回环（不算局域网） */
    LOOPBACK("Loopback", "127.0.0.0/8", "本地回环地址");
}

// =============================================================
// ComplianceLevel — 合规等级
// =============================================================
/**
 * Permission compliance level / 权限合规等级
 *
 * @param emoji Emoji representation
 * @param labelZh 中文标签
 * @param color 合规颜色
 */
enum class ComplianceLevel(
    val emoji: String,
    val labelZh: String,
    val labelEn: String,
    val color: Color
) {
    /** 合规 — 有声明且请求了权限 */
    COMPLIANT("✅", "合规", "Compliant", Color(0xFF4CAF50)),
    /** 警告 — targetSDK < 37，无需声明但建议准备 */
    NOT_REQUIRED("🟡", "暂不需要", "Not Required", Color(0xFFFF9800)),
    /** 不合规 — targetSDK >= 37 但缺少权限声明 */
    NON_COMPLIANT("🚫", "不合规", "Non-Compliant", Color(0xFFE53935)),
    /** 未知 — 无法确定权限状态 */
    UNKNOWN("❓", "未知", "Unknown", Color(0xFF9E9E9E))
}

// =============================================================
// ScanStatus — 扫描状态
// =============================================================
/**
 * Project scan status / 项目扫描状态
 */
enum class ScanStatus(val label: String) {
    /** 初始空闲 */
    IDLE("待命"),
    /** 正在扫描 */
    SCANNING("扫描中"),
    /** 扫描完成 */
    DONE("完成"),
    /** 扫描出错 */
    ERROR("错误")
}

// =============================================================
// GenerateStatus — Manifest 生成状态
// =============================================================
/**
 * Manifest generation status / Manifest 生成状态
 */
enum class GenerateStatus(val label: String) {
    /** 空闲 */
    IDLE("待命"),
    /** 准备就绪（已生成 Diff） */
    READY("就绪"),
    /** 已应用（Diff 已写入） */
    APPLIED("已应用")
}

// =============================================================
// ReportFormat — 报告导出格式
// =============================================================
/**
 * Compliance report export format / 合规报告导出格式
 */
enum class ReportFormat(val label: String, val extension: String) {
    JSON("JSON", "json"),
    MARKDOWN("Markdown", "md"),
    HTML("HTML", "html")
}

// =============================================================
// LanAccessPath — 局域网访问路径（核心数据结构）
// =============================================================
/**
 * 局域网访问路径 — 描述代码中一处局域网访问行为
 *
 * Represents a single local network access path found during AST scan.
 *
 * @param id 唯一路径 ID（UUID）
 * @param file 文件路径（相对路径）
 * @param line 代码行号
 * @param method 访问方法名（如 "InetAddress.getByName"）
 * @param targetIpRange 目标 IP 地址段（如 "192.168.0.0/16"）
 * @param accessType 访问类型
 * @param hasPermission 是否声明了权限（null 表示未知）
 * @param snippet 代码片段（前 3 行）
 * @param suggestion 修复建议文本
 */
data class LanAccessPath(
    val id: String,
    val file: String,
    val line: Int,
    val method: String,
    val targetIpRange: String,
    val accessType: AccessType,
    val hasPermission: Boolean?,
    val snippet: String,
    val suggestion: String
)

// =============================================================
// ManifestDiff — Manifest Diff 变更
// =============================================================
/**
 * AndroidManifest.xml Diff 变更项
 *
 * Represents a single permission change to be applied to AndroidManifest.xml.
 *
 * @param permission 权限名称（如 "android.permission.ACCESS_LOCAL_NETWORK"）
 * @param action Diff 操作类型（ADD / REMOVE）
 * @param reason 变更原因
 */
data class ManifestDiff(
    val permission: String,
    val action: DiffAction,
    val reason: String
) {
    enum class DiffAction { ADD, REMOVE }
}

// =============================================================
// ComplianceReport — 合规报告摘要
// =============================================================
/**
 * 合规报告摘要 — 扫描完成后汇总统计
 *
 * @param totalPaths 发现的局域网访问路径总数
 * @param compliantPaths 已合规路径数
 * @param nonCompliantPaths 不合规路径数
 * @param unknownPaths 未知状态路径数
 * @param targetSdk targetSDK 版本号
 * @param requiresPermission targetSDK >= 37 是否需要声明权限
 * @param missingPermissions 缺失的权限列表
 */
data class ComplianceReport(
    val totalPaths: Int,
    val compliantPaths: Int,
    val nonCompliantPaths: Int,
    val unknownPaths: Int,
    val targetSdk: Int,
    val requiresPermission: Boolean,
    val missingPermissions: List<String>
) {
    /** 合规率 */
    val complianceRate: Float
        get() = if (totalPaths == 0) 1f
                else compliantPaths.toFloat() / totalPaths

    /** 合规率百分比 */
    val compliancePercent: String
        get() = "${(complianceRate * 100).toInt()}%"
}

// =============================================================
// LanNetworkState — 页面状态
// =============================================================
/**
 * LanNetwork 页面状态（MVI State）
 *
 * Single source of truth for the entire LAN Network Permission UI.
 *
 * @param scanStatus 当前扫描状态
 * @param scanProgress 扫描进度 0.0~1.0
 * @param scannedFilesCount 已扫描文件数
 * @param totalFilesCount 待扫描文件总数
 * @param selectedProjectPath 已选项目路径
 * @param accessPaths 发现的局域网访问路径列表
 * @param selectedPathId 选中的路径 ID（查看详情）
 * @param report 合规报告（扫描完成后生成）
 * @param generateStatus Manifest 生成状态
 * @param pendingDiff 待应用的 Diff（生成后待确认）
 * @param reportFormat 报告导出格式
 * @param isExporting 是否正在导出报告
 * @param ciMode CI 模式（只输出报告，不弹 UI）
 * @param error 错误信息
 */
data class LanNetworkState(
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanProgress: Float = 0f,
    val scannedFilesCount: Int = 0,
    val totalFilesCount: Int = 0,
    val selectedProjectPath: String? = null,
    val accessPaths: List<LanAccessPath> = emptyList(),
    val selectedPathId: String? = null,
    val report: ComplianceReport? = null,
    val generateStatus: GenerateStatus = GenerateStatus.IDLE,
    val pendingDiff: List<ManifestDiff> = emptyList(),
    val reportFormat: ReportFormat = ReportFormat.MARKDOWN,
    val isExporting: Boolean = false,
    val ciMode: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** 初始状态 */
        val Initial = LanNetworkState()
    }

    /** 当前选中的路径（用于详情展示） */
    val selectedPath: LanAccessPath?
        get() = accessPaths.find { it.id == selectedPathId }

    /** 合规路径（权限已声明且 targetSDK >= 37） */
    val compliantPaths: List<LanAccessPath>
        get() = accessPaths.filter { it.hasPermission == true }

    /** 不合规路径（权限未声明且 targetSDK >= 37） */
    val nonCompliantPaths: List<LanAccessPath>
        get() = accessPaths.filter { it.hasPermission == false }

    /** 未知状态路径 */
    val unknownPaths: List<LanAccessPath>
        get() = accessPaths.filter { it.hasPermission == null }
}

// =============================================================
// LanNetworkIntent — 用户意图
// =============================================================
/**
 * LanNetwork 用户意图（MVI Intent）
 *
 * All user actions correspond to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface LanNetworkIntent {
    /** 选择项目文件夹 / Select project folder */
    data class SelectProject(val path: String) : LanNetworkIntent

    /** 开始项目扫描 / Start project scan */
    data object StartScan : LanNetworkIntent

    /** 取消正在进行的扫描 / Cancel ongoing scan */
    data object CancelScan : LanNetworkIntent

    /** 选择路径查看详情 / Select path to view detail */
    data class SelectPath(val pathId: String) : LanNetworkIntent

    /** 清除选中的路径 / Clear selected path */
    data object ClearSelectedPath : LanNetworkIntent

    /** 生成 Manifest Diff / Generate Manifest Diff */
    data object GenerateManifestDiff : LanNetworkIntent

    /** 应用 Manifest Diff / Apply Manifest Diff */
    data object ApplyManifestDiff : LanNetworkIntent

    /** 设置报告导出格式 / Set report export format */
    data class SetReportFormat(val format: ReportFormat) : LanNetworkIntent

    /** 导出合规报告 / Export compliance report */
    data object ExportReport : LanNetworkIntent

    /** 切换 CI 模式 / Toggle CI mode */
    data class SetCiMode(val enabled: Boolean) : LanNetworkIntent

    /** 运行 CI 合规检测（无 UI，纯报告输出） / Run CI compliance check */
    data object RunCiCheck : LanNetworkIntent

    /** 关闭错误信息 / Dismiss error */
    data object DismissError : LanNetworkIntent
}

// =============================================================
// LanNetworkEffect — 副作用
// =============================================================
/**
 * LanNetwork 副作用（MVI Effect）
 *
 * One-time side effects, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface LanNetworkEffect {
    /** 显示 Toast / Show toast message */
    data class ShowToast(val message: String) : LanNetworkEffect

    /** 显示错误 / Show error message */
    data class ShowError(val message: String) : LanNetworkEffect

    /** 分享导出文件 / Share exported file */
    data class ShareFile(val filePath: String) : LanNetworkEffect

    /** 扫描完成通知 / Scan complete notification */
    data object ScanComplete : LanNetworkEffect

    /** Manifest 应用成功 / Manifest diff applied successfully */
    data object DiffApplied : LanNetworkEffect

    /** 打开文件选择器 / Open file picker */
    data object OpenFilePicker : LanNetworkEffect

    /** 导航到路径详情 / Navigate to path detail */
    data class NavigateToDetail(val pathId: String) : LanNetworkEffect

    /** 从详情页返回 / Navigate back from detail */
    data object NavigateBack : LanNetworkEffect
}
