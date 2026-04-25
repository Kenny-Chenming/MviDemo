package com.mvi.kenny.feature.bubbles.bubblebar

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.bubbles.BubblesColors
import com.mvi.kenny.feature.bubbles.BubblesIntent
import com.mvi.kenny.feature.bubbles.BubblesViewModel
import com.mvi.kenny.feature.bubbles.BubbleBarGuide

/**
 * ============================================================
 * BubbleBarIntegrationScreen — Bubble Bar 集成指南
 * ============================================================
 * PRD-148: Android 17 App Bubbles 浮窗开发工具包
 * 展示如何让自己的 App 出现在 Bubble Bar 中，管理多个浮窗状态。
 *
 * Bubble Bar 是大屏设备（折叠屏/平板）Taskbar 的特性，
 * 小屏设备行为不同。本工具提供分步骤集成指南。
 *
 * 集成步骤：
 * 1. 添加 Bubble Metadata 声明
 * 2. 创建 Bubble XML 配置
 * 3. 处理生命周期回调
 * 4. 大屏设备测试验证
 *
 * @param viewModel BubblesViewModel instance / BubblesViewModel 实例
 * @see BubbleBarGuide 集成指南步骤数据类
 * @see BubblesIntent.CopyCode 复制代码片段
 */
@Composable
fun BubbleBarIntegrationScreen(
    viewModel: BubblesViewModel = viewModel()
) {
    // Collect state from ViewModel / 从 ViewModel 收集状态
    val state by viewModel.state.collectAsState()

    // Track completed steps / 跟踪已完成步骤
    var completedSteps by remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ============================================================
        // Header / 头部
        // ============================================================
        Text(
            text = "Bubble Bar Integration",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Bubble Bar 集成指南 — 让你的 App 出现在大屏设备的 Bubble Bar 中",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Info Banner / 信息横幅
        // ============================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BubblesColors.PiPColor.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = BubblesColors.PiPColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bubble Bar 仅在大屏设备（折叠屏/平板）Taskbar 中显示。小屏设备 Bubble 行为由系统独立管理。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Progress Summary / 进度摘要
        // ============================================================
        val totalSteps = state.bubbleBarGuideSteps.size
        val doneSteps = completedSteps.size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress / 进度",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$doneSteps / $totalSteps completed",
                style = MaterialTheme.typography.labelMedium,
                color = if (doneSteps == totalSteps) BubblesColors.BubblesActive
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar / 进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = if (totalSteps > 0) doneSteps.toFloat() / totalSteps else 0f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(BubblesColors.BubblesActive)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Guide Steps / 指南步骤
        // ============================================================
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(state.bubbleBarGuideSteps) { index, guide ->
                BubbleBarGuideCard(
                    guide = guide,
                    stepNumber = index + 1,
                    isCompleted = completedSteps.contains(index + 1),
                    onToggleComplete = {
                        completedSteps = if (completedSteps.contains(index + 1)) {
                            completedSteps - (index + 1)
                        } else {
                            completedSteps + (index + 1)
                        }
                    },
                    onCopyCode = { code ->
                        if (code != null) viewModel.sendIntent(BubblesIntent.CopyCode(code))
                    }
                )
            }

            // Bottom spacing / 底部留白
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/**
 * ============================================================
 * BubbleBarGuideCard — 集成指南步骤卡片
 * ============================================================
 * 展示单个集成步骤，包含步骤编号、标题、描述、代码示例和完成状态。
 *
 * @param guide Guide step data / 指南步骤数据
 * @param stepNumber Step number (1-indexed) / 步骤编号
 * @param isCompleted Whether this step is completed / 是否已完成
 * @param onToggleComplete Callback when user toggles completion / 切换完成状态回调
 * @param onCopyCode Callback to copy code snippet / 复制代码片段回调
 */
@Composable
private fun BubbleBarGuideCard(
    guide: BubbleBarGuide,
    stepNumber: Int,
    isCompleted: Boolean,
    onToggleComplete: () -> Unit,
    onCopyCode: (String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                BubblesColors.BubblesActive.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: Step number + Title + Checkbox / 头部：步骤编号 + 标题 + 复选框
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Step number circle / 步骤编号圆圈
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) BubblesColors.BubblesActive
                                else BubblesColors.Primary.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = stepNumber.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BubblesColors.Primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = guide.titleZh,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted)
                                BubblesColors.BubblesActive
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = guide.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { onToggleComplete() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description / 描述
            Text(
                text = guide.descriptionZh,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = guide.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            // Code snippet / 代码片段
            if (guide.codeSnippet != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E2E) // 深色代码背景
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Code / 代码",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF888888)
                            )
                            IconButton(
                                onClick = { onCopyCode(guide.codeSnippet) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy code",
                                    tint = Color(0xFF888888),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = guide.codeSnippet,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF89B4FA)
                        )
                    }
                }
            }
        }
    }
}
