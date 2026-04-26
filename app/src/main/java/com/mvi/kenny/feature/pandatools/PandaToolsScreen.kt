package com.mvi.kenny.feature.pandatools

// ============================================================
// PandaToolsScreen — Android Studio Panda 4 AI Tools Screen
// PRD-136 | Android Studio Panda 4 AI 编程助手开发工具包
// ============================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.viewmodel.compose.viewModel

// ============================================================
// 颜色常量 / Color Constants
// ============================================================
private val GoogleBlue = Color(0xFF1A73E8)
private val GoogleYellow = Color(0xFFFBBC04)
private val GoogleGreen = Color(0xFF34A853)
private val GoogleRed = Color(0xFFEA4335)
private val SurfaceVariant = Color(0xFFF1F3F4)
private val BackgroundColor = Color(0xFFFAFAFA)

// ============================================================
// 主入口 / Main Entry Point
// ============================================================

/**
 * PandaTools 主页面 — 包含 5 个 Tab 的开发者工具平台。
 * PandaTools main screen — developer tools platform with 5 tabs.
 *
 * @param viewModel PandaTools ViewModel
 * @param onNavigateBack 返回导航回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PandaToolsScreen(
    viewModel: PandaToolsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 收集副作用 / Collect side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PandaToolsEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is PandaToolsEffect.ShowError -> snackbarHostState.showSnackbar("❌ ${effect.message}")
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🦭 Panda 4 AI Tools", fontWeight = FontWeight.Bold)
                        Text(
                            "Android Studio Panda 4 编程助手工具包",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回 / Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GoogleBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundColor)
        ) {
            // Tab 导航 / Tab Navigation
            PandaTabRow(
                currentTab = state.currentTab,
                onTabSelected = { viewModel.processIntent(PandaToolsIntent.SwitchTab(it)) }
            )

            // Tab 内容 / Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (state.currentTab) {
                    PandaTab.SkillsEditor -> SkillsEditorTab(state, viewModel)
                    PandaTab.PlanManagement -> PlanManagementTab(state, viewModel)
                    PandaTab.NepAnalysis -> NepAnalysisTab(state, viewModel)
                    PandaTab.PermissionAudit -> PermissionAuditTab(state, viewModel)
                    PandaTab.QualityScore -> QualityScoreTab(state, viewModel)
                }
            }
        }
    }
}

// ============================================================
// Tab Row — 顶部 Tab 导航
// ============================================================

@Composable
private fun PandaTabRow(
    currentTab: PandaTab,
    onTabSelected: (PandaTab) -> Unit
) {
    TabRow(
        selectedTabIndex = PandaTab.entries.indexOf(currentTab),
        containerColor = Color.White,
        contentColor = GoogleBlue
    ) {
        PandaTab.entries.forEach { tab ->
            Tab(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        tab.titleCn,
                        fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            PandaTab.SkillsEditor -> Icons.Outlined.Code
                            PandaTab.PlanManagement -> Icons.Outlined.Assignment
                            PandaTab.NepAnalysis -> Icons.Outlined.Analytics
                            PandaTab.PermissionAudit -> Icons.Outlined.Security
                            PandaTab.QualityScore -> Icons.Outlined.Star
                        },
                        contentDescription = tab.titleEn
                    )
                }
            )
        }
    }
}

// ============================================================
// Tab 1: Skills 编辑器
// ============================================================

@Composable
private fun SkillsEditorTab(state: PandaToolsState, viewModel: PandaToolsViewModel) {
    val editorState = state.skillsEditorState
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // 左侧文件树 / Left: File Tree
        Card(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "📁 项目文件 / Project Files",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (editorState.projectPath.isEmpty()) {
                    OutlinedTextField(
                        value = editorState.projectPath,
                        onValueChange = { viewModel.processIntent(PandaToolsIntent.OpenProject(it)) },
                        label = { Text("项目路径 / Project Path") },
                        placeholder = { Text("/path/to/project") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    // 模拟文件列表 / Mock file list
                    val mockFiles = listOf(
                        SkillFile("1", "compose-navigation.yaml", editorState.projectPath, "Official"),
                        SkillFile("2", "gradle-config.yaml", editorState.projectPath, "Official"),
                        SkillFile("3", "material-design.yaml", editorState.projectPath, "Community"),
                        SkillFile("4", "custom-agent.yaml", editorState.projectPath, "Community")
                    )
                    mockFiles.forEach { file ->
                        val isSelected = editorState.selectedFile?.id == file.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) GoogleBlue.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { viewModel.processIntent(PandaToolsIntent.SelectFile(file)) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = if (file.category == "Official") GoogleBlue else GoogleGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                file.name,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 中部编辑器 / Middle: Editor
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // 编辑器工具栏 / Editor Toolbar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            editorState.selectedFile?.name ?: "选择文件 / Select a file",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Row {
                            Button(
                                onClick = { viewModel.processIntent(PandaToolsIntent.ValidateFile) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoogleYellow),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("🔍 验证", fontSize = 12.sp, color = Color.Black)
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.processIntent(PandaToolsIntent.SaveFile) },
                                enabled = editorState.isDirty,
                                colors = ButtonDefaults.buttonColors(containerColor = GoogleGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("💾 保存", fontSize = 12.sp)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // 编辑器内容 / Editor Content
                    OutlinedTextField(
                        value = editorState.fileContent,
                        onValueChange = { viewModel.processIntent(PandaToolsIntent.UpdateFileContent(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        ),
                        placeholder = { Text("选择左侧文件开始编辑 / Select a file from the left") },
                        enabled = editorState.selectedFile != null
                    )

                    // 验证输出 / Validation Output
                    if (editorState.validationOutput.isNotEmpty()) {
                        val hasErrors = editorState.validationErrors.any { it.severity == "Error" }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            color = if (hasErrors) GoogleRed.copy(alpha = 0.1f) else GoogleGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                editorState.validationOutput,
                                modifier = Modifier.padding(8.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = if (hasErrors) GoogleRed else GoogleGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Tab 2: 计划管理
// ============================================================

@Composable
private fun PlanManagementTab(state: PandaToolsState, viewModel: PandaToolsViewModel) {
    val planState = state.planManagementState

    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧计划列表 / Left: Plan List
        Card(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📋 计划列表 / Plans", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        "${planState.plans.size} 个计划",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                if (planState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else {
                    LazyColumn {
                        items(planState.plans) { plan ->
                            PlanListItem(
                                plan = plan,
                                isSelected = planState.selectedPlan?.id == plan.id,
                                onClick = { viewModel.processIntent(PandaToolsIntent.SelectPlan(plan)) }
                            )
                        }
                    }
                }
            }
        }

        // 右侧计划详情 / Right: Plan Detail
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            if (planState.selectedPlan == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("选择左侧计划查看详情 / Select a plan from the left", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val plan = planState.selectedPlan
                Column(modifier = Modifier.padding(16.dp)) {
                    // 计划头部 / Plan Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.height(4.dp))
                            Row {
                                StatusBadge(plan.status)
                                Spacer(Modifier.width(8.dp))
                                Text("v${plan.version}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        // 操作按钮 / Action Buttons
                        Row {
                            Button(
                                onClick = { viewModel.processIntent(PandaToolsIntent.ApprovePlan(plan.id)) },
                                enabled = plan.status == "Pending",
                                colors = ButtonDefaults.buttonColors(containerColor = GoogleGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("✅ 批准", fontSize = 12.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.processIntent(PandaToolsIntent.RequestPlanRevision(plan.id, "请优化步骤描述")) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("📝 请求修改", fontSize = 12.sp)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // 步骤列表 / Steps List
                    Text("📝 执行步骤 / Execution Steps", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn {
                        itemsIndexed(plan.steps) { index, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Checkbox(
                                    checked = step.isCompleted,
                                    onCheckedChange = null,
                                    enabled = false,
                                    colors = CheckboxDefaults.colors(checkedColor = GoogleGreen)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "${index + 1}. ${step.title}",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (step.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(step.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // 开发者评论 / Developer Comments
                    if (plan.developerComments.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Text("💬 评审注释 / Review Comments", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        plan.developerComments.forEach { comment ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                color = SurfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(comment.author, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(comment.timestamp.take(10), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(comment.content, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanListItem(plan: AgentPlan, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) GoogleBlue.copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(plan.title, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(plan.status)
                Text("v${plan.version}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(plan.updatedAt.take(10), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ============================================================
// Tab 3: NEP 分析
// ============================================================

@Composable
private fun NepAnalysisTab(state: PandaToolsState, viewModel: PandaToolsViewModel) {
    val nepState = state.nepAnalysisState

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 概览卡片 / Overview Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "总预测次数",
                value = nepState.totalPredictions.toString(),
                subtitle = "Total Predictions",
                color = GoogleBlue
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "预测准确率",
                value = "${(nepState.accuracyRate * 100).toInt()}%",
                subtitle = "Accuracy Rate",
                color = GoogleGreen
            )
        }

        Spacer(Modifier.height(12.dp))

        // 代码风格画像 / Code Style Profile
        nepState.codeStyleProfile?.let { profile ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎨 代码风格画像 / Code Style Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoChip("语言: ${profile.languagePreference}")
                        InfoChip("命名: ${profile.namingPattern}")
                        InfoChip("平均行: ${profile.avgLineLength}")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        profile.refactoringPatterns.forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tag, fontSize = 11.sp) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // 预测历史 / Prediction History
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📊 预测历史 / Prediction History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                LazyColumn {
                    items(nepState.predictionHistory) { record ->
                        PredictionRecordItem(
                            record = record,
                            isSelected = nepState.selectedRecord?.id == record.id,
                            onClick = { viewModel.processIntent(PandaToolsIntent.SelectPredictionRecord(record)) },
                            onMarkMisclassification = { viewModel.processIntent(PandaToolsIntent.MarkPredictionMisclassification(record.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, title: String, value: String, subtitle: String, color: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = color)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PredictionRecordItem(
    record: PredictionRecord,
    isSelected: Boolean,
    onClick: () -> Unit,
    onMarkMisclassification: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) GoogleBlue.copy(alpha = 0.05f) else SurfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (record.isMatch) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (record.isMatch) GoogleGreen else GoogleRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(record.filePath, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
                Text(record.language, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text("Predicted: ${record.predictedContent}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!record.isMatch) {
                Text("Actual:    ${record.actualContent}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = GoogleRed.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                TextButton(onClick = onMarkMisclassification, contentPadding = PaddingValues(0.dp)) {
                    Text("标记误判", fontSize = 11.sp)
                }
            }
        }
    }
}

// ============================================================
// Tab 4: 权限审计
// ============================================================

@Composable
private fun PermissionAuditTab(state: PandaToolsState, viewModel: PandaToolsViewModel) {
    val auditState = state.permissionAuditState

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 审计控制区 / Audit Control Area
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GoogleBlue.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🔒 Agent Mode 权限合规审计", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("检测 Agent 权限配置是否合规，识别过度授权风险", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { viewModel.processIntent(PandaToolsIntent.RunPermissionAudit) },
                    enabled = !auditState.isAuditing,
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue)
                ) {
                    if (auditState.isAuditing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(if (auditState.isAuditing) "审计中..." else "开始审计")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // 审计概览 / Audit Overview
        auditState.auditOverview?.let { overview ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "权限总数", value = overview.totalPermissions.toString(), subtitle = "Total Permissions", color = GoogleBlue)
                StatCard(modifier = Modifier.weight(1f), title = "已覆盖", value = overview.coveredPermissions.toString(), subtitle = "Covered", color = GoogleGreen)
                StatCard(modifier = Modifier.weight(1f), title = "覆盖率", value = "${(overview.coverageRate * 100).toInt()}%", subtitle = "Coverage", color = GoogleYellow)
                StatCard(modifier = Modifier.weight(1f), title = "风险等级", value = overview.riskLevel, subtitle = "Risk Level", color = when (overview.riskLevel) { "High" -> GoogleRed; "Medium" -> GoogleYellow; else -> GoogleGreen })
            }
        }

        Spacer(Modifier.height(12.dp))

        // 权限矩阵 + 风险项 / Permission Matrix + Risk Items
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 权限矩阵 / Permission Matrix
            Card(modifier = Modifier.weight(1f).fillMaxHeight(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📋 权限矩阵 / Permission Matrix", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn {
                        item {
                            // 表头 / Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceVariant)
                                    .padding(8.dp)
                            ) {
                                listOf("操作", "文件", "网络", "执行", "风险").forEach { header ->
                                    Text(header, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        items(auditState.permissionMatrix) { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text(row.operation, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text(row.fileAccess, fontSize = 12.sp, color = GoogleBlue, modifier = Modifier.weight(1f))
                                Text(row.networkAccess, fontSize = 12.sp, color = GoogleBlue, modifier = Modifier.weight(1f))
                                Text(row.execAccess, fontSize = 12.sp, color = GoogleBlue, modifier = Modifier.weight(1f))
                                val riskColor = when (row.riskLevel) { "High" -> GoogleRed; "Medium" -> GoogleYellow; else -> GoogleGreen }
                                Text(row.riskLevel, fontSize = 12.sp, color = riskColor, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }

            // 风险项 / Risk Items
            Card(modifier = Modifier.weight(1f).fillMaxHeight(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚠️ 风险项 / Risk Items", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    if (auditState.riskItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("点击「开始审计」扫描风险项", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn {
                            items(auditState.riskItems) { risk ->
                                RiskItemCard(risk)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskItemCard(risk: RiskItem) {
    val riskColor = when (risk.riskLevel) { "High" -> GoogleRed; "Medium" -> GoogleYellow; else -> GoogleGreen }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = riskColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (risk.riskLevel) { "High" -> Icons.Default.Error; "Medium" -> Icons.Default.Warning; else -> Icons.Default.Info },
                    contentDescription = null,
                    tint = riskColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(risk.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = riskColor)
            }
            Spacer(Modifier.height(4.dp))
            Text(risk.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("💡 建议: ${risk.suggestion}", fontSize = 11.sp, color = GoogleBlue)
        }
    }
}

// ============================================================
// Tab 5: 质量评分
// ============================================================

@Composable
private fun QualityScoreTab(state: PandaToolsState, viewModel: PandaToolsViewModel) {
    val scoreState = state.qualityScoreState

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 排序和筛选 / Sort and Filter
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("排序:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                SkillSortOption.entries.forEach { option ->
                    FilterChip(
                        selected = scoreState.sortBy == option,
                        onClick = { viewModel.processIntent(PandaToolsIntent.SortSkills(option)) },
                        label = { Text(option.labelCn) }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Skill 排行榜 / Skill Leaderboard
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(scoreState.skills) { skill ->
                SkillCardItem(
                    skill = skill,
                    onClick = { viewModel.processIntent(PandaToolsIntent.SelectSkill(skill)) }
                )
            }
        }
    }
}

@Composable
private fun SkillCardItem(skill: SkillCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(skill.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (skill.isOfficial) {
                            Spacer(Modifier.width(6.dp))
                            Surface(color = GoogleBlue, shape = RoundedCornerShape(4.dp)) {
                                Text("官方", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(skill.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        skill.tags.forEach { tag ->
                            SuggestionChip(onClick = {}, label = { Text(tag, fontSize = 10.sp) })
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GoogleYellow, modifier = Modifier.size(16.dp))
                        Text(String.format("%.1f", skill.rating), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoogleYellow)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${skill.usageCount} 次使用", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(skill.author, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ============================================================
// 通用组件 / Common Components
// ============================================================

@Composable
private fun StatusBadge(status: String) {
    val (color, label) = when (status) {
        "Approved" -> GoogleGreen to "已批准"
        "Pending" -> GoogleYellow to "待审核"
        "Rejected" -> GoogleRed to "已拒绝"
        "Executing" -> GoogleBlue to "执行中"
        "Completed" -> GoogleGreen to "已完成"
        "Draft" -> Color.Gray to "草稿"
        "RequestChange" -> GoogleRed to "需修改"
        else -> Color.Gray to status
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
        Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(color = SurfaceVariant, shape = RoundedCornerShape(4.dp)) {
        Text(text, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}
