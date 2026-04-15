package com.mvi.kenny.feature.ottermcp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============================================================
// PRD-096 | Android Studio Otter MCP Server 生态接入工具包
// ViewModel — 业务逻辑与状态管理
// ============================================================

class OtterMcpViewModel : ViewModel() {

    private val _state = MutableStateFlow(OtterMcpState())
    val state: StateFlow<OtterMcpState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<OtterMcpEffect>()
    val effect: SharedFlow<OtterMcpEffect> = _effect.asSharedFlow()

    init { loadMockServers() }

    fun processIntent(intent: OtterMcpIntent) {
        when (intent) {
            is OtterMcpIntent.SelectTab -> selectTab(intent.index)
            is OtterMcpIntent.LoadDashboard -> loadDashboard()
            is OtterMcpIntent.RefreshConnectionStatus -> refreshConnectionStatus()
            is OtterMcpIntent.SelectConnectionType -> selectConnectionType(intent.type)
            is OtterMcpIntent.UpdateServerUrl -> updateServerUrl(intent.url)
            is OtterMcpIntent.UpdateAuthToken -> updateAuthToken(intent.token)
            is OtterMcpIntent.ToggleTool -> toggleTool(intent.tool)
            is OtterMcpIntent.TestConnection -> testConnection()
            is OtterMcpIntent.SaveConnection -> saveConnection()
            is OtterMcpIntent.NextStep -> nextStep()
            is OtterMcpIntent.PrevStep -> prevStep()
            is OtterMcpIntent.SelectTemplateTab -> selectTemplateTab(intent.category)
            is OtterMcpIntent.ImportTemplate -> importTemplate(intent.template)
            is OtterMcpIntent.LoadLogs -> loadLogs(intent.filters)
            is OtterMcpIntent.LoadMore -> loadMore()
            is OtterMcpIntent.SelectLog -> selectLog(intent.call)
            is OtterMcpIntent.ExportLogs -> exportLogs(intent.format)
            is OtterMcpIntent.RefreshDevices -> refreshDevices()
            is OtterMcpIntent.SelectDevice -> selectDevice(intent.device)
            is OtterMcpIntent.TakeScreenshot -> takeScreenshot()
            is OtterMcpIntent.InstallApk -> installApk(intent.path)
            is OtterMcpIntent.ReadLogcat -> readLogcat()
            is OtterMcpIntent.SimulateInput -> simulateInput(intent.command)
        }
    }

    private fun selectTab(index: Int) { _state.update { it.copy(selectedTab = index) } }

    private fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(dashboardState = it.dashboardState.copy(isLoading = true)) }
            delay(300)
            _state.update { it.copy(dashboardState = it.dashboardState.copy(isLoading = false)) }
        }
    }

    private fun refreshConnectionStatus() {
        viewModelScope.launch {
            _state.update {
                val updated = it.dashboardState.servers.map { s -> s.copy(isConnected = (0..10).random() > 2) }
                it.copy(dashboardState = it.dashboardState.copy(servers = updated))
            }
            _effect.emit(OtterMcpEffect.ShowToast("连接状态已刷新"))
        }
    }

    private fun selectConnectionType(type: ConnectionType) { _state.update { it.copy(wizardState = it.wizardState.copy(connectionType = type)) } }
    private fun updateServerUrl(url: String) { _state.update { it.copy(wizardState = it.wizardState.copy(serverUrl = url)) } }
    private fun updateAuthToken(token: String) { _state.update { it.copy(wizardState = it.wizardState.copy(authToken = token)) } }

    private fun toggleTool(tool: String) {
        _state.update {
            val current = it.wizardState.selectedTools
            val updated = if (tool in current) current - tool else current + tool
            it.copy(wizardState = it.wizardState.copy(selectedTools = updated))
        }
    }

    private fun testConnection() {
        viewModelScope.launch {
            _state.update { it.copy(wizardState = it.wizardState.copy(isTesting = true)) }
            delay(1000)
            val result = if (_state.value.wizardState.serverUrl.isNotEmpty()) TestResult.Success else TestResult.Error("Server URL 不能为空")
            _state.update { it.copy(wizardState = it.wizardState.copy(isTesting = false, connectionTestResult = result)) }
            _effect.emit(OtterMcpEffect.ShowTestResult(result))
        }
    }

    private fun saveConnection() {
        viewModelScope.launch {
            _state.update { it.copy(wizardState = it.wizardState.copy(isSaving = true)) }
            delay(500)
            _state.update { it.copy(wizardState = it.wizardState.copy(isSaving = false)) }
            _effect.emit(OtterMcpEffect.NavigateToDashboard)
        }
    }

    private fun nextStep() {
        _state.update {
            val step = it.wizardState.currentStep
            if (step < 2) it.copy(wizardState = it.wizardState.copy(currentStep = step + 1)) else it
        }
    }

    private fun prevStep() {
        _state.update {
            val step = it.wizardState.currentStep
            if (step > 0) it.copy(wizardState = it.wizardState.copy(currentStep = step - 1)) else it
        }
    }

    private fun selectTemplateTab(category: TemplateCategory) {
        _state.update { it.copy(templateState = it.templateState.copy(selectedTab = category, isLoading = true)) }
        viewModelScope.launch {
            delay(200)
            _state.update { it.copy(templateState = it.templateState.copy(templates = getMockTemplates(category), isLoading = false)) }
        }
    }

    private fun importTemplate(template: McpTemplate) {
        viewModelScope.launch { _effect.emit(OtterMcpEffect.ShowToast("模板「${template.name}」已导入")) }
    }

    private fun loadLogs(filters: AuditFilters) {
        viewModelScope.launch {
            _state.update { it.copy(auditState = it.auditState.copy(isLoading = true, filters = filters)) }
            delay(300)
            _state.update { it.copy(auditState = it.auditState.copy(isLoading = false, logs = getMockAuditLogs())) }
        }
    }

    private fun loadMore() {
        viewModelScope.launch {
            _state.update { it.copy(auditState = it.auditState.copy(isLoading = true)) }
            delay(300)
            _state.update { it.copy(auditState = it.auditState.copy(isLoading = false, hasMore = false)) }
        }
    }

    private fun selectLog(call: McpToolCall) { _state.update { it.copy(auditState = it.auditState.copy(selectedCall = call)) } }

    private fun exportLogs(format: ExportFormat) {
        viewModelScope.launch { _effect.emit(OtterMcpEffect.ShowToast("已导出 ${format.name} 文件")) }
    }

    private fun refreshDevices() {
        viewModelScope.launch {
            val devices = listOf(
                AndroidDevice("1", "Pixel 7", "Google Pixel 7", true),
                AndroidDevice("2", "Galaxy S24", "Samsung Galaxy S24", false),
            )
            _state.update { it.copy(deviceState = it.deviceState.copy(connectedDevices = devices)) }
        }
    }

    private fun selectDevice(device: AndroidDevice) { _state.update { it.copy(deviceState = it.deviceState.copy(selectedDevice = device)) } }

    private fun takeScreenshot() {
        viewModelScope.launch {
            _state.update { it.copy(deviceState = it.deviceState.copy(isLoadingScreenshot = true)) }
            delay(800)
            _state.update { it.copy(deviceState = it.deviceState.copy(isLoadingScreenshot = false)) }
            _effect.emit(OtterMcpEffect.ShowToast("截图已保存"))
        }
    }

    private fun installApk(path: String) { viewModelScope.launch { _effect.emit(OtterMcpEffect.ShowToast("APK 安装中: $path")) } }
    private fun readLogcat() { viewModelScope.launch { _effect.emit(OtterMcpEffect.ShowToast("Logcat 读取中...")) } }
    private fun simulateInput(command: String) { viewModelScope.launch { _effect.emit(OtterMcpEffect.ShowToast("输入模拟: $command")) } }

    private fun loadMockServers() {
        val servers = listOf(
            McpServerState("1", "Figma MCP", "https://figma.example.com/mcp", true, System.currentTimeMillis(), listOf("file.read", "file.write")),
            McpServerState("2", "Linear MCP", "https://linear.example.com/mcp", true, System.currentTimeMillis(), listOf("issue.create", "issue.update")),
            McpServerState("3", "Android Device MCP", "adb://localhost", false, System.currentTimeMillis() - 60000, listOf("device.screenshot", "device.input")),
        )
        _state.update { it.copy(dashboardState = it.dashboardState.copy(servers = servers)) }
    }

    private fun getMockTemplates(category: TemplateCategory): List<McpTemplate> = listOf(
        McpTemplate("t1", "Android 设备交互 Server", "通过 ADB 操作 Android 设备", category, "{}"),
        McpTemplate("t2", "Gradle 构建 Server", "触发构建任务、获取构建产物", category, "{}"),
    )

    private fun getMockAuditLogs(): List<McpToolCall> = listOf(
        McpToolCall("c1", "1", "Figma MCP", "file.read", System.currentTimeMillis() - 30000, "fileId=abc", "OK"),
        McpToolCall("c2", "2", "Linear MCP", "issue.create", System.currentTimeMillis() - 60000, "title=BUG", "OK"),
        McpToolCall("c3", "3", "Android Device MCP", "device.screenshot", System.currentTimeMillis() - 120000, "", "OK"),
    )
}
