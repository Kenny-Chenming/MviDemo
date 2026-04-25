package com.mvi.kenny.feature.composemigration

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ============================================================
 * ComposeMigrationScreen — PRD-162 UI Screen
 * Compose 1.12.0 Migration 开发工具包
 * ============================================================
 * 
 * Main screen with 7 tabs:
 * 1. 合规检测 (Compliance) — Project info + compliance status
 * 2. 迁移扫描 (Migration Scan) — Before/After diff viewer
 * 3. KMP向导 (KMP Wizard) — Stepper wizard for KMP split
 * 4. KSP迁移 (KSP Migration) — kapt → KSP migration list
 * 5. API替换 (API Replace) — applicationVariants → androidComponents
 * 6. Kotlin合规 (Kotlin Compliance) — Built-in Kotlin check
 * 7. 验证套件 (Verification) — Upgrade verification checklist
 * 
 * Color Theme:
 * — Primary: #6366F1 (Indigo 500)
 * — Pass: #22C55E (Green 500)
 * — Warn: #F59E0B (Amber 500)
 * — Fail: #EF4444 (Red 500)
 * 
 * @param state Current UI state from ViewModel
 * @param onIntent Send user intent to ViewModel
 */

// ============================================================
// Main Screen / 主屏幕
// ============================================================
@Composable
fun ComposeMigrationScreen(
    state: ComposeMigrationState,
    onIntent: (ComposeMigrationIntent) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Collect effects from ViewModel
    LaunchedEffect(Unit) {
        // Note: In real usage, would collect from ViewModel's effect channel
        // For now, effects are handled via state changes
    }
    
    // Error snackbar
    state.errorMessage?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            onIntent(ComposeMigrationIntent.DismissError)
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC)) // Light gray background
        ) {
            // Project Selector / 项目选择器
            ProjectSelectorCard(
                projectPath = state.projectPath,
                onPathChange = { onIntent(ComposeMigrationIntent.UpdateProjectPath(it)) },
                onSelectProject = { onIntent(ComposeMigrationIntent.SelectProject(state.projectPath)) },
                scanInProgress = state.scanInProgress
            )
            
            // Scan Progress Bar / 扫描进度条
            AnimatedVisibility(
                visible = state.scanInProgress,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ScanProgressBar(
                    phase = state.scanPhase,
                    progress = state.scanProgress
                )
            }
            
            // Tab Row / 分Tab
            TabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                containerColor = Color.White,
                contentColor = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY),
                modifier = Modifier.fillMaxWidth()
            ) {
                MigrationTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { onIntent(ComposeMigrationIntent.SelectTab(tab)) },
                        text = {
                            Text(
                                text = tab.tabTitle,
                                fontSize = 12.sp,
                                fontWeight = if (state.selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            // Tab Content / 分Tab内容
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (state.selectedTab) {
                    MigrationTab.COMPLIANCE -> ComplianceTabContent(
                        state = state,
                        onIntent = onIntent
                    )
                    MigrationTab.MIGRATION_SCAN -> MigrationScanTabContent(
                        state = state,
                        onIntent = onIntent
                    )
                    MigrationTab.KMP_WIZARD -> KmpWizardTabContent(
                        state = state,
                        onIntent = onIntent
                    )
                    MigrationTab.KSP_MIGRATION -> KspMigrationTabContent(
                        state = state,
                        onIntent = onIntent
                    )
                    MigrationTab.API_REPLACE -> ApiReplaceTabContent(
                        state = state,
                        onIntent = onIntent
                    )
                    MigrationTab.KOTLIN_COMPLIANCE -> KotlinComplianceTabContent(
                        state = state,
                        onIntent = onIntent
                    )
                    MigrationTab.VERIFICATION -> VerificationTabContent(
                        state = state,
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}

// ============================================================
// Extension: Tab Title / 扩展：Tab标题
// ============================================================
private val MigrationTab.tabTitle: String
    get() = when (this) {
        MigrationTab.COMPLIANCE -> "合规检测"
        MigrationTab.MIGRATION_SCAN -> "迁移扫描"
        MigrationTab.KMP_WIZARD -> "KMP向导"
        MigrationTab.KSP_MIGRATION -> "KSP迁移"
        MigrationTab.API_REPLACE -> "API替换"
        MigrationTab.KOTLIN_COMPLIANCE -> "Kotlin合规"
        MigrationTab.VERIFICATION -> "验证套件"
    }

// ============================================================
// Project Selector Card / 项目选择器卡片
// ============================================================
@Composable
private fun ProjectSelectorCard(
    projectPath: String,
    onPathChange: (String) -> Unit,
    onSelectProject: () -> Unit,
    scanInProgress: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "项目选择器 / Project Selector",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = projectPath,
                    onValueChange = onPathChange,
                    label = { Text("项目路径 / Project Path") },
                    placeholder = { Text("e.g. /path/to/your/compose/project") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !scanInProgress
                )
                Button(
                    onClick = onSelectProject,
                    enabled = projectPath.isNotEmpty() && !scanInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)
                    )
                ) {
                    if (scanInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("扫描 / Scan")
                    }
                }
            }
        }
    }
}

// ============================================================
// Scan Progress Bar / 扫描进度条
// ============================================================
@Composable
private fun ScanProgressBar(
    phase: ScanPhase,
    progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY),
            trackColor = Color(0xFFE2E8F0)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = phase.phaseText,
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )
    }
}

private val ScanPhase.phaseText: String
    get() = when (this) {
        ScanPhase.IDLE -> "就绪 / Ready"
        ScanPhase.ANALYZING_GRADLE -> "正在分析 Gradle 配置… / Analyzing Gradle configuration…"
        ScanPhase.CHECKING_AGP -> "正在检查 AGP 版本… / Checking AGP version…"
        ScanPhase.CHECKING_COMPOSE -> "正在检查 Compose 依赖… / Checking Compose dependencies…"
        ScanPhase.CHECKING_KSP -> "正在检查 KSP 配置… / Checking KSP configuration…"
        ScanPhase.GENERATING_REPORT -> "正在生成报告… / Generating report…"
    }

// ============================================================
// Compliance Tab Content / 合规检测Tab内容
// ============================================================
@Composable
private fun ComplianceTabContent(
    state: ComposeMigrationState,
    onIntent: (ComposeMigrationIntent) -> Unit
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // Project Info Card / 项目信息卡
        if (state.compileSdkVersion != null) {
            ProjectInfoCard(state = state)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Compliance Status Indicator / 合规状态指示器
        ComplianceStatusIndicator(status = state.complianceStatus)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Compliance Items List / 合规详情列表
        Text(
            text = "合规详情 / Compliance Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        state.complianceItems.forEach { item ->
            ComplianceItemCard(item = item)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Export Report Button / 导出报告按钮
        Spacer(modifier = Modifier.height(16.dp))
        ExportReportSection(
            format = state.reportExportFormat,
            onFormatChange = { onIntent(ComposeMigrationIntent.SetExportFormat(it)) },
            onExport = { onIntent(ComposeMigrationIntent.ExportReport) }
        )
    }
}

@Composable
private fun ProjectInfoCard(state: ComposeMigrationState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "项目信息 / Project Info",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(label = "SDK", value = "${state.compileSdkVersion ?: "-"}")
                InfoChip(label = "AGP", value = state.agpVersion ?: "-")
                InfoChip(label = "Kotlin", value = state.kotlinVersion ?: "-")
                InfoChip(label = "Compose BOM", value = state.composeBomVersion ?: "-")
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
    }
}

@Composable
private fun ComplianceStatusIndicator(status: ComplianceStatus) {
    val (icon, color, label) = when (status) {
        ComplianceStatus.PASS -> Triple(Icons.Default.CheckCircle, Color(ComposeMigrationConstants.COLOR_PASS), "全部合规 / All Pass")
        ComplianceStatus.WARN -> Triple(Icons.Default.Warning, Color(ComposeMigrationConstants.COLOR_WARN), "有警告 / Warnings")
        ComplianceStatus.FAIL -> Triple(Icons.Default.Error, Color(ComposeMigrationConstants.COLOR_FAIL), "不合规 / Fail")
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun ComplianceItemCard(item: ComplianceItem) {
    val borderColor = when (item.status) {
        ComplianceStatus.PASS -> Color(ComposeMigrationConstants.COLOR_PASS)
        ComplianceStatus.WARN -> Color(ComposeMigrationConstants.COLOR_WARN)
        ComplianceStatus.FAIL -> Color(ComposeMigrationConstants.COLOR_FAIL)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = item.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description,
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "当前: ${item.currentValue} → 要求: ${item.requiredValue}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF1E293B)
                )
            }
            item.fixSuggestion?.let { suggestion ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "修复: $suggestion",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)
                )
            }
            item.fileLocation?.let { location ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "位置: $location",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ComplianceStatus) {
    val (text, color) = when (status) {
        ComplianceStatus.PASS -> "PASS" to Color(ComposeMigrationConstants.COLOR_PASS)
        ComplianceStatus.WARN -> "WARN" to Color(ComposeMigrationConstants.COLOR_WARN)
        ComplianceStatus.FAIL -> "FAIL" to Color(ComposeMigrationConstants.COLOR_FAIL)
    }
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ExportReportSection(
    format: ReportFormat,
    onFormatChange: (ReportFormat) -> Unit,
    onExport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "导出报告 / Export Report",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReportFormat.entries.forEach { fmt ->
                    FilterChip(
                        selected = format == fmt,
                        onClick = { onFormatChange(fmt) },
                        label = { Text(fmt.name) }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onExport,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)
                    )
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导出 / Export")
                }
            }
        }
    }
}

// ============================================================
// Migration Scan Tab Content / 迁移扫描Tab内容
// ============================================================
@Composable
private fun MigrationScanTabContent(
    state: ComposeMigrationState,
    onIntent: (ComposeMigrationIntent) -> Unit
) {
    if (state.diffResults.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.Compare,
            message = "暂无迁移Diff\nNo migration diffs found\n\n请先在 合规检测 Tab 完成扫描\nRun scan in Compliance tab first"
        )
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.diffResults.size} 个Diff / Diff Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { onIntent(ComposeMigrationIntent.ApplyAllDiffs) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)
                    )
                ) {
                    Text("应用全部 / Apply All")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            state.diffResults.forEach { diff ->
                DiffCard(
                    diff = diff,
                    isSelected = state.selectedDiff?.id == diff.id,
                    onSelect = { onIntent(ComposeMigrationIntent.SelectDiff(diff)) },
                    onApply = { onIntent(ComposeMigrationIntent.ApplyDiff(diff.id)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DiffCard(
    diff: DiffResult,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF1F5F9) else Color.White
        ),
        border = if (isSelected) BorderStroke(2.dp, Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = diff.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                DiffTypeBadge(type = diff.changeType)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = diff.filePath,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Before/After preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CodePreviewBox(
                    title = "BEFORE",
                    lines = diff.beforeLines.take(3),
                    backgroundColor = Color(0xFFFEE2E2),
                    textColor = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f)
                )
                CodePreviewBox(
                    title = "AFTER",
                    lines = diff.afterLines.take(3),
                    backgroundColor = Color(0xFFDCFCE7),
                    textColor = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (diff.canAutoApply) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onApply,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)
                    )
                ) {
                    Text("应用此Diff / Apply")
                }
            }
        }
    }
}

@Composable
private fun DiffTypeBadge(type: DiffChangeType) {
    val (text, color) = when (type) {
        DiffChangeType.ADD -> "ADD" to Color(ComposeMigrationConstants.COLOR_PASS)
        DiffChangeType.REMOVE -> "REMOVE" to Color(ComposeMigrationConstants.COLOR_FAIL)
        DiffChangeType.MODIFY -> "MODIFY" to Color(ComposeMigrationConstants.COLOR_WARN)
    }
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun CodePreviewBox(
    title: String,
    lines: List<String>,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        lines.forEach { line ->
            Text(
                text = line,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = textColor
            )
        }
    }
}

// ============================================================
// KMP Wizard Tab Content / KMP向导Tab内容
// ============================================================
@Composable
private fun KmpWizardTabContent(
    state: ComposeMigrationState,
    onIntent: (ComposeMigrationIntent) -> Unit
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // Stepper / 分步器
        KmpStepper(
            currentStep = state.kmpWizardStep,
            onStepClick = { onIntent(ComposeMigrationIntent.UpdateKmpWizardStep(it)) }
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        // Step Content / 各步骤内容
        when (state.kmpWizardStep) {
            0 -> KmpStep0_Detect(state = state, onIntent = onIntent)
            1 -> KmpStep1_Configure(state = state, onIntent = onIntent)
            2 -> KmpStep2_Preview(state = state)
            3 -> KmpStep3_Execute(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun KmpStepper(currentStep: Int, onStepClick: (Int) -> Unit) {
    val steps = listOf("检测", "配置", "预览", "执行")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, label ->
            val isActive = index == currentStep
            val isCompleted = index < currentStep
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { if (index <= currentStep) onStepClick(index) }
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when {
                        isActive -> Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)
                        isCompleted -> Color(ComposeMigrationConstants.COLOR_PASS)
                        else -> Color(0xFFE2E8F0)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = if (isActive) Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY) else Color(0xFF64748B)
                )
            }
            
            if (index < steps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterVertically),
                    color = if (isCompleted) Color(ComposeMigrationConstants.COLOR_PASS) else Color(0xFFE2E8F0),
                    thickness = 2.dp
                )
            }
        }
    }
}

@Composable
private fun KmpStep0_Detect(
    state: ComposeMigrationState,
    onIntent: (ComposeMigrationIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Step 1: 检测当前KMP项目结构 / Detect Current KMP Project Structure",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "点击「下一步」开始检测项目中的KMP模块结构\nClick Next to detect KMP module structure in your project",
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onIntent(ComposeMigrationIntent.KmpWizardNextStep) },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)
                )
            ) {
                Text("下一步 / Next")
            }
        }
    }
}

@Composable
private fun KmpStep1_Configure(
    state: ComposeMigrationState,
    onIntent: (ComposeMigrationIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Step 2: 配置模块名称 / Configure Module Names",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = state.kmpAndroidAppName,
                onValueChange = { onIntent(ComposeMigrationIntent.UpdateAndroidAppName(it)) },
                label = { Text("androidApp 模块名 / androidApp Module Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.kmpComposeAppName,
                onValueChange = { onIntent(ComposeMigrationIntent.UpdateComposeAppName(it)) },
                label = { Text("composeApp 模块名 / composeApp Module Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (state.kmpDetectedModules.isNotEmpty()) {
                Text("检测到的模块 / Detected Modules:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                state.kmpDetectedModules.forEach { module ->
                    Text("• $module", color = Color(0xFF64748B))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Button(
                onClick = { onIntent(ComposeMigrationIntent.KmpWizardNextStep) },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)
                )
            ) {
                Text("下一步 / Next")
            }
        }
    }
}

@Composable
private fun KmpStep2_Preview(state: ComposeMigrationState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Step 3: 预览生成的目录结构 / Preview Generated Directory Structure",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Directory tree preview / 目录树预览
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = state.kmpPreviewStructure.ifEmpty { "预览中… / Generating preview…" },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* Will be handled by KmpWizardNextStep which triggers execute */ },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)
                )
            ) {
                Text("确认并执行 / Confirm & Execute")
            }
        }
    }
}

@Composable
private fun KmpStep3_Execute(
    state: ComposeMigrationState,
    onIntent: (ComposeMigrationIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Step 4: 确认并执行 / Confirm & Execute",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "KMP模块拆分即将执行，请确保：\nKMP module split is about to execute, please ensure:\n\n" +
                            "1. 已备份项目 / Project is backed up\n" +
                            "2. 没有未提交的更改 / No uncommitted changes\n" +
                            "3. 模块名称已确认 / Module names are confirmed",
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onIntent(ComposeMigrationIntent.ExecuteKmpWizard) },
                    enabled = !state.scanInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(ComposeMigrationConstants.COLOR_FAIL)
                    )
                ) {
                    if (state.scanInProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("执行拆分 / Execute Split")
                    }
                }
            }
        }
    }
}

// ============================================================
// KSP Migration Tab Content / KSP迁移Tab内容
// ============================================================
@Composable
private fun KspMigrationTabContent(
    state: ComposeMigrationState,
    onIntent: (ComposeMigrationIntent) -> Unit
) {
    if (state.kspMigrations.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.Transform,
            message = "暂无KSP迁移项\nNo KSP migrations needed\n\n或请先完成项目扫描\nOr run project scan first"
        )
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                text = "kapt → KSP 迁移清单 / kapt → KSP Migration List",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "以下kapt依赖需要迁移到KSP\nThe following kapt dependencies need migration to KSP",
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            state.kspMigrations.forEach { migration ->
                KspMigrationCard(migration = migration)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun KspMigrationCard(migration: KspMigrationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = migration.kaptDependency,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (migration.isBreakingChange) {
                    Surface(
                        color = Color(ComposeMigrationConstants.COLOR_WARN).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "破坏性变更",
                            fontSize = 10.sp,
                            color = Color(ComposeMigrationConstants.COLOR_WARN),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("替换为 / Replace with:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                text = migration.suggestedProcessor,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            Text("位置: ${migration.fileLocation}:${migration.lineNumber}", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

// ============================================================
// API Replace Tab Content / API替换Tab内容
// ============================================================
@Composable
private fun ApiReplaceTabContent(
    state: ComposeMigrationState,
    onIntent: (ComposeMigrationIntent) -> Unit
) {
    if (state.apiMigrations.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.Code,
            message = "暂无API替换项\nNo API replacements needed\n\n或请先完成项目扫描\nOr run project scan first"
        )
    } else {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                text = "applicationVariants → androidComponents API 替换",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            state.apiMigrations.forEach { migration ->
                ApiMigrationCard(migration = migration)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ApiMigrationCard(migration: ApiMigrationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${migration.filePath}:${migration.lineNumber}",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("当前代码 / Current:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFEE2E2),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = migration.currentCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("替换方案 / Replacement:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = migration.suggestedReplacement,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFF16A34A),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(migration.description, fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}

// ============================================================
// Kotlin Compliance Tab Content / Kotlin合规Tab内容
// ============================================================
@Composable
private fun KotlinComplianceTabContent(
    state: ComposeMigrationState,
    onIntent: (ComposeMigrationIntent) -> Unit
) {
    val result = state.kotlinComplianceResult
    
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        if (result == null) {
            EmptyStateMessage(
                icon = Icons.Default.CheckCircle,
                message = "暂无Kotlin合规数据\nNo Kotlin compliance data\n\n请先完成项目扫描\nRun project scan first"
            )
        } else {
            // Overall Status / 总体状态
            KotlinComplianceStatusCard(result = result)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Redundant Declarations / 冗余声明列表
            if (result.redundantPluginDeclarations.isNotEmpty()) {
                Text(
                    text = "冗余Plugin声明 / Redundant Plugin Declarations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                result.redundantPluginDeclarations.forEach { decl ->
                    KotlinPluginCard(declaration = decl)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(ComposeMigrationConstants.COLOR_PASS).copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(ComposeMigrationConstants.COLOR_PASS)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "无冗余kotlin-android plugin声明\nNo redundant kotlin-android plugin declarations",
                            color = Color(ComposeMigrationConstants.COLOR_PASS)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KotlinComplianceStatusCard(result: KotlinComplianceResult) {
    val color = when (result.overallStatus) {
        ComplianceStatus.PASS -> Color(ComposeMigrationConstants.COLOR_PASS)
        ComplianceStatus.WARN -> Color(ComposeMigrationConstants.COLOR_WARN)
        ComplianceStatus.FAIL -> Color(ComposeMigrationConstants.COLOR_FAIL)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (result.overallStatus == ComplianceStatus.PASS) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = when (result.overallStatus) {
                        ComplianceStatus.PASS -> "Kotlin 合规 / Kotlin Compliant"
                        ComplianceStatus.WARN -> "Kotlin 警告 / Kotlin Warnings"
                        ComplianceStatus.FAIL -> "Kotlin 不合规 / Kotlin Non-Compliant"
                    },
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = if (result.hasRedundantKotlinPlugin) {
                        "发现 ${result.redundantPluginDeclarations.size} 个冗余声明\n${result.redundantPluginDeclarations.size} redundant declarations found"
                    } else {
                        "AGP 9 内置Kotlin支持，无冗余声明\nAGP 9 has built-in Kotlin support, no redundant declarations"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun KotlinPluginCard(declaration: PluginDeclaration) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = declaration.pluginId,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${declaration.filePath}:${declaration.lineNumber}",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = declaration.suggestedAction,
                fontSize = 12.sp,
                color = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)
            )
        }
    }
}

// ============================================================
// Verification Tab Content / 验证套件Tab内容
// ============================================================
@Composable
private fun VerificationTabContent(
    state: ComposeMigrationState,
    onIntent: (ComposeMigrationIntent) -> Unit
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // Run Verification Button / 运行验证按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "升级验证套件 / Upgrade Verification Suite",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { onIntent(ComposeMigrationIntent.RunVerification) },
                enabled = !state.scanInProgress,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(ComposeMigrationConstants.THEME_COLOR_PRIMARY)
                )
            ) {
                if (state.scanInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("运行验证 / Run Verification")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        // Verification Checklist / 验证清单
        state.verificationItems.forEach { item ->
            VerificationItemCard(
                item = item,
                onToggle = { onIntent(ComposeMigrationIntent.ToggleVerificationItem(item.id)) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Summary / 汇总
        val passed = state.verificationItems.count { it.status == VerificationStatus.PASS }
        val total = state.verificationItems.size
        if (total > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "验证进度: $passed / $total 项通过",
                        fontWeight = FontWeight.Bold,
                        color = if (passed == total) Color(ComposeMigrationConstants.COLOR_PASS) else Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
private fun VerificationItemCard(
    item: VerificationItem,
    onToggle: () -> Unit
) {
    val statusColor = when (item.status) {
        VerificationStatus.PENDING -> Color(0xFF94A3B8)
        VerificationStatus.PASS -> Color(ComposeMigrationConstants.COLOR_PASS)
        VerificationStatus.FAIL -> Color(ComposeMigrationConstants.COLOR_FAIL)
        VerificationStatus.SKIP -> Color(0xFF94A3B8)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) statusColor.copy(alpha = 0.1f) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = statusColor
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isChecked) statusColor else Color(0xFF1E293B)
                )
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            VerificationStatusBadge(status = item.status)
        }
    }
}

@Composable
private fun VerificationStatusBadge(status: VerificationStatus) {
    val (text, color) = when (status) {
        VerificationStatus.PENDING -> "待检查" to Color(0xFF94A3B8)
        VerificationStatus.PASS -> "通过" to Color(ComposeMigrationConstants.COLOR_PASS)
        VerificationStatus.FAIL -> "失败" to Color(ComposeMigrationConstants.COLOR_FAIL)
        VerificationStatus.SKIP -> "跳过" to Color(0xFF94A3B8)
    }
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

// ============================================================
// Empty State / 空状态
// ============================================================
@Composable
private fun EmptyStateMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFCBD5E1)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = Color(0xFF94A3B8)
        )
    }
}
