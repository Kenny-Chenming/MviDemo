package com.mvi.kenny.feature.agp9migration

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Contract types are top-level in same package — no import needed
// Contract types: Agp9MigrationState, Agp9MigrationIntent, Agp9MigrationEffect + enums/data classes

// ===== Color Palette — Developer Tools Dark Theme =====
// ===== 配色方案 — 开发者工具深色主题 =====

private val DevToolsPrimary = Color(0xFF4FC3F7)       // Primary: Light blue
private val DevToolsSecondary = Color(0xFF80CBC4)     // Secondary: Teal
private val DevToolsSurface = Color(0xFF1E1E1E)        // Surface: Dark gray
private val DevToolsSurfaceVariant = Color(0xFF2D2D2D)  // Surface variant
private val SeverityP0 = Color(0xFFEF5350)            // P0: Red (blocking)
private val SeverityP1 = Color(0xFFFFA726)            // P1: Orange (warning)
private val SeverityP2 = Color(0xFF42A5F5)            // P2: Blue (suggestion)
private val SeverityNone = Color(0xFF66BB6A)           // None: Green (compliant)
private val OnSurfaceLight = Color(0xFFE0E0E0)
private val OnSurfaceDim = Color(0xFF9E9E9E)

// ===== Navigation Tabs =====
// ===== 导航标签页 =====

private enum class AGPTab(
    val label: String,
    val icon: ImageVector,
    val description: String  // Bilingual label
) {
    Dashboard("Dashboard", Icons.Default.Dashboard, "仪表盘 / 健康度总览"),
    NDKIsolator("NDK Isolator", Icons.Default.Memory, "NDK 隔离引擎"),
    Migrator("Migrator", Icons.Default.Build, "android{} 迁移"),
    Progress("Progress", Icons.Default.Timeline, "迁移进度追踪"),
    Settings("Settings", Icons.Default.Settings, "设置 / Settings")
}

// ===== Main Screen =====
// ===== 主屏幕 =====

@Composable
fun AGP9MigrationScreen(
    viewModel: Agp9MigrationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(AGPTab.Dashboard) }

    // Collect effects / 收集副作用
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Agp9MigrationEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is Agp9MigrationEffect.ReportGenerated -> {
                    snackbarHostState.showSnackbar("Report: ${effect.filePath}")
                }
                is Agp9MigrationEffect.ShowConfirmDialog -> {
                    // Handled in-dialog / 对话框内处理
                }
                is Agp9MigrationEffect.MigrationCompleted -> {
                    snackbarHostState.showSnackbar("Migration completed!")
                }
                is Agp9MigrationEffect.NavigateToModuleScan -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = DevToolsSurfaceVariant
            ) {
                AGPTab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 11.sp) },
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DevToolsPrimary,
                            selectedTextColor = DevToolsPrimary,
                            indicatorColor = DevToolsPrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DevToolsSurface)
                .padding(paddingValues)
        ) {
            // Header / 顶部标题栏
            AGP9Header()

            // Tab Content / 标签页内容
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabContent"
            ) { tab ->
                when (tab) {
                    AGPTab.Dashboard -> DashboardTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    AGPTab.NDKIsolator -> NDKIsolatorTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    AGPTab.Migrator -> MigratorTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    AGPTab.Progress -> ProgressTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    AGPTab.Settings -> SettingsTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                }
            }
        }
    }
}

// ===== Header =====
// ===== 标题栏 =====

@Composable
private fun AGP9Header() {
    Surface(
        color = DevToolsSurfaceVariant,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = DevToolsPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "AGP 9.0 Migration Toolkit",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "KMP NDK/C++ 迁移检测与自动化修复工具包",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Badge(
                    containerColor = DevToolsPrimary.copy(alpha = 0.2f),
                    contentColor = DevToolsPrimary
                ) {
                    Text("P1", fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
    }
    HorizontalDivider(color = DevToolsPrimary.copy(alpha = 0.3f), thickness = 1.dp)
}

// ===== Dashboard Tab =====
// ===== 仪表盘标签页 =====

@Composable
private fun DashboardTab(
    state: Agp9MigrationState,
    onIntent: (Agp9MigrationIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Health Score Card / 健康度卡片
        item {
            HealthScoreCard(state = state, onIntent = onIntent)
        }

        // Scan Button / 扫描按钮
        item {
            ScanControlCard(state = state, onIntent = onIntent)
        }

        // Module List / 模块列表
        if (state.moduleItems.isNotEmpty()) {
            item {
                Text(
                    text = "Module Status / 模块状态",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurfaceLight,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(state.moduleItems) { module ->
                ModuleCard(module = module, onIntent = onIntent)
            }
        }

        // P0 Blockers / P0 阻断项
        val p0Items = state.moduleItems.filter { it.severity == Severity.P0 }
        if (p0Items.isNotEmpty()) {
            item {
                Text(
                    text = "🚨 P0 Blocking Issues / P0 阻断问题",
                    style = MaterialTheme.typography.titleSmall,
                    color = SeverityP0,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(p0Items) { module ->
                ModuleCard(module = module, onIntent = onIntent, isHighPriority = true)
            }
        }
    }
}

@Composable
private fun HealthScoreCard(
    state: Agp9MigrationState,
    onIntent: (Agp9MigrationIntent) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DevToolsSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Migration Health Score / 迁移健康度",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceDim
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${state.overallHealthScore}",
                        style = MaterialTheme.typography.displayMedium,
                        color = when {
                            state.overallHealthScore >= 70 -> SeverityNone
                            state.overallHealthScore >= 40 -> SeverityP1
                            else -> SeverityP0
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnSurfaceDim,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        state.overallHealthScore >= 70 -> "Good / 状态良好"
                        state.overallHealthScore >= 40 -> "Needs attention / 需要处理"
                        else -> "Critical / 严重阻塞"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
            // Circular progress indicator / 环形进度
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { state.overallHealthScore / 100f },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 8.dp,
                    color = when {
                        state.overallHealthScore >= 70 -> SeverityNone
                        state.overallHealthScore >= 40 -> SeverityP1
                        else -> SeverityP0
                    },
                    trackColor = DevToolsSurface
                )
                Text(
                    text = "${state.moduleItems.count { it.severity != Severity.NONE }}",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceLight,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ScanControlCard(
    state: Agp9MigrationState,
    onIntent: (Agp9MigrationIntent) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DevToolsSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (state.scanStatus == ScanStatus.IDLE) "Ready to scan / 准备就绪"
                        else if (state.scanStatus == ScanStatus.SCANNING) "Scanning... / 扫描中"
                        else "Scan completed / 扫描完成",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceLight
                    )
                    if (state.scanStatus == ScanStatus.SCANNING) {
                        state.currentPhase?.let {
                            Text(
                                text = it.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceDim
                            )
                        }
                    }
                }
                if (state.scanStatus == ScanStatus.SCANNING) {
                    FilledTonalButton(
                        onClick = { onIntent(Agp9MigrationIntent.CancelScan) }
                    ) {
                        Text("Cancel / 取消")
                    }
                } else {
                    Button(
                        onClick = { onIntent(Agp9MigrationIntent.RunFullScan) },
                        colors = ButtonDefaults.buttonColors(containerColor = DevToolsPrimary)
                    ) {
                        Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run Full Scan / 运行扫描")
                    }
                }
            }
            // Scan progress bar / 扫描进度条
            if (state.scanStatus == ScanStatus.SCANNING) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { state.scanProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = DevToolsPrimary,
                    trackColor = DevToolsSurface
                )
            }
        }
    }
}

@Composable
private fun ModuleCard(
    module: ModuleItem,
    onIntent: (Agp9MigrationIntent) -> Unit,
    isHighPriority: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isHighPriority) SeverityP0.copy(alpha = 0.1f)
            else DevToolsSurfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onIntent(Agp9MigrationIntent.SelectModule(module)) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Severity badge / 严重度标签
            SeverityBadge(severity = module.severity)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = module.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (module.hasNDK) IssueChip("NDK", SeverityP0)
                    if (module.hasAndroidBlockIssue) IssueChip("android{}", SeverityP1)
                    if (module.hasKSPIssue) IssueChip("KSP", SeverityP2)
                }
            }

            // Status indicator / 状态指示器
            StatusChip(status = module.status)
        }
    }
}

// ===== NDK Isolator Tab =====
// ===== NDK 隔离引擎标签页 =====

@Composable
private fun NDKIsolatorTab(
    state: Agp9MigrationState,
    onIntent: (Agp9MigrationIntent) -> Unit
) {
    if (state.ndkIsolationItems.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.Memory,
            title = "No NDK modules detected / 未检测到 NDK 模块",
            subtitle = "Run a full scan first / 请先运行完整扫描"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "NDK/C++ modules requiring isolation",
                    style = MaterialTheme.typography.titleSmall,
                    color = SeverityP0
                )
                Text(
                    text = "KMP 模块中的 NDK/C++ 代码需要隔离到 Android-only 模块",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
            items(state.ndkIsolationItems) { item ->
                NDKIsolationCard(item = item, onIntent = onIntent)
            }
        }
    }

    // NDK Detail Dialog / NDK 详情对话框
    if (state.isNDKDialogOpen && state.selectedNDKItem != null) {
        NDKDetailDialog(
            item = state.selectedNDKItem,
            onDismiss = { onIntent(Agp9MigrationIntent.DismissNDKDialog) },
            onStartIsolation = { target -> onIntent(Agp9MigrationIntent.StartNDKIsolation(state.selectedNDKItem, target)) }
        )
    }
}

@Composable
private fun NDKIsolationCard(
    item: NDKIsolationItem,
    onIntent: (Agp9MigrationIntent) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DevToolsSurfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onIntent(Agp9MigrationIntent.SelectNDKItem(item)) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = SeverityP0,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = item.moduleName,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceLight,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Badge(containerColor = SeverityP0.copy(alpha = 0.2f), contentColor = SeverityP0) {
                    Text("P0", modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.modulePath,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${item.nativeFiles.size} native file(s) detected / 检测到 ${item.nativeFiles.size} 个原生文件",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item.nativeFiles.forEach { file ->
                    AssistChip(
                        onClick = {},
                        label = { Text(file.fileName, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = DevToolsSurface
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = { onIntent(Agp9MigrationIntent.SelectNDKItem(item)) }
                ) {
                    Text("View Details / 查看详情")
                }
            }
        }
    }
}

@Composable
private fun NDKDetailDialog(
    item: NDKIsolationItem,
    onDismiss: () -> Unit,
    onStartIsolation: (String) -> Unit
) {
    var selectedTarget by remember { mutableStateOf(item.suggestedTarget) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = SeverityP0)
                Text("NDK Isolation / NDK 隔离引擎")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Module: ${item.moduleName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight
                )
                Text(
                    text = "Native files / 原生文件:",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
                item.nativeFiles.forEach { file ->
                    Text(
                        text = "• ${file.fileName} (${file.filePath})",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceLight
                    )
                    file.calledFromKotlin.forEach { kotlinFile ->
                        Text(
                            text = "  ↳ Called from: $kotlinFile",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDim
                        )
                    }
                }
                HorizontalDivider(color = OnSurfaceDim.copy(alpha = 0.3f))
                Text(
                    text = "Suggested migration target / 建议迁移目标:",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
                OutlinedTextField(
                    value = selectedTarget,
                    onValueChange = { selectedTarget = it },
                    label = { Text("Target module / 目标模块") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onStartIsolation(selectedTarget) },
                colors = ButtonDefaults.buttonColors(containerColor = SeverityP0)
            ) {
                Text("Start Isolation / 开始隔离")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel / 取消")
            }
        }
    )
}

// ===== Migrator Tab =====
// ===== android{} 迁移标签页 =====

@Composable
private fun MigratorTab(
    state: Agp9MigrationState,
    onIntent: (Agp9MigrationIntent) -> Unit
) {
    if (state.androidBlockItems.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.Build,
            title = "No android{} issues / 没有 android{} 问题",
            subtitle = "All modules are compliant / 所有模块均已合规"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "android{} Block Migration / android{} 块迁移",
                    style = MaterialTheme.typography.titleSmall,
                    color = SeverityP1
                )
                Text(
                    text = "在 AGP 9.0 中，KMP 模块不再支持 android{} block，需要迁移到 androidApp 模块",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
            items(state.androidBlockItems) { item ->
                AndroidBlockCard(item = item, onIntent = onIntent)
            }
        }
    }

    // Android Block Dialog / android{} 详情对话框
    if (state.isAndroidBlockDialogOpen && state.selectedAndroidBlockItem != null) {
        AndroidBlockDialog(
            item = state.selectedAndroidBlockItem,
            onDismiss = { onIntent(Agp9MigrationIntent.DismissAndroidBlockDialog) },
            onApply = { onIntent(Agp9MigrationIntent.ApplyAndroidBlockMigration(state.selectedAndroidBlockItem)) }
        )
    }
}

@Composable
private fun AndroidBlockCard(
    item: AndroidBlockItem,
    onIntent: (Agp9MigrationIntent) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DevToolsSurfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onIntent(Agp9MigrationIntent.PreviewAndroidBlockMigration(item)) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = SeverityP1,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = item.sourceModule,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceLight,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Badge(containerColor = SeverityP1.copy(alpha = 0.2f), contentColor = SeverityP1) {
                    Text("P1", modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Removed / 将移除: ${item.removedProperties.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = SeverityP1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Target / 目标模块: ${item.targetModuleName}",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FilledTonalButton(
                    onClick = { onIntent(Agp9MigrationIntent.PreviewAndroidBlockMigration(item)) }
                ) {
                    Text("Preview & Apply / 预览并应用")
                }
            }
        }
    }
}

@Composable
private fun AndroidBlockDialog(
    item: AndroidBlockItem,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("android{} Migration Preview / android{} 迁移预览")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Current / 当前配置 (${item.sourceModule}):",
                    style = MaterialTheme.typography.bodySmall,
                    color = SeverityP1
                )
                Surface(
                    color = DevToolsSurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = item.currentAndroidBlock,
                        style = MaterialTheme.typography.bodySmall,
                        color = SeverityP0,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Text(
                    text = "Suggested / 建议配置:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SeverityNone
                )
                Surface(
                    color = DevToolsSurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 100.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = item.suggestedAndroidBlock,
                        style = MaterialTheme.typography.bodySmall,
                        color = SeverityNone,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Text(
                    text = "Removed properties / 将移除: ${item.removedProperties.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SeverityP1
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(containerColor = SeverityP1)
            ) {
                Text("Apply Migration / 应用迁移")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel / 取消")
            }
        }
    )
}

// ===== Progress Tab =====
// ===== 迁移进度标签页 =====

@Composable
private fun ProgressTab(
    state: Agp9MigrationState,
    onIntent: (Agp9MigrationIntent) -> Unit
) {
    val progress = state.migrationProgress

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress Summary Card / 进度摘要卡片
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DevToolsSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Migration Progress / 迁移进度",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceLight
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { progress.overallPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = DevToolsPrimary,
                        trackColor = DevToolsSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatChip("${progress.completedModules}/${progress.totalModules}", "Completed / 完成", SeverityNone)
                        StatChip("${progress.inProgressModules}", "In Progress / 进行中", SeverityP1)
                        StatChip("${progress.blockedModules}", "Blocked / 阻塞中", SeverityP0)
                    }
                }
            }
        }

        // Module Progress Table / 模块进度表格
        if (state.moduleItems.isNotEmpty()) {
            item {
                Text(
                    text = "Module Details / 模块详情",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurfaceLight
                )
            }
            items(state.moduleItems) { module ->
                ModuleProgressRow(module = module)
            }
        }
    }
}

@Composable
private fun StatChip(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceDim
        )
    }
}

@Composable
private fun ModuleProgressRow(module: ModuleItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DevToolsSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SeverityBadge(severity = module.severity)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight
                )
                Text(
                    text = module.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
            StatusChip(status = module.status)
        }
    }
}

// ===== Settings Tab =====
// ===== 设置标签页 =====

@Composable
private fun SettingsTab(
    state: Agp9MigrationState,
    onIntent: (Agp9MigrationIntent) -> Unit
) {
    var settings by remember { mutableStateOf(state.settings) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Tool Settings / 工具设置",
                style = MaterialTheme.typography.titleSmall,
                color = OnSurfaceLight
            )
        }

        // AGP Version / AGP 版本
        item {
            SettingsCard(title = "Target AGP Version / 目标 AGP 版本") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("9.0", "9.1", "9.2").forEach { version ->
                        FilterChip(
                            selected = settings.targetAGPVersion == version,
                            onClick = {
                                settings = settings.copy(targetAGPVersion = version)
                                onIntent(Agp9MigrationIntent.UpdateSettings(settings))
                            },
                            label = { Text(version) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DevToolsPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = DevToolsPrimary
                            )
                        )
                    }
                }
            }
        }

        // Kotlin Version / Kotlin 版本
        item {
            SettingsCard(title = "Target Kotlin Version / 目标 Kotlin 版本") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("2.3", "2.4").forEach { version ->
                        FilterChip(
                            selected = settings.targetKotlinVersion == version,
                            onClick = {
                                settings = settings.copy(targetKotlinVersion = version)
                                onIntent(Agp9MigrationIntent.UpdateSettings(settings))
                            },
                            label = { Text(version) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DevToolsPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = DevToolsPrimary
                            )
                        )
                    }
                }
            }
        }

        // Report Format / 报告格式
        item {
            SettingsCard(title = "Report Export Format / 报告导出格式") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportFormat.entries.forEach { format ->
                        FilterChip(
                            selected = settings.reportFormat == format,
                            onClick = {
                                settings = settings.copy(reportFormat = format)
                                onIntent(Agp9MigrationIntent.UpdateSettings(settings))
                            },
                            label = { Text(format.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DevToolsPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = DevToolsPrimary
                            )
                        )
                    }
                }
            }
        }

        // Auto Backup / 自动备份
        item {
            SettingsCard(title = "Auto Backup / 自动备份") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create git backup before migration / 迁移前创建 git 备份",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceLight
                    )
                    Switch(
                        checked = settings.autoBackup,
                        onCheckedChange = {
                            settings = settings.copy(autoBackup = it)
                            onIntent(Agp9MigrationIntent.UpdateSettings(settings))
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = DevToolsPrimary)
                    )
                }
            }
        }

        // Export Report Button / 导出报告按钮
        item {
            Button(
                onClick = { onIntent(Agp9MigrationIntent.ExportReport(settings.reportFormat)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DevToolsPrimary)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Report / 导出报告")
            }
        }
    }
}

// ===== Reusable Components =====
// ===== 可复用组件 =====

@Composable
private fun SeverityBadge(severity: Severity) {
    val (color, label) = when (severity) {
        Severity.P0 -> SeverityP0 to "P0"
        Severity.P1 -> SeverityP1 to "P1"
        Severity.P2 -> SeverityP2 to "P2"
        Severity.NONE -> SeverityNone to "✓"
    }
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusChip(status: MigrationStatus) {
    val (color, label) = when (status) {
        MigrationStatus.NOT_STARTED -> OnSurfaceDim to "Not Started / 未开始"
        MigrationStatus.IN_PROGRESS -> SeverityP1 to "In Progress / 进行中"
        MigrationStatus.COMPLETED -> SeverityNone to "Completed / 完成"
        MigrationStatus.BLOCKED -> SeverityP0 to "Blocked / 阻塞"
    }
    Badge(containerColor = color.copy(alpha = 0.2f), contentColor = color) {
        Text(label, modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp)
    }
}

@Composable
private fun IssueChip(label: String, color: Color) {
    Badge(containerColor = color.copy(alpha = 0.15f), contentColor = color) {
        Text(label, modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp)
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DevToolsSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun EmptyStateMessage(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSurfaceDim,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceLight
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
        }
    }
}
