package com.mvi.kenny.feature.aapmmonitor

/**
 * ============================================================
 * AAPMonitorScreen.kt — 主界面 UI
 * ============================================================
 */

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

private object AAPMColors {
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
    val AAPMActive = Color(0xFFE94560)
    val AAPMInactive = Color(0xFF27AE60)
    val AAPMUnknown = Color(0xFFF39C12)
}

@Composable
fun AAPMonitorScreen(
    state: AAPMonitorState,
    onIntent: (AAPMonitorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("仪表盘", "扫描结果", "AAPM监控", "合规引导", "知识库")

    Column(modifier = modifier.fillMaxSize().background(AAPMColors.Background)) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = AAPMColors.CardBackground,
            contentColor = AAPMColors.TextPrimary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, color = if (selectedTab == index) AAPMColors.TextPrimary else AAPMColors.TextSecondary) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> DashboardTab(state, onIntent)
                1 -> ScanResultsTab(state, onIntent)
                2 -> AAPMMonitorTab(state, onIntent)
                3 -> ComplianceGuideTab(state, onIntent)
                4 -> KnowledgeBaseTab(state, onIntent)
            }
        }
    }
}

@Composable
private fun DashboardTab(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AAPM 检测仪表盘", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AAPMColors.TextPrimary)
        ScanStatusCard(state, onIntent)
        QuickStatsRow(state)
        QuickActionsCard(state, onIntent)
        if (state.scanResults.isNotEmpty()) {
            RecentIssuesPreview(state, onIntent)
        }
    }
}

@Composable
private fun ScanStatusCard(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (state.scanStatus) {
                ScanStatus.IDLE -> {
                    Icon(Icons.Default.PlayCircle, null, tint = AAPMColors.TextSecondary, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("点击开始扫描", color = AAPMColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text("检测项目中的 AccessibilityService 使用情况", color = AAPMColors.TextSecondary, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { onIntent(AAPMonitorIntent.StartScan) }, colors = ButtonDefaults.buttonColors(containerColor = AAPMColors.SafeColor)) {
                        Icon(Icons.Default.Search, null); Spacer(Modifier.width(8.dp)); Text("开始扫描")
                    }
                }
                ScanStatus.SCANNING -> {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(state.scanProgress, Modifier.size(80.dp), AAPMColors.P1Color, 8.dp, AAPMColors.SurfaceVariant)
                        Text("${(state.scanProgress * 100).toInt()}%", color = AAPMColors.TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("正在扫描...", color = AAPMColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = { onIntent(AAPMonitorIntent.CancelScan) }) { Text("取消") }
                }
                ScanStatus.DONE -> {
                    Icon(Icons.Default.CheckCircle, null, tint = AAPMColors.SafeColor, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("扫描完成", color = AAPMColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text("发现 ${state.unresolvedCount} 个问题", color = if (state.unresolvedCount > 0) AAPMColors.P0Color else AAPMColors.SafeColor, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { onIntent(AAPMonitorIntent.StartScan) }, colors = ButtonDefaults.buttonColors(containerColor = AAPMColors.SurfaceVariant)) {
                        Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(8.dp)); Text("重新扫描")
                    }
                }
                ScanStatus.ERROR -> {
                    Icon(Icons.Default.Error, null, tint = AAPMColors.P0Color, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("扫描出错", color = AAPMColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text(state.error ?: "未知错误", color = AAPMColors.P0Color, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(state: AAPMonitorState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("P0 问题", "${state.p0Count}", AAPMColors.P0Color, Modifier.weight(1f))
        StatCard("P1 问题", "${state.p1Count}", AAPMColors.P1Color, Modifier.weight(1f))
        StatCard("P2 问题", "${state.p2Count}", AAPMColors.P2Color, Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 12.sp, color = AAPMColors.TextSecondary)
        }
    }
}

@Composable
private fun QuickActionsCard(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("快捷操作", color = AAPMColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onIntent(AAPMonitorIntent.ExportReport) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = AAPMColors.TextPrimary)) {
                    Icon(Icons.Default.Share, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("导出报告", fontSize = 12.sp)
                }
                OutlinedButton(onClick = { onIntent(AAPMonitorIntent.RefreshAapmStatus) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = AAPMColors.TextPrimary)) {
                    Icon(Icons.Default.Refresh, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("刷新AAPM", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun RecentIssuesPreview(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("最近问题", color = AAPMColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                TextButton(onClick = {}) { Text("查看全部", color = AAPMColors.SafeColor) }
            }
            Spacer(Modifier.height(8.dp))
            state.scanResults.take(3).forEachIndexed { index, issue ->
                IssuePreviewItem(issue, { onIntent(AAPMonitorIntent.SelectIssue(issue)) })
                if (index < 2) HorizontalDivider(color = AAPMColors.SurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun IssuePreviewItem(issue: AccessibilityIssue, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(when (issue.severity) { Severity.P0 -> AAPMColors.P0Color; Severity.P1 -> AAPMColors.P1Color; Severity.P2 -> AAPMColors.P2Color }))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(issue.serviceName, color = AAPMColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(issue.filePath.split("/").last(), color = AAPMColors.TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        SeverityBadge(issue.severity)
    }
}

@Composable
private fun ScanResultsTab(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        FilterChipsRow(state, onIntent)
        Spacer(Modifier.height(16.dp))
        if (state.filteredResults.isEmpty()) {
            EmptyState(Icons.Default.CheckCircle, "没有发现问题", if (state.scanStatus == ScanStatus.IDLE) "请先运行扫描" else "所有问题已修复")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.filteredResults, key = { it.id }) { issue ->
                    ProblemCard(issue, { onIntent(AAPMonitorIntent.SelectIssue(issue)) }, { onIntent(AAPMonitorIntent.MarkIssueResolved(issue)) })
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = state.filterOptions.module == null,
                onClick = { onIntent(AAPMonitorIntent.SetFilter(FilterOptions())) },
                label = { Text("全部") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AAPMColors.SafeColor, selectedLabelColor = AAPMColors.TextPrimary)
            )
        }
        item {
            FilterChip(
                selected = state.filterOptions.module == "P0",
                onClick = { onIntent(AAPMonitorIntent.SetFilter(FilterOptions(module = "P0"))) },
                label = { Text("P0") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AAPMColors.P0Color, selectedLabelColor = AAPMColors.TextPrimary)
            )
        }
        item {
            FilterChip(
                selected = state.filterOptions.module == "P1",
                onClick = { onIntent(AAPMonitorIntent.SetFilter(FilterOptions(module = "P1"))) },
                label = { Text("P1") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AAPMColors.P1Color, selectedLabelColor = AAPMColors.TextPrimary)
            )
        }
        item {
            FilterChip(
                selected = state.filterOptions.module == "P2",
                onClick = { onIntent(AAPMonitorIntent.SetFilter(FilterOptions(module = "P2"))) },
                label = { Text("P2") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AAPMColors.P2Color, selectedLabelColor = AAPMColors.Background)
            )
        }
    }
}

@Composable
private fun ProblemCard(issue: AccessibilityIssue, onClick: () -> Unit, onResolve: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground), shape = RoundedCornerShape(12.dp)) {
        Row {
            Box(Modifier.width(4.dp).fillMaxHeight().background(when (issue.severity) { Severity.P0 -> AAPMColors.P0Color; Severity.P1 -> AAPMColors.P1Color; Severity.P2 -> AAPMColors.P2Color }))
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(issue.serviceName, color = AAPMColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    SeverityBadge(issue.severity)
                }
                Spacer(Modifier.height(8.dp))
                Text(issue.description, color = AAPMColors.TextSecondary, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(issue.filePath.split("/").takeLast(2).joinToString("/"), color = AAPMColors.TextTertiary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    if (!issue.isResolved) {
                        TextButton(onClick = onResolve) { Icon(Icons.Default.Check, null, tint = AAPMColors.SafeColor, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("标记已解决", color = AAPMColors.SafeColor, fontSize = 12.sp) }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = AAPMColors.SafeColor, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("已解决", color = AAPMColors.SafeColor, fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeverityBadge(severity: Severity) {
    val (color, text) = when (severity) { Severity.P0 -> AAPMColors.P0Color to "P0"; Severity.P1 -> AAPMColors.P1Color to "P1"; Severity.P2 -> AAPMColors.P2Color to "P2" }
    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AAPMMonitorTab(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 0.3f, animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse), label = "pulse_alpha")

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("AAPM 实时监控", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AAPMColors.TextPrimary)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Box(Modifier.size(16.dp).clip(CircleShape).background(when (state.aapmStatus) { AapmStatus.ACTIVE -> AAPMColors.AAPMActive.copy(alpha = alpha); AapmStatus.INACTIVE -> AAPMColors.AAPMInactive; AapmStatus.UNKNOWN -> AAPMColors.AAPMUnknown }))
                    Spacer(Modifier.width(12.dp))
                    Text(when (state.aapmStatus) { AapmStatus.ACTIVE -> "AAPM 已激活"; AapmStatus.INACTIVE -> "AAPM 未激活"; AapmStatus.UNKNOWN -> "状态未知" }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = when (state.aapmStatus) { AapmStatus.ACTIVE -> AAPMColors.AAPMActive; AapmStatus.INACTIVE -> AAPMColors.AAPMInactive; AapmStatus.UNKNOWN -> AAPMColors.AAPMUnknown })
                }
                Spacer(Modifier.height(16.dp))
                Text(when (state.aapmStatus) { AapmStatus.ACTIVE -> "您的设备已启用高级保护模式"; AapmStatus.INACTIVE -> "当前设备未启用高级保护模式"; AapmStatus.UNKNOWN -> "无法获取 AAPM 状态" }, color = AAPMColors.TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = { onIntent(AAPMonitorIntent.RefreshAapmStatus) }, enabled = !state.isLoading) {
                    if (state.isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp) else Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(8.dp)); Text("刷新状态")
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("设备信息", color = AAPMColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                state.deviceInfo?.let { info ->
                    InfoRow("SDK 版本", "${info.sdkVersion}")
                    InfoRow("设备厂商", info.manufacturer)
                    InfoRow("设备型号", info.model)
                    InfoRow("Root 状态", if (info.isRooted) "是" else "否")
                } ?: Text("点击刷新获取设备信息", color = AAPMColors.TextSecondary, fontSize = 14.sp)
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("建议降级策略", color = AAPMColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                StrategyItem("1", "Autofill API 迁移", "密码管理器迁移至 AutofillService")
                StrategyItem("2", "Screen Capture API", "截屏功能迁移至 MediaProjection")
                StrategyItem("3", "NotificationListener", "消息拦截迁移至 NotificationListenerService")
                StrategyItem("4", "InputMethodService", "安全键盘迁移至自定义输入法")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = AAPMColors.TextSecondary, fontSize = 14.sp)
        Text(value, color = AAPMColors.TextPrimary, fontSize = 14.sp)
    }
}

@Composable
private fun StrategyItem(number: String, title: String, description: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(24.dp).clip(CircleShape).background(AAPMColors.SafeColor), contentAlignment = Alignment.Center) { Text(number, color = AAPMColors.Background, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(12.dp))
        Column { Text(title, color = AAPMColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium); Text(description, color = AAPMColors.TextSecondary, fontSize = 12.sp) }
    }
}

@Composable
private fun ComplianceGuideTab(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("合规标注引导", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AAPMColors.TextPrimary)
        Text("按照以下步骤完成 Android 17 AAPM 合规", color = AAPMColors.TextSecondary, fontSize = 14.sp)
        StepperIndicator(state.complianceStep)
        ComplianceStepContent(state.complianceStep, { onIntent(AAPMonitorIntent.SetComplianceStep(it)) })
    }
}

@Composable
private fun StepperIndicator(currentStep: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        (1..4).forEach { step ->
            val isCompleted = step < currentStep
            val isCurrent = step == currentStep
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(when { isCompleted -> AAPMColors.SafeColor; isCurrent -> AAPMColors.P1Color; else -> AAPMColors.SurfaceVariant }), contentAlignment = Alignment.Center) {
                    if (isCompleted) Icon(Icons.Default.Check, null, tint = AAPMColors.Background, Modifier.size(20.dp)) else Text("$step", color = if (isCurrent) AAPMColors.Background else AAPMColors.TextSecondary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text(when (step) { 1 -> "理解"; 2 -> "检查"; 3 -> "设置"; 4 -> "申请"; else -> "" }, color = if (isCurrent) AAPMColors.TextPrimary else AAPMColors.TextSecondary, fontSize = 12.sp)
            }
            if (step < 4) Box(Modifier.weight(1f).height(2.dp).padding(horizontal = 8.dp).background(if (isCompleted) AAPMColors.SafeColor else AAPMColors.SurfaceVariant))
        }
    }
}

@Composable
private fun ComplianceStepContent(step: Int, onStepChange: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            val stepTitle = when (step) { 1 -> "理解 Android 17 AAPM 要求"; 2 -> "检查应用资格"; 3 -> "设置 isAccessibilityTool 标志"; 4 -> "申请辅助工具认证"; else -> "" }
            val stepDesc = when (step) { 1 -> "了解高级保护模式对辅助工具的影响"; 2 -> "确认您的应用是否符合辅助工具认证条件"; 3 -> "在 AndroidManifest.xml 中正确配置"; 4 -> "提交 Google 认证申请流程"; else -> "" }
            Text(stepTitle, color = AAPMColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(stepDesc, color = AAPMColors.TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            when (step) {
                1 -> { BulletPoint("所有未标记为 isAccessibilityTool=true 的应用将被完全禁止使用 AccessibilityService API"); BulletPoint("受影响应用：密码管理器、自动化工具、屏幕录制"); BulletPoint("强制迁移窗口：Android 17 正式发布后") }
                2 -> { BulletPoint("密码管理器（Password Manager）"); BulletPoint("自动化工具（Tasker、MacroDroid 等）"); BulletPoint("屏幕录制/截图应用"); BulletPoint("需要 Google 认证审核") }
                3 -> { Text("在 AndroidManifest.xml 中添加以下配置：", color = AAPMColors.TextSecondary, fontSize = 14.sp); Spacer(Modifier.height(8.dp)); CodeBlock("""<accessibility-service android:accessibilityToolType="passwordManager" ... />""") }
                4 -> { BulletPoint("准备应用说明文档和截图"); BulletPoint("在 Google Play Console 提交审核"); BulletPoint("等待 Google 安全团队审核（通常 2-4 周）") }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (step > 1) OutlinedButton(onClick = { onStepChange(step - 1) }) { Icon(Icons.Default.ArrowBack, null); Spacer(Modifier.width(8.dp)); Text("上一步") } else Spacer(Modifier.width(1.dp))
                if (step < 4) Button(onClick = { onStepChange(step + 1) }) { Text("下一步"); Spacer(Modifier.width(8.dp)); Icon(Icons.Default.ArrowForward, null) } else Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = AAPMColors.SafeColor)) { Icon(Icons.Default.Check, null); Spacer(Modifier.width(8.dp)); Text("完成") }
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AAPMColors.Background).padding(12.dp).horizontalScroll(rememberScrollState())) {
        Text(code, color = AAPMColors.SafeColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun KnowledgeBaseTab(state: AAPMonitorState, onIntent: (AAPMonitorIntent) -> Unit) {
    LaunchedEffect(Unit) { onIntent(AAPMonitorIntent.LoadKnowledgeBase) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("替代方案知识库", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AAPMColors.TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("从 AccessibilityService 迁移到 Scoped API", color = AAPMColors.TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onIntent(AAPMonitorIntent.SetSearchQuery(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索替代方案...", color = AAPMColors.TextTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = AAPMColors.TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AAPMColors.SafeColor, unfocusedBorderColor = AAPMColors.SurfaceVariant, focusedTextColor = AAPMColors.TextPrimary, unfocusedTextColor = AAPMColors.TextPrimary, cursorColor = AAPMColors.SafeColor),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        val filteredEntries = state.knowledgeEntries.filter { entry -> state.searchQuery.isEmpty() || entry.name.contains(state.searchQuery, ignoreCase = true) || entry.description.contains(state.searchQuery, ignoreCase = true) }
        if (filteredEntries.isEmpty()) {
            EmptyState(Icons.Default.Book, "未找到相关知识库条目", "尝试其他搜索关键词")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredEntries, key = { it.name }) { entry -> KnowledgeBaseItemCard(entry) }
            }
        }
    }
}

@Composable
private fun KnowledgeBaseItemCard(entry: ScopedApiEntry) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AAPMColors.CardBackground), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(entry.name, color = AAPMColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.ArrowForward, null, tint = AAPMColors.TextSecondary, Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(entry.description, color = AAPMColors.TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Text("替代方案：", color = AAPMColors.TextTertiary, fontSize = 12.sp)
            Text(entry.alternative, color = AAPMColors.SafeColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(8.dp))
            Text("适用场景：", color = AAPMColors.TextTertiary, fontSize = 12.sp)
            Text(entry.applicableScenario, color = AAPMColors.P1Color, fontSize = 12.sp)
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
        Text("•", color = AAPMColors.SafeColor, modifier = Modifier.padding(end = 8.dp))
        Text(text, color = AAPMColors.TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = AAPMColors.TextTertiary, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(message, color = AAPMColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(description, color = AAPMColors.TextSecondary, fontSize = 14.sp)
    }
}
