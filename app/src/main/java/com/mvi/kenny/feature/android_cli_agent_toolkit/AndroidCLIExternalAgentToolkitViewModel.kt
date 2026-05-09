package com.mvi.kenny.feature.android_cli_agent_toolkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * PRD-241: Android CLI × External AI Agent Integration Toolkit — ViewModel
 * MVI 架构：处理 Intent，输出 State + Effect
 */
class AndroidCLIExternalAgentToolkitViewModel : ViewModel() {

    private val _state = MutableStateFlow(AndroidCLIExternalAgentToolkitState())
    val state: StateFlow<AndroidCLIExternalAgentToolkitState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AndroidCLIExternalAgentToolkitEffect>()
    val effect: kotlinx.coroutines.flow.Flow<AndroidCLIExternalAgentToolkitEffect> = _effect.asSharedFlow()

    fun sendIntent(intent: AndroidCLIExternalAgentToolkitIntent) {
        viewModelScope.launch {
            when (intent) {
                is AndroidCLIExternalAgentToolkitIntent.SelectTab -> {
                    _state.value = _state.value.copy(selectedTab = intent.index)
                }

                is AndroidCLIExternalAgentToolkitIntent.ToggleAgentGuideExpanded -> {
                    val current = _state.value.expandedAgentGuideId
                    _state.value = _state.value.copy(
                        expandedAgentGuideId = if (current == intent.guideId) null else intent.guideId
                    )
                }

                is AndroidCLIExternalAgentToolkitIntent.TogglePatternExpanded -> {
                    val current = _state.value.expandedPatternId
                    _state.value = _state.value.copy(
                        expandedPatternId = if (current == intent.patternId) null else intent.patternId
                    )
                }

                is AndroidCLIExternalAgentToolkitIntent.ToggleCommandExpanded -> {
                    val current = _state.value.expandedCommandId
                    _state.value = _state.value.copy(
                        expandedCommandId = if (current == intent.commandId) null else intent.commandId
                    )
                }

                is AndroidCLIExternalAgentToolkitIntent.ToggleTriggerExpanded -> {
                    val current = _state.value.expandedTriggerId
                    _state.value = _state.value.copy(
                        expandedTriggerId = if (current == intent.triggerId) null else intent.triggerId
                    )
                }

                is AndroidCLIExternalAgentToolkitIntent.ToggleSwiftExportExpanded -> {
                    val current = _state.value.expandedSwiftExportId
                    _state.value = _state.value.copy(
                        expandedSwiftExportId = if (current == intent.guideId) null else intent.guideId
                    )
                }

                is AndroidCLIExternalAgentToolkitIntent.ToggleQuailFeatureExpanded -> {
                    val current = _state.value.expandedQuailFeatureId
                    _state.value = _state.value.copy(
                        expandedQuailFeatureId = if (current == intent.featureId) null else intent.featureId
                    )
                }

                is AndroidCLIExternalAgentToolkitIntent.ToggleKbApiExpanded -> {
                    val current = _state.value.expandedKbApiId
                    _state.value = _state.value.copy(
                        expandedKbApiId = if (current == intent.apiId) null else intent.apiId
                    )
                }

                is AndroidCLIExternalAgentToolkitIntent.ToggleFrameworkExpanded -> {
                    val current = _state.value.expandedFrameworkId
                    _state.value = _state.value.copy(
                        expandedFrameworkId = if (current == intent.frameworkId) null else intent.frameworkId
                    )
                }

                is AndroidCLIExternalAgentToolkitIntent.CopyCommand -> {
                    _state.value = _state.value.copy(copiedItemId = intent.itemId)
                    _effect.emit(AndroidCLIExternalAgentToolkitEffect.CopyToClipboard(intent.command))
                }

                is AndroidCLIExternalAgentToolkitIntent.OpenUrl -> {
                    _effect.emit(AndroidCLIExternalAgentToolkitEffect.OpenUrl(intent.url))
                }
            }
        }
    }
}
