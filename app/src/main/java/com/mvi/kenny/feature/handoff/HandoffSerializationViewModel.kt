package com.mvi.kenny.feature.handoff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * HandoffSerializationViewModel — 序列化框架 ViewModel
 * ============================================================
 */
class HandoffSerializationViewModel : ViewModel() {

    private val _state = MutableStateFlow(HandoffSerializationState.Initial)
    val state: StateFlow<HandoffSerializationState> = _state.asStateFlow()

    private val _effect = Channel<HandoffSerializationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        _state.value = _state.value.copy(
            securityFilters = listOf(
                SecurityFilter("密码字段", ".*password.*", "自动排除所有包含 password 的字段", true),
                SecurityFilter("认证 Token", ".*token.*|.*auth.*", "自动排除 Token 和认证信息", true),
                SecurityFilter("会话密钥", ".*session.*|.*session_id.*", "自动排除会话相关数据", true),
                SecurityFilter("信用卡号", ".*credit.*|.*card.*|.*cvv.*", "自动排除支付相关信息", true),
                SecurityFilter("身份证号", ".*id_number.*|.*ssn.*", "自动排除身份标识信息", false)
            )
        )
    }

    fun sendIntent(intent: HandoffSerializationIntent) {
        when (intent) {
            is HandoffSerializationIntent.SelectTemplate -> _state.value = _state.value.copy(selectedTemplate = intent.template)
            is HandoffSerializationIntent.SelectStrategy -> _state.value = _state.value.copy(selectedStrategy = intent.strategy)
            is HandoffSerializationIntent.ToggleSecurityFilter -> {
                _state.value = _state.value.copy(
                    securityFilters = _state.value.securityFilters.map { f ->
                        if (f.name == intent.filterName) f.copy(enabled = !f.enabled) else f
                    }
                )
            }
            is HandoffSerializationIntent.GeneratePreview -> generatePreview()
            is HandoffSerializationIntent.ShowConflictSheet -> {
                _state.value = _state.value.copy(
                    showConflictSheet = true,
                    conflictData = ConflictData("document_content", "原始文档内容 v1...", "另一设备修改后的内容 v2...")
                )
            }
            is HandoffSerializationIntent.HideConflictSheet -> _state.value = _state.value.copy(showConflictSheet = false, conflictData = null)
            is HandoffSerializationIntent.ResolveConflict -> {
                _state.value = _state.value.copy(showConflictSheet = false)
                viewModelScope.launch { _effect.send(HandoffSerializationEffect.ShowToast("冲突已解决: ${intent.resolution}")) }
            }
        }
    }

    private fun generatePreview() {
        val s = _state.value
        val strategyHint = when (s.selectedStrategy) {
            SerializationStrategy.LWW -> "last_write_wins"
            SerializationStrategy.MERGE -> "auto_merge"
            SerializationStrategy.MANUAL_CONFIRM -> "manual"
        }
        val timestamp = System.currentTimeMillis()
        val json = """
{
  "handoff_payload": {
    "version": 1,
    "template": "${s.selectedTemplate.name.lowercase()}",
    "strategy": "$strategyHint",
    "timestamp": $timestamp,
    "device_id": "device-001",
    "data": {
      "ui_state": {
        "scroll_offset": 0,
        "cursor_position": 42,
        "selected_range": [10, 42]
      }
    },
    "excluded_fields": ["password", "token", "session_key"]
  }
}
        """.trimIndent()
        _state.value = _state.value.copy(previewJson = json)
        viewModelScope.launch { _effect.send(HandoffSerializationEffect.PreviewGenerated) }
    }
}
