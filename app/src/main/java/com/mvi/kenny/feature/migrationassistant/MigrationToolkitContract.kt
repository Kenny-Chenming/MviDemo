package com.mvi.kenny.feature.migrationassistant

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * MigrationToolkitContract — Android Studio Migration Assistant 移植工具包 MVI 契约
 * MigrationToolkit MVI Contract — Android Studio Migration Assistant Dev Toolkit
 * ============================================================
 *
 * PRD-268 | Android Studio iOS/React Native/Web Migration Assistant 移植工具包
 * Ref: memory/agency/designs/PRD-268-Android-Studio-iOS-RN-Web-Migration-Assistant移植工具包.md
 *
 * MVI Architecture:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions; ViewModel executes logic on receiving Intent
 * - Effect: One-time side effects (Toast, Navigation) delivered via Channel
 * —————————————————————————————————————————————————————
 *
 * Page Structure (3 Platform Tabs + Sub-sections):
 * - Tab 1: iOS → Android (10 sub-modules)
 * - Tab 2: React Native → Android
 * - Tab 3: Web → Android
 * - Plus: CI Integration / Play Store Compliance / Limitations
 */

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * Source platform enumeration for migration
 * 迁移源平台枚举
 *
 * @param displayName Platform display name / 平台显示名称
 * @param iconName Material icon name / 图标名称
 * @param color Platform accent color / 平台主题色
 */
enum class Platform(
    val displayName: String,
    val iconName: String,
    val color: Long
) {
    IOS("iOS", "apple", 0xFFA3A3A3),
    REACT_NATIVE("React Native", "react", 0xFF61DAFB),
    WEB("Web", "web", 0xFFF7DF1E);

    val colorValue: Color get() = Color(color)
}

/**
 * iOS sub-section enumeration
 * iOS 子模块枚举
 *
 * @param title Sub-section title / 子模块标题
 * @param description Sub-section description / 子模块描述
 */
enum class IosSection(
    val title: String,
    val description: String
) {
    GETTING_STARTED("上手指南", "6步完成 iOS → Android 迁移"),
    API_MAPPING("API映射表", "UIKit → Jetpack Compose 完整映射"),
    PITFALLS("踩坑案例", "真实迁移踩坑案例与解决方案"),
    QUALITY_VERIFICATION("质量验证", "功能一致性测试与性能基准")
}

/**
 * React Native sub-section enumeration
 * React Native 子模块枚举
 */
enum class ReactNativeSection(
    val title: String,
    val description: String
) {
    COMPONENT_MAPPING("组件映射", "RN 组件 → Android Compose 映射"),
    STATE_MIGRATION("状态迁移", "Redux/MobX → ViewModel 迁移指南"),
    LIBRARY_RECOMMENDATION("等价库推荐", "Android 原生库替代方案")
}

/**
 * Web sub-section enumeration
 * Web 子模块枚举
 */
enum class WebSection(
    val title: String,
    val description: String
) {
    PWA_CONVERSION("PWA转换", "渐进式 Web App 转换清单"),
    RESPONSIVE_ADAPTATION("响应式适配", "响应式 Web 适配指南"),
    NATIVE_ENHANCEMENT("原生增强", "Camera/Location/Push 增强方案")
}

/**
 * API mapping data model
 * API 映射数据模型
 *
 * @param id Unique identifier / 唯一标识
 * @param sourceApi Source platform API / 源平台 API
 * @param targetApi Target platform API / 目标平台 API
 * @param sourceLanguage Source language (Swift/Objective-C/RN JS/Web) / 源语言
 * @param notes Additional notes / 补充说明
 * @param isAutomated Whether mapping is automated by Migration Assistant / 是否为自动映射
 */
data class ApiMapping(
    val id: String,
    val sourceApi: String,
    val targetApi: String,
    val sourceLanguage: String,
    val notes: String,
    val isAutomated: Boolean
)

/**
 * Pitfall case data model
 * 踩坑案例数据模型
 *
 * @param id Case unique identifier / 案例唯一标识
 * @param title Case title / 案例标题
 * @param problem Problem description / 问题描述
 * @param solution Solution / 解决方案
 * @param codeExample Optional code example / 代码示例
 * @param isExpanded Expansion state / 展开状态
 */
data class PitfallCase(
    val id: String,
    val title: String,
    val problem: String,
    val solution: String,
    val codeExample: String? = null,
    val isExpanded: Boolean = false
)

/**
 * CI integration template data model
 * CI 集成模板数据模型
 *
 * @param id Template unique identifier / 模板唯一标识
 * @param name Template name / 模板名称
 * @param description Template description / 模板描述
 * @param workflowYaml GitHub Actions YAML content / YAML 内容
 */
data class CITemplate(
    val id: String,
    val name: String,
    val description: String,
    val workflowYaml: String
)

/**
 * Quality checklist item
 * 质量检查清单项
 *
 * @param id Item unique identifier / 清单项唯一标识
 * @param title Check item title / 检查项标题
 * @param description Check item description / 检查项描述
 * @param isChecked Whether item is checked / 是否已勾选
 * @param category Category / 分类
 */
data class QualityCheckItem(
    val id: String,
    val title: String,
    val description: String,
    val isChecked: Boolean = false,
    val category: String
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * Migration Toolkit page state
 * 移植工具包页面状态
 *
 * MVI Architecture: Model layer, holds all page state.
 * State is Immutable — each state change creates a new State object.
 *
 * @param activePlatform Currently selected source platform / 当前选中的源平台
 * @param activeIosSection Currently selected iOS sub-section / 当前选中的 iOS 子模块
 * @param activeReactNativeSection Currently selected RN sub-section / 当前选中的 RN 子模块
 * @param activeWebSection Currently selected Web sub-section / 当前选中的 Web 子模块
 * @param searchQuery API mapping search query / API 映射搜索关键词
 * @param expandedCaseId Currently expanded pitfall case ID / 当前展开的踩坑案例 ID
 * @param expandedMappingId Currently expanded API mapping ID / 当前展开的 API 映射 ID
 * @param isLoading Loading state / 加载状态
 * @param selectedApiMapping Currently selected API mapping for detail view / 选中的 API 映射详情
 * @param apiMappings All API mappings list / 所有 API 映射列表
 * @param pitfallCases All pitfall cases list / 所有踩坑案例列表
 * @param ciTemplates CI integration templates / CI 集成模板
 * @param qualityChecklist Quality verification checklist / 质量验证清单
 * @param activeCommonSection Currently selected common section (CI/PlayStore/Limitations) / 通用章节
 *
 * @see MigrationToolkitIntent
 * @see MigrationToolkitViewModel
 */
data class MigrationToolkitState(
    // Platform tab selection / 平台 Tab 选择
    val activePlatform: Platform = Platform.IOS,
    // Sub-section selection per platform / 各平台子模块选择
    val activeIosSection: IosSection = IosSection.GETTING_STARTED,
    val activeReactNativeSection: ReactNativeSection = ReactNativeSection.COMPONENT_MAPPING,
    val activeWebSection: WebSection = WebSection.PWA_CONVERSION,
    // Search & filter / 搜索过滤
    val searchQuery: String = "",
    // Expansion state / 展开状态
    val expandedCaseId: String? = null,
    val expandedMappingId: String? = null,
    // Loading / 加载状态
    val isLoading: Boolean = false,
    // Selected item detail / 选中项详情
    val selectedApiMapping: ApiMapping? = null,
    // Data lists / 数据列表
    val apiMappings: List<ApiMapping> = emptyList(),
    val pitfallCases: List<PitfallCase> = emptyList(),
    val ciTemplates: List<CITemplate> = emptyList(),
    val qualityChecklist: List<QualityCheckItem> = emptyList(),
    // Common section selection / 通用章节选择
    val activeCommonSection: CommonSection = CommonSection.CI_INTEGRATION,
    // Filtered results based on search / 基于搜索的过滤结果
    val filteredApiMappings: List<ApiMapping> = emptyList(),
    // Decision tree expansion / 决策树展开状态
    val decisionTreeExpandedNodes: Set<String> = emptySet()
) {
    companion object {
        /** Initial / default state / 初始状态 */
        val Initial = MigrationToolkitState()
    }

    /**
     * Get API mappings filtered by search query
     * 获取过滤后的 API 映射列表
     */
    fun getFilteredMappings(): List<ApiMapping> {
        if (searchQuery.isBlank()) return apiMappings
        val query = searchQuery.lowercase()
        return apiMappings.filter {
            it.sourceApi.lowercase().contains(query) ||
            it.targetApi.lowercase().contains(query) ||
            it.sourceLanguage.lowercase().contains(query)
        }
    }
}

/**
 * Common section enumeration for CI/PlayStore/Limitations
 * 通用章节枚举
 */
enum class CommonSection(val title: String, val description: String) {
    CI_INTEGRATION("CI集成", "GitHub Actions 自动化流水线"),
    PLAY_STORE_COMPLIANCE("Play Store合规", "隐私政策/权限/API级别"),
    LIMITATIONS("局限性与替代方案", "官方工具 vs 第三方工具决策矩阵")
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * User intentions for Migration Toolkit
 * 移植工具包用户意图
 *
 * Every user interaction on the page corresponds to an Intent.
 * ViewModel receives Intent, executes business logic, then updates State.
 *
 * @see MigrationToolkitViewModel.sendIntent
 */
sealed interface MigrationToolkitIntent {

    /**
     * Select platform tab / 选择平台 Tab
     *
     * @param platform Target platform / 目标平台
     */
    data class SelectPlatform(val platform: Platform) : MigrationToolkitIntent

    /**
     * Select iOS sub-section / 选择 iOS 子模块
     *
     * @param section Target section / 目标子模块
     */
    data class SelectIosSection(val section: IosSection) : MigrationToolkitIntent

    /**
     * Select React Native sub-section / 选择 React Native 子模块
     *
     * @param section Target section / 目标子模块
     */
    data class SelectReactNativeSection(val section: ReactNativeSection) : MigrationToolkitIntent

    /**
     * Select Web sub-section / 选择 Web 子模块
     *
     * @param section Target section / 目标子模块
     */
    data class SelectWebSection(val section: WebSection) : MigrationToolkitIntent

    /**
     * Search API mappings / 搜索 API 映射
     *
     * @param query Search query / 搜索关键词
     */
    data class SearchMappings(val query: String) : MigrationToolkitIntent

    /**
     * Toggle pitfall case expansion / 切换踩坑案例展开状态
     *
     * @param caseId Case unique identifier / 案例唯一标识
     */
    data class ToggleCaseExpanded(val caseId: String) : MigrationToolkitIntent

    /**
     * Toggle API mapping detail expansion / 切换 API 映射详情展开
     *
     * @param mappingId Mapping unique identifier / 映射唯一标识
     */
    data class ToggleMappingExpanded(val mappingId: String) : MigrationToolkitIntent

    /**
     * Toggle quality check item / 切换质量检查项勾选状态
     *
     * @param itemId Item unique identifier / 清单项唯一标识
     */
    data class ToggleQualityCheck(val itemId: String) : MigrationToolkitIntent

    /**
     * Select API mapping for detail view / 选中 API 映射查看详情
     *
     * @param mapping Selected mapping or null to clear / 选中的映射或 null 清除
     */
    data class SelectApiMapping(val mapping: ApiMapping?) : MigrationToolkitIntent

    /**
     * Copy API mapping as CSV / 复制 API 映射为 CSV 格式
     *
     * @param mapping Mapping to copy / 要复制的映射
     */
    data class CopyMappingCsv(val mapping: ApiMapping) : MigrationToolkitIntent

    /**
     * Copy all visible API mappings as CSV / 复制所有可见映射为 CSV
     */
    data object CopyAllMappingsCsv : MigrationToolkitIntent

    /**
     * Toggle decision tree node expansion / 切换决策树节点展开状态
     *
     * @param nodeId Node unique identifier / 节点唯一标识
     */
    data class ToggleDecisionNode(val nodeId: String) : MigrationToolkitIntent

    /**
     * Load initial data / 加载初始数据
     */
    data object LoadData : MigrationToolkitIntent

    /**
     * Select common section / 选择通用章节
     *
     * @param section Target section / 目标章节
     */
    data class SelectCommonSection(val section: CommonSection) : MigrationToolkitIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * One-time side effects for Migration Toolkit
 * 移植工具包副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect {}
 *
 * @see MigrationToolkitViewModel
 */
sealed interface MigrationToolkitEffect {

    /**
     * Show toast message / 显示 Toast
     *
     * @param message Toast message text / Toast 文本
     */
    data class ShowToast(val message: String) : MigrationToolkitEffect

    /**
     * Copy text to system clipboard / 复制文本到剪贴板
     *
     * @param text Text to copy / 要复制的文本
     * @param label Clipboard label / 剪贴板标签
     */
    data class CopyToClipboard(val text: String, val label: String) : MigrationToolkitEffect

    /**
     * Scroll to specific pitfall case / 滚动到指定踩坑案例
     *
     * @param caseId Case unique identifier / 案例唯一标识
     */
    data class ScrollToCase(val caseId: String) : MigrationToolkitEffect

    /**
     * Show quality report generation success / 显示质量报告生成成功
     *
     * @param itemCount Number of items checked / 已检查项数量
     */
    data class ShowQualityReportReady(val itemCount: Int) : MigrationToolkitEffect
}
