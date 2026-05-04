package com.mvi.kenny.feature.room3importmigration.components

// ================================================================
// DiffCard — Before/After 代码对比卡片组件
// ================================================================
// Displays a code migration diff with before/after code blocks.
//
// Usage:
//   DiffCard(
//     title = "SupportSQLiteDatabase → SQLiteDriver",
//     filePath = "app/src/main/.../Database.kt",
//     beforeCode = "val db: SupportSQLiteDatabase = ...",
//     afterCode = "val driver = AndroidDriver(...)",
//     riskLevel = RiskLevel.P0_CRITICAL,
//     onCopy = { ... }
//   )
// ================================================================

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
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
import com.mvi.kenny.feature.room3importmigration.RiskLevel
import androidx.compose.foundation.text.selection.SelectionContainer

// Color definitions / 颜色定义
private object DiffCardColors {
    val Surface = Color(0xFF161B22)
    val SurfaceVariant = Color(0xFF21262D)
    val CardBorder = Color(0xFF30363D)
    val TerminalGreen = Color(0xFF39D353)
    val TerminalBlue = Color(0xFF58A6FF)
    val TerminalYellow = Color(0xFFD29922)
    val DiffRed = Color(0xFFF85149)
    val DiffRedBg = Color(0x1AF85149)
    val DiffGreen = Color(0xFF3FB950)
    val DiffGreenBg = Color(0x1A3FB950)
    val OnSurface = Color(0xFFE6EDF3)
    val OnSurfaceVariant = Color(0xFF8B949E)
    val P0Color = Color(0xFFF85149)
    val P1Color = Color(0xFFD29922)
    val P2Color = Color(0xFF3FB950)
}

/**
 * DiffCard — 代码迁移前后对比卡片
 *
 * @param title 卡片标题
 * @param filePath 文件路径
 * @param beforeCode 迁移前代码
 * @param afterCode 迁移后代码
 * @param riskLevel 风险等级
 * @param onCopy 复制修复后代码回调
 */
@Composable
fun DiffCard(
    title: String,
    filePath: String,
    beforeCode: String,
    afterCode: String,
    riskLevel: RiskLevel,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DiffCardColors.Surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                DiffCardColors.CardBorder.copy(alpha = 0.7f)
            )
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Risk indicator / 风险指示
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(riskLevel.color)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    color = DiffCardColors.OnSurface,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                RiskLevelBadge(riskLevel)
            }

            // File path / 文件路径
            if (filePath.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "📄 $filePath",
                    color = DiffCardColors.TerminalBlue,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))

            // Before Code / 迁移前代码
            Text(
                "❌ Before",
                color = DiffCardColors.DiffRed,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            SelectionContainer {
                Text(
                    beforeCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DiffCardColors.DiffRed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DiffCardColors.DiffRedBg)
                        .padding(8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Arrow / 箭头
            Text(
                "⬇️",
                color = DiffCardColors.OnSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(12.dp))

            // After Code / 迁移后代码
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "✅ After",
                    color = DiffCardColors.DiffGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onCopy,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = DiffCardColors.DiffGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Copy",
                        color = DiffCardColors.DiffGreen,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            SelectionContainer {
                Text(
                    afterCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DiffCardColors.DiffGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DiffCardColors.DiffGreenBg)
                        .padding(8.dp)
                )
            }
        }
    }
}

/**
 * Risk Level Badge — 风险等级标签
 */
@Composable
private fun RiskLevelBadge(riskLevel: RiskLevel) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = riskLevel.color.copy(alpha = 0.2f)
    ) {
        Text(
            text = "${riskLevel.emoji} ${riskLevel.displayName}",
            color = riskLevel.color,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
