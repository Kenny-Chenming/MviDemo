package com.mvi.kenny.feature.handoff

/**
 * ============================================================
 * HandoffSerializationContract — 序列化框架 MVI 契约
 * ============================================================
 */

data class HandoffSerializationState(
    val selectedTemplate: SerializationTemplate = SerializationTemplate.BASIC_STATE,
    val selectedStrategy: SerializationStrategy = SerializationStrategy.LWW,
    val securityFilters: List<SecurityFilter> = emptyList(),
    val previewJson: String = "",
    val showConflictSheet: Boolean = false,
    val conflictData: ConflictData? = null
) {
    companion object { val Initial = HandoffSerializationState() }
}

sealed interface HandoffSerializationIntent {
    data class SelectTemplate(val template: SerializationTemplate) : HandoffSerializationIntent
    data class SelectStrategy(val strategy: SerializationStrategy) : HandoffSerializationIntent
    data class ToggleSecurityFilter(val filterName: String) : HandoffSerializationIntent
    data object GeneratePreview : HandoffSerializationIntent
    data object ShowConflictSheet : HandoffSerializationIntent
    data object HideConflictSheet : HandoffSerializationIntent
    data class ResolveConflict(val resolution: String) : HandoffSerializationIntent
}

sealed interface HandoffSerializationEffect {
    data class ShowToast(val message: String) : HandoffSerializationEffect
    data object PreviewGenerated : HandoffSerializationEffect
}
