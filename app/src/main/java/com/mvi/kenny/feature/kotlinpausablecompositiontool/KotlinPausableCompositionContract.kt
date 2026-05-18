package com.mvi.kenny.feature.kotlinpausablecompositiontool

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * KotlinPausableCompositionContract — Kotlin 2.2 + Pausable Composition Tool MVI Contract
 * ============================================================
 * MVI (Model-View-Intent) Architecture Pattern for the Kotlin 2.2 Context Parameters
 * and Compose Pausable Composition developer adaptation toolkit.
 *
 * MVI 三支柱：
 * - Model (State): Immutable data class, single source of truth for UI
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions, ViewModel processes and updates State
 *
 * Effect: One-time side effects (snackbar, navigation), delivered via Channel
 *
 * @see KotlinPausableCompositionViewModel State management
 * @see KotlinPausableCompositionScreen Main screen with 5 tabs
 */

// =============================================================
// BenefitLevel — 优化收益等级
// =============================================================
/**
 * Optimization benefit level for Composable scanning results.
 * Composable 扫描结果的优化收益等级
 *
 * @param label Display label / 显示标签
 * @param color Badge color / Badge 颜色
 */
enum class BenefitLevel(val label: String, val color: Color) {
    /** High benefit: Significant performance improvement expected / 高收益：预计显著性能提升 */
    HIGH("高", Color(0xFFCF6679)),

    /** Medium benefit: Moderate performance improvement expected / 中收益：预计中等性能提升 */
    MEDIUM("中", Color(0xFFFFAB40)),

    /** Low benefit: Minor or negligible performance improvement / 低收益：性能提升微小或可忽略 */
    LOW("低", Color(0xFF69F0AE))
}

// =============================================================
// ComposableInfo — Composable 扫描结果
// =============================================================
/**
 * Composable function scan result / Composable 函数扫描结果
 *
 * @param id Unique ID for this result / 唯一ID
 * @param name Composable function name / Composable 函数名
 * @param optimizationBenefit Estimated optimization benefit level / 预估优化收益等级
 * @param suggestion Optimization suggestion text / 优化建议文本
 */
data class ComposableInfo(
    val id: String,
    val name: String,
    val optimizationBenefit: BenefitLevel,
    val suggestion: String
)

// =============================================================
// MainState — 页面状态
// =============================================================
/**
 * Main State for Kotlin 2.2 + Pausable Composition Tool / 主状态
 *
 * Single source of truth for the entire tool UI.
 *
 * @param selectedTab Current selected tab index (0-4) / 当前选中的 Tab 索引
 * @param codeCopied Whether code was recently copied (triggers snackbar) / 代码是否刚被复制
 * @param scanInProgress Whether scan is currently running / 扫描是否进行中
 * @param scanResults List of Composable scan results / Composable 扫描结果列表
 * @param expandedCodeId ID of expanded code block (null = all collapsed) / 展开的代码块 ID
 * @param codeCopiedMessage The snackbar message shown when code is copied / 复制成功时显示的消息
 */
data class MainState(
    val selectedTab: Int = 0,
    val codeCopied: Boolean = false,
    val scanInProgress: Boolean = false,
    val scanResults: List<ComposableInfo> = emptyList(),
    val expandedCodeId: String? = null,
    val codeCopiedMessage: String = ""
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = MainState()
    }
}

// =============================================================
// MainIntent — 用户意图
// =============================================================
/**
 * Main User Intents / 主用户意图
 *
 * Every user action in the UI corresponds to an Intent.
 *
 * @see KotlinPausableCompositionViewModel.sendIntent Process all intents
 */
sealed interface MainIntent {
    /** Tab selected by user / 用户选中的 Tab
     * @param index Tab index (0-4) / Tab 索引
     */
    data class TabSelected(val index: Int) : MainIntent

    /** Copy code to clipboard / 复制代码到剪贴板
     * @param code Code text to copy / 要复制的代码文本
     * @param codeId Unique ID of the code block / 代码块唯一 ID
     */
    data class CopyCode(val code: String, val codeId: String) : MainIntent

    /** Start Composable scan / 开始 Composable 扫描 */
    data object StartScan : MainIntent

    /** Toggle code block expand/collapse / 切换代码块展开/折叠
     * @param codeId Unique ID of the code block / 代码块唯一 ID
     */
    data class ToggleCodeExpand(val codeId: String) : MainIntent

    /** Dismiss the "copied" snackbar / 关闭"已复制"提示
     */
    data object DismissCopiedSnackbar : MainIntent
}

// =============================================================
// MainEffect — 副作用
// =============================================================
/**
 * Main Side Effects / 主副作用
 *
 * One-time events, immutable, consumed only once.
 *
 * @see KotlinPausableCompositionViewModel Send via _effect.send()
 */
sealed interface MainEffect {
    /** Show snackbar with message / 显示带有消息的 Snackbar
     * @param message Snackbar message text / Snackbar 消息文本
     */
    data class ShowCopiedSnackbar(val message: String) : MainEffect
}
