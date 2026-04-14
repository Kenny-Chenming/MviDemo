package com.mvi.kenny.feature.journeys

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================
// JourneysResultScreen — 执行结果屏幕
// PRD-099 | Journeys for Android Studio 自动化 E2E 测试工具包
// =============================================================
/**
 * Journey Execution Result Screen / Journey 执行结果屏幕
 *
 * Displays step-by-step execution results with timeline view.
 *
 * @param viewModel JourneysViewModel instance / JourneysViewModel 实例
 * @param onNavigateBack Callback to navigate back / 返回回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneysResultScreen(
    viewModel: JourneysViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val result = state.executionResult

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            result?.journeyName ?: "Execution Result / 执行结果",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        result?.let {
                            Text(
                                text = formatDuration(it.durationMs),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back / 返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        result?.journeyId?.let {
                            viewModel.processIntent(JourneysIntent.RunJourney(it))
                        }
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Re-run / 重新运行",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { /* Share */ }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share / 分享",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            if (state.isRunning) {
                // Running indicator / 运行中指示器
                RunningIndicator()
            }

            result?.let { executionResult ->
                // Overall status banner / 整体状态横幅
                OverallStatusBanner(result = executionResult)

                // Step timeline / 步骤时间线
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        executionResult.steps,
                        key = { _, stepResult -> stepResult.step.id }
                    ) { index, stepResult ->
                        StepTimelineItem(
                            index = index + 1,
                            stepResult = stepResult,
                            onRerun = {
                                result.journeyId.let { journeyId ->
                                    viewModel.processIntent(
                                        JourneysIntent.RunSingleStep(journeyId, stepResult.step.id)
                                    )
                                }
                            }
                        )
                    }

                    // Summary / 汇总
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        ExecutionSummary(result = executionResult)
                    }
                }
            } ?: run {
                // No result / 无结果
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "⏳", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No execution result yet / 暂无执行结果",
                            fontSize = 16.sp,
                            color = Color(0xFF5F5F5F)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { state.activeJourneyId?.let {
                                viewModel.processIntent(JourneysIntent.RunJourney(it))
                            } },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Journey / 运行 Journey")
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// RunningIndicator — 运行中指示器
// ================================================================
/**
 * Running indicator / 运行中指示器
 */
@Composable
private fun RunningIndicator() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(0xFF2196F3),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Running... / 运行中...",
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2196F3)
            )
        }
    }
}

// ================================================================
// OverallStatusBanner — 整体状态横幅
// ================================================================
/**
 * Overall execution status banner / 整体执行状态横幅
 */
@Composable
private fun OverallStatusBanner(result: ExecutionResult) {
    val bgColor = when (result.overallStatus) {
        StepStatus.PASSED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        StepStatus.FAILED -> Color(0xFFF44336).copy(alpha = 0.1f)
        else -> Color(0xFF9E9E9E).copy(alpha = 0.1f)
    }
    val textColor = when (result.overallStatus) {
        StepStatus.PASSED -> Color(0xFF4CAF50)
        StepStatus.FAILED -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = result.overallStatus.emoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${result.overallStatus.emoji} ${result.overallStatus.label}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor
                    )
                    if (result.overallStatus == StepStatus.FAILED && result.failedStepIndex >= 0) {
                        Text(
                            text = "Failed at Step ${result.failedStepIndex + 1} / 失败于步骤 ${result.failedStepIndex + 1}",
                            fontSize = 12.sp,
                            color = textColor
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDuration(result.durationMs),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                )
                Text(
                    text = "${result.steps.size} steps / 步骤",
                    fontSize = 12.sp,
                    color = Color(0xFF5F5F5F)
                )
            }
        }
    }
}

// ================================================================
// StepTimelineItem — 步骤时间线项
// ================================================================
/**
 * Step timeline item / 步骤时间线项
 */
@Composable
private fun StepTimelineItem(
    index: Int,
    stepResult: StepResult,
    onRerun: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (stepResult.status) {
        StepStatus.PASSED -> Color(0xFF4CAF50)
        StepStatus.FAILED -> Color(0xFFF44336)
        StepStatus.RUNNING -> Color(0xFF2196F3)
        StepStatus.PENDING -> Color(0xFF9E9E9E)
        StepStatus.SKIPPED -> Color(0xFFFF9800)
    }

    val connectorColor = when {
        stepResult.status == StepStatus.PASSED -> Color(0xFF4CAF50)
        stepResult.status == StepStatus.FAILED -> Color(0xFFF44336)
        else -> Color(0xFFE0E0E0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main row / 主行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator / 状态指示器
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stepResult.status.emoji, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Step info / 步骤信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Step $index: ${stepResult.step.actionType}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF1F1F1F)
                    )
                    Text(
                        text = stepResult.step.description,
                        fontSize = 12.sp,
                        color = Color(0xFF5F5F5F)
                    )
                }

                // Duration / 时长
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatDuration(stepResult.durationMs),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = statusColor
                    )
                    Text(
                        text = if (stepResult.status == StepStatus.SKIPPED)
                            "Blocked / 阻塞" else "Duration / 时长",
                        fontSize = 10.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            // Expanded details / 展开详情
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(14.dp)
                ) {
                    // Target info / 目标信息
                    DetailRow(label = "Target / 目标", value = stepResult.step.target)

                    // Error details if failed / 错误详情（如失败）
                    if (stepResult.status == StepStatus.FAILED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF44336).copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "❌ Error / 错误",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color(0xFFF44336)
                                )
                                stepResult.errorMessage?.let {
                                    Text(
                                        text = "Message: $it",
                                        fontSize = 12.sp,
                                        color = Color(0xFF1F1F1F)
                                    )
                                }
                                stepResult.expectedResult?.let {
                                    Text(
                                        text = "Expected / 预期: $it",
                                        fontSize = 12.sp,
                                        color = Color(0xFF1F1F1F)
                                    )
                                }
                                stepResult.actualResult?.let {
                                    Text(
                                        text = "Actual / 实际: $it",
                                        fontSize = 12.sp,
                                        color = Color(0xFFF44336)
                                    )
                                }
                            }
                        }
                    }

                    // Screenshot placeholder / 截图占位
                    stepResult.screenshotPath?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "📸 Screenshot / 截图",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1F1F1F)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE0E0E0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Screenshot: $it",
                                        fontSize = 11.sp,
                                        color = Color(0xFF5F5F5F)
                                    )
                                }
                            }
                        }
                    }

                    // Re-run button / 重新运行按钮
                    if (stepResult.status == StepStatus.FAILED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRerun,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("▶ Re-run this step / 重新运行此步骤")
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// ExecutionSummary — 执行汇总
// ================================================================
/**
 * Execution summary card / 执行汇总卡片
 */
@Composable
private fun ExecutionSummary(result: ExecutionResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Execution Summary / 执行汇总",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF1F1F1F)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val passedCount = result.steps.count { it.status == StepStatus.PASSED }
            val failedCount = result.steps.count { it.status == StepStatus.FAILED }
            val skippedCount = result.steps.count { it.status == StepStatus.SKIPPED }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(emoji = "✅", label = "Passed / 通过", value = passedCount.toString())
                SummaryItem(emoji = "❌", label = "Failed / 失败", value = failedCount.toString())
                SummaryItem(emoji = "⏭️", label = "Skipped / 跳过", value = skippedCount.toString())
            }
        }
    }
}

/**
 * Summary item / 汇总项
 */
@Composable
private fun SummaryItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 24.sp)
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFF1F1F1F)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF5F5F5F)
        )
    }
}

// ================================================================
// DetailRow — 详情行
// ================================================================
/**
 * Detail row / 详情行
 */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF5F5F5F)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color(0xFF1F1F1F)
        )
    }
}

// ================================================================
// Utility Functions — 工具函数
// ================================================================
/**
 * Format duration in milliseconds to human-readable string
 * / 将毫秒时长格式化为人类可读字符串
 */
private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "—"
    val seconds = durationMs / 1000
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}
