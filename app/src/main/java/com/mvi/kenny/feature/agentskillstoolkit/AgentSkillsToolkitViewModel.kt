package com.mvi.kenny.feature.agentskillstoolkit

// ================================================================
// AgentSkillsToolkitViewModel — Android Agent Skills 技能库生态工具包 ViewModel
// ================================================================
// ViewModel for Android Agent Skills Ecosystem Toolkit.
//
// PRD-233: Android Agent Skills 技能库生态工具包
//
// MVI Pattern:
//   State — AgentSkillsToolkitState (immutable, updated via reduce)
//   Intent — AgentSkillsToolkitIntent (user actions)
//   Effect — AgentSkillsToolkitEffect (one-time side effects via Channel)
//
// This ViewModel simulates all scanner/validation/generation results
// with realistic demo data. No real file system operations are performed.
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * AgentSkillsToolkitViewModel — Main ViewModel for the Agent Skills Toolkit
 * Handles all user intents, manages state, and emits side effects.
 */
class AgentSkillsToolkitViewModel : ViewModel() {

    // ================================================================
    // MVI State — UI 状态
    // ================================================================
    private val _state = MutableStateFlow(AgentSkillsToolkitState())
    val state: StateFlow<AgentSkillsToolkitState> = _state.asStateFlow()

    // ================================================================
    // MVI Effect — 副作用 Channel
    // ================================================================
    private val _effect = Channel<AgentSkillsToolkitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ================================================================
    // Initialization — 初始化演示数据
    // ================================================================
    init {
        loadInitialData()
    }

    /**
     * Load initial demo data for all tabs.
     * 加载所有 Tab 的初始演示数据。
     */
    private fun loadInitialData() {
        _state.value = _state.value.copy(
            templates = getSampleTemplates(),
            cliCommands = getSampleCliCommands(),
            ciSteps = getSampleCiSteps(),
            sharedSkills = getSampleSharedSkills()
        )
    }

    // ================================================================
    // Intent Handler — 处理所有用户意图
    // ================================================================

    /**
     * Process user intent and update state accordingly.
     * 处理用户意图并更新状态。
     */
    fun sendIntent(intent: AgentSkillsToolkitIntent) {
        when (intent) {
            is AgentSkillsToolkitIntent.SelectTab -> selectTab(intent.index)
            is AgentSkillsToolkitIntent.SelectTemplate -> selectTemplate(intent.template)
            is AgentSkillsToolkitIntent.GenerateSkillFile -> generateSkillFile()
            is AgentSkillsToolkitIntent.ClearGenerationState -> clearGenerationState()
            is AgentSkillsToolkitIntent.UpdateValidationPath -> updateValidationPath(intent.path)
            is AgentSkillsToolkitIntent.StartScan -> startScan()
            is AgentSkillsToolkitIntent.ToggleResultExpanded -> toggleResultExpanded(intent.resultId)
            is AgentSkillsToolkitIntent.SelectCliSection -> selectCliSection(intent.section)
            is AgentSkillsToolkitIntent.UpdateKbQuery -> updateKbQuery(intent.query)
            is AgentSkillsToolkitIntent.SearchKnowledgeBase -> searchKnowledgeBase()
            is AgentSkillsToolkitIntent.SelectKbResult -> selectKbResult(intent.result)
            is AgentSkillsToolkitIntent.ToggleCiStep -> toggleCiStep(intent.index)
            is AgentSkillsToolkitIntent.FilterShareSkills -> filterShareSkills(intent.category)
            is AgentSkillsToolkitIntent.UpdateEfficiencyInput -> updateEfficiencyInput(intent.input)
            is AgentSkillsToolkitIntent.GenerateEfficiencyReport -> generateEfficiencyReport()
        }
    }

    // ================================================================
    // Tab Navigation — Tab 切换
    // ================================================================

    private fun selectTab(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    // ================================================================
    // Tab 0: Skill Creation — 技能创作
    // ================================================================

    private fun selectTemplate(template: SkillTemplate) {
        _state.value = _state.value.copy(selectedTemplate = template)
        viewModelScope.launch {
            _effect.send(AgentSkillsToolkitEffect.ShowSkillPreview(template))
        }
    }

    private fun generateSkillFile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true, generationSuccess = false)
            // Simulate file generation delay / 模拟文件生成延迟
            delay(1500)
            _state.value = _state.value.copy(isGenerating = false, generationSuccess = true)
            _effect.send(AgentSkillsToolkitEffect.ShowSnackbar("Skill file generated successfully! / Skill 文件生成成功"))
        }
    }

    private fun clearGenerationState() {
        _state.value = _state.value.copy(generationSuccess = false, selectedTemplate = null)
    }

    // ================================================================
    // Tab 1: Validation Scanner — 验证扫描
    // ================================================================

    private fun updateValidationPath(path: String) {
        _state.value = _state.value.copy(validationPath = path)
    }

    private fun startScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isScanning = true,
                scanProgress = 0f,
                validationPhase = ValidationPhase.PARSING,
                validationResults = emptyList()
            )

            // Phase 1: Parsing / 阶段1：解析
            delay(600)
            _state.value = _state.value.copy(scanProgress = 0.25f, validationPhase = ValidationPhase.ANALYZING)

            // Phase 2: Analyzing / 阶段2：分析
            delay(800)
            _state.value = _state.value.copy(scanProgress = 0.6f, validationPhase = ValidationPhase.GENERATING_REPORT)

            // Phase 3: Generating report / 阶段3：生成报告
            delay(600)
            _state.value = _state.value.copy(
                scanProgress = 1f,
                validationPhase = ValidationPhase.COMPLETED,
                validationResults = getSampleValidationResults()
            )

            _effect.send(AgentSkillsToolkitEffect.ShowSnackbar("Scan completed! / 扫描完成"))
        }
    }

    private fun toggleResultExpanded(resultId: String) {
        val current = _state.value.expandedResultId
        _state.value = _state.value.copy(
            expandedResultId = if (current == resultId) null else resultId
        )
    }

    // ================================================================
    // Tab 2: CLI Guide — CLI 命令教程
    // ================================================================

    private fun selectCliSection(section: CliSection) {
        _state.value = _state.value.copy(cliSection = section)
    }

    // ================================================================
    // Tab 3: Knowledge Base + CI — 知识库查询 + CI 配置
    // ================================================================

    private fun updateKbQuery(query: String) {
        _state.value = _state.value.copy(kbQuery = query)
    }

    private fun searchKnowledgeBase() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSearchingKb = true)
            delay(1000)
            _state.value = _state.value.copy(
                isSearchingKb = false,
                kbResults = getSampleKbResults(_state.value.kbQuery)
            )
        }
    }

    private fun selectKbResult(result: KbResult) {
        _state.value = _state.value.copy(kbSelectedResult = result)
    }

    private fun toggleCiStep(index: Int) {
        val current = _state.value.completedSteps
        _state.value = _state.value.copy(
            completedSteps = if (index in current) current - index else current + index
        )
    }

    // ================================================================
    // Tab 4: Share + Efficiency — 分享平台 + 效能评估
    // ================================================================

    private fun filterShareSkills(category: SkillCategory?) {
        _state.value = _state.value.copy(shareFilter = category)
    }

    private fun updateEfficiencyInput(input: EfficiencyInput) {
        _state.value = _state.value.copy(efficiencyInput = input)
    }

    private fun generateEfficiencyReport() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isGeneratingReport = true)
            delay(2000)
            _state.value = _state.value.copy(
                isGeneratingReport = false,
                efficiencyReport = EfficiencyReport(
                    skillName = _state.value.efficiencyInput.skillPath.ifEmpty { "Room Migration Skill" },
                    taskCompletionRate = 87f,
                    avgTimeMinutes = 12.5f,
                    accuracyScore = 91f,
                    coverageScore = 84f,
                    overallScore = 87f
                )
            )
        }
    }

    // ================================================================
    // Sample Data — 示例数据（用于演示）
    // ================================================================

    private fun getSampleTemplates(): List<SkillTemplate> = listOf(
        SkillTemplate(
            id = "tpl-room-migration",
            name = "Room Database Migration",
            nameCn = "Room 数据库迁移",
            description = "Step-by-step guide for migrating Room database schemas with complex column changes.",
            descriptionCn = "复杂列变更场景下 Room 数据库 Schema 迁移的分步指南。",
            category = SkillCategory.ROOM,
            content = """# Skill: Room Database Migration

## Description
This skill guides agents through Room database migration with version tracking, fallback strategies, and testing.

## Trigger
- Agent encounters `Migration` or `RoomDatabase` in task description
- User mentions "migrate database" or "Room upgrade"

## Instructions
1. Identify current and target schema versions
2. Generate migration strategy (automatic vs manual)
3. Write `Migration` class or use `fallbackToDestructiveMigration`
4. Add version to `Room.databaseBuilder()`
5. Write unit tests for migration

## Examples
\`\`\`kotlin
val migration = object : Migration(1, 2) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE users ADD COLUMN age INTEGER")
  }
}
\`\`\`

## Outputs
- Migration class file
- Updated database version
- Unit test file

## Error Handling
- If table rename needed: Use `ALTER TABLE ... RENAME TO`
- If column type change: Use intermediate column + data copy
""",
            usageCount = 2847,
            rating = 4.8f
        ),
        SkillTemplate(
            id = "tpl-compose-testing",
            name = "Jetpack Compose Testing",
            nameCn = "Jetpack Compose 测试",
            description = "Comprehensive guide for testing Compose UI with testing v2 API.",
            descriptionCn = "使用 testing v2 API 测试 Compose UI 的完整指南。",
            category = SkillCategory.TESTING,
            content = """# Skill: Jetpack Compose Testing

## Description
Guides agents through Compose UI testing using the v2 Testing API with StandardTestDispatcher.

## Trigger
- Task mentions "test Compose" or "composeTestRule"
- User asks about "Compose testing v2" or "dispatcher"

## Instructions
1. Replace composeTestRule with createComposeRule
2. Configure StandardTestDispatcher in test
3. Use runTest { } with proper dispatchers
4. Write semantic assertions with testTag

## Examples
\`\`\`kotlin
@Composable
fun Greeting(name: String) {
  Text(text = "Hello ${'$'}name!", testTag = "greeting")
}

@Test
fun testGreeting() = runTest {
  composeTestRule.setContent { Greeting("Kenny") }
  composeTestRule.onNodeWithTag("greeting")
    .assertTextContains("Hello Kenny!")
}
\`\`\`
""",
            usageCount = 1923,
            rating = 4.6f
        ),
        SkillTemplate(
            id = "tpl-agp-upgrade",
            name = "AGP Upgrade Assistant",
            nameCn = "AGP 升级助手",
            description = "Automated guidance for upgrading Android Gradle Plugin versions with compatibility checks.",
            descriptionCn = "AGP 版本升级的自动化指导，含兼容性检查。",
            category = SkillCategory.AGP,
            content = """# Skill: AGP Upgrade Assistant

## Description
Guides agents through AGP version upgrades with deprecation warnings and breaking changes detection.

## Trigger
- Task mentions "upgrade AGP" or "update Android Gradle Plugin"
- User asks about "AGP 9.0" or "AGP compatibility"

## Instructions
1. Check current AGP version in settings.gradle.kts
2. Identify target AGP version
3. Run compatibility checks for Kotlin, Gradle, KSP versions
4. Update gradle wrapper first
5. Update AGP version incrementally (major versions one at a time)
6. Run ./gradlew :app:dependencies to check conflicts
""",
            usageCount = 1456,
            rating = 4.5f
        ),
        SkillTemplate(
            id = "tpl-ksp2-migration",
            name = "KSP1 to KSP2 Migration",
            nameCn = "KSP1 到 KSP2 迁移",
            description = "Complete guide for migrating from KSP1 to KSP2 with Kotlin 2.2+.",
            descriptionCn = "Kotlin 2.2+ 环境下 KSP1 到 KSP2 的完整迁移指南。",
            category = SkillCategory.KSP,
            content = """# Skill: KSP1 to KSP2 Migration

## Description
Guides agents through KSP1 → KSP2 migration with Kotlin 2.2+.

## Trigger
- Task mentions "KSP2" or "ksp.useKSP2"
- User asks about "KSP1 deprecated" or "Kotlin 2.2 KSP"

## Instructions
1. Check Kotlin version (must be ≥ 2.0 for KSP2)
2. Add to gradle.properties: ksp.useKSP2=true
3. Verify all annotation processors support KSP2
4. Update Room to 2.6.0+ for KSP2 support
5. Run build and check for KSP2-related errors

## Examples
\`\`\`properties
# gradle.properties
ksp.useKSP2=true
kotlin.version=2.2.20
\`\`\`
""",
            usageCount = 892,
            rating = 4.7f
        ),
        SkillTemplate(
            id = "tpl-compose-layout",
            name = "Compose Layout Debug",
            nameCn = "Compose 布局调试",
            description = "Debug and fix common Jetpack Compose layout issues with modifier chains.",
            descriptionCn = "调试和修复常见的 Jetpack Compose 布局问题。",
            category = SkillCategory.COMPOSE,
            content = """# Skill: Compose Layout Debug

## Description
Diagnoses and fixes common Compose layout issues: sizing, alignment, overflow, and modifier ordering.

## Trigger
- Task mentions "layout broken" or "Composable not showing"
- User asks about "Compose layout" or "Modifier order"

## Instructions
1. Check parent constraints (fillMaxSize vs wrapContent)
2. Verify Modifier.order: padding → size → alignment → offset
3. Use LayoutInspector to debug actual layout bounds
4. Check for "ConstraintLayout" vs "Box" appropriateness
5. Review Intrinsic measurements for LazyColumn items
""",
            usageCount = 2341,
            rating = 4.4f
        ),
        SkillTemplate(
            id = "tpl-arch-clean",
            name = "Clean Architecture Setup",
            nameCn = "Clean Architecture 初始化",
            description = "Scaffold a Clean Architecture Android project with domain/data/presentation layers.",
            descriptionCn = "脚手架 Clean Architecture Android 项目，含 domain/data/presentation 三层。",
            category = SkillCategory.ARCH,
            content = """# Skill: Clean Architecture Setup

## Description
Scaffolds a new Android project with Clean Architecture principles.

## Trigger
- User asks to "set up Clean Architecture" or "create new project structure"
- Task mentions "domain layer" or "use case"

## Instructions
1. Create three modules: :domain, :data, :presentation
2. Define UseCase classes in :domain
3. Implement Repository interfaces in :domain
4. Create Repository implementations in :data
5. Set up ViewModel in :presentation
6. Use Hilt for dependency injection across layers
""",
            usageCount = 1678,
            rating = 4.9f
        )
    )

    private fun getSampleCliCommands(): List<CliCommand> = listOf(
        CliCommand(
            section = CliSection.LIST,
            command = "android skills list",
            description = "List all locally installed Android Skills.",
            descriptionCn = "列出本地已安装的所有 Android Skills。",
            examples = listOf(
                "android skills list" to "Show all installed skills",
                "android skills list --format json" to "Output as JSON"
            ),
            examplesCn = listOf(
                "android skills list" to "显示所有已安装的 skills",
                "android skills list --format json" to "以 JSON 格式输出"
            )
        ),
        CliCommand(
            section = CliSection.INSTALL,
            command = "android skills install <skill-name>",
            description = "Install a Skill from a registry (default: Google Skill Registry).",
            descriptionCn = "从 registry 安装 Skill（默认：Google Skill Registry）。",
            examples = listOf(
                "android skills install room-migration" to "Install Room Migration skill",
                "android skills install --from ./my-skill" to "Install from local path"
            ),
            examplesCn = listOf(
                "android skills install room-migration" to "安装 Room Migration skill",
                "android skills install --from ./my-skill" to "从本地路径安装"
            )
        ),
        CliCommand(
            section = CliSection.PUBLISH,
            command = "android skills publish <path>",
            description = "Publish a local Skill to the configured registry.",
            descriptionCn = "将本地 Skill 发布到已配置的 registry。",
            examples = listOf(
                "android skills publish ./room-migration-skill" to "Publish skill from path",
                "android skills publish --registry my-private-registry" to "Publish to private registry"
            ),
            examplesCn = listOf(
                "android skills publish ./room-migration-skill" to "从路径发布 skill",
                "android skills publish --registry my-private-registry" to "发布到私有 registry"
            )
        ),
        CliCommand(
            section = CliSection.UNINSTALL,
            command = "android skills uninstall <skill-name>",
            description = "Uninstall a locally installed Skill.",
            descriptionCn = "卸载本地已安装的 Skill。",
            examples = listOf(
                "android skills uninstall room-migration" to "Uninstall Room Migration skill",
                "android skills uninstall --all" to "Uninstall all skills"
            ),
            examplesCn = listOf(
                "android skills uninstall room-migration" to "卸载 Room Migration skill",
                "android skills uninstall --all" to "卸载所有 skills"
            )
        ),
        CliCommand(
            section = CliSection.CONFIG,
            command = "android skills config",
            description = "Configure private Skill registry and CLI settings.",
            descriptionCn = "配置私有 Skill registry 和 CLI 设置。",
            examples = listOf(
                "android skills config --registry-url https://my-registry.example.com" to "Set private registry URL",
                "android skills config --editor vscode" to "Set preferred editor for skill preview"
            ),
            examplesCn = listOf(
                "android skills config --registry-url https://my-registry.example.com" to "设置私有 registry URL",
                "android skills config --editor vscode" to "设置 skill 预览的首选编辑器"
            )
        )
    )

    private fun getSampleCiSteps(): List<CiStep> = listOf(
        CiStep(
            index = 0,
            title = "Add Gradle Plugin",
            titleCn = "添加 Gradle 插件",
            description = "Add the Android Agent Skills CI plugin to your build configuration.",
            descriptionCn = "将 Android Agent Skills CI 插件添加到构建配置中。",
            codeSnippet = """// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// build.gradle.kts (project level)
plugins {
    id("com.android.agent-skills-ci") version "1.0.0" apply false
}""",
            codeSnippetLanguage = "kotlin"
        ),
        CiStep(
            index = 1,
            title = "Apply Plugin to App Module",
            titleCn = "在 App 模块应用插件",
            description = "Apply the CI plugin to your app module's build.gradle.kts.",
            descriptionCn = "在 app 模块的 build.gradle.kts 中应用 CI 插件。",
            codeSnippet = """// app/build.gradle.kts
plugins {
    id("com.android.agent-skills-ci")
}

// agentSkillsConfig {
//     skillsDir = file(".skills/")
//     strictMode = true  // Fail build on validation errors
// }""",
            codeSnippetLanguage = "kotlin"
        ),
        CiStep(
            index = 2,
            title = "Create .skills/ Directory",
            titleCn = "创建 .skills/ 目录",
            description = "Place your Skill files (.md) in the .skills/ directory at project root.",
            descriptionCn = "将 Skill 文件（.md）放在项目根目录的 .skills/ 目录中。",
            codeSnippet = """# Project structure
my-android-project/
├── .skills/
│   ├── room-migration.md
│   ├── compose-testing.md
│   └── agp-upgrade.md
├── app/
│   └── build.gradle.kts
└── build.gradle.kts""",
            codeSnippetLanguage = "text"
        ),
        CiStep(
            index = 3,
            title = "Run CI Validation Task",
            titleCn = "运行 CI 验证任务",
            description = "The plugin automatically runs validation during the build. You can also run it manually.",
            descriptionCn = "插件会在构建期间自动运行验证。也可以手动运行。",
            codeSnippet = """# Run validation manually
./gradlew validateAgentSkills

# Run with detailed report
./gradlew validateAgentSkills --report

# CI configuration example (GitHub Actions)
- name: Validate Agent Skills
  run: ./gradlew validateAgentSkills --strict""",
            codeSnippetLanguage = "text"
        ),
        CiStep(
            index = 4,
            title = "Configure Strict Mode",
            titleCn = "配置严格模式",
            description = "Enable strict mode to fail builds when Skills have validation errors.",
            descriptionCn = "启用严格模式，当 Skills 有验证错误时使构建失败。",
            codeSnippet = """// gradle.properties
# Enable strict mode — blocks CI on Skill validation errors
agent.skills.ci.strict=true

# Custom skills directory (default: .skills/)
agent.skills.ci.directory=.skills/

# Allowed Skill categories (comma-separated)
agent.skills.ci.allowedCategories=ROOM,COMPOSE,TESTING,AGP""",
            codeSnippetLanguage = "text"
        )
    )

    private fun getSampleKbResults(query: String): List<KbResult> {
        val all = listOf(
            KbResult(
                id = "kb-1",
                title = "Android Skills — Agent Skills Specification",
                titleCn = "Android Skills — Agent Skills 规范",
                snippet = "Android Skills are markdown files stored in .skills/ or .agent/skills/ directories that provide on-demand expertise to AI coding agents.",
                snippetCn = "Android Skills 是存储在 .skills/ 或 .agent/skills/ 目录中的 Markdown 文件，为 AI 编码 Agent 提供按需专业知识。",
                url = "https://developer.android.com/tools/agents/skills",
                relevance = 0.95f
            ),
            KbResult(
                id = "kb-2",
                title = "Android CLI Reference",
                titleCn = "Android CLI 参考文档",
                snippet = "The android CLI provides commands for managing Android projects, SDK components, and Skills in a terminal environment.",
                snippetCn = "android CLI 提供在终端环境中管理 Android 项目、SDK 组件和 Skills 的命令。",
                url = "https://developer.android.com/tools/cli/android-cli",
                relevance = 0.90f
            ),
            KbResult(
                id = "kb-3",
                title = "Android Studio Agent Mode",
                titleCn = "Android Studio Agent Mode",
                snippet = "Agent Mode in Android Studio Panda 3+ allows agents to use Skills for specialized Android development tasks.",
                snippetCn = "Android Studio Panda 3+ 中的 Agent Mode 允许 Agent 使用 Skills 处理专业 Android 开发任务。",
                url = "https://developer.android.com/studio/gemini/skills",
                relevance = 0.88f
            ),
            KbResult(
                id = "kb-4",
                title = "Android Knowledge Base API",
                titleCn = "Android 知识库 API",
                snippet = "The Knowledge Base API provides grounding for agent responses with the latest Android development best practices and documentation.",
                snippetCn = "知识库 API 通过最新的 Android 开发最佳实践和文档为 Agent 响应提供基础支持。",
                url = "https://developer.android.com/static/tools/knowledge-base-api/",
                relevance = 0.85f
            )
        )
        if (query.isBlank()) return all
        return all.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.titleCn.contains(query, ignoreCase = true) ||
            it.snippet.contains(query, ignoreCase = true)
        }.sortedByDescending { it.relevance }
    }

    private fun getSampleSharedSkills(): List<SharedSkill> = listOf(
        SharedSkill(
            id = "share-1",
            name = "Room Migration Master",
            nameCn = "Room 迁移大师",
            description = "The ultimate guide for Room database migration across all Android versions.",
            descriptionCn = "覆盖所有 Android 版本的 Room 数据库迁移终极指南。",
            author = "android-dev-team",
            category = SkillCategory.ROOM,
            downloadCount = 12847,
            rating = 4.9f,
            tags = listOf("Room", "Migration", "Database")
        ),
        SharedSkill(
            id = "share-2",
            name = "Compose Testing v2 Champion",
            nameCn = "Compose Testing v2 冠军",
            description = "Master Compose UI testing with the new v2 Testing API and StandardTestDispatcher.",
            descriptionCn = "掌握使用新版 v2 Testing API 和 StandardTestDispatcher 的 Compose UI 测试。",
            author = "jetpack-team",
            category = SkillCategory.TESTING,
            downloadCount = 8934,
            rating = 4.7f,
            tags = listOf("Compose", "Testing", "v2")
        ),
        SharedSkill(
            id = "share-3",
            name = "AGP Upgrade Guardian",
            nameCn = "AGP 升级守护者",
            description = "Safely navigate AGP upgrades with comprehensive compatibility checks.",
            descriptionCn = "通过全面的兼容性检查安全地完成 AGP 升级。",
            author = "gradle-expert",
            category = SkillCategory.AGP,
            downloadCount = 6721,
            rating = 4.6f,
            tags = listOf("AGP", "Gradle", "Upgrade")
        ),
        SharedSkill(
            id = "share-4",
            name = "KSP2 Pioneer",
            nameCn = "KSP2 先驱",
            description = "Everything you need for a smooth KSP1 to KSP2 migration journey.",
            descriptionCn = "顺利完成 KSP1 到 KSP2 迁移所需的一切。",
            author = "kotlin-core",
            category = SkillCategory.KSP,
            downloadCount = 4523,
            rating = 4.8f,
            tags = listOf("KSP", "Kotlin", "Migration")
        ),
        SharedSkill(
            id = "share-5",
            name = "Compose Layout Wizard",
            nameCn = "Compose 布局巫师",
            description = "Debug and optimize any Jetpack Compose layout with expert techniques.",
            descriptionCn = "使用专家技术调试和优化任何 Jetpack Compose 布局。",
            author = "compose-master",
            category = SkillCategory.COMPOSE,
            downloadCount = 9876,
            rating = 4.5f,
            tags = listOf("Compose", "Layout", "Debug")
        )
    )

    private fun getSampleValidationResults(): List<ValidationResult> = listOf(
        ValidationResult(
            id = "v-1",
            file = ".skills/room-migration.md",
            line = 8,
            severity = ValidationSeverity.ERROR,
            category = ValidationCategory.TRIGGER,
            message = "Trigger section is missing required fields: 'on' and 'when'.",
            messageCn = "Trigger 区域缺少必需字段：'on' 和 'when'。",
            fixSuggestion = "Add trigger fields:\non: [file pattern or directory]\nwhen: [specific task conditions]",
            fixSuggestionCn = "添加触发器字段：\non: [文件模式或目录]\nwhen: [具体任务条件]"
        ),
        ValidationResult(
            id = "v-2",
            file = ".skills/compose-testing.md",
            line = 15,
            severity = ValidationSeverity.WARNING,
            category = ValidationCategory.EXAMPLES,
            message = "Example code block is missing language identifier.",
            messageCn = "示例代码块缺少语言标识符。",
            fixSuggestion = "Add language identifier after code fence: ```kotlin",
            fixSuggestionCn = "在代码围栏后添加语言标识符：```kotlin"
        ),
        ValidationResult(
            id = "v-3",
            file = ".skills/agp-upgrade.md",
            line = 22,
            severity = ValidationSeverity.INFO,
            category = ValidationCategory.OUTPUTS,
            message = "Outputs section could be more specific. Consider listing expected file paths.",
            messageCn = "Outputs 区域可以更具体。建议列出预期的文件路径。",
            fixSuggestion = "outputs:\n  - path: src/main/kotlin/.../UseCase.kt\n    description: Generated use case file",
            fixSuggestionCn = "outputs:\n  - path: src/main/kotlin/.../UseCase.kt\n    description: 生成的 use case 文件"
        ),
        ValidationResult(
            id = "v-4",
            file = ".skills/ksp2-guide.md",
            line = 3,
            severity = ValidationSeverity.ERROR,
            category = ValidationCategory.FRONTMATTER,
            message = "Frontmatter is missing required field: 'version'.",
            messageCn = "元数据区缺少必需字段：'version'。",
            fixSuggestion = """Add frontmatter at the top of the file:
---
version: 1.0
author: your-name
lastUpdated: 2026-05-01
---""",
            fixSuggestionCn = """在文件顶部添加元数据：
---
version: 1.0
author: 你的名字
lastUpdated: 2026-05-01
---"""
        ),
        ValidationResult(
            id = "v-5",
            file = ".skills/layout-debug.md",
            line = 31,
            severity = ValidationSeverity.WARNING,
            category = ValidationCategory.INSTRUCTIONS,
            message = "Instructions section has empty step at index 3.",
            messageCn = "Instructions 区域第 3 步为空。",
            fixSuggestion = "Remove empty step or add content to step 3.",
            fixSuggestionCn = "删除空步骤或为第 3 步添加内容。"
        )
    )
}
