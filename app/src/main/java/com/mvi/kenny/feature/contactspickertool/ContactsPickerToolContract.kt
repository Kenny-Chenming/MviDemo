package com.mvi.kenny.feature.contactspickertool

import android.net.Uri
import androidx.compose.ui.graphics.Color

// =============================================================
// ContactsPickerToolContract — Android 17 Contacts Picker API
// 迁移选择器与隐私合规工具包 MVI 契约
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class, single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, etc.)
//
// @see ContactsPickerToolViewModel State management
// @see ContactsPickerToolScreen Main screen

// =============================================================
// ScanStatus — 扫描状态枚举
// =============================================================
/**
 * Scan status / 扫描状态
 *
 * Represents the current state of the READ_CONTACTS permission scan.
 *
 * @param label Display label / 显示标签
 */
enum class ScanStatus(val label: String) {
    IDLE("Idle / 空闲"),
    SCANNING("Scanning / 扫描中"),
    COMPLETED("Completed / 完成"),
    ERROR("Error / 错误")
}

// =============================================================
// ToolTab — 功能 Tab 枚举
// =============================================================
/**
 * Tool module tab / 功能模块 Tab
 *
 * @param title Display title / 显示标题
 */
enum class ToolTab(val title: String) {
    SCANNER("READ_CONTACTS 扫描器"),
    LIBRARY("Picker 封装库"),
    REPORT("隐私合规报告"),
    TEMPLATES("集成模板"),
    COMPATIBILITY("跨版本兼容")
}

// =============================================================
// Migrability — 可迁移性评级
// =============================================================
/**
 * READ_CONTACTS call site migrability rating / 调用点可迁移性评级
 *
 * @param label Display label / 显示标签
 * @param priority Priority number for sorting / 排序优先级
 * @param color Badge color / 徽章颜色
 */
enum class Migrability(val label: String, val priority: Int, val color: Color) {
    P0("P0 — 必须迁移", 0, Color(0xFFB3261E)),   // Red — critical
    P1("P1 — 建议迁移", 1, Color(0xFFF57C00)),   // Orange — important
    P2("P2 — 可选迁移", 2, Color(0xFFF1C40F)),   // Yellow — optional
    NOT_APPLICABLE("N/A — 不适用", 3, Color(0xFF757575)) // Gray
}

// =============================================================
// PermissionCallSite — READ_CONTACTS 权限调用点
// =============================================================
/**
 * READ_CONTACTS permission call site / READ_CONTACTS 权限调用点
 *
 * Represents a single location in the codebase where READ_CONTACTS
 * permission is being requested or used.
 *
 * @param id Unique identifier / 唯一标识符
 * @param filePath Source file path / 源文件路径
 * @param lineNumber Line number in source file / 源文件行号
 * @param methodName Method name containing the call / 包含该调用的方法名
 * @param className Class name containing the call / 包含该调用的类名
 * @param callChain Full call chain (e.g., "MainActivity.onCreate → Helper.getContacts") / 完整调用链
 * @param migrabilityRating Migration priority rating / 可迁移性评级
 * @param migrationSuggestion Suggested migration approach / 迁移建议
 * @param isAlreadyUsingPicker Whether this call site already uses Contacts Picker / 是否已使用 Contacts Picker
 */
data class PermissionCallSite(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val methodName: String,
    val className: String,
    val callChain: String,
    val migrabilityRating: Migrability,
    val migrationSuggestion: String,
    val isAlreadyUsingPicker: Boolean = false
)

// =============================================================
// PickerField — Contacts Picker 可请求的字段类型
// =============================================================
/**
 * Contacts Picker requestable data field / Contacts Picker 可请求的数据字段
 *
 * @param key EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS key / 字段 key
 * @param label Display label / 显示标签
 * @param description Field description / 字段描述
 */
data class PickerField(
    val key: String,
    val label: String,
    val description: String
) {
    companion object {
        /** All supported fields / 所有支持的字段 */
        val ALL_FIELDS = listOf(
            PickerField("email", "Email", "Contact email address / 联系人邮箱地址"),
            PickerField("phone", "Phone", "Contact phone number / 联系人电话号码"),
            PickerField("address", "Address", "Contact postal address / 联系人邮政地址"),
            PickerField("name", "Name", "Contact display name / 联系人显示名称")
        )
    }
}

// =============================================================
// PickerConfig — Contacts Picker 配置
// =============================================================
/**
 * Contacts Picker configuration / Contacts Picker 配置
 *
 * @param selectedFields Set of fields to request / 要请求的字段集合
 * @param selectionLimit Max number of contacts selectable (0 = unlimited) / 最大可选联系人数（0 = 不限）
 * @param matchAllDataFields Whether to match all data fields / 是否匹配所有数据字段
 * @param allowMultipleSelection Whether to allow multiple contact selection / 是否允许多选联系人
 */
data class PickerConfig(
    val selectedFields: Set<String> = setOf("name", "phone"),
    val selectionLimit: Int = 0,
    val matchAllDataFields: Boolean = false,
    val allowMultipleSelection: Boolean = false
)

// =============================================================
// ReportFormat — 报告导出格式
// =============================================================
/**
 * Report export format / 报告导出格式
 */
enum class ReportFormat(val label: String) {
    PDF("PDF"),
    MARKDOWN("Markdown"),
    JSON("JSON")
}

// =============================================================
// ReportConfig — 报告配置
// =============================================================
/**
 * Privacy compliance report configuration / 隐私合规报告配置
 *
 * @param includePermissionUsage Whether to include permission usage records / 是否包含权限使用记录
 * @param includeBeforeAfterComparison Whether to include before/after migration comparison / 是否包含迁移前后对比
 * @param includeDataFlowDiagram Whether to include data flow diagram / 是否包含数据流向图
 * @param includeGooglePlayDataSafety Whether to generate Google Play Data Safety form mapping / 是否生成 Google Play Data Safety 表单映射
 * @param format Report export format / 报告导出格式
 */
data class ReportConfig(
    val includePermissionUsage: Boolean = true,
    val includeBeforeAfterComparison: Boolean = true,
    val includeDataFlowDiagram: Boolean = true,
    val includeGooglePlayDataSafety: Boolean = true,
    val format: ReportFormat = ReportFormat.PDF
)

// =============================================================
// GeneratedReport — 生成的合规报告
// =============================================================
/**
 * Generated privacy compliance report / 生成的隐私合规报告
 *
 * @param fileName Report file name / 报告文件名
 * @param filePath Report file path / 报告文件路径
 * @param generatedAt Timestamp of generation / 生成时间戳
 * @param summary Report summary / 报告摘要
 * @param permissionUsageCount Number of READ_CONTACTS usages found / 发现的 READ_CONTACTS 使用次数
 * @param migratedCount Number of call sites migrated / 已迁移的调用点数量
 * @param remainingCount Number of call sites remaining / 剩余调用点数量
 */
data class GeneratedReport(
    val fileName: String,
    val filePath: String,
    val generatedAt: Long,
    val summary: String,
    val permissionUsageCount: Int,
    val migratedCount: Int,
    val remainingCount: Int
)

// =============================================================
// Template — 集成模板
// =============================================================
/**
 * Contacts Picker integration template / Contacts Picker 集成模板
 *
 * @param id Template unique identifier / 模板唯一标识符
 * @param category Template category (dial/sms/email/social) / 模板分类
 * @param name Template name / 模板名称
 * @param description Template description / 模板描述
 * @param code Code content / 代码内容
 * @param lineCount Approximate line count / 约代码行数
 * @param tags Search tags / 搜索标签
 */
data class Template(
    val id: String,
    val category: TemplateCategory,
    val name: String,
    val description: String,
    val code: String,
    val lineCount: Int,
    val tags: List<String>
)

/**
 * Template category / 模板分类
 */
enum class TemplateCategory(val label: String) {
    DIAL("拨号 / Dial"),
    SMS("短信 / SMS"),
    EMAIL("邮件 / Email"),
    SOCIAL("社交分享 / Social Share")
}

// =============================================================
// CompatibilityResult — 跨版本兼容性检测结果
// =============================================================
/**
 * Cross-version compatibility check result / 跨版本兼容性检测结果
 *
 * @param deviceAndroidVersion Device Android version / 设备 Android 版本
 * @param isCompatible Whether device supports Contacts Picker API / 设备是否支持 Contacts Picker API
 * @param supportedFields List of supported fields on this device / 此设备支持的字段列表
 * @param unsupportedFields List of unsupported fields on this device / 此设备不支持的字段列表
 * @param recommendation Compatibility recommendation / 兼容性建议
 * @param fallbackStrategy Fallback strategy for older devices / 旧设备降级策略
 */
data class CompatibilityResult(
    val deviceAndroidVersion: Int,
    val isCompatible: Boolean,
    val supportedFields: List<String>,
    val unsupportedFields: List<String>,
    val recommendation: String,
    val fallbackStrategy: String
)

// =============================================================
// ContactsPickerToolState — 页面状态
// =============================================================
/**
 * Contacts Picker Tool State / Contacts Picker 工具页面状态
 *
 * Single source of truth for the entire Contacts Picker Tool UI.
 * All UI state is derived from this data class.
 *
 * @param activeTab Currently active tool tab / 当前活跃的工具 Tab
 * @param selectedModule Currently selected project/module / 当前选中的项目/模块
 * @param scanStatus Current scan status / 当前扫描状态
 * @param scanProgress Scan progress 0.0~1.0 / 扫描进度
 * @param scanResults List of READ_CONTACTS call sites found / 发现的 READ_CONTACTS 调用点列表
 * @param selectedCallSite Selected call site for detail view / 选中的调用点详情
 * @param pickerConfig Contacts Picker configuration / Contacts Picker 配置
 * @param reportConfig Privacy report configuration / 报告配置
 * @param generatedReport Generated report, null if not generated yet / 生成的报告
 * @param selectedTemplate Selected integration template / 选中的集成模板
 * @param compatibilityResult Compatibility check result / 兼容性检测结果
 * @param isCheckingCompatibility Whether compatibility check is in progress / 是否正在检测兼容性
 * @param isGeneratingReport Whether report is being generated / 是否正在生成报告
 * @param isExportingTemplate Whether template is being exported / 是否正在导出模板
 * @param isLoading Whether any background operation is in progress / 是否有任何后台操作进行中
 * @param error Error message if any / 错误信息
 */
data class ContactsPickerToolState(
    val activeTab: ToolTab = ToolTab.SCANNER,
    val selectedModule: String? = null,
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanProgress: Float = 0f,
    val scanResults: List<PermissionCallSite> = emptyList(),
    val selectedCallSite: PermissionCallSite? = null,
    val pickerConfig: PickerConfig = PickerConfig(),
    val reportConfig: ReportConfig = ReportConfig(),
    val generatedReport: GeneratedReport? = null,
    val selectedTemplate: Template? = null,
    val compatibilityResult: CompatibilityResult? = null,
    val isCheckingCompatibility: Boolean = false,
    val isGeneratingReport: Boolean = false,
    val isExportingTemplate: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = ContactsPickerToolState()
    }

    /**
     * Filtered scan results by migrability / 按可迁移性过滤的扫描结果
     * Sorted by priority (P0 first) / 按优先级排序（P0 优先）
     */
    val sortedScanResults: List<PermissionCallSite>
        get() = scanResults.sortedBy { it.migrabilityRating.priority }

    /**
     * Count of P0 critical call sites / P0 严重调用点数量
     */
    val p0Count: Int
        get() = scanResults.count { it.migrabilityRating == Migrability.P0 }

    /**
     * Count of P1 important call sites / P1 重要调用点数量
     */
    val p1Count: Int
        get() = scanResults.count { it.migrabilityRating == Migrability.P1 }

    /**
     * Count of P2 optional call sites / P2 可选调用点数量
     */
    val p2Count: Int
        get() = scanResults.count { it.migrabilityRating == Migrability.P2 }

    /**
     * Count of already migrated call sites / 已迁移的调用点数量
     */
    val migratedCount: Int
        get() = scanResults.count { it.isAlreadyUsingPicker }

    /**
     * Scan completion percentage / 扫描完成百分比
     */
    val scanPercentage: Int
        get() = (scanProgress * 100).toInt()
}

// =============================================================
// ContactsPickerToolIntent — 用户意图
// =============================================================
/**
 * Contacts Picker Tool User Intents / Contacts Picker 工具用户意图
 *
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface ContactsPickerToolIntent {
    /** Select a tool tab / 选择工具 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: ToolTab) : ContactsPickerToolIntent

    /** Start READ_CONTACTS permission scan / 开始 READ_CONTACTS 权限扫描
     * @param modulePath Module or project path to scan / 要扫描的模块或项目路径
     */
    data class StartScan(val modulePath: String) : ContactsPickerToolIntent

    /** Cancel ongoing scan / 取消正在进行的扫描 */
    data object CancelScan : ContactsPickerToolIntent

    /** Select a call site to view detail / 选择调用点查看详情
     * @param callSite Call site to select / 要选择的调用点
     */
    data class SelectCallSite(val callSite: PermissionCallSite) : ContactsPickerToolIntent

    /** Clear selected call site / 清除选中的调用点 */
    data object ClearCallSite : ContactsPickerToolIntent

    /** Update Contacts Picker configuration / 更新 Contacts Picker 配置
     * @param config New picker configuration / 新的 Picker 配置
     */
    data class UpdatePickerConfig(val config: PickerConfig) : ContactsPickerToolIntent

    /** Generate privacy compliance report / 生成隐私合规报告
     * @param format Report format / 报告格式
     */
    data class GenerateReport(val format: ReportFormat) : ContactsPickerToolIntent

    /** Select an integration template / 选择集成模板
     * @param template Template to select / 要选择的模板
     */
    data class SelectTemplate(val template: Template) : ContactsPickerToolIntent

    /** Export selected template to target path / 导出选中的模板到目标路径
     * @param template Template to export / 要导出的模板
     * @param targetPath Target file path / 目标文件路径
     */
    data class ExportTemplate(val template: Template, val targetPath: String) : ContactsPickerToolIntent

    /** Check device compatibility for Contacts Picker / 检测设备 Contacts Picker 兼容性 */
    data object CheckCompatibility : ContactsPickerToolIntent

    /** Update report configuration / 更新报告配置
     * @param config New report configuration / 新的报告配置
     */
    data class UpdateReportConfig(val config: ReportConfig) : ContactsPickerToolIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : ContactsPickerToolIntent

    /** Clear generated report / 清除生成的报告 */
    data object ClearReport : ContactsPickerToolIntent
}

// =============================================================
// ContactsPickerToolEffect — 副作用
// =============================================================
/**
 * Contacts Picker Tool Side Effects / Contacts Picker 工具副作用
 *
 * One-time events, consumed only once by UI layer.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface ContactsPickerToolEffect {
    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error message / 是否为错误消息
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : ContactsPickerToolEffect

    /** Export success event / 导出成功事件
     * @param filePath Exported file path / 导出文件路径
     */
    data class ExportSuccess(val filePath: String) : ContactsPickerToolEffect

    /** Scan complete event / 扫描完成事件 */
    data object ScanComplete : ContactsPickerToolEffect

    /** Navigate to template detail / 导航到模板详情
     * @param template Template to show detail / 要显示详情的模板
     */
    data class NavigateToTemplateDetail(val template: Template) : ContactsPickerToolEffect

    /** Open file exporter / 打开文件导出器 */
    data object OpenFileExporter : ContactsPickerToolEffect

    /** Copy template code to clipboard / 复制模板代码到剪贴板
     * @param code Code to copy / 要复制的代码
     */
    data class CopyToClipboard(val code: String) : ContactsPickerToolEffect
}
