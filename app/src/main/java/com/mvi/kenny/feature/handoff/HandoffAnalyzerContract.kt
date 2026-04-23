package com.mvi.kenny.feature.handoff

/**
 * ============================================================
 * HandoffAnalyzerContract — 适用性分析器 MVI 契约
 * ============================================================
 */

data class HandoffAnalyzerState(
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val analyzedActivities: List<ActivityHandoffScore> = emptyList(),
    val selectedActivity: ActivityHandoffScore? = null,
    val dependencyTree: ActivityDependencyTree? = null,
    val inputPath: String = ""
) {
    companion object { val Initial = HandoffAnalyzerState() }
}

sealed interface HandoffAnalyzerIntent {
    data class UpdateInputPath(val path: String) : HandoffAnalyzerIntent
    data object StartScan : HandoffAnalyzerIntent
    data object CancelScan : HandoffAnalyzerIntent
    data class SelectActivity(val activity: ActivityHandoffScore) : HandoffAnalyzerIntent
    data object ClearSelection : HandoffAnalyzerIntent
}

sealed interface HandoffAnalyzerEffect {
    data class ShowToast(val message: String) : HandoffAnalyzerEffect
    data class ScanCompleted(val totalCount: Int, val handoffableCount: Int) : HandoffAnalyzerEffect
}
