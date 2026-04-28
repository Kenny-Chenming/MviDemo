package com.mvi.kenny.feature.memorylimits

// ================================================================
// MemoryLimitsScreen — Android 17 App Memory Limits 开发工具包 Screen
// ================================================================
// CLI-style Screen for Android 17 per-app memory limits toolkit.
//
// PRD-194: Android 17 App Memory Limits 开发工具包
// Design Reference: memory/agency/designs/PRD-194-Android-17-App-Memory-Limits-开发工具包.md
//
// This screen provides a terminal-like interface for:
//   - Memory Risk Scanner: Color-coded risk assessment per device tier
//   - Memory Budget Calculator: Device RAM → per-app limit mapping
//   - Diagnostic Workflow: Detection → Heap Dump → Analysis → Fix → Verification
//   - CI Integration Templates: GitHub Actions memory simulation
//
// Visual Style:
//   - Dark terminal theme (monospace font, dark background)
//   - Color-coded output by risk level (CRITICAL=red, HIGH=orange, MEDIUM=yellow, LOW=green)
//   - Risk dashboard with device tier cards
//   - Progress indicators for scan and diagnostic workflows
// ================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Color Palette — 颜色规范（Design Reference）
// ================================================================
private object MemoryLimitsColors {
    // Primary palette from design spec
    val DeepPurpleBlue = Color(0xFF1E1E3F)    // #1E1E3F - 深紫蓝（背景）
    val BrightPurple = Color(0xFF7C3AED)        // #7C3AED - 亮紫（强调）
    val SafeGreen = Color(0xFF3FB950)           // #3FB950 - 安全绿
    val WarningYellow = Color(0xFFF59E0B)      // #F59E0B - 警告黄
    val DangerRed = Color(0xFFEF4444)           // #EF4444 - 危险红
    val CriticalRed = Color(0xFF6E1A1A)         // #6E1A1A - 极高风险红（深色背景）

    // Terminal background colors
    val TerminalBg = Color(0xFF0D1117)          // Terminal dark background
    val TerminalGreen = Color(0xFF3FB950)       // Terminal green text
    val TerminalYellow = Color(0xFFF59E0B)      // Terminal yellow text
    val TerminalRed = Color(0xFFEF4444)         // Terminal red text
    val TerminalDim = Color(0xFF8B949E)         // Dimmed terminal text
}

// ================================================================
// TerminalLine — 终端输出行
// ================================================================
/**
 * ============================================================
 * TerminalLine — 终端输出行数据类
 * ================================================================
 * Represents a single line of terminal output.
 *
 * @property text Output text content
 * @property riskLevel Risk level for color coding
 * @property timestamp Timestamp of the output line
 */
private data class TerminalLine(
    val text: String,
    val riskLevel: RiskLevel = RiskLevel.UNKNOWN,
    val timestamp: Long = System.currentTimeMillis()
)

// ================================================================
// MemoryLimitsScreen — Main Screen Composable
// ================================================================
/**
 * ============================================================
 * MemoryLimitsScreen — 主屏幕组件
 * ================================================================
 * Terminal-style screen for Android 17 Memory Limits toolkit.
 *
 * Features:
 *   - Risk Assessment Dashboard: Cards for each device RAM tier
 *   - Scan Control: Start/Cancel scan buttons
 *   - Terminal Output: Real-time scan progress
 *   - Budget Calculator: Device RAM → memory budget mapping
 *   - Diagnostic Workflow: Step-by-step memory diagnostic
 *
 * @param viewModel MemoryLimitsViewModel instance
 */
@Composable
fun MemoryLimitsScreen(
    viewModel: MemoryLimitsViewModel
) {
    // Collect states from ViewModel
    // 从 ViewModel 收集状态
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Local terminal output history
    // 本地终端输出历史
    var terminalOutput by remember { mutableStateOf<List<TerminalLine>>(emptyList()) }

    // Lifecycle-aware effect collection
    // 生命周期感知的副作用收集
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collectLatest { effect ->
                when (effect) {
                    is MemoryLimitsEffect.TerminalOutput -> {
                        terminalOutput = terminalOutput + TerminalLine(
                            text = effect.message,
                            riskLevel = effect.riskLevel
                        )
                    }
                    is MemoryLimitsEffect.ScanCompleted -> {
                        terminalOutput = terminalOutput + TerminalLine(
                            text = "🎉 扫描完成！耗时: ${effect.durationMs}ms",
                            riskLevel = effect.assessments.maxByOrNull { it.riskLevel.priority }?.riskLevel ?: RiskLevel.LOW
                        )
                    }
                    is MemoryLimitsEffect.ReportGenerated -> {
                        terminalOutput = terminalOutput + TerminalLine(
                            text = "📄 报告已生成: ${effect.filePath}",
                            riskLevel = RiskLevel.LOW
                        )
                    }
                    is MemoryLimitsEffect.DiagnosticCompleted -> {
                        terminalOutput = terminalOutput + TerminalLine(
                            text = "🔬 诊断完成: ${effect.reportPath}",
                            riskLevel = RiskLevel.LOW
                        )
                    }
                    is MemoryLimitsEffect.Error -> {
                        terminalOutput = terminalOutput + TerminalLine(
                            text = "❌ 错误: ${effect.message}",
                            riskLevel = RiskLevel.HIGH
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    // Main layout
    // 主布局
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MemoryLimitsColors.DeepPurpleBlue,
                        Color(0xFF121225)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Header
        // 头部
        MemoryLimitsHeader()

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        // 内容区
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left: Risk Assessment Dashboard
            // 左：风险评估仪表盘
            Column(
                modifier = Modifier.weight(0.55f)
            ) {
                RiskAssessmentSection(
                    state = state,
                    onScanClick = {
                        viewModel.processIntent(MemoryLimitsIntent.StartScan(
                            sourceDir = "/path/to/project",
                            packageName = "com.example.app"
                        ))
                    },
                    onCancelClick = {
                        viewModel.processIntent(MemoryLimitsIntent.CancelScan)
                    }
                )
            }

            // Right: Terminal + Budget
            // 右：终端输出 + 预算计算器
            Column(
                modifier = Modifier.weight(0.45f)
            ) {
                BudgetCalculatorSection(
                    state = state,
                    onCalculateClick = { tier, appType ->
                        viewModel.processIntent(MemoryLimitsIntent.CalculateBudget(
                            deviceRamGb = tier.ramGb,
                            appType = appType
                        ))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Terminal Output
        // 终端输出
        TerminalOutputSection(
            terminalOutput = terminalOutput,
            isScanning = state.scanState.isScanning,
            progress = state.scanState.progress,
            phaseDescription = state.scanState.currentPhaseDescription
        )
    }
}

// ================================================================
// MemoryLimitsHeader — 头部组件
// ================================================================
/**
 * ============================================================
 * MemoryLimitsHeader — 头部组件
 * ================================================================
 * Header with title and description.
 */
@Composable
private fun MemoryLimitsHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MemoryLimitsColors.BrightPurple.copy(alpha = 0.3f),
                            MemoryLimitsColors.DeepPurpleBlue
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = MemoryLimitsColors.BrightPurple,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Android 17 Memory Limits Toolkit",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Per-App RAM Limits 开发工具包 — PRD-194",
                        color = MemoryLimitsColors.TerminalDim,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Android 17 引入 per-app device memory limits，系统根据设备总 RAM 为每个 App 设定内存上限。",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

// ================================================================
// RiskAssessmentSection — 风险评估区域
// ================================================================
/**
 * ============================================================
 * RiskAssessmentSection — 风险评估区域
 * ================================================================
 * Shows risk assessment cards for each device RAM tier.
 *
 * @param state Current MemoryLimitsState
 * @param onScanClick Callback when scan button is clicked
 * @param onCancelClick Callback when cancel button is clicked
 */
@Composable
private fun RiskAssessmentSection(
    state: MemoryLimitsState,
    onScanClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MemoryLimitsColors.TerminalBg.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Section header
            // 区域头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = MemoryLimitsColors.BrightPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📊 Risk Assessment / 风险评估",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Scan/Cancel button
                // 扫描/取消按钮
                if (state.scanState.isScanning) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MemoryLimitsColors.TerminalRed
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onScanClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MemoryLimitsColors.BrightPurple
                        )
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan / 扫描", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar if scanning
            // 扫描时显示进度条
            if (state.scanState.isScanning) {
                LinearProgressIndicator(
                    progress = { state.scanState.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MemoryLimitsColors.BrightPurple,
                    trackColor = MemoryLimitsColors.TerminalDim.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${state.scanState.currentPhaseDescription} (${state.scanState.progress}%)",
                    color = MemoryLimitsColors.TerminalDim,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Risk level summary
            // 风险等级摘要
            if (state.riskAssessments.isNotEmpty()) {
                OverallRiskBadge(state.overallRiskLevel, state.overallRiskDescription)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Tier cards
            // 分层卡片
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.riskAssessments) { assessment ->
                    TierRiskCard(assessment = assessment)
                }
            }
        }
    }
}

// ================================================================
// OverallRiskBadge — 总体风险徽章
// ================================================================
/**
 * ============================================================
 * OverallRiskBadge — 总体风险徽章
 * ================================================================
 * Shows overall risk level with description.
 */
@Composable
private fun OverallRiskBadge(riskLevel: RiskLevel, description: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = riskLevel.color.copy(alpha = 0.15f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = riskLevel.color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = riskLevel.emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${riskLevel.emoji} ${riskLevel.displayName} / ${riskLevel.displayNameEn}",
                    color = riskLevel.color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ================================================================
// TierRiskCard — 分层风险卡片
// ================================================================
/**
 * ============================================================
 * TierRiskCard — 分层风险卡片
 * ================================================================
 * Shows risk assessment for a specific device RAM tier.
 *
 * @param assessment Risk assessment for this tier
 */
@Composable
private fun TierRiskCard(assessment: RiskAssessment) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF161B22)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = assessment.riskLevel.color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Tier header
            // 分层头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = assessment.riskLevel.color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = assessment.tier.displayName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "${assessment.riskLevel.emoji} ${assessment.riskLevel.displayName}",
                    color = assessment.riskLevel.color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Memory usage bar
            // 内存使用条
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${assessment.appEstimatedMemoryMb} MB / ${assessment.limitMb} MB",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${String.format("%.1f", assessment.usagePercent)}%",
                        color = assessment.riskLevel.color,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (assessment.usagePercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .border(
                            width = 1.dp,
                            color = assessment.riskLevel.color.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(3.dp)
                        ),
                    color = assessment.riskLevel.color,
                    trackColor = Color.Transparent
                )
            }

            // Headroom info
            // 剩余空间信息
            Spacer(modifier = Modifier.height(6.dp))
            Row {
                Text(
                    text = "剩余空间: ",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
                Text(
                    text = "${assessment.headroomMb} MB",
                    color = if (assessment.headroomMb < 100) MemoryLimitsColors.TerminalRed else MemoryLimitsColors.SafeGreen,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Findings count
            // 发现问题数量
            if (assessment.findings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "相关问题: ${assessment.findings.size}个",
                    color = MemoryLimitsColors.TerminalYellow,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ================================================================
// BudgetCalculatorSection — 内存预算计算器区域
// ================================================================
/**
 * ============================================================
 * BudgetCalculatorSection — 内存预算计算器区域
 * ================================================================
 * Shows memory budget calculator for device RAM tiers.
 *
 * @param state Current MemoryLimitsState
 * @param onCalculateClick Callback when calculate button is clicked
 */
@Composable
private fun BudgetCalculatorSection(
    state: MemoryLimitsState,
    onCalculateClick: (DeviceRamTier, AppType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MemoryLimitsColors.TerminalBg.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Section header
            // 区域头部
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = MemoryLimitsColors.BrightPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "💰 Budget Calculator / 预算计算器",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tier selector
            // 分层选择器
            Text(
                text = "Device RAM Tier / 设备 RAM 分层:",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Tier chips
            // 分层选择 chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DeviceRamTier.entries.take(3).forEach { tier ->
                    FilterChip(
                        selected = state.selectedTier == tier,
                        onClick = { onCalculateClick(tier, state.selectedAppType) },
                        label = { Text("${tier.ramGb}GB", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MemoryLimitsColors.BrightPurple.copy(alpha = 0.3f),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DeviceRamTier.entries.drop(3).forEach { tier ->
                    FilterChip(
                        selected = state.selectedTier == tier,
                        onClick = { onCalculateClick(tier, state.selectedAppType) },
                        label = { Text("${tier.ramGb}GB", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MemoryLimitsColors.BrightPurple.copy(alpha = 0.3f),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App type selector
            // App 类型选择器
            Text(
                text = "App Type / App 类型:",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            val appTypes = listOf(AppType.GENERAL, AppType.MEDIA, AppType.GAME)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                appTypes.forEach { appType ->
                    FilterChip(
                        selected = state.selectedAppType == appType,
                        onClick = { onCalculateClick(state.selectedTier, appType) },
                        label = { Text(appType.displayName, fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MemoryLimitsColors.BrightPurple.copy(alpha = 0.3f),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Budget result
            // 预算结果
            state.budgets[state.selectedTier]?.let { budget ->
                BudgetResultCard(budget = budget)
            }

            Spacer(modifier = Modifier.weight(1f))

            // CI Integration hint
            // CI 集成提示
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MemoryLimitsColors.BrightPurple.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MemoryLimitsColors.BrightPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CI 模板可用: GitHub Actions 内存模拟",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ================================================================
// BudgetResultCard — 预算结果卡片
// ================================================================
/**
 * ============================================================
 * BudgetResultCard — 预算结果卡片
 * ================================================================
 * Shows memory budget breakdown for a device tier.
 *
 * @param budget Memory budget for this tier
 */
@Composable
private fun BudgetResultCard(budget: MemoryBudget) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF161B22)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "📋 ${budget.tier.displayName} Memory Budget",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main budget values
            // 主要预算值
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BudgetValue(
                    label = "Recommended",
                    value = "${budget.recommendedLimitMb} MB",
                    color = MemoryLimitsColors.SafeGreen
                )
                BudgetValue(
                    label = "Warning",
                    value = "${budget.warningThresholdMb} MB",
                    color = MemoryLimitsColors.WarningYellow
                )
                BudgetValue(
                    label = "Critical",
                    value = "${budget.criticalThresholdMb} MB",
                    color = MemoryLimitsColors.DangerRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Module breakdown
            // 模块分配
            Text(
                text = "Module Breakdown / 模块分配:",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            budget.moduleBudgets.forEach { (module, allocation) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = module,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "$allocation MB",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ================================================================
// BudgetValue — 预算值显示
// ================================================================
/**
 * ============================================================
 * BudgetValue — 预算值显示组件
 * ================================================================
 */
@Composable
private fun BudgetValue(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 9.sp
        )
    }
}

// ================================================================
// TerminalOutputSection — 终端输出区域
// ================================================================
/**
 * ============================================================
 * TerminalOutputSection — 终端输出区域
 * ================================================================
 * Shows real-time terminal output with color-coded messages.
 *
 * @param terminalOutput List of terminal output lines
 * @param isScanning Whether scan is in progress
 * @param progress Scan progress percentage
 * @param phaseDescription Current phase description
 */
@Composable
private fun TerminalOutputSection(
    terminalOutput: List<TerminalLine>,
    isScanning: Boolean,
    progress: Int,
    phaseDescription: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D1117)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Terminal header
            // 终端头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = MemoryLimitsColors.TerminalGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Terminal Output",
                        color = MemoryLimitsColors.TerminalGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                if (isScanning) {
                    Text(
                        text = "$progress%",
                        color = MemoryLimitsColors.TerminalYellow,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Terminal content
            // 终端内容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                terminalOutput.takeLast(15).forEach { line ->
                    val textColor = when (line.riskLevel) {
                        RiskLevel.CRITICAL -> MemoryLimitsColors.TerminalRed
                        RiskLevel.HIGH -> MemoryLimitsColors.TerminalRed.copy(alpha = 0.8f)
                        RiskLevel.MEDIUM -> MemoryLimitsColors.TerminalYellow
                        RiskLevel.LOW -> MemoryLimitsColors.TerminalGreen
                        RiskLevel.UNKNOWN -> Color.White.copy(alpha = 0.7f)
                    }

                    Text(
                        text = line.text,
                        color = textColor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }

                // Cursor animation when scanning
                // 扫描时显示光标动画
                if (isScanning) {
                    Row {
                        Text(
                            text = "> $phaseDescription ",
                            color = MemoryLimitsColors.TerminalYellow,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        var cursorVisible by remember { mutableStateOf(true) }
                        LaunchedEffect(Unit) {
                            while (isScanning) {
                                cursorVisible = !cursorVisible
                                delay(500)
                            }
                        }
                        Text(
                            text = if (cursorVisible) "█" else " ",
                            color = MemoryLimitsColors.TerminalYellow,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
