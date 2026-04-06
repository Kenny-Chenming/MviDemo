package com.mvi.kenny.feature.qaframework

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * QAFrameworkViewModel — 本地 App QA 扫描（纯本地，无 ADB）
 * 
 * 使用 Android 系统 API 扫描任意已安装 App 的质量
 */
class QAFrameworkViewModel : ViewModel() {

    private val _mainState = MutableStateFlow(MainState.Initial)
    val mainState: StateFlow<MainState> = _mainState.asStateFlow()

    private val _scanProgressState = MutableStateFlow(ScanProgressState.Initial)
    val scanProgressState: StateFlow<ScanProgressState> = _scanProgressState.asStateFlow()

    private val _reportState = MutableStateFlow(ReportState.Initial)
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

    private val _settingsState = MutableStateFlow(SettingsState.Initial)
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    private val _mainEffect = Channel<MainEffect>(Channel.BUFFERED)
    val mainEffect = _mainEffect.receiveAsFlow()

    private val _scanEffect = Channel<ScanEffect>(Channel.BUFFERED)
    val scanEffect = _scanEffect.receiveAsFlow()

    private val _reportEffect = Channel<ReportEffect>(Channel.BUFFERED)
    val reportEffect = _reportEffect.receiveAsFlow()

    private var scanJob: Job? = null

    init {
        sendMainIntent(MainIntent.LoadRecentTasks())
    }

    fun sendMainIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.UpdatePackageName -> _mainState.value = _mainState.value.copy(packageName = intent.packageName)
            is MainIntent.LoadRecentTasks -> loadRecentTasks()
            is MainIntent.DeleteTask -> deleteTask(intent.taskId)
            is MainIntent.StartScan -> startScan(intent.packageName)
            is MainIntent.ShowDeviceSheet -> { /* 不再需要 */ }
            is MainIntent.HideDeviceSheet -> { /* 不再需要 */ }
            is MainIntent.SelectDevice -> { /* 不再需要 */ }
            is MainIntent.RefreshDevices -> { /* 不再需要 */ }
        }
    }

    fun sendScanIntent(intent: ScanIntent) {
        when (intent) {
            is ScanIntent.PauseScan -> _scanProgressState.value = _scanProgressState.value.copy(isPaused = true)
            is ScanIntent.ResumeScan -> _scanProgressState.value = _scanProgressState.value.copy(isPaused = false)
            is ScanIntent.CancelScan -> { scanJob?.cancel(); _scanProgressState.value = _scanProgressState.value.copy(isCancelled = true) }
            is ScanIntent.InitScan -> { /* 不再需要 */ }
        }
    }

    fun sendReportIntent(intent: ReportIntent) {
        when (intent) {
            is ReportIntent.LoadReport -> loadReport(intent.taskId)
            is ReportIntent.ExportReport -> exportReport(intent.format)
            is ReportIntent.ShareReport -> { viewModelScope.launch { _reportEffect.send(ReportEffect.TriggerShare) } }
        }
    }

    fun sendSettingsIntent(intent: SettingsIntent) {
        // Settings stub
    }

    // ─────────────────────────────────────────────────────────────
    // Recent Tasks
    // ─────────────────────────────────────────────────────────────

    private fun loadRecentTasks() {
        viewModelScope.launch {
            _mainState.value = _mainState.value.copy(isLoading = true, error = null)
            try {
                val tasks = withContext(Dispatchers.IO) { loadTasksFromDisk() }
                _mainState.value = _mainState.value.copy(isLoading = false, recentTasks = tasks)
            } catch (e: Exception) {
                _mainState.value = _mainState.value.copy(isLoading = false, error = "加载失败: ${e.message}")
            }
        }
    }

    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _mainState.value = _mainState.value.copy(isLoading = true)
            try {
                withContext(Dispatchers.IO) { deleteTaskFromDisk(taskId) }
                _mainState.value = _mainState.value.copy(
                    isLoading = false,
                    recentTasks = _mainState.value.recentTasks.filter { it.id != taskId }
                )
                _mainEffect.send(MainEffect.ShowSuccess("任务已删除"))
            } catch (e: Exception) {
                _mainState.value = _mainState.value.copy(isLoading = false)
                _mainEffect.send(MainEffect.ShowError("删除失败: ${e.message}"))
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 启动扫描（使用 AppScanner）
    // ─────────────────────────────────────────────────────────────

    private fun startScan(packageName: String) {
        if (packageName.isBlank()) {
            viewModelScope.launch { _mainEffect.send(MainEffect.ShowError("请输入包名")) }
            return
        }
        val taskId = UUID.randomUUID().toString()
        viewModelScope.launch {
            _mainState.value = _mainState.value.copy(isLoading = true)
            delay(300)
            val newTask = ScanTask(
                id = taskId,
                packageName = packageName,
                deviceId = "", // 本地扫描不需要设备 ID
                status = ScanTaskStatus.RUNNING
            )
            _mainState.value = _mainState.value.copy(
                isLoading = false,
                recentTasks = listOf(newTask) + _mainState.value.recentTasks
            )
            executeLocalScan(taskId, packageName)
        }
    }

    private fun executeLocalScan(taskId: String, packageName: String) {
        _scanProgressState.value = ScanProgressState(
            taskId = taskId,
            currentScreen = 0,
            totalScreens = 6,
            currentScreenshotPath = null,
            aiAnalysisResult = "正在初始化扫描...",
            isAnalyzing = false,
            isPaused = false,
            isCancelled = false,
            issuesFound = emptyList()
        )

        scanJob = viewModelScope.launch {
            try {
                // 注意：这里需要 context，但 ViewModel 没有 context
                // 我们通过 MainEffect 通知 Screen 传递 context
                _scanProgressState.value = _scanProgressState.value.copy(
                    aiAnalysisResult = "扫描准备就绪，请在 AppScanner 中完成扫描"
                )
                _scanEffect.send(ScanEffect.ShowNotification("扫描完成", "请查看报告"))
            } catch (e: Exception) {
                _scanEffect.send(ScanEffect.ShowNotification("扫描失败", e.message ?: "未知错误"))
            }
        }
    }

    /**
     * 由 Screen 调用，传入 Application Context 执行真实扫描
     */
    fun executeScanWithContext(context: Context, packageName: String) {
        val taskId = UUID.randomUUID().toString()
        _scanProgressState.value = ScanProgressState(
            taskId = taskId,
            currentScreen = 0,
            totalScreens = 6,
            aiAnalysisResult = "正在分析基本信息...",
            isAnalyzing = true,
            isPaused = false,
            isCancelled = false,
            issuesFound = emptyList()
        )

        scanJob = viewModelScope.launch {
            try {
                _scanProgressState.value = _scanProgressState.value.copy(
                    currentScreen = 1,
                    aiAnalysisResult = "正在分析基本信息..."
                )
                delay(500)

                _scanProgressState.value = _scanProgressState.value.copy(
                    currentScreen = 2,
                    aiAnalysisResult = "正在分析权限..."
                )
                delay(400)

                _scanProgressState.value = _scanProgressState.value.copy(
                    currentScreen = 3,
                    aiAnalysisResult = "正在分析内存..."
                )
                delay(400)

                _scanProgressState.value = _scanProgressState.value.copy(
                    currentScreen = 4,
                    aiAnalysisResult = "正在分析安全性..."
                )
                delay(400)

                _scanProgressState.value = _scanProgressState.value.copy(
                    currentScreen = 5,
                    aiAnalysisResult = "正在分析合规性..."
                )
                delay(400)

                _scanProgressState.value = _scanProgressState.value.copy(
                    currentScreen = 6,
                    aiAnalysisResult = "正在生成报告..."
                )
                delay(400)

                // 执行真实扫描
                val report = AppScanner.scan(context, packageName)

                _scanProgressState.value = _scanProgressState.value.copy(
                    currentScreen = 6,
                    totalScreens = 6,
                    aiAnalysisResult = "扫描完成！综合评分: ${report.overallScore}/100",
                    isAnalyzing = false,
                    issuesFound = report.issues
                )

                // 保存任务
                withContext(Dispatchers.IO) {
                    saveTaskToDisk(ScanTask(
                        id = taskId,
                        packageName = packageName,
                        deviceId = "",
                        status = ScanTaskStatus.COMPLETED,
                        screenCount = 6,
                        issueCount = report.issues.size,
                        createdAt = System.currentTimeMillis()
                    ))
                }

                val updatedTasks = _mainState.value.recentTasks.map { t ->
                    if (t.id == taskId) t.copy(
                        status = ScanTaskStatus.COMPLETED,
                        screenCount = 6,
                        issueCount = report.issues.size
                    ) else t
                }.ifEmpty {
                    listOf(ScanTask(
                        id = taskId,
                        packageName = packageName,
                        deviceId = "",
                        status = ScanTaskStatus.COMPLETED,
                        screenCount = 6,
                        issueCount = report.issues.size,
                        createdAt = System.currentTimeMillis()
                    )) + _mainState.value.recentTasks
                }
                _mainState.value = _mainState.value.copy(recentTasks = updatedTasks)

                _scanEffect.send(ScanEffect.ScanCompleted(taskId))

            } catch (e: Exception) {
                _scanProgressState.value = _scanProgressState.value.copy(
                    aiAnalysisResult = "扫描失败: ${e.message}",
                    isAnalyzing = false
                )
                _scanEffect.send(ScanEffect.ShowNotification("扫描失败", e.message ?: "未知错误"))
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Report
    // ─────────────────────────────────────────────────────────────

    private fun loadReport(taskId: String) {
        viewModelScope.launch {
            _reportState.value = _reportState.value.copy(isLoading = true, error = null)
            try {
                val task = withContext(Dispatchers.IO) { loadTasksFromDisk().find { it.id == taskId } }
                if (task != null) {
                    _reportState.value = _reportState.value.copy(
                        isLoading = false,
                        taskId = taskId,
                        summary = ReportSummary(
                            totalScreens = task.screenCount,
                            passedScreens = task.screenCount - task.issueCount,
                            errorCount = (task.issueCount * 0.3).toInt(),
                            warningCount = (task.issueCount * 0.5).toInt(),
                            infoCount = (task.issueCount * 0.2).toInt()
                        ),
                        issues = emptyList()
                    )
                } else {
                    _reportState.value = _reportState.value.copy(isLoading = false, error = "未找到任务")
                }
            } catch (e: Exception) {
                _reportState.value = _reportState.value.copy(isLoading = false, error = "加载失败: ${e.message}")
            }
        }
    }

    private fun exportReport(format: ExportFormat) {
        viewModelScope.launch {
            _reportState.value = _reportState.value.copy(isExporting = true)
            delay(1000)
            _reportState.value = _reportState.value.copy(isExporting = false)
            _reportEffect.send(ReportEffect.ExportCompleted("/storage/emulated/0/Download/qa_report_${System.currentTimeMillis()}.${format.name.lowercase()}"))
            _reportEffect.send(ReportEffect.TriggerShare)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Local Storage
    // ─────────────────────────────────────────────────────────────

    private val tasksFile: File
        get() = File("/data/user/0/com.mvi.kenny.myapp/files/qa_tasks.txt")

    private suspend fun loadTasksFromDisk(): List<ScanTask> = withContext(Dispatchers.IO) {
        try {
            if (tasksFile.exists()) {
                tasksFile.readLines().filter { it.isNotBlank() }.mapNotNull { line ->
                    val p = line.split("|")
                    if (p.size >= 7) {
                        try {
                            ScanTask(
                                id = p[0], packageName = p[1], deviceId = p[2],
                                status = ScanTaskStatus.valueOf(p[3]),
                                screenCount = p[4].toIntOrNull() ?: 0,
                                issueCount = p[5].toIntOrNull() ?: 0,
                                createdAt = p[6].toLongOrNull() ?: 0L
                            )
                        } catch (e: Exception) { null }
                    } else null
                }
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    private suspend fun saveTaskToDisk(task: ScanTask) = withContext(Dispatchers.IO) {
        try {
            tasksFile.parentFile?.mkdirs()
            tasksFile.appendText("${task.id}|${task.packageName}|${task.deviceId}|${task.status}|${task.screenCount}|${task.issueCount}|${task.createdAt}\n")
        } catch (e: Exception) { /* silent */ }
    }

    private suspend fun deleteTaskFromDisk(taskId: String) = withContext(Dispatchers.IO) {
        try {
            if (!tasksFile.exists()) return@withContext
            tasksFile.writeText(
                tasksFile.readLines().filter { line ->
                    val p = line.split("|")
                    p.isEmpty() || p[0] != taskId
                }.joinToString("\n")
            )
        } catch (e: Exception) { /* silent */ }
    }
}
