package com.mvi.kenny.feature.appcompat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCompatMigrationScreen(
    onUpdateTopBar: (com.mvi.kenny.base.TopBarConfig) -> Unit,
    viewModel: AppCompatMigrationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabTitles = listOf(
        "仪表盘" to Icons.Default.Info,
        "MinSdk" to Icons.Default.SwapVert,
        "JSpecify" to Icons.Default.CheckCircle,
        "KGP版本" to Icons.Default.Code,
        "报告" to Icons.Default.FileDownload,
        "设置" to Icons.Default.Settings
    )

    LaunchedEffect(selectedTabIndex) {
        onUpdateTopBar(
            com.mvi.kenny.base.TopBarConfig(
                title = "AppCompat 1.8.0 迁移工具",
                actions = listOf(
                    com.mvi.kenny.base.TopBarAction(
                        Icons.Default.Refresh,
                        "刷新"
                    ) { viewModel.sendIntent(AppCompatMigrationIntent.RefreshScan) }
                )
            )
        )
    }

    LaunchedEffect(Unit) { viewModel.effect.collect { } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AppCompat 1.8.0 迁移工具") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp
            ) {
                tabTitles.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        icon = { Icon(icon, title, modifier = Modifier.size(20.dp)) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> DashboardTab(state, viewModel)
                1 -> MinSdkAnalyzerTab(state, viewModel)
                2 -> JSpecifyCheckerTab(state, viewModel)
                3 -> KGPVersionTab(state, viewModel)
                4 -> ReportsTab(state, viewModel)
                5 -> SettingsTab(state, viewModel)
            }
        }
    }
}

// ============ Dashboard Tab ============

@Composable
private fun DashboardTab(state: AppCompatMigrationState, viewModel: AppCompatMigrationViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HealthScoreCard(state.healthScore, state.scanState) }
        item { SummaryStatsRow(state) }
        item { QuickAccessCard(state, viewModel) }
        item { KGPSummaryCard(state, viewModel) }
    }
}

@Composable
private fun HealthScoreCard(healthScore: Int, scanState: ScanState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "迁移健康度 / Migration Health",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                HealthScoreCircle(score = healthScore)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$healthScore",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            healthScore >= 80 -> Color(0xFF4CAF50)
                            healthScore >= 50 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                    Text(
                        text = when {
                            healthScore >= 80 -> "优秀 / Excellent"
                            healthScore >= 50 -> "需处理 / Needs Attention"
                            else -> "紧急 / Critical"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (scanState == ScanState.Scanning) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF9C27B0)
                )
                Text(
                    text = "扫描中... / Scanning...",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun HealthScoreCircle(score: Int) {
    val color = Color(0xFF9C27B0)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2
        val cy = size.height / 2
        val radius = minOf(cx, cy) * 0.8f
        drawCircle(
            color = color.copy(alpha = 0.15f),
            radius = radius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = color,
            radius = radius * (score / 100f),
            center = Offset(cx, cy),
            style = Stroke(width = 8.dp.toPx())
        )
    }
}

@Composable
private fun SummaryStatsRow(state: AppCompatMigrationState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            Modifier.weight(1f),
            "受影响模块",
            "${state.affectedModulesCount}",
            "/ ${state.minSdkAnalysis.size}",
            Color(0xFFFF9800)
        )
        StatCard(
            Modifier.weight(1f),
            "P0 违规",
            "${state.p0Count}",
            "Critical",
            Color(0xFFF44336)
        )
        StatCard(
            Modifier.weight(1f),
            "JSpecify",
            "${(state.jspecifyCoverage * 100).toInt()}%",
            "Coverage",
            Color(0xFF9C27B0)
        )
        val kgp = state.kgpVersionStatus
        StatCard(
            Modifier.weight(1f),
            "KGP",
            if (kgp.isCompatible) "OK" else "NO",
            if (kgp.isCompatible) "Compatible" else "Incompatible",
            if (kgp.isCompatible) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickAccessCard(state: AppCompatMigrationState, viewModel: AppCompatMigrationViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "快捷操作 / Quick Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.sendIntent(AppCompatMigrationIntent.SelectTab(1)) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Icon(Icons.Default.SwapVert, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("MinSdk")
                }
                OutlinedButton(
                    onClick = { viewModel.sendIntent(AppCompatMigrationIntent.SelectTab(2)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Security, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("JSpecify")
                }
                OutlinedButton(
                    onClick = { viewModel.sendIntent(AppCompatMigrationIntent.SelectTab(3)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Code, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("KGP")
                }
            }
        }
    }
}

@Composable
private fun KGPSummaryCard(state: AppCompatMigrationState, viewModel: AppCompatMigrationViewModel) {
    val kgp = state.kgpVersionStatus
    val isComp = kgp.isCompatible
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Code, null,
                        tint = if (isComp) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "KGP 版本状态 / KGP Version Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (isComp) "OK" else "NO",
                    color = if (isComp) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Kotlin: ${kgp.kotlinVersion}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "KGP: ${kgp.kgpVersion}", style = MaterialTheme.typography.bodySmall)
                }
                if (!isComp && kgp.upgradePath != null) {
                    Text(
                        text = kgp.upgradePath,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800)
                    )
                }
            }
            if (!isComp) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.sendIntent(AppCompatMigrationIntent.CheckKGPVersion) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(4.dp))
                    Text("查看升级建议")
                }
            }
        }
    }
}

// ============ MinSdk Analyzer Tab ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MinSdkAnalyzerTab(state: AppCompatMigrationState, viewModel: AppCompatMigrationViewModel) {
    val sheetState = rememberModalBottomSheetState()
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "受 minSdk 23 提升影响的模块",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            items(state.minSdkAnalysis, key = { it.moduleName }) { module ->
                ModuleImpactCard(module) {
                    viewModel.sendIntent(AppCompatMigrationIntent.ShowModuleDetail(module))
                }
            }
            if (state.minSdkAnalysis.isEmpty()) {
                item { EmptyStateCard(Icons.Default.Build, "暂无数据，请先执行全量扫描") }
            }
        }
    }
    if (state.showModuleSheet && state.selectedModule != null) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.sendIntent(AppCompatMigrationIntent.DismissModuleSheet)
            },
            sheetState = sheetState
        ) {
            ModuleDetailSheetContent(state.selectedModule!!)
        }
    }
}

@Composable
private fun ModuleImpactCard(
    module: ModuleMinSdkImpact,
    onClick: () -> Unit
) {
    val isAffected = module.affectedByAppCompat && module.currentMinSdk < 23
    val cardColor = when {
        isAffected -> Color(0xFFFF9800).copy(alpha = 0.1f)
        module.currentMinSdk >= 23 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Build, null,
                        tint = when {
                            isAffected -> Color(0xFFFF9800)
                            module.currentMinSdk >= 23 -> Color(0xFF4CAF50)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = module.moduleName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "minSdk: ${module.currentMinSdk}",
                    color = when {
                        isAffected -> Color(0xFFFF9800)
                        module.currentMinSdk >= 23 -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when {
                        isAffected -> "⚠️ 需要升级到 ${module.suggestedMinSdk}"
                        module.currentMinSdk >= 23 -> "✅ 符合要求"
                        else -> "未受影响"
                    },
                    color = when {
                        isAffected -> Color(0xFFFF9800)
                        module.currentMinSdk >= 23 -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (module.affectedByAppCompat) {
                    Text(
                        text = "AppCompat 依赖",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9C27B0)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleDetailSheetContent(module: ModuleMinSdkImpact) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text(
            text = "模块详情 / Module Detail",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        InfoRow("模块名", module.moduleName)
        InfoRow("当前 minSdk", "${module.currentMinSdk}")
        InfoRow("建议 minSdk", "${module.suggestedMinSdk}")
        InfoRow("受 AppCompat 影响", if (module.affectedByAppCompat) "是" else "否")
        InfoRow("Activity 版本", module.activityVersion)
        if (module.dependencyChain.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "依赖链 / Dependency Chain",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            module.dependencyChain.forEach {
                Text(
                    text = "• $it",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}

// ============ JSpecify Checker Tab ============

@Composable
private fun JSpecifyCheckerTab(
    state: AppCompatMigrationState,
    viewModel: AppCompatMigrationViewModel
) {
    var packageInput by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = packageInput,
                onValueChange = { packageInput = it },
                label = { Text("包名") },
                placeholder = { Text("com.example.app") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    viewModel.sendIntent(AppCompatMigrationIntent.ScanJSpecifyViolations(packageInput))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
            ) {
                Icon(Icons.Default.Refresh, "扫描")
                Spacer(Modifier.width(4.dp))
                Text("扫描")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = ViolationSeverity.P0 in state.filterSeveritySet,
                onClick = {
                    val s = state.filterSeveritySet.toMutableSet().apply {
                        if (ViolationSeverity.P0 in this) remove(ViolationSeverity.P0) else add(ViolationSeverity.P0)
                    }
                    viewModel.sendIntent(AppCompatMigrationIntent.FilterBySeverity(s))
                },
                label = { Text("P0 (${state.p0Count})") },
                leadingIcon = { Icon(Icons.Default.Error, null, tint = Color(0xFFF44336)) }
            )
            FilterChip(
                selected = ViolationSeverity.P1 in state.filterSeveritySet,
                onClick = {
                    val s = state.filterSeveritySet.toMutableSet().apply {
                        if (ViolationSeverity.P1 in this) remove(ViolationSeverity.P1) else add(ViolationSeverity.P1)
                    }
                    viewModel.sendIntent(AppCompatMigrationIntent.FilterBySeverity(s))
                },
                label = { Text("P1 (${state.p1Count})") },
                leadingIcon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFFF9800)) }
            )
            FilterChip(
                selected = ViolationSeverity.P2 in state.filterSeveritySet,
                onClick = {
                    val s = state.filterSeveritySet.toMutableSet().apply {
                        if (ViolationSeverity.P2 in this) remove(ViolationSeverity.P2) else add(ViolationSeverity.P2)
                    }
                    viewModel.sendIntent(AppCompatMigrationIntent.FilterBySeverity(s))
                },
                label = { Text("P2 (${state.p2Count})") },
                leadingIcon = { Icon(Icons.Default.Info, null, tint = Color(0xFF9C27B0)) }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.filteredViolations, key = { it.id }) { violation ->
                JSpecifyViolationCard(violation) {
                    viewModel.sendIntent(AppCompatMigrationIntent.ShowViolationDetail(violation))
                }
            }
            if (state.filteredViolations.isEmpty()) {
                item { EmptyStateCard(Icons.Default.Security, "暂无 JSpecify 违规") }
            }
        }
    }
    if (state.showViolationDialog && state.selectedViolation != null) {
        JSpecifyViolationDetailDialog(state.selectedViolation!!) {
            viewModel.sendIntent(AppCompatMigrationIntent.DismissViolationDialog)
        }
    }
}

@Composable
private fun JSpecifyViolationCard(
    violation: JSpecifyViolation,
    onClick: () -> Unit
) {
    val sevColor = when (violation.severity) {
        ViolationSeverity.P0 -> Color(0xFFF44336)
        ViolationSeverity.P1 -> Color(0xFFFF9800)
        ViolationSeverity.P2 -> Color(0xFF9C27B0)
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = sevColor.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (violation.severity) {
                            ViolationSeverity.P0 -> Icons.Default.Error
                            ViolationSeverity.P1 -> Icons.Default.Warning
                            ViolationSeverity.P2 -> Icons.Default.Info
                        }, null, tint = sevColor
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = violation.severity.name,
                        color = sevColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Line ${violation.line}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = violation.filePath,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "当前: ${violation.currentAnnotation}",
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.AutoAwesome, "->", modifier = Modifier.size(16.dp))
                Text(
                    text = "建议: ${violation.suggestedAnnotation}",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun JSpecifyViolationDetailDialog(
    violation: JSpecifyViolation,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("JSpecify 违规详情") },
        text = {
            Column {
                Text(
                    text = "Severity: ${violation.severity.name}",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "File: ${violation.filePath}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Line: ${violation.line}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "当前注解:", color = Color(0xFFF44336))
                Text(
                    text = violation.currentAnnotation,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "建议注解:", color = Color(0xFF4CAF50))
                Text(
                    text = violation.suggestedAnnotation,
                    fontFamily = FontFamily.Monospace
                )
                if (violation.suggestion.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "建议:")
                    Text(
                        text = violation.suggestion,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

// ============ KGP Version Tab ============

@Composable
private fun KGPVersionTab(
    state: AppCompatMigrationState,
    viewModel: AppCompatMigrationViewModel
) {
    val kgp = state.kgpVersionStatus
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (kgp.isCompatible)
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else
                        Color(0xFFF44336).copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "当前版本状态 / Current Version Status",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (kgp.isCompatible) "OK" else "NO",
                            color = if (kgp.isCompatible) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Kotlin",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = kgp.kotlinVersion,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "KGP",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = kgp.kgpVersion,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }
            }
        }
        if (!kgp.isCompatible) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome, null,
                                tint = Color(0xFF9C27B0)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "升级路径 / Upgrade Path",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        if (kgp.isKotlin2Required) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF9800).copy(alpha = 0.1f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning, null,
                                    tint = Color(0xFFFF9800)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "AppCompat 1.8.0 要求 Kotlin 2.0 + KGP 2.0.0+",
                                    color = Color(0xFFFF9800),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (kgp.upgradePath != null) {
                            Text(
                                text = kgp.upgradePath,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF9C27B0)
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kotlin / KGP 兼容性矩阵",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Kotlin",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "KGP",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "AppCompat 1.8.0",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    listOf(
                        Triple("2.0.0", "2.0.0+", "OK"),
                        Triple("1.9.24", "1.9.24", "NO")
                    ).forEach { (k, g, s) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Text(
                                text = k,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = g,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = s,
                                color = if (s == "OK") Color(0xFF4CAF50) else Color(0xFFF44336),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============ Reports Tab ============

@Composable
private fun ReportsTab(
    state: AppCompatMigrationState,
    viewModel: AppCompatMigrationViewModel
) {
    var selectedType by remember { mutableStateOf(ReportType.JSpecifyCompliance) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.JSON) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "报告类型 / Report Type",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ReportType.entries.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedType = type }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (type) {
                                        ReportType.MinSdkImpact -> "minSdk 影响分析报告"
                                        ReportType.JSpecifyCompliance -> "JSpecify 合规报告"
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = when (type) {
                                        ReportType.MinSdkImpact -> "MinSdk Impact Report"
                                        ReportType.JSpecifyCompliance -> "JSpecify Report"
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "报告预览 / Report Preview",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    when (selectedType) {
                        ReportType.MinSdkImpact -> {
                            Text(
                                text = "模块总数: ${state.minSdkAnalysis.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "受影响模块: ${state.affectedModulesCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        ReportType.JSpecifyCompliance -> {
                            val r = state.complianceReport
                            if (r != null) {
                                Text(
                                    text = "扫描时间: ${r.scanTimestamp}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "扫描文件: ${r.totalFiles}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "JSpecify 覆盖率: ${(r.coverage * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "违规数: ${r.violations.size}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Text(
                                    text = "暂无数据，请先执行扫描",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "导出格式 / Export Format",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExportFormat.entries.forEach { f ->
                            FilterChip(
                                selected = selectedFormat == f,
                                onClick = { selectedFormat = f },
                                label = {
                                    Text(
                                        when (f) {
                                            ExportFormat.PDF -> "PDF"
                                            ExportFormat.JSON -> "JSON"
                                            ExportFormat.HTML -> "HTML"
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = {
                    viewModel.sendIntent(
                        AppCompatMigrationIntent.ExportReport(selectedType, selectedFormat)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
            ) {
                Icon(Icons.Default.FileDownload, null)
                Spacer(Modifier.width(8.dp))
                Text("导出报告 / Export Report")
            }
        }
    }
}

// ============ Settings Tab ============

@Composable
private fun SettingsTab(
    state: AppCompatMigrationState,
    viewModel: AppCompatMigrationViewModel
) {
    val settings = state.settings
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "扫描设置 / Scan Settings",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsSwitchItem(
                        title = "自动修复",
                        subtitle = "Auto Fix",
                        checked = settings.autoFixEnabled
                    ) {
                        viewModel.sendIntent(
                            AppCompatMigrationIntent.UpdateSettings(
                                settings.copy(autoFixEnabled = it)
                            )
                        )
                    }
                    HorizontalDivider()
                    SettingsSwitchItem(
                        title = "修复前创建备份",
                        subtitle = "Backup Before Fix",
                        checked = settings.backupEnabled
                    ) {
                        viewModel.sendIntent(
                            AppCompatMigrationIntent.UpdateSettings(
                                settings.copy(backupEnabled = it)
                            )
                        )
                    }
                    HorizontalDivider()
                    SettingsSwitchItem(
                        title = "检查 Activity 依赖版本",
                        subtitle = "Check Activity Dependency",
                        checked = settings.enableActivityCheck
                    ) {
                        viewModel.sendIntent(
                            AppCompatMigrationIntent.UpdateSettings(
                                settings.copy(enableActivityCheck = it)
                            )
                        )
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "JSpecify 设置 / JSpecify Settings",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsSwitchItem(
                        title = "启用 JSpecify 严格模式",
                        subtitle = "Enable -Xjspecify-annotations=strict",
                        checked = settings.jspecifyAnnotationsStrict
                    ) {
                        viewModel.sendIntent(
                            AppCompatMigrationIntent.UpdateSettings(
                                settings.copy(jspecifyAnnotationsStrict = it)
                            )
                        )
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "排除路径 / Excluded Paths",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = settings.scanExcludePaths.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
