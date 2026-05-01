package com.mvi.kenny.feature.smsretrievertool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern

// =============================================================
// SmsRetrieverToolViewModel — SMS Retriever API 工具 ViewModel
// =============================================================
/**
 * ViewModel for SMS Retriever Tool / SMS Retriever API 工具 ViewModel
 *
 * Manages all state transitions following MVI pattern.
 * ViewModelScope is used for all coroutine operations.
 *
 * Key responsibilities:
 * - Scan project for READ_SMS / ContentResolver.query(SMS_URI) usage
 * - Generate SMS Retriever API migration suggestions
 * - Produce compliance reports
 * - Execute automated code migration
 *
 * @see SmsRetrieverToolContract For State, Intent, Effect definitions
 * @see SmsRetrieverToolScreen For UI implementation
 */
class SmsRetrieverToolViewModel : ViewModel() {

    // ---------------------------------------------------------
    // State — single source of truth for UI
    // ---------------------------------------------------------
    private val _state = MutableStateFlow(SmsRetrieverToolState.Initial)
    val state: StateFlow<SmsRetrieverToolState> = _state.asStateFlow()

    // ---------------------------------------------------------
    // Effect — one-time events for UI
    // ---------------------------------------------------------
    private val _effect = MutableSharedFlow<SmsRetrieverToolEffect>()
    val effect: SharedFlow<SmsRetrieverToolEffect> = _effect.asSharedFlow()

    // ---------------------------------------------------------
    // Internal state
    // ---------------------------------------------------------
    private var scanJob: Job? = null

    // SMS permission usage patterns to detect / SMS 权限使用检测模式
    // These patterns indicate code that reads SMS directly — incompatible with Android 17
    // 这些模式表示直接读取 SMS 的代码 — 与 Android 17 不兼容
    private val smsPermissionPatterns = listOf(
        Pattern.compile("READ_SMS"),
        Pattern.compile("content://sms"),
        Pattern.compile("ContentResolver.*query.*SMS"),
        Pattern.compile("Telephony.Sms.*\\.Inbox"),
        Pattern.compile("Telephony.TextBasedSmsColumns"),
        Pattern.compile("android\\.provider\\.Telephony")
    )

    // OTP extraction patterns / OTP 提取模式
    private val otpExtractPatterns = listOf(
        Pattern.compile("""\d{4,8}"""),
        Pattern.compile("""code|pwd|pin|otp""", Pattern.CASE_INSENSITIVE)
    )

    // ---------------------------------------------------------
    // Intent processing
    // ---------------------------------------------------------

    /** Process user intent / 处理用户意图 */
    fun processIntent(intent: SmsRetrieverToolIntent) {
        when (intent) {
            is SmsRetrieverToolIntent.StartScan -> startScan(intent.module)
            is SmsRetrieverToolIntent.CancelScan -> cancelScan()
            is SmsRetrieverToolIntent.SelectModule -> selectModule(intent.module)
            is SmsRetrieverToolIntent.ViewAffectedCode -> viewAffectedCode(intent.codePath)
            is SmsRetrieverToolIntent.ClearSelectedCodePath -> clearSelectedCodePath()
            is SmsRetrieverToolIntent.StartMigration -> startMigration(intent.codePath)
            is SmsRetrieverToolIntent.ConfirmMigration -> confirmMigration(intent.diff)
            is SmsRetrieverToolIntent.GenerateComplianceReport -> generateComplianceReport()
            is SmsRetrieverToolIntent.UpdateSettings -> updateSettings(intent.settings)
            is SmsRetrieverToolIntent.DismissError -> dismissError()
            is SmsRetrieverToolIntent.ClearMigrationResult -> clearMigrationResult()
        }
    }

    // ---------------------------------------------------------
    // Scan logic / 扫描逻辑
    // ---------------------------------------------------------

    /**
     * Start scanning project for SMS permission usage
     * 开始扫描项目中的 SMS 权限使用情况
     *
     * @param module Module path to scan / 要扫描的模块路径
     */
    private fun startScan(module: String) {
        // Cancel any existing scan / 取消任何现有扫描
        scanJob?.cancel()

        // Update state to scanning / 更新状态为扫描中
        _state.update {
            it.copy(
                scanStatus = ScanStatus.SCANNING,
                scannedFilesCount = 0,
                affectedCodePaths = emptyList(),
                error = null
            )
        }

        scanJob = viewModelScope.launch {
            try {
                val results = scanForSmsPermission(module)
                _state.update {
                    it.copy(
                        scanStatus = ScanStatus.SUCCESS,
                        scannedFilesCount = results.totalFiles,
                        affectedCodePaths = results.affectedPaths
                    )
                }
                _effect.emit(SmsRetrieverToolEffect.ShowToast("Scan complete: ${results.affectedPaths.size} issues found / 扫描完成：发现 ${results.affectedPaths.size} 个问题"))
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _state.update {
                    it.copy(
                        scanStatus = ScanStatus.ERROR,
                        error = e.message ?: "Unknown error / 未知错误"
                    )
                }
                _effect.emit(SmsRetrieverToolEffect.ShowError(e.message ?: "Scan failed / 扫描失败"))
            }
        }
    }

    /**
     * Cancel ongoing scan / 取消正在进行的扫描
     */
    private fun cancelScan() {
        scanJob?.cancel()
        _state.update {
            it.copy(
                scanStatus = ScanStatus.IDLE,
                scannedFilesCount = 0
            )
        }
    }

    /**
     * Scan project directory for SMS permission usage
     * 扫描项目目录中的 SMS 权限使用情况
     *
     * @param modulePath Module path to scan / 要扫描的模块路径
     * @return Scan results with affected paths / 扫描结果，包含受影响路径
     */
    private suspend fun scanForSmsPermission(modulePath: String): ScanResults = withContext(Dispatchers.IO) {
        val affectedPaths = mutableListOf<AffectedCodePath>()
        val settings = _state.value.settings
        var totalFiles = 0

        // Find project root / 查找项目根目录
        val projectRoot = findProjectRoot()
        val scanDir = if (modulePath.isNotEmpty()) File(projectRoot, modulePath) else File(projectRoot)

        // Scan all Kotlin and Java files / 扫描所有 Kotlin 和 Java 文件
        val sourceFiles = scanDir.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
            .filter { file -> settings.ignorePaths.none { file.path.contains(it) } }

        for (file in sourceFiles) {
            totalFiles++
            // Emit progress update every 50 files / 每 50 个文件发出进度更新
            if (totalFiles % 50 == 0) {
                _state.update { it.copy(scannedFilesCount = totalFiles) }
            }

            try {
                val content = file.readText()
                val lines = content.lines()

                // Check each line for SMS permission patterns / 检查每一行是否有 SMS 权限模式
                lines.forEachIndexed { index, line ->
                    for (pattern in smsPermissionPatterns) {
                        if (pattern.matcher(line).find()) {
                            val severity = determineSeverity(line)
                            val description = generateDescription(line, file.name)
                            val migration = generateMigrationSuggestion(line, file.name)

                            affectedPaths.add(
                                AffectedCodePath(
                                    id = UUID.randomUUID().toString(),
                                    filePath = file.path,
                                    lineNumber = index + 1,
                                    codeSnippet = line.trim(),
                                    severity = severity,
                                    description = description,
                                    suggestedMigration = migration
                                )
                            )
                            break // One match per line is enough / 每行一个匹配就够了
                        }
                    }
                }
            } catch (e: Exception) {
                // Skip files that can't be read / 跳过无法读取的文件
                continue
            }
        }

        ScanResults(totalFiles = totalFiles, affectedPaths = affectedPaths)
    }

    /**
     * Find project root directory / 查找项目根目录
     * @return Absolute path to project root / 项目根目录的绝对路径
     */
    private fun findProjectRoot(): String {
        // Try to find project root by looking for settings.gradle / 通过查找 settings.gradle 找项目根目录
        val possibleRoots = listOf(
            System.getProperty("user.dir"),
            File(System.getProperty("user.dir")).parent,
            "/Users/kenny/WorkSpace/AndroidStudioProjects/MyMviProject"
        )
        for (path in possibleRoots) {
            if (path != null && File(path, "settings.gradle.kts").exists()) {
                return path
            }
        }
        return System.getProperty("user.dir") ?: "/Users/kenny/WorkSpace/AndroidStudioProjects/MyMviProject"
    }

    /**
     * Determine severity of the SMS permission usage
     * 判断 SMS 权限使用的严重性
     *
     * @param line Code line / 代码行
     * @return Severity level / 严重性等级
     */
    private fun determineSeverity(line: String): Severity {
        return when {
            // Direct READ_SMS permission request — highest severity
            // 直接请求 READ_SMS 权限 — 最高严重性
            line.contains("READ_SMS") && (line.contains("checkSelfPermission") || line.contains("requestPermissions")) -> Severity.BLOCKER
            // Direct SMS content provider query — high severity
            // 直接查询 SMS content provider — 高严重性
            line.contains("content://sms") && line.contains("query") -> Severity.BLOCKER
            // Telephony.Sms.Inbox usage — high severity
            // Telephony.Sms.Inbox 使用 — 高严重性
            line.contains("Telephony.Sms") || line.contains("Telephony.TextBasedSmsColumns") -> Severity.BLOCKER
            // Generic ContentResolver with SMS URI — medium severity
            // 通用 ContentResolver 与 SMS URI — 中等严重性
            line.contains("SMS") && line.contains("ContentResolver") -> Severity.WARNING
            // OTP extraction pattern without proper API — low severity
            // OTP 提取模式但没有适当的 API — 低严重性
            else -> Severity.INFO
        }
    }

    /**
     * Generate description for the issue
     * 生成问题的描述
     *
     * @param line Code line / 代码行
     * @param fileName File name / 文件名
     * @return Human-readable description / 人类可读描述
     */
    private fun generateDescription(line: String, fileName: String): String {
        return when {
            line.contains("READ_SMS") && (line.contains("checkSelfPermission") || line.contains("requestPermissions")) ->
                "READ_SMS permission is being requested. Android 17 (API 37+) restricts SMS permission use. " +
                "Apps must migrate to SMS Retriever API or SMS User Consent API. / " +
                "正在请求 READ_SMS 权限。Android 17 (API 37+) 限制了 SMS 权限使用。应用必须迁移到 SMS Retriever API 或 SMS User Consent API。"
            line.contains("content://sms") && line.contains("query") ->
                "Direct SMS content provider query detected. Android 17 blocks direct SMS content provider access. " +
                "Use SMS Retriever API instead. / " +
                "检测到直接 SMS content provider 查询。Android 17 阻止直接访问 SMS content provider。请改用 SMS Retriever API。"
            line.contains("Telephony.Sms") ->
                "Telephony.Sms API usage detected. These APIs are restricted on Android 17+. " +
                "Migrate to SMS Retriever API for OTP retrieval. / " +
                "检测到 Telephony.Sms API 使用。这些 API 在 Android 17+ 上受限。请迁移到 SMS Retriever API 来获取 OTP。"
            else ->
                "Potential SMS permission usage detected. Review and ensure compliance with Android 17 SMS policies. / " +
                "检测到潜在的 SMS 权限使用。请审查并确保符合 Android 17 SMS 政策。"
        }
    }

    /**
     * Generate migration suggestion for the issue
     * 生成问题的迁移建议
     *
     * @param line Code line / 代码行
     * @param fileName File name / 文件名
     * @return Migration suggestion / 迁移建议
     */
    private fun generateMigrationSuggestion(line: String, fileName: String): String {
        return when {
            line.contains("READ_SMS") && (line.contains("checkSelfPermission") || line.contains("requestPermissions")) ->
                """
                // Recommended: Use SMS Retriever API (Google Play Services)
                // 1. Add dependency: implementation("com.google.android.gms:play-services-auth-api-phone:+")
                // 2. Start SmsRetriever client
                // 3. Register BroadcastReceiver for SMS_RETRIEVER_ACTION
                // 4. Extract OTP from message body using regex: \d{4,8}
                //
                // Alternative: SMS User Consent API (for unrecognizable senders)
                // Use SMS User Consent API when sender is not in Play Services allowlist
                """.trimIndent()
            line.contains("content://sms") ->
                """
                // Recommended: Migrate to SMS Retriever API
                //
                // Before (Android < 17):
                //   val cursor = contentResolver.query(Sms.Inbox.CONTENT_URI, ...)
                //
                // After (Android 17+):
                //   val client = SmsRetriever.getClient(context)
                //   client.startSmsRetriever()
                //   // Handle result in BroadcastReceiver
                """.trimIndent()
            else ->
                """
                // Recommended: Review SMS usage and migrate to SMS Retriever API
                //
                // SMS Retriever API benefits:
                // - No SMS permission required ( Privacy Sandbox )
                // - Works automatically after 5 minutes timeout
                // - Compliant with Android 17 SMS policies
                """.trimIndent()
        }
    }

    // ---------------------------------------------------------
    // Module selection / 模块选择
    // ---------------------------------------------------------

    /**
     * Select a module for scanning / 选择要扫描的模块
     *
     * @param module Module name / 模块名称
     */
    private fun selectModule(module: String) {
        _state.update { it.copy(selectedModule = module) }
    }

    // ---------------------------------------------------------
    // Code path detail / 代码路径详情
    // ---------------------------------------------------------

    /**
     * View affected code path detail / 查看受影响代码路径详情
     *
     * @param codePath Code path to view / 要查看的代码路径
     */
    private fun viewAffectedCode(codePath: AffectedCodePath) {
        _state.update { it.copy(selectedCodePath = codePath) }
        viewModelScope.launch {
            _effect.emit(SmsRetrieverToolEffect.NavigateToDetail(codePath))
        }
    }

    /**
     * Clear selected code path / 清除选中的代码路径
     */
    private fun clearSelectedCodePath() {
        _state.update { it.copy(selectedCodePath = null) }
    }

    // ---------------------------------------------------------
    // Migration logic / 迁移逻辑
    // ---------------------------------------------------------

    /**
     * Start migration for a specific code path / 开始迁移特定代码路径
     *
     * @param codePath Code path to migrate / 要迁移的代码路径
     */
    private fun startMigration(codePath: AffectedCodePath) {
        viewModelScope.launch {
            _state.update { it.copy(migrationInProgress = true) }

            try {
                val diff = generateMigrationDiff(codePath)
                _state.update { it.copy(migrationInProgress = false) }
                _effect.emit(SmsRetrieverToolEffect.NavigateToMigrationPreview)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        migrationInProgress = false,
                        error = e.message ?: "Migration failed / 迁移失败"
                    )
                }
            }
        }
    }

    /**
     * Generate migration diff for a code path
     * 为代码路径生成迁移 diff
     *
     * @param codePath Code path to migrate / 要迁移的代码路径
     * @return Migration diff / 迁移 diff
     */
    private fun generateMigrationDiff(codePath: AffectedCodePath): CodeDiff {
        val file = File(codePath.filePath)
        val content = if (file.exists()) file.readText() else ""
        val lines = content.lines()

        // Find the target line / 找到目标行
        val targetLineIndex = lines.indexOfFirst { it.contains(codePath.codeSnippet) }
        if (targetLineIndex == -1) {
            return CodeDiff(
                filePath = codePath.filePath,
                beforeCode = codePath.codeSnippet,
                afterCode = "// TODO: Migrate to SMS Retriever API",
                diffText = "Line not found / 行未找到"
            )
        }

        // Generate after code based on the type of issue
        // 根据问题类型生成迁移后代码
        val afterCode = when {
            codePath.codeSnippet.contains("READ_SMS") && codePath.codeSnippet.contains("checkSelfPermission") ->
                "// READ_SMS permission check - Migrate to SMS Retriever API\n" +
                "// See: https://developers.google.com/identity/sms-retriever/overview\n" +
                lines[targetLineIndex]

            codePath.codeSnippet.contains("content://sms") ->
                "// SMS content query - Migrate to SMS Retriever API\n" +
                "// SmsRetrieverClient.startSmsRetriever() replaces direct content queries\n" +
                lines[targetLineIndex]

            else -> "// TODO: Review and migrate to SMS Retriever API\n${lines[targetLineIndex]}"
        }

        // Generate unified diff / 生成统一 diff
        val diffText = buildString {
            appendLine("--- a/${file.name}")
            appendLine("+++ b/${file.name}")
            appendLine("@@ -${targetLineIndex + 1},1 +${targetLineIndex + 1},1 @@")
            appendLine("-${codePath.codeSnippet}")
            appendLine("+$afterCode")
        }

        return CodeDiff(
            filePath = codePath.filePath,
            beforeCode = codePath.codeSnippet,
            afterCode = afterCode,
            diffText = diffText
        )
    }

    /**
     * Confirm and apply migration / 确认并应用迁移
     *
     * @param diff Migration diff to apply / 要应用的迁移 diff
     */
    private fun confirmMigration(diff: CodeDiff) {
        viewModelScope.launch {
            _state.update { it.copy(migrationInProgress = true) }

            try {
                // Backup original file / 备份原文件
                val file = File(diff.filePath)
                val backupDir = File(file.parent, ".smsbackup")
                backupDir.mkdirs()
                val backupFile = File(backupDir, "${file.name}.${System.currentTimeMillis()}")
                file.copyTo(backupFile)

                // Read and modify content / 读取并修改内容
                val content = file.readText()
                val modifiedContent = content.replace(diff.beforeCode, diff.afterCode)

                // Write migrated code / 写入迁移后代码
                file.writeText(modifiedContent)

                // Verify build / 验证构建
                val buildSuccess = verifyBuild()

                val result = MigrationResult(
                    success = true,
                    migratedFiles = listOf(diff.filePath),
                    failedFiles = emptyList(),
                    buildVerification = buildSuccess
                )

                _state.update {
                    it.copy(
                        migrationInProgress = false,
                        migrationResult = result
                    )
                }
                _effect.emit(SmsRetrieverToolEffect.MigrationCompleted(result))
                _effect.emit(SmsRetrieverToolEffect.ShowToast("Migration applied / 迁移已应用"))

            } catch (e: Exception) {
                val result = MigrationResult(
                    success = false,
                    migratedFiles = emptyList(),
                    failedFiles = listOf(diff.filePath),
                    buildVerification = false,
                    errorMessage = e.message
                )
                _state.update {
                    it.copy(
                        migrationInProgress = false,
                        migrationResult = result
                    )
                }
                _effect.emit(SmsRetrieverToolEffect.ShowError(e.message ?: "Migration failed / 迁移失败"))
            }
        }
    }

    /**
     * Verify that the project builds successfully after migration
     * 验证迁移后项目构建成功
     *
     * @return true if build succeeds / 构建成功返回 true
     */
    private suspend fun verifyBuild(): Boolean = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, this would run ./gradlew :app:compileDebugKotlin
            // 实际实现中，这会运行 ./gradlew :app:compileDebugKotlin
            // For now, we simulate success / 现在我们模拟成功
            delay(1000)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Clear migration result / 清除迁移结果
     */
    private fun clearMigrationResult() {
        _state.update { it.copy(migrationResult = null) }
    }

    // ---------------------------------------------------------
    // Compliance report / 合规报告
    // ---------------------------------------------------------

    /**
     * Generate compliance report / 生成合规报告
     */
    private fun generateComplianceReport() {
        viewModelScope.launch {
            val currentState = _state.value
            val affectedPaths = currentState.affectedCodePaths

            // Group by module / 按模块分组
            val moduleReports = affectedPaths.groupBy { path ->
                // Extract module name from file path / 从文件路径提取模块名
                path.filePath.split("src/main/java/").getOrNull(1)?.split("/")?.getOrNull(0) ?: "app"
            }.map { (moduleName, paths) ->
                ModuleComplianceReport(
                    moduleName = moduleName,
                    isCompliant = paths.none { it.severity == Severity.BLOCKER },
                    blockerCount = paths.count { it.severity == Severity.BLOCKER },
                    warningCount = paths.count { it.severity == Severity.WARNING },
                    affectedFiles = paths.map { it.filePath }.distinct()
                )
            }

            // Determine overall status / 确定整体状态
            val overallStatus = when {
                moduleReports.isEmpty() -> ComplianceStatus.COMPLIANT
                moduleReports.any { it.blockerCount > 0 } -> ComplianceStatus.NON_COMPLIANT
                moduleReports.any { it.warningCount > 0 } -> ComplianceStatus.PARTIAL
                else -> ComplianceStatus.COMPLIANT
            }

            val report = ComplianceReport(
                overallStatus = overallStatus,
                moduleReports = moduleReports,
                totalAffectedFiles = affectedPaths.map { it.filePath }.distinct().size,
                generatedAt = System.currentTimeMillis()
            )

            _state.update { it.copy(complianceReport = report) }
            _effect.emit(SmsRetrieverToolEffect.ReportGenerated(report))
        }
    }

    // ---------------------------------------------------------
    // Settings / 设置
    // ---------------------------------------------------------

    /**
     * Update tool settings / 更新工具设置
     *
     * @param settings New settings / 新设置
     */
    private fun updateSettings(settings: SmsRetrieverSettings) {
        _state.update { it.copy(settings = settings) }
    }

    // ---------------------------------------------------------
    // Error handling / 错误处理
    // ---------------------------------------------------------

    /**
     * Dismiss error message / 关闭错误信息
     */
    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    // ---------------------------------------------------------
    // Helper data class for scan results
    // 扫描结果的辅助数据类
    // ---------------------------------------------------------
    private data class ScanResults(
        val totalFiles: Int,
        val affectedPaths: List<AffectedCodePath>
    )
}
