package com.mvi.kenny.feature.antigravity2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * ============================================================
 * Antigravity2ViewModel — Google Antigravity 2.0 状态管理
 * ============================================================
 * PRD-293 | Google Antigravity 2.0 Android 集成开发工具包
 *
 * @see Antigravity2Contract
 * @see Antigravity2State
 * @see Antigravity2Intent
 * @see Antigravity2Effect
 */
class Antigravity2ViewModel : ViewModel() {

    private val _state = MutableStateFlow(Antigravity2State.Initial)
    val state: StateFlow<Antigravity2State> = _state.asStateFlow()

    private val _effect = Channel<Antigravity2Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        _state.value = _state.value.copy(
            dashboardModules = getDashboardModules(),
            newsItems = getNewsItems(),
            androidBundles = getAndroidBundles(),
            skills = getSkills(),
            filteredSkills = getSkills(),
            enterpriseChecklist = getEnterpriseChecklist(),
            countdownDays = calculateCountdown()
        )
    }

    /**
     * 处理用户意图
     */
    fun sendIntent(intent: Antigravity2Intent) {
        when (intent) {
            is Antigravity2Intent.SelectTab -> selectTab(intent.index)
            is Antigravity2Intent.SearchSkills -> searchSkills(intent.query)
            is Antigravity2Intent.ToggleChecklistItem -> toggleChecklistItem(intent.itemId)
            is Antigravity2Intent.NextMigrationStep -> nextMigrationStep()
            is Antigravity2Intent.PreviousMigrationStep -> previousMigrationStep()
        }
    }

    private fun selectTab(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    /**
     * 搜索 Skills（带 debounce 效果）
     * @param query 搜索关键词
     */
    private fun searchSkills(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        val filtered = if (query.isBlank()) {
            _state.value.skills
        } else {
            _state.value.skills.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
        _state.value = _state.value.copy(filteredSkills = filtered)
    }

    /**
     * 切换 Checklist 项
     * @param itemId 项 ID
     */
    private fun toggleChecklistItem(itemId: String) {
        val updated = _state.value.enterpriseChecklist.map { item ->
            if (item.id == itemId) item.copy(checked = !item.checked) else item
        }
        _state.value = _state.value.copy(enterpriseChecklist = updated)
        val checked = updated.count { it.checked }
        val total = updated.size
        viewModelScope.launch {
            _effect.send(Antigravity2Effect.ShowToast("已勾选 $checked / $total 项"))
        }
    }

    private fun nextMigrationStep() {
        val current = _state.value.migrationStep
        if (current < 4) {
            _state.value = _state.value.copy(migrationStep = current + 1)
        }
    }

    private fun previousMigrationStep() {
        val current = _state.value.migrationStep
        if (current > 0) {
            _state.value = _state.value.copy(migrationStep = current - 1)
        }
    }

    private fun calculateCountdown(): Int {
        // Gemini CLI 停用日期：2026-06-18
        val today = LocalDate.of(2026, 6, 20)
        val deadline = LocalDate.of(2026, 6, 18)
        return ChronoUnit.DAYS.between(today, deadline).toInt().coerceAtLeast(0)
    }

    private fun getDashboardModules(): List<AntigravityModule> = listOf(
        AntigravityModule(
            id = "mod_desktop",
            title = "Desktop App",
            description = "独立桌面应用，多 Agent 并行编排，后台任务调度",
            icon = "🖥️"
        ),
        AntigravityModule(
            id = "mod_cli",
            title = "Antigravity CLI",
            description = "Go 重写 CLI，替代 Gemini CLI（6月18日停用）",
            icon = "⌨️"
        ),
        AntigravityModule(
            id = "mod_sdk",
            title = "SDK (Preview)",
            description = "自定义 Agent 开发 SDK，agent harness 构建专属 Agent",
            icon = "🔧"
        ),
        AntigravityModule(
            id = "mod_managed",
            title = "Managed Agents",
            description = "Gemini API 内置，基于 agent harness，AI Studio 可用",
            icon = "🚀"
        )
    )

    private fun getNewsItems(): List<NewsItem> = listOf(
        NewsItem(
            id = "news_1",
            title = "⚠️ Gemini CLI 6月18日停用，请立即迁移到 Antigravity CLI",
            date = "2026-06-18",
            isUrgent = true
        ),
        NewsItem(
            id = "news_2",
            title = "Antigravity 2.0 正式发布：Desktop + CLI + SDK + Managed Agents",
            date = "2026-06-01",
            isUrgent = false
        ),
        NewsItem(
            id = "news_3",
            title = "ADK 2.0 + Antigravity Agents CLI 深度集成",
            date = "2026-06-05",
            isUrgent = false
        ),
        NewsItem(
            id = "news_4",
            title = "Android Resources Bundle 正式支持 Android 开发场景",
            date = "2026-06-10",
            isUrgent = false
        ),
        NewsItem(
            id = "news_5",
            title = "Google I/O 2026：93 并行 Subagents 12小时构建完整 OS",
            date = "2026-05-21",
            isUrgent = false
        )
    )

    private fun getAndroidBundles(): List<AndroidResourceBundle> = listOf(
        AndroidResourceBundle(
            id = "bundle_cli",
            name = "Android CLI Skills",
            description = "Android 开发专用的 Antigravity CLI Skills",
            category = BundleCategory.CLI
        ),
        AndroidResourceBundle(
            id = "bundle_skills",
            name = "SKILL.md 格式",
            description = "Antigravity Skills 标准格式，动态加载专业知识",
            category = BundleCategory.SKILL
        ),
        AndroidResourceBundle(
            id = "bundle_sdk",
            name = "Antigravity SDK",
            description = "自定义 Agent 开发 SDK（Preview）",
            category = BundleCategory.SDK
        ),
        AndroidResourceBundle(
            id = "bundle_managed",
            name = "Managed Agents",
            description = "Gemini API 内置 Managed Agents",
            category = BundleCategory.MANAGED_AGENT
        )
    )

    private fun getSkills(): List<AntigravitySkill> = listOf(
        AntigravitySkill("sk_android_1", "Android Skill Kit", "Android 开发全套 Skills，涵盖 Compose/KMP/NDK", SkillCategory.ANDROID, true),
        AntigravitySkill("sk_android_2", "Gradle Skill", "Gradle 构建优化，AGP 迁移，依赖管理", SkillCategory.ANDROID, false),
        AntigravitySkill("sk_android_3", "Compose Skill", "Jetpack Compose 最佳实践，迁移指南", SkillCategory.ANDROID, true),
        AntigravitySkill("sk_prod_1", "Git Workflow Skill", "Git Flow + PR 审核流程自动化", SkillCategory.PRODUCTIVITY, false),
        AntigravitySkill("sk_prod_2", "CI/CD Skill", "GitHub Actions + Android CI/CD 全流程", SkillCategory.PRODUCTIVITY, true),
        AntigravitySkill("sk_ai_1", "Gemini API Skill", "Gemini API 调用 + Prompt 工程", SkillCategory.AI, true),
        AntigravitySkill("sk_ai_2", "ADK Agent Skill", "ADK Agent 构建与编排", SkillCategory.AI, false),
        AntigravitySkill("sk_deploy_1", "GCP Deploy Skill", "Google Cloud 部署，Cloud Run 配置", SkillCategory.DEPLOYMENT, false),
        AntigravitySkill("sk_deploy_2", "Firebase Skill", "Firebase 部署与监控集成", SkillCategory.DEPLOYMENT, false)
    )

    private fun getEnterpriseChecklist(): List<ChecklistItem> = listOf(
        ChecklistItem("cl_1", "申请 Google Cloud 项目", "确保有 GCP 项目并启用结算", false),
        ChecklistItem("cl_2", "申请 Gemini Enterprise Agent Platform 访问", "通过 Google Cloud Console 申请", false),
        ChecklistItem("cl_3", "配置 Agent 权限与角色", "IAM 角色配置，最小权限原则", false),
        ChecklistItem("cl_4", "配置 VPC 和网络安全", "私有网络，IP 白名单，安全策略", false),
        ChecklistItem("cl_5", "配置数据合规保留策略", "数据保留期限，GDPR/CCPA 合规", false),
        ChecklistItem("cl_6", "测试部署流程", "在 staging 环境完整测试", false),
        ChecklistItem("cl_7", "配置监控与告警", "Cloud Monitoring + Alerting", false),
        ChecklistItem("cl_8", "配置备份与灾难恢复", "多区域备份，恢复演练", false)
    )
}
