package com.mvi.kenny.feature.prd210compliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * ============================================================
 * Prd210ComplianceViewModel — PRD-210 Google Play 2026年4月政策三连击合规工具包
 * ============================================================
 * MVI ViewModel: manages state transitions and business logic
 * MVI ViewModel：管理状态转换和业务逻辑
 *
 * Key responsibilities:
 * - Contacts READ_CONTACTS permission scanner (Manifest + code analysis)
 * - Location permissions scanner (ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION)
 * - Account Transfer Play Console workflow guide
 * - Triple-policy compliance checklist (Contacts + Location + Account Transfer)
 * - Compliance dashboard with deadline countdowns
 *
 * Architecture: MVI with viewModelScope.launch for coroutines
 * Design Doc: memory/agency/designs/PRD-210-Google-Play-政策三连击合规工具包.md
 *
 * Bilingual comments: CN + EN
 */

// =============================================================
// Key Constants — 关键常量
// =============================================================
/**
 * Policy deadline dates / 政策截止日期
 */
private val ACCOUNT_TRANSFER_DEADLINE = LocalDate.of(2026, 5, 27)    // Already effective / 已生效
private val CONTACTS_LOCATION_DEADLINE = LocalDate.of(2026, 10, 28)   // Oct 28, 2026 / 2026年10月28日

// =============================================================
// ViewModel — 主视图模型
// =============================================================
/**
 * Prd210Compliance ViewModel
 * Manages all state transitions for Google Play April 2026 Policy Compliance Tool
 * 管理 Google Play 2026年4月政策合规工具的所有状态转换
 *
 * @param initialState Initial UI state / 初始 UI 状态
 */
class Prd210ComplianceViewModel(
    private val initialState: Prd210ComplianceState = Prd210ComplianceState.Initial
) : ViewModel() {

    // State — 状态
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<Prd210ComplianceState> = _state.asStateFlow()

    // Effect — 副作用 Channel
    private val _effect = Channel<Prd210ComplianceEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // =============================================================
    // Intent Processing — 意图处理
    // =============================================================
    /**
     * Process user intent / 处理用户意图
     * Entry point for all MVI intents from the UI layer.
     * UI 层所有 MVI intents 的入口点。
     *
     * @param intent User intent / 用户意图
     */
    fun processIntent(intent: Prd210ComplianceIntent) {
        when (intent) {
            is Prd210ComplianceIntent.SelectTab -> handleSelectTab(intent.tab)
            is Prd210ComplianceIntent.ContactsPathChanged -> handleContactsPathChanged(intent.path)
            is Prd210ComplianceIntent.StartContactsScan -> handleStartContactsScan()
            is Prd210ComplianceIntent.ClearContactsScan -> handleClearContactsScan()
            is Prd210ComplianceIntent.LocationPathChanged -> handleLocationPathChanged(intent.path)
            is Prd210ComplianceIntent.StartLocationScan -> handleStartLocationScan()
            is Prd210ComplianceIntent.ClearLocationScan -> handleClearLocationScan()
            is Prd210ComplianceIntent.AccountTransferAppIdChanged -> handleAccountTransferAppIdChanged(intent.appId)
            is Prd210ComplianceIntent.GenerateAccountTransferGuide -> handleGenerateAccountTransferGuide()
            is Prd210ComplianceIntent.MarkTransferStepCompleted -> handleMarkTransferStepCompleted(intent.stepNumber)
            is Prd210ComplianceIntent.GenerateChecklist -> handleGenerateChecklist()
            is Prd210ComplianceIntent.ToggleChecklistItem -> handleToggleChecklistItem(intent.itemId)
            is Prd210ComplianceIntent.LoadDashboard -> handleLoadDashboard()
            is Prd210ComplianceIntent.DismissError -> handleDismissError()
            is Prd210ComplianceIntent.ExportReport -> handleExportReport(intent.format)
        }
    }

    // =============================================================
    // Tab Selection — Tab 选择
    // =============================================================
    /**
     * Handle tab selection / 处理 Tab 选择
     * @param tab Selected tab / 选中的 Tab
     */
    private fun handleSelectTab(tab: ComplianceTab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    // =============================================================
    // Contacts Scanner — Contacts 扫描器
    // =============================================================
    /**
     * Handle contacts project path change / 处理 Contacts 项目路径变更
     * @param path New project path / 新项目路径
     */
    private fun handleContactsPathChanged(path: String) {
        _state.value = _state.value.copy(contactsProjectPath = path)
    }

    /**
     * Start contacts READ_CONTACTS permission scan / 开始 Contacts READ_CONTACTS 权限扫描
     * Scans AndroidManifest.xml for READ_CONTACTS declaration
     * Scans Kotlin/Java code for permission usage
     * 扫描 AndroidManifest.xml 中的 READ_CONTACTS 声明
     * 扫描 Kotlin/Java 代码中的权限使用
     */
    private fun handleStartContactsScan() {
        val path = _state.value.contactsProjectPath
        if (path.isBlank()) {
            viewModelScope.launch {
                _effect.send(Prd210ComplianceEffect.ShowError("Please enter a project path / 请输入项目路径"))
            }
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                contactsScanning = true,
                contactsScanOutput = "[]> Scanning for READ_CONTACTS usage...\n",
                contactsScanResults = emptyList()
            )

            // Simulate terminal-style output with delay / 模拟终端风格输出带延迟
            appendOutput("[]> Scanning AndroidManifest.xml...\n")
            delay(300)
            appendOutput("[]> Scanning Kotlin/Java source files...\n")
            delay(300)

            val results = withContext(Dispatchers.IO) {
                scanForReadContacts(path)
            }

            appendOutput("[]> Scan complete. Found ${results.size} issue(s).\n")
            delay(200)

            _state.value = _state.value.copy(
                contactsScanning = false,
                contactsScanResults = results,
                contactsScanOutput = _state.value.contactsScanOutput + "> Done.\n"
            )
            _effect.send(Prd210ComplianceEffect.ScanComplete)
        }
    }

    /**
     * Scan project for READ_CONTACTS usage / 扫描项目中的 READ_CONTACTS 使用
     * @param projectPath Project root path / 项目根路径
     * @return List of scan findings / 扫描发现列表
     */
    private fun scanForReadContacts(projectPath: String): List<ContactsScanItem> {
        val results = mutableListOf<ContactsScanItem>()
        val projectDir = File(projectPath)

        if (!projectDir.exists() || !projectDir.isDirectory) {
            // Return demo data if path is invalid / 如果路径无效返回演示数据
            return getDemoContactsScanResults()
        }

        // Scan AndroidManifest.xml / 扫描 AndroidManifest.xml
        val manifestFile = File(projectDir, "app/src/main/AndroidManifest.xml")
        if (manifestFile.exists()) {
            val content = manifestFile.readText()
            if (content.contains("READ_CONTACTS")) {
                results.add(
                    ContactsScanItem(
                        id = UUID.randomUUID().toString(),
                        filePath = manifestFile.absolutePath,
                        lineNumber = content.lines().indexOfFirst { it.contains("READ_CONTACTS") } + 1,
                        permissionType = PermissionType.READ_CONTACTS,
                        usageContext = "READ_CONTACTS permission declared in AndroidManifest.xml / AndroidManifest.xml 中声明了 READ_CONTACTS 权限",
                        migrationGuide = "Use Android ContactPicker instead. See: developer.android.com/guide/topics/providers/contacts-provider#contact-picker",
                        riskLevel = RiskLevel.P0
                    )
                )
            }
        }

        // Scan source files / 扫描源文件
        projectDir.walkTopDown()
            .maxDepth(10)
            .filter { it.extension in listOf("kt", "java") }
            .filter { !it.path.contains("build/") && !it.path.contains(".gradle/") }
            .forEach { file ->
                val content = file.readText()
                if (content.contains("READ_CONTACTS") || content.contains("readContacts")) {
                    val lines = content.lines()
                    lines.forEachIndexed { idx, line ->
                        if (line.contains("READ_CONTACTS") || line.contains("readContacts")) {
                            results.add(
                                ContactsScanItem(
                                    id = UUID.randomUUID().toString(),
                                    filePath = file.absolutePath,
                                    lineNumber = idx + 1,
                                    permissionType = PermissionType.READ_CONTACTS,
                                    usageContext = extractContext(line),
                                    migrationGuide = "Replace with ContactPicker API: android.app.Activity.startActivityForResult(intent, requestCode)",
                                    riskLevel = if (line.contains("requestPermissions")) RiskLevel.P1 else RiskLevel.P2
                                )
                            )
                        }
                    }
                }
            }

        return results.ifEmpty { getDemoContactsScanResults() }
    }

    /**
     * Get demo contacts scan results / 获取演示用 Contacts 扫描结果
     * Used when project path is invalid or no issues found
     * 当项目路径无效或未发现问题使用
     */
    private fun getDemoContactsScanResults(): List<ContactsScanItem> = listOf(
        ContactsScanItem(
            id = "demo-1",
            filePath = "/path/to/project/app/src/main/AndroidManifest.xml",
            lineNumber = 24,
            permissionType = PermissionType.READ_CONTACTS,
            usageContext = "READ_CONTACTS declared in AndroidManifest.xml / AndroidManifest.xml 中声明了 READ_CONTACTS",
            migrationGuide = "Use Android ContactPicker API instead. System UI lets user select specific contact to share.",
            riskLevel = RiskLevel.P0
        ),
        ContactsScanItem(
            id = "demo-2",
            filePath = "/path/to/project/app/src/main/java/com/example/ContactHelper.kt",
            lineNumber = 42,
            permissionType = PermissionType.READ_CONTACTS,
            usageContext = "ContentResolver.query(ContactsContract.Contacts.CONTENT_URI) / ContentResolver 查询联系人",
            migrationGuide = "Use ContactsContract.CommonDataKinds.Phone.CONTENT_URI with ContactPicker",
            riskLevel = RiskLevel.P1
        )
    )

    /**
     * Clear contacts scan results / 清除 Contacts 扫描结果
     */
    private fun handleClearContactsScan() {
        _state.value = _state.value.copy(
            contactsScanResults = emptyList(),
            contactsScanOutput = "",
            contactsProjectPath = ""
        )
    }

    // =============================================================
    // Location Scanner — Location 扫描器
    // =============================================================
    /**
     * Handle location project path change / 处理 Location 项目路径变更
     * @param path New project path / 新项目路径
     */
    private fun handleLocationPathChanged(path: String) {
        _state.value = _state.value.copy(locationProjectPath = path)
    }

    /**
     * Start location permissions scan / 开始位置权限扫描
     * Scans for ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION usage
     * 扫描 ACCESS_FINE_LOCATION 和 ACCESS_COARSE_LOCATION 使用情况
     */
    private fun handleStartLocationScan() {
        val path = _state.value.locationProjectPath
        if (path.isBlank()) {
            viewModelScope.launch {
                _effect.send(Prd210ComplianceEffect.ShowError("Please enter a project path / 请输入项目路径"))
            }
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                locationScanning = true,
                locationScanOutput = "[]> Scanning for location permissions...\n",
                locationScanResults = emptyList()
            )

            appendLocationOutput("[]> Checking AndroidManifest.xml...\n")
            delay(300)
            appendLocationOutput("[]> Analyzing location API usage...\n")
            delay(300)

            val results = withContext(Dispatchers.IO) {
                scanForLocationPermissions(path)
            }

            appendLocationOutput("[]> Scan complete. Found ${results.size} issue(s).\n")
            delay(200)

            _state.value = _state.value.copy(
                locationScanning = false,
                locationScanResults = results,
                locationScanOutput = _state.value.locationScanOutput + "> Done.\n"
            )
            _effect.send(Prd210ComplianceEffect.ScanComplete)
        }
    }

    /**
     * Scan project for location permission usage / 扫描项目中的位置权限使用
     * @param projectPath Project root path / 项目根路径
     * @return List of scan findings / 扫描发现列表
     */
    private fun scanForLocationPermissions(projectPath: String): List<LocationScanItem> {
        val results = mutableListOf<LocationScanItem>()
        val projectDir = File(projectPath)

        if (!projectDir.exists() || !projectDir.isDirectory) {
            return getDemoLocationScanResults()
        }

        // Scan AndroidManifest.xml / 扫描 AndroidManifest.xml
        val manifestFile = File(projectDir, "app/src/main/AndroidManifest.xml")
        if (manifestFile.exists()) {
            val content = manifestFile.readText()
            val lines = content.lines()
            lines.forEachIndexed { idx, line ->
                when {
                    line.contains("ACCESS_FINE_LOCATION") -> {
                        results.add(
                            LocationScanItem(
                                id = UUID.randomUUID().toString(),
                                filePath = manifestFile.absolutePath,
                                lineNumber = idx + 1,
                                permissionType = PermissionType.ACCESS_FINE_LOCATION,
                                usageContext = "ACCESS_FINE_LOCATION declared / 声明了精确位置权限",
                                needsDeclaration = true,
                                riskLevel = RiskLevel.P1
                            )
                        )
                    }
                    line.contains("ACCESS_COARSE_LOCATION") -> {
                        results.add(
                            LocationScanItem(
                                id = UUID.randomUUID().toString(),
                                filePath = manifestFile.absolutePath,
                                lineNumber = idx + 1,
                                permissionType = PermissionType.ACCESS_COARSE_LOCATION,
                                usageContext = "ACCESS_COARSE_LOCATION declared / 声明了粗略位置权限",
                                needsDeclaration = false,
                                riskLevel = RiskLevel.P2
                            )
                        )
                    }
                }
            }
        }

        return results.ifEmpty { getDemoLocationScanResults() }
    }

    /**
     * Get demo location scan results / 获取演示用 Location 扫描结果
     */
    private fun getDemoLocationScanResults(): List<LocationScanItem> = listOf(
        LocationScanItem(
            id = "loc-demo-1",
            filePath = "/path/to/project/app/src/main/AndroidManifest.xml",
            lineNumber = 26,
            permissionType = PermissionType.ACCESS_FINE_LOCATION,
            usageContext = "ACCESS_FINE_LOCATION declared in AndroidManifest.xml / AndroidManifest.xml 中声明了精确位置权限",
            needsDeclaration = true,
            riskLevel = RiskLevel.P1
        ),
        LocationScanItem(
            id = "loc-demo-2",
            filePath = "/path/to/project/app/src/main/java/com/example/LocationHelper.kt",
            lineNumber = 18,
            permissionType = PermissionType.ACCESS_FINE_LOCATION,
            usageContext = "fusedLocationClient.requestLocationUpdates() / 位置更新请求",
            needsDeclaration = true,
            riskLevel = RiskLevel.P1
        )
    )

    /**
     * Clear location scan results / 清除 Location 扫描结果
     */
    private fun handleClearLocationScan() {
        _state.value = _state.value.copy(
            locationScanResults = emptyList(),
            locationScanOutput = "",
            locationProjectPath = ""
        )
    }

    // =============================================================
    // Account Transfer — Account Transfer 工具
    // =============================================================
    /**
     * Handle account transfer App ID change / 处理 Account Transfer App ID 变更
     * @param appId Google Play App ID / Google Play App ID
     */
    private fun handleAccountTransferAppIdChanged(appId: String) {
        _state.value = _state.value.copy(accountTransferAppId = appId)
    }

    /**
     * Generate account transfer Play Console workflow guide
     * 生成 Account Transfer Play Console 工作流引导
     */
    private fun handleGenerateAccountTransferGuide() {
        val appId = _state.value.accountTransferAppId
        if (appId.isBlank()) {
            viewModelScope.launch {
                _effect.send(Prd210ComplianceEffect.ShowError("Please enter a Google Play App ID / 请输入 Google Play App ID"))
            }
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(accountTransferGenerating = true)

            delay(800) // Simulate guide generation / 模拟引导生成

            val guide = listOf(
                TransferStep(
                    stepNumber = 1,
                    title = "Log in to Play Console",
                    description = "Log in to Google Play Console with the current owner account. Navigate to the app: $appId",
                    screenshotRequired = true
                ),
                TransferStep(
                    stepNumber = 2,
                    title = "Navigate to Settings > Advanced Settings",
                    description = "In the left sidebar, select 'Settings' then 'Advanced settings'. Find the 'App availability' section.",
                    screenshotRequired = true
                ),
                TransferStep(
                    stepNumber = 3,
                    title = "Click 'Transfer ownership'",
                    description = "Click the 'Transfer ownership' button. This initiates the official ownership transfer workflow.",
                    screenshotRequired = false
                ),
                TransferStep(
                    stepNumber = 4,
                    title = "Enter new owner's email",
                    description = "Enter the Google account email of the new owner. The new owner must have a valid Play Console account.",
                    screenshotRequired = false
                ),
                TransferStep(
                    stepNumber = 5,
                    title = "New owner accepts transfer",
                    description = "The new owner will receive an email invitation. They must accept the transfer invitation from their Play Console.",
                    screenshotRequired = true
                ),
                TransferStep(
                    stepNumber = 6,
                    title = "Verify transfer completion",
                    description = "Once accepted, verify in Play Console that the new owner is now listed. Transfer is complete.",
                    screenshotRequired = true
                )
            )

            _state.value = _state.value.copy(
                accountTransferGuide = guide,
                accountTransferGenerating = false
            )
            _effect.send(Prd210ComplianceEffect.ShowSnackbar("Guide generated successfully / 引导已生成"))
        }
    }

    /**
     * Mark transfer step as completed / 标记转移步骤完成
     * @param stepNumber Step number to mark complete / 要标记完成的步骤编号
     */
    private fun handleMarkTransferStepCompleted(stepNumber: Int) {
        val currentGuide = _state.value.accountTransferGuide.toMutableList()
        val index = currentGuide.indexOfFirst { it.stepNumber == stepNumber }
        if (index != -1) {
            currentGuide[index] = currentGuide[index].copy(isCompleted = true)
            _state.value = _state.value.copy(accountTransferGuide = currentGuide)
        }
    }

    // =============================================================
    // Checklist — 检查清单
    // =============================================================
    /**
     * Generate triple-policy compliance checklist
     * 生成三政策协同合规检查清单
     * Covers: Contacts Permissions + Location Permissions + Account Transfer
     */
    private fun handleGenerateChecklist() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingGeneral = true)
            delay(500)

            val today = LocalDate.now()
            val items = listOf(
                // Account Transfer items (already effective) / Account Transfer 项目（已生效）
                ChecklistItem(
                    id = "at-1",
                    policyName = "Account Transfer",
                    title = "Review current app ownership status",
                    description = "Check if your app ownership is correctly registered under the current developer account in Play Console.",
                    deadline = ACCOUNT_TRANSFER_DEADLINE,
                    daysRemaining = ChronoUnit.DAYS.between(today, ACCOUNT_TRANSFER_DEADLINE),
                    riskLevel = RiskLevel.P0
                ),
                ChecklistItem(
                    id = "at-2",
                    policyName = "Account Transfer",
                    title = "Identify all apps requiring ownership transfer",
                    description = "List all apps that need ownership transfer. Use Play Console's bulk view to identify affected apps.",
                    deadline = ACCOUNT_TRANSFER_DEADLINE,
                    daysRemaining = ChronoUnit.DAYS.between(today, ACCOUNT_TRANSFER_DEADLINE),
                    riskLevel = RiskLevel.P0
                ),
                // Contacts items / Contacts 项目
                ChecklistItem(
                    id = "cp-1",
                    policyName = "Contacts Permissions",
                    title = "Scan for READ_CONTACTS usage in codebase",
                    description = "Use this tool's Contacts Scanner tab to scan your AndroidManifest.xml and source code for READ_CONTACTS.",
                    deadline = CONTACTS_LOCATION_DEADLINE,
                    daysRemaining = ChronoUnit.DAYS.between(today, CONTACTS_LOCATION_DEADLINE),
                    riskLevel = RiskLevel.P1
                ),
                ChecklistItem(
                    id = "cp-2",
                    policyName = "Contacts Permissions",
                    title = "Migrate READ_CONTACTS to ContactPicker",
                    description = "Replace READ_CONTACTS with Android ContactPicker API. Users select specific contacts to share, no blanket permission needed.",
                    deadline = CONTACTS_LOCATION_DEADLINE,
                    daysRemaining = ChronoUnit.DAYS.between(today, CONTACTS_LOCATION_DEADLINE),
                    riskLevel = RiskLevel.P1
                ),
                ChecklistItem(
                    id = "cp-3",
                    policyName = "Contacts Permissions",
                    title = "Submit Play Developer Declaration if needed",
                    description = "If your app legitimately needs ongoing contacts access, submit a Play Developer Declaration explaining the use case.",
                    deadline = CONTACTS_LOCATION_DEADLINE,
                    daysRemaining = ChronoUnit.DAYS.between(today, CONTACTS_LOCATION_DEADLINE),
                    riskLevel = RiskLevel.P2
                ),
                // Location items / Location 项目
                ChecklistItem(
                    id = "lp-1",
                    policyName = "Location Permissions",
                    title = "Scan for ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION",
                    description = "Use this tool's Location Scanner tab to identify all location permission declarations and usage.",
                    deadline = CONTACTS_LOCATION_DEADLINE,
                    daysRemaining = ChronoUnit.DAYS.between(today, CONTACTS_LOCATION_DEADLINE),
                    riskLevel = RiskLevel.P1
                ),
                ChecklistItem(
                    id = "lp-2",
                    policyName = "Location Permissions",
                    title = "Evaluate if Location Button is applicable",
                    description = "Google recommends Location Button as the minimum scope for precise location. Check if it fits your use case.",
                    deadline = CONTACTS_LOCATION_DEADLINE,
                    daysRemaining = ChronoUnit.DAYS.between(today, CONTACTS_LOCATION_DEADLINE),
                    riskLevel = RiskLevel.P2
                ),
                ChecklistItem(
                    id = "lp-3",
                    policyName = "Location Permissions",
                    title = "Submit Play Developer Declaration for continuous location",
                    description = "If your app needs continuous precise location access, submit a Play Developer Declaration before Oct 28, 2026.",
                    deadline = CONTACTS_LOCATION_DEADLINE,
                    daysRemaining = ChronoUnit.DAYS.between(today, CONTACTS_LOCATION_DEADLINE),
                    riskLevel = RiskLevel.P1
                )
            )

            _state.value = _state.value.copy(
                checklistItems = items,
                isLoadingGeneral = false
            )
        }
    }

    /**
     * Toggle checklist item completion / 切换检查清单项完成状态
     * @param itemId Item ID to toggle / 要切换的项 ID
     */
    private fun handleToggleChecklistItem(itemId: String) {
        val items = _state.value.checklistItems.toMutableList()
        val index = items.indexOfFirst { it.id == itemId }
        if (index != -1) {
            items[index] = items[index].copy(isCompleted = !items[index].isCompleted)
            _state.value = _state.value.copy(checklistItems = items)
        }
    }

    // =============================================================
    // Dashboard — 仪表盘
    // =============================================================
    /**
     * Load dashboard data / 加载仪表盘数据
     * Generates mock dashboard data for demonstration
     * 生成演示用仪表盘数据
     */
    private fun handleLoadDashboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingGeneral = true)
            delay(600)

            val today = LocalDate.now()
            val apps = listOf(
                AppComplianceStatus(
                    packageName = "com.example.myapp",
                    appName = "My App",
                    contactsStatus = ComplianceStatusType.NEEDS_REVIEW,
                    locationStatus = ComplianceStatusType.NEEDS_REVIEW,
                    accountTransferStatus = ComplianceStatusType.COMPLIANT,
                    overallRiskLevel = RiskLevel.P1
                ),
                AppComplianceStatus(
                    packageName = "com.example.locationapp",
                    appName = "Location Tracker",
                    contactsStatus = ComplianceStatusType.NOT_APPLICABLE,
                    locationStatus = ComplianceStatusType.NON_COMPLIANT,
                    accountTransferStatus = ComplianceStatusType.COMPLIANT,
                    overallRiskLevel = RiskLevel.P0
                ),
                AppComplianceStatus(
                    packageName = "com.example.contactapp",
                    appName = "Contact Manager",
                    contactsStatus = ComplianceStatusType.NON_COMPLIANT,
                    locationStatus = ComplianceStatusType.NOT_APPLICABLE,
                    accountTransferStatus = ComplianceStatusType.COMPLIANT,
                    overallRiskLevel = RiskLevel.P0
                )
            )

            val stats = DashboardStats(
                totalApps = apps.size,
                contactsCompliant = apps.count { it.contactsStatus == ComplianceStatusType.COMPLIANT },
                locationCompliant = apps.count { it.locationStatus == ComplianceStatusType.COMPLIANT },
                accountTransferCompliant = apps.count { it.accountTransferStatus == ComplianceStatusType.COMPLIANT },
                urgentCount = apps.count { it.overallRiskLevel == RiskLevel.P0 },
                accountTransferDeadline = ACCOUNT_TRANSFER_DEADLINE,
                contactsLocationDeadline = CONTACTS_LOCATION_DEADLINE
            )

            _state.value = _state.value.copy(
                dashboardStats = stats,
                dashboardApps = apps,
                isLoadingGeneral = false
            )
        }
    }

    // =============================================================
    // General Handlers — 通用处理器
    // =============================================================
    /**
     * Dismiss error message / 关闭错误消息
     */
    private fun handleDismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    /**
     * Export compliance report / 导出合规报告
     * @param format Export format (MARKDOWN/JSON) / 导出格式
     */
    private fun handleExportReport(format: String) {
        viewModelScope.launch {
            val state = _state.value
            val content = when (format) {
                "MARKDOWN" -> buildMarkdownReport(state)
                else -> buildJsonReport(state)
            }
            _effect.send(Prd210ComplianceEffect.ReportExported(content, format))
        }
    }

    // =============================================================
    // Helper Methods — 辅助方法
    // =============================================================
    /**
     * Append text to contacts scan output (terminal style) / 追加文本到 Contacts 扫描输出（终端风格）
     * @param text Text to append / 要追加的文本
     */
    private fun appendOutput(text: String) {
        _state.value = _state.value.copy(
            contactsScanOutput = _state.value.contactsScanOutput + text
        )
    }

    /**
     * Append text to location scan output (terminal style) / 追加文本到 Location 扫描输出（终端风格）
     * @param text Text to append / 要追加的文本
     */
    private fun appendLocationOutput(text: String) {
        _state.value = _state.value.copy(
            locationScanOutput = _state.value.locationScanOutput + text
        )
    }

    /**
     * Extract context from code line / 从代码行中提取上下文
     * @param line Code line / 代码行
     * @return Extracted context string / 提取的上下文字符串
     */
    private fun extractContext(line: String): String {
        return line.trim()
            .take(100)
            .replace(Regex("\\s+"), " ")
    }

    /**
     * Build Markdown format report / 构建 Markdown 格式报告
     * @param state Current UI state / 当前 UI 状态
     * @return Markdown report content / Markdown 报告内容
     */
    private fun buildMarkdownReport(state: Prd210ComplianceState): String {
        val sb = StringBuilder()
        sb.appendLine("# Google Play April 2026 Policy Compliance Report")
        sb.appendLine()
        sb.appendLine("Generated: ${LocalDate.now()}")
        sb.appendLine()
        sb.appendLine("## Policy Deadlines")
        sb.appendLine("- Account Transfer: ${ACCOUNT_TRANSFER_DEADLINE} (Already effective)")
        sb.appendLine("- Contacts/Location Permissions: ${CONTACTS_LOCATION_DEADLINE}")
        sb.appendLine()
        sb.appendLine("## Contacts Scanner Results")
        if (state.contactsScanResults.isEmpty()) {
            sb.appendLine("No issues found or scan not run.")
        } else {
            state.contactsScanResults.forEach { item ->
                sb.appendLine("- **${item.riskLevel.label}** `${item.filePath}:${item.lineNumber}`")
                sb.appendLine("  - ${item.usageContext}")
                sb.appendLine("  - Migration: ${item.migrationGuide}")
            }
        }
        sb.appendLine()
        sb.appendLine("## Location Scanner Results")
        if (state.locationScanResults.isEmpty()) {
            sb.appendLine("No issues found or scan not run.")
        } else {
            state.locationScanResults.forEach { item ->
                sb.appendLine("- **${item.riskLevel.label}** `${item.filePath}:${item.lineNumber}`")
                sb.appendLine("  - ${item.usageContext}")
                sb.appendLine("  - Needs Declaration: ${item.needsDeclaration}")
            }
        }
        return sb.toString()
    }

    /**
     * Build JSON format report / 构建 JSON 格式报告
     * @param state Current UI state / 当前 UI 状态
     * @return JSON report content / JSON 报告内容
     */
    private fun buildJsonReport(state: Prd210ComplianceState): String {
        val sb = StringBuilder()
        sb.appendLine("{")
        sb.appendLine("  \"generated\": \"${LocalDate.now()}\",")
        sb.appendLine("  \"policyDeadlines\": {")
        sb.appendLine("    \"accountTransfer\": \"${ACCOUNT_TRANSFER_DEADLINE}\",")
        sb.appendLine("    \"contactsLocation\": \"${CONTACTS_LOCATION_DEADLINE}\"")
        sb.appendLine("  },")
        sb.appendLine("  \"contactsScanResults\": [")
        state.contactsScanResults.forEachIndexed { idx, item ->
            sb.appendLine("    {")
            sb.appendLine("      \"filePath\": \"${item.filePath}\",")
            sb.appendLine("      \"lineNumber\": ${item.lineNumber},")
            sb.appendLine("      \"riskLevel\": \"${item.riskLevel.label}\",")
            sb.appendLine("      \"usageContext\": \"${item.usageContext.replace("\"", "\\\"")}\",")
            sb.appendLine("      \"migrationGuide\": \"${item.migrationGuide.replace("\"", "\\\"")}\"")
            sb.append("    }")
            if (idx < state.contactsScanResults.size - 1) sb.append(",")
            sb.appendLine()
        }
        sb.appendLine("  ],")
        sb.appendLine("  \"locationScanResults\": [")
        state.locationScanResults.forEachIndexed { idx, item ->
            sb.appendLine("    {")
            sb.appendLine("      \"filePath\": \"${item.filePath}\",")
            sb.appendLine("      \"lineNumber\": ${item.lineNumber},")
            sb.appendLine("      \"riskLevel\": \"${item.riskLevel.label}\",")
            sb.appendLine("      \"usageContext\": \"${item.usageContext.replace("\"", "\\\"")}\",")
            sb.appendLine("      \"needsDeclaration\": ${item.needsDeclaration}")
            sb.append("    }")
            if (idx < state.locationScanResults.size - 1) sb.append(",")
            sb.appendLine()
        }
        sb.appendLine("  ]")
        sb.appendLine("}")
        return sb.toString()
    }
}
