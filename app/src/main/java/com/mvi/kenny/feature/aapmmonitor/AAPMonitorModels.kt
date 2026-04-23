package com.mvi.kenny.feature.aapmmonitor

/**
 * ============================================================
 * AAPMonitorModels.kt — 数据模型定义
 * ============================================================
 */

enum class ScanStatus { IDLE, SCANNING, DONE, ERROR }
enum class AapmStatus { ACTIVE, INACTIVE, UNKNOWN }
enum class Severity { P0, P1, P2 }

data class FilterOptions(
    val module: String? = null,
    val apiType: String? = null,
    val filePath: String? = null
)

data class AccessibilityIssue(
    val id: String,
    val serviceName: String,
    val filePath: String,
    val severity: Severity,
    val description: String,
    val suggestedFix: String,
    val callChain: List<String> = emptyList(),
    val isResolved: Boolean = false
)

data class ScopedApiEntry(
    val name: String,
    val description: String,
    val alternative: String,
    val applicableScenario: String
)

data class DeviceAapmInfo(
    val sdkVersion: Int,
    val isRooted: Boolean,
    val manufacturer: String,
    val model: String
)

data class ScanReport(
    val generatedAt: Long,
    val totalIssues: Int,
    val p0Count: Int,
    val p1Count: Int,
    val p2Count: Int,
    val issues: List<AccessibilityIssue>
)
