package com.mvi.kenny.feature.appfunctionsmcp

/**
 * ============================================================
 * AppFunctionsMcpContract — PRD-274 AppFunctions On-Device MCP 开发工具包
 * AppFunctions On-Device MCP Developer Toolkit MVI Contract
 * ============================================================
 *
 * Design Doc: memory/agency/designs/PRD-274-Android-AppFunctions-On-Device-MCP开发工具包.md
 * Status: ✅ Design Complete, Ready for Development
 *
 * MVI Architecture:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable functions, consume State, render UI
 * - Intent: User intentions; ViewModel executes business logic
 * - Effect: One-time side effects (Toast, Navigation) via Channel
 * —————————————————————————————————————————————————————
 *
 * 需求背景:
 * Google 发布 AppFunctions — Android 平台的 On-Device MCP 实现。
 * App 能在 OS 级别注册自身能力，供 Gemini 等 AI Agent 发现并调用，无需网络。
 * App 从"被动 AI 功能容器"变为"主动 AI 可发现工具节点"。
 *
 * Target Users:
 * - All Android developers wanting their apps discoverable by AI agents
 * - Cross-app workflow developers (AI orchestrates multiple apps)
 * - E-commerce/content/productivity app developers
 * - Android architects designing AppFunctions integration
 * - AI Agent framework researchers
 */

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * 文档章节枚举 — 覆盖 AppFunctions 开发工具包全部 10 个模块
 * Document chapter enumeration — covers all 10 modules of AppFunctions toolkit
 *
 * Each section has:
 * - id: Unique identifier (e.g., "0x00")
 * - titleCn: Chinese title
 * - titleEn: English title
 * - description: Brief description
 * - difficulty: Entry level (入门/进阶/高级)
 * - tags: Related technology tags
 */
enum class DocSection(
    val id: String,
    val titleCn: String,
    val titleEn: String,
    val description: String,
    val difficulty: String,
    val tags: List<String>
) {
    // 0x00: AppFunctions 概述与环境配置
    OVERVIEW("0x00", "AppFunctions 概述与环境配置", "Overview & Environment Setup",
        "AppFunctions 是什么、开发环境要求（Android 16/API 36）、快速开始 Hello AppFunction 示例",
        "入门", listOf("Android 16", "API 36", "Jetpack", "Setup")),

    // 0x01: AppFunction 注解与 KDoc 编写规范
    ANNOTATION("0x01", "AppFunction 注解与 KDoc 编写规范", "Annotation & KDoc Specification",
        "@AppFunction 注解完整用法、KDoc → AI 可理解描述映射、参数命名最佳实践",
        "入门", listOf("@AppFunction", "KDoc", "Annotation", "AI Description")),

    // 0x02: AppFunction 参数类型与返回值设计
    PARAM_TYPES("0x02", "AppFunction 参数类型与返回值设计", "Parameter Types & Return Values",
        "支持的基础类型与复合类型、Suspend 函数与 Flow 返回值、Kotlinx Serialization 复杂对象",
        "进阶", listOf("Kotlinx Serialization", "Suspend", "Flow", "Serializable")),

    // 0x03: AppFunctions 安全与权限配置
    SECURITY("0x03", "AppFunctions 安全与权限配置", "Security & Permissions",
        "EXECUTE_APP_FUNCTIONS 权限申请、用户确认机制设计、恶意调用防护策略",
        "进阶", listOf("EXECUTE_APP_FUNCTIONS", "Permission", "User Confirmation", "Security")),

    // 0x04: AppFunction 发现与执行机制
    DISCOVERY("0x04", "AppFunction 发现与执行机制", "Discovery & Execution",
        "OS Registry 工作原理、Gemini 查询流程、调用生命周期详解",
        "进阶", listOf("OS Registry", "Gemini", "Discovery", "Lifecycle")),

    // 0x05: AppFunctions × Deep Links × Intents 选型决策树
    DECISION_TREE("0x05", "AppFunctions × Deep Links × Intents 选型决策树", "Decision Tree: AppFunctions vs Deep Links vs Intents",
        "三种跨 App 机制对比表、何时用哪个（含场景对照）、共存架构设计",
        "入门", listOf("Deep Link", "Intent", "Cross-app", "Architecture")),

    // 0x06: AppFunction 版本管理与灰度发布
    VERSIONING("0x06", "AppFunction 版本管理与灰度发布", "Versioning & Rollout",
        "函数签名变更处理、向后兼容策略、注册表更新机制",
        "高级", listOf("Versioning", "Compatibility", "Rollout", "Registry")),

    // 0x07: AppFunctions CI/CD 测试工具
    CICD("0x07", "AppFunctions CI/CD 测试工具", "CI/CD Testing Tools",
        "AppFunction 可发现性测试、参数验证测试、错误处理测试",
        "进阶", listOf("CI/CD", "Testing", "Discovery Test", "Validation")),

    // 0x08: AppFunctions 企业合规与隐私
    COMPLIANCE("0x08", "AppFunctions 企业合规与隐私", "Enterprise Compliance & Privacy",
        "MDM 策略影响、隐私政策合规、GDPR 数据最小化",
        "高级", listOf("MDM", "GDPR", "Privacy", "Compliance")),

    // 0x09: AppFunctions × ADK for Android 协同架构
    ADK_INTEGRATION("0x09", "AppFunctions × ADK for Android 协同架构", "AppFunctions × ADK for Android Integration",
        "App 内置 Agent vs 外部 Agent 发现、AppFunctions 作为 MCP 节点",
        "高级", listOf("ADK", "Android", "MCP Node", "Agent"));
}

/**
 * 代码示例数据模型 — 每个文档章节中的代码示例
 * Code example data model — code examples within each chapter
 *
 * @param id 示例唯一标识
 * @param sectionId 所属章节 ID
 * @param title 示例标题
 * @param description 示例描述
 * @param code 示例代码内容
 * @param language 代码语言 (kotlin, xml, gradle)
 * @param requirements 运行要求（如"需要 Android 16+"）
 */
data class CodeExample(
    val id: String,
    val sectionId: String,
    val title: String,
    val description: String,
    val code: String,
    val language: String = "kotlin",
    val requirements: String = ""
)

/**
 * 信息卡片类型枚举
 * Info card type enumeration
 *
 * @param symbol Emoji 符号
 * @param backgroundColor 背景色 (hex)
 * @param borderColor 边框色 (hex)
 */
enum class InfoCardType(val symbol: String, val backgroundColor: String, val borderColor: String) {
    TIP("💡", "#E8F5E9", "#3FB950"),      // 提示卡
    WARNING("⚠️", "#FFF3E0", "#D29922"),  // 注意卡
    KEY("🔑", "#E3F2FD", "#4285F4"),     // 关键卡
    ERROR("❌", "#FFEBEE", "#F85149");    // 错误卡
}

/**
 * 信息卡片数据模型
 * Info card data model
 *
 * @param type 卡片类型
 * @param title 卡片标题
 * @param content 卡片内容
 */
data class InfoCard(
    val type: InfoCardType,
    val title: String,
    val content: String
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * AppFunctions On-Device MCP 开发工具包页面状态
 * AppFunctions On-Device MCP Developer Toolkit Page State
 *
 * MVI Architecture — State is immutable; each state change creates a new State object.
 *
 * @param currentSection 当前显示的章节
 * @param expandedSections 已展开的章节 ID 集合
 * @param codeExamples 当前章节的代码示例列表
 * @param selectedCodeExample 选中的代码示例（用于放大查看）
 * @param isLoading 是否正在加载内容
 * @param searchQuery 搜索查询字符串
 * @param filteredSections 搜索过滤后的章节列表（null 表示未搜索）
 * @param infoCards 当前章节的信息卡片列表
 * @param error 错误信息，null 表示无错误
 */
data class AppFunctionsMcpState(
    val currentSection: DocSection = DocSection.OVERVIEW,
    val expandedSections: Set<String> = setOf(DocSection.OVERVIEW.id),
    val codeExamples: List<CodeExample> = emptyList(),
    val selectedCodeExample: CodeExample? = null,
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filteredSections: List<DocSection>? = null,
    val infoCards: List<InfoCard> = emptyList(),
    val error: String? = null
) {
    companion object {
        /** 初始状态 / Initial state */
        val Initial = AppFunctionsMcpState()
    }

    /**
     * 当前是否处于搜索模式
     * Whether currently in search mode
     */
    val isInSearchMode: Boolean
        get() = !searchQuery.isBlank() && filteredSections != null
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * AppFunctions On-Device MCP 开发工具包用户意图
 * AppFunctions On-Device MCP Developer Toolkit User Intents
 *
 * Every user interaction on the page corresponds to an Intent.
 * ViewModel receives Intent, executes business logic, then updates State.
 *
 * @see AppFunctionsMcpViewModel.sendIntent processes all Intents
 */
sealed interface AppFunctionsMcpIntent {

    /**
     * 导航到指定章节
     * Navigate to a specific document section
     *
     * @param section 目标章节
     */
    data class NavigateToSection(val section: DocSection) : AppFunctionsMcpIntent

    /**
     * 展开/折叠章节
     * Expand or collapse a section
     *
     * @param sectionId 章节 ID
     */
    data class ToggleSectionExpand(val sectionId: String) : AppFunctionsMcpIntent

    /**
     * 展开所有章节
     * Expand all sections
     */
    data object ExpandAllSections : AppFunctionsMcpIntent

    /**
     * 折叠所有章节
     * Collapse all sections
     */
    data object CollapseAllSections : AppFunctionsMcpIntent

    /**
     * 选中代码示例（放大查看）
     * Select a code example for expanded view
     *
     * @param example 选中的代码示例，null 表示关闭
     */
    data class SelectCodeExample(val example: CodeExample?) : AppFunctionsMcpIntent

    /**
     * 搜索章节内容
     * Search within section content
     *
     * @param query 搜索关键词
     */
    data class SearchSections(val query: String) : AppFunctionsMcpIntent

    /**
     * 清除搜索
     * Clear search and return to normal view
     */
    data object ClearSearch : AppFunctionsMcpIntent

    /**
     * 加载章节内容（代码示例 + 信息卡片）
     * Load section content (code examples + info cards)
     *
     * @param sectionId 章节 ID
     */
    data class LoadSectionContent(val sectionId: String) : AppFunctionsMcpIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * AppFunctions On-Device MCP 开发工具包副作用
 * AppFunctions On-Device MCP Developer Toolkit Effects
 *
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}
 *
 * @see AppFunctionsMcpViewModel 中通过 _effect.send() 发送 Effect
 */
sealed interface AppFunctionsMcpEffect {

    /**
     * 显示 Toast 消息
     * Show a toast message
     *
     * @param message Toast 文本内容
     */
    data class ShowToast(val message: String) : AppFunctionsMcpEffect

    /**
     * 复制代码到剪贴板
     * Copy code to clipboard
     *
     * @param code 要复制的代码
     */
    data class CopyCodeToClipboard(val code: String) : AppFunctionsMcpEffect

    /**
     * 滚动到指定章节
     * Scroll to a specific section
     *
     * @param sectionId 章节 ID
     */
    data class ScrollToSection(val sectionId: String) : AppFunctionsMcpEffect
}
