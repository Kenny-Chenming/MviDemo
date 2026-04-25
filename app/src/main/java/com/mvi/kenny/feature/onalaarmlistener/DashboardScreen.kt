package com.mvi.kenny.feature.onalaarmlistener

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================
// DashboardScreen — 合规状态仪表盘
// =============================================================
/**
 * Dashboard Screen / 合规状态仪表盘
 *
 * Shows overall battery compliance status:
 * - Compliance score gauge (0-100) / 合规评分仪表（0-100）
 * - Risk level indicator / 风险等级指示器
 * - Top affected tasks / 受影响任务排行
 * - Quick action buttons / 快捷操作按钮
 *
 * @param state Current UI state / 当前 UI 状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
fun DashboardScreen(
    state: OnAlarmState,
    onIntent: (OnAlarmIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ─────────────────────────────────────────────────────
        // Compliance Score Card / 合规评分卡片
        // ─────────────────────────────────────────────────────
        item {
            ComplianceScoreCard(
                score = state.overallScore,
                riskLevel = state.batteryRiskLevel,
                taskCount = state.affectedTasks.size,
                migratedCount = state.migratedCount
            )
        }

        // ─────────────────────────────────────────────────────
        // Quick Actions / 快捷操作
        // ─────────────────────────────────────────────────────
        item {
            QuickActionsRow(
                onStartScan = { onIntent(OnAlarmIntent.StartQuickScan) },
                onRefresh = { onIntent(OnAlarmIntent.RefreshDashboard) },
                isScanning = state.isScanning
            )
        }

        // ─────────────────────────────────────────────────────
        // Risk Level Banner / 风险等级横幅
        // ─────────────────────────────────────────────────────
        item {
            RiskLevelBanner(riskLevel = state.batteryRiskLevel)
        }

        // ─────────────────────────────────────────────────────
        // Task Count Summary / 任务统计摘要
        // ─────────────────────────────────────────────────────
        item {
            TaskCountSummary(
                taskGroups = state.taskGroups,
                highImpactCount = state.highImpactCount
            )
        }

        // ─────────────────────────────────────────────────────
        // Top Affected Tasks / 受影响任务排行
        // ─────────────────────────────────────────────────────
        if (state.affectedTasks.isNotEmpty()) {
            item {
                Text(
                    text = "Top Affected Tasks / 受影响任务排行",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(
                items = state.affectedTasks.take(5),
                key = { it.id }
            ) { task ->
                AffectedTaskCard(
                    task = task,
                    onClick = { onIntent(OnAlarmIntent.NavigateToTask(task)) }
                )
            }

            if (state.affectedTasks.size > 5) {
                item {
                    TextButton(
                        onClick = { onIntent(OnAlarmIntent.SelectTab(ToolTab.ANALYSIS)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View all ${state.affectedTasks.size} tasks / 查看全部 ${state.affectedTasks.size} 个任务")
                    }
                }
            }
        } else {
            // Empty state / 空状态
            item {
                EmptyDashboardState(
                    onStartScan = { onIntent(OnAlarmIntent.StartQuickScan) }
                )
            }
        }

        // ─────────────────────────────────────────────────────
        // Version Info Banner / 版本信息横幅
        // ─────────────────────────────────────────────────────
        item {
            VersionInfoBanner()
        }
    }
}

// =============================================================
// ComplianceScoreCard — 合规评分环形仪表
// =============================================================
/**
 * Compliance score gauge card / 合规评分环形仪表卡片
 *
 * @param score Compliance score 0-100 / 合规评分 0-100
 * @param riskLevel Battery risk level / 电池风险等级
 * @param taskCount Total affected task count / 受影响任务总数
 * @param migratedCount Migrated task count / 已迁移任务数
 */
@Composable
private fun ComplianceScoreCard(
    score: Int,
    riskLevel: RiskLevel,
    taskCount: Int,
    migratedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score gauge / 评分仪表
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    ComplianceScoreGauge(
                        score = score,
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = riskLevel.color
                        )
                        Text(
                            text = "Score / 评分",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = riskLevel.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = riskLevel.color
                )
            }

            // Stats / 统计
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    icon = Icons.Default.List,
                    label = "Total Tasks / 总任务",
                    value = "$taskCount"
                )
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Migrated / 已迁移",
                    value = "$migratedCount"
                )
                StatItem(
                    icon = Icons.Default.Warning,
                    label = "Remaining / 剩余",
                    value = "${taskCount - migratedCount}"
                )
            }
        }
    }
}

/**
 * Animated compliance score gauge / 动画合规评分仪表
 *
 * @param score Score 0-100 / 评分 0-100
 * @param modifier Modifier / 修饰器
 */
@Composable
private fun ComplianceScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ScoreAnimation"
    )

    val sweepAngle = (animatedScore / 100f) * 270f
    val gaugeColor = when {
        score >= 80 -> Color(0xFF4CAF50)
        score >= 50 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Canvas(modifier = modifier) {
        val strokeWidth = 12.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

        // Background arc / 背景弧
        drawArc(
            color = Color.Gray.copy(alpha = 0.3f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Score arc / 评分弧
        drawArc(
            color = gaugeColor,
            startAngle = 135f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * Stat item row / 统计项行
 */
@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================
// QuickActionsRow — 快捷操作按钮组
// =============================================================
/**
 * Quick action buttons row / 快捷操作按钮组
 */
@Composable
private fun QuickActionsRow(
    onStartScan: () -> Unit,
    onRefresh: () -> Unit,
    isScanning: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onStartScan,
            enabled = !isScanning,
            modifier = Modifier.weight(1f)
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Radar, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan / 扫描")
        }

        OutlinedButton(
            onClick = onRefresh,
            enabled = !isScanning
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh / 刷新")
        }
    }
}

// =============================================================
// RiskLevelBanner — 风险等级横幅
// =============================================================
/**
 * Risk level banner / 风险等级横幅
 */
@Composable
private fun RiskLevelBanner(riskLevel: RiskLevel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = riskLevel.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (riskLevel) {
                    RiskLevel.GREEN -> Icons.Default.CheckCircle
                    RiskLevel.YELLOW -> Icons.Default.Warning
                    RiskLevel.RED -> Icons.Default.Error
                    RiskLevel.UNKNOWN -> Icons.Default.Help
                },
                contentDescription = null,
                tint = riskLevel.color,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = when (riskLevel) {
                        RiskLevel.GREEN -> "Compliant / 合规"
                        RiskLevel.YELLOW -> "Warning / 警告"
                        RiskLevel.RED -> "High Risk / 高风险"
                        RiskLevel.UNKNOWN -> "Unknown / 未知"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = riskLevel.color
                )
                Text(
                    text = when (riskLevel) {
                        RiskLevel.GREEN -> "Your app meets Battery Technical Quality standards. / 您的应用符合电池技术质量标准。"
                        RiskLevel.YELLOW -> "Some battery issues detected. Review recommended. / 检测到一些电池问题，建议审查。"
                        RiskLevel.RED -> "Critical battery issues found. Immediate action required. / 发现严重电池问题，需要立即处理。"
                        RiskLevel.UNKNOWN -> "Run a scan to assess battery compliance. / 运行扫描以评估电池合规性。"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =============================================================
// TaskCountSummary — 任务统计摘要
// =============================================================
/**
 * Task count summary by type / 按类型分类的任务统计
 */
@Composable
private fun TaskCountSummary(
    taskGroups: Map<TaskType, List<AffectedTask>>,
    highImpactCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Task Summary / 任务摘要",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TaskType.entries.take(4).forEach { type ->
                    val count = taskGroups[type]?.size ?: 0
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = type.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (highImpactCount > 0) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$highImpactCount high impact tasks need attention / $highImpactCount 个高影响任务需要关注",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

// =============================================================
// AffectedTaskCard — 受影响任务卡片
// =============================================================
/**
 * Affected task card / 受影响任务卡片
 */
@Composable
private fun AffectedTaskCard(
    task: AffectedTask,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type icon / 类型图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(task.taskType.let { type ->
                        when (type) {
                            TaskType.WAKELOCK -> Color(0xFFFFF3E0)
                            TaskType.ALARM -> Color(0xFFE3F2FD)
                            TaskType.WORK_MANAGER -> Color(0xFFF3E5F5)
                            TaskType.EXPEDITED, TaskType.USER_INITIATED -> Color(0xFFE8F5E9)
                        }
                    }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (task.taskType) {
                        TaskType.WAKELOCK -> Icons.Default.BatteryChargingFull
                        TaskType.ALARM -> Icons.Default.Alarm
                        TaskType.WORK_MANAGER -> Icons.Default.WorkHistory
                        TaskType.EXPEDITED, TaskType.USER_INITIATED -> Icons.Default.FlashOn
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = when (task.taskType) {
                        TaskType.WAKELOCK -> Color(0xFFFF9800)
                        TaskType.ALARM -> Color(0xFF2196F3)
                        TaskType.WORK_MANAGER -> Color(0xFF9C27B0)
                        TaskType.EXPEDITED, TaskType.USER_INITIATED -> Color(0xFF4CAF50)
                    }
                )
            }

            // Task info / 任务信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.taskName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${task.className} / L${task.lineNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Impact badge / 影响等级徽章
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = task.impactLevel.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = task.impactLevel.color.copy(alpha = 0.1f),
                    labelColor = task.impactLevel.color
                ),
                modifier = Modifier.height(24.dp)
            )

            // Migrated indicator / 已迁移指示器
            if (task.isMigrated) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Migrated / 已迁移",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// =============================================================
// EmptyDashboardState — 空状态
// =============================================================
/**
 * Empty dashboard state / 空状态
 */
@Composable
private fun EmptyDashboardState(
    onStartScan: () -> Unit
) {
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
                imageVector = Icons.Default.Radar,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Text(
                text = "No scan data / 暂无扫描数据",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Run a scan to check your app's battery compliance with Android 17 OnAlarmListener requirements. / 运行扫描以检查您的应用对 Android 17 OnAlarmListener 要求的电池合规性。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onStartScan) {
                Icon(Icons.Default.Radar, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Scan / 开始扫描")
            }
        }
    }
}

// =============================================================
// VersionInfoBanner — 版本信息横幅
// =============================================================
/**
 * Android 17 version info banner / Android 17 版本信息横幅
 */
@Composable
private fun VersionInfoBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Android 17 OnAlarmListener API requires targetSdk 35+. Battery Technical Quality Enforcement is active. / Android 17 OnAlarmListener API 要求 targetSdk 35+，电池技术质量强制执行已生效。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
