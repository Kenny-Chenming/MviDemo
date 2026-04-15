package com.mvi.kenny.feature.handoff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HandoffViewModel : ViewModel() {
    private val _dashboardState = MutableStateFlow(HandoffDashboardState())
    val dashboardState: StateFlow<HandoffDashboardState> = _dashboardState.asStateFlow()
    private val _apiLibraryState = MutableStateFlow(HandoffApiLibraryState())
    val apiLibraryState: StateFlow<HandoffApiLibraryState> = _apiLibraryState.asStateFlow()
    private val _analyzerState = MutableStateFlow(HandoffAnalyzerState())
    val analyzerState: StateFlow<HandoffAnalyzerState> = _analyzerState.asStateFlow()
    private val _serializationState = MutableStateFlow(HandoffSerializationState())
    val serializationState: StateFlow<HandoffSerializationState> = _serializationState.asStateFlow()
    private val _pairingState = MutableStateFlow(HandoffPairingState())
    val pairingState: StateFlow<HandoffPairingState> = _pairingState.asStateFlow()
    private val _uxGuideState = MutableStateFlow(HandoffUXGuideState())
    val uxGuideState: StateFlow<HandoffUXGuideState> = _uxGuideState.asStateFlow()
    private val _debugState = MutableStateFlow(HandoffDebugState())
    val debugState: StateFlow<HandoffDebugState> = _debugState.asStateFlow()
    private val _effect = Channel<HandoffEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun sendIntent(intent: HandoffIntent) {
        when (intent) {
            is HandoffIntent.StartDeviceScan -> handleStartDeviceScan()
            is HandoffIntent.StopDeviceScan -> _dashboardState.update { it.copy(isScanning = false) }
            is HandoffIntent.NavigateToFeature -> handleNavigateToFeature(intent.feature)
            is HandoffIntent.RemovePairedDevice -> _dashboardState.update { s -> s.copy(pairedDevices = s.pairedDevices.filter { it.deviceId != intent.deviceId }) }
            is HandoffIntent.RefreshHandoffStatus -> _dashboardState.update { it.copy(isHandoffAvailable = true) }
            is HandoffIntent.SelectApiTab -> _apiLibraryState.update { it.copy(selectedTab = intent.tab) }
            is HandoffIntent.CopyCode -> handleCopyCode(intent.itemId)
            is HandoffIntent.UpdateHandoffConfig -> _apiLibraryState.update { it.copy(handoffConfig = intent.config) }
            is HandoffIntent.StartScan -> handleStartScan(intent.apkPath)
            is HandoffIntent.SelectActivityDetail -> _analyzerState.update { it.copy(selectedActivity = intent.activity) }
            is HandoffIntent.ClearActivitySelection -> _analyzerState.update { it.copy(selectedActivity = null) }
            is HandoffIntent.SelectStrategy -> _serializationState.update { it.copy(selectedStrategy = intent.strategy) }
            is HandoffIntent.AddSecurityFilter -> _serializationState.update { s -> s.copy(securityFilters = s.securityFilters + intent.filter) }
            is HandoffIntent.RemoveSecurityFilter -> _serializationState.update { s -> s.copy(securityFilters = s.securityFilters.filter { it.fieldName != intent.fieldName }) }
            is HandoffIntent.GenerateSerializationCode -> handleGenerateCode()
            is HandoffIntent.StartDiscovery -> handleStartDiscovery()
            is HandoffIntent.StopDiscovery -> _pairingState.update { it.copy(isDiscovering = false) }
            is HandoffIntent.SelectDeviceForPairing -> _pairingState.update { it.copy(selectedDevice = intent.device, currentStep = PairingStep.PAIR) }
            is HandoffIntent.ConfirmPairing -> handleConfirmPairing(intent.device)
            is HandoffIntent.CancelPairing -> _pairingState.update { it.copy(selectedDevice = null, currentStep = PairingStep.DISCOVER) }
            is HandoffIntent.UpdateWebFallbackUrl -> _pairingState.update { it.copy(webFallbackUrl = intent.url) }
            is HandoffIntent.SelectUXCategory -> _uxGuideState.update { it.copy(selectedCategory = intent.category) }
            is HandoffIntent.SelectUXScenario -> _uxGuideState.update { it.copy(selectedScenario = intent.scenario) }
            is HandoffIntent.ToggleNotificationPreview -> _uxGuideState.update { it.copy(showNotificationPreview = !it.showNotificationPreview) }
            is HandoffIntent.SimulateSend -> handleSimulateSend()
            is HandoffIntent.SimulateReceive -> handleSimulateReceive()
            is HandoffIntent.SimulateConflict -> handleSimulateConflict()
            is HandoffIntent.ResolveConflict -> handleResolveConflict(intent.strategy)
            is HandoffIntent.ClearDebugLogs -> _debugState.update { it.copy(debugLogs = emptyList()) }
        }
    }

    private fun handleStartDeviceScan() {
        _dashboardState.update { it.copy(isScanning = true) }
        viewModelScope.launch {
            delay(2000)
            val mock = getMockPairedDevices()
            _dashboardState.update { it.copy(isScanning = false, pairedDevices = mock) }
            sendEffect(HandoffEffect.ShowToast("扫描完成，发现 ${mock.size} 个设备"))
        }
    }

    private fun handleNavigateToFeature(feature: HandoffFeature) {
        _dashboardState.update { it.copy(selectedFeature = feature) }
        sendEffect(HandoffEffect.NavigateTo(feature.screen))
    }

    private fun handleCopyCode(itemId: String) {
        _apiLibraryState.update { it.copy(copiedItem = itemId) }
        sendEffect(HandoffEffect.CopyToClipboardSuccess(itemId))
        viewModelScope.launch { delay(2000); _apiLibraryState.update { it.copy(copiedItem = null) } }
    }

    private fun handleStartScan(apkPath: String) {
        _analyzerState.update { it.copy(scanStatus = ScanStatus.SCANNING, apkPath = apkPath, scanProgress = 0) }
        viewModelScope.launch {
            for (i in 1..10) { delay(300); _analyzerState.update { it.copy(scanProgress = i * 10) } }
            val activities = getMockAnalyzedActivities()
            _analyzerState.update { it.copy(scanStatus = ScanStatus.DONE, analyzedActivities = activities) }
            sendEffect(HandoffEffect.ShowToast("分析完成，共 ${activities.size} 个 Activity"))
        }
    }

    private fun handleGenerateCode() {
        _serializationState.update { it.copy(isGenerating = true) }
        viewModelScope.launch { delay(1000); _serializationState.update { it.copy(isGenerating = false) }; sendEffect(HandoffEffect.ShowToast("代码已生成")) }
    }

    private fun handleStartDiscovery() {
        _pairingState.update { it.copy(isDiscovering = true, currentStep = PairingStep.DISCOVER) }
        viewModelScope.launch {
            delay(2000)
            val discovered = listOf(PairedDevice("new_1", "Pixel 7", DeviceType.PHONE, System.currentTimeMillis(), null, true), PairedDevice("new_2", "Galaxy Tab", DeviceType.TABLET, System.currentTimeMillis(), null, true))
            _pairingState.update { it.copy(isDiscovering = false, discoveredDevices = discovered) }
            sendEffect(HandoffEffect.ShowToast("发现 ${discovered.size} 个设备"))
        }
    }

    private fun handleConfirmPairing(device: PairedDevice) {
        viewModelScope.launch {
            _pairingState.update { it.copy(pairingProgress = 0) }
            for (i in 1..5) { delay(200); _pairingState.update { it.copy(pairingProgress = i * 20) } }
            val newPaired = device.copy(pairedAt = System.currentTimeMillis(), isOnline = true)
            _pairingState.update { s -> s.copy(pairedDevices = s.pairedDevices + newPaired, discoveredDevices = s.discoveredDevices.filter { it.deviceId != device.deviceId }, selectedDevice = null, currentStep = PairingStep.DISCOVER, pairingProgress = 0) }
            sendEffect(HandoffEffect.PairingSuccess(device.deviceName))
        }
    }

    private fun handleSimulateSend() {
        _debugState.update { it.copy(isSimulatingSend = true) }
        viewModelScope.launch {
            delay(1500)
            val json = """{"activityName": "com.example.app.MainActivity","state": {"documentId": "doc_999"},"timestamp": ${System.currentTimeMillis()},"deviceId": "simulated_device"}"""
            _debugState.update { it.copy(isSimulatingSend = false, serializedJson = json, debugLogs = it.debugLogs + DebugLogEntry(System.currentTimeMillis(), LogLevel.SUCCESS, "Handoff 发送成功", json)) }
            sendEffect(HandoffEffect.ShowToast("模拟发送完成"))
        }
    }

    private fun handleSimulateReceive() {
        _debugState.update { it.copy(isSimulatingReceive = true) }
        viewModelScope.launch {
            delay(1500)
            val data = "MainActivity{documentId=doc_123}"
            _debugState.update { it.copy(isSimulatingReceive = false, deserializedData = data, debugLogs = it.debugLogs + DebugLogEntry(System.currentTimeMillis(), LogLevel.SUCCESS, "Handoff 接收成功", data)) }
            sendEffect(HandoffEffect.ShowToast("模拟接收完成"))
        }
    }

    private fun handleSimulateConflict() {
        val conflict = """{"local": {"scrollPosition": "0.8"},"remote": {"scrollPosition": "0.3"}}"""
        _debugState.update { it.copy(simulatedConflict = conflict, isConflictResolved = false, debugLogs = it.debugLogs + DebugLogEntry(System.currentTimeMillis(), LogLevel.WARNING, "检测到状态冲突", conflict)) }
    }

    private fun handleResolveConflict(strategy: SerializationStrategy) {
        _debugState.update { it.copy(isConflictResolved = true, selectedResolution = strategy, debugLogs = it.debugLogs + DebugLogEntry(System.currentTimeMillis(), LogLevel.SUCCESS, "冲突已解决: ${strategy.labelZh}", null)) }
    }

    private fun getMockPairedDevices(): List<PairedDevice> = listOf(
        PairedDevice("device_1", "Pixel 8 Pro", DeviceType.PHONE, System.currentTimeMillis() - 86400000, System.currentTimeMillis() - 3600000, true),
        PairedDevice("device_2", "Samsung Tab S9", DeviceType.TABLET, System.currentTimeMillis() - 172800000, null, false),
        PairedDevice("device_3", "Pixel Fold", DeviceType.FOLDABLE, System.currentTimeMillis() - 259200000, System.currentTimeMillis() - 7200000, true)
    )

    private fun getMockAnalyzedActivities(): List<ActivityHandoffScore> = listOf(
        ActivityHandoffScore("MainActivity", 90, HandoffPriority.HIGH, listOf("支持配置变更", "单一 Activity"), listOf("建议添加 PendingIntent")),
        ActivityHandoffScore("DetailActivity", 85, HandoffPriority.HIGH, listOf("内容可独立传输"), emptyList()),
        ActivityHandoffScore("SearchActivity", 70, HandoffPriority.MEDIUM, listOf("搜索结果可共享"), listOf("搜索历史需本地保留")),
        ActivityHandoffScore("SettingsActivity", 60, HandoffPriority.LOW, listOf("部分设置可传输"), listOf("包含敏感信息")),
        ActivityHandoffScore("LoginActivity", 10, HandoffPriority.NOT_SUITABLE, emptyList(), listOf("安全原因不建议 Handoff"))
    )

    private fun sendEffect(effect: HandoffEffect) { viewModelScope.launch { _effect.send(effect) } }
}
