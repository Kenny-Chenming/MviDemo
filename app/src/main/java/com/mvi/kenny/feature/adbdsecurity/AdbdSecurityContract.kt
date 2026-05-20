package com.mvi.kenny.feature.adbdsecurity

// ================================================================
// AdbdSecurityContract — Android adbd CVE-2026-0073 Security Toolkit MVI Contract
// ================================================================
// MVI architecture contract for Android adbd CVE-2026-0073 Vulnerability Detection & Security Toolkit.
//
// PRD-264: Android adbd CVE-2026-0073 无线ADB漏洞检测与安全加固工具包
// Design Reference: memory/agency/designs/PRD-264-Android-adbd-CVE-2026-0073漏洞检测与安全加固工具包.md
//
// CVE-2026-0073 is a zero-click RCE vulnerability in Android adbd (wireless ADB).
// Attackers on the same network can gain shell access without user interaction.
// Affected: Android 14/15/16/16-qpr2
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
//
// This toolkit provides 4 developer tools:
//   1. Scan Tool (首页)    — One-click vulnerability scan
//   2. CI Integration (CI集成) — GitHub Actions / Jenkins plugin
//   3. Security Guide (安全指南) — Wireless ADB best practices
//   4. About (关于)        — CVE details & references
// ================================================================

import androidx.compose.ui.graphics.Color

// =============================================================
// Color Palette — 安全工具仪表盘风格（Material Design 3 Security）
// =============================================================
object AdbdSecurityColors {
    val Primary = Color(0xFF1E88E5)           // 安全蓝 — 主色
    val CriticalBg = Color(0xFFFFEBEE)         // 浅红背景
    val CriticalBorder = Color(0xFFC62828)      // 深红边框/文字
    val CriticalText = Color(0xFFC62828)
    val PatchedBg = Color(0xFFE8F5E9)          // 浅绿背景
    val PatchedBorder = Color(0xFF2E7D32)       // 深绿边框/文字
    val PatchedText = Color(0xFF2E7D32)
    val VulnerableBg = Color(0xFFFFF3E0)       // 浅橙背景
    val VulnerableBorder = Color(0xFFE65100)    // 深橙边框/文字
    val VulnerableText = Color(0xFFE65100)
    val Surface = Color(0xFFFFFFFF)
    val Background = Color(0xFFF5F5F5)
    val OnSurface = Color(0xFF1C1B1F)
    val OnSurfaceVariant = Color(0xFF49454F)
    val CodeBlockBg = Color(0xFF263238)        // 深色代码块背景
    val CodeBlockText = Color(0xFFB0BEC5)      // 代码文字色
}

// =============================================================
// BottomTab — 底部导航Tab
// =============================================================
enum class BottomTab(val displayName: String, val emoji: String, val description: String) {
    HOME("首页", "🔍", "Scan for CVE-2026-0073 vulnerability"),
    CI("CI集成", "⚙️", "CI/CD integration tools"),
    GUIDE("安全指南", "🛡️", "Wireless ADB security best practices"),
    ABOUT("关于", "📋", "CVE details and references")
}

// =============================================================
// ScanStatus — 扫描状态
// =============================================================
enum class ScanStatus { IDLE, SCANNING, SUCCESS, FAILED }

// =============================================================
// VulnerabilityStatus — 漏洞状态
// =============================================================
enum class VulnerabilityStatus(val displayName: String, val chineseName: String) {
    UNKNOWN("Unknown", "未知"),
    VULNERABLE("Vulnerable", "存在漏洞"),
    PATCHED("Patched", "已修补"),
    UNSUPPORTED("Unsupported Device", "不支持的设备")
}

// =============================================================
// SeverityLevel — 严重性等级
// =============================================================
enum class SeverityLevel(val displayName: String, val emoji: String, val priority: Int) {
    CRITICAL("Critical", "🔴", 1),
    HIGH("High", "🟠", 2),
    MEDIUM("Medium", "🟡", 3),
    LOW("Low", "🟢", 4),
    NONE("None", "✅", 5)
}

// =============================================================
// DeviceInfo — 设备信息
// =============================================================
data class DeviceInfo(
    val deviceName: String = "Unknown",
    val androidVersion: String = "Unknown",
    val adbVersion: String = "Unknown",
    val sdkVersion: String = "Unknown",
    val buildFingerprint: String = "Unknown"
)

// =============================================================
// ScanResult — 扫描结果
// =============================================================
data class ScanResult(
    val deviceInfo: DeviceInfo,
    val vulnerabilityStatus: VulnerabilityStatus,
    val severityLevel: SeverityLevel,
    val cveDescription: String,
    val attackVector: String,
    val affectedVersions: String,
    val exploitStatus: String,
    val patchMethod: String,
    val workaround: String,
    val scanTimestamp: Long = System.currentTimeMillis()
)

// =============================================================
// PatchQuery — 补丁状态查询
// =============================================================
data class PatchQuery(
    val deviceModel: String = "",
    val androidVersion: String = "",
    val adbVersion: String = ""
)

// =============================================================
// PatchQueryResult — 补丁查询结果
// =============================================================
data class PatchQueryResult(
    val isAffected: Boolean,
    val affectedReason: String,
    val recommendedAction: String
)

// =============================================================
// =============================================================
// MVI State — 页面状态（不可变）
// =============================================================
data class AdbdSecurityState(
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val vulnerabilityStatus: VulnerabilityStatus = VulnerabilityStatus.UNKNOWN,
    val severityLevel: SeverityLevel = SeverityLevel.NONE,
    val scanProgress: Float = 0f,
    val errorMessage: String? = null,
    val currentTab: BottomTab = BottomTab.HOME,
    // Scan animation
    val scanPhase: String = "Ready",
    // CI page
    val ciOutputFormat: CIOutputFormat = CIOutputFormat.JSON,
    val showGitHubActions: Boolean = true,
    val showJenkins: Boolean = false,
    // Guide page
    val expandedSecurityConfig: Int? = null,
    // Patch query
    val patchQuery: PatchQuery = PatchQuery(),
    val patchQueryResult: PatchQueryResult? = null,
    val isQueryingPatch: Boolean = false,
    // Latest scan result (for display)
    val latestScanResult: ScanResult? = null
)

enum class CIOutputFormat { JSON, HTML, SPFX }

// =============================================================
// MVI Intent — 用户操作
// =============================================================
sealed class AdbdSecurityIntent {
    object StartScan : AdbdSecurityIntent()
    object RetryScan : AdbdSecurityIntent()
    object ExportJson : AdbdSecurityIntent()
    object CopyReport : AdbdSecurityIntent()
    data class SelectTab(val tab: BottomTab) : AdbdSecurityIntent()
    data class QueryPatchStatus(
        val deviceModel: String,
        val androidVersion: String,
        val adbVersion: String
    ) : AdbdSecurityIntent()
    data class SetCIOutputFormat(val format: CIOutputFormat) : AdbdSecurityIntent()
    data class SetExpandedSecurityConfig(val index: Int?) : AdbdSecurityIntent()
}

// =============================================================
// MVI Effect — 一次性副作用（通过 Channel）
// =============================================================
sealed class AdbdSecurityEffect {
    object ScanCompleted : AdbdSecurityEffect()
    object ReportCopied : AdbdSecurityEffect()
    object JsonExported : AdbdSecurityEffect()
    data class ShowError(val message: String) : AdbdSecurityEffect()
    data class ShowToast(val message: String) : AdbdSecurityEffect()
}
