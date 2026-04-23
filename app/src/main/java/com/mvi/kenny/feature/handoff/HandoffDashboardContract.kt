package com.mvi.kenny.feature.handoff

/**
 * ============================================================
 * HandoffDashboardContract — Dashboard MVI 契约
 * ============================================================
 */

data class HandoffDashboardState(
    val isHandoffAvailable: Boolean = false,
    val pairedDevices: List<PairedDevice> = emptyList(),
    val recentHandoffs: List<HandoffRecord> = emptyList(),
    val allFeatures: List<HandoffFeature> = emptyList(),
    val isScanning: Boolean = false,
    val androidVersion: Int = 0,
    val apiLevel: Int = 0,
    val error: String? = null
) {
    companion object { val Initial = HandoffDashboardState() }
}

sealed interface HandoffDashboardIntent {
    data object StartDeviceScan : HandoffDashboardIntent
    data object StopDeviceScan : HandoffDashboardIntent
    data class NavigateToFeature(val feature: HandoffFeature) : HandoffDashboardIntent
    data class RemovePairedDevice(val deviceId: String) : HandoffDashboardIntent
    data object RefreshHandoffStatus : HandoffDashboardIntent
    data object DismissError : HandoffDashboardIntent
}

sealed interface HandoffDashboardEffect {
    data class NavigateTo(val screen: HandoffScreen) : HandoffDashboardEffect
    data class ShowToast(val message: String) : HandoffDashboardEffect
    data class ShowError(val throwable: Throwable) : HandoffDashboardEffect
    data class ScanCompleted(val count: Int) : HandoffDashboardEffect
    data class PairingSucceeded(val deviceName: String) : HandoffDashboardEffect
}
