package com.mvi.kenny.feature.largescreen.screens

// ================================================================
// FoldableGuideScreen — 折叠屏/自由窗口适配检查清单
// ================================================================
// Tool 5: Foldable / Freeform adaptation checklist.
//
// PRD-155: Android 17 大屏强制适配
// Step-by-step checklist for foldable and freeform window adaptation.
//
// Features:
//   - Categorized checklist items (WindowManager / SplitScreen / Freeform)
//   - Per-item status toggle (Unchecked / Pass / Fail / N/A)
//   - Reference documentation links
//   - Progress tracking
// —————————————————————————————————————————————————————————————

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.largescreen.CheckStatus
import com.mvi.kenny.feature.largescreen.ChecklistItem
import com.mvi.kenny.feature.largescreen.LargeScreenIntent
import com.mvi.kenny.feature.largescreen.LargeScreenState

/**
 * ============================================================
 * FoldableGuideScreen — 折叠屏适配检查清单主界面
 * ============================================================
 */
@Composable
fun FoldableGuideScreen(
    state: LargeScreenState,
    onIntent: (LargeScreenIntent) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("全部") }
    val categories = listOf("全部") + state.checklistItems.map { it.category }.distinct()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ─────────────────────────────────────────────────────
        // Progress Overview — 进度概览
        // ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF6750A4).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "适配进度",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${checklistProgress(state.checklistItems).first}/${state.checklistItems.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val progress = checklistProgress(state.checklistItems).second
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF6750A4),
                    trackColor = Color(0xFF6750A4).copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProgressStat(
                        label = "通过",
                        value = state.checklistItems.count { it.status == CheckStatus.PASSED }.toString(),
                        color = Color(0xFF146B3A)
                    )
                    ProgressStat(
                        label = "失败",
                        value = state.checklistItems.count { it.status == CheckStatus.FAILED }.toString(),
                        color = Color(0xFFB3261E)
                    )
                    ProgressStat(
                        label = "未检查",
                        value = state.checklistItems.count { it.status == CheckStatus.UNCHECKED }.toString(),
                        color = Color(0xFF625B71)
                    )
                    ProgressStat(
                        label = "不适用",
                        value = state.checklistItems.count { it.status == CheckStatus.NOT_APPLICABLE }.toString(),
                        color = Color(0xFF625B71)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────
        // Category Filter — 分类筛选
        // ─────────────────────────────────────────────────────
        Text(
            text = "检查分类",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val count = if (category == "全部") {
                    state.checklistItems.size
                } else {
                    state.checklistItems.count { it.category == category }
                }
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = if (category == "全部") "全部 ($count)" else "$category ($count)",
                            fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6750A4),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────
        // Checklist Items — 检查项列表
        // ─────────────────────────────────────────────────────
        val filteredItems = if (selectedCategory == "全部") {
            state.checklistItems
        } else {
            state.checklistItems.filter { it.category == selectedCategory }
        }.sortedBy { it.order }

        // Group by category
        val groupedItems = filteredItems.groupBy { it.category }

        groupedItems.forEach { (category, items) ->
            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            items.forEach { item ->
                ChecklistItemCard(
                    item = item,
                    onStatusChange = { status ->
                        onIntent(LargeScreenIntent.UpdateChecklistItem(item, status))
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * ============================================================
 * ChecklistItemCard — 检查项卡片
 * ============================================================
 */
@Composable
private fun ChecklistItemCard(
    item: ChecklistItem,
    onStatusChange: (CheckStatus) -> Unit
) {
    val statusColor = when (item.status) {
        CheckStatus.PASSED -> Color(0xFF146B3A)
        CheckStatus.FAILED -> Color(0xFFB3261E)
        CheckStatus.UNCHECKED -> Color(0xFF625B71)
        CheckStatus.NOT_APPLICABLE -> Color(0xFF625B71).copy(alpha = 0.5f)
    }

    val statusIcon: ImageVector = when (item.status) {
        CheckStatus.PASSED -> Icons.Default.CheckCircle
        CheckStatus.FAILED -> Icons.Default.Error
        CheckStatus.UNCHECKED -> Icons.Default.Pending
        CheckStatus.NOT_APPLICABLE -> Icons.Default.HelpOutline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (item.status) {
                CheckStatus.PASSED -> Color(0xFF146B3A).copy(alpha = 0.08f)
                CheckStatus.FAILED -> Color(0xFFB3261E).copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status toggle buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        StatusToggleButton(
                            isSelected = item.status == CheckStatus.PASSED,
                            icon = Icons.Default.Check,
                            color = Color(0xFF146B3A),
                            label = "通过",
                            onClick = { onStatusChange(CheckStatus.PASSED) }
                        )
                        StatusToggleButton(
                            isSelected = item.status == CheckStatus.FAILED,
                            icon = Icons.Default.Error,
                            color = Color(0xFFB3261E),
                            label = "失败",
                            onClick = { onStatusChange(CheckStatus.FAILED) }
                        )
                        StatusToggleButton(
                            isSelected = item.status == CheckStatus.NOT_APPLICABLE,
                            icon = Icons.Default.HelpOutline,
                            color = Color(0xFF625B71),
                            label = "N/A",
                            onClick = { onStatusChange(CheckStatus.NOT_APPLICABLE) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (item.referenceDoc.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Open reference doc */ },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.referenceDoc,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6750A4),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * ============================================================
 * StatusToggleButton — 状态切换按钮
 * ============================================================
 */
@Composable
private fun StatusToggleButton(
    isSelected: Boolean,
    icon: ImageVector,
    color: Color,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                color = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) color else color.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * ============================================================
 * ProgressStat — 进度统计
 * ============================================================
 */
@Composable
private fun ProgressStat(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Calculate checklist progress.
 * Returns Pair(count of passed, progress ratio 0.0~1.0)
 */
private fun checklistProgress(items: List<ChecklistItem>): Pair<Int, Float> {
    val passed = items.count { it.status == CheckStatus.PASSED }
    val total = items.filter { it.status != CheckStatus.NOT_APPLICABLE }.size
    val progress = if (total > 0) passed.toFloat() / total else 0f
    return Pair(passed, progress)
}
