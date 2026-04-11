package com.mvi.kenny.feature.alarmmigration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collect

/**
 * ============================================================
 * AlarmMigrationScreen — AlarmManager Listener Mode 迁移工具主界面
 * ============================================================
 * CLI-style interface for AlarmManager PendingIntent → OnAlarmListener migration.
 * AlarmManager PendingIntent → OnAlarmListener 迁移的 CLI 风格界面。
 *
 * Features:
 * - Scan: AST-based AlarmManager usage detection
 * - Preview: Dry-run migration change preview
 * - Migrate: Execute code transformation
 * - Report: HTML report generation
 * - Diff: Compare two scan results
 *
 * @see AlarmMigrationViewModel State management
 * @see AlarmMigrationContract MVI contract
 * @see CliColors CLI color constants
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmMigrationScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: AlarmMigrationViewModel = viewModel()
) {
    // Collect states from ViewModel / 从 ViewModel 收集状态
    val scanState by viewModel.scanState.collectAsState()
    val migrationState by viewModel.migrationState.collectAsState()
    val reportState by viewModel.reportState.collectAsState()

    // Tab state / Tab 状态
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Scan / 扫描", "Preview / 预览", "Report / 报告", "Diff / 对比")

    // Effect collection / 副作用收集
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AlarmMigrationEffect.ShowToast -> { /* Toast handled externally */ }
                is AlarmMigrationEffect.ShowError -> { /* Error handled externally */ }
                else -> {}
            }
        }
    }

    // Update TopBar with current tab title / 更新 TopBar 标题
    LaunchedEffect(selectedTabIndex) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Alarm 迁移工具 / Alarm Migration",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.BugReport,
                        contentDescription = "Alarm Manager Migration",
                        onClick = { }
                    )
                )
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row / Tab 行
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        // Tab Content / Tab 内容
        when (selectedTabIndex) {
            0 -> ScanTab(
                scanState = scanState,
                onIntent = viewModel::sendIntent
            )
            1 -> PreviewTab(
                scanState = scanState,
                migrationState = migrationState,
                onIntent = viewModel::sendIntent
            )
            2 -> ReportTab(
                scanState = scanState,
                reportState = reportState,
                onIntent = viewModel::sendIntent
            )
            3 -> DiffTab(
                onIntent = viewModel::sendIntent
            )
        }
    }
}

// =============================================================
// Scan Tab — 扫描 Tab
// =============================================================
/**
 * Scan tab with CLI-style output / 带 CLI 风格输出的扫描 Tab
 *
 * @param scanState Current scan state / 当前扫描状态
 * @param onIntent Intent sender / 意图发送器
 */
@Composable
private fun ScanTab(
    scanState: AlarmScanState,
    onIntent: (AlarmMigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Module Path Input / 模块路径输入
        OutlinedTextField(
            value = scanState.modulePath,
            onValueChange = { onIntent(AlarmMigrationIntent.SetModulePath(it)) },
            label = { Text("Module Path / 模块路径") },
            placeholder = { Text("e.g., app/src/main/java") },
            leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Scan / Cancel Button / 扫描/取消按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (scanState.scanStatus == AlarmScanStatus.SCANNING) {
                Button(
                    onClick = { onIntent(AlarmMigrationIntent.CancelScan) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CliColors.CRITICAL
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel / 取消")
                }
            } else {
                Button(
                    onClick = { onIntent(AlarmMigrationIntent.StartScan) },
                    enabled = scanState.modulePath.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan / 扫描")
                }
            }
        }

        // Progress Bar / 进度条
        if (scanState.scanStatus == AlarmScanStatus.SCANNING) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { scanState.scanProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = CliColors.INFO,
            )
            Text(
                text = "${(scanState.scanProgress * 100).toInt()}% — ${scanState.summary.filesScanned} files / 文件",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Cards / 摘要卡片
        if (scanState.summary.total > 0) {
            SummaryCards(summary = scanState.summary)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Filter Chips / 过滤芯片
        FilterChipsRow(
            currentFilter = scanState.filterCriteria,
            onIntent = onIntent
        )

        Spacer(modifier = Modifier.height(12.dp))

        // CLI Output Panel / CLI 输出面板
        if (scanState.filteredResults.isNotEmpty()) {
            CliOutputPanel(
                issues = scanState.filteredResults,
                modifier = Modifier.weight(1f)
            )
        } else if (scanState.scanStatus == AlarmScanStatus.COMPLETED) {
            EmptyState(
                icon = Icons.Default.CheckCircle,
                message = "No issues found / 未发现问题",
                color = CliColors.SUCCESS
            )
        } else {
            EmptyState(
                icon = Icons.Default.BugReport,
                message = "Run scan to detect AlarmManager usage / 运行扫描以检测 AlarmManager 用法",
                color = CliColors.INFO
            )
        }
    }
}

// =============================================================
// Preview Tab — 预览 Tab
// =============================================================
/**
 * Preview tab with dry-run migration display / 带 dry-run 迁移预览的 Tab
 *
 * @param scanState Current scan state / 当前扫描状态
 * @param migrationState Current migration state / 当前迁移状态
 * @param onIntent Intent sender / 意图发送器
 */
@Composable
private fun PreviewTab(
    scanState: AlarmScanState,
    migrationState: AlarmMigrationState,
    onIntent: (AlarmMigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Status Header / 状态头
        StatusHeader(
            migrationStatus = migrationState.migrationStatus,
            progress = migrationState.migrationProgress
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (migrationState.migrationStatus) {
            AlarmMigrationStatus.PREVIEW -> {
                // Generate Preview Button / 生成预览按钮
                val unfixedIssues = scanState.results.filter { !it.isFixed && !it.isIgnored }
                Button(
                    onClick = { onIntent(AlarmMigrationIntent.GeneratePreview(unfixedIssues)) },
                    enabled = unfixedIssues.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Preview / 生成预览 (${unfixedIssues.size} issues)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (migrationState.pendingChanges.isNotEmpty()) {
                    Text(
                        text = "Pending Changes / 待处理变更 (${migrationState.pendingChanges.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Migration Preview List / 迁移预览列表
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(migrationState.pendingChanges) { change ->
                            ChangeCard(change = change)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Execute / Rollback Buttons / 执行/回滚按钮
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                onIntent(AlarmMigrationIntent.ExecuteMigration(migrationState.pendingChanges))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CliColors.SUCCESS
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Execute / 执行")
                        }
                    }
                } else {
                    EmptyState(
                        icon = Icons.Default.Pending,
                        message = "No pending changes. Run scan first. / 无待处理变更，请先运行扫描。",
                        color = CliColors.WARNING
                    )
                }
            }

            AlarmMigrationStatus.MIGRATING -> {
                // Migration Progress / 迁移进度
                LinearProgressIndicator(
                    progress = { migrationState.migrationProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CliColors.INFO,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Migrating... ${(migrationState.migrationProgress * 100).toInt()}%",
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Completed Files / 已完成文件
                Text(
                    text = "Completed / 已完成 (${migrationState.completedFiles.size})",
                    fontWeight = FontWeight.Bold
                )
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(migrationState.completedFiles) { file ->
                        Text(
                            text = "✓ $file",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                // Blocked Files / 阻塞文件
                if (migrationState.blockedFiles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Blocked / 阻塞 (${migrationState.blockedFiles.size})",
                        fontWeight = FontWeight.Bold,
                        color = CliColors.CRITICAL
                    )
                    LazyColumn {
                        items(migrationState.blockedFiles) { blocked ->
                            Text(
                                text = "✗ ${blocked.filePath}: ${blocked.reason}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = CliColors.CRITICAL,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            AlarmMigrationStatus.COMPLETED -> {
                // Migration Complete / 迁移完成
                SuccessCard(
                    message = "Migration completed successfully! / 迁移成功完成！",
                    completedCount = migrationState.completedFiles.size,
                    blockedCount = migrationState.blockedFiles.size,
                    rollbackAvailable = migrationState.rollbackAvailable,
                    onRollback = { onIntent(AlarmMigrationIntent.Rollback) }
                )
            }

            AlarmMigrationStatus.ROLLED_BACK -> {
                EmptyState(
                    icon = Icons.Default.Refresh,
                    message = "Rollback complete / 回滚完成",
                    color = CliColors.WARNING
                )
            }

            else -> {
                EmptyState(
                    icon = Icons.Default.Pending,
                    message = "Generate preview to see pending changes / 生成预览以查看待处理变更",
                    color = CliColors.INFO
                )
            }
        }
    }
}

// =============================================================
// Report Tab — 报告 Tab
// =============================================================
/**
 * Report tab for HTML report generation / HTML 报告生成 Tab
 *
 * @param scanState Current scan state / 当前扫描状态
 * @param reportState Current report state / 当前报告状态
 * @param onIntent Intent sender / 意图发送器
 */
@Composable
private fun ReportTab(
    scanState: AlarmScanState,
    reportState: AlarmReportState,
    onIntent: (AlarmMigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Report Status / 报告状态
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "HTML Report / HTML 报告",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Generate an interactive HTML report of the scan results. / 生成扫描结果的可交互 HTML 报告。",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (reportState.reportPath != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Last report: ${reportState.reportPath}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = CliColors.SUCCESS
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generate Report Button / 生成报告按钮
        Button(
            onClick = { onIntent(AlarmMigrationIntent.GenerateReport(scanState)) },
            enabled = reportState.reportStatus != AlarmScanStatus.SCANNING && scanState.summary.total > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (reportState.reportStatus == AlarmScanStatus.SCANNING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Icon(Icons.Default.Description, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (reportState.reportStatus == AlarmScanStatus.SCANNING) "Generating... / 生成中..." else "Generate HTML Report / 生成 HTML 报告")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Report Preview / 报告预览
        if (reportState.reportPath != null) {
            HtmlReportPreview(
                scanState = scanState,
                reportPath = reportState.reportPath,
                onOpenReport = { onIntent(AlarmMigrationIntent.OpenReport(reportState.reportPath!!)) },
                modifier = Modifier.weight(1f)
            )
        } else {
            EmptyState(
                icon = Icons.Default.Description,
                message = "Generate a report to see preview / 生成报告以查看预览",
                color = CliColors.INFO
            )
        }
    }
}

// =============================================================
// Diff Tab — 对比 Tab
// =============================================================
/**
 * Diff tab for comparing two scan results / 对比两次扫描结果的 Tab
 *
 * @param onIntent Intent sender / 意图发送器
 */
@Composable
private fun DiffTab(
    onIntent: (AlarmMigrationIntent) -> Unit
) {
    var beforePath by remember { mutableStateOf("") }
    var afterPath by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Diff / 差异对比",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Compare two scan results to track migration progress. / 对比两次扫描结果以跟踪迁移进度。",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Before Scan Path / 前一次扫描路径
        OutlinedTextField(
            value = beforePath,
            onValueChange = { beforePath = it },
            label = { Text("Before Scan JSON / 前一次扫描 JSON") },
            placeholder = { Text("/path/to/before_scan.json") },
            leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // After Scan Path / 后一次扫描路径
        OutlinedTextField(
            value = afterPath,
            onValueChange = { afterPath = it },
            label = { Text("After Scan JSON / 后一次扫描 JSON") },
            placeholder = { Text("/path/to/after_scan.json") },
            leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Compare Button / 对比按钮
        Button(
            onClick = { onIntent(AlarmMigrationIntent.DiffScans(beforePath, afterPath)) },
            enabled = beforePath.isNotEmpty() && afterPath.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CompareArrows, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Compare / 对比")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CLI Diff Output Preview / CLI 差异输出预览
        CliDiffPreview()
    }
}

// =============================================================
// CLI Output Panel — CLI 输出面板
// =============================================================
/**
 * CLI-style output panel with colored severity labels.
 * 带颜色标记严重程度的 CLI 风格输出面板。
 *
 * @param issues List of issues to display / 要显示的问题列表
 * @param modifier Modifier / 修饰器
 */
@Composable
private fun CliOutputPanel(
    issues: List<AlarmUsageIssue>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
            .padding(12.dp)
    ) {
        // Terminal header / 终端头部
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            listOf(Color(0xFFFF5F56), Color(0xFFFFBD2E), Color(0xFF27C93F)).forEach { dotColor ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(dotColor)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "kaifu alarm scan",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF8C8C8C)
            )
        }

        HorizontalDivider(color = Color(0xFF3C3C3C))

        // Issues list / 问题列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(issues) { issue ->
                CliIssueItem(issue = issue)
            }
        }
    }
}

/**
 * Single CLI issue item / 单个 CLI 问题条目
 */
@Composable
private fun CliIssueItem(issue: AlarmUsageIssue) {
    val severityColor = issue.severity.cliColor
    val severityLabel = issue.severity.label
    val apiLabel = issue.apiType.oldApi.split("+").first().trim()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Severity badge + location / 严重程度徽章 + 位置
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Severity label / 严重程度标签
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(severityColor.copy(alpha = 0.2f))
                    .border(1.dp, severityColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "[${issue.severity.label.uppercase()}]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = severityColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // File:line / 文件:行号
            Text(
                text = "${issue.filePath.split("/").last()}:${issue.lineNumber}",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.width(8.dp))

            // API type / API 类型
            Text(
                text = "— $apiLabel",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF9CDCFE)
            )
        }

        // Suggestion / 建议
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
        ) {
            Text(
                text = "└─ 建议: ",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF6A9955)
            )
            Text(
                text = "替换为 ${issue.apiType.newApi.split("+").first().trim()}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFFDCDCAA)
            )
        }
    }
}

// =============================================================
// Summary Cards — 摘要卡片
// =============================================================
/**
 * Summary statistics cards / 摘要统计卡片
 */
@Composable
private fun SummaryCards(summary: ScanSummary) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SummaryCard(
            label = "Total / 总计",
            count = summary.total,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Critical / 严重",
            count = summary.critical,
            color = CliColors.CRITICAL,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Warning / 警告",
            count = summary.warning,
            color = CliColors.WARNING,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Info / 提示",
            count = summary.info,
            color = CliColors.INFO,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Single summary card / 单个摘要卡片
 */
@Composable
private fun SummaryCard(
    label: String,
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================
// Filter Chips Row — 过滤芯片行
// =============================================================
/**
 * Severity filter chips row / 严重程度过滤芯片行
 */
@Composable
private fun FilterChipsRow(
    currentFilter: FilterCriteria,
    onIntent: (AlarmMigrationIntent) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        // All filter / 全部过滤
        FilterChip(
            selected = currentFilter.severity == null,
            onClick = { onIntent(AlarmMigrationIntent.SetSeverityFilter(null)) },
            label = { Text("All / 全部") }
        )

        AlarmSeverity.entries.forEach { severity ->
            FilterChip(
                selected = currentFilter.severity == severity,
                onClick = { onIntent(AlarmMigrationIntent.SetSeverityFilter(severity)) },
                label = { Text(severity.labelZh) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = severity.color.copy(alpha = 0.2f)
                )
            )
        }

        // Show Fixed toggle / 显示已修复切换
        FilterChip(
            selected = currentFilter.showFixed,
            onClick = { onIntent(AlarmMigrationIntent.ToggleShowFixed(!currentFilter.showFixed)) },
            label = { Text("Fixed / 已修复") },
            leadingIcon = {
                if (currentFilter.showFixed) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        )
    }
}

// =============================================================
// Status Header — 状态头
// =============================================================
/**
 * Migration status header / 迁移状态头
 */
@Composable
private fun StatusHeader(
    migrationStatus: AlarmMigrationStatus,
    progress: Float
) {
    val (icon, color, label) = when (migrationStatus) {
        AlarmMigrationStatus.PREVIEW -> Triple(Icons.Default.Pending, CliColors.INFO, "Preview Mode / 预览模式")
        AlarmMigrationStatus.MIGRATING -> Triple(Icons.Default.Refresh, CliColors.WARNING, "Migrating... / 迁移中...")
        AlarmMigrationStatus.COMPLETED -> Triple(Icons.Default.CheckCircle, CliColors.SUCCESS, "Completed / 完成")
        AlarmMigrationStatus.ROLLED_BACK -> Triple(Icons.Default.ArrowBack, CliColors.WARNING, "Rolled Back / 已回滚")
        AlarmMigrationStatus.ERROR -> Triple(Icons.Default.Error, CliColors.CRITICAL, "Error / 错误")
        else -> Triple(Icons.Default.Pending, Color.Gray, "Idle / 待机")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = color
        )
        if (migrationStatus == AlarmMigrationStatus.MIGRATING) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${(progress * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// =============================================================
// Change Card — 变更卡片
// =============================================================
/**
 * Single migration change card / 单个迁移变更卡片
 */
@Composable
private fun ChangeCard(change: PendingChange) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // File path / 文件路径
            Text(
                text = change.filePath.split("/").last(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = change.filePath,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Original code / 原始代码
            Text(
                text = "Original / 原始:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CliColors.CRITICAL
            )
            Text(
                text = change.originalCode.take(100) + if (change.originalCode.length > 100) "..." else "",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2D2D2D))
                    .padding(6.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Migrated code / 迁移后代码
            Text(
                text = "Migrated / 迁移后:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CliColors.SUCCESS
            )
            Text(
                text = change.migratedCode.take(100) + if (change.migratedCode.length > 100) "..." else "",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2D2D2D))
                    .padding(6.dp)
            )
        }
    }
}

// =============================================================
// Success Card — 成功卡片
// =============================================================
/**
 * Migration success card / 迁移成功卡片
 */
@Composable
private fun SuccessCard(
    message: String,
    completedCount: Int,
    blockedCount: Int,
    rollbackAvailable: Boolean,
    onRollback: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CliColors.SUCCESS.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CliColors.SUCCESS)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = CliColors.SUCCESS,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CliColors.SUCCESS
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$completedCount files migrated / 文件已迁移, $blockedCount blocked / 被阻塞",
                fontSize = 14.sp
            )
            if (rollbackAvailable) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onRollback,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CliColors.WARNING
                    )
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rollback / 回滚")
                }
            }
        }
    }
}

// =============================================================
// Empty State — 空状态
// =============================================================
/**
 * Empty state placeholder / 空状态占位符
 */
@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =============================================================
// CLI Diff Preview — CLI 差异预览
// =============================================================
/**
 * CLI-style diff preview panel / CLI 风格的差异预览面板
 */
@Composable
private fun CliDiffPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
            .padding(12.dp)
    ) {
        Text(
            text = "kaifu alarm diff <before.json> <after.json>",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = Color(0xFF9CDCFE)
        )
        HorizontalDivider(color = Color(0xFF3C3C3C), modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Run diff to compare two scan results / 运行 diff 对比两次扫描结果",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFF6A9955)
        )
    }
}

// =============================================================
// HtmlReportPreview — HTML 报告预览组件
// =============================================================
/**
 * HTML report preview component / HTML 报告预览组件
 *
 * Shows a preview of the generated HTML report with summary stats.
 * 显示生成的 HTML 报告预览，包含摘要统计。
 *
 * @param scanState Scan state to display / 要显示的扫描状态
 * @param reportPath Path to the report file / 报告文件路径
 * @param onOpenReport Callback to open report / 打开报告的回调
 * @param modifier Modifier / 修饰器
 */
@Composable
fun HtmlReportPreview(
    scanState: AlarmScanState,
    reportPath: String,
    onOpenReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        // Report header / 报告头部
        Text(
            text = "Report Preview / 报告预览",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Summary stats / 摘要统计
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatBox(
                label = "Total / 总计",
                value = scanState.summary.total.toString(),
                color = Color.Gray,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "Critical / 严重",
                value = scanState.summary.critical.toString(),
                color = CliColors.CRITICAL,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "Warning / 警告",
                value = scanState.summary.warning.toString(),
                color = CliColors.WARNING,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "Info / 提示",
                value = scanState.summary.info.toString(),
                color = CliColors.INFO,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Report info / 报告信息
        Text(
            text = "Report Path / 报告路径:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = reportPath,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Open button / 打开按钮
        Button(
            onClick = onOpenReport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Report / 打开报告")
        }
    }
}

/**
 * Single statistics box / 单个统计框
 */
@Composable
private fun StatBox(
    label: String,
    value: String,
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}