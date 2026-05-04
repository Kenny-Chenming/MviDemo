package com.mvi.kenny.feature.devverification.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.devverification.ComplianceLevel
import com.mvi.kenny.feature.devverification.VerificationStatus

/**
 * PRD-224 | Android 开发者身份验证合规工具包
 * Reusable Components — Adapted for PRD-224 types
 *
 * Note: PRD-224 uses VerificationStatus and ComplianceLevel from the new contract.
 * This file provides compatibility components.
 * Bilingual comments: CN + EN
 */

// ── VerificationStatus Badge (based on old VerificationStatusBadge pattern) ──
// VerificationStatus 徽章（基于旧有样式，用于新 VerificationStatus 类型）

@Composable
fun VerificationStatusBadge(
    isVerified: Boolean,
    modifier: Modifier = Modifier
) {
    val (color, label) = if (isVerified) {
        Color(0xFF66BB6A) to "已验证"
    } else {
        Color(0xFFEF5350) to "未验证"
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isVerified) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
            } else {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Compliance Level Card (adapted from old ComplianceScoreCard) ──
// 合规等级卡片（适配新的 ComplianceLevel 类型）

@Composable
fun ComplianceLevelCard(
    level: ComplianceLevel,
    totalChecks: Int = 4,
    passedChecks: Int = 0,
    modifier: Modifier = Modifier
) {
    val color = when (level) {
        ComplianceLevel.COMPLIANT -> Color(0xFF66BB6A)
        ComplianceLevel.WARNING -> Color(0xFFFFB74D)
        ComplianceLevel.NON_COMPLIANT -> Color(0xFFEF5350)
        ComplianceLevel.UNKNOWN -> Color(0xFF9E9E9E)
    }

    val label = when (level) {
        ComplianceLevel.COMPLIANT -> "All checks passed"
        ComplianceLevel.WARNING -> "Some issues found"
        ComplianceLevel.NON_COMPLIANT -> "Action required"
        ComplianceLevel.UNKNOWN -> "Run scan to assess"
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ComplianceProgressRing(
                    progress = when (level) {
                        ComplianceLevel.COMPLIANT -> 1f
                        ComplianceLevel.WARNING -> 0.6f
                        ComplianceLevel.NON_COMPLIANT -> 0.2f
                        ComplianceLevel.UNKNOWN -> 0f
                    },
                    color = color,
                    size = 48.dp
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = level.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$passedChecks/$totalChecks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "checks",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

// ── Circular Progress Ring ──
// 环形进度条
@Composable
fun ComplianceProgressRing(
    progress: Float,
    color: Color,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp
) {
    Canvas(modifier = Modifier.size(size)) {
        val sweepAngle = progress * 360f
        drawArc(
            color = Color(0xFF424242),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            size = Size(size.toPx(), size.toPx())
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            size = Size(size.toPx(), size.toPx())
        )
    }
}

// ── Simple Deadline Countdown Chip (adapted) ──
// 简单截止日期倒计时标签
@Composable
fun DeadlineCountdownChip(
    regionName: String,
    daysRemaining: Long,
    modifier: Modifier = Modifier
) {
    val color = when {
        daysRemaining <= 30 -> Color(0xFFEF5350)
        daysRemaining <= 90 -> Color(0xFFFFB74D)
        else -> Color(0xFF66BB6A)
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = regionName,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = "${daysRemaining}d",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ── Verification Status Indicator Dot ──
// 验证状态指示点
@Composable
fun VerificationStatusDot(
    status: VerificationStatus,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        VerificationStatus.REGISTERED -> Color(0xFF66BB6A)
        VerificationStatus.UNREGISTERED -> Color(0xFFEF5350)
        VerificationStatus.UNKNOWN -> Color(0xFFFFB74D)
    }
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}
