package com.mvi.kenny.feature.ottermcp

import androidx.compose.ui.graphics.Color

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

data class TestResult(val success: Boolean, val message: String, val availableTools: List<String> = emptyList())
data class McpServerState(val id: String, val name: String, val url: String, val isConnected: Boolean, val lastHeartbeat: Long, val allowedTools: List<String> = emptyList())
data class McpToolCall(val id: String, val toolName: String, val serverName: String, val serverId: String, val timestamp: Long, val params: String, val fullParams: String = "", val result: String, val fullResult: String = "", val success: Boolean)
enum class ExportFormat { CSV, JSON }
data class AuditFilters(val startTime: Long? = null, val endTime: Long? = null, val serverId: String? = null, val toolName: String? = null, val sessionId: String? = null)
enum class TemplateCategory(val label: String, val color: Color) {
    DEVICE_INTERACTION("设备交互", Color(0xFF2196F3)),
    BUILD_SYSTEM("构建系统", Color(0xFF9C27B0)),
    PLAY_CONSOLE("Play Console", Color(0xFF009688)),
    FIREBASE("Firebase", Color(0xFFFF9800)),
    CRASHLYTICS("Crashlytics", Color(0xFFFF5722)),
    CUSTOM("自定义", Color(0xFF607D8B))
}
data class McpTemplate(val id: String, val name: String, val description: String, val category: TemplateCategory, val iconName: String, val configJson: String, val tools: List<String>)
data class AndroidDevice(val id: String, val name: String, val model: String, val isConnected: Boolean)
data class Screenshot(val id: String, val timestamp: Long, val filePath: String, val deviceId: String)
enum class OperationType(val label: String, val iconName: String) {
    SCREENSHOT("截图 / Screenshot", "Screenshot"),
    INSTALL("安装应用 / Install APK", "Upload"),
    LOGCAT("读取日志 / Read Logcat", "Description"),
    INPUT("模拟输入 / Simulate Input", "Input")
}
data class DeviceOperation(val id: String, val type: OperationType, val timestamp: Long, val params: String, val resultText: String?, val success: Boolean?)
data class PermissionMatrix(val serverId: String, val serverName: String, val toolName: String, val isAllowed: Boolean)
