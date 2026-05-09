package com.mvi.kenny.feature.panda4agenttools

// ================================================================
// Panda4AgentToolsScreen — Android Studio Panda 4 AI Agent 增强工具包 UI
// ================================================================
// Complete UI for Panda 4 AI Agent Enhancement Toolkit (5-tab layout).
//
// PRD-240: Android Studio Panda 4 AI Agent 增强工具包
// Design Reference: memory/agency/designs/PRD-240-Android-Studio-Panda-4-AI-Agent-增强工具包.md
//
// Five feature tabs:
// 1. Planning Mode — plan templates + readability tool + × Agent Skills
// 2. Next Edit Prediction — adoption dashboard + custom prediction rules
// 3. Agent Web Search — URL quality scoring + enterprise KB
// 4. Ask Mode — knowledge base configuration
// 5. Dev Verification — CI config + audit log export
// ================================================================

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import java.util.UUID
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.panda4agenttools.Panda4AgentColors as C
import com.mvi.kenny.feature.panda4agenttools.Panda4AgentTab as TabEnum
import kotlinx.coroutines.launch

// ================================================================
// Screen Entry Point / 屏幕入口
// ================================================================

/**
 * ============================================================
 * Panda4AgentToolsScreen — 主入口 Composable
 * ================================================================
 * Root composable for the entire 5-tab toolkit.
 *
 * @param state Current UI state from ViewModel
 * @param onIntent Lambda to send user intents to ViewModel
 * @param effect Flow of one-time side effects (handled via SnackbarHost)
 */
@Composable
fun Panda4AgentToolsScreen(
    state: Panda4AgentToolsState,
    onIntent: (Panda4AgentToolsIntent) -> Unit,
    effect: kotlinx.coroutines.flow.Flow<Panda4AgentToolsEffect>
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Collect effects and show snackbar
    LaunchedEffect(effect) {
        effect.collect { e ->
            when (e) {
                is Panda4AgentToolsEffect.ShowSnackbar -> {
                    scope.launch { snackbarHostState.showSnackbar(e.message) }
                }
                is Panda4AgentToolsEffect.DownloadFile -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("导出完成: ${e.path}")
                    }
                }
                is Panda4AgentToolsEffect.CopyToClipboard -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("已复制到剪贴板")
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(C.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top App Bar ────────────────────────────────────────────────────
            TopAppBar()

            // ── Tab Row ────────────────────────────────────────────────────────
            TabNavigationRow(
                selectedIndex = state.currentTabIndex,
                onTabSelected = { onIntent(Panda4AgentToolsIntent.SelectTab(it)) }
            )

            // ── Tab Content ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state.currentTabIndex) {
                    0 -> PlanningModeTab(state, onIntent)
                    1 -> NextEditPredictionTab(state, onIntent)
                    2 -> AgentWebSearchTab(state, onIntent)
                    3 -> AskModeTab(state, onIntent)
                    4 -> DevVerificationTab(state, onIntent)
                }
            }
        }

        // ── Snackbar Host ─────────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ================================================================
// Top App Bar / 顶部导航栏
// ================================================================

@Composable
private fun TopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(C.Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = C.Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "Android Studio Panda 4 AI Agent 增强工具",
                color = C.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Panda 4 AI Agent Enhancement Toolkit • PRD-240",
                color = C.TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ================================================================
// Tab Navigation Row / Tab 导航栏
// ================================================================

@Composable
private fun TabNavigationRow(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = TabEnum.entries

    Column(modifier = Modifier.background(C.Surface)) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = C.Surface,
            contentColor = C.TextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = C.Primary,
                    height = 2.dp
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onTabSelected(index) },
                    selectedContentColor = C.Primary,
                    unselectedContentColor = C.TextSecondary,
                    text = {
                        Text(
                            text = tab.title,
                            fontSize = 12.sp,
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
}

/** Get icon for each tab / 获取各 Tab 图标 */
private fun getTabIcon(tab: TabEnum): ImageVector = when (tab) {
    TabEnum.PlanningMode -> Icons.Default.FactCheck
    TabEnum.NextEditPrediction -> Icons.Default.Edit
    TabEnum.AgentWebSearch -> Icons.Default.Web
    TabEnum.AskMode -> Icons.Default.Search
    TabEnum.DevVerification -> Icons.Default.Security
}

// ================================================================
// Tab 1: Planning Mode / 计划模式
// ================================================================

@Composable
private fun PlanningModeTab(
    state: Panda4AgentToolsState,
    onIntent: (Panda4AgentToolsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C.Background)
            .padding(16.dp)
    ) {
        // ── Scenario Selector ───────────────────────────────────────────────
        Text(
            text = "场景选择 / Scenario",
            color = C.TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlanScenario.entries.forEach { scenario ->
                FilterChip(
                    selected = state.selectedScenario == scenario,
                    onClick = { onIntent(Panda4AgentToolsIntent.SelectScenario(scenario)) },
                    label = { Text(scenario.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = C.Primary.copy(alpha = 0.2f),
                        selectedLabelColor = C.Primary,
                        containerColor = C.Card,
                        labelColor = C.TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Template List ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "计划模板库 / Plan Templates",
                color = C.TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${state.planTemplates.size} 个模板",
                color = C.TextSecondary,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.planTemplates) { template ->
                TemplateCard(
                    template = template,
                    onPreview = { onIntent(Panda4AgentToolsIntent.PreviewTemplate(template)) }
                )
            }
        }

        // ── Plan Formatter Section ─────────────────────────────────────────
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "计划格式化工具 / Plan Formatter",
            color = C.TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.rawPlanInput,
            onValueChange = { onIntent(Panda4AgentToolsIntent.UpdateRawPlanInput(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("粘贴 AI 生成的原始计划文本...", color = C.TextSecondary, fontSize = 12.sp) },
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = C.Primary,
                unfocusedBorderColor = C.Border,
                focusedTextColor = C.TextPrimary,
                unfocusedTextColor = C.TextPrimary,
                cursorColor = C.Primary,
                focusedContainerColor = C.Surface,
                unfocusedContainerColor = C.Surface
            )
        
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onIntent(Panda4AgentToolsIntent.FormatPlan(state.rawPlanInput)) },
            enabled = state.rawPlanInput.isNotBlank() && !state.isFormattingPlan,
            colors = ButtonDefaults.buttonColors(containerColor = C.Primary)
        ) {
            if (state.isFormattingPlan) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = C.TextPrimary, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text("格式化计划 / Format Plan", fontSize = 13.sp)
        }

        // ── Formatted Output ────────────────────────────────────────────────
        if (state.formattedPlanOutput.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = C.Card),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("格式化结果", color = C.Success, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        TextButton(onClick = { /* copy */ }) {
                            Text("复制", color = C.Primary, fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = state.formattedPlanOutput,
                        color = C.TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 15,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // ── Template Preview BottomSheet ────────────────────────────────────
        state.previewTemplate?.let { template ->
            Spacer(modifier = Modifier.height(12.dp))
            TemplatePreviewPanel(
                template = template,
                onClose = { onIntent(Panda4AgentToolsIntent.ClosePreview) }
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: PlanTemplate,
    onPreview: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview() },
        colors = CardDefaults.cardColors(containerColor = C.Card),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        color = C.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = template.description,
                        color = C.TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    Icons.Default.Description,
                    null,
                    tint = C.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (template.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    template.tags.split(",").take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(C.Primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(tag.trim(), color = C.Primary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplatePreviewPanel(
    template: PlanTemplate,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "模板预览: ${template.title}",
                    color = C.Primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "关闭", tint = C.TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(C.Background, RoundedCornerShape(6.dp))
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = template.content,
                    color = C.TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ================================================================
// Tab 2: Next Edit Prediction / NEP 预测采纳
// ================================================================

@Composable
private fun NextEditPredictionTab(
    state: Panda4AgentToolsState,
    onIntent: (Panda4AgentToolsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C.Background)
            .padding(16.dp)
    ) {
        // ── Adoption Rate Dashboard ────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = C.Card),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("采纳率 / Adoption Rate", color = C.TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(state.adoptionRate * 100).toInt()}%",
                            color = when {
                                state.adoptionRate >= 0.7f -> C.Success
                                state.adoptionRate >= 0.4f -> C.Warning
                                else -> C.Error
                            },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Simple trend visualization (7 dots)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.adoptionTrend.takeLast(7).forEach { rate ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        when {
                                            rate >= 0.7f -> C.Success
                                            rate >= 0.4f -> C.Warning
                                            else -> C.Error
                                        },
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { state.adoptionRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when {
                        state.adoptionRate >= 0.7f -> C.Success
                        state.adoptionRate >= 0.4f -> C.Warning
                        else -> C.Error
                    },
                    trackColor = C.Surface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "基于过去 7 天 NEP 预测采纳数据 / Based on last 7 days NEP prediction adoption",
                    color = C.TextSecondary,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onIntent(Panda4AgentToolsIntent.AnalyzeAdoptionRate) },
                    enabled = !state.isAnalyzingRules,
                    colors = ButtonDefaults.buttonColors(containerColor = C.Primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isAnalyzingRules) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = C.TextPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("刷新采纳率 / Refresh Rate", fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Prediction Rules ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("自定义预测规则 / Custom Rules", color = C.TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            TextButton(onClick = {
                onIntent(Panda4AgentToolsIntent.AddPredictionRule(
                    PredictionRule(
                        id = UUID.randomUUID().toString(),
                        name = "新规则 ${state.predictionRules.size + 1}",
                        pattern = "**/*.kt:modify:*Test.kt",
                        description = "用户自定义规则"
                    )
                ))
            }) {
                Icon(Icons.Default.Add, null, tint = C.Primary, modifier = Modifier.size(14.dp))
                Text("添加", color = C.Primary, fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.predictionRules) { rule ->
                RuleCard(
                    rule = rule,
                    onToggle = { onIntent(Panda4AgentToolsIntent.ToggleRuleEnabled(rule.id)) },
                    onDelete = { onIntent(Panda4AgentToolsIntent.DeletePredictionRule(rule.id)) }
                )
            }
        }
    }
}

@Composable
private fun RuleCard(
    rule: PredictionRule,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Card),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(rule.name, color = C.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    rule.pattern,
                    color = C.Primary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (rule.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(rule.description, color = C.TextSecondary, fontSize = 11.sp, maxLines = 1)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "触发 ${rule.adoptionCount} 次",
                    color = C.TextSecondary,
                    fontSize = 10.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (rule.isEnabled) Icons.Default.ToggleOn else Icons.Default.Speed,
                        "Toggle",
                        tint = if (rule.isEnabled) C.Success else C.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = C.Error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ================================================================
// Tab 3: Agent Web Search / Agent 网络搜索
// ================================================================

@Composable
private fun AgentWebSearchTab(
    state: Panda4AgentToolsState,
    onIntent: (Panda4AgentToolsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C.Background)
            .padding(16.dp)
    ) {
        // ── URL Quality Analyzer ───────────────────────────────────────────
        Text("URL 质量评分 / URL Quality Analyzer", color = C.TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.searchUrlInput,
                onValueChange = { /* updated separately */ },
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入 URL 分析质量...", color = C.TextSecondary, fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = C.Primary,
                    unfocusedBorderColor = C.Border,
                    focusedTextColor = C.TextPrimary,
                    unfocusedTextColor = C.TextPrimary,
                    cursorColor = C.Primary,
                    focusedContainerColor = C.Surface,
                    unfocusedContainerColor = C.Surface
                )
            
            )
            Button(
                onClick = { onIntent(Panda4AgentToolsIntent.AnalyzeSearchUrl(state.searchUrlInput)) },
                enabled = state.searchUrlInput.isNotBlank() && !state.isAnalyzingUrl,
                colors = ButtonDefaults.buttonColors(containerColor = C.Primary)
            ) {
                if (state.isAnalyzingUrl) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = C.TextPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                }
            }
        }

        // ── Quality Result ─────────────────────────────────────────────────
        state.searchResult?.let { result ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = C.Card),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("质量评分: ", color = C.TextSecondary, fontSize = 12.sp)
                        Text(
                            "${result.overallScore}/100",
                            color = when {
                                result.overallScore >= 80 -> C.Success
                                result.overallScore >= 60 -> C.Warning
                                else -> C.Error
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ScoreBar("相关性 Relevance", result.relevanceScore, C.Primary)
                    ScoreBar("新鲜度 Freshness", result.freshnessScore, C.Warning)
                    ScoreBar("权威性 Authority", result.authorityScore, C.Success)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(result.summary, color = C.TextSecondary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Knowledge Base ─────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("企业知识库 / Enterprise KB", color = C.TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("${state.knowledgeBases.size} 条", color = C.TextSecondary, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))

        // KB Add Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = C.Card),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.newKbTitleInput,
                        onValueChange = { newValue -> onIntent(Panda4AgentToolsIntent.UpdateKbInput(newValue, state.newKbContentInput, state.newKbType)) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("标题", color = C.TextSecondary, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = C.Primary,
                            unfocusedBorderColor = C.Border,
                            focusedTextColor = C.TextPrimary,
                            unfocusedTextColor = C.TextPrimary,
                            cursorColor = C.Primary,
                            focusedContainerColor = C.Surface,
                            unfocusedContainerColor = C.Surface
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.newKbContentInput,
                    onValueChange = { newValue -> onIntent(Panda4AgentToolsIntent.UpdateKbInput(state.newKbTitleInput, newValue, state.newKbType)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("内容 / URL / 代码", color = C.TextSecondary, fontSize = 12.sp) },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = C.Primary,
                        unfocusedBorderColor = C.Border,
                        focusedTextColor = C.TextPrimary,
                        unfocusedTextColor = C.TextPrimary,
                        cursorColor = C.Primary,
                        focusedContainerColor = C.Surface,
                        unfocusedContainerColor = C.Surface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KbType.entries.forEach { type ->
                            FilterChip(
                                selected = state.newKbType == type,
                                onClick = { onIntent(Panda4AgentToolsIntent.UpdateKbInput(state.newKbTitleInput, state.newKbContentInput, type)) },
                                label = { Text(type.name, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = C.Primary.copy(alpha = 0.2f),
                                    selectedLabelColor = C.Primary,
                                    containerColor = C.Surface,
                                    labelColor = C.TextSecondary
                                )
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            if (state.newKbTitleInput.isNotBlank() && state.newKbContentInput.isNotBlank()) {
                                onIntent(Panda4AgentToolsIntent.AddKnowledgeBase(
                                    KnowledgeBase(
                                        id = UUID.randomUUID().toString(),
                                        type = state.newKbType,
                                        title = state.newKbTitleInput,
                                        content = state.newKbContentInput
                                    )
                                ))
                            }
                        },
                        modifier = Modifier
                            .background(C.Primary, RoundedCornerShape(6.dp))
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, "添加", tint = C.TextPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // KB List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.knowledgeBases) { kb ->
                KbCard(
                    kb = kb,
                    onDelete = { onIntent(Panda4AgentToolsIntent.DeleteKnowledgeBase(kb.id)) }
                )
            }
        }
    }
}

@Composable
private fun ScoreBar(label: String, score: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = C.TextSecondary, fontSize = 11.sp)
            Text("$score", color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = C.Surface
        )
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun KbCard(
    kb: KnowledgeBase,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Card),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Icon(
                    when (kb.type) {
                        KbType.Markdown -> Icons.Default.Description
                        KbType.Url -> Icons.Default.Link
                        KbType.Code -> Icons.Default.Code
                    },
                    null,
                    tint = C.Primary,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(kb.title, color = C.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        kb.content.take(80) + if (kb.content.length > 80) "..." else "",
                        color = C.TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = C.Error, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ================================================================
// Tab 4: Ask Mode / 问答模式
// ================================================================

@Composable
private fun AskModeTab(
    state: Panda4AgentToolsState,
    onIntent: (Panda4AgentToolsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C.Background)
            .padding(16.dp)
    ) {
        // ── Knowledge Base Config ──────────────────────────────────────────
        Text("知识库配置 / Knowledge Base", color = C.TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))

        // Ask Mode query input
        OutlinedTextField(
            value = state.askQueryInput,
            onValueChange = { onIntent(Panda4AgentToolsIntent.UpdateAskQuery(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("输入问题 / Enter your question...", color = C.TextSecondary, fontSize = 12.sp) },
            minLines = 2,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = C.Primary,
                unfocusedBorderColor = C.Border,
                focusedTextColor = C.TextPrimary,
                unfocusedTextColor = C.TextPrimary,
                cursorColor = C.Primary,
                focusedContainerColor = C.Surface,
                unfocusedContainerColor = C.Surface
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onIntent(Panda4AgentToolsIntent.PreviewAskOutput(state.askQueryInput)) },
            enabled = state.askQueryInput.isNotBlank() && !state.isPreviewingAsk,
            colors = ButtonDefaults.buttonColors(containerColor = C.Primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isPreviewingAsk) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = C.TextPrimary, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text("预览 Ask Mode / Preview Ask Mode", fontSize = 13.sp)
        }

        // Preview output
        if (state.askPreviewOutput.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = C.Card),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("预览结果 / Preview Output", color = C.Success, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        state.askPreviewOutput,
                        color = C.TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ask Mode knowledge items
        Text("已配置知识项 / Configured Items", color = C.TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.askKnowledgeItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = C.Card),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (item.type) {
                                KbType.Markdown -> Icons.Default.Description
                                KbType.Url -> Icons.Default.Link
                                KbType.Code -> Icons.Default.Code
                            },
                            null,
                            tint = C.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, color = C.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(item.type.name, color = C.TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
            if (state.askKnowledgeItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = C.Surface),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无配置知识项 / No items configured", color = C.TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Tab 5: Dev Verification / 开发者验证
// ================================================================

@Composable
private fun DevVerificationTab(
    state: Panda4AgentToolsState,
    onIntent: (Panda4AgentToolsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C.Background)
            .padding(16.dp)
    ) {
        // ── CI Config ───────────────────────────────────────────────────
        Text("CI 集成配置 / CI Integration Config", color = C.TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = C.Card),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Identity Provider
                Text("身份提供者 / Identity Provider", color = C.TextSecondary, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = state.ciConfig.identityProvider,
                    onValueChange = {
                        onIntent(Panda4AgentToolsIntent.UpdateCiConfig(state.ciConfig.copy(identityProvider = it)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("GitHub", color = C.TextSecondary, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = C.Primary,
                        unfocusedBorderColor = C.Border,
                        focusedTextColor = C.TextPrimary,
                        unfocusedTextColor = C.TextPrimary,
                        cursorColor = C.Primary,
                        focusedContainerColor = C.Surface,
                        unfocusedContainerColor = C.Surface
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Repository
                Text("仓库 / Repository", color = C.TextSecondary, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = state.ciConfig.repository,
                    onValueChange = {
                        onIntent(Panda4AgentToolsIntent.UpdateCiConfig(state.ciConfig.copy(repository = it)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("owner/repo", color = C.TextSecondary, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = C.Primary,
                        unfocusedBorderColor = C.Border,
                        focusedTextColor = C.TextPrimary,
                        unfocusedTextColor = C.TextPrimary,
                        cursorColor = C.Primary,
                        focusedContainerColor = C.Surface,
                        unfocusedContainerColor = C.Surface
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Required Role
                Text("所需角色 / Required Role", color = C.TextSecondary, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = state.ciConfig.requiredRole,
                    onValueChange = {
                        onIntent(Panda4AgentToolsIntent.UpdateCiConfig(state.ciConfig.copy(requiredRole = it)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("write", color = C.TextSecondary, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = C.Primary,
                        unfocusedBorderColor = C.Border,
                        focusedTextColor = C.TextPrimary,
                        unfocusedTextColor = C.TextPrimary,
                        cursorColor = C.Primary,
                        focusedContainerColor = C.Surface,
                        unfocusedContainerColor = C.Surface
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Approval Workflow toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("审批工作流 / Approval Workflow", color = C.TextPrimary, fontSize = 12.sp)
                    IconButton(
                        onClick = {
                            onIntent(Panda4AgentToolsIntent.UpdateCiConfig(
                                state.ciConfig.copy(approvalWorkflow = !state.ciConfig.approvalWorkflow)
                            ))
                        }
                    ) {
                        Icon(
                            if (state.ciConfig.approvalWorkflow) Icons.Default.ToggleOn else Icons.Default.Speed,
                            null,
                            tint = if (state.ciConfig.approvalWorkflow) C.Success else C.TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Audit Log ────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("审计日志 / Audit Logs", color = C.TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { onIntent(Panda4AgentToolsIntent.LoadAuditLogs) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Refresh, "刷新", tint = C.Primary, modifier = Modifier.size(18.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Export buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExportFormat.entries.forEach { format ->
                OutlinedButton(
                    onClick = { onIntent(Panda4AgentToolsIntent.ExportAuditLogs(format)) },
                    enabled = !state.isExporting,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = C.Primary),
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = C.Primary, strokeWidth = 2.dp)
                    } else {
                        Text(format.name, fontSize = 11.sp)
                    }
                }
            }
        }

        if (state.isExporting) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.exportProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = C.Primary,
                trackColor = C.Surface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Audit log list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.auditLogs) { log ->
                AuditLogCard(log = log)
            }
            if (state.auditLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = C.Surface),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.isLoadingAudit) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = C.Primary, strokeWidth = 2.dp)
                            } else {
                                Text("暂无审计日志 / No audit logs", color = C.TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(log: AuditLog) {
    val actionColor = when (log.action) {
        "PlanCreated", "PlanApproved" -> C.Success
        "PlanRejected" -> C.Error
        "PlanReviewed" -> C.Warning
        "TaskExecuted" -> C.Primary
        else -> C.TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Card),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(actionColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(log.action, color = actionColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
                Text(log.timestamp, color = C.TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(log.description, color = C.TextPrimary, fontSize = 12.sp)
            if (log.plannerOutput.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Planner: ${log.plannerOutput.take(60)}...",
                    color = C.TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (log.agentAction.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Agent: ${log.agentAction.take(60)}...",
                    color = C.TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("执行者: ${log.executor}", color = C.TextSecondary, fontSize = 10.sp)
                if (log.deviationDegree > 0f) {
                    Text("偏差: ${(log.deviationDegree * 100).toInt()}%", color = C.Warning, fontSize = 10.sp)
                }
            }
        }
    }
}