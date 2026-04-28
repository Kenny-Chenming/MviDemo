package com.mvi.kenny.feature.skillsworkflow

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.skillsworkflow.SkillCategory
import com.mvi.kenny.feature.skillsworkflow.SkillDetailIntent
import com.mvi.kenny.feature.skillsworkflow.SkillDetailState
import com.mvi.kenny.feature.skillsworkflow.SkillHubIntent
import com.mvi.kenny.feature.skillsworkflow.SkillHubState
import com.mvi.kenny.feature.skillsworkflow.SkillInfo
import com.mvi.kenny.feature.skillsworkflow.SkillStatus
import com.mvi.kenny.feature.skillsworkflow.SkillsWorkflowEffect
import com.mvi.kenny.feature.skillsworkflow.SkillsWorkflowIntent
import com.mvi.kenny.feature.skillsworkflow.SkillsWorkflowState
import com.mvi.kenny.feature.skillsworkflow.SkillsWorkflowTab
import com.mvi.kenny.feature.skillsworkflow.TestReport
import com.mvi.kenny.feature.skillsworkflow.WorkflowColors
import com.mvi.kenny.feature.skillsworkflow.WorkflowIntent
import com.mvi.kenny.feature.skillsworkflow.WorkflowNode
import com.mvi.kenny.feature.skillsworkflow.WorkflowState
import com.mvi.kenny.feature.skillsworkflow.WizardIntent
import com.mvi.kenny.feature.skillsworkflow.WizardState
import com.mvi.kenny.feature.skillsworkflow.WizardStep
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// =============================================================
// SkillsWorkflowScreen — Android CLI Skills Workflow 自动化工具包主界面
// PRD-195: Android CLI Skills Workflow 自动化工具包
// =============================================================
@Composable
fun SkillsWorkflowScreen(
    state: SkillsWorkflowState,
    onIntent: (SkillsWorkflowIntent) -> Unit,
    effect: kotlinx.coroutines.flow.SharedFlow<SkillsWorkflowEffect>
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        effect.collectLatest { eff ->
            when (eff) {
                is SkillsWorkflowEffect.ShowSnackbar -> {
                    scope.launch { snackbarHostState.showSnackbar(eff.message) }
                }
                is SkillsWorkflowEffect.ShowVerificationResult -> {
                    scope.launch { snackbarHostState.showSnackbar(eff.message) }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(WorkflowColors.Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            WorkflowTopBar(
                activeTab = state.activeTab,
                onTabSelected = { onIntent(SkillsWorkflowIntent.SelectTab(it)) }
            )
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = state.activeTab,
                    modifier = Modifier.fillMaxSize(),
                    label = "workflow_tab_content",
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { tab ->
                    when (tab) {
                        SkillsWorkflowTab.SKILL_HUB -> SkillHubContent(
                            state = state.skillHubState,
                            onIntent = { onIntent(SkillsWorkflowIntent.ForwardToSkillHub(it)) }
                        )
                        SkillsWorkflowTab.SKILL_DETAIL -> SkillDetailContent(
                            state = state.skillDetailState,
                            onIntent = { onIntent(SkillsWorkflowIntent.ForwardToSkillDetail(it)) },
                            onBack = { onIntent(SkillsWorkflowIntent.SelectTab(SkillsWorkflowTab.SKILL_HUB)) }
                        )
                        SkillsWorkflowTab.WORKFLOW_EDITOR -> WorkflowEditorContent(
                            state = state.workflowState,
                            onIntent = { onIntent(SkillsWorkflowIntent.ForwardToWorkflow(it)) }
                        )
                        SkillsWorkflowTab.WIZARD -> WizardContent(
                            state = state.wizardState,
                            onIntent = { onIntent(SkillsWorkflowIntent.ForwardToWizard(it)) }
                        )
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// =============================================================
// WorkflowTopBar — 顶部 Tab 导航栏
// =============================================================
@Composable
private fun WorkflowTopBar(
    activeTab: SkillsWorkflowTab,
    onTabSelected: (SkillsWorkflowTab) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(WorkflowColors.Surface).padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Terminal, contentDescription = null, tint = WorkflowColors.Primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Skills Workflow CLI", color = WorkflowColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text("PRD-195", color = WorkflowColors.TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(8.dp))
        ScrollableTabRow(
            selectedTabIndex = SkillsWorkflowTab.entries.indexOf(activeTab),
            edgePadding = 12.dp,
            containerColor = Color.Transparent,
            contentColor = WorkflowColors.TextPrimary,
            divider = {}
        ) {
            SkillsWorkflowTab.entries.forEach { tab ->
                val selected = activeTab == tab
                Tab(
                    selected = selected,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Icon(
                            imageVector = getTabIcon(tab),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selected) WorkflowColors.Primary else WorkflowColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (tab) {
                                SkillsWorkflowTab.SKILL_HUB -> "Hub"
                                SkillsWorkflowTab.SKILL_DETAIL -> "Detail"
                                SkillsWorkflowTab.WORKFLOW_EDITOR -> "Workflow"
                                SkillsWorkflowTab.WIZARD -> "Wizard"
                            },
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) WorkflowColors.Primary else WorkflowColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// SkillHubContent — 技能中心内容区
// =============================================================
@Composable
private fun SkillHubContent(
    state: SkillHubState,
    onIntent: (SkillHubIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onIntent(SkillHubIntent.SearchSkills(it)) },
            placeholder = { Text("Search skills... / 搜索 Skill", color = WorkflowColors.TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WorkflowColors.TextSecondary) },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onIntent(SkillHubIntent.SearchSkills("")) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = WorkflowColors.TextSecondary)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WorkflowColors.Primary,
                unfocusedBorderColor = WorkflowColors.Card,
                focusedContainerColor = WorkflowColors.Surface,
                unfocusedContainerColor = WorkflowColors.Surface,
                focusedTextColor = WorkflowColors.TextPrimary,
                unfocusedTextColor = WorkflowColors.TextPrimary
            ),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        LazyRow(modifier = Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { onIntent(SkillHubIntent.FilterByCategory(null)) },
                    label = { Text("All", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WorkflowColors.Primary,
                        selectedLabelColor = WorkflowColors.Background
                    )
                )
            }
            items(SkillCategory.entries.toTypedArray()) { category ->
                FilterChip(
                    selected = state.selectedCategory == category,
                    onClick = { onIntent(SkillHubIntent.FilterByCategory(category)) },
                    label = { Text("${category.emoji} ${category.label.split(" ")[0]}", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WorkflowColors.Primary,
                        selectedLabelColor = WorkflowColors.Background
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WorkflowColors.Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.installedSkills.isNotEmpty()) {
                    item { SectionTitle("📦 Installed / 已安装") }
                    items(state.installedSkills) { skill ->
                        SkillCard(
                            skill = skill,
                            isInstalled = true,
                            onInstall = { },
                            onUninstall = { onIntent(SkillHubIntent.UninstallSkill(skill.id)) },
                            onClick = { onIntent(SkillHubIntent.OpenSkillDetail(skill.id)) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                item { SectionTitle("🌐 Market / 市场") }

                if (state.filteredMarketSkills.isEmpty()) {
                    item { EmptyState(message = "No skills found / 未找到 Skill") }
                } else {
                    items(state.filteredMarketSkills) { skill ->
                        SkillCard(
                            skill = skill,
                            isInstalled = false,
                            onInstall = { onIntent(SkillHubIntent.InstallSkill(skill.id)) },
                            onUninstall = { },
                            onClick = { onIntent(SkillHubIntent.OpenSkillDetail(skill.id)) }
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// SkillDetailContent — 技能详情内容区
// =============================================================
@Composable
private fun SkillDetailContent(
    state: SkillDetailState,
    onIntent: (SkillDetailIntent) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(WorkflowColors.Background)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(WorkflowColors.Surface).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WorkflowColors.TextPrimary)
            }
            Text(state.skill?.name ?: "Skill Detail", color = WorkflowColors.TextPrimary, fontWeight = FontWeight.Bold)
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WorkflowColors.Primary)
            }
        } else if (state.skill != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { SkillDetailHeader(skill = state.skill) }
                item {
                    InstallCommandCard(
                        command = state.skill.fullInstallCommand,
                        onCopy = { onIntent(SkillDetailIntent.CopyInstallCommand(state.skill.fullInstallCommand)) }
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { onIntent(SkillDetailIntent.RunTest(state.skill.id)) },
                            modifier = Modifier.weight(1f),
                            enabled = !state.isRunningTest,
                            colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Primary)
                        ) {
                            if (state.isRunningTest) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = WorkflowColors.Background)
                            } else {
                                Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (state.isRunningTest) "Testing..." else "Run Test")
                        }
                        OutlinedButton(
                            onClick = { onIntent(SkillDetailIntent.PublishSkill(state.skill.id)) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Publish")
                        }
                    }
                }
                if (state.testReport != null) {
                    item { TestReportCard(report = state.testReport) }
                }
                item { ReadmeCard(readme = state.readme) }
                if (state.versionHistory.isNotEmpty()) {
                    item { VersionHistoryCard(versions = state.versionHistory) }
                }
            }
        }
    }
}

// =============================================================
// WorkflowEditorContent — 工作流编排器内容区
// =============================================================
@Composable
private fun WorkflowEditorContent(
    state: WorkflowState,
    onIntent: (WorkflowIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(WorkflowColors.Surface).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.workflowName,
                onValueChange = { onIntent(WorkflowIntent.UpdateWorkflowMeta(it, state.workflowDescription)) },
                placeholder = { Text("Workflow name", color = WorkflowColors.TextSecondary, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WorkflowColors.Primary,
                    unfocusedBorderColor = WorkflowColors.Card,
                    focusedTextColor = WorkflowColors.TextPrimary,
                    unfocusedTextColor = WorkflowColors.TextPrimary
                ),
                shape = RoundedCornerShape(6.dp)
            )
            IconButton(onClick = { onIntent(WorkflowIntent.AddNode("skill-${state.nodes.size + 1}")) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Node", tint = WorkflowColors.Primary)
            }
            Button(
                onClick = { onIntent(WorkflowIntent.ExecuteWorkflow) },
                enabled = state.canExecute && !state.isExecuting,
                colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Primary)
            ) {
                Icon(if (state.isExecuting) Icons.Default.HourglassTop else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (state.isExecuting) "Running..." else "Run", fontSize = 12.sp)
            }
            IconButton(onClick = { onIntent(WorkflowIntent.SaveWorkflow) }) {
                Icon(Icons.Default.Save, contentDescription = "Save", tint = WorkflowColors.TextSecondary)
            }
            IconButton(onClick = { onIntent(WorkflowIntent.ClearAll) }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear", tint = WorkflowColors.Error)
            }
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().background(WorkflowColors.Background).clickable { onIntent(WorkflowIntent.SelectNode(null)) }
        ) {
            if (state.nodes.isEmpty()) {
                EmptyState(message = "Add nodes to build workflow\n点击 + 添加节点")
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    state.edges.forEach { edge ->
                        val fromNode = state.nodes.find { it.id == edge.fromNodeId }
                        val toNode = state.nodes.find { it.id == edge.toNodeId }
                        if (fromNode != null && toNode != null) {
                            drawLine(
                                color = WorkflowColors.Accent,
                                start = Offset(fromNode.positionX + 60, fromNode.positionY + 25),
                                end = Offset(toNode.positionX, toNode.positionY + 25),
                                strokeWidth = 2f
                            )
                        }
                    }
                }
                state.nodes.forEach { node ->
                    WorkflowNodeView(
                        node = node,
                        isSelected = node.id == state.selectedNodeId,
                        onClick = { onIntent(WorkflowIntent.SelectNode(node.id)) },
                        onDelete = { onIntent(WorkflowIntent.RemoveNode(node.id)) },
                        onSetEntry = { onIntent(WorkflowIntent.SetEntryPoint(node.id)) },
                        modifier = Modifier.offset(x = node.positionX.dp, y = node.positionY.dp)
                    )
                }
            }
        }

        if (state.executionLog.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().height(150.dp).background(WorkflowColors.Surface).padding(8.dp)
            ) {
                Text("Execution Log", color = WorkflowColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn {
                    items(state.executionLog) { line ->
                        Text(
                            text = line,
                            color = when {
                                line.startsWith("✓") || line.startsWith("✅") -> WorkflowColors.Success
                                line.startsWith("❌") -> WorkflowColors.Error
                                line.startsWith("🚀") || line.startsWith("📍") -> WorkflowColors.Accent
                                else -> WorkflowColors.TerminalGreen
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        if (state.selectedNode != null) {
            NodePropertiesPanel(
                node = state.selectedNode!!,
                allNodes = state.nodes,
                onConnect = { toId -> onIntent(WorkflowIntent.ConnectNodes(state.selectedNodeId!!, toId, "")) },
                onDisconnect = { edgeId -> onIntent(WorkflowIntent.DisconnectNodes(edgeId)) },
                onClose = { onIntent(WorkflowIntent.SelectNode(null)) }
            )
        }
    }
}

// =============================================================
// WizardContent — 安装向导内容区
// =============================================================
@Composable
private fun WizardContent(
    state: WizardState,
    onIntent: (WizardIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(WorkflowColors.Background).padding(16.dp)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Setup Wizard / 安装向导", color = WorkflowColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${state.completedStepsCount}/${state.steps.size}", color = WorkflowColors.Primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.progressPercent },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = WorkflowColors.Primary,
                trackColor = WorkflowColors.Card
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            state.steps.forEachIndexed { index, step ->
                val isActive = index == state.currentStep
                val isCompleted = step.isCompleted
                Box(
                    modifier = Modifier.weight(1f).height(4.dp).background(
                        when {
                            isCompleted -> WorkflowColors.Success
                            isActive -> WorkflowColors.Primary
                            else -> WorkflowColors.Card
                        },
                        RoundedCornerShape(2.dp)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val currentStep = state.steps.getOrNull(state.currentStep)
        if (currentStep != null) {
            WizardStepContent(step = currentStep, state = state, onIntent = onIntent)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(
                onClick = { onIntent(WizardIntent.PreviousStep) },
                enabled = state.currentStep > 0,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WorkflowColors.TextPrimary)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back")
            }
            if (state.isComplete) {
                Button(
                    onClick = { onIntent(WizardIntent.ResetWizard) },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Success)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restart / 重新开始")
                }
            } else {
                Button(
                    onClick = {
                        if (currentStep?.isCompleted == false) {
                            onIntent(WizardIntent.CompleteCurrentStep)
                        }
                        onIntent(WizardIntent.NextStep)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Primary)
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// =============================================================
// Helper Components / 辅助组件
// =============================================================

@Composable
private fun SectionTitle(title: String) {
    Text(text = title, color = WorkflowColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Hub, contentDescription = null, tint = WorkflowColors.TextSecondary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, color = WorkflowColors.TextSecondary, textAlign = TextAlign.Center, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SkillCard(
    skill: SkillInfo,
    isInstalled: Boolean,
    onInstall: () -> Unit,
    onUninstall: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = WorkflowColors.Card),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(WorkflowColors.Surface, CircleShape), contentAlignment = Alignment.Center) {
                Text(skill.category.emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(skill.name, color = WorkflowColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(skill.version, color = WorkflowColors.TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Text(skill.description, color = WorkflowColors.TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⬇️ ${skill.downloadCount}", color = WorkflowColors.TextSecondary, fontSize = 10.sp)
                    Text("⭐ ${skill.rating}", color = WorkflowColors.Warning, fontSize = 10.sp)
                    Text(skill.status.label, color = skill.status.color, fontSize = 10.sp)
                }
            }
            if (isInstalled) {
                IconButton(onClick = onUninstall) { Icon(Icons.Default.Delete, contentDescription = "Uninstall", tint = WorkflowColors.Error) }
            } else {
                IconButton(onClick = onInstall) { Icon(Icons.Default.Download, contentDescription = "Install", tint = WorkflowColors.Primary) }
            }
        }
    }
}

@Composable
private fun SkillDetailHeader(skill: SkillInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(56.dp).background(WorkflowColors.Card, CircleShape), contentAlignment = Alignment.Center) {
                    Text(skill.category.emoji, fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(skill.name, color = WorkflowColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("by ${skill.author} · ${skill.version}", color = WorkflowColors.TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(skill.status.label, color = skill.status.color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(skill.description, color = WorkflowColors.TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(skill.tags) { tag ->
                    Badge(containerColor = WorkflowColors.Card) {
                        Text(tag, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = WorkflowColors.Accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun InstallCommandCard(command: String, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Terminal, contentDescription = null, tint = WorkflowColors.TerminalGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = command, color = WorkflowColors.TerminalGreen, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = WorkflowColors.TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun TestReportCard(report: TestReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (report.isPassed) WorkflowColors.Success.copy(alpha = 0.1f) else WorkflowColors.Warning.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (report.isPassed) Icons.Default.CheckCircle else Icons.Default.Warning, contentDescription = null, tint = if (report.isPassed) WorkflowColors.Success else WorkflowColors.Warning)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Report / 测试报告", color = WorkflowColors.TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text("${report.passedTestCount}/${report.totalTestCount} passed", color = if (report.isPassed) WorkflowColors.Success else WorkflowColors.Warning, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { report.passRate },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = if (report.isPassed) WorkflowColors.Success else WorkflowColors.Warning,
                trackColor = WorkflowColors.Card
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Coverage / 覆盖率: ${report.coverage}%", color = WorkflowColors.TextSecondary, fontSize = 11.sp)
            Text("Time / 耗时: ${report.executionTimeMs}ms", color = WorkflowColors.TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ReadmeCard(readme: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("README / 说明文档", color = WorkflowColors.TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(readme, color = WorkflowColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
    }
}

@Composable
private fun VersionHistoryCard(versions: List<String>) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Version History / 版本历史", color = WorkflowColors.TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            versions.forEachIndexed { index, version ->
                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (index == 0) Icons.Default.FiberManualRecord else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (index == 0) WorkflowColors.Primary else WorkflowColors.TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(version, color = if (index == 0) WorkflowColors.Primary else WorkflowColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    if (index == 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = WorkflowColors.Primary) { Text("current", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 9.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkflowNodeView(
    node: WorkflowNode,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSetEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(120.dp)
            .background(if (isSelected) WorkflowColors.Primary.copy(alpha = 0.2f) else WorkflowColors.Surface, RoundedCornerShape(8.dp))
            .border(2.dp, if (node.isEntryPoint) WorkflowColors.Success else if (isSelected) WorkflowColors.Primary else WorkflowColors.Card, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (node.isEntryPoint) { Text("▶", color = WorkflowColors.Success, fontSize = 10.sp) }
        Text(node.skillName.take(15), color = WorkflowColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (!node.isEntryPoint) {
                IconButton(onClick = onSetEntry, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.PlayCircle, contentDescription = "Set Entry", tint = WorkflowColors.TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WorkflowColors.Error, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun NodePropertiesPanel(
    node: WorkflowNode,
    allNodes: List<WorkflowNode>,
    onConnect: (String) -> Unit,
    onDisconnect: (String) -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Node: ${node.skillName}", color = WorkflowColors.TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = WorkflowColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Connect to / 连接到:", color = WorkflowColors.TextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(allNodes.filter { it.id != node.id }) { target ->
                    OutlinedButton(
                        onClick = { onConnect(target.id) },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(target.skillName.take(10), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardStepContent(
    step: WizardStep,
    state: WizardState,
    onIntent: (WizardIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WorkflowColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (step.isCompleted) WorkflowColors.Success.copy(alpha = 0.2f) else WorkflowColors.Primary.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (step.isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = WorkflowColors.Success)
                    } else {
                        Text("${step.stepNumber}", color = WorkflowColors.Primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(step.title, color = WorkflowColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(step.description, color = WorkflowColors.TextSecondary, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            when (step.stepNumber) {
                1 -> {
                    if (state.androidCliInstalled) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WorkflowColors.Success)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Android CLI ${state.cliVersion} installed!", color = WorkflowColors.Success)
                        }
                    } else {
                        Button(
                            onClick = { onIntent(WizardIntent.InstallAndroidCli) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isInstallingCli,
                            colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Primary)
                        ) {
                            if (state.isInstallingCli) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = WorkflowColors.Background)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Installing...")
                            } else {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Install Android CLI")
                            }
                        }
                    }
                }
                2 -> {
                    Text("Environment configuration is optional for experienced users.", color = WorkflowColors.TextSecondary, fontSize = 12.sp)
                    if (step.isCompleted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WorkflowColors.Success)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Skipped (optional)", color = WorkflowColors.TextSecondary)
                        }
                    }
                }
                3 -> {
                    val skills = listOf(
                        "@android/migrate-to-compose" to "Migrate to Compose",
                        "@android/r8-config-audit" to "R8 Config Audit",
                        "@community/kotlin-best-practices" to "Kotlin Best Practices"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        skills.forEach { (id, name) ->
                            OutlinedButton(
                                onClick = { onIntent(WizardIntent.InstallFirstSkill(id)) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                            }
                        }
                    }
                    if (state.installedSkillsCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WorkflowColors.Success)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${state.installedSkillsCount} skill(s) installed", color = WorkflowColors.Success)
                        }
                    }
                }
                4 -> {
                    Text("Create your own custom Android Skill using the Skill Creator.", color = WorkflowColors.TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onIntent(WizardIntent.CreateFirstSkill) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Accent)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Skill Creator")
                    }
                }
                5 -> {
                    Button(
                        onClick = { onIntent(WizardIntent.RunVerification) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = WorkflowColors.Primary)
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run Verification")
                    }
                }
            }
        }
    }
}

// =============================================================
// Icon Helper Functions / 图标辅助函数
// =============================================================
private fun getTabIcon(tab: SkillsWorkflowTab): ImageVector {
    return when (tab) {
        SkillsWorkflowTab.SKILL_HUB -> Icons.Default.Hub
        SkillsWorkflowTab.SKILL_DETAIL -> Icons.Default.Info
        SkillsWorkflowTab.WORKFLOW_EDITOR -> Icons.Default.AccountTree
        SkillsWorkflowTab.WIZARD -> Icons.Default.BuildCircle
    }
}
