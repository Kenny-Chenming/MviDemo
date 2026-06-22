package com.mvi.kenny.feature.continueon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================
// ContinueOnContract — Android 17 Continue On (Handoff) API 开发工具包
// MVI Contract / MVI 契约
// ============================================================
// PRD-296 | Android 17 Continue On (Handoff) API Dev Toolkit
//
// MVI Architecture Pattern / MVI 架构模式:
// - Model (State): Immutable data class — single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, etc.) via Channel
//
// 4 Tab Modules / 4个Tab模块:
// 1. Guide    — 完整开发指南 + 代码示例
// 2. Toolkit  — 4个子模块工具入口卡片
// 3. Scenarios — 交互式决策树 + 10个预置场景
// 4. Reference — API差异表/权限清单/错误码/FAQ

// ============================================================
// ContinueOnTab — Tab枚举
// ============================================================
/**
 * Tab type / Tab类型
 *
 * @param titleZh Chinese title / 中文标题
 * @param titleEn English title / 英文标题
 */
enum class ContinueOnTab(val titleZh: String, val titleEn: String) {
    GUIDE("指南", "Guide"),
    TOOLKIT("工具包", "Toolkit"),
    SCENARIOS("场景", "Scenarios"),
    REFERENCE("参考", "Reference")
}

// ============================================================
// ContinueOnTool — Toolkit子模块
// ============================================================
/**
 * Toolkit submodule / 工具包子模块
 *
 * @param id Unique identifier / 唯一标识
 * @param titleZh Chinese title / 中文标题
 * @param titleEn English title / 英文标题
 * @param description Description / 描述
 * @param iconName Material icon name / 图标名称
 */
data class ContinueOnTool(
    val id: String,
    val titleZh: String,
    val titleEn: String,
    val description: String,
    val iconName: String
)

// ============================================================
// DecisionNode — 决策树节点
// ============================================================
/**
 * Decision tree node / 决策树节点
 *
 * @param id Node ID / 节点ID
 * @param questionZh Chinese question / 中文问题
 * @param questionEn English question / 英文问题
 * @param children Child nodes / 子节点
 * @param recommendation Recommendation text / 推荐方案
 * @param isExpanded Whether expanded / 是否展开
 */
data class DecisionNode(
    val id: String,
    val questionZh: String,
    val questionEn: String,
    val children: List<DecisionNode> = emptyList(),
    val recommendation: String? = null,
    val isExpanded: Boolean = false
)

// ============================================================
// Scenario — 预置场景
// ============================================================
/**
 * Pre-built scenario / 预置场景
 *
 * @param id Scenario ID / 场景ID
 * @param titleZh Chinese title / 中文标题
 * @param titleEn English title / 英文标题
 * @param description Description / 描述
 * @param appType App type / App类型
 * @param handoffType Handoff type / Handoff类型
 * @param keyPoints Key implementation points / 关键实现要点
 */
data class Scenario(
    val id: String,
    val titleZh: String,
    val titleEn: String,
    val description: String,
    val appType: String,
    val handoffType: String,
    val keyPoints: List<String>
)

// ============================================================
// CodeExample — 代码示例
// ============================================================
/**
 * Code example / 代码示例
 *
 * @param id Example ID / 示例ID
 * @param title Example title / 示例标题
 * @param kotlinCode Kotlin code / Kotlin代码
 * @param javaCode Java code / Java代码
 */
data class CodeExample(
    val id: String,
    val title: String,
    val kotlinCode: String,
    val javaCode: String
)

// ============================================================
// ContinueOnState — MVI State
// ============================================================
/**
 * MVI State / MVI状态
 *
 * @param currentTab Currently selected tab / 当前Tab
 * @param isDarkMode Dark mode enabled / 深色模式
 * @param isLoading Loading state / 加载状态
 * @param decisionTreeState Decision tree state / 决策树状态
 * @param guideExpandState Guide expansion state map / 指南展开状态
 * @param selectedLanguage Selected code language / 选中代码语言
 * @param selectedScenario Selected scenario / 选中场景
 * @param error Error message / 错误信息
 * @param snackbarMessage Snackbar message / 提示消息
 */
data class ContinueOnState(
    val currentTab: ContinueOnTab = ContinueOnTab.GUIDE,
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = false,
    val decisionTreeState: DecisionTreeState = DecisionTreeState(),
    val guideExpandState: Map<String, Boolean> = emptyMap(),
    val selectedLanguage: String = "Kotlin",
    val selectedScenario: Scenario? = null,
    val error: String? = null,
    val snackbarMessage: String? = null
) {
    companion object {
        val Initial = ContinueOnState()
    }
}

/**
 * Decision tree state / 决策树状态
 *
 * @param nodes All nodes / 所有节点
 * @param selectedNodeId Selected node ID / 选中节点ID
 * @param expandedNodeIds Expanded node IDs / 展开节点ID集合
 */
data class DecisionTreeState(
    val nodes: List<DecisionNode> = emptyList(),
    val selectedNodeId: String? = null,
    val expandedNodeIds: Set<String> = emptySet()
)

// ============================================================
// ContinueOnIntent — MVI Intent
// ============================================================
/**
 * MVI Intent / MVI意图
 *
 * Represents user intentions that the ViewModel processes.
 */
sealed class ContinueOnIntent {
    data object LoadGuide : ContinueOnIntent()
    data class SelectTab(val tab: ContinueOnTab) : ContinueOnIntent()
    data class ToggleGuideExpand(val key: String) : ContinueOnIntent()
    data class SelectDecisionNode(val nodeId: String) : ContinueOnIntent()
    data class ToggleDarkMode(val enabled: Boolean) : ContinueOnIntent()
    data class SelectLanguage(val lang: String) : ContinueOnIntent()
    data class CopyCode(val code: String) : ContinueOnIntent()
    data object ClearData : ContinueOnIntent()
    data class SelectScenario(val scenario: Scenario) : ContinueOnIntent()
    data object DismissSnackbar : ContinueOnIntent()
    data object DismissError : ContinueOnIntent()
}

// ============================================================
// ContinueOnEffect — MVI Effect
// ============================================================
/**
 * MVI Effect / MVI副作用
 *
 * One-time side effects delivered via Channel.
 */
sealed class ContinueOnEffect {
    data class ShowSnackbar(val message: String) : ContinueOnEffect()
    data object NavigateToSettings : ContinueOnEffect()
    data class ShareCode(val code: String) : ContinueOnEffect()
    data class CopyToClipboard(val text: String, val label: String) : ContinueOnEffect()
}
