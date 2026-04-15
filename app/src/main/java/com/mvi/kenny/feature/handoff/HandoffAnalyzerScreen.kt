package com.mvi.kenny.feature.handoff

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * ============================================================
 * HandoffAnalyzerScreen — 适用性分析器
 * ============================================================
 * 分析 APK/Activity 的 Handoff 适配评分与优先级
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoffAnalyzerScreen(
    onNavigateBack: () -> Unit,
    viewModel: HandoffViewModel
) {
    val state by viewModel.analyzerState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("适用性分析器") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scan Input Section / 扫描输入区
            ScanInputSection(
                apkPath = state.apkPath,
                scanStatus = state.scanStatus,
                scanProgress = state.scanProgress,
                onStartScan = { viewModel.sendIntent(HandoffIntent.StartScan(state.apkPath)) }
            )

            // Results / 结果列表
            if (state.analyzedActivities.isNotEmpty()) {
                Text(
                    text = "分析结果 (${state.analyzedActivities.size} 个 Activity)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.analyzedActivities) { activity ->
                        ActivityScoreCard(
                            activity = activity,
                            isSelected = state.selectedActivity == activity,
                            onClick = { viewModel.sendIntent(HandoffIntent.SelectActivityDetail(activity)) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            } else if (state.scanStatus == ScanStatus.IDLE) {
                // Empty state / 空状态
                EmptyAnalyzerState()
            }
        }
    }
}

/**
 * Scan Input Section / 扫描输入区
 */
@Composable
private fun ScanInputSection(
    apkPath: String,
    scanStatus: ScanStatus,
    scanProgress: Int,
    onStartScan: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = HandoffColors.Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("APK / 源码路径", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Path display / 路径显示
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Text(
                    text = apkPath.ifEmpty { "demo.apk (模拟路径)" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (apkPath.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                           else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scan button / 扫描按钮
            if (scanStatus == ScanStatus.SCANNING) {
                Column {
                    LinearProgressIndicator(
                        progress = { scanProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = HandoffColors.Primary
                    )
                    Text(
                        text = "扫描中... $scanProgress%",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                androidx.compose.material3.Button(
                    onClick = onStartScan,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (scanStatus == ScanStatus.DONE) "重新扫描" else "开始扫描")
                }
            }
        }
    }
}

/**
 * Activity Score Card / Activity 评分卡片
 */
@Composable
private fun ActivityScoreCard(
    activity: ActivityHandoffScore,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val priorityColor = when (activity.priority) {
        HandoffPriority.HIGH -> HandoffColors.HandoffActive
        HandoffPriority.MEDIUM -> HandoffColors.Primary
        HandoffPriority.LOW -> Color(0xFFFF9800)
        HandoffPriority.NOT_SUITABLE -> HandoffColors.Error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HandoffColors.Primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score circle / 评分圆圈
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(priorityColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${activity.score}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = priorityColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activity.activityName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Priority badge / 优先级标签
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priorityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activity.priority.labelZh,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor
                        )
                    }
                }

                // Reasons / 原因
                if (activity.reasons.isNotEmpty()) {
                    Text(
                        text = activity.reasons.take(2).joinToString("、"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Warnings / 警告
                if (activity.warnings.isNotEmpty()) {
                    Text(
                        text = "⚠ ${activity.warnings.first()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = HandoffColors.Error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * Empty Analyzer State / 空状态
 */
@Composable
private fun EmptyAnalyzerState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Analytics,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无分析结果",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = "输入 APK 路径开始分析",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
