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
 */
class AndroidCliSkillsToolkitViewModel : ViewModel() {

    // State / UI 状态
    private val _state = MutableStateFlow(AndroidCliSkillsToolkitState.Initial)
    val state: StateFlow<AndroidCliSkillsToolkitState> = _state.asStateFlow()

    // Effect / 副作用通道
    private val _effect = Channel<AndroidCliSkillsToolkitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /**
     * Process user intention / 处理用户意图
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

    // Tab Navigation / Tab 切换
    private fun handleSelectTab(index: Int) { _state.update { it.copy(selectedTab = index) } }

    // Copy to Clipboard / 复制到剪贴板
    private fun handleCopySnippet(snippetId: String, content: String) {
        _state.update { it.copy(copiedSnippetId = snippetId) }
        viewModelScope.launch {
            _effect.send(AndroidCliSkillsToolkitEffect.CopyToClipboard(content))
            _effect.send(AndroidCliSkillsToolkitEffect.ShowToast("已复制 / Copied"))
        }
    }
    private fun handleClearCopyFeedback() { _state.update { it.copy(copiedSnippetId = null) } }

    // CLI Simulator / CLI 模拟器
    private fun handleUpdateCliCommand(command: String) { _state.update { it.copy(cliCommandInput = command) } }

    private fun handleExecuteCliCommand() {
        val currentCommand = _state.value.cliCommandInput.trim()
        if (currentCommand.isEmpty()) return
        val (output, isError) = simulateCliOutput(currentCommand)
        _state.update { currentState ->
            currentState.copy(
                cliCommandInput = "",
                cliOutputEntries = currentState.cliOutputEntries + CliOutputEntry(currentCommand, output, isError)
            )
        }
    }

    private fun handleClearCliOutput() { _state.update { it.copy(cliOutputEntries = emptyList()) } }

    private fun simulateCliOutput(command: String): Pair<String, Boolean> {
        return when {
            command.startsWith("curl") && command.contains("android") -> Pair("  % Total    % Received   Downloaded   Speed\n100  12.3k  --:--:--  2.1MB/s\n\n# Android CLI installed successfully!\n# Version: android-cli-2026.05.01", false)
            command.contains("gradle") && command.contains("wrapper") -> Pair("BUILD SUCCESSFUL in 3s\nCreating properties file: gradle/wrapper/gradle-wrapper.properties\nDone! You can now run ./gradlew to build your project.", false)
            command.contains("assembleDebug") || command.contains("build") -> Pair("> Task :app:preBuild UP-TO-DATE\n> Task :app:compileDebugKotlin UP-TO-DATE\n> Task :app:packageDebug UP-TO-DATE\n\nBUILD SUCCESSFUL in 45s\nAPK: app/build/outputs/apk/debug/app-debug.apk", false)
            command.contains("skill") && command.contains("list") -> Pair("Available Skills (3):\n  1. android-permissions@1.2.0  [verified]  Permission handling best practices\n  2. compose-best-practices@1.0  [community] Jetpack Compose performance patterns\n  3. kmm-network@2.1.0         [official]  KMM networking patterns", false)
            command.contains("skill") && command.contains("create") -> Pair("Scaffold created: my-android-skill/\n  ✓ skill.json\n  ✓ rules/\n  ✓ examples/\n  ✓ references/\nDone! Edit skill.json to get started.", false)
            command.contains("agent") && command.contains("status") -> Pair("Agent Status: Running\n  Model: Claude 4 (claude-sonnet-4-20250514)\n  Context: 200K tokens\n  Connected: android-studio-panda-4\n  Active Session: PRD-260-implementation", false)
            command.contains("help") || command == "--help" || command == "-h" -> Pair("Android CLI + Skills Agent Toolkit\n\nUsage: android <command> [options]\n\nCommands:\n  skill      Manage Android Skills\n  agent      Interact with AI Agent\n  build      Build Android project\n  kb         Query knowledge base\n  ci         CI/CD integration\n\nExamples:\n  android skill create my-skill\n  android agent status\n  android build --variant debug", false)
            command.contains("kb") && command.contains("query") -> Pair("[KB] Query: How to use WorkManager in Android 17?\n\nResults:\n  1. Android Developers: WorkManager (official)\n     https://developer.android.com/guide/background/work\n  2. Best Practices: WorkManager + foregroundService\n     android-permissions/skill.json\n  3. Community: WorkManager vs AlarmManager\n     compose-best-practices/skill.json", false)
            command.contains("ci") && command.contains("setup") -> Pair("CI Setup: Generating GitHub Actions workflow...\n  ✓ .github/workflows/android-ci.yml created\n  ✓ Gradle caching configured\n  ✓ Android SDK setup\n  ✓ Artifact publishing configured\n\nPush to enable CI pipeline.", false)
            else -> Pair("Error: Unknown command '$command'\nRun 'android --help' for usage information.", true)
        }
    }

    // Skill Editor / Skill 编辑器
    private fun handleUpdateSkillEditor(editor: SkillEditorState) { _state.update { it.copy(skillEditor = editor) } }
    private fun handleToggleSkillEditor() { _state.update { it.copy(isSkillEditorVisible = !it.isSkillEditorVisible) } }

    // Expandable Cards / 可展开卡片
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
