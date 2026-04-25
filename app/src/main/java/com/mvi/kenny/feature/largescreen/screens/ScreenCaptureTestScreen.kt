package com.mvi.kenny.feature.largescreen.screens

// ================================================================
// ScreenCaptureTestScreen — 大屏形态 UI 自动化截图测试工具
// ================================================================
// Tool 2: Multi-window UI screenshot testing tool.
//
// PRD-155: Android 17 大屏强制适配
// Captures and compares screenshots across window sizes:
//   sw=360dp (Phone), sw=600dp (Small Tablet), sw=840dp (Large Tablet), sw=960dp (Desktop)
//
// Features:
//   - Window size selector (chips)
//   - Run screenshot test simulation
//   - Grid view of results (PASS/FAIL/CRASH)
//   - Layout regression details
// —————————————————————————————————————————————————————————————

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.largescreen.LargeScreenIntent
import com.mvi.kenny.feature.largescreen.LargeScreenState
import com.mvi.kenny.feature.largescreen.ScreenCaptureResult
import com.mvi.kenny.feature.largescreen.ScreenCaptureStatus
import com.mvi.kenny.feature.largescreen.SummaryCard
import com.mvi.kenny.feature.largescreen.WindowSize
import com.mvi.kenny.feature.largescreen.WindowType

/**
 * ============================================================
 * ScreenCaptureTestScreen — 截图测试主界面
 * ============================================================
 *
 * @param state 当前状态
 * @param onIntent Intent 发送器
 */
@Composable
fun ScreenCaptureTestScreen(
    state: LargeScreenState,
    onIntent: (LargeScreenIntent) -> Unit
) {
    var selectedSizes by remember { mutableStateOf(state.selectedWindowSizes) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ─────────────────────────────────────────────────────
        // Window Size Selector — 窗口尺寸选择器
        // ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "选择测试窗口尺寸",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "选择需要测试的窗口尺寸组合，系统将模拟截图测试",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                val allSizes = listOf(
                    WindowSizeOption("手机", "360×800dp", Icons.Default.PhoneAndroid, WindowType.PHONE),
                    WindowSizeOption("小平板", "600×840dp", Icons.Default.Tablet, WindowType.TABLET),
                    WindowSizeOption("大平板", "840×1200dp", Icons.Default.Tablet, WindowType.TABLET),
                    WindowSizeOption("桌面", "960×1080dp", Icons.Default.DesktopWindows, WindowType.TABLET)
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allSizes.forEach { opt ->
                        val size = WindowSize(opt.widthDp, opt.heightDp, opt.type)
                        val isSelected = selectedSizes.any { it.widthDp == opt.widthDp }
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSizes = if (isSelected) {
                                    selectedSizes.filter { it.widthDp != opt.widthDp }
                                } else {
                                    selectedSizes + size
                                }
                            },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(opt.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    Text(opt.size, fontSize = 10.sp)
                                }
                            },
                            leadingIcon = {
                                Icon(opt.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6750A4),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onIntent(LargeScreenIntent.CaptureScreens(selectedSizes)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedSizes.isNotEmpty() && !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) {
                    Icon(Icons.Default.Screenshot, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("运行截图测试")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────
        // Results Summary — 结果摘要
        // ─────────────────────────────────────────────────────
        if (state.screenCaptures.isNotEmpty()) {
            val passCount = state.screenCaptures.values.count { it.status == ScreenCaptureStatus.PASS }
            val failCount = state.screenCaptures.values.count { it.status == ScreenCaptureStatus.FAIL }
            val crashCount = state.screenCaptures.values.count { it.status == ScreenCaptureStatus.CRASH }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "通过",
                    value = passCount.toString(),
                    subtitle = "布局正常",
                    icon = Icons.Default.CheckCircle,
                    accentColor = Color(0xFF146B3A),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "失败",
                    value = failCount.toString(),
                    subtitle = "布局异常",
                    icon = Icons.Default.Warning,
                    accentColor = Color(0xFFF5A623),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "崩溃",
                    value = crashCount.toString(),
                    subtitle = "运行时错误",
                    icon = Icons.Default.BrokenImage,
                    accentColor = Color(0xFFB3261E),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─────────────────────────────────────────────────────
            // Capture Result Grid — 截图结果网格
            // ─────────────────────────────────────────────────────
            Text(
                text = "截图测试结果",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.screenCaptures.entries.toList()) { (size, result) ->
                    CaptureResultCard(size = size, result = result)
                }
            }
        } else if (!state.isLoading) {
            EmptyCaptureState()
        }
    }
}

/**
 * Window size option data.
 */
private data class WindowSizeOption(
    val label: String,
    val size: String,
    val icon: ImageVector,
    val type: WindowType,
    val widthDp: Int = size.split("×")[0].toInt(),
    val heightDp: Int = size.split("×")[1].replace("dp", "").toInt()
)

/**
 * ============================================================
 * CaptureResultCard — 截图结果卡片
 * ============================================================
 */
@Composable
private fun CaptureResultCard(
    size: WindowSize,
    result: ScreenCaptureResult
) {
    val statusColor = when (result.status) {
        ScreenCaptureStatus.PASS -> Color(0xFF146B3A)
        ScreenCaptureStatus.FAIL -> Color(0xFFF5A623)
        ScreenCaptureStatus.CRASH -> Color(0xFFB3261E)
        ScreenCaptureStatus.PENDING -> Color(0xFF625B71)
    }

    val statusIcon = when (result.status) {
        ScreenCaptureStatus.PASS -> Icons.Default.CheckCircle
        ScreenCaptureStatus.FAIL -> Icons.Default.Warning
        ScreenCaptureStatus.CRASH -> Icons.Default.BrokenImage
        ScreenCaptureStatus.PENDING -> Icons.Default.Android
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (size.type) {
                            WindowType.PHONE -> Icons.Default.PhoneAndroid
                            else -> Icons.Default.Tablet
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = size.breakpointLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${size.widthDp}dp × ${size.heightDp}dp",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (result.status) {
                                ScreenCaptureStatus.PASS -> "通过"
                                ScreenCaptureStatus.FAIL -> "失败"
                                ScreenCaptureStatus.CRASH -> "崩溃"
                                ScreenCaptureStatus.PENDING -> "待测"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Diff regions if any
            if (result.diffRegions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                result.diffRegions.forEach { diff ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFF5A623).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF5A623),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = diff.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF5A623)
                        )
                    }
                }
            }

            // Error message if crash
            result.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB3261E),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * ============================================================
 * EmptyCaptureState — 空状态
 * ============================================================
 */
@Composable
private fun EmptyCaptureState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Screenshot,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "暂无截图测试结果",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "选择窗口尺寸并运行测试",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
