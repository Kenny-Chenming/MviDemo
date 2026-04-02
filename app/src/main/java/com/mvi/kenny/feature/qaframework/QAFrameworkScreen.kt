package com.mvi.kenny.feature.qaframework

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrimaryColor = Color(0xFF6750A4)
private val BackgroundColor = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1C1B1F)
private val ErrorColor = Color(0xFFB3261E)
private val WarningColor = Color(0xFFF9A825)
private val SuccessColor = Color(0xFF2E7D32)
private val InfoColor = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QAFrameworkScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit = {},
    viewModel: QAFrameworkViewModel = viewModel()
) {
    val mainState by viewModel.mainState.collectAsState()
    val scanState by viewModel.scanProgressState.collectAsState()
    val reportState by viewModel.reportState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    var currentSubPage by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        launch { viewModel.mainEffect.collect { effect ->
            when (effect) {
                is MainEffect.ShowError -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
                is MainEffect.ShowSuccess -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
                is MainEffect.NavigateToScan -> { currentSubPage = 1 }
                is MainEffect.NavigateToReport -> { currentSubPage = 2 }
            }
        } }
        launch { viewModel.scanEffect.collect { effect ->
            when (effect) {
                is ScanEffect.ScanCompleted -> {
                    viewModel.sendReportIntent(ReportIntent.LoadReport(effect.taskId))
                    currentSubPage = 2
                }
                is ScanEffect.DeviceDisconnected -> scope.launch { snackbarHostState.showSnackbar("设备断开: ${effect.deviceId}") }
                is ScanEffect.ShowNotification -> { }
            }
        } }
        launch { viewModel.reportEffect.collect { effect ->
            when (effect) {
                is ReportEffect.ExportCompleted -> scope.launch { snackbarHostState.showSnackbar("报告已导出: ${effect.filePath}") }
                is ReportEffect.ExportFailed -> scope.launch { snackbarHostState.showSnackbar("导出失败: ${effect.error}") }
                is ReportEffect.TriggerShare -> scope.launch { snackbarHostState.showSnackbar("分享面板（桩实现）") }
            }
        } }
    }
    LaunchedEffect(currentSubPage) {
        val config = when (currentSubPage) {
            0 -> TopBarConfig(title = "QA 框架", actions = listOf(TopBarAction(icon = Icons.Default.BugReport, contentDescription = "设置", onClick = { currentSubPage = 4 })))
            1 -> TopBarConfig(title = "扫描进行中")
            2 -> TopBarConfig(title = "扫描报告")
            3 -> TopBarConfig(title = "报告详情")
            4 -> TopBarConfig(title = "设置")
            else -> TopBarConfig(title = "QA 框架")
        }
        onUpdateTopBar(config)
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentSubPage) {
                0 -> MainSubScreen(mainState = mainState, onIntent = { viewModel.sendMainIntent(it) })
                1 -> ScanProgressSubScreen(scanState = scanState, onIntent = { viewModel.sendScanIntent(it) })
                2 -> ReportSubScreen(reportState = reportState, onIntent = { viewModel.sendReportIntent(it) }, onNavigateToDetail = { currentSubPage = 3 }, onNavigateBack = { currentSubPage = 0 })
                3 -> ReportDetailSubScreen(reportState = reportState, onNavigateBack = { currentSubPage = 2 })
                4 -> SettingsSubScreen(settingsState = settingsState, onIntent = { viewModel.sendSettingsIntent(it) }, onNavigateBack = { currentSubPage = 0 })
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun MainSubScreen(mainState: MainState, onIntent: (MainIntent) -> Unit) {
    var localPackageName by remember(mainState.packageName) { mutableStateOf(mainState.packageName) }
    val sheetState = rememberModalBottomSheetState()
    Column(modifier = Modifier.fillMaxSize().background(BackgroundColor).verticalScroll(rememberScrollState()).padding(16.dp)) {
        ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("快速开始 / Quick Start", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(value = localPackageName, onValueChange = { localPackageName = it; onIntent(MainIntent.UpdatePackageName(it)) }, label = { Text("App 包名 / Package Name") }, placeholder = { Text("com.example.app") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Android, contentDescription = null) })
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { onIntent(MainIntent.ShowDeviceSheet) }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.DeviceHub, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(mainState.availableDevices.find { it.id == mainState.selectedDeviceId }?.name ?: "选择设备") }
                    TextButton(onClick = { onIntent(MainIntent.RefreshDevices) }) { Icon(Icons.Default.Refresh, contentDescription = "刷新设备") }
                }
                Spacer(modifier = Modifier.height(16.dp))
                FilledTonalButton(onClick = { val deviceId = mainState.selectedDeviceId; if (localPackageName.isNotBlank() && deviceId != null) { onIntent(MainIntent.StartScan(localPackageName, deviceId)) } }, modifier = Modifier.fillMaxWidth(), enabled = localPackageName.isNotBlank() && mainState.selectedDeviceId != null) { Icon(Icons.Default.PlayArrow, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("开始扫描 / Start Scan") }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("最近任务 / Recent Tasks", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        when {
            mainState.isLoading -> Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryColor) }
            mainState.recentTasks.isEmpty() -> Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceColor)) { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("暂无历史任务 / No recent tasks", color = Color.Gray) } }
            else -> mainState.recentTasks.forEach { task -> TaskItemCard(task = task, onClick = { onIntent(MainIntent.StartScan(task.packageName, task.deviceId)) }, onDelete = { onIntent(MainIntent.DeleteTask(task.id)) }); Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
    if (mainState.showDeviceSheet) {
        ModalBottomSheet(onDismissRequest = { onIntent(MainIntent.HideDeviceSheet) }, sheetState = sheetState, containerColor = SurfaceColor) {
            DeviceSelectSheet(devices = mainState.availableDevices, selectedDeviceId = mainState.selectedDeviceId, onSelectDevice = { onIntent(MainIntent.SelectDevice(it)) })
        }
    }
}

@Composable private fun TaskItemCard(task: ScanTask, onClick: () -> Unit, onDelete: () -> Unit) {
    val (icon, iconColor) = when (task.status) { ScanTaskStatus.COMPLETED -> Icons.Default.CheckCircle to SuccessColor; ScanTaskStatus.FAILED -> Icons.Default.Error to ErrorColor; ScanTaskStatus.RUNNING -> Icons.Default.PlayArrow to PrimaryColor; ScanTaskStatus.PAUSED -> Icons.Default.Pause to WarningColor; ScanTaskStatus.IDLE -> Icons.Default.BugReport to Color.Gray }
    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.packageName, style = MaterialTheme.typography.bodyLarge, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("屏幕: ${task.screenCount} | 问题: ${task.issueCount}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(formatTimestamp(task.createdAt), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Gray) }
        }
    }
}

@Composable private fun DeviceSelectSheet(devices: List<ConnectedDevice>, selectedDeviceId: String?, onSelectDevice: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("选择设备 / Select Device", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        devices.forEach { device ->
            ListItem(
                headlineContent = { Text(device.name, color = Color.White) },
                supportingContent = { Text(device.androidVersion, color = Color.Gray) },
                leadingContent = { RadioButton(selected = device.id == selectedDeviceId, onClick = { onSelectDevice(device.id) }) },
                trailingContent = { if (device.isConnected) Icon(Icons.Default.CheckCircle, contentDescription = "已连接", tint = SuccessColor) },
                modifier = Modifier.clickable { onSelectDevice(device.id) }
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable private fun ScanProgressSubScreen(scanState: ScanProgressState, onIntent: (ScanIntent) -> Unit) {
    val animatedProgress by animateFloatAsState(targetValue = scanState.progress, label = "progress")
    Column(modifier = Modifier.fillMaxSize().background(BackgroundColor).padding(16.dp)) {
        ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("扫描进度 / Progress", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("${scanState.currentScreen} / ${scanState.totalScreens}", style = MaterialTheme.typography.titleMedium, color = PrimaryColor)
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = PrimaryColor, trackColor = Color.Gray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("屏幕 ${scanState.currentScreen} / ${scanState.totalScreens} — ${(scanState.progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (scanState.isAnalyzing) { CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryColor, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)); Text("AI 分析中 / Analyzing...", style = MaterialTheme.typography.bodyMedium, color = PrimaryColor) }
                    else { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessColor); Spacer(modifier = Modifier.width(8.dp)); Text(scanState.aiAnalysisResult ?: "等待开始...", style = MaterialTheme.typography.bodyMedium, color = Color.White) }
                }
                if (scanState.issuesFound.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        scanState.issuesFound.take(3).forEach { issue ->
                            val (icon, color) = when (issue.severity) { IssueSeverity.ERROR -> Icons.Default.Error to ErrorColor; IssueSeverity.WARNING -> Icons.Default.Warning to WarningColor; IssueSeverity.INFO -> Icons.Default.Info to InfoColor }
                            Icon(imageVector = icon, contentDescription = issue.severity.name, tint = color, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                        }
                        Text("已发现 ${scanState.issuesFound.size} 个问题", style = MaterialTheme.typography.bodySmall, color = WarningColor)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ElevatedCard(modifier = Modifier.fillMaxWidth().height(200.dp), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("截图预览区域（CDP 截图接入后显示）", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("Screen ${scanState.currentScreen} preview", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (scanState.isPaused) { FilledTonalButton(onClick = { onIntent(ScanIntent.ResumeScan) }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.PlayArrow, contentDescription = null); Spacer(modifier = Modifier.width(4.dp)); Text("继续 / Resume") } }
            else { FilledTonalButton(onClick = { onIntent(ScanIntent.PauseScan) }, modifier = Modifier.weight(1f), enabled = !scanState.isAnalyzing) { Icon(Icons.Default.Pause, contentDescription = null); Spacer(modifier = Modifier.width(4.dp)); Text("暂停 / Pause") } }
            FilledTonalButton(onClick = { onIntent(ScanIntent.CancelScan) }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Close, contentDescription = null, tint = ErrorColor); Spacer(modifier = Modifier.width(4.dp)); Text("取消 / Cancel", color = ErrorColor) }
        }
    }
}

@Composable private fun ReportSubScreen(reportState: ReportState, onIntent: (ReportIntent) -> Unit, onNavigateToDetail: () -> Unit, onNavigateBack: () -> Unit) {
    var expandedIssueId by remember { mutableStateOf<String?>(null) }
    val summary = reportState.summary
    Column(modifier = Modifier.fillMaxSize().background(BackgroundColor).padding(16.dp)) {
        when {
            reportState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryColor) }
            summary == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.BugReport, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp)); Spacer(modifier = Modifier.height(16.dp)); Text("暂无报告 / No reports", color = Color.Gray); Spacer(modifier = Modifier.height(16.dp)); FilledTonalButton(onClick = onNavigateBack) { Text("返回 / Go Back") } } }
            else -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("报告摘要 / Summary", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem("${summary.totalScreens}", "总屏幕数", PrimaryColor)
                            StatItem("${summary.passedScreens}", "通过", SuccessColor)
                            StatItem("${summary.errorCount}", "错误", ErrorColor)
                            StatItem("${summary.warningCount}", "警告", WarningColor)
                            StatItem("${summary.infoCount}", "提示", InfoColor)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("问题列表 / Issues (${reportState.issues.size})", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reportState.issues, key = { it.id }) { issue -> IssueCard(issue = issue, isExpanded = expandedIssueId == issue.id, onToggle = { expandedIssueId = if (expandedIssueId == issue.id) null else issue.id }, onViewDetail = onNavigateToDetail) }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
    if (summary != null) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
            LargeFloatingActionButton(onClick = { onIntent(ReportIntent.ExportReport(ExportFormat.PDF)) }, containerColor = PrimaryColor) {
                if (reportState.isExporting) { CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) } else { Icon(Icons.Default.Share, contentDescription = "导出报告", tint = Color.White) }
            }
        }
    }
}

@Composable private fun StatItem(value: String, label: String, color: Color) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold); Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray) } }

@Composable private fun IssueCard(issue: QAIssue, isExpanded: Boolean, onToggle: () -> Unit, onViewDetail: () -> Unit) {
    val (icon, color) = when (issue.severity) { IssueSeverity.ERROR -> Icons.Default.Error to ErrorColor; IssueSeverity.WARNING -> Icons.Default.Warning to WarningColor; IssueSeverity.INFO -> Icons.Default.Info to InfoColor }
    ElevatedCard(modifier = Modifier.fillMaxWidth().animateContentSize().clickable(onClick = onToggle), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("屏幕 ${issue.screenIndex + 1}", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(issue.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = if (isExpanded) Int.MAX_VALUE else 2, overflow = TextOverflow.Ellipsis)
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    issue.suggestions.forEach { suggestion -> Row(modifier = Modifier.padding(vertical = 2.dp)) { Text("• ", color = SuccessColor); Text(suggestion, style = MaterialTheme.typography.bodySmall, color = Color.White) } }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onViewDetail) { Text("查看详情 / View Detail") }
                }
            }
        }
    }
}

@Composable private fun ReportDetailSubScreen(reportState: ReportState, onNavigateBack: () -> Unit) {
    val selectedIssue = reportState.issues.firstOrNull()
    Column(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White) }; Text(selectedIssue?.let { "屏幕 ${it.screenIndex + 1} 详情" } ?: "报告详情", style = MaterialTheme.typography.titleMedium, color = Color.White) }
        if (selectedIssue == null) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("无详情数据 / No details", color = Color.Gray) } }
        else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                OutlinedCard(modifier = Modifier.fillMaxWidth().height(240.dp)) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp)); Spacer(modifier = Modifier.height(8.dp)); Text("截图预览（CDP 截图接入后显示）", style = MaterialTheme.typography.bodySmall, color = Color.Gray) } } }
                Spacer(modifier = Modifier.height(16.dp))
                val (icon, color, label) = when (selectedIssue.severity) { IssueSeverity.ERROR -> Triple(Icons.Default.Error, ErrorColor, "严重错误"); IssueSeverity.WARNING -> Triple(Icons.Default.Warning, WarningColor, "警告"); IssueSeverity.INFO -> Triple(Icons.Default.Info, InfoColor, "信息提示") }
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(label, style = MaterialTheme.typography.labelMedium, color = color) }
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) { Column(modifier = Modifier.padding(16.dp)) { Text("AI 分析 / AI Analysis", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp)); Text(selectedIssue.description, style = MaterialTheme.typography.bodyMedium, color = Color.White) } }
                Spacer(modifier = Modifier.height(16.dp))
                ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) { Column(modifier = Modifier.padding(16.dp)) { Text("操作建议 / Suggestions", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp)); selectedIssue.suggestions.forEachIndexed { index, suggestion -> Row(modifier = Modifier.padding(vertical = 4.dp)) { Text("${index + 1}. ", style = MaterialTheme.typography.bodyMedium, color = SuccessColor); Text(suggestion, style = MaterialTheme.typography.bodyMedium, color = Color.White) } } } }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SettingsSubScreen(settingsState: SettingsState, onIntent: (SettingsIntent) -> Unit, onNavigateBack: () -> Unit) {
    var cdpHostText by remember(settingsState.cdpHost) { mutableStateOf(settingsState.cdpHost) }
    var cdpPortText by remember(settingsState.cdpPort) { mutableStateOf(settingsState.cdpPort.toString()) }
    var showAIModelMenu by remember { mutableStateOf(false) }
    var showExportFormatMenu by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().background(BackgroundColor).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White) }; Spacer(modifier = Modifier.width(4.dp)); Text("设置 / Settings", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(16.dp))
        ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("CDP 连接配置 / CDP Connection", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = cdpHostText, onValueChange = { cdpHostText = it }, label = { Text("主机地址 / Host") }, placeholder = { Text("127.0.0.1") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = cdpPortText, onValueChange = { cdpPortText = it.filter { c -> c.isDigit() } }, label = { Text("端口 / Port") }, placeholder = { Text("9222") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(onClick = { val host = cdpHostText.ifBlank { "127.0.0.1" }; val port = cdpPortText.toIntOrNull() ?: 9222; onIntent(SettingsIntent.UpdateCdpHost(host)); onIntent(SettingsIntent.UpdateCdpPort(port)) }, modifier = Modifier.align(Alignment.End)) { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("保存 / Save") }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("AI 模型 / AI Model", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Box {
                    ListItem(
                        headlineContent = { Text("模型类型 / Model Type", color = Color.White) },
                        supportingContent = { Text(when (settingsState.aiModel) { AIModel.LOCAL -> "本地模型（Google ML Kit / Llama）"; AIModel.CLOUD -> "云端模型（OpenAI Vision API）" }, color = Color.Gray) },
                        trailingContent = { IconButton(onClick = { showAIModelMenu = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "展开", tint = Color.White) } },
                        modifier = Modifier.clickable { showAIModelMenu = true }
                    )
                    DropdownMenu(expanded = showAIModelMenu, onDismissRequest = { showAIModelMenu = false }) {
                        DropdownMenuItem(text = { Text("本地模型（Google ML Kit / Llama）") }, onClick = { onIntent(SettingsIntent.UpdateAIModel(AIModel.LOCAL)); showAIModelMenu = false })
                        DropdownMenuItem(text = { Text("云端模型（OpenAI Vision API）") }, onClick = { onIntent(SettingsIntent.UpdateAIModel(AIModel.CLOUD)); showAIModelMenu = false })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("并行设备数 / Parallel Devices", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text("当前值: ${settingsState.parallelDevices} 台", style = MaterialTheme.typography.bodyMedium, color = PrimaryColor)
                Slider(value = settingsState.parallelDevices.toFloat(), onValueChange = { onIntent(SettingsIntent.UpdateParallelDevices(it.toInt())) }, valueRange = 1f..5f, steps = 3, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("1", style = MaterialTheme.typography.labelSmall, color = Color.Gray); Text("5", style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("截图质量 / Screenshot Quality", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text("当前值: ${settingsState.screenshotQuality}%", style = MaterialTheme.typography.bodyMedium, color = PrimaryColor)
                Slider(value = settingsState.screenshotQuality.toFloat(), onValueChange = { onIntent(SettingsIntent.UpdateScreenshotQuality(it.toInt())) }, valueRange = 10f..100f, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("10%", style = MaterialTheme.typography.labelSmall, color = Color.Gray); Text("100%", style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = SurfaceColor)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("导出格式 / Export Format", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Box {
                    ListItem(
                        headlineContent = { Text("当前格式 / Current Format", color = Color.White) },
                        supportingContent = { Text(when (settingsState.exportFormat) { ExportFormat.PDF -> "PDF 文档"; ExportFormat.JSON -> "JSON 数据" }, color = Color.Gray) },
                        trailingContent = { IconButton(onClick = { showExportFormatMenu = true }) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "展开", tint = Color.White) } },
                        modifier = Modifier.clickable { showExportFormatMenu = true }
                    )
                    DropdownMenu(expanded = showExportFormatMenu, onDismissRequest = { showExportFormatMenu = false }) {
                        DropdownMenuItem(text = { Text("PDF 文档") }, onClick = { onIntent(SettingsIntent.UpdateExportFormat(ExportFormat.PDF)); showExportFormatMenu = false })
                        DropdownMenuItem(text = { Text("JSON 数据") }, onClick = { onIntent(SettingsIntent.UpdateExportFormat(ExportFormat.JSON)); showExportFormatMenu = false })
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
