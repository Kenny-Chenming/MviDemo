package com.mvi.kenny.feature.androidcliskillstoolkit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * AndroidCliSkillsToolkitScreen — Android CLI + Skills AI Agent 开发工作流工具包主界面
 * ============================================================
 * PRD-260 | Android CLI + Skills AI Agent 开发工作流工具包
 * 5 Tab Layout: 上手指南 / Skills规范 / LLM集成 / 工程工具 / 生态广场
 */

private val tabIcons = listOf(
    Icons.Default.Rocket, Icons.Default.Extension, Icons.Default.AutoAwesome,
    Icons.Default.Build, Icons.Default.Groups
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidCliSkillsToolkitScreen(
    viewModel: AndroidCliSkillsToolkitViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AndroidCliSkillsToolkitEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AndroidCliSkillsToolkitEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("AndroidCliSkillsToolkit", effect.text)
                    clipboard.setPrimaryClip(clip)
                    viewModel.sendIntent(AndroidCliSkillsToolkitIntent.ClearCopyFeedback)
                }
                is AndroidCliSkillsToolkitEffect.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                    context.startActivity(intent)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Android CLI + Skills 工具箱", color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AndroidCliColors.Surface),
                actions = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Back", color = AndroidCliColors.Primary)
                    }
                }
            )
        },
        containerColor = AndroidCliColors.Background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = AndroidCliColors.Surface,
                contentColor = AndroidCliColors.TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]), color = AndroidCliColors.TabIndicator)
                }
            ) {
                AndroidCliTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.SelectTab(index)) },
                        text = { Text(tab.title, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(tabIcons[index], contentDescription = tab.title, modifier = Modifier.size(20.dp)) },
                        selectedContentColor = AndroidCliColors.Primary,
                        unselectedContentColor = AndroidCliColors.TextSecondary
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when (state.selectedTab) {
                    0 -> GettingStartedTab(state, viewModel)
                    1 -> SkillsSpecsTab(state, viewModel)
                    2 -> LlmIntegrationTab(state, viewModel)
                    3 -> EngineeringToolsTab(state, viewModel)
                    4 -> EcosystemTab(state, viewModel)
                }
            }
        }
    }
}

// ============================================================
// Tab 0: Getting Started / 上手指南
// ============================================================

@Composable
private fun GettingStartedTab(state: AndroidCliSkillsToolkitState, viewModel: AndroidCliSkillsToolkitViewModel) {
    val snippets = rememberGettingStartedSnippets()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("// Android CLI + Skills AI Agent 上手指南", color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Google 发布 Android CLI + Skills，让 AI Agent 可独立于 Android Studio 构建 Android 应用", color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = AndroidCliColors.Divider)
        }
        item { GettingStartedStepsCard(state, viewModel) }
        item { CliSimulatorCard(state, viewModel) }
        items(snippets) { snippet ->
            CodeSnippetCard(snippet, state.copiedSnippetId == snippet.id, snippet.id in state.expandedCardIds,
                onToggle = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.ToggleCard(snippet.id)) },
                onCopy = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.CopySnippet(snippet.id, snippet.content)) })
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun GettingStartedStepsCard(state: AndroidCliSkillsToolkitState, viewModel: AndroidCliSkillsToolkitViewModel) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AndroidCliColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("// 安装步骤 / Installation Steps", color = AndroidCliColors.Secondary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val steps = listOf(
                "1. 安装 Android CLI" to "curl -fsSL https://android.google.cn/cli | bash",
                "2. 登录 Google 账号" to "android auth login",
                "3. 连接 AI Agent" to "android agent connect --provider=claude",
                "4. 创建第一个项目" to "android create project --template=basic",
                "5. 启动 AI 构建" to "android build --agent=auto"
            )
            steps.forEach { (title, command) ->
                Text(title, color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(AndroidCliColors.CodeBackground).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(command, color = AndroidCliColors.TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    IconButton(onClick = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.CopySnippet("install-$title", command)) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AndroidCliColors.TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CliSimulatorCard(state: AndroidCliSkillsToolkitState, viewModel: AndroidCliSkillsToolkitViewModel) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AndroidCliColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("// CLI 命令模拟器", color = AndroidCliColors.Tertiary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                TextButton(onClick = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.ClearCliOutput) }) { Text("Clear", color = AndroidCliColors.TextSecondary, fontSize = 11.sp) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(6.dp)).background(AndroidCliColors.CodeBackground).border(1.dp, AndroidCliColors.Divider, RoundedCornerShape(6.dp)).padding(8.dp).verticalScroll(rememberScrollState())) {
                if (state.cliOutputEntries.isEmpty()) {
                    Text("# 输出将显示在这里...\n# Output will appear here...", color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.cliOutputEntries.forEach { entry ->
                            Column {
                                Text("\$${entry.command}", color = AndroidCliColors.Secondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                Text(entry.output, color = if (entry.isError) AndroidCliColors.Error else AndroidCliColors.TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 14.sp)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.cliCommandInput,
                    onValueChange = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.UpdateCliCommand(it)) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("android agent status", color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AndroidCliColors.TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AndroidCliColors.Primary, unfocusedBorderColor = AndroidCliColors.Divider, cursorColor = AndroidCliColors.Primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.ExecuteCliCommand) }),
                    singleLine = true, shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.ExecuteCliCommand) }, modifier = Modifier.size(48.dp).background(AndroidCliColors.Primary, RoundedCornerShape(8.dp))) {
                    Icon(Icons.Default.Send, contentDescription = "Execute", tint = Color.White)
                }
            }
        }
    }
}

@Composable private fun rememberGettingStartedSnippets(): List<CodeSnippet> = listOf(
    CodeSnippet(id = "gs-android-cli-install", title = "安装 Android CLI (macOS/Linux)", language = "bash",
        content = "# macOS / Linux 安装 Android CLI\ncurl -fsSL https://android.google.cn/cli | bash\n\n# 验证安装\nandroid --version\n# android-cli v2026.05.01\n\n# 配置 AI Provider\nandroid config set provider claude  # Claude\nandroid config set provider openai  # GPT-4o\nandroid config set provider gemini  # Gemini 2.5"),
    CodeSnippet(id = "gs-first-project", title = "创建第一个 AI 构建项目", language = "bash",
        content = "# 创建新项目（基于 AI 模板）\nandroid create project \\\n  --name MyFirstAgentApp \\\n  --template agent-optimized \\\n  --package com.example.agentapp \\\n  --org example.com \\\n  --platform android-17\n\ncd MyFirstAgentApp\n\n# 首次构建（AI Agent 自动优化）\nandroid build --agent=auto --variant=debug\n\n# 查看 AI 生成的构建报告\nandroid agent report"),
    CodeSnippet(id = "gs-agent-connect", title = "连接 AI Agent", language = "bash",
        content = "# 连接 Claude Agent\nandroid agent connect \\\n  --provider=claude \\\n  --model=claude-sonnet-4-20250514 \\\n  --context=200k\n\n# 查看 Agent 状态\nandroid agent status\n\n# 断开连接\nandroid agent disconnect")
)

// ============================================================
// Tab 1: Skills Specs / Skills 规范
// ============================================================

@Composable
private fun SkillsSpecsTab(state: AndroidCliSkillsToolkitState, viewModel: AndroidCliSkillsToolkitViewModel) {
    val snippets = rememberSkillsSpecsSnippets()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("// Android Skills 编写规范", color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Skills 是 AI Agent 的行为规范，定义工具、触发条件和执行工作流", color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = AndroidCliColors.Divider)
        }
        item { SkillEditorToggleCard(state, viewModel) }
        item { AnimatedVisibility(visible = state.isSkillEditorVisible) { SkillEditorCard(state, viewModel) } }
        item { SkillStructureCard(viewModel) }
        items(snippets) { snippet ->
            CodeSnippetCard(snippet, state.copiedSnippetId == snippet.id, snippet.id in state.expandedCardIds,
                onToggle = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.ToggleCard(snippet.id)) },
                onCopy = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.CopySnippet(snippet.id, snippet.content)) })
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun SkillEditorToggleCard(state: AndroidCliSkillsToolkitState, viewModel: AndroidCliSkillsToolkitViewModel) {
    Card(modifier = Modifier.fillMaxWidth().clickable { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.ToggleSkillEditor) }, colors = CardDefaults.cardColors(containerColor = AndroidCliColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = AndroidCliColors.Primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Skill 编辑器", color = AndroidCliColors.TextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(if (state.isSkillEditorVisible) "点击收起" else "点击展开内置 YAML/JSON 编辑器", color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
            Icon(if (state.isSkillEditorVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = if (state.isSkillEditorVisible) "Collapse" else "Expand", tint = AndroidCliColors.TextSecondary)
        }
    }
}

@Composable
private fun SkillEditorCard(state: AndroidCliSkillsToolkitState, viewModel: AndroidCliSkillsToolkitViewModel) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AndroidCliColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("// 内置 Skill 编辑器 / Built-in Skill Editor", color = AndroidCliColors.Warning, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.UpdateSkillEditor(state.skillEditor.copy(activeFormat = "yaml"))) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (state.skillEditor.activeFormat == "yaml") AndroidCliColors.Primary else AndroidCliColors.TextSecondary),
                    modifier = Modifier.weight(1f)
                ) { Text("YAML", fontFamily = FontFamily.Monospace, fontSize = 11.sp) }
                OutlinedButton(
                    onClick = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.UpdateSkillEditor(state.skillEditor.copy(activeFormat = "json"))) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (state.skillEditor.activeFormat == "json") AndroidCliColors.Primary else AndroidCliColors.TextSecondary),
                    modifier = Modifier.weight(1f)
                ) { Text("JSON", fontFamily = FontFamily.Monospace, fontSize = 11.sp) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = state.skillEditor.name, onValueChange = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.UpdateSkillEditor(state.skillEditor.copy(name = it))) },
                label = { Text("Skill Name", fontFamily = FontFamily.Monospace, fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AndroidCliColors.Primary, unfocusedBorderColor = AndroidCliColors.Divider, cursorColor = AndroidCliColors.Primary),
                singleLine = true, shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = state.skillEditor.description, onValueChange = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.UpdateSkillEditor(state.skillEditor.copy(description = it))) },
                label = { Text("Description", fontFamily = FontFamily.Monospace, fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AndroidCliColors.Primary, unfocusedBorderColor = AndroidCliColors.Divider, cursorColor = AndroidCliColors.Primary),
                singleLine = true, shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = state.skillEditor.triggers, onValueChange = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.UpdateSkillEditor(state.skillEditor.copy(triggers = it))) },
                label = { Text("Triggers (comma-separated)", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                placeholder = { Text("workmanager, background-task, periodic", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = AndroidCliColors.TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AndroidCliColors.Primary, unfocusedBorderColor = AndroidCliColors.Divider, cursorColor = AndroidCliColors.Primary),
                singleLine = true, shape = RoundedCornerShape(8.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("// ${state.skillEditor.activeFormat.uppercase()} Content", color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            val defaultYaml = """name: "${state.skillEditor.name.ifEmpty { "my-android-skill" }}"
version: "1.0.0"
description: "${state.skillEditor.description.ifEmpty { "My Android Skill" }}"
triggers:
  - ${state.skillEditor.triggers.ifEmpty { "android" }.split(",").joinToString("\n  - ") { it.trim() }}
rules:
  - id: rule-001
    title: Rule Title
    description: Rule description"""
            val defaultJson = """{
  "name": "${state.skillEditor.name.ifEmpty { "my-android-skill" }}",
  "version": "1.0.0",
  "description": "${state.skillEditor.description.ifEmpty { "My Android Skill" }}",
  "triggers": [${state.skillEditor.triggers.ifEmpty { "android" }.split(",").joinToString(", ") { "\"${it.trim()}\"" }}],
  "rules": [{"id": "rule-001", "title": "Rule Title", "description": "Rule description"}]
}"""
            val codeContent = if (state.skillEditor.activeFormat == "yaml") {
                state.skillEditor.contentYAML.ifEmpty { defaultYaml }
            } else {
                state.skillEditor.contentJSON.ifEmpty { defaultJson }
            }
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(6.dp)).background(AndroidCliColors.CodeBackground).border(1.dp, AndroidCliColors.Divider, RoundedCornerShape(6.dp)).padding(12.dp)) {
                SyntaxHighlightedEditor(
                    content = codeContent,
                    onValueChange = { newContent ->
                        val editor = if (state.skillEditor.activeFormat == "yaml") state.skillEditor.copy(contentYAML = newContent) else state.skillEditor.copy(contentJSON = newContent)
                        viewModel.sendIntent(AndroidCliSkillsToolkitIntent.UpdateSkillEditor(editor))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.CopySnippet("skill-editor-${state.skillEditor.activeFormat}", codeContent)) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = AndroidCliColors.Primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun SyntaxHighlightedEditor(content: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var textFieldValue by remember(content) { mutableStateOf(TextFieldValue(text = content, selection = TextRange(content.length))) }
    LaunchedEffect(content) { if (textFieldValue.text != content) { textFieldValue = TextFieldValue(text = content, selection = TextRange(content.length)) } }
    Box(modifier = modifier) {
        if (textFieldValue.text.isEmpty()) { Text("// Start typing...", color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp) }
        Text(textFieldValue.text, color = AndroidCliColors.TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.matchParentSize())
        BasicTextField(value = textFieldValue, onValueChange = { newValue -> textFieldValue = newValue; onValueChange(newValue.text) },
            modifier = Modifier.matchParentSize(),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Transparent),
            cursorBrush = SolidColor(AndroidCliColors.Primary))
    }
}

@Composable
private fun SkillStructureCard(viewModel: AndroidCliSkillsToolkitViewModel) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AndroidCliColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("// Skill JSON Schema", color = AndroidCliColors.Warning, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(AndroidCliColors.CodeBackground).border(1.dp, AndroidCliColors.Divider, RoundedCornerShape(6.dp)).padding(12.dp).horizontalScroll(rememberScrollState())) {
                Text("""{
  "name": "workmanager-best-practices",
  "version": "1.0.0",
  "description": "WorkManager 最佳实践指南",
  "author": "example.com",
  "triggers": ["workmanager", "background-task", "periodic"],
  "rules": [...],
  "examples": [...],
  "antiPatterns": [...]
}""", color = AndroidCliColors.TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.CopySnippet("skill-schema", """{"name": "workmanager-best-practices","version": "1.0.0","description": "WorkManager 最佳实践指南","author": "example.com","triggers": ["workmanager","background-task","periodic"],"rules": [],"examples": [],"antiPatterns": []}""")) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = AndroidCliColors.Primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Schema", color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable private fun rememberSkillsSpecsSnippets(): List<CodeSnippet> = listOf(
    CodeSnippet(id = "skill-create", title = "创建自定义 Skill", language = "bash",
        content = "# 创建新 Skill 脚手架\nandroid skill create my-android-skill\n\n# 目录结构\nmy-android-skill/\n├── skill.json          # Skill 定义\n├── rules/               # 规则目录\n├── examples/            # 示例目录\n└── references/          # 参考资料\n\n# 在项目中安装 Skill\nandroid skill install ./my-android-skill\n\n# 验证安装\nandroid skill list"),
    CodeSnippet(id = "skill-publish", title = "发布 Skill 到社区", language = "bash",
        content = "# 登录 Android CLI 账号\nandroid auth login\n\n# 验证 Skill 质量\nandroid skill audit ./my-android-skill\n\n# 发布到社区市场\nandroid skill publish ./my-android-skill \\\n  --tag=android-development \\\n  --license=MIT\n\n# 查看发布状态\nandroid skill status my-android-skill"),
    CodeSnippet(id = "skill-trigger-example", title = "Skill 触发条件示例", language = "yaml",
        content = "# Skill 触发条件配置示例\ntriggers:\n  # 精确匹配\n  - \"workmanager\"\n  # 模糊匹配（前缀）\n  - \"background\"\n  # 正则表达式\n  - \"^.*periodic.*work$\"\n  # 上下文关键词\n  - context:\n      platform: \"android\"\n      minSdk: 21\n      maxSdk: 34")
)

// ============================================================
// Tab 2: LLM Integration / LLM 集成
// ============================================================

@Composable
private fun LlmIntegrationTab(state: AndroidCliSkillsToolkitState, viewModel: AndroidCliSkillsToolkitViewModel) {
    val snippets = rememberLlmIntegrationSnippets()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("// LLM 集成指南", color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("CLI × Claude / GPT / Gemini 集成指南 + Vibe Coding 实战", color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = AndroidCliColors.Divider)
        }
        item { LlmProviderComparisonCard() }
        items(snippets) { snippet ->
            CodeSnippetCard(snippet, state.copiedSnippetId == snippet.id, snippet.id in state.expandedCardIds,
                onToggle = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.ToggleCard(snippet.id)) },
                onCopy = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.CopySnippet(snippet.id, snippet.content)) })
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun LlmProviderComparisonCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AndroidCliColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("// LLM Provider 对比", color = AndroidCliColors.Warning, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val providers = listOf(
                Triple("Claude (Anthropic)", "200K", "✅ 最强 Android 代码理解\n✅ 最佳多文件推理\n✅ 隐私优先"),
                Triple("GPT-4o (OpenAI)", "128K", "✅ 快速生成\n⚠️ Android 上下文稍弱\n✅ 生态完善"),
                Triple("Gemini 2.5 (Google)", "1M", "✅ 超长上下文\n✅ Android 原生集成\n⚠️ 工具调用较新")
            )
            providers.forEach { (name, ctx, desc) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Text(name, color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(140.dp))
                    Text(ctx, color = AndroidCliColors.Secondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.width(50.dp))
                    Text(desc, color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 14.sp)
                }
            }
        }
    }
}

@Composable private fun rememberLlmIntegrationSnippets(): List<CodeSnippet> = listOf(
    CodeSnippet(id = "llm-claude-config", title = "配置 Claude Provider", language = "bash",
        content = "# 配置 Claude Provider\nandroid config set provider claude\nandroid config set model claude-sonnet-4-20250514\nandroid config set api-key ${'$'}ANTHROPIC_API_KEY_PLACEHOLDER\n\n# 验证连接\nandroid agent status\n\n# 查看可用模型\nandroid config list-models"),
    CodeSnippet(id = "llm-vibe-coding", title = "Vibe Coding 实战工作流", language = "bash",
        content = "# Vibe Coding 工作流（纯自然语言开发）\n\n# 1. 描述需求\nandroid agent describe \\\n  --prompt \"实现一个实时聊天功能，支持文字和图片\"\n\n# 2. Agent 生成代码\nandroid agent generate \\\n  --mode=vibe \\\n  --context=full-project\n\n# 3. 自动构建测试\nandroid build --variant=debug --agent=auto\n\n# 4. 查看构建报告\nandroid agent report --format=markdown"),
    CodeSnippet(id = "llm-multi-agent", title = "多 Agent 协作", language = "bash",
        content = "# 多 Agent 协作模式\n\n# 启动 Architect Agent（架构设计）\nandroid agent start --role=architect --project=myapp\n\n# 启动 Coder Agent（代码生成）\nandroid agent start --role=coder --project=myapp\n\n# 启动 Reviewer Agent（代码审查）\nandroid agent start --role=reviewer --project=myapp\n\n# 协调多 Agent 协作\nandroid agent coordinate --workflow=tdd")
)

// ============================================================
// Tab 3: Engineering Tools / 工程工具
// ============================================================

@Composable
private fun EngineeringToolsTab(state: AndroidCliSkillsToolkitState, viewModel: AndroidCliSkillsToolkitViewModel) {
    val snippets = rememberEngineeringToolsSnippets()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("// 工程工具", color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("KB 查询 + CI/CD 集成 + Studio 协同工作流", color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = AndroidCliColors.Divider)
        }
        item { KbQueryCard(viewModel) }
        item { CICard(viewModel) }
        items(snippets) { snippet ->
            CodeSnippetCard(
                snippet = snippet,
                isCopied = state.copiedSnippetId == snippet.id,
                isExpanded = snippet.id in state.expandedCardIds,
                onToggle = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.ToggleCard(snippet.id)) },
                onCopy = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.CopySnippet(snippet.id, snippet.content)) }
            )
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun KbQueryCard(viewModel: AndroidCliSkillsToolkitViewModel) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AndroidCliColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("// 知识库查询 (KB Query)", color = AndroidCliColors.Secondary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val queries = listOf(
                "android agent kb query \"WorkManager periodic work\"",
                "android agent kb query \"Jetpack Compose performance\"",
                "android agent kb query \"Kotlin coroutines flow\"",
                "android agent kb query \"Room database migration\""
            )
            queries.forEach { query ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(4.dp)).background(AndroidCliColors.CodeBackground).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(query, color = AndroidCliColors.TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    IconButton(onClick = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.CopySnippet("kb-$query", query)) }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AndroidCliColors.TextSecondary, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CICard(viewModel: AndroidCliSkillsToolkitViewModel) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AndroidCliColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("// CI/CD 集成", color = AndroidCliColors.Tertiary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("支持的 CI 平台:", color = AndroidCliColors.TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("GitHub Actions", "GitLab CI", "Jenkins", "CircleCI", "Bitrise").forEach { platform ->
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AndroidCliColors.Primary.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(platform, color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable private fun rememberEngineeringToolsSnippets(): List<CodeSnippet> = listOf(
    CodeSnippet(id = "eng-kb-query", title = "KB 查询命令", language = "bash",
        content = "# 查询 Android 知识库\nandroid kb query \"WorkManager best practices\"\n\n# 查询特定版本文档\nandroid kb query \"Jetpack Compose 1.5\" --version=1.5\n\n# 导出 KB 到本地\nandroid kb export --output=./kb/\n\n# 搜索相关 Skills\nandroid skill search \"workmanager\""),
    CodeSnippet(id = "eng-ci-github", title = "GitHub Actions CI 配置", language = "yaml",
        content = "# .github/workflows/android-ci.yml\nname: Android CI\non: [push, pull_request]\njobs:\n  build:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v4\n      - name: Setup Android CLI\n        run: |\n          curl -fsSL https://android.google.cn/cli | bash\n          android agent connect --provider=claude\n      - name: Build with AI Agent\n        run: android build --agent=auto --variant=release"),
    CodeSnippet(id = "eng-studio-sync", title = "Android Studio 协同工作流", language = "bash",
        content = "# Android Studio 协同模式\n\n# 在 CLI 中启动 Agent\nandroid agent start --mode=collaborative\n\n# Studio 端连接到 Agent\n# Settings → Agent → Connect to CLI\n\n# Agent 生成代码后同步到 Studio\nandroid studio sync\n\n# 查看 Agent 的代码变更\nandroid agent diff --format=studio")
)

// ============================================================
// Tab 4: Ecosystem / 生态广场
// ============================================================

@Composable
private fun EcosystemTab(state: AndroidCliSkillsToolkitState, viewModel: AndroidCliSkillsToolkitViewModel) {
    val snippets = rememberEcosystemSnippets()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("// 生态广场", color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Skills 质量评估 + 社区分享平台", color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = AndroidCliColors.Divider)
        }
        item { SkillQualityCard() }
        item { CommunityStatsCard() }
        items(snippets) { snippet ->
            CodeSnippetCard(snippet, state.copiedSnippetId == snippet.id, snippet.id in state.expandedCardIds,
                onToggle = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.ToggleCard(snippet.id)) },
                onCopy = { viewModel.sendIntent(AndroidCliSkillsToolkitIntent.CopySnippet(snippet.id, snippet.content)) })
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun SkillQualityCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AndroidCliColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("// Skills 质量评分", color = AndroidCliColors.Warning, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val metrics = listOf(
                Triple("正确性", "Correctness", "规则定义的准确性"),
                Triple("完整性", "Completeness", "覆盖场景的全面性"),
                Triple("可维护性", "Maintainability", "代码结构清晰度"),
                Triple("实用性", "Practicality", "实际开发中的价值")
            )
            metrics.forEach { (cn, en, desc) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$cn / $en", color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(desc, color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AndroidCliColors.Tertiary.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("≥80%", color = AndroidCliColors.Tertiary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityStatsCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AndroidCliColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("// 社区统计", color = AndroidCliColors.Secondary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val stats = listOf("总 Skills 数量" to "1,234", "已验证 Skills" to "567", "社区贡献者" to "89", "月活跃用户" to "12.5K")
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                stats.forEach { (label, value) ->
                    Column(modifier = Modifier.width(100.dp).clip(RoundedCornerShape(8.dp)).background(AndroidCliColors.SurfaceVariant).padding(12.dp)) {
                        Text(value, color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(label, color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable private fun rememberEcosystemSnippets(): List<CodeSnippet> = listOf(
    CodeSnippet(id = "eco-skill-audit", title = "Skill 质量审计", language = "bash",
        content = "# 运行 Skill 质量审计\nandroid skill audit ./my-android-skill\n\n# 输出示例\n# Quality Score: 87/100\n# ✓ Correctness: 90/100\n# ✓ Completeness: 85/100\n# ✓ Maintainability: 88/100\n# ✓ Practicality: 85/100\n\n# 生成审计报告\nandroid skill audit ./my-android-skill --format=json --output=audit-report.json"),
    CodeSnippet(id = "eco-community-browse", title = "浏览社区 Skills", language = "bash",
        content = "# 浏览社区 Skills\nandroid skill browse\n\n# 搜索 Skills\nandroid skill search \"jetpack compose\"\n\n# 按类别浏览\nandroid skill browse --category=performance\n\n# 查看 Skill 详情\nandroid skill info workmanager-best-practices\n\n# 安装社区 Skill\nandroid skill install workmanager-best-practices"),
    CodeSnippet(id = "eco-contribute", title = "贡献 Skill 到社区", language = "bash",
        content = "# 贡献 Skill 到社区\n\n# 1. 确保通过质量审计\nandroid skill audit ./my-skill --threshold=80\n\n# 2. 添加文档\nandroid skill doc ./my-skill --generate\n\n# 3. 提交 Pull Request\nandroid skill submit ./my-skill \\\n  --repo=android-cn/community-skills \\\n  --license=Apache-2.0\n\n# 4. 等待审核（通常 3-5 工作日）\nandroid skill status my-skill")
)

// ============================================================
// Shared: CodeSnippetCard / 共享代码片段卡片
// ============================================================

@Composable
private fun CodeSnippetCard(
    snippet: CodeSnippet,
    isCopied: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = AndroidCliColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(snippet.title, color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(snippet.language.uppercase(), color = AndroidCliColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = if (isCopied) AndroidCliColors.Tertiary else AndroidCliColors.TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = if (isExpanded) "Collapse" else "Expand", tint = AndroidCliColors.TextSecondary)
                }
            }
            AnimatedVisibility(visible = isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(AndroidCliColors.CodeBackground).border(1.dp, AndroidCliColors.Divider, RoundedCornerShape(6.dp)).padding(12.dp)) {
                        Text(snippet.content, color = AndroidCliColors.TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.horizontalScroll(rememberScrollState()))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onCopy) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = AndroidCliColors.Primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isCopied) "已复制!" else "复制", color = AndroidCliColors.Primary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
