package com.mvi.kenny.feature.panda4tools

// ================================================================
// Panda4ToolsScreen — Android Studio Panda 4 AI Agent 开发工具包 UI
// ================================================================
// Complete UI for the Panda 4 AI Agent Toolkit.
//
// PRD-158: Android Studio Panda 4 AI Agent 开发工具包
// Design Reference: memory/agency/designs/PRD-158-Android-Studio-Panda-4-AI-Agent-开发工具包.md
//
// 八大工具模块：
// 1. Planning Mode 计划评审
// 2. NEP 链式编辑验证
// 3. Planning Git 集成
// 4. Planning 审计日志
// 5. NEP 代码审查联动
// 6. Gemini API Starter CLI
// 7. Google One 配额监控
// 8. Panda 4 vs Panda 3 对比
// ================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.base.TopBarConfig

private val PandaPrimary = Color(0xFF6750A4)
private val SuccessGreen = Color(0xFF4CAF50)
private val WarningYellow = Color(0xFFFFC107)
private val ErrorRed = Color(0xFFF44336)
private val InfoBlue = Color(0xFF2196F3)

private fun getModuleIcon(module: ToolModule): ImageVector {
    return when (module) {
        ToolModule.PlanningReview -> Icons.Default.FactCheck
        ToolModule.NepVerify -> Icons.Default.CheckCircle
        ToolModule.PlanningGit -> Icons.Default.AccountTree
        ToolModule.PlanningAudit -> Icons.Default.History
        ToolModule.NepReview -> Icons.Default.RateReview
        ToolModule.GeminiStarter -> Icons.Default.AutoAwesome
        ToolModule.QuotaMonitor -> Icons.Default.PieChart
        ToolModule.PandaCompare -> Icons.Default.Compare
    }
}

@Composable
fun Panda4ToolsScreen(
    state: Panda4ToolsState,
    onIntent: (Panda4ToolsIntent) -> Unit,
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    LaunchedEffect(state.selectedModule) {
        onUpdateTopBar(TopBarConfig(title = "Panda 4 AI Tools", actions = emptyList()))
    }
    val snackbarHostState = remember { SnackbarHostState() }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ModuleSelectorRow(
                selectedModule = state.selectedModule,
                onModuleSelected = { onIntent(Panda4ToolsIntent.SelectModule(it)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state.selectedModule) {
                    ToolModule.PlanningReview -> PlanningReviewContent(state, onIntent)
                    ToolModule.NepVerify -> NepVerifyContent(state, onIntent)
                    ToolModule.PlanningGit -> PlanningGitContent(state, onIntent)
                    ToolModule.PlanningAudit -> PlanningAuditContent(state, onIntent)
                    ToolModule.NepReview -> NepReviewContent(state, onIntent)
                    ToolModule.GeminiStarter -> GeminiStarterContent(state, onIntent)
                    ToolModule.QuotaMonitor -> QuotaMonitorContent(state, onIntent)
                    ToolModule.PandaCompare -> PandaCompareContent(state, onIntent)
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ModuleSelectorRow(selectedModule: ToolModule, onModuleSelected: (ToolModule) -> Unit) {
    Column {
        Text(text = "Panda 4 AI Agent 工具包", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ToolModule.entries) { module ->
                FilterChip(
                    selected = module == selectedModule,
                    onClick = { onModuleSelected(module) },
                    label = { Text(module.title, fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = getModuleIcon(module), contentDescription = null, modifier = Modifier.size(18.dp)) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PandaPrimary, selectedLabelColor = Color.White, selectedLeadingIconColor = Color.White)
                )
            }
        }
    }
}

@Composable
private fun PlanningReviewContent(state: Panda4ToolsState, onIntent: (Panda4ToolsIntent) -> Unit) {
    var planFilePath by remember { mutableStateOf(state.planFilePath) }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Planning Mode 计划评审", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("解析 Planning Mode 计划，输出结构化评审意见", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(value = planFilePath, onValueChange = { planFilePath = it }, label = { Text("计划文件路径") }, placeholder = { Text("例如：./planning-output.md") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onIntent(Panda4ToolsIntent.StartReview(planFilePath)) }, enabled = planFilePath.isNotBlank() && state.reviewStatus != ReviewStatus.Reviewing) {
                        if (state.reviewStatus == ReviewStatus.Reviewing) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)) }
                        Text("开始评审")
                    }
                    OutlinedButton(onClick = { onIntent(Panda4ToolsIntent.ClearReview) }) { Text("清除") }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (state.reviewStatus == ReviewStatus.Reviewing) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.1f))) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.width(12.dp)); Text("正在解析计划文件并分析...") }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        state.reviewReport?.let { report -> ReviewReportCard(report = report) }
    }
}

@Composable
private fun ReviewReportCard(report: PlanReview) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("评审报告", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val (badgeColor, badgeText) = when (report.overallRecommendation) { "Approved" -> SuccessGreen to "✓ 批准"; "Needs Revision" -> WarningYellow to "⚠ 需修改"; else -> ErrorRed to "✗ 拒绝" }
                Box(modifier = Modifier.background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text(text = badgeText, color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            }
            Text("生成时间：${report.generatedAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("复杂度评分：", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(8.dp))
                val complexityColor = when { report.complexityScore >= 8 -> ErrorRed; report.complexityScore >= 6 -> WarningYellow; else -> SuccessGreen }
                Text(text = "${report.complexityScore}/10", color = complexityColor, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (report.risks.isNotEmpty()) {
                Text("风险点（${report.risks.size}）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                report.risks.forEach { risk -> RiskPointRow(risk = risk); Spacer(modifier = Modifier.height(8.dp)) }
            }
            if (report.missedDependencies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("遗漏依赖（${report.missedDependencies.size}）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                report.missedDependencies.forEach { dep -> Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Warning, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(text = dep, fontSize = 13.sp) } }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("可测试性评分", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Text(text = "${report.testabilityScore.score}/10", fontWeight = FontWeight.Bold, color = PandaPrimary); Spacer(modifier = Modifier.width(12.dp)); Text(text = report.testabilityScore.analysis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (report.testabilityScore.missingTests.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("缺失测试：", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                report.testabilityScore.missingTests.forEach { test -> Text("- $test", fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp)) }
            }
        }
    }
}

@Composable
private fun RiskPointRow(risk: RiskPoint) {
    val severityColor = when (risk.severity) { "high" -> ErrorRed; "medium" -> WarningYellow; else -> InfoBlue }
    Card(modifier = Modifier.fillMaxWidth().border(1.dp, severityColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)), colors = CardDefaults.cardColors(containerColor = severityColor.copy(alpha = 0.05f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(severityColor, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(text = risk.severity.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = risk.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = risk.description, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "建议：${risk.suggestion}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun NepVerifyContent(state: Panda4ToolsState, onIntent: (Panda4ToolsIntent) -> Unit) {
    val demoSequences = remember {
        listOf(
            NepSequence(1, "app/src/main/java/com/example/MainActivity.kt", "modify", "添加 ViewModel 初始化代码"),
            NepSequence(2, "app/src/main/java/com/example/MainViewModel.kt", "insert", "添加 StateFlow 状态定义"),
            NepSequence(3, "app/src/main/java/com/example/Repository.kt", "insert", "添加数据仓库方法"),
            NepSequence(4, "app/build.gradle.kts", "modify", "添加 Compose 依赖"),
            NepSequence(5, "app/src/main/AndroidManifest.xml", "modify", "更新权限配置")
        )
    }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("NEP 链式编辑验证", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("验证 NEP 预测序列的语法正确性、引用一致性和构建可编译性", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onIntent(Panda4ToolsIntent.LoadNepSequence(demoSequences)) }, enabled = state.nepSequences.isEmpty()) { Text("加载示例序列") }
                    Button(onClick = { onIntent(Panda4ToolsIntent.StartVerification) }, enabled = state.nepSequences.isNotEmpty() && state.verifyStatus != VerifyStatus.Verifying, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                        if (state.verifyStatus == VerifyStatus.Verifying) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)) }
                        Text("开始验证")
                    }
                    OutlinedButton(onClick = { onIntent(Panda4ToolsIntent.ClearVerification) }) { Text("清除") }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (state.nepSequences.isNotEmpty()) {
            Text("预测序列（${state.nepSequences.size} 步）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            state.nepSequences.forEach { seq -> NepSequenceRow(sequence = seq, verifyReport = state.verifyReport); Spacer(modifier = Modifier.height(8.dp)) }
        }
        state.verifyReport?.let { report -> Spacer(modifier = Modifier.height(16.dp)); VerifyReportCard(report = report) }
    }
}

@Composable
private fun NepSequenceRow(sequence: NepSequence, verifyReport: VerifyReport?) {
    val statusIcon = when { verifyReport == null -> Icons.Default.Pending; verifyReport.errors.any { it.stepNumber == sequence.stepNumber } -> Icons.Default.Close; else -> Icons.Default.CheckCircle }
    val statusColor = when { verifyReport == null -> MaterialTheme.colorScheme.onSurfaceVariant; verifyReport.errors.any { it.stepNumber == sequence.stepNumber } -> ErrorRed; else -> SuccessGreen }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).background(PandaPrimary, CircleShape), contentAlignment = Alignment.Center) { Text(text = "${sequence.stepNumber}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) { Text(text = sequence.description, fontWeight = FontWeight.Medium, fontSize = 13.sp); Text(text = sequence.filePath, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun VerifyReportCard(report: VerifyReport) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (report.overallStatus == VerifyStatus.Passed) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = if (report.overallStatus == VerifyStatus.Passed) Icons.Default.CheckCircle else Icons.Default.Error, contentDescription = null, tint = if (report.overallStatus == VerifyStatus.Passed) SuccessGreen else ErrorRed, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (report.overallStatus == VerifyStatus.Passed) "验证通过" else "验证失败", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column { Text("通过步骤", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = "${report.passedSteps}", fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 20.sp) }
                Column { Text("失败步骤", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = "${report.failedSteps}", fontWeight = FontWeight.Bold, color = if (report.failedSteps > 0) ErrorRed else SuccessGreen, fontSize = 20.sp) }
                Column { Text("可编译", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = if (report.isCompilable) "是" else "否", fontWeight = FontWeight.Bold, color = if (report.isCompilable) SuccessGreen else ErrorRed, fontSize = 20.sp) }
            }
            if (report.errors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("错误详情", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                report.errors.forEach { error ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.05f))) {
                        Column(modifier = Modifier.padding(8.dp)) { Text(text = "Step ${error.stepNumber} — ${error.errorType}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ErrorRed); Text(text = error.message, fontSize = 12.sp); Text(text = error.filePath, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
            if (report.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("警告信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                report.warnings.forEach { warning -> Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Warning, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(text = warning, fontSize = 12.sp) } }
            }
        }
    }
}

@Composable
private fun PlanningGitContent(state: Panda4ToolsState, onIntent: (Panda4ToolsIntent) -> Unit) {
    var planFilePath by remember { mutableStateOf(state.planFilePath) }
    var branchName by remember { mutableStateOf(state.branchName) }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Planning Git 集成", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("将计划内容自动写入 Git commit message 并创建分支", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(value = planFilePath, onValueChange = { planFilePath = it }, label = { Text("计划文件路径") }, placeholder = { Text("例如：./planning-output.md") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = branchName, onValueChange = { branchName = it }, label = { Text("分支名称（可选）") }, placeholder = { Text("例如：feature/planning-xxx") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { onIntent(Panda4ToolsIntent.ExecuteGitIntegration(planFilePath, branchName)) }, enabled = planFilePath.isNotBlank() && !state.isLoading) {
                    if (state.isLoading) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)) }
                    Text("执行 Git 集成")
                }
            }
        }
        state.gitIntegration?.let { result ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (result.success) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error, contentDescription = null, tint = if (result.success) SuccessGreen else ErrorRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (result.success) "集成成功" else "集成失败", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (result.success) { GitDetailRow("分支名称", result.branchName); GitDetailRow("Commit Hash", result.commitHash); GitDetailRow("计划文件", result.planFilePath) }
                    Text(text = result.message, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun GitDetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { Text(text = "$label：", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
}

@Composable
private fun PlanningAuditContent(state: Panda4ToolsState, onIntent: (Panda4ToolsIntent) -> Unit) {
    val mockEntries = remember {
        listOf(
            AuditEntry("audit-001", "2026-04-25 14:30:00", "Planning", "Agent 生成计划：实现用户登录功能", "1. 创建 LoginActivity\n2. 创建 LoginViewModel", "1. 创建 LoginActivity\n2. 创建 LoginViewModel", false),
            AuditEntry("audit-002", "2026-04-25 14:31:15", "Edit", "修改 LoginActivity.kt", "添加 ViewModel 初始化", "添加 ViewModel 初始化（包含错误处理）", true),
            AuditEntry("audit-003", "2026-04-25 14:32:00", "Build", "执行 Gradle 构建", "assembleDebug", "assembleDebug（构建成功）", false)
        )
    }
    LaunchedEffect(Unit) { if (state.auditLog.isEmpty()) { onIntent(Panda4ToolsIntent.LoadAuditLog(mockEntries)) } }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Planning 审计日志", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("导出规划→执行的完整时间线审计日志", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onIntent(Panda4ToolsIntent.ExportAuditLog("json")) }, enabled = state.auditLog.isNotEmpty() && !state.isExportingAudit) { Text("导出 JSON") }
                    OutlinedButton(onClick = { onIntent(Panda4ToolsIntent.ExportAuditLog("csv")) }, enabled = state.auditLog.isNotEmpty() && !state.isExportingAudit) { Text("导出 CSV") }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("审计时间线（${state.auditLog.size} 条记录）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        state.auditLog.forEach { entry -> AuditEntryCard(entry = entry); Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun AuditEntryCard(entry: AuditEntry) {
    val actionColor = when (entry.action) { "Planning" -> PandaPrimary; "Edit" -> InfoBlue; "Build" -> SuccessGreen; else -> MaterialTheme.colorScheme.onSurfaceVariant }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (entry.deviation) WarningYellow.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(28.dp).background(actionColor, CircleShape), contentAlignment = Alignment.Center) { Text(text = entry.action.first().toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = entry.description, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
            if (entry.deviation) { Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Warning, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(text = "偏离计划", fontSize = 11.sp, color = WarningYellow) } }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) { Text("Planner 输出", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = entry.plannerOutput, fontSize = 11.sp, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(4.dp)) }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) { Text("Agent 执行", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = entry.agentAction, fontSize = 11.sp, modifier = Modifier.background(if (entry.deviation) WarningYellow.copy(alpha = 0.1f) else SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(4.dp)) }
            }
            Text(text = entry.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun NepReviewContent(state: Panda4ToolsState, onIntent: (Panda4ToolsIntent) -> Unit) {
    var targetBranch by remember { mutableStateOf("main") }
    val demoSequences = remember {
        listOf(
            NepSequence(1, "app/src/main/java/com/example/MainActivity.kt", "modify", "添加 ViewModel 初始化代码"),
            NepSequence(2, "app/src/main/java/com/example/MainViewModel.kt", "insert", "添加 StateFlow 状态定义"),
            NepSequence(3, "app/src/main/java/com/example/Repository.kt", "insert", "添加数据仓库方法")
        )
    }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("NEP 代码审查联动", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("将 NEP 预测的 Diff 自动创建代码审查请求", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(value = targetBranch, onValueChange = { targetBranch = it }, label = { Text("目标分支") }, placeholder = { Text("例如：main, develop") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { onIntent(Panda4ToolsIntent.CreateCodeReview(demoSequences, targetBranch)) }, enabled = !state.isCreatingReview, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                    if (state.isCreatingReview) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)) }
                    Text("创建代码审查")
                }
            }
        }
        state.codeReviewRequest?.let { request ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen); Spacer(modifier = Modifier.width(8.dp)); Text(text = "代码审查已创建", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.height(12.dp))
                    CodePreviewCard(request = request)
                }
            }
        }
    }
}

@Composable
private fun CodePreviewCard(request: CodeReviewRequest) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = request.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column { Text(text = "变更步骤", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = "${request.changes.size}", fontWeight = FontWeight.Bold, color = PandaPrimary) }
                Column { Text(text = "目标分支", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = request.targetBranch, fontWeight = FontWeight.Medium) }
                Column { Text(text = "审查 URL", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = if (request.reviewUrl.isNotBlank()) "已生成" else "生成中...", fontWeight = FontWeight.Medium, color = if (request.reviewUrl.isNotBlank()) InfoBlue else MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (request.changes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "变更文件", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                request.changes.forEach { change -> Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Description, contentDescription = null, tint = PandaPrimary, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(6.dp)); Text(text = change, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
        }
    }
}

@Composable
private fun GeminiStarterContent(state: Panda4ToolsState, onIntent: (Panda4ToolsIntent) -> Unit) {
    var apiKey by remember { mutableStateOf("") }
    var promptTemplate by remember { mutableStateOf("Explain this code") }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Gemini API Starter CLI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("快速启动 Gemini API 调用，预置常用提示词模板", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Key") }, placeholder = { Text("输入你的 Gemini API Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = promptTemplate, onValueChange = { promptTemplate = it }, label = { Text("提示词模板") }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 5)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onIntent(Panda4ToolsIntent.StartGeminiCli(apiKey, promptTemplate)) }, enabled = apiKey.isNotBlank() && !state.isRunningCli, colors = ButtonDefaults.buttonColors(containerColor = PandaPrimary)) {
                        if (state.isRunningCli) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)) }
                        Text("启动 CLI")
                    }
                    OutlinedButton(onClick = { val templates = listOf("Explain this code", "Review this code for bugs", "Suggest improvements", "Write tests"); promptTemplate = templates.random() }) { Text("随机模板") }
                }
            }
        }
        state.geminiResponse?.let { response ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("响应结果", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold); Text(text = "Token: ${response.usage.totalTokens}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = response.content, style = MaterialTheme.typography.bodySmall, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(12.dp))
                }
            }
        }
    }
}

@Composable
private fun QuotaMonitorContent(state: Panda4ToolsState, onIntent: (Panda4ToolsIntent) -> Unit) {
    var email by remember { mutableStateOf(state.quotaInfo?.email ?: "") }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Google One 配额监控", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("监控 Gemini API 配额使用情况，支持阈值告警", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Google 账号邮箱") }, placeholder = { Text("your-email@gmail.com") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { onIntent(Panda4ToolsIntent.FetchQuota(email)) }, enabled = email.isNotBlank() && !state.isFetchingQuota, colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)) {
                    if (state.isFetchingQuota) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)) }
                    Text("查询配额")
                }
            }
        }
        state.quotaInfo?.let { info ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = info.email, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = "配额类型：${info.quotaType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    QuotaLevelRow(label = "日配额", used = info.dailyUsed, limit = info.dailyLimit, level = info.dailyLevel)
                    Spacer(modifier = Modifier.height(8.dp))
                    QuotaLevelRow(label = "月配额", used = info.monthlyUsed, limit = info.monthlyLimit, level = info.monthlyLevel)
                    Spacer(modifier = Modifier.height(8.dp))
                    QuotaLevelRow(label = "请求速率", used = info.rpmUsed, limit = info.rpmLimit, level = info.rpmLevel)
                    if (info.alerts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "告警通知", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        info.alerts.forEach { alert ->
                            Row(modifier = Modifier.fillMaxWidth().background(WarningYellow.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Warning, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(text = alert, fontSize = 12.sp) }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaLevelRow(label: String, used: Long, limit: Long, level: QuotaLevel) {
    val progress = if (limit > 0) (used.toFloat() / limit).coerceIn(0f, 1f) else 0f
    val progressColor = when (level) { QuotaLevel.Normal -> SuccessGreen; QuotaLevel.Warning -> WarningYellow; QuotaLevel.Critical -> ErrorRed; QuotaLevel.Exhausted -> ErrorRed }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(text = label, fontWeight = FontWeight.Medium, fontSize = 13.sp); Text(text = "${formatNumber(used)} / ${formatNumber(limit)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = progressColor, trackColor = MaterialTheme.colorScheme.surfaceVariant)
    }
}

private fun formatNumber(num: Long): String {
    return when { num >= 1_000_000 -> String.format("%.1fM", num / 1_000_000.0); num >= 1_000 -> String.format("%.1fK", num / 1_000.0); else -> num.toString() }
}

@Composable
private fun PandaCompareContent(state: Panda4ToolsState, onIntent: (Panda4ToolsIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Panda 4 vs Panda 3 对比", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Android Studio Panda 4 新功能特性与 Panda 3 的详细对比", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                Button(onClick = { onIntent(Panda4ToolsIntent.LoadFeatureComparison) }, enabled = !state.isLoadingComparison, colors = ButtonDefaults.buttonColors(containerColor = PandaPrimary)) {
                    if (state.isLoadingComparison) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(modifier = Modifier.width(8.dp)) }
                    Text("加载对比数据")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (state.featureComparison.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = PandaPrimary.copy(alpha = 0.1f))) { Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) { Text(text = "Panda 4", fontWeight = FontWeight.Bold, color = PandaPrimary) } }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) { Text(text = "Panda 3", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
            Spacer(modifier = Modifier.height(8.dp))
            state.featureComparison.forEach { feature -> FeatureComparisonCard(feature = feature); Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun FeatureComparisonCard(feature: FeatureComparison) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(if (feature.isNewInPanda4) SuccessGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(text = if (feature.isNewInPanda4) "NEW" else "IMPROVED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (feature.isNewInPanda4) SuccessGreen else PandaPrimary) }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = feature.featureName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = feature.panda4Description, style = MaterialTheme.typography.bodySmall, color = PandaPrimary, modifier = Modifier.weight(1f))
                Text(text = feature.panda3Description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            }
            if (feature.improvementPercentage.isNotBlank()) { Spacer(modifier = Modifier.height(8.dp)); Text(text = "性能提升：${feature.improvementPercentage}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen) }
        }
    }
}
