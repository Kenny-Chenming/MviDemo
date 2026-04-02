package com.mvi.kenny.feature.android17migration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.R
import com.mvi.kenny.base.TopBarActions
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collect

/**
 * ============================================================
 * MigrationDashboardScreen — Android 17 迁移助手主仪表盘
 * ============================================================
 * Main entry point for Android 17 migration assistance.
 *
 * Features:
 * - Project selection via folder picker
 * - Migration scan with real-time progress
 * - Issue cards list with severity filtering
 * - Quick actions: start scan, export report, auto-fix
 *
 * Architecture:
 * - MVI pattern with MigrationViewModel
 * - Bilingual labels (Chinese primary, English secondary)
 *
 * @param onUpdateTopBar TopBar configuration callback to parent
 * @param onNavigateToDetail Navigation callback for issue detail
 * @param onNavigateToWizard Navigation callback for fix wizard
 *
 * @see MigrationViewModel State management
 * @see IssueDetailScreen Issue detail page
 * @see WizardScreen Fix wizard
 */
@Composable
fun MigrationDashboardScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToWizard: (String) -> Unit = {},
    viewModel: MigrationViewModel = viewModel()
) {
    // Subscribe to ViewModel state
    val state by viewModel.state.collectAsState()

    // Track current filter tab
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf(
        stringResource(R.string.migration_filter_all) to null,
        stringResource(R.string.migration_filter_critical) to Severity.CRITICAL,
        stringResource(R.string.migration_filter_warning) to Severity.WARNING,
        stringResource(R.string.migration_filter_info) to Severity.INFO
    )

    // Track current export format selection
    var showExportDialog by remember { mutableStateOf(false) }

    // TopBar configuration
    val topBarConfig by remember {
        mutableStateOf(
            TopBarConfig(
                title = "Android 17 Migration",
                actions = listOf(
                    TopBarActions.settings { showExportDialog = true }
                )
            )
        )
    }

    // Report TopBar to parent
    LaunchedEffect(topBarConfig) {
        onUpdateTopBar(topBarConfig)
    }

    // Collect effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MigrationEffect.NavigateToDetail -> onNavigateToDetail(effect.issueId)
                is MigrationEffect.NavigateToWizard -> onNavigateToWizard(effect.issueId)
                else -> { /* Other effects handled by specific screens */ }
            }
        }
    }

    // Handle tab changes
    LaunchedEffect(selectedTabIndex) {
        val filter = filterTabs.getOrNull(selectedTabIndex)?.second
        viewModel.sendIntent(MigrationIntent.SetSeverityFilter(filter))
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Project Selection Card
            item {
                ProjectSelectionCard(
                    selectedPath = state.selectedProjectPath,
                    onSelectProject = { path ->
                        viewModel.sendIntent(MigrationIntent.SelectProject(path))
                    },
                    enabled = state.scanStatus != ScanStatus.SCANNING
                )
            }

            // Scan Progress Section
            item {
                ScanProgressSection(
                    scanStatus = state.scanStatus,
                    scanProgress = state.scanProgress,
                    scannedFilesCount = state.scannedFilesCount,
                    totalFilesCount = state.totalFilesCount,
                    onStartScan = { viewModel.sendIntent(MigrationIntent.StartScan) },
                    onCancelScan = { viewModel.sendIntent(MigrationIntent.CancelScan) }
                )
            }

            // Summary Cards
            item {
                SummaryCards(
                    totalIssues = state.totalIssues,
                    criticalCount = state.criticalCount,
                    warningCount = state.warningCount,
                    infoCount = state.infoCount,
                    fixedCount = state.fixedCount,
                    pendingCount = state.pendingCount
                )
            }

            // Migration Progress Bar
            item {
                MigrationProgressBar(
                    progress = state.migrationProgress,
                    progressPercent = state.progressPercent,
                    fixedCount = state.fixedCount,
                    totalIssues = state.totalIssues
                )
            }

            // Quick Action Buttons
            item {
                QuickActionButtons(
                    hasIssues = state.pendingCount > 0,
                    isExporting = state.isExporting,
                    onStartScan = { viewModel.sendIntent(MigrationIntent.StartScan) },
                    onAutoFix = { viewModel.sendIntent(MigrationIntent.RunAutoFix) },
                    onExport = { showExportDialog = true }
                )
            }

            // Filter Tabs
            item {
                FilterTabs(
                    selectedTabIndex = selectedTabIndex,
                    filterTabs = filterTabs,
                    onTabSelected = { selectedTabIndex = it }
                )
            }

            // Issue Cards List
            if (state.filteredIssues.isEmpty() && state.scanStatus == ScanStatus.DONE) {
                item {
                    EmptyStateCard(
                        onStartScan = { viewModel.sendIntent(MigrationIntent.StartScan) }
                    )
                }
            } else {
                items(
                    items = state.filteredIssues,
                    key = { it.id }
                ) { issue ->
                    IssueCard(
                        issue = issue,
                        onCardClick = {
                            viewModel.sendIntent(MigrationIntent.NavigateToWizard(issue.id))
                        },
                        onFixClick = {
                            viewModel.sendIntent(MigrationIntent.FixIssue(issue.id))
                        },
                        onIgnoreClick = {
                            viewModel.sendIntent(MigrationIntent.IgnoreIssue(issue.id))
                        }
                    )
                }
            }
        }
    }

    // Export Format Dialog
    if (showExportDialog) {
        ExportFormatDialog(
            onDismiss = { showExportDialog = false },
            onExportMarkdown = {
                showExportDialog = false
                viewModel.sendIntent(MigrationIntent.ExportReport(ExportFormat.MARKDOWN))
            },
            onExportPdf = {
                showExportDialog = false
                viewModel.sendIntent(MigrationIntent.ExportReport(ExportFormat.PDF))
            }
        )
    }
}

@Composable
private fun ProjectSelectionCard(
    selectedPath: String?,
    onSelectProject: (String) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) {
                    onSelectProject("/Users/kenny/WorkSpace/AndroidStudioProjects/MyMviProject")
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedPath?.substringAfterLast("/") ?: "选择 Android 项目",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (selectedPath != null) {
                    Text(
                        text = selectedPath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Tap to select project folder / 点击选择项目文件夹",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanProgressSection(
    scanStatus: ScanStatus,
    scanProgress: Float,
    scannedFilesCount: Int,
    totalFilesCount: Int,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (scanStatus) {
                ScanStatus.SCANNING -> MaterialTheme.colorScheme.primaryContainer
                ScanStatus.DONE -> MaterialTheme.colorScheme.tertiaryContainer
                ScanStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = scanStatus.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (scanStatus == ScanStatus.SCANNING) {
                    IconButton(onClick = onCancelScan) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Cancel scan / 取消扫描",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (scanStatus == ScanStatus.IDLE) {
                    Button(
                        onClick = onStartScan,
                        enabled = scannedFilesCount > 0 || totalFilesCount > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start Scan")
                    }
                }
            }

            if (scanStatus == ScanStatus.SCANNING) {
                Spacer(modifier = Modifier.height(12.dp))
                val animatedProgress by animateFloatAsState(
                    targetValue = scanProgress,
                    animationSpec = tween(durationMillis = 300),
                    label = "scan_progress"
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scanning: $scannedFilesCount / $totalFilesCount files",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (scanStatus == ScanStatus.DONE) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scan complete! / 扫描完成！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun SummaryCards(
    totalIssues: Int,
    criticalCount: Int,
    warningCount: Int,
    infoCount: Int,
    fixedCount: Int,
    pendingCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = stringResource(R.string.migration_total),
            count = totalIssues,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(100.dp)
        )
        SummaryCard(
            title = stringResource(R.string.migration_critical),
            count = criticalCount,
            color = Severity.CRITICAL.color,
            modifier = Modifier.width(100.dp)
        )
        SummaryCard(
            title = stringResource(R.string.migration_warning),
            count = warningCount,
            color = Severity.WARNING.color,
            modifier = Modifier.width(100.dp)
        )
        SummaryCard(
            title = stringResource(R.string.migration_info),
            count = infoCount,
            color = Severity.INFO.color,
            modifier = Modifier.width(100.dp)
        )
        SummaryCard(
            title = stringResource(R.string.migration_fixed),
            count = fixedCount,
            color = Color(0xFF43A047),
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun MigrationProgressBar(
    progress: Float,
    progressPercent: String,
    fixedCount: Int,
    totalIssues: Int
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Migration Progress / 迁移进度",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = progressPercent,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 500),
                label = "migration_progress"
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$fixedCount / $totalIssues issues resolved / 已解决",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionButtons(
    hasIssues: Boolean,
    isExporting: Boolean,
    onStartScan: () -> Unit,
    onAutoFix: () -> Unit,
    onExport: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onStartScan,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Scan / 扫描")
        }

        Button(
            onClick = onAutoFix,
            modifier = Modifier.weight(1f),
            enabled = hasIssues && !isExporting,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF43A047)
            )
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Auto Fix")
        }

        OutlinedButton(
            onClick = onExport,
            modifier = Modifier.weight(1f),
            enabled = !isExporting
        ) {
            if (isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text("Export")
        }
    }
}

@Composable
private fun FilterTabs(
    selectedTabIndex: Int,
    filterTabs: List<Pair<String, Severity?>>,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.fillMaxWidth()
    ) {
        filterTabs.forEachIndexed { index, (label, _) ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }
}

@Composable
private fun IssueCard(
    issue: MigrationIssue,
    onCardClick: () -> Unit,
    onFixClick: () -> Unit,
    onIgnoreClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeverityBadge(severity = issue.severity)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = issue.titleZh,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = issue.descriptionZh,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${issue.affectedFiles} files",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${issue.affectedMethods} methods",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onIgnoreClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "忽略",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onFixClick,
                    enabled = !issue.isFixed,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    if (issue.isFixed) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (issue.isFixed) "已修复" else "修复",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SeverityBadge(severity: Severity) {
    Box(
        modifier = Modifier
            .background(
                color = severity.color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = severity.labelZh,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = severity.color
        )
    }
}

@Composable
private fun EmptyStateCard(onStartScan: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF43A047),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "All clear!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "No migration issues found / 未发现迁移问题",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onStartScan) {
                Text("Scan Again / 重新扫描")
            }
        }
    }
}

@Composable
private fun ExportFormatDialog(
    onDismiss: () -> Unit,
    onExportMarkdown: () -> Unit,
    onExportPdf: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Export Report / 导出报告",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Choose export format / 选择导出格式",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onExportMarkdown,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Markdown (.md)")
                }

                OutlinedButton(
                    onClick = onExportPdf,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("PDF (.pdf)")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
