package com.mvi.kenny.feature.compose11layouts

// ============================================================
// Compose11LayoutsContract — PRD-161 MVI 契约
// Compose 1.11 Layout & Style APIs 开发工具包
// ============================================================
/**
 * MVI (Model-View-Intent) Architecture Pattern.
 *
 * MVI 三要素：
 * - Model (State): 页面状态的唯一真相来源，Immutable 数据类
 * - View: Composable 函数，消费 State，渲染 UI
 * - Intent: 用户意图（用户操作），ViewModel 收到 Intent 后执行业务逻辑
 *
 * Effect: 一次性副作用（导航、Toast），通过 Channel 传递
 *
 * @see Compose11LayoutsViewModel
 * @see Compose11LayoutsScreen
 */

// ============================================================
// Section Enum — 工具分区枚举
// ============================================================
/**
 * 布局 API 工具箱的功能分区。
 * Represents the functional sections of the Layout API Toolbox.
 *
 * @param titleCn 中文标题
 * @param titleEn 英文标题
 * @param description 分区功能描述
 */
enum class LayoutSection(
    val titleCn: String,
    val titleEn: String,
    val description: String,
    val icon: String
) {
    HOME(
        titleCn = "首页",
        titleEn = "Home",
        description = "Compose 1.11 布局 API 工具箱首页 — 四大工具入口",
        icon = "🏠"
    ),
    GRID(
        titleCn = "Grid API",
        titleEn = "Grid API Guide & Templates",
        description = "Grid API 指南与模板库 — 仪表盘 / 日历 / 棋盘 / 画廊",
        icon = "📐"
    ),
    FLEXBOX(
        titleCn = "FlexBox API",
        titleEn = "FlexBox API Guide & Templates",
        description = "FlexBox API 指南与模板库 — 导航栏 / 侧边栏 / 流式布局",
        icon = "📦"
    ),
    STYLE(
        titleCn = "Style API",
        titleEn = "Style API Guide",
        description = "Style API 开发指南 — 性能考量 / 动画过渡集成",
        icon = "🎨"
    ),
    MEDIA_QUERY(
        titleCn = "MediaQuery × 折叠屏",
        titleEn = "MediaQuery × Foldable Templates",
        description = "折叠屏 tabletop mode / 平板 / 手机自适应模板",
        icon = "📱"
    ),
    COMPLIANCE(
        titleCn = "CI 合规检测",
        titleEn = "CI Compliance Scanner",
        description = "扫描项目中的 Grid/FlexBox/Style API 使用合规性",
        icon = "🔍"
    ),
    VERIFICATION(
        titleCn = "升级验证",
        titleEn = "Upgrade Verification",
        description = "BOM 版本检测 / 依赖树分析 / 升级建议",
        icon = "✅"
    )
}

// ============================================================
// Tab Enum — 子 Tab 枚举
// ============================================================
/**
 * Grid/FlexBox 分区的子 Tab。
 * Sub-tabs for Grid and FlexBox sections.
 */
enum class LayoutTab(val label: String, val labelEn: String) {
    GUIDE("指南", "Guide"),
    TEMPLATES("模板库", "Templates")
}

// ============================================================
// Template Data Models — 模板数据模型
// ============================================================

/**
 * Grid 模板模型。
 * Represents a Grid layout template.
 *
 * @param id 模板唯一标识
 * @param nameCn 中文名称
 * @param nameEn 英文名称
 * @param description 模板描述
 * @param category 模板分类（dashboard/calendar/chessboard/gallery）
 * @param code 模板代码片段
 * @param parameters 可调参数列表
 */
data class GridTemplate(
    val id: String,
    val nameCn: String,
    val nameEn: String,
    val description: String,
    val category: GridCategory,
    val code: String,
    val parameters: List<TemplateParameter> = emptyList()
)

enum class GridCategory(val label: String) {
    DASHBOARD("仪表盘"),
    CALENDAR("日历"),
    CHESSBOARD("棋盘"),
    GALLERY("画廊"),
    CHART("统计图表")
}

/**
 * FlexBox 模板模型。
 * Represents a FlexBox layout template.
 *
 * @param id 模板唯一标识
 * @param nameCn 中文名称
 * @param nameEn 英文名称
 * @param description 模板描述
 * @param category 模板分类（navbar/sidebar/cardlist/flow）
 * @param code 模板代码片段
 * @param parameters 可调参数列表
 */
data class FlexBoxTemplate(
    val id: String,
    val nameCn: String,
    val nameEn: String,
    val description: String,
    val category: FlexBoxCategory,
    val code: String,
    val parameters: List<TemplateParameter> = emptyList()
)

enum class FlexBoxCategory(val label: String) {
    NAVBAR("导航栏"),
    SIDEBAR("侧边栏"),
    CARD_LIST("卡片列表"),
    FLOW("流式布局")
}

/**
 * 模板可调参数。
 * Adjustable parameter for template preview.
 *
 * @param key 参数键名
 * @param label 参数显示标签
 * @param type 参数类型（int/float/enum）
 * @param defaultValue 默认值
 * @param minValue 最小值（用于滑块）
 * @param maxValue 最大值（用于滑块）
 * @param options 选项列表（用于枚举类型）
 */
data class TemplateParameter(
    val key: String,
    val label: String,
    val type: String,
    val defaultValue: Any,
    val minValue: Float? = null,
    val maxValue: Float? = null,
    val options: List<String> = emptyList()
)

/**
 * MediaQuery 模板模型。
 * Represents a MediaQuery + Foldable responsive template.
 *
 * @param id 模板唯一标识
 * @param nameCn 中文名称
 * @param nameEn 英文名称
 * @param description 模板描述
 * @param screenMode 适配的屏幕形态
 * @param code 模板代码片段
 */
data class MediaQueryTemplate(
    val id: String,
    val nameCn: String,
    val nameEn: String,
    val description: String,
    val screenMode: ScreenMode,
    val code: String
)

enum class ScreenMode(val label: String, val labelEn: String) {
    PHONE("手机", "Phone"),
    TABLET("平板", "Tablet"),
    FOLDABLE("折叠屏", "Foldable"),
    TOPIC_TABLE_MODE("桌面模式", "Tabletop Mode")
}

/**
 * Style API 章节模型。
 * Represents a chapter in the Style API guide.
 *
 * @param id 章节 ID
 * @param title 章节标题
 * @param content 章节内容（Markdown 格式）
 * @param codeExample 示例代码
 * @param isExpanded 是否展开
 */
data class StyleGuideSection(
    val id: String,
    val title: String,
    val content: String,
    val codeExample: String = "",
    val isExpanded: Boolean = false
)

// ============================================================
// Compliance Data Models — CI 合规检测数据模型
// ============================================================

/**
 * 合规扫描结果。
 * Represents a compliance scan result.
 *
 * @param id 违规项唯一标识
 * @param severity 严重程度（ERROR/WARN/INFO）
 * @param title 违规标题
 * @param description 违规描述
 * @param filePath 涉及文件路径
 * @param lineNumber 行号
 * @param suggestion 修复建议
 */
data class ComplianceResult(
    val id: String,
    val severity: ComplianceSeverity,
    val title: String,
    val description: String,
    val filePath: String,
    val lineNumber: Int? = null,
    val suggestion: String
)

enum class ComplianceSeverity(val label: String, val color: Long) {
    ERROR("错误", 0xFFEF4444),
    WARN("警告", 0xFFF59E0B),
    INFO("信息", 0xFF8B5CF6)
}

/**
 * 升级验证结果。
 * Represents an upgrade verification result.
 *
 * @param currentBom 当前使用的 Compose BOM 版本
 * @param latestBom 最新可用的 Compose BOM 版本
 * @param dependencyTree Compose 依赖树分析结果
 * @param suggestions 升级建议列表
 * @param isCompliant 是否合规
 */
data class VerificationResult(
    val currentBom: String,
    val latestBom: String,
    val dependencyTree: List<DependencyNode>,
    val suggestions: List<String>,
    val isCompliant: Boolean
)

/**
 * 依赖树节点。
 * Represents a node in the dependency tree.
 *
 * @param name 依赖名称
 * @param version 当前版本
 * @param latestVersion 最新版本
 * @param children 子依赖
 */
data class DependencyNode(
    val name: String,
    val version: String,
    val latestVersion: String,
    val children: List<DependencyNode> = emptyList()
)

// ============================================================
// State — 页面状态（MVI Model）
// ============================================================

/**
 * 布局 API 工具箱页面状态。
 * Immutable page state — the single source of truth in MVI.
 *
 * @param selectedSection 当前选中的功能分区
 * @param selectedTab 当前选中的子 Tab（Guide / Templates）
 * @param currentComposeBom 当前选择的 Compose BOM 版本
 * @param gridTemplates Grid 模板列表
 * @param flexboxTemplates FlexBox 模板列表
 * @param styleGuideSections Style API 指南章节列表
 * @param mediaQueryTemplates MediaQuery 模板列表
 * @param complianceResults 合规扫描结果列表
 * @param verificationResult 升级验证结果
 * @param scanInProgress 是否正在扫描
 * @param scanProgress 扫描进度 0.0–1.0
 * @param currentScanFile 当前正在扫描的文件
 * @param selectedTemplate 选中的模板（用于预览）
 * @param previewParameters 预览参数
 * @param isDarkMode 是否为暗色模式
 * @param expandedStyleSections 展开的 Style 指南章节 ID 集合
 * @param errorMessage 错误信息
 */
data class Compose11LayoutsState(
    val selectedSection: LayoutSection = LayoutSection.HOME,
    val selectedTab: LayoutTab = LayoutTab.GUIDE,
    val currentComposeBom: String = "2024.01.01",
    val gridTemplates: List<GridTemplate> = emptyList(),
    val flexboxTemplates: List<FlexBoxTemplate> = emptyList(),
    val styleGuideSections: List<StyleGuideSection> = emptyList(),
    val mediaQueryTemplates: List<MediaQueryTemplate> = emptyList(),
    val complianceResults: List<ComplianceResult> = emptyList(),
    val verificationResult: VerificationResult? = null,
    val scanInProgress: Boolean = false,
    val scanProgress: Float = 0f,
    val currentScanFile: String? = null,
    val selectedTemplate: Any? = null,
    val previewParameters: Map<String, Any> = emptyMap(),
    val isDarkMode: Boolean = false,
    val expandedStyleSections: Set<String> = emptySet(),
    val errorMessage: String? = null,
    // CI Scanner
    val projectPathInput: String = "",
    // Verification
    val verificationInProgress: Boolean = false,
    // Preview Dialog
    val isPreviewDialogOpen: Boolean = false,
    // Screen mode for MediaQuery preview
    val selectedScreenMode: ScreenMode = ScreenMode.PHONE
) {
    companion object {
        /** 初始状态 */
        val Initial = Compose11LayoutsState()
    }
}

// ============================================================
// Intent — 用户意图（MVI Intent）
// ============================================================

/**
 * 布局 API 工具箱用户意图。
 * Sealed class representing all user intentions.
 * Each intent triggers ViewModel to process business logic.
 */
sealed class Compose11LayoutsIntent {
    // 分区选择 / Section selection
    data class SelectSection(val section: LayoutSection) : Compose11LayoutsIntent()

    // Tab 切换 / Tab switching
    data class SelectTab(val tab: LayoutTab) : Compose11LayoutsIntent()

    // BOM 版本切换 / BOM version change
    data class ChangeBomVersion(val version: String) : Compose11LayoutsIntent()

    // 模板选择 / Template selection
    data class SelectTemplate(val template: Any) : Compose11LayoutsIntent()

    // 预览参数更新 / Preview parameter update
    data class UpdatePreviewParameter(val key: String, val value: Any) : Compose11LayoutsIntent()

    // 预览 Dialog 开关 / Preview Dialog toggle
    data object TogglePreviewDialog : Compose11LayoutsIntent()
    data object ClosePreviewDialog : Compose11LayoutsIntent()

    // CI 扫描 / CI Scan
    data class UpdateProjectPath(val path: String) : Compose11LayoutsIntent()
    data class StartComplianceScan(val projectPath: String) : Compose11LayoutsIntent()

    // 模板代码复制 / Template code copy
    data class CopyTemplateCode(val templateId: String) : Compose11LayoutsIntent()

    // 合规报告导出 / Compliance report export
    data class ExportComplianceReport(val format: ReportFormat) : Compose11LayoutsIntent()

    // 升级验证 / Upgrade verification
    data object RunVerification : Compose11LayoutsIntent()

    // 暗色模式切换 / Dark mode toggle
    data object ToggleDarkMode : Compose11LayoutsIntent()

    // Style 指南章节展开/折叠 / Style guide section expand/collapse
    data class ToggleStyleSection(val sectionId: String) : Compose11LayoutsIntent()

    // MediaQuery 屏幕形态切换 / Screen mode switch
    data class SelectScreenMode(val mode: ScreenMode) : Compose11LayoutsIntent()

    // 错误消息清除 / Error message dismiss
    data object DismissError : Compose11LayoutsIntent()
}

enum class ReportFormat { PDF, JSON, MARKDOWN }

// ============================================================
// Effect — 副作用（MVI Effect）
// ============================================================

/**
 * 布局 API 工具箱副作用。
 * One-time side effects emitted via Channel.
 * ViewModel sends, UI observes and reacts.
 */
sealed class Compose11LayoutsEffect {
    // Toast 提示
    data class ShowToast(val message: String) : Compose11LayoutsEffect()

    // 代码已复制到剪贴板
    data object CodeCopied : Compose11LayoutsEffect()

    // 分享报告
    data class ShareReport(val content: String, val format: ReportFormat) : Compose11LayoutsEffect()

    // 扫描完成
    data object ScanComplete : Compose11LayoutsEffect()

    // 错误提示
    data class ShowError(val message: String) : Compose11LayoutsEffect()
}
