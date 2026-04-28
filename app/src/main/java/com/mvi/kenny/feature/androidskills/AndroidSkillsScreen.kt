package com.mvi.kenny.feature.androidskills

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// =============================================================
// AndroidSkillsScreen — Android CLI & Android Skills 工具包主界面
// PRD-184: Android CLI & Android Skills 工具包
// =============================================================
/**
 * Main screen for Android Skills Toolkit / Android Skills 工具包主界面
 *
 * Implements a tab-based navigation with 9 sub-tools:
 * - Skill Creator: Create new Android Skills
 * - Skill Publisher: Publish skills to GitHub
 * - Skill Audit: Validate and score skill quality
 * - Templates: SKILL.md templates library
 * - Agent Matrix: AI Agent capability comparison
 * - CLI Guide: Android CLI task encapsulation
 * - MCP Guide: Skills × MCP Server integration
 * - Version Management: SemVer & changelog
 * - Quick Start: Getting started guide
 *
 * @param state Current UI state / 当前 UI 状态
 * @param onIntent Intent handler / 意图处理器
 * @param effect Effect collector / 副作用收集器
 */

@Composable
fun AndroidSkillsScreen(
    state: AndroidSkillsState,
    onIntent: (AndroidSkillsIntent) -> Unit,
    effect: kotlinx.coroutines.flow.SharedFlow<AndroidSkillsEffect>
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        effect.collectLatest { eff ->
            when (eff) {
                is AndroidSkillsEffect.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(eff.message)
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab row / Tab 栏
            SkillTabRow(
                activeTab = state.activeTab,
                onTabSelected = { onIntent(AndroidSkillsIntent.SelectTab(it)) }
            )

            // Tab content / Tab 内容
            AnimatedContent(
                targetState = state.activeTab,
                modifier = Modifier.weight(1f),
                label = "tab_content"
            ) { tab ->
                when (tab) {
                    AndroidSkillsTab.SKILL_CREATOR -> SkillCreatorContent(
                        state = state.skillCreatorState,
                        onIntent = { onIntent(AndroidSkillsIntent.ForwardToSkillCreator(it)) }
                    )
                    AndroidSkillsTab.SKILL_PUBLISHER -> PublisherContent()
                    AndroidSkillsTab.SKILL_AUDIT -> AuditContent()
                    AndroidSkillsTab.TEMPLATES -> TemplatesContent(
                        onSelectTemplate = { onIntent(AndroidSkillsIntent.ForwardToSkillCreator(SkillCreatorIntent.SelectTemplate(it))) }
                    )
                    AndroidSkillsTab.AGENT_MATRIX -> AgentMatrixContent()
                    AndroidSkillsTab.CLI_GUIDE -> CLIGuideContent()
                    AndroidSkillsTab.MCP_GUIDE -> MCPGuideContent()
                    AndroidSkillsTab.VERSION_MGMT -> VersionMgmtContent()
                    AndroidSkillsTab.QUICK_START -> QuickStartContent()
                }
            }
        }

        // Snackbar host / Snackbar 容器
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// =============================================================
// SkillTabRow — Skill Tab 栏
// =============================================================
@Composable
private fun SkillTabRow(
    activeTab: AndroidSkillsTab,
    onTabSelected: (AndroidSkillsTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = AndroidSkillsTab.entries.indexOf(activeTab),
        edgePadding = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        AndroidSkillsTab.entries.forEach { tab ->
            Tab(
                selected = activeTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = getTabIcon(tab),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

// =============================================================
// SkillCreatorContent — Skill Creator 内容区
// =============================================================
@Composable
private fun SkillCreatorContent(
    state: SkillCreatorState,
    onIntent: (SkillCreatorIntent) -> Unit
) {
    val skillCreatorEffect = remember { kotlinx.coroutines.flow.MutableSharedFlow<SkillCreatorEffect>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        skillCreatorEffect.collectLatest { eff ->
            when (eff) {
                is SkillCreatorEffect.ShowSnackbar -> {
                    scope.launch { snackbarHostState.showSnackbar(eff.message) }
                }
                is SkillCreatorEffect.ShowAuditWarning -> {
                    scope.launch { snackbarHostState.showSnackbar("⚠️ ${eff.message}") }
                }
                is SkillCreatorEffect.NavigateToPublish -> {
                    scope.launch { snackbarHostState.showSnackbar("Navigate to: ${eff.repoUrl}") }
                }
                is SkillCreatorEffect.ShowQualityScore -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Quality Score: ${eff.score.overall}/100 (${eff.score.grade.label})"
                        )
                    }
                }
                is SkillCreatorEffect.CopySuccess -> {
                    scope.launch { snackbarHostState.showSnackbar("Copied!") }
                }
                is SkillCreatorEffect.ExportSuccess -> {
                    scope.launch { snackbarHostState.showSnackbar("Exported to: ${eff.path}") }
                }
                is SkillCreatorEffect.PublishSuccess -> {
                    scope.launch { snackbarHostState.showSnackbar("Published: ${eff.repoUrl}") }
                }
            }
        }
    }

    var newCondition by remember { mutableStateOf("") }
    var showGeneratedMd by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Skill Type Selection / Skill 类型选择
            item {
                SectionHeader(title = "1. Select Skill Type / 选择 Skill 类型")
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkillType.entries.forEach { type ->
                        FilterChip(
                            selected = state.skillType == type,
                            onClick = { onIntent(SkillCreatorIntent.SelectSkillType(type)) },
                            label = { Text(type.label, maxLines = 1) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getSkillTypeIcon(type),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Section: Skill Name & Description / Skill 名称和描述
            item {
                SectionHeader(title = "2. Skill Name & Description / Skill 名称和描述")
            }

            item {
                OutlinedTextField(
                    value = state.skillName,
                    onValueChange = { onIntent(SkillCreatorIntent.UpdateSkillName(it)) },
                    label = { Text("Skill Name / Skill 名称") },
                    placeholder = { Text("e.g., XML to Compose Migration") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = state.skillDescription,
                    onValueChange = { onIntent(SkillCreatorIntent.UpdateSkillDescription(it)) },
                    label = { Text("Description / 描述 (optional)") },
                    placeholder = { Text("Brief description of this skill...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }

            // Section: Trigger Conditions / 触发条件
            item {
                SectionHeader(title = "3. Trigger Conditions / 触发条件")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCondition,
                        onValueChange = { newCondition = it },
                        label = { Text("Add trigger condition / 添加触发条件") },
                        placeholder = { Text("e.g., user asks about XML to Compose migration") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCondition.isNotBlank()) {
                                onIntent(SkillCreatorIntent.AddTriggerCondition(newCondition))
                                newCondition = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }

            items(state.triggerConditions) { condition ->
                ChipItem(
                    label = condition,
                    onDelete = { onIntent(SkillCreatorIntent.RemoveTriggerCondition(condition)) }
                )
            }

            // Section: Tools / 工具
            item {
                SectionHeader(title = "4. Tools / 工具")
            }

            item {
                Text(
                    text = "Select from available tools / 从可用工具中选择",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allTools = listOf(
                        SkillTool("file_reader", "File Reader", "Read files / 读取文件"),
                        SkillTool("file_writer", "File Writer", "Write files / 写入文件", PermissionLevel.WRITE),
                        SkillTool("grep", "Grep Search", "Search patterns / 搜索模式"),
                        SkillTool("shell_exec", "Shell Executor", "Execute commands / 执行命令", PermissionLevel.EXECUTE),
                        SkillTool("gh_cli", "GitHub CLI", "GitHub operations / GitHub 操作", PermissionLevel.EXECUTE),
                        SkillTool("lint_check", "Lint Check", "Run lint / 运行 lint"),
                        SkillTool("adb", "ADB", "Android Debug Bridge / Android 调试桥", PermissionLevel.EXECUTE)
                    )
                    allTools.forEach { tool ->
                        val isSelected = state.tools.any { it.id == tool.id }
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    onIntent(SkillCreatorIntent.RemoveTool(tool))
                                } else {
                                    onIntent(SkillCreatorIntent.AddTool(tool))
                                }
                            },
                            label = { Text(tool.name, maxLines = 1) }
                        )
                    }
                }
            }

            if (state.tools.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.tools.forEach { tool ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${tool.name}",
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = tool.permissionLevel.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = { onIntent(SkillCreatorIntent.RemoveTool(tool)) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section: Workflow / 工作流
            item {
                SectionHeader(title = "5. Workflow / 工作流")
            }

            if (state.workflow.isNotEmpty()) {
                items(state.workflow.size) { index ->
                    val step = state.workflow[index]
                    WorkflowStepItem(
                        step = step,
                        onRemove = { onIntent(SkillCreatorIntent.RemoveWorkflowStep(index)) }
                    )
                }
            } else {
                item {
                    Text(
                        text = "Select a skill type to auto-populate workflow / 选择 Skill 类型以自动填充工作流",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Section: Actions / 操作
            item {
                SectionHeader(title = "6. Actions / 操作")
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onIntent(SkillCreatorIntent.GenerateSkillMd) },
                            modifier = Modifier.weight(1f),
                            enabled = state.skillType != null && state.skillName.isNotBlank()
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generate SKILL.md")
                        }

                        OutlinedButton(
                            onClick = { onIntent(SkillCreatorIntent.CopyToClipboard) },
                            modifier = Modifier.weight(1f),
                            enabled = state.generatedSkillMd.isNotBlank()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onIntent(SkillCreatorIntent.RunAudit) },
                            modifier = Modifier.weight(1f),
                            enabled = !state.isRunningAudit
                        ) {
                            if (state.isRunningAudit) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (state.isRunningAudit) "Auditing..." else "Run Audit")
                        }

                        OutlinedButton(
                            onClick = { showGeneratedMd = true },
                            modifier = Modifier.weight(1f),
                            enabled = state.generatedSkillMd.isNotBlank()
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview")
                        }
                    }

                    Button(
                        onClick = { onIntent(SkillCreatorIntent.Publish) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.canPublish && !state.isPublishing,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        if (state.isPublishing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (state.isPublishing) "Publishing..." else "Publish to GitHub")
                    }
                }
            }

            // Section: Audit Result / 审核结果
            if (state.auditResult != null || state.qualityScore != null) {
                item {
                    SectionHeader(title = "Audit Result / 审核结果")
                }

                if (state.qualityScore != null) {
                    item {
                        QualityScoreCard(score = state.qualityScore)
                    }
                }

                if (state.auditResult != null) {
                    item {
                        AuditResultCard(result = state.auditResult!!)
                    }
                }
            }

            // Clear button / 清除按钮
            item {
                TextButton(
                    onClick = { onIntent(SkillCreatorIntent.ClearAll) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All / 清除所有")
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Generated SKILL.md preview dialog / 生成的 SKILL.md 预览对话框
    if (showGeneratedMd && state.generatedSkillMd.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { showGeneratedMd = false },
            title = { Text("Generated SKILL.md / 生成的 SKILL.md") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = state.generatedSkillMd,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showGeneratedMd = false }) {
                    Text("Close / 关闭")
                }
            }
        )
    }
}

// =============================================================
// PublisherContent — Skill Publisher 内容区
// =============================================================
@Composable
private fun PublisherContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "Skill Publisher / Skill 发布器")
        }

        item {
            InfoCard(
                title = "GitHub Release Flow / GitHub 发布流程",
                icon = Icons.Default.Publish,
                items = listOf(
                    "1. Create Skill repository with gh CLI",
                    "2. Write SKILL.md following the template",
                    "3. Tag version with git tag v1.0.0",
                    "4. Push to GitHub: git push origin v1.0.0",
                    "5. Install to Agent: gh skill install owner/repo@v1.0.0"
                )
            )
        }

        item {
            var repoName by remember { mutableStateOf("") }
            var version by remember { mutableStateOf("v1.0.0") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Quick Publish / 快速发布", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = repoName,
                        onValueChange = { repoName = it },
                        label = { Text("Repository Name / 仓库名称") },
                        placeholder = { Text("e.g., xml-to-compose-migration") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = version,
                        onValueChange = { version = it },
                        label = { Text("Version / 版本") },
                        placeholder = { Text("e.g., v1.0.0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* Simulate publish */ },
                            modifier = Modifier.weight(1f),
                            enabled = repoName.isNotBlank()
                        ) {
                            Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Publish")
                        }
                        OutlinedButton(onClick = { repoName = ""; version = "v1.0.0" }) {
                            Text("Clear")
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Version Commands / 版本命令")
        }

        item {
            CommandCard(
                commands = listOf(
                    "gh repo create my-android-skill --public",
                    "git tag v1.0.0",
                    "git push origin v1.0.0",
                    "gh skill install owner/repo@v1.0.0",
                    "gh skill list"
                )
            )
        }
    }
}

// =============================================================
// AuditContent — Skill Audit 内容区
// =============================================================
@Composable
private fun AuditContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "Skill Quality Audit / Skill 质量审核")
        }

        item {
            InfoCard(
                title = "Quality Dimensions / 质量维度",
                icon = Icons.Default.FactCheck,
                items = listOf(
                    "Coverage (30%): Trigger conditions, tools, workflow completeness",
                    "Accuracy (30%): Naming, description, trigger precision",
                    "Maintainability (20%): Workflow clarity, tool organization",
                    "Documentation (20%): SKILL.md completeness, examples"
                )
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Score Ranges / 评分范围", fontWeight = FontWeight.Bold)
                    ScoreRangeRow("90-100", "Excellent", Color(0xFF4CAF50))
                    ScoreRangeRow("75-89", "Good", Color(0xFF8BC34A))
                    ScoreRangeRow("60-74", "Fair", Color(0xFFFF9800))
                    ScoreRangeRow("0-59", "Needs Improvement", Color(0xFFF44336))
                }
            }
        }

        item {
            var skillMdPath by remember { mutableStateOf("") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Audit SKILL.md / 审核 SKILL.md", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = skillMdPath,
                        onValueChange = { skillMdPath = it },
                        label = { Text("SKILL.md Path / SKILL.md 路径") },
                        placeholder = { Text("/path/to/SKILL.md") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = { /* Simulate audit */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = skillMdPath.isNotBlank()
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run Audit / 运行审核")
                    }
                }
            }
        }
    }
}

// =============================================================
// TemplatesContent — Skill Templates 内容区
// =============================================================
@Composable
private fun TemplatesContent(
    onSelectTemplate: (SkillTemplate) -> Unit
) {
    val templates = remember {
        listOf(
            SkillTemplate(
                id = "migration_xml_compose",
                name = "XML to Compose Migration",
                description = "Migrate Android XML layouts to Jetpack Compose",
                category = SkillType.MIGRATION,
                fileName = "SKILL.md",
                content = "",
                tags = listOf("migration", "xml", "compose", "layout")
            ),
            SkillTemplate(
                id = "optimization_perf",
                name = "Performance Optimization",
                description = "Optimize Android app performance and reduce ANR",
                category = SkillType.OPTIMIZATION,
                fileName = "SKILL.md",
                content = "",
                tags = listOf("performance", "optimization", "profiling")
            ),
            SkillTemplate(
                id = "debug_r8",
                name = "R8 Analyzer",
                description = "Analyze R8/D8 compilation output and debugging",
                category = SkillType.DEBUGGING,
                fileName = "SKILL.md",
                content = "",
                tags = listOf("r8", "debugging", "proguard", "obfuscation")
            ),
            SkillTemplate(
                id = "ci_github_actions",
                name = "CI Configuration",
                description = "Set up GitHub Actions CI pipeline for Android",
                category = SkillType.CI_CONFIG,
                fileName = "SKILL.md",
                content = "",
                tags = listOf("ci", "github-actions", "automation")
            ),
            SkillTemplate(
                id = "skill_template",
                name = "SKILL.md Template",
                description = "Standard SKILL.md template for Android Skills",
                category = SkillType.DOCUMENTATION,
                fileName = "SKILL.md",
                content = "",
                tags = listOf("template", "documentation", "skill")
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "Skill Templates / Skill 模板库")
        }

        items(templates) { template ->
            TemplateCard(
                template = template,
                onUse = { onSelectTemplate(template) }
            )
        }
    }
}

// =============================================================
// AgentMatrixContent — Agent Capability Matrix 内容区
// =============================================================
@Composable
private fun AgentMatrixContent() {
    val agents = remember {
        listOf(
            AgentInfo(
                name = "Claude Code",
                supportedFeatures = listOf("gh skill CLI", "SKILL.md (root)", "Local skills", "MCP servers"),
                skillFormat = "SKILL.md (Claude Code format)",
                installCommand = "gh skill install owner/repo"
            ),
            AgentInfo(
                name = "Cursor",
                supportedFeatures = listOf("gh skill CLI", "SKILL.md", "Local skills"),
                skillFormat = "SKILL.md (Cursor format)",
                installCommand = "gh skill install owner/repo"
            ),
            AgentInfo(
                name = "Gemini CLI",
                supportedFeatures = listOf("gh skill CLI", "SKILL.md", "Local skills", "MCP servers"),
                skillFormat = "SKILL.md (Gemini format)",
                installCommand = "gh skill install owner/repo"
            ),
            AgentInfo(
                name = "GitHub Copilot",
                supportedFeatures = listOf("gh skill CLI", "SKILL.md", "Remote skills"),
                skillFormat = "SKILL.md (Copilot format)",
                installCommand = "gh skill install owner/repo"
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "AI Agent Capability Matrix / AI Agent 能力矩阵")
        }

        item {
            Text(
                text = "Compare skill compatibility across AI coding agents / 对比 AI 编码 Agent 的 Skill 兼容性",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(agents) { agent ->
            AgentCard(agent = agent)
        }
    }
}

// =============================================================
// CLIGuideContent — CLI Guide 内容区
// =============================================================
@Composable
private fun CLIGuideContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "Android CLI Guide / Android CLI 指南")
        }

        item {
            InfoCard(
                title = "CLI Task Encapsulation / CLI 任务封装",
                icon = Icons.Default.Terminal,
                items = listOf(
                    "androidcli analyze-r8 <output.json> — Analyze R8 output",
                    "androidcli list-skills — List installed skills",
                    "androidcli validate-skill <path> — Validate SKILL.md",
                    "androidcli audit <path> — Run quality audit",
                    "androidcli publish <repo> — Publish to GitHub"
                )
            )
        }

        item {
            SectionHeader(title = "Examples / 示例")
        }

        item {
            CommandCard(
                title = "R8 Analysis CLI / R8 分析 CLI",
                commands = listOf(
                    "androidcli analyze-r8 ./app/build/output.json",
                    "androidcli analyze-r8 --verbose ./build.log",
                    "androidcli analyze-r8 --format=json ./result.json"
                )
            )
        }

        item {
            CommandCard(
                title = "Skill Validation CLI / Skill 验证 CLI",
                commands = listOf(
                    "androidcli validate-skill ./SKILL.md",
                    "androidcli validate-skill --strict ./skill/",
                    "androidcli validate-skill --schema=official ./"
                )
            )
        }
    }
}

// =============================================================
// MCPGuideContent — MCP Guide 内容区
// =============================================================
@Composable
private fun MCPGuideContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "MCP Integration Guide / MCP 集成指南")
        }

        item {
            InfoCard(
                title = "Skills × MCP Server Synergy / Skills × MCP Server 协同",
                icon = Icons.Default.Hub,
                items = listOf(
                    "Skills: On-demand expert knowledge for specific tasks",
                    "MCP: Real-time tool access and system integration",
                    "Use Skills for: Domain knowledge, best practices, templates",
                    "Use MCP for: File system, Git, terminal, API calls",
                    "Combine both: Best of knowledge + real capabilities"
                )
            )
        }

        item {
            SectionHeader(title = "When to Use Each / 何时使用")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Use Skills for / 使用 Skills 的场景", fontWeight = FontWeight.Bold)
                    Text("• Learning project conventions / 学习项目规范", style = MaterialTheme.typography.bodySmall)
                    Text("• Best practices for Android patterns / Android 最佳实践", style = MaterialTheme.typography.bodySmall)
                    Text("• Domain-specific knowledge / 领域特定知识", style = MaterialTheme.typography.bodySmall)
                    Text("• Step-by-step task workflows / 分步任务工作流", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Use MCP for / 使用 MCP 的场景", fontWeight = FontWeight.Bold)
                    Text("• Real-time file operations / 实时文件操作", style = MaterialTheme.typography.bodySmall)
                    Text("• Git operations / Git 操作", style = MaterialTheme.typography.bodySmall)
                    Text("• Terminal command execution / 终端命令执行", style = MaterialTheme.typography.bodySmall)
                    Text("• API integrations / API 集成", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// =============================================================
// VersionMgmtContent — Version Management 内容区
// =============================================================
@Composable
private fun VersionMgmtContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "Version Management / 版本管理")
        }

        item {
            InfoCard(
                title = "SemVer + Changelog / SemVer + 变更日志规范",
                icon = Icons.Default.Star,
                items = listOf(
                    "Format: MAJOR.MINOR.PATCH (e.g., v1.2.3)",
                    "MAJOR: Breaking changes incompatible with previous versions",
                    "MINOR: New functionality backward compatible",
                    "PATCH: Bug fixes backward compatible",
                    "Tag format: v1.0.0 (prefix with 'v')"
                )
            )
        }

        item {
            SectionHeader(title = "Changelog Template / 变更日志模板")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CHANGELOG.md", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = """
                        # Changelog

                        ## [1.0.0] - 2026-04-28
                        ### Added
                        - Initial release
                        - SKILL.md template
                        - Audit tool

                        ## [0.1.0] - 2026-04-01
                        ### Added
                        - Draft version
                        """.trimIndent(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStartContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "Quick Start Guide / 快速入门")
        }

        item {
            InfoCard(
                title = "5 Steps to Create Your First Skill / 5 步创建第一个 Skill",
                icon = Icons.Default.AutoAwesome,
                items = listOf(
                    "Step 1: Select Skill Type — Choose migration/optimization/debug/CI/config/audit/publish/CLI/MCP",
                    "Step 2: Fill in Name & Description — Be specific and descriptive",
                    "Step 3: Add Trigger Conditions — When should this skill activate?",
                    "Step 4: Select Tools & Workflow — Define what the skill can do",
                    "Step 5: Generate, Audit & Publish — Validate and release"
                )
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF673AB7).copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ready to Start?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Go to Skill Creator tab and start building your Android Skill!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { /* Navigate to Skill Creator */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Creating / 开始创建")
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Skill Creation Flow / Skill 创建流程")
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowStep(number = 1, title = "Select Type", description = "Choose skill category")
                FlowStep(number = 2, title = "Fill Info", description = "Name, description, triggers")
                FlowStep(number = 3, title = "Define Tools", description = "Add available tools")
                FlowStep(number = 4, title = "Design Workflow", description = "Step-by-step process")
                FlowStep(number = 5, title = "Audit & Publish", description = "Validate and release")
            }
        }
    }
}

// =============================================================
// Helper Composable Functions / 辅助Composable函数
// =============================================================

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun ChipItem(
    label: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun WorkflowStepItem(
    step: WorkflowStep,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${step.stepNumber}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "~${step.estimatedMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove step",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun QualityScoreCard(score: QualityScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = score.grade.color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quality Score / 质量评分", fontWeight = FontWeight.Bold)
                Badge(containerColor = score.grade.color) {
                    Text(
                        text = score.grade.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${score.overall}/100",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = score.grade.color
            )
            Spacer(modifier = Modifier.height(8.dp))
            ScoreRow("Coverage", score.coverage)
            ScoreRow("Accuracy", score.accuracy)
            ScoreRow("Maintainability", score.maintainability)
            ScoreRow("Documentation", score.documentation)
        }
    }
}

@Composable
private fun ScoreRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text("$value/100", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AuditResultCard(result: AuditResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isPassed)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.isPassed) Icons.Default.Check else Icons.Default.Delete,
                    contentDescription = null,
                    tint = if (result.isPassed) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (result.isPassed) "Audit Passed / 审核通过" else "Audit Failed / 审核失败",
                    fontWeight = FontWeight.Bold
                )
            }
            if (result.errors.isNotEmpty()) {
                Text("Errors / 错误:", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                result.errors.forEach { Text("  - $it", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF44336)) }
            }
            if (result.warnings.isNotEmpty()) {
                Text("Warnings / 警告:", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                result.warnings.forEach { Text("  - $it", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF9800)) }
            }
            if (result.suggestions.isNotEmpty()) {
                Text("Suggestions / 建议:", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                result.suggestions.forEach { Text("  - $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    items: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun CommandCard(
    title: String? = null,
    commands: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title != null) {
                Text(title, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(8.dp))
            }
            commands.forEach { cmd ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("$ ", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(cmd, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ScoreRangeRow(range: String, label: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(range, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TemplateCard(
    template: SkillTemplate,
    onUse: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUse() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getSkillTypeIcon(template.category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, fontWeight = FontWeight.Bold)
                Text(
                    template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    template.tags.take(3).forEach { tag ->
                        Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                            Text(tag, modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp)
                        }
                    }
                }
            }
            Icon(Icons.Default.Add, contentDescription = "Use template")
        }
    }
}

@Composable
private fun AgentCard(agent: AgentInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(agent.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Format: ${agent.skillFormat}", style = MaterialTheme.typography.bodySmall)
            Text("Install: ${agent.installCommand}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Features / 功能:", fontWeight = FontWeight.Medium, fontSize = 12.sp)
            agent.supportedFeatures.forEach { feature ->
                Text("  - $feature", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FlowStep(number: Int, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// =============================================================
// Icon Helper Functions / 图标辅助函数
// =============================================================

private fun getTabIcon(tab: AndroidSkillsTab): ImageVector {
    return when (tab) {
        AndroidSkillsTab.SKILL_CREATOR -> Icons.Default.Extension
        AndroidSkillsTab.SKILL_PUBLISHER -> Icons.Default.Publish
        AndroidSkillsTab.SKILL_AUDIT -> Icons.Default.FactCheck
        AndroidSkillsTab.TEMPLATES -> Icons.Default.Description
        AndroidSkillsTab.AGENT_MATRIX -> Icons.Default.Hub
        AndroidSkillsTab.CLI_GUIDE -> Icons.Default.Terminal
        AndroidSkillsTab.MCP_GUIDE -> Icons.Default.Hub
        AndroidSkillsTab.VERSION_MGMT -> Icons.Default.Star
        AndroidSkillsTab.QUICK_START -> Icons.Default.AutoAwesome
    }
}

private fun getSkillTypeIcon(type: SkillType): ImageVector {
    return when (type) {
        SkillType.MIGRATION -> Icons.Default.SwapHoriz
        SkillType.OPTIMIZATION -> Icons.Default.Star
        SkillType.DEBUGGING -> Icons.Default.Build
        SkillType.CI_CONFIG -> Icons.Default.Build
        SkillType.DOCUMENTATION -> Icons.Default.Description
        SkillType.AUDIT -> Icons.Default.FactCheck
        SkillType.PUBLISH -> Icons.Default.Publish
        SkillType.CLI_GUIDE -> Icons.Default.Terminal
        SkillType.MCP_INTEGRATION -> Icons.Default.Hub
    }
}
