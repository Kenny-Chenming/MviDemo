package com.mvi.kenny.feature.perappmemorylimits

// ================================================================
// PerAppMemoryLimitsScreen — Android 17 Per-App Memory Limits Screen
// ================================================================
// Terminal-style UI screen for Android 17 Per-App Memory Limits Toolkit.
//
// PRD-235: Android 17 Per-App 内存限制检测与优化工具包
// Design Reference: memory/agency/designs/PRD-235-Android-17-Per-App-内存限制检测与优化工具包.md
//
// Five tool tabs:
//   1. Calculator — Device RAM → App Memory Limit calculator
//   2. Leak Scanner — Source code memory leak pattern scanner
//   3. Monitor SDK — Runtime memory monitoring SDK integration
//   4. Pressure Test — CI memory pressure test on Android Emulator
//   5. Gradle Plugin — CI compliance check Gradle plugin
//
// Visual Style: Dark terminal CLI theme
//   - CYAN titles, GREEN pass, YELLOW warn, RED fail
//   - JetBrains Mono font, 4px grid spacing
// ================================================================

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest

// ─────────────────────────────────────────────────────────────────
// Color aliases — color palette from contract
// ─────────────────────────────────────────────────────────────────
private val C = PerAppMemoryColors

// ─────────────────────────────────────────────────────────────────
// PerAppMemoryLimitsScreen — Main Composable
// ─────────────────────────────────────────────────────────────────
@Composable
fun PerAppMemoryLimitsScreen(
    viewModel: PerAppMemoryLimitsViewModel = remember { PerAppMemoryLimitsViewModel() }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Collect effects
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    is PerAppMemoryLimitsEffect.ShowToast -> { /* handled by UI */ }
                    is PerAppMemoryLimitsEffect.ShowError -> { /* handled by UI */ }
                    is PerAppMemoryLimitsEffect.OpenFile -> { /* handle file open */ }
                    is PerAppMemoryLimitsEffect.ScanCompleted -> { /* handled via state */ }
                    is PerAppMemoryLimitsEffect.MemoryWarning -> { /* handled via state */ }
                    is PerAppMemoryLimitsEffect.MemoryCritical -> { /* handled via state */ }
                    is PerAppMemoryLimitsEffect.OOMOccurred -> { /* handled via state */ }
                    is PerAppMemoryLimitsEffect.ReportGenerated -> { /* handled via state */ }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C.Background)
            .padding(16.dp)
    ) {
        // Header
        HeaderSection()

        Spacer(modifier = Modifier.height(12.dp))

        // Tab navigation
        TabNavigation(
            selectedTab = state.selectedTab,
            onTabSelected = { viewModel.processIntent(PerAppMemoryLimitsIntent.SelectTab(it)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tool content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (state.selectedTab) {
                ToolTab.CALCULATOR -> CalculatorTab(
                    state = state.calculatorState,
                    onIntent = viewModel::processIntent
                )
                ToolTab.LEAK_SCANNER -> LeakScannerTab(
                    state = state.leakScannerState,
                    onIntent = viewModel::processIntent
                )
                ToolTab.MONITOR_SDK -> MonitorSDKTab(
                    state = state.monitorSDKState,
                    onIntent = viewModel::processIntent
                )
                ToolTab.PRESSURE_TEST -> PressureTestTab(
                    state = state.pressureTestState,
                    onIntent = viewModel::processIntent
                )
                ToolTab.GRADLE_PLUGIN -> GradlePluginTab(
                    state = state.gradlePluginState,
                    onIntent = viewModel::processIntent
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Header Section
// ─────────────────────────────────────────────────────────────────
@Composable
private fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(C.Surface)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = null,
                tint = C.Primary,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = "Android 17 Per-App Memory Limits",
                    color = C.Primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Per-App Memory Limits Detection & Optimization Toolkit",
                    color = C.OnSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusChip("Android 17+", C.Accent)
            StatusChip("Beta 4 Ready", C.Pass)
            StatusChip("CI Ready", C.Primary)
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab Navigation
// ─────────────────────────────────────────────────────────────────
@Composable
private fun TabNavigation(
    selectedTab: ToolTab,
    onTabSelected: (ToolTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .clip(RoundedCornerShape(8.dp))
            .background(C.Surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ToolTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) C.Primary.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = tab.emoji,
                        fontSize = 14.sp
                    )
                    Text(
                        text = tab.displayName,
                        color = if (isSelected) C.Primary else C.OnSurfaceVariant,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Calculator Tab
// ─────────────────────────────────────────────────────────────────
@Composable
private fun CalculatorTab(
    state: CalculatorState,
    onIntent: (PerAppMemoryLimitsIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section: Select RAM Tier
        CLISection(title = "📊 Device RAM Tier Selection") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select device RAM tier to calculate per-app memory limit:",
                    color = C.OnSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeviceRamTier.entries.filter { it != DeviceRamTier.CUSTOM }.forEach { tier ->
                        val isSelected = state.selectedTier == tier
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) C.Primary.copy(alpha = 0.15f) else C.SurfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) C.Primary else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onIntent(PerAppMemoryLimitsIntent.SelectTier(tier)) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${tier.ramGB}GB",
                                    color = if (isSelected) C.Primary else C.OnSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = tier.displayName.substringAfter(" (").substringBefore(")"),
                                    color = C.OnSurfaceVariant,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Custom RAM input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isCustom = state.selectedTier == DeviceRamTier.CUSTOM
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCustom) C.Primary.copy(alpha = 0.15f) else C.SurfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isCustom) C.Primary else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onIntent(PerAppMemoryLimitsIntent.SelectTier(DeviceRamTier.CUSTOM)) }
                            .padding(8.dp)
                    ) {
                        Text("Custom", color = if (isCustom) C.Primary else C.OnSurface, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    if (isCustom) {
                        Text("RAM (GB):", color = C.OnSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "${state.customRamGB.toInt()}GB",
                            color = C.Primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Section: Calculate Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onIntent(PerAppMemoryLimitsIntent.Calculate) },
                colors = ButtonDefaults.buttonColors(containerColor = C.Primary),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = C.Background)
                Spacer(Modifier.width(8.dp))
                Text("Calculate", color = C.Background, fontFamily = FontFamily.Monospace)
            }
            OutlinedButton(
                onClick = { onIntent(PerAppMemoryLimitsIntent.ToggleCalculatorDetails) },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (state.showDetails) "Hide Details" else "Show Details", color = C.Primary, fontFamily = FontFamily.Monospace)
            }
        }

        // Section: Result
        if (!state.isCalculating && state.calculatedLimitMB > 0) {
            CLISection(title = "✅ Calculation Result") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResultRow("Device RAM", "${if (state.selectedTier == DeviceRamTier.CUSTOM) "${state.customRamGB.toInt()}GB" else "${state.selectedTier.ramGB}GB"}", C.Info)
                    HorizontalDivider(color = C.SurfaceVariant)
                    ResultRow("App Memory Limit", "${state.calculatedLimitMB}MB", C.Primary, bold = true)
                    if (state.showDetails) {
                        Spacer(Modifier.height(4.dp))
                        ResultRow("  Max Heap", "${state.calculatedHeapMB}MB", C.OnSurfaceVariant)
                        ResultRow("  Native Limit", "${state.calculatedNativeMB}MB", C.OnSurfaceVariant)
                        ResultRow("  System/Other", "${state.calculatedLimitMB - state.calculatedHeapMB - state.calculatedNativeMB}MB", C.OnSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    RecommendationBanner(
                        text = if (state.calculatedLimitMB < 300)
                            "⚠️ LOW_END device — Implement aggressive memory management"
                        else if (state.calculatedLimitMB < 600)
                            "💡 MID_RANGE device — Optimize bitmap loading and caches"
                        else
                            "✅ HIGH_END device — Within comfortable limits"
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ResultRow(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = C.OnSurfaceVariant, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = color, fontSize = 12.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun RecommendationBanner(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(C.Primary.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Text(text, color = C.Primary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

// ─────────────────────────────────────────────────────────────────
// Leak Scanner Tab
// ─────────────────────────────────────────────────────────────────
@Composable
private fun LeakScannerTab(
    state: LeakScannerState,
    onIntent: (PerAppMemoryLimitsIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Project Path Input
        CLISection(title = "🔍 Memory Leak Scanner") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Project Path:", color = C.OnSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.projectPath,
                        onValueChange = { onIntent(PerAppMemoryLimitsIntent.UpdateProjectPath(it)) },
                        placeholder = { Text("./MyApp", color = C.OnSurfaceVariant, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = C.Primary,
                            unfocusedBorderColor = C.SurfaceVariant,
                            focusedTextColor = C.OnSurface,
                            unfocusedTextColor = C.OnSurface
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        singleLine = true
                    )
                    Button(
                        onClick = { onIntent(PerAppMemoryLimitsIntent.StartScan) },
                        enabled = !state.isScanning,
                        colors = ButtonDefaults.buttonColors(containerColor = C.Primary)
                    ) {
                        if (state.isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = C.Background, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null, tint = C.Background)
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(if (state.isScanning) "Scanning..." else "Scan", color = C.Background, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    if (state.isScanning) {
                        OutlinedButton(
                            onClick = { onIntent(PerAppMemoryLimitsIntent.CancelScan) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = C.Fail)
                        ) {
                            Text("Cancel", color = C.Fail, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Progress
                if (state.isScanning || state.scanPhase == ScanPhase.COMPLETED) {
                    Column {
                        LinearProgressIndicator(
                            progress = { state.progress / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = C.Primary,
                            trackColor = C.SurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = when (state.scanPhase) {
                                ScanPhase.SCANNING -> "Scanning... ${state.scannedFiles} files"
                                ScanPhase.ANALYZING -> "Analyzing patterns..."
                                ScanPhase.GENERATING_REPORT -> "Generating report..."
                                ScanPhase.COMPLETED -> "Scan complete — ${state.findings.size} issues found"
                                else -> ""
                            },
                            color = C.OnSurfaceVariant,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Summary Cards
        if (state.summary.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.summary.forEach { (severity, count) ->
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        emoji = severity.emoji,
                        count = count,
                        label = severity.displayName,
                        color = severity.color
                    )
                }
            }
        }

        // Findings List
        if (state.findings.isNotEmpty()) {
            CLISection(title = "📋 Findings (${state.findings.size})") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.findings.forEach { finding ->
                        FindingCard(finding = finding)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { onIntent(PerAppMemoryLimitsIntent.ClearFindings) }) {
                            Text("Clear Results", color = C.OnSurfaceVariant, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SummaryCard(modifier: Modifier = Modifier, emoji: String, count: Int, label: String, color: Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 18.sp)
            Text(
                text = count.toString(),
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(label, color = C.OnSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun FindingCard(finding: LeakFinding) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(C.SurfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(finding.pattern.severity.emoji, fontSize = 14.sp)
                Text(finding.pattern.displayName, color = finding.pattern.severity.color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Text("${finding.file}:${finding.line}", color = C.OnSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
        Text(
            text = finding.code,
            color = C.OnSurface,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .fillMaxWidth()
                .background(C.Background, RoundedCornerShape(4.dp))
                .padding(6.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Top) {
            Text("💡", fontSize = 10.sp)
            Text(finding.suggestion, color = C.Info, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Monitor SDK Tab
// ─────────────────────────────────────────────────────────────────
@Composable
private fun MonitorSDKTab(
    state: MonitorSDKState,
    onIntent: (PerAppMemoryLimitsIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Memory Gauge
        CLISection(title = "📊 Runtime Memory Monitor") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Circular gauge
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    MemoryGauge(
                        usedPercent = state.memoryInfo.usedPercent,
                        monitoringState = state.monitoringState
                    )
                }

                // Memory info
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ResultRow("Total Memory", "${state.memoryInfo.totalMemoryMB}MB", C.Info)
                    ResultRow("Used", "${state.memoryInfo.usedMemoryMB}MB", if (state.memoryInfo.usedPercent > 0.9f) C.Fail else if (state.memoryInfo.usedPercent > 0.8f) C.Warn else C.Pass)
                    ResultRow("Available", "${state.memoryInfo.availableMemoryMB}MB", C.OnSurfaceVariant)
                    ResultRow("Usage", "${(state.memoryInfo.usedPercent * 100).toInt()}%", if (state.memoryInfo.usedPercent > 0.9f) C.Fail else if (state.memoryInfo.usedPercent > 0.8f) C.Warn else C.Pass)
                }

                // Control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onIntent(PerAppMemoryLimitsIntent.StartMonitoring) },
                        enabled = state.monitoringState == MonitoringState.IDLE,
                        colors = ButtonDefaults.buttonColors(containerColor = C.Primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = C.Background)
                        Spacer(Modifier.width(4.dp))
                        Text("Start", color = C.Background, fontFamily = FontFamily.Monospace)
                    }
                    OutlinedButton(
                        onClick = { onIntent(PerAppMemoryLimitsIntent.StopMonitoring) },
                        enabled = state.monitoringState != MonitoringState.IDLE,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = C.Fail)
                        Spacer(Modifier.width(4.dp))
                        Text("Stop", color = C.Fail, fontFamily = FontFamily.Monospace)
                    }
                }

                // History sparkline
                if (state.memoryHistory.isNotEmpty()) {
                    SparklineChart(
                        data = state.memoryHistory,
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    )
                }
            }
        }

        // Threshold Configuration
        CLISection(title = "⚙️ Threshold Configuration") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Warning Threshold: ${(state.warningThreshold * 100).toInt()}%", color = C.OnSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Slider(
                    value = state.warningThreshold,
                    onValueChange = { onIntent(PerAppMemoryLimitsIntent.UpdateWarningThreshold(it)) },
                    valueRange = 0.5f..0.95f,
                    colors = SliderDefaults.colors(thumbColor = C.Warn, activeTrackColor = C.Warn)
                )
                Text("Critical Threshold: ${(state.criticalThreshold * 100).toInt()}%", color = C.OnSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Slider(
                    value = state.criticalThreshold,
                    onValueChange = { onIntent(PerAppMemoryLimitsIntent.UpdateCriticalThreshold(it)) },
                    valueRange = 0.7f..0.99f,
                    colors = SliderDefaults.colors(thumbColor = C.Fail, activeTrackColor = C.Fail)
                )
            }
        }

        // Integration Snippet
        CLISection(title = "📋 SDK Integration Snippet") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.integrationSnippet.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(C.Background, RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = state.integrationSnippet,
                            color = C.OnSurface,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                OutlinedButton(
                    onClick = { onIntent(PerAppMemoryLimitsIntent.GenerateIntegrationSnippet) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = C.Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate Integration Snippet", color = C.Primary, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MemoryGauge(usedPercent: Float, monitoringState: MonitoringState) {
    val animatedPercent by animateFloatAsState(
        targetValue = usedPercent,
        animationSpec = tween(500),
        label = "memory_gauge"
    )
    val gaugeColor = when (monitoringState) {
        MonitoringState.CRITICAL, MonitoringState.OOM -> C.Fail
        MonitoringState.WARNING -> C.Warn
        MonitoringState.MONITORING -> C.Primary
        else -> C.OnSurfaceVariant
    }

    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Track
            drawArc(
                color = C.SurfaceVariant,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Progress
            drawArc(
                color = gaugeColor,
                startAngle = 135f,
                sweepAngle = 270f * animatedPercent,
                useCenter = false,
                topLeft = topLeft,
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedPercent * 100).toInt()}%",
                color = gaugeColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = monitoringState.displayName,
                color = C.OnSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun SparklineChart(data: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height
        val stepX = w / (data.size - 1).coerceAtLeast(1)
        val maxVal = data.maxOrNull() ?: 1f
        val minVal = data.minOrNull() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(0.001f)

        data.forEachIndexed { i, v ->
            val x = i * stepX
            val y = h - ((v - minVal) / range * h)
            if (i > 0) {
                val prevX = (i - 1) * stepX
                val prevV = data[i - 1]
                val prevY = h - ((prevV - minVal) / range * h)
                drawLine(
                    color = C.Primary,
                    start = Offset(prevX, prevY),
                    end = Offset(x, y),
                    strokeWidth = 2.dp.toPx()
                )
            }
            drawCircle(color = C.Primary, radius = 2.dp.toPx(), center = Offset(x, y))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Pressure Test Tab
// ─────────────────────────────────────────────────────────────────
@Composable
private fun PressureTestTab(
    state: PressureTestState,
    onIntent: (PerAppMemoryLimitsIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CLISection(title = "💾 CI Memory Pressure Test") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select device tier to simulate:", color = C.OnSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DeviceRamTier.entries.filter { it != DeviceRamTier.CUSTOM }.forEach { tier ->
                        val isSelected = state.selectedTier == tier
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) C.Primary.copy(alpha = 0.15f) else C.SurfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) C.Primary else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onIntent(PerAppMemoryLimitsIntent.SelectTestTier(tier)) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${tier.ramGB}GB", color = if (isSelected) C.Primary else C.OnSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("${tier.appMemoryLimitMB}MB", color = C.OnSurfaceVariant, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onIntent(PerAppMemoryLimitsIntent.RunPressureTest) },
                        enabled = !state.isRunning,
                        colors = ButtonDefaults.buttonColors(containerColor = C.Primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = C.Background)
                        Spacer(Modifier.width(4.dp))
                        Text(if (state.isRunning) "Running..." else "Run Test", color = C.Background, fontFamily = FontFamily.Monospace)
                    }
                    OutlinedButton(
                        onClick = { onIntent(PerAppMemoryLimitsIntent.ClearTestResults) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear", color = C.OnSurfaceVariant, fontFamily = FontFamily.Monospace)
                    }
                }

                if (state.isRunning) {
                    Column {
                        LinearProgressIndicator(
                            progress = { state.progress / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = C.Primary,
                            trackColor = C.SurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = state.currentPhase,
                            color = C.OnSurfaceVariant,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Test Results
        if (state.results.isNotEmpty()) {
            state.results.forEach { result ->
                TestResultCard(result = result)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TestResultCard(result: PressureTestResult) {
    CLISection(title = "📊 Test Result") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ResultRow("Device RAM", result.deviceRAM, C.Info)
                ResultRow("App Limit", result.appMemoryLimit, C.Primary)
                ResultRow("Peak Memory", "${result.peakMemoryMB}MB", if (result.oomOccurred) C.Fail else C.Warn)
            }
            HorizontalDivider(color = C.SurfaceVariant)

            val statusColor = when (result.testStatus) {
                TestStatus.PASS -> C.Pass
                TestStatus.DEGRADED -> C.Warn
                TestStatus.OOM -> C.Fail
                TestStatus.ERROR -> C.Fail
                else -> C.OnSurfaceVariant
            }
            val statusEmoji = when (result.testStatus) {
                TestStatus.PASS -> "✅"
                TestStatus.DEGRADED -> "⚠️"
                TestStatus.OOM -> "❌"
                TestStatus.ERROR -> "❌"
                else -> "⏸️"
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(statusEmoji, fontSize = 16.sp)
                Text("Status: ${result.testStatus.name}", color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(C.Primary.copy(alpha = 0.1f))
                    .padding(10.dp)
            ) {
                Text("💡 ${result.recommendation}", color = C.Primary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Gradle Plugin Tab
// ─────────────────────────────────────────────────────────────────
@Composable
private fun GradlePluginTab(
    state: GradlePluginState,
    onIntent: (PerAppMemoryLimitsIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CLISection(title = "🔧 Gradle Plugin CI Compliance Check") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onIntent(PerAppMemoryLimitsIntent.CheckCompliance) },
                    enabled = !state.isChecking,
                    colors = ButtonDefaults.buttonColors(containerColor = C.Primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = C.Background, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = C.Background)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isChecking) "Checking..." else "Run Compliance Check", color = C.Background, fontFamily = FontFamily.Monospace)
                }

                // Check result
                if (state.checkMessages.isNotEmpty()) {
                    val resultColor = when (state.checkResult) {
                        CIComplianceResult.PASS -> C.Pass
                        CIComplianceResult.WARN -> C.Warn
                        CIComplianceResult.FAIL -> C.Fail
                        else -> C.OnSurfaceVariant
                    }
                    val resultEmoji = when (state.checkResult) {
                        CIComplianceResult.PASS -> "✅"
                        CIComplianceResult.WARN -> "⚠️"
                        CIComplianceResult.FAIL -> "❌"
                        else -> "⏸️"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(resultColor.copy(alpha = 0.15f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(resultEmoji, fontSize = 18.sp)
                        Text(
                            text = "CI Compliance: ${state.checkResult.name}",
                            color = resultColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.checkMessages.forEach { msg ->
                            val isPass = msg.contains("[PASS]")
                            val isFail = msg.contains("[FAIL]")
                            val isWarn = msg.contains("[WARN]")
                            val msgColor = when {
                                isPass -> C.Pass
                                isFail -> C.Fail
                                isWarn -> C.Warn
                                else -> C.OnSurface
                            }
                            Text(
                                text = msg,
                                color = msgColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Configuration Snippet
        CLISection(title = "📋 Plugin Configuration Snippet") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.configurationSnippet.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(C.Background, RoundedCornerShape(6.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = state.configurationSnippet,
                            color = C.OnSurface,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                OutlinedButton(
                    onClick = { onIntent(PerAppMemoryLimitsIntent.GenerateConfigSnippet) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = C.Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate Configuration Snippet", color = C.Primary, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// CLISection — Reusable CLI-style section wrapper
// ─────────────────────────────────────────────────────────────────
@Composable
private fun CLISection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(C.Surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = C.Primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        HorizontalDivider(color = C.SurfaceVariant, thickness = 1.dp)
        content()
    }
}
