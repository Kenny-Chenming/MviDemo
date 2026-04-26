package com.mvi.kenny.feature.agenticai

// ================================================================
// AgenticAIContract — Android Agentic AI AppFunctions MVI 契约
// ================================================================
// MVI architecture contract for Android Agentic AI AppFunctions & UI Automation Framework.
//
// PRD-169: Android Agentic AI AppFunctions & UI Automation Framework 开发工具包
// Design Reference: memory/agency/designs/PRD-169-Android-Agentic-AI-AppFunctions-Framework.md
//
// MVI 三要素 / Three pillars:
//   Model (State)   — Immutable page state, single source of truth
//   View            — Composable function, consumes State, renders UI
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * TabIndex — AgenticAI 9个Tab索引枚举
 * ============================================================
 *
 * @param title Tab 显示标题
 */
enum class AgenticTab(val title: String, val subtitle: String) {
    HOME("首页", "Agentic AI 全景"),
    APP_FUNCTIONS("AppFunctions", "函数定义与接入"),
    GEMINI_INTEGRATION("Gemini×AF", "集成模板"),
    UI_AUTOMATION("UI Automation", "UI自动化框架"),
    DESIGN_STANDARDS("设计规范", "Agentic设计原则"),
    SECURITY_PRIVACY("安全隐私", "权限与审计"),
    A2A_PROTOCOL("A2A协议", "Agent间通信"),
    CLI_SKILLS("CLI Skills", "命令行技能"),
    CI_COMPLIANCE("CI合规", "合规检测报告")
}

/**
 * ============================================================
 * PathNode — 路径图节点
 * ============================================================
 *
 * @param id Node unique ID
 * @param label Node display label
 * @param description Node description
 * @param targetTab Target tab to navigate to
 */
data class PathNode(
    val id: String,
    val label: String,
    val description: String,
    val targetTab: AgenticTab
)

/**
 * ============================================================
 * AppFunctionTemplate — AppFunctions 注册模板
 * ================================================================
 *
 * @param id Template ID
 * @param name Template name
 * @param category Category (e-commerce, food, travel, music)
 * @param description Template description
 * @param codeSnippet Template code
 */
data class AppFunctionTemplate(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val codeSnippet: String
)

/**
 * ============================================================
 * CodeExample — 代码示例
 * ============================================================
 *
 * @param title Example title
 * @param language Language (kotlin, json, xml)
 * @param code Code content
 * @param description Example description
 */
data class CodeExample(
    val title: String,
    val language: String,
    val code: String,
    val description: String
)

/**
 * ============================================================
 * DesignChecklistItem — 设计检查清单条目
 * ============================================================
 *
 * @param id Item ID
 * @param title Check item title
 * @param description Check item description
 * @param isChecked Whether item is checked
 * @param category Category (transparency, authorization, control)
 */
data class DesignChecklistItem(
    val id: String,
    val title: String,
    val description: String,
    val isChecked: Boolean = false,
    val category: String
)

/**
 * ============================================================
 * ComplianceCheckItem — CI合规检测条目
 * ============================================================
 *
 * @param id Check ID
 * @param title Check title
 * @param description Check description
 * @param status Status (pass, fail, warning)
 * @param suggestion Fix suggestion
 */
data class ComplianceCheckItem(
    val id: String,
    val title: String,
    val description: String,
    val status: ComplianceStatus,
    val suggestion: String
)

/**
 * ============================================================
 * ComplianceStatus — 合规状态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 * @param color Status color
 */
enum class ComplianceStatus(val displayName: String, val emoji: String, val color: Color) {
    PASS("通过", "✅", Color(0xFF4CAF50)),
    FAIL("失败", "❌", Color(0xFFF44336)),
    WARNING("警告", "⚠️", Color(0xFFFF9800)),
    SKIP("跳过", "⏭️", Color(0xFF9E9E9E))
}

/**
 * ============================================================
 * AgentCard — Agent Card 规范
 * ============================================================
 *
 * @param name Agent name
 * @param description Agent description
 * @param version Agent version
 * @param capabilities List of capabilities
 * @param skills List of skills
 */
data class AgentCard(
    val name: String,
    val description: String,
    val version: String,
    val capabilities: List<String>,
    val skills: List<String>
)

/**
 * ============================================================
 * SkillDefinition — CLI Skill 定义
 * ============================================================
 *
 * @param id Skill ID
 * @param name Skill name
 * @param description Skill description
 * @param commands List of CLI commands
 * @param category Skill category
 */
data class SkillDefinition(
    val id: String,
    val name: String,
    val description: String,
    val commands: List<String>,
    val category: String
)

/**
 * ============================================================
 * AgenticUIState — AgenticAI 页面状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 *
 * @param selectedTab Currently selected tab index
 * @param ioCountdown Google I/O 2026 countdown text
 * @param pathMapSelectedNode Currently selected path node ID
 * @param currentTemplate Currently selected template
 * @param complianceScore Overall compliance score (0-100)
 * @param complianceChecks List of compliance check items
 * @param checklistItems Design checklist items
 * @param agentCards List of Agent Cards
 * @param skillDefinitions List of CLI skill definitions
 * @param isLoading Whether data is loading
 * @param isDarkTheme Dark mode enabled
 *
 * @see AgenticTab
 */
data class AgenticUIState(
    val selectedTab: Int = 0,
    val ioCountdown: String = "",
    val pathMapSelectedNode: String? = null,
    val currentTemplate: AppFunctionTemplate? = null,
    val complianceScore: Int = 0,
    val complianceChecks: List<ComplianceCheckItem> = emptyList(),
    val checklistItems: List<DesignChecklistItem> = emptyList(),
    val agentCards: List<AgentCard> = emptyList(),
    val skillDefinitions: List<SkillDefinition> = emptyList(),
    val isLoading: Boolean = false,
    val isDarkTheme: Boolean = false
) {
    companion object {
        /** Initial/empty state */
        val Initial = AgenticUIState()
    }
}

/**
 * ============================================================
 * AgenticIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see AgenticAIViewModel.sendIntent handles all Intents
 */
sealed interface AgenticIntent {

    /** 用户切换 Tab
     * @param index Tab index
     */
    data class SelectTab(val index: Int) : AgenticIntent

    /** 用户点击路径图节点
     * @param nodeId Node ID
     */
    data class SelectPathNode(val nodeId: String) : AgenticIntent

    /** 用户复制代码
     * @param code Code to copy
     */
    data class CopyCode(val code: String) : AgenticIntent

    /** 用户运行合规检测
     * @param packageName App package name
     */
    data class RunComplianceCheck(val packageName: String) : AgenticIntent

    /** 用户导出合规报告
     * @param format Export format (pdf, json, md)
     */
    data class ExportReport(val format: String) : AgenticIntent

    /** 用户选择模板
     * @param template Selected template
     */
    data class SelectTemplate(val template: AppFunctionTemplate) : AgenticIntent

    /** 用户切换设计清单条目勾选状态
     * @param itemId Item ID
     */
    data class ToggleChecklistItem(val itemId: String) : AgenticIntent

    /** 用户切换主题
     * @param isDark Dark mode enabled
     */
    data class ToggleTheme(val isDark: Boolean) : AgenticIntent
}

/**
 * ============================================================
 * AgenticEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see AgenticAIViewModel _effect.send() sends Effects
 */
sealed interface AgenticEffect {

    /** 显示 Toast 消息
     * @param message Toast 文本
     */
    data class ShowToast(val message: String) : AgenticEffect()

    /** 导航到指定 Tab
     * @param tabIndex Tab index
     */
    data class NavigateToTab(val tabIndex: Int) : AgenticEffect()

    /** 代码已复制到剪贴板
     */
    data object CodeCopied : AgenticEffect()

    /** 报告已导出
     * @param filePath 导出文件路径
     */
    data class ReportExported(val filePath: String) : AgenticEffect()
}

// ================================================================
// 辅助函数 / Helper Functions
// ================================================================

/**
 * 格式化倒计时字符串
 *
 * @param days 天数
 * @param hours 小时数
 * @param minutes 分钟数
 * @param seconds 秒数
 * @return 格式化倒计时字符串
 */
fun formatCountdown(days: Long, hours: Long, minutes: Long, seconds: Long): String =
    "$days 天 $hours 小时 $minutes 分 $seconds 秒"
