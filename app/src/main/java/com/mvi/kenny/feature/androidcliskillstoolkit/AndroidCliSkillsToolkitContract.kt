package com.mvi.kenny.feature.androidcliskillstoolkit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

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
 *
 * @property primary 紫蓝渐变主色 / Primary purple-blue gradient color
 * @property secondary 青色辅色 / Secondary cyan color
 * @property tertiary 绿色强调色 / Tertiary green accent
 * @property background 深色背景 / Dark background
 * @property surface 卡片背景色 / Card surface color
 * @property surfaceVariant 次级表面色 / Surface variant
 * @property codeBackground 代码块背景 / Code block background
 * @property textPrimary 主要文本 / Primary text
 * @property textSecondary 次要文本 / Secondary text
 * @property divider 分隔线颜色 / Divider color
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
 *
 * @param title Tab display title / Tab 显示标题
 * @param iconName Material icon name / Material 图标名
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
 *
 * @param id Unique identifier / 唯一标识
 * @param title Snippet title / 片段标题
 * @param language Language type (bash/kotlin/yaml/json) / 语言类型
 * @param content Code content / 代码内容
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
 *
 * @param name Skill name / Skill 名称
 * @param description Skill description / Skill 描述
 * @param triggers Trigger keywords / 触发关键词
 * @param contentYAML YAML content / YAML 内容
 * @param contentJSON JSON content / JSON 内容
 * @param activeFormat Active format (yaml/json) / 当前格式
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
 *
 * @param command Input command / 输入的命令
 * @param output Command output / 命令输出
 * @param isError Whether the output is an error / 是否为错误输出
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
 *
 * MVI Architecture: Model layer, holds all page state.
 * State is Immutable — each state change creates a new State object.
 *
 * @param selectedTab Current selected tab index / 当前选中的 Tab 索引
 * @param copiedSnippetId Currently copied snippet ID (for UI feedback) / 当前已复制的片段 ID
 * @param cliCommandInput Current CLI command input / 当前 CLI 命令输入
 * @param cliOutputEntries List of CLI output entries / CLI 输出条目列表
 * @param skillEditor Skill editor state / Skill 编辑器状态
 * @param isSkillEditorVisible Whether the skill editor is visible / Skill 编辑器是否可见
 * @param expandedCardIds Set of expanded card IDs / 已展开的卡片 ID 集合
 *
 * @see AndroidCliSkillsToolkitIntent
 * @see AndroidCliSkillsToolkitViewModel
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
 *
 * Every user interaction on the page corresponds to an Intent.
 * ViewModel receives Intent, executes business logic, then updates State.
 *
 * @see AndroidCliSkillsToolkitViewModel.sendIntent
 */
sealed interface AndroidCliSkillsToolkitIntent {

    /**
     * Switch tab / 切换 Tab
     *
     * @param index Target tab index / 目标 Tab 索引
     */
    data class SelectTab(val index: Int) : AndroidCliSkillsToolkitIntent

    /**
     * Copy code snippet to clipboard / 复制代码片段到剪贴板
     *
     * @param snippetId Snippet unique identifier / 片段唯一标识
     * @param content Snippet content / 片段内容
     */
    data class CopySnippet(val snippetId: String, val content: String) : AndroidCliSkillsToolkitIntent

    /**
     * Clear copy feedback / 清除复制反馈
     */
    data object ClearCopyFeedback : AndroidCliSkillsToolkitIntent

    /**
     * Update CLI command input / 更新 CLI 命令输入
     *
     * @param command CLI command text / CLI 命令文本
     */
    data class UpdateCliCommand(val command: String) : AndroidCliSkillsToolkitIntent

    /**
     * Execute CLI command (simulated) / 执行 CLI 命令（模拟）
     */
    data object ExecuteCliCommand : AndroidCliSkillsToolkitIntent

    /**
     * Clear CLI output / 清空 CLI 输出
     */
    data object ClearCliOutput : AndroidCliSkillsToolkitIntent

    /**
     * Update skill editor content / 更新 Skill 编辑器内容
     *
     * @param editor Updated skill editor state / 更新后的 Skill 编辑器状态
     */
    data class UpdateSkillEditor(val editor: SkillEditorState) : AndroidCliSkillsToolkitIntent

    /**
     * Toggle skill editor visibility / 切换 Skill 编辑器可见性
     */
    data object ToggleSkillEditor : AndroidCliSkillsToolkitIntent

    /**
     * Toggle expandable card expansion state / 切换可展开卡片展开状态
     *
     * @param cardId Card unique identifier / 卡片唯一标识
     */
    data class ToggleCard(val cardId: String) : AndroidCliSkillsToolkitIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * One-time side effects for Android CLI Skills Toolkit
 * Android CLI + Skills AI Agent 开发工作流工具包副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect {}
 *
 * @see AndroidCliSkillsToolkitViewModel
 */
sealed interface AndroidCliSkillsToolkitEffect {

    /**
     * Show toast message / 显示 Toast
     *
     * @param message Toast message text / Toast 文本
     */
    data class ShowToast(val message: String) : AndroidCliSkillsToolkitEffect

    /**
     * Copy text to system clipboard / 复制文本到剪贴板
     *
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : AndroidCliSkillsToolkitEffect

    /**
     * Open external URL / 打开外部 URL
     *
     * @param url External URL to open / 要打开的外部 URL
     */
    data class OpenUrl(val url: String) : AndroidCliSkillsToolkitEffect
}
