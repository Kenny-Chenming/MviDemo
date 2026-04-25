package com.mvi.kenny.feature.lannetwork

import androidx.compose.animation.AnimatedVisibility
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// LanNetworkScreen — Android 17 ACCESS_LOCAL_NETWORK 主界面
// ================================================================
// PRD-159: Android 17 ACCESS_LOCAL_NETWORK 权限合规检测与迁移工具包
//
// 功能模块 / Feature Modules:
//   1. 局域网访问路径扫描结果列表
//   2. 权限声明生成器（Manifest Diff）
//   3. 运行时权限请求引导
//   4. 降级处理模板
//   5. CI 合规检测
//
// @see LanNetworkContract MVI contract
// @see LanNetworkViewModel ViewModel
// ================================================================

// =============================================================
// Tab 定义
// =============================================================
private enum class LanNetworkTab(val title: String, val titleZh: String) {
    SCAN("Scan", "扫描"),
    MIGRATE("Migrate", "迁移"),
    CI("CI", "CI 检测")
}

// =============================================================
// LanNetworkScreen — 主入口 Composable
// =============================================================
@Composable
fun LanNetworkScreen(
    viewModel: LanNetworkViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    // Listen for effects / 监听副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LanNetworkEffect.ShowToast -> {
                    // Toast handled by SnackbarHost in parent
                }
                is LanNetworkEffect.ShowError -> {
                    // Error handled by SnackbarHost in parent
                }
                is LanNetworkEffect.ScanComplete -> {
                    selectedTab = 1 // Jump to Migrate tab after scan
                }
                else -> {}
            }
        }
    }

    // Update parent TopBar / 更新父级 TopBar
    LaunchedEffect(selectedTab, state.scanStatus) {
        onUpdateTopBar(
            TopBarConfig(
                title = "LAN Permission · 局域网权限",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "重新扫描",
                        onClick = { viewModel.sendIntent(LanNetworkIntent.StartScan) }
                    )
                )
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            LanNetworkTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tab.title)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                tab.titleZh,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> ScanTab(
                state = state,
                onIntent = viewModel::sendIntent
            )
            1 -> MigrateTab(
                state = state,
                onIntent = viewModel::sendIntent
            )
            2 -> CiTab(
                state = state,
                onIntent = viewModel::sendIntent
            )
        }
    }

    // Path Detail Bottom Sheet / 路径详情弹窗
    state.selectedPath?.let { path ->
        PathDetailDialog(
            path = path,
            onDismiss = { viewModel.sendIntent(LanNetworkIntent.ClearSelectedPath) }
        )
    }
}

// =============================================================
// ScanTab — 扫描结果 Tab
// =============================================================
@Composable
private fun ScanTab(
    state: LanNetworkState,
    onIntent: (LanNetworkIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Project Path Input / 项目路径输入
        ProjectPathCard(
            selectedPath = state.selectedProjectPath,
            onSelectProject = { path -> onIntent(LanNetworkIntent.SelectProject(path)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scan Button / 扫描按钮
        ScanButtonCard(
            scanStatus = state.scanStatus,
            scanProgress = state.scanProgress,
            hasProject = state.selectedProjectPath != null,
            onStartScan = { onIntent(LanNetworkIntent.StartScan) },
            onCancelScan = { onIntent(LanNetworkIntent.CancelScan) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scan Results / 扫描结果
        if (state.scanStatus == ScanStatus.DONE && state.report != null) {
            ReportSummaryCard(report = state.report!!)

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips / 过滤标签
            PathFilterChips(
                accessPaths = state.accessPaths,
                onIntent = onIntent
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Path List / 路径列表
            AccessPathList(
                accessPaths = state.accessPaths,
                onPathClick = { pathId -> onIntent(LanNetworkIntent.SelectPath(pathId)) }
            )
        } else if (state.scanStatus == ScanStatus.IDLE) {
            EmptyScanPlaceholder()
        }
    }
}

// =============================================================
// MigrateTab — 迁移引导 Tab
// =============================================================
@Composable
private fun MigrateTab(
    state: LanNetworkState,
    onIntent: (LanNetworkIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (state.report == null) {
            NoScanResultPlaceholder()
        } else {
            // Manifest Diff Section / Manifest 变更区
            ManifestDiffSection(
                diffs = state.pendingDiff,
                generateStatus = state.generateStatus,
                onGenerate = { onIntent(LanNetworkIntent.GenerateManifestDiff) },
                onApply = { onIntent(LanNetworkIntent.ApplyManifestDiff) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Permission Request Template / 运行时权限请求模板
            RuntimePermissionTemplate(
                hasPermission = state.report!!.nonCompliantPaths == 0
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Degradation Template / 降级处理模板
            DegradationTemplate(hasNonCompliant = state.report!!.nonCompliantPaths > 0)
        }
    }
}

// =============================================================
// CiTab — CI 合规检测 Tab
// =============================================================
@Composable
private fun CiTab(
    state: LanNetworkState,
    onIntent: (LanNetworkIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "CI / CD 合规检测",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "无 UI 模式，输出结构化合规报告（JSON/Markdown/HTML）",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Report Format Selector / 报告格式选择
        ReportFormatSection(
            format = state.reportFormat,
            onFormatChange = { onIntent(LanNetworkIntent.SetReportFormat(it)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Export Button / 导出按钮
        ExportButton(
            format = state.reportFormat,
            isExporting = state.isExporting,
            hasReport = state.report != null,
            onExport = { onIntent(LanNetworkIntent.ExportReport) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // CI Command Reference / CI 命令参考
        CiCommandReference(
            projectPath = state.selectedProjectPath ?: "/path/to/project",
            reportFormat = state.reportFormat
        )
    }
}

// =============================================================
// ProjectPathCard — 项目路径输入卡片
// =============================================================
@Composable
private fun ProjectPathCard(
    selectedPath: String?,
    onSelectProject: (String) -> Unit
) {
    var textFieldValue by remember(selectedPath) {
        mutableStateOf(selectedPath ?: "")
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.FileOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "项目路径 / Project Path",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("选择或输入 Android 项目根目录") },
                    singleLine = true,
                    trailingIcon = {
                        if (selectedPath != null) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "已选择",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (textFieldValue.isNotBlank()) {
                            onSelectProject(textFieldValue)
                        }
                    }
                ) {
                    Text("确定")
                }
            }
        }
    }
}

// =============================================================
// ScanButtonCard — 扫描控制卡片
// =============================================================
@Composable
private fun ScanButtonCard(
    scanStatus: ScanStatus,
    scanProgress: Float,
    hasProject: Boolean,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "权限扫描 / Permission Scan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                when (scanStatus) {
                    ScanStatus.IDLE -> {
                        Button(
                            onClick = onStartScan,
                            enabled = hasProject
                        ) {
                            Text(text = if (hasProject) "开始扫描" else "请先选择项目")
                        }
                    }
                    ScanStatus.SCANNING -> {
                        Button(
                            onClick = onCancelScan,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("取消")
                        }
                    }
                    ScanStatus.DONE -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("完成", color = Color(0xFF4CAF50))
                        }
                    }
                    ScanStatus.ERROR -> {
                        Text("扫描出错", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Progress Bar / 进度条
            AnimatedVisibility(visible = scanStatus == ScanStatus.SCANNING) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    val animatedProgress by animateFloatAsState(
                        targetValue = scanProgress,
                        label = "scan_progress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${(scanProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Hint / 提示
            if (scanStatus == ScanStatus.IDLE && !hasProject) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "请先选择项目路径以开始扫描",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

// =============================================================
// ReportSummaryCard — 合规报告摘要卡片
// =============================================================
@Composable
private fun ReportSummaryCard(report: ComplianceReport) {
    val complianceColor = when {
        report.complianceRate >= 0.8f -> Color(0xFF4CAF50)
        report.complianceRate >= 0.5f -> Color(0xFFFF9800)
        else -> Color(0xFFE53935)
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = complianceColor.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "合规报告摘要 / Compliance Summary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "targetSDK ${report.targetSdk} · ${if (report.requiresPermission) "需要声明权限" else "暂不需要权限"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    report.compliancePercent,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = complianceColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = report.totalPaths.toString(),
                    label = "总路径",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    value = report.compliantPaths.toString(),
                    label = "合规",
                    color = Color(0xFF4CAF50)
                )
                StatItem(
                    value = report.nonCompliantPaths.toString(),
                    label = "不合规",
                    color = Color(0xFFE53935)
                )
                StatItem(
                    value = report.unknownPaths.toString(),
                    label = "未知",
                    color = Color(0xFF9E9E9E)
                )
            }

            // Missing permissions / 缺失权限
            if (report.missingPermissions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE53935).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "缺失权限: ${report.missingPermissions.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE53935)
                    )
                }
            }
        }
    }
}

// =============================================================
// StatItem — 统计单项
// =============================================================
@Composable
private fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =============================================================
// PathFilterChips — 路径过滤标签
// =============================================================
@Composable
private fun PathFilterChips(
    accessPaths: List<LanAccessPath>,
    onIntent: (LanNetworkIntent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = false,
            onClick = { },
            label = {
                Text("全部 ${accessPaths.size}")
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        FilterChip(
            selected = false,
            onClick = { },
            label = {
                Text("不合规 ${accessPaths.count { it.hasPermission == false }}")
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFE53935).copy(alpha = 0.2f)
            )
        )
        FilterChip(
            selected = false,
            onClick = { },
            label = {
                Text("合规 ${accessPaths.count { it.hasPermission == true }}")
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
            )
        )
    }
}

// =============================================================
// AccessPathList — 访问路径列表
// =============================================================
@Composable
private fun AccessPathList(
    accessPaths: List<LanAccessPath>,
    onPathClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(accessPaths, key = { it.id }) { path ->
            AccessPathCard(path = path, onClick = { onPathClick(path.id) })
        }
    }
}

// =============================================================
// AccessPathCard — 单条访问路径卡片
// =============================================================
@Composable
private fun AccessPathCard(
    path: LanAccessPath,
    onClick: () -> Unit
) {
    val statusColor = when (path.hasPermission) {
        true -> Color(0xFF4CAF50)
        false -> Color(0xFFE53935)
        null -> Color(0xFF9E9E9E)
    }
    val statusIcon = when (path.hasPermission) {
        true -> Icons.Default.CheckCircle
        false -> Icons.Default.Error
        null -> Icons.Default.Help
    }
    val statusText = when (path.hasPermission) {
        true -> "合规"
        false -> "不合规"
        null -> "未知"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor
                    )
                }
                Text(
                    path.accessType.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                path.file,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Line ${path.line} · ${path.method}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    path.targetIpRange,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

// =============================================================
// PathDetailDialog — 路径详情弹窗
// =============================================================
@Composable
private fun PathDetailDialog(
    path: LanAccessPath,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "路径详情 / Path Detail",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // File & Line
                DetailRow("文件", path.file)
                DetailRow("行号", "Line ${path.line}")
                DetailRow("方法", path.method)
                DetailRow("地址段", path.targetIpRange)
                DetailRow("类型", path.accessType.displayName)

                val permissionStatus = when (path.hasPermission) {
                    true -> "✅ 已声明"
                    false -> "🚫 未声明"
                    null -> "❓ 未知"
                }
                DetailRow("权限状态", permissionStatus)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Code Snippet
                Text(
                    "代码片段 / Code Snippet",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF1E1E1E),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        path.snippet,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFD4D4D4)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Suggestion
                Text(
                    "修复建议 / Fix Suggestion",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    path.suggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =============================================================
// DetailRow — 详情行
// =============================================================
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// =============================================================
// ManifestDiffSection — Manifest 变更区
// =============================================================
@Composable
private fun ManifestDiffSection(
    diffs: List<ManifestDiff>,
    generateStatus: GenerateStatus,
    onGenerate: () -> Unit,
    onApply: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Manifest 权限声明生成器",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (generateStatus) {
                GenerateStatus.IDLE -> {
                    Text(
                        "根据扫描结果生成 AndroidManifest.xml 变更 Diff",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onGenerate) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("生成 Diff")
                    }
                }
                GenerateStatus.READY -> {
                    diffs.forEach { diff ->
                        DiffItem(diff = diff)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { }) {
                            Text("复制到剪贴板")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onApply) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("应用变更")
                        }
                    }
                }
                GenerateStatus.APPLIED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Manifest 已更新，请刷新项目",
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// DiffItem — Diff 变更项
// =============================================================
@Composable
private fun DiffItem(diff: ManifestDiff) {
    val actionColor = if (diff.action == ManifestDiff.DiffAction.ADD) Color(0xFF4CAF50) else Color(0xFFE53935)
    val actionText = if (diff.action == ManifestDiff.DiffAction.ADD) "+" else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                actionColor.copy(alpha = 0.1f),
                RoundedCornerShape(6.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            actionText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = actionColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                diff.permission,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
            Text(
                diff.reason,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================
// RuntimePermissionTemplate — 运行时权限请求模板
// =============================================================
@Composable
private fun RuntimePermissionTemplate(hasPermission: Boolean) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF6750A4)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "运行时权限请求模板",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (hasPermission) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "所有路径已声明权限，无需运行时请求引导",
                        color = Color(0xFF4CAF50)
                    )
                }
            } else {
                // Permission request code template / 权限请求代码模板
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        """
                        |// 1. 检查权限 / Check permission
                        |val permission = Manifest.permission.ACCESS_LOCAL_NETWORK
                        |if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        |    // 2. 请求权限 / Request permission
                        |    requestPermissions(arrayOf(permission), REQUEST_CODE_LOCAL_NETWORK)
                        |}
                        |
                        |// 3. 处理结果 / Handle result
                        |override fun onRequestPermissionsResult(...) {
                        |    if (granted == PermissionChecker.PERMISSION_GRANTED) {
                        |        // 权限获取成功，可访问局域网
                        |    } else {
                        |        // 权限被拒绝，使用降级策略
                        |        showDegradedExperience()
                        |    }
                        |}
                        """.trimMargin(),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFD4D4D4)
                    )
                }
            }
        }
    }
}

// =============================================================
// DegradationTemplate — 降级处理模板
// =============================================================
@Composable
private fun DegradationTemplate(hasNonCompliant: Boolean) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "降级处理模板 / Degradation Template",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "当用户拒绝权限时，优雅降级而非崩溃",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    """
                    |// 降级策略 / Degradation Strategy
                    |sealed class NetworkResult<out T> {
                    |    data class Success<T>(val data: T) : NetworkResult<T>()
                    |    data class Error(val cause: Throwable) : NetworkResult<Nothing>()
                    |    object PermissionDenied : NetworkResult<Nothing>()
                    |}
                    |
                    |suspend fun accessLocalNetwork(): NetworkResult<ByteArray> {
                    |    if (!hasLocalNetworkPermission()) {
                    |        return NetworkResult.PermissionDenied
                    |    }
                    |    return try {
                    |        val data = socket.read()
                    |        NetworkResult.Success(data)
                    |    } catch (e: Exception) {
                    |        NetworkResult.Error(e)
                    |    }
                    |}
                    """.trimMargin(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFD4D4D4)
                )
            }
        }
    }
}

// =============================================================
// EmptyScanPlaceholder — 空扫描占位符
// =============================================================
@Composable
private fun EmptyScanPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "暂未扫描",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "请先选择项目并开始扫描",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// =============================================================
// NoScanResultPlaceholder — 无扫描结果占位符
// =============================================================
@Composable
private fun NoScanResultPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "请先完成扫描",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "切换到「扫描」Tab 开始权限检测",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// =============================================================
// ReportFormatSection — 报告格式选择区
// =============================================================
@Composable
private fun ReportFormatSection(
    format: ReportFormat,
    onFormatChange: (ReportFormat) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "报告格式 / Report Format",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ReportFormat.entries.forEachIndexed { index, f ->
                    SegmentedButton(
                        selected = format == f,
                        onClick = { onFormatChange(f) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ReportFormat.entries.size
                        )
                    ) {
                        Text(f.label)
                    }
                }
            }
        }
    }
}

// =============================================================
// ExportButton — 导出报告按钮
// =============================================================
@Composable
private fun ExportButton(
    format: ReportFormat,
    isExporting: Boolean,
    hasReport: Boolean,
    onExport: () -> Unit
) {
    Button(
        onClick = onExport,
        enabled = hasReport && !isExporting,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isExporting) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("导出中...")
        } else {
            Icon(Icons.Default.Description, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("导出 ${format.label} 报告")
        }
    }
}

// =============================================================
// CiCommandReference — CI 命令参考
// =============================================================
@Composable
private fun CiCommandReference(projectPath: String, reportFormat: ReportFormat) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "CI / CD 命令参考",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    CommandItem(
                        label = "Gradle Plugin (构建时自动检测)",
                        command = "./gradlew appAnalyzeLocalNetworkPermissions"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CommandItem(
                        label = "CLI 扫描",
                        command = "lan-permission-scanner scan \\\n  --project-path $projectPath \\\n  --output report.${reportFormat.extension}"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CommandItem(
                        label = "CLI 生成 Diff",
                        command = "lan-permission-scanner generate \\\n  --input report.json \\\n  --output-path app/src/main/AndroidManifest.xml"
                    )
                }
            }
        }
    }
}

// =============================================================
// CommandItem — CI 命令单项
// =============================================================
@Composable
private fun CommandItem(label: String, command: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9E9E9E)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            command,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFD4D4D4)
        )
    }
}