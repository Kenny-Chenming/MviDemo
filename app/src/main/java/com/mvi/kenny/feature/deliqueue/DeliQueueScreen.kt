package com.mvi.kenny.feature.deliqueue

// ================================================================
// DeliQueueScreen — Android 17 DeliQueue 迁移检测工具包 Screen
// ================================================================
// CLI-style Screen for Android 17 lock-free MessageQueue migration toolkit.
//
// PRD-198: Android 17 DeliQueue（Lock-free MessageQueue）迁移检测工具包
// Design Reference: memory/agency/designs/PRD-198-Android-17-DeliQueue-迁移检测工具包.md
//
// This screen provides a terminal-like interface with 3 tabs:
//   Tab 1: Scanner — Project scan entry + progress + results summary
//   Tab 2: Impact Analysis — File-level and library-level risk display
//   Tab 3: Migration Wizard — Step-by-step fix guidance with diff view
//
// Visual Style:
//   - Dark terminal theme (#0D1117 background, #161B22 cards)
//   - Color-coded risk levels: P0=red, P1=amber, P2=green
//   - Monospace font for code/logs
//   - Tab-based navigation
// ================================================================

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Color Palette — 颜色规范（Design Reference）
// ================================================================
private object DeliQueueColors {
    val Background = Color(0xFF0D1117)          // #0D1117 - 主背景
    val CardBackground = Color(0xFF161B22)        // #161B22 - 卡片背景
    val CardBorder = Color(0xFF30363D)            // #30363D - 卡片边框
    val AccentBlue = Color(0xFF58A6FF)           // #58A6FF - 强调蓝
    val RiskP0 = Color(0xFFF85149)               // #F85149 - P0 风险红
    val RiskP1 = Color(0xFFD29922)               // #D29922 - P1 风险黄
    val RiskP2 = Color(0xFF3FB950)               // #3FB950 - P2 风险绿
    val TextPrimary = Color(0xFFE6EDF3)           // #E6EDF3 - 主文字
    val TextSecondary = Color(0xFF8B949E)        // #8B949E - 次文字
    val LogTimestamp = Color(0xFF79C0FF)         // #79C0FF - 日志时间戳
    val TabIndicator = Color(0xFF58A6FF)         // Tab 指示器
}

// ================================================================
// DeliQueueScreen — Main Composable
// ================================================================
/**
 * ============================================================
 * DeliQueueScreen — DeliQueue 迁移检测工具包主界面
 * ============================================================
 * Entry point for the DeliQueue migration toolkit UI.
 * Provides 3-tab navigation: Scanner | Impact Analysis | Migration Wizard.
 *
 * @param viewModel DeliQueueViewModel instance
 */
@Composable
fun DeliQueueScreen(
    viewModel: DeliQueueViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Collect effects for one-time events (toast, clipboard, etc.)
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DeliQueueEffect.ShowToast -> {
                    // Toast handled by the consuming component if needed
                }
                is DeliQueueEffect.CopiedToClipboard -> {
                    // Clipboard handled automatically
                }
                is DeliQueueEffect.ScanCompleted -> {
                    // Scan completed, state already updated
                }
                is DeliQueueEffect.TerminalOutput -> {
                    // Terminal output, state already updated
                }
                is DeliQueueEffect.CIVerificationResult -> {
                    // CI result handled by state
                }
                is DeliQueueEffect.Error -> {
                    // Error handled by state
                }
            }
        }
    }

    // Main UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeliQueueColors.Background)
    ) {
        // Tab Row
        DeliQueueTabRow(
            selectedTabIndex = state.currentTab,
            onTabSelected = { viewModel.sendIntent(DeliQueueIntent.SwitchTab(it)) }
        )

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (state.currentTab) {
                0 -> ScannerTab(
                    state = state,
                    onStartScan = { viewModel.sendIntent(DeliQueueIntent.StartScan) },
                    onCancelScan = { viewModel.sendIntent(DeliQueueIntent.CancelScan) },
                    onClearResults = { viewModel.sendIntent(DeliQueueIntent.ClearResults) },
                    onNavigateToImpact = { viewModel.sendIntent(DeliQueueIntent.SwitchTab(1)) }
                )
                1 -> ImpactTab(
                    state = state,
                    onSelectFinding = { viewModel.sendIntent(DeliQueueIntent.SelectFinding(it)) },
                    onSwitchDimension = { viewModel.sendIntent(DeliQueueIntent.SetImpactDimension(it)) },
                    onNavigateToMigration = { viewModel.sendIntent(DeliQueueIntent.SwitchTab(2)) }
                )
                2 -> MigrationTab(
                    state = state,
                    onNextStep = { viewModel.sendIntent(DeliQueueIntent.NextStep) },
                    onPrevStep = { viewModel.sendIntent(DeliQueueIntent.PreviousStep) },
                    onApplyFix = { viewModel.sendIntent(DeliQueueIntent.ApplyFix(it)) },
                    onRunCI = { viewModel.sendIntent(DeliQueueIntent.RunCIVerification) },
                    onCopyToClipboard = { viewModel.sendIntent(DeliQueueIntent.CopyToClipboard(it)) },
                    onSelectFinding = { viewModel.sendIntent(DeliQueueIntent.SelectFinding(it)) }
                )
            }
        }
    }
}

// ================================================================
// Tab Row — Tab 导航栏
// ================================================================
/**
 * ============================================================
 * DeliQueueTabRow — Tab 导航栏
 * ============================================================
 */
@Composable
private fun DeliQueueTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        "🔍 扫描器" to Icons.Default.Search,
        "📊 影响分析" to Icons.Default.Analytics,
        "🔧 迁移向导" to Icons.Default.Build
    )

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = DeliQueueColors.CardBackground,
        contentColor = DeliQueueColors.TextPrimary,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = DeliQueueColors.TabIndicator
            )
        }
    ) {
        tabs.forEachIndexed { index, (title, icon) ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        fontFamily = FontFamily.Default,
                        color = if (selectedTabIndex == index) DeliQueueColors.AccentBlue else DeliQueueColors.TextSecondary
                    )
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (selectedTabIndex == index) DeliQueueColors.AccentBlue else DeliQueueColors.TextSecondary
                    )
                }
            )
        }
    }
}

// ================================================================
// Scanner Tab — 扫描器 Tab
// ================================================================
/**
 * ============================================================
 * ScannerTab — 扫描器 Tab
 * ============================================================
 * Project scan entry point with progress display and log output.
 */
@Composable
private fun ScannerTab(
    state: DeliQueueState,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit,
    onClearResults: () -> Unit,
    onNavigateToImpact: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Project path input
        TerminalCard(
            title = "📁 项目路径 / Project Path"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.projectPath.ifEmpty { "/Users/kenny/WorkSpace/AndroidStudioProjects/MyMviProject" },
                    onValueChange = { },
                    modifier = Modifier.weight(1f),
                    label = { Text("项目路径") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeliQueueColors.AccentBlue,
                        unfocusedBorderColor = DeliQueueColors.CardBorder,
                        focusedTextColor = DeliQueueColors.TextPrimary,
                        unfocusedTextColor = DeliQueueColors.TextPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onStartScan,
                    enabled = !state.scanState.isScanning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeliQueueColors.AccentBlue,
                        disabledContainerColor = DeliQueueColors.CardBorder
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (state.scanState.isScanning) "扫描中..." else "开始扫描")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress indicator (when scanning)
        if (state.scanState.isScanning) {
            TerminalCard(title = "📈 扫描进度 / Progress") {
                Column {
                    LinearProgressIndicator(
                        progress = { state.scanState.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = DeliQueueColors.AccentBlue,
                        trackColor = DeliQueueColors.CardBorder
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.scanState.phase.name,
                            color = DeliQueueColors.TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${state.scanState.progress}%",
                            color = DeliQueueColors.AccentBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onCancelScan,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DeliQueueColors.RiskP0
                        )
                    ) {
                        Text("取消扫描")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Log output
        TerminalCard(
            title = "📋 扫描日志 / Scan Logs",
            modifier = Modifier.weight(1f)
        ) {
            if (state.scanState.logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🔍",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击「开始扫描」检测项目中的 MessageQueue 反射用法",
                            color = DeliQueueColors.TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.scanState.logs) { log ->
                        LogLine(log = log)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results summary (when completed)
        if (state.scanState.phase == ScanPhase.COMPLETED) {
            TerminalCard(title = "📊 扫描结果摘要 / Results Summary") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RiskBadge(
                        label = "P0 崩溃",
                        count = state.scanState.p0Count,
                        color = DeliQueueColors.RiskP0
                    )
                    RiskBadge(
                        label = "P1 异常",
                        count = state.scanState.p1Count,
                        color = DeliQueueColors.RiskP1
                    )
                    RiskBadge(
                        label = "P2 潜在",
                        count = state.scanState.p2Count,
                        color = DeliQueueColors.RiskP2
                    )
                    RiskBadge(
                        label = "受影响库",
                        count = state.scanState.affectedLibraryCount,
                        color = DeliQueueColors.AccentBlue
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Risk description
                Text(
                    text = state.overallRiskDescription,
                    color = DeliQueueColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                if (state.scanState.findings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClearResults,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("清除结果")
                        }
                        Button(
                            onClick = onNavigateToImpact,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeliQueueColors.AccentBlue
                            )
                        ) {
                            Text("→ 查看影响分析")
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Impact Tab — 影响分析 Tab
// ================================================================
/**
 * ============================================================
 * ImpactTab — 影响分析 Tab
 * ============================================================
 * Displays findings grouped by file or library dimension.
 */
@Composable
private fun ImpactTab(
    state: DeliQueueState,
    onSelectFinding: (RiskFinding) -> Unit,
    onSwitchDimension: (ImpactDimension) -> Unit,
    onNavigateToMigration: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Dimension toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.impactDimension == ImpactDimension.FILE,
                onClick = { onSwitchDimension(ImpactDimension.FILE) },
                label = { Text("📄 按文件") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DeliQueueColors.AccentBlue,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = state.impactDimension == ImpactDimension.LIBRARY,
                onClick = { onSwitchDimension(ImpactDimension.LIBRARY) },
                label = { Text("📦 按第三方库") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DeliQueueColors.AccentBlue,
                    selectedLabelColor = Color.White
                )
            )
        }

        // Content
        if (state.scanState.findings.isEmpty() && state.scanState.libraryFindings.isEmpty()) {
            EmptyImpactState()
        } else {
            when (state.impactDimension) {
                ImpactDimension.FILE -> FileFindingsList(
                    findings = state.scanState.findings,
                    onSelectFinding = onSelectFinding,
                    modifier = Modifier.weight(1f)
                )
                ImpactDimension.LIBRARY -> LibraryFindingsList(
                    findings = state.scanState.libraryFindings,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigate to migration wizard
            if (state.scanState.findings.isNotEmpty()) {
                Button(
                    onClick = onNavigateToMigration,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeliQueueColors.AccentBlue
                    )
                ) {
                    Icon(Icons.Default.Build, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🔧 前往迁移向导")
                }
            }
        }
    }
}

@Composable
private fun FileFindingsList(
    findings: List<RiskFinding>,
    onSelectFinding: (RiskFinding) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(findings) { finding ->
            FindingCard(
                finding = finding,
                onClick = { onSelectFinding(finding) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LibraryFindingsList(
    findings: List<LibraryFinding>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(findings) { library ->
            LibraryCard(library = library)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EmptyImpactState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📊", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请先运行扫描以查看影响分析",
                color = DeliQueueColors.TextSecondary
            )
        }
    }
}

// ================================================================
// Migration Tab — 迁移向导 Tab
// ================================================================
/**
 * ============================================================
 * MigrationTab — 迁移向导 Tab
 * ============================================================
 * 4-step wizard: Identify → Evaluate → Fix → Verify
 */
@Composable
private fun MigrationTab(
    state: DeliQueueState,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onApplyFix: (RiskFinding) -> Unit,
    onRunCI: () -> Unit,
    onCopyToClipboard: (String) -> Unit,
    onSelectFinding: (RiskFinding) -> Unit
) {
    val wizard = state.migrationWizard

    Column(modifier = Modifier.fillMaxSize()) {
        // Step indicator
        StepIndicator(currentStep = wizard.currentStep)

        Spacer(modifier = Modifier.height(16.dp))

        // Step content
        Box(modifier = Modifier.weight(1f)) {
            when (wizard.currentStep) {
                MigrationStep.IDENTIFY -> IdentifyStep(
                    state = state,
                    onSelectFinding = onSelectFinding
                )
                MigrationStep.EVALUATE -> EvaluateStep(
                    finding = wizard.selectedFinding,
                    onCopyToClipboard = onCopyToClipboard
                )
                MigrationStep.FIX -> FixStep(
                    finding = wizard.selectedFinding,
                    diff = wizard.generatedDiff,
                    isApplying = wizard.isApplyingFix,
                    onApplyFix = onApplyFix,
                    onCopyToClipboard = onCopyToClipboard
                )
                MigrationStep.VERIFY -> VerifyStep(
                    state = state,
                    onRunCI = onRunCI
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPrevStep,
                enabled = !wizard.isFirstStep,
                modifier = Modifier.weight(1f)
            ) {
                Text("← 上一步")
            }
            Button(
                onClick = onNextStep,
                enabled = wizard.selectedFinding != null && !wizard.isLastStep,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeliQueueColors.AccentBlue
                )
            ) {
                Text(if (wizard.isLastStep) "完成" else "下一步 →")
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: MigrationStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MigrationStep.entries.forEach { step ->
            val isActive = step == currentStep
            val isCompleted = step.stepNumber < currentStep.stepNumber

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = when {
                                isActive -> DeliQueueColors.AccentBlue
                                isCompleted -> DeliQueueColors.RiskP2
                                else -> DeliQueueColors.CardBorder
                            },
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCompleted) "✓" else step.stepNumber.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.title,
                    color = if (isActive) DeliQueueColors.AccentBlue else DeliQueueColors.TextSecondary,
                    fontSize = 11.sp
                )
            }

            if (step != MigrationStep.VERIFY) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(2.dp)
                        .background(
                            if (isCompleted) DeliQueueColors.RiskP2 else DeliQueueColors.CardBorder
                        )
                )
            }
        }
    }
}

@Composable
private fun IdentifyStep(
    state: DeliQueueState,
    onSelectFinding: (RiskFinding) -> Unit
) {
    Column {
        Text(
            text = "📋 Step 1: 识别受影响的方法",
            style = MaterialTheme.typography.titleMedium,
            color = DeliQueueColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "选择一个风险项开始迁移向导",
            color = DeliQueueColors.TextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (state.scanState.findings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔍 请先在扫描器中运行扫描",
                    color = DeliQueueColors.TextSecondary
                )
            }
        } else {
            LazyColumn {
                items(state.scanState.findings) { finding ->
                    FindingCard(finding = finding, onClick = { onSelectFinding(finding) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun EvaluateStep(
    finding: RiskFinding?,
    onCopyToClipboard: (String) -> Unit
) {
    Column {
        Text(
            text = "📖 Step 2: 评估替代方案",
            style = MaterialTheme.typography.titleMedium,
            color = DeliQueueColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (finding == null) {
            Text("请先在 Step 1 选择一个风险项", color = DeliQueueColors.TextSecondary)
        } else {
            TerminalCard(title = "🔍 当前反射调用") {
                Text(
                    text = "${finding.filePath}:${finding.lineNumber}",
                    color = DeliQueueColors.TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = finding.codeSnippet,
                    color = DeliQueueColors.RiskP0,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TerminalCard(title = "✅ 推荐替代公开 API") {
                Text(
                    text = finding.alternativeApi,
                    color = DeliQueueColors.RiskP2,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = finding.target.descriptionZh,
                    color = DeliQueueColors.TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { onCopyToClipboard(finding.alternativeApi) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制")
                }
            }
        }
    }
}

@Composable
private fun FixStep(
    finding: RiskFinding?,
    diff: String,
    isApplying: Boolean,
    onApplyFix: (RiskFinding) -> Unit,
    onCopyToClipboard: (String) -> Unit
) {
    Column {
        Text(
            text = "🔧 Step 3: 修复代码",
            style = MaterialTheme.typography.titleMedium,
            color = DeliQueueColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (finding == null) {
            Text("请先选择风险项", color = DeliQueueColors.TextSecondary)
        } else {
            TerminalCard(title = "📝 代码 Diff") {
                Text(
                    text = diff.ifEmpty { "生成中..." },
                    color = DeliQueueColors.TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { onCopyToClipboard(diff) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制 Diff")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onApplyFix(finding) },
                enabled = !isApplying,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeliQueueColors.RiskP2
                )
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("应用修复中...")
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("应用修复到项目")
                }
            }
        }
    }
}

@Composable
private fun VerifyStep(
    state: DeliQueueState,
    onRunCI: () -> Unit
) {
    Column {
        Text(
            text = "✅ Step 4: 验证修复",
            style = MaterialTheme.typography.titleMedium,
            color = DeliQueueColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        TerminalCard(title = "🧪 CI 合规检测") {
            Text(
                text = "运行 CI 合规检测，验证所有 MessageQueue 反射调用是否已修复。",
                color = DeliQueueColors.TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRunCI,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeliQueueColors.AccentBlue
                )
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("运行 CI 验证")
            }
        }

        if (state.migrationWizard.ciVerificationPassed) {
            Spacer(modifier = Modifier.height(12.dp))
            TerminalCard(title = "🎉 验证结果") {
                Text(
                    text = "✅ CI 合规检测通过！所有 MessageQueue 反射用法已修复。",
                    color = DeliQueueColors.RiskP2,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (state.scanState.findings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            TerminalCard(title = "⚠️ 验证结果") {
                Text(
                    text = "❌ 仍有 ${state.scanState.findings.size} 个未修复的反射调用",
                    color = DeliQueueColors.RiskP0
                )
            }
        }
    }
}

// ================================================================
// Reusable UI Components — 可复用 UI 组件
// ================================================================

@Composable
private fun TerminalCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = DeliQueueColors.CardBorder,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = DeliQueueColors.CardBackground
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = DeliQueueColors.AccentBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun LogLine(log: ScanLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = "[${log.timestamp}]",
            color = DeliQueueColors.LogTimestamp,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = "[${log.level.prefix}]",
            color = log.level.color,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = log.message,
            color = DeliQueueColors.TextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun RiskBadge(
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = DeliQueueColors.TextSecondary,
            fontSize = 11.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FindingCard(
    finding: RiskFinding,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = when (finding.riskLevel) {
                    RiskLevel.P0 -> DeliQueueColors.RiskP0
                    RiskLevel.P1 -> DeliQueueColors.RiskP1
                    RiskLevel.P2 -> DeliQueueColors.RiskP2
                    RiskLevel.UNKNOWN -> DeliQueueColors.CardBorder
                },
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = DeliQueueColors.CardBackground
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = finding.filePath,
                    color = DeliQueueColors.TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                RiskLevelBadge(riskLevel = finding.riskLevel)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Line ${finding.lineNumber}",
                color = DeliQueueColors.TextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = finding.codeSnippet,
                color = DeliQueueColors.RiskP0,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "→ ${finding.alternativeApi}",
                color = DeliQueueColors.RiskP2,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun RiskLevelBadge(riskLevel: RiskLevel) {
    Surface(
        color = riskLevel.color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "${riskLevel.emoji} ${riskLevel.name}",
            color = riskLevel.color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun LibraryCard(library: LibraryFinding) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = DeliQueueColors.AccentBlue.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = DeliQueueColors.CardBackground
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = library.libraryName,
                        color = DeliQueueColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "v${library.version}",
                        color = DeliQueueColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
                RecommendationBadge(recommendation = library.recommendation)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = library.impactDescription,
                color = DeliQueueColors.TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "受影响类: ${library.affectedClasses.joinToString(", ")}",
                color = DeliQueueColors.AccentBlue,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun RecommendationBadge(recommendation: LibraryRecommendation) {
    val (color, emoji) = when (recommendation) {
        LibraryRecommendation.UPGRADE -> DeliQueueColors.AccentBlue to "⬆️"
        LibraryRecommendation.UPDATE -> DeliQueueColors.RiskP2 to "🔄"
        LibraryRecommendation.REMOVE -> DeliQueueColors.RiskP0 to "🗑️"
        LibraryRecommendation.MONITOR -> DeliQueueColors.RiskP1 to "👀"
        LibraryRecommendation.REPLACE -> DeliQueueColors.AccentBlue to "🔀"
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "$emoji ${recommendation.displayName}",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
