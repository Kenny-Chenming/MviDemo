package com.mvi.kenny.feature.androidcliskillstoolkit

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
 * AndroidCliSkillsToolkitViewModel — Android CLI + Skills AI Agent 开发工作流工具包 ViewModel
 * ============================================================
 *
 * MVI Architecture: ViewModel handles Intent → executes business logic → updates State.
 *
 * State management:
 * - _state: MutableStateFlow holding current UI state
 * - state: Public immutable StateFlow exposed to UI layer
 *
 * Effect management:
 * - _effect: Channel for one-time side effects (Toast, Clipboard, URL)
 * - effect: Public receiveAsFlow for UI to collect
 *
 * @see AndroidCliSkillsToolkitState
 * @see AndroidCliSkillsToolkitIntent
 * @see AndroidCliSkillsToolkitEffect
 */
class AndroidCliSkillsToolkitViewModel : ViewModel() {

    // ============================================================
    // State — UI 状态
    // ============================================================

    private val _state = MutableStateFlow(AndroidCliSkillsToolkitState.Initial)
    val state: StateFlow<AndroidCliSkillsToolkitState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用通道
    // ============================================================

    private val _effect = Channel<AndroidCliSkillsToolkitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent Processing — 意图处理
    // ============================================================

    /**
     * Process user intention
     * 处理用户意图
     *
     * Entry point for all user interactions. Called from UI layer via:
     *   viewModel.sendIntent(AndroidCliSkillsToolkitIntent.xxx)
     *
     * @param intent User intention / 用户意图
     */
    fun sendIntent(intent: AndroidCliSkillsToolkitIntent) {
        when (intent) {
            is AndroidCliSkillsToolkitIntent.SelectTab -> handleSelectTab(intent.index)
            is AndroidCliSkillsToolkitIntent.CopySnippet -> handleCopySnippet(intent.snippetId, intent.content)
            is AndroidCliSkillsToolkitIntent.ClearCopyFeedback -> handleClearCopyFeedback()
            is AndroidCliSkillsToolkitIntent.UpdateCliCommand -> handleUpdateCliCommand(intent.command)
            is AndroidCliSkillsToolkitIntent.ExecuteCliCommand -> handleExecuteCliCommand()
            is AndroidCliSkillsToolkitIntent.ClearCliOutput -> handleClearCliOutput()
            is AndroidCliSkillsToolkitIntent.UpdateSkillEditor -> handleUpdateSkillEditor(intent.editor)
            is AndroidCliSkillsToolkitIntent.ToggleSkillEditor -> handleToggleSkillEditor()
            is AndroidCliSkillsToolkitIntent.ToggleCard -> handleToggleCard(intent.cardId)
        }
    }

    // ============================================================
    // Tab Navigation / Tab 切换
    // ============================================================

    /**
     * Handle tab selection
     * 处理 Tab 切换
     *
     * @param index Selected tab index / 选中的 Tab 索引
     */
    private fun handleSelectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    // ============================================================
    // Copy to Clipboard / 复制到剪贴板
    // ============================================================

    /**
     * Handle copy snippet to clipboard
     * 处理复制代码片段到剪贴板
     *
     * @param snippetId Snippet identifier / 片段标识
     * @param content Content to copy / 要复制的内容
     */
    private fun handleCopySnippet(snippetId: String, content: String) {
        _state.update { it.copy(copiedSnippetId = snippetId) }
        viewModelScope.launch {
            _effect.send(AndroidCliSkillsToolkitEffect.CopyToClipboard(content))
            _effect.send(AndroidCliSkillsToolkitEffect.ShowToast("已复制 / Copied"))
        }
    }

    /**
     * Clear copy feedback after timeout
     * 清除复制反馈（超时后）
     */
    private fun handleClearCopyFeedback() {
        _state.update { it.copy(copiedSnippetId = null) }
    }

    // ============================================================
    // CLI Simulator / CLI 模拟器
    // ============================================================

    /**
     * Handle CLI command input update
     * 处理 CLI 命令输入更新
     *
     * @param command CLI command / CLI 命令
     */
    private fun handleUpdateCliCommand(command: String) {
        _state.update { it.copy(cliCommandInput = command) }
    }

    /**
     * Handle CLI command execution (simulated)
     * 处理 CLI 命令执行（模拟）
     *
     * Generates realistic CLI output based on the command.
     * This is a simulation — no actual CLI execution.
     */
    private fun handleExecuteCliCommand() {
        val currentCommand = _state.value.cliCommandInput.trim()
        if (currentCommand.isEmpty()) return

        // Simulated output based on command patterns / 根据命令模式生成模拟输出
        val (output, isError) = simulateCliOutput(currentCommand)

        _state.update { currentState ->
            currentState.copy(
                cliCommandInput = "",
                cliOutputEntries = currentState.cliOutputEntries + CliOutputEntry(
                    command = currentCommand,
                    output = output,
                    isError = isError
                )
            )
        }
    }

    /**
     * Clear all CLI output entries
     * 清空所有 CLI 输出条目
     */
    private fun handleClearCliOutput() {
        _state.update { it.copy(cliOutputEntries = emptyList()) }
    }

    /**
     * Simulate CLI output based on command input
     * 根据命令输入模拟 CLI 输出
     *
     * @param command Input command / 输入的命令
     * @return Pair of (output, isError) / (输出, 是否为错误) 对
     */
    private fun simulateCliOutput(command: String): Pair<String, Boolean> {
        return when {
            // Android CLI installation / Android CLI 安装
            command.startsWith("curl") && command.contains("android") -> {
                Pair("""
                    |  % Total    % Received   Downloaded   Speed
                    |100  12.3k  --:--:--  2.1MB/s
                    |
                    |# Android CLI installed successfully!
                    |# Version: android-cli-2026.05.01
                """.trimMargin(), false)
            }
            // Gradle wrapper / Gradle 包装器
            command.contains("gradle") && command.contains("wrapper") -> {
                Pair("""
                    |BUILD SUCCESSFUL in 3s
                    |Creating properties file: gradle/wrapper/gradle-wrapper.properties
                    |Done! You can now run ./gradlew to build your project.
                """.trimMargin(), false)
            }
            // Build command / 构建命令
            command.contains("assembleDebug") || command.contains("build") -> {
                Pair("""
                    |> Task :app:preBuild UP-TO-DATE
                    |> Task :app:compileDebugKotlin UP-TO-DATE
                    |> Task :app:processDebugResources UP-TO-DATE
                    |> Task :app:packageDebug UP-TO-DATE
                    |
                    |BUILD SUCCESSFUL in 45s
                    |APK: app/build/outputs/apk/debug/app-debug.apk
                """.trimMargin(), false)
            }
            // Skill listing / Skill 列表
            command.contains("skill") && command.contains("list") -> {
                Pair("""
                    |Available Skills (3):
                    |  1. android-permissions@1.2.0  [verified]  Permission handling best practices
                    |  2. compose-best-practices@1.0  [community] Jetpack Compose performance patterns
                    |  3. kmm-network@2.1.0         [official]  KMM networking patterns
                """.trimMargin(), false)
            }
            // Skill create / Skill 创建
            command.contains("skill") && command.contains("create") -> {
                Pair("""
                    |Scaffold created: my-android-skill/
                    |  ✓ skill.json
                    |  ✓ rules/
                    |  ✓ examples/
                    |  ✓ references/
                    |Done! Edit skill.json to get started.
                """.trimMargin(), false)
            }
            // Agent status / Agent 状态
            command.contains("agent") && command.contains("status") -> {
                Pair("""
                    |Agent Status: Running
                    |  Model: Claude 4 (claude-sonnet-4-20250514)
                    |  Context: 200K tokens
                    |  Connected: android-studio-panda-4
                    |  Active Session: PRD-260-implementation
                """.trimMargin(), false)
            }
            // Help command / 帮助命令
            command.contains("help") || command == "--help" || command == "-h" -> {
                Pair("""
                    |Android CLI + Skills Agent Toolkit
                    |
                    |Usage: android <command> [options]
                    |
                    |Commands:
                    |  skill      Manage Android Skills
                    |  agent      Interact with AI Agent
                    |  build      Build Android project
                    |  kb         Query knowledge base
                    |  ci         CI/CD integration
                    |
                    |Examples:
                    |  android skill create my-skill
                    |  android agent status
                    |  android build --variant debug
                """.trimMargin(), false)
            }
            // KB query / KB 查询
            command.contains("kb") && command.contains("query") -> {
                Pair("""
                    |[KB] Query: How to use WorkManager in Android 17?
                    |
                    |Results:
                    |  1. Android Developers: WorkManager (official)
                    |     https://developer.android.com/guide/background/work
                    |  2. Best Practices: WorkManager + foregroundService
                    |     android-permissions/skill.json
                    |  3. Community: WorkManager vs AlarmManager
                    |     compose-best-practices/skill.json
                """.trimMargin(), false)
            }
            // CI integration / CI 集成
            command.contains("ci") && command.contains("setup") -> {
                Pair("""
                    |CI Setup: Generating GitHub Actions workflow...
                    |  ✓ .github/workflows/android-ci.yml created
                    |  ✓ Gradle caching configured
                    |  ✓ Android SDK setup
                    |  ✓ Artifact publishing configured
                    |
                    |Push to enable CI pipeline.
                """.trimMargin(), false)
            }
            // Unknown command / 未知命令
            else -> {
                Pair("Error: Unknown command '$command'\nRun 'android --help' for usage information.", true)
            }
        }
    }

    // ============================================================
    // Skill Editor / Skill 编辑器
    // ============================================================

    /**
     * Handle skill editor content update
     * 处理 Skill 编辑器内容更新
     *
     * @param editor Updated editor state / 更新后的编辑器状态
     */
    private fun handleUpdateSkillEditor(editor: SkillEditorState) {
        _state.update { it.copy(skillEditor = editor) }
    }

    /**
     * Toggle skill editor visibility
     * 切换 Skill 编辑器可见性
     */
    private fun handleToggleSkillEditor() {
        _state.update { it.copy(isSkillEditorVisible = !it.isSkillEditorVisible) }
    }

    // ============================================================
    // Expandable Cards / 可展开卡片
    // ============================================================

    /**
     * Toggle card expansion state
     * 切换卡片展开/收起状态
     *
     * @param cardId Card identifier / 卡片标识
     */
    private fun handleToggleCard(cardId: String) {
        _state.update { currentState ->
            val newExpanded = if (cardId in currentState.expandedCardIds) {
                currentState.expandedCardIds - cardId
            } else {
                currentState.expandedCardIds + cardId
            }
            currentState.copy(expandedCardIds = newExpanded)
        }
    }
}
