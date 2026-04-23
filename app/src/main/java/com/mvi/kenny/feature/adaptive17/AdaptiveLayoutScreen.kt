package com.mvi.kenny.feature.adaptive17

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

private val GoogleBlue = Color(0xFF1A73E8)
private val CriticalRed = Color(0xFFD93025)
private val WarningYellow = Color(0xFFF9AB00)
private val LowGreen = Color(0xFF1E8E3E)
private val BackgroundDark = Color(0xFF1E1E1E)
private val BackgroundCard = Color(0xFF2D2D2D)
private val TextPrimary = Color(0xFFE8EAED)
private val TextSecondary = Color(0xFF9AA0A6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveLayoutScreen(
    scanViewModel: AdaptiveScanViewModel,
    templateViewModel: TemplateGeneratorViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("扫描仪表盘", "模板生成器")

    val scanState by scanViewModel.state.collectAsStateWithLifecycle()
    val templateState by templateViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scanViewModel.effect.collectLatest { effect ->
            when (effect) {
                is ScanEffect.ShowViolationDetail -> { }
                is ScanEffect.NavigateToTemplateGenerator -> {
                    templateViewModel.handleIntent(TemplateIntent.UpdateActivityName(effect.activityName))
                    selectedTab = 1
                }
                is ScanEffect.CopyFixCode -> {
                    copyToClipboard(context, effect.code)
                }
                is ScanEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is ScanEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        templateViewModel.effect.collectLatest { effect ->
            when (effect) {
                is TemplateEffect.CopySuccess -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is TemplateEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is TemplateEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    icon = {
                        Icon(
                            imageVector = if (index == 0) Icons.Default.Warning else Icons.Default.Description,
                            contentDescription = null
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> ScanDashboardTab(state = scanState, onIntent = scanViewModel::handleIntent)
            1 -> TemplateGeneratorTab(state = templateState, onIntent = templateViewModel::handleIntent)
        }
    }
}

@Composable
private fun ScanDashboardTab(
    state: AdaptiveScanState,
    onIntent: (ScanIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScanStatusCard(
                isScanning = state.isScanning,
                progress = state.progress,
                onStartScan = { onIntent(ScanIntent.StartScan) },
                onExportHtml = { onIntent(ScanIntent.ExportHtml) }
            )
        }

        if (state.totalCount > 0) {
            item {
                SummaryStatsRow(
                    criticalCount = state.criticalCount,
                    warningCount = state.warningCount,
                    lowCount = state.lowCount,
                    totalCount = state.totalCount
                )
            }

            item {
                Text(
                    text = "违规清单",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(
                items = state.violations,
                key = { it.id }
            ) { violation ->
                ViolationCard(
                    violation = violation,
                    onApplyFix = { onIntent(ScanIntent.ApplyFix(violation.id)) },
                    onViewDetail = { onIntent(ScanIntent.SelectViolation(violation)) }
                )
            }
        }

        if (!state.isScanning && state.totalCount == 0) {
            item { EmptyStateCard() }
        }

        state.error?.let { error ->
            item {
                ErrorCard(error = error, onDismiss = { onIntent(ScanIntent.DismissError) })
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ScanStatusCard(
    isScanning: Boolean,
    progress: Float,
    onStartScan: () -> Unit,
    onExportHtml: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = GoogleBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Android 17 自适应布局检测",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GoogleBlue, strokeWidth = 2.dp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "检测 screenOrientation / resizeableActivity / maxAspectRatio 等 Android 17 大屏限制",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isScanning) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = GoogleBlue,
                    trackColor = BackgroundDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${(progress * 100).toInt()}% 检测中...", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onStartScan,
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isScanning) "扫描中..." else "开始扫描")
                }

                Button(
                    onClick = onExportHtml,
                    enabled = !isScanning && progress > 0f,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C4043), disabledContainerColor = Color(0xFF5F6368)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导出报告")
                }
            }
        }
    }
}

@Composable
private fun SummaryStatsRow(
    criticalCount: Int,
    warningCount: Int,
    lowCount: Int,
    totalCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatChip(label = "严重", count = criticalCount, color = CriticalRed)
        StatChip(label = "中等", count = warningCount, color = WarningYellow)
        StatChip(label = "低级", count = lowCount, color = LowGreen)
        StatChip(label = "总计", count = totalCount, color = GoogleBlue)
    }
}

@Composable
private fun StatChip(label: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$label: $count",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun ViolationCard(
    violation: Violation,
    onApplyFix: () -> Unit,
    onViewDetail: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val severityColor = when (violation.severity) {
        ViolationSeverity.CRITICAL -> CriticalRed
        ViolationSeverity.WARNING -> WarningYellow
        ViolationSeverity.LOW -> LowGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize().clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = BackgroundCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(12.dp).background(severityColor, RoundedCornerShape(6.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = violation.id, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontFamily = FontFamily.Monospace)
                        Text(text = violation.activityName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = violation.violationType.labelZh, style = MaterialTheme.typography.bodySmall, color = severityColor)
                    }
                }

                Surface(shape = RoundedCornerShape(8.dp), color = severityColor.copy(alpha = 0.15f)) {
                    Text(
                        text = violation.severity.labelZh,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = severityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = violation.filePath,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                Text(text = "L${violation.lineNumber}", style = MaterialTheme.typography.bodySmall, color = GoogleBlue, fontFamily = FontFamily.Monospace)
            }

            if (isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "描述", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = violation.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "建议修复", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            .background(BackgroundDark, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF5F6368), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = violation.suggestedFix,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF81C995),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onApplyFix,
                            colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (violation.isAutoFixable) "复制修复" else "生成模板", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onViewDetail,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C4043)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("查看详情", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BackgroundCard)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Default.Window, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "暂无扫描结果", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(text = "点击上方「开始扫描」检测项目中的自适应布局违规", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun ErrorCard(error: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CriticalRed.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CriticalRed)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = error, style = MaterialTheme.typography.bodySmall, color = CriticalRed)
            }
            Button(onClick = onDismiss) { Text("关闭") }
        }
    }
}

@Composable
private fun TemplateGeneratorTab(
    state: TemplateState,
    onIntent: (TemplateIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.selectedActivity,
            onValueChange = { onIntent(TemplateIntent.UpdateActivityName(it)) },
            label = { Text("Activity 名称") },
            placeholder = { Text("例如：CameraActivity") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "布局场景", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LayoutScenario.entries.forEach { scenario ->
                FilterChip(
                    selected = state.selectedScenario == scenario,
                    onClick = { onIntent(TemplateIntent.SelectScenario(scenario)) },
                    label = { Text(scenario.labelZh) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoogleBlue.copy(alpha = 0.2f),
                        selectedLabelColor = GoogleBlue
                    )
                )
            }
        }

        Surface(shape = RoundedCornerShape(8.dp), color = BackgroundCard, modifier = Modifier.fillMaxWidth()) {
            Text(text = state.selectedScenario.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(12.dp))
        }

        Button(
            onClick = { onIntent(TemplateIntent.GenerateTemplate) },
            enabled = !state.isGenerating,
            colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (state.isGenerating) "生成中..." else "生成模板代码")
        }

        state.generatedCode?.let { code ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "生成的模板代码", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                IconButton(onClick = { onIntent(TemplateIntent.CopyToClipboard) }) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "复制代码", tint = GoogleBlue)
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f)
                    .background(BackgroundDark, RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF5F6368), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(text = code, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = Color(0xFFE8EAED), fontSize = 11.sp)
                }
            }
        }

        if (state.generatedCode == null && !state.isGenerating) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = BackgroundCard)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "选择场景并点击生成", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(text = "将生成可运行的 Compose 模板代码", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Adaptive Layout Fix", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}
