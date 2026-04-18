package com.mvi.kenny.feature.nav310rc01

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvi.kenny.feature.nav310rc01.Nav31State
import com.mvi.kenny.feature.nav310rc01.Nav31Intent
import com.mvi.kenny.feature.nav310rc01.Nav31Effect
import com.mvi.kenny.feature.nav310rc01.Nav310Presets
import com.mvi.kenny.feature.nav310rc01.UpgradeLevel
import com.mvi.kenny.feature.nav310rc01.Severity
import com.mvi.kenny.feature.nav310rc01.Nav31Tab
import com.mvi.kenny.feature.nav310rc01.BreakingChangeItem
import com.mvi.kenny.feature.nav310rc01.RecipeCard
import com.mvi.kenny.feature.nav310rc01.MigrationStep
import com.mvi.kenny.feature.nav310rc01.NavEntryTreeNode
import com.mvi.kenny.feature.nav310rc01.RingChartData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * ============================================================
 * Nav310Rc01ViewModel — Navigation 3.1.0-rc01 工具 ViewModel
 * ============================================================
 * PRD-117 | Jetpack Navigation 3.1.0-rc01 新版 API 变更检测与迁移工具包
 */
class Nav310Rc01ViewModel : ViewModel() {

    private val _state = MutableStateFlow(Nav31State())
    val state: StateFlow<Nav31State> = _state.asStateFlow()

    private val _effect = Channel<Nav31Effect>(Channel.BUFFERED)
    val effect: Flow<Nav31Effect> = _effect.receiveAsFlow()

    fun processIntent(intent: Nav31Intent) {
        when (intent) {
            is Nav31Intent.NavigateToTab -> handleNavigateToTab(intent.tab)
            is Nav31Intent.DetectBreakingChanges -> handleDetectBreakingChanges()
            is Nav31Intent.SelectChange -> handleSelectChange(intent.item)
            is Nav31Intent.GeneratePatch -> handleGeneratePatch(intent.items)
            is Nav31Intent.GenerateAllPatches -> handleGenerateAllPatches()
            is Nav31Intent.SelectRecipe -> handleSelectRecipe(intent.recipe)
            is Nav31Intent.ToggleTreeNode -> handleToggleTreeNode(intent.nodeId)
            is Nav31Intent.SelectMigrationStep -> handleSelectMigrationStep(intent.step)
            is Nav31Intent.CompleteMigrationStep -> handleCompleteMigrationStep(intent.stepNumber)
            is Nav31Intent.ScanLegacyCode -> handleScanLegacyCode()
            is Nav31Intent.ClearError -> handleClearError()
        }
    }

    private fun handleNavigateToTab(tab: Nav31Tab) {
        _state.update { it.copy(activeTab = tab) }
    }

    private fun handleDetectBreakingChanges() {
        viewModelScope.launch {
            _state.update { it.copy(isDetecting = true, detectProgress = 0f, breakingChanges = emptyList(), errorMessage = null) }
            repeat(10) { stage ->
                delay(150)
                _state.update { it.copy(detectProgress = (stage + 1) / 10f) }
            }
            val changes = Nav310Presets.BREAKING_CHANGES
            val ringData = RingChartData(
                high = changes.count { it.severity == Severity.High },
                medium = changes.count { it.severity == Severity.Medium },
                low = changes.count { it.severity == Severity.Low }
            )
            _state.update { it.copy(isDetecting = false, detectProgress = 1f, breakingChanges = changes, breakingChangesRing = ringData) }
            _effect.send(Nav31Effect.ShowDetectComplete(total = changes.size, high = ringData.high, medium = ringData.medium, low = ringData.low))
        }
    }

    private fun handleSelectChange(item: BreakingChangeItem?) {
        _state.update { it.copy(selectedChange = item) }
    }

    private fun handleGeneratePatch(items: List<BreakingChangeItem>) {
        viewModelScope.launch {
            if (items.isEmpty()) {
                _effect.send(Nav31Effect.ShowError("请先选择要迁移的变更项"))
                return@launch
            }
            _effect.send(Nav31Effect.PatchGenerated(items.size))
            _effect.send(Nav31Effect.ShowSnackbar("已生成 ${items.size} 项迁移补丁"))
        }
    }

    private fun handleGenerateAllPatches() {
        viewModelScope.launch {
            val all = _state.value.breakingChanges
            if (all.isEmpty()) {
                _effect.send(Nav31Effect.ShowError("没有可迁移的变更项，请先运行检测"))
                return@launch
            }
            _effect.send(Nav31Effect.PatchGenerated(all.size))
            _effect.send(Nav31Effect.ShowSnackbar("已生成全部 ${all.size} 项迁移补丁"))
        }
    }

    private fun handleSelectRecipe(recipe: RecipeCard?) {
        _state.update { it.copy(selectedRecipe = recipe) }
    }

    private fun handleToggleTreeNode(nodeId: String) {
        val currentTree = _state.value.navEntryTree ?: return
        val newTree = toggleNode(currentTree.root, nodeId)
        _state.update { it.copy(navEntryTree = currentTree.copy(root = newTree)) }
    }

    private fun toggleNode(node: NavEntryTreeNode, nodeId: String): NavEntryTreeNode {
        if (node.id == nodeId) return node.copy(isExpanded = !node.isExpanded)
        return node.copy(children = node.children.map { toggleNode(it, nodeId) })
    }

    private fun handleSelectMigrationStep(step: MigrationStep?) {
        _state.update { it.copy(selectedMigrationStep = step) }
    }

    private fun handleCompleteMigrationStep(stepNumber: Int) {
        val updatedSteps = _state.value.migrationSteps.map { step ->
            if (step.stepNumber == stepNumber) step.copy(isCompleted = true) else step
        }
        _state.update { it.copy(migrationSteps = updatedSteps) }
    }

    private fun handleScanLegacyCode() {
        viewModelScope.launch {
            _state.update { it.copy(isDetecting = true, detectProgress = 0f) }
            repeat(10) { stage ->
                delay(100)
                _state.update { it.copy(detectProgress = (stage + 1) / 10f) }
            }
            _state.update { it.copy(isDetecting = false, detectProgress = 1f) }
            _effect.send(Nav31Effect.ShowSnackbar("扫描完成，发现 3 处需适配位置"))
        }
    }

    private fun handleClearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    init {
        _state.update {
            it.copy(
                currentVersion = "1.0.0",
                latestVersion = "1.1.0-rc01",
                upgradeRecommendation = UpgradeLevel.Recommended,
                navDisplayReadiness = 0.35f,
                navDisplayTimeline = Nav310Presets.TIMELINE,
                recipes = Nav310Presets.RECIPES,
                migrationSteps = Nav310Presets.MIGRATION_STEPS,
                navEntryTree = Nav310Presets.NAV_ENTRY_TREE
            )
        }
    }
}
