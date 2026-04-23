package com.mvi.kenny.feature.handoff

import android.os.Build
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
 * HandoffDashboardViewModel — Dashboard ViewModel
 * ============================================================
 */
class HandoffDashboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(HandoffDashboardState.Initial)
    val state: StateFlow<HandoffDashboardState> = _state.asStateFlow()

    private val _effect = Channel<HandoffDashboardEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init { loadInitialData() }

    private fun loadInitialData() {
        val apiLevel = Build.VERSION.SDK_INT
        val isHandoffAvailable = apiLevel >= 37
        _state.value = _state.value.copy(
            isHandoffAvailable = isHandoffAvailable,
            androidVersion = Build.VERSION.SDK_INT,
            apiLevel = apiLevel,
            allFeatures = buildFeatureList(),
            pairedDevices = buildMockPairedDevices(),
            recentHandoffs = buildMockHandoffRecords()
        )
    }

    private fun buildFeatureList(): List<HandoffFeature> = listOf(
        HandoffFeature(HandoffScreen.ApiLibrary, "API 封装库", "API Library", "HandoffActivity + HandoffManager Kotlin/Compose 封装，开箱即用", "Code", true),
        HandoffFeature(HandoffScreen.Analyzer, "适用性分析器", "Analyzer", "APK/源码扫描，评估每个 Activity 的 Handoff 适配度", "Analytics", true),
        HandoffFeature(HandoffScreen.Serialization, "序列化框架", "Serialization", "安全状态序列化 + 冲突解决策略（LWW/Merge/手动）", "SwapVert", true),
        HandoffFeature(HandoffScreen.Pairing, "设备配对", "Pairing", "Discover → Pair → Confirm 全流程，附带 Web 降级配置", "Bluetooth", false),
        HandoffFeature(HandoffScreen.UXGuide, "UX 设计规范", "UX Guide", "Handoff 可用时通知样式、Launcher 快捷方式、场景化指南", "DesignServices", false),
        HandoffFeature(HandoffScreen.Debug, "调试面板", "Debug Panel", "模拟发送/接收、序列化数据查看、状态冲突模拟", "BugReport", false)
    )

    private fun buildMockPairedDevices(): List<PairedDevice> = listOf(
        PairedDevice("device-001", "Pixel 9 Pro", DeviceType.PHONE, System.currentTimeMillis() - 3600000, true),
        PairedDevice("device-002", "Samsung Galaxy Tab S10", DeviceType.TABLET, System.currentTimeMillis() - 7200000, true),
        PairedDevice("device-003", "Pixel Watch 3", DeviceType.WATCH, System.currentTimeMillis() - 86400000, false)
    )

    private fun buildMockHandoffRecords(): List<HandoffRecord> = listOf(
        HandoffRecord("record-001", "com.example.email", "com.example.email.ComposeActivity", "device-001", System.currentTimeMillis() - 3600000, HandoffStatus.SUCCESS, 4096),
        HandoffRecord("record-002", "com.example.browser", "com.example.browser.TabActivity", "device-002", System.currentTimeMillis() - 7200000, HandoffStatus.SUCCESS, 8192),
        HandoffRecord("record-003", "com.example.notes", "com.example.notes.EditorActivity", "device-003", System.currentTimeMillis() - 86400000, HandoffStatus.FAILED, 2048)
    )

    fun sendIntent(intent: HandoffDashboardIntent) {
        when (intent) {
            is HandoffDashboardIntent.StartDeviceScan -> startDeviceScan()
            is HandoffDashboardIntent.StopDeviceScan -> stopDeviceScan()
            is HandoffDashboardIntent.NavigateToFeature -> navigateToFeature(intent.feature)
            is HandoffDashboardIntent.RemovePairedDevice -> removePairedDevice(intent.deviceId)
            is HandoffDashboardIntent.RefreshHandoffStatus -> refreshHandoffStatus()
            is HandoffDashboardIntent.DismissError -> _state.value = _state.value.copy(error = null)
        }
    }

    private fun startDeviceScan() {
        _state.value = _state.value.copy(isScanning = true)
        viewModelScope.launch {
            delay(3000)
            val found = listOf(
                PairedDevice("device-scan-001", "Pixel 8", DeviceType.PHONE, 0L, true),
                PairedDevice("device-scan-002", "Android Tablet Demo", DeviceType.TABLET, 0L, true)
            )
            _state.value = _state.value.copy(isScanning = false, pairedDevices = _state.value.pairedDevices + found)
            _effect.send(HandoffDashboardEffect.ScanCompleted(found.size))
            _effect.send(HandoffDashboardEffect.ShowToast("发现 ${found.size} 台设备"))
        }
    }

    private fun stopDeviceScan() {
        _state.value = _state.value.copy(isScanning = false)
        viewModelScope.launch { _effect.send(HandoffDashboardEffect.ShowToast("扫描已停止")) }
    }

    private fun navigateToFeature(feature: HandoffFeature) {
        viewModelScope.launch { _effect.send(HandoffDashboardEffect.NavigateTo(feature.screen)) }
    }

    private fun removePairedDevice(deviceId: String) {
        val name = _state.value.pairedDevices.find { it.id == deviceId }?.name ?: "设备"
        _state.value = _state.value.copy(pairedDevices = _state.value.pairedDevices.filter { it.id != deviceId })
        viewModelScope.launch { _effect.send(HandoffDashboardEffect.ShowToast("已移除 $name")) }
    }

    private fun refreshHandoffStatus() {
        viewModelScope.launch {
            delay(1000)
            _effect.send(HandoffDashboardEffect.ShowToast("状态已刷新"))
        }
    }
}
