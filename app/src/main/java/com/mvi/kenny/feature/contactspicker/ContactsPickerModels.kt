package com.mvi.kenny.feature.contactspicker

import androidx.compose.ui.graphics.Color

// =============================================================
// ScanStatus — 扫描状态
// =============================================================
/**
 * Scan status for READ_CONTACTS scan operation.
 * READ_CONTACTS 扫描操作状态
 */
enum class ScanStatus(val label: String) {
    IDLE("待命"),
    SCANNING("扫描中"),
    COMPLETED("完成"),
    ERROR("错误")
}

// =============================================================
// ToolTab — 功能模块 Tab
// =============================================================
/**
 * Tool tab pages / 功能模块 Tab 页
 *
 * @property label Display label / 显示标签
 */
enum class ToolTab(val label: String) {
    SCANNER("扫描器"),
    LIBRARY("封装库"),
    REPORT("报告"),
    TEMPLATES("模板"),
    COMPATIBILITY("兼容检测")
}

// =============================================================
// ContactField — 联系人字段枚举
// =============================================================
/**
 * Contact fields available in Contacts Picker API.
 * Contacts Picker API 支持的联系人字段
 *
 * @param value Android ContactsContract field value / Android ContactsContract 字段值
 * @param label Display label / 显示标签
 */
enum class ContactField(
    val value: String,
    val label: String
) {
    NAME("name", "姓名"),
    PHONE("phone", "电话"),
    EMAIL("email", "邮箱"),
    ADDRESS("address", "地址"),
    ORGANIZATION("organization", "公司/组织"),
    PHOTO("photo", "头像"),
    NOTE("note", "备注")
}

// =============================================================
// Migrability — 可迁移性评分
// =============================================================
/**
 * Migrability score for READ_CONTACTS call sites.
 * READ_CONTACTS 调用点的可迁移性评分
 *
 * @param label Display label / 显示标签
 * @param labelZh Display label (Chinese) / 显示标签（中文）
 * @param color Severity color / 颜色
 */
enum class Migrability(
    val label: String,
    val labelZh: String,
    val color: Color
) {
    /** P0: Can migrate directly to Contacts Picker / 可直接迁移到 Contacts Picker */
    P0("P0 - 直接迁移", "P0 - 直接迁移", Color(0xFFE53935)),

    /** P1: Can migrate with minor changes / 可小幅修改后迁移 */
    P1("P1 - 需修改", "P1 - 需修改", Color(0xFFFB8C00)),

    /** P2: Complex migration, needs review / 复杂迁移，需人工评估 */
    P2("P2 - 需评估", "P2 - 需评估", Color(0xFFFDD835))
}

// =============================================================
// ReportFormat — 报告格式
// =============================================================
/**
 * Privacy compliance report export format.
 * 隐私合规报告导出格式
 */
enum class ReportFormat {
    MARKDOWN,
    JSON,
    PDF
}

// =============================================================
// SnackbarType — Snackbar 类型
// =============================================================
/**
 * Snackbar type for effect messages.
 * 效果消息类型
 *
 * @param color Compose Color / Compose 颜色
 */
enum class SnackbarType(val color: Color) {
    SUCCESS(Color(0xFF4CAF50)),
    ERROR(Color(0xFFB3261E)),
    INFO(Color(0xFF6750A4))
}

// =============================================================
// PermissionCallSite — READ_CONTACTS 调用点
// =============================================================
/**
 * READ_CONTACTS permission call site found during scan.
 * 扫描过程中发现的 READ_CONTACTS 权限调用点
 *
 * @param id Unique ID / 唯一ID
 * @param filePath File path / 文件路径
 * @param lineNumber Line number in file / 文件行号
 * @param callChain Call chain description / 调用链描述
 * @param migrability Migrability score / 可迁移性评分
 * @param codeSnippet Code snippet around the call / 调用处代码片段
 */
data class PermissionCallSite(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val callChain: String,
    val migrability: Migrability,
    val codeSnippet: String
)

// =============================================================
// PickerConfig — Picker 配置
// =============================================================
/**
 * Contacts Picker API configuration.
 * Contacts Picker API 配置
 *
 * @param fields List of requested contact fields / 请求的联系人字段列表
 * @param selectionLimit Maximum number of contacts user can select / 用户最多可选择的联系人数
 */
data class PickerConfig(
    val fields: List<ContactField> = listOf(ContactField.NAME, ContactField.PHONE),
    val selectionLimit: Int = 1
)

// =============================================================
// ReportConfig — 报告配置
// =============================================================
/**
 * Privacy compliance report configuration.
 * 隐私合规报告配置
 *
 * @param includePermissionUsage Include permission usage records / 包含权限使用记录
 * @param includeMigrationComparison Include before/after migration comparison / 包含迁移前后对比
 */
data class ReportConfig(
    val includePermissionUsage: Boolean = true,
    val includeMigrationComparison: Boolean = true
)

// =============================================================
// GeneratedReport — 生成的报告
// =============================================================
/**
 * Generated privacy compliance report.
 * 生成的隐私合规报告
 *
 * @param content Report content / 报告内容
 * @param format Report format / 报告格式
 */
data class GeneratedReport(
    val content: String,
    val format: ReportFormat
)

// =============================================================
// Template — 集成模板
// =============================================================
/**
 * Contacts Picker integration template.
 * Contacts Picker 集成模板
 *
 * @param id Template ID / 模板ID
 * @param name Template name / 模板名称
 * @param scenario Scenario description / 适用场景描述
 * @param code Template code snippet / 模板代码
 */
data class Template(
    val id: String,
    val name: String,
    val scenario: String,
    val code: String
)

// =============================================================
// CompatibilityResult — 兼容性检测结果
// =============================================================
/**
 * Android version compatibility result.
 * Android 版本兼容性检测结果
 *
 * @param currentSdkInt Current device SDK version / 当前设备 SDK 版本
 * @param isCompatible Whether Contacts Picker API is available / Contacts Picker API 是否可用
 * @param fallbackStrategy Fallback strategy description / 降级策略描述
 * @param features List of available features / 可用功能列表
 */
data class CompatibilityResult(
    val currentSdkInt: Int,
    val isCompatible: Boolean,
    val fallbackStrategy: String,
    val features: List<String>
)
