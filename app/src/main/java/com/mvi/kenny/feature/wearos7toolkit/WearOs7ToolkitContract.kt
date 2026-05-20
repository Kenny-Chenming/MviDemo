package com.mvi.kenny.feature.wearos7toolkit

import androidx.compose.ui.graphics.Color

// ============================================================
// WearOs7Colors — Design System Colors
// Wear OS 7 设计系统颜色，与 Google Wear OS 品牌对齐
// ============================================================
object WearOs7Colors {
    val Primary       = Color(0xFF4285F4)  // Google Blue
    val Secondary     = Color(0xFF34A853)  // Google Green
    val Accent        = Color(0xFFEA4335)   // Google Red
    val Warning       = Color(0xFFFBBC04)  // Google Yellow
    val Background    = Color(0xFFFFFFFF)
    val Surface       = Color(0xFFF8F9FA)
    val OnSurface     = Color(0xFF202124)
    val OnSurfaceVar  = Color(0xFF5F6368)
    val CodeBg        = Color(0xFFF8F9FA)
    val Border        = Color(0xFFE8EAED)
}

// ============================================================
// Enums — Module Types & Code Languages
// 枚举定义：模块类型 / 代码语言
// ============================================================
enum class WearModuleType(val index: Int, val labelZh: String, val labelEn: String, val priority: String) {
    A_APP_FUNCTIONS(0, "AppFunctions API 指南",    "AppFunctions API Guide",     "P0"),
    B_WORKOUT_TRACKER(1,"Wear Workout Tracker",   "Wear Workout Tracker",       "P0"),
    C_LIVE_UPDATES(2,   "Live Updates 桥接指南",   "Live Updates Bridge Guide",  "P1"),
    D_WIDGETS(3,        "双尺寸 Widget 开发",       "Dual-Size Widget Guide",     "P1"),
    E_WATCH_FACE(4,     "Watch Face Format v5",     "Watch Face Format v5",       "P1"),
    F_GEMINI_INTEL(5,   "Gemini Intelligence",     "Gemini Intelligence",        "P2"),
    G_DECISION_TREE(6,  "健身方案决策树",           "Workout Decision Tree",       "P1"),
    H_POWER_OPT(7,      "功耗优化指南",             "Power Optimization Guide",   "P2"),
    I_CANARY_EMU(8,     "Canary Emulator 上手",    "Canary Emulator Guide",      "P2"),
    J_CI_PLUGIN(9,      "AppFunctions CI 插件",    "AppFunctions CI Plugin",     "P2"),
}

enum class CodeLanguage(val label: String) { KOTLIN("Kotlin"), JAVA("Java"), XML("XML"), YAML("YAML") }
enum class AppCategory(val labelZh: String) {
    FITNESS("健身/健康"), NAVIGATION("导航"), COMMUNICATION("通讯"),
    ENTERTAINMENT("娱乐"), HEALTH("健康"), PRODUCTIVITY("生产力"), OTHER("其他")
}

// ============================================================
// Data Classes — Content Structures
// 数据类：内容结构
// ============================================================
data class ContentSection(val id: String, val title: String, val body: String)
data class CodeExample(val id: String, val title: String, val lang: CodeLanguage, val code: String)
data class ApiEntry(val param: String, val type: String, val desc: String, val required: Boolean)
data class NoteCallout(val type: String, val content: String) // type: tip|warning|info
data class BeforeAfter(val before: String, val after: String, val explanation: String)
data class ModuleData(
    val module: WearModuleType,
    val overview: String,
    val sections: List<ContentSection>,
    val codeExamples: List<CodeExample>,
    val apiTable: List<ApiEntry>,
    val notes: List<NoteCallout>,
    val beforeAfter: BeforeAfter? = null
)

// ============================================================
// State — MVI State
// 状态：MVI 状态机
// ============================================================
data class WearOs7ToolkitState(
    val currentModule:    WearModuleType              = WearModuleType.A_APP_FUNCTIONS,
    val selectedLang:     CodeLanguage               = CodeLanguage.KOTLIN,
    val expandedSections: Set<String>                = emptySet(),
    val codeCopiedId:     String?                    = null,
    val decisionCategory: AppCategory?               = null,
    val isLoading:        Boolean                    = false,
    val searchQuery:      String                     = "",
    val moduleData:       Map<WearModuleType, ModuleData> = emptyMap(),
    val errorMsg:         String?                    = null
) {
    companion object { val Initial = WearOs7ToolkitState() }
}

// ============================================================
// Intent — MVI Intent
// 意图：MVI 用户意图
// ============================================================
sealed interface WearOs7ToolkitIntent {
    data class NavigateToModule(val module: WearModuleType) : WearOs7ToolkitIntent
    data class SwitchLanguage(val lang: CodeLanguage)        : WearOs7ToolkitIntent
    data class ToggleSection(val sectionId: String)          : WearOs7ToolkitIntent
    data class CopyCode(val codeId: String)                 : WearOs7ToolkitIntent
    data class SetDecisionInput(val category: AppCategory)  : WearOs7ToolkitIntent
    data object ClearDecision                               : WearOs7ToolkitIntent
    data class UpdateSearch(val query: String)               : WearOs7ToolkitIntent
    data object ClearError                                  : WearOs7ToolkitIntent
}

// ============================================================
// Effect — MVI Side Effects
// 副作用：MVI 副作用（一次性事件）
// ============================================================
sealed interface WearOs7ToolkitEffect {
    data class ShowToast(val message: String)     : WearOs7ToolkitEffect
    data class CopyToClipboard(val code: String)  : WearOs7ToolkitEffect
}
