package com.mvi.kenny.feature.agentskills

/**
 * ============================================================
 * AgentSkillsScreen — Android Studio Panda 3 Agent Skills 开发工具包
 * ============================================================
 * PRD-041 | Android Studio Panda 3 Agent Skills 开发工具包
 *
 * 页面结构（Page Structure）:
 * — 三栏布局（Three-panel layout）适配：
 *   — 移动端使用 NavigationRail（左侧） + 主体内容 + 右滑抽屉（预览）
 *   — Tab 切换代替三栏并列
 *
 * 六个子页面（Six sub-pages）:
 * — My Skills: 已安装 Skills 列表管理
 * — Editor: Skill 创建/编辑向导
 * — Market: Skills 市场浏览安装
 * — Permissions: 权限策略仪表盘
 * — Audit: 执行审计时间线
 * — Lint Settings: Lint/Detekt 联动配置
 *
 * 设计规范对应（Mapping to Design Doc）:
 * — Section 3.2 页面清单 → Tab 对应每个页面
 * — Section 5 组件清单 → UI 组件实现
 * — Section 7 视觉规范 → Material 3 + Dark theme
 *
 * @param viewModel AgentSkillsViewModel 实例
 * @param onUpdateTopBar TopBar 配置更新回调
 * @see AgentSkillsViewModel
 * @see AgentSkillsContract
 * —————————————————————————————————————————————————————
 */

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.base.TopBarAction

// ================================================================
// Color Palette (from Design Doc Section 7.1)
// ================================================================

private val AccentBlue = Color(0xFF4FC3F7)
private val SuccessGreen = Color(0xFF81C784)
private val WarningYellow = Color(0xFFFFB74D)
private val ErrorRed = Color(0xFFE57373)
private val DangerRed = Color(0xFFEF5350)

// Dark theme palette
private val DarkBg1 = Color(0xFF1E1E1E)  // 面板背景
private val DarkBg2 = Color(0xFF252526)  // 编辑器背景
private val DarkBg3 = Color(0xFF2D2D30)  // 输入框背景
private val DarkBorder = Color(0xFF3C3C3C)
private val DarkTextPrimary = Color(0xFFCCCCCC)
private val DarkTextSecondary = Color(0xFF808080)

// ================================================================
// AgentSkillsScreen — Main Container
// ================================================================

@Composable
fun AgentSkillsScreen(
    viewModel: AgentSkillsViewModel,
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Update TopBar config based on current tab
    LaunchedEffect(state.currentTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = state.currentTab.displayName,
                actions = buildTopBarActions(state.currentTab, viewModel)
            )
        )
    }

    // Collect effects for toast
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            // Toast handled by parent (via TopBar snackbar or similar)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab indicator row（移动端顶部 Tab 切换）
        AgentSkillsTabRow(
            currentTab = state.currentTab,
            onTabSelected = { tab ->
                viewModel.sendIntent(AgentSkillsIntent.SwitchTab(tab))
            }
        )

        // Content area — animated transition between tabs
        AnimatedContent(
            targetState = state.currentTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "TabContentTransition"
        ) { tab ->
            when (tab) {
                AgentSkillsTab.MY_SKILLS -> MySkillsContent(
                    state = state.mySkills,
                    onIntent = { viewModel.sendIntent(AgentSkillsIntent.MySkillsWrapper(it)) }
                )
                AgentSkillsTab.SKILL_EDITOR -> SkillEditorContent(
                    state = state.skillEditor,
                    onIntent = { viewModel.sendIntent(AgentSkillsIntent.EditorSkillIntent(it)) }
                )
                AgentSkillsTab.MARKET -> SkillMarketContent(
                    state = state.skillMarket,
                    onIntent = { viewModel.sendIntent(AgentSkillsIntent.MarketIntent(it)) }
                )
                AgentSkillsTab.PERMISSIONS -> PermissionDashboardContent(
                    state = state.permissionDashboard,
                    onIntent = { viewModel.sendIntent(AgentSkillsIntent.PermissionIntent(it)) }
                )
                AgentSkillsTab.AUDIT -> AuditContent(
                    state = state.audit,
                    onIntent = { viewModel.sendIntent(AgentSkillsIntent.AuditWrapper(it)) }
                )
                AgentSkillsTab.LINT_SETTINGS -> LintIntegrationContent(
                    state = state.lintIntegration,
                    mySkills = state.mySkills.skills,
                    onIntent = { viewModel.sendIntent(AgentSkillsIntent.LintIntent(it)) }
                )
            }
        }
    }
}

// ================================================================
// TopBar Action Builder
// ================================================================

private fun buildTopBarActions(tab: AgentSkillsTab, vm: AgentSkillsViewModel): List<TopBarAction> {
    return when (tab) {
        AgentSkillsTab.MY_SKILLS -> listOf(
            TopBarAction(Icons.Default.Add, "Create New Skill") {
                vm.sendIntent(AgentSkillsIntent.SwitchTab(AgentSkillsTab.SKILL_EDITOR))
                vm.sendIntent(AgentSkillsIntent.EditorSkillIntent(SkillEditorIntent.CreateNewSkill))
            }
        )
        AgentSkillsTab.SKILL_EDITOR -> listOf(
            TopBarAction(Icons.Default.Save, "Save Skill") {
                vm.sendIntent(AgentSkillsIntent.EditorSkillIntent(SkillEditorIntent.SaveSkill))
            }
        )
        AgentSkillsTab.MARKET -> listOf(
            TopBarAction(Icons.Default.Refresh, "Refresh Market") {
                vm.sendIntent(AgentSkillsIntent.MarketIntent(SkillMarketIntent.LoadMarket))
            }
        )
        AgentSkillsTab.PERMISSIONS -> listOf(
            TopBarAction(Icons.Default.Save, "Save Policy") {
                vm.sendIntent(AgentSkillsIntent.PermissionIntent(PermissionDashboardIntent.SavePolicy))
            }
        )
        AgentSkillsTab.AUDIT -> listOf(
            TopBarAction(Icons.Default.Download, "Export Audit") {
                vm.sendIntent(AgentSkillsIntent.AuditWrapper(AuditIntent.ExportAudit))
            }
        )
        AgentSkillsTab.LINT_SETTINGS -> listOf(
            TopBarAction(Icons.Default.Save, "Save Settings") {
                vm.sendIntent(AgentSkillsIntent.LintIntent(LintIntegrationIntent.SaveSettings))
            }
        )
    }
}

// ================================================================
// Tab Row (Mobile-adapted Navigation)
// ================================================================

@Composable
private fun AgentSkillsTabRow(
    currentTab: AgentSkillsTab,
    onTabSelected: (AgentSkillsTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = AgentSkillsTab.entries.indexOf(currentTab),
        edgePadding = 8.dp,
        containerColor = DarkBg1,
        contentColor = AccentBlue,
        indicator = { tabPositions ->
            val index = AgentSkillsTab.entries.indexOf(currentTab)
            if (index >= 0 && index < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                    color = AccentBlue
                )
            }
        }
    ) {
        AgentSkillsTab.entries.forEach { tab ->
            Tab(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

private fun getTabIcon(tab: AgentSkillsTab): ImageVector {
    return when (tab) {
        AgentSkillsTab.MY_SKILLS -> Icons.Default.FolderSpecial
        AgentSkillsTab.SKILL_EDITOR -> Icons.Default.Edit
        AgentSkillsTab.MARKET -> Icons.Default.Store
        AgentSkillsTab.PERMISSIONS -> Icons.Default.Security
        AgentSkillsTab.AUDIT -> Icons.Default.History
        AgentSkillsTab.LINT_SETTINGS -> Icons.Default.Link
    }
}

// ================================================================
// 6.1 My Skills Content — 我的 Skills 管理列表
// ================================================================

@Composable
private fun MySkillsContent(
    state: MySkillsState,
    onIntent: (MySkillsIntent) -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
        return
    }

    if (state.skills.isEmpty()) {
        EmptyState(
            icon = Icons.Default.FolderSpecial,
            title = "No Skills Yet",
            message = "Go to Market to discover and install Skills"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DarkBg1),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.skills, key = { it.id }) { skill ->
            MySkillCard(skill = skill, onIntent = onIntent)
        }
    }
}

@Composable
private fun MySkillCard(
    skill: MySkill,
    onIntent: (MySkillsIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkBg2),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = skill.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = skill.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Switch(
                    checked = skill.isEnabled,
                    onCheckedChange = { enabled ->
                        onIntent(MySkillsIntent.ToggleSkill(skill.id, enabled))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentBlue,
                        checkedTrackColor = AccentBlue.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(skill.version, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = DarkBg3,
                            labelColor = DarkTextSecondary
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(skill.category, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = DarkBg3,
                            labelColor = DarkTextSecondary
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }

                Row {
                    IconButton(onClick = { onIntent(MySkillsIntent.EditSkill(skill)) }) {
                        Icon(Icons.Default.Edit, "Edit", tint = AccentBlue, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { onIntent(MySkillsIntent.DeleteSkill(skill.id)) }) {
                        Icon(Icons.Default.Delete, "Delete", tint = ErrorRed, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

// ================================================================
// 6.2 Skill Editor Content — Skill 创建/编辑向导
// ================================================================

@Composable
private fun SkillEditorContent(
    state: SkillEditorState,
    onIntent: (SkillEditorIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(DarkBg1)) {
        // Wizard Step Indicator
        WizardStepIndicator(
            currentStep = state.wizardStep,
            steps = listOf("DSL Definition", "Permissions", "Test"),
            onStepClick = { step -> onIntent(SkillEditorIntent.SetWizardStep(step)) }
        )

        // Step content
        Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            when (state.wizardStep) {
                0 -> DslEditorStep(state, onIntent)
                1 -> PermissionsConfigStep(state, onIntent)
                2 -> LocalTestStep(state, onIntent)
            }
        }

        // Bottom action bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    if (state.wizardStep > 0) {
                        onIntent(SkillEditorIntent.SetWizardStep(state.wizardStep - 1))
                    } else {
                        onIntent(SkillEditorIntent.DiscardChanges)
                    }
                }
            ) {
                Text(if (state.wizardStep > 0) "← Back" else "Discard")
            }

            if (state.wizardStep < 2) {
                Button(
                    onClick = { onIntent(SkillEditorIntent.SetWizardStep(state.wizardStep + 1)) },
                    enabled = when (state.wizardStep) {
                        0 -> state.isDslValid && state.name.isNotBlank()
                        else -> true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Next →")
                }
            } else {
                Button(
                    onClick = { onIntent(SkillEditorIntent.SaveSkill) },
                    enabled = !state.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save Skill")
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardStepIndicator(
    currentStep: Int,
    steps: List<String>,
    onStepClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        steps.forEachIndexed { index, stepName ->
            val isActive = index == currentStep
            val isCompleted = index < currentStep

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { if (index <= currentStep) onStepClick(index) }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> SuccessGreen
                                isActive -> AccentBlue
                                else -> DarkBg3
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            text = "${index + 1}",
                            color = if (isActive) Color.White else DarkTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stepName,
                    color = if (isActive) AccentBlue else DarkTextSecondary,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            }

            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .background(if (isCompleted) SuccessGreen else DarkBorder)
                )
            }
        }
    }
}

@Composable
private fun DslEditorStep(
    state: SkillEditorState,
    onIntent: (SkillEditorIntent) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Skill name
        OutlinedTextField(
            value = state.name,
            onValueChange = { onIntent(SkillEditorIntent.UpdateName(it)) },
            label = { Text("Skill Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = DarkBorder,
                focusedLabelColor = AccentBlue
            ),
            singleLine = true
        )

        // Description
        OutlinedTextField(
            value = state.description,
            onValueChange = { onIntent(SkillEditorIntent.UpdateDescription(it)) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = DarkBorder,
                focusedLabelColor = AccentBlue
            ),
            minLines = 2,
            maxLines = 3
        )

        // Version
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.version,
                onValueChange = { onIntent(SkillEditorIntent.UpdateVersion(it)) },
                label = { Text("Version") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedLabelColor = AccentBlue
                ),
                singleLine = true
            )
        }

        // File types
        Text("Applicable File Types", color = DarkTextPrimary, style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SkillFileType.entries) { ft ->
                FilterChip(
                    selected = state.fileTypes.contains(ft),
                    onClick = { onIntent(SkillEditorIntent.ToggleFileType(ft)) },
                    label = { Text(ft.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // DSL Editor
        Text("DSL Content", color = DarkTextPrimary, style = MaterialTheme.typography.labelMedium)

        // Validation errors display
        if (state.validationErrors.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    state.validationErrors.forEach { err ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (err.severity == ValidationSeverity.ERROR) Icons.Default.Error else Icons.Default.Warning,
                                contentDescription = null,
                                tint = when (err.severity) {
                                    ValidationSeverity.ERROR -> ErrorRed
                                    ValidationSeverity.WARNING -> WarningYellow
                                    ValidationSeverity.INFO -> AccentBlue
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = err.message,
                                color = DarkTextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // DSL code area (monospace, dark background)
        Surface(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            color = DarkBg2,
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 0.dp
        ) {
            OutlinedTextField(
                value = state.dslContent,
                onValueChange = { onIntent(SkillEditorIntent.UpdateDsl(it)) },
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = DarkTextPrimary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = DarkBorder,
                    cursorColor = AccentBlue,
                    focusedTextColor = DarkTextPrimary,
                    unfocusedTextColor = DarkTextPrimary
                )
            )
        }
    }
}

@Composable
private fun PermissionsConfigStep(
    state: SkillEditorState,
    onIntent: (SkillEditorIntent) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Permission Configuration",
            style = MaterialTheme.typography.titleMedium,
            color = DarkTextPrimary
        )
        Text(
            "Configure read/write/execute permissions for this Skill. These settings control what the AI Agent can do when this Skill is active.",
            style = MaterialTheme.typography.bodySmall,
            color = DarkTextSecondary
        )

        // Quick templates
        Text("Quick Templates", color = DarkTextPrimary, style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PermissionTemplate.entries.take(2).forEach { template ->
                OutlinedButton(
                    onClick = { /* Template selection handled in wizard */ },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
                ) {
                    Text(template.displayName, fontSize = 12.sp)
                }
            }
        }

        // Permission summary from DSL
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkBg2),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PermissionRow("allowRead", "Allow Read", Icons.Default.Visibility, SuccessGreen)
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))
                PermissionRow("allowWrite", "Allow Write", Icons.Default.Edit, WarningYellow)
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))
                PermissionRow("fileTypes", "File Types: ${state.fileTypes.joinToString { it.displayName }}", Icons.Default.Folder, AccentBlue)
            }
        }
    }
}

@Composable
private fun PermissionRow(key: String, label: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = DarkTextPrimary, style = MaterialTheme.typography.bodyMedium)
        }
        Text(key, color = DarkTextSecondary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    }
}

@Composable
private fun LocalTestStep(
    state: SkillEditorState,
    onIntent: (SkillEditorIntent) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Local Test Environment",
            style = MaterialTheme.typography.titleMedium,
            color = DarkTextPrimary
        )
        Text(
            "Test your Skill with a mock Agent. Enter a code snippet to validate Skill behavior.",
            style = MaterialTheme.typography.bodySmall,
            color = DarkTextSecondary
        )

        // Test input
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Test Code Snippet") },
            placeholder = { Text("// Enter Kotlin code to test...") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = DarkBorder,
                focusedLabelColor = AccentBlue
            ),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
        )

        // Run test button
        Button(
            onClick = { onIntent(SkillEditorIntent.RunLocalTest("")) },
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, null)
            Spacer(Modifier.width(8.dp))
            Text("Run Local Test")
        }

        // Test result
        state.testResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.passed) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (result.passed) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (result.passed) SuccessGreen else ErrorRed
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (result.passed) "Test Passed" else "Test Failed",
                            color = if (result.passed) SuccessGreen else ErrorRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${result.durationMs}ms",
                            color = DarkTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        color = DarkBg2,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = result.output,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = DarkTextPrimary
                        )
                    }
                }
            }
        }
    }
}

// ================================================================
// 6.3 Skill Market Content — Skills 市场浏览
// ================================================================

@Composable
private fun SkillMarketContent(
    state: SkillMarketState,
    onIntent: (SkillMarketIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(DarkBg1)) {
        // Search & Filter bar
        Column(
            modifier = Modifier.background(DarkBg2).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.filter.searchQuery,
                onValueChange = { onIntent(SkillMarketIntent.Search(it)) },
                placeholder = { Text("Search Skills...", color = DarkTextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = DarkTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = DarkBorder
                )
            )

            // Category chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MarketCategory.entries) { category ->
                    FilterChip(
                        selected = state.filter.category == category,
                        onClick = { onIntent(SkillMarketIntent.SetCategory(category)) },
                        label = { Text(category.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Skills list
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            val filteredSkills = state.skills.filter { skill ->
                val matchesCategory = state.filter.category == MarketCategory.ALL || skill.category == state.filter.category
                val matchesSearch = state.filter.searchQuery.isBlank() ||
                        skill.name.contains(state.filter.searchQuery, ignoreCase = true) ||
                        skill.description.contains(state.filter.searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }.let { skills ->
                when (state.filter.sortBy) {
                    MarketSortOption.POPULAR -> skills.sortedByDescending { it.downloadCount }
                    MarketSortOption.RATING -> skills.sortedByDescending { it.rating }
                    MarketSortOption.NEWEST -> skills.sortedByDescending { it.lastUpdated }
                    MarketSortOption.DOWNLOADS -> skills.sortedByDescending { it.downloadCount }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredSkills, key = { it.id }) { skill ->
                    MarketSkillCard(skill = skill, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
private fun MarketSkillCard(
    skill: MarketSkill,
    onIntent: (SkillMarketIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onIntent(SkillMarketIntent.SelectSkill(skill)) },
        colors = CardDefaults.cardColors(containerColor = DarkBg2),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = skill.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = skill.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentBlue
                    )
                }

                // Install/Uninstall button
                if (skill.isInstalled) {
                    OutlinedButton(
                        onClick = { onIntent(SkillMarketIntent.UninstallSkill(skill.id)) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Uninstall", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = { onIntent(SkillMarketIntent.InstallSkill(skill.id)) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Install", fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodySmall,
                color = DarkTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating stars
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = WarningYellow, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", skill.rating),
                        color = DarkTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Default.Download, null, tint = DarkTextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${skill.downloadCount}",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )
                }

                AssistChip(
                    onClick = {},
                    label = { Text(skill.category.displayName, fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = DarkBg3,
                        labelColor = DarkTextSecondary
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}

// ================================================================
// 6.4 Permission Dashboard Content — 权限策略仪表盘
// ================================================================

@Composable
private fun PermissionDashboardContent(
    state: PermissionDashboardState,
    onIntent: (PermissionDashboardIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(DarkBg1)) {
        // Toolbar with templates
        Row(
            modifier = Modifier.background(DarkBg2).fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Quick Apply:",
                color = DarkTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            PermissionTemplate.entries.forEach { template ->
                OutlinedButton(
                    onClick = { onIntent(PermissionDashboardIntent.ApplyTemplate(template)) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(template.displayName, fontSize = 11.sp)
                }
            }
        }

        // Permission tree
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.rootNodes) { node ->
                PermissionTreeNodeView(node = node, onIntent = onIntent, depth = 0)
            }
        }

        // Save bar
        if (state.isDirty) {
            Row(
                modifier = Modifier.background(DarkBg2).fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = WarningYellow, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Unsaved changes", color = WarningYellow, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onIntent(PermissionDashboardIntent.ResetPolicy) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTextSecondary),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Reset", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onIntent(PermissionDashboardIntent.SavePolicy) },
                        enabled = !state.isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.height(32.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionTreeNodeView(
    node: PermissionNode,
    onIntent: (PermissionDashboardIntent) -> Unit,
    depth: Int
) {
    val modeColor = when (node.mode) {
        PermissionMode.ALLOW -> SuccessGreen
        PermissionMode.READONLY -> WarningYellow
        PermissionMode.DENY -> ErrorRed
        PermissionMode.UNSET -> DarkTextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp)
            .clickable { onIntent(PermissionDashboardIntent.SelectNode(node)) },
        colors = CardDefaults.cardColors(containerColor = DarkBg2),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (node.isDirectory) {
                    IconButton(
                        onClick = { onIntent(PermissionDashboardIntent.ToggleNodeExpand(node.path)) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (node.isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = DarkTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Icon(
                    imageVector = if (node.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                    contentDescription = null,
                    tint = DarkTextSecondary,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(
                        text = node.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = node.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Mode selector
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PermissionMode.entries.forEach { mode ->
                    val isSelected = node.mode == mode
                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { onIntent(PermissionDashboardIntent.SetNodePermission(node.path, mode)) },
                        color = if (isSelected) modeColor else modeColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = mode.symbol,
                                color = if (isSelected) Color.White else modeColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Children (if expanded)
    if (node.isExpanded && node.children.isNotEmpty()) {
        Column(
            modifier = Modifier.padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            node.children.forEach { child ->
                PermissionTreeNodeView(node = child, onIntent = onIntent, depth = depth + 1)
            }
        }
    }
}

// ================================================================
// 6.5 Audit Content — 执行审计视图
// ================================================================

@Composable
private fun AuditContent(
    state: AuditState,
    onIntent: (AuditIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(DarkBg1)) {
        // Filter bar
        Row(
            modifier = Modifier.background(DarkBg2).fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Time range filter
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(AuditTimeRange.entries) { range ->
                    FilterChip(
                        selected = state.timeRange == range,
                        onClick = { onIntent(AuditIntent.SetTimeRange(range)) },
                        label = { Text(range.displayName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }

        // Audit entries
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else if (state.entries.isEmpty()) {
            EmptyState(
                icon = Icons.Default.History,
                title = "No Audit Entries",
                message = "Skill execution logs will appear here"
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    AuditEntryCard(entry = entry, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
private fun AuditEntryCard(
    entry: AuditEntry,
    onIntent: (AuditIntent) -> Unit
) {
    val outcomeColor = Color(entry.outcome.color)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onIntent(AuditIntent.SelectEntry(entry)) },
        colors = CardDefaults.cardColors(containerColor = DarkBg2),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Outcome indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(outcomeColor)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.action,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = entry.outcome.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = outcomeColor
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row {
                    Text(
                        text = entry.skillName,
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentBlue,
                        fontSize = 11.sp
                    )
                    Text(
                        text = " • ${entry.agentName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary,
                        fontSize = 11.sp
                    )
                }

                entry.targetPath?.let { path ->
                    Text(
                        text = path,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = entry.timestamp.substringAfter(" "),
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = "${entry.durationMs}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ================================================================
// 6.6 Lint Integration Content — Lint/Detekt 联动设置
// ================================================================

@Composable
private fun LintIntegrationContent(
    state: LintIntegrationState,
    mySkills: List<MySkill>,
    onIntent: (LintIntegrationIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(DarkBg1)) {
        // Header info
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Link Lint/Detekt rules to Agent Skills. When a rule is triggered, the linked Skill will be used for validation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextSecondary
                )
            }
        }

        // Mappings list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.lintMappings, key = { it.lintRuleId }) { mapping ->
                LintMappingCard(
                    mapping = mapping,
                    skills = mySkills,
                    onIntent = onIntent
                )
            }
        }

        // Save bar
        if (state.isDirty) {
            Row(
                modifier = Modifier.background(DarkBg2).fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onIntent(LintIntegrationIntent.SaveSettings) },
                    enabled = !state.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Settings")
                    }
                }
            }
        }
    }
}

@Composable
private fun LintMappingCard(
    mapping: LintRuleMapping,
    skills: List<MySkill>,
    onIntent: (LintIntegrationIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkBg2),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mapping.lintRuleName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = mapping.lintRuleId,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Switch(
                    checked = mapping.isEnabled,
                    onCheckedChange = { enabled ->
                        onIntent(LintIntegrationIntent.ToggleMapping(mapping.lintRuleId, enabled))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentBlue,
                        checkedTrackColor = AccentBlue.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            // Skill link dropdown
            var expanded by remember { mutableStateOf(false) }
            val linkedSkill = skills.find { it.id == mapping.targetSkillId }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Linked Skill:",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextSecondary,
                    fontSize = 12.sp
                )

                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = linkedSkill?.name ?: "None",
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None", color = DarkTextSecondary) },
                            onClick = {
                                onIntent(LintIntegrationIntent.LinkToSkill(mapping.lintRuleId, null))
                                expanded = false
                            }
                        )
                        skills.forEach { skill ->
                            DropdownMenuItem(
                                text = { Text(skill.name, color = DarkTextPrimary) },
                                onClick = {
                                    onIntent(LintIntegrationIntent.LinkToSkill(mapping.lintRuleId, skill.id))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Shared Components — 共享组件
// ================================================================

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DarkTextSecondary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = DarkTextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = DarkTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}