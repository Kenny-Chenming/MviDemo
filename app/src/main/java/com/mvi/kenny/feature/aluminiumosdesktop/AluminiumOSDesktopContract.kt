package com.mvi.kenny.feature.aluminiumosdesktop

// ================================================================
// AluminiumOSDesktopContract — Aluminium OS Android App 桌面适配工具包
// ================================================================
// MVI Contract for Aluminium OS desktop adaptation toolkit.
//
// PRD-227: Aluminium OS Android App 桌面适配工具包
// Design Reference: memory/agency/designs/PRD-227-Aluminium-OS-桌面适配工具包.md
//
// 5-Tab Architecture:
//   Tab 0: 桌面兼容性自检 (Compatibility Scanner)
//   Tab 1: 桌面 UI 改造清单 (UI Modification Checklist)
//   Tab 2: 键鼠适配指南 (Keyboard/Mouse Adaptation Guide)
//   Tab 3: Gemini AI 集成 (Gemini AI Integration)
//   Tab 4: Play Store 优化 (Play Store Optimization)
//
// MVI 三要素 / Three pillars:
//   State  — Immutable UI state, single source of truth
//   Intent — User intentions processed by ViewModel
//   Effect — One-time side effects via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// Risk Level — Risk rating for compatibility issues
// ================================================================
// 兼容性问题的风险等级
enum class RiskLevel(
    val label: String,
    val labelCn: String,
    val color: Color,
    val priority: Int
) {
    P0("P0 Critical", "P0 严重", Color(0xFFF85149), 1),   // 阻断性问题
    P1("P1 High", "P1 高风险", Color(0xFFFFB74D), 2),      // 高风险
    P2("P2 Medium", "P2 中风险", Color(0xFFFFD54F), 3),    // 中风险
    P3("P3 Low", "P3 低风险", Color(0xFF8B949E), 4),       // 低风险/建议改进
    PASS("Pass", "通过", Color(0xFF3FB950), 5)             // 无问题
}

// ================================================================
// Tab 0: Compatibility Scanner — Models
// ================================================================

/**
 * CompatibilityIssue — Desktop compatibility issue found by scanner
 * 桌面兼容性问题（扫描器检测结果）
 */
data class CompatibilityIssue(
    val id: String,
    val file: String,
    val line: Int,
    val issueType: IssueType,
    val riskLevel: RiskLevel,
    val description: String,
    val descriptionCn: String,
    val suggestion: String,
    val suggestionCn: String,
    val beforeCode: String? = null,
    val afterCode: String? = null
)

/** Issue type enumeration for classification */
enum class IssueType(val displayName: String, val displayNameCn: String) {
    TOUCH_DEPENDENCY("Touch-only dependency", "触摸依赖"),
    FIXED_ORIENTATION("Fixed screen orientation", "固定屏幕方向"),
    NO_KEYBOARD_NAV("Missing keyboard navigation", "缺少键盘导航"),
    RESIZE_DISABLED("Window resize disabled", "窗口调整大小被禁用"),
    NO_DESKTOP_WINDOWING("No Desktop Windowing support", "不支持桌面窗口化"),
    RIGHT_CLICK_MISSING("Missing right-click context menu", "缺少右键菜单"),
    DRAG_DROP_MISSING("Missing drag-and-drop support", "缺少拖放支持"),
    ASPECT_RATIO_FIXED("Fixed aspect ratio assumption", "固定宽高比假设"),
    NO_SHORTCUT("No keyboard shortcuts", "缺少快捷键"),
    PORTRAIT_ONLY("Portrait-only layout", "仅竖屏布局")
}

/** Scan phase for progress indication */
enum class ScanPhase {
    IDLE,
    SCANNING_MANIFEST,
    SCANNING_KOTLIN,
    SCANNING_JAVA,
    ANALYZING,
    GENERATING_REPORT,
    COMPLETED,
    ERROR
}

/** Scan state — Tab 0 state */
data class CompatibilityScanState(
    val phase: ScanPhase = ScanPhase.IDLE,
    val progress: Int = 0,
    val issues: List<CompatibilityIssue> = emptyList(),
    val scannedFilesCount: Int = 0,
    val errorMessage: String? = null,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val selectedIssue: CompatibilityIssue? = null
) {
    val isScanning: Boolean
        get() = phase != ScanPhase.IDLE &&
                phase != ScanPhase.COMPLETED &&
                phase != ScanPhase.ERROR

    val durationMs: Long
        get() = if (endTime > 0 && startTime > 0) endTime - startTime else 0L

    val issuesByRisk: Map<RiskLevel, List<CompatibilityIssue>>
        get() = issues.groupBy { it.riskLevel }

    val p0Count: Int get() = issues.count { it.riskLevel == RiskLevel.P0 }
    val p1Count: Int get() = issues.count { it.riskLevel == RiskLevel.P1 }
    val p2Count: Int get() = issues.count { it.riskLevel == RiskLevel.P2 }
    val p3Count: Int get() = issues.count { it.riskLevel == RiskLevel.P3 }
    val passCount: Int get() = issues.count { it.riskLevel == RiskLevel.PASS }
}

// ================================================================
// Tab 1: UI Modification Checklist — Models
// ================================================================

/**
 * UIModificationItem — A single UI modification checklist item
 * UI 改造清单条目
 */
data class UIModificationItem(
    val id: String,
    val category: UIModCategory,
    val title: String,
    val titleCn: String,
    val description: String,
    val descriptionCn: String,
    val codeExample: String? = null,
    val codeExampleCn: String? = null,
    val priority: RiskLevel,
    val isCompleted: Boolean = false
)

enum class UIModCategory(val label: String, val labelCn: String) {
    WINDOW_MANAGEMENT("Window Management", "窗口管理"),
    LAYOUT_ADJUSTMENT("Layout Adjustment", "布局调整"),
    DESKTOP_WINDOWING("Desktop Windowing", "桌面窗口化"),
    INTERACTION_MODE("Interaction Mode", "交互模式"),
    ORIENTATION_SUPPORT("Orientation Support", "方向支持"),
    MULTI_WINDOW("Multi-Window", "多窗口支持")
}

// ================================================================
// Tab 2: Keyboard/Mouse Adaptation — Models
// ================================================================

/**
 * KeyboardMouseGuide — Keyboard and mouse adaptation guide entry
 * 键鼠适配指南条目
 */
data class KeyboardMouseGuide(
    val id: String,
    val title: String,
    val titleCn: String,
    val description: String,
    val descriptionCn: String,
    val category: KeyboardCategory,
    val codeExample: String,
    val codeExampleCn: String,
    val beforeCode: String? = null,
    val afterCode: String? = null,
    val framework: String  // "Compose" or "View"
)

enum class KeyboardCategory(val label: String, val labelCn: String) {
    KEYBOARD_NAV("Keyboard Navigation", "键盘导航"),
    SHORTCUT_KEYS("Shortcut Keys", "快捷键"),
    RIGHT_CLICK_MENU("Right-Click Context Menu", "右键上下文菜单"),
    DRAG_DROP("Drag and Drop", "拖放"),
    FOCUS_MANAGEMENT("Focus Management", "焦点管理")
}

// ================================================================
// Tab 3: Gemini AI Integration — Models
// ================================================================

/**
 * GeminiIntegrationGuide — Gemini AI integration guide entry
 * Gemini AI 集成指南条目
 */
data class GeminiIntegrationGuide(
    val id: String,
    val title: String,
    val titleCn: String,
    val description: String,
    val descriptionCn: String,
    val codeExample: String,
    val codeExampleCn: String,
    val integrationType: GeminiIntegrationType,
    val framework: String
)

enum class GeminiIntegrationType(val label: String, val labelCn: String) {
    APP_FUNCTIONS("AppFunctions API", "AppFunctions API"),
    AICORE("AICore / Device AI", "AICore / 设备 AI"),
    GEMINI_SDK("Gemini SDK (Cloud)", "Gemini SDK (云端)")
}

// ================================================================
// Tab 4: Play Store Optimization — Models
// ================================================================

/**
 * PlayStoreGuide — Play Store desktop optimization guide entry
 * Play Store 桌面优化指南条目
 */
data class PlayStoreGuide(
    val id: String,
    val title: String,
    val titleCn: String,
    val description: String,
    val descriptionCn: String,
    val steps: List<String>,
    val stepsCn: List<String>,
    val consolePath: String,
    val isCompleted: Boolean = false
)

// ================================================================
// Top-level MVI State
// ================================================================

/**
 * AluminiumOSDesktopState — Root MVI state for the entire toolkit
 * 主状态 — 整个工具包的状态
 */
data class AluminiumOSDesktopState(
    // ── Tab Navigation ──
    // Tab 索引 (0-4)
    val selectedTab: Int = 0,

    // ── Tab 0: Compatibility Scanner ──
    val scanState: CompatibilityScanState = CompatibilityScanState(),
    val projectSourceDir: String = "",  // 项目源码目录输入
    val packageName: String = "",        // 包名输入

    // ── Tab 1: UI Modification Checklist ──
    val checklistItems: List<UIModificationItem> = emptyList(),
    val expandedChecklistItem: String? = null,

    // ── Tab 2: Keyboard/Mouse Adaptation ──
    val keyboardGuides: List<KeyboardMouseGuide> = emptyList(),
    val expandedKeyboardGuide: String? = null,
    val selectedFramework: String = "Compose",  // "Compose" or "View"

    // ── Tab 3: Gemini AI Integration ──
    val geminiGuides: List<GeminiIntegrationGuide> = emptyList(),
    val expandedGeminiGuide: String? = null,

    // ── Tab 4: Play Store Optimization ──
    val playStoreGuides: List<PlayStoreGuide> = emptyList(),
    val expandedPlayStoreGuide: String? = null
)

// ================================================================
// MVI Intent
// ================================================================

/**
 * AluminiumOSDesktopIntent — All user intentions
 * 所有用户意图
 */
sealed class AluminiumOSDesktopIntent {

    // ── Tab Navigation ──
    data class SelectTab(val index: Int) : AluminiumOSDesktopIntent()

    // ── Tab 0: Compatibility Scanner ──
    data class UpdateSourceDir(val dir: String) : AluminiumOSDesktopIntent()
    data class UpdatePackageName(val packageName: String) : AluminiumOSDesktopIntent()
    data object StartScan : AluminiumOSDesktopIntent()
    data object CancelScan : AluminiumOSDesktopIntent()
    data object ClearScanResults : AluminiumOSDesktopIntent()
    data class SelectIssue(val issue: CompatibilityIssue?) : AluminiumOSDesktopIntent()

    // ── Tab 1: UI Modification Checklist ──
    data class ToggleChecklistItem(val id: String) : AluminiumOSDesktopIntent()
    data class ExpandChecklistItem(val id: String?) : AluminiumOSDesktopIntent()

    // ── Tab 2: Keyboard/Mouse Adaptation ──
    data class SelectFramework(val framework: String) : AluminiumOSDesktopIntent()
    data class ExpandKeyboardGuide(val id: String?) : AluminiumOSDesktopIntent()

    // ── Tab 3: Gemini AI Integration ──
    data class ExpandGeminiGuide(val id: String?) : AluminiumOSDesktopIntent()

    // ── Tab 4: Play Store Optimization ──
    data class TogglePlayStoreGuide(val id: String) : AluminiumOSDesktopIntent()
    data class ExpandPlayStoreGuide(val id: String?) : AluminiumOSDesktopIntent()
}

// ================================================================
// MVI Effect
// ================================================================

/**
 * AluminiumOSDesktopEffect — One-time side effects
 * 一次性副作用
 */
sealed class AluminiumOSDesktopEffect {
    data class ShowToast(val message: String) : AluminiumOSDesktopEffect()
    data class ScanCompleted(
        val total: Int,
        val p0: Int, val p1: Int, val p2: Int, val p3: Int,
        val durationMs: Long
    ) : AluminiumOSDesktopEffect()
    data class TerminalOutput(
        val message: String,
        val riskLevel: RiskLevel = RiskLevel.PASS
    ) : AluminiumOSDesktopEffect()
    data class CopyToClipboard(val text: String) : AluminiumOSDesktopEffect()
    data class Error(val message: String) : AluminiumOSDesktopEffect()
    data class OpenUrl(val url: String) : AluminiumOSDesktopEffect()
}
