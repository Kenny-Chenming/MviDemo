package com.mvi.kenny.feature.messagequeue

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ===== Color Palette — MessageQueue Reflection Toolkit Theme =====
// ===== 配色方案 — MessageQueue 反射工具包主题 =====

private val MQPrimary = Color(0xFFEF5350)              // Red — MessageQueue danger association
private val MQSecondary = Color(0xFFFFCA28)            // Amber — Warning
private val MQSurface = Color(0xFF1A1A1A)             // Dark surface
private val MQSurfaceVariant = Color(0xFF2D2D2D)        // Surface variant
private val SeverityCrash = Color(0xFFEF5350)           // Red — Direct crash
private val SeverityAnomaly = Color(0xFFFFCA28)        // Amber — Behavior anomaly
private val SeveritySafe = Color(0xFF66BB6A)            // Green — Safe
private val OnSurfaceLight = Color(0xFFE0E0E0)
private val OnSurfaceDim = Color(0xFF9E9E9E)
private val CodeBackground = Color(0xFF0D1117)          // GitHub dark code bg

// ===== Severity Color Mapper =====
// ===== 严重级别颜色映射 =====

@Composable
private fun SeverityColor(severity: Severity): Color = when (severity) {
    Severity.CRASH -> SeverityCrash
    Severity.BEHAVIOR_ANOMALY -> SeverityAnomaly
    Severity.SAFE -> SeveritySafe
}

// ===== Main Screen =====
// ===== 主屏幕 =====

@Composable
fun MessageQueueScreen(
    viewModel: MessageQueueViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Collect effects / 收集副作用
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MessageQueueEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = if (effect.isError) SnackbarDuration.Long else SnackbarDuration.Short
                    )
                }
                is MessageQueueEffect.ReportGenerated -> {
                    snackbarHostState.showSnackbar("Report: ${effect.filePath}")
                }
                is MessageQueueEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is MessageQueueEffect.ShareFile -> {
                    snackbarHostState.showSnackbar("Share: ${effect.filePath}")
                }
                is MessageQueueEffect.MigrationCompleted -> {
                    snackbarHostState.showSnackbar("All migrations completed!")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row / 标签页行
            MQTabRow(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.processIntent(MessageQueueIntent.SelectTab(it)) }
            )

            // Content / 内容
            Box(modifier = Modifier.fillMaxSize()) {
                when (state.selectedTab) {
                    MessageQueueTab.DASHBOARD -> DashboardContent(
                        state = state,
                        onStartScan = { viewModel.processIntent(MessageQueueIntent.StartScan) }
                    )
                    MessageQueueTab.SCANNER -> ScannerContent(
                        state = state,
                        viewModel = viewModel,
                        onSelectAccess = { viewModel.processIntent(MessageQueueIntent.SelectAccess(it)) }
                    )
                    MessageQueueTab.IMPACT -> ImpactContent(
                        state = state,
                        onSelectAccess = { viewModel.processIntent(MessageQueueIntent.SelectAccess(it)) }
                    )
                    MessageQueueTab.KNOWLEDGE -> KnowledgeContent(
                        state = state,
                        viewModel = viewModel
                    )
                    MessageQueueTab.MIGRATION -> MigrationContent(
                        state = state,
                        viewModel = viewModel
                    )
                    MessageQueueTab.TESTS -> TestsContent(
                        state = state,
                        viewModel = viewModel
                    )
                    MessageQueueTab.SETTINGS -> SettingsContent(
                        state = state,
                        onUpdateSettings = { viewModel.processIntent(MessageQueueIntent.UpdateSettings(it)) },
                        onExportReport = { viewModel.processIntent(MessageQueueIntent.ExportReport(it)) }
                    )
                }
            }
        }
    }

    // Detail Bottom Sheet / 详情底部弹窗
    if (state.isDetailSheetOpen && state.selectedAccess != null) {
        DetailBottomSheet(
            access = state.selectedAccess!!,
            onDismiss = { viewModel.processIntent(MessageQueueIntent.DismissDetail) }
        )
    }
}

// ===== Tab Row =====
// ===== 标签页行 =====

@Composable
private fun MQTabRow(
    selectedTab: MessageQueueTab,
    onTabSelected: (MessageQueueTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = MessageQueueTab.entries.indexOf(selectedTab),
        edgePadding = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        MessageQueueTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.label,
                        fontSize = 12.sp
                    )
                },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            MessageQueueTab.DASHBOARD -> Icons.Default.Dashboard
                            MessageQueueTab.SCANNER -> Icons.Default.Search
                            MessageQueueTab.IMPACT -> Icons.Default.Warning
                            MessageQueueTab.KNOWLEDGE -> Icons.Default.MenuBook
                            MessageQueueTab.MIGRATION -> Icons.Default.AutoFixHigh
                            MessageQueueTab.TESTS -> Icons.Default.Science
                            MessageQueueTab.SETTINGS -> Icons.Default.Settings
                        },
                        contentDescription = tab.labelZh,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

// ===== Dashboard Content =====
// ===== 仪表盘内容 =====

@Composable
private fun DashboardContent(
    state: MessageQueueState,
    onStartScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Health Score Card / 健康度卡片
        item {
            HealthScoreCard(state = state, onStartScan = onStartScan)
        }

        // Scan Progress / 扫描进度
        if (state.scanStatus == ScanStatus.SCANNING) {
            item {
                ScanProgressCard(state = state)
            }
        }

        // Impact Summary Cards / 影响摘要卡片
        item {
            Text(
                text = "Impact Summary / 影响摘要",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceLight
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImpactMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Crash / 直接崩溃",
                    count = state.impactSummary.crashCount,
                    color = SeverityCrash
                )
                ImpactMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Anomaly / 行为异常",
                    count = state.impactSummary.anomalyCount,
                    color = SeverityAnomaly
                )
                ImpactMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Safe / 安全",
                    count = state.impactSummary.safeCount,
                    color = SeveritySafe
                )
            }
        }

        // Top Risk Paths / 高风险路径
        if (state.impactSummary.topRiskPaths.isNotEmpty()) {
            item {
                Text(
                    text = "Top Risk Paths / 高风险路径",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceLight
                )
            }

            items(state.impactSummary.topRiskPaths.take(5)) { access ->
                RiskPathCard(access = access)
            }
        }

        // Scan Results Summary / 扫描结果摘要
        if (state.scanStatus == ScanStatus.COMPLETED) {
            item {
                ScanSummaryCard(state = state)
            }
        }
    }
}

@Composable
private fun HealthScoreCard(
    state: MessageQueueState,
    onStartScan: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MQSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MessageQueue Health / 消息队列健康度",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceLight
                    )
                    Text(
                        text = if (state.scanStatus == ScanStatus.IDLE) "Run scan to detect / 运行扫描以检测"
                        else "Android 17 Lock-Free Ready / Android 17 无锁兼容",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim
                    )
                }

                // Circular health indicator / 环形健康度指示
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                state.impactSummary.healthScore >= 80 -> SeveritySafe
                                state.impactSummary.healthScore >= 50 -> SeverityAnomaly
                                else -> SeverityCrash
                            }.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${state.impactSummary.healthScore}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            state.impactSummary.healthScore >= 80 -> SeveritySafe
                            state.impactSummary.healthScore >= 50 -> SeverityAnomaly
                            else -> SeverityCrash
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartScan,
                enabled = state.scanStatus != ScanStatus.SCANNING,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MQPrimary)
            ) {
                if (state.scanStatus == ScanStatus.SCANNING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scanning... / 扫描中...")
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Scan / 开始扫描")
                }
            }
        }
    }
}

@Composable
private fun ScanProgressCard(state: MessageQueueState) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MQSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = state.currentPhase?.label ?: "Scanning...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight
                )
                Text(
                    text = "${(state.scanProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MQPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.scanProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MQPrimary,
                trackColor = MQSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Scanned ${state.scannedFilesCount}/${state.totalFilesCount} files / 已扫描 ${state.scannedFilesCount}/${state.totalFilesCount} 个文件",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
        }
    }
}

@Composable
private fun ImpactMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    color: Color
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MQSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
        }
    }
}

@Composable
private fun RiskPathCard(access: ReflectionAccess) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MQSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(SeverityColor(access.severity))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = access.filePath.substringAfterLast("/"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = OnSurfaceLight
                )
                Text(
                    text = "Line ${access.lineNumber} · ${access.accessedField.fieldName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = when (access.severity) {
                            Severity.CRASH -> "CRASH"
                            Severity.BEHAVIOR_ANOMALY -> "ANOMALY"
                            Severity.SAFE -> "SAFE"
                        },
                        fontSize = 10.sp
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = SeverityColor(access.severity).copy(alpha = 0.2f),
                    labelColor = SeverityColor(access.severity)
                )
            )
        }
    }
}

@Composable
private fun ScanSummaryCard(state: MessageQueueState) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MQSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scan Complete / 扫描完成",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurfaceLight
                )
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SeveritySafe
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Found ${state.impactSummary.totalAccessPoints} reflection access points / " +
                        "发现 ${state.impactSummary.totalAccessPoints} 个反射访问点",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceDim
            )
        }
    }
}

// ===== Scanner Content =====
// ===== 扫描器内容 =====

@Composable
private fun ScannerContent(
    state: MessageQueueState,
    viewModel: MessageQueueViewModel,
    onSelectAccess: (ReflectionAccess) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Filter chips / 过滤标签
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SeverityFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.selectedFilter == filter,
                    onClick = { viewModel.processIntent(MessageQueueIntent.FilterBySeverity(filter)) },
                    label = { Text("${filter.label} (${filter.labelZh})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (filter) {
                            SeverityFilter.ALL -> Color(0xFF6750A4)
                            SeverityFilter.CRASH -> SeverityCrash.copy(alpha = 0.3f)
                            SeverityFilter.ANOMALY -> SeverityAnomaly.copy(alpha = 0.3f)
                            SeverityFilter.SAFE -> SeveritySafe.copy(alpha = 0.3f)
                        }
                    )
                )
            }
        }

        if (state.scanResults.isEmpty() && state.scanStatus != ScanStatus.COMPLETED) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = OnSurfaceDim
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Run scan first / 请先运行扫描",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceDim
                    )
                }
            }
        } else {
            val filtered = viewModel.getFilteredResults()
            Text(
                text = "${filtered.size} results / ${filtered.size} 条结果",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered) { access ->
                    ScanResultCard(
                        access = access,
                        onClick = { onSelectAccess(access) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun ScanResultCard(
    access: ReflectionAccess,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = MQSurfaceVariant)
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
                            .background(SeverityColor(access.severity))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = access.filePath.substringAfterLast("/"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = OnSurfaceLight
                    )
                }
                Text(
                    text = "L${access.lineNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = access.accessedField.fieldName,
                style = MaterialTheme.typography.bodySmall,
                color = MQPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CodeBackground, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = access.codeSnippet,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color(0xFFE0E0E0),
                    maxLines = 1
                )
            }
        }
    }
}

// ===== Impact Content =====
// ===== 影响分析内容 =====

@Composable
private fun ImpactContent(
    state: MessageQueueState,
    onSelectAccess: (ReflectionAccess) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Severity Breakdown / 严重级别分布",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceLight
            )
        }

        // Severity sections / 严重级别分组
        val crashItems = state.scanResults.filter { it.severity == Severity.CRASH }
        val anomalyItems = state.scanResults.filter { it.severity == Severity.BEHAVIOR_ANOMALY }
        val safeItems = state.scanResults.filter { it.severity == Severity.SAFE }

        if (crashItems.isNotEmpty()) {
            item {
                SeveritySection(
                    title = "CRASH / 直接崩溃 (${crashItems.size})",
                    color = SeverityCrash,
                    items = crashItems,
                    onSelectAccess = onSelectAccess
                )
            }
        }

        if (anomalyItems.isNotEmpty()) {
            item {
                SeveritySection(
                    title = "BEHAVIOR ANOMALY / 行为异常 (${anomalyItems.size})",
                    color = SeverityAnomaly,
                    items = anomalyItems,
                    onSelectAccess = onSelectAccess
                )
            }
        }

        if (safeItems.isNotEmpty()) {
            item {
                SeveritySection(
                    title = "SAFE / 安全 (${safeItems.size})",
                    color = SeveritySafe,
                    items = safeItems,
                    onSelectAccess = onSelectAccess
                )
            }
        }

        if (state.scanResults.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No scan results yet / 暂无扫描结果", color = OnSurfaceDim)
                }
            }
        }
    }
}

@Composable
private fun SeveritySection(
    title: String,
    color: Color,
    items: List<ReflectionAccess>,
    onSelectAccess: (ReflectionAccess) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        items.forEach { access ->
            ScanResultCard(
                access = access,
                onClick = { onSelectAccess(access) }
            )
        }
    }
}

// ===== Knowledge Base Content =====
// ===== 知识库内容 =====

@Composable
private fun KnowledgeContent(
    state: MessageQueueState,
    viewModel: MessageQueueViewModel
) {
    val knowledgeItems = viewModel.getFilteredKnowledgeBase()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar / 搜索栏
        item {
            OutlinedTextField(
                value = state.knowledgeSearchQuery,
                onValueChange = { viewModel.processIntent(MessageQueueIntent.SearchKnowledgeBase(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search patterns or APIs... / 搜索模式或 API...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.knowledgeSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.processIntent(MessageQueueIntent.SearchKnowledgeBase("")) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MQPrimary,
                    unfocusedBorderColor = OnSurfaceDim
                )
            )
        }

        items(knowledgeItems) { solution ->
            KnowledgeCard(solution = solution)
        }

        if (knowledgeItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching solutions / 无匹配的解决方案", color = OnSurfaceDim)
                }
            }
        }
    }
}

@Composable
private fun KnowledgeCard(solution: AlternativeSolution) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MQSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solution.alternativeApi,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4FC3F7)
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "Risk: ${solution.riskLevel.name}",
                            fontSize = 10.sp,
                            color = SeverityColor(solution.riskLevel)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = SeverityColor(solution.riskLevel).copy(alpha = 0.15f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pattern: ${solution.targetPattern}",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
            Text(
                text = solution.useCase,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Code example / 代码示例
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CodeBackground, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = solution.exampleCode,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFF80CBC4)
                )
            }

            if (solution.migrationSteps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Steps / 步骤:",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDim
                )
                solution.migrationSteps.take(2).forEachIndexed { index, step ->
                    Text(
                        text = "${index + 1}. $step",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceLight,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}

// ===== Migration Content =====
// ===== 迁移引擎内容 =====

@Composable
private fun MigrationContent(
    state: MessageQueueState,
    viewModel: MessageQueueViewModel
) {
    // Initialize migration steps if empty / 如果为空则初始化迁移步骤
    LaunchedEffect(state.migrationSteps.size) {
        if (state.migrationSteps.isEmpty() && state.scanStatus == ScanStatus.COMPLETED) {
            viewModel.initializeMigrationSteps()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Migration Steps / 迁移步骤 (${state.migrationSteps.count { it.isCompleted }}/${state.migrationSteps.size})",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceLight
            )
        }

        if (state.migrationSteps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = OnSurfaceDim
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Complete a scan first / 请先完成扫描", color = OnSurfaceDim)
                    }
                }
            }
        }

        items(state.migrationSteps) { step ->
            MigrationStepCard(
                step = step,
                onPreview = { viewModel.processIntent(MessageQueueIntent.PreviewMigration(step)) },
                onApply = { viewModel.processIntent(MessageQueueIntent.StartMigration(step)) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Migration Preview Dialog / 迁移预览对话框
    if (state.selectedMigrationStep != null) {
        MigrationPreviewDialog(
            step = state.selectedMigrationStep,
            onDismiss = { viewModel.processIntent(MessageQueueIntent.DismissMigrationPreview) },
            onApply = { viewModel.processIntent(MessageQueueIntent.StartMigration(state.selectedMigrationStep)) }
        )
    }
}

@Composable
private fun MigrationStepCard(
    step: MigrationStep,
    onPreview: () -> Unit,
    onApply: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = when {
                step.isCompleted -> SeveritySafe.copy(alpha = 0.1f)
                step.isInProgress -> MQPrimary.copy(alpha = 0.1f)
                else -> MQSurfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.access.filePath.substringAfterLast("/"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = OnSurfaceLight
                    )
                    Text(
                        text = "Line ${step.access.lineNumber} · ${step.targetSolution.alternativeApi}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim
                    )
                }
                when {
                    step.isCompleted -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SeveritySafe)
                    step.isInProgress -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MQPrimary)
                    step.errorMessage != null -> Icon(Icons.Default.Error, contentDescription = null, tint = SeverityCrash)
                }
            }

            if (step.errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = SeverityCrash
                )
            }

            if (!step.isCompleted && !step.isInProgress) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onPreview) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Preview", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApply,
                        colors = ButtonDefaults.buttonColors(containerColor = MQPrimary)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Migrate", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MigrationPreviewDialog(
    step: MigrationStep,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Migration Preview / 迁移预览") },
        text = {
            Column {
                Text(
                    text = "Before / 迁移前:",
                    style = MaterialTheme.typography.labelSmall,
                    color = SeverityCrash
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CodeBackground, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = step.previewBefore,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Color(0xFFE0E0E0)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "After / 迁移后:",
                    style = MaterialTheme.typography.labelSmall,
                    color = SeveritySafe
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CodeBackground, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = step.previewAfter,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Color(0xFF80CBC4)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onApply, colors = ButtonDefaults.buttonColors(containerColor = MQPrimary)) {
                Text("Apply / 应用")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel / 取消")
            }
        }
    )
}

// ===== Tests Content =====
// ===== 回归测试内容 =====

@Composable
private fun TestsContent(
    state: MessageQueueState,
    viewModel: MessageQueueViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Regression Tests / 回归测试 (${state.regressionTests.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceLight
                )
                Row {
                    OutlinedButton(
                        onClick = { viewModel.processIntent(MessageQueueIntent.GenerateAllTests) }
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Generate", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.processIntent(MessageQueueIntent.RunAllTests) },
                        enabled = state.regressionTests.isNotEmpty() && !state.isRunningAllTests,
                        colors = ButtonDefaults.buttonColors(containerColor = MQPrimary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run All", fontSize = 12.sp)
                    }
                }
            }
        }

        if (state.regressionTests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Science,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = OnSurfaceDim
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Click Generate to create test cases / 点击生成以创建测试用例", color = OnSurfaceDim)
                    }
                }
            }
        }

        items(state.regressionTests) { test ->
            TestCaseCard(
                test = test,
                onRun = { viewModel.processIntent(MessageQueueIntent.RunTest(test)) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun TestCaseCard(
    test: RegressionTest,
    onRun: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = when {
                test.isPassed == true -> SeveritySafe.copy(alpha = 0.1f)
                test.isPassed == false -> SeverityCrash.copy(alpha = 0.1f)
                else -> MQSurfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = test.testMethodName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = OnSurfaceLight
                    )
                    Text(
                        text = test.testDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim
                    )
                }
                when {
                    test.isRunning -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MQPrimary
                    )
                    test.isPassed == true -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SeveritySafe)
                    test.isPassed == false -> Icon(Icons.Default.Cancel, contentDescription = null, tint = SeverityCrash)
                    else -> null
                }?.let { icon ->
                    Spacer(modifier = Modifier.width(8.dp))
                    icon
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CodeBackground, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = test.testCode,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFF80CBC4),
                    maxLines = 5
                )
            }

            if (test.isPassed == null && !test.isRunning) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRun,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = MQPrimary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run", fontSize = 12.sp)
                }
            }
        }
    }
}

// ===== Settings Content =====
// ===== 设置内容 =====

@Composable
private fun SettingsContent(
    state: MessageQueueState,
    onUpdateSettings: (MessageQueueSettings) -> Unit,
    onExportReport: (ReportFormat) -> Unit
) {
    val settings = state.settings

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Scan Settings / 扫描设置",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceLight
            )
        }

        // Scan paths / 扫描路径
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MQSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Include Paths / 包含路径",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    settings.scanIncludePaths.forEach { path ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = OnSurfaceDim
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = path,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = OnSurfaceLight
                            )
                        }
                    }
                }
            }
        }

        // Report format / 报告格式
        item {
            Text(
                text = "Report Format / 报告格式",
                style = MaterialTheme.typography.titleSmall,
                color = OnSurfaceLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportFormat.entries.forEach { format ->
                    FilterChip(
                        selected = settings.reportFormat == format,
                        onClick = { onExportReport(format) },
                        label = { Text(format.name) }
                    )
                }
            }
        }

        // Auto backup / 自动备份
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MQSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Auto Backup / 自动备份",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceLight
                        )
                        Text(
                            text = "Backup before migration / 迁移前自动备份",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDim
                        )
                    }
                    Switch(
                        checked = settings.autoBackup,
                        onCheckedChange = { onUpdateSettings(settings.copy(autoBackup = it)) },
                        colors = SwitchDefaults.colors(checkedTrackColor = MQPrimary)
                    )
                }
            }
        }

        // Export button / 导出按钮
        item {
            Button(
                onClick = { onExportReport(settings.reportFormat) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MQPrimary)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Report / 导出报告")
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ===== Detail Bottom Sheet =====
// ===== 详情底部弹窗 =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailBottomSheet(
    access: ReflectionAccess,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MQSurfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = access.filePath.substringAfterLast("/"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceLight
                    )
                    Text(
                        text = "Line ${access.lineNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim
                    )
                }
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = access.severity.name,
                            fontSize = 12.sp,
                            color = SeverityColor(access.severity)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = SeverityColor(access.severity).copy(alpha = 0.2f)
                    )
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = OnSurfaceDim.copy(alpha = 0.3f)
            )

            // Field info / 字段信息
            Text(
                text = "Field / 字段",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDim
            )
            Text(
                text = "${access.accessedField.fieldName} — ${access.accessedField.description}",
                style = MaterialTheme.typography.bodyMedium,
                color = MQPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Reflection type / 反射类型
            Text(
                text = "Reflection Type / 反射类型",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDim
            )
            Text(
                text = access.reflectionType.name,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Code snippet / 代码片段
            Text(
                text = "Code Snippet / 代码片段",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDim
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CodeBackground, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = access.codeSnippet,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFFE0E0E0)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Context / 上下文
            Text(
                text = "Context / 上下文",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDim
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp)
                    .background(CodeBackground, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = access.contextSnippet,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFF9E9E9E)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Severity explanation / 严重级别说明
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = SeverityColor(access.severity).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = when (access.severity) {
                            Severity.CRASH -> Icons.Default.Error
                            Severity.BEHAVIOR_ANOMALY -> Icons.Default.Warning
                            Severity.SAFE -> Icons.Default.CheckCircle
                        },
                        contentDescription = null,
                        tint = SeverityColor(access.severity),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (access.severity) {
                            Severity.CRASH -> "Direct call to Field.set() modifies MessageQueue internal state. " +
                                    "This WILL crash on Android 17 with lock-free MessageQueue. / " +
                                    "直接调用 Field.set() 修改 MessageQueue 内部状态，在 Android 17 无锁实现上会崩溃。"
                            Severity.BEHAVIOR_ANOMALY -> "Field.get() is called but ART optimization may change the value after inlining. " +
                                    "Behavior may differ on Android 17. / " +
                                    "调用了 Field.get() 但 ART 优化可能在内联后改变值，行为可能与 Android 17 不一致。"
                            Severity.SAFE -> "This field is immutable after initialization. Safe to read on Android 17. / " +
                                    "此字段初始化后不可变，在 Android 17 上读取安全。"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceLight
                    )
                }
            }
        }
    }
}
