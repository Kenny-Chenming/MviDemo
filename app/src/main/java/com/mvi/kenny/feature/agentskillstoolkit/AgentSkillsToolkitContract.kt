package com.mvi.kenny.feature.agentskillstoolkit

// ================================================================
// AgentSkillsToolkitContract — Android Agent Skills 技能库生态工具包
// ================================================================
// MVI Contract for Android Agent Skills Ecosystem Toolkit.
//
// PRD-233: Android Agent Skills 技能库生态工具包
// Google 2026年4月推出 Android Agent Skills 生态系统
//
// 5-Tab Architecture:
//   Tab 0: SkillCreationScreen  — Skill 创作指南 + 模板选择器
//   Tab 1: SkillValidationScreen — Skill 验证扫描器 + 修复建议
//   Tab 2: CliGuideScreen       — CLI 命令教程 + 导入导出工具
//   Tab 3: KbCiScreen          — Knowledge Base 查询 + CI 插件配置
//   Tab 4: ShareEfficiencyScreen — 分享平台 + 效能评估
//
// MVI 三要素 / Three pillars:
//   State  — Immutable UI state, single source of truth
//   Intent — User intentions processed by ViewModel
//   Effect — One-time side effects via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// Tab 0: Skill Templates — 创作指南 + 模板库
// ================================================================

/**
 * SkillTemplate — Android Skill 模板数据模型
 * Android Skill 模板，包含格式规范和示例内容
 */
data class SkillTemplate(
    val id: String,
    val name: String,
    val nameCn: String,
    val description: String,
    val descriptionCn: String,
    val category: SkillCategory,
    val content: String,        // Full Skill markdown content
    val usageCount: Int = 0,
    val rating: Float = 0f
)

/**
 * SkillCategory — Skill 分类标签
 */
enum class SkillCategory(val label: String, val labelCn: String) {
    ROOM("Room / Database", "Room 数据库"),
    COMPOSE("Jetpack Compose", "Jetpack Compose"),
    AGP("AGP / Gradle", "AGP / Gradle"),
    TESTING("Testing", "测试"),
    KSP("KSP / Annotation", "KSP / 注解处理"),
    ARCH("Architecture", "架构"),
    OTHER("Other", "其他")
}

// ================================================================
// Tab 1: Validation Scanner — 验证扫描器
// ================================================================

/**
 * ValidationPhase — 扫描进度阶段
 */
enum class ValidationPhase {
    IDLE, PARSING, ANALYZING, GENERATING_REPORT, COMPLETED, ERROR
}

/**
 * ValidationResult — Skill 文件验证结果
 */
data class ValidationResult(
    val id: String,
    val file: String,
    val line: Int,
    val severity: ValidationSeverity,
    val category: ValidationCategory,
    val message: String,
    val messageCn: String,
    val fixSuggestion: String,
    val fixSuggestionCn: String
)

/**
 * ValidationSeverity — 验证结果严重程度
 */
enum class ValidationSeverity(val color: Long) {
    ERROR(0xFFF85149),    // #F85149 — 红色，致命问题
    WARNING(0xFFD29922),  // #D29922 — 黄色，建议修复
    INFO(0xFF58A6FF)     // #58A6FF — 蓝色，参考信息
}

/**
 * ValidationCategory — 验证问题分类
 */
enum class ValidationCategory(val label: String, val labelCn: String) {
    FRONTMATTER("Frontmatter", "元数据区"),
    DESCRIPTION("Description Section", "描述区域"),
    TRIGGER("Trigger Section", "触发条件区域"),
    INSTRUCTIONS("Instructions Section", "指令区域"),
    EXAMPLES("Examples Section", "示例区域"),
    OUTPUTS("Outputs Section", "输出区域"),
    ERROR_HANDLING("Error Handling Section", "错误处理区域"),
    FORMAT("Format", "格式规范")
}

// ================================================================
// Tab 2: CLI Guide — CLI 命令教程
// ================================================================

/**
 * CliSection — CLI 子章节
 */
enum class CliSection(val command: String, val title: String, val titleCn: String) {
    LIST("list", "List Skills", "列出 Skills"),
    INSTALL("install", "Install Skill", "安装 Skill"),
    PUBLISH("publish", "Publish Skill", "发布 Skill"),
    UNINSTALL("uninstall", "Uninstall Skill", "卸载 Skill"),
    CONFIG("config", "Configure Registry", "配置私有 Registry")
}

/**
 * CliCommand — CLI 命令详情
 */
data class CliCommand(
    val section: CliSection,
    val command: String,
    val description: String,
    val descriptionCn: String,
    val examples: List<Pair<String, String>>,  // (command, description)
    val examplesCn: List<Pair<String, String>>
)

// ================================================================
// Tab 3: Knowledge Base + CI — 知识库查询 + CI 插件配置
// ================================================================

/**
 * KbResult — Knowledge Base 查询结果
 */
data class KbResult(
    val id: String,
    val title: String,
    val titleCn: String,
    val snippet: String,
    val snippetCn: String,
    val url: String,
    val relevance: Float  // 0-1 relevance score
)

/**
 * CiStep — CI 配置步骤
 */
data class CiStep(
    val index: Int,
    val title: String,
    val titleCn: String,
    val description: String,
    val descriptionCn: String,
    val codeSnippet: String,
    val codeSnippetLanguage: String  // "groovy" or "kotlin"
)

// ================================================================
// Tab 4: Share + Efficiency — 分享平台 + 效能评估
// ================================================================

/**
 * SharedSkill — 分享平台的 Skill 条目
 */
data class SharedSkill(
    val id: String,
    val name: String,
    val nameCn: String,
    val description: String,
    val descriptionCn: String,
    val author: String,
    val category: SkillCategory,
    val downloadCount: Int,
    val rating: Float,
    val tags: List<String>
)

/**
 * EfficiencyInput — 效能评估输入
 */
data class EfficiencyInput(
    val skillPath: String = "",
    val taskDescription: String = ""
)

/**
 * EfficiencyReport — 效能评估报告
 */
data class EfficiencyReport(
    val skillName: String,
    val taskCompletionRate: Float,    // 0-100%
    val avgTimeMinutes: Float,
    val accuracyScore: Float,         // 0-100%
    val coverageScore: Float,         // 0-100%
    val overallScore: Float            // 0-100%
)

// ================================================================
// MVI State — 主状态数据类
// ================================================================

/**
 * AgentSkillsToolkitState — 完整 UI 状态
 * All UI state for the Android Agent Skills Toolkit.
 */
data class AgentSkillsToolkitState(
    // Navigation
    val selectedTab: Int = 0,

    // Tab 0: Skill Creation
    val templates: List<SkillTemplate> = emptyList(),
    val selectedTemplate: SkillTemplate? = null,
    val isGenerating: Boolean = false,
    val generationSuccess: Boolean = false,

    // Tab 1: Validation
    val validationPath: String = "",
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val validationPhase: ValidationPhase = ValidationPhase.IDLE,
    val validationResults: List<ValidationResult> = emptyList(),
    val expandedResultId: String? = null,

    // Tab 2: CLI Guide
    val cliSection: CliSection = CliSection.LIST,
    val cliCommands: List<CliCommand> = emptyList(),

    // Tab 3: KB + CI
    val kbQuery: String = "",
    val kbResults: List<KbResult> = emptyList(),
    val kbSelectedResult: KbResult? = null,
    val isSearchingKb: Boolean = false,
    val ciSteps: List<CiStep> = emptyList(),
    val completedSteps: Set<Int> = emptySet(),

    // Tab 4: Share + Efficiency
    val shareFilter: SkillCategory? = null,
    val sharedSkills: List<SharedSkill> = emptyList(),
    val efficiencyInput: EfficiencyInput = EfficiencyInput(),
    val efficiencyReport: EfficiencyReport? = null,
    val isGeneratingReport: Boolean = false
)

// ================================================================
// MVI Intent — 用户意图
// ================================================================

/**
 * AgentSkillsToolkitIntent — 所有用户操作意图
 * Sealed class representing all possible user actions.
 */
sealed class AgentSkillsToolkitIntent {
    // Navigation / Tab
    data class SelectTab(val index: Int) : AgentSkillsToolkitIntent()

    // Tab 0: Skill Creation
    data class SelectTemplate(val template: SkillTemplate) : AgentSkillsToolkitIntent()
    data object GenerateSkillFile : AgentSkillsToolkitIntent()
    data object ClearGenerationState : AgentSkillsToolkitIntent()

    // Tab 1: Validation
    data class UpdateValidationPath(val path: String) : AgentSkillsToolkitIntent()
    data object StartScan : AgentSkillsToolkitIntent()
    data class ToggleResultExpanded(val resultId: String) : AgentSkillsToolkitIntent()

    // Tab 2: CLI
    data class SelectCliSection(val section: CliSection) : AgentSkillsToolkitIntent()

    // Tab 3: KB + CI
    data class UpdateKbQuery(val query: String) : AgentSkillsToolkitIntent()
    data object SearchKnowledgeBase : AgentSkillsToolkitIntent()
    data class SelectKbResult(val result: KbResult) : AgentSkillsToolkitIntent()
    data class ToggleCiStep(val index: Int) : AgentSkillsToolkitIntent()

    // Tab 4: Share + Efficiency
    data class FilterShareSkills(val category: SkillCategory?) : AgentSkillsToolkitIntent()
    data class UpdateEfficiencyInput(val input: EfficiencyInput) : AgentSkillsToolkitIntent()
    data object GenerateEfficiencyReport : AgentSkillsToolkitIntent()
}

// ================================================================
// MVI Effect — 副作用
// ================================================================

/**
 * AgentSkillsToolkitEffect — 一次性副作用
 * One-time side effects emitted via Channel.
 */
sealed class AgentSkillsToolkitEffect {
    data class ShowSnackbar(val message: String) : AgentSkillsToolkitEffect()
    data class CopyToClipboard(val text: String) : AgentSkillsToolkitEffect()
    data class OpenUrl(val url: String) : AgentSkillsToolkitEffect()
    data class ShowSkillPreview(val template: SkillTemplate) : AgentSkillsToolkitEffect()
    data class ShowFixSuggestion(val result: ValidationResult) : AgentSkillsToolkitEffect()
}
