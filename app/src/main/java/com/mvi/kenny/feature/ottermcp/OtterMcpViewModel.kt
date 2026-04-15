package com.mvi.kenny.feature.ottermcp

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

class OtterMcpViewModel : ViewModel() {
    private val _state = MutableStateFlow(OtterMcpState.Initial)
    val state: StateFlow<OtterMcpState> = _state.asStateFlow()
    private val _effect = Channel<OtterMcpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadMockServers()
        loadMockRecentCalls()
        loadMockTemplates()
    }

    fun sendIntent(intent: OtterMcpIntent) {
        when (intent) {
            is OtterMcpIntent.NavigateTo -> { _state.value = _state.value.copy(currentScreen = intent.screen) }
            is OtterMcpIntent.LoadDashboard -> loadDashboard()
            is OtterMcpIntent.RefreshConnections -> refreshConnections()
            is OtterMcpIntent.Dashboard -> handleDashboard(intent.intent)
            is OtterMcpIntent.ConnectionWizard -> handleConnectionWizard(intent.intent)
            is OtterMcpIntent.TemplateGallery -> handleTemplateGallery(intent.intent)
            is OtterMcpIntent.Security -> handleSecurity(intent.intent)
            is OtterMcpIntent.AuditLog -> handleAuditLog(intent.intent)
            is OtterMcpIntent.DeviceInteraction -> handleDeviceInteraction(intent.intent)
        }
    }

    private fun handleDashboard(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.LoadDashboard -> loadDashboard()
            is DashboardIntent.RefreshConnectionStatus -> refreshConnections()
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(dashboardState = _state.value.dashboardState.copy(isLoading = true))
            delay(500)
            loadMockServers(); loadMockRecentCalls()
            _state.value = _state.value.copy(dashboardState = _state.value.dashboardState.copy(isLoading = false))
        }
    }

    private fun refreshConnections() {
        viewModelScope.launch {
            _state.value = _state.value.copy(dashboardState = _state.value.dashboardState.copy(isLoading = true))
            delay(1000)
            _state.value = _state.value.copy(dashboardState = _state.value.dashboardState.copy(isLoading = false))
            _effect.send(OtterMcpEffect.ShowToast("Connection status refreshed"))
        }
    }

    private fun loadMockServers() {
        val mockServers = listOf(
            McpServerState("server-1", "Android-MCP (本地设备交互)", "http://localhost:3456", true, System.currentTimeMillis(), listOf("device.screenshot", "device.install", "device.input")),
            McpServerState("server-2", "Figma Design Sync", "https://mcp.figma.com/v1", true, System.currentTimeMillis() - 30000, listOf("figma.get_file", "figma.get_images")),
            McpServerState("server-3", "Linear Issue Tracker", "https://mcp.linear.app/v1", false, System.currentTimeMillis() - 120000, listOf("linear.list_issues", "linear.create_issue"))
        )
        _state.value = _state.value.copy(dashboardState = _state.value.dashboardState.copy(servers = mockServers))
    }

    private fun loadMockRecentCalls() {
        val now = System.currentTimeMillis()
        val mockCalls = listOf(
            McpToolCall("call-1", "device.screenshot", "Android-MCP", "server-1", now - 60000, "{device: 'emulator-5554'}", """{"device": "emulator-5554", "format": "png"}""", "Screenshot captured", """{"status": "success", "path": "/tmp/screenshot.png"}""", true),
            McpToolCall("call-2", "figma.get_file", "Figma Design Sync", "server-2", now - 120000, "{file_key: 'abc123'}", """{"file_key": "abc123", "depth": 1}""", "File retrieved", """{"status": "success"}""", true),
            McpToolCall("call-3", "linear.create_issue", "Linear Issue Tracker", "server-3", now - 180000, "{title: 'Bug fix'}", """{"title": "Bug fix", "team_id": "team-1"}""", "Connection failed", """{"error": "Connection refused"}""", false)
        )
        _state.value = _state.value.copy(dashboardState = _state.value.dashboardState.copy(recentCalls = mockCalls))
    }

    private fun handleConnectionWizard(intent: ConnectionWizardIntent) {
        when (intent) {
            is ConnectionWizardIntent.SelectConnectionType -> { _state.value = _state.value.copy(connectionWizardState = _state.value.connectionWizardState.copy(connectionType = intent.type)) }
            is ConnectionWizardIntent.UpdateServerUrl -> { _state.value = _state.value.copy(connectionWizardState = _state.value.connectionWizardState.copy(serverUrl = intent.url)) }
            is ConnectionWizardIntent.UpdateAuthToken -> { _state.value = _state.value.copy(connectionWizardState = _state.value.connectionWizardState.copy(authToken = intent.token)) }
            is ConnectionWizardIntent.UpdateServerName -> { _state.value = _state.value.copy(connectionWizardState = _state.value.connectionWizardState.copy(serverName = intent.name)) }
            is ConnectionWizardIntent.ToggleTool -> { val tools = _state.value.connectionWizardState.selectedTools; _state.value = _state.value.copy(connectionWizardState = _state.value.connectionWizardState.copy(selectedTools = if (tools.contains(intent.tool)) tools - intent.tool else tools + intent.tool)) }
            is ConnectionWizardIntent.NextStep -> { val step = _state.value.connectionWizardState.currentStep; if (step < 2) _state.value = _state.value.copy(connectionWizardState = _state.value.connectionWizardState.copy(currentStep = step + 1)) }
            is ConnectionWizardIntent.PreviousStep -> { val step = _state.value.connectionWizardState.currentStep; if (step > 0) _state.value = _state.value.copy(connectionWizardState = _state.value.connectionWizardState.copy(currentStep = step - 1)) }
            is ConnectionWizardIntent.TestConnection -> { viewModelScope.launch { _state.value = _state.value.copy(connectionWizardState = _state.value.connectionWizardState.copy(isTesting = true)); delay(2000); _state.value = _state.value.copy(connectionWizardState = _state.value.connectionWizardState.copy(isTesting = false, connectionTestResult = TestResult(true, "Connection successful / 连接成功", listOf("tool.1", "tool.2", "tool.3")))) } }
            is ConnectionWizardIntent.SaveConnection -> { viewModelScope.launch { _state.value = _state.value.copy(connectionWizardState = _state.value.connectionWizardState.copy(isSaving = true)); delay(1000); _state.value = _state.value.copy(connectionWizardState = _state.value.connectionWizardState.copy(isSaving = false)); _effect.send(OtterMcpEffect.ShowToast("Connection saved")); _state.value = _state.value.copy(currentScreen = SubScreen.DASHBOARD); loadDashboard() } }
            is ConnectionWizardIntent.Reset -> { _state.value = _state.value.copy(connectionWizardState = ConnectionWizardState.Initial) }
        }
    }

    private fun handleTemplateGallery(intent: TemplateGalleryIntent) {
        when (intent) {
            is TemplateGalleryIntent.SelectTab -> { _state.value = _state.value.copy(templateGalleryState = _state.value.templateGalleryState.copy(selectedTab = intent.category, templates = mockTemplates.filter { it.category == intent.category })) }
            is TemplateGalleryIntent.SelectTemplate -> { _state.value = _state.value.copy(templateGalleryState = _state.value.templateGalleryState.copy(selectedTemplate = intent.template)) }
            is TemplateGalleryIntent.DismissTemplateDetail -> { _state.value = _state.value.copy(templateGalleryState = _state.value.templateGalleryState.copy(selectedTemplate = null)) }
            is TemplateGalleryIntent.ImportTemplate -> { viewModelScope.launch { _state.value = _state.value.copy(templateGalleryState = _state.value.templateGalleryState.copy(isImporting = true)); delay(1500); _state.value = _state.value.copy(templateGalleryState = _state.value.templateGalleryState.copy(isImporting = false, selectedTemplate = null)); _effect.send(OtterMcpEffect.ShowToast("Template imported: ${intent.template.name}")) } }
        }
    }

    private fun loadMockTemplates() {
        _state.value = _state.value.copy(templateGalleryState = _state.value.templateGalleryState.copy(templates = mockTemplates.filter { it.category == TemplateCategory.DEVICE_INTERACTION }))
    }

    private fun handleSecurity(intent: SecurityIntent) {
        when (intent) {
            is SecurityIntent.ToggleToolPermission -> { val matrix = _state.value.securityState.permissionMatrix.toMutableList(); val idx = matrix.indexOfFirst { it.serverId == intent.serverId && it.toolName == intent.toolName }; if (idx >= 0) { matrix[idx] = matrix[idx].copy(isAllowed = intent.allowed); _state.value = _state.value.copy(securityState = _state.value.securityState.copy(permissionMatrix = matrix)) } }
            is SecurityIntent.ToggleSensitiveOps -> { if (!intent.enabled) viewModelScope.launch { _effect.send(OtterMcpEffect.ShowError("Warning: Disabling sensitive operations may pose security risks")) }; _state.value = _state.value.copy(securityState = _state.value.securityState.copy(sensitiveOpsEnabled = intent.enabled)) }
            is SecurityIntent.SaveSettings -> { viewModelScope.launch { _state.value = _state.value.copy(securityState = _state.value.securityState.copy(isSaving = true)); delay(800); _state.value = _state.value.copy(securityState = _state.value.securityState.copy(isSaving = false)); _effect.send(OtterMcpEffect.ShowToast("Security settings saved")) } }
            is SecurityIntent.LoadSettings -> { val servers = _state.value.dashboardState.servers; val matrix = servers.flatMap { server -> server.allowedTools.map { PermissionMatrix(server.id, server.name, it, true) } }; _state.value = _state.value.copy(securityState = _state.value.securityState.copy(permissionMatrix = matrix, servers = servers)) }
        }
    }

    private fun handleAuditLog(intent: AuditLogIntent) {
        when (intent) {
            is AuditLogIntent.LoadLogs -> { viewModelScope.launch { _state.value = _state.value.copy(auditLogState = _state.value.auditLogState.copy(isLoading = true, filters = intent.filters)); delay(800); val logs = generateMockAuditLogs(); _state.value = _state.value.copy(auditLogState = _state.value.auditLogState.copy(logs = logs, isLoading = false, hasMore = logs.size >= 20)) } }
            is AuditLogIntent.LoadMore -> { viewModelScope.launch { _state.value = _state.value.copy(auditLogState = _state.value.auditLogState.copy(isLoading = true)); delay(500); val more = generateMockAuditLogs(_state.value.auditLogState.logs.size); _state.value = _state.value.copy(auditLogState = _state.value.auditLogState.copy(logs = _state.value.auditLogState.logs + more, isLoading = false, hasMore = more.isNotEmpty())) } }
            is AuditLogIntent.Refresh -> { viewModelScope.launch { _state.value = _state.value.copy(auditLogState = _state.value.auditLogState.copy(isLoading = true)); delay(800); val logs = generateMockAuditLogs(); _state.value = _state.value.copy(auditLogState = _state.value.auditLogState.copy(logs = logs, isLoading = false)) } }
            is AuditLogIntent.SelectLog -> { _state.value = _state.value.copy(auditLogState = _state.value.auditLogState.copy(selectedCall = intent.call)) }
            is AuditLogIntent.DismissDetail -> { _state.value = _state.value.copy(auditLogState = _state.value.auditLogState.copy(selectedCall = null)) }
            is AuditLogIntent.ExportLogs -> { viewModelScope.launch { _state.value = _state.value.copy(auditLogState = _state.value.auditLogState.copy(isExporting = true)); delay(1500); _state.value = _state.value.copy(auditLogState = _state.value.auditLogState.copy(isExporting = false)); _effect.send(OtterMcpEffect.ShowToast("Export completed")) } }
            is AuditLogIntent.UpdateFilters -> { viewModelScope.launch { _state.value = _state.value.copy(auditLogState = _state.value.auditLogState.copy(isLoading = true, filters = intent.filters)); delay(800); _state.value = _state.value.copy(auditLogState = _state.value.auditLogState.copy(logs = generateMockAuditLogs(), isLoading = false)) } }
        }
    }

    private fun generateMockAuditLogs(offset: Int = 0): List<McpToolCall> {
        val tools = listOf("device.screenshot", "device.install", "device.input", "figma.get_file", "linear.list_issues", "firebase.analytics")
        val servers = listOf("Android-MCP" to "server-1", "Figma Sync" to "server-2", "Linear" to "server-3")
        return (0 until 20).map { i ->
            val idx = offset + i; val (name, sid) = servers[idx % servers.size]; val tool = tools[idx % tools.size]; val success = idx % 5 != 0
            McpToolCall("call-$idx", tool, name, sid, System.currentTimeMillis() - idx * 60000L, "{param: 'value-$idx'}", "", if (success) "Success" else "Error", "", success)
        }
    }

    private fun handleDeviceInteraction(intent: DeviceInteractionIntent) {
        when (intent) {
            is DeviceInteractionIntent.RefreshDevices -> { viewModelScope.launch { val devs = listOf(AndroidDevice("device-1", "Pixel 7 Pro (Emulator)", "Pixel 7 Pro", true), AndroidDevice("device-2", "Samsung Galaxy S23", "SM-S918B", true), AndroidDevice("device-3", "Xiaomi 13", "2201123G", false)); _state.value = _state.value.copy(deviceInteractionState = _state.value.deviceInteractionState.copy(connectedDevices = devs, selectedDevice = devs.firstOrNull { it.isConnected })); _effect.send(OtterMcpEffect.ShowToast("Found ${devs.count { it.isConnected }} device(s)")) } }
            is DeviceInteractionIntent.SelectDevice -> { _state.value = _state.value.copy(deviceInteractionState = _state.value.deviceInteractionState.copy(selectedDevice = intent.device)) }
            is DeviceInteractionIntent.TakeScreenshot -> { viewModelScope.launch { val dev = _state.value.deviceInteractionState.selectedDevice; if (dev == null) { _effect.send(OtterMcpEffect.ShowError("No device selected")); return@launch }; _state.value = _state.value.copy(deviceInteractionState = _state.value.deviceInteractionState.copy(isLoadingScreenshot = true)); delay(1500); val ss = Screenshot(UUID.randomUUID().toString(), System.currentTimeMillis(), "/tmp/screenshot_${System.currentTimeMillis()}.png", dev.id); _state.value = _state.value.copy(deviceInteractionState = _state.value.deviceInteractionState.copy(isLoadingScreenshot = false, recentScreenshots = listOf(ss) + _state.value.deviceInteractionState.recentScreenshots.take(4))); _effect.send(OtterMcpEffect.ShowToast("Screenshot captured")) } }
            is DeviceInteractionIntent.InstallApk -> { viewModelScope.launch { val dev = _state.value.deviceInteractionState.selectedDevice; if (dev == null) { _effect.send(OtterMcpEffect.ShowError("No device selected")); return@launch }; _state.value = _state.value.copy(deviceInteractionState = _state.value.deviceInteractionState.copy(isInstalling = true)); delay(3000); _state.value = _state.value.copy(deviceInteractionState = _state.value.deviceInteractionState.copy(isInstalling = false)); _effect.send(OtterMcpEffect.ShowToast("APK installed: com.example.app")) } }
            is DeviceInteractionIntent.ReadLogcat -> { viewModelScope.launch { val dev = _state.value.deviceInteractionState.selectedDevice; if (dev == null) { _effect.send(OtterMcpEffect.ShowError("No device selected")); return@launch }; _state.value = _state.value.copy(deviceInteractionState = _state.value.deviceInteractionState.copy(isReadingLogcat = true)); delay(2000); _state.value = _state.value.copy(deviceInteractionState = _state.value.deviceInteractionState.copy(isReadingLogcat = false, logcatOutput = "--------- beginning of main\\n08-15 10:30:15.123 I/ActivityManager: Starting activity")) } }
            is DeviceInteractionIntent.SimulateInput -> { viewModelScope.launch { val dev = _state.value.deviceInteractionState.selectedDevice; if (dev == null) { _effect.send(OtterMcpEffect.ShowError("No device selected")); return@launch }; _effect.send(OtterMcpEffect.ShowToast("Input simulated: ${intent.command}")) } }
            is DeviceInteractionIntent.ClearLog -> { _state.value = _state.value.copy(deviceInteractionState = _state.value.deviceInteractionState.copy(operationLog = emptyList(), logcatOutput = "")) }
        }
    }

    private val mockTemplates = listOf(
        McpTemplate("tpl-1", "Android 设备交互 MCP", "通过 ADB 直接操控 Android 设备", TemplateCategory.DEVICE_INTERACTION, "PhoneAndroid", """{"server": "android-mcp"}""", listOf("device.screenshot", "device.install", "device.logcat", "device.input")),
        McpTemplate("tpl-2", "Gradle 构建加速 MCP", "智能缓存与并行构建优化", TemplateCategory.BUILD_SYSTEM, "Build", """{"cache": true}""", listOf("gradle.build", "gradle.clean")),
        McpTemplate("tpl-3", "Play Console API", "对接 Google Play Console API", TemplateCategory.PLAY_CONSOLE, "PlayArrow", """{"package": "com.example.app"}""", listOf("play.publish", "play.stats")),
        McpTemplate("tpl-4", "Firebase 全家桶 MCP", "Analytics、Crashlytics、Remote Config", TemplateCategory.FIREBASE, "Cloud", """{"project": "my-project"}""", listOf("firebase.analytics", "firebase.crashlytics")),
        McpTemplate("tpl-5", "Crashlytics 崩溃分析", "实时监控崩溃报告", TemplateCategory.CRASHLYTICS, "BugReport", """{"threshold": 5}""", listOf("crashlytics.list", "crashlytics.details")),
        McpTemplate("tpl-6", "Notion 任务管理", "将 Linear issues 同步到 Notion", TemplateCategory.CUSTOM, "Description", """{"database_id": "xxx"}""", listOf("notion.create_page", "notion.query"))
    )
}
