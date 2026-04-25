package com.mvi.kenny.feature.memorylimits

// ================================================================
// MemoryLimitsViewModel — Android 17 内存限制 MVI ViewModel
// ================================================================
// ViewModel for Android 17 Memory Limits detection & optimization toolkit.
//
// PRD-151: Android 17 App Memory Limits 内存限制检测与调优开发工具包
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Manage memory monitoring (start/stop/pause)
//   - Collect MemoryLimiter kill events from ApplicationExitInfo
//   - Trigger heap dump collection on memory anomaly
//   - Run CI compliance checks
//   - Simulate memory limits on different device RAM configurations
//   - Expose one-time Effects (navigation, toast, errors)
// ================================================================

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * ============================================================
 * MemoryLimitsViewModel — 内存限制工具 ViewModel
 * ============================================================
 * Manages the MemoryLimitsState and processes MemoryLimitsIntent.
 *
 * In a real implementation, this would:
 *   - Use ActivityManager.getProcessMemoryInfo to poll memory usage
 *   - Parse ApplicationExitInfo for MemoryLimiter events
 *   - Configure ProfilingManager TRIGGER_TYPE_ANOMALY
 *   - Collect and analyze heap dumps
 *
 * Current implementation simulates all data for demonstration purposes.
 *
 * @see MemoryLimitsState
 * @see MemoryLimitsIntent
 * @see MemoryLimitsEffect
 */
class MemoryLimitsViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(MemoryLimitsState.Initial)
    val state: StateFlow<MemoryLimitsState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via SharedFlow
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<MemoryLimitsEffect>()
    val effect = _effect.asSharedFlow()

    // ─────────────────────────────────────────────────────────────
    // Internal state
    // ─────────────────────────────────────────────────────────────
    private var monitoringJob: Job? = null
    private var historyStartTime: Long = 0L
    private var historyEndTime: Long = 0L

    init {
        // Initialize with mock data for demonstration
        _update {
            copy(
                memoryUsageBytes = 256L * 1024 * 1024, // 256 MB initial
                memoryLimitBytes = 512L * 1024 * 1024, // 512 MB limit
                memoryUsagePercent = 50f,
                riskLevel = RiskLevel.SAFE,
                limiterEvents = createMockLimiterEvents(),
                moduleMemoryBreakdown = createMockModuleMemory(),
                memoryHistory = createMockMemoryHistory()
            )
        }
    }

    /**
     * ============================================================
     * sendIntent — Intent 处理入口
     * ============================================================
     * Entry point for all user intents. Called from UI layer.
     *
     * @param intent User intent
     * @see MemoryLimitsIntent
     */
    fun sendIntent(intent: MemoryLimitsIntent) {
        when (intent) {
            is MemoryLimitsIntent.StartMonitoring -> startMonitoring()
            is MemoryLimitsIntent.StopMonitoring -> stopMonitoring()
            is MemoryLimitsIntent.ToggleMonitoring -> toggleMonitoring()
            is MemoryLimitsIntent.TriggerHeapDump -> triggerHeapDump(intent.reason)
            is MemoryLimitsIntent.RunComplianceCheck -> runComplianceCheck(intent.targetRam)
            is MemoryLimitsIntent.RunSimulator -> runSimulator(intent.deviceRam)
            is MemoryLimitsIntent.SwitchTab -> switchTab(intent.tab)
            is MemoryLimitsIntent.LoadHistoricalEvents -> loadHistoricalEvents(intent.startTime, intent.endTime)
            is MemoryLimitsIntent.ClearError -> clearError()
            is MemoryLimitsIntent.SelectDevicePreset -> selectDevicePreset(intent.preset)
            is MemoryLimitsIntent.ExportCiReport -> exportCiReport(intent.format)
            is MemoryLimitsIntent.RefreshMemoryData -> refreshMemoryData()
        }
    }

    // ================================================================
    // Intent Handlers
    // ================================================================

    /**
     * StartMonitoring — 开始内存监控
     *
     * Polls memory usage every second and updates state.
     * In production, would use:
     *   - ActivityManager.getProcessMemoryInfo
     *   - ApplicationExitInfo parsing for MemoryLimiter events
     */
    private fun startMonitoring() {
        if (_state.value.monitoringState == MonitoringState.Monitoring) return

        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            _update {
                copy(
                    monitoringState = MonitoringState.Monitoring,
                    isMonitoringPaused = false,
                    errorMessage = null
                )
            }

            try {
                var tick = 0
                while (isActive) {
                    // Simulate memory fluctuation (±5MB per tick)
                    val currentUsage = _state.value.memoryUsageBytes
                    val fluctuation = ((-5L..5L).random()) * 1024 * 1024
                    val newUsage = (currentUsage + fluctuation).coerceIn(50L * 1024 * 1024, _state.value.memoryLimitBytes)

                    val usagePercent = calculateUsagePercent(newUsage, _state.value.memoryLimitBytes)
                    val risk = RiskLevel.fromUsagePercent(usagePercent)

                    // Add to history (keep last 60 data points)
                    val newHistory = _state.value.memoryHistory.toMutableList()
                    newHistory.add(MemoryDataPoint(timestamp = System.currentTimeMillis(), usageBytes = newUsage))
                    if (newHistory.size > 60) newHistory.removeAt(0)

                    _update {
                        copy(
                            memoryUsageBytes = newUsage,
                            memoryUsagePercent = usagePercent,
                            riskLevel = risk,
                            memoryHistory = newHistory
                        )
                    }

                    delay(1000) // Update every second
                    tick++
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _update { copy(monitoringState = MonitoringState.Error, errorMessage = e.message) }
                _effect.emit(MemoryLimitsEffect.ShowError("监控异常: ${e.message}"))
            }
        }

        viewModelScope.launch {
            _effect.emit(MemoryLimitsEffect.ShowToast("内存监控已启动"))
        }
    }

    /**
     * StopMonitoring — 停止内存监控
     */
    private fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _update {
            copy(
                monitoringState = MonitoringState.Idle,
                isMonitoringPaused = false
            )
        }
        viewModelScope.launch {
            _effect.emit(MemoryLimitsEffect.ShowToast("内存监控已停止"))
        }
    }

    /**
     * ToggleMonitoring — 暂停/恢复监控
     */
    private fun toggleMonitoring() {
        val currentPaused = _state.value.isMonitoringPaused
        _update { copy(isMonitoringPaused = !currentPaused) }

        viewModelScope.launch {
            if (!currentPaused) {
                _effect.emit(MemoryLimitsEffect.ShowToast("监控已暂停"))
            } else {
                _effect.emit(MemoryLimitsEffect.ShowToast("监控已恢复"))
            }
        }
    }

    /**
     * TriggerHeapDump — 触发 Heap Dump
     *
     * @param reason 触发原因
     */
    private fun triggerHeapDump(reason: String) {
        viewModelScope.launch {
            try {
                // Simulate heap dump collection
                delay(1500)

                val dumpId = "dump_${System.currentTimeMillis()}"
                val mockResult = HeapDumpResult(
                    id = dumpId,
                    timestamp = System.currentTimeMillis(),
                    filePath = "/data/local/tmp/heap_dump_$dumpId.hprof",
                    fileSizeBytes = (50L..200L).random() * 1024 * 1024,
                    leakingObjects = createMockLeakingObjects(),
                    suspiciousReferences = createMockSuspiciousReferences(),
                    suggestions = listOf(
                        "Bitmap 内存未释放，建议调用 recycle()",
                        "单例对象持有 Activity 引用，导致 Activity 泄漏",
                        "Handler 内部类未在 onDestroy 中移除消息"
                    ),
                    totalLeakingBytes = (20L..80L).random() * 1024 * 1024
                )

                _update {
                    copy(heapDumpResults = listOf(mockResult) + heapDumpResults.take(9))
                }

                _effect.emit(MemoryLimitsEffect.HeapDumpCollected(dumpId))
                _effect.emit(MemoryLimitsEffect.ShowToast("Heap Dump 已采集: $dumpId"))

            } catch (e: Exception) {
                _effect.emit(MemoryLimitsEffect.ShowError("Heap Dump 采集失败: ${e.message}"))
            }
        }
    }

    /**
     * RunComplianceCheck — 运行合规检查
     *
     * @param targetRam 目标设备 RAM（字节）
     */
    private fun runComplianceCheck(targetRam: Long) {
        viewModelScope.launch {
            try {
                _update { copy(ciReport = null) }

                // Simulate compliance check process
                delay(2000)

                val report = createMockCiReport(targetRam)
                _update { copy(ciReport = report) }

                _effect.emit(MemoryLimitsEffect.ShowToast("合规检查完成"))

            } catch (e: Exception) {
                _effect.emit(MemoryLimitsEffect.ShowError("合规检查失败: ${e.message}"))
            }
        }
    }

    /**
     * RunSimulator — 运行内存限制模拟器
     *
     * @param deviceRam 设备 RAM（字节）
     */
    private fun runSimulator(deviceRam: Long) {
        viewModelScope.launch {
            try {
                _update { copy(isSimulatorRunning = true, simulatorResult = null) }

                // Simulate memory pressure test
                delay(3000)

                val currentUsage = _state.value.memoryUsageBytes
                // Android 17 per-app limit = max(128MB, device_ram * 0.1) approximately
                val simulatedLimit = maxOf(128L * 1024 * 1024, (deviceRam * 0.1).toLong())

                val wouldTriggerOom = currentUsage > simulatedLimit
                val usagePercent = calculateUsagePercent(currentUsage, simulatedLimit)
                val risk = RiskLevel.fromUsagePercent(usagePercent)

                val result = SimulatorResult(
                    deviceRam = deviceRam,
                    appMemoryLimit = simulatedLimit,
                    estimatedUsageBytes = currentUsage,
                    wouldTriggerOom = wouldTriggerOom,
                    riskLevel = risk,
                    warnings = if (wouldTriggerOom) {
                        listOf(
                            "当前内存使用量 (${formatBytes(currentUsage)}) 超过模拟器估算上限 (${formatBytes(simulatedLimit)})",
                            "在高内存压力场景下，App 可能被 MemoryLimiter 杀死"
                        )
                    } else {
                        listOf("当前内存使用量在安全范围内")
                    },
                    suggestions = if (wouldTriggerOom) {
                        listOf(
                            "优化 Bitmap 内存占用",
                            "减少不必要的对象缓存",
                            "使用 WeakReference 持有可重建对象",
                            "检查并修复内存泄漏"
                        )
                    } else {
                        listOf("继续保持当前的内存使用习惯")
                    }
                )

                _update { copy(isSimulatorRunning = false, simulatorResult = result) }

            } catch (e: Exception) {
                _update { copy(isSimulatorRunning = false) }
                _effect.emit(MemoryLimitsEffect.ShowError("模拟器运行失败: ${e.message}"))
            }
        }
    }

    /**
     * SwitchTab — 切换 Dashboard Tab
     *
     * @param tab Target tab
     */
    private fun switchTab(tab: DashboardTab) {
        _update { copy(activeTab = tab) }
    }

    /**
     * LoadHistoricalEvents — 加载历史事件
     *
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     */
    private fun loadHistoricalEvents(startTime: Long, endTime: Long) {
        historyStartTime = startTime
        historyEndTime = endTime
        // In production, would query ApplicationExitInfo history
        viewModelScope.launch {
            delay(500)
            _update { copy(limiterEvents = createMockLimiterEvents()) }
        }
    }

    /**
     * ClearError — 清除错误消息
     */
    private fun clearError() {
        _update { copy(errorMessage = null) }
    }

    /**
     * SelectDevicePreset — 选择设备预设
     *
     * @param preset 设备预设
     */
    private fun selectDevicePreset(preset: DeviceRamPreset) {
        _update {
            copy(
                selectedDevicePreset = preset,
                simulatedDeviceRam = if (preset != DeviceRamPreset.Custom) preset.ramBytes else simulatedDeviceRam
            )
        }
    }

    /**
     * ExportCiReport — 导出 CI 报告
     *
     * @param format 导出格式
     */
    private fun exportCiReport(format: ReportFormat) {
        viewModelScope.launch {
            try {
                val report = _state.value.ciReport ?: throw IllegalStateException("No CI report available")
                delay(500)
                val filePath = "/tmp/memory_compliance_report.${format.extension}"
                _effect.emit(MemoryLimitsEffect.ExportCiReportSuccess(filePath))
                _effect.emit(MemoryLimitsEffect.ShowToast("报告已导出至 $filePath"))
            } catch (e: Exception) {
                _effect.emit(MemoryLimitsEffect.ShowError("导出失败: ${e.message}"))
            }
        }
    }

    /**
     * RefreshMemoryData — 刷新内存数据
     */
    private fun refreshMemoryData() {
        viewModelScope.launch {
            try {
                // Simulate refresh
                delay(300)
                _update {
                    copy(
                        moduleMemoryBreakdown = createMockModuleMemory()
                    )
                }
                _effect.emit(MemoryLimitsEffect.ShowToast("数据已刷新"))
            } catch (e: Exception) {
                _effect.emit(MemoryLimitsEffect.ShowError("刷新失败: ${e.message}"))
            }
        }
    }

    // ================================================================
    // Private Helpers
    // ================================================================

    /**
     * update — State update helper using immutable copy
     */
    private inline fun _update(update: MemoryLimitsState.() -> MemoryLimitsState) {
        _state.update { it.update() }
    }

    // ================================================================
    // Mock Data Generators (演示用)
    // ================================================================

    /**
     * createMockLimiterEvents — 创建模拟 MemoryLimiter 事件
     */
    private fun createMockLimiterEvents(): List<LimiterEvent> {
        val now = System.currentTimeMillis()
        return listOf(
            LimiterEvent(
                id = "event_001",
                timestamp = now - 86400000, // 1 day ago
                memoryUsageBytes = 480L * 1024 * 1024,
                memoryLimitBytes = 512L * 1024 * 1024,
                processName = "com.example.myapp",
                reasonDescription = "MemoryLimiter killed process due to exceeding memory limit",
                stackTrace = "at com.example.myapp.MainActivity.onCreate(MainActivity.kt:42)\nat android.app.Activity.performCreate(Activity.java:8124)",
                packageName = "com.example.myapp"
            ),
            LimiterEvent(
                id = "event_002",
                timestamp = now - 172800000, // 2 days ago
                memoryUsageBytes = 500L * 1024 * 1024,
                memoryLimitBytes = 512L * 1024 * 1024,
                processName = "com.example.myapp:bg",
                reasonDescription = "MemoryLimiter killed background process",
                stackTrace = "at com.example.myapp.service.BackgroundService.onHandleIntent(BackgroundService.kt:28)",
                packageName = "com.example.myapp"
            )
        )
    }

    /**
     * createMockModuleMemory — 创建模拟模块内存占用
     */
    private fun createMockModuleMemory(): List<ModuleMemory> {
        val totalUsage = _state.value.memoryUsageBytes.coerceAtLeast(256L * 1024 * 1024)
        return listOf(
            ModuleMemory(
                moduleName = "Bitmap/Images",
                usageBytes = (totalUsage * 0.35).toLong(),
                percentage = 35f,
                riskLevel = RiskLevel.WARNING
            ),
            ModuleMemory(
                moduleName = "Cache",
                usageBytes = (totalUsage * 0.25).toLong(),
                percentage = 25f,
                riskLevel = RiskLevel.SAFE
            ),
            ModuleMemory(
                moduleName = "Native Heap",
                usageBytes = (totalUsage * 0.20).toLong(),
                percentage = 20f,
                riskLevel = RiskLevel.SAFE
            ),
            ModuleMemory(
                moduleName = "Java Heap",
                usageBytes = (totalUsage * 0.15).toLong(),
                percentage = 15f,
                riskLevel = RiskLevel.SAFE
            ),
            ModuleMemory(
                moduleName = "Code/Templates",
                usageBytes = (totalUsage * 0.05).toLong(),
                percentage = 5f,
                riskLevel = RiskLevel.SAFE
            )
        )
    }

    /**
     * createMockMemoryHistory — 创建模拟内存历史曲线
     */
    private fun createMockMemoryHistory(): List<MemoryDataPoint> {
        val now = System.currentTimeMillis()
        val history = mutableListOf<MemoryDataPoint>()
        var baseUsage = 200L * 1024 * 1024
        for (i in 59 downTo 0) {
            baseUsage += ((-5L..5L).random()) * 1024 * 1024
            history.add(MemoryDataPoint(timestamp = now - i * 1000, usageBytes = baseUsage))
        }
        return history
    }

    /**
     * createMockLeakingObjects — 创建模拟泄漏对象
     */
    private fun createMockLeakingObjects(): List<LeakingObject> {
        return listOf(
            LeakingObject(
                className = "android.graphics.Bitmap",
                shallowSizeBytes = 4L * 1024 * 1024,
                retainedSizeBytes = 16L * 1024 * 1024,
                instanceCount = 12,
                children = listOf(
                    LeakingObject(
                        className = "byte[] (pixel data)",
                        shallowSizeBytes = 12L * 1024 * 1024,
                        retainedSizeBytes = 12L * 1024 * 1024,
                        instanceCount = 8
                    )
                )
            ),
            LeakingObject(
                className = "com.example.myapp.MainActivity",
                shallowSizeBytes = 512L * 1024,
                retainedSizeBytes = 8L * 1024 * 1024,
                instanceCount = 3
            )
        )
    }

    /**
     * createMockSuspiciousReferences — 创建模拟可疑引用
     */
    private fun createMockSuspiciousReferences(): List<SuspiciousReference> {
        return listOf(
            SuspiciousReference(
                fromObject = "com.example.myapp.ImageLoader",
                toObject = "android.graphics.Bitmap",
                referenceType = "static field",
                pathDescription = "ImageLoader.sCurrentBitmap (static) → Bitmap"
            ),
            SuspiciousReference(
                fromObject = "com.example.myapp.MainActivity",
                toObject = "android.os.Handler",
                referenceType = "inner class",
                pathDescription = "MainActivity$1 (anonymous Handler) → MainActivity"
            )
        )
    }

    /**
     * createMockCiReport — 创建模拟 CI 报告
     */
    private fun createMockCiReport(targetRam: Long): CiReport {
        val simulatedLimit = maxOf(128L * 1024 * 1024, (targetRam * 0.1).toLong())
        val currentUsage = _state.value.memoryUsageBytes
        val score = if (currentUsage < simulatedLimit * 0.7) 85 else if (currentUsage < simulatedLimit) 65 else 40

        return CiReport(
            id = "ci_report_${System.currentTimeMillis()}",
            timestamp = System.currentTimeMillis(),
            overallStatus = if (score >= 70) ComplianceStatus.PASS else if (score >= 50) ComplianceStatus.WARNING else ComplianceStatus.FAIL,
            overallScore = score,
            moduleReports = listOf(
                ModuleComplianceReport(
                    moduleName = "Image Module",
                    status = if (currentUsage * 0.35 < simulatedLimit * 0.7) ComplianceStatus.PASS else ComplianceStatus.FAIL,
                    riskLevel = RiskLevel.WARNING,
                    memoryUsageBytes = (currentUsage * 0.35).toLong(),
                    memoryLimitBytes = simulatedLimit,
                    suggestions = listOf("使用 inSampleSize 降低图片分辨率", "使用 Glide 的 memoryCacheSize 配置")
                ),
                ModuleComplianceReport(
                    moduleName = "Cache Module",
                    status = ComplianceStatus.PASS,
                    riskLevel = RiskLevel.SAFE,
                    memoryUsageBytes = (currentUsage * 0.25).toLong(),
                    memoryLimitBytes = simulatedLimit,
                    suggestions = listOf("当前缓存大小适中")
                ),
                ModuleComplianceReport(
                    moduleName = "Native Heap",
                    status = ComplianceStatus.WARNING,
                    riskLevel = RiskLevel.WARNING,
                    memoryUsageBytes = (currentUsage * 0.20).toLong(),
                    memoryLimitBytes = simulatedLimit,
                    suggestions = listOf("检查 JNI 代码中的内存分配", "使用 ndk-stack 分析原生堆栈")
                )
            ),
            passedChecks = 5,
            failedChecks = 2,
            warningChecks = 3
        )
    }
}
