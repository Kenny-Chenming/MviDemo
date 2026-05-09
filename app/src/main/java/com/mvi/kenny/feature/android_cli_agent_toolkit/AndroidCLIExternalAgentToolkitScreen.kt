package com.mvi.kenny.feature.android_cli_agent_toolkit

// PRD-241: Android CLI × External AI Agent Integration Toolkit
// 5-Tab: Agent Integration / Multi-Agent / CLI Parser / Swift Export & Quail / KB API

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Terminal
import com.mvi.kenny.base.TopBarConfig
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest

private object ToolColors {
    val Background = Color(0xFF0D1117)
    val Surface = Color(0xFF161B22)
    val CodeBlock = Color(0xFF21262D)
    val Primary = Color(0xFF58A6FF)
    val Secondary = Color(0xFF3FB950)
    val Info = Color(0xFF58A6FF)
    val Warning = Color(0xFFD29922)
    val Error = Color(0xFFF85149)
    val TextPrimary = Color(0xFFE6EDF3)
    val TextSecondary = Color(0xFF8B949E)
    val CodeString = Color(0xFFA5D6FF)
    val Border = Color(0xFF30363D)
    val Orange = Color(0xFFFFA657)
}

@Composable
fun AndroidCLIExternalAgentToolkitScreen(
    state: AndroidCLIExternalAgentToolkitState,
    viewModel: AndroidCLIExternalAgentToolkitViewModel,
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val tabTitles = listOf("Agent 集成指南", "多 Agent 协作", "CLI 解析 & 触发", "Swift Export & Quail", "KB API & 编排")

    LaunchedEffect(state.selectedTab) {
        onUpdateTopBar(TopBarConfig(title = "${tabTitles[state.selectedTab]} · Android CLI × External AI Agent"))
    }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AndroidCLIExternalAgentToolkitEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Command", effect.text))
                    Toast.makeText(context, "Copied! / 已复制!", Toast.LENGTH_SHORT).show()
                }
                is AndroidCLIExternalAgentToolkitEffect.ShowSnackbar -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AndroidCLIExternalAgentToolkitEffect.OpenUrl -> {
                    try { uriHandler.openUri(effect.url) } catch (e: Exception) { Toast.makeText(context, "Failed to open URL", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(ToolColors.Background)) {
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = ToolColors.Surface,
            contentColor = ToolColors.Primary,
            edgePadding = 12.dp,
            divider = { HorizontalDivider(color = ToolColors.Border, thickness = 1.dp) }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.SelectTab(index)) },
                    text = { Text(title, color = if (state.selectedTab == index) ToolColors.Primary else ToolColors.TextSecondary, fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
                    icon = { Icon(imageVector = when (index) { 0 -> Icons.Default.AutoAwesome; 1 -> Icons.Default.Terminal; 2 -> Icons.Default.Code; 3 -> Icons.Default.Check; else -> Icons.Default.Info }, contentDescription = null, tint = if (state.selectedTab == index) ToolColors.Primary else ToolColors.TextSecondary, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        when (state.selectedTab) {
            0 -> AgentIntegrationTab(state, viewModel)
            1 -> MultiAgentTab(state, viewModel)
            2 -> CliParserTab(state, viewModel)
            3 -> SwiftExportTab(state, viewModel)
            4 -> KbApiTab(state, viewModel)
        }
    }
}

@Composable
private fun AgentIntegrationTab(state: AndroidCLIExternalAgentToolkitState, viewModel: AndroidCLIExternalAgentToolkitViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { SectionHeader("Agent Integration Guide", "Agent 集成指南", "Integrate Android CLI with Claude Code, Cursor, and Gemini CLI", Icons.Default.AutoAwesome) }
        items(state.agentGuides) { guide ->
            AgentGuideCard(
                guide = guide,
                isExpanded = state.expandedAgentGuideId == guide.id,
                onToggle = { viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.ToggleAgentGuideExpanded(guide.id)) },
                onCopy = { cmd -> viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.CopyCommand(cmd, guide.id)) },
                onOpenUrl = { url -> viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.OpenUrl(url)) }
            )
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun SectionHeader(title: String, titleCn: String, subtitle: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = ToolColors.Primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row {
                Text(text = title, color = ToolColors.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "// $titleCn", color = ToolColors.Primary, style = MaterialTheme.typography.titleMedium)
            }
            Text(text = subtitle, color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AgentGuideCard(guide: AgentGuide, isExpanded: Boolean, onToggle: () -> Unit, onCopy: (String) -> Unit, onOpenUrl: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onToggle() }, colors = CardDefaults.cardColors(containerColor = ToolColors.Surface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(ToolColors.Primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text(text = guide.agentType.logo, fontSize = 20.sp) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = guide.title, color = ToolColors.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = guide.subtitleCn, color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                    }
                }
                Icon(imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expand", tint = ToolColors.TextSecondary)
            }
            AnimatedVisibility(visible = isExpanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = ToolColors.Border)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Install Command / 安装命令:", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeBlockWithCopy(code = guide.installCommand, onCopy = { onCopy(guide.installCommand) })
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Setup Steps / 配置步骤:", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    guide.setupSteps.forEachIndexed { index, (step, desc) ->
                        Row {
                            Text(text = "${index + 1}.", color = ToolColors.Secondary, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.width(24.dp))
                            Text(text = step, color = ToolColors.TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                        Text(text = "   $desc", color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, modifier = Modifier.padding(start = 24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Typical Workflow / 典型工作流:", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeBlockWithCopy(code = guide.typicalWorkflow, onCopy = { onCopy(guide.typicalWorkflow) })
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Code Example / 代码示例:", color = ToolColors.Info, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeBlockWithCopy(code = guide.codeExample, onCopy = { onCopy(guide.codeExample) })
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { onOpenUrl(guide.url) }, colors = ButtonDefaults.buttonColors(containerColor = ToolColors.Primary), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "View Documentation / 查看文档", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiAgentTab(state: AndroidCLIExternalAgentToolkitState, viewModel: AndroidCLIExternalAgentToolkitViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { SectionHeader("Multi-Agent Collaboration", "多 Agent 协作框架", "Orchestrator/Executor/Reviewer roles working together via Android CLI", Icons.Default.Terminal) }
        item { Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) { AgentRole.entries.forEach { RoleLegendItem(it) } } }
        item { Text(text = "Collaboration Flow / 协作流程:", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
        items(state.collaborationSteps.chunked(1)) { stepList -> stepList.forEach { CollaborationStepCard(it) { cmd -> viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.CopyCommand(cmd, it.stepIndex.toString())) } } }
        item { Text(text = "Orchestration Patterns / 编排模式:", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
        items(state.orchestrationPatterns) { pattern -> OrchestrationPatternCard(pattern, state.expandedPatternId == pattern.id) { viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.TogglePatternExpanded(pattern.id)) } }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun RoleLegendItem(role: AgentRole) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(ToolColors.Surface, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Box(modifier = Modifier.size(12.dp).background(Color(role.color), RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = role.roleName, color = ToolColors.TextPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(text = role.roleNameCn, color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
        }
    }
}

@Composable
private fun CollaborationStepCard(step: CollaborationStep, onCopy: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ToolColors.Surface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoleChip(step.role)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Step ${step.stepIndex}", color = ToolColors.TextSecondary, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        Text(text = step.action, color = ToolColors.TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = step.descriptionCn, color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            CodeBlockWithCopy(code = step.cliCommand, onCopy = { onCopy(step.cliCommand) }, compact = true)
        }
    }
}

@Composable
private fun RoleChip(role: AgentRole) {
    Box(modifier = Modifier.background(Color(role.color).copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text = role.roleName, color = Color(role.color), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
private fun OrchestrationPatternCard(pattern: OrchestrationPattern, isExpanded: Boolean, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onToggle() }, colors = CardDefaults.cardColors(containerColor = ToolColors.Surface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row {
                        Text(text = pattern.patternName, color = ToolColors.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "// ${pattern.patternNameCn}", color = ToolColors.Primary, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(text = pattern.descriptionCn, color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }
                Icon(imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expand", tint = ToolColors.TextSecondary)
            }
            AnimatedVisibility(visible = isExpanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = ToolColors.Border)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Use Cases / 使用场景:", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    pattern.useCasesCn.forEach { Text(text = "• $it", color = ToolColors.TextPrimary, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

@Composable
private fun CliParserTab(state: AndroidCLIExternalAgentToolkitState, viewModel: AndroidCLIExternalAgentToolkitViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { SectionHeader("CLI Output Parser", "CLI 输出解析器", "Parse Android CLI command outputs into structured JSON for AI agents", Icons.Default.Code) }
        items(state.parsedCommands) { cmd -> ParsedCommandCard(cmd, state.expandedCommandId == cmd.id, { viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.ToggleCommandExpanded(cmd.id)) }, { c -> viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.CopyCommand(c, cmd.id)) }) }
        item { Spacer(modifier = Modifier.height(8.dp)); SectionHeader("Auto Trigger Rules", "自动触发规则", "Automatically install skills based on task patterns", Icons.Default.AutoAwesome) }
        items(state.triggerRules) { rule -> TriggerRuleCard(rule, state.expandedTriggerId == rule.id, { viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.ToggleTriggerExpanded(rule.id)) }, { c -> viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.CopyCommand(c, rule.id)) }) }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun ParsedCommandCard(command: ParsedCommand, isExpanded: Boolean, onToggle: () -> Unit, onCopy: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onToggle() }, colors = CardDefaults.cardColors(containerColor = ToolColors.Surface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = command.commandTypeCn, color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(text = command.originalCommand, color = ToolColors.TextPrimary, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
                Icon(imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expand", tint = ToolColors.TextSecondary)
            }
            AnimatedVisibility(visible = isExpanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = ToolColors.Border)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Args / 参数:", color = ToolColors.Info, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    command.args.forEach { (k, v) -> Row { Text(text = "$k: ", color = ToolColors.Secondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp); Text(text = v, color = ToolColors.TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 11.sp) } }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Output Format: ${command.outputFormat} / ${command.outputFormatCn}", color = ToolColors.Warning, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeBlockWithCopy(code = command.exampleOutput, onCopy = { onCopy(command.exampleOutput) })
                }
            }
        }
    }
}

@Composable
private fun TriggerRuleCard(rule: TriggerRule, isExpanded: Boolean, onToggle: () -> Unit, onCopy: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onToggle() }, colors = CardDefaults.cardColors(containerColor = ToolColors.Surface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = rule.taskPatternCn, color = ToolColors.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = rule.descriptionCn, color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }
                Icon(imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Expand", tint = ToolColors.TextSecondary)
            }
            AnimatedVisibility(visible = isExpanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = ToolColors.Border)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Pattern: ${rule.taskPattern}", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Recommended Skill: ${rule.recommendedSkill} / ${rule.recommendedSkillCn}", color = ToolColors.Secondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeBlockWithCopy(code = rule.autoInstallCommand, onCopy = { onCopy(rule.autoInstallCommand) })
                }
            }
        }
    }
}

@Composable
private fun SwiftExportTab(state: AndroidCLIExternalAgentToolkitState, viewModel: AndroidCLIExternalAgentToolkitViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { SectionHeader("Swift Export OTB", "Swift Export OTB 集成指南", "Kotlin 2.2.21 Swift Export Out-of-the-Box for KMP projects", Icons.Default.Check) }
        items(state.swiftExportGuides) { guide -> SwiftExportCard(guide, state.expandedSwiftExportId == guide.id, { viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.ToggleSwiftExportExpanded(guide.id)) }, { c -> viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.CopyCommand(c, guide.id)) }) }
        item { Spacer(modifier = Modifier.height(8.dp)); SectionHeader("Quail New Features", "Android Studio Quail 新功能", "LeakCanary Profiler / Compose Screenshot Testing / Material Symbols", Icons.Default.Info) }
        items(state.quailFeatures) { feature -> QuailFeatureCard(feature, state.expandedQuailFeatureId == feature.id, { viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.ToggleQuailFeatureExpanded(feature.id)) }, { c -> viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.CopyCommand(c, feature.id)) }, { u -> viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.OpenUrl(u)) }) }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun SwiftExportCard(guide: SwiftExportGuide, isExpanded: Boolean, onToggle: () -> Unit, onCopy: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onToggle() }, colors = CardDefaults.cardColors(containerColor = ToolColors.Surface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Swift Export / Swift 导出", color = ToolColors.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.background(ToolColors.Secondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(text = "Kotlin ${guide.kotlinVersion}", color = ToolColors.Secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    }
                    Text(text = "Supported: ${guide.supportedPlatforms.joinToString(", ")}", color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Icon(imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = ToolColors.TextSecondary)
            }
            AnimatedVisibility(visible = isExpanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = ToolColors.Border)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "KMP Config Steps / KMP 配置步骤:", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    guide.kmpConfigSteps.forEach { (step, desc) ->
                        Row {
                            Text(text = "▸", color = ToolColors.Secondary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = step, color = ToolColors.TextPrimary, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "— $desc", color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Export Command / 导出命令:", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeBlockWithCopy(code = guide.swiftExportCommand, onCopy = { onCopy(guide.swiftExportCommand) })
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Code Example / 代码示例:", color = ToolColors.Info, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeBlockWithCopy(code = guide.codeExample, onCopy = { onCopy(guide.codeExample) })
                }
            }
        }
    }
}

@Composable
private fun QuailFeatureCard(feature: QuailFeature, isExpanded: Boolean, onToggle: () -> Unit, onCopy: (String) -> Unit, onOpenUrl: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onToggle() }, colors = CardDefaults.cardColors(containerColor = ToolColors.Surface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = feature.featureName, color = ToolColors.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = feature.featureNameCn, color = ToolColors.Primary, style = MaterialTheme.typography.bodySmall)
                }
                Icon(imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = ToolColors.TextSecondary)
            }
            AnimatedVisibility(visible = isExpanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = ToolColors.Border)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Description / 描述:", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(text = feature.descriptionCn, color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Install Command / 安装命令:", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeBlockWithCopy(code = feature.installCommand, onCopy = { onCopy(feature.installCommand) })
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Usage Steps / 使用步骤:", color = ToolColors.Info, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    feature.usageStepsCn.forEachIndexed { index, step -> Row { Text(text = "${index + 1}.", color = ToolColors.Secondary, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.width(20.dp)); Text(text = step, color = ToolColors.TextPrimary, style = MaterialTheme.typography.bodySmall) } }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Code Example / 代码示例:", color = ToolColors.Info, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeBlockWithCopy(code = feature.codeExample, onCopy = { onCopy(feature.codeExample) })
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { onOpenUrl(feature.url) }, colors = ButtonDefaults.buttonColors(containerColor = ToolColors.Primary), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "View Documentation / 查看文档", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun KbApiTab(state: AndroidCLIExternalAgentToolkitState, viewModel: AndroidCLIExternalAgentToolkitViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { SectionHeader("Knowledge Base API", "Knowledge Base API 封装", "Agent可直接调用的 KB API SDK", Icons.Default.Info) }
        items(state.kbApiMethods) { api -> KbApiMethodCard(api, state.expandedKbApiId == api.id, { viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.ToggleKbApiExpanded(api.id)) }, { c -> viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.CopyCommand(c, api.id)) }) }
        item { Spacer(modifier = Modifier.height(8.dp)); SectionHeader("Orchestration Framework", "多 Agent 编排框架", "Orchestrator Agent 调度多个 Executor Agent 通过 Android CLI 协作", Icons.Default.Terminal) }
        items(state.orchestrationFrameworks) { fw -> OrchestrationFrameworkCard(fw, state.expandedFrameworkId == fw.id, { viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.ToggleFrameworkExpanded(fw.id)) }, { c -> viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.CopyCommand(c, fw.id)) }, { u -> viewModel.sendIntent(AndroidCLIExternalAgentToolkitIntent.OpenUrl(u)) }) }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun KbApiMethodCard(api: KbApiMethod, isExpanded: Boolean, onToggle: () -> Unit, onCopy: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onToggle() }, colors = CardDefaults.cardColors(containerColor = ToolColors.Surface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row {
                        Text(text = api.methodName, color = ToolColors.Primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "// ${api.methodNameCn}", color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(text = api.endpoint, color = ToolColors.Secondary, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
                Icon(imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = ToolColors.TextSecondary)
            }
            AnimatedVisibility(visible = isExpanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = ToolColors.Border)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Description / 描述:", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(text = api.descriptionCn, color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Parameters /Parameters / 参数:", color = ToolColors.Info, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    api.parametersCn.forEach { Text(text = "• $it", color = ToolColors.TextPrimary, style = MaterialTheme.typography.bodySmall) }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Code Example / 代码示例:", color = ToolColors.Info, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeBlockWithCopy(code = api.codeExample, onCopy = { onCopy(api.codeExample) })
                }
            }
        }
    }
}

@Composable
private fun OrchestrationFrameworkCard(fw: OrchestrationFramework, isExpanded: Boolean, onToggle: () -> Unit, onCopy: (String) -> Unit, onOpenUrl: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onToggle() }, colors = CardDefaults.cardColors(containerColor = ToolColors.Surface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = fw.frameworkName, color = ToolColors.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = fw.frameworkNameCn, color = ToolColors.Primary, style = MaterialTheme.typography.bodySmall)
                }
                Icon(imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = ToolColors.TextSecondary)
            }
            AnimatedVisibility(visible = isExpanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = ToolColors.Border)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Description / 描述:", color = ToolColors.Primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(text = fw.descriptionCn, color = ToolColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Components / 组件:", color = ToolColors.Info, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    fw.componentsCn.forEach { Row { Text(text = "▸", color = ToolColors.Secondary, fontFamily = FontFamily.Monospace, fontSize = 12.sp); Spacer(modifier = Modifier.width(8.dp)); Text(text = it, color = ToolColors.TextPrimary, style = MaterialTheme.typography.bodySmall) } }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Config Example / 配置示例:", color = ToolColors.Info, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeBlockWithCopy(code = fw.configExample, onCopy = { onCopy(fw.configExample) })
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { onOpenUrl(fw.url) }, colors = ButtonDefaults.buttonColors(containerColor = ToolColors.Primary), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "View Documentation / 查看文档", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlockWithCopy(code: String, onCopy: () -> Unit, compact: Boolean = false) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ToolColors.CodeBlock), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(if (compact) 8.dp else 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onCopy, modifier = Modifier.size(if (compact) 24.dp else 32.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy / 复制", tint = ToolColors.Primary, modifier = Modifier.size(if (compact) 14.dp else 18.dp))
                }
            }
            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Text(text = code, color = ToolColors.CodeString, fontFamily = FontFamily.Monospace, fontSize = if (compact) 11.sp else 12.sp, modifier = Modifier.padding(horizontal = if (compact) 8.dp else 12.dp, vertical = if (compact) 4.dp else 8.dp))
            }
        }
    }
}
