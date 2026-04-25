package com.mvi.kenny.feature.lannetworktool

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

// =============================================================
// LocalNetworkToolViewModel — 局域网权限工具 ViewModel
// =============================================================
/**
 * ViewModel for Local Network Permission Tool / 局域网权限工具 ViewModel
 *
 * Manages all state transitions following MVI pattern.
 * ViewModelScope is used for all coroutine operations.
 *
 * Core responsibilities:
 * — Scan Android project source code for LAN access patterns
 * — Generate ACCESS_LOCAL_NETWORK compliance reports
 * — Produce AndroidManifest.xml migration diffs
 * — Generate runtime permission request code templates
 *
 * Android 17 ACCESS_LOCAL_NETWORK background:
 * — Belongs to NEARBY_DEVICES permission group
 * — Required for targetSDK 37+ apps accessing RFC1918 private IPs
 * — Silent failure when revoked (no exception thrown)
 *
 * @see LocalNetworkToolContract For State, Intent, Effect definitions
 * @see LocalNetworkToolScreen For UI implementation
 */
class LocalNetworkToolViewModel : ViewModel() {

    // ---------------------------------------------------------
    // State — single source of truth for UI
    // ---------------------------------------------------------
    private val _state = MutableStateFlow(LocalNetworkToolState.Initial)
    val state: StateFlow<LocalNetworkToolState> = _state.asStateFlow()

    // ---------------------------------------------------------
    // Effect — one-time events for UI
    // ---------------------------------------------------------
    private val _effect = MutableSharedFlow<LocalNetworkToolEffect>()
    val effect: SharedFlow<LocalNetworkToolEffect> = _effect.asSharedFlow()

    // ---------------------------------------------------------
    // Internal state
    // ---------------------------------------------------------
    private var scanJob: Job? = null

    // ============================================================
    // Intent Processing — 处理用户意图
    // ============================================================
    /**
     * Process user intent / 处理用户意图
     *
     * Entry point for all user actions.
     *
     * @param intent User intent to process / 要处理的用户意图
     */
    fun processIntent(intent: LocalNetworkToolIntent) {
        when (intent) {
            // General
            is LocalNetworkToolIntent.SelectTab -> handleSelectTab(intent.tab)
            LocalNetworkToolIntent.DismissError -> handleDismissError()

            // Scanner
            is LocalNetworkToolIntent.SetProjectPath -> handleSetProjectPath(intent.path)
            LocalNetworkToolIntent.StartScan -> handleStartScan()
            LocalNetworkToolIntent.CancelScan -> handleCancelScan()
            is LocalNetworkToolIntent.SetScanStep -> handleSetScanStep(intent.step)
            is LocalNetworkToolIntent.SelectAccessPath -> handleSelectAccessPath(intent.path)
            LocalNetworkToolIntent.ClearResults -> handleClearResults()

            // Report
            LocalNetworkToolIntent.GenerateReport -> handleGenerateReport()
            is LocalNetworkToolIntent.ExportReport -> handleExportReport(intent.format)

            // Migration
            LocalNetworkToolIntent.GenerateManifestDiff -> handleGenerateManifestDiff()
            is LocalNetworkToolIntent.ApplyManifestDiff -> handleApplyManifestDiff(intent.applyToProject)
            is LocalNetworkToolIntent.SelectTemplate -> handleSelectTemplate(intent.template)
            LocalNetworkToolIntent.GeneratePermissionCode -> handleGeneratePermissionCode()
            LocalNetworkToolIntent.CopyPermissionCode -> handleCopyPermissionCode()

            // Settings
            is LocalNetworkToolIntent.ToggleReportFormat -> handleToggleReportFormat(intent.format)
            is LocalNetworkToolIntent.SetTargetSdkOverride -> handleSetTargetSdkOverride(intent.sdk)
        }
    }

    // ============================================================
    // General Handlers — 通用处理器
    // ============================================================
    /**
     * Handle tab selection / 处理 Tab 选择
     */
    private fun handleSelectTab(tab: MainTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    /**
     * Handle error dismissal / 处理错误关闭
     */
    private fun handleDismissError() {
        _state.update { it.copy(error = null) }
    }

    // ============================================================
    // Scanner Handlers — 扫描器处理器
    // ============================================================
    /**
     * Handle project path set / 处理项目路径设置
     */
    private fun handleSetProjectPath(path: String) {
        _state.update { it.copy(projectPath = path) }
    }

    /**
     * Handle scan start / 处理扫描开始
     */
    private fun handleStartScan() {
        val projectPath = _state.value.projectPath
        if (projectPath.isBlank()) {
            _state.update { it.copy(error = "Please select a project path / 请选择项目路径") }
            return
        }

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    scanProgress = 0f,
                    scanStep = ScanStep.SCANNING,
                    scanLogs = emptyList(),
                    accessPaths = emptyList()
                )
            }

            appendLog(LogLevel.INFO, "Starting LAN permission scan / 开始局域网权限扫描...")
            appendLog(LogLevel.INFO, "Project: $projectPath")

            try {
                // Step 1: Validate project / 步骤 1: 验证项目
                delay(200)
                appendLog(LogLevel.INFO, "Validating Android project structure / 验证 Android 项目结构...")
                val valid = withContext(Dispatchers.IO) {
                    validateAndroidProject(projectPath)
                }
                if (!valid) {
                    appendLog(LogLevel.ERROR, "Not a valid Android project / 不是有效的 Android 项目")
                    _state.update { it.copy(isScanning = false, error = "Invalid Android project / 无效的 Android 项目") }
                    return@launch
                }
                appendLog(LogLevel.SUCCESS, "Android project validated / Android 项目验证通过")
                _state.update { it.copy(scanProgress = 0.15f) }
                delay(200)

                // Step 2: Detect target SDK / 步骤 2: 检测目标 SDK
                appendLog(LogLevel.INFO, "Detecting targetSdkVersion / 检测 targetSdkVersion...")
                val targetSdk = withContext(Dispatchers.IO) {
                    detectTargetSdk(projectPath)
                }
                appendLog(LogLevel.INFO, "targetSdkVersion = $targetSdk")
                val requiresPermission = targetSdk >= 37
                if (requiresPermission) {
                    appendLog(LogLevel.WARNING, "targetSDK $targetSdk >= 37: ACCESS_LOCAL_NETWORK permission required / 需要 ACCESS_LOCAL_NETWORK 权限")
                } else {
                    appendLog(LogLevel.INFO, "targetSDK $targetSdk < 37: Legacy app — implicit ACCESS_LOCAL_NETWORK grant / 传统应用 — 隐式授予 ACCESS_LOCAL_NETWORK")
                }
                _state.update { it.copy(scanProgress = 0.30f) }
                delay(200)

                // Step 3: Scan for InetAddress usage / 步骤 3: 扫描 InetAddress 使用
                appendLog(LogLevel.SCANNING, "Scanning for InetAddress.getByName() calls / 扫描 InetAddress.getByName() 调用...")
                val inetAddressPaths = withContext(Dispatchers.IO) {
                    scanInetAddressUsage(projectPath)
                }
                appendLog(LogLevel.SUCCESS, "Found ${inetAddressPaths.size} InetAddress calls / 发现 ${inetAddressPaths.size} 个 InetAddress 调用")
                _state.update {
                    it.copy(
                        accessPaths = it.accessPaths + inetAddressPaths,
                        scanProgress = 0.50f
                    )
                }
                delay(200)

                // Step 4: Scan for Socket usage / 步骤 4: 扫描 Socket 使用
                appendLog(LogLevel.SCANNING, "Scanning for Socket connections / 扫描 Socket 连接...")
                val socketPaths = withContext(Dispatchers.IO) {
                    scanSocketUsage(projectPath)
                }
                appendLog(LogLevel.SUCCESS, "Found ${socketPaths.size} Socket connections / 发现 ${socketPaths.size} 个 Socket 连接")
                _state.update {
                    it.copy(
                        accessPaths = it.accessPaths + socketPaths,
                        scanProgress = 0.70f
                    )
                }
                delay(200)

                // Step 5: Scan for OkHttp/Retrofit / 步骤 5: 扫描 OkHttp/Retrofit
                appendLog(LogLevel.SCANNING, "Scanning for OkHttp/Retrofit usage / 扫描 OkHttp/Retrofit 使用...")
                val httpPaths = withContext(Dispatchers.IO) {
                    scanHttpClientUsage(projectPath)
                }
                appendLog(LogLevel.SUCCESS, "Found ${httpPaths.size} HTTP client usages / 发现 ${httpPaths.size} 个 HTTP 客户端使用")
                _state.update {
                    it.copy(
                        accessPaths = it.accessPaths + httpPaths,
                        scanProgress = 0.90f
                    )
                }
                delay(200)

                // Step 6: Scan manifest / 步骤 6: 扫描清单
                appendLog(LogLevel.SCANNING, "Scanning AndroidManifest.xml for permission declarations / 扫描 AndroidManifest.xml 权限声明...")
                val manifestPermissions = withContext(Dispatchers.IO) {
                    scanManifestPermissions(projectPath)
                }
                appendLog(LogLevel.INFO, "Current manifest permissions: $manifestPermissions")

                // Update permission status based on manifest
                _state.update { currentState ->
                    val updatedPaths = currentState.accessPaths.map { path ->
                        val hasPermission = manifestPermissions.contains("android.permission.ACCESS_LOCAL_NETWORK")
                        val newStatus = if (!requiresPermission) {
                            PermissionStatus.NOT_NEEDED
                        } else if (hasPermission) {
                            PermissionStatus.DECLARED
                        } else {
                            PermissionStatus.MISSING
                        }
                        path.copy(permissionStatus = newStatus)
                    }
                    currentState.copy(
                        accessPaths = updatedPaths,
                        scanProgress = 1.0f
                    )
                }

                delay(200)

                val nonCompliant = _state.value.nonCompliantCount
                val total = _state.value.accessPaths.size

                appendLog(LogLevel.SUCCESS, "Scan complete! / 扫描完成！")
                appendLog(LogLevel.INFO, "Total: $total paths, $nonCompliant non-compliant / 总计: $total 个路径, $nonCompliant 个不合规")

                _state.update {
                    it.copy(
                        isScanning = false,
                        scanStep = ScanStep.RESULTS
                    )
                }

                _effect.emit(LocalNetworkToolEffect.ScanCompleted(total, nonCompliant))

                if (nonCompliant > 0) {
                    _effect.emit(LocalNetworkToolEffect.ShowSnackbar(
                        "$nonCompliant non-compliant paths need permission declaration / $nonCompliant 个不合规路径需要声明权限",
                        isError = false
                    ))
                }

            } catch (e: Exception) {
                appendLog(LogLevel.ERROR, "Scan failed: ${e.message}")
                _state.update {
                    it.copy(
                        isScanning = false,
                        error = "Scan failed: ${e.message} / 扫描失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Handle scan cancellation / 处理扫描取消
     */
    private fun handleCancelScan() {
        scanJob?.cancel()
        _state.update {
            it.copy(
                isScanning = false,
                scanStep = ScanStep.PROJECT_SELECTION,
                scanProgress = 0f
            )
        }
        appendLog(LogLevel.WARNING, "Scan cancelled by user / 扫描被用户取消")
    }

    /**
     * Handle scan step change / 处理扫描步骤变更
     */
    private fun handleSetScanStep(step: ScanStep) {
        _state.update { it.copy(scanStep = step) }
    }

    /**
     * Handle access path selection / 处理访问路径选择
     */
    private fun handleSelectAccessPath(path: LanAccessPath) {
        _state.update { it.copy(selectedAccessPath = path) }
    }

    /**
     * Handle clear results / 处理清除结果
     */
    private fun handleClearResults() {
        _state.update {
            it.copy(
                accessPaths = emptyList(),
                complianceReport = null,
                manifestDiff = null,
                scanLogs = emptyList(),
                scanStep = ScanStep.PROJECT_SELECTION,
                scanProgress = 0f
            )
        }
    }

    // ============================================================
    // Report Handlers — 报告处理器
    // ============================================================
    /**
     * Handle report generation / 处理报告生成
     */
    private fun handleGenerateReport() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                delay(300) // Simulate processing / 模拟处理

                val accessPaths = _state.value.accessPaths
                val targetSdk = _state.value.targetSdkOverride ?: 37

                val report = ComplianceReport(
                    totalPaths = accessPaths.size,
                    compliantPaths = accessPaths.count { it.permissionStatus == PermissionStatus.DECLARED },
                    nonCompliantPaths = accessPaths.count { it.permissionStatus == PermissionStatus.MISSING },
                    uncertainPaths = accessPaths.count { it.permissionStatus == PermissionStatus.UNCERTAIN },
                    accessPaths = accessPaths,
                    targetSdk = targetSdk,
                    requiresPermission = targetSdk >= 37
                )

                _state.update {
                    it.copy(
                        complianceReport = report,
                        isLoading = false
                    )
                }

                _effect.emit(LocalNetworkToolEffect.ShowSnackbar("Report generated / 报告已生成"))

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to generate report: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Handle report export / 处理报告导出
     */
    private fun handleExportReport(format: ReportFormat) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val report = _state.value.complianceReport
                    ?: throw IllegalStateException("No report to export / 没有可导出的报告")

                val content = withContext(Dispatchers.IO) {
                    generateReportContent(report, format)
                }

                val exportPath = withContext(Dispatchers.IO) {
                    val exportDir = File(System.getProperty("user.home"), ".openclaw/workspace/reports/lan-permissions")
                    exportDir.mkdirs()
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    val file = File(exportDir, "lan_compliance_$timestamp.${format.extension}")
                    file.writeText(content)
                    file.absolutePath
                }

                _state.update { it.copy(isLoading = false) }
                _effect.emit(LocalNetworkToolEffect.ReportExported(exportPath, format))

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to export report: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    // ============================================================
    // Migration Handlers — 迁移处理器
    // ============================================================
    /**
     * Handle manifest diff generation / 处理清单差异生成
     */
    private fun handleGenerateManifestDiff() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                delay(300) // Simulate processing / 模拟处理

                val nonCompliantPaths = _state.value.accessPaths
                    .filter { it.permissionStatus == PermissionStatus.MISSING }

                val addedPermissions = listOf("android.permission.ACCESS_LOCAL_NETWORK")

                val manifestDiff = ManifestDiff(
                    original = buildManifestSnippet(addedPermissions, isOriginal = true),
                    modified = buildManifestSnippet(addedPermissions, isOriginal = false),
                    addedPermissions = addedPermissions,
                    conflicts = emptyList()
                )

                _state.update {
                    it.copy(
                        manifestDiff = manifestDiff,
                        isLoading = false
                    )
                }

                _effect.emit(LocalNetworkToolEffect.ShowSnackbar("Manifest diff generated / 清单差异已生成"))

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to generate diff: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Handle manifest diff application / 处理清单差异应用
     */
    private fun handleApplyManifestDiff(applyToProject: Boolean) {
        viewModelScope.launch {
            if (!applyToProject) {
                _effect.emit(LocalNetworkToolEffect.ShowSnackbar("Preview mode — no changes applied / 预览模式 — 未应用任何更改"))
                return@launch
            }

            _state.update { it.copy(isLoading = true) }

            try {
                val projectPath = _state.value.projectPath
                if (projectPath.isBlank()) {
                    throw IllegalStateException("No project path set / 未设置项目路径")
                }

                withContext(Dispatchers.IO) {
                    applyManifestToProject(projectPath)
                }

                _state.update { it.copy(isLoading = false) }
                _effect.emit(LocalNetworkToolEffect.ManifestDiffApplied("$projectPath/app/src/main/AndroidManifest.xml"))

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to apply diff: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Handle template selection / 处理模板选择
     */
    private fun handleSelectTemplate(template: PermissionTemplate) {
        _state.update { it.copy(selectedTemplate = template) }
    }

    /**
     * Handle permission code generation / 处理权限代码生成
     */
    private fun handleGeneratePermissionCode() {
        viewModelScope.launch {
            val template = _state.value.selectedTemplate
            if (template == null) {
                _state.update { it.copy(error = "Please select a template first / 请先选择一个模板") }
                return@launch
            }

            _state.update { it.copy(isLoading = true) }

            try {
                delay(200) // Simulate processing / 模拟处理

                _state.update {
                    it.copy(
                        generatedPermissionCode = template.templateCode,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to generate code: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Handle permission code copy / 处理权限代码复制
     */
    private fun handleCopyPermissionCode() {
        viewModelScope.launch {
            val code = _state.value.generatedPermissionCode
            if (code.isBlank()) {
                _effect.emit(LocalNetworkToolEffect.ShowSnackbar("No code to copy / 没有可复制的代码", isError = true))
                return@launch
            }

            _effect.emit(LocalNetworkToolEffect.CodeCopied(code))
            _effect.emit(LocalNetworkToolEffect.ShowSnackbar("Code copied to clipboard / 代码已复制到剪贴板"))
        }
    }

    // ============================================================
    // Settings Handlers — 设置处理器
    // ============================================================
    /**
     * Handle report format toggle / 处理报告格式切换
     */
    private fun handleToggleReportFormat(format: ReportFormat) {
        _state.update { currentState ->
            val newFormats = if (format in currentState.selectedFormats) {
                currentState.selectedFormats - format
            } else {
                currentState.selectedFormats + format
            }
            currentState.copy(selectedFormats = newFormats.ifEmpty { listOf(ReportFormat.JSON) })
        }
    }

    /**
     * Handle target SDK override set / 处理目标 SDK 覆盖设置
     */
    private fun handleSetTargetSdkOverride(sdk: Int?) {
        _state.update { it.copy(targetSdkOverride = sdk) }
    }

    // ============================================================
    // Scan Implementation — 扫描实现
    // ============================================================
    /**
     * Validate Android project structure / 验证 Android 项目结构
     */
    private fun validateAndroidProject(projectPath: String): Boolean {
        val buildFile = File(projectPath, "build.gradle.kts")
        val appDir = File(projectPath, "app/src/main/java")
        return buildFile.exists() && appDir.exists()
    }

    /**
     * Detect targetSdkVersion from build.gradle / 从 build.gradle 检测 targetSdkVersion
     */
    private fun detectTargetSdk(projectPath: String): Int {
        // Try app/build.gradle.kts first / 首先尝试 app/build.gradle.kts
        val appBuildFile = File(projectPath, "app/build.gradle.kts")
        if (appBuildFile.exists()) {
            val content = appBuildFile.readText()
            val match = Regex("""targetSdk[VW]ersion\s*[:=]\s*(\d+)""").find(content)
            if (match != null) {
                return match.groupValues[1].toIntOrNull() ?: 35
            }
        }

        // Try build.gradle / 尝试 build.gradle
        val buildFile = File(projectPath, "build.gradle.kts")
        if (buildFile.exists()) {
            val content = buildFile.readText()
            val match = Regex("""targetSdk[VW]ersion\s*[:=]\s*(\d+)""").find(content)
            if (match != null) {
                return match.groupValues[1].toIntOrNull() ?: 35
            }
        }

        return 35 // Default / 默认值
    }

    /**
     * Scan for InetAddress.getByName() usage / 扫描 InetAddress.getByName() 使用
     */
    private fun scanInetAddressUsage(projectPath: String): List<LanAccessPath> {
        val results = mutableListOf<LanAccessPath>()
        val javaDir = File(projectPath, "app/src/main/java")
        val ktDir = File(projectPath, "app/src/main/kotlin")

        val dirs = listOfNotNull(
            if (javaDir.exists()) javaDir else null,
            if (ktDir.exists()) ktDir else null
        )

        for (dir in dirs) {
            dir.walkTopDown()
                .filter { it.extension in listOf("kt", "java") }
                .forEach { file ->
                    val content = file.readText()
                    // Pattern: InetAddress.getByName("...") or InetAddress.getAllByName("...")
                    val pattern = Regex("""InetAddress\s*\.\s*(?:getByName|getAllByName)\s*\(\s*"([^"]+)"""")
                    pattern.findAll(content).forEach { match ->
                        val ip = match.groupValues[1]
                        val lineNumber = content.substring(0, match.range.first).count { it == '\n' } + 1
                        results.add(
                            LanAccessPath(
                                id = UUID.randomUUID().toString(),
                                file = file.absolutePath,
                                line = lineNumber,
                                method = "InetAddress.getByName()",
                                targetIpRange = classifyIpAddress(ip),
                                accessType = AccessType.INET_ADDRESS,
                                permissionStatus = PermissionStatus.UNCERTAIN,
                                codeSnippet = match.value.take(100)
                            )
                        )
                    }
                }
        }

        return results
    }

    /**
     * Scan for Socket usage / 扫描 Socket 使用
     */
    private fun scanSocketUsage(projectPath: String): List<LanAccessPath> {
        val results = mutableListOf<LanAccessPath>()
        val javaDir = File(projectPath, "app/src/main/java")
        val ktDir = File(projectPath, "app/src/main/kotlin")

        val dirs = listOfNotNull(
            if (javaDir.exists()) javaDir else null,
            if (ktDir.exists()) ktDir else null
        )

        for (dir in dirs) {
            dir.walkTopDown()
                .filter { it.extension in listOf("kt", "java") }
                .forEach { file ->
                    val content = file.readText()
                    // Pattern: Socket("host", port) or InetSocketAddress(...)
                    val socketPattern = Regex("""(?:Socket|InetSocketAddress)\s*\(\s*"([^"]+)"""")
                    socketPattern.findAll(content).forEach { match ->
                        val host = match.groupValues[1]
                        val lineNumber = content.substring(0, match.range.first).count { it == '\n' } + 1
                        results.add(
                            LanAccessPath(
                                id = UUID.randomUUID().toString(),
                                file = file.absolutePath,
                                line = lineNumber,
                                method = "Socket / InetSocketAddress",
                                targetIpRange = classifyIpAddress(host),
                                accessType = AccessType.SOCKET,
                                permissionStatus = PermissionStatus.UNCERTAIN,
                                codeSnippet = match.value.take(100)
                            )
                        )
                    }
                }
        }

        return results
    }

    /**
     * Scan for OkHttp/Retrofit HTTP client usage / 扫描 OkHttp/Retrofit HTTP 客户端使用
     */
    private fun scanHttpClientUsage(projectPath: String): List<LanAccessPath> {
        val results = mutableListOf<LanAccessPath>()
        val javaDir = File(projectPath, "app/src/main/java")
        val ktDir = File(projectPath, "app/src/main/kotlin")

        val dirs = listOfNotNull(
            if (javaDir.exists()) javaDir else null,
            if (ktDir.exists()) ktDir else null
        )

        for (dir in dirs) {
            dir.walkTopDown()
                .filter { it.extension in listOf("kt", "java") }
                .forEach { file ->
                    val content = file.readText()
                    // Pattern: baseUrl("http://192.168...") or .newBuilder("http://10...")
                    val baseUrlPattern = Regex("""(?:baseUrl|newBuilder)\s*\(\s*"https?://([^/]+)""")
                    baseUrlPattern.findAll(content).forEach { match ->
                        val host = match.groupValues[1]
                        val lineNumber = content.substring(0, match.range.first).count { it == '\n' } + 1
                        val accessType = if (content.contains("Retrofit")) AccessType.RETROFIT else AccessType.OKHTTP
                        results.add(
                            LanAccessPath(
                                id = UUID.randomUUID().toString(),
                                file = file.absolutePath,
                                line = lineNumber,
                                method = "$accessType.baseUrl / newBuilder",
                                targetIpRange = classifyIpAddress(host),
                                accessType = accessType,
                                permissionStatus = PermissionStatus.UNCERTAIN,
                                codeSnippet = match.value.take(100)
                            )
                        )
                    }
                }
        }

        return results
    }

    /**
     * Scan AndroidManifest.xml for existing permissions / 扫描 AndroidManifest.xml 现有权限
     */
    private fun scanManifestPermissions(projectPath: String): List<String> {
        val manifestFile = File(projectPath, "app/src/main/AndroidManifest.xml")
        if (!manifestFile.exists()) return emptyList()

        val content = manifestFile.readText()
        val permissions = mutableListOf<String>()

        // Match <uses-permission android:name="..." />
        val pattern = Regex("""<uses-permission\s+android:name\s*=\s*"([^"]+)"""")
        pattern.findAll(content).forEach { match ->
            permissions.add(match.groupValues[1])
        }

        return permissions
    }

    /**
     * Classify IP address to RFC1918 range / 将 IP 地址分类到 RFC1918 地址段
     */
    private fun classifyIpAddress(ip: String): String {
        // Check if it's an actual IP or a hostname / 检查是 IP 还是主机名
        val trimmed = ip.trim().removeSuffix("/")

        // Check for RFC1918 private ranges / 检查 RFC1918 私有地址段
        return when {
            trimmed.startsWith("192.168.") -> "192.168.0.0/16 (Class C private / C 类私有)"
            trimmed.startsWith("10.") -> "10.0.0.0/8 (Class A private / A 类私有)"
            trimmed.startsWith("172.") -> {
                val second = trimmed.substringAfter("172.").substringBefore(".").toIntOrNull()
                if (second != null && second in 16..31) {
                    "172.16.0.0/12 (Class B private / B 类私有)"
                } else {
                    "Public IP or non-RFC1918 / 公网 IP 或非 RFC1918"
                }
            }
            trimmed.startsWith("169.254.") -> "169.254.0.0/16 (Link-local / 链路本地)"
            trimmed == "localhost" || trimmed == "127.0.0.1" -> "127.0.0.0/8 (Loopback / 回环地址) — NOT in scope / 不在范围内"
            else -> "Hostname or public IP: $trimmed — needs verification / 主机名或公网 IP — 需验证"
        }
    }

    // ============================================================
    // Helper Functions — 辅助函数
    // ============================================================
    /**
     * Append a log entry / 追加日志条目
     */
    private fun appendLog(level: LogLevel, message: String, details: String = "") {
        _state.update { currentState ->
            val newEntry = ScanLogEntry(
                level = level,
                message = message,
                details = details
            )
            currentState.copy(scanLogs = currentState.scanLogs + newEntry)
        }
    }

    /**
     * Build manifest XML snippet / 构建清单 XML 代码片段
     */
    private fun buildManifestSnippet(permissions: List<String>, isOriginal: Boolean): String {
        return buildString {
            appendLine("<!-- AndroidManifest.xml ${if (isOriginal) "ORIGINAL" else "MODIFIED"} -->")
            appendLine("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">")
            appendLine("    <uses-permission android:name=\"android.permission.INTERNET\" />")
            if (!isOriginal) {
                permissions.forEach { perm ->
                    appendLine("    <uses-permission android:name=\"$perm\" />")
                }
            }
            appendLine("    <!-- ... other declarations ... -->")
            appendLine("</manifest>")
        }
    }

    /**
     * Apply manifest diff to project / 将清单差异应用到项目
     */
    private fun applyManifestToProject(projectPath: String) {
        val manifestFile = File(projectPath, "app/src/main/AndroidManifest.xml")
        if (!manifestFile.exists()) {
            throw IllegalStateException("AndroidManifest.xml not found / 未找到 AndroidManifest.xml")
        }

        val originalContent = manifestFile.readText()
        val permission = "android.permission.ACCESS_LOCAL_NETWORK"

        // Check if already declared / 检查是否已声明
        if (originalContent.contains(permission)) {
            return // Already has the permission / 已有该权限
        }

        // Insert before </manifest> closing tag / 插入到 </manifest> 闭合标签之前
        val newContent = originalContent.replace(
            "</manifest>",
            "    <uses-permission android:name=\"$permission\" />\n</manifest>"
        )

        manifestFile.writeText(newContent)
    }

    /**
     * Generate report content in specified format / 以指定格式生成报告内容
     */
    private fun generateReportContent(report: ComplianceReport, format: ReportFormat): String {
        return when (format) {
            ReportFormat.JSON -> buildJsonReport(report)
            ReportFormat.HTML -> buildHtmlReport(report)
            ReportFormat.MARKDOWN -> buildMarkdownReport(report)
        }
    }

    /**
     * Build JSON format report / 构建 JSON 格式报告
     */
    private fun buildJsonReport(report: ComplianceReport): String {
        val paths = report.accessPaths.joinToString(",\n") { path ->
            """
            {
                "id": "${path.id}",
                "file": "${path.file}",
                "line": ${path.line},
                "method": "${path.method}",
                "targetIpRange": "${path.targetIpRange}",
                "accessType": "${path.accessType.name}",
                "permissionStatus": "${path.permissionStatus.name}"
            }
            """.trimIndent()
        }

        return """
{
    "report": {
        "generatedAt": "${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())}",
        "targetSdk": ${report.targetSdk},
        "requiresPermission": ${report.requiresPermission},
        "summary": {
            "totalPaths": ${report.totalPaths},
            "compliantPaths": ${report.compliantPaths},
            "nonCompliantPaths": ${report.nonCompliantPaths},
            "uncertainPaths": ${report.uncertainPaths},
            "complianceScore": ${report.complianceScore}
        },
        "accessPaths": [$paths]
    }
}
        """.trimIndent()
    }

    /**
     * Build HTML format report / 构建 HTML 格式报告
     */
    private fun buildHtmlReport(report: ComplianceReport): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val scoreColor = when (report.complianceLevel) {
            ComplianceLevel.FULL -> "#4CAF50"
            ComplianceLevel.PARTIAL -> "#FF9800"
            ComplianceLevel.NON_COMPLIANT -> "#F44336"
        }

        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>LAN Permission Compliance Report / 局域网权限合规报告</title>
    <style>
        body { font-family: 'Roboto', sans-serif; margin: 40px; background: #f5f5f5; }
        .container { max-width: 900px; margin: 0 auto; background: white; padding: 32px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        h1 { color: #6750A4; }
        .score { font-size: 48px; font-weight: bold; color: $scoreColor; text-align: center; }
        .summary { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin: 24px 0; }
        .summary-card { padding: 16px; border-radius: 8px; text-align: center; }
        .summary-card.total { background: #E3F2FD; }
        .summary-card.compliant { background: #E8F5E9; }
        .summary-card.non-compliant { background: #FFEBEE; }
        .summary-card.uncertain { background: #FFF3E0; }
        table { width: 100%; border-collapse: collapse; margin-top: 24px; }
        th { background: #6750A4; color: white; padding: 12px; text-align: left; }
        td { padding: 10px; border-bottom: 1px solid #eee; }
        .status-declared { color: #4CAF50; }
        .status-missing { color: #F44336; }
        .status-uncertain { color: #FF9800; }
        .status-not-needed { color: #9E9E9E; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🔒 ACCESS_LOCAL_NETWORK Compliance Report</h1>
        <p>Generated / 生成时间: $timestamp</p>
        <p>Target SDK: ${report.targetSdk} (requires permission: ${report.requiresPermission})</p>

        <div class="score">${report.complianceScore}%</div>
        <p style="text-align:center">${report.summaryText}</p>

        <div class="summary">
            <div class="summary-card total">
                <div style="font-size:24px;font-weight:bold">${report.totalPaths}</div>
                <div>Total Paths / 总路径</div>
            </div>
            <div class="summary-card compliant">
                <div style="font-size:24px;font-weight:bold">${report.compliantPaths}</div>
                <div>Compliant / 合规</div>
            </div>
            <div class="summary-card non-compliant">
                <div style="font-size:24px;font-weight:bold">${report.nonCompliantPaths}</div>
                <div>Non-Compliant / 不合规</div>
            </div>
            <div class="summary-card uncertain">
                <div style="font-size:24px;font-weight:bold">${report.uncertainPaths}</div>
                <div>Uncertain / 不确定</div>
            </div>
        </div>

        <h2>Access Paths Details / 访问路径详情</h2>
        <table>
            <thead>
                <tr>
                    <th>File / 文件</th>
                    <th>Line</th>
                    <th>Method / 方法</th>
                    <th>IP Range / IP 段</th>
                    <th>Status / 状态</th>
                </tr>
            </thead>
            <tbody>
                ${report.accessPaths.joinToString("\n") { path ->
                    """<tr>
                        <td>${path.file.substringAfterLast("/")}</td>
                        <td>${path.line}</td>
                        <td>${path.method}</td>
                        <td>${path.targetIpRange.take(30)}</td>
                        <td class="status-${path.permissionStatus.name.lowercase().replace("_", "-")}">${path.permissionStatus.label}                        <td>${path.permissionStatus.label}</td>
                    </tr>
                    """
                }}
            </tbody>
        </table>
    </div>
</body>
</html>
        """.trimIndent()
    }

    /**
     * Build Markdown format report / 构建 Markdown 格式报告
     */
    private fun buildMarkdownReport(report: ComplianceReport): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        return buildString {
            appendLine("# 🔒 ACCESS_LOCAL_NETWORK Compliance Report")
            appendLine()
            appendLine("**Generated / 生成时间:** $timestamp")
            appendLine()
            appendLine("## Summary / 摘要")
            appendLine()
            appendLine("| Metric | Value |")
            appendLine("|--------|-------|")
            appendLine("| **Compliance Score / 合规评分** | ${report.complianceScore}% |")
            appendLine("| **Compliance Level / 合规等级** | ${report.complianceLevel.label} |")
            appendLine("| **Target SDK** | ${report.targetSdk} |")
            appendLine("| **Requires Permission / 需要权限** | ${report.requiresPermission} |")
            appendLine()
            appendLine("## Path Counts / 路径统计")
            appendLine()
            appendLine("| Status | Count |")
            appendLine("|--------|-------|")
            appendLine("| **Total / 总计** | ${report.totalPaths} |")
            appendLine("| **Compliant / 合规** | ${report.compliantPaths} |")
            appendLine("| **Non-Compliant / 不合规** | ${report.nonCompliantPaths} |")
            appendLine("| **Uncertain / 不确定** | ${report.uncertainPaths} |")
            appendLine()
            appendLine("## Access Paths / 访问路径")
            appendLine()
            appendLine("| File | Line | Method | IP Range | Status |")
            appendLine("|------|------|--------|----------|--------|")
            report.accessPaths.forEach { path ->
                appendLine("| ${path.file.substringAfterLast("/")} | ${path.line} | ${path.method} | ${path.targetIpRange.take(25)} | ${path.permissionStatus.label} |")
            }
            appendLine()
            appendLine("## Notes / 备注")
            appendLine()
            appendLine("- RFC1918 private IP ranges: 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16")
            appendLine("- Android 17 (API 37+) requires ACCESS_LOCAL_NETWORK for LAN access")
            appendLine("- Legacy apps (targetSDK < 37) get implicit ACCESS_LOCAL_NETWORK grant")
            appendLine()
            appendLine("*Report generated by LAN Permission Tool / 报告由局域网权限工具生成*")
        }
    }
}
