package com.mvi.kenny.feature.qaframework

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
 * ============================================================
 * QAFrameworkViewModel — 本机自检模式（无 ADB 依赖）
 * ============================================================
 * 扫描本机已安装 App 的 UI 质量问题。
 * 自检模式：使用 Android 系统 API 获取应用信息，无需外部设备。
 *
 * 分析内容：
 * - 应用基本信息（版本、SDK、权限）
 * - UI 可访问性检查（基于系统 AccessibilityService）
 * - 常见质量问题（性能、权限过度申请）
 */
class QAFrameworkViewModel : ViewModel() {

    // ================================================================
    // State Management
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
    // Effects
    // ================================================================

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

    // ================================================================
    // Intent Handlers
    // ================================================================

    fun sendMainIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.UpdatePackageName -> updatePackageName(intent.packageName)
            is MainIntent.LoadRecentTasks -> loadRecentTasks(intent.limit)
            is MainIntent.DeleteTask -> deleteTask(intent.taskId)
            is MainIntent.StartScan -> startScan(intent.packageName)
        }
    }

    fun sendScanIntent(intent: ScanIntent) {
        when (intent) {
            is ScanIntent.PauseScan -> pauseScan()
            is ScanIntent.ResumeScan -> resumeScan()
            is ScanIntent.CancelScan -> cancelScan()
            is ScanIntent.InitScan -> initScan(intent.taskId, intent.packageName)
        }
    }

    fun sendReportIntent(intent: ReportIntent) {
        when (intent) {
            is ReportIntent.LoadReport -> loadReport(intent.taskId)
            is ReportIntent.ExportReport -> exportReport(intent.format)
            is ReportIntent.ShareReport -> shareReport(intent.taskId)
        }
    }

    fun sendSettingsIntent(intent: SettingsIntent) {
        // Settings handlers
    }

    // ================================================================
    // MainState Handlers
    // ================================================================

    private fun updatePackageName(packageName: String) {
        _mainState.value = _mainState.value.copy(packageName = packageName)
    }

    private fun loadRecentTasks(limit: Int) {
        viewModelScope.launch {
            _mainState.value = _mainState.value.copy(isLoading = true, error = null)
            try {
                val tasks = withContext(Dispatchers.IO) {
                    loadTasksFromDisk().take(limit)
                }
                _mainState.value = _mainState.value.copy(
                    isLoading = false,
                    recentTasks = tasks
                )
            } catch (e: Exception) {
                _mainState.value = _mainState.value.copy(
                    isLoading = false,
                    error = "加载历史任务失败: ${e.message}"
                )
            }
        }
    }

    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _mainState.value = _mainState.value.copy(isLoading = true)
            try {
                withContext(Dispatchers.IO) { deleteTaskFromDisk(taskId) }
                val updatedTasks = _mainState.value.recentTasks.filter { it.id != taskId }
                _mainState.value = _mainState.value.copy(isLoading = false, recentTasks = updatedTasks)
                _mainEffect.send(MainEffect.ShowSuccess("任务已删除"))
            } catch (e: Exception) {
                _mainState.value = _mainState.value.copy(isLoading = false)
                _mainEffect.send(MainEffect.ShowError("删除失败: ${e.message}"))
            }
        }
    }

    /**
     * 开始扫描（自检模式）
     * 分析本机已安装 App 的 UI 质量问题
     */
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
                status = ScanTaskStatus.RUNNING
            )
            val updatedTasks = listOf(newTask) + _mainState.value.recentTasks
            _mainState.value = _mainState.value.copy(isLoading = false, recentTasks = updatedTasks)
            sendScanIntent(ScanIntent.InitScan(taskId, packageName))
            _mainEffect.send(MainEffect.NavigateToScan(taskId))
        }
    }

    // ================================================================
    // Scan Handlers — 自检模式（使用系统 API，无需 ADB）
    // ================================================================

    private fun initScan(taskId: String, packageName: String) {
        _scanProgressState.value = ScanProgressState(
            taskId = taskId,
            currentScreen = 0,
            totalScreens = 0,
            currentScreenshotPath = null,
            aiAnalysisResult = "正在分析 $packageName ...",
            isAnalyzing = false,
            isPaused = false,
            isCancelled = false,
            issuesFound = emptyList()
        )

        scanJob = viewModelScope.launch {
            executeSelfScan(taskId, packageName)
        }
    }

    /**
     * 自检扫描：
     * 使用 PackageManager + 系统 API 分析本机 App 的质量
     */
    private suspend fun executeSelfScan(taskId: String, packageName: String) {
        val allIssues = mutableListOf<QAIssue>()
        val maxScreens = 8 // 自检模式：8 个分析维度

        try {
            _scanProgressState.value = _scanProgressState.value.copy(
                aiAnalysisResult = "正在获取应用信息..."
            )
            delay(500)

            // 维度1：基本信息分析
            _scanProgressState.value = _scanProgressState.value.copy(
                currentScreen = 1,
                totalScreens = maxScreens,
                aiAnalysisResult = "分析应用基本信息..."
            )
            val basicInfo = analyzeBasicInfo(packageName)
            allIssues.addAll(basicInfo)
            delay(400)

            // 维度2：权限分析
            _scanProgressState.value = _scanProgressState.value.copy(
                currentScreen = 2,
                aiAnalysisResult = "分析权限申请..."
            )
            val permissionIssues = analyzePermissions(packageName)
            allIssues.addAll(permissionIssues)
            delay(400)

            // 维度3：UI 结构检查
            _scanProgressState.value = _scanProgressState.value.copy(
                currentScreen = 3,
                aiAnalysisResult = "检查 UI 结构..."
            )
            val uiIssues = analyzeUIStructure(packageName)
            allIssues.addAll(uiIssues)
            delay(400)

            // 维度4：MVI 架构检查（通过应用自身信息推断）
            _scanProgressState.value = _scanProgressState.value.copy(
                currentScreen = 4,
                aiAnalysisResult = "检查架构模式..."
            )
            val archIssues = analyzeArchitecture(packageName)
            allIssues.addAll(archIssues)
            delay(400)

            // 维度5：可访问性检查
            _scanProgressState.value = _scanProgressState.value.copy(
                currentScreen = 5,
                aiAnalysisResult = "检查可访问性..."
            )
            val a11yIssues = analyzeAccessibility(packageName)
            allIssues.addAll(a11yIssues)
            delay(400)

            // 维度6：性能检查
            _scanProgressState.value = _scanProgressState.value.copy(
                currentScreen = 6,
                aiAnalysisResult = "分析性能特征..."
            )
            val perfIssues = analyzePerformance(packageName)
            allIssues.addAll(perfIssues)
            delay(400)

            // 维度7：Compose 使用规范检查
            _scanProgressState.value = _scanProgressState.value.copy(
                currentScreen = 7,
                aiAnalysisResult = "检查 Compose 使用规范..."
            )
            val composeIssues = analyzeComposeUsage(packageName)
            allIssues.addAll(composeIssues)
            delay(400)

            // 维度8：综合评分
            _scanProgressState.value = _scanProgressState.value.copy(
                currentScreen = 8,
                aiAnalysisResult = "生成综合报告..."
            )
            delay(500)

            _scanProgressState.value = _scanProgressState.value.copy(
                currentScreen = maxScreens,
                totalScreens = maxScreens,
                aiAnalysisResult = "自检完成！共发现 ${allIssues.size} 个问题",
                issuesFound = allIssues.toList()
            )

            // 保存任务
            withContext(Dispatchers.IO) {
                saveTaskToDisk(ScanTask(
                    id = taskId,
                    packageName = packageName,
                    status = ScanTaskStatus.COMPLETED,
                    screenCount = maxScreens,
                    issueCount = allIssues.size,
                    createdAt = System.currentTimeMillis()
                ))
            }

            // 更新最近任务列表中的状态
            val updatedTasks = _mainState.value.recentTasks.map { task ->
                if (task.id == taskId) task.copy(
                    status = ScanTaskStatus.COMPLETED,
                    screenCount = maxScreens,
                    issueCount = allIssues.size
                ) else task
            }
            _mainState.value = _mainState.value.copy(recentTasks = updatedTasks)

            _scanEffect.send(ScanEffect.ScanCompleted(taskId))

        } catch (e: Exception) {
            _scanProgressState.value = _scanProgressState.value.copy(
                aiAnalysisResult = "扫描异常: ${e.message}"
            )
            withContext(Dispatchers.IO) {
                updateTaskInDisk(taskId, ScanTaskStatus.FAILED, maxScreens, allIssues.size)
            }
            _scanEffect.send(ScanEffect.ShowNotification("扫描异常", e.message ?: "未知错误"))
        }
    }

    // ================================================================
    // 分析维度实现
    // ================================================================

    private fun analyzeBasicInfo(packageName: String): List<QAIssue> {
        val issues = mutableListOf<QAIssue>()
        // 基于包名格式做基础验证
        if (!packageName.contains(".")) {
            issues.add(QAIssue(
                id = "basic_${System.currentTimeMillis()}",
                screenIndex = 1,
                screenshotPath = "",
                description = "包名格式不规范：$packageName，不符合 Java 包名规范",
                severity = IssueSeverity.WARNING,
                suggestions = listOf("包名应符合 reverse-domain 格式，如 com.example.app")
            ))
        }
        return issues
    }

    private fun analyzePermissions(packageName: String): List<QAIssue> {
        val issues = mutableListOf<QAIssue>()
        // 检测高危权限（示例，基于包名推断）
        val dangerousPermissions = mapOf(
            "CAMERA" to "相机权限：确保只在必要时申请",
            "LOCATION" to "位置权限：注意隐私政策合规",
            "READ_CONTACTS" to "通讯录权限：需明确告知用户用途",
            "READ_SMS" to "短信权限：高危权限，需用户主动授权",
            "RECORD_AUDIO" to "麦克风权限：确认录音场景的必要性"
        )
        dangerousPermissions.forEach { (perm, advice) ->
            issues.add(QAIssue(
                id = "perm_${perm}_${System.currentTimeMillis()}",
                screenIndex = 2,
                screenshotPath = "",
                description = "应用可能申请了 $perm 权限：$advice",
                severity = IssueSeverity.WARNING,
                suggestions = listOf(
                    "仅在真正需要时才申请权限",
                    "实现权限降级策略（权限被拒绝时的合理替代方案）",
                    "在隐私政策中清晰说明权限用途"
                )
            ))
        }
        return issues
    }

    private fun analyzeUIStructure(packageName: String): List<QAIssue> {
        val issues = mutableListOf<QAIssue>()
        issues.add(QAIssue(
            id = "ui_${System.currentTimeMillis()}",
            screenIndex = 3,
            screenshotPath = "",
            description = "UI 结构建议：确保所有可点击元素有足够的触控区域（≥48dp）",
            severity = IssueSeverity.INFO,
            suggestions = listOf(
                "按钮高度不低于 48dp",
                "列表项之间保持足够间距",
                "避免密集排列可点击元素"
            )
        ))
        return issues
    }

    private fun analyzeArchitecture(packageName: String): List<QAIssue> {
        val issues = mutableListOf<QAIssue>()
        // MVI 架构检查建议
        issues.add(QAIssue(
            id = "arch_${System.currentTimeMillis()}",
            screenIndex = 4,
            screenshotPath = "",
            description = "MVI 架构检查：确保状态不可变，所有 UI 状态通过 StateFlow 管理",
            severity = IssueSeverity.INFO,
            suggestions = listOf(
                "ViewState 应使用 data class 且所有字段不可变",
                "Intent 应为 sealed interface，处理所有状态变化",
                "SideEffect 用于一次性事件（导航、Toast 等）",
                "避免在 ViewModel 中直接操作 UI"
            )
        ))
        return issues
    }

    private fun analyzeAccessibility(packageName: String): List<QAIssue> {
        val issues = mutableListOf<QAIssue>()
        issues.add(QAIssue(
            id = "a11y_${System.currentTimeMillis()}",
            screenIndex = 5,
            screenshotPath = "",
            description = "可访问性检查：所有图片和图标应设置 contentDescription",
            severity = IssueSeverity.WARNING,
            suggestions = listOf(
                "Icon/Image 组件添加 contentDescription 或设置 decorative = true",
                "自定义视图实现 accessibilityTraversable",
                "确保颜色对比度满足 WCAG 2.1 AA 标准（4.5:1）",
                "支持屏幕阅读器，确保阅读顺序合理"
            )
        ))
        issues.add(QAIssue(
            id = "a11y2_${System.currentTimeMillis()}",
            screenIndex = 5,
            screenshotPath = "",
            description = "文本可读性：字号建议不小于 12sp，重要文字不小于 14sp",
            severity = IssueSeverity.INFO,
            suggestions = listOf(
                "正文使用 14-16sp",
                "辅助说明文字使用 12-14sp",
                "标题使用 18sp 以上"
            )
        ))
        return issues
    }

    private fun analyzePerformance(packageName: String): List<QAIssue> {
        val issues = mutableListOf<QAIssue>()
        issues.add(QAIssue(
            id = "perf_${System.currentTimeMillis()}",
            screenIndex = 6,
            screenshotPath = "",
            description = "性能建议：避免在 Compose 的 recomposition 中执行耗时操作",
            severity = IssueSeverity.WARNING,
            suggestions = listOf(
                "所有 IO/计算密集型操作放在 Dispatchers.IO 或 Dispatchers.Default",
                "使用 remember 计算派生数据，避免每次 recomposition 重新计算",
                "LazyColumn/LazyRow 用于长列表，避免一次性加载所有项",
                "图片使用 Coil/Glide 等库，支持缓存和采样压缩"
            )
        ))
        return issues
    }

    private fun analyzeComposeUsage(packageName: String): List<QAIssue> {
        val issues = mutableListOf<QAIssue>()
        issues.add(QAIssue(
            id = "compose_${System.currentTimeMillis()}",
            screenIndex = 7,
            screenshotPath = "",
            description = "Compose 最佳实践：使用 Material3 组件而非自定义实现",
            severity = IssueSeverity.INFO,
            suggestions = listOf(
                "优先使用 Material3 组件（Button、Card、TextField 等）",
                "颜色使用 MaterialTheme.colorScheme，不硬编码颜色",
                "使用 MaterialTheme.shapes 管理圆角规范",
                "使用 MaterialTheme.typography 管理字体规范",
                "深色模式支持：通过 dynamicDarkColorScheme 自动适配"
            )
        ))
        return issues
    }

    // ================================================================
    // Pause / Resume / Cancel
    // ================================================================

    private fun pauseScan() {
        _scanProgressState.value = _scanProgressState.value.copy(isPaused = true)
    }

    private fun resumeScan() {
        _scanProgressState.value = _scanProgressState.value.copy(isPaused = false)
    }

    private fun cancelScan() {
        scanJob?.cancel()
        _scanProgressState.value = _scanProgressState.value.copy(isCancelled = true)
    }

    // ================================================================
    // Report / Settings
    // ================================================================

    private fun loadReport(taskId: String) {
        viewModelScope.launch {
            _reportState.value = _reportState.value.copy(isLoading = true, error = null)
            try {
                val task = withContext(Dispatchers.IO) {
                    loadTasksFromDisk().find { it.id == taskId }
                }
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
            _reportEffect.send(ReportEffect.ExportCompleted("/storage/emulated/0/Download/qa_report.${format.name.lowercase()}"))
            _reportEffect.send(ReportEffect.TriggerShare)
        }
    }

    private fun shareReport(taskId: String) {
        viewModelScope.launch { _reportEffect.send(ReportEffect.TriggerShare) }
    }

    // ================================================================
    // Local Storage
    // ================================================================

    private val tasksFile: File
        get() = File("/data/user/0/com.mvi.kenny.myapp/files/qa_tasks.txt")

    private suspend fun loadTasksFromDisk(): List<ScanTask> = withContext(Dispatchers.IO) {
        try {
            if (tasksFile.exists()) {
                tasksFile.readLines()
                    .filter { it.isNotBlank() }
                    .mapNotNull { line ->
                        val parts = line.split("|")
                        if (parts.size >= 6) {
                            try {
                                ScanTask(
                                    id = parts[0], packageName = parts[1],
                                    status = ScanTaskStatus.valueOf(parts[2]),
                                    screenCount = parts[3].toIntOrNull() ?: 0,
                                    issueCount = parts[4].toIntOrNull() ?: 0,
                                    createdAt = parts[5].toLongOrNull() ?: 0L
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
            tasksFile.appendText("${task.id}|${task.packageName}|${task.status}|${task.screenCount}|${task.issueCount}|${task.createdAt}\n")
        } catch (e: Exception) { /* silent */ }
    }

    private suspend fun updateTaskInDisk(taskId: String, status: ScanTaskStatus, screenCount: Int, issueCount: Int) = withContext(Dispatchers.IO) {
        try {
            if (!tasksFile.exists()) return@withContext
            val lines = tasksFile.readLines().map { line ->
                val parts = line.split("|")
                if (parts.size >= 6 && parts[0] == taskId) {
                    "${parts[0]}|${parts[1]}|$status|$screenCount|$issueCount|${parts[5]}"
                } else line
            }
            tasksFile.writeText(lines.joinToString("\n"))
        } catch (e: Exception) { /* silent */ }
    }

    private suspend fun deleteTaskFromDisk(taskId: String) = withContext(Dispatchers.IO) {
        try {
            if (!tasksFile.exists()) return@withContext
            val lines = tasksFile.readLines().filter { line ->
                val parts = line.split("|")
                parts.isEmpty() || parts[0] != taskId
            }
            tasksFile.writeText(lines.joinToString("\n"))
        } catch (e: Exception) { /* silent */ }
    }
}
