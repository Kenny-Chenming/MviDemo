package com.mvi.kenny.feature.smsretrievertool

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// =============================================================
// SmsRetrieverToolScreen — Android 17 SMS Retriever API
// 迁移检测与自动化工具包主界面
// =============================================================
/**
 * Main screen for SMS Retriever Tool / SMS Retriever API 工具主界面
 *
 * Implements a dashboard + multi-tab interface as specified in the design doc:
 * - Tab navigation: Scanner / Detail / Migration Preview / Report / Settings
 *
 * Key features:
 * - Scan project for READ_SMS permission usage
 * - View affected code paths with severity分级
 * - Preview and apply automated migration
 * - Generate compliance reports
 * - Configure tool settings
 *
 * @param viewModel ViewModel instance / ViewModel 实例
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsRetrieverToolScreen(
    viewModel: SmsRetrieverToolViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Scanner", "Detail", "Preview", "Report", "Settings")
    val scope = rememberCoroutineScope()

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SmsRetrieverToolEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is SmsRetrieverToolEffect.ShowError -> {
                    snackbarHostState.showSnackbar("Error: ${effect.message}")
                }
                is SmsRetrieverToolEffect.NavigateToDetail -> {
                    selectedTabIndex = 1 // Switch to Detail tab / 切换到详情标签
                }
                is SmsRetrieverToolEffect.NavigateToMigrationPreview -> {
                    selectedTabIndex = 2 // Switch to Preview tab / 切换到预览标签
                }
                is SmsRetrieverToolEffect.ReportGenerated -> {
                    selectedTabIndex = 3 // Switch to Report tab / 切换到报告标签
                }
                is SmsRetrieverToolEffect.MigrationCompleted -> {
                    selectedTabIndex = 0 // Back to Scanner / 返回扫描器
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SMS Retriever Tool",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White
                ),
                actions = {
                    // Refresh / 刷新
                    IconButton(
                        onClick = {
                            state.selectedModule?.let {
                                viewModel.processIntent(SmsRetrieverToolIntent.StartScan(it))
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh / 刷新",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                tabTitles.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    when (index) {
                                        0 -> { // Scanner tab / 扫描器标签
                                            if (state.blockerCount > 0) {
                                                Badge { Text("${state.blockerCount}") }
                                            }
                                        }
                                        3 -> { // Report tab / 报告标签
                                            if (state.complianceReport != null) {
                                                Badge { Icon(Icons.Default.Check, null) }
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = when (index) {
                                        0 -> Icons.Default.Search
                                        1 -> Icons.Default.Description
                                        2 -> Icons.Default.ArrowBack
                                        3 -> Icons.Default.Description
                                        else -> Icons.Default.Settings
                                    },
                                    contentDescription = title
                                )
                            }
                        },
                        label = { Text(title) },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab content / Tab 内容
            when (selectedTabIndex) {
                0 -> ScannerTab(state, viewModel)
                1 -> DetailTab(state, viewModel)
                2 -> MigrationPreviewTab(state, viewModel)
                3 -> ComplianceReportTab(state, viewModel)
                4 -> SettingsTab(state, viewModel)
            }
        }
    }
}

// =============================================================
// ScannerTab — 扫描器标签页
// =============================================================
/**
 * Scanner tab content / 扫描器标签页内容
 *
 * Displays scan controls, progress, and results list.
 * 用户可以输入模块路径并开始扫描，查看受影响代码路径列表。
 *
 * @param state Current UI state / 当前 UI 状态
 * @param viewModel ViewModel for processing intents / 处理意图的 ViewModel
 */
@Composable
private fun ScannerTab(
    state: SmsRetrieverToolState,
    viewModel: SmsRetrieverToolViewModel
) {
    var moduleInput by remember { mutableStateOf(state.selectedModule ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Module input / 模块输入
        OutlinedTextField(
            value = moduleInput,
            onValueChange = {
                moduleInput = it
                viewModel.processIntent(SmsRetrieverToolIntent.SelectModule(it))
            },
            label = { Text("Module Path / 模块路径") },
            placeholder = { Text("e.g., app/src/main/java or leave empty for all / 留空扫描全部") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Scan button / 扫描按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    viewModel.processIntent(SmsRetrieverToolIntent.StartScan(moduleInput))
                },
                enabled = state.scanStatus != ScanStatus.SCANNING,
                modifier = Modifier.weight(1f)
            ) {
                if (state.scanStatus == ScanStatus.SCANNING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (state.scanStatus == ScanStatus.SCANNING) "Scanning..." else "Start Scan / 开始扫描")
            }

            if (state.scanStatus == ScanStatus.SCANNING) {
                TextButton(
                    onClick = { viewModel.processIntent(SmsRetrieverToolIntent.CancelScan) }
                ) {
                    Text("Cancel / 取消")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress indicator / 进度指示器
        if (state.scanStatus == ScanStatus.SCANNING) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Scanned ${state.scannedFilesCount} files / 已扫描 ${state.scannedFilesCount} 个文件",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Status summary / 状态摘要
        if (state.scanStatus == ScanStatus.SUCCESS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1C1B1F))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusBadge(count = state.blockerCount, label = "Blocker", color = Color(0xFFB3261E))
                StatusBadge(count = state.warningCount, label = "Warning", color = Color(0xFFE8A317))
                StatusBadge(count = state.infoCount, label = "Passed", color = Color(0xFF386A20))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Error display / 错误显示
        if (state.scanStatus == ScanStatus.ERROR) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFB3261E)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.error ?: "Unknown error / 未知错误",
                        color = Color(0xFFB3261E)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Results list / 结果列表
        if (state.hasResults) {
            Text(
                text = "Affected Code Paths / 受影响代码路径 (${state.affectedCodePaths.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.sortedAffectedPaths) { path ->
                    AffectedCodeListItem(
                        codePath = path,
                        onClick = {
                            viewModel.processIntent(SmsRetrieverToolIntent.ViewAffectedCode(path))
                        }
                    )
                }
            }
        } else if (state.scanStatus == ScanStatus.SUCCESS) {
            // Scan completed but no results / 扫描完成但无结果
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF386A20)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "No SMS permission issues found / 未发现 SMS 权限问题",
                        color = Color(0xFF386A20),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// =============================================================
// DetailTab — 详情标签页
// =============================================================
/**
 * Detail tab content / 详情标签页内容
 *
 * Displays detailed information about a selected affected code path.
 * 显示选定受影响代码路径的详细信息。
 *
 * @param state Current UI state / 当前 UI 状态
 * @param viewModel ViewModel for processing intents / 处理意图的 ViewModel
 */
@Composable
private fun DetailTab(
    state: SmsRetrieverToolState,
    viewModel: SmsRetrieverToolViewModel
) {
    val selectedPath = state.selectedCodePath

    if (selectedPath == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select a code path to view details / 选择代码路径查看详情",
                    color = Color.Gray
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // File info / 文件信息
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = selectedPath.filePath,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF6750A4)
                )
                Text(
                    text = "Line ${selectedPath.lineNumber} / 第 ${selectedPath.lineNumber} 行",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Severity chip / 严重性标签
        SeverityChip(severity = selectedPath.severity)

        Spacer(modifier = Modifier.height(16.dp))

        // Code snippet / 代码片段
        Text(
            text = "Problematic Code / 问题代码",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeHighlightBox(code = selectedPath.codeSnippet)

        Spacer(modifier = Modifier.height(16.dp))

        // Description / 说明
        Text(
            text = "Issue Description / 问题说明",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = selectedPath.description,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Migration suggestion / 迁移建议
        Text(
            text = "Migration Suggestion / 迁移建议",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeHighlightBox(code = selectedPath.suggestedMigration)

        Spacer(modifier = Modifier.height(24.dp))

        // Migrate button / 迁移按钮
        androidx.compose.material3.Button(
            onClick = {
                viewModel.processIntent(SmsRetrieverToolIntent.StartMigration(selectedPath))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.migrationInProgress
        ) {
            if (state.migrationInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Start Migration / 开始迁移")
        }
    }
}

// =============================================================
// MigrationPreviewTab — 迁移预览标签页
// =============================================================
/**
 * Migration preview tab content / 迁移预览标签页内容
 *
 * Shows before/after diff and migration controls.
 * 显示迁移前/后对比和迁移控制。
 *
 * @param state Current UI state / 当前 UI 状态
 * @param viewModel ViewModel for processing intents / 处理意图的 ViewModel
 */
@Composable
private fun MigrationPreviewTab(
    state: SmsRetrieverToolState,
    viewModel: SmsRetrieverToolViewModel
) {
    val selectedPath = state.selectedCodePath

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (selectedPath == null || state.migrationResult == null) {
            // Show preview UI / 显示预览 UI
            if (selectedPath != null) {
                Text(
                    text = "Migration Preview / 迁移预览",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Before code / 迁移前代码
                Text(
                    text = "Before / 迁移前",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CodeHighlightBox(
                    code = selectedPath.codeSnippet,
                    backgroundColor = Color(0xFFFFEBEE)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Arrow / 箭头
                Text(
                    text = "▼",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // After code / 迁移后代码
                Text(
                    text = "After / 迁移后",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CodeHighlightBox(
                    code = selectedPath.suggestedMigration.split("\n").take(10).joinToString("\n"),
                    backgroundColor = Color(0xFFE8F5E9)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm migration button / 确认迁移按钮
                androidx.compose.material3.Button(
                    onClick = {
                        val diff = CodeDiff(
                            filePath = selectedPath.filePath,
                            beforeCode = selectedPath.codeSnippet,
                            afterCode = selectedPath.suggestedMigration,
                            diffText = "// Migration diff / 迁移差异"
                        )
                        viewModel.processIntent(SmsRetrieverToolIntent.ConfirmMigration(diff))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.migrationInProgress
                ) {
                    if (state.migrationInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Confirm Migration / 确认迁移")
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Select a code path first / 请先选择代码路径")
                }
            }
        } else {
            // Show migration result / 显示迁移结果
            MigrationResultView(result = state.migrationResult)
        }
    }
}

// =============================================================
// ComplianceReportTab — 合规报告标签页
// =============================================================
/**
 * Compliance report tab content / 合规报告标签页内容
 *
 * Displays overall compliance status and module details.
 * 显示整体合规状态和模块详情。
 *
 * @param state Current UI state / 当前 UI 状态
 * @param viewModel ViewModel for processing intents / 处理意图的 ViewModel
 */
@Composable
private fun ComplianceReportTab(
    state: SmsRetrieverToolState,
    viewModel: SmsRetrieverToolViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Generate report button / 生成报告按钮
        androidx.compose.material3.Button(
            onClick = {
                viewModel.processIntent(SmsRetrieverToolIntent.GenerateComplianceReport)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Description, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Report / 生成报告")
        }

        Spacer(modifier = Modifier.height(16.dp))

        val report = state.complianceReport
        if (report == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No report generated yet / 尚未生成报告",
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Overall status card / 整体状态卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = report.overallStatus.color.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (report.overallStatus) {
                            ComplianceStatus.COMPLIANT -> Icons.Default.Check
                            ComplianceStatus.PARTIAL -> Icons.Default.Warning
                            ComplianceStatus.NON_COMPLIANT -> Icons.Default.Error
                        },
                        contentDescription = null,
                        tint = report.overallStatus.color,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = report.overallStatus.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = report.overallStatus.color
                        )
                        Text(
                            text = "${report.totalAffectedFiles} affected files / ${report.totalAffectedFiles} 个受影响文件",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Module reports / 模块报告
            Text(
                text = "Module Details / 模块详情",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(report.moduleReports) { moduleReport ->
                    ModuleComplianceCard(report = moduleReport)
                }
            }
        }
    }
}

// =============================================================
// SettingsTab — 设置标签页
// =============================================================
/**
 * Settings tab content / 设置标签页内容
 *
 * Allows configuring target SDK, OTP regex, and ignore paths.
 * 允许配置目标 SDK、OTP 正则表达式和忽略路径。
 *
 * @param state Current UI state / 当前 UI 状态
 * @param viewModel ViewModel for processing intents / 处理意图的 ViewModel
 */
@Composable
private fun SettingsTab(
    state: SmsRetrieverToolState,
    viewModel: SmsRetrieverToolViewModel
) {
    var targetSdk by remember { mutableStateOf(state.settings.targetSdk.toString()) }
    var otpRegex by remember { mutableStateOf(state.settings.customOtpRegex) }
    var ignorePaths by remember { mutableStateOf(state.settings.ignorePaths.joinToString("\n")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Tool Settings / 工具设置",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Target SDK / 目标 SDK
        OutlinedTextField(
            value = targetSdk,
            onValueChange = { targetSdk = it },
            label = { Text("Target SDK Version / 目标 SDK 版本") },
            placeholder = { Text("37") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // OTP Regex / OTP 正则
        OutlinedTextField(
            value = otpRegex,
            onValueChange = { otpRegex = it },
            label = { Text("OTP Regex Pattern / OTP 正则表达式") },
            placeholder = { Text("\\d{4,8}") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Ignore paths / 忽略路径
        OutlinedTextField(
            value = ignorePaths,
            onValueChange = { ignorePaths = it },
            label = { Text("Ignore Paths (one per line) / 忽略路径（每行一个）") },
            placeholder = { Text("**/test/**\n**/mock/**") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save button / 保存按钮
        androidx.compose.material3.Button(
            onClick = {
                val newSettings = SmsRetrieverSettings(
                    targetSdk = targetSdk.toIntOrNull() ?: 37,
                    customOtpRegex = otpRegex,
                    ignorePaths = ignorePaths.split("\n").filter { it.isNotBlank() }
                )
                viewModel.processIntent(SmsRetrieverToolIntent.UpdateSettings(newSettings))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Settings / 保存设置")
        }
    }
}

// =============================================================
// Composable helper components / 可组合辅助组件
// =============================================================

/**
 * Status badge with count / 带计数的状态徽章
 */
@Composable
private fun StatusBadge(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

/**
 * Severity chip / 严重性标签
 */
@Composable
private fun SeverityChip(severity: Severity) {
    val backgroundColor = severity.color.copy(alpha = 0.15f)
    val textColor = severity.color

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = severity.label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * Affected code list item / 受影响代码列表项
 */
@Composable
private fun AffectedCodeListItem(
    codePath: AffectedCodePath,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1B1F)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = codePath.filePath.split("/").takeLast(2).joinToString("/"),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF6750A4),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SeverityChip(severity = codePath.severity)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Line ${codePath.lineNumber}: ${codePath.codeSnippet.take(60)}...",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Code highlight box / 代码高亮框
 */
@Composable
private fun CodeHighlightBox(
    code: String,
    backgroundColor: Color = Color(0xFF1C1B1F)
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(12.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFE0E0E0)
        )
    }
}

/**
 * Migration result view / 迁移结果视图
 */
@Composable
private fun MigrationResultView(result: MigrationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.success) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.success) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (result.success) Color(0xFF386A20) else Color(0xFFB3261E)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (result.success) "Migration Successful / 迁移成功" else "Migration Failed / 迁移失败",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (result.success) Color(0xFF386A20) else Color(0xFFB3261E)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Migrated files / 已迁移文件: ${result.migratedFiles.size}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (result.failedFiles.isNotEmpty()) {
                Text(
                    text = "Failed files / 失败文件: ${result.failedFiles.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB3261E)
                )
            }
            Text(
                text = "Build verification / 构建验证: ${if (result.buildVerification) "✅ Passed" else "❌ Failed"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Module compliance card / 模块合规卡片
 */
@Composable
private fun ModuleComplianceCard(report: ModuleComplianceReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (report.isCompliant) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.moduleName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (report.isCompliant) Icons.Default.Check else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (report.isCompliant) Color(0xFF386A20) else Color(0xFFE8A317)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Blockers: ${report.blockerCount} | Warnings: ${report.warningCount}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Primary button / 主要按钮
 */
@Composable
private fun Button(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        content()
    }
}
