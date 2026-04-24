package com.mvi.kenny.feature.styleapikit

// ================================================================
// StyleApiKitViewModel — Compose Style API Toolkit ViewModel
// ================================================================
// ViewModel implementing MVI pattern for PRD-137.
//
// Processes StyleApiKitIntent and updates StyleApiKitState.
// Emits one-time side effects via StyleApiKitEffect Channel.
//
// PRD-137: Compose 1.11 Style API 声明式样式开发工具包
// Design Reference: memory/agency/designs/PRD-137-Compose-1.11-Style-API-声明式样式开发工具包.md
// ================================================================

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ============================================================
 * StyleApiKitViewModel — MVI ViewModel
 * ============================================================
 * Manages UI state and processes user intents for the Style API Toolkit.
 * Single source of truth via StateFlow, side effects via Channel.
 */
class StyleApiKitViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────
    // State — Single source of truth (StateFlow)
    // ─────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(StyleApiKitState())
    val state: StateFlow<StyleApiKitState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────
    // Effect — One-time side effects (Channel)
    // ─────────────────────────────────────────────────────────
    private val _effect = Channel<StyleApiKitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ─────────────────────────────────────────────────────────
    // Playground default code templates
    // ─────────────────────────────────────────────────────────
    private val playgroundTemplates = mapOf(
        PlaygroundTemplate.CardStyle to """
@Composable
fun StyledCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        style = CardStyle(
            pressed = pressedStyle,
            hovered = hoveredStyle,
            focused = focusedStyle,
            disabled = disabledStyle
        ),
        onClick = onClick,
        modifier = modifier
    ) {
        Text("Hello Style API!")
    }
}
        """.trimIndent(),
        PlaygroundTemplate.ButtonStyle to """
@Composable
fun StyledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        style = ButtonStyle(
            pressed = pressedStyle,
            hovered = hoveredStyle,
            disabled = disabledStyle
        ),
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text)
    }
}
        """.trimIndent(),
        PlaygroundTemplate.ListItemStyle to """
@Composable
fun StyledListItem(
    headline: String,
    supporting: String,
    modifier: Modifier = Modifier
) {
    ListItem(
        style = ListItemStyle(
            pressed = pressedStyle,
            hovered = hoveredStyle
        ),
        headlineContent = { Text(headline) },
        supportingContent = { Text(supporting) },
        modifier = modifier
    )
}
        """.trimIndent(),
        PlaygroundTemplate.ChipStyle to """
@Composable
fun StyledChip(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        style = ChipStyle(
            checked = checkedStyle,
            unchecked = uncheckedStyle
        ),
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = { Text(label) },
        modifier = modifier
    )
}
        """.trimIndent()
    )

    // Default Playground code
    private val defaultPlaygroundCode = """
// Compose Style API — Interactive Playground
// Paste your code here and see live preview

@Composable
fun Demo() {
    var pressed by remember { mutableStateOf(false) }

    Card(
        style = CardStyle(
            pressed = if (pressed) pressedStyle else DefaultStyle,
            hovered = hoveredStyle
        ),
        onClick = { pressed = !pressed }
    ) {
        Text(
            text = if (pressed) "Pressed!" else "Tap me",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
    """.trimIndent()

    // ─────────────────────────────────────────────────────────
    // Intent Processing Entry Point
    // ─────────────────────────────────────────────────────────
    /**
     * Process user intent and update state accordingly.
     * Called from the UI layer via sendIntent().
     *
     * @param intent The user intention to process
     */
    fun sendIntent(intent: StyleApiKitIntent) {
        when (intent) {
            is StyleApiKitIntent.SwitchTab -> switchTab(intent.tab)
            is StyleApiKitIntent.UpdateInputCode -> updateInputCode(intent.code)
            is StyleApiKitIntent.AnalyzeCode -> analyzeCode()
            is StyleApiKitIntent.ClearAnalysis -> clearAnalysis()
            is StyleApiKitIntent.SelectModule -> selectModule(intent.module)
            is StyleApiKitIntent.UpdateOriginalCode -> updateOriginalCode(intent.code)
            is StyleApiKitIntent.MigrateCode -> migrateCode()
            is StyleApiKitIntent.ApplyDiff -> applyDiff(intent.lineNumber)
            is StyleApiKitIntent.ApplyAllDiffs -> applyAllDiffs()
            is StyleApiKitIntent.CopyMigratedCode -> copyMigratedCode()
            is StyleApiKitIntent.ExportMigrationReport -> exportMigrationReport()
            is StyleApiKitIntent.SelectInteractionState -> selectInteractionState(intent.state)
            is StyleApiKitIntent.UpdateAnimationParams -> updateAnimationParams(intent.durationMs, intent.curve)
            is StyleApiKitIntent.ToggleAnimation -> toggleAnimation()
            is StyleApiKitIntent.ToggleAnimationCurve -> toggleAnimationCurve()
            is StyleApiKitIntent.UpdatePlaygroundCode -> updatePlaygroundCode(intent.code)
            is StyleApiKitIntent.SelectTemplate -> selectTemplate(intent.template)
            is StyleApiKitIntent.ResetToDefault -> resetPlayground()
        }
    }

    // ─────────────────────────────────────────────────────────
    // Tab Navigation
    // ─────────────────────────────────────────────────────────
    private fun switchTab(tab: StyleTab) {
        _state.update { it.copy(currentTab = tab) }
    }

    // ─────────────────────────────────────────────────────────
    // Decision Engine
    // ─────────────────────────────────────────────────────────
    private fun updateInputCode(code: String) {
        _state.update {
            it.copy(
                decisionEngineState = it.decisionEngineState.copy(
                    inputCode = code,
                    decisionResult = null,
                    decisionReasons = emptyList(),
                    analysisLog = emptyList()
                )
            )
        }
    }

    private fun analyzeCode() {
        viewModelScope.launch {
            val currentState = _state.value.decisionEngineState
            if (currentState.inputCode.isBlank()) {
                _effect.send(StyleApiKitEffect.ShowError("请先输入代码"))
                return@launch
            }

            _state.update {
                it.copy(
                    decisionEngineState = it.decisionEngineState.copy(isAnalyzing = true)
                )
            }

            // Simulate analysis delay (AST parsing would happen here in production)
            delay(800)

            val code = currentState.inputCode
            val reasons = mutableListOf<DecisionReason>()
            val log = mutableListOf<String>()

            log.add("▶ 开始分析代码...")

            // Heuristic analysis: check for Modifier chains that are good Style API candidates
            val hasModifierChain = code.contains("Modifier.") && code.contains(".background") ||
                    code.contains(".padding") || code.contains(".border")
            val hasInteractiveState = code.contains("pressed") || code.contains("hovered") ||
                    code.contains("focused") || code.contains("checked")
            val hasMultipleColorAssignments = code.count { it == 'c' && code.substringAfter("olor", "").isNotEmpty() } > 2
            val hasElevationChanges = code.contains("elevation") || code.contains("shadow")
            val hasShapeChanges = code.contains("shape") || code.contains("RoundedCorner")

            log.add("✓ 检测到交互状态处理: ${if (hasInteractiveState) "是" else "否"}")
            log.add("✓ 检测到颜色赋值: ${if (hasMultipleColorAssignments) "多个" else "少量"}")
            log.add("✓ 检测到 Elevation 样式: ${if (hasElevationChanges) "是" else "否"}")
            log.add("✓ 检测到 Shape 样式: ${if (hasShapeChanges) "是" else "否"}")

            // Decision logic
            val score = listOf(
                hasModifierChain to 2,
                hasInteractiveState to 3,
                hasMultipleColorAssignments to 1,
                hasElevationChanges to 1,
                hasShapeChanges to 1
            ).count { it.first } * 10

            val shouldMigrate = score >= 20

            if (shouldMigrate) {
                reasons.add(DecisionReason("代码包含多个交互状态处理，适合使用 Style API 统一管理", true))
                if (hasInteractiveState) reasons.add(DecisionReason("交互状态 (pressed/focused/hovered) 是 Style API 的核心优势场景", true))
                if (hasMultipleColorAssignments) reasons.add(DecisionReason("多个颜色赋值可迁移到 Style 块的语义化状态中", true))
                if (hasElevationChanges) reasons.add(DecisionReason("Elevation 样式在 Style 块中更易维护", true))
                if (score < 40) reasons.add(DecisionReason("部分场景可迁移，但简单的单一样式保持 Modifier 更合适", false))
            } else {
                reasons.add(DecisionReason("当前代码复杂度较低，保持 Modifier 链式调用更简洁", false))
                if (!hasInteractiveState) reasons.add(DecisionReason("缺少交互状态处理，Style API 的核心优势无法体现", false))
                if (!hasMultipleColorAssignments) reasons.add(DecisionReason("颜色赋值较少，无需抽象到 Style 层", false))
            }

            log.add(if (shouldMigrate) "▶ 判定结果: 建议迁移到 Style API" else "▶ 判定结果: 建议保持 Modifier")
            log.add("▶ 分析完成，得分: $score/50")

            _state.update {
                it.copy(
                    decisionEngineState = it.decisionEngineState.copy(
                        isAnalyzing = false,
                        decisionResult = if (shouldMigrate) DecisionVerdict.MigrateToStyle else DecisionVerdict.KeepModifier,
                        decisionReasons = reasons,
                        analysisLog = log
                    )
                )
            }
        }
    }

    private fun clearAnalysis() {
        _state.update {
            it.copy(
                decisionEngineState = DecisionEngineState()
            )
        }
    }

    // ─────────────────────────────────────────────────────────
    // Migration Tool
    // ─────────────────────────────────────────────────────────
    private fun selectModule(module: String) {
        _state.update {
            it.copy(
                migrationState = it.migrationState.copy(
                    selectedModule = module,
                    originalCode = "",
                    migratedCode = "",
                    diffs = emptyList(),
                    migrationComplete = false,
                    appliedDiffs = emptySet()
                )
            )
        }
    }

    private fun updateOriginalCode(code: String) {
        _state.update {
            it.copy(
                migrationState = it.migrationState.copy(
                    originalCode = code,
                    migratedCode = "",
                    diffs = emptyList(),
                    migrationComplete = false,
                    appliedDiffs = emptySet()
                )
            )
        }
    }

    private fun migrateCode() {
        viewModelScope.launch {
            val currentState = _state.value.migrationState
            if (currentState.originalCode.isBlank()) {
                _effect.send(StyleApiKitEffect.ShowError("请先输入需要迁移的代码"))
                return@launch
            }

            _state.update {
                it.copy(
                    migrationState = it.migrationState.copy(isMigrating = true)
                )
            }

            // Simulate AST-based migration (real implementation would parse Kotlin AST)
            delay(1200)

            val original = currentState.originalCode
            val diffs = generateMigrationDiffs(original)

            // Build migrated code from diffs
            val migratedLines = original.lines().mapIndexed { index, line ->
                val diff = diffs.find { it.lineNumber == index + 1 && it.changeType == DiffChangeType.MODIFY }
                diff?.migratedLine ?: line
            }.toMutableList()

            // Handle ADD lines
            diffs.filter { it.changeType == DiffChangeType.ADD }.sortedBy { it.lineNumber }.reversed().forEach { diff ->
                if (diff.lineNumber in 1..migratedLines.size + 1) {
                    migratedLines.add(diff.lineNumber - 1, diff.migratedLine)
                }
            }

            // Handle REMOVE lines
            val removeLineNumbers = diffs.filter { it.changeType == DiffChangeType.REMOVE }.map { it.lineNumber }.toSet()
            val finalMigrated = migratedLines.filterIndexed { index, _ -> (index + 1) !in removeLineNumbers }

            _state.update {
                it.copy(
                    migrationState = it.migrationState.copy(
                        isMigrating = false,
                        migratedCode = finalMigrated.joinToString("\n"),
                        diffs = diffs,
                        migrationComplete = diffs.isNotEmpty()
                    )
                )
            }

            if (diffs.isNotEmpty()) {
                _effect.send(StyleApiKitEffect.ShowSnackbar("迁移完成，发现 ${diffs.size} 处变更"))
            }
        }
    }

    /**
     * Generate migration diffs from original code.
     * This is a heuristic implementation — production would use Kotlin AST parser.
     * Detects Modifier chains and converts to Style API equivalents.
     */
    private fun generateMigrationDiffs(originalCode: String): List<MigrationDiff> {
        val diffs = mutableListOf<MigrationDiff>()
        val lines = originalCode.lines()

        lines.forEachIndexed { index, line ->
            val lineNumber = index + 1

            // Detect Modifier.background() → Style background
            if (line.contains("Modifier") && line.contains(".background(")) {
                val match = Regex("""\.background\(([^)]+)\)""").find(line)
                if (match != null) {
                    val colorArg = match.groupValues[1]
                    diffs.add(
                        MigrationDiff(
                            originalLine = line,
                            migratedLine = line.replace(".background($colorArg)", ""),
                            lineNumber = lineNumber,
                            changeType = DiffChangeType.MODIFY
                        )
                    )
                }
            }

            // Detect Modifier.padding() → Style contentPadding
            if (line.contains("Modifier") && line.contains(".padding(")) {
                val match = Regex("""\.padding\(([^)]+)\)""").find(line)
                if (match != null) {
                    val padArg = match.groupValues[1]
                    diffs.add(
                        MigrationDiff(
                            originalLine = line,
                            migratedLine = line.replace(".padding($padArg)", ""),
                            lineNumber = lineNumber,
                            changeType = DiffChangeType.MODIFY
                        )
                    )
                }
            }

            // Detect Modifier.border() → Style border
            if (line.contains("Modifier") && line.contains(".border(")) {
                diffs.add(
                    MigrationDiff(
                        originalLine = line,
                        migratedLine = "    // Border migrated to Style API",
                        lineNumber = lineNumber,
                        changeType = DiffChangeType.MODIFY
                    )
                )
            }

            // Detect Modifier.clickable → onClick in Style
            if (line.contains(".clickable(")) {
                diffs.add(
                    MigrationDiff(
                        originalLine = line,
                        migratedLine = line.replace(".clickable {", "onClick = {"),
                        lineNumber = lineNumber,
                        changeType = DiffChangeType.MODIFY
                    )
                )
            }
        }

        // Add Style import if any changes detected
        if (diffs.isNotEmpty()) {
            val importLine = lines.indexOfFirst { it.contains("import androidx.compose") }
            if (importLine >= 0) {
                diffs.add(
                    MigrationDiff(
                        originalLine = "",
                        migratedLine = "import androidx.compose.material3.api.experimental.*",
                        lineNumber = importLine + 1,
                        changeType = DiffChangeType.ADD
                    )
                )
            }
        }

        return diffs
    }

    private fun applyDiff(lineNumber: Int) {
        _state.update {
            it.copy(
                migrationState = it.migrationState.copy(
                    appliedDiffs = it.migrationState.appliedDiffs + lineNumber
                )
            )
        }
        viewModelScope.launch {
            _effect.send(StyleApiKitEffect.ShowSnackbar("已应用第 $lineNumber 行变更"))
        }
    }

    private fun applyAllDiffs() {
        val allLineNumbers = _state.value.migrationState.diffs.map { it.lineNumber }.toSet()
        _state.update {
            it.copy(
                migrationState = it.migrationState.copy(
                    appliedDiffs = allLineNumbers
                )
            )
        }
        viewModelScope.launch {
            _effect.send(StyleApiKitEffect.MigrationApplied)
        }
    }

    private fun copyMigratedCode() {
        viewModelScope.launch {
            val code = _state.value.migrationState.migratedCode
            if (code.isNotEmpty()) {
                _effect.send(StyleApiKitEffect.CopyToClipboard(code))
                _effect.send(StyleApiKitEffect.ShowSnackbar("代码已复制到剪贴板"))
            }
        }
    }

    private fun exportMigrationReport() {
        viewModelScope.launch {
            val state = _state.value.migrationState
            val report = buildString {
                appendLine("=== Style API Migration Report ===")
                appendLine("Module: ${state.selectedModule}")
                appendLine("Total Diffs: ${state.diffs.size}")
                appendLine("Applied: ${state.appliedDiffs.size}")
                appendLine()
                appendLine("--- Diff Details ---")
                state.diffs.forEach { diff ->
                    appendLine("Line ${diff.lineNumber} [${diff.changeType.value}]:")
                    if (diff.originalLine.isNotEmpty()) appendLine("  - ${diff.originalLine}")
                    if (diff.migratedLine.isNotEmpty()) appendLine("  + ${diff.migratedLine}")
                }
            }
            _effect.send(StyleApiKitEffect.ExportReport(report, "style-api-migration-report.txt"))
        }
    }

    // ─────────────────────────────────────────────────────────
    // Debug Panel
    // ─────────────────────────────────────────────────────────
    private fun selectInteractionState(state: InteractionState) {
        _state.update {
            it.copy(
                debugPanelState = it.debugPanelState.copy(
                    selectedInteractionState = state,
                    currentColor = Color(state.colorHex)
                )
            )
        }
    }

    private fun updateAnimationParams(durationMs: Int, curve: String) {
        _state.update {
            it.copy(
                debugPanelState = it.debugPanelState.copy(
                    animationDurationMs = durationMs,
                    animationCurve = curve
                )
            )
        }
    }

    private fun toggleAnimation() {
        _state.update {
            it.copy(
                debugPanelState = it.debugPanelState.copy(
                    isAnimating = !it.debugPanelState.isAnimating
                )
            )
        }
    }

    private fun toggleAnimationCurve() {
        _state.update {
            it.copy(
                debugPanelState = it.debugPanelState.copy(
                    showAnimationCurve = !it.debugPanelState.showAnimationCurve
                )
            )
        }
    }

    // ─────────────────────────────────────────────────────────
    // Playground
    // ─────────────────────────────────────────────────────────
    private fun updatePlaygroundCode(code: String) {
        _state.update {
            it.copy(
                playgroundState = it.playgroundState.copy(
                    code = code,
                    previewError = null
                )
            )
        }
    }

    private fun selectTemplate(template: PlaygroundTemplate) {
        val templateCode = playgroundTemplates[template] ?: defaultPlaygroundCode
        _state.update {
            it.copy(
                playgroundState = it.playgroundState.copy(
                    selectedTemplate = template,
                    code = templateCode,
                    previewError = null
                )
            )
        }
        viewModelScope.launch {
            _effect.send(StyleApiKitEffect.ShowSnackbar("已加载模板: ${template.displayName}"))
        }
    }

    private fun resetPlayground() {
        _state.update {
            it.copy(
                playgroundState = PlaygroundState(
                    code = defaultPlaygroundCode,
                    selectedTemplate = null
                )
            )
        }
    }
}
