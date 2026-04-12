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
import com.mvi.kenny.feature.bubbles.components.BubblesComponentScreen
import com.mvi.kenny.feature.bubbles.intro.BubblesIntroScreen
import com.mvi.kenny.feature.bubbles.intro.BubblesGradleWizardScreen
import com.mvi.kenny.feature.bubbles.sizing.BubblesSizeConfigScreen
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * BubblesMainScreen — Android 17 App Bubbles 主屏幕
 * ============================================================
 * 浮动窗口开发者接入工具包主界面。
 *
 * 布局：Tab 式导航（三个主 Tab）
 * - Bubbles 接入引导（Intro）
 * - Compose 组件库（Components）
 * - 场景模板（Templates）
 *
 * @param onUpdateTopBar Update parent TopBar callback / 更新父 TopBar 回调
 * @param viewModel BubblesViewModel instance / BubblesViewModel 实例
 *
 * @see BubblesIntroScreen 引导首页
 * @see BubblesGradleWizardScreen Gradle 向导
 * @see BubblesComponentScreen 组件库
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
                BubblesTab.INTRO -> {
                    // Check if wizard is active / 检查向导是否激活
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
                BubblesTab.COMPONENTS -> {
                    BubblesComponentScreen(
                        state = state.componentsState,
                        onIntent = viewModel::sendIntent
                    )
                }
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
        targetValue = if (selectedTab == BubblesTab.INTRO) BubblesColors.Primary
                     else if (selectedTab == BubblesTab.COMPONENTS) BubblesColors.BubblesActive
                     else BubblesColors.PiPColor,
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
