package com.mvi.kenny.feature.createmywidget

// ================================================================
// CreateMyWidgetContract — Android 17 Create My Widget MVI 契约
// ================================================================
// MVI Architecture Contract for Android 17 Create My Widget Generative UI Toolkit.
//
// PRD-263: Android 17 Create My Widget 生成式 UI 开发工具包
// Design Reference: memory/agency/designs/PRD-263-Android-17-Create-My-Widget生成式UI开发工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View            — Composable function, consumes State, renders UI
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
// ================================================================

/**
 * ============================================================
 * ToolPriority — 工具优先级枚举
 * ============================================================
 *
 * @param label 显示标签
 */
enum class ToolPriority(val label: String, val colorHex: Long) {
    P1("P1 · 必备", 0xFFE53935),
    P2("P2 · 推荐", 0xFF1E88E5)
}

/**
 * ============================================================
 * ToolCategory — 工具分类枚举
 * ============================================================
 *
 * @param label 中文显示名称
 */
enum class ToolCategory(val label: String) {
    API_SPEC("API 规范"),
    DIFFERENTIATION("差异化策略"),
    DATA_BINDING("数据绑定"),
    CI_TOOL("CI 工具"),
    DESIGN("设计规范"),
    GOOGLEBOOK("Googlebook"),
    CAPABILITY("Capability 声明"),
    ROADMAP("演进路线图")
}

/**
 * ============================================================
 * CreateMyWidgetTool — 工具数据模型
 * ============================================================
 *
 * @param id 工具唯一标识 (1-8)
 * @param name 工具名称
 * @param nameEn 英文名称
 * @param description 一句话描述
 * @param priority 优先级
 * @param category 分类
 * @param isNew 是否为新工具
 * @param androidVersion 对应 Android 版本
 * @param estimatedReadTime 预估阅读时间（分钟）
 */
data class CreateMyWidgetTool(
    val id: String,
    val name: String,
    val nameEn: String,
    val description: String,
    val priority: ToolPriority,
    val category: ToolCategory,
    val isNew: Boolean,
    val androidVersion: String = "Android 17+",
    val estimatedReadTime: Int = 5
)

/**
 * ============================================================
 * CreateMyWidgetState — 页面状态（MVI State）
 * ================================================================
 *
 * @param overviewSelectedTool 当前选中的工具 ID，null 表示显示总览
 * @param tools 全部 8 个工具列表
 * @param searchQuery 当前搜索关键词
 * @param filteredTools 搜索过滤后的工具列表
 * @param isLoading 加载状态
 * @param isSearching 是否处于搜索模式
 * @param errorMessage 错误信息
 *
 * @see CreateMyWidgetTool
 * @see CreateMyWidgetIntent
 */
data class CreateMyWidgetState(
    val overviewSelectedTool: String? = null,
    val tools: List<CreateMyWidgetTool> = defaultTools,
    val searchQuery: String = "",
    val filteredTools: List<CreateMyWidgetTool> = defaultTools,
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        /** Initial / default state */
        val Initial = CreateMyWidgetState()
    }
}

/**
 * ============================================================
 * CreateMyWidgetIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see CreateMyWidgetViewModel.sendIntent handles all Intents
 */
sealed interface CreateMyWidgetIntent {

    /** 加载全部工具 / Load all tools */
    data object LoadTools : CreateMyWidgetIntent

    /** 选择工具进入详情 / Select a tool to view detail
     * @param toolId 工具 ID
     */
    data class SelectTool(val toolId: String) : CreateMyWidgetIntent

    /** 返回总览页面 / Navigate back to overview */
    data object NavigateBack : CreateMyWidgetIntent

    /** 搜索工具 / Search tools
     * @param query 搜索关键词
     */
    data class SearchTools(val query: String) : CreateMyWidgetIntent

    /** 复制代码 / Copy code to clipboard
     * @param code 要复制的代码
     * @param label 代码标签（用于 Toast）
     */
    data class CopyCode(val code: String, val label: String) : CreateMyWidgetIntent

    /** 清除错误 / Clear error message */
    data object ClearError : CreateMyWidgetIntent
}

/**
 * ============================================================
 * CreateMyWidgetEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see CreateMyWidgetViewModel _effect.send() sends Effects
 */
sealed interface CreateMyWidgetEffect {

    /** 显示 Toast 消息
     * @param message Toast 文本
     */
    data class ShowToast(val message: String) : CreateMyWidgetEffect

    /** 复制到剪贴板
     * @param code 要复制的代码
     * @param label 代码标签
     */
    data class CopyToClipboard(val code: String, val label: String) : CreateMyWidgetEffect

    /** 导航到工具详情
     * @param toolId 工具 ID
     */
    data class NavigateToDetail(val toolId: String) : CreateMyWidgetEffect

    /** 返回总览 */
    data object NavigateToOverview : CreateMyWidgetEffect
}

// ================================================================
// 默认工具数据 / Default Tools Data
// ================================================================

/**
 * ============================================================
 * defaultTools — 8 个默认工具列表
 * ============================================================
 */
val defaultTools = listOf(
    CreateMyWidgetTool(
        id = "1",
        name = "Widget API 规范指南",
        nameEn = "Widget API Specification",
        description = "Widget App 如何暴露 capability 给 Gemini，实现 Create My Widget 正确生成",
        priority = ToolPriority.P1,
        category = ToolCategory.API_SPEC,
        isNew = true,
        estimatedReadTime = 8
    ),
    CreateMyWidgetTool(
        id = "2",
        name = "Widget 差异化开发指南",
        nameEn = "Widget Differentiation Strategy",
        description = "AI 通用模板 vs 专业 Widget 的设计策略，让你的 Widget 在生成式 UI 时代脱颖而出",
        priority = ToolPriority.P1,
        category = ToolCategory.DIFFERENTIATION,
        isNew = true,
        estimatedReadTime = 6
    ),
    CreateMyWidgetTool(
        id = "3",
        name = "Widget 数据绑定规范",
        nameEn = "Widget Data Binding Spec",
        description = "Gemini 生成的 Widget 如何从 App 读取实时数据，含 RemoteViewsFactory 完整示例",
        priority = ToolPriority.P1,
        category = ToolCategory.DATA_BINDING,
        isNew = true,
        estimatedReadTime = 7
    ),
    CreateMyWidgetTool(
        id = "4",
        name = "Widget CI 验证工具",
        nameEn = "Widget CI Validator",
        description = "检测 Widget App 是否满足 Create My Widget 生成要求，输出合规报告",
        priority = ToolPriority.P1,
        category = ToolCategory.CI_TOOL,
        isNew = true,
        estimatedReadTime = 5
    ),
    CreateMyWidgetTool(
        id = "5",
        name = "生成式 UI Widget 设计规范白皮书",
        nameEn = "Generative UI Widget Design Whitepaper",
        description = "Gemini 生成的 Widget 的视觉/交互设计标准，与现有 Widget 的差异分析",
        priority = ToolPriority.P2,
        category = ToolCategory.DESIGN,
        isNew = true,
        estimatedReadTime = 10
    ),
    CreateMyWidgetTool(
        id = "6",
        name = "Googlebook 适配指南",
        nameEn = "Googlebook Adaptation Guide",
        description = "桌面场景下的 Widget 生成与展示，布局适配与桌面专属功能支持",
        priority = ToolPriority.P2,
        category = ToolCategory.GOOGLEBOOK,
        isNew = true,
        estimatedReadTime = 6
    ),
    CreateMyWidgetTool(
        id = "7",
        name = "开发者 Capability 声明规范",
        nameEn = "Capability Declaration Spec",
        description = "Widget 需要声明哪些 metadata 才能被 Gemini 正确理解，含完整 AndroidManifest 示例",
        priority = ToolPriority.P1,
        category = ToolCategory.CAPABILITY,
        isNew = true,
        estimatedReadTime = 7
    ),
    CreateMyWidgetTool(
        id = "8",
        name = "Android 生成式 UI 演进路线图",
        nameEn = "Generative UI Roadmap",
        description = "除了 Widget，Android 未来会生成哪些 UI 组件，官方路线图深度解读",
        priority = ToolPriority.P2,
        category = ToolCategory.ROADMAP,
        isNew = true,
        estimatedReadTime = 8
    )
)
