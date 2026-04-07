package com.mvi.kenny.feature.agentskills

/**
 * ============================================================
 * AgentSkillsContract — Android Studio Panda 3 Agent Skills 开发工具包
 * ============================================================
 * PRD-041 | Android Studio Panda 3 Agent Skills 开发工具包
 * 设计文档：designs/PRD-041-Android-Studio-Panda-3-Agent-Skills.md
 *
 * MVI 三要素（Three pillars of MVI）:
 * — State: 页面状态的唯一真相来源（Single source of truth），Immutable data class
 * — Intent: 用户意图（User intent），ViewModel receives Intent and executes business logic
 * — Effect: 一次性副作用（One-time side effects），delivered via Channel
 *
 * 整体架构（Overall Architecture）:
 * — 三个子 Feature: SkillEditor / PermissionDashboard / SkillMarket
 * — 每个子 Feature 有独立的 State/Intent/Effect
 * — 父级 AgentSkillsState 组合所有子状态（Composition over inheritance）
 *
 * 与设计文档的对应关系（Mapping to Design Doc）:
 * — Section 3.2: 页面清单（Page List）定义了 6 个页面
 * — Section 5: 组件清单（Component List）定义了 UI 组件
 * — Section 6: MVI 状态定义（State Definition）定义了 State/Intent/Effect 结构
 * — Section 7: 视觉规范（Visual Spec）定义了主题/颜色/字体
 *
 * @see AgentSkillsViewModel 状态管理
 * @see AgentSkillsScreen UI 层
 * —————————————————————————————————————————————————————
 */

// ================================================================
// 6.1 SkillEditorFeature — Skill 创建与编辑器
// ================================================================

/** Skill 文件类型枚举（适用文件类型） */
enum class SkillFileType(val displayName: String, val extensions: List<String>) {
    KOTLIN("Kotlin", listOf("kt", "kts")),
    XML("XML", listOf("xml")),
    GRADLE("Gradle", listOf("gradle", "gradle.kts")),
    COMPOSE("Compose", listOf("kt")),
    ALL("All Files", listOf("*"))
}

/** DSL 校验错误 */
data class ValidationError(
    val line: Int,
    val column: Int,
    val message: String,
    val severity: ValidationSeverity
)

/** 校验严重程度 */
enum class ValidationSeverity {
    ERROR,   // 严重错误（红色），阻止保存
    WARNING, // 警告（黄色），允许保存但提示
    INFO     // 信息（蓝色），参考用
}

/** 保存结果 */
sealed class SaveResult {
    data object Success : SaveResult()
    data class Error(val message: String) : SaveResult()
}

/** 测试结果 */
data class TestResult(
    val passed: Boolean,
    val output: String,
    val durationMs: Long
)

/**
 * Skill 编辑器状态（Skill Editor State）
 * —————————————————————————————————————————————————————
 *
 * @param skillId 当前编辑的 Skill ID（null 表示新建）
 * @param name Skill 名称
 * @param description Skill 描述
 * @param dslContent DSL 内容（Kotlin DSL 格式）
 * @param version 版本号（semver 格式）
 * @param versionNotes 版本变更说明
 * @param fileTypes 适用的文件类型列表
 * @param validationErrors DSL 校验错误列表
 * @param isDirty 是否有未保存的更改
 * @param isSaving 是否正在保存
 * @param saveResult 上次保存结果
 * @param testResult 上次测试结果
 */
data class SkillEditorState(
    val skillId: String? = null,
    val name: String = "",
    val description: String = "",
    val dslContent: String = DEFAULT_DSL_TEMPLATE,
    val version: String = "1.0.0",
    val versionNotes: String = "",
    val fileTypes: List<SkillFileType> = listOf(SkillFileType.ALL),
    val validationErrors: List<ValidationError> = emptyList(),
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val saveResult: SaveResult? = null,
    val testResult: TestResult? = null,
    val wizardStep: Int = 0  // 0=DSL定义, 1=权限配置, 2=本地测试
) {
    companion object {
        /** 初始状态（Initial state） */
        val Initial = SkillEditorState()

        /** 默认 DSL 模板（Default DSL template for new Skill） */
        const val DEFAULT_DSL_TEMPLATE = """skill {
    name = "My Skill"
    description = "Describe what this skill does"
    version = "1.0.0"
    
    // 适用文件类型（Applicable file types）
    fileTypes = ["*.kt"]
    
    // 约束规则（Constraint rules）
    constraints {
        // 代码风格约束（Code style constraints）
        maxLineLength = 120
        indentStyle = IndentStyle.KOTLIN
        
        // 架构约束（Architecture constraints）
        requireViewModel = true
        requireContractPattern = true
        
        // 安全约束（Security constraints）
        noReflection = false
        noRuntimeCodeGen = false
    }
    
    // 权限配置（Permission configuration）
    permissions {
        allowRead = true
        allowWrite = false
        allowedPaths = ["src/main/kotlin"]
        deniedPaths = ["src/main/res/values/secrets.xml"]
    }
}
"""
    }

    /** DSL 是否有效（无 ERROR 级别错误） */
    val isDslValid: Boolean get() = validationErrors.none { it.severity == ValidationSeverity.ERROR }
}

/** Skill 编辑器用户意图（Skill Editor Intents） */
sealed interface SkillEditorIntent {
    data class UpdateDsl(val content: String) : SkillEditorIntent()
    data class UpdateName(val name: String) : SkillEditorIntent()
    data class UpdateDescription(val desc: String) : SkillEditorIntent()
    data class UpdateVersion(val version: String) : SkillEditorIntent()
    data class UpdateVersionNotes(val notes: String) : SkillEditorIntent()
    data class ToggleFileType(val fileType: SkillFileType) : SkillEditorIntent()
    data class ValidateDsl(val content: String) : SkillEditorIntent()
    data object SaveSkill : SkillEditorIntent()
    data class RunLocalTest(val codeSnippet: String) : SkillEditorIntent()
    data object DiscardChanges : SkillEditorIntent()
    data class SetWizardStep(val step: Int) : SkillEditorIntent()
    data object CreateNewSkill : SkillEditorIntent()
    data class LoadSkill(val skillId: String) : SkillEditorIntent()
}

/** Skill 编辑器副作用（Skill Editor Effects） */
sealed interface SkillEditorEffect {
    data object NavigateBack : SkillEditorEffect()
    data class ShowToast(val message: String) : SkillEditorEffect()
    data object DSLValid : SkillEditorEffect()
    data class DSLInvalid(val errors: List<ValidationError>) : SkillEditorEffect()
    data class SkillSaved(val skillId: String) : SkillEditorEffect()
}

// ================================================================
// 6.2 PermissionDashboardFeature — 权限策略仪表盘
// ================================================================

/** 权限模式（Permission mode for path） */
enum class PermissionMode(val symbol: String, val color: Long) {
    ALLOW(  "✓", 0xFF81C784), // 绿色：完全允许
    READONLY("R", 0xFFFFB74D), // 黄色：只读
    DENY(   "✕", 0xFFE57373), // 红色：拒绝
    UNSET(  "—", 0xFF9E9E9E)  // 灰色：未设置
}

/** 权限模板（Permission template for quick apply） */
enum class PermissionTemplate(val displayName: String, val description: String) {
    READ_ONLY("Read-Only", "只读模式，只允许读取源码文件"),
    STANDARD_DEV("Standard Dev", "标准开发模式，允许读写源码，限制资源文件"),
    ADMIN("Admin", "管理模式，允许读写所有文件（慎用）")
}

/** 权限树节点（Permission tree node） */
data class PermissionNode(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val mode: PermissionMode = PermissionMode.UNSET,
    val children: List<PermissionNode> = emptyList(),
    val isExpanded: Boolean = false
)

/**
 * 权限仪表盘状态（Permission Dashboard State）
 * —————————————————————————————————————————————————————
 *
 * @param rootNodes 权限树根节点列表
 * @param selectedNode 当前选中的节点
 * @param activeTemplate 当前激活的权限模板
 * @param isDirty 是否有未保存的更改
 * @param isSaving 是否正在保存
 */
data class PermissionDashboardState(
    val rootNodes: List<PermissionNode> = emptyList(),
    val selectedNode: PermissionNode? = null,
    val activeTemplate: PermissionTemplate? = null,
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val saveResult: SaveResult? = null
) {
    companion object {
        /** 初始状态 */
        val Initial = PermissionDashboardState()

        /** 生成示例权限树（Mock data for demo） */
        fun createMockTree(): List<PermissionNode> = listOf(
            PermissionNode(
                path = "src/main/kotlin",
                name = "kotlin",
                isDirectory = true,
                mode = PermissionMode.ALLOW,
                children = listOf(
                    PermissionNode("src/main/kotlin/com", "com", true, PermissionMode.ALLOW, listOf(
                        PermissionNode("src/main/kotlin/com/example", "example", true, PermissionMode.ALLOW, listOf(
                            PermissionNode("src/main/kotlin/com/example/MyFile.kt", "MyFile.kt", false, PermissionMode.ALLOW)
                        ))
                    )),
                    PermissionNode("src/main/kotlin/util", "util", true, PermissionMode.ALLOW, listOf(
                        PermissionNode("src/main/kotlin/util/Helper.kt", "Helper.kt", false, PermissionMode.ALLOW)
                    ))
                )
            ),
            PermissionNode(
                path = "src/main/res",
                name = "res",
                isDirectory = true,
                mode = PermissionMode.READONLY,
                children = listOf(
                    PermissionNode("src/main/res/values", "values", true, PermissionMode.READONLY, listOf(
                        PermissionNode("src/main/res/values/strings.xml", "strings.xml", false, PermissionMode.READONLY),
                        PermissionNode("src/main/res/values/colors.xml", "colors.xml", false, PermissionMode.READONLY)
                    )),
                    PermissionNode("src/main/res/values/secrets.xml", "secrets.xml", false, PermissionMode.DENY)
                )
            ),
            PermissionNode(
                path = "build.gradle.kts",
                name = "build.gradle.kts",
                isDirectory = false,
                mode = PermissionMode.DENY
            )
        )
    }
}

/** 权限仪表盘用户意图 */
sealed interface PermissionDashboardIntent {
    data class SetNodePermission(val nodePath: String, val mode: PermissionMode) : PermissionDashboardIntent()
    data class ApplyTemplate(val template: PermissionTemplate) : PermissionDashboardIntent()
    data class ToggleNodeExpand(val nodePath: String) : PermissionDashboardIntent()
    data class SelectNode(val node: PermissionNode) : PermissionDashboardIntent()
    data object SavePolicy : PermissionDashboardIntent()
    data object ResetPolicy : PermissionDashboardIntent()
}

/** 权限仪表盘副作用 */
sealed interface PermissionDashboardEffect {
    data class ShowToast(val message: String) : PermissionDashboardEffect()
    data object PolicySaved : PermissionDashboardEffect()
}

// ================================================================
// SkillMarketFeature — Skills 市场浏览
// ================================================================

/** 市场过滤条件（Market filter criteria） */
data class MarketFilter(
    val searchQuery: String = "",
    val category: MarketCategory = MarketCategory.ALL,
    val sortBy: MarketSortOption = MarketSortOption.POPULAR
)

/** 市场分类（Market category） */
enum class MarketCategory(val displayName: String) {
    ALL("全部"),
    CODE_STYLE("代码风格"),
    ARCHITECTURE("架构规范"),
    TESTING("测试规范"),
    SECURITY("安全规范"),
    PERFORMANCE("性能优化")
}

/** 市场排序选项（Market sort option） */
enum class MarketSortOption(val displayName: String) {
    POPULAR("最受欢迎"),
    RATING("评分最高"),
    NEWEST("最新发布"),
    DOWNLOADS("下载量最多")
}

/** 市场 Skill 条目（Market skill listing） */
data class MarketSkill(
    val id: String,
    val name: String,
    val description: String,
    val author: String,
    val version: String,
    val rating: Float,       // 0.0 ~ 5.0
    val downloadCount: Int,
    val category: MarketCategory,
    val tags: List<String>,
    val isInstalled: Boolean,
    val lastUpdated: String
)

/**
 * Skills 市场状态（Skills Market State）
 * —————————————————————————————————————————————————————
 *
 * @param skills 市场 Skill 列表
 * @param filter 当前过滤条件
 * @param selectedSkill 当前选中的 Skill（用于详情预览）
 * @param isLoading 是否正在加载
 * @param errorMessage 错误信息
 */
data class SkillMarketState(
    val skills: List<MarketSkill> = emptyList(),
    val filter: MarketFilter = MarketFilter(),
    val selectedSkill: MarketSkill? = null,
    val isLoading: Boolean = false,
    val isInstalling: Boolean = false,
    val installingSkillId: String? = null,
    val errorMessage: String? = null
) {
    companion object {
        val Initial = SkillMarketState()
    }
}

/** 市场用户意图 */
sealed interface SkillMarketIntent {
    data object LoadMarket : SkillMarketIntent()
    data class Search(val query: String) : SkillMarketIntent()
    data class SetCategory(val category: MarketCategory) : SkillMarketIntent()
    data class SetSortOption(val option: MarketSortOption) : SkillMarketIntent()
    data class SelectSkill(val skill: MarketSkill) : SkillMarketIntent()
    data class InstallSkill(val skillId: String) : SkillMarketIntent()
    data class UninstallSkill(val skillId: String) : SkillMarketIntent()
    data object DismissDetail : SkillMarketIntent()
}

/** 市场副作用 */
sealed interface SkillMarketEffect {
    data class ShowToast(val message: String) : SkillMarketEffect()
    data class SkillInstalled(val skillId: String) : SkillMarketEffect()
}

// ================================================================
// MySkillsFeature — 我的 Skills 管理
// ================================================================

/** 我的 Skill 状态 */
data class MySkill(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val lastModified: String,
    val isEnabled: Boolean,
    val category: String
)

data class MySkillsState(
    val skills: List<MySkill> = emptyList(),
    val selectedSkill: MySkill? = null,
    val isLoading: Boolean = false
) {
    companion object {
        val Initial = MySkillsState()
    }
}

sealed interface MySkillsIntent {
    data object LoadMySkills : MySkillsIntent()
    data class ToggleSkill(val skillId: String, val enabled: Boolean) : MySkillsIntent()
    data class EditSkill(val skill: MySkill) : MySkillsIntent()
    data class DeleteSkill(val skillId: String) : MySkillsIntent()
}

sealed interface MySkillsEffect {
    data class ShowToast(val message: String) : MySkillsEffect()
    data class NavigateToEditor(val skillId: String) : MySkillsEffect()
}

// ================================================================
// AuditFeature — 执行审计视图
// ================================================================

/** 审计时间范围（Audit time range filter） */
enum class AuditTimeRange(val displayName: String) {
    WEEK("近一周"),
    DAY("近一天"),
    HOUR("近一小时"),
    ALL("全部")
}

/** 审计日志条目（Audit log entry） */
data class AuditEntry(
    val id: String,
    val timestamp: String,
    val skillName: String,
    val agentName: String,
    val action: String,         // e.g., "Read file", "Write file", "Run command"
    val targetPath: String?,     // 受影响文件/路径
    val outcome: AuditOutcome,
    val durationMs: Long
)

/** 审计结果 */
enum class AuditOutcome(val displayName: String, val color: Long) {
    SUCCESS("成功", 0xFF81C784),
    DENIED("拒绝", 0xFFE57373),
    WARNING("警告", 0xFFFFB74D),
    ERROR("错误", 0xFFEF5350),
}

/**
 * 审计视图状态（Audit View State）
 * —————————————————————————————————————————————————————
 *
 * @param entries 审计日志条目列表
 * @param timeRange 当前时间范围过滤
 * @param skillFilter 按 Skill 名称过滤
 * @param selectedEntry 当前选中的条目（查看详情）
 * @param isLoading 是否正在加载
 */
data class AuditState(
    val entries: List<AuditEntry> = emptyList(),
    val timeRange: AuditTimeRange = AuditTimeRange.WEEK,
    val skillFilter: String = "",
    val selectedEntry: AuditEntry? = null,
    val isLoading: Boolean = false
) {
    companion object {
        val Initial = AuditState()

        /** 生成模拟审计数据（Mock audit data for demo） */
        fun createMockEntries(): List<AuditEntry> = listOf(
            AuditEntry("a1", "2026-04-07 14:30:22", "Kotlin Style Checker", "Gemini 3", "Read file", "src/main/kotlin/com/example/App.kt", AuditOutcome.SUCCESS, 120),
            AuditEntry("a2", "2026-04-07 14:30:45", "Architecture Enforcer", "Gemini 3", "Validate pattern", "src/main/kotlin/com/example/viewmodel", AuditOutcome.SUCCESS, 85),
            AuditEntry("a3", "2026-04-07 14:31:02", "Security Scanner", "Claude 3", "Read file", "src/main/res/values/secrets.xml", AuditOutcome.DENIED, 50),
            AuditEntry("a4", "2026-04-07 13:45:11", "Kotlin Style Checker", "Gemini 3", "Read file", "src/main/kotlin/com/example/util/Helper.kt", AuditOutcome.SUCCESS, 95),
            AuditEntry("a5", "2026-04-07 12:20:00", "Detekt Integration", "Claude 3", "Run check", null, AuditOutcome.WARNING, 3200),
            AuditEntry("a6", "2026-04-06 22:15:33", "Architecture Enforcer", "Gemini 3", "Write file", "src/main/kotlin/com/example/App.kt", AuditOutcome.SUCCESS, 210),
            AuditEntry("a7", "2026-04-06 18:00:01", "Security Scanner", "Gemini 3", "Scan project", null, AuditOutcome.SUCCESS, 5500)
        )
    }
}

sealed interface AuditIntent {
    data object LoadAudit : AuditIntent()
    data class SetTimeRange(val range: AuditTimeRange) : AuditIntent()
    data class SetSkillFilter(val skillName: String) : AuditIntent()
    data class SelectEntry(val entry: AuditEntry) : AuditIntent()
    data object DismissDetail : AuditIntent()
    data object ExportAudit : AuditIntent()
}

sealed interface AuditEffect {
    data class ShowToast(val message: String) : AuditEffect()
    data class ExportReady(val filePath: String) : AuditEffect()
}

// ================================================================
// LintIntegrationFeature — Lint/Detekt 联动设置
// ================================================================

/** Lint 规则映射条目 */
data class LintRuleMapping(
    val lintRuleId: String,
    val lintRuleName: String,
    val targetSkillId: String?,
    val isEnabled: Boolean
)

data class LintIntegrationState(
    val lintMappings: List<LintRuleMapping> = emptyList(),
    val detektConfigPath: String = "detekt.yml",
    val isDirty: Boolean = false,
    val isSaving: Boolean = false
) {
    companion object {
        val Initial = LintIntegrationState()
    }
}

sealed interface LintIntegrationIntent {
    data object LoadMappings : LintIntegrationIntent()
    data class ToggleMapping(val lintRuleId: String, val enabled: Boolean) : LintIntegrationIntent()
    data class LinkToSkill(val lintRuleId: String, val skillId: String?) : LintIntegrationIntent()
    data object SaveSettings : LintIntegrationIntent()
}

sealed interface LintIntegrationEffect {
    data class ShowToast(val message: String) : LintIntegrationEffect()
    data object SettingsSaved : LintIntegrationEffect()
}

// ================================================================
// AgentSkillsState — 父级状态（组合所有子 Feature 状态）
// ================================================================

/**
 * Agent Skills 工具包主状态（Parent state composing all sub-features）
 * —————————————————————————————————————————————————————
 *
 * 采用组合模式（Composition Pattern）：
 * 父级状态包含所有子 Feature 的状态，ViewModel 统一管理。
 * UI 层根据当前导航 Tab 渲染对应子 Feature 的界面。
 *
 * @param currentTab 当前活跃的导航 Tab
 * @param skillEditor Skill 编辑器子状态
 * @param permissionDashboard 权限仪表盘子状态
 * @param skillMarket Skills 市场子状态
 * @param mySkills 我的 Skills 子状态
 * @param audit 审计视图子状态
 * @param lintIntegration Lint 联动设置子状态
 */
data class AgentSkillsState(
    val currentTab: AgentSkillsTab = AgentSkillsTab.MY_SKILLS,
    val skillEditor: SkillEditorState = SkillEditorState.Initial,
    val permissionDashboard: PermissionDashboardState = PermissionDashboardState.Initial,
    val skillMarket: SkillMarketState = SkillMarketState.Initial,
    val mySkills: MySkillsState = MySkillsState.Initial,
    val audit: AuditState = AuditState.Initial,
    val lintIntegration: LintIntegrationState = LintIntegrationState.Initial
) {
    companion object {
        val Initial = AgentSkillsState()
    }
}

/** Agent Skills 导航 Tab 枚举 */
enum class AgentSkillsTab(
    val displayName: String,
    val iconName: String,
    val description: String
) {
    MY_SKILLS("My Skills", "folder_special", "管理已安装的 Skills"),
    SKILL_EDITOR("Editor", "edit_document", "创建和编辑 Skill DSL"),
    MARKET("Market", "store", "浏览和安装市场 Skills"),
    PERMISSIONS("Permissions", "security", "配置 Agent 权限策略"),
    AUDIT("Audit", "history", "查看 Skills 执行审计日志"),
    LINT_SETTINGS("Lint Link", "link", "关联 Lint/Detekt 规则")
}

/** 顶层用户意图（Top-level intents dispatched from UI） */
sealed interface AgentSkillsIntent {
    data class SwitchTab(val tab: AgentSkillsTab) : AgentSkillsIntent()

    // Skill Editor Intents
    data class EditorSkillIntent(val intent: SkillEditorIntent) : AgentSkillsIntent()

    // Permission Dashboard Intents
    data class PermissionIntent(val intent: PermissionDashboardIntent) : AgentSkillsIntent()

    // Market Intents
    data class MarketIntent(val intent: SkillMarketIntent) : AgentSkillsIntent()

    // My Skills Intents
    data class MySkillsWrapper(val intent: MySkillsIntent) : AgentSkillsIntent()

    // Audit Intents
    data class AuditWrapper(val intent: AuditIntent) : AgentSkillsIntent()

    // Lint Integration Intents
    data class LintIntent(val intent: LintIntegrationIntent) : AgentSkillsIntent()
}

/** 顶层副作用 */
sealed interface AgentSkillsEffect {
    data class ShowToast(val message: String) : AgentSkillsEffect()
    data object NavigateToEditor : AgentSkillsEffect()
    data class SkillSaved(val skillId: String) : AgentSkillsEffect()
}
