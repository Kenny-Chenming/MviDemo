package com.mvi.kenny.feature.bubbles

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================
 * BubblesViewModel — Android 17 App Bubbles MVI ViewModel
 * ============================================================
 * Manages state for all Bubbles feature screens.
 * 处理所有 Bubbles 功能屏幕的状态管理。
 *
 * Architecture: MVI (Model-View-Intent)
 * - State: Single source of truth via StateFlow
 * - Intent: User actions processed in sendIntent()
 * - Effect: One-time events via Channel
 *
 * @see BubblesContract For all state/intent/effect definitions
 */
class BubblesViewModel : ViewModel() {

    // =============================================================
    // State — 单一数据源
    // =============================================================
    private val _state = MutableStateFlow(BubblesMainState())
    val state: StateFlow<BubblesMainState> = _state.asStateFlow()

    // =============================================================
    // Effect — 一次性副作用
    // =============================================================
    private val _effect = Channel<BubblesEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // =============================================================
    // sendIntent — 处理所有用户意图
    // =============================================================
    /**
     * Process user intent and update state accordingly.
     * 处理用户意图并相应更新状态。
     *
     * @param intent User intent to process / 要处理的用户意图
     */
    fun sendIntent(intent: BubblesIntent) {
        when (intent) {
            // Tab selection / Tab 选择
            is BubblesIntent.SelectTab -> handleSelectTab(intent.tab)

            // Gradle Wizard / Gradle 向导
            is BubblesIntent.StartGradleWizard -> handleStartWizard(intent.modulePath)
            is BubblesIntent.NextWizardStep -> handleNextStep(intent.currentStep)
            is BubblesIntent.PrevWizardStep -> handlePrevStep(intent.currentStep)
            is BubblesIntent.SelectModule -> handleSelectModule(intent.module)
            is BubblesIntent.ToggleActivity -> handleToggleActivity(intent.activity)
            is BubblesIntent.UpdateMetadata -> handleUpdateMetadata(intent.config)
            is BubblesIntent.ApplyGradleConfig -> handleApplyConfig(intent.state)
            is BubblesIntent.ResetWizard -> handleResetWizard()

            // Components / 组件库
            is BubblesIntent.SelectComponent -> handleSelectComponent(intent.component)
            is BubblesIntent.ClearComponentSelection -> handleClearComponentSelection()

            // Size Simulator / 尺寸模拟器
            is BubblesIntent.SelectSizePreset -> handleSelectPreset(intent.preset)
            is BubblesIntent.UpdateCustomSize -> handleUpdateCustomSize(intent.width, intent.height)
            is BubblesIntent.UpdateTargetScreenSize -> handleUpdateTargetScreen(intent.screenSize)

            // Templates / 场景模板
            is BubblesIntent.SelectScenario -> handleSelectScenario(intent.scenario)
            is BubblesIntent.ClearScenarioSelection -> handleClearScenarioSelection()

            // Playground / Playground
            is BubblesIntent.UpdatePlaygroundDrag -> handleUpdateDrag(intent.isDragging)
            is BubblesIntent.UpdatePlaygroundPosition -> handleUpdatePosition(intent.position)
            is BubblesIntent.UpdatePlaygroundSize -> handleUpdateSize(intent.size)

            // Misc / 其他
            is BubblesIntent.ToggleVsPiP -> handleToggleVsPiP()
            is BubblesIntent.TogglePreviewAnimation -> handleToggleAnimation()

            // PRD-148: Compliance / 合规检测
            is BubblesIntent.StartComplianceScan -> handleStartComplianceScan()
            is BubblesIntent.AnalyzeSuitabilities -> handleAnalyzeSuitabilities()

            // PRD-148: Layout Preview / 布局预览
            is BubblesIntent.UpdateLayoutBubbleSize -> handleUpdateLayoutBubbleSize(intent.bubbleSize)

            // PRD-148: Playground / Playground
            is BubblesIntent.UpdatePlaygroundConfig -> handleUpdatePlaygroundConfig(intent.config)

            // PRD-148: Copy Code / 复制代码
            is BubblesIntent.CopyCode -> handleCopyCode(intent.code)
        }
    }

    // =============================================================
    // Tab Handlers / Tab 处理器
    // =============================================================
    /**
     * Handle tab selection.
     * 处理 Tab 选择。
     */
    private fun handleSelectTab(tab: BubblesTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    // =============================================================
    // Gradle Wizard Handlers / Gradle 向导处理器
    // =============================================================
    /**
     * Handle wizard start.
     * 处理向导启动。
     */
    private fun handleStartWizard(modulePath: String) {
        _state.update {
            it.copy(
                wizardState = it.wizardState.copy(
                    selectedModule = modulePath,
                    currentStep = WizardStep.MODULE_SELECT
                ),
                selectedTab = BubblesTab.INTRO
            )
        }
    }

    /**
     * Handle next wizard step.
     * 处理下一步向导。
     */
    private fun handleNextStep(currentStep: WizardStep) {
        val nextStep = when (currentStep) {
            WizardStep.MODULE_SELECT -> {
                // Validate module selection / 验证模块选择
                if (_state.value.wizardState.selectedModule.isEmpty()) {
                    sendEffect(BubblesEffect.ShowError("Please select a module"))
                    return
                }
                WizardStep.ACTIVITY_SELECT
            }
            WizardStep.ACTIVITY_SELECT -> {
                // Validate activity selection / 验证 Activity 选择
                if (_state.value.wizardState.selectedActivities.isEmpty()) {
                    sendEffect(BubblesEffect.ShowError("Please select at least one activity"))
                    return
                }
                WizardStep.METADATA_CONFIG
            }
            WizardStep.METADATA_CONFIG -> {
                // Generate manifest preview / 生成 Manifest 预览
                val manifest = generateManifestXml(
                    _state.value.wizardState.selectedActivities,
                    _state.value.wizardState.bubbleMetadata
                )
                _state.update {
                    it.copy(
                        wizardState = it.wizardState.copy(generatedManifestXml = manifest)
                    )
                }
                WizardStep.MANIFEST_PREVIEW
            }
            WizardStep.MANIFEST_PREVIEW -> {
                // Apply config / 应用配置
                applyConfigInternal()
                return
            }
            WizardStep.DONE -> WizardStep.DONE
        }
        _state.update { it.copy(wizardState = it.wizardState.copy(currentStep = nextStep)) }
    }

    /**
     * Handle previous wizard step.
     * 处理上一步向导。
     */
    private fun handlePrevStep(currentStep: WizardStep) {
        val prevStep = when (currentStep) {
            WizardStep.MODULE_SELECT -> WizardStep.MODULE_SELECT
            WizardStep.ACTIVITY_SELECT -> WizardStep.MODULE_SELECT
            WizardStep.METADATA_CONFIG -> WizardStep.ACTIVITY_SELECT
            WizardStep.MANIFEST_PREVIEW -> WizardStep.METADATA_CONFIG
            WizardStep.DONE -> WizardStep.DONE
        }
        _state.update { it.copy(wizardState = it.wizardState.copy(currentStep = prevStep)) }
    }

    /**
     * Handle module selection.
     * 处理模块选择。
     */
    private fun handleSelectModule(module: String) {
        _state.update {
            it.copy(
                wizardState = it.wizardState.copy(
                    selectedModule = module,
                    availableActivities = getMockActivitiesForModule(module)
                )
            )
        }
    }

    /**
     * Handle activity toggle.
     * 处理 Activity 切换。
     */
    private fun handleToggleActivity(activity: String) {
        _state.update { state ->
            val current = state.wizardState.selectedActivities.toMutableList()
            if (current.contains(activity)) {
                current.remove(activity)
            } else {
                current.add(activity)
            }
            state.copy(
                wizardState = state.wizardState.copy(selectedActivities = current)
            )
        }
    }

    /**
     * Handle metadata update.
     * 处理元数据更新。
     */
    private fun handleUpdateMetadata(config: BubbleMetadataConfig) {
        _state.update {
            it.copy(wizardState = it.wizardState.copy(bubbleMetadata = config))
        }
    }

    /**
     * Handle config application.
     * 处理配置应用。
     */
    private fun handleApplyConfig(wizardState: BubblesGradleWizardState) {
        _state.update { it.copy(wizardState = it.wizardState.copy(isApplying = true)) }
        applyConfigInternal()
    }

    /**
     * Internal config application logic.
     * 内部配置应用逻辑。
     */
    private fun applyConfigInternal() {
        viewModelScope.launch {
            _state.update {
                it.copy(wizardState = it.wizardState.copy(isApplying = true))
            }

            // Simulate config application / 模拟配置应用
            kotlinx.coroutines.delay(1000)

            val result = ApplyResult(
                success = true,
                message = "Manifest updated successfully!",
                manifestPath = "${_state.value.wizardState.selectedModule}/src/main/AndroidManifest.xml"
            )

            _state.update {
                it.copy(
                    wizardState = it.wizardState.copy(
                        isApplying = false,
                        applyResult = result,
                        currentStep = WizardStep.DONE
                    )
                )
            }

            sendEffect(BubblesEffect.ConfigApplySuccess(result.manifestPath ?: ""))
        }
    }

    /**
     * Handle wizard reset.
     * 处理向导重置。
     */
    private fun handleResetWizard() {
        _state.update {
            it.copy(wizardState = BubblesGradleWizardState())
        }
    }

    // =============================================================
    // Components Handlers / 组件库处理器
    // =============================================================
    /**
     * Handle component selection.
     * 处理组件选择。
     */
    private fun handleSelectComponent(component: BubbleComponent) {
        _state.update {
            it.copy(
                componentsState = it.componentsState.copy(
                    selectedComponent = component,
                    playgroundState = it.componentsState.playgroundState.copy(
                        selectedComponent = component
                    )
                )
            )
        }
        sendEffect(BubblesEffect.NavigateToComponentDetail(component.name))
    }

    /**
     * Handle clear component selection.
     * 处理清除组件选择。
     */
    private fun handleClearComponentSelection() {
        _state.update {
            it.copy(componentsState = it.componentsState.copy(selectedComponent = null))
        }
    }

    // =============================================================
    // Size Simulator Handlers / 尺寸模拟器处理器
    // =============================================================
    /**
     * Handle preset selection.
     * 处理预设选择。
     */
    private fun handleSelectPreset(preset: BubblePreset) {
        _state.update {
            it.copy(
                sizeSimulatorState = it.sizeSimulatorState.copy(
                    selectedPreset = preset,
                    customWidth = preset.defaultWidth,
                    customHeight = preset.defaultHeight
                )
            )
        }
    }

    /**
     * Handle custom size update.
     * 处理自定义尺寸更新。
     */
    private fun handleUpdateCustomSize(width: Dp, height: Dp) {
        _state.update {
            it.copy(
                sizeSimulatorState = it.sizeSimulatorState.copy(
                    customWidth = width,
                    customHeight = height,
                    selectedPreset = BubblePreset.CUSTOM
                )
            )
        }
    }

    /**
     * Handle target screen size update.
     * 处理目标屏幕尺寸更新。
     */
    private fun handleUpdateTargetScreen(screenSize: ScreenSize) {
        _state.update {
            it.copy(sizeSimulatorState = it.sizeSimulatorState.copy(targetScreenSize = screenSize))
        }
    }

    // =============================================================
    // Templates Handlers / 场景模板处理器
    // =============================================================
    /**
     * Handle scenario selection.
     * 处理场景选择。
     */
    private fun handleSelectScenario(scenario: BubbleScenario) {
        _state.update { it.copy(templatesState = it.templatesState.copy(selectedScenario = scenario)) }
        sendEffect(BubblesEffect.NavigateToScenarioDemo(scenario.id))
    }

    /**
     * Handle clear scenario selection.
     * 处理清除场景选择。
     */
    private fun handleClearScenarioSelection() {
        _state.update { it.copy(templatesState = it.templatesState.copy(selectedScenario = null)) }
    }

    // =============================================================
    // Playground Handlers / Playground 处理器
    // =============================================================
    /**
     * Handle drag state update.
     * 处理拖动状态更新。
     */
    private fun handleUpdateDrag(isDragging: Boolean) {
        _state.update {
            it.copy(
                componentsState = it.componentsState.copy(
                    playgroundState = it.componentsState.playgroundState.copy(isDragging = isDragging)
                )
            )
        }
    }

    /**
     * Handle position update.
     * 处理位置更新。
     */
    private fun handleUpdatePosition(position: Offset) {
        _state.update {
            it.copy(
                componentsState = it.componentsState.copy(
                    playgroundState = it.componentsState.playgroundState.copy(windowPosition = position)
                )
            )
        }
    }

    /**
     * Handle size update.
     * 处理尺寸更新。
     */
    private fun handleUpdateSize(size: DpSize) {
        _state.update {
            it.copy(
                componentsState = it.componentsState.copy(
                    playgroundState = it.componentsState.playgroundState.copy(windowSize = size)
                )
            )
        }
    }

    // =============================================================
    // Misc Handlers / 其他处理器
    // =============================================================
    /**
     * Handle vs PiP toggle.
     * 处理 vs PiP 切换。
     */
    private fun handleToggleVsPiP() {
        _state.update {
            it.copy(introState = it.introState.copy(showVsPiP = !it.introState.showVsPiP))
        }
    }

    /**
     * Handle animation toggle.
     * 处理动画切换。
     */
    private fun handleToggleAnimation() {
        _state.update {
            it.copy(introState = it.introState.copy(isAnimating = !it.introState.isAnimating))
        }
    }

    // =============================================================
    // PRD-148: Compliance Handlers / 合规检测处理器
    // =============================================================
    /**
     * Handle start compliance scan.
     * 处理开始合规扫描。
     */
    private fun handleStartComplianceScan() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, scanProgress = 0f, complianceResults = emptyList()) }
            // Simulate scan progress / 模拟扫描进度
            for (i in 1..10) {
                kotlinx.coroutines.delay(200)
                _state.update { it.copy(scanProgress = i / 10f) }
            }
            _state.update {
                it.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    complianceResults = getDefaultComplianceResults()
                )
            }
            sendEffect(BubblesEffect.ShowSnackbar("Compliance scan completed"))
        }
    }

    /**
     * Handle suitabilities analysis.
     * 处理适用性分析。
     */
    private fun handleAnalyzeSuitabilities() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, scanProgress = 0f) }
            for (i in 1..10) {
                kotlinx.coroutines.delay(150)
                _state.update { it.copy(scanProgress = i / 10f) }
            }
            _state.update {
                it.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    suitabilities = getDefaultSuitabilities()
                )
            }
            sendEffect(BubblesEffect.ShowSnackbar("Suitability analysis completed"))
        }
    }

    // =============================================================
    // PRD-148: Layout Preview Handlers / 布局预览处理器
    // =============================================================
    /**
     * Handle layout bubble size update.
     * 处理布局浮窗尺寸更新。
     */
    private fun handleUpdateLayoutBubbleSize(bubbleSize: BubbleSize) {
        _state.update { it.copy(layoutPreviewSize = bubbleSize) }
    }

    // =============================================================
    // PRD-148: Playground Handlers / Playground 处理器
    // =============================================================
    /**
     * Handle playground config update.
     * 处理 Playground 配置更新。
     */
    private fun handleUpdatePlaygroundConfig(config: BubblePlaygroundConfig) {
        _state.update { it.copy(playgroundConfig = config) }
    }

    // =============================================================
    // PRD-148: Copy Code Handler / 复制代码处理器
    // =============================================================
    /**
     * Handle copy code.
     * 处理复制代码。
     */
    private fun handleCopyCode(code: String) {
        sendEffect(BubblesEffect.ShowSnackbar("Code copied to clipboard"))
    }

    // =============================================================
    // Helper Functions / 辅助函数
    // =============================================================
    /**
     * Generate Manifest XML preview.
     * 生成 Manifest XML 预览。
     */
    private fun generateManifestXml(
        activities: List<String>,
        metadata: BubbleMetadataConfig
    ): String {
        val activityEntries = activities.joinToString("\n        ") { activity ->
            """
            |        <activity
            |            android:name=".$activity"
            |            android:exported="true"
            |            android:supportsPictureInPicture="true"
            |            android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation" />
            """.trimMargin()
        }

        return """
            |<!-- AndroidManifest.xml Auto-generated by Bubbles Toolkit -->
            |<manifest xmlns:android="http://schemas.android.com/apk/res/android">
            |
            |    <!-- Bubble permissions -->
            |    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
            |
            |    <application>
            |
            |        <!-- Bubble activities -->
            |        $activityEntries
            |
            |        <!-- Bubble Metadata Provider -->
            |        <meta-data
            |            android:name="android.app.bubble"
            |            android:resource="@xml/bubble_metadata" />
            |
            |    </application>
            |</manifest>
        """.trimMargin()
    }

    /**
     * Get mock activities for module (simulate module scanning).
     * 获取模块的模拟 Activity 列表（模拟模块扫描）。
     */
    private fun getMockActivitiesForModule(module: String): List<String> {
        return when (module) {
            "app" -> listOf(
                "MainActivity",
                "SettingsActivity",
                "DetailActivity",
                "PlayerActivity",
                "SearchActivity"
            )
            "library" -> listOf(
                "LibraryActivity",
                "ReaderActivity"
            )
            else -> listOf("MainActivity")
        }
    }

    /**
     * Send effect to UI.
     * 发送副作用到 UI。
     */
    private fun sendEffect(effect: BubblesEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
