package com.mvi.kenny.feature.agentskillstoolkit

// ================================================================
// AgentSkillsToolkitScreen — Android Agent Skills 技能库生态工具包主界面
// ================================================================
// Main Screen for Android Agent Skills Ecosystem Toolkit.
//
// PRD-233: Android Agent Skills 技能库生态工具包
// Google 2026年4月推出 Android Agent Skills 生态系统
//
// 5-Tab Architecture:
//   Tab 0: SkillCreationScreen  — Skill 创作指南 + 模板选择器
//   Tab 1: SkillValidationScreen — Skill 验证扫描器 + 修复建议
//   Tab 2: CliGuideScreen       — CLI 命令教程 + 导入导出工具
//   Tab 3: KbCiScreen          — Knowledge Base 查询 + CI 插件配置
//   Tab 4: ShareEfficiencyScreen — 分享平台 + 效能评估
//
// Visual Style: Deep dark terminal theme (#0D1117 bg, #58A6FF primary, #3FB950 success)
// ================================================================

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.Flow

// ================================================================
// Theme Colors — 主题颜色（与现有工具一致）
// ================================================================
private object AgentSkillsColors {
    val Background = Color(0xFF0D1117)
    val Surface = Color(0xFF161B22)
    val SurfaceLight = Color(0xFF21262D)
    val Primary = Color(0xFF58A6FF)
    val Secondary = Color(0xFF3FB950)
    val Error = Color(0xFFF85149)
    val Warning = Color(0xFFD29922)
    val Info = Color(0xFFA371F7)
    val TextPrimary = Color(0xFFE6EDF3)
    val TextSecondary = Color(0xFF8B949E)
    val Border = Color(0xFF30363D)
    val CodeBlock = Color(0xFF0D1117)
    val CodeKeyword = Color(0xFFFF7B72)
    val CodeString = Color(0xFFA5D6FF)
    val CodeComment = Color(0xFF8B949E)
}

// ================================================================
// Tab Metadata — Tab 元数据
// ================================================================
private data class TabMeta(
    val title: String,
    val titleCn: String,
    val icon: ImageVector
)

private val tabMetaList = listOf(
    TabMeta("Create", "创作", Icons.Default.AutoAwesome),
    TabMeta("Validate", "验证", Icons.Default.CheckCircle),
    TabMeta("CLI Guide", "CLI指南", Icons.Default.Terminal),
    TabMeta("KB + CI", "知识库+CI", Icons.Default.Search),
    TabMeta("Share", "分享", Icons.Default.Share)
)

// ================================================================
// Main Screen Composable — 主界面
// ================================================================

/**
 * AgentSkillsToolkitScreen — Android Agent Skills 技能库生态工具包主界面
 * @param viewModel The MVI ViewModel
 * @param onUpdateTopBar Callback to update parent TopBar
 */
@Composable
fun AgentSkillsToolkitScreen(
    viewModel: AgentSkillsToolkitViewModel,
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Update TopBar / 更新顶部栏
    LaunchedEffect(state.selectedTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Agent Skills Toolkit",
                actions = emptyList()
            )
        )
    }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AgentSkillsToolkitEffect.ShowSnackbar -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AgentSkillsToolkitEffect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.text))
                    Toast.makeText(context, "Copied! / 已复制", Toast.LENGTH_SHORT).show()
                }
                is AgentSkillsToolkitEffect.OpenUrl -> {
                    // URL opening not implemented in demo / 演示中未实现 URL 打开
                }
                is AgentSkillsToolkitEffect.ShowSkillPreview -> {
                    // Show skill preview sheet / 显示 Skill 预览抽屉
                }
                is AgentSkillsToolkitEffect.ShowFixSuggestion -> {
                    // Show fix suggestion / 显示修复建议
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AgentSkillsColors.Background)
    ) {
        // Tab Row / Tab 切换栏
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = AgentSkillsColors.Surface,
            contentColor = AgentSkillsColors.Primary,
            edgePadding = 8.dp,
            divider = {}
        ) {
            tabMetaList.forEachIndexed { index, meta ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.sendIntent(AgentSkillsToolkitIntent.SelectTab(index)) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = meta.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(meta.title, color = AgentSkillsColors.TextPrimary)
                        }
                    },
                    selectedContentColor = AgentSkillsColors.Primary,
                    unselectedContentColor = AgentSkillsColors.TextSecondary
                )
            }
        }

        // Tab Content / Tab 内容区
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AgentSkillsColors.Background)
        ) {
            when (state.selectedTab) {
                0 -> SkillCreationTab(state, viewModel)
                1 -> SkillValidationTab(state, viewModel)
                2 -> CliGuideTab(state, viewModel)
                3 -> KbCiTab(state, viewModel)
                4 -> ShareEfficiencyTab(state, viewModel)
            }
        }
    }
}

// ================================================================
// Tab 0: Skill Creation — 技能创作
// ================================================================

@Composable
private fun SkillCreationTab(state: AgentSkillsToolkitState, viewModel: AgentSkillsToolkitViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header / 标题区
        item {
            SectionHeader(
                title = "Skill Creation Guide",
                titleCn = "Skill 创作指南",
                subtitle = "Choose a template and generate your Android Skill file / 选择模板并生成 Skill 文件",
                icon = Icons.Default.AutoAwesome
            )
        }

        // Skill Format Specification / Skill 格式规范
        item {
            SkillFormatCard()
        }

        // Template Selection / 模板选择
        item {
            Text(
                text = "Template Library / 模板库",
                style = MaterialTheme.typography.titleMedium,
                color = AgentSkillsColors.TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(state.templates) { template ->
            SkillTemplateCard(
                template = template,
                isSelected = state.selectedTemplate?.id == template.id,
                onSelect = { viewModel.sendIntent(AgentSkillsToolkitIntent.SelectTemplate(template)) },
                onGenerate = {
                    viewModel.sendIntent(AgentSkillsToolkitIntent.SelectTemplate(template))
                    viewModel.sendIntent(AgentSkillsToolkitIntent.GenerateSkillFile)
                }
            )
        }

        // Generation Success State / 生成成功状态
        if (state.generationSuccess) {
            item {
                GenerationSuccessCard(
                    templateName = state.selectedTemplate?.name ?: "",
                    onDismiss = { viewModel.sendIntent(AgentSkillsToolkitIntent.ClearGenerationState) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun SectionHeader(title: String, titleCn: String, subtitle: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(AgentSkillsColors.Primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = AgentSkillsColors.Primary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleLarge, color = AgentSkillsColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(text = titleCn, style = MaterialTheme.typography.bodyMedium, color = AgentSkillsColors.Primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = AgentSkillsColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun SkillFormatCard() {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AgentSkillsColors.Info, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Skill Format Specification / Skill 格式规范", color = AgentSkillsColors.TextPrimary, style = MaterialTheme.typography.titleMedium)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = AgentSkillsColors.TextSecondary)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    FormatSection("Description", "Skill 的简要描述，说明其用途和适用场景")
                    FormatSection("Trigger", "触发条件：on (文件模式) + when (任务条件)")
                    FormatSection("Instructions", "Agent 执行任务的分步指令")
                    FormatSection("Examples", "代码示例，展示 Skill 的典型用法")
                    FormatSection("Outputs", "Skill 执行的预期产物（生成的文件/修改的配置）")
                    FormatSection("Error Handling", "错误处理：常见错误及修复方法")
                }
            }
        }
    }
}

@Composable
private fun FormatSection(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("## $title", color = AgentSkillsColors.CodeKeyword, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        Text(desc, color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable
private fun SkillTemplateCard(
    template: SkillTemplate,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onGenerate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(2.dp, AgentSkillsColors.Primary, RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryChip(category = template.category)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(template.name, color = AgentSkillsColors.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(template.nameCn, color = AgentSkillsColors.Primary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(template.description, color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AgentSkillsColors.Warning, modifier = Modifier.size(16.dp))
                    Text("${template.rating}", color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(Icons.Default.Download, contentDescription = null, tint = AgentSkillsColors.TextSecondary, modifier = Modifier.size(14.dp))
                    Text("${template.usageCount}", color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                }
            }

            // Expand / Generate buttons / 展开/生成按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AgentSkillsColors.TextSecondary)
                ) {
                    Text(if (expanded) "Collapse" else "Preview")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onGenerate,
                    colors = ButtonDefaults.buttonColors(containerColor = AgentSkillsColors.Primary)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate")
                }
            }

            // Expanded preview / 展开预览
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = AgentSkillsColors.Border)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AgentSkillsColors.CodeBlock, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = template.content,
                            color = AgentSkillsColors.TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(category: SkillCategory) {
    val color = when (category) {
        SkillCategory.ROOM -> AgentSkillsColors.Error
        SkillCategory.COMPOSE -> AgentSkillsColors.Primary
        SkillCategory.AGP -> AgentSkillsColors.Warning
        SkillCategory.TESTING -> AgentSkillsColors.Secondary
        SkillCategory.KSP -> AgentSkillsColors.Info
        SkillCategory.ARCH -> Color(0xFFFF914D)
        SkillCategory.OTHER -> AgentSkillsColors.TextSecondary
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(category.label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GenerationSuccessCard(templateName: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Secondary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AgentSkillsColors.Secondary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Skill file generated! / Skill 文件生成成功", color = AgentSkillsColors.Secondary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("~/.skills/$templateName.md", color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onDismiss, colors = ButtonDefaults.outlinedButtonColors(contentColor = AgentSkillsColors.Secondary)) {
                Text("Dismiss")
            }
        }
    }
}

// ================================================================
// Tab 1: Skill Validation Scanner — 技能验证扫描器
// ================================================================

@Composable
private fun SkillValidationTab(state: AgentSkillsToolkitState, viewModel: AgentSkillsToolkitViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Skill Validation Scanner",
                titleCn = "Skill 验证扫描器",
                subtitle = "Scan .skills/ directory and validate Skill file format / 扫描 .skills/ 目录并验证 Skill 文件格式",
                icon = Icons.Default.CheckCircle
            )
        }

        // Path Input / 路径输入
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Scan Directory / 扫描目录", color = AgentSkillsColors.TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.validationPath,
                        onValueChange = { viewModel.sendIntent(AgentSkillsToolkitIntent.UpdateValidationPath(it)) },
                        placeholder = { Text("~/.skills/ or ./project/.skills/", color = AgentSkillsColors.TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AgentSkillsColors.Primary,
                            unfocusedBorderColor = AgentSkillsColors.Border,
                            focusedTextColor = AgentSkillsColors.TextPrimary,
                            unfocusedTextColor = AgentSkillsColors.TextPrimary,
                            cursorColor = AgentSkillsColors.Primary
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.sendIntent(AgentSkillsToolkitIntent.StartScan) },
                        enabled = !state.isScanning,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AgentSkillsColors.Primary)
                    ) {
                        if (state.isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AgentSkillsColors.TextPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scanning... / 扫描中...")
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Scan / 开始扫描")
                        }
                    }
                }
            }
        }

        // Progress / 进度
        if (state.isScanning) {
            item {
                ScanProgressCard(phase = state.validationPhase, progress = state.scanProgress)
            }
        }

        // Results / 结果列表
        if (state.validationResults.isNotEmpty()) {
            item {
                Text(
                    "Validation Results / 验证结果 (${state.validationResults.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = AgentSkillsColors.TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Summary chips / 摘要标签
            item {
                val errors = state.validationResults.count { it.severity == ValidationSeverity.ERROR }
                val warnings = state.validationResults.count { it.severity == ValidationSeverity.WARNING }
                val infos = state.validationResults.count { it.severity == ValidationSeverity.INFO }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (errors > 0) SeverityChip(count = errors, label = "Errors", color = AgentSkillsColors.Error)
                    if (warnings > 0) SeverityChip(count = warnings, label = "Warnings", color = AgentSkillsColors.Warning)
                    if (infos > 0) SeverityChip(count = infos, label = "Info", color = AgentSkillsColors.Info)
                }
            }

            items(state.validationResults) { result ->
                ValidationResultCard(
                    result = result,
                    isExpanded = state.expandedResultId == result.id,
                    onToggle = { viewModel.sendIntent(AgentSkillsToolkitIntent.ToggleResultExpanded(result.id)) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun ScanProgressCard(phase: ValidationPhase, progress: Float) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Scanning / 扫描中", color = AgentSkillsColors.TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("${(progress * 100).toInt()}%", color = AgentSkillsColors.Primary, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AgentSkillsColors.Primary,
                trackColor = AgentSkillsColors.Border
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (phase) {
                    ValidationPhase.PARSING -> "Parsing Skill files / 解析 Skill 文件"
                    ValidationPhase.ANALYZING -> "Analyzing structure / 分析结构"
                    ValidationPhase.GENERATING_REPORT -> "Generating report / 生成报告"
                    else -> "Preparing..."
                },
                color = AgentSkillsColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SeverityChip(count: Int, label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text("$count $label", color = color, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ValidationResultCard(result: ValidationResult, isExpanded: Boolean, onToggle: () -> Unit) {
    val borderColor = when (result.severity) {
        ValidationSeverity.ERROR -> AgentSkillsColors.Error
        ValidationSeverity.WARNING -> AgentSkillsColors.Warning
        ValidationSeverity.INFO -> AgentSkillsColors.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(borderColor, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "${result.file}:${result.line}",
                            color = AgentSkillsColors.TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "[${result.category.labelCn}]",
                            color = AgentSkillsColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                SeverityBadge(severity = result.severity)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AgentSkillsColors.TextSecondary
                )
            }

            Text(
                result.messageCn,
                color = AgentSkillsColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = AgentSkillsColors.Border)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("修复建议 / Fix:", color = AgentSkillsColors.Warning, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AgentSkillsColors.CodeBlock, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            result.fixSuggestionCn,
                            color = AgentSkillsColors.TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeverityBadge(severity: ValidationSeverity) {
    val (color, label) = when (severity) {
        ValidationSeverity.ERROR -> AgentSkillsColors.Error to "ERROR"
        ValidationSeverity.WARNING -> AgentSkillsColors.Warning to "WARNING"
        ValidationSeverity.INFO -> AgentSkillsColors.Info to "INFO"
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ================================================================
// Tab 2: CLI Guide — CLI 命令教程
// ================================================================

@Composable
private fun CliGuideTab(state: AgentSkillsToolkitState, viewModel: AgentSkillsToolkitViewModel) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Android CLI Skills Guide",
                titleCn = "Android CLI Skills 命令指南",
                subtitle = "Master android skills commands for managing Agent Skills / 掌握 android skills 命令管理 Agent Skills",
                icon = Icons.Default.Terminal
            )
        }

        // Section selector / 子章节选择
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CliSection.entries) { section ->
                    FilterChip(
                        selected = state.cliSection == section,
                        onClick = { viewModel.sendIntent(AgentSkillsToolkitIntent.SelectCliSection(section)) },
                        label = { Text(section.titleCn) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgentSkillsColors.Primary.copy(alpha = 0.2f),
                            selectedLabelColor = AgentSkillsColors.Primary,
                            containerColor = AgentSkillsColors.Surface,
                            labelColor = AgentSkillsColors.TextSecondary
                        )
                    )
                }
            }
        }

        // Current section content / 当前章节内容
        val commands = state.cliCommands.filter { it.section == state.cliSection }
        items(commands) { cmd ->
            CliCommandCard(command = cmd)
        }

        // Private registry config / 私有 registry 配置
        item {
            PrivateRegistryCard()
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun CliCommandCard(command: CliCommand) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(command.command, color = AgentSkillsColors.Primary, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(command.descriptionCn, color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("command", command.command))
                        Toast.makeText(context, "Copied! / 已复制", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AgentSkillsColors.Primary, modifier = Modifier.size(20.dp))
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = AgentSkillsColors.Border)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Examples / 示例:", color = AgentSkillsColors.Info, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    command.examplesCn.forEach { (cmd, desc) ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(AgentSkillsColors.CodeBlock, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(cmd, color = AgentSkillsColors.CodeString, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(desc, color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivateRegistryCard() {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.SurfaceLight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = AgentSkillsColors.Warning, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Private Registry Config / 私有 Registry 配置", color = AgentSkillsColors.TextPrimary, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Configure ~/.android/config.toml for private Skill registries / 配置 ~/.android/config.toml 用于私有 Skill registry", color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = AgentSkillsColors.Border)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AgentSkillsColors.CodeBlock, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            """# ~/.android/config.toml
[skills]
registry = "https://my-private-registry.example.com"
token = "your-registry-token"

[skills.private]
enabled = true
autoSync = true""",
                            color = AgentSkillsColors.CodeString,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ================================================================
// Tab 3: Knowledge Base + CI — 知识库查询 + CI 插件配置
// ================================================================

@Composable
private fun KbCiTab(state: AgentSkillsToolkitState, viewModel: AgentSkillsToolkitViewModel) {
    var activeSubTab by remember { mutableStateOf(0) } // 0: KB, 1: CI

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-tab row / 子 Tab 栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AgentSkillsColors.Surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                label = { Text("Knowledge Base", color = AgentSkillsColors.TextPrimary) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AgentSkillsColors.Primary.copy(alpha = 0.2f),
                    selectedLabelColor = AgentSkillsColors.Primary,
                    containerColor = AgentSkillsColors.SurfaceLight,
                    labelColor = AgentSkillsColors.TextSecondary
                )
            )
            FilterChip(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                label = { Text("CI Plugin", color = AgentSkillsColors.TextPrimary) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AgentSkillsColors.Primary.copy(alpha = 0.2f),
                    selectedLabelColor = AgentSkillsColors.Primary,
                    containerColor = AgentSkillsColors.SurfaceLight,
                    labelColor = AgentSkillsColors.TextSecondary
                )
            )
        }

        // Content / 内容区
        when (activeSubTab) {
            0 -> KnowledgeBaseTab(state, viewModel)
            1 -> CiPluginTab(state, viewModel)
        }
    }
}

@Composable
private fun KnowledgeBaseTab(state: AgentSkillsToolkitState, viewModel: AgentSkillsToolkitViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AgentSkillsColors.Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Android Knowledge Base", color = AgentSkillsColors.TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("查询 Android 官方文档和 Agent 开发规范 / Query Android official docs and Agent development specs", color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.kbQuery,
                        onValueChange = { viewModel.sendIntent(AgentSkillsToolkitIntent.UpdateKbQuery(it)) },
                        placeholder = { Text("Search Android KB... / 搜索 Android 知识库...", color = AgentSkillsColors.TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AgentSkillsColors.Primary,
                            unfocusedBorderColor = AgentSkillsColors.Border,
                            focusedTextColor = AgentSkillsColors.TextPrimary,
                            unfocusedTextColor = AgentSkillsColors.TextPrimary,
                            cursorColor = AgentSkillsColors.Primary
                        ),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { viewModel.sendIntent(AgentSkillsToolkitIntent.SearchKnowledgeBase) },
                                enabled = !state.isSearchingKb
                            ) {
                                if (state.isSearchingKb) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AgentSkillsColors.Primary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = AgentSkillsColors.Primary)
                                }
                            }
                        }
                    )
                }
            }
        }

        if (state.kbResults.isNotEmpty()) {
            items(state.kbResults) { result ->
                KbResultCard(result = result)
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun KbResultCard(result: KbResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(result.titleCn, color = AgentSkillsColors.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(AgentSkillsColors.Secondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("${(result.relevance * 100).toInt()}%", color = AgentSkillsColors.Secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(result.snippetCn, color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Link, contentDescription = null, tint = AgentSkillsColors.Info, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(result.url, color = AgentSkillsColors.Info, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun CiPluginTab(state: AgentSkillsToolkitState, viewModel: AgentSkillsToolkitViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Agent Skills CI Plugin",
                titleCn = "Agent Skills CI 验证插件",
                subtitle = "Gradle plugin to validate .skills/ directory format in CI pipelines / Gradle 插件，验证 CI 流水线中 .skills/ 目录格式",
                icon = Icons.Default.CheckCircle
            )
        }

        items(state.ciSteps) { step ->
            CiStepCard(
                step = step,
                isCompleted = step.index in state.completedSteps,
                onToggle = { viewModel.sendIntent(AgentSkillsToolkitIntent.ToggleCiStep(step.index)) }
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun CiStepCard(step: CiStep, isCompleted: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCompleted) Modifier.border(2.dp, AgentSkillsColors.Secondary, RoundedCornerShape(12.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { onToggle() },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(
                            checkedColor = AgentSkillsColors.Secondary,
                            uncheckedColor = AgentSkillsColors.Border
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Step ${step.index + 1}: ${step.titleCn}", color = AgentSkillsColors.TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(step.descriptionCn, color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (isCompleted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AgentSkillsColors.Secondary, modifier = Modifier.size(24.dp))
                }
            }

            if (step.codeSnippet.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("code", step.codeSnippet))
                            Toast.makeText(context, "Copied! / 已复制", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AgentSkillsColors.Primary, modifier = Modifier.size(18.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AgentSkillsColors.CodeBlock, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        step.codeSnippet,
                        color = AgentSkillsColors.CodeString,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ================================================================
// Tab 4: Share + Efficiency — 分享平台 + 效能评估
// ================================================================

@Composable
private fun ShareEfficiencyTab(state: AgentSkillsToolkitState, viewModel: AgentSkillsToolkitViewModel) {
    var activeSubTab by remember { mutableStateOf(0) } // 0: Share, 1: Efficiency

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-tab row / 子 Tab 栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AgentSkillsColors.Surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                label = { Text("Share Platform / 分享平台", color = AgentSkillsColors.TextPrimary) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AgentSkillsColors.Primary.copy(alpha = 0.2f),
                    selectedLabelColor = AgentSkillsColors.Primary,
                    containerColor = AgentSkillsColors.SurfaceLight,
                    labelColor = AgentSkillsColors.TextSecondary
                )
            )
            FilterChip(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                label = { Text("Efficiency / 效能", color = AgentSkillsColors.TextPrimary) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AgentSkillsColors.Primary.copy(alpha = 0.2f),
                    selectedLabelColor = AgentSkillsColors.Primary,
                    containerColor = AgentSkillsColors.SurfaceLight,
                    labelColor = AgentSkillsColors.TextSecondary
                )
            )
        }

        // Content / 内容区
        when (activeSubTab) {
            0 -> SharePlatformTab(state, viewModel)
            1 -> EfficiencyTab(state, viewModel)
        }
    }
}

@Composable
private fun SharePlatformTab(state: AgentSkillsToolkitState, viewModel: AgentSkillsToolkitViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Skill Share Platform",
                titleCn = "Skill 分享平台",
                subtitle = "Discover, share and rate Android Skills with the community / 发现、分享和评分 Android Skills，与社区互动",
                icon = Icons.Default.Share
            )
        }

        // Category filter / 分类筛选
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.shareFilter == null,
                        onClick = { viewModel.sendIntent(AgentSkillsToolkitIntent.FilterShareSkills(null)) },
                        label = { Text("All", color = AgentSkillsColors.TextPrimary) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgentSkillsColors.Primary.copy(alpha = 0.2f),
                            selectedLabelColor = AgentSkillsColors.Primary,
                            containerColor = AgentSkillsColors.Surface,
                            labelColor = AgentSkillsColors.TextSecondary
                        )
                    )
                }
                items(SkillCategory.entries) { cat ->
                    FilterChip(
                        selected = state.shareFilter == cat,
                        onClick = { viewModel.sendIntent(AgentSkillsToolkitIntent.FilterShareSkills(cat)) },
                        label = { Text(cat.labelCn, color = AgentSkillsColors.TextPrimary) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgentSkillsColors.Primary.copy(alpha = 0.2f),
                            selectedLabelColor = AgentSkillsColors.Primary,
                            containerColor = AgentSkillsColors.Surface,
                            labelColor = AgentSkillsColors.TextSecondary
                        )
                    )
                }
            }
        }

        // Skill list / Skill 列表
        val filtered = if (state.shareFilter == null) state.sharedSkills
                        else state.sharedSkills.filter { it.category == state.shareFilter }
        items(filtered) { skill ->
            SharedSkillCard(skill = skill)
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun SharedSkillCard(skill: SharedSkill) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryChip(category = skill.category)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(skill.name, color = AgentSkillsColors.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(skill.nameCn, color = AgentSkillsColors.Primary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(skill.description, color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        skill.tags.take(3).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .background(AgentSkillsColors.SurfaceLight, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(tag, color = AgentSkillsColors.TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AgentSkillsColors.Warning, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("${skill.rating}", color = AgentSkillsColors.TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = AgentSkillsColors.TextSecondary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("${skill.downloadCount}", color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = AgentSkillsColors.Border)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("by ${skill.author}", color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                Button(
                    onClick = { /* Download action */ },
                    colors = ButtonDefaults.buttonColors(containerColor = AgentSkillsColors.Primary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Install", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EfficiencyTab(state: AgentSkillsToolkitState, viewModel: AgentSkillsToolkitViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Skill Efficiency Evaluation",
                titleCn = "Skill 效能评估",
                subtitle = "Measure Agent task completion rate, accuracy, and coverage when using Skills / 测量 Agent 使用 Skill 后的任务完成率、准确率和覆盖率",
                icon = Icons.Default.Speed
            )
        }

        // Input card / 输入卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Input / 输入", color = AgentSkillsColors.TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.efficiencyInput.skillPath,
                        onValueChange = {
                            viewModel.sendIntent(AgentSkillsToolkitIntent.UpdateEfficiencyInput(state.efficiencyInput.copy(skillPath = it)))
                        },
                        label = { Text("Skill Path / Skill 路径", color = AgentSkillsColors.TextSecondary) },
                        placeholder = { Text("~/.skills/room-migration.md", color = AgentSkillsColors.TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AgentSkillsColors.Primary,
                            unfocusedBorderColor = AgentSkillsColors.Border,
                            focusedTextColor = AgentSkillsColors.TextPrimary,
                            unfocusedTextColor = AgentSkillsColors.TextPrimary,
                            cursorColor = AgentSkillsColors.Primary
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.efficiencyInput.taskDescription,
                        onValueChange = {
                            viewModel.sendIntent(AgentSkillsToolkitIntent.UpdateEfficiencyInput(state.efficiencyInput.copy(taskDescription = it)))
                        },
                        label = { Text("Task Description / 任务描述", color = AgentSkillsColors.TextSecondary) },
                        placeholder = { Text("Migrate Room database from v1 to v2", color = AgentSkillsColors.TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AgentSkillsColors.Primary,
                            unfocusedBorderColor = AgentSkillsColors.Border,
                            focusedTextColor = AgentSkillsColors.TextPrimary,
                            unfocusedTextColor = AgentSkillsColors.TextPrimary,
                            cursorColor = AgentSkillsColors.Primary
                        ),
                        minLines = 2,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.sendIntent(AgentSkillsToolkitIntent.GenerateEfficiencyReport) },
                        enabled = !state.isGeneratingReport,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AgentSkillsColors.Primary)
                    ) {
                        if (state.isGeneratingReport) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AgentSkillsColors.TextPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing... / 分析中...")
                        } else {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Report / 生成报告")
                        }
                    }
                }
            }
        }

        // Report / 报告
        if (state.efficiencyReport != null) {
            item {
                EfficiencyReportCard(report = state.efficiencyReport!!)
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun EfficiencyReportCard(report: EfficiencyReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AgentSkillsColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = AgentSkillsColors.Secondary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Efficiency Report / 效能报告", color = AgentSkillsColors.TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(report.skillName, color = AgentSkillsColors.Primary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = AgentSkillsColors.Border)
            Spacer(modifier = Modifier.height(16.dp))

            // Score grid / 评分网格
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EfficiencyMetric(label = "Overall", labelCn = "综合评分", value = report.overallScore, unit = "%", color = AgentSkillsColors.Primary)
                EfficiencyMetric(label = "Completion", labelCn = "完成率", value = report.taskCompletionRate, unit = "%", color = AgentSkillsColors.Secondary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EfficiencyMetric(label = "Accuracy", labelCn = "准确率", value = report.accuracyScore, unit = "%", color = AgentSkillsColors.Info)
                EfficiencyMetric(label = "Coverage", labelCn = "覆盖率", value = report.coverageScore, unit = "%", color = AgentSkillsColors.Warning)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                EfficiencyMetric(label = "Avg Time", labelCn = "平均耗时", value = report.avgTimeMinutes, unit = "min", color = AgentSkillsColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun EfficiencyMetric(label: String, labelCn: String, value: Float, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$label / $labelCn", color = AgentSkillsColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "${value.toInt()}$unit",
            color = color,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
