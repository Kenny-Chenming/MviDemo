package com.mvi.kenny.feature.backgroundaudiohardening

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * ============================================================
 * BackgroundAudioHardeningScreen — Android 17 Background Audio Hardening 合规工具
 * ============================================================
 * Main screen composable for the Background Audio Hardening compliance tool.
 *
 * Features:
 * - Overview dashboard with compliance score
 * - Background audio code scanner
 * - MediaSession template generator
 * - Foreground Service configuration
 * - CI compliance detection
 * - Fallback strategy templates
 *
 * @param viewModel ViewModel instance
 * @param onNavigateBack Navigation callback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundAudioHardeningScreen(
    viewModel: BackgroundAudioHardeningViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Effect collection / 副作用收集
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BackgroundAudioHardeningEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is BackgroundAudioHardeningEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Code", effect.text))
                }
                is BackgroundAudioHardeningEffect.OpenUrl -> {
                    // URL opening handled externally
                }
                is BackgroundAudioHardeningEffect.ReportExported -> {
                    Toast.makeText(context, "报告已导出: ${effect.path}", Toast.LENGTH_LONG).show()
                }
                is BackgroundAudioHardeningEffect.NavigateToFile -> {
                    // File navigation handled externally
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                // Top bar / 顶部栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔊 Background Audio Hardening",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.processIntent(BackgroundAudioHardeningIntent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }

                // Tab row / 标签页导航
                ScrollableTabRow(
                    selectedTabIndex = AudioHardeningTab.entries.indexOf(state.activeTab),
                    edgePadding = 8.dp,
                    divider = {}
                ) {
                    AudioHardeningTab.entries.forEach { tab ->
                        Tab(
                            selected = state.activeTab == tab,
                            onClick = { viewModel.processIntent(BackgroundAudioHardeningIntent.SwitchTab(tab)) },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab content / 标签页内容
            AnimatedContent(
                targetState = state.activeTab,
                transitionSpec = {
                    fadeIn(tween(200)) + slideInHorizontally { it / 4 } togetherWith
                            fadeOut(tween(200)) + slideOutHorizontally { -it / 4 }
                },
                label = "TabContent"
            ) { tab ->
                when (tab) {
                    AudioHardeningTab.Overview -> OverviewTab(state, viewModel)
                    AudioHardeningTab.Scanner -> ScannerTab(state, viewModel)
                    AudioHardeningTab.MediaSession -> MediaSessionTab(state, viewModel)
                    AudioHardeningTab.FGSConfig -> FGSConfigTab(state, viewModel)
                    AudioHardeningTab.CICompliance -> CIComplianceTab(state, viewModel)
                    AudioHardeningTab.FallbackStrategy -> FallbackStrategyTab(state, viewModel)
                }
            }
        }
    }
}

// ============================================================
// Overview Tab — 概览标签页
// ============================================================

@Composable
private fun OverviewTab(state: BackgroundAudioHardeningState, viewModel: BackgroundAudioHardeningViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Score card / 合规分数卡片
        ScoreCard(state)

        Spacer(modifier = Modifier.height(16.dp))

        // Issue summary / 问题摘要
        if (state.auditResult != null) {
            IssueSummaryCard(state.auditResult)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick actions / 快捷操作
        QuickActionsCard(state, viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Android 17 audio changes info / Android 17 音频变更说明
        AudioChangesInfoCard()
    }
}

@Composable
private fun ScoreCard(state: BackgroundAudioHardeningState) {
    val score = state.auditResult?.score ?: 0
    val scoreColor = when {
        score >= 80 -> Color(0xFF198754)
        score >= 60 -> Color(0xFFFD7E14)
        else -> Color(0xFFDC3545)
    }

    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(1000),
        label = "ScoreAnimation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "合规分数 / Compliance Score",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Score circle / 分数圆环
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedScore / 100f },
                    modifier = Modifier.size(120.dp),
                    color = scoreColor,
                    trackColor = scoreColor.copy(alpha = 0.2f),
                    strokeWidth = 10.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${animatedScore.toInt()}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                    Text(
                        text = "/ 100",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when {
                    score >= 80 -> "✅ 合规状态良好"
                    score >= 60 -> "⚠️ 需要改进"
                    else -> "🔴 存在严重问题"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = scoreColor
            )
        }
    }
}

@Composable
private fun IssueSummaryCard(result: AuditResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "问题摘要 / Issue Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IssueCountBadge(
                    count = result.criticalCount,
                    label = "严重",
                    color = Color(0xFFDC3545)
                )
                IssueCountBadge(
                    count = result.warningCount,
                    label = "警告",
                    color = Color(0xFFFD7E14)
                )
                IssueCountBadge(
                    count = result.suggestionCount,
                    label = "建议",
                    color = Color(0xFF0D6EFD)
                )
            }

            if (result.issues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Show first 3 issues / 显示前3个问题
                result.issues.take(3).forEach { issue ->
                    IssueItem(issue, onClick = {
                        // Navigate handled by ViewModel
                    })
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun IssueCountBadge(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun QuickActionsCard(state: BackgroundAudioHardeningState, viewModel: BackgroundAudioHardeningViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "快捷操作 / Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.processIntent(
                            BackgroundAudioHardeningIntent.StartAudit(
                                state.projectPath.ifEmpty { "./app" },
                                state.targetSdk
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("重新扫描")
                }
                OutlinedButton(
                    onClick = { viewModel.processIntent(BackgroundAudioHardeningIntent.ExportReport) },
                    modifier = Modifier.weight(1f),
                    enabled = state.auditResult != null
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导出报告")
                }
            }
        }
    }
}

@Composable
private fun AudioChangesInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFD7E14)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Android 17 音频变更 / Android 17 Audio Changes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Android 17 对后台音频播放实施严格的生命周期检查。音频播放、音频焦点请求、音量变更在 App 缺乏有效前台生命周期状态时会静默失败。",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "后台音频操作不抛异常、不崩溃，只在系统日志中有记录，开发者极难察觉。",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFDC3545)
            )
        }
    }
}

// ============================================================
// Scanner Tab — 音频扫描器标签页
// ============================================================

@Composable
private fun ScannerTab(state: BackgroundAudioHardeningState, viewModel: BackgroundAudioHardeningViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Scan configuration / 扫描配置
        ScanConfigCard(state, viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Scan progress / 扫描进度
        if (state.auditStatus == AuditStatus.Scanning) {
            ScanProgressCard(state)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Scan results / 扫描结果
        if (state.auditResult != null) {
            ScanResultsCard(state.auditResult, viewModel)
        }
    }
}

@Composable
private fun ScanConfigCard(state: BackgroundAudioHardeningState, viewModel: BackgroundAudioHardeningViewModel) {
    var projectPath by remember { mutableStateOf(state.projectPath) }
    var targetSdk by remember { mutableStateOf(state.targetSdk.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "扫描配置 / Scan Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = projectPath,
                onValueChange = { projectPath = it },
                label = { Text("项目路径 / Project Path") },
                placeholder = { Text("./app") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = targetSdk,
                onValueChange = { targetSdk = it.filter { c -> c.isDigit() } },
                label = { Text("Target SDK") },
                placeholder = { Text("37") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.processIntent(
                            BackgroundAudioHardeningIntent.StartAudit(
                                projectPath.ifEmpty { "./app" },
                                targetSdk.toIntOrNull() ?: 37
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = state.auditStatus != AuditStatus.Scanning
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("开始扫描")
                }

                if (state.auditStatus == AuditStatus.Scanning) {
                    OutlinedButton(
                        onClick = { viewModel.processIntent(BackgroundAudioHardeningIntent.CancelAudit) }
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("取消")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanProgressCard(state: BackgroundAudioHardeningState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F1FF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "正在扫描... ${state.scanProgress}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.scanProgress / 100f },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScanResultsCard(result: AuditResult, viewModel: BackgroundAudioHardeningViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "扫描结果 / Scan Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "耗时: ${result.scanDurationMs}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Violation type chips / 违规类型标签
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("全部 (${result.totalIssues})") }
                )
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text("严重 (${result.criticalCount})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFDC3545).copy(alpha = 0.2f)
                    )
                )
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text("警告 (${result.warningCount})") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Issue list / 问题列表
            result.issues.forEach { issue ->
                IssueDetailCard(issue, viewModel)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun IssueDetailCard(issue: ComplianceIssue, viewModel: BackgroundAudioHardeningViewModel) {
    val severityColor = when (issue.severity) {
        IssueSeverity.Critical -> Color(0xFFDC3545)
        IssueSeverity.Warning -> Color(0xFFFD7E14)
        IssueSeverity.Suggestion -> Color(0xFF0D6EFD)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, severityColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = severityColor.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(severityColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = issue.severity.labelCN,
                        style = MaterialTheme.typography.labelSmall,
                        color = severityColor
                    )
                }
                Text(
                    text = issue.type.labelCN,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = issue.filePath,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Line ${issue.lineNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Code snippet / 代码片段
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF8F9FA))
                    .padding(8.dp)
            ) {
                Text(
                    text = issue.codeSnippet,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = issue.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.processIntent(BackgroundAudioHardeningIntent.CopyCode(issue.codeSnippet)) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制代码", fontSize = 12.sp)
                }
            }
        }
    }
}

// ============================================================
// MediaSession Tab — MediaSession 模板标签页
// ============================================================

@Composable
private fun MediaSessionTab(state: BackgroundAudioHardeningState, viewModel: BackgroundAudioHardeningViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Template type selector / 模板类型选择
        TemplateTypeCard(state, viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Generated code / 生成的代码
        if (state.generatedCode.isNotEmpty()) {
            GeneratedCodeCard(state, viewModel)
        }
    }
}

@Composable
private fun TemplateTypeCard(state: BackgroundAudioHardeningState, viewModel: BackgroundAudioHardeningViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "选择模板类型 / Select Template Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            TemplateType.entries.forEach { templateType ->
                val isSelected = state.generatedTemplateType == templateType
                val backgroundColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            viewModel.processIntent(BackgroundAudioHardeningIntent.GenerateTemplate(templateType))
                        },
                    colors = CardDefaults.cardColors(containerColor = backgroundColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = templateType.labelCN,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = templateType.labelEN,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratedCodeCard(state: BackgroundAudioHardeningState, viewModel: BackgroundAudioHardeningViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "生成的代码 / Generated Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    viewModel.processIntent(BackgroundAudioHardeningIntent.CopyCode(state.generatedCode))
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制代码")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8F9FA))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = state.generatedCode,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ============================================================
// FGS Config Tab — FGS 配置标签页
// ============================================================

@Composable
private fun FGSConfigTab(state: BackgroundAudioHardeningState, viewModel: BackgroundAudioHardeningViewModel) {
    var serviceClassName by remember { mutableStateOf("PlaybackService") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Foreground Service 配置 / Foreground Service Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "为 mediaPlayback 类型 Foreground Service 生成合规的 AndroidManifest.xml 配置",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = serviceClassName,
                    onValueChange = { serviceClassName = it },
                    label = { Text("Service 类名 / Service Class Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.processIntent(
                            BackgroundAudioHardeningIntent.GenerateFGSConfig(
                                serviceClassName.ifEmpty { "PlaybackService" }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Build, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("生成 FGS 配置")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generated config / 生成的配置
        if (state.generatedCode.isNotEmpty() && state.activeTab == AudioHardeningTab.FGSConfig) {
            GeneratedCodeCard(state, viewModel)
        }
    }
}

// ============================================================
// CI Compliance Tab — CI 合规检测标签页
// ============================================================

@Composable
private fun CIComplianceTab(state: BackgroundAudioHardeningState, viewModel: BackgroundAudioHardeningViewModel) {
    var ciConfig by remember { mutableStateOf(state.ciConfig.ifEmpty { defaultCIConfig }) }
    var failOnError by remember { mutableStateOf(state.failOnError) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CI 合规配置 / CI Compliance Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "配置 GitHub Actions / GitLab CI 音频合规检测流水线",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ciConfig,
                    onValueChange = {
                        ciConfig = it
                        viewModel.processIntent(BackgroundAudioHardeningIntent.UpdateCIConfig(it, failOnError))
                    },
                    label = { Text("CI 配置 / CI Configuration") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 15
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fail on Error / 错误时失败")
                    androidx.compose.material3.Switch(
                        checked = failOnError,
                        onCheckedChange = {
                            failOnError = it
                            viewModel.processIntent(BackgroundAudioHardeningIntent.UpdateCIConfig(ciConfig, it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.processIntent(BackgroundAudioHardeningIntent.CopyCode(ciConfig))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("复制 CI 配置")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CI Info card / CI 信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F1FF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF0D6EFD))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CI 集成说明 / CI Integration Notes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 合规检测在 PR 构建时自动运行\n• 严重问题检测到时默认警告（不阻断构建）\n• 开启 Fail on Error 后，严重问题会阻断构建\n• 报告自动生成并上传到 CI Artifacts",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private val defaultCIConfig = """
    |# Audio Compliance CI Pipeline / 音频合规 CI 流水线
    |# Android 17 Background Audio Hardening Detection
    |
    |name: Audio Compliance Check
    |
    |on:
    |  pull_request:
    |    paths:
    |      - '**.kt'
    |      - '**.java'
    |      - '**/AndroidManifest.xml'
    |
    |jobs:
    |  audio-compliance:
    |    runs-on: ubuntu-latest
    |    steps:
    |      - uses: actions/checkout@v4
    |      - uses: actions/setup-java@v4
    |        with:
    |          distribution: 'temurin'
    |          java-version: '17'
    |
    |      - name: Run Audio Compliance Audit
    |        run: |
    |          ./gradlew auditAudioCompliance \
    |            --target-sdk 37 \
    |            --format html \
    |            --output ./reports
    |
    |      - name: Upload Report
    |        uses: actions/upload-artifact@v4
    |        with:
    |          name: audio-compliance-report
    |          path: reports/audio-compliance-report.html
""".trimMargin()

// ============================================================
// Fallback Strategy Tab — 降级策略标签页
// ============================================================

@Composable
private fun FallbackStrategyTab(state: BackgroundAudioHardeningState, viewModel: BackgroundAudioHardeningViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "降级策略模板 / Fallback Strategy Templates",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "音频操作失败时的优雅降级方案，确保用户体验",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Fallback strategy cards / 降级策略卡片
        FallbackStrategy.entries.forEach { strategy ->
            FallbackStrategyCard(strategy, viewModel)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// Fallback strategy enum / 降级策略枚举
enum class FallbackStrategy(val labelCN: String, val labelEN: String, val description: String) {
    GracefulPause("优雅暂停", "Graceful Pause", "检测到音频操作失败时，自动暂停播放并显示提示"),
    VolumeDuck("音量降低", "Volume Duck", "后台音频焦点丢失时，降低音量而非完全暂停"),
    NotificationPrompt("通知引导", "Notification Prompt", "引导用户将 App 切换到前台以恢复音频播放"),
    FallbackPlayback("降级播放", "Fallback Playback", "当在线播放失败时，降级到本地缓存播放")
}

@Composable
private fun FallbackStrategyCard(strategy: FallbackStrategy, viewModel: BackgroundAudioHardeningViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strategy.labelCN,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strategy.labelEN,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strategy.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "复制模板",
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        viewModel.processIntent(
                            BackgroundAudioHardeningIntent.CopyCode(fallbackTemplateCode(strategy))
                        )
                    }
            )
        }
    }
}

private fun fallbackTemplateCode(strategy: FallbackStrategy): String = when (strategy) {
    FallbackStrategy.GracefulPause -> """
        |/**
        | * 优雅暂停策略 / Graceful Pause Strategy
        | * 检测音频操作失败并优雅降级
        | */
        |class GracefulPauseManager(private val player: AudioPlayer) {
        |
        |    private var wasPlaying = false
        |
        |    fun onAudioOperationFailed() {
        |        if (player.isPlaying()) {
        |            wasPlaying = true
        |            player.pause()
        |            showUserNotification("音频播放已暂停，请在 App 内继续")
        |        }
        |    }
        |
        |    fun restorePlayback() {
        |        if (wasPlaying) {
        |            player.play()
        |            wasPlaying = false
        |        }
        |    }
        |}
    """.trimMargin()

    FallbackStrategy.VolumeDuck -> """
        |/**
        | * 音量降低策略 / Volume Duck Strategy
        | * 音频焦点丢失时降低音量而非完全暂停
        | */
        |class VolumeDuckManager(private val audioManager: AudioManager) {
        |
        |    fun onAudioFocusLossTransient() {
        |        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 0)
        |    }
        |
        |    fun onAudioFocusGain() {
        |        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0)
        |    }
        |}
    """.trimMargin()

    FallbackStrategy.NotificationPrompt -> """
        |/**
        | * 通知引导策略 / Notification Prompt Strategy
        | * 引导用户回到前台
        | */
        |class NotificationPromptManager(private val context: Context) {
        |
        |    fun showForegroundPrompt() {
        |        val intent = Intent(context, MainActivity::class.java).apply {
        |            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        |        }
        |        val pendingIntent = PendingIntent.getActivity(
        |            context, 0, intent,
        |            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        |        )
        |
        |        NotificationCompat.Builder(context, CHANNEL_ID)
        |            .setContentTitle("音频暂停")
        |            .setContentText("点击回到 App 继续播放")
        |            .setContentIntent(pendingIntent)
        |            .setAutoCancel(true)
        |            .build()
        |    }
        |}
    """.trimMargin()

    FallbackStrategy.FallbackPlayback -> """
        |/**
        | * 降级播放策略 / Fallback Playback Strategy
        | * 在线播放失败时降级到本地缓存
        | */
        |class FallbackPlaybackManager(
        |    private val onlinePlayer: OnlineAudioPlayer,
        |    private val cachePlayer: CachedAudioPlayer
        |) {
        |
        |    fun playWithFallback(url: String) {
        |        try {
        |            onlinePlayer.play(url)
        |        } catch (e: AudioPlaybackException) {
        |            // 降级到本地缓存 / Fallback to cache
        |            cachePlayer.play(url)
        |            showUserNotification("已切换到离线播放模式")
        |        }
        |    }
        |}
    """.trimMargin()
}

// ============================================================
// Shared Components — 共享组件
// ============================================================

@Composable
private fun IssueItem(issue: ComplianceIssue, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = when (issue.severity) {
            IssueSeverity.Critical -> Color(0xFFDC3545)
            IssueSeverity.Warning -> Color(0xFFFD7E14)
            IssueSeverity.Suggestion -> Color(0xFF0D6EFD)
        }
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = issue.type.labelCN,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = issue.filePath.substringAfterLast("/"),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
