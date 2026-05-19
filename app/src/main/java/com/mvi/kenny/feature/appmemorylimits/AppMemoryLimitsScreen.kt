package com.mvi.kenny.feature.appmemorylimits

// ================================================================
// AppMemoryLimitsScreen — Android 17 App Memory Limits Developer Toolkit
// ================================================================
// Developer Dashboard UI screen for Android 17 App Memory Limits Toolkit.
//
// PRD-262: Android 17 App Memory Limits 开发者适配工具包
//
// 8 Developer Tools:
//   1. 查询工具 — Device memory limit query
//   2. 调试工具包 — MemoryLimiter debug toolkit
//   3. CI验证工具 — CI validator
//   4. 影响评估工具 — Impact assessment
//   5. 内存优化指南 — Optimization guide
//   6. Trigger-based Profiling 集成指南 — Profiling integration
//   7. OOM Killer 关系解读 — OOM Killer relationship
//   8. 多设备 RAM 分级测试工具 — Multi-device RAM testing
//
// Visual Style: Material Design 3, Developer Dashboard
// ================================================================

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

// ─────────────────────────────────────────────────────────────────
// Color aliases
// ─────────────────────────────────────────────────────────────────
private val C = AppMemoryLimitsColors

// ─────────────────────────────────────────────────────────────────
// Main Screen Composable
// ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMemoryLimitsScreen(
    viewModel: AppMemoryLimitsViewModel = remember { AppMemoryLimitsViewModel() }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Collect effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AppMemoryLimitsEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AppMemoryLimitsEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is AppMemoryLimitsEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Code", effect.code))
                    Toast.makeText(context, "代码已复制", Toast.LENGTH_SHORT).show()
                }
                is AppMemoryLimitsEffect.ExportFile -> {
                    Toast.makeText(context, "报告已导出 (" + effect.format.displayName + ")", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Auto-detect device on first load
    LaunchedEffect(Unit) {
        viewModel.sendIntent(AppMemoryLimitsIntent.DetectDevice)
    }

    val selectedToolName = if (state.selectedToolIndex >= 0) {
        state.tools.getOrNull(state.selectedToolIndex)?.name ?: "工具详情"
    } else {
        "App Memory Limits"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedToolName) },
                navigationIcon = {
                    if (state.selectedToolIndex >= 0) {
                        IconButton(onClick = { viewModel.sendIntent(AppMemoryLimitsIntent.SelectTool(-1)) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    if (state.selectedToolIndex >= 0) {
                        val tool = state.tools.getOrNull(state.selectedToolIndex)
                        if (tool?.hasCI == true) {
                            IconButton(onClick = {
                                viewModel.sendIntent(AppMemoryLimitsIntent.ExportReport(ExportFormat.JSON))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "导出报告")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = C.Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(C.Background)
        ) {
            if (state.selectedToolIndex >= 0) {
                val tool = state.tools.getOrNull(state.selectedToolIndex)
                if (tool != null) {
                    ToolDetailContent(
                        tool = tool,
                        state = state,
                        onIntent = { intent -> viewModel.sendIntent(intent) }
                    )
                }
            } else {
                OverviewContent(
                    state = state,
                    onIntent = { intent -> viewModel.sendIntent(intent) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Overview Mode — 概览页
// ─────────────────────────────────────────────────────────────────
@Composable
private fun OverviewContent(
    state: AppMemoryLimitsState,
    onIntent: (AppMemoryLimitsIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Android 17 App Memory Limits",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = C.OnSurface
                )
                Text(
                    text = "MemoryLimiter · Per-app RAM Caps · Android 17",
                    style = MaterialTheme.typography.bodySmall,
                    color = C.OnSurfaceVariant
                )
            }
        }

        // Category legend
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryLegendItem(category = ToolCategory.QUERY, emoji = "🔍", label = "查询")
                CategoryLegendItem(category = ToolCategory.DEBUG, emoji = "🔧", label = "调试")
                CategoryLegendItem(category = ToolCategory.CI, emoji = "✅", label = "CI验证")
                CategoryLegendItem(category = ToolCategory.GUIDE, emoji = "📖", label = "指南")
            }
        }

        // Device Info Card
        item {
            DeviceInfoCard(state = state, onIntent = onIntent)
        }

        // Risk Assessment Card
        item {
            RiskAssessmentCard(state = state, onIntent = onIntent)
        }

        // Tool Grid Header
        item {
            Text(
                text = "开发者工具",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = C.OnSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // 4x2 Tool Grid — iterate in pairs
        val toolRows = state.tools.chunked(2)
        itemsIndexed(toolRows) { _: Int, row: List<DevTool> ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { tool ->
                    ToolCard(
                        tool = tool,
                        onClick = { onIntent(AppMemoryLimitsIntent.SelectTool(tool.id - 1)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Quick Simulator
        item {
            QuickSimulatorCard(state = state, onIntent = onIntent)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────
// Device Info Card
// ─────────────────────────────────────────────────────────────────
@Composable
private fun DeviceInfoCard(
    state: AppMemoryLimitsState,
    onIntent: (AppMemoryLimitsIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Memory,
                    contentDescription = null,
                    tint = C.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "设备信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    OutlinedButton(onClick = { onIntent(AppMemoryLimitsIntent.DetectDevice) }) {
                        Text("刷新", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "设备总内存", value = formatBytes(state.deviceRamBytes), valueColor = C.OnSurface)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(label = "Per-App 内存上限", value = formatBytes(state.appMemoryLimitBytes), valueColor = C.Primary)
            Spacer(modifier = Modifier.height(12.dp))
            val ramPercent = if (state.deviceRamBytes > 0L) "25%" else "—"
            Text(
                text = "系统内存使用上限: " + ramPercent + " of device RAM (max 512MB)",
                style = MaterialTheme.typography.bodySmall,
                color = C.OnSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Risk Assessment Card
// ─────────────────────────────────────────────────────────────────
@Composable
private fun RiskAssessmentCard(
    state: AppMemoryLimitsState,
    onIntent: (AppMemoryLimitsIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = state.riskLevel.color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "风险评估",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AppType.entries.forEachIndexed { index, appType ->
                    SegmentedButton(
                        selected = state.currentAppType == appType,
                        onClick = { onIntent(AppMemoryLimitsIntent.LoadRiskAssessment(appType)) },
                        shape = SegmentedButtonDefaults.itemShape(index, AppType.entries.size)
                    ) {
                        Text(appType.displayName, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            RiskBadge(riskLevel = state.riskLevel)
            Spacer(modifier = Modifier.height(8.dp))

            val suggestion = when (state.riskLevel) {
                AppRiskLevel.HIGH -> "建议优先优化：Bitmap / 对象池 / 缓存策略"
                AppRiskLevel.MEDIUM -> "建议监控内存使用，关注后台进程"
                AppRiskLevel.LOW -> "当前风险较低，保持良好的内存管理习惯"
            }
            Text(
                text = suggestion,
                style = MaterialTheme.typography.bodySmall,
                color = C.OnSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Risk Badge
// ─────────────────────────────────────────────────────────────────
@Composable
private fun RiskBadge(riskLevel: AppRiskLevel) {
    Surface(
        color = riskLevel.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = riskLevel.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = riskLevel.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = riskLevel.color
            )
            Spacer(modifier = Modifier.width(8.dp))
            val desc = when (riskLevel) {
                AppRiskLevel.HIGH -> "可能被 MemoryLimiter 杀死"
                AppRiskLevel.MEDIUM -> "需要关注内存使用"
                AppRiskLevel.LOW -> "风险可控"
            }
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = riskLevel.color)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tool Card
// ─────────────────────────────────────────────────────────────────
@Composable
private fun ToolCard(
    tool: DevTool,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color = C.Primary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = tool.category.emoji, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tool.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = C.OnSurface
                    )
                }
                if (tool.hasCI) {
                    Icon(
                        Icons.Default.FactCheck,
                        contentDescription = "CI支持",
                        tint = C.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tool.shortDescription,
                style = MaterialTheme.typography.bodySmall,
                color = C.OnSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            CategoryBadge(category = tool.category)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Category Badge
// ─────────────────────────────────────────────────────────────────
@Composable
private fun CategoryBadge(category: ToolCategory) {
    val bgColor = when (category) {
        ToolCategory.QUERY -> C.Primary.copy(alpha = 0.1f)
        ToolCategory.DEBUG -> Color(0xFFFF6B6B).copy(alpha = 0.1f)
        ToolCategory.CI -> C.LowRisk.copy(alpha = 0.1f)
        ToolCategory.GUIDE -> Color(0xFF9B59B6).copy(alpha = 0.1f)
    }
    val textColor = when (category) {
        ToolCategory.QUERY -> C.Primary
        ToolCategory.DEBUG -> Color(0xFFFF6B6B)
        ToolCategory.CI -> C.LowRisk
        ToolCategory.GUIDE -> Color(0xFF9B59B6)
    }
    Surface(color = bgColor, shape = RoundedCornerShape(4.dp)) {
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Category Legend Item
// ─────────────────────────────────────────────────────────────────
@Composable
private fun CategoryLegendItem(category: ToolCategory, emoji: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = C.OnSurfaceVariant)
    }
}

// ─────────────────────────────────────────────────────────────────
// Quick Simulator Card
// ─────────────────────────────────────────────────────────────────
@Composable
private fun QuickSimulatorCard(
    state: AppMemoryLimitsState,
    onIntent: (AppMemoryLimitsIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = C.Primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RAM 分级模拟器",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ramOptions = listOf(
                    6L * 1024 * 1024 * 1024 to "6GB",
                    8L * 1024 * 1024 * 1024 to "8GB",
                    12L * 1024 * 1024 * 1024 to "12GB",
                    16L * 1024 * 1024 * 1024 to "16GB"
                )
                ramOptions.forEach { (ram, label) ->
                    val isSelected = state.simulatorResult?.deviceRam == ram
                    Button(
                        onClick = { onIntent(AppMemoryLimitsIntent.RunSimulator(ram)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) C.Primary else C.Background,
                            contentColor = if (isSelected) Color.White else C.Primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label, fontSize = 12.sp)
                    }
                }
            }

            state.simulatorResult?.let { result ->
                Spacer(modifier = Modifier.height(12.dp))
                SimulatorResultCard(result = result)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Simulator Result Card
// ─────────────────────────────────────────────────────────────────
@Composable
private fun SimulatorResultCard(result: SimulatorResult) {
    Surface(
        color = result.riskLevel.color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatBytes(result.deviceRam) + " 设备",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "上限: " + formatBytes(result.appMemoryLimit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = C.Primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            result.warnings.forEach { warning ->
                Text(text = warning, style = MaterialTheme.typography.bodySmall, color = result.riskLevel.color)
            }
            if (result.suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 " + result.suggestions.first(),
                    style = MaterialTheme.typography.bodySmall,
                    color = C.OnSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tool Detail Content — 工具详情页
// ─────────────────────────────────────────────────────────────────
@Composable
private fun ToolDetailContent(
    tool: DevTool,
    state: AppMemoryLimitsState,
    onIntent: (AppMemoryLimitsIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ToolHeader(tool = tool) }

        item {
            SectionCard(title = "背景", icon = Icons.Default.Description) {
                Text(text = tool.background, style = MaterialTheme.typography.bodyMedium, color = C.OnSurface)
            }
        }

        item {
            SectionCard(title = "核心功能", icon = Icons.Default.CheckCircle) {
                tool.features.forEach { feature ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("• ", color = C.Primary)
                        Text(feature, style = MaterialTheme.typography.bodyMedium, color = C.OnSurface)
                    }
                }
            }
        }

        if (tool.codeExample.isNotBlank()) {
            item {
                SectionCard(title = "代码示例", icon = Icons.Default.Code) {
                    CodeBlock(
                        code = tool.codeExample,
                        onCopy = { onIntent(AppMemoryLimitsIntent.CopyCode(tool.codeExample)) }
                    )
                }
            }
        }

        if (!tool.gradleConfig.isNullOrBlank()) {
            item {
                SectionCard(title = "Gradle 配置", icon = Icons.Default.Layers) {
                    CodeBlock(
                        code = tool.gradleConfig,
                        onCopy = { onIntent(AppMemoryLimitsIntent.CopyCode(tool.gradleConfig)) }
                    )
                }
            }
        }

        if (!tool.ciExample.isNullOrBlank()) {
            item {
                SectionCard(title = "CI 集成示例 (GitHub Actions)", icon = Icons.Default.FactCheck) {
                    CodeBlock(
                        code = tool.ciExample,
                        onCopy = { onIntent(AppMemoryLimitsIntent.CopyCode(tool.ciExample)) }
                    )
                }
            }
        }

        if (!tool.outputFormat.isNullOrBlank()) {
            item {
                SectionCard(title = "输出格式", icon = Icons.Default.Description) {
                    Surface(
                        color = C.Primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = tool.outputFormat,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = C.Primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────
// Tool Header
// ─────────────────────────────────────────────────────────────────
@Composable
private fun ToolHeader(tool: DevTool) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color = C.Primary.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = tool.category.emoji, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tool.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = C.OnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryBadge(category = tool.category)
                        if (tool.hasCI) {
                            Surface(
                                color = C.LowRisk.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.FactCheck,
                                        contentDescription = null,
                                        tint = C.LowRisk,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "CI 支持", style = MaterialTheme.typography.labelSmall, color = C.LowRisk)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = tool.shortDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = C.OnSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Section Card
// ─────────────────────────────────────────────────────────────────
@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = C.Primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = C.OnSurface)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Code Block
// ─────────────────────────────────────────────────────────────────
@Composable
private fun CodeBlock(
    code: String,
    onCopy: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = "复制", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("复制代码", fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, C.OnSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            color = C.CodeBlock,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFE0E0E0),
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Info Row Helper
// ─────────────────────────────────────────────────────────────────
@Composable
private fun InfoRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = C.OnSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
