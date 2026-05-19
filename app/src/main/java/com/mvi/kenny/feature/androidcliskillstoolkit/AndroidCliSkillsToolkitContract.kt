package com.mvi.kenny.feature.androidcliskillstoolkit

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * AndroidCliSkillsToolkitContract — Android CLI + Skills AI Agent 开发工作流工具包 MVI 契约
 * AndroidCliSkillsToolkit MVI Contract — Android CLI + Skills AI Agent Development Workflow Toolkit
 * ============================================================
 *
 * PRD-260 | Android CLI + Skills AI Agent 开发工作流工具包
 * Ref: memory/agency/designs/PRD-260-Android-CLI-Skills-AI-Agent开发工作流工具包.md
 *
 * MVI Architecture:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions; ViewModel executes logic on receiving Intent
 * - Effect: One-time side effects (Toast, Navigation, Clipboard) delivered via Channel
 * —————————————————————————————————————————————————————
 *
 * 5 Tab Layout:
 * - Tab 1: 上手指南 (Getting Started) — Android CLI 安装/配置 + 第一个 AI 构建应用
 * - Tab 2: Skills 规范 (Skills Specs) — Android Skills 编写规范 + 自定义 Skill 开发
 * - Tab 3: LLM 集成 (LLM Integration) — CLI × Claude/GPT/Gemini 集成指南 + Vibe Coding 实战
 * - Tab 4: 工程工具 (Engineering Tools) — KB 查询 + CI/CD 集成 + Studio 协同工作流
 * - Tab 5: 生态广场 (Ecosystem) — Skills 质量评估 + 社区分享平台
 */

// ============================================================
// Color Palette / 配色方案
// ============================================================

/**
 * Android CLI Skills Toolkit color palette
 * 紫蓝渐变主题，体现 AI 工具属性
 */
object AndroidCliColors {
    val Primary = Color(0xFF7C4DFF)        // 紫色主色 / Purple primary
    val PrimaryVariant = Color(0xFF448AFF) // 蓝色变体 / Blue variant
    val Secondary = Color(0xFF00E5FF)       // 青色 / Cyan
    val Tertiary = Color(0xFF76FF03)       // 绿色 / Green
    val Warning = Color(0xFFFF9800)          // 橙色警告 / Orange warning
    val Error = Color(0xFFFF5252)            // 红色错误 / Red error
    val Background = Color(0xFF121212)     // 深色背景 / Dark background
    val Surface = Color(0xFF1E1E1E)         // 卡片背景 / Card background
    val SurfaceVariant = Color(0xFF2D2D2D)  // 次级表面 / Surface variant
    val CodeBackground = Color(0xFF0D1117)  // 代码块背景 / Code block background
    val TextPrimary = Color(0xFFE0E0E0)    // 主要文本 / Primary text
    val TextSecondary = Color(0xFF9E9E9E)   // 次要文本 / Secondary text
    val Divider = Color(0xFF3D3D3D)        // 分隔线 / Divider
    val TabIndicator = Color(0xFF7C4DFF)   // Tab 指示器 / Tab indicator
}

// ============================================================
// Tab Enum / Tab 枚举
// ============================================================

/**
 * Tab enumeration for Android CLI Skills Toolkit
 * Android CLI Skills Toolkit Tab 枚举
 */
enum class AndroidCliTab(val title: String, val iconName: String) {
    GETTING_STARTED("上手指南", "Rocket"),
    SKILLS_SPECS("Skills规范", "Extension"),
    LLM_INTEGRATION("LLM集成", "AutoAwesome"),
    ENGINEERING_TOOLS("工程工具", "Build"),
    ECOSYSTEM("生态广场", "Groups")
}

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * Code snippet data model for display and copy functionality
 * 代码片段数据模型，用于展示和复制功能
 */
data class CodeSnippet(
    val id: String,
    val title: String,
    val language: String = "bash",
    val content: String
)

/**
 * Skill editor state for the built-in YAML/JSON editor
 * 内置 YAML/JSON 编辑器的技能编辑器状态
 */
data class SkillEditorState(
    val name: String = "",
    val description: String = "",
    val triggers: String = "",
    val contentYAML: String = "",
    val contentJSON: String = "",
    val activeFormat: String = "yaml"
)

/**
 * CLI command output entry for the command simulator
 * CLI 命令模拟器的命令输出条目
 */
data class CliOutputEntry(
    val command: String,
    val output: String,
    val isError: Boolean = false
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * Android CLI Skills Toolkit page state
 * Android CLI + Skills AI Agent 开发工作流工具包页面状态
 */
data class AndroidCliSkillsToolkitState(
    val selectedTab: Int = 0,
    // Copy feedback / 复制反馈
    val copiedSnippetId: String? = null,
    // CLI simulator / CLI 模拟器
    val cliCommandInput: String = "",
    val cliOutputEntries: List<CliOutputEntry> = emptyList(),
    // Skill editor / Skill 编辑器
    val skillEditor: SkillEditorState = SkillEditorState(),
    val isSkillEditorVisible: Boolean = false,
    // Expandable cards / 可展开卡片
    val expandedCardIds: Set<String> = emptySet()
) {
    companion object {
        /** Initial / default state / 初始状态 */
        val Initial = AndroidCliSkillsToolkitState()
    }
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * User intentions for Android CLI Skills Toolkit
 * Android CLI + Skills AI Agent 开发工作流工具包用户意图
 */
sealed interface AndroidCliSkillsToolkitIntent {
    data class SelectTab(val index: Int) : AndroidCliSkillsToolkitIntent
    data class CopySnippet(val snippetId: String, val content: String) : AndroidCliSkillsToolkitIntent
    data object ClearCopyFeedback : AndroidCliSkillsToolkitIntent
    data class UpdateCliCommand(val command: String) : AndroidCliSkillsToolkitIntent
    data object ExecuteCliCommand : AndroidCliSkillsToolkitIntent
    data object ClearCliOutput : AndroidCliSkillsToolkitIntent
    data class UpdateSkillEditor(val editor: SkillEditorState) : AndroidCliSkillsToolkitIntent
    data object ToggleSkillEditor : AndroidCliSkillsToolkitIntent
    data class ToggleCard(val cardId: String) : AndroidCliSkillsToolkitIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * One-time side effects for Android CLI Skills Toolkit
 * Android CLI + Skills AI Agent 开发工作流工具包副作用
 */
sealed interface AndroidCliSkillsToolkitEffect {
    data class ShowToast(val message: String) : AndroidCliSkillsToolkitEffect
    data class CopyToClipboard(val text: String) : AndroidCliSkillsToolkitEffect
    data class OpenUrl(val url: String) : AndroidCliSkillsToolkitEffect
}
