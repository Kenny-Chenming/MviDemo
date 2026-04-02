package com.mvi.kenny.feature.qaframework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ============================================================
 * QAFrameworkViewModel — AI驱动QA框架 ViewModel
 * ============================================================
 * 持有四个子状态的统一 ViewModel：
 * - MainState: 主界面状态
 * - ScanProgressState: 扫描进度状态
 * - ReportState: 报告状态
 * - SettingsState: 设置状态
 *
 * 每个子状态独立管理，通过 Channel 处理各自的 Effect。
 *
 * Stub 实现说明：
 * - 扫描流程使用模拟进度（定时更新 progress）
 * - 设备连接使用桩数据（模拟设备列表）
 * - 报告生成使用桩数据（模拟问题列表）
 * - 后续可接入真实 CDP 连接和 AI 分析 API
 *
 * @see QAFrameworkContract 各 State / Intent / Effect 定义
 */
class QAFrameworkViewModel : ViewModel() {

    // ================================================================
    // State Management — 状态管理
    // ================================================================

    private val _mainState = MutableStateFlow(MainState.Initial)
    val mainState: StateFlow<MainState> = _mainState.asStateFlow()

    private val _scanProgressState = MutableStateFlow(ScanProgressState.Initial)
    val scanProgressState: StateFlow<ScanProgressState> = _scanProgressState.asStateFlow()

    private val _reportState = MutableStateFlow(ReportState.Initial)
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

    private val _settingsState = MutableStateFlow(SettingsState.Initial)
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    // ================================================================
    // Effect Channels — 副作用通道
    // ================================================================

    private val _mainEffect = Channel<MainEffect>(Channel.BUFFERED)
    val mainEffect = _mainEffect.receiveAsFlow()

    private val _scanEffect = Channel<ScanEffect>(Channel.BUFFERED)
    val scanEffect = _scanEffect.receiveAsFlow()

    private val _reportEffect = Channel<ReportEffect>(Channel.BUFFERED)
    val reportEffect = _reportEffect.receiveAsFlow()

    // ================================================================
    // Current State Snapshots — 当前状态快照（供 Compose lambda 使用）
    // ================================================================

    val currentMainState: MainState get() = _mainState.value
    val currentScanState: ScanProgressState get() = _scanProgressState.value
    val currentReportState: ReportState get() = _reportState.value
    val currentSettingsState: SettingsState get() = _settingsState.value

    init {
        // ViewModel 创建时自动加载最近任务和设备列表
        sendMainIntent(MainIntent.LoadRecentTasks())
        sendMainIntent(MainIntent.RefreshDevices)
    }

    // ================================================================
    // Intent Handlers — 意图处理入口
    // ================================================================

    /**
     * 处理主界面 Intent
     * @param intent 主界面用户意图
     */
    fun sendMainIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.UpdatePackageName -> updatePackageName(intent.packageName)
            is MainIntent.LoadRecentTasks -> loadRecentTasks(intent.limit)
            is MainIntent.DeleteTask -> deleteTask(intent.taskId)
            is MainIntent.StartScan -> startScan(intent.packageName, intent.deviceId)
            is MainIntent.ShowDeviceSheet -> showDeviceSheet()
            is MainIntent.HideDeviceSheet -> hideDeviceSheet()
            is MainIntent.SelectDevice -> selectDevice(intent.deviceId)
            is MainIntent.RefreshDevices -> refreshDevices()
        }
    }

    /**
     * 处理扫描进度 Intent
     * @param intent 扫描进度用户意图
     */
    fun sendScanIntent(intent: ScanIntent) {
        when (intent) {
            is ScanIntent.PauseScan -> pauseScan()
            is ScanIntent.ResumeScan -> resumeScan()
            is ScanIntent.CancelScan -> cancelScan()
            is ScanIntent.InitScan -> initScan(intent.taskId, intent.packageName, intent.deviceId)
        }
    }

    /**
     * 处理报告 Intent
     * @param intent 报告用户意图
     */
    fun sendReportIntent(intent: ReportIntent) {
        when (intent) {
            is ReportIntent.LoadReport -> loadReport(intent.taskId)
            is ReportIntent.ExportReport -> exportReport(intent.format)
            is ReportIntent.ShareReport -> shareReport(intent.taskId)
        }
    }

    /**
     * 处理设置 Intent
     * @param intent 设置用户意图
     */
    fun sendSettingsIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.UpdateCdpHost -> updateCdpHost(intent.host)
            is SettingsIntent.UpdateCdpPort -> updateCdpPort(intent.port)
            is SettingsIntent.UpdateAIModel -> updateAIModel(intent.model)
            is SettingsIntent.UpdateParallelDevices -> updateParallelDevices(intent.count)
            is SettingsIntent.UpdateScreenshotQuality -> updateScreenshotQuality(intent.quality)
            is SettingsIntent.UpdateExportFormat -> updateExportFormat(intent.format)
        }
    }

    // ================================================================
    // MainState Handlers — 主界面状态处理
    // ================================================================

    /**
     * 更新包名输入
     */
    private fun updatePackageName(packageName: String) {
        _mainState.value = _mainState.value.copy(packageName = packageName)
    }

    /**
     * 加载最近任务列表（桩实现 — 模拟 3 个历史任务）
     * TODO: 后续接入 Room Database 获取真实历史数据
     */
    private fun loadRecentTasks(limit: Int) {
        viewModelScope.launch {
            _mainState.value = _mainState.value.copy(isLoading = true, error = null)
            try {
                delay(300) // 模拟数据库查询
                val mockTasks = listOf(
                    ScanTask(
                        id = UUID.randomUUID().toString(),
                        packageName = "com.example.app",
                        deviceId = "emulator-5554",
                        status = ScanTaskStatus.COMPLETED,
                        screenCount = 25,
                        issueCount = 3,
                        createdAt = System.currentTimeMillis() - 3600_000
                    ),
                    ScanTask(
                        id = UUID.randomUUID().toString(),
                        packageName = "com.example.app",
                        deviceId = "emulator-5554",
                        status = ScanTaskStatus.COMPLETED,
                        screenCount = 18,
                        issueCount = 1,
                        createdAt = System.currentTimeMillis() - 86400_000
                    ),
                    ScanTask(
                        id = UUID.randomUUID().toString(),
                        packageName = "com.example.webview",
                        deviceId = "device-abc123",
                        status = ScanTaskStatus.FAILED,
                        screenCount = 5,
                        issueCount = 0,
                        createdAt = System.currentTimeMillis() - 172800_000
                    )
                )
                _mainState.value = _mainState.value.copy(
                    isLoading = false,
                    recentTasks = mockTasks.take(limit)
                )
            } catch (e: Exception) {
                _mainState.value = _mainState.value.copy(
                    isLoading = false,
                    error = "加载历史任务失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 删除指定任务
     */
    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _mainState.value = _mainState.value.copy(isLoading = true)
            try {
                delay(200)
                val updatedTasks = _mainState.value.recentTasks.filter { it.id != taskId }
                _mainState.value = _mainState.value.copy(
                    isLoading = false,
                    recentTasks = updatedTasks
                )
                _mainEffect.send(MainEffect.ShowSuccess("任务已删除"))
            } catch (e: Exception) {
                _mainState.value = _mainState.value.copy(isLoading = false)
                _mainEffect.send(MainEffect.ShowError("删除失败: ${e.message}"))
            }
        }
    }

    /**
     * 开始扫描（桩实现 — 启动模拟扫描流程）
     * TODO: 后续接入真实 CDP + ADB 截图逻辑
     */
    private fun startScan(packageName: String, deviceId: String) {
        val taskId = UUID.randomUUID().toString()
        viewModelScope.launch {
            _mainState.value = _mainState.value.copy(isLoading = true)
            try {
                delay(300)
                // 创建新任务并添加到列表
                val newTask = ScanTask(
                    id = taskId,
                    packageName = packageName,
                    deviceId = deviceId,
                    status = ScanTaskStatus.RUNNING
                )
                val updatedTasks = listOf(newTask) + _mainState.value.recentTasks
                _mainState.value = _mainState.value.copy(
                    isLoading = false,
                    recentTasks = updatedTasks,
                    showDeviceSheet = false
                )
                // 初始化扫描状态并启动模拟扫描
                sendScanIntent(ScanIntent.InitScan(taskId, packageName, deviceId))
                _mainEffect.send(MainEffect.NavigateToScan(taskId))
            } catch (e: Exception) {
                _mainState.value = _mainState.value.copy(isLoading = false)
                _mainEffect.send(MainEffect.ShowError("启动扫描失败: ${e.message}"))
            }
        }
    }

    private fun showDeviceSheet() {
        _mainState.value = _mainState.value.copy(showDeviceSheet = true)
    }

    private fun hideDeviceSheet() {
        _mainState.value = _mainState.value.copy(showDeviceSheet = false)
    }

    private fun selectDevice(deviceId: String) {
        _mainState.value = _mainState.value.copy(
            selectedDeviceId = deviceId,
            showDeviceSheet = false
        )
    }

    /**
     * 刷新设备列表（桩实现 — 模拟 2 个可用设备）
     * TODO: 后续接入真实 ADB 设备查询
     */
    private fun refreshDevices() {
        viewModelScope.launch {
            try {
                delay(500) // 模拟 ADB 查询延迟
                val mockDevices = listOf(
                    ConnectedDevice(
                        id = "emulator-5554",
                        name = "Pixel 6 Pro (Emulator)",
                        androidVersion = "Android 14",
                        isConnected = true
                    ),
                    ConnectedDevice(
                        id = "device-abc123",
                        name = "Samsung Galaxy S23",
                        androidVersion = "Android 13",
                        isConnected = true
                    )
                )
                _mainState.value = _mainState.value.copy(
                    availableDevices = mockDevices,
                    selectedDeviceId = _mainState.value.selectedDeviceId
                        ?: mockDevices.firstOrNull()?.id
                )
            } catch (e: Exception) {
                _mainEffect.send(MainEffect.ShowError("刷新设备列表失败: ${e.message}"))
            }
        }
    }

    // ================================================================
    // ScanProgressState Handlers — 扫描进度状态处理
    // ================================================================

    /**
     * 初始化扫描（桩实现 — 模拟扫描 10 个屏幕）
     * TODO: 后续接入真实 CDP 连接和截图流程
     */
    private fun initScan(taskId: String, packageName: String, deviceId: String) {
        _scanProgressState.value = ScanProgressState(
            taskId = taskId,
            currentScreen = 0,
            totalScreens = 10,
            currentScreenshotPath = null,
            aiAnalysisResult = null,
            isAnalyzing = false,
            isPaused = false,
            isCancelled = false,
            issuesFound = emptyList()
        )
        // 启动模拟扫描协程
        viewModelScope.launch {
            simulateScanLoop(taskId)
        }
    }

    /**
     * 模拟扫描循环（桩实现）
     * 每秒更新一次进度，模拟截图 + AI 分析流程
     */
    private suspend fun simulateScanLoop(taskId: String) {
        val total = _scanProgressState.value.totalScreens
        for (screenIndex in 0 until total) {
            // 检查是否已取消
            if (_scanProgressState.value.isCancelled) break
            // 检查是否暂停
            while (_scanProgressState.value.isPaused) {
                delay(500)
                if (_scanProgressState.value.isCancelled) return
            }

            // 模拟截图 + AI 分析耗时（1-2 秒）
            _scanProgressState.value = _scanProgressState.value.copy(
                currentScreen = screenIndex,
                isAnalyzing = true,
                aiAnalysisResult = "正在分析屏幕 ${screenIndex + 1}..."
            )
            delay(800)

            // 模拟发现问题的场景（随机）
            val newIssues = if ((screenIndex % 3) == 0 && screenIndex > 0) {
                val issue = QAIssue(
                    id = UUID.randomUUID().toString(),
                    screenIndex = screenIndex,
                    screenshotPath = "/mock/screenshot_$screenIndex.png",
                    description = "屏幕 ${screenIndex + 1} 发现疑似布局溢出问题，建议检查 ${screenIndex + 1} 区域的元素宽度。",
                    severity = if (screenIndex % 6 == 0) IssueSeverity.ERROR else IssueSeverity.WARNING,
                    suggestions = listOf(
                        "检查容器宽度是否足够",
                        "验证子元素的约束条件",
                        "考虑使用 ScrollView 包装过长内容"
                    )
                )
                _scanProgressState.value.issuesFound + issue
            } else {
                _scanProgressState.value.issuesFound
            }

            _scanProgressState.value = _scanProgressState.value.copy(
                currentScreen = screenIndex + 1,
                isAnalyzing = false,
                aiAnalysisResult = "屏幕 ${screenIndex + 1} 分析完成",
                issuesFound = newIssues
            )

            delay(400) // 模拟滑动到下一屏的间隔
        }

        // 扫描完成
        if (!_scanProgressState.value.isCancelled) {
            _scanProgressState.value = _scanProgressState.value.copy(
                aiAnalysisResult = "扫描完成！共发现 ${_scanProgressState.value.issuesFound.size} 个问题"
            )
            _scanEffect.send(ScanEffect.ScanCompleted(taskId))
            _scanEffect.send(
                ScanEffect.ShowNotification(
                    title = "扫描完成",
                    body = "共扫描 ${total} 个屏幕，发现 ${_scanProgressState.value.issuesFound.size} 个问题"
                )
            )
        }
    }

    /**
     * 暂停扫描
     */
    private fun pauseScan() {
        _scanProgressState.value = _scanProgressState.value.copy(isPaused = true)
    }

    /**
     * 继续扫描
     */
    private fun resumeScan() {
        _scanProgressState.value = _scanProgressState.value.copy(isPaused = false)
    }

    /**
     * 取消扫描
     */
    private fun cancelScan() {
        _scanProgressState.value = _scanProgressState.value.copy(isCancelled = true)
    }

    // ================================================================
    // ReportState Handlers — 报告状态处理
    // ================================================================

    /**
     * 加载报告（桩实现）
     * TODO: 后续接入真实报告数据
     */
    private fun loadReport(taskId: String) {
        viewModelScope.launch {
            _reportState.value = _reportState.value.copy(isLoading = true, error = null)
            try {
                delay(500)
                val mockIssues = listOf(
                    QAIssue(
                        id = UUID.randomUUID().toString(),
                        screenIndex = 0,
                        screenshotPath = "/mock/screenshot_0.png",
                        description = "首页 Banner 区域存在文字截断问题，在小屏设备上最后一字无法完整显示。",
                        severity = IssueSeverity.ERROR,
                        suggestions = listOf(
                            "使用 ellipsize = TextOverflow.Ellipsis 代替直接截断",
                            "或增加 Banner 高度以容纳长文本",
                            "考虑使用自适应字号的 Text 组件"
                        )
                    ),
                    QAIssue(
                        id = UUID.randomUUID().toString(),
                        screenIndex = 3,
                        screenshotPath = "/mock/screenshot_3.png",
                        description = "列表项点击区域偏小（仅 32dp），低于 Material Design 最小触摸目标 48dp。",
                        severity = IssueSeverity.WARNING,
                        suggestions = listOf(
                            "增加列表项的 minHeight 至 48dp",
                            "使用 ButtonDefaults.makeMinimumHeight() 确保一致性"
                        )
                    ),
                    QAIssue(
                        id = UUID.randomUUID().toString(),
                        screenIndex = 6,
                        screenshotPath = "/mock/screenshot_6.png",
                        description = "表单提交按钮在输入错误时未提供清晰的错误反馈。",
                        severity = IssueSeverity.INFO,
                        suggestions = listOf(
                            "在按钮下方显示具体错误信息",
                            "使用 TextField 的 isError 状态高亮问题字段"
                        )
                    )
                )
                val summary = ReportSummary(
                    totalScreens = 10,
                    passedScreens = 7,
                    errorCount = mockIssues.count { it.severity == IssueSeverity.ERROR },
                    warningCount = mockIssues.count { it.severity == IssueSeverity.WARNING },
                    infoCount = mockIssues.count { it.severity == IssueSeverity.INFO }
                )
                _reportState.value = _reportState.value.copy(
                    isLoading = false,
                    taskId = taskId,
                    summary = summary,
                    issues = mockIssues
                )
            } catch (e: Exception) {
                _reportState.value = _reportState.value.copy(
                    isLoading = false,
                    error = "加载报告失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 导出报告（桩实现）
     */
    private fun exportReport(format: ExportFormat) {
        viewModelScope.launch {
            _reportState.value = _reportState.value.copy(isExporting = true)
            try {
                delay(1000) // 模拟导出耗时
                _reportState.value = _reportState.value.copy(isExporting = false)
                _reportEffect.send(ReportEffect.ExportCompleted("/mock/report.${format.name.lowercase()}"))
                _reportEffect.send(ReportEffect.TriggerShare)
            } catch (e: Exception) {
                _reportState.value = _reportState.value.copy(isExporting = false)
                _reportEffect.send(ReportEffect.ExportFailed(e.message ?: "导出失败"))
            }
        }
    }

    /**
     * 分享报告（桩实现）
     */
    private fun shareReport(taskId: String) {
        viewModelScope.launch {
            _reportEffect.send(ReportEffect.TriggerShare)
        }
    }

    // ================================================================
    // SettingsState Handlers — 设置状态处理
    // ================================================================

    private fun updateCdpHost(host: String) {
        _settingsState.value = _settingsState.value.copy(cdpHost = host)
    }

    private fun updateCdpPort(port: Int) {
        _settingsState.value = _settingsState.value.copy(cdpPort = port)
    }

    private fun updateAIModel(model: AIModel) {
        _settingsState.value = _settingsState.value.copy(aiModel = model)
    }

    private fun updateParallelDevices(count: Int) {
        _settingsState.value = _settingsState.value.copy(
            parallelDevices = count.coerceIn(1, 5)
        )
    }

    private fun updateScreenshotQuality(quality: Int) {
        _settingsState.value = _settingsState.value.copy(
            screenshotQuality = quality.coerceIn(10, 100)
        )
    }

    private fun updateExportFormat(format: ExportFormat) {
        _settingsState.value = _settingsState.value.copy(exportFormat = format)
    }
}
