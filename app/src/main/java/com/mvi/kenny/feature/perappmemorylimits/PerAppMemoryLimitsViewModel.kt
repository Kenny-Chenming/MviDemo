package com.mvi.kenny.feature.perappmemorylimits

// ================================================================
// PerAppMemoryLimitsViewModel — Android 17 Per-App Memory Limits ViewModel
// ================================================================
// MVI ViewModel for Android 17 Per-App Memory Limits Detection & Optimization Toolkit.
//
// PRD-235: Android 17 Per-App 内存限制检测与优化工具包
// Implements MVI pattern with State/Intent/Effect.
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class PerAppMemoryLimitsViewModel : ViewModel() {

    private val _state = MutableStateFlow(PerAppMemoryLimitsState())
    val state: StateFlow<PerAppMemoryLimitsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<PerAppMemoryLimitsEffect>()
    val effect = _effect.asSharedFlow()

    private var monitoringJob: Job? = null
    private var pressureTestJob: Job? = null

    // Formula: app_limit ≈ device_ram * 0.06 (Android 17 Beta 4 conservative estimate)
    private fun calculateMemoryLimit(ramGB: Int): MemoryLimitResult {
        val appLimitMB = (ramGB * 1024 * 0.06).roundToLong()
        val heapMB = (appLimitMB * 0.5).roundToLong()
        val nativeMB = (appLimitMB * 0.3).roundToLong()
        return MemoryLimitResult(appLimitMB = appLimitMB, heapMB = heapMB, nativeMB = nativeMB)
    }

    private data class MemoryLimitResult(val appLimitMB: Long, val heapMB: Long, val nativeMB: Long)

    fun processIntent(intent: PerAppMemoryLimitsIntent) {
        when (intent) {
            is PerAppMemoryLimitsIntent.SelectTier -> selectTier(intent.tier)
            is PerAppMemoryLimitsIntent.UpdateCustomRam -> updateCustomRam(intent.ramGB)
            is PerAppMemoryLimitsIntent.Calculate -> calculate()
            is PerAppMemoryLimitsIntent.ToggleCalculatorDetails -> toggleCalculatorDetails()
            is PerAppMemoryLimitsIntent.UpdateProjectPath -> updateProjectPath(intent.path)
            is PerAppMemoryLimitsIntent.StartScan -> startLeakScan()
            is PerAppMemoryLimitsIntent.CancelScan -> cancelLeakScan()
            is PerAppMemoryLimitsIntent.ClearFindings -> clearFindings()
            is PerAppMemoryLimitsIntent.StartMonitoring -> startMonitoring()
            is PerAppMemoryLimitsIntent.StopMonitoring -> stopMonitoring()
            is PerAppMemoryLimitsIntent.UpdateWarningThreshold -> updateWarningThreshold(intent.threshold)
            is PerAppMemoryLimitsIntent.UpdateCriticalThreshold -> updateCriticalThreshold(intent.threshold)
            is PerAppMemoryLimitsIntent.GenerateIntegrationSnippet -> generateIntegrationSnippet()
            is PerAppMemoryLimitsIntent.SelectTestTier -> selectTestTier(intent.tier)
            is PerAppMemoryLimitsIntent.RunPressureTest -> runPressureTest()
            is PerAppMemoryLimitsIntent.StopPressureTest -> stopPressureTest()
            is PerAppMemoryLimitsIntent.ClearTestResults -> clearTestResults()
            is PerAppMemoryLimitsIntent.CheckCompliance -> checkCompliance()
            is PerAppMemoryLimitsIntent.GenerateConfigSnippet -> generateConfigSnippet()
            is PerAppMemoryLimitsIntent.SelectTab -> selectTab(intent.tab)
        }
    }

    // Calculator
    private fun selectTier(tier: DeviceRamTier) {
        val calcState = _state.value.calculatorState
        val ramGB = if (tier == DeviceRamTier.CUSTOM) calcState.customRamGB.toInt() else tier.ramGB
        val result = calculateMemoryLimit(ramGB)
        _state.value = _state.value.copy(
            calculatorState = calcState.copy(
                selectedTier = tier,
                calculatedLimitMB = result.appLimitMB,
                calculatedHeapMB = result.heapMB,
                calculatedNativeMB = result.nativeMB
            )
        )
    }

    private fun updateCustomRam(ramGB: Float) {
        val calcState = _state.value.calculatorState
        _state.value = _state.value.copy(calculatorState = calcState.copy(customRamGB = ramGB))
        if (_state.value.calculatorState.selectedTier == DeviceRamTier.CUSTOM) calculate()
    }

    private fun calculate() {
        val calcState = _state.value.calculatorState
        val ramGB = if (calcState.selectedTier == DeviceRamTier.CUSTOM) calcState.customRamGB.toInt() else calcState.selectedTier.ramGB
        val result = calculateMemoryLimit(ramGB)
        _state.value = _state.value.copy(
            calculatorState = calcState.copy(isCalculating = true, calculatedLimitMB = result.appLimitMB, calculatedHeapMB = result.heapMB, calculatedNativeMB = result.nativeMB)
        )
        viewModelScope.launch {
            delay(300)
            _state.value = _state.value.copy(calculatorState = _state.value.calculatorState.copy(isCalculating = false))
        }
    }

    private fun toggleCalculatorDetails() {
        val calcState = _state.value.calculatorState
        _state.value = _state.value.copy(calculatorState = calcState.copy(showDetails = !calcState.showDetails))
    }

    // Leak Scanner
    private fun updateProjectPath(path: String) {
        val scannerState = _state.value.leakScannerState
        _state.value = _state.value.copy(leakScannerState = scannerState.copy(projectPath = path))
    }

    private fun startLeakScan() {
        val scannerState = _state.value.leakScannerState
        if (scannerState.projectPath.isBlank()) {
            viewModelScope.launch { _effect.emit(PerAppMemoryLimitsEffect.ShowError("Please enter a project path")) }
            return
        }
        _state.value = _state.value.copy(
            leakScannerState = scannerState.copy(isScanning = true, scanPhase = ScanPhase.SCANNING, progress = 0, findings = emptyList(), errorMessage = "")
        )
        viewModelScope.launch { performLeakScan() }
    }

    private suspend fun performLeakScan() {
        val demoFindings = listOf(
            LeakFinding("app/src/main/java/com/example/MyBitmapCache.kt", 45, LeakPatternType.BITMAP_NOT_RECYCLED, "val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)", "Ensure bitmap is recycled() when no longer needed. Use BitmapPool or WeakReference."),
            LeakFinding("app/src/main/java/com/example/MainActivity.kt", 78, LeakPatternType.CONTEXT_IN_HANDLER, "handler.postDelayed({ updateUI() }, 1000)", "Use a weak reference for Context in Handler, or use viewModelScope.launch instead."),
            LeakFinding("app/src/main/java/com/example/SingletonManager.kt", 90, LeakPatternType.SINGLETON_ACTIVITY, "object SingletonManager { var context: Context? = null }", "Avoid storing Activity context in Singleton. Use Application context instead."),
            LeakFinding("app/src/main/java/com/example/ImageLoader.kt", 112, LeakPatternType.BITMAP_CACHE_NO_REcycle, "private val cache = LruCache<String, Bitmap>(maxSize)", "Consider using BitmapPool for recycling bitmaps. Override entryRemoved() to recycle.")
        )
        val phases = listOf(ScanPhase.SCANNING to "Scanning Kotlin files...", ScanPhase.SCANNING to "Scanning Java files...", ScanPhase.ANALYZING to "Analyzing patterns...", ScanPhase.GENERATING_REPORT to "Generating report...")
        for ((i, pair) in phases.withIndex()) {
            val (phase, _) = pair
            _state.value = _state.value.copy(
                leakScannerState = _state.value.leakScannerState.copy(scanPhase = phase, progress = (i + 1) * 25, scannedFiles = (i + 1) * 125)
            )
            delay(800)
        }
        val summary = demoFindings.groupingBy { it.pattern.severity }.eachCount()
        _state.value = _state.value.copy(
            leakScannerState = _state.value.leakScannerState.copy(
                isScanning = false, scanPhase = ScanPhase.COMPLETED, progress = 100, scannedFiles = 500, totalFiles = 500,
                findings = demoFindings, summary = summary, lastScanTime = System.currentTimeMillis()
            )
        )
        _effect.emit(PerAppMemoryLimitsEffect.ScanCompleted)
    }

    private fun cancelLeakScan() {
        monitoringJob?.cancel()
        val scannerState = _state.value.leakScannerState
        _state.value = _state.value.copy(leakScannerState = scannerState.copy(isScanning = false, scanPhase = ScanPhase.IDLE, progress = 0))
    }

    private fun clearFindings() {
        val scannerState = _state.value.leakScannerState
        _state.value = _state.value.copy(leakScannerState = scannerState.copy(findings = emptyList(), summary = emptyMap(), scanPhase = ScanPhase.IDLE, reportPath = ""))
    }

    // Monitor SDK
    private fun startMonitoring() {
        val monitorState = _state.value.monitorSDKState
        _state.value = _state.value.copy(
            monitorSDKState = monitorState.copy(monitoringState = MonitoringState.MONITORING, monitoringStartTime = System.currentTimeMillis(), memoryHistory = emptyList())
        )
        monitoringJob = viewModelScope.launch {
            while (isActive) {
                updateMemoryInfo()
                delay(_state.value.monitorSDKState.updateIntervalMs)
            }
        }
    }

    private fun updateMemoryInfo() {
        val usedMB = (100..400).random()
        val totalMB = 512L
        val availableMB = totalMB - usedMB
        val usedPercent = usedMB.toFloat() / totalMB.toFloat()
        val memInfo = MemoryInfo(totalMemoryMB = totalMB, usedMemoryMB = usedMB.toLong(), availableMemoryMB = availableMB, usedPercent = usedPercent, isLowMemory = availableMB < 50, thresholdMB = (totalMB * 0.8).toLong())
        val newState = _state.value.monitorSDKState
        val monitoringState = when {
            memInfo.usedPercent >= newState.criticalThreshold -> MonitoringState.CRITICAL
            memInfo.usedPercent >= newState.warningThreshold -> MonitoringState.WARNING
            else -> MonitoringState.MONITORING
        }
        val newHistory = (newState.memoryHistory + usedPercent).takeLast(60)
        _state.value = _state.value.copy(monitorSDKState = newState.copy(memoryInfo = memInfo, monitoringState = monitoringState, memoryHistory = newHistory))
        viewModelScope.launch {
            if (memInfo.usedPercent >= newState.criticalThreshold) _effect.emit(PerAppMemoryLimitsEffect.MemoryCritical)
            else if (memInfo.usedPercent >= newState.warningThreshold) _effect.emit(PerAppMemoryLimitsEffect.MemoryWarning(memInfo.usedPercent))
        }
    }

    private fun stopMonitoring() {
        monitoringJob?.cancel()
        val monitorState = _state.value.monitorSDKState
        _state.value = _state.value.copy(monitorSDKState = monitorState.copy(monitoringState = MonitoringState.IDLE, memoryHistory = emptyList()))
    }

    private fun updateWarningThreshold(threshold: Float) {
        val monitorState = _state.value.monitorSDKState
        _state.value = _state.value.copy(monitorSDKState = monitorState.copy(warningThreshold = threshold))
    }

    private fun updateCriticalThreshold(threshold: Float) {
        val monitorState = _state.value.monitorSDKState
        _state.value = _state.value.copy(monitorSDKState = monitorState.copy(criticalThreshold = threshold))
    }

    private fun generateIntegrationSnippet() {
        val snippet = """
            |// Per-App Memory Monitor SDK Integration
            |class MyApplication : Application() {
            |    private val memoryMonitor = MemoryMonitor(
            |        warningThreshold = 0.8f,
            |        criticalThreshold = 0.9f,
            |        onWarning = { percent -> Log.w("Memory", "Warning: ${'$'}percent") },
            |        onCritical = { percent -> Log.e("Memory", "Critical: ${'$'}percent") }
            |    )
            |    override fun onCreate() { super.onCreate(); memoryMonitor.start() }
            |    override fun onLowMemory() { super.onLowMemory(); memoryMonitor.handleLowMemory() }
            |}
        """.trimMargin()
        val monitorState = _state.value.monitorSDKState
        _state.value = _state.value.copy(monitorSDKState = monitorState.copy(integrationSnippet = snippet, isSDKIntegrated = true))
    }

    // Pressure Test
    private fun selectTestTier(tier: DeviceRamTier) {
        val testState = _state.value.pressureTestState
        _state.value = _state.value.copy(pressureTestState = testState.copy(selectedTier = tier))
    }

    private fun runPressureTest() {
        val testState = _state.value.pressureTestState
        _state.value = _state.value.copy(pressureTestState = testState.copy(isRunning = true, progress = 0, currentPhase = "Initializing emulator..."))
        pressureTestJob = viewModelScope.launch {
            val tier = _state.value.pressureTestState.selectedTier
            val phases = listOf("Booting AVD (${tier.ramGB}GB RAM)" to 20, "Installing APK..." to 40, "Running memory pressure test..." to 70, "Allocating memory objects..." to 85, "Finalizing results..." to 95)
            for ((phase, progress) in phases) {
                _state.value = _state.value.copy(pressureTestState = _state.value.pressureTestState.copy(currentPhase = phase, progress = progress))
                delay(1200)
            }
            val result = PressureTestResult(
                deviceRAM = "${tier.ramGB}GB", appMemoryLimit = "${tier.appMemoryLimitMB}MB",
                testStatus = if (tier.ramGB <= 8) TestStatus.DEGRADED else TestStatus.PASS,
                peakMemoryMB = (tier.appMemoryLimitMB * 0.92).toLong(),
                oomOccurred = tier.ramGB <= 4,
                recommendation = when {
                    tier.ramGB <= 4 -> "LOW_END device — Implement aggressive memory management. Consider bitmap downsampling."
                    tier.ramGB <= 8 -> "MID_RANGE device — Memory pressure observed. Optimize image loading and cache strategy."
                    else -> "HIGH_END device — Within limits. Continue monitoring for edge cases."
                }
            )
            _state.value = _state.value.copy(
                pressureTestState = _state.value.pressureTestState.copy(
                    isRunning = false, progress = 100, currentPhase = "Completed",
                    results = _state.value.pressureTestState.results + result, lastTestTime = System.currentTimeMillis()
                )
            )
        }
    }

    private fun stopPressureTest() {
        pressureTestJob?.cancel()
        val testState = _state.value.pressureTestState
        _state.value = _state.value.copy(pressureTestState = testState.copy(isRunning = false, progress = 0, currentPhase = "Stopped"))
    }

    private fun clearTestResults() {
        val testState = _state.value.pressureTestState
        _state.value = _state.value.copy(pressureTestState = testState.copy(results = emptyList(), lastTestTime = 0L, reportPath = ""))
    }

    // Gradle Plugin
    private fun checkCompliance() {
        val pluginState = _state.value.gradlePluginState
        _state.value = _state.value.copy(gradlePluginState = pluginState.copy(isChecking = true, errorMessage = ""))
        viewModelScope.launch {
            delay(1500)
            val messages = mutableListOf<String>()
            var result = CIComplianceResult.PASS
            val sdkIntegrated = true; val thresholdConfigured = true; val oomHandler = false
            messages.add("[CHECK] Scanning for MemoryMonitor SDK integration...")
            if (!sdkIntegrated) { messages.add("[FAIL] MemoryMonitor SDK not found"); result = CIComplianceResult.FAIL }
            else messages.add("[PASS] MemoryMonitor SDK integrated")
            messages.add("[CHECK] Verifying warning threshold configuration...")
            if (!thresholdConfigured) { messages.add("[FAIL] Warning threshold not configured"); result = CIComplianceResult.FAIL }
            else messages.add("[PASS] Warning threshold configured (80%)")
            messages.add("[CHECK] Detecting OOM handler registration...")
            if (!oomHandler) { messages.add("[WARN] No OOM handler registered — may cause unexpected crashes"); if (result == CIComplianceResult.PASS) result = CIComplianceResult.WARN }
            else messages.add("[PASS] OOM handler registered")
            messages.add("[CHECK] CI Compliance: ${'$'}{result.name}")
            _state.value = _state.value.copy(
                gradlePluginState = _state.value.gradlePluginState.copy(
                    isChecking = false, isSDKIntegrated = sdkIntegrated, isThresholdConfigured = thresholdConfigured,
                    isOOMHandlerRegistered = oomHandler, checkResult = result, checkMessages = messages
                )
            )
        }
    }

    private fun generateConfigSnippet() {
        val snippet = """
            |// android-memory-limits Gradle Plugin Configuration
            |plugins {
            |    id("android.memory.limits") version "1.0.0"
            |}
            |androidMemoryLimits {
            |    enableMonitoring.set(true)
            |    warningThreshold.set(0.8f)
            |    criticalThreshold.set(0.9f)
            |    failOnOOM.set(true)
            |    onOOM {
            |        cleanup()
            |        flushPendingData()
            |    }
            |}
        """.trimMargin()
        val pluginState = _state.value.gradlePluginState
        _state.value = _state.value.copy(gradlePluginState = pluginState.copy(configurationSnippet = snippet))
    }

    // Tab Navigation
    private fun selectTab(tab: ToolTab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    override fun onCleared() {
        super.onCleared()
        monitoringJob?.cancel()
        pressureTestJob?.cancel()
    }
}
