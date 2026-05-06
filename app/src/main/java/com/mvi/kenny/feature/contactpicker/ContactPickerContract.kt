package com.mvi.kenny.feature.contactpicker

// ================================================================
// ContactPickerContract — Google Play Contact Picker 强制迁移工具包 MVI 契约
// ================================================================
// MVI Architecture Contract for Google Play Contact Picker Migration Toolkit.
//
// PRD-234: Google Play Contact Picker 强制迁移工具包
// Design Reference: memory/agency/designs/PRD-234-Google-Play-Contact-Picker-强制迁移工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
//
// 合规截止日期 / Deadline: 2026-10-27 (Google Play Console review enforcement)
// ================================================================

import androidx.compose.ui.graphics.Color

// =============================================================
// Color palette — 深色 Terminal 风格配色
// Contact Picker 主题：Indigo（隐私保护）
// =============================================================
object ContactPickerColors {
    val Primary = Color(0xFF4F46E5)        // Indigo - 隐私保护
    val Accent = Color(0xFF818CF8)         // Light Indigo
    val Background = Color(0xFF0D1117)     // 深黑
    val Surface = Color(0xFF161B22)         // 卡片背景
    val SurfaceVariant = Color(0xFF21262D) // 输入框
    val OnSurface = Color(0xFFE6EDF3)       // 主文字
    val OnSurfaceVariant = Color(0xFF8B949E) // 次级文字
    // 紧迫性评级 / Urgency Level
    val UrgencyP0 = Color(0xFFEF4444)        // 红色 - 必须迁移
    val UrgencyP1 = Color(0xFFF59E0B)       // 橙色 - 建议迁移
    val UrgencyP2 = Color(0xFF10B981)       // 绿色 - 暂不处理
    // 代码块 / Code block
    val CodeBackground = Color(0xFF1E1E1E)
    val CodeKeyword = Color(0xFF569CD6)
    val CodeString = Color(0xFFCE9178)
}

// =============================================================
// Deadline — 合规截止日期常量
// =============================================================
object ContactPickerDeadline {
    const val DEADLINE = "2026-10-27"
    fun daysUntil(deadline: String): Long {
        val formatter = java.time.LocalDate.parse(deadline)
        val today = java.time.LocalDate.now()
        return java.time.temporal.ChronoUnit.DAYS.between(today, formatter)
    }
}

// =============================================================
// ContactPickerTab — 功能 Tab 枚举（5 个主要 Tab）
// =============================================================
enum class ContactPickerTab(val title: String, val emoji: String) {
    SCANNER("合规扫描器", "📡"),
    API_GUIDE("API 指南", "📖"),
    DECISION_TREE("决策树", "🌳"),
    DECLARATION("Declaration", "📝"),
    LOCATION_BUTTON("Location Button", "📍")
}

// =============================================================
// UrgencyLevel — 迁移紧迫性评级
// =============================================================
enum class UrgencyLevel {
    MUST_MIGRATE,   // P0 - 必须迁移
    CAN_EXEMPT,     // P1 - 可申请豁免
    NO_ACTION       // P2 - 无需处理
}

// =============================================================
// ScanResult — 扫描结果
// =============================================================
data class ScanResult(
    val urgencyLevel: UrgencyLevel,
    val manifestFindings: List<ManifestFinding> = emptyList(),
    val codeFindings: List<CodeFinding> = emptyList(),
    val migrationDeadline: String = ContactPickerDeadline.DEADLINE,
    val migrationSteps: List<MigrationStep> = emptyList()
)

data class ManifestFinding(
    val permission: String,
    val lineNumber: Int,
    val context: String = ""
)

data class CodeFinding(
    val fileName: String,
    val lineNumber: Int,
    val codeSnippet: String,
    val useCase: String
)

data class MigrationStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val codeDiff: CodeDiff? = null
)

data class CodeDiff(
    val before: String,
    val after: String,
    val language: String = "kotlin"
)

// =============================================================
// DeclarationForm — Play Developer Declaration 申请表单
// =============================================================
data class DeclarationForm(
    val appName: String = "",
    val packageName: String = "",
    val readContactsUseCase: String = "",
    val whyCannotUseContactPicker: String = "",
    val alternativeMeasures: String = "",
    val savedToPrefs: Boolean = false
)

// =============================================================
// ContactPickerState — 主状态（UiState）
// =============================================================
data class ContactPickerState(
    val selectedTab: Int = 0,
    val scanResult: ScanResult? = null,
    val isScanning: Boolean = false,
    val decisionTreeStep: Int = 0,
    val decisionTreeAnswers: Map<Int, String> = emptyMap(),
    val declarationForm: DeclarationForm = DeclarationForm(),
    val apiGuideSelectedCategory: String = "basics",
    val daysRemaining: Long = ContactPickerDeadline.daysUntil(ContactPickerDeadline.DEADLINE)
)

// =============================================================
// ContactPickerIntent — 用户意图（Intent）
// =============================================================
sealed class ContactPickerIntent {
    data class SelectTab(val index: Int) : ContactPickerIntent()
    data object StartScan : ContactPickerIntent()
    data class AnswerDecisionNode(val nodeId: Int, val answer: String) : ContactPickerIntent()
    data object ResetDecisionTree : ContactPickerIntent()
    data class UpdateDeclarationField(val field: String, val value: String) : ContactPickerIntent()
    data object SaveDeclaration : ContactPickerIntent()
    data class CopyCodeBlock(val code: String) : ContactPickerIntent()
    data class SelectApiCategory(val category: String) : ContactPickerIntent()
}

// =============================================================
// ContactPickerEffect — 副作用（Effect）
// =============================================================
sealed class ContactPickerEffect {
    data class ShowSnackbar(val message: String) : ContactPickerEffect()
    data class CopyToClipboard(val code: String) : ContactPickerEffect()
    data object NavigateToApiGuide : ContactPickerEffect()
}
