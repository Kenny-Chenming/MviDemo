package com.mvi.kenny.feature.androidagenttoolkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * ============================================================
 * AndroidAgentToolkitViewModel — Android CLI/Skills/KB AI Agent 工具链 ViewModel
 * ============================================================
 * PRD-126 | Android CLI + Android Skills + Android Knowledge Base AI Agent 开发工具包
 *
 * MVI 架构实现：
 * — StateFlow 管理页面状态，UI 订阅此流获取最新状态
 * — Intent sealed interface 接收用户意图，switch 分发处理
 * — Effect Channel 传递一次性副作用（Toast / Clipboard 等）
 *
 * @see AndroidAgentToolkitContract
 * @see AndroidAgentToolkitScreen
 */
class AndroidAgentToolkitViewModel : ViewModel() {

    // ================================================================
    // State / 页面状态
    // ================================================================
    private val _state = MutableStateFlow(AndroidAgentToolkitState.Initial)
    val state: StateFlow<AndroidAgentToolkitState> = _state.asStateFlow()

    // ================================================================
    // Effect / 副作用
    // ================================================================
    private val _effect = MutableSharedFlow<AndroidAgentToolkitEffect>()
    val effect = _effect.asSharedFlow()

    // ================================================================
    // Internal state / 内部状态
    // ================================================================
    private var cliProcess: Process? = null  // Running CLI process / 运行中的 CLI 进程
    private var kbSyncJob: Job? = null       // KB sync job / 知识库同步任务

    // ================================================================
    // Demo data / 演示数据
    // ================================================================
    private val demoSkills = listOf(
        SkillInfo(
            id = "skill-android-build",
            name = "android-build",
            version = "1.2.0",
            description = "Android 项目构建完整工作流，涵盖编译/签名/打包。",
            scenarios = listOf("CI/CD", "Build", "Signing"),
            cliVersion = ">=2.0",
            agpVersion = ">=8.0",
            isInstalled = true
        ),
        SkillInfo(
            id = "skill-compose-check",
            name = "compose-check",
            version = "2.0.0",
            description = "Jetpack Compose 代码质量检测，支持 Composable 最佳实践检查。",
            scenarios = listOf("Code Review", "Compose", "Quality"),
            cliVersion = ">=2.0",
            agpVersion = ">=8.2",
            isInstalled = true
        ),
        SkillInfo(
            id = "skill-kmp-setup",
            name = "kmp-setup",
            version = "1.0.0",
            description = "Kotlin Multiplatform 项目初始化模板，自动配置 iOS/Android/Desktop。",
            scenarios = listOf("KMP", "Setup", "Cross-platform"),
            cliVersion = ">=2.0",
            agpVersion = ">=8.3",
            isInstalled = false
        ),
        SkillInfo(
            id = "skill-perf-profile",
            name = "perf-profile",
            version = "1.1.0",
            description = "Android 性能剖析集成工具，连接 Android Studio Profiler。",
            scenarios = listOf("Performance", "Profiler", "Optimization"),
            cliVersion = ">=2.0",
            agpVersion = ">=8.1",
            isInstalled = false
        )
    )

    private val demoKBEntries = listOf(
        KBEntry(
            id = "kb-001",
            title = "Android CLI Quick Start Guide",
            category = "Tools",
            androidVersion = "17",
            snippet = "Android CLI provides sdkmanager, avdmanager, and apksigner commands for AI agents...",
            relevanceScore = 0.95f
        ),
        KBEntry(
            id = "kb-002",
            title = "Android Skills Specification v2.0",
            category = "Tools",
            androidVersion = "17",
            snippet = "Skills define how AI agents interact with Android development tools...",
            relevanceScore = 0.92f
        ),
        KBEntry(
            id = "kb-003",
            title = "Compose Performance Best Practices",
            category = "Compose",
            androidVersion = "17",
            snippet = "Use remember and derivedStateOf to optimize recomposition in Jetpack Compose...",
            relevanceScore = 0.88f
        ),
        KBEntry(
            id = "kb-004",
            title = "Navigation 3 Migration Guide",
            category = "Navigation",
            androidVersion = "17",
            snippet = "Migrating from Nav2 to Nav3 requires updating navigate() calls...",
            relevanceScore = 0.85f
        )
    )

    private val demoPipelineYaml = """
# AI Agent Android CI/CD Pipeline
name: Android Agent Build

on:
  push:
    branches: [main, develop]
  pull_request:

jobs:
  agent-build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup Android CLI
        run: |
          # AI Agent setup steps
          echo "Setting up Android CLI environment"
      - name: Run AI Agent Build
        run: |
          # Agent executes build commands
          echo "AI Agent executing build workflow"
""".trimIndent()

    // ================================================================
    // Intent handler / Intent 处理器
    // ================================================================
    /**
     * 处理用户意图 / Process user intent
     *
     * @param intent 用户意图 / User intent
     */
    fun sendIntent(intent: AndroidAgentToolkitIntent) {
        when (intent) {
            // Tab Navigation / Tab 导航
            is AndroidAgentToolkitIntent.SelectTab -> handleSelectTab(intent.index)

            // CLI / CLI
            is AndroidAgentToolkitIntent.UpdateCommandInput -> handleUpdateCommandInput(intent.input)
            is AndroidAgentToolkitIntent.ExecuteCommand -> handleExecuteCommand()
            is AndroidAgentToolkitIntent.CancelCommand -> handleCancelCommand()
            is AndroidAgentToolkitIntent.ClearCommandHistory -> handleClearCommandHistory()

            // Skills / Skills
            is AndroidAgentToolkitIntent.UpdateSkillSearch -> handleUpdateSkillSearch(intent.query)
            is AndroidAgentToolkitIntent.SelectSkill -> handleSelectSkill(intent.skill)
            is AndroidAgentToolkitIntent.InstallSelectedSkill -> handleInstallSelectedSkill()
            is AndroidAgentToolkitIntent.UninstallSelectedSkill -> handleUninstallSelectedSkill()
            is AndroidAgentToolkitIntent.CreateNewSkill -> handleCreateNewSkill()

            // Knowledge Base / 知识库
            is AndroidAgentToolkitIntent.UpdateKBQuery -> handleUpdateKBQuery(intent.query)
            is AndroidAgentToolkitIntent.SyncKnowledgeBase -> handleSyncKnowledgeBase()
            is AndroidAgentToolkitIntent.ClearKBCache -> handleClearKBCache()
            is AndroidAgentToolkitIntent.DownloadKBSubset -> handleDownloadKBSubset(intent.categories)

            // CI/CD
            is AndroidAgentToolkitIntent.SelectPipelineTemplate -> handleSelectPipelineTemplate(intent.template)
            is AndroidAgentToolkitIntent.UpdatePipelineYaml -> handleUpdatePipelineYaml(intent.yaml)
            is AndroidAgentToolkitIntent.UpdateEnvVariable -> handleUpdateEnvVariable(intent.key, intent.value)
            is AndroidAgentToolkitIntent.RunPipelineDiagnostics -> handleRunPipelineDiagnostics()
            is AndroidAgentToolkitIntent.ExportPipelineConfig -> handleExportPipelineConfig()

            // Settings / 设置
            is AndroidAgentToolkitIntent.UpdateAIProvider -> handleUpdateAIProvider(intent.provider)
            is AndroidAgentToolkitIntent.UpdateOutputFormat -> handleUpdateOutputFormat(intent.format)
            is AndroidAgentToolkitIntent.UpdateLogLevel -> handleUpdateLogLevel(intent.level)

            // Dashboard / 仪表盘
            is AndroidAgentToolkitIntent.RefreshDashboard -> handleRefreshDashboard()
        }
    }

    // ================================================================
    // Tab Navigation / Tab 导航
    // ================================================================
    private fun handleSelectTab(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    // ================================================================
    // CLI Tab handlers / CLI Tab 处理器
    // ================================================================
    private fun handleUpdateCommandInput(input: String) {
        _state.value = _state.value.copy(commandInput = input)
    }

    private fun handleExecuteCommand() {
        val currentCommand = _state.value.commandInput.trim()
        if (currentCommand.isEmpty()) return

        val recordId = UUID.randomUUID().toString()
        val newRecord = CommandRecord(
            id = recordId,
            command = currentCommand,
            timestamp = System.currentTimeMillis(),
            status = CommandStatus.RUNNING
        )

        _state.value = _state.value.copy(
            isExecuting = true,
            commandHistory = _state.value.commandHistory + newRecord,
            commandInput = ""
        )

        viewModelScope.launch {
            // Simulate CLI execution / 模拟 CLI 执行
            // Note: In production, use ProcessBuilder for actual command execution
            // 注意：在生产环境中，使用 ProcessBuilder 执行实际命令
            delay(1500)

            val output = simulateCliOutput(currentCommand)
            val errorOutput = if (currentCommand.contains("invalid") || currentCommand.contains("error")) {
                "bash: $currentCommand: command not found"
            } else ""

            val finalStatus = if (errorOutput.isEmpty()) CommandStatus.SUCCESS else CommandStatus.FAILED

            val updatedHistory = _state.value.commandHistory.map { record ->
                if (record.id == recordId) {
                    record.copy(
                        status = finalStatus,
                        output = output,
                        errorOutput = errorOutput
                    )
                } else record
            }

            _state.value = _state.value.copy(
                isExecuting = false,
                commandHistory = updatedHistory
            )

            _effect.emit(AndroidAgentToolkitEffect.LogAgentCommand(currentCommand, output))
        }
    }

    /**
     * Simulate CLI command output / 模拟 CLI 命令输出
     *
     * @param command User-entered command / 用户输入的命令
     * @return Simulated stdout / 模拟的标准输出
     */
    private fun simulateCliOutput(command: String): String {
        return when {
            command.startsWith("sdkmanager") -> """
                [SDK Manager] Installed packages:
                platforms;android-17         | 4.0.0
                build-tools;34.0.0         | 34.0.0
                tools                        | 26.0.0
            """.trimIndent()

            command.startsWith("avdmanager") -> """
                [AVD Manager] Available devices:
                pixel_8_pro_api37          | Google Pixel 8 Pro
                emulator-5554              | Android Emulator
            """.trimIndent()

            command.startsWith("lint") -> """
                Lint found 0 errors, 3 warnings
                Warning: Use of deprecated API
                Warning: Missing contentDescription
            """.trimIndent()

            command.startsWith("gradle") -> """
                BUILD SUCCESSFUL in 45s
                12 tasks executed
            """.trimIndent()

            else -> "[CLI] Executed: $command\nDone."
        }
    }

    private fun handleCancelCommand() {
        cliProcess?.destroy()
        cliProcess = null
        _state.value = _state.value.copy(
            isExecuting = false,
            commandHistory = _state.value.commandHistory.map { record ->
                if (record.status == CommandStatus.RUNNING) {
                    record.copy(status = CommandStatus.FAILED, errorOutput = "Cancelled by user")
                } else record
            }
        )
    }

    private fun handleClearCommandHistory() {
        _state.value = _state.value.copy(commandHistory = emptyList())
    }

    // ================================================================
    // Skills Tab handlers / Skills Tab 处理器
    // ================================================================
    private fun handleUpdateSkillSearch(query: String) {
        _state.value = _state.value.copy(skillSearchQuery = query)
    }

    private fun handleSelectSkill(skill: SkillInfo) {
        _state.value = _state.value.copy(selectedSkill = skill)
    }

    private fun handleInstallSelectedSkill() {
        val skill = _state.value.selectedSkill ?: return
        viewModelScope.launch {
            // Simulate installation / 模拟安装
            delay(1000)
            val updatedSkills = _state.value.installedSkills.map {
                if (it.id == skill.id) it.copy(isInstalled = true) else it
            }
            val updatedMarketplace = _state.value.skillMarketplace.map {
                if (it.id == skill.id) it.copy(isInstalled = true) else it
            }
            _state.value = _state.value.copy(
                installedSkills = updatedSkills,
                skillMarketplace = updatedMarketplace
            )
            _effect.emit(AndroidAgentToolkitEffect.ShowToast("Installed: ${skill.name}"))
        }
    }

    private fun handleUninstallSelectedSkill() {
        val skill = _state.value.selectedSkill ?: return
        viewModelScope.launch {
            delay(500)
            val updatedSkills = _state.value.installedSkills.map {
                if (it.id == skill.id) it.copy(isInstalled = false) else it
            }
            val updatedMarketplace = _state.value.skillMarketplace.map {
                if (it.id == skill.id) it.copy(isInstalled = false) else it
            }
            _state.value = _state.value.copy(
                installedSkills = updatedSkills,
                skillMarketplace = updatedMarketplace
            )
            _effect.emit(AndroidAgentToolkitEffect.ShowToast("Uninstalled: ${skill.name}"))
        }
    }

    private fun handleCreateNewSkill() {
        viewModelScope.launch {
            _effect.emit(AndroidAgentToolkitEffect.ShowToast("Skill template created"))
        }
    }

    // ================================================================
    // Knowledge Base Tab handlers / 知识库 Tab 处理器
    // ================================================================
    private fun handleUpdateKBQuery(query: String) {
        _state.value = _state.value.copy(kbQueryInput = query)

        // Debounced search / 防抖搜索
        viewModelScope.launch {
            delay(300)
            if (_state.value.kbQueryInput == query && query.isNotEmpty()) {
                val results = demoKBEntries.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.snippet.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
                }
                _state.value = _state.value.copy(kbSearchResults = results)
            } else if (query.isEmpty()) {
                _state.value = _state.value.copy(kbSearchResults = emptyList())
            }
        }
    }

    private fun handleSyncKnowledgeBase() {
        kbSyncJob?.cancel()
        kbSyncJob = viewModelScope.launch {
            _state.value = _state.value.copy(
                kbSyncStatus = SyncStatus(SyncState.SYNCING, 0f)
            )

            for (progress in listOf(0.25f, 0.5f, 0.75f, 1f)) {
                delay(600)
                _state.value = _state.value.copy(
                    kbSyncStatus = SyncStatus(SyncState.SYNCING, progress)
                )
            }

            _state.value = _state.value.copy(
                kbSyncStatus = SyncStatus(SyncState.IDLE),
                kbLastSyncTime = "2026-04-20 06:42",
                kbCacheSize = "128 MB"
            )
            _effect.emit(AndroidAgentToolkitEffect.ShowToast("Knowledge base synced"))
        }
    }

    private fun handleClearKBCache() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                kbCacheSize = "0 MB",
                kbSearchResults = emptyList(),
                kbQueryInput = ""
            )
            _effect.emit(AndroidAgentToolkitEffect.ShowToast("Cache cleared"))
        }
    }

    private fun handleDownloadKBSubset(categories: List<String>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                kbSyncStatus = SyncStatus(SyncState.SYNCING, 0f)
            )
            delay(2000)
            _state.value = _state.value.copy(
                kbSyncStatus = SyncStatus(SyncState.IDLE),
                kbCacheSize = "256 MB"
            )
            _effect.emit(AndroidAgentToolkitEffect.ShowToast("Downloaded ${categories.size} categories"))
        }
    }

    // ================================================================
    // CI/CD Tab handlers / CI/CD Tab 处理器
    // ================================================================
    private fun handleSelectPipelineTemplate(template: PipelineTemplate) {
        _state.value = _state.value.copy(
            selectedPipelineTemplate = template,
            pipelineYaml = demoPipelineYaml  // Load demo template / 加载演示模板
        )
    }

    private fun handleUpdatePipelineYaml(yaml: String) {
        _state.value = _state.value.copy(pipelineYaml = yaml)
    }

    private fun handleUpdateEnvVariable(key: String, value: String) {
        val updated = _state.value.envVariables.toMutableMap()
        updated[key] = value
        _state.value = _state.value.copy(envVariables = updated)
    }

    private fun handleRunPipelineDiagnostics() {
        viewModelScope.launch {
            // Simulate diagnostics run / 模拟诊断运行
            delay(1000)
            val issues = listOf(
                PipelineIssue(
                    id = "issue-1",
                    severity = "warning",
                    message = "Missing ANDROID_SDK_ROOT environment variable",
                    lineNumber = 8
                ),
                PipelineIssue(
                    id = "issue-2",
                    severity = "info",
                    message = "Consider using cache action for faster builds",
                    lineNumber = 12
                )
            )
            _state.value = _state.value.copy(pipelineDiagnostics = issues)
        }
    }

    private fun handleExportPipelineConfig() {
        viewModelScope.launch {
            _effect.emit(AndroidAgentToolkitEffect.TriggerPipelineExport(_state.value.pipelineYaml))
            _effect.emit(AndroidAgentToolkitEffect.ShowToast("Pipeline config exported"))
        }
    }

    // ================================================================
    // Settings Tab handlers / 设置 Tab 处理器
    // ================================================================
    private fun handleUpdateAIProvider(provider: AIProvider) {
        _state.value = _state.value.copy(aiProvider = provider)
    }

    private fun handleUpdateOutputFormat(format: OutputFormat) {
        _state.value = _state.value.copy(outputFormat = format)
    }

    private fun handleUpdateLogLevel(level: LogLevel) {
        _state.value = _state.value.copy(logLevel = level)
    }

    // ================================================================
    // Dashboard handler / 仪表盘处理器
    // ================================================================
    private fun handleRefreshDashboard() {
        viewModelScope.launch {
            val moduleStatuses = listOf(
                ModuleStatus(
                    name = "Android CLI",
                    description = "Command-line tools for SDK management and builds",
                    state = ModuleState.INSTALLED,
                    version = "2.0.0"
                ),
                ModuleStatus(
                    name = "Android Skills",
                    description = "Standardized skill definitions for AI agents",
                    state = ModuleState.INSTALLED,
                    version = "2.0.0"
                ),
                ModuleStatus(
                    name = "Knowledge Base",
                    description = "Android development knowledge for AI agents",
                    state = ModuleState.PENDING_CONFIG,
                    version = "1.0.0"
                )
            )

            val tokenStats = TokenSavingStats(
                totalTokens = 50_000,
                savedTokens = 35_000,
                savingPercent = 70,
                projectCount = 12
            )

            _state.value = _state.value.copy(
                moduleStatuses = moduleStatuses,
                tokenSavingStats = tokenStats,
                installedSkills = demoSkills.filter { it.isInstalled },
                skillMarketplace = demoSkills,
                pipelineYaml = demoPipelineYaml
            )
        }
    }

    // ================================================================
    // Cleanup / 清理
    // ================================================================
    override fun onCleared() {
        super.onCleared()
        cliProcess?.destroy()
        kbSyncJob?.cancel()
    }
}
