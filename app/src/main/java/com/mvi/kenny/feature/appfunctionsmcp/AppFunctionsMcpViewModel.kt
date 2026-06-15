package com.mvi.kenny.feature.appfunctionsmcp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * AppFunctionsMcpViewModel — PRD-274 AppFunctions On-Device MCP 开发工具包
 * AppFunctions On-Device MCP Developer Toolkit ViewModel
 * ============================================================
 *
 * Design Doc: memory/agency/designs/PRD-274-Android-AppFunctions-On-Device-MCP开发工具包.md
 *
 * MVI Pattern:
 * - State: UI state, single source of truth (via StateFlow)
 * - Intent: User intentions received via sendIntent()
 * - Effect: One-time side effects sent via _effect Channel
 *
 * Development Notes:
 * - Android 16+ required (API 36) for AppFunctions
 * - Private Preview: Gemini integration requires special access申请
 * - @AppFunction 注解的 suspend 函数才会被 OS Registry 发现
 * - 复杂对象必须使用 Kotlinx Serialization + @Serializable
 * - 敏感操作必须走系统确认弹窗，不可绕过
 */

// ============================================================
// ViewModel Implementation / ViewModel 实现
// ============================================================

/**
 * AppFunctions On-Device MCP 开发工具包 ViewModel
 *
 * 处理所有用户意图（Intent），执行业务逻辑，生成新状态（State）。
 * 通过 StateFlow 暴露状态，通过 Channel 发送一次性副作用（Effect）。
 *
 * @see AppFunctionsMcpState 页面状态
 * @see AppFunctionsMcpIntent 用户意图
 * @see AppFunctionsMcpEffect 副作用
 */
class AppFunctionsMcpViewModel : ViewModel() {

    // ——————————————————————————————————————————————————————
    // State — UI 状态（唯一真相来源）
    // ——————————————————————————————————————————————————————

    private val _state = MutableStateFlow(AppFunctionsMcpState.Initial)
    val state: StateFlow<AppFunctionsMcpState> = _state.asStateFlow()

    // ——————————————————————————————————————————————————————
    // Effect — 一次性副作用（通过 Channel 传递）
    // ——————————————————————————————————————————————————————

    private val _effect = Channel<AppFunctionsMcpEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ——————————————————————————————————————————————————————
    // Intent 处理入口
    // ——————————————————————————————————————————————————————

    /**
     * 处理用户意图的统一入口
     * Central entry point for all user intents
     *
     * ViewModel 收到 Intent 后执行业务逻辑，然后更新 State。
     *
     * @param intent 用户意图
     * @see AppFunctionsMcpIntent 所有可用 Intent
     */
    fun sendIntent(intent: AppFunctionsMcpIntent) {
        viewModelScope.launch {
            when (intent) {
                is AppFunctionsMcpIntent.NavigateToSection -> handleNavigateToSection(intent.section)
                is AppFunctionsMcpIntent.ToggleSectionExpand -> handleToggleSectionExpand(intent.sectionId)
                is AppFunctionsMcpIntent.ExpandAllSections -> handleExpandAllSections()
                is AppFunctionsMcpIntent.CollapseAllSections -> handleCollapseAllSections()
                is AppFunctionsMcpIntent.SelectCodeExample -> handleSelectCodeExample(intent.example)
                is AppFunctionsMcpIntent.SearchSections -> handleSearchSections(intent.query)
                is AppFunctionsMcpIntent.ClearSearch -> handleClearSearch()
                is AppFunctionsMcpIntent.LoadSectionContent -> handleLoadSectionContent(intent.sectionId)
            }
        }
    }

    // ——————————————————————————————————————————————————————
    // Intent Handlers / Intent 处理器
    // ——————————————————————————————————————————————————————

    /**
     * 处理章节导航
     * Handle section navigation
     *
     * 导航到新章节时，自动展开该章节并加载内容。
     * When navigating to a new section, auto-expand and load content.
     *
     * @param section 目标章节
     */
    private fun handleNavigateToSection(section: DocSection) {
        val currentState = _state.value
        val newExpanded = currentState.expandedSections.toMutableSet()

        // 如果章节未展开，则展开它 / Expand if not already expanded
        if (!newExpanded.contains(section.id)) {
            newExpanded.add(section.id)
        }

        _state.value = currentState.copy(
            currentSection = section,
            expandedSections = newExpanded
        )

        // 加载章节内容 / Load section content
        handleLoadSectionContent(section.id)
    }

    /**
     * 处理章节展开/折叠切换
     * Handle section expand/collapse toggle
     *
     * @param sectionId 章节 ID
     */
    private fun handleToggleSectionExpand(sectionId: String) {
        val currentState = _state.value
        val newExpanded = currentState.expandedSections.toMutableSet()

        if (newExpanded.contains(sectionId)) {
            newExpanded.remove(sectionId)
        } else {
            newExpanded.add(sectionId)
        }

        _state.value = currentState.copy(expandedSections = newExpanded)
    }

    /**
     * 展开所有章节
     * Expand all sections
     */
    private fun handleExpandAllSections() {
        val currentState = _state.value
        val allSectionIds = DocSection.entries.map { it.id }.toSet()

        _state.value = currentState.copy(expandedSections = allSectionIds)
    }

    /**
     * 折叠所有章节
     * Collapse all sections
     */
    private fun handleCollapseAllSections() {
        val currentState = _state.value
        _state.value = currentState.copy(
            expandedSections = setOf(currentState.currentSection.id)
        )
    }

    /**
     * 处理代码示例选择（放大查看）
     * Handle code example selection for expanded view
     *
     * @param example 选中的代码示例，null 表示关闭放大视图
     */
    private fun handleSelectCodeExample(example: CodeExample?) {
        val currentState = _state.value
        _state.value = currentState.copy(selectedCodeExample = example)
    }

    /**
     * 处理章节搜索
     * Handle section search
     *
     * 根据搜索关键词过滤章节列表。
     * Filter sections based on search query.
     *
     * @param query 搜索关键词
     */
    private fun handleSearchSections(query: String) {
        val currentState = _state.value

        if (query.isBlank()) {
            _state.value = currentState.copy(
                searchQuery = "",
                filteredSections = null
            )
            return
        }

        // 根据标题、描述、标签过滤 / Filter by title, description, tags
        val lowerQuery = query.lowercase()
        val filtered = DocSection.entries.filter { section ->
            section.titleCn.contains(query, ignoreCase = true) ||
            section.titleEn.contains(query, ignoreCase = true) ||
            section.description.contains(query, ignoreCase = true) ||
            section.tags.any { it.contains(lowerQuery) }
        }

        _state.value = currentState.copy(
            searchQuery = query,
            filteredSections = filtered
        )
    }

    /**
     * 清除搜索，恢复正常视图
     * Clear search and return to normal view
     */
    private fun handleClearSearch() {
        val currentState = _state.value
        _state.value = currentState.copy(
            searchQuery = "",
            filteredSections = null
        )
    }

    /**
     * 加载指定章节的内容（代码示例 + 信息卡片）
     * Load content for a specific section (code examples + info cards)
     *
     * 从设计文档提取的各章节核心代码示例。
     * Each section has curated code examples from the design doc.
     *
     * @param sectionId 章节 ID
     */
    private fun handleLoadSectionContent(sectionId: String) {
        val currentState = _state.value
        _state.value = currentState.copy(isLoading = true)

        // 根据章节 ID 加载对应的代码示例和信息卡片
        // Load corresponding code examples and info cards based on section ID
        val (examples, infoCards) = when (sectionId) {
            "0x00" -> loadOverviewContent()
            "0x01" -> loadAnnotationContent()
            "0x02" -> loadParamTypesContent()
            "0x03" -> loadSecurityContent()
            "0x04" -> loadDiscoveryContent()
            "0x05" -> loadDecisionTreeContent()
            "0x06" -> loadVersioningContent()
            "0x07" -> loadCicdContent()
            "0x08" -> loadComplianceContent()
            "0x09" -> loadAdkIntegrationContent()
            else -> Pair(emptyList(), emptyList())
        }

        _state.value = _state.value.copy(
            codeExamples = examples,
            infoCards = infoCards,
            isLoading = false
        )
    }

    // ——————————————————————————————————————————————————————
    // Section Content Loaders / 各章节内容加载器
    // ——————————————————————————————————————————————————————

    /**
     * 0x00 概述与环境配置 — 内容加载
     * Overview & Environment Setup — Content Loader
     */
    private fun loadOverviewContent(): Pair<List<CodeExample>, List<InfoCard>> {
        val examples = listOf(
            CodeExample(
                id = "0x00-001",
                sectionId = "0x00",
                title = "Hello AppFunction — 第一个 AppFunction",
                description = "最简单的 AppFunction 示例，展示基本结构",
                requirements = "Android 16+ (API 36)",
                code = """\
// Hello AppFunction — Your first AppFunction
// 第一个 AppFunction 示例
@AppFunction(
    isDescribedByKDoc = true,
    version = 1
)
suspend fun getGreeting(name: String): String {
    // KDoc 描述会被 Gemini 用来理解函数意图
    // KDoc description is used by Gemini to understand function intent
    return "Hello, ${'$'}name! Welcome to AppFunctions."
}"""
            ),
            CodeExample(
                id = "0x00-002",
                sectionId = "0x00",
                title = "build.gradle.kts 依赖配置",
                description = "AppFunctions Jetpack 库依赖配置",
                requirements = "AndroidX snapshot 仓库",
                code = """\
// build.gradle.kts — AppFunctions 依赖配置
// AppFunctions dependency configuration
plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

dependencies {
    // AppFunctions Core — Android 16+ required
    implementation("androidx.compose.runtime:remote-creation-core:1.0.0-alpha11")
    // 注意：AppFunctions 需要 Android 16 (API 36)
    // Note: AppFunctions requires Android 16 (API 36)
}"""
            ),
            CodeExample(
                id = "0x00-003",
                sectionId = "0x00",
                title = "AndroidManifest.xml Service 声明",
                description = "AppFunctions Service 注册",
                requirements = "Android 16+",
                code = """\
<!-- AndroidManifest.xml — AppFunctions Service 声明 -->
<!-- AppFunctions Service declaration -->
<service
    android:name=".MyAppFunctionService"
    android:exported="true"
    android:permission="android.permission.EXECUTE_APP_FUNCTIONS">
    <intent-filter>
        <action android:name="androidx.compose.runtime.appfunctions.IAppFunctionService" />
    </intent-filter>
</service>"""
            )
        )

        val infoCards = listOf(
            InfoCard(InfoCardType.TIP, "开发环境要求", "Android 16 (API 36) + Jetpack 库 + Kotlin 2.0+"),
            InfoCard(InfoCardType.WARNING, "Private Preview 限制", "截至 2026年5月，AppFunctions + Gemini 集成仍为 Private Preview，需要申请测试资格"),
            InfoCard(InfoCardType.KEY, "核心注解", "@AppFunction 是 AppFunctions 的核心——所有暴露给 AI Agent 的函数都必须标注此注解")
        )

        return Pair(examples, infoCards)
    }

    /**
     * 0x01 注解与 KDoc 编写规范 — 内容加载
     * Annotation & KDoc Specification — Content Loader
     */
    private fun loadAnnotationContent(): Pair<List<CodeExample>, List<InfoCard>> {
        val examples = listOf(
            CodeExample(
                id = "0x01-001",
                sectionId = "0x01",
                title = "@AppFunction 注解完整用法",
                description = "@AppFunction 注解的所有参数说明",
                requirements = "Android 16+",
                code = """\
/**
 * @AppFunction 注解完整参数说明
 * Complete @AppFunction annotation parameters
 *
 * @param isDescribedByKDoc KDoc 自动生成 AI 可理解描述
 * @param version 函数版本号（签名变更时必须递增）
 */
@AppFunction(
    isDescribedByKDoc = true,  // KDoc → AI description
    version = 1                  // 版本号，签名变更时递增
)
suspend fun searchRecipes(
    /** Recipe name or ingredients to search for */
    query: String,
    /** Maximum number of results to return */
    maxResults: Int = 10
): List<Recipe> {
    // ... implementation
}"""
            ),
            CodeExample(
                id = "0x01-002",
                sectionId = "0x01",
                title = "KDoc → AI Description 映射规则",
                description = "如何编写 KDoc 使 AI 准确理解函数意图",
                requirements = "",
                code = """\
/**
 * Searches for recipes by name or ingredients.
 * 搜索食谱，通过名称或食材查找
 *
 * This function searches the user's recipe database and returns
 * matching recipes sorted by relevance.
 *
 * @param query Search terms (recipe name or ingredients)
 * @param maxResults Maximum number of results (default: 10)
 * @return List of matching Recipe objects
 *
 * AI 理解: 当用户说"找到面条食谱"时，Gemini 会调用此函数
 * Gemini calls this when user says "find noodle recipes"
 */
@AppFunction(isDescribedByKDoc = true, version = 1)
suspend fun searchRecipes(query: String, maxResults: Int = 10): List<Recipe>"""
            )
        )

        val infoCards = listOf(
            InfoCard(InfoCardType.KEY, "KDoc 是 AI 理解的关键", "KDoc 描述的质量直接决定 Gemini 调用函数的准确性"),
            InfoCard(InfoCardType.TIP, "参数命名建议", "使用描述性参数名 + KDoc 说明，AI 会综合理解"),
            InfoCard(InfoCardType.WARNING, "版本号必须维护", "AppFunction 签名变化时，版本号必须递增，否则 Agent 可能使用旧描述")
        )

        return Pair(examples, infoCards)
    }

    /**
     * 0x02 参数类型与返回值设计 — 内容加载
     * Parameter Types & Return Values — Content Loader
     */
    private fun loadParamTypesContent(): Pair<List<CodeExample>, List<InfoCard>> {
        val examples = listOf(
            CodeExample(
                id = "0x02-001",
                sectionId = "0x02",
                title = "Kotlinx Serialization 复杂对象",
                description = "使用 @Serializable 标注复杂对象参数和返回值",
                requirements = "kotlinx-serialization",
                code = """\
// 复杂对象必须使用 @Serializable 标注
// Complex objects must be annotated with @Serializable
@Serializable
data class Recipe(
    val id: String,
    val name: String,
    val ingredients: List<String>,
    val cookingTimeMinutes: Int
)

/**
 * Search for recipes by query string.
 * @param query Search terms
 * @return List of matching recipes
 */
@AppFunction(isDescribedByKDoc = true, version = 1)
suspend fun searchRecipes(query: String): List<Recipe> {
    // ... implementation
}"""
            ),
            CodeExample(
                id = "0x02-002",
                sectionId = "0x02",
                title = "Suspend 函数与 Flow 返回值",
                description = "支持 suspend 函数和 Flow 异步流返回",
                requirements = "",
                code = """\
// Suspend 函数 — 标准异步调用
// Suspend function — standard async call
@AppFunction(isDescribedByKDoc = true, version = 1)
suspend fun getRecipe(id: String): Recipe?

// Flow 返回值 — 流式数据返回
// Flow return value — streaming data
@AppFunction(isDescribedByKDoc = true, version = 1)
fun getRecipesStream(query: String): Flow<List<Recipe>> {
    return flow {
        // Emit results as they become available
        // 结果产生时立即发送
    }
}"""
            )
        )

        val infoCards = listOf(
            InfoCard(InfoCardType.KEY, "必须使用 Kotlinx Serialization", "复杂对象序列化是 AppFunctions 的强制要求，不可用其他序列化方案"),
            InfoCard(InfoCardType.TIP, "优先使用 suspend 函数", "简单场景用 suspend 函数，复杂流式场景才用 Flow"),
            InfoCard(InfoCardType.WARNING, "普通 suspend 函数不会被发现", "只有标注了 @AppFunction 的 suspend 函数才会被 OS Registry 发现")
        )

        return Pair(examples, infoCards)
    }

    /**
     * 0x03 安全与权限配置 — 内容加载
     * Security & Permissions — Content Loader
     */
    private fun loadSecurityContent(): Pair<List<CodeExample>, List<InfoCard>> {
        val examples = listOf(
            CodeExample(
                id = "0x03-001",
                sectionId = "0x03",
                title = "EXECUTE_APP_FUNCTIONS 权限配置",
                description = "调用方和提供方双方权限配置",
                requirements = "",
                code = """\
<!-- 调用方 (Caller) AndroidManifest.xml -->
<!-- Caller AndroidManifest.xml -->
<uses-permission android:name="android.permission.EXECUTE_APP_FUNCTIONS" />

<!-- 提供方 (Provider) AndroidManifest.xml -->
<!-- Provider AndroidManifest.xml -->
<service
    android:name=".MyAppFunctionService"
    android:exported="true"
    android:permission="android.permission.EXECUTE_APP_FUNCTIONS">
    <intent-filter>
        <action android:name="androidx.compose.runtime.appfunctions.IAppFunctionService" />
    </intent-filter>
</service>"""
            ),
            CodeExample(
                id = "0x03-002",
                sectionId = "0x03",
                title = "用户确认机制 — 敏感操作",
                description = "敏感操作必须走系统确认弹窗",
                requirements = "",
                code = """\
/**
 * 发送消息 — 敏感操作，需要用户确认
 * Send message — sensitive operation, requires user confirmation
 *
 * @param recipient Message recipient
 * @param content Message content
 * @return Confirmation status
 */
@AppFunction(
    isDescribedByKDoc = true,
    version = 1,
    requiresUserConfirmation = true  // 强制用户确认 / Force user confirmation
)
suspend fun sendMessage(recipient: String, content: String): SendResult {
    // 系统会自动弹出确认弹窗
    // System will automatically show confirmation dialog
    // 不能在此函数中伪造确认 / Cannot fake confirmation here
}"""
            )
        )

        val infoCards = listOf(
            InfoCard(InfoCardType.WARNING, "用户确认不能伪造", "敏感操作必须走系统确认弹窗，App 无法绕过——这是安全设计"),
            InfoCard(InfoCardType.KEY, "双向权限控制", "调用方需要 EXECUTE_APP_FUNCTIONS 权限，提供方需要在 AndroidManifest 中声明"),
            InfoCard(InfoCardType.TIP, "渐进式暴露策略", "先暴露读操作（搜索），再考虑写操作（支付/消息）")
        )

        return Pair(examples, infoCards)
    }

    /**
     * 0x04 发现与执行机制 — 内容加载
     * Discovery & Execution — Content Loader
     */
    private fun loadDiscoveryContent(): Pair<List<CodeExample>, List<InfoCard>> {
        val examples = listOf(
            CodeExample(
                id = "0x04-001",
                sectionId = "0x04",
                title = "OS Registry 工作原理",
                description = "AppFunctions 注册表的工作机制",
                requirements = "",
                code = """\
// AppFunctions OS Registry 工作流程
// AppFunctions OS Registry workflow
//
// 1. App 启动时 / App Startup:
//    AppFunctionService.onCreate() → 注册所有 @AppFunction 到 OS Registry
//
// 2. Agent 查询时 / Agent Query:
//    Gemini → OS Registry → 查询匹配的 AppFunction
//
// 3. 执行调用 / Execution:
//    OS → AppFunctionService → 执行对应函数 → 返回结果给 Agent

class MyAppFunctionService : AppFunctionService() {
    override fun onCreate() {
        super.onCreate()
        // 所有 @AppFunction 自动注册到 OS Registry
        // All @AppFunction are automatically registered to OS Registry
    }
}"""
            ),
            CodeExample(
                id = "0x04-002",
                sectionId = "0x04",
                title = "调用生命周期详解",
                description = "从 Agent 发现到执行完成的完整生命周期",
                requirements = "",
                code = """\
// AppFunction 调用生命周期 / AppFunction call lifecycle
//
// 1. DISCOVERED    — Agent 在 Registry 中找到匹配的 AppFunction
// 2. VALIDATED     — 参数类型和数量验证通过
// 3. PENDING       — 等待用户确认（如需要）
// 4. EXECUTING     — 函数正在执行
// 5. USER_CONFIRM  — 等待用户交互确认
// 6. COMPLETED     — 执行成功，返回结果
// 7. FAILED        — 执行失败，返回错误

enum class AppFunctionState {
    PENDING,
    EXECUTING,
    USER_CONFIRMATION_REQUIRED,
    COMPLETED,
    FAILED
}"""
            )
        )

        val infoCards = listOf(
            InfoCard(InfoCardType.KEY, "OS Registry 是核心", "AppFunctions 的核心机制——App 在 OS 级别注册，Agent 在 OS 级别查询"),
            InfoCard(InfoCardType.TIP, "测试独立于 Gemini", "先用 Android Studio 的 AppFunctions Explorer 测试，避免依赖 Gemini 集成进度")
        )

        return Pair(examples, infoCards)
    }

    /**
     * 0x05 选型决策树 — 内容加载
     * Decision Tree — Content Loader
     */
    private fun loadDecisionTreeContent(): Pair<List<CodeExample>, List<InfoCard>> {
        val examples = listOf(
            CodeExample(
                id = "0x05-001",
                sectionId = "0x05",
                title = "三种跨 App 机制对比表",
                description = "AppFunctions vs Deep Links vs Intents 核心区别",
                requirements = "",
                code = """\
// AppFunctions vs Deep Links vs Intents — 选型决策
// When to use AppFunctions, Deep Links, or Intents

// ✅ 使用 AppFunctions 的场景:
// - 需要 AI Agent（如 Gemini）发现和调用
// - 跨 App 编排工作流（AI 驱动的多 App 协作）
// - 需要 OS 级别的函数注册表
// Use AppFunctions when:
// - AI Agent (Gemini) needs to discover and call your app
// - Cross-app orchestration (AI-driven multi-app workflows)
// - OS-level function registry required

// ✅ 使用 Deep Links 的场景:
// - 用户直接点击链接跳转
// - 已有的 URL Scheme 需要保持兼容
// Use Deep Links when:
// - User clicks a link to navigate
// - Existing URL schemes need to remain compatible

// ✅ 使用 Intents 的场景:
// - 明确知道目标 App
// - 需要立即同步执行
// Use Intents when:
// - You know the target app explicitly
// - Immediate synchronous execution needed"""
            ),
            CodeExample(
                id = "0x05-002",
                sectionId = "0x05",
                title = "共存架构设计",
                description = "三种机制共存的推荐架构",
                requirements = "",
                code = """\
// 共存架构 / Coexistence architecture
// AppFunctions + Deep Links + Intents 可以共存

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. AppFunctions — AI 发现和调用
        //    AppFunctions — AI discovery and calls
        registerAppFunctions()

        // 2. Deep Links — URL 导航
        //    Deep Links — URL navigation
        handleDeepLinks()

        // 3. Intents — 显式 App 间调用
        //    Intents — explicit inter-app calls
        handleIntents()
    }
}"""
            )
        )

        val infoCards = listOf(
            InfoCard(InfoCardType.KEY, "AppFunctions 是 AI 时代的跨 App 机制", "Deep Links 给人用，Intents 给 App 用，AppFunctions 给 AI Agent 用"),
            InfoCard(InfoCardType.TIP, "可以共存", "三种机制互不冲突，可以同时实现"),
            InfoCard(InfoCardType.WARNING, "不要过度设计", "只有确实需要 AI 发现时才用 AppFunctions，其他场景用 Deep Links/Intents 更简单")
        )

        return Pair(examples, infoCards)
    }

    /**
     * 0x06 版本管理与灰度发布 — 内容加载
     * Versioning & Rollout — Content Loader
     */
    private fun loadVersioningContent(): Pair<List<CodeExample>, List<InfoCard>> {
        val examples = listOf(
            CodeExample(
                id = "0x06-001",
                sectionId = "0x06",
                title = "版本号递增规则",
                description = "何时以及如何递增 AppFunction 版本号",
                requirements = "",
                code = """\
// 版本号递增规则 / Version increment rules
//
// 必须递增版本号的情况 / When you MUST increment version:
// - 参数类型变化 / Parameter type changes
// - 返回值类型变化 / Return type changes
// - 函数签名变化 / Function signature changes
// - 行为语义变化 / Behavioral semantics change
//
// 不用递增的情况 / When NOT to increment:
// - 内部实现优化 / Internal implementation optimization
// - 性能改进 / Performance improvements
// - Bug 修复（行为不变）/ Bug fixes (behavior unchanged)

@AppFunction(isDescribedByKDoc = true, version = 2)  // 版本从 1 → 2
suspend fun searchRecipes(
    query: String,
    maxResults: Int = 10,
    includeNutritions: Boolean = false  // 新增参数 → 必须递增版本
): List<Recipe>"""
            ),
            CodeExample(
                id = "0x06-002",
                sectionId = "0x06",
                title = "向后兼容策略",
                description = "保持向后兼容的最佳实践",
                requirements = "",
                code = """\
// 向后兼容策略 / Backward compatibility strategy
//
// 策略 1: 参数默认值
// Strategy 1: Parameter default values
@AppFunction(isDescribedByKDoc = true, version = 2)
suspend fun searchRecipes(
    query: String,
    maxResults: Int = 10,           // 新参数有默认值，旧 Agent 调用不受影响
    includeNutritions: Boolean = false
): List<Recipe>
//
// 策略 2: 函数重载（保留旧版本）
// Strategy 2: Function overload (keep old version)
@AppFunction(isDescribedByKDoc = true, version = 1)
suspend fun searchRecipes(query: String): List<Recipe>  // 旧版本保留

@AppFunction(isDescribedByKDoc = true, version = 2)
suspend fun searchRecipes(
    query: String,
    maxResults: Int = 10,
    includeNutritions: Boolean = false
): List<Recipe>"""
            )
        )

        val infoCards = listOf(
            InfoCard(InfoCardType.KEY, "版本号是 Agent 的指南针", "版本号告诉 Agent 函数的签名，变化时必须告知"),
            InfoCard(InfoCardType.WARNING, "不要删除旧版本函数", "保留旧版本函数直到所有 Agent 都迁移到新版本"),
            InfoCard(InfoCardType.TIP, "使用参数默认值", "新增可选参数时使用默认值，可以保持向后兼容")
        )

        return Pair(examples, infoCards)
    }

    /**
     * 0x07 CI/CD 测试工具 — 内容加载
     * CI/CD Testing Tools — Content Loader
     */
    private fun loadCicdContent(): Pair<List<CodeExample>, List<InfoCard>> {
        val examples = listOf(
            CodeExample(
                id = "0x07-001",
                sectionId = "0x07",
                title = "AppFunction 可发现性测试",
                description = "验证函数是否正确注册到 OS Registry",
                requirements = "",
                code = """\
// 可发现性测试 / Discoverability test
// 验证 AppFunction 是否被 OS Registry 正确注册
// Verify AppFunction is correctly registered in OS Registry

@Test
fun testAppFunctionDiscoverability() {
    // 1. 启动 App
    //    Start the app
    
    // 2. 查询 Registry 中的 AppFunction
    //    Query AppFunctions in Registry
    val registry = OSRegistry.getInstance()
    val functions = registry.queryFunctions(
        namespace = "com.example.myapp"
    )
    
    // 3. 验证预期函数存在
    //    Verify expected functions exist
    assert(functions.any { it.name == "searchRecipes" })
    assert(functions.any { it.name == "getGroceryList" })
}"""
            ),
            CodeExample(
                id = "0x07-002",
                sectionId = "0x07",
                title = "参数验证测试",
                description = "验证参数类型和数量是否正确",
                requirements = "",
                code = """\
// 参数验证测试 / Parameter validation test
@Test
fun testAppFunctionParameterValidation() {
    val function = registry.getFunction("searchRecipes")
    
    // 验证参数类型
    // Verify parameter types
    assert(function.parameters.size == 2)
    assert(function.parameters[0].name == "query")
    assert(function.parameters[0].type == "kotlin.String")
    assert(function.parameters[1].name == "maxResults")
    assert(function.parameters[1].type == "kotlin.Int")
    assert(function.parameters[1].hasDefaultValue == true)
    
    // 验证返回值类型
    // Verify return type
    assert(function.returnType == "kotlin.collections.List")
}"""
            )
        )

        val infoCards = listOf(
            InfoCard(InfoCardType.TIP, "测试独立于 Gemini", "先用 Android Studio AppFunctions Explorer 测试，不依赖 Gemini 集成进度"),
            InfoCard(InfoCardType.KEY, "CI/CD 必须包含可发现性测试", "每次构建都应验证 AppFunction 是否正确注册"),
            InfoCard(InfoCardType.WARNING, "参数变更必须更新测试", "函数签名变化时，测试用例也要相应更新")
        )

        return Pair(examples, infoCards)
    }

    /**
     * 0x08 企业合规与隐私 — 内容加载
     * Enterprise Compliance & Privacy — Content Loader
     */
    private fun loadComplianceContent(): Pair<List<CodeExample>, List<InfoCard>> {
        val examples = listOf(
            CodeExample(
                id = "0x08-001",
                sectionId = "0x08",
                title = "MDM 策略影响",
                description = "企业 MDM 环境下 AppFunctions 的行为",
                requirements = "",
                code = """\
// MDM 环境下 AppFunctions 行为 / AppFunctions behavior under MDM
//
// 企业设备 MDM 策略可能影响:
// MDM policies may affect:
// - 禁用 EXECUTE_APP_FUNCTIONS 权限
//   Disable EXECUTE_APP_FUNCTIONS permission
// - 限制可发现的 AppFunction 范围
//   Limit discoverable AppFunction scope
// - 要求额外的合规审批
//   Require additional compliance approval

// 检测 MDM 策略 / Detect MDM policies
fun isAppFunctionAllowedByMDM(): Boolean {
    val devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE)
    // Check if MDM allows AppFunctions
    return devicePolicyManager.isFunctionExecutionAllowed()
}"""
            ),
            CodeExample(
                id = "0x08-002",
                sectionId = "0x08",
                title = "GDPR 数据最小化示例",
                description = "AppFunction 中的数据最小化实现",
                requirements = "",
                code = """\
// GDPR 数据最小化 / GDPR Data minimization
//
// AppFunction 应只返回必要数据，不暴露额外信息
// AppFunction should only return necessary data

@AppFunction(isDescribedByKDoc = true, version = 1)
suspend fun getUserProfile(
    /** Fields to retrieve (only these fields will be returned) */
    fields: List<ProfileField>
): UserProfile {
    // ✅ 好的实现：只返回请求的字段
    // Good: only return requested fields
    val fullProfile = database.getUserProfile()
    return fullProfile.copy(
        // 过滤掉未请求的敏感字段
        // Filter out unrequested sensitive fields
        email = if (fields.contains(ProfileField.EMAIL)) fullProfile.email else null,
        phone = if (fields.contains(ProfileField.PHONE)) fullProfile.phone else null,
        address = null  // 永远不返回地址，除非明确请求
    )
}"""
            )
        )

        val infoCards = listOf(
            InfoCard(InfoCardType.WARNING, "MDM 可能禁用 AppFunctions", "企业设备上的 MDM 策略会影响 AppFunctions 的可用性"),
            InfoCard(InfoCardType.KEY, "GDPR 数据最小化", "AppFunction 只能返回必要的最小数据集，不能返回完整的数据库记录"),
            InfoCard(InfoCardType.TIP, "隐私政策必须更新", "实现 AppFunctions 后，隐私政策需要明确说明 AI Agent 访问情况")
        )

        return Pair(examples, infoCards)
    }

    /**
     * 0x09 ADK 协同架构 — 内容加载
     * ADK Integration — Content Loader
     */
    private fun loadAdkIntegrationContent(): Pair<List<CodeExample>, List<InfoCard>> {
        val examples = listOf(
            CodeExample(
                id = "0x09-001",
                sectionId = "0x09",
                title = "AppFunctions vs ADK 角色对比",
                description = "AppFunctions 让 App 被发现，ADK 让 App 内置 Agent",
                requirements = "",
                code = """\
// AppFunctions vs ADK for Android — 角色对比
// AppFunctions vs ADK for Android — Role comparison
//
// AppFunctions:
// - App 是被动工具节点，被 AI Agent 发现和调用
// - App is a passive tool node, discovered and called by AI Agent
// - 场景: Gemini 发现邮件 App → 调用搜索函数
//
// ADK for Android:
// - App 内置 Agent，为 App 的用户提供服务
// - App has built-in Agent, serves App's users
// - 场景: App 内置购物 Agent → 帮用户管理购物车

// 两者互补 / Both complement each other
// AppFunctions: App as AI tool (被 AI 用)
// ADK: App with AI inside (有 AI 在内)"""
            ),
            CodeExample(
                id = "0x09-002",
                sectionId = "0x09",
                title = "AppFunctions 作为 MCP 节点",
                description = "AppFunctions 是 On-Device MCP 实现",
                requirements = "ADK 0.1.0+",
                code = """\
// AppFunctions = On-Device MCP
// AppFunctions 是 Android 平台原生的 MCP 协议实现
// AppFunctions = Android-native MCP protocol implementation

// MCP Server (传统云端) / Traditional cloud MCP Server:
// Agent → HTTP → MCP Server → Tools

// AppFunctions (On-Device):
// Agent (Gemini) → OS Registry → AppFunction → Tools
//                         ↑
//                    Android OS 内置
//                    Built into Android OS

// AppFunctions 与 ADK 协同 / AppFunctions + ADK collaboration:
// 1. ADK Agent 需要调用外部工具时，通过 AppFunctions 发现其他 App
//    When ADK Agent needs external tools, discovers other apps via AppFunctions
// 2. AppFunctions 提供的函数可被 ADK Agent 直接调用
//    Functions provided by AppFunctions can be called directly by ADK Agent"""
            )
        )

        val infoCards = listOf(
            InfoCard(InfoCardType.KEY, "AppFunctions = On-Device MCP", "AppFunctions 是 Android 平台内置的 MCP 协议实现，完全运行在设备上，无需网络"),
            InfoCard(InfoCardType.TIP, "与 ADK 互补", "ADK 让 App 内置 Agent，AppFunctions 让 App 被外部 Agent 发现——两者协同"),
            InfoCard(InfoCardType.WARNING, "Private Preview", "ADK for Android 0.1.0 截至 2026年5月仍为早期版本，生产使用需评估稳定性")
        )

        return Pair(examples, infoCards)
    }
}
