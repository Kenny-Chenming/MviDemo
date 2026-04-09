package com.mvi.kenny.feature.aapmmonitor

/**
 * ============================================================
 * ServiceDetailScreen.kt — 问题详情页
 * ServiceDetailScreen.kt — Issue Detail Screen
 * ============================================================
 * 显示单个 AccessibilityService 问题详情，包括调用链火焰图、影响范围和修复建议
 * Displays single AccessibilityService issue details including call chain flame chart,
 * impact scope, and fix suggestions
 */

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================
// Theme Colors — 主题颜色（与 AAPMonitorScreen 保持一致）
// Theme Colors — Consistent with AAPMonitorScreen
// ============================================================

private object DetailColors {
    val Background = Color(0xFF1A1A2E)
    val CardBackground = Color(0xFF16213E)
    val SurfaceVariant = Color(0xFF1F2B47)
    val P0Color = Color(0xFFE94560)
    val P1Color = Color(0xFFF39C12)
    val P2Color = Color(0xFFF1C40F)
    val SafeColor = Color(0xFF27AE60)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA0A0B0)
    val TextTertiary = Color(0xFF6B6B80)
}

// ============================================================
// ServiceDetailScreen — 问题详情页
// ServiceDetailScreen — Issue Detail Screen
// ============================================================

/**
 * 问题详情页
 * Issue detail screen
 *
 * @param issue 要展示的问题
 * @param onNavigateBack 返回回调
 * @param onMarkResolved 标记已解决回调
 * @param onOpenFile 打开文件回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    issue: AccessibilityIssue,
    onNavigateBack: () -> Unit,
    onMarkResolved: (AccessibilityIssue) -> Unit,
    onOpenFile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isCallChainExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "问题详情",
                        color = DetailColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = DetailColors.TextPrimary
                        )
                    }
                },
                actions = {
                    if (!issue.isResolved) {
                        IconButton(onClick = { onMarkResolved(issue) }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "标记已解决",
                                tint = DetailColors.SafeColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DetailColors.CardBackground
                )
            )
        },
        containerColor = DetailColors.Background
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========== 严重程度与状态卡片 ==========
            // Severity and Status Card
            SeverityStatusCard(issue = issue)

            // ========== 问题基本信息 ==========
            // Issue Basic Info
            IssueInfoCard(issue = issue)

            // ========== 调用链火焰图 ==========
            // Call Chain Flame Chart
            if (issue.callChain.isNotEmpty()) {
                CallChainCard(
                    callChain = issue.callChain,
                    isExpanded = isCallChainExpanded,
                    onToggle = { isCallChainExpanded = !isCallChainExpanded }
                )
            }

            // ========== 修复建议 ==========
            // Fix Suggestion Card
            FixSuggestionCard(issue = issue)

            // ========== 文件路径 ==========
            // File Path Card
            FilePathCard(
                filePath = issue.filePath,
                onOpenFile = { onOpenFile(issue.filePath) }
            )

            // ========== 底部操作按钮 ==========
            // Bottom Action Buttons
            ActionButtons(
                issue = issue,
                onMarkResolved = { onMarkResolved(issue) }
            )
        }
    }
}

// ============================================================
// Severity Status Card — 严重程度与状态卡片
// ============================================================

@Composable
private fun SeverityStatusCard(issue: AccessibilityIssue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DetailColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 严重程度指示器
            // Severity indicator
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        when (issue.severity) {
                            Severity.P0 -> DetailColors.P0Color
                            Severity.P1 -> DetailColors.P1Color
                            Severity.P2 -> DetailColors.P2Color
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = issue.severity.name,
                    color = DetailColors.Background,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (issue.severity) {
                        Severity.P0 -> "崩溃级问题"
                        Severity.P1 -> "功能失效级"
                        Severity.P2 -> "警告级问题"
                    },
                    color = DetailColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (issue.severity) {
                        Severity.P0 -> "必须立即修复，否则 App 在 AAPM 模式下完全不可用"
                        Severity.P1 -> "核心功能不可用，建议优先修复"
                        Severity.P2 -> "潜在问题，建议关注并计划修复"
                    },
                    color = DetailColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ============================================================
// Issue Info Card — 问题信息卡片
// ============================================================

@Composable
private fun IssueInfoCard(issue: AccessibilityIssue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DetailColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 服务名称
            // Service Name
            SectionTitle(title = "服务名称", icon = Icons.Default.Settings)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = issue.serviceName,
                color = DetailColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 问题描述
            // Issue Description
            SectionTitle(title = "问题描述", icon = Icons.Default.Warning)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = issue.description,
                color = DetailColors.TextSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )

            // 已解决状态
            // Resolved Status
            if (issue.isResolved) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DetailColors.SafeColor.copy(alpha = 0.15f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = DetailColors.SafeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "此问题已标记为已解决",
                        color = DetailColors.SafeColor,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ============================================================
// Call Chain Card — 调用链火焰图卡片
// ============================================================

@Composable
private fun CallChainCard(
    callChain: List<String>,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = DetailColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = DetailColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "调用链（${callChain.size} 层）",
                        color = DetailColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = DetailColors.TextSecondary
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "调用深度越深，颜色越浅，表示问题根源越深",
                    color = DetailColors.TextTertiary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 火焰图可视化
                // Flame chart visualization
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    callChain.forEachIndexed { index, method ->
                        CallChainLevel(
                            methodName = method,
                            depth = index,
                            maxDepth = callChain.size
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 原始调用链文本
                // Raw call chain text
                Text(
                    text = "原始调用链：",
                    color = DetailColors.TextTertiary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DetailColors.Background)
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = callChain.joinToString(" → "),
                        color = DetailColors.TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ============================================================
// Call Chain Level — 调用链层级节点
// ============================================================

@Composable
private fun CallChainLevel(methodName: String, depth: Int, maxDepth: Int) {
    val alpha = 1f - (depth.toFloat() / maxDepth) * 0.5f
    val color = when {
        depth == 0 -> DetailColors.P0Color
        depth == 1 -> DetailColors.P1Color
        else -> DetailColors.P2Color
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 深度指示
        // Depth indicator
        Box(
            modifier = Modifier
                .width((depth * 16 + 8).dp)
                .height(2.dp)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = alpha * 0.2f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = methodName,
                color = DetailColors.TextPrimary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================
// Fix Suggestion Card — 修复建议卡片
// ============================================================

@Composable
private fun FixSuggestionCard(issue: AccessibilityIssue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DetailColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = DetailColors.SafeColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "修复建议",
                    color = DetailColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = issue.suggestedFix,
                color = DetailColors.TextSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 快速操作
            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionChip(
                    label = "复制建议",
                    icon = Icons.Default.ContentCopy,
                    onClick = { /* Copy to clipboard */ }
                )
                QuickActionChip(
                    label = "查看文档",
                    icon = Icons.Default.OpenInNew,
                    onClick = { /* Open documentation */ }
                )
            }
        }
    }
}

// ============================================================
// File Path Card — 文件路径卡片
// ============================================================

@Composable
private fun FilePathCard(filePath: String, onOpenFile: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DetailColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = DetailColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "文件位置",
                    color = DetailColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DetailColors.Background)
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = filePath,
                    color = DetailColors.TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenFile,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, DetailColors.SurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("在编辑器中打开", color = DetailColors.TextPrimary)
            }
        }
    }
}

// ============================================================
// Action Buttons — 底部操作按钮
// ============================================================

@Composable
private fun ActionButtons(
    issue: AccessibilityIssue,
    onMarkResolved: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!issue.isResolved) {
            Button(
                onClick = onMarkResolved,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailColors.SafeColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("标记为已解决", fontSize = 16.sp)
            }
        }

        OutlinedButton(
            onClick = { /* Navigate to similar issues */ },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, DetailColors.SurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = DetailColors.TextPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("查看类似问题", color = DetailColors.TextPrimary, fontSize = 16.sp)
        }
    }
}

// ============================================================
// Common Components — 通用组件
// ============================================================

@Composable
private fun SectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DetailColors.TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = DetailColors.TextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun RowScope.QuickActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        border = BorderStroke(1.dp, DetailColors.SurfaceVariant),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = DetailColors.TextPrimary)
    }
}
