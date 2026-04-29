package com.mvi.kenny.feature.lannetwork

// ================================================================
// LanPermissionScreen — PRD-199 Android 17 Local Network Permission
// 合规检测工具包主界面
// ================================================================
// Main UI screen for Android 17 Local Network Permission toolkit.
//
// Contains:
//   - LanPermissionDashboard (首页仪表盘，8 模块卡片)
//   - ScannerScreen (LAN 权限扫描器)
//   - PermissionTemplateScreen (权限集成模板)
//   - DevicePickerScreen (系统设备选择器)
//   - CIToolScreen (CI 合规检测工具)
//   - FallbackTemplateScreen (降级策略模板)
//   - CoordinationGuideScreen (LAN × Wi-Fi 协同指南)
//   - Legacy评估Screen (Legacy App 兼容性评估)
//   - DiscoveryGuideScreen (设备发现新模式)
//
// PRD-199: Android 17 Local Network Permission 合规检测工具包
// Design: memory/agency/designs/PRD-199-Android-17-Local-Network-Permission-合规检测工具包.md
// ================================================================

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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Color Theme — 深色 Terminal 风格配色
// ================================================================
private object LanPermissionColors {
    val primary = Color(0xFF4FC3F7)
    val secondary = Color(0xFF81C784)
    val error = Color(0xFFEF5350)
    val warning = Color(0xFFFFB74D)
    val surface = Color(0xFF1E1E1E)
    val background = Color(0xFF121212)
    val onSurface = Color(0xFFE0E0E0)
    val codeBackground = Color(0xFF2D2D2D)
    val border = Color(0xFF3D3D3D)
    val p2Color = Color(0xFF4FC3F7)
}

// ================================================================
// LanPermissionScreen — Main entry point
// ================================================================
@Composable
fun LanPermissionScreen(
    viewModel: LanPermissionViewModel,
    onNavigateToCode: (String, Int) -> Unit = { _, _ -> },
    onShowToast: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LanPermissionEffect.ShowToast -> onShowToast(effect.message)
                is LanPermissionEffect.CodeCopied -> onShowToast("Code copied / 代码已复制")
                is LanPermissionEffect.ShowError -> onShowToast("Error: ${effect.message}")
                is LanPermissionEffect.NavigateToCode -> onNavigateToCode(effect.filePath, effect.line)
                is LanPermissionEffect.ScanCompleted -> onShowToast("Scan completed / 扫描完成")
                is LanPermissionEffect.CiCheckCompleted -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LanPermissionColors.background)
    ) {
        when (val module = state.selectedModule) {
            null -> LanPermissionDashboard(
                state = state,
                onModuleClick = { viewModel.processIntent(LanPermissionIntent.SelectModule(it)) }
            )
            ModuleType.SCANNER -> ScannerScreen(
                state = state,
                onStartScan = { viewModel.processIntent(LanPermissionIntent.StartScan) },
                onCancelScan = { viewModel.processIntent(LanPermissionIntent.CancelScan) },
                onFilterRisk = { viewModel.processIntent(LanPermissionIntent.FilterByRisk(it)) },
                onNavigateToCode = onNavigateToCode,
                onBack = { viewModel.processIntent(LanPermissionIntent.NavigateBack) }
            )
            ModuleType.PERMISSION_TEMPLATE -> PermissionTemplateScreen(
                state = state,
                onSelectTemplate = { viewModel.processIntent(LanPermissionIntent.SelectTemplate(it)) },
                onCopyCode = { viewModel.processIntent(LanPermissionIntent.CopyCode(it)) },
                onBack = { viewModel.processIntent(LanPermissionIntent.NavigateBack) }
            )
            ModuleType.DEVICE_PICKER -> DevicePickerScreen(
                state = state,
                onCopyCode = { viewModel.processIntent(LanPermissionIntent.CopyCode(it)) },
                onBack = { viewModel.processIntent(LanPermissionIntent.NavigateBack) }
            )
            ModuleType.CI_TOOL -> CIToolScreen(
                state = state,
                onUpdateConfig = { viewModel.processIntent(LanPermissionIntent.UpdateCiConfig(it)) },
                onRunCheck = { viewModel.processIntent(LanPermissionIntent.RunCiCheck) },
                onCopyCode = { viewModel.processIntent(LanPermissionIntent.CopyCode(it)) },
                onBack = { viewModel.processIntent(LanPermissionIntent.NavigateBack) }
            )
            ModuleType.FALLBACK_TEMPLATE -> FallbackTemplateScreen(
                state = state,
                onCopyCode = { viewModel.processIntent(LanPermissionIntent.CopyCode(it)) },
                onBack = { viewModel.processIntent(LanPermissionIntent.NavigateBack) }
            )
            ModuleType.COORDINATION_GUIDE -> CoordinationGuideScreen(
                state = state,
                onCopyCode = { viewModel.processIntent(LanPermissionIntent.CopyCode(it)) },
                onBack = { viewModel.processIntent(LanPermissionIntent.NavigateBack) }
            )
            ModuleType.LEGACY_EVAL -> Legacy评估Screen(
                state = state,
                onBack = { viewModel.processIntent(LanPermissionIntent.NavigateBack) }
            )
            ModuleType.DISCOVERY_GUIDE -> DiscoveryGuideScreen(
                state = state,
                onCopyCode = { viewModel.processIntent(LanPermissionIntent.CopyCode(it)) },
                onBack = { viewModel.processIntent(LanPermissionIntent.NavigateBack) }
            )
        }
    }
}

// ================================================================
// LanPermissionDashboard — 首页仪表盘（8 模块卡片）
// ================================================================
@Composable
private fun LanPermissionDashboard(
    state: LanPermissionState,
    onModuleClick: (ModuleType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        DashboardHeader()
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ModuleType.entries.chunked(2)) { rowModules ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowModules.forEach { module ->
                        ModuleCard(
                            module = module,
                            onClick = { onModuleClick(module) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowModules.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF0D47A1))
                )
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = LanPermissionColors.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "🛡️ Android 17 Local Network Permission",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Android 17 | API 37+ | 局域网权限合规",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Android 17 引入 Local Network Protection，所有 targeting API 37+ 且访问局域网的 App 必须申请 ACCESS_LOCAL_NETWORK 运行时权限。",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ModuleCard(
    module: ModuleType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (module.priority) {
        "P0" -> LanPermissionColors.error
        "P1" -> LanPermissionColors.warning
        else -> LanPermissionColors.primary
    }

    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = LanPermissionColors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = module.iconEmoji, fontSize = 24.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(priorityColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = module.priority,
                        color = priorityColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = module.titleZh,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = module.titleEn,
                color = LanPermissionColors.primary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = module.description,
                color = LanPermissionColors.onSurface.copy(alpha = 0.6f),
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

// ================================================================
// ScannerScreen — LAN 权限影响扫描器
// ================================================================
@Composable
private fun ScannerScreen(
    state: LanPermissionState,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit,
    onFilterRisk: (RiskLevel?) -> Unit,
    onNavigateToCode: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenTopBar(
            title = "🔍 LAN 权限影响扫描器",
            subtitle = "LAN Permission Impact Scanner",
            onBack = onBack
        )
        Spacer(modifier = Modifier.height(16.dp))
        ScanControlPanel(
            scanProgress = state.scanProgress,
            onStartScan = onStartScan,
            onCancelScan = onCancelScan
        )
        Spacer(modifier = Modifier.height(16.dp))
        RiskFilterChips(
            currentFilter = state.riskFilter,
            p0Count = state.p0Count,
            p1Count = state.p1Count,
            p2Count = state.p2Count,
            onFilterSelect = onFilterRisk
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (state.hasResults) {
            Text(
                text = "📊 扫描结果 / Scan Results (${state.filteredResults.size})",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.filteredResults) { result ->
                    ScanResultCard(
                        result = result,
                        onClick = { onNavigateToCode(result.file, result.line) }
                    )
                }
            }
        } else if (state.scanProgress == ScanProgress.IDLE) {
            EmptyScanState()
        }
    }
}

@Composable
private fun ScanControlPanel(
    scanProgress: ScanProgress,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LanPermissionColors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "项目扫描 / Project Scan",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "扫描 App 源码中的所有局域网访问模式",
                        color = LanPermissionColors.onSurface.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
                when (scanProgress) {
                    ScanProgress.SCANNING -> Button(
                        onClick = onCancelScan,
                        colors = ButtonDefaults.buttonColors(containerColor = LanPermissionColors.error)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("取消 / Cancel")
                    }
                    else -> Button(
                        onClick = onStartScan,
                        colors = ButtonDefaults.buttonColors(containerColor = LanPermissionColors.primary)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("开始扫描 / Start Scan")
                    }
                }
            }
            if (scanProgress == ScanProgress.SCANNING) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = LanPermissionColors.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = scanProgress.label, color = LanPermissionColors.primary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun RiskFilterChips(
    currentFilter: RiskLevel?,
    p0Count: Int, p1Count: Int, p2Count: Int,
    onFilterSelect: (RiskLevel?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(label = "全部 / All", count = p0Count + p1Count + p2Count, color = LanPermissionColors.primary, isSelected = currentFilter == null, onClick = { onFilterSelect(null) })
        FilterChip(label = "P0", count = p0Count, color = LanPermissionColors.error, isSelected = currentFilter == RiskLevel.P0, onClick = { onFilterSelect(RiskLevel.P0) })
        FilterChip(label = "P1", count = p1Count, color = LanPermissionColors.warning, isSelected = currentFilter == RiskLevel.P1, onClick = { onFilterSelect(RiskLevel.P1) })
        FilterChip(label = "P2", count = p2Count, color = LanPermissionColors.p2Color, isSelected = currentFilter == RiskLevel.P2, onClick = { onFilterSelect(RiskLevel.P2) })
    }
}

@Composable
private fun FilterChip(label: String, count: Int, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) color.copy(alpha = 0.3f) else LanPermissionColors.surface)
            .border(width = 1.dp, color = if (isSelected) color else LanPermissionColors.border, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$label ($count)",
            color = if (isSelected) color else LanPermissionColors.onSurface,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ScanResultCard(result: ScanResult, onClick: () -> Unit) {
    val riskColor = when (result.riskLevel) {
        RiskLevel.P0 -> LanPermissionColors.error
        RiskLevel.P1 -> LanPermissionColors.warning
        RiskLevel.P2 -> LanPermissionColors.p2Color
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = LanPermissionColors.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(riskColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = result.riskLevel.label, color = riskColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = result.accessType, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                if (result.isCompliant) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Compliant", tint = LanPermissionColors.secondary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${result.file}:${result.line}", color = LanPermissionColors.primary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(LanPermissionColors.codeBackground)
                    .padding(8.dp)
            ) {
                Text(text = result.snippet, color = LanPermissionColors.onSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = result.suggestion, color = riskColor, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmptyScanState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = LanPermissionColors.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "点击「开始扫描」分析项目", color = LanPermissionColors.onSurface.copy(alpha = 0.5f), fontSize = 14.sp, textAlign = TextAlign.Center)
        Text(text = "Click 'Start Scan' to analyze your project", color = LanPermissionColors.onSurface.copy(alpha = 0.3f), fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

// ================================================================
// PermissionTemplateScreen — 运行时权限集成模板
// ================================================================
@Composable
private fun PermissionTemplateScreen(
    state: LanPermissionState,
    onSelectTemplate: (TemplateType) -> Unit,
    onCopyCode: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScreenTopBar(title = "📝 运行时权限集成模板", subtitle = "Runtime Permission Template", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        TemplateTabs(selectedTemplate = state.selectedTemplate, onSelectTemplate = onSelectTemplate)
        Spacer(modifier = Modifier.height(12.dp))
        val templates = state.codeTemplates[ModuleType.PERMISSION_TEMPLATE] ?: emptyList()
        val selectedTemplate = templates.find { it.templateType == state.selectedTemplate } ?: templates.firstOrNull()
        if (selectedTemplate != null) {
            CodeTemplateContent(template = selectedTemplate, onCopyCode = onCopyCode)
        }
    }
}

// ================================================================
// DevicePickerScreen — 系统设备选择器集成
// ================================================================
@Composable
private fun DevicePickerScreen(
    state: LanPermissionState,
    onCopyCode: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScreenTopBar(title = "🎯 系统设备选择器集成", subtitle = "System Device Picker Integration", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        InfoCard(
            title = "隐私保护路径 / Privacy-First Approach",
            description = "使用系统设备选择器（NearbyDevices API），无需申请 ACCESS_LOCAL_NETWORK 权限，操作系统处理设备发现，用户隐私得到最大保护。"
        )
        Spacer(modifier = Modifier.height(12.dp))
        val templates = state.codeTemplates[ModuleType.DEVICE_PICKER] ?: emptyList()
        templates.forEach { template ->
            CodeTemplateCard(template = template, onCopyCode = { onCopyCode(template.code) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ================================================================
// CIToolScreen — CI 合规检测工具
// ================================================================
@Composable
private fun CIToolScreen(
    state: LanPermissionState,
    onUpdateConfig: (CiConfig) -> Unit,
    onRunCheck: () -> Unit,
    onCopyCode: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScreenTopBar(title = "⚙️ CI 合规检测工具", subtitle = "CI Compliance Detection Tool", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        CiConfigCard(config = state.ciConfig, isLoading = state.isLoading, onRunCheck = onRunCheck)
        Spacer(modifier = Modifier.height(12.dp))
        val templates = state.codeTemplates[ModuleType.CI_TOOL] ?: emptyList()
        templates.forEach { template ->
            CodeTemplateCard(template = template, onCopyCode = { onCopyCode(template.code) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CiConfigCard(config: CiConfig, isLoading: Boolean, onRunCheck: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LanPermissionColors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "⚙️ CI 配置 / Configuration", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            ConfigItem(label = "Target SDK", value = "${config.targetSdk}")
            ConfigItem(label = "Min SDK", value = "${config.minSdk}")
            ConfigItem(label = "Check Legacy", value = "${config.checkLegacy}")
            ConfigItem(label = "Report Format", value = config.reportFormat)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRunCheck,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LanPermissionColors.primary),
                enabled = !isLoading
            ) {
                Text("运行 CI 检测 / Run CI Check")
                Spacer(modifier = Modifier.width(8.dp))
                Text("运行 CI 检测 / Run CI Check")
            }
        }
    }
}

@Composable
private fun ConfigItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = LanPermissionColors.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        Text(text = value, color = LanPermissionColors.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ================================================================
// FallbackTemplateScreen — 降级策略模板
// ================================================================
@Composable
private fun FallbackTemplateScreen(
    state: LanPermissionState,
    onCopyCode: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScreenTopBar(title = "🔄 降级策略模板", subtitle = "Fallback Strategy Template", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        InfoCard(
            title = "降级原则 / Fallback Principles",
            description = "降级不等于禁用功能。应提示用户「当前无法发现设备，请在设置中开启局域网权限」，不要静默失败。"
        )
        Spacer(modifier = Modifier.height(12.dp))
        val templates = state.codeTemplates[ModuleType.FALLBACK_TEMPLATE] ?: emptyList()
        templates.forEach { template ->
            CodeTemplateCard(template = template, onCopyCode = { onCopyCode(template.code) })
        }
    }
}

// ================================================================
// CoordinationGuideScreen — LAN × Wi-Fi 协同指南
// ================================================================
@Composable
private fun CoordinationGuideScreen(
    state: LanPermissionState,
    onCopyCode: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScreenTopBar(title = "📡 LAN × Wi-Fi 协同指南", subtitle = "LAN × NEARBY_WIFI_DEVICES Guide", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        DecisionTreeCard()
        Spacer(modifier = Modifier.height(12.dp))
        val templates = state.codeTemplates[ModuleType.COORDINATION_GUIDE] ?: emptyList()
        templates.forEach { template ->
            CodeTemplateCard(template = template, onCopyCode = { onCopyCode(template.code) })
        }
    }
}

@Composable
private fun DecisionTreeCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LanPermissionColors.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "🌳 权限选择决策树 / Permission Decision Tree", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            DecisionTreeNode(
                question = "1️⃣ 设备发现是否可以使用系统选择器？",
                options = listOf("是 → 使用 NearbyDevices API (无需权限)", "否 → 继续问题 2"),
                recommendation = "✓ 隐私优先方案"
            )
            HorizontalDivider(color = LanPermissionColors.border, modifier = Modifier.padding(vertical = 8.dp))
            DecisionTreeNode(
                question = "2️⃣ 是否只需要 Wi-Fi 设备发现？",
                options = listOf("是 → 使用 NEARBY_WIFI_DEVICES (无需主动请求)", "否 → 需要 ACCESS_LOCAL_NETWORK"),
                recommendation = "⚠️ 部分设备可能需要两个权限组合"
            )
            HorizontalDivider(color = LanPermissionColors.border, modifier = Modifier.padding(vertical = 8.dp))
            DecisionTreeNode(
                question = "3️⃣ 是否需要访问以太网设备？",
                options = listOf("是 → 必须使用 ACCESS_LOCAL_NETWORK", "否 → 考虑 NEARBY_WIFI_DEVICES"),
                recommendation = "🔴 ACCESS_LOCAL_NETWORK 覆盖 Wi-Fi 和 Ethernet"
            )
        }
    }
}

@Composable
private fun DecisionTreeNode(question: String, options: List<String>, recommendation: String) {
    Column {
        Text(text = question, color = LanPermissionColors.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        options.forEach { option ->
            Text(text = "  • $option", color = LanPermissionColors.onSurface, fontSize = 11.sp, lineHeight = 16.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = recommendation, color = LanPermissionColors.secondary, fontSize = 11.sp)
    }
}

// ================================================================
// Legacy评估Screen — Legacy App 兼容性评估
// ================================================================
@Composable
private fun Legacy评估Screen(state: LanPermissionState, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScreenTopBar(title = "📋 Legacy App 兼容性评估", subtitle = "Legacy App Compatibility Evaluation", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        InfoCard(
            title = "Legacy App 兼容性说明 / Compatibility Notes",
            description = "targetSdk < 37 的 App 保留原有行为（通过 INTERNET 权限隐式授权），但从 targetSdk 升级到 37+ 时需要申请 ACCESS_LOCAL_NETWORK 运行时权限。"
        )
        Spacer(modifier = Modifier.height(12.dp))
        CompatibilityMatrix()
    }
}

@Composable
private fun CompatibilityMatrix() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LanPermissionColors.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "📊 兼容性矩阵 / Compatibility Matrix", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "targetSdk", color = LanPermissionColors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                Text(text = "权限需求", color = LanPermissionColors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text(text = "行为", color = LanPermissionColors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
            }
            HorizontalDivider(color = LanPermissionColors.border, modifier = Modifier.padding(vertical = 6.dp))
            CompatibilityRowItem(sdk = "< 37", permission = "INTERNET (隐式)", behavior = "原有行为", color = LanPermissionColors.secondary)
            CompatibilityRowItem(sdk = "37+", permission = "ACCESS_LOCAL_NETWORK", behavior = "需要运行时请求", color = LanPermissionColors.warning)
        }
    }
}

@Composable
private fun CompatibilityRowItem(sdk: String, permission: String, behavior: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = sdk, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.5f))
        Text(text = permission, color = LanPermissionColors.onSurface, fontSize = 11.sp, modifier = Modifier.weight(2f))
        Text(text = behavior, color = LanPermissionColors.onSurface.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.weight(1.5f))
    }
}

// ================================================================
// DiscoveryGuideScreen — 设备发现新模式指南
// ================================================================
@Composable
private fun DiscoveryGuideScreen(
    state: LanPermissionState,
    onCopyCode: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScreenTopBar(title = "🌐 设备发现新模式指南", subtitle = "Device Discovery New Pattern Guide", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        DiscoveryPatternsCard()
        Spacer(modifier = Modifier.height(12.dp))
        val templates = state.codeTemplates[ModuleType.DISCOVERY_GUIDE] ?: emptyList()
        templates.forEach { template ->
            CodeTemplateCard(template = template, onCopyCode = { onCopyCode(template.code) })
        }
    }
}

@Composable
private fun DiscoveryPatternsCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = LanPermissionColors.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "🔍 发现协议对比 / Discovery Protocol Comparison", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            DiscoveryProtocolRow(protocol = "mDNS / Bonjour", description = "本地网络服务发现，基于 DNS-SD", permission = "ACCESS_LOCAL_NETWORK", useCase = "零配置网络，适用于本地设备发现")
            HorizontalDivider(color = LanPermissionColors.border, modifier = Modifier.padding(vertical = 6.dp))
            DiscoveryProtocolRow(protocol = "UPnP / SSDP", description = "通用即插即用协议", permission = "ACCESS_LOCAL_NETWORK", useCase = "路由器、智能家居设备发现")
            HorizontalDivider(color = LanPermissionColors.border, modifier = Modifier.padding(vertical = 6.dp))
            DiscoveryProtocolRow(protocol = "NearbyDevices API", description = "系统级设备选择器", permission = "无 (系统处理)", useCase = "隐私优先的设备发现方案")
        }
    }
}

@Composable
private fun DiscoveryProtocolRow(protocol: String, description: String, permission: String, useCase: String) {
    Column {
        Text(text = protocol, color = LanPermissionColors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = description, color = LanPermissionColors.onSurface.copy(alpha = 0.7f), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "权限: $permission", color = if (permission.startsWith("无")) LanPermissionColors.secondary else LanPermissionColors.warning, fontSize = 10.sp)
        Text(text = "场景: $useCase", color = LanPermissionColors.onSurface.copy(alpha = 0.6f), fontSize = 10.sp)
    }
}

// ================================================================
// Shared Components — 共享组件
// ================================================================

@Composable
private fun ScreenTopBar(title: String, subtitle: String, onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back / 返回",
                tint = LanPermissionColors.primary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = LanPermissionColors.primary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun InfoCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LanPermissionColors.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = LanPermissionColors.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = LanPermissionColors.onSurface.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun TemplateTabs(
    selectedTemplate: TemplateType,
    onSelectTemplate: (TemplateType) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = TemplateType.entries.indexOf(selectedTemplate),
        containerColor = LanPermissionColors.surface,
        contentColor = LanPermissionColors.primary,
        edgePadding = 0.dp
    ) {
        TemplateType.entries.forEach { template ->
            Tab(
                selected = template == selectedTemplate,
                onClick = { onSelectTemplate(template) },
                text = {
                    Text(
                        text = template.label,
                        color = if (template == selectedTemplate) LanPermissionColors.primary
                                else LanPermissionColors.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            )
        }
    }
}

@Composable
private fun CodeTemplateContent(
    template: CodeTemplate,
    onCopyCode: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = template.description,
                    color = LanPermissionColors.onSurface.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = { onCopyCode(template.code) }) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy / 复制",
                    tint = LanPermissionColors.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(LanPermissionColors.codeBackground)
                .border(width = 1.dp, color = LanPermissionColors.border, shape = RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            SelectionContainer {
                Text(
                    text = template.code,
                    color = LanPermissionColors.onSurface,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun CodeTemplateCard(
    template: CodeTemplate,
    onCopyCode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LanPermissionColors.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = template.description,
                        color = LanPermissionColors.onSurface.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
                IconButton(onClick = onCopyCode) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy / 复制",
                        tint = LanPermissionColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(LanPermissionColors.codeBackground)
                    .padding(10.dp)
            ) {
                SelectionContainer {
                    Text(
                        text = template.code,
                        color = LanPermissionColors.onSurface,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 15,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
