package com.mvi.kenny.feature.androidclitoolkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================
 * AndroidCliToolkitViewModel — Android CLI 1.0 AI Agent 开发集成工具包 ViewModel
 * ============================================================
 *
 * MVI Architecture: ViewModel handles Intent → executes business logic → updates State.
 *
 * 5 Tab Layout:
 * - Tab 1: 命令参考 (Command Reference)
 * - Tab 2: Agent集成 (Agent Integration)
 * - Tab 3: 语义重构 (Semantic Refactoring)
 * - Tab 4: Preview渲染 (Preview Rendering)
 * - Tab 5: Journeys测试 (Journeys Testing)
 */
class AndroidCliToolkitViewModel : ViewModel() {

    // State / UI 状态
    private val _state = MutableStateFlow(AndroidCliToolkitState.Initial)
    val state: StateFlow<AndroidCliToolkitState> = _state.asStateFlow()

    // Effect / 副作用通道
    private val _effect = Channel<AndroidCliToolkitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadInitialData()
    }

    /**
     * Process user intention / 处理用户意图
     */
    fun sendIntent(intent: AndroidCliToolkitIntent) {
        when (intent) {
            is AndroidCliToolkitIntent.SelectTab -> handleSelectTab(intent.index)
            is AndroidCliToolkitIntent.ExpandCommand -> handleExpandCommand(intent.commandId)
            is AndroidCliToolkitIntent.SelectVersion -> handleSelectVersion(intent.version)
            is AndroidCliToolkitIntent.Search -> handleSearch(intent.query)
            is AndroidCliToolkitIntent.CopyCodeBlock -> handleCopyCodeBlock(intent.codeBlockId, intent.content)
            is AndroidCliToolkitIntent.ClearCopyFeedback -> handleClearCopyFeedback()
            is AndroidCliToolkitIntent.NavigateToInstallGuide -> handleNavigateToInstallGuide(intent.platform)
        }
    }

    // Tab Navigation / Tab 切换
    private fun handleSelectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    // Command Expansion / 命令展开
    private fun handleExpandCommand(commandId: String) {
        _state.update { currentState ->
            currentState.copy(
                expandedCommandId = if (currentState.expandedCommandId == commandId) null else commandId
            )
        }
    }

    // Version Selection / 版本选择
    private fun handleSelectVersion(version: String) {
        _state.update { it.copy(selectedVersion = version, isLoading = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            _state.update { it.copy(isLoading = false) }
        }
    }

    // Search / 搜索
    private fun handleSearch(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    // Copy Code Block / 复制代码块
    private fun handleCopyCodeBlock(codeBlockId: String, content: String) {
        _state.update { it.copy(copiedSnippetId = codeBlockId, copiedCodeBlock = content) }
        viewModelScope.launch {
            _effect.send(AndroidCliToolkitEffect.CopyToClipboard(content))
            _effect.send(AndroidCliToolkitEffect.ShowCopiedToast("已复制到剪贴板 / Copied to clipboard"))
        }
    }

    private fun handleClearCopyFeedback() {
        _state.update { it.copy(copiedSnippetId = null, copiedCodeBlock = "") }
    }

    // Navigate to Install Guide / 导航到安装指南
    private fun handleNavigateToInstallGuide(platform: String) {
        viewModelScope.launch {
            _effect.send(AndroidCliToolkitEffect.NavigateToInstallGuide(platform))
        }
    }

    // Load Initial Data / 加载初始数据
    private fun loadInitialData() {
        _state.update { it.copy(isLoading = true) }

        val commands = getCliCommands()
        val agentConfigs = getAgentConfigs()
        val semanticRefactors = getSemanticRefactors()
        val previewRenderItems = getPreviewRenderItems()
        val journeysTestItems = getJourneysTestItems()

        _state.update {
            it.copy(
                isLoading = false,
                commands = commands,
                agentConfigs = agentConfigs,
                semanticRefactors = semanticRefactors,
                previewRenderItems = previewRenderItems,
                journeysTestItems = journeysTestItems
            )
        }
    }

    // ============================================================
    // CLI Commands Data / CLI 命令数据
    // ============================================================
    private fun getCliCommands(): List<CliCommand> = listOf(
        CliCommand(
            id = "cmd-studio-init",
            command = "android studio init",
            description = "Initialize a new Android project with AI Agent context awareness",
            descriptionCn = "初始化新的 Android 项目，带 AI Agent 上下文感知",
            category = "Project",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android studio init --name MyApp --package com.example.myapp",
            exampleOutput = "✓ Project created: MyApp\n✓ AI context indexed\n✓ Ready for Agent Mode",
            relatedCommands = listOf("android studio sync", "android studio build")
        ),
        CliCommand(
            id = "cmd-studio-sync",
            command = "android studio sync",
            description = "Synchronize project with AI context index",
            descriptionCn = "同步项目与 AI 上下文索引",
            category = "Project",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android studio sync --force",
            exampleOutput = "✓ Indexed 1,234 files\n✓ Context size: 45MB\n✓ Ready",
            relatedCommands = listOf("android studio init", "android studio build")
        ),
        CliCommand(
            id = "cmd-studio-build",
            command = "android studio build",
            description = "Build Android project with AI-optimized compilation",
            descriptionCn = "使用 AI 优化编译构建 Android 项目",
            category = "Build",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android studio build --variant debug --agent claude",
            exampleOutput = "BUILD SUCCESSFUL\nAgent-assisted build completed in 45s",
            relatedCommands = listOf("android studio sync", "android studio test")
        ),
        CliCommand(
            id = "cmd-studio-test",
            command = "android studio test",
            description = "Run unit and UI tests with AI test generation",
            descriptionCn = "使用 AI 测试生成运行单元测试和 UI 测试",
            category = "Test",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android studio test --type ui --agent gpt",
            exampleOutput = "✓ 24 tests passed\n✓ AI-generated: 8 test cases",
            relatedCommands = listOf("android studio build", "android studio preview")
        ),
        CliCommand(
            id = "cmd-studio-preview",
            command = "android studio preview",
            description = "Render Compose Preview with AI-assisted validation",
            descriptionCn = "使用 AI 辅助验证渲染 Compose Preview",
            category = "Preview",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android studio preview --component MyScreen --export-png",
            exampleOutput = "✓ Preview rendered: MyScreen.png\n✓ AI validation: PASS",
            relatedCommands = listOf("android studio build", "android studio journeys")
        ),
        CliCommand(
            id = "cmd-studio-journeys",
            command = "android studio journeys",
            description = "Run Journeys UI testing framework",
            descriptionCn = "运行 Journeys UI 测试框架",
            category = "Test",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android studio journeys --spec login.spec --record",
            exampleOutput = "✓ 12 journeys executed\n✓ Recording: journeys.mov",
            relatedCommands = listOf("android studio test", "android studio preview")
        ),
        CliCommand(
            id = "cmd-skill-list",
            command = "android skill list",
            description = "List all available Android Skills in the ecosystem",
            descriptionCn = "列出生态系统中所有可用的 Android Skills",
            category = "Skills",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android skill list --category compose --verified",
            exampleOutput = "Available Skills (12):\n  android-permissions@1.2.0  [verified]\n  compose-best-practices@1.0  [official]",
            relatedCommands = listOf("android skill install", "android skill create")
        ),
        CliCommand(
            id = "cmd-skill-install",
            command = "android skill install",
            description = "Install an Android Skill for AI Agent use",
            descriptionCn = "为 AI Agent 安装 Android Skill",
            category = "Skills",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android skill install android-permissions@1.2.0",
            exampleOutput = "✓ Skill installed: android-permissions@1.2.0\n✓ 3 rules loaded",
            relatedCommands = listOf("android skill list", "android skill uninstall")
        ),
        CliCommand(
            id = "cmd-semantic-resolve",
            command = "android semantic resolve",
            description = "Resolve symbols using Semantic Symbol Resolution engine",
            descriptionCn = "使用语义符号解析引擎解析符号",
            category = "Semantic",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android semantic resolve --symbol R.string.app_name --context",
            exampleOutput = "Resolved: R.string.app_name → res/values/strings.xml:42",
            relatedCommands = listOf("android semantic diff", "android studio build")
        ),
        CliCommand(
            id = "cmd-semantic-diff",
            command = "android semantic diff",
            description = "Show semantic diff between two Android project versions",
            descriptionCn = "显示两个 Android 项目版本之间的语义差异",
            category = "Semantic",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android semantic diff --from v1.0 --to v2.0",
            exampleOutput = "API Changes: 3 breaking, 5 additive\nSymbol changes: 12 renamed",
            relatedCommands = listOf("android semantic resolve")
        ),
        CliCommand(
            id = "cmd-agent-status",
            command = "android agent status",
            description = "Check AI Agent connection and context status",
            descriptionCn = "检查 AI Agent 连接和上下文状态",
            category = "Agent",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android agent status --agent claude",
            exampleOutput = "Agent: Claude Code\nContext: 200K tokens\nConnected: ✓",
            relatedCommands = listOf("android agent connect", "android agent disconnect")
        ),
        CliCommand(
            id = "cmd-lint",
            command = "android lint",
            description = "Run Android Lint with AI-powered rule suggestions",
            descriptionCn = "运行带 AI 规则建议的 Android Lint",
            category = "Lint",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android lint --category security --ai-suggest",
            exampleOutput = "Lint completed: 3 warnings, 1 error\nAI Suggestions: 2 available",
            relatedCommands = listOf("android r8", "android studio build")
        ),
        CliCommand(
            id = "cmd-r8",
            command = "android r8",
            description = "Run R8/D8 shrinking with AI optimization",
            descriptionCn = "使用 AI 优化运行 R8/D8 压缩",
            category = "Optimize",
            versions = listOf("1.0.0", "1.0.x"),
            exampleUsage = "android r8 --mode release --ai-optimize",
            exampleOutput = "APK size: 12.4MB (↓23%)\nAI optimization: 4 suggestions applied",
            relatedCommands = listOf("android lint", "android studio build")
        )
    )

    // ============================================================
    // Agent Configs Data / Agent 配置数据
    // ============================================================
    private fun getAgentConfigs(): List<AgentConfig> = listOf(
        AgentConfig(
            id = "agent-claude-code",
            agentType = AgentType.CLAUDE_CODE,
            title = "Claude Code Integration",
            titleCn = "Claude Code 集成",
            installCommand = "npm install -g @anthropic/claude-code",
            configSteps = listOf(
                "Install Claude Code CLI" to "安装 Claude Code CLI",
                "Run `claude --login` to authenticate" to "运行 `claude --login` 进行认证",
                "Create .claude/commands/ directory" to "创建 .claude/commands/ 目录",
                "Add Android Skills to project" to "将 Android Skills 添加到项目"
            ),
            configExample = "# .claude/commands/android-build.md\n# Android Build Command for Claude Code\n\nYou are an Android development expert. When asked to build:\n\n1. Run: `android studio sync --force`\n2. Then: `android studio build --variant debug`\n3. Report build status and APK location",
            gradleSyncNote = "Gradle sync is automatic via `android studio sync` — no manual sync needed",
            gradleSyncNoteCn = "Gradle 同步通过 `android studio sync` 自动完成 — 无需手动同步",
            url = "https://docs.anthropic.com/en/docs/claude-code"
        ),
        AgentConfig(
            id = "agent-codex",
            agentType = AgentType.CODEX,
            title = "Codex (OpenAI) Integration",
            titleCn = "Codex (OpenAI) 集成",
            installCommand = "pip install openai-codex",
            configSteps = listOf(
                "Install Codex CLI" to "安装 Codex CLI",
                "Set OPENAI_API_KEY environment variable" to "设置 OPENAI_API_KEY 环境变量",
                "Configure codex.json for Android projects" to "配置 codex.json 用于 Android 项目",
                "Enable Android skill set" to "启用 Android skill set"
            ),
            configExample = """{
  "model": "gpt-4o",
  "skills": ["android-permissions", "compose-best-practices"],
  "context": {
    "android_sdk": "/Users/user/Library/Android/sdk",
    "project_type": "jetpack-compose"
  }
}""",
            gradleSyncNote = "Run `gradle sync` manually or via Android Studio after API changes",
            gradleSyncNoteCn = "API 变更后手动运行 `gradle sync` 或通过 Android Studio",
            url = "https://platform.openai.com/docs/codex"
        ),
        AgentConfig(
            id = "agent-gpt-cli",
            agentType = AgentType.GPT,
            title = "GPT CLI Integration",
            titleCn = "GPT CLI 集成",
            installCommand = "brew install openai/tap/gpt-cli",
            configSteps = listOf(
                "Install GPT CLI via Homebrew" to "通过 Homebrew 安装 GPT CLI",
                "Authenticate with `gpt auth`" to "使用 `gpt auth` 认证",
                "Link to Android project" to "链接到 Android 项目",
                "Load Android Skills" to "加载 Android Skills"
            ),
            configExample = "model: gpt-4o\nandroid:\n  skills_dir: ./android-skills\n  sdk_path: \${'$'}{ANDROID_HOME}\n  build_command: ./gradlew assembleDebug",
            gradleSyncNote = "GPT CLI uses Gradle wrapper directly — no Studio sync required",
            gradleSyncNoteCn = "GPT CLI 直接使用 Gradle wrapper — 无需 Studio 同步",
            url = "https://github.com/openai/gpt-cli"
        )
    )

    // ============================================================
    // Semantic Refactoring Data / 语义重构数据
    // ============================================================
    private fun getSemanticRefactors(): List<SemanticRefactorItem> = listOf(
        SemanticRefactorItem(
            id = "semantic-1",
            title = "Symbol Resolution in Large Projects",
            titleCn = "大型项目中的符号解析",
            description = "How Semantic Symbol Resolution handles R class and resource references in projects with 1000+ files",
            descriptionCn = "语义符号解析如何在包含 1000+ 文件的项目中处理 R 类和资源引用",
            codeExample = "# Semantic resolution of R.drawable.icon\nandroid semantic resolve --symbol R.drawable.icon\n\n# Output:\n# Resolved: R.drawable.icon\n# File: app/src/main/res/drawable/icon.xml\n# Size: 48x48dp (mdpi)\n# References: 12 call sites indexed\n# Context hash: a3f8b2c1",
            limitations = "Symbol resolution accuracy degrades when file is simultaneously modified by multiple agents",
            limitationsCn = "当文件被多个 agent 同时修改时，符号解析准确性会下降",
            bestPractices = listOf(
                "Run `android studio sync` before refactoring",
                "Lock files during AI-assisted refactoring",
                "Use `android semantic diff` to validate changes"
            ),
            bestPracticesCn = listOf(
                "重构前运行 `android studio sync`",
                "AI 辅助重构期间锁定文件",
                "使用 `android semantic diff` 验证变更"
            )
        ),
        SemanticRefactorItem(
            id = "semantic-2",
            title = "Cross-Module Symbol Resolution",
            titleCn = "跨模块符号解析",
            description = "Resolving symbols across app, library, and feature modules using Semantic Symbol Resolution",
            descriptionCn = "使用语义符号解析跨 app、library 和 feature 模块解析符号",
            codeExample = "# Resolve symbol in feature module\nandroid semantic resolve \\\n  --symbol com.example.library.utils.Helper \\\n  --modules app,feature,library\n\n# Output:\n# Found in: :library module\n# Package: com.example.library.utils\n# Class: Helper (internal)\n# Visibility: internal",
            limitations = "Cannot resolve dynamic proxy classes or generated code (e.g., DataBinding)",
            limitationsCn = "无法解析动态代理类或生成代码（如 DataBinding）",
            bestPractices = listOf(
                "Index all modules before cross-module resolution",
                "Use stable module boundaries",
                "Avoid circular module dependencies"
            ),
            bestPracticesCn = listOf(
                "跨模块解析前索引所有模块",
                "使用稳定的模块边界",
                "避免循环模块依赖"
            )
        ),
        SemanticRefactorItem(
            id = "semantic-3",
            title = "Lint/R8 Integration",
            titleCn = "Lint/R8 集成",
            description = "Running Lint and R8 with AI-powered semantic analysis to catch breaking changes",
            descriptionCn = "运行带 AI 语义分析的 Lint 和 R8 以捕获破坏性变更",
            codeExample = "# Run Lint with AI semantic analysis\nandroid lint --category breaking --ai-analyze\n\n# Run R8 with AI optimization\nandroid r8 --mode release --ai-optimize \\\n  --consumer-rules rules/proguard.txt",
            limitations = "AI optimization suggestions require internet for LLM inference",
            limitationsCn = "AI 优化建议需要互联网进行 LLM 推理",
            bestPractices = listOf(
                "Run Lint in CI before every merge",
                "Review AI suggestions before applying",
                "Keep R8 optimization rules in version control"
            ),
            bestPracticesCn = listOf(
                "每次合并前在 CI 中运行 Lint",
                "应用前审查 AI 建议",
                "将 R8 优化规则保存在版本控制中"
            )
        )
    )

    // ============================================================
    // Preview Rendering Data / Preview 渲染数据
    // ============================================================
    private fun getPreviewRenderItems(): List<PreviewRenderItem> = listOf(
        PreviewRenderItem(
            id = "preview-1",
            title = "CI Preview Rendering Pipeline",
            titleCn = "CI Preview 渲染流水线",
            description = "How to render Compose Previews in CI/CD using `android studio preview` command",
            descriptionCn = "如何在 CI/CD 中使用 `android studio preview` 命令渲染 Compose Previews",
            ciCommand = "android studio preview --component MyScreen --export-png --ci",
            codeExample = "# GitHub Actions workflow for Preview rendering\n- name: Render Compose Previews\n  run: |\n    for component in MyScreen YourScreen HisScreen; do\n      android studio preview \\\n        --component \$component \\\n        --export-png \\\n        --output ./previews\n    done\n\n- name: Upload Preview Artifacts\n  uses: actions/upload-artifact@v4\n  with:\n    name: compose-previews\n    path: previews/*.png",
            imageNote = "Preview images may be large (up to 4K). Use lossless PNG for quality, JPEG for thumbnails.",
            imageNoteCn = "Preview 图片可能很大（最高 4K）。质量要求用无损 PNG，缩略图用 JPEG。"
        ),
        PreviewRenderItem(
            id = "preview-2",
            title = "Preview Rendering in Docker",
            titleCn = "Docker 中 Preview 渲染",
            description = "Running `android studio preview` in a Docker container for consistent CI environments",
            descriptionCn = "在 Docker 容器中运行 `android studio preview` 以获得一致的 CI 环境",
            ciCommand = "docker run --rm android-ci-preview android studio preview --component MyScreen",
            codeExample = "# Dockerfile for Android CLI Preview rendering\nFROM ubuntu:22.04\n\n# Install Android CLI and dependencies\nRUN apt-get update && apt-get install -y \\\n    openjdk-17-jdk wget unzip \\\n    && wget -q https://dl.google.com/android/cli/android-cli-1.0.0.tgz \\\n    && tar -xzf android-cli-1.0.0.tgz -C /usr/local \\\n    && rm android-cli-1.0.0.tgz\n\nENV ANDROID_HOME=/opt/android-sdk\nENV PATH=\$PATH:\$ANDROID_HOME/bin\n\nWORKDIR /workspace\nCOPY . .\nRUN android studio sync --force\n\nENTRYPOINT [\"android\", \"studio\", \"preview\"]",
            imageNote = "Docker rendering requires Xvfb for headless display emulation.",
            imageNoteCn = "Docker 渲染需要 Xvfb 来模拟无头显示。"
        ),
        PreviewRenderItem(
            id = "preview-3",
            title = "Preview with Multiple Themes",
            titleCn = "多主题 Preview 渲染",
            description = "Render the same Preview component across Light, Dark, and Dynamic Color themes",
            descriptionCn = "跨 Light、Dark 和 Dynamic Color 主题渲染相同的 Preview 组件",
            ciCommand = "android studio preview --component MyScreen --theme all --export-png",
            codeExample = "# Render all theme variants\nfor theme in light dark dynamic; do\n  android studio preview \\\n    --component MyScreen \\\n    --theme \$theme \\\n    --export-png \\\n    --output ./previews/\$theme\ndone",
            imageNote = "Dynamic Color is only available on Pixel devices running Android 12+.",
            imageNoteCn = "Dynamic Color 仅在运行 Android 12+ 的 Pixel 设备上可用。"
        )
    )

    // ============================================================
    // Journeys Testing Data / Journeys 测试数据
    // ============================================================
    private fun getJourneysTestItems(): List<JourneysTestItem> = listOf(
        JourneysTestItem(
            id = "journeys-1",
            title = "Basic Journeys Test",
            titleCn = "基础 Journeys 测试",
            description = "Writing your first Journeys UI test with the Journeys testing framework",
            descriptionCn = "使用 Journeys 测试框架编写你的第一个 Journeys UI 测试",
            testCode = """@JourneysTest
class LoginJourneysTest {

    @Test
    fun `login journey - success`() {
        // Define the journey / 定义 journey
        journey("User logs in successfully") {
            startAt(LoginScreen)

            // Step 1: Enter credentials / 步骤1: 输入凭据
            step("Enter email") {
                onView(withId(R.id.email_input))
                    .perform(typeText("user@example.com"))
            }

            step("Enter password") {
                onView(withId(R.id.password_input))
                    .perform(typeText("password123"))
            }

            // Step 2: Submit / 步骤2: 提交
            step("Tap login button") {
                onView(withId(R.id.login_button))
                    .perform(click())
            }

            // Step 3: Verify / 步骤3: 验证
            verify("Home screen is shown") {
                onView(withId(R.id.home_screen))
                    .isDisplayed()
            }
        }
    }
}""",
            testFramework = "Journeys Testing Framework",
            frameworkVersion = "1.0.0"
        ),
        JourneysTestItem(
            id = "journeys-2",
            title = "Journeys Recording Mode",
            titleCn = "Journeys 录制模式",
            description = "Using `android studio journeys --record` to generate test code from user interactions",
            descriptionCn = "使用 `android studio journeys --record` 从用户交互生成测试代码",
            testCode = """# Start recording a new journey
android studio journeys --record \
  --spec login.spec \
  --output ./test/journeys

# This generates:
# login.spec (journey definition)
# login-journey.kt (test code)
# login-recording.mov (video recording)

# Run the recorded journey
android studio journeys --spec login.spec --playback""",
            testFramework = "Journeys Testing Framework",
            frameworkVersion = "1.0.0"
        ),
        JourneysTestItem(
            id = "journeys-3",
            title = "Journeys in CI/CD",
            titleCn = "CI/CD 中的 Journeys 测试",
            description = "Running Journeys tests in GitHub Actions with device farm integration",
            descriptionCn = "在 GitHub Actions 中使用设备场集成运行 Journeys 测试",
            testCode = """# .github/workflows/journeys-test.yml
name: Journeys UI Tests

on: [push, pull_request]

jobs:
  journeys:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Android CLI
        run: |
          wget -q https://dl.google.com/android/cli/android-cli-1.0.0.tgz
          tar -xzf android-cli-1.0.0.tgz

      - name: Run Journeys Tests
        run: |
          ./android-cli/android studio journeys \
            --spec ./test/journeys/**/*.spec \
            --device cloud \
            --provider firebase-test-lab

      - name: Upload Results
        uses: actions/upload-artifact@v4
        with:
          name: journeys-results
          path: ./journeys-results/""",
            testFramework = "Journeys Testing Framework",
            frameworkVersion = "1.0.0"
        )
    )
}
