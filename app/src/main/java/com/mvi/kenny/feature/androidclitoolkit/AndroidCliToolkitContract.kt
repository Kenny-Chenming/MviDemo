package com.mvi.kenny.feature.androidclitoolkit

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * AndroidCliToolkitContract — Android CLI 1.0 AI Agent 开发集成工具包 MVI 契约
 * AndroidCliToolkit MVI Contract — Android CLI 1.0 AI Agent Development Integration Toolkit
 * ============================================================
 *
 * PRD-259 | Android CLI 1.0 AI Agent 开发集成工具包
 * Android CLI 1.0 Stable 于 2026年5月19日 Google I/O 2026 正式发布
 *
 * MVI Architecture:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions; ViewModel executes logic on receiving Intent
 * - Effect: One-time side effects (Toast, Navigation, Clipboard) delivered via Channel
 *
 * 5 Tab Layout:
 * - Tab 1: 命令参考 (Command Reference) — `android studio` 子命令全集及用法
 * - Tab 2: Agent集成 (Agent Integration) — Claude Code / Codex / GPT 配置指南
 * - Tab 3: 语义重构 (Semantic Refactoring) — Semantic Symbol Resolution 最佳实践
 * - Tab 4: Preview渲染 (Preview Rendering) — Compose Preview CI 集成指南
 * - Tab 5: Journeys测试 (Journeys Testing) — UI 测试框架完全指南
 */

// ============================================================
// Color Palette / 配色方案
// ============================================================

/**
 * Android CLI Toolkit color palette
 * Material 3 + 工具型产品风格，代码高亮突出
 */
object AndroidCliColors {
    val Primary = Color(0xFF1E88E5)         // Android Blue / Android 蓝
    val PrimaryVariant = Color(0xFF1565C0)   // 深蓝 / Dark blue
    val Secondary = Color(0xFF00ACC1)       // 青色 / Cyan
    val Background = Color(0xFF121212)      // 深色背景 / Dark background
    val Surface = Color(0xFF1E1E1E)           // 卡片背景（Monokai风格）/ Card background (Monokai style)
    val SurfaceVariant = Color(0xFF2D2D2D)   // 次级表面 / Surface variant
    val CodeBackground = Color(0xFF1E1E1E)   // 代码块背景 / Code block background (Monokai #1E1E1E)
    val TextPrimary = Color(0xFFE0E0E0)      // 主要文本 / Primary text
    val TextSecondary = Color(0xFF9E9E9E)    // 次要文本 / Secondary text
    val Divider = Color(0xFF3D3D3D)         // 分隔线 / Divider
    val TabIndicator = Color(0xFF1E88E5)     // Tab 指示器 / Tab indicator
    val InactiveTab = Color(0xFF757575)      // 未激活 Tab / Inactive tab

    // Monokai code highlight colors / Monokai 代码高亮颜色
    val CodeKeyword = Color(0xFFF92672)      // 关键字 / Keyword (Monokai pink)
    val CodeCommand = Color(0xFF569CD6)       // 命令关键字 / Command keyword (Monokai blue)
    val CodeParam = Color(0xFF9CDCFE)         // 参数 / Parameter (Monokai light blue)
    val CodeString = Color(0xFFE6DB74)        // 字符串 / String (Monokai yellow)
    val CodeComment = Color(0xFF75715E)       // 注释 / Comment (Monokai gray)
    val CodeNormal = Color(0xFFF8F8F2)       // 普通文本 / Normal text (Monokai off-white)
    val CodeFunction = Color(0xFFA6E22E)      // 函数 / Function (Monokai green)
    val CodeNumber = Color(0xFFAE81FF)       // 数字 / Number (Monokai purple)
}

// ============================================================
// Tab Enum / Tab 枚举
// ============================================================

/**
 * Tab enumeration for Android CLI Toolkit
 * Android CLI Toolkit Tab 枚举
 */
enum class CliTab(val title: String, val iconName: String) {
    COMMAND_REFERENCE("命令参考", "Terminal"),
    AGENT_INTEGRATION("Agent集成", "SmartToy"),
    SEMANTIC_REFACTORING("语义重构", "Edit"),
    PREVIEW_RENDERING("Preview渲染", "Visibility"),
    JOURNEYS_TESTING("Journeys测试", "Test")
}

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * CLI command data model for command reference tab
 * CLI 命令数据模型，用于命令参考 Tab
 */
data class CliCommand(
    val id: String,
    val command: String,
    val description: String,
    val descriptionCn: String,
    val category: String,
    val versions: List<String>,           // Applicable versions / 适用版本
    val exampleUsage: String,
    val exampleOutput: String,
    val relatedCommands: List<String>
)

/**
 * Agent configuration data model for agent integration tab
 * Agent 配置数据模型，用于 Agent 集成 Tab
 */
data class AgentConfig(
    val id: String,
    val agentType: AgentType,
    val title: String,
    val titleCn: String,
    val installCommand: String,
    val configSteps: List<Pair<String, String>>,  // (English, Chinese) / (英文, 中文)
    val configExample: String,
    val gradleSyncNote: String,
    val gradleSyncNoteCn: String,
    val url: String
)

/**
 * Agent type enumeration
 * Agent 类型枚举
 */
enum class AgentType(val displayName: String, val displayNameCn: String) {
    CLAUDE_CODE("Claude Code", "Claude Code"),
    CODEX("Codex", "Codex"),
    GPT("GPT CLI", "GPT CLI")
}

/**
 * Semantic refactoring item model
 * 语义重构条目数据模型
 */
data class SemanticRefactorItem(
    val id: String,
    val title: String,
    val titleCn: String,
    val description: String,
    val descriptionCn: String,
    val codeExample: String,
    val limitations: String,
    val limitationsCn: String,
    val bestPractices: List<String>,
    val bestPracticesCn: List<String>
)

/**
 * Preview rendering item model
 * Preview 渲染条目数据模型
 */
data class PreviewRenderItem(
    val id: String,
    val title: String,
    val titleCn: String,
    val description: String,
    val descriptionCn: String,
    val ciCommand: String,
    val codeExample: String,
    val imageNote: String,
    val imageNoteCn: String
)

/**
 * Journeys test item model
 * Journeys 测试条目数据模型
 */
data class JourneysTestItem(
    val id: String,
    val title: String,
    val titleCn: String,
    val description: String,
    val descriptionCn: String,
    val testCode: String,
    val testFramework: String,
    val frameworkVersion: String
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * Android CLI Toolkit page state
 * Android CLI 1.0 AI Agent 开发集成工具包页面状态
 */
data class AndroidCliToolkitState(
    // Tab navigation / Tab 导航
    val selectedTab: Int = 0,

    // Command expansion / 命令展开状态
    val expandedCommandId: String? = null,

    // Version selector / 版本选择器
    val selectedVersion: String = "1.0.0",

    // Search / 搜索
    val searchQuery: String = "",

    // Loading state / 加载状态
    val isLoading: Boolean = false,

    // Data / 数据
    val commands: List<CliCommand> = emptyList(),
    val agentConfigs: List<AgentConfig> = emptyList(),
    val semanticRefactors: List<SemanticRefactorItem> = emptyList(),
    val previewRenderItems: List<PreviewRenderItem> = emptyList(),
    val journeysTestItems: List<JourneysTestItem> = emptyList(),

    // Copy feedback / 复制反馈
    val copiedSnippetId: String? = null,

    // Copy code block target / 复制目标代码块
    val copiedCodeBlock: String = ""
) {
    companion object {
        /** Initial / default state / 初始状态 */
        val Initial = AndroidCliToolkitState()
    }
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * User intentions for Android CLI Toolkit
 * Android CLI 1.0 AI Agent 开发集成工具包用户意图
 */
sealed interface AndroidCliToolkitIntent {
    // Tab navigation / Tab 导航
    data class SelectTab(val index: Int) : AndroidCliToolkitIntent

    // Command expansion / 命令展开
    data class ExpandCommand(val commandId: String) : AndroidCliToolkitIntent

    // Version selection / 版本选择
    data class SelectVersion(val version: String) : AndroidCliToolkitIntent

    // Search / 搜索
    data class Search(val query: String) : AndroidCliToolkitIntent

    // Copy code block / 复制代码块
    data class CopyCodeBlock(val codeBlockId: String, val content: String) : AndroidCliToolkitIntent

    // Clear copy feedback / 清除复制反馈
    data object ClearCopyFeedback : AndroidCliToolkitIntent

    // Navigate to install guide / 导航到安装指南
    data class NavigateToInstallGuide(val platform: String) : AndroidCliToolkitIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * One-time side effects for Android CLI Toolkit
 * Android CLI 1.0 AI Agent 开发集成工具包副作用
 */
sealed interface AndroidCliToolkitEffect {
    // Show copied toast / 显示复制成功 Toast
    data class ShowCopiedToast(val text: String) : AndroidCliToolkitEffect

    // Navigate to install guide / 导航到安装指南
    data class NavigateToInstallGuide(val platform: String) : AndroidCliToolkitEffect

    // Copy to clipboard / 复制到剪贴板
    data class CopyToClipboard(val text: String) : AndroidCliToolkitEffect
}
