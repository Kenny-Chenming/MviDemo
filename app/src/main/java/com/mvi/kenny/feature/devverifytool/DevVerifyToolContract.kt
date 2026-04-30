package com.mvi.kenny.feature.devverifytool

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

/**
 * ============================================================
 * DevVerifyToolContract — PRD-209 Android 开发者验证合规工具包
 * ============================================================
 * MVI Architecture: State (UI data) + Intent (user actions) + Effect (one-time events)
 *
 * Design Doc: memory/agency/designs/PRD-209-Android-开发者验证合规工具包.md
 * Status: 设计完成，待移交开发 | Completed: 2026-04-30
 *
 * Bilingual comments: CN + EN
 */

// =============================================================
// VerificationTab — Tab 枚举
// =============================================================
/**
 * 5 功能 Tab / 5 functional tabs
 * @param title Tab display title / Tab 显示标题
 */
enum class VerificationTab(val title: String) {
    SCANNER("扫描器"),          // App verification status scanner / App 验证状态扫描器
    BATCH_REGISTER("批量注册"), // Batch registration CLI tool / 批量注册 CLI 工具
    KEY_MANAGEMENT("密钥管理"), // Signing key registration management / 签名密钥注册管理
    DEADLINES("截止日期"),      // Regional deadline countdown / 地域截止日期倒计时
    DASHBOARD("仪表盘")         // Team compliance dashboard / 团队合规状态仪表盘
}

// =============================================================
// Compliance Status — 合规状态
// =============================================================
/**
 * Per-app compliance status / 单 App 合规状态
 */
enum class AppComplianceStatus(val label: String, val labelZh: String, val color: Color) {
    VERIFIED("Verified", "已注册", Color(0xFF3FB950)),     // ✅ Compliant / 已合规
    NOT_VERIFIED("Not Verified", "未注册", Color(0xFFF85149)), // ❌ Non-compliant / 未合规
    EXPIRING_SOON("Expiring Soon", "即将过期", Color(0xFFD29922)), // ⚠️ Will expire / 即将过期
    UNKNOWN("Unknown", "未知", Color(0xFF8B949E))          // ❓ Status unknown / 状态未知
}

// =============================================================
// Region — 地区
// =============================================================
/**
 * Supported regions for developer verification deadline / 支持开发者验证截止日期的地区
 * @param displayName Region display name / 地区显示名
 * @param deadline Deadline date / 截止日期
 */
enum class DevRegion(val displayName: String, val displayNameZh: String, val deadline: LocalDate) {
    BRAZIL("Brazil", "巴西", LocalDate.of(2026, 9, 30)),
    INDONESIA("Indonesia", "印度尼西亚", LocalDate.of(2026, 9, 30)),
    SINGAPORE("Singapore", "新加坡", LocalDate.of(2026, 9, 30)),
    THAILAND("Thailand", "泰国", LocalDate.of(2026, 9, 30)),
    GLOBAL("Global", "全球", LocalDate.of(2027, 3, 31))
}

// =============================================================
// Key Type — 密钥类型
// =============================================================
/**
 * Signing key type / 签名密钥类型
 */
enum class KeyType(val label: String, val labelZh: String) {
    GOOGLE_MANAGED("Google Managed", "Google 托管"),
    SELF_SIGNED("Self-Signed", "自签名"),
    UPLOAD_KEY("Upload Key", "上传密钥")
}

// =============================================================
// App Compliance Item — App 合规项
// =============================================================
/**
 * Single app compliance result / 单个 App 合规扫描结果
 * @param id Unique ID / 唯一 ID
 * @param packageName App package name / App 包名
 * @param appName App display name / App 显示名称
 * @param status Compliance status / 合规状态
 * @param signingKeyType Key type used for signing / 签名密钥类型
 * @param keyFingerprint Key fingerprint / 密钥指纹
 * @param verificationDate Date of verification / 验证日期
 * @param regionCovered Regions where app is registered / App 注册覆盖地区
 */
data class AppComplianceItem(
    val id: String,
    val packageName: String,
    val appName: String,
    val status: AppComplianceStatus,
    val signingKeyType: KeyType? = null,
    val keyFingerprint: String? = null,
    val verificationDate: LocalDate? = null,
    val regionCovered: List<DevRegion> = emptyList()
)

// =============================================================
// Key Item — 密钥项
// =============================================================
/**
 * Signing key management item / 签名密钥管理项
 * @param id Unique ID / 唯一 ID
 * @param appName Associated app name / 关联 App 名称
 * @param packageName Associated package name / 关联包名
 * @param keyType Key type / 密钥类型
 * @param isRegistered Whether registered with Google / 是否已注册 Google
 * @param expiresAt Key expiration date / 密钥过期日期
 * @param isExpiringSoon Whether expiring within 30 days / 是否 30 天内过期
 */
data class KeyItem(
    val id: String,
    val appName: String,
    val packageName: String,
    val keyType: KeyType,
    val isRegistered: Boolean,
    val expiresAt: LocalDate? = null,
    val isExpiringSoon: Boolean = false
)

// =============================================================
// Region Deadline Item — 地区截止日期项
// =============================================================
/**
 * Regional deadline countdown item / 地域截止日期倒计时项
 * @param region Region / 地区
 * @param daysRemaining Days remaining / 剩余天数
 * @param isSelected Whether selected by user / 是否被用户选中
 * @param progress Progress 0.0~1.0 (for visual) / 进度（视觉用）
 */
data class RegionDeadlineItem(
    val region: DevRegion,
    val daysRemaining: Long,
    val isSelected: Boolean = false,
    val progress: Float = 0f
)

// =============================================================
// Operation Log — 操作日志
// =============================================================
/**
 * Operation log entry / 操作日志条目
 * @param id Unique ID / 唯一 ID
 * @param timestamp Operation timestamp / 操作时间戳
 * @param action Action description / 操作描述
 * @param target Target (app/package) / 操作目标
 * @param isSuccess Whether operation succeeded / 操作是否成功
 */
data class OperationLog(
    val id: String,
    val timestamp: Long,
    val action: String,
    val target: String,
    val isSuccess: Boolean
)

// =============================================================
// Dashboard Stats — 仪表盘统计
// =============================================================
/**
 * Dashboard statistics / 仪表盘统计数据
 * @param totalApps Total number of apps / App 总数
 * @param verifiedCount Verified apps count / 已注册数
 * @param notVerifiedCount Not verified apps count / 未注册数
 * @param expiringSoonCount Expiring soon count / 即将过期数
 * @param complianceRate Compliance rate 0.0~1.0 / 合规率
 */
data class DashboardStats(
    val totalApps: Int = 0,
    val verifiedCount: Int = 0,
    val notVerifiedCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val complianceRate: Float = 0f
)

// =============================================================
// DevVerifyToolState — 主状态
// =============================================================
/**
 * DevVerifyTool MVI State / PRD-209 主状态
 *
 * Single source of truth for the entire Dev Verification Tool UI.
 * 整个开发者验证工具 UI 的单一数据源。
 *
 * @param selectedTab Currently selected tab / 当前选中 Tab
 * @param scanResults List of scanned app compliance items / 扫描结果列表
 * @param isScanning Whether scan is in progress / 是否正在扫描
 * @param scanProgress Scan progress 0.0~1.0 / 扫描进度
 * @param csvPath Selected CSV path for batch register / 批量注册的 CSV 路径
 * @param batchRegisterOutput Terminal-style output text / 终端风格输出文本
 * @param isRegistering Whether batch registration is in progress / 是否正在批量注册
 * @param keyItems List of signing key items / 密钥管理列表
 * @param selectedRegions Set of selected regions for deadline view / 截止日期视图选中的地区
 * @param regionDeadlineItems List of region deadline items / 地区截止日期列表
 * @param dashboardStats Dashboard statistics / 仪表盘统计数据
 * @param operationLogs List of operation logs / 操作日志列表
 * @param errorMessage Error message if any / 错误信息
 */
data class DevVerifyToolState(
    val selectedTab: VerificationTab = VerificationTab.SCANNER,
    val scanResults: List<AppComplianceItem> = emptyList(),
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val csvPath: String? = null,
    val batchRegisterOutput: String = "",
    val isRegistering: Boolean = false,
    val keyItems: List<KeyItem> = emptyList(),
    val selectedRegions: Set<DevRegion> = DevRegion.entries.toSet(),
    val regionDeadlineItems: List<RegionDeadlineItem> = emptyList(),
    val dashboardStats: DashboardStats = DashboardStats(),
    val operationLogs: List<OperationLog> = emptyList(),
    val errorMessage: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = DevVerifyToolState()
    }
}

// =============================================================
// DevVerifyToolIntent — 用户意图
// =============================================================
/**
 * DevVerifyTool MVI Intent / PRD-209 用户意图
 *
 * Every user action in the UI corresponds to an Intent.
 * 每一个 UI 中的用户操作都对应一个 Intent。
 */
sealed interface DevVerifyToolIntent {
    /** Select a tab / 选中 Tab
     * @param tab Tab to select / 要选中的 Tab
     */
    data class SelectTab(val tab: VerificationTab) : DevVerifyToolIntent

    /** Start app compliance scan / 开始 App 合规扫描 */
    data object StartScan : DevVerifyToolIntent

    /** Cancel ongoing scan / 取消正在进行的扫描 */
    data object CancelScan : DevVerifyToolIntent

    /** Import CSV file for batch registration / 导入 CSV 文件进行批量注册
     * @param path CSV file path / CSV 文件路径
     */
    data class ImportCsv(val path: String) : DevVerifyToolIntent

    /** Execute batch registration / 执行批量注册 */
    data object ExecuteBatchRegister : DevVerifyToolIntent

    /** Toggle region selection for deadline view / 切换截止日期视图的地区选择
     * @param region Region to toggle / 要切换的地区
     */
    data class ToggleRegion(val region: DevRegion) : DevVerifyToolIntent

    /** Refresh dashboard statistics / 刷新仪表盘统计数据 */
    data object RefreshDashboard : DevVerifyToolIntent

    /** Clear error message / 清除错误信息 */
    data object DismissError : DevVerifyToolIntent

    /** Select app to view detail / 选择查看详情的 App
     * @param packageName Package name / 包名
     */
    data class SelectApp(val packageName: String) : DevVerifyToolIntent

    /** Export compliance report / 导出合规报告
     * @param format Export format (JSON/MARKDOWN) / 导出格式
     */
    data class ExportReport(val format: String) : DevVerifyToolIntent
}

// =============================================================
// DevVerifyToolEffect — 副作用
// =============================================================
/**
 * DevVerifyTool MVI Effect / PRD-209 一次性副作用
 *
 * One-time side effects, delivered via Channel.
 * 一次性副作用，通过 Channel 投递。
 */
sealed interface DevVerifyToolEffect {
    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message text / 消息文本
     */
    data class ShowSnackbar(val message: String) : DevVerifyToolEffect

    /** Show error message / 显示错误消息
     * @param message Error description / 错误描述
     */
    data class ShowError(val message: String) : DevVerifyToolEffect

    /** Registration complete notification / 注册完成通知 */
    data object RegistrationComplete : DevVerifyToolEffect

    /** Export report ready / 报告导出就绪
     * @param json Report content / 报告内容
     */
    data class ExportReportReady(val json: String) : DevVerifyToolEffect

    /** Open file picker for CSV / 打开 CSV 文件选择器 */
    data object OpenCsvPicker : DevVerifyToolEffect
}
