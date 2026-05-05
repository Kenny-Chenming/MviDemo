package com.mvi.kenny.feature.glimmertoolkit

// ================================================================
// GlimmerToolkitContract — Jetpack Compose Glimmer AI 眼镜 UI 开发工具包
// ================================================================
// MVI Contract for Jetpack Compose Glimmer AI Glasses UI Development Toolkit.
//
// PRD-230: Jetpack Compose Glimmer AI 眼镜 UI 开发工具包
//
// 10-Tab Architecture:
//   Tab 0:  MigrationScannerScreen      — Compose Material → Glimmer 迁移扫描器
//   Tab 1:  ComponentMappingGuideScreen — 组件对照指南（Compose → Glimmer API）
//   Tab 2:  AdditiveDisplayBPMScreen   — 加法显示最佳实践（5大原则）
//   Tab 3:  ProjectedTemplateScreen    — Jetpack Projected 集成模板
//   Tab 4:  FocalLengthGuideScreen   — 1米焦距设计规范
//   Tab 5:  CompatibilityCheckerScreen — 兼容性自检工具
//   Tab 6:  KMPModuleGuideScreen      — KMP 模块化架构指南
//   Tab 7:  ProjectedDecisionTreeScreen— Projected 协同决策树
//   Tab 8:  CIComplianceScreen         — CI 合规检测工具
//   Tab 9:  IO2026PreviewScreen       — Google I/O 2026 预期预览
//
// MVI 三要素 / Three pillars:
//   State  — Immutable UI state, single source of truth
//   Intent — User intentions processed by ViewModel
//   Effect — One-time side effects via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// Scan State — Tab 0: Migration Scanner
// ================================================================

/** Scan phase for progress indication */
enum class GlimmerScanPhase {
    IDLE, SCANNING_GRADLE, SCANNING_COMPOSE, ANALYZING,
    GENERATING_REPORT, COMPLETED, ERROR
}

/** Scan state for migration scanner */
data class GlimmerScanState(
    val phase: GlimmerScanPhase = GlimmerScanPhase.IDLE,
    val progress: Int = 0,
    val results: List<GlimmerScanResult> = emptyList(),
    val scannedFilesCount: Int = 0,
    val errorMessage: String? = null
) {
    val isScanning: Boolean
        get() = phase != GlimmerScanPhase.IDLE &&
                phase != GlimmerScanPhase.COMPLETED &&
                phase != GlimmerScanPhase.ERROR
}

/**
 * GlimmerScanResult — Migration scan result entry
 * 迁移扫描结果条目
 */
data class GlimmerScanResult(
    val id: String,
    val file: String,
    val line: Int,
    val category: GlimmerMigrationCategory,
    val severity: GlimmerSeverity,
    val composeApi: String,
    val glimmerApi: String,
    val description: String,
    val descriptionCn: String,
    val codeExample: String? = null,
    val glimmerExample: String? = null
)

enum class GlimmerMigrationCategory(val label: String, val labelCn: String) {
    TEXT("Text / TextField", "文本 / 文本输入框"),
    ICON("Icon / Image", "图标 / 图片"),
    CARD("Card / Surface", "卡片 / 表面"),
    LIST("List / LazyColumn", "列表 / 懒加载列表"),
    CHIP("Chip / FilterChip", "标签 / 过滤标签"),
    BUTTON("Button / IconButton", "按钮 / 图标按钮"),
    LAYOUT("Layout / Box / Column / Row", "布局 / 盒 / 列 / 行"),
    ANIMATION("Animation / AnimatedVisibility", "动画 / 动画可见性"),
    NAVIGATION("Navigation / NavHost", "导航 / 导航主机"),
    THEMING("Theming / Colors / Typography", "主题 / 颜色 / 字体")
}

enum class GlimmerSeverity(val label: String, val color: Color) {
    BLOCKER("🔴 Blocker", Color(0xFFF85149)),
    HIGH("🟠 High", Color(0xFFFFB74D)),
    MEDIUM("🟡 Medium", Color(0xFFFFD54F)),
    LOW("🟢 Low", Color(0xFF8B949E))
}

// ================================================================
// Tab 1: Component Mapping Guide
// ================================================================

/**
 * ComponentMapping — Compose → Glimmer API mapping entry
 * 组件对照条目
 */
data class ComponentMapping(
    val id: String,
    val category: String,
    val categoryCn: String,
    val composeApi: String,
    val glimmerApi: String,
    val description: String,
    val descriptionCn: String,
    val codeExample: String,
    val glimmerExample: String,
    val notes: String? = null
)

// ================================================================
// Tab 2: Additive Display Best Practices
// ================================================================

/**
 * AdditiveDisplayPrinciple — Additive display principle entry
 * 加法显示原则条目
 */
data class AdditiveDisplayPrinciple(
    val id: String,
    val title: String,
    val titleCn: String,
    val principle: String,
    val principleCn: String,
    val description: String,
    val descriptionCn: String,
    val codeExample: String,
    val codeExampleCn: String,
    val icon: String
)

// ================================================================
// Tab 3: Projected Template
// ================================================================

/**
 * ProjectedStep — Projected integration step entry
 * Projected 集成步骤条目
 */
data class ProjectedStep(
    val id: String,
    val stepNumber: Int,
    val title: String,
    val titleCn: String,
    val description: String,
    val descriptionCn: String,
    val codeSnippet: String,
    val codeSnippetCn: String,
    val isCompleted: Boolean = false
)

// ================================================================
// Tab 4: Focal Length Guide
// ================================================================

/**
 * FocalLengthSpec — Focal length design specification entry
 * 焦距设计规范条目
 */
data class FocalLengthSpec(
    val id: String,
    val elementType: String,
    val elementTypeCn: String,
    val minSize: String,
    val recommendedSize: String,
    val lineHeight: String,
    val spacing: String,
    val example: String,
    val notes: String
)

// ================================================================
// Tab 5: Compatibility Checker
// ================================================================

enum class CompatibilityRiskLevel(
    val label: String,
    val labelCn: String,
    val color: Color
) {
    CRITICAL("P0 Critical", "P0 严重", Color(0xFFF85149)),
    HIGH("P1 High", "P1 高风险", Color(0xFFFFB74D)),
    MEDIUM("P2 Medium", "P2 中风险", Color(0xFFFFD54F)),
    LOW("P3 Low", "P3 低风险", Color(0xFF8B949E)),
    PASS("Pass", "通过", Color(0xFF3FB950))
}

/**
 * CompatibilityIssue — Compatibility issue entry
 * 兼容性问题条目
 */
data class CompatibilityIssue(
    val id: String,
    val title: String,
    val titleCn: String,
    val description: String,
    val descriptionCn: String,
    val riskLevel: CompatibilityRiskLevel,
    val affectedVersions: String,
    val suggestion: String,
    val suggestionCn: String
)

// ================================================================
// Tab 6: KMP Module Guide
// ================================================================

/**
 * KMPModuleGuide — KMP module architecture guide entry
 * KMP 模块化架构指南条目
 */
data class KMPModuleGuide(
    val id: String,
    val moduleName: String,
    val moduleNameCn: String,
    val moduleType: KMPModuleType,
    val description: String,
    val descriptionCn: String,
    val expectCode: String,
    val androidActual: String,
    val wearActual: String? = null,
    val glimmerActual: String? = null
)

enum class KMPModuleType(val label: String, val labelCn: String) {
    SHARED("Shared Module", "共享模块"),
    WEAR_SPECIFIC("Wear-specific Module", "手表特定模块"),
    GLIMMER_SPECIFIC("Glimmer-specific Module", "眼镜特定模块"),
    PLATFORM_SPECIFIC("Platform-specific Module", "平台特定模块")
}

// ================================================================
// Tab 7: Projected Decision Tree
// ================================================================

/**
 * DecisionNode — Decision tree node entry
 * 决策树节点条目
 */
data class DecisionNode(
    val id: String,
    val question: String,
    val questionCn: String,
    val options: List<DecisionOption>,
    val recommendation: String,
    val recommendationCn: String,
    val useCase: String,
    val useCaseCn: String
)

data class DecisionOption(
    val label: String,
    val labelCn: String,
    val nextNodeId: String?
)

// ================================================================
// Tab 8: CI Compliance
// ================================================================

/**
 * CIComplianceRule — CI compliance rule entry
 * CI 合规规则条目
 */
data class CIComplianceRule(
    val id: String,
    val ruleId: String,
    val title: String,
    val titleCn: String,
    val description: String,
    val descriptionCn: String,
    val checkCommand: String,
    val fixCommand: String,
    val severity: CompatibilityRiskLevel,
    val isEnabled: Boolean = true
)

// ================================================================
// Tab 9: I/O 2026 Preview
// ================================================================

/**
 * IO2026Session — I/O 2026 expected session entry
 * I/O 2026 预期会话条目
 */
data class IO2026Session(
    val id: String,
    val title: String,
    val titleCn: String,
    val category: String,
    val categoryCn: String,
    val expectedDate: String,
    val description: String,
    val descriptionCn: String,
    val likelihood: IOLikelihood,
    val relatedPRD: String? = null
)

enum class IOLikelihood(val label: String, val labelCn: String) {
    CONFIRMED("✅ Confirmed", "已确认"),
    HIGHLY_LIKELY("🔥 Highly Likely", "极有可能"),
    LIKELY("📍 Likely", "可能"),
    SPECULATIVE("💭 Speculative", "推测")
}

// ================================================================
// Top-level MVI State
// ================================================================

/**
 * GlimmerToolkitState — Root MVI state for the entire toolkit
 * 主状态 — 整个工具包的状态
 */
data class GlimmerToolkitState(
    // ── Tab Navigation ──
    val selectedTab: Int = 0,
    // ── Tab 0: Migration Scanner ──
    val projectPath: String = "",
    val scanState: GlimmerScanState = GlimmerScanState(),
    val expandedScanResult: String? = null,
    // ── Tab 1: Component Mapping ──
    val componentMappings: List<ComponentMapping> = emptyList(),
    val selectedMappingCategory: String? = null,
    val expandedMapping: String? = null,
    // ── Tab 2: Additive Display ──
    val additivePrinciples: List<AdditiveDisplayPrinciple> = emptyList(),
    val expandedPrinciple: String? = null,
    // ── Tab 3: Projected Template ──
    val projectedSteps: List<ProjectedStep> = emptyList(),
    val expandedStep: String? = null,
    // ── Tab 4: Focal Length ──
    val focalLengthSpecs: List<FocalLengthSpec> = emptyList(),
    val expandedFocalSpec: String? = null,
    // ── Tab 5: Compatibility ──
    val compatibilityIssues: List<CompatibilityIssue> = emptyList(),
    val expandedIssue: String? = null,
    val selectedRiskFilter: CompatibilityRiskLevel? = null,
    // ── Tab 6: KMP Module ──
    val kmpModuleGuides: List<KMPModuleGuide> = emptyList(),
    val expandedKMPGuide: String? = null,
    // ── Tab 7: Decision Tree ──
    val decisionNodes: List<DecisionNode> = emptyList(),
    val currentNodeId: String? = null,
    // ── Tab 8: CI Compliance ──
    val ciComplianceRules: List<CIComplianceRule> = emptyList(),
    val expandedCIRule: String? = null,
    // ── Tab 9: I/O Preview ──
    val io2026Sessions: List<IO2026Session> = emptyList(),
    val expandedIOSession: String? = null,
    val io2026Countdown: Long = 0L
)

// ================================================================
// MVI Intent
// ================================================================

/**
 * GlimmerToolkitIntent — All user intentions
 * 所有用户意图
 */
sealed class GlimmerToolkitIntent {
    // ── Tab Navigation ──
    data class SelectTab(val index: Int) : GlimmerToolkitIntent()
    // ── Tab 0: Migration Scanner ──
    data class UpdateProjectPath(val path: String) : GlimmerToolkitIntent()
    data object StartScan : GlimmerToolkitIntent()
    data object ClearScanResults : GlimmerToolkitIntent()
    data class ExpandScanResult(val id: String?) : GlimmerToolkitIntent()
    // ── Tab 1: Component Mapping ──
    data class SelectMappingCategory(val category: String?) : GlimmerToolkitIntent()
    data class ExpandMapping(val id: String?) : GlimmerToolkitIntent()
    // ── Tab 2: Additive Display ──
    data class ExpandPrinciple(val id: String?) : GlimmerToolkitIntent()
    // ── Tab 3: Projected Template ──
    data class ToggleProjectedStep(val id: String) : GlimmerToolkitIntent()
    data class ExpandStep(val id: String?) : GlimmerToolkitIntent()
    // ── Tab 4: Focal Length ──
    data class ExpandFocalSpec(val id: String?) : GlimmerToolkitIntent()
    // ── Tab 5: Compatibility ──
    data class FilterByRisk(val risk: CompatibilityRiskLevel?) : GlimmerToolkitIntent()
    data class ExpandIssue(val id: String?) : GlimmerToolkitIntent()
    // ── Tab 6: KMP Module ──
    data class ExpandKMPGuide(val id: String?) : GlimmerToolkitIntent()
    // ── Tab 7: Decision Tree ──
    data class SelectDecisionNode(val nodeId: String) : GlimmerToolkitIntent()
    // ── Tab 8: CI Compliance ──
    data class ExpandCIRule(val id: String?) : GlimmerToolkitIntent()
    // ── Tab 9: I/O Preview ──
    data class ExpandIOSession(val id: String?) : GlimmerToolkitIntent()
}

// ================================================================
// MVI Effect
// ================================================================

/**
 * GlimmerToolkitEffect — One-time side effects
 * 一次性副作用
 */
sealed class GlimmerToolkitEffect {
    data class ShowToast(val message: String) : GlimmerToolkitEffect()
    data class ScanCompleted(val total: Int, val durationMs: Long) : GlimmerToolkitEffect()
    data class CopyToClipboard(val text: String) : GlimmerToolkitEffect()
    data class Error(val message: String) : GlimmerToolkitEffect()
}
