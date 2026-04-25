package com.mvi.kenny.feature.onalaarmlistener

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// =============================================================
// ScannerScreen — 引导式扫描流程
// =============================================================
/**
 * Scanner Screen / 引导式扫描流程
 *
 * 5-step guided scan flow:
 * 1. Project Selection / 项目选择
 * 2. WakeLock Scan / WakeLock 扫描
 * 3. Alarm/WorkManager Scan / Alarm/WorkManager 扫描
 * 4. Compliance Evaluation / 合规评估
 * 5. Diff Generation / Diff 生成
 *
 * @param state Current UI state / 当前 UI 状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
fun ScannerScreen(
    state: OnAlarmState,
    onIntent: (OnAlarmIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // ─────────────────────────────────────────────────────
        // Step Indicator / 步骤指示器
        // ─────────────────────────────────────────────────────
        ScanProgressStepper(
            currentStep = state.scannerCurrentStep,
            isScanning = state.isScanning,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // ─────────────────────────────────────────────────────
        // Scan Progress (if scanning) / 扫描进度（扫描中）
        // ─────────────────────────────────────────────────────
        if (state.isScanning) {
            LinearProgressIndicator(
                progress = { state.scanProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scanning... ${state.scanPercentage}% / 扫描中... ${state.scanPercentage}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ─────────────────────────────────────────────────────
        // Step Content / 步骤内容
        // ─────────────────────────────────────────────────────
        AnimatedContent(
            targetState = state.scannerCurrentStep,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
            },
            label = "StepContent",
            modifier = Modifier.weight(1f)
        ) { step ->
            when (step) {
                ScanStep.PROJECT_SELECTION -> ProjectSelectionStep(
                    state = state,
                    onIntent = onIntent
                )
                ScanStep.WAKELOCK_SCAN -> WakeLockScanStep(
                    state = state,
                    onIntent = onIntent
                )
                ScanStep.ALARM_WORK_SCAN -> AlarmWorkScanStep(
                    state = state,
                    onIntent = onIntent
                )
                ScanStep.COMPLIANCE_EVALUATION -> ComplianceEvaluationStep(
                    state = state,
                    onIntent = onIntent
                )
                ScanStep.DIFF_GENERATION -> DiffGenerationStep(
                    state = state,
                    onIntent = onIntent
                )
            }
        }

        // ─────────────────────────────────────────────────────
        // Navigation Buttons / 导航按钮
        // ─────────────────────────────────────────────────────
        ScanNavigationButtons(
            currentStep = state.scannerCurrentStep,
            isScanning = state.isScanning,
            canGoBack = state.scannerCurrentStep != ScanStep.PROJECT_SELECTION,
            canGoNext = state.scannerCurrentStep != ScanStep.DIFF_GENERATION,
            onBack = { onIntent(OnAlarmIntent.PreviousStep) },
            onNext = { onIntent(OnAlarmIntent.NextStep) },
            onScan = { onIntent(OnAlarmIntent.StartScan) },
            onGenerateDiff = { onIntent(OnAlarmIntent.GenerateDiff) }
        )
    }
}

// =============================================================
// ScanProgressStepper — 扫描步骤指示器
// =============================================================
/**
 * Scan progress stepper / 扫描步骤指示器
 *
 * @param currentStep Current scan step / 当前扫描步骤
 * @param isScanning Whether scan is in progress / 扫描是否进行中
 * @param modifier Modifier / 修饰器
 */
@Composable
private fun ScanProgressStepper(
    currentStep: ScanStep,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScanStep.entries.forEachIndexed { index, step ->
            val isActive = step == currentStep
            val isCompleted = step.stepNumber < currentStep.stepNumber

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${step.stepNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            if (index < ScanStep.entries.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(2.dp)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

// =============================================================
// ProjectSelectionStep — 项目选择步骤
// =============================================================
/**
 * Step 1: Project Selection / 步骤 1：项目选择
 */
@Composable
private fun ProjectSelectionStep(
    state: OnAlarmState,
    onIntent: (OnAlarmIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Project Selection / 项目选择",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select the project or module to scan for WakeLock, AlarmManager, and WorkManager usage. / 选择要扫描的项目或模块，检查 WakeLock、AlarmManager 和 WorkManager 的使用情况。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Demo project path selector / 演示项目路径选择器
        var selectedPath by remember { mutableStateOf("app/src/main/java/com/example") }

        OutlinedTextField(
            value = selectedPath,
            onValueChange = { selectedPath = it },
            label = { Text("Module Path / 模块路径") },
            placeholder = { Text("e.g., app/src/main/java/com/example") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { onIntent(OnAlarmIntent.SelectProject(selectedPath)) }) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "Browse / 浏览")
                }
            }
        )

        Text(
            text = "Note: In production, this would open a directory picker. Demo mode uses simulated scan results. / 注意：生产环境会打开目录选择器。此处使用模拟扫描结果演示。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

// =============================================================
// WakeLockScanStep — WakeLock 扫描步骤
// =============================================================
/**
 * Step 2: WakeLock Scan / 步骤 2：WakeLock 扫描
 */
@Composable
private fun WakeLockScanStep(
    state: OnAlarmState,
    onIntent: (OnAlarmIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "WakeLock Scan Results / WakeLock 扫描结果",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (state.wakeLockResults.isEmpty() && !state.isScanning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No WakeLock found yet / 尚未发现 WakeLock",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Click Next or Start Scan to begin / 点击下一步或开始扫描",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Scan log / 扫描日志
            ScanLogTerminal(
                logs = state.scanLogs.filter { log ->
                    log.message.contains("WakeLock", ignoreCase = true) ||
                    log.message.contains("扫描", ignoreCase = true) ||
                    log.level != LogLevel.INFO
                },
                modifier = Modifier.height(120.dp)
            )

            // WakeLock results / WakeLock 结果
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.wakeLockResults, key = { it.id }) { finding ->
                    WakeLockFindingCard(finding = finding)
                }
            }
        }
    }
}

// =============================================================
// AlarmWorkScanStep — Alarm/WorkManager 扫描步骤
// =============================================================
/**
 * Step 3: Alarm/WorkManager Scan / 步骤 3：Alarm/WorkManager 扫描
 */
@Composable
private fun AlarmWorkScanStep(
    state: OnAlarmState,
    onIntent: (OnAlarmIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Alarm/WorkManager Scan Results / Alarm/WorkManager 扫描结果",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (state.alarmResults.isEmpty() && !state.isScanning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Alarm/WorkManager found yet / 尚未发现 Alarm/WorkManager",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            ScanLogTerminal(
                logs = state.scanLogs.filter { log ->
                    log.message.contains("Alarm", ignoreCase = true) ||
                    log.message.contains("WorkManager", ignoreCase = true) ||
                    log.level != LogLevel.INFO
                },
                modifier = Modifier.height(120.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.alarmResults, key = { it.id }) { finding ->
                    AlarmFindingCard(finding = finding)
                }
            }
        }
    }
}

// =============================================================
// ComplianceEvaluationStep — 合规评估步骤
// =============================================================
/**
 * Step 4: Compliance Evaluation / 步骤 4：合规评估
 */
@Composable
private fun ComplianceEvaluationStep(
    state: OnAlarmState,
    onIntent: (OnAlarmIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Compliance Evaluation / 合规评估",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Score summary card / 评分摘要卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = state.batteryRiskLevel.color.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Compliance Score: ${state.overallScore}/100",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = state.batteryRiskLevel.color
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(state.batteryRiskLevel.label) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = state.batteryRiskLevel.color.copy(alpha = 0.2f),
                            labelColor = state.batteryRiskLevel.color
                        )
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.affectedTasks.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total Tasks / 总任务",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.highImpactCount}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                        Text(
                            text = "High Impact / 高影响",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.wakeLockResults.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "WakeLock / WakeLock",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.alarmResults.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Alarm/WM / Alarm/WM",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Recommendations / 建议
        Text(
            text = "Recommendations / 建议",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.affectedTasks.take(3).forEach { task ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = task.migrationSuggestion,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// =============================================================
// DiffGenerationStep — Diff 生成步骤
// =============================================================
/**
 * Step 5: Diff Generation / 步骤 5：Diff 生成
 */
@Composable
private fun DiffGenerationStep(
    state: OnAlarmState,
    onIntent: (OnAlarmIntent) -> Unit
) {
    // Local scroll state for diff preview / Diff 预览的本地滚动状态
    val diffScrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Migration Diff / 迁移 Diff",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Generate a diff showing WakeLock → OnAlarmListener migration. / 生成展示 WakeLock → OnAlarmListener 迁移的 Diff。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (state.generatedDiff != null) {
            // Diff preview / Diff 预览
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0D1117)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Text(
                        text = state.generatedDiff,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFFE6E1E5),
                        modifier = Modifier.verticalScroll(diffScrollState)
                    )
                }
            }

            // Export buttons / 导出按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportFormat.entries.forEach { format ->
                    OutlinedButton(
                        onClick = { onIntent(OnAlarmIntent.ExportDiff(format)) },
                        enabled = !state.isExportingDiff,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(format.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        } else {
            // Generate diff button / 生成 Diff 按钮
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Ready to generate diff / 准备生成 Diff",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${state.wakeLockResults.size} WakeLock findings ready for migration / ${state.wakeLockResults.size} 个 WakeLock 发现准备迁移",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { onIntent(OnAlarmIntent.GenerateDiff) },
                        enabled = !state.isLoading && state.wakeLockResults.isNotEmpty()
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Diff / 生成 Diff")
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun rememberScrollState() = androidx.compose.foundation.rememberScrollState()

// =============================================================
// ScanLogTerminal — 扫描日志终端
// =============================================================
/**
 * Scan log terminal / 扫描日志终端
 *
 * @param logs List of scan log entries / 扫描日志列表
 * @param modifier Modifier / 修饰器
 */
@Composable
private fun ScanLogTerminal(
    logs: List<ScanLogEntry>,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val listState = rememberLazyListState()
    val scrollState = rememberScrollState()

    // Auto-scroll to bottom / 自动滚动到底部
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D1117)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(logs) { entry ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dateFormat.format(Date(entry.timestamp)),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFF8B949E)
                    )
                    Text(
                        text = entry.level.symbol,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = when (entry.level) {
                            LogLevel.INFO -> Color(0xFF58A6FF)
                            LogLevel.WARN -> Color(0xFFD29922)
                            LogLevel.ERROR -> Color(0xFFF85149)
                            LogLevel.SUCCESS -> Color(0xFF3FB950)
                        }
                    )
                    Text(
                        text = entry.message,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFFE6E1E5)
                    )
                }
            }
        }
    }
}

// =============================================================
// WakeLockFindingCard — WakeLock 发现卡片
// =============================================================
@Composable
private fun WakeLockFindingCard(finding: WakeLockFinding) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${finding.className}.${finding.methodName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "${finding.filePath}:${finding.lineNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = finding.suggestion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// =============================================================
// AlarmFindingCard — Alarm 发现卡片
// =============================================================
@Composable
private fun AlarmFindingCard(finding: AlarmFinding) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${finding.className}.${finding.methodName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                AssistChip(
                    onClick = {},
                    label = { Text(finding.alarmType, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.height(24.dp)
                )
            }
            Text(
                text = "${finding.filePath}:${finding.lineNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = finding.suggestion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// =============================================================
// ScanNavigationButtons — 扫描导航按钮
// =============================================================
/**
 * Scan navigation buttons / 扫描导航按钮
 */
@Composable
private fun ScanNavigationButtons(
    currentStep: ScanStep,
    isScanning: Boolean,
    canGoBack: Boolean,
    canGoNext: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onScan: () -> Unit,
    onGenerateDiff: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (canGoBack) {
            OutlinedButton(
                onClick = onBack,
                enabled = !isScanning,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back / 上一步")
            }
        }

        if (currentStep == ScanStep.PROJECT_SELECTION || currentStep == ScanStep.ALARM_WORK_SCAN) {
            Button(
                onClick = if (currentStep == ScanStep.PROJECT_SELECTION) onNext else onScan,
                enabled = !isScanning,
                modifier = Modifier.weight(1f)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        if (currentStep == ScanStep.PROJECT_SELECTION) Icons.Default.ArrowForward else Icons.Default.Radar,
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when (currentStep) {
                        ScanStep.PROJECT_SELECTION -> "Next / 下一步"
                        ScanStep.ALARM_WORK_SCAN -> "Start Scan / 开始扫描"
                        else -> "Next / 下一步"
                    }
                )
            }
        } else if (currentStep == ScanStep.DIFF_GENERATION) {
            // No next button on last step / 最后一步没有"下一步"按钮
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete / 完成")
            }
        }
    }
}
