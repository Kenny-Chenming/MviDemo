package com.mvi.kenny.feature.bubbles

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.feature.bubbles.bubblebar.BubbleBarIntegrationScreen
import com.mvi.kenny.feature.bubbles.compliance.BubbleComplianceScreen
import com.mvi.kenny.feature.bubbles.components.BubblesComponentScreen
import com.mvi.kenny.feature.bubbles.intro.BubblesIntroScreen
import com.mvi.kenny.feature.bubbles.intro.BubblesGradleWizardScreen
import com.mvi.kenny.feature.bubbles.layoutpreview.BubbleLayoutPreviewScreen
import com.mvi.kenny.feature.bubbles.playground.BubblePlaygroundScreen
import com.mvi.kenny.feature.bubbles.sizing.BubblesSizeConfigScreen
import com.mvi.kenny.feature.bubbles.suitability.BubbleSuitabilityScreen
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * BubblesMainScreen — Android 17 App Bubbles 主屏幕
 * ============================================================
 * 浮动窗口开发者接入工具包主界面。
 *
 * 布局：Tab 式导航（8 个主 Tab）
 * - Bubbles 接入引导（Intro）
 * - Compose 组件库（Components）
 * - 场景模板（Templates）
 * - PRD-148: 多窗口合规检测（Compliance）
 * - PRD-148: 布局预览（Layout）
 * - PRD-148: Bubble Bar 集成（ BubbleBar）
 * - PRD-148: 适用性分析（Suitability）
 * - PRD-148: Playground
 *
 * @param onUpdateTopBar Update parent TopBar callback / 更新父 TopBar 回调
 * @param viewModel BubblesViewModel instance / BubblesViewModel 实例
 *
 * @see BubblesIntroScreen 引导首页
 * @see BubblesGradleWizardScreen Gradle 向导
 * @see BubblesComponentScreen 组件库
 * @see BubbleComplianceScreen 合规检测
 * @see BubbleLayoutPreviewScreen 布局预览
 * @see BubbleBarIntegrationScreen Bubble Bar 集成
 * @see BubbleSuitabilityScreen 适用性分析
 * @see BubblePlaygroundScreen Playground
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BubblesMainScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: BubblesViewModel = viewModel()
) {
    // Collect state from ViewModel / 从 ViewModel 收集状态
    val state by viewModel.state.collectAsState()

    // Update TopBar when tab changes / Tab 变化时更新 TopBar
    LaunchedEffect(state.selectedTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "App Bubbles · ${state.selectedTab.titleZh}",
                actions = emptyList()
            )
        )
    }

    // Listen for effects / 监听副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is BubblesEffect.ShowToast -> {
                    // Toast handling would go here / Toast 处理
                }
                is BubblesEffect.ShowError -> {
                    // Error handling would go here / 错误处理
                }
                is BubblesEffect.ConfigApplySuccess -> {
                    // Config success handling / 配置成功处理
                }
                is BubblesEffect.NavigateToComponentDetail -> {
                    // Navigate to component detail / 导航到组件详情
                }
                is BubblesEffect.NavigateToScenarioDemo -> {
                    // Navigate to scenario demo / 导航到场景 Demo
                }
                is BubblesEffect.OpenExternalLink -> {
                    // Open external link / 打开外部链接
                }
                is BubblesEffect.ShareCode -> {
                    // Share code / 分享代码
                }
                is BubblesEffect.ShowSnackbar -> {
                    // Snackbar handling would go here / Snackbar 处理
                }
                is BubblesEffect.ShareReport -> {
                    // Share report / 分享报告
                }
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row / Tab 栏
            BubblesTabRow(
                selectedTab = state.selectedTab,
                onTabSelected = { tab ->
                    viewModel.sendIntent(BubblesIntent.SelectTab(tab))
                }
            )

            // Content based on selected tab / 根据选中的 Tab 显示内容
            when (state.selectedTab) {
                // ========================================================
                // Tab: INTRO — 接入引导
                // ========================================================
                BubblesTab.INTRO -> {
                    if (state.wizardState.currentStep != WizardStep.MODULE_SELECT ||
                        state.wizardState.selectedModule.isNotEmpty()) {
                        BubblesGradleWizardScreen(
                            state = state.wizardState,
                            onIntent = { intent ->
                                when (intent) {
                                    is BubblesIntent.NextWizardStep ->
                                        viewModel.sendIntent(intent.copy(currentStep = state.wizardState.currentStep))
                                    is BubblesIntent.PrevWizardStep ->
                                        viewModel.sendIntent(intent.copy(currentStep = state.wizardState.currentStep))
                                    else -> viewModel.sendIntent(intent)
                                }
                            }
                        )
                    } else {
                        BubblesIntroScreen(
                            state = state.introState,
                            onIntent = viewModel::sendIntent
                        )
                    }
                }

                // ========================================================
                // Tab: COMPONENTS — 组件库
                // ========================================================
                BubblesTab.COMPONENTS -> {
                    BubblesComponentScreen(
                        state = state.componentsState,
                        onIntent = viewModel::sendIntent
                    )
                }

                // ========================================================
                // Tab: TEMPLATES — 场景模板
                // ========================================================
                BubblesTab.TEMPLATES -> {
                    BubblesSizeConfigScreen(
                        simulatorState = state.sizeSimulatorState,
                        templatesState = state.templatesState,
                        onSimulatorIntent = { intent ->
                            when (intent) {
                                is BubblesIntent.SelectSizePreset -> viewModel.sendIntent(intent)
                                is BubblesIntent.UpdateCustomSize -> viewModel.sendIntent(intent)
                                is BubblesIntent.UpdateTargetScreenSize -> viewModel.sendIntent(intent)
                                else -> {}
                            }
                        },
                        onTemplatesIntent = viewModel::sendIntent
                    )
                }

                // ========================================================
                // PRD-148: Tab: COMPLIANCE — 多窗口合规检测
                // ========================================================
                BubblesTab.COMPLIANCE -> {
                    BubbleComplianceScreen(viewModel = viewModel)
                }

                // ========================================================
                // PRD-148: Tab: LAYOUT — 浮窗自适应布局预览
                // ========================================================
                BubblesTab.LAYOUT -> {
                    BubbleLayoutPreviewScreen(viewModel = viewModel)
                }

                // ========================================================
                // PRD-148: Tab: BUBBLE_BAR — Bubble Bar 集成指南
                // ========================================================
                BubblesTab.BUBBLE_BAR -> {
                    BubbleBarIntegrationScreen(viewModel = viewModel)
                }

                // ========================================================
                // PRD-148: Tab: SUITABILITY — Activity 浮窗适用性分析
                // ========================================================
                BubblesTab.SUITABILITY -> {
                    BubbleSuitabilityScreen(viewModel = viewModel)
                }

                // ========================================================
                // PRD-148: Tab: PLAYGROUND — Bubble 浮窗体验 Playground
                // ========================================================
                BubblesTab.PLAYGROUND -> {
                    BubblePlaygroundScreen(viewModel = viewModel)
                }
            }
        }
    }
}

/**
 * ============================================================
 * BubblesTabRow — Tab 栏组件
 * ============================================================
 * Material 3 Tab Row with Bubbles color scheme.
 * Material 3 Tab 栏，采用 Bubbles 配色。
 *
 * @param selectedTab Currently selected tab / 当前选中的 Tab
 * @param onTabSelected Callback when tab is selected / Tab 选择回调
 */
@Composable
private fun BubblesTabRow(
    selectedTab: BubblesTab,
    onTabSelected: (BubblesTab) -> Unit
) {
    val tabs = BubblesTab.entries

    // Animate selected tab indicator / 动画选中的 Tab 指示器
    val selectedTabIndex = tabs.indexOf(selectedTab)
    val indicatorColor by animateColorAsState(
        targetValue = when (selectedTab) {
            BubblesTab.INTRO, BubblesTab.LAYOUT, BubblesTab.PLAYGROUND -> BubblesColors.Primary
            BubblesTab.COMPONENTS, BubblesTab.SUITABILITY -> BubblesColors.BubblesActive
            BubblesTab.TEMPLATES, BubblesTab.COMPLIANCE -> BubblesColors.PiPColor
            BubblesTab.BUBBLE_BAR -> BubblesColors.Tertiary
        },
        label = "tab_indicator_color"
    )

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.titleZh,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}
