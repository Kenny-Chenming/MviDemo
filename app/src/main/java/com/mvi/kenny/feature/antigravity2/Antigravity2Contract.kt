package com.mvi.kenny.feature.antigravity2

/**
 * ============================================================
 * Antigravity2Contract — Google Antigravity 2.0 MVI 契约
 * ============================================================
 * PRD-293 | Google Antigravity 2.0 Android 集成开发工具包
 * Design: 仪表盘型 App，4 Tab 结构，深色主题
 *
 * MVI 三要素：State / Intent / Effect
 *
 * @see Antigravity2ViewModel
 * @see Antigravity2Screen
 */

/** 仪表盘模块数据模型 */
data class AntigravityModule(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val status: ModuleStatus = ModuleStatus.AVAILABLE
)

enum class ModuleStatus { AVAILABLE, COMING_SOON, DEPRECATED }

/** Android Resources Bundle 模块 */
data class AndroidResourceBundle(
    val id: String,
    val name: String,
    val description: String,
    val category: BundleCategory
)

enum class BundleCategory { CLI, SKILL, SDK, MANAGED_AGENT }

/** Antigravity Skill 数据模型 */
data class AntigravitySkill(
    val id: String,
    val name: String,
    val description: String,
    val category: SkillCategory,
    val isPopular: Boolean = false
)

enum class SkillCategory { ANDROID, PRODUCTIVITY, AI, DEPLOYMENT }

/** 企业部署 Checklist 项 */
data class ChecklistItem(
    val id: String,
    val title: String,
    val description: String,
    val checked: Boolean = false
)

/** 最新动态条目 */
data class NewsItem(
    val id: String,
    val title: String,
    val date: String,
    val isUrgent: Boolean = false
)

/**
 * 页面状态（State）
 * —————————————————————————————————————————————————————
 * @param selectedTab 当前选中的 Tab (0-3)
 * @param dashboardModules 仪表盘四大模块
 * @param newsItems 最新动态列表
 * @param androidBundles Android Resources Bundle 列表
 * @param skills Skills 市场列表
 * @param filteredSkills 筛选后的 Skills
 * @param searchQuery Skills 搜索关键词
 * @param enterpriseChecklist 企业部署 Checklist
 * @param migrationStep 迁移助手当前步骤 (0-based)
 * @param countdownDays Gemini CLI 停用倒计时天数
 */
data class Antigravity2State(
    val selectedTab: Int = 0,
    val dashboardModules: List<AntigravityModule> = emptyList(),
    val newsItems: List<NewsItem> = emptyList(),
    val androidBundles: List<AndroidResourceBundle> = emptyList(),
    val skills: List<AntigravitySkill> = emptyList(),
    val filteredSkills: List<AntigravitySkill> = emptyList(),
    val searchQuery: String = "",
    val enterpriseChecklist: List<ChecklistItem> = emptyList(),
    val migrationStep: Int = 0,
    val countdownDays: Int = 0
) {
    companion object {
        val Initial = Antigravity2State()
    }
}

/**
 * 用户意图（Intent）
 * —————————————————————————————————————————————————————
 */
sealed interface Antigravity2Intent {
    data class SelectTab(val index: Int) : Antigravity2Intent
    data class SearchSkills(val query: String) : Antigravity2Intent
    data class ToggleChecklistItem(val itemId: String) : Antigravity2Intent
    data object NextMigrationStep : Antigravity2Intent
    data object PreviousMigrationStep : Antigravity2Intent
}

/**
 * 副作用（Effect）
 * —————————————————————————————————————————————————————
 */
sealed interface Antigravity2Effect {
    data class ShowToast(val message: String) : Antigravity2Effect
    data class NavigateToSkillsDetail(val skillId: String) : Antigravity2Effect
}
