package com.mvi.kenny.feature.playbillingmigration

// ================================================================
// PlayBillingMigrationScreen — Google Play Billing Library 9 迁移工具 UI
// ================================================================
// Jetpack Compose UI for PBL 9 Migration Toolkit.
//
// PRD-S: Google Play Billing Library 9 企业级迁移工具包
// Design: memory/agency/designs/PRD-S-Play-Billing-Library-9-Migration-Toolkit.md
//
// Tab Structure:
//   Tab 0: Dashboard — Overview of all scanned projects
//   Tab 1: Scan Results — API call analysis and risk assessment
//   Tab 2: Migration Wizard — Step-by-step migration guide
//   Tab 3: CI/CD Config — CI/CD YAML generator
//   Tab 4: Compliance Report — Export compliance documentation
// ================================================================

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.playbillingmigration.PBLVersion
import com.mvi.kenny.feature.playbillingmigration.PlayBillingMigrationEffect
import com.mvi.kenny.feature.playbillingmigration.PlayBillingMigrationIntent
import com.mvi.kenny.feature.playbillingmigration.PlayBillingMigrationState
import com.mvi.kenny.feature.playbillingmigration.RiskLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Colors / 颜色定义
// ================================================================

/** Deep Purple 600 — 专业企业感 / Professional enterprise feel */
private val PrimaryColor = Color(0xFF7C4DFF)

/** Dark Surface — 开发者友好 / Developer-friendly dark theme */
private val DarkSurface = Color(0xFF1C1C1E)

/** Card Background */
private val CardBg = Color(0xFF2C2C2E)

/** Success Green */
private val SuccessGreen = Color(0xFF4CAF50)

// ================================================================
// Main Screen / 主界面
// ================================================================

/**
 * ============================================================
 * PlayBillingMigrationScreen — PBL 9 迁移工具主界面
 * ============================================================
 * 5-Tab 布局，涵盖扫描→迁移→CI/CD→报告全流程。
 *
 * @param viewModel PlayBillingMigrationViewModel
 * @param onNavigateBack 导航返回回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayBillingMigrationScreen(
    viewModel: PlayBillingMigrationViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Effect Collector / 副作用监听 ────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PlayBillingMigrationEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("PBL Migration YAML", effect.content)
                    clipboard.setPrimaryClip(clip)
                }
                is PlayBillingMigrationEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is PlayBillingMigrationEffect.ReportGenerated -> {
                    // Report generated, show confirmation
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PBL v9 迁移工具包",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(PlayBillingMigrationIntent.ResetAll) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "重置 / Reset",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = DarkSurface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Deadline countdown banner / 截止日期倒计时横幅
            DeadlineBanner(days = state.deadlineDays)

            // Tab Row / 标签页导航
            val tabTitles = listOf(
                "📊 仪表板" to "Dashboard",
                "🔍 扫描结果" to "Scan Results",
                "🛠️ 迁移向导" to "Migration",
                "⚙️ CI/CD" to "CI/CD Config",
                "📄 合规报告" to "Report"
            )

            ScrollableTabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = DarkSurface,
                contentColor = PrimaryColor,
                edgePadding = 8.dp
            ) {
                tabTitles.forEachIndexed { index, (cn, en) ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.sendIntent(PlayBillingMigrationIntent.SelectTab(index)) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = cn,
                                    fontSize = 12.sp,
                                    color = if (state.selectedTab == index) PrimaryColor else Color.Gray
                                )
                            }
                        }
                    )
                }
            }

            // Tab Content / 标签页内容
            AnimatedContent(
                targetState = state.selectedTab,
                label = "TabContent",
                modifier = Modifier.fillMaxSize()
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> DashboardTab(state, viewModel)
                    1 -> ScanResultsTab(state, viewModel)
                    2 -> MigrationWizardTab(state, viewModel)
                    3 -> CICDConfigTab(state, viewModel)
                    4 -> ComplianceReportTab(state, viewModel)
                }
            }
        }
    }
}

// ================================================================
// Deadline Banner / 截止日期倒计时
// ================================================================

/**
 * 截止日期倒计时横幅
 * @param days 剩余天数
 */
@Composable
private fun DeadlineBanner(days: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3D2E00)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PBL v7/v8 延期申请截止",
                    color = Color(0xFFFFB300),
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "$days 天",
                color = Color(0xFFFFB300),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// ================================================================
// Tab 0: Dashboard / 仪表板
// ================================================================

/**
 * 仪表板 Tab — 展示所有已扫描项目概览
 */
@Composable
private fun DashboardTab(
    state: PlayBillingMigrationState,
    viewModel: PlayBillingMigrationViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header / 页头
        Text(
            text = "已扫描项目",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Overview of scanned projects / 已扫描项目总览",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Scan button / 扫描按钮
        Button(
            onClick = { viewModel.sendIntent(PlayBillingMigrationIntent.ScanProject("")) },
            enabled = !state.isScanning,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .padding(end = 8.dp),
                    color = Color.White
                )
            }
            Text(
                text = if (state.isScanning) state.scanProgressText else "🔍 扫描项目 / Scan Projects",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scan progress / 扫描进度
        if (state.isScanning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = state.scanProgressText,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.scanProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PrimaryColor
                    )
                    Text(
                        text = "${(state.scanProgress * 100).toInt()}%",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Project cards / 项目卡片
        if (state.scannedProjects.isEmpty() && !state.isScanning) {
            EmptyStateCard(
                emoji = "📦",
                title = "暂无扫描结果",
                subtitle = "点击上方按钮扫描项目 / Tap the button above to scan"
            )
        } else {
            state.scannedProjects.forEach { project ->
                ProjectCard(
                    project = project,
                    onClick = { viewModel.sendIntent(PlayBillingMigrationIntent.SelectProject(project)) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Overall risk summary / 整体风险摘要
        if (state.scanResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            RiskSummaryCard(state)
        }
    }
}

/**
 * 项目卡片 / Project card for dashboard
 */
@Composable
private fun ProjectCard(
    project: ScannedProject,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
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
                    text = project.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PBL ${project.currentPBLVersion.version} — ${project.pendingMigrations} 项待迁移",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = "最后扫描: ${project.lastScanned}",
                    color = Color.DarkGray,
                    fontSize = 11.sp
                )
            }

            // Risk badge / 风险徽章
            Box(
                modifier = Modifier
                    .background(
                        project.riskLevel.color.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, project.riskLevel.color, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${project.riskLevel.emoji} ${project.riskLevel.displayName}",
                    color = project.riskLevel.color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 风险摘要卡片 / Risk summary card
 */
@Composable
private fun RiskSummaryCard(state: PlayBillingMigrationState) {
    val criticalCount = state.scanResults.count { it.riskLevel == RiskLevel.P0_CRITICAL }
    val highCount = state.scanResults.count { it.riskLevel == RiskLevel.P1_HIGH }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 风险摘要 / Risk Summary",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RiskCountItem(emoji = "🔴", count = criticalCount, label = "P0 严重")
                RiskCountItem(emoji = "🟠", count = highCount, label = "P1 高风险")
                RiskCountItem(
                    emoji = "🟡",
                    count = state.scanResults.size - criticalCount - highCount,
                    label = "P2 中风险"
                )
            }
        }
    }
}

@Composable
private fun RiskCountItem(emoji: String, count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 24.sp)
        Text(text = "$count", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
    }
}

// ================================================================
// Tab 1: Scan Results / 扫描结果
// ================================================================

/**
 * 扫描结果 Tab — API 调用点分析和风险评估
 */
@Composable
private fun ScanResultsTab(
    state: PlayBillingMigrationState,
    viewModel: PlayBillingMigrationViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // PBL Version & Risk overview / PBL 版本和风险总览
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VersionCard(
                version = state.detectedPBLVersion,
                modifier = Modifier.weight(1f)
            )
            RiskCard(
                risk = state.overallRiskLevel,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section header / 分节标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔍 API 调用点 / API Call Points",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "${state.scanResults.size} 项",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.scanResults.isEmpty()) {
            EmptyStateCard(
                emoji = "🔍",
                title = "暂无扫描结果",
                subtitle = "请先在仪表板扫描项目 / Scan projects from Dashboard first"
            )
        } else {
            state.scanResults.forEach { result ->
                ApiCallCard(result)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun VersionCard(version: PBLVersion, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📦 PBL 版本", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = version.version,
                color = if (version == PBLVersion.V9) SuccessGreen else Color(0xFFFFB300),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = version.displayName, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun RiskCard(risk: RiskLevel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "⚠️ 风险等级", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = risk.emoji, fontSize = 24.sp)
            Text(
                text = risk.displayName,
                color = risk.color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * API 调用卡片 / API call result card
 */
@Composable
private fun ApiCallCard(result: ApiCallResult) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.description,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${result.filePath}:${result.lineNumber}",
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Risk badge / 风险徽章
                Box(
                    modifier = Modifier
                        .background(
                            result.riskLevel.color.copy(alpha = 0.2f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = result.riskLevel.emoji,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // API Type chip / API 类型标签
            Box(
                modifier = Modifier
                    .background(
                        PrimaryColor.copy(alpha = 0.2f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = result.apiType,
                    color = PrimaryColor,
                    fontSize = 11.sp
                )
            }

            // Expanded: code snippets / 展开：代码片段
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "❌ 问题代码 / Problematic Code",
                    color = Color(0xFFF85149),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                CodeSnippet(result.codeSnippet, Color(0xFFF85149))

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "✅ 修复后代码 / Fixed Code",
                    color = SuccessGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                CodeSnippet(result.fixSnippet, SuccessGreen)

                // Copy buttons / 复制按钮
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Fix Code", result.fixSnippet))
                            Toast.makeText(
                                context,
                                "已复制修复代码 / Fix copied",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "复制修复 / Copy Fix", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeSnippet(code: String, highlightColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            .border(1.dp, highlightColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            color = highlightColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ================================================================
// Tab 2: Migration Wizard / 迁移向导
// ================================================================

/**
 * 迁移向导 Tab — 分步骤引导完成 PBL 9 迁移
 */
@Composable
private fun MigrationWizardTab(
    state: PlayBillingMigrationState,
    viewModel: PlayBillingMigrationViewModel
) {
    if (state.migrationSteps.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🛠️", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "请先扫描项目 / Scan projects first",
                color = Color.Gray
            )
        }
        return
    }

    val currentStep = state.migrationSteps.getOrNull(state.currentStepIndex)
    val currentStepIdx = state.currentStepIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Progress indicator / 进度指示器
        Text(
            text = "🛠️ 迁移向导 / Migration Wizard",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Step ${currentStepIdx + 1} of ${state.migrationSteps.size} — ${currentStep?.title ?: ""}",
            color = Color.Gray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Step progress bar / 步骤进度条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            state.migrationSteps.forEachIndexed { index, step ->
                val isActive = index == currentStepIdx
                val isCompleted = step.status == MigrationStepStatus.COMPLETED

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            when {
                                isCompleted -> SuccessGreen
                                isActive -> PrimaryColor
                                else -> Color.DarkGray
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isCompleted -> Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        else -> Text(
                            text = "${step.stepNumber}",
                            color = if (isActive) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                if (index < state.migrationSteps.size - 1) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .background(
                                if (isCompleted) SuccessGreen else Color.DarkGray,
                                RoundedCornerShape(1.dp)
                            )
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Current step card / 当前步骤卡片
        currentStep?.let { step ->
            MigrationStepCard(step = step, viewModel = viewModel)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation buttons / 导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.sendIntent(PlayBillingMigrationIntent.PrevMigrationStep) },
                enabled = currentStepIdx > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "⬅️ 上一步 / Prev")
            }

            Button(
                onClick = {
                    currentStep?.let { step ->
                        when (step.status) {
                            MigrationStepStatus.PENDING -> {
                                viewModel.sendIntent(PlayBillingMigrationIntent.StartMigrationStep(step.id))
                            }
                            MigrationStepStatus.IN_PROGRESS -> {
                                viewModel.sendIntent(PlayBillingMigrationIntent.CompleteMigrationStep(step.id))
                                if (currentStepIdx < state.migrationSteps.size - 1) {
                                    viewModel.sendIntent(PlayBillingMigrationIntent.NextMigrationStep)
                                }
                            }
                            else -> {
                                if (currentStepIdx < state.migrationSteps.size - 1) {
                                    viewModel.sendIntent(PlayBillingMigrationIntent.NextMigrationStep)
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (currentStep?.status) {
                        MigrationStepStatus.IN_PROGRESS -> SuccessGreen
                        else -> PrimaryColor
                    }
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (currentStep?.status) {
                        MigrationStepStatus.PENDING -> "▶️ 开始 / Start"
                        MigrationStepStatus.IN_PROGRESS -> "✅ 完成 / Complete"
                        else -> "下一步 / Next ➡️"
                    },
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 迁移步骤卡片 / Migration step card
 */
@Composable
private fun MigrationStepCard(
    step: MigrationStep,
    viewModel: PlayBillingMigrationViewModel
) {
    var showCode by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Step ${step.stepNumber}: ${step.title}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = step.titleEn,
                        color = PrimaryColor,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            step.status.color.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${step.status.emoji} ${step.status.displayName}",
                        color = step.status.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description / 描述
            Text(
                text = step.description,
                color = Color.Gray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Affected files / 影响文件数
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📁 影响 ${step.affectedFiles} 个文件",
                    color = Color.DarkGray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .background(
                            step.riskLevel.color.copy(alpha = 0.2f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${step.riskLevel.emoji} ${step.riskLevel.displayName}",
                        color = step.riskLevel.color,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toggle code view / 切换代码视图
            OutlinedButton(
                onClick = { showCode = !showCode },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (showCode) Icons.Default.Refresh else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (showCode) "隐藏代码 / Hide Code" else "查看迁移代码 / View Migration Code",
                    fontSize = 12.sp
                )
            }

            // Code comparison / 代码对比
            if (showCode && step.beforeCode.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "❌ 迁移前 / Before",
                    color = Color(0xFFF85149),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                CodeSnippet(step.beforeCode, Color(0xFFF85149))

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "✅ 迁移后 / After",
                    color = SuccessGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                CodeSnippet(step.afterCode, SuccessGreen)
            }
        }
    }
}

// ================================================================
// Tab 3: CI/CD Config / CI/CD 配置
// ================================================================

/**
 * CI/CD 配置 Tab — 生成 GitHub Actions / GitLab CI YAML 配置
 */
@Composable
private fun CICDConfigTab(
    state: PlayBillingMigrationState,
    viewModel: PlayBillingMigrationViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "⚙️ CI/CD 配置生成器",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "CI/CD Configuration Generator / CI/CD 配置生成器",
            color = Color.Gray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Provider selector / 提供商选择
        Text(
            text = "选择 CI/CD 提供商 / Select Provider",
            color = Color.Gray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            CICDProvider.entries.forEachIndexed { index, provider ->
                SegmentedButton(
                    selected = state.selectedProvider == provider,
                    onClick = {
                        viewModel.sendIntent(PlayBillingMigrationIntent.SelectCICDProvider(provider))
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = CICDProvider.entries.size
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "${provider.icon} ${provider.displayName}",
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Repository URL / 仓库 URL
        OutlinedTextField(
            value = state.repositoryUrl,
            onValueChange = { viewModel.sendIntent(PlayBillingMigrationIntent.UpdateRepositoryUrl(it)) },
            label = { Text("仓库 URL / Repository URL") },
            placeholder = { Text("https://github.com/org/repo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Branch name / 分支名
        OutlinedTextField(
            value = state.branchName,
            onValueChange = { viewModel.sendIntent(PlayBillingMigrationIntent.UpdateBranchName(it)) },
            label = { Text("分支名 / Branch Name") },
            placeholder = { Text("main") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Generate button / 生成按钮
        Button(
            onClick = { viewModel.sendIntent(PlayBillingMigrationIntent.GenerateCICDConfig) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "⚡ 生成 CI/CD 配置 / Generate CI/CD Config", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // YAML Config display / YAML 配置展示
        if (state.cicdYamlConfig.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📄 YAML 配置 / YAML Config",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                viewModel.sendIntent(
                                    PlayBillingMigrationIntent.CopyCICDYaml(state.cicdYamlConfig)
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy YAML",
                                tint = PrimaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color(0xFF161B22), RoundedCornerShape(8.dp))
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = state.cicdYamlConfig,
                            color = Color(0xFF7EE787),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// ================================================================
// Tab 4: Compliance Report / 合规报告
// ================================================================

/**
 * 合规报告 Tab — 导出 PBL 9 合规状态报告
 */
@Composable
private fun ComplianceReportTab(
    state: PlayBillingMigrationState,
    viewModel: PlayBillingMigrationViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "📄 合规报告",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Compliance Report / 合规报告导出",
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Report preview / 报告预览
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📋 报告摘要 / Report Summary",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val totalItems = state.scanResults.size
                val completedSteps = state.migrationSteps.count { it.status == MigrationStepStatus.COMPLETED }
                val totalSteps = state.migrationSteps.size

                ReportRow(label = "检测到 API 调用点 / API Call Points", value = "$totalItems 项")
                ReportRow(label = "PBL 当前版本 / Current PBL Version", value = state.detectedPBLVersion.version)
                ReportRow(label = "风险等级 / Risk Level", value = state.overallRiskLevel.displayName)
                ReportRow(label = "迁移进度 / Migration Progress", value = "$completedSteps / $totalSteps 步骤")
                ReportRow(label = "延期截止 / Deadline", value = "${state.deadlineDays} 天后（2026-11-01）")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Format selector / 格式选择
        Text(
            text = "导出格式 / Export Format",
            color = Color.Gray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportFormat.entries.forEach { format ->
                val isSelected = state.selectedReportFormat == format
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.sendIntent(PlayBillingMigrationIntent.SelectReportFormat(format))
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PrimaryColor.copy(alpha = 0.2f) else CardBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (format) {
                                ReportFormat.PDF -> "📕 PDF"
                                ReportFormat.JSON -> "📋 JSON"
                                ReportFormat.MARKDOWN -> "📝 MD"
                            },
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = format.displayName,
                            color = if (isSelected) PrimaryColor else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Export button / 导出按钮
        Button(
            onClick = { viewModel.sendIntent(PlayBillingMigrationIntent.ExportReport) },
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "📥 导出 ${state.selectedReportFormat.displayName} / Export Report",
                color = Color.White
            )
        }

        // Report URI indicator / 报告生成指示
        if (state.reportUri != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "报告已生成 / Report generated",
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 13.sp)
        Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ================================================================
// Empty State / 空状态
// ================================================================

/**
 * 空状态卡片 / Empty state card
 */
@Composable
private fun EmptyStateCard(emoji: String, title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
