package com.mvi.kenny.feature.ottermcp

import androidx.compose.ui.graphics.Color

// ==================== Enums ====================
enum class SubScreen(val title: String) {
    DASHBOARD("Dashboard / 仪表盘"),
    CONNECTION_WIZARD("Connection Wizard / 连接向导"),
    TEMPLATE_GALLERY("Template Gallery / 模板库"),
    SECURITY("Security / 安全策略"),
    AUDIT_LOG("Audit Log / 审计日志"),
    DEVICE_SERVER("Device Server / 设备交互")
}

enum class ConnectionType(val label: String, val description: String) {
    EXTERNAL("连接已有 MCP Server", "Connect to an existing MCP Server"),
    ANDROID_TEMPLATE("从模板创建", "Create Android-specific MCP Server from template")
}

enum class ExportFormat { CSV, JSON }

enum class TemplateCategory(val label: String, val color: Color) {
    DEVICE_INTERACTION("设备交互", Color(0xFF2196F3)),
    BUILD_SYSTEM("构建系统", Color(0xFF9C27B0)),
    PLAY_CONSOLE("Play Console", Color(0xFF009688)),
    FIREBASE("Firebase", Color(0xFFFF9800)),
    CRASHLYTICS("Crashlytics", Color(0xFFFF5722)),
    CUSTOM("自定义", Color(0xFF607D8B))
}

enum class OperationType(val label: String, val iconName: String) {
    SCREENSHOT("截图 / Screenshot", "Screenshot"),
    INSTALL("安装应用 / Install APK", "Upload"),
    LOGCAT("读取日志 / Read Logcat", "Description"),
    INPUT("模拟输入 / Simulate Input", "Input")
}

// ==================== Data Classes ====================
data class TestResult(val success: Boolean, val message: String, val availableTools: List<String> = emptyList())
data class McpServerState(val id: String, val name: String, val url: String, val isConnected: Boolean, val lastHeartbeat: Long, val allowedTools: List<String> = emptyList())
data class McpToolCall(val id: String, val toolName: String, val serverName: String, val serverId: String, val timestamp: Long, val params: String, val fullParams: String = "", val result: String, val fullResult: String = "", val success: Boolean)
data class AuditFilters(val startTime: Long? = null, val endTime: Long? = null, val serverId: String? = null, val toolName: String? = null, val sessionId: String? = null)
data class McpTemplate(val id: String, val name: String, val description: String, val category: TemplateCategory, val iconName: String, val configJson: String, val tools: List<String>)
data class AndroidDevice(val id: String, val name: String, val model: String, val isConnected: Boolean)
data class Screenshot(val id: String, val timestamp: Long, val filePath: String, val deviceId: String)
data class DeviceOperation(val id: String, val type: OperationType, val timestamp: Long, val params: String, val resultText: String?, val success: Boolean?)
data class PermissionMatrix(val serverId: String, val serverName: String, val toolName: String, val isAllowed: Boolean)

// ==================== MVI State ====================
data class DashboardState(val servers: List<McpServerState> = emptyList(), val recentCalls: List<McpToolCall> = emptyList(), val isLoading: Boolean = false, val error: String? = null) { companion object { val Initial = DashboardState() } }
data class ConnectionWizardState(val currentStep: Int = 0, val connectionType: ConnectionType? = null, val serverUrl: String = "", val authToken: String = "", val selectedTools: Set<String> = emptySet(), val serverName: String = "", val connectionTestResult: TestResult? = null, val isTesting: Boolean = false, val isSaving: Boolean = false, val error: String? = null) { companion object { val Initial = ConnectionWizardState() } val canProceed: Boolean get() = when (currentStep) { 0 -> connectionType != null; 1 -> serverUrl.isNotBlank(); else -> true } }
data class TemplateGalleryState(val selectedTab: TemplateCategory = TemplateCategory.DEVICE_INTERACTION, val templates: List<McpTemplate> = emptyList(), val isLoading: Boolean = false, val selectedTemplate: McpTemplate? = null, val isImporting: Boolean = false, val error: String? = null) { companion object { val Initial = TemplateGalleryState() } }
data class AuditLogState(val logs: List<McpToolCall> = emptyList(), val filters: AuditFilters = AuditFilters(), val isLoading: Boolean = false, val hasMore: Boolean = true, val selectedCall: McpToolCall? = null, val isExporting: Boolean = false, val error: String? = null) { companion object { val Initial = AuditLogState() } }
data class SecurityState(val permissionMatrix: List<PermissionMatrix> = emptyList(), val servers: List<McpServerState> = emptyList(), val sensitiveOpsEnabled: Boolean = true, val isLoading: Boolean = false, val isSaving: Boolean = false, val error: String? = null) { companion object { val Initial = SecurityState() } }
data class DeviceInteractionState(val connectedDevices: List<AndroidDevice> = emptyList(), val selectedDevice: AndroidDevice? = null, val recentScreenshots: List<Screenshot> = emptyList(), val operationLog: List<DeviceOperation> = emptyList(), val isLoadingScreenshot: Boolean = false, val isInstalling: Boolean = false, val isReadingLogcat: Boolean = false, val logcatOutput: String = "", val error: String? = null) { companion object { val Initial = DeviceInteractionState() } }

data class OtterMcpState(val currentScreen: SubScreen = SubScreen.DASHBOARD, val dashboardState: DashboardState = DashboardState.Initial, val connectionWizardState: ConnectionWizardState = ConnectionWizardState.Initial, val templateGalleryState: TemplateGalleryState = TemplateGalleryState.Initial, val securityState: SecurityState = SecurityState.Initial, val auditLogState: AuditLogState = AuditLogState.Initial, val deviceInteractionState: DeviceInteractionState = DeviceInteractionState.Initial) { companion object { val Initial = OtterMcpState() } }

// ==================== MVI Intent - Top-level sealed interfaces ====================
// Screens receive these types directly / 屏幕直接接收这些类型
sealed interface DashboardIntent { data object LoadDashboard : DashboardIntent; data object RefreshConnectionStatus : DashboardIntent }
sealed interface ConnectionWizardIntent { data class SelectConnectionType(val type: ConnectionType) : ConnectionWizardIntent; data class UpdateServerUrl(val url: String) : ConnectionWizardIntent; data class UpdateAuthToken(val token: String) : ConnectionWizardIntent; data class UpdateServerName(val name: String) : ConnectionWizardIntent; data class ToggleTool(val tool: String) : ConnectionWizardIntent; data object NextStep : ConnectionWizardIntent; data object PreviousStep : ConnectionWizardIntent; data object TestConnection : ConnectionWizardIntent; data object SaveConnection : ConnectionWizardIntent; data object Reset : ConnectionWizardIntent }
sealed interface TemplateGalleryIntent { data class SelectTab(val category: TemplateCategory) : TemplateGalleryIntent; data class SelectTemplate(val template: McpTemplate) : TemplateGalleryIntent; data object DismissTemplateDetail : TemplateGalleryIntent; data class ImportTemplate(val template: McpTemplate) : TemplateGalleryIntent }
sealed interface AuditLogIntent { data class LoadLogs(val filters: AuditFilters) : AuditLogIntent; data object LoadMore : AuditLogIntent; data object Refresh : AuditLogIntent; data class SelectLog(val call: McpToolCall) : AuditLogIntent; data object DismissDetail : AuditLogIntent; data class ExportLogs(val format: ExportFormat) : AuditLogIntent; data class UpdateFilters(val filters: AuditFilters) : AuditLogIntent }
sealed interface SecurityIntent { data class ToggleToolPermission(val serverId: String, val toolName: String, val allowed: Boolean) : SecurityIntent; data class ToggleSensitiveOps(val enabled: Boolean) : SecurityIntent; data object SaveSettings : SecurityIntent; data object LoadSettings : SecurityIntent }
sealed interface DeviceInteractionIntent { data object RefreshDevices : DeviceInteractionIntent; data class SelectDevice(val device: AndroidDevice) : DeviceInteractionIntent; data object TakeScreenshot : DeviceInteractionIntent; data class InstallApk(val apkPath: String) : DeviceInteractionIntent; data class ReadLogcat(val filter: String?) : DeviceInteractionIntent; data class SimulateInput(val command: String) : DeviceInteractionIntent; data object ClearLog : DeviceInteractionIntent }

// ==================== MVI Intent - Wrapper ====================
// Unified intent for ViewModel / ViewModel 的统一意图
sealed interface OtterMcpIntent {
    data class NavigateTo(val screen: SubScreen) : OtterMcpIntent
    data object LoadDashboard : OtterMcpIntent
    data object RefreshConnections : OtterMcpIntent
    // Wrapper intents for sub-screens / 子屏幕的包装意图
    data class Dashboard(val intent: DashboardIntent) : OtterMcpIntent
    data class ConnectionWizard(val intent: ConnectionWizardIntent) : OtterMcpIntent
    data class TemplateGallery(val intent: TemplateGalleryIntent) : OtterMcpIntent
    data class Security(val intent: SecurityIntent) : OtterMcpIntent
    data class AuditLog(val intent: AuditLogIntent) : OtterMcpIntent
    data class DeviceInteraction(val intent: DeviceInteractionIntent) : OtterMcpIntent
}

// ==================== MVI Effect ====================
sealed interface OtterMcpEffect { data class ShowToast(val message: String) : OtterMcpEffect; data class ShowError(val message: String) : OtterMcpEffect }
