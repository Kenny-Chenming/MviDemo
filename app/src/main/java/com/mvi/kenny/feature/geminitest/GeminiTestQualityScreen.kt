package com.mvi.kenny.feature.geminitest

// ================================================================
// GeminiTestQualityScreen — Gemini Test Quality Hub 主界面
// ================================================================
// Main screen composable for PRD-104: Gemini Unit Test Generation Toolkit.
//
// 6-tab interface: Dashboard / Custom Generator / Spec Engine /
// Traceability / Blind Spot Analyzer / CI Integration.
//
// PRD-104: Android Studio Panda 4 Gemini 单元测试生成工具包
// ================================================================

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.cos
import kotlin.math.sin

// Color palette — Gemini AI themed
private val GeminiPurple = Color(0xFF8A4FE8)
private val GeminiBlue = Color(0xFF4285F4)
private val SuccessGreen = Color(0xFF34C759)
private val WarningYellow = Color(0xFFFFD60A)
private val WarningOrange = Color(0xFFFF9F0A)
private val DangerRed = Color(0xFFFF453A)
private val DarkBg = Color(0xFF1E1E1E)
private val DarkCard = Color(0xFF2D2D2D)
private val DarkMuted = Color(0xFFB0B0B0)
private val DarkSubtle = Color(0xFF505050)

// ================================================================
// GeminiTestQualityScreen — Main Entry Point
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiTestQualityScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: GeminiTestQualityViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Update TopBar when tab changes / Tab 切换时更新 TopBar
    LaunchedEffect(state.selectedTab) {
        onUpdateTopBar(TopBarConfig(
            title = "Gemini 测试质量中心",
            actions = listOf(
                TopBarAction(
                    icon = Icons.Default.Refresh,
                    contentDescription = "刷新",
                    onClick = { viewModel.sendIntent(GeminiTestQualityIntent.RefreshAll) }
                )
            )
        ))
    }

    // Collect effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is GeminiTestQualityEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is GeminiTestQualityEffect.ExportReport -> {
                    snackbarHostState.showSnackbar("报告已导出: ${effect.filePath}")
                }
                is GeminiTestQualityEffect.NavigateToTestFile -> {
                    snackbarHostState.showSnackbar("打开: ${effect.filePath}")
                }
                is GeminiTestQualityEffect.RunCIValidation -> {
                    snackbarHostState.showSnackbar("CI 验证已触发")
                }
                is GeminiTestQualityEffect.GenerationCompleted -> {
                    snackbarHostState.showSnackbar(
                        "生成完成: ${effect.testFileName} (${effect.testCaseCount} 测试用例)"
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Gemini 测试质量中心",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            state.selectedTab.titleCn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(GeminiTestQualityIntent.RefreshAll) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row / Tab 导航栏
            ScrollableTabRow(
                selectedTabIndex = TestQualityTab.entries.indexOf(state.selectedTab),
                containerColor = DarkCard,
                contentColor = Color.White,
                edgePadding = 8.dp
            ) {
                TestQualityTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.sendIntent(GeminiTestQualityIntent.SelectTab(tab)) },
                        text = {
                            Text(
                                tab.titleCn,
                                color = if (state.selectedTab == tab) GeminiPurple else Color.White
                            )
                        }
                    )
                }
            }

            // Tab Content / Tab 内容区
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                when (state.selectedTab) {
                    TestQualityTab.Dashboard -> DashboardTab(state, viewModel)
                    TestQualityTab.CustomGenerator -> CustomGeneratorTab(state, viewModel)
                    TestQualityTab.SpecEngine -> SpecEngineTab(state, viewModel)
                    TestQualityTab.Traceability -> TraceabilityTab(state, viewModel)
                    TestQualityTab.BlindSpot -> BlindSpotTab(state, viewModel)
                    TestQualityTab.CIIntegration -> CIIntegrationTab(state, viewModel)
                }
            }
        }
    }
}

// ================================================================
// Tab 1: Dashboard / 质量仪表盘
// ================================================================

@Composable
private fun DashboardTab(
    state: GeminiTestQualityState,
    viewModel: GeminiTestQualityViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Quality Score Card / 质量评分卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "质量评分 / Quality Score",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RadarChartView(
                            scores = state.qualityScore,
                            modifier = Modifier.size(120.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val animatedScore by animateFloatAsState(
                                targetValue = state.averageQuality.toFloat(),
                                animationSpec = tween(500),
                                label = "score"
                            )
                            Text(
                                "${animatedScore.toInt()}",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = scoreColor(state.averageQuality)
                            )
                            Text(
                                "综合评分 / Overall",
                                fontSize = 12.sp,
                                color = DarkMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MetricBadge("测试", state.totalTests, GeminiBlue)
                                MetricBadge("用例", state.totalTestCases, SuccessGreen)
                                MetricBadge("风险", state.totalMissingRisks, DangerRed)
                            }
                        }
                    }
                }
            }
        }

        // Coverage Heatmap / 覆盖率热力图
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "覆盖率热力图 / Coverage Heatmap",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (state.coverageHeatmap.isEmpty()) {
                        Text("暂无数据 / No data", color = DarkMuted, fontSize = 12.sp)
                    } else {
                        state.coverageHeatmap.forEach { (_, coverage) ->
                            HeatmapRow(coverage = coverage)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // Generated Tests List Header
        item {
            Text(
                "Gemini 生成测试 / Generated Tests",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
        }

        if (state.generatedTests.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.AutoAwesome,
                    message = "Run Gemini Test Generation first",
                    subMessage = "使用框架定制生成器创建测试"
                )
            }
        } else {
            items(state.generatedTests) { test ->
                TestFileCard(
                    test = test,
                    onEvaluate = {
                        viewModel.sendIntent(GeminiTestQualityIntent.EvaluateQuality(test.testFileName))
                    }
                )
            }
        }
    }
}

@Composable
private fun MetricBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$count", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = DarkMuted)
    }
}

@Composable
private fun HeatmapRow(coverage: BranchCoverage) {
    val color = when (coverage.level) {
        CoverageLevel.High -> SuccessGreen
        CoverageLevel.Medium -> WarningYellow
        CoverageLevel.Low -> WarningOrange
        CoverageLevel.Critical -> DangerRed
    }
    val animatedProgress by animateFloatAsState(
        targetValue = coverage.coverage,
        animationSpec = tween(500),
        label = "coverage"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${coverage.fileName}:${coverage.lineNumber}",
                fontSize = 11.sp,
                color = DarkMuted,
                fontFamily = FontFamily.Monospace
            )
            Text(
                "${coverage.branchName} (${(coverage.coverage * 100).toInt()}%)",
                fontSize = 11.sp,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF404040)
        )
    }
}

@Composable
private fun TestFileCard(
    test: GeneratedTestFile,
    onEvaluate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    test.testFileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    test.sourceFileName,
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(test.framework.displayName, fontSize = 10.sp, color = GeminiPurple)
                    Text("${test.testCaseCount} cases", fontSize = 10.sp, color = SuccessGreen)
                    Text(
                        "${test.missingRiskCount} risks",
                        fontSize = 10.sp,
                        color = if (test.missingRiskCount > 3) DangerRed else DarkMuted
                    )
                }
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                CircularProgressIndicator(
                    progress = { test.qualityScore / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = scoreColor(test.qualityScore),
                    trackColor = Color(0xFF404040),
                    strokeWidth = 4.dp
                )
                Text(
                    "${test.qualityScore}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            IconButton(onClick = onEvaluate) {
                Icon(Icons.Default.Info, contentDescription = "Evaluate", tint = GeminiBlue)
            }
        }
    }
}

// ================================================================
// Tab 2: Custom Generator / 框架定制生成器
// ================================================================

@Composable
private fun CustomGeneratorTab(
    state: GeminiTestQualityState,
    viewModel: GeminiTestQualityViewModel
) {
    var selectedFramework by remember { mutableStateOf(state.selectedFramework) }
    var sourceFileName by remember { mutableStateOf("") }
    var boundaryEnabled by remember { mutableStateOf(state.coverageStrategy.enableBoundaryValue) }
    var equivalenceEnabled by remember { mutableStateOf(state.coverageStrategy.enableEquivalenceClass) }
    var pathEnabled by remember { mutableStateOf(state.coverageStrategy.enablePathCoverage) }
    var exceptionEnabled by remember { mutableStateOf(state.coverageStrategy.enableExceptionPath) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Framework Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("测试框架 / Test Framework", color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TestFramework.entries.forEach { fw ->
                            FilterChip(
                                selected = selectedFramework == fw,
                                onClick = { selectedFramework = fw },
                                label = { Text(fw.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GeminiPurple,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Source File Input
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("源文件 / Source File", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sourceFileName,
                        onValueChange = { sourceFileName = it },
                        label = { Text("输入源文件路径，如 UserRepository.kt") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiPurple,
                            unfocusedBorderColor = Color(0xFF505050),
                            focusedLabelColor = GeminiPurple,
                            unfocusedLabelColor = DarkMuted,
                            cursorColor = GeminiPurple,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }
        }

        // Coverage Strategy
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("覆盖策略 / Coverage Strategy", color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    StrategySwitch("边界值测试 / Boundary Value", boundaryEnabled) { boundaryEnabled = it }
                    StrategySwitch("等价类划分 / Equivalence Class", equivalenceEnabled) { equivalenceEnabled = it }
                    StrategySwitch("路径覆盖 / Path Coverage", pathEnabled) { pathEnabled = it }
                    StrategySwitch("异常路径 / Exception Path", exceptionEnabled) { exceptionEnabled = it }
                }
            }
        }

        // Generate Button
        item {
            Button(
                onClick = {
                    if (sourceFileName.isNotBlank()) {
                        viewModel.sendIntent(GeminiTestQualityIntent.UpdateCoverageStrategy(
                            CoverageStrategy(boundaryEnabled, equivalenceEnabled, pathEnabled, exceptionEnabled)
                        ))
                        viewModel.sendIntent(GeminiTestQualityIntent.GenerateTests(sourceFileName, selectedFramework))
                    }
                },
                enabled = sourceFileName.isNotBlank() && !state.isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GeminiPurple,
                    disabledContainerColor = Color(0xFF505050)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(
                        progress = { state.generationProgress },
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${(state.generationProgress * 100).toInt()}% — 生成中…")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini 生成测试 / Generate with Gemini")
                }
            }
        }
    }
}

@Composable
private fun StrategySwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GeminiPurple,
                uncheckedThumbColor = DarkMuted,
                uncheckedTrackColor = Color(0xFF404040)
            )
        )
    }
}

// ================================================================
// Tab 3: Spec Engine / 规范引擎
// ================================================================

@Composable
private fun SpecEngineTab(
    state: GeminiTestQualityState,
    viewModel: GeminiTestQualityViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "规范模板库 / Spec Templates",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                TextButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = GeminiPurple)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("新建模板", color = GeminiPurple)
                }
            }
        }

        items(state.specTemplates) { template ->
            SpecTemplateCard(
                template = template,
                isActive = template.id == state.activeSpec?.id,
                onActivate = { viewModel.sendIntent(GeminiTestQualityIntent.ActivateSpec(template.id)) },
                onDelete = { viewModel.sendIntent(GeminiTestQualityIntent.DeleteSpecTemplate(template.id)) }
            )
        }

        if (state.specTemplates.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Security,
                    message = "No spec templates yet",
                    subMessage = "创建规范模板让 Gemini 按规范生成测试"
                )
            }
        }
    }

    if (showAddDialog) {
        AddSpecTemplateDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, desc ->
                viewModel.sendIntent(GeminiTestQualityIntent.SaveSpecTemplate(
                    TestSpecTemplate(
                        id = "spec-${System.currentTimeMillis()}",
                        name = name,
                        description = desc,
                        isActive = false
                    )
                ))
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SpecTemplateCard(
    template: TestSpecTemplate,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (isActive) GeminiPurple else Color(0xFF404040)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        template.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkMuted
                    )
                }
                if (isActive) {
                    Badge(containerColor = GeminiPurple) {
                        Text("Active", modifier = Modifier.padding(horizontal = 4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isActive) {
                    FilterChip(
                        selected = false,
                        onClick = onActivate,
                        label = { Text("激活", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SuccessGreen
                        )
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddSpecTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, desc: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建规范模板 / New Spec Template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("模板名称") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiPurple,
                        unfocusedBorderColor = Color(0xFF505050),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiPurple,
                        unfocusedBorderColor = Color(0xFF505050),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, desc) }, enabled = name.isNotBlank()) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        containerColor = DarkCard,
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

// ================================================================
// Tab 4: Traceability / 可追溯性追踪器
// ================================================================

@Composable
private fun TraceabilityTab(
    state: GeminiTestQualityState,
    viewModel: GeminiTestQualityViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                "源码变更 → 测试影响 / Source Changes → Test Impact",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
        }

        if (state.traceabilityRecords.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.History,
                    message = "No traceability records yet",
                    subMessage = "源码变更记录将显示在此处"
                )
            }
        } else {
            items(state.traceabilityRecords) { record ->
                TraceabilityCard(record = record)
            }
        }
    }
}

@Composable
private fun TraceabilityCard(record: TraceabilityRecord) {
    val changeColor = when (record.changeType) {
        "ADD" -> SuccessGreen
        "MODIFY" -> WarningYellow
        "DELETE" -> DangerRed
        else -> DarkMuted
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    record.sourceFileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Badge(containerColor = changeColor) {
                    Text(record.changeType, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("变更时间: ${record.changedAt}", fontSize = 12.sp, color = DarkMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "影响测试: ${record.impactedTests.joinToString(", ")}",
                fontSize = 12.sp,
                color = GeminiBlue
            )
            if (record.regressionNeeded) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("需要回归测试", fontSize = 12.sp, color = WarningOrange)
                }
            }
        }
    }
}

// ================================================================
// Tab 5: Blind Spot Analyzer / 盲区分析器
// ================================================================

@Composable
private fun BlindSpotTab(
    state: GeminiTestQualityState,
    viewModel: GeminiTestQualityViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "测试盲区 / Blind Spots",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                Button(
                    onClick = { viewModel.sendIntent(GeminiTestQualityIntent.AnalyzeBlindSpots) },
                    enabled = !state.isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (state.isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (state.isGenerating) "分析中…" else "开始分析")
                }
            }
        }

        if (state.blindSpots.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.CheckCircle,
                    message = "All branches covered! 🎉",
                    subMessage = "或点击「开始分析」检测盲区"
                )
            }
        } else {
            items(state.blindSpots) { spot ->
                BlindSpotCard(spot = spot)
            }
        }
    }
}

@Composable
private fun BlindSpotCard(spot: BlindSpot) {
    val severityColor = Color(spot.severity.colorHex)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, severityColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
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
                        "${spot.fileName}:${spot.lineNumber}",
                        fontSize = 12.sp,
                        color = DarkMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        spot.methodName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
                Badge(containerColor = severityColor) {
                    Text(spot.severity.displayName, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                spot.description,
                style = MaterialTheme.typography.bodySmall,
                color = DarkMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("建议测试:", fontSize = 12.sp, color = GeminiBlue)
            spot.suggestedTestcases.forEach { tc ->
                Text(
                    "• $tc",
                    fontSize = 11.sp,
                    color = DarkMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ================================================================
// Tab 6: CI Integration / CI/CD 集成
// ================================================================

@Composable
private fun CIIntegrationTab(
    state: GeminiTestQualityState,
    viewModel: GeminiTestQualityViewModel
) {
    var selectedProvider by remember { mutableStateOf(state.ciConfig.provider) }
    var autoRegen by remember { mutableStateOf(state.ciConfig.autoRegenerateOnFailure) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // CI Provider Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CI Provider / CI 提供商", color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CIProvider.entries.forEach { provider ->
                            FilterChip(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider },
                                label = { Text(provider.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GeminiPurple,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Auto-Regenerate Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "失败时自动重新生成 / Auto-Regenerate on Failure",
                            color = Color.White
                        )
                        Text(
                            "测试失败时自动触发 Gemini 重新生成测试",
                            fontSize = 12.sp,
                            color = DarkMuted
                        )
                    }
                    Switch(
                        checked = autoRegen,
                        onCheckedChange = { autoRegen = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF404040)
                        )
                    )
                }
            }
        }

        // Generate Config Button
        item {
            Button(
                onClick = {
                    viewModel.sendIntent(GeminiTestQualityIntent.GenerateCIConfig(selectedProvider))
                },
                enabled = !state.isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Code, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("生成 ${selectedProvider.displayName} 配置")
            }
        }

        // YAML Output
        if (state.ciConfig.yamlContent.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("YAML 配置预览 / YAML Preview", color = Color.White)
                            IconButton(onClick = { /* Copy to clipboard */ }) {
                                Icon(Icons.Default.Share, contentDescription = "Copy", tint = GeminiPurple)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF0D0D0D),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                state.ciConfig.yamlContent,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = SuccessGreen,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Shared UI Components / 共享 UI 组件
// ================================================================

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    message: String,
    subMessage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = DarkSubtle,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = DarkMuted,
                textAlign = TextAlign.Center
            )
            Text(
                subMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF606060),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Radar chart for quality score visualization
@Composable
private fun RadarChartView(
    scores: QualityScore,
    modifier: Modifier = Modifier
) {
    val dimensions = listOf(
        "分支" to scores.branchCoverage,
        "条件" to scores.conditionCoverage,
        "路径" to scores.pathCoverage,
        "边界" to scores.boundaryCoverage,
        "异常" to scores.exceptionHandling
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = minOf(size.width, size.height) / 2 * 0.85f
        val angleStep = 360f / dimensions.size

        // Draw grid circles
        listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { ratio ->
            drawCircle(
                color = Color(0xFF404040),
                radius = radius * ratio,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw axis lines
        dimensions.forEachIndexed { index, _ ->
            val angle = Math.toRadians((angleStep * index - 90).toDouble())
            val endX = center.x + radius * cos(angle).toFloat()
            val endY = center.y + radius * sin(angle).toFloat()
            drawLine(
                color = Color(0xFF404040),
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw data polygon
        val pathPoints = dimensions.mapIndexed { index, (_, value) ->
            val angle = Math.toRadians((angleStep * index - 90).toDouble())
            Offset(
                center.x + radius * value * cos(angle).toFloat(),
                center.y + radius * value * sin(angle).toFloat()
            )
        }

        val fillPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(pathPoints.first().x, pathPoints.first().y)
            pathPoints.drop(1).forEach { point -> lineTo(point.x, point.y) }
            close()
        }
        drawPath(path = fillPath, color = GeminiPurple.copy(alpha = 0.3f))

        dimensions.forEachIndexed { index, _ ->
            val next = (index + 1) % dimensions.size
            drawLine(
                color = GeminiPurple,
                start = pathPoints[index],
                end = pathPoints[next],
                strokeWidth = 2.dp.toPx()
            )
        }

        pathPoints.forEach { point ->
            drawCircle(color = GeminiPurple, radius = 4.dp.toPx(), center = point)
        }
    }
}

private fun scoreColor(score: Int): Color = when {
    score >= 80 -> SuccessGreen
    score >= 60 -> WarningYellow
    score >= 40 -> WarningOrange
    else -> DangerRed
}
