package com.mvi.kenny.feature.room3importmigration.components

// ================================================================
// ChecklistItemCard — CI 检查清单条目组件
// ================================================================
// Interactive checklist item with risk level badge.
//
// Usage:
//   ChecklistItemCard(
//     item = CIChecklistItem(...),
//     onToggle = { checked -> ... }
//   )
// ================================================================

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.room3importmigration.CIChecklistItem
import com.mvi.kenny.feature.room3importmigration.RiskLevel

// Color definitions / 颜色定义
private object ChecklistColors {
    val Surface = Color(0xFF161B22)
    val SurfaceVariant = Color(0xFF21262D)
    val CardBorder = Color(0xFF30363D)
    val TerminalGreen = Color(0xFF39D353)
    val TerminalBlue = Color(0xFF58A6FF)
    val DiffGreen = Color(0xFF3FB950)
    val DiffGreenBg = Color(0x1A3FB950)
    val OnSurface = Color(0xFFE6EDF3)
    val OnSurfaceVariant = Color(0xFF8B949E)
    val CheckedGreen = Color(0xFF238636)
    val CheckedGreenBg = Color(0x1A238636)
    val P0Color = Color(0xFFF85149)
    val P1Color = Color(0xFFD29922)
    val P2Color = Color(0xFF3FB950)
    val P3Color = Color(0xFF6BCF7F)
}

/**
 * ChecklistItemCard — CI 检查清单条目卡片
 *
 * @param item 检查清单条目数据
 * @param onToggle 勾选状态变更回调
 */
@Composable
fun ChecklistItemCard(
    item: CIChecklistItem,
    onToggle: (Boolean) -> Unit
) {
    val borderColor = if (item.isChecked) {
        ChecklistColors.CheckedGreen.copy(alpha = 0.5f)
    } else {
        item.riskLevel.color.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked)
                ChecklistColors.CheckedGreenBg.copy(alpha = 0.1f)
            else
                ChecklistColors.Surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!item.isChecked) }
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox / 复选框
            Icon(
                imageVector = if (item.isChecked)
                    Icons.Default.CheckCircle
                else
                    Icons.Default.RadioButtonUnchecked,
                contentDescription = if (item.isChecked) "Checked" else "Unchecked",
                tint = if (item.isChecked)
                    ChecklistColors.CheckedGreen
                else
                    ChecklistColors.OnSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )

            Spacer(Modifier.width(12.dp))

            // Content / 内容
            Column(modifier = Modifier.weight(1f)) {
                // Title + Category / 标题 + 类别
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = if (item.isChecked)
                            ChecklistColors.DiffGreen
                        else
                            ChecklistColors.OnSurface,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    CategoryBadge(category = item.category)
                }

                Spacer(Modifier.height(4.dp))

                // Description / 描述
                Text(
                    text = item.description,
                    color = ChecklistColors.OnSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Spacer(Modifier.height(8.dp))

                // Risk Badge / 风险标签
                RiskBadgeItem(riskLevel = item.riskLevel, isChecked = item.isChecked)
            }
        }
    }
}

/**
 * Category Badge — 类别标签
 */
@Composable
private fun CategoryBadge(category: String) {
    val color = when (category) {
        "Import" -> Color(0xFF58A6FF)
        "SQLiteDriver" -> Color(0xFFFFA657)
        "Suspend" -> Color(0xFFBC8CFF)
        "KSP" -> Color(0xFF39D353)
        "Driver" -> Color(0xFFFF7B72)
        "KMP" -> Color(0xFF79C0FF)
        "CI" -> Color(0xFFD29922)
        else -> Color(0xFF8B949E)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = category,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Risk Badge (for checklist item) — 风险标签
 */
@Composable
private fun RiskBadgeItem(riskLevel: RiskLevel, isChecked: Boolean) {
    val color = if (isChecked) ChecklistColors.DiffGreen else riskLevel.color

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "${riskLevel.emoji} ${riskLevel.displayName}",
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
