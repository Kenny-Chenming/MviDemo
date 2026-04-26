package com.mvi.kenny.feature.androidxraiglasses

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ============================================================
 * Android XR Contract — MVI 架构定义
 * ============================================================
 * PRD-168 / Android XR AI Glasses Development Toolkit
 * Defines State, Intent, Effect, and companion data classes
 */

// =============================================================
// XRColors — XR 主题配色 / XR Theme Colors
// =============================================================
object XRColors {
    val Primary = Color(0xFF8B5CF6)      // XR Purple / XR 紫色
    val Secondary = Color(0xFF06B6D4)     // Cyan Blue / 青蓝色
    val Accent = Color(0xFFF59E0B)        // Amber / 琥珀色
    val Surface = Color(0xFF0F172A)       // Deep Space Background / 深空背景
    val OnSurface = Color(0xFFE2E8F0)     // Text on Surface / 表面文字
    val CardGradientStart = Color(0xFF1E293B) // Card gradient start / 卡片渐变起始
    val CardGradientEnd = Color(0xFF0F172A)   // Card gradient end / 卡片渐变结束
}

// =============================================================
// XRTab — Tab 枚举 / Tab Navigation Enum
// =============================================================
enum class XRTab(val title: String) {
    OVERVIEW("首页概览"),
    GLIMMER_GUIDE("Glimmer迁移"),
    GLIMMER_TEMPLATES("UI模板"),
    PROJECTED_LIBRARY("Projected"),
    ARCORE_GUIDE("ARCore"),
    UX_RULES("UX规范"),
    DUAL_DEVICE("双端协同"),
    EMULATOR("模拟器"),
    CI_PIPELINE("CI流水线"),
    APPENDIX("附录")
}

// =============================================================
// AppType — 应用类型 / Application Type
// =============================================================
enum class AppType(val label: String) {
    EXISTING("现有App扩展"),
    NEW_GLASSES("全新眼镜App"),
    NEW_PHONE("手机端新App")
}

// =============================================================
// XRModule — XR 开发模块 / XR Development Module
// =============================================================
data class XRModule(
    val id: String,
    val name: String,
    val description: String,
    val isRecommended: Boolean = false
)

// =============================================================
// GlassesType — 眼镜设备类型 / Glasses Device Type
// =============================================================
enum class GlassesType(
    val label: String,
    val screenWidth: Int,
    val screenHeight: Int
) {
    GENERIC("通用眼镜", 360, 360),
    GALAXY_XR("Galaxy XR", 360, 360),
    META_QUEST("Meta Quest", 640, 360),
    SNAP_DRAGON("Snapdragon XR", 400, 400),
    Apple_VISION("Apple Vision", 384, 384)
}

// =============================================================
// TemplateCategory — 模板分类 / Template Category
// =============================================================
enum class TemplateCategory(val label: String) {
    NOTIFICATION_CARD("通知卡片"),
    AR_OVERLAY("AR叠加"),
    VOICE_ASSISTANT("语音助手"),
    NAVIGATION("导航指引"),
    SETTINGS_PANEL("设置面板"),
    INFO_CARD("信息卡片")
}

// =============================================================
// GlimmerTemplate — Glimmer UI 模板 / Glimmer UI Template
// =============================================================
data class GlimmerTemplate(
    val id: String,
    val name: String,
    val category: TemplateCategory,
    val description: String,
    val usageCode: String,
    val fullCode: String,
    val tags: List<String>
)

// =============================================================
// GlimmerGuide — Glimmer 迁移指南 / Glimmer Migration Guide
// =============================================================
data class GlimmerGuide(
    val comparisonTable: List<GlimmerComparisonItem>,
    val decisionTree: List<DecisionTreeNode>,
    val componentList: List<GlimmerComponent>,
    val migrationSteps: List<MigrationStep>,
    val codeDiff: String
)

data class GlimmerComparisonItem(
    val feature: String,
    val standardCompose: String,
    val glimmer: String,
    val difference: String
)

data class DecisionTreeNode(
    val question: String,
    val yesBranch: String,
    val noBranch: String,
    val recommendation: String = ""
)

data class GlimmerComponent(
    val name: String,
    val description: String,
    val version: String,
    val isExperimental: Boolean = false
)

data class MigrationStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val codeExample: String = ""
)

// =============================================================
// ProjectedGuide — Projected Library 使用指南
// =============================================================
data class ProjectedGuide(
    val conceptExplanation: String,
    val apiSteps: List<ProjectedApiStep>,
    val scenarioTemplates: List<ProjectedScenario>,
    val permissionConfig: List<PermissionItem>
)

data class ProjectedApiStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val codeSnippet: String = ""
)

data class ProjectedScenario(
    val name: String,
    val description: String,
    val flow: String
)

data class PermissionItem(
    val permission: String,
    val description: String,
    val isRequired: Boolean
)

// =============================================================
// ARCoreGuide — ARCore for AI Glasses 开发指南
// =============================================================
data class ARCoreGuide(
    val differencesFromStandard: List<String>,
    val glimmerArCapabilities: List<String>,
    val developmentSteps: List<ARCoreStep>,
    val sampleCode: String
)

data class ARCoreStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val warning: String = ""
)

// =============================================================
// UXRules — 眼镜 App UX 设计规范
// =============================================================
data class UXRules(
    val principles: List<UXPrinciple>,
    val doList: List<String>,
    val dontList: List<String>,
    val typographySpec: TypographySpec? = null
)

data class UXPrinciple(
    val icon: String,
    val title: String,
    val description: String
)

data class TypographySpec(
    val fontFamily: String,
    val headingSize: String,
    val bodySize: String,
    val captionSize: String
)

// =============================================================
// DualDeviceTemplate — 手机-眼镜协同架构模板
// =============================================================
data class DualDeviceTemplate(
    val architectureDiagram: String,
    val dataSyncStrategy: String,
    val codeTemplates: List<CodeTemplateItem>,
    val testingMethods: List<String>
)

data class CodeTemplateItem(
    val title: String,
    val description: String,
    val code: String
)

// =============================================================
// EmulatorGuide — Android XR Emulator 使用指南
// =============================================================
data class EmulatorGuide(
    val downloadUrl: String = "",
    val setupSteps: List<EmulatorStep> = emptyList(),
    val fovSetting: String = "",
    val resolutionSetting: String = "",
    val dpiSetting: String = "",
    val debuggingMethods: List<String> = emptyList(),
    val faq: List<FaqItem> = emptyList()
)

data class EmulatorStep(
    val stepNumber: Int,
    val title: String,
    val description: String
)

data class FaqItem(
    val question: String,
    val answer: String
)

// =============================================================
// CITemplate — CI 构建流水线
// =============================================================
data class CITemplate(
    val githubActionsTemplate: String = "",
    val gitlabCiTemplate: String = "",
    val deviceMatrix: List<DeviceConfig> = emptyList(),
    val prBotConfig: String = ""
)

data class DeviceConfig(
    val deviceName: String,
    val apiLevel: Int,
    val abi: String,
    val isXrDevice: Boolean = false
)

// =============================================================
// ComponentCheatSheet — Glimmer 组件速查表
// =============================================================
data class ComponentCheatSheet(
    val components: List<GlimmerComponent>,
    val composeMapping: List<ComposeMappingItem>
)

data class ComposeMappingItem(
    val standardCompose: String,
    val glimmerEquivalent: String,
    val note: String = ""
)

// =============================================================
// AndroidXRState — MVI State
// =============================================================
data class AndroidXRState(
    val appType: AppType = AppType.EXISTING,
    val recommendedPath: List<XRModule> = emptyList(),
    val daysUntilIO: Int = 23,
    val currentTab: XRTab = XRTab.OVERVIEW,
    val glimmerGuide: GlimmerGuide? = null,
    val selectedTemplate: GlimmerTemplate? = null,
    val projectedGuide: ProjectedGuide? = null,
    val arcoreGuide: ARCoreGuide? = null,
    val uxRules: UXRules? = null,
    val dualDeviceTemplate: DualDeviceTemplate? = null,
    val emulatorGuide: EmulatorGuide? = null,
    val ciTemplate: CITemplate? = null,
    val selectedGlassesType: GlassesType = GlassesType.GENERIC,
    val errorMessage: String? = null
)

// =============================================================
// AndroidXRIntent — MVI Intent
// =============================================================
sealed class AndroidXRIntent {
    data class SwitchTab(val tab: XRTab) : AndroidXRIntent()
    data class SetAppType(val appType: AppType) : AndroidXRIntent()
    data class SelectGlassesType(val glassesType: GlassesType) : AndroidXRIntent()
    data object LoadGlimmerGuide : AndroidXRIntent()
    data object LoadProjectedGuide : AndroidXRIntent()
    data object LoadARCoreGuide : AndroidXRIntent()
    data object LoadUXRules : AndroidXRIntent()
    data object LoadDualDeviceTemplate : AndroidXRIntent()
    data object LoadEmulatorGuide : AndroidXRIntent()
    data object LoadCITemplate : AndroidXRIntent()
    data class CopyCode(val code: String) : AndroidXRIntent()
    data class SelectTemplate(val template: GlimmerTemplate) : AndroidXRIntent()
}

// =============================================================
// AndroidXREffect — MVI Effect (Side Effects)
// =============================================================
sealed class AndroidXREffect {
    data class ShowToast(val message: String) : AndroidXREffect()
    data class ShowError(val message: String) : AndroidXREffect()
    data object ScrollToTop : AndroidXREffect()
}
