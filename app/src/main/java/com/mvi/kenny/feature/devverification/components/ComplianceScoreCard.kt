package com.mvi.kenny.feature.devverification.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import com.mvi.kenny.feature.devverification.ComplianceScore

/**
 * PRD-078 | Android Developer Verification Compliance Toolkit
 * Reusable Components — ComplianceScoreCard, VerificationStatusBadge, etc.
 *
 * Design: Section 5 — Component List
 * Bilingual comments: CN + EN
 */

/* ============ Compliance Score Card ============ */
/* ============ 合规评分卡片 ============ */

@Composable
fun ComplianceScoreCard(
    score: ComplianceScore,
    totalApps: Int,
    verifiedApps: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val scoreColor = when (score) {
        ComplianceScore.PASS -> Color(0xFF2E7D32)
        ComplianceScore.AT_RISK -> Color(0xFFF57F17)
        ComplianceScore.FAILED -> Color(0xFFC62828)
        ComplianceScore.UNKNOWN -> Color(0xFF757575)
    }

    // Animated progress / 动画进度
    val animatedProgress by animateFloatAsState(
        targetValue = when (score) {
            ComplianceScore.PASS -> 1f
            ComplianceScore.AT_RISK -> 0.6f
            ComplianceScore.FAILED -> 0.3f
            ComplianceScore.UNKNOWN -> 0f
        },
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "score_progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circular Score / 环形评分
            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(size)) {
                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.toPx() - strokeWidth) / 2

                    // Background arc / 背景弧
                    drawArc(
                        color = Color(0xFFE0E0E0),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc / 进度弧
                    drawArc(
                        color = scoreColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$verifiedApps/$totalApps",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Score Details / 评分详情
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Compliance Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = scoreColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = score.label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (score) {
                        ComplianceScore.PASS -> "All apps verified"
                        ComplianceScore.AT_RISK -> "Some apps need verification"
                        ComplianceScore.FAILED -> "Action required"
                        ComplianceScore.UNKNOWN -> "Run scan to assess"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* ============ Verification Status Badge ============ */
/* ============ 验证状态徽章 ============ */

@Composable
fun VerificationStatusBadge(
    isVerified: Boolean,
    modifier: Modifier = Modifier
) {
    val (color, label) = if (isVerified) {
        Color(0xFF2E7D32) to "Verified"
    } else {
        Color(0xFFC62828) to "Unverified"
    }

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/* ============ Deadline Countdown Chip ============ */
/* ============ 截止日期倒计时胶囊 ============ */

@Composable
fun DeadlineCountdownChip(
    region: com.mvi.kenny.feature.devverification.Region,
    daysRemaining: Long,
    modifier: Modifier = Modifier
) {
    val chipColor = when {
        daysRemaining <= 30 -> Color(0xFFC62828)
        daysRemaining <= 90 -> Color(0xFFF57F17)
        else -> Color(0xFF2E7D32)
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = chipColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = region.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$daysRemaining",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = chipColor
            )
            Text(
                text = "days left",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ============ App Verification List Item ============ */
/* ============ App 验证列表项 ============ */

@Composable
fun AppVerificationListItem(
    app: com.mvi.kenny.feature.devverification.AppVerificationStatus,
    onSelect: () -> Unit,
    onBatchVerify: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSelect,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(app.appName, fontWeight = FontWeight.Medium)
                Text(
                    app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                app.verificationDate?.let { date ->
                    Text(
                        "Verified: $date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VerificationStatusBadge(isVerified = app.isVerified)
                if (!app.isVerified) {
                    IconButton(onClick = onBatchVerify) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.Check,
                            contentDescription = "Verify",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/* ============ Region Status Row ============ */
/* ============ 地区合规状态行 ============ */

@Composable
fun RegionStatusRow(
    region: com.mvi.kenny.feature.devverification.Region,
    score: ComplianceScore,
    modifier: Modifier = Modifier
) {
    val statusColor = when (score) {
        ComplianceScore.PASS -> Color(0xFF2E7D32)
        ComplianceScore.AT_RISK -> Color(0xFFF57F17)
        ComplianceScore.FAILED -> Color(0xFFC62828)
        ComplianceScore.UNKNOWN -> Color(0xFF757575)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(8.dp),
                color = statusColor,
                shape = MaterialTheme.shapes.extraSmall
            ) {}
            Spacer(Modifier.width(8.dp))
            Text(region.displayName)
        }
        Text(
            score.label,
            style = MaterialTheme.typography.labelMedium,
            color = statusColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ============ Wizard Step Indicator ============ */
/* ============ 向导步骤指示器 ============ */

@Composable
fun WizardStepIndicator(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = index < currentStep
            val isCurrent = index == currentStep

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(
                                androidx.compose.material.icons.Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isCurrent)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    step,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Divider(
                        color = if (isCompleted)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        thickness = 2.dp
                    )
                }
            }
        }
    }
}

/* ============ Loading Pulse Bar ============ */
/* ============ 加载脉冲进度条 ============ */

@Composable
fun LoadingPulseBar(
    message: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(modifier = modifier) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
