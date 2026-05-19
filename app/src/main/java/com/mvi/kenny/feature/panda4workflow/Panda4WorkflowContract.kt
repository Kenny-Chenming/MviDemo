package com.mvi.kenny.feature.panda4workflow

/**
 * ============================================================
 * Panda4WorkflowContract — Android Studio Panda 4 AI 工作流工具包 MVI 契约
 * Panda4Workflow MVI Contract — Android Studio Panda 4 AI Workflow Toolkit
 * ============================================================
 *
 * PRD-259 | Android Studio Panda 4 AI 原生开发工作流工具包
 * Ref: memory/agency/designs/PRD-259-Android-Studio-Panda-4-AI工作流工具包.md
 *
 * MVI Architecture:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions; ViewModel executes logic on receiving Intent
 * - Effect: One-time side effects (Toast, Navigation) delivered via Channel
 * —————————————————————————————————————————————————————
 *
 * Bottom Tab Navigation (8 Tabs + 2 Tools):
 * - Tab 1: Overview / 总览
 * - Tab 2: Planning Mode Guide / Planning Mode 最佳实践
 * - Tab 3: Next Edit Prediction / NEP 深度解析
 * - Tab 4: Agent Web Search / Agent Web Search 使用指南
 * - Tab 5: Agent Mode Skills / Skills 开发指南
 * - Tab 6: Combined Workflow / Planning + NEP 组合工作流
 * - Tab 7: AI Norms / AI 原生开发规范白皮书
 * - Tab 8: Privacy & Security / NEP 隐私与代码安全
 * - Tool 1: vs 竞品对比工具
 * - Tool 2: Permission Guide / 权限管理指南
 */

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * Bottom navigation tab enumeration
 * 底部导航 Tab 枚举
 *
 * @param title Tab display title
 * @param iconName Material icon name
 */
enum class Panda4Tab(val title: String, val iconName: String) {
    OVERVIEW("总览", "AutoAwesome"),
    PLANNING_MODE("Planning", "Plan"),
    NEP("NEP", "AutoFix"),
    WEB_SEARCH("Web Search", "Search"),
    SKILLS("Skills", "Extension"),
    COMBINED("组合工作流", "Link"),
    NORMS("开发规范", "Policy"),
    PRIVACY("隐私安全", "Security"),
    COMPARISON("竞品对比", "Compare"),
    PERMISSION("权限管理", "Settings")
}

/**
 * Scenario data model for竞品对比 tool
 * 竞品对比场景数据模型
 *
 * @param id Scenario unique identifier
 * @param name Scenario display name / 场景名称
 * @param description Scenario description / 场景描述
 */
data class ComparisonScenario(
    val id: Int,
    val name: String,
    val description: String
)

/**
 * Comparison result data model
 * 竞品对比结果数据模型
 *
 * @param recommended Recommended tool / 推荐工具
 * @param reasoning Recommendation reasoning / 推荐理由
 * @param alternatives Alternative tools / 备选工具
 */
data class ComparisonResult(
    val recommended: String,
    val reasoning: String,
    val alternatives: List<String>
)

/**
 * Expandable card data model for documentation tabs
 * 可展开文档卡片数据模型
 *
 * @param id Card unique identifier
 * @param title Card title / 卡片标题
 * @param description Brief description / 简要描述
 * @param codeContent Code snippet / 代码片段
 * @param language Programming language (kotlin/xml)
 * @param isExpanded Expansion state / 是否展开
 */
data class DocCard(
    val id: Int,
    val title: String,
    val description: String,
    val codeContent: String,
    val language: String = "kotlin",
    val isExpanded: Boolean = false
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * Panda 4 Workflow Toolkit page state
 * Panda 4 AI 工作流工具包页面状态
 *
 * MVI Architecture: Model layer, holds all page state.
 * State is Immutable — each state change creates a new State object.
 *
 * @param selectedTab Current bottom navigation tab index / 当前选中的 Tab
 * @param expandedCards Set of expanded card indices / 已展开的卡片索引集合
 * @param selectedScenarios Set of selected scenario IDs for竞品对比 / 竞品对比选中的场景
 * @param comparisonResult Comparison result / 对比结果
 * @param error Error message, null means no error / 错误信息，null 表示无错误
 *
 * @see Panda4WorkflowIntent
 * @see Panda4WorkflowViewModel
 */
data class Panda4WorkflowState(
    val selectedTab: Int = 0,
    // Expansion state / 展开状态
    val expandedCards: Set<Int> = emptySet(),
    // Comparison tool state / 竞品对比状态
    val selectedScenarios: Set<Int> = emptySet(),
    val comparisonResult: ComparisonResult? = null,
    // Error state / 错误状态
    val error: String? = null
) {
    companion object {
        /** Initial / default state / 初始状态 */
        val Initial = Panda4WorkflowState()
    }
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * User intentions for Panda 4 Workflow Toolkit
 * Panda 4 AI 工作流工具包用户意图
 *
 * Every user interaction on the page corresponds to an Intent.
 * ViewModel receives Intent, executes business logic, then updates State.
 *
 * @see Panda4WorkflowViewModel.sendIntent
 */
sealed interface Panda4WorkflowIntent {

    /**
     * Switch bottom navigation tab / 切换底部 Tab
     *
     * @param index Target tab index / 目标 Tab 索引
     */
    data class SelectTab(val index: Int) : Panda4WorkflowIntent

    /**
     * Toggle expandable card expansion state
     * 切换可展开卡片的展开/收起状态
     *
     * @param cardId Card unique identifier / 卡片唯一标识
     */
    data class ToggleCard(val cardId: Int) : Panda4WorkflowIntent

    /**
     * Toggle comparison scenario selection
     * 切换竞品对比场景选中状态
     *
     * @param scenarioId Scenario unique identifier / 场景唯一标识
     */
    data class ToggleScenario(val scenarioId: Int) : Panda4WorkflowIntent

    /**
     * Run comparison tool to generate recommendation
     * 运行竞品对比工具生成推荐
     */
    data object RunComparison : Panda4WorkflowIntent

    /**
     * Copy text to clipboard (all tabs)
     * 复制文本到剪贴板
     *
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : Panda4WorkflowIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * One-time side effects for Panda 4 Workflow Toolkit
 * Panda 4 AI 工作流工具包副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect {}
 *
 * @see Panda4WorkflowViewModel
 */
sealed interface Panda4WorkflowEffect {

    /**
     * Show toast message / 显示 Toast
     *
     * @param message Toast message text / Toast 文本
     */
    data class ShowToast(val message: String) : Panda4WorkflowEffect

    /**
     * Copy text to system clipboard / 复制文本到剪贴板
     *
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : Panda4WorkflowEffect
}
