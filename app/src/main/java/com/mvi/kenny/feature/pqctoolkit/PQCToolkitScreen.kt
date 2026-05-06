package com.mvi.kenny.feature.pqctoolkit

// ================================================================
// PQCToolkitScreen — Android 17 PQC Migration Toolkit Screen
// ================================================================
// Terminal-style UI screen for Android 17 Post-Quantum Cryptography Toolkit.
//
// PRD-236: Android 17 后量子密码学（PQC）迁移工具包
// Design Reference: memory/agency/designs/PRD-236-Android-17-PQC-后量子密码学迁移工具包.md
//
// Six tool tabs:
//   1. Crypto Scanner — Detect RSA/ECDSA usage
//   2. ML-DSA Generator — Generate quantum-safe keys
//   3. Migration Guide — RSA/ECDSA → ML-DSA steps
//   4. Benchmark — Performance comparison
//   5. Play Signing Guide — Google Play PQC guide
//   6. Gradle Plugin — CI compliance check
//
// Visual Style: Dark terminal CLI theme (MAGENTA/PURPLE security theme)
//   - MAGENTA titles, PURPLE accents, GREEN pass, AMBER warn, RED fail
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

// ─────────────────────────────────────────────────────────────────
// Color aliases
// ─────────────────────────────────────────────────────────────────
private val C = PQCColors

// ─────────────────────────────────────────────────────────────────
// PQCToolkitScreen — Main Composable
// ─────────────────────────────────────────────────────────────────
@Composable
fun PQCToolkitScreen(
    viewModel: PQCToolkitViewModel = remember { PQCToolkitViewModel() },
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        onUpdateTopBar(TopBarConfig(title = "PQC Toolkit"))
    }
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    is PQCToolkitEffect.ShowToast -> { }
                    is PQCToolkitEffect.ShowError -> { }
                    is PQCToolkitEffect.ReportGenerated -> { }
                    is PQCToolkitEffect.ScanCompleted -> { }
                    is PQCToolkitEffect.KeyGenerated -> { }
                    is PQCToolkitEffect.BenchmarkCompleted -> { }
                    is PQCToolkitEffect.ReadinessUpdate -> { }
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
        HeaderSection()
        Spacer(modifier = Modifier.height(12.dp))
        TabNavigation(
            selectedTab = state.selectedTab,
            onTabSelected = { viewModel.processIntent(PQCToolkitIntent.SelectTab(it)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (state.selectedTab) {
                PQCToolTab.CRYPTO_SCANNER -> CryptoScannerTab(
                    state = state.cryptoScannerState,
                    onIntent = viewModel::processIntent
                )
                PQCToolTab.ML_DSA_GENERATOR -> MLDSAGeneratorTab(
                    state = state.mlDSAGeneratorState,
                    onIntent = viewModel::processIntent
                )
                PQCToolTab.MIGRATION_GUIDE -> MigrationGuideTab(
                    state = state.migrationGuideState,
                    onIntent = viewModel::processIntent
                )
                PQCToolTab.BENCHMARK -> BenchmarkTab(
                    state = state.benchmarkState,
                    onIntent = viewModel::processIntent
                )
                PQCToolTab.PLAY_SIGNING -> PlaySigningGuideTab(
                    state = state.playSigningGuideState,
                    onIntent = viewModel::processIntent
                )
                PQCToolTab.GRADLE_PLUGIN -> GradlePluginTab(
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
                imageVector = Icons.Default.VpnKey,
                contentDescription = null,
                tint = C.Primary,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = "Android 17 PQC Migration Toolkit",
                    color = C.Primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Post-Quantum Cryptography — ML-DSA / Hybrid Migration",
                    color = C.OnSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusChip("Android 17+", C.Accent)
            StatusChip("ML-DSA ✓", C.Pass)
            StatusChip("2029 Deadline ⏰", C.Warn)
            StatusChip("NIST Standard", C.Info)
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
        Text(text, color = color, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

// ─────────────────────────────────────────────────────────────────
// Tab Navigation
// ─────────────────────────────────────────────────────────────────
@Composable
private fun TabNavigation(
    selectedTab: PQCToolTab,
    onTabSelected: (PQCToolTab) -> Unit
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
        PQCToolTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) C.Primary.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(tab.emoji, fontSize = 14.sp)
                    Text(
                        text = tab.displayName,
                        color = if (isSelected) C.Primary else C.OnSurfaceVariant,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Crypto Scanner Tab
// ─────────────────────────────────────────────────────────────────
@Composable
private fun CryptoScannerTab(
    state: CryptoScannerState,
    onIntent: (PQCToolkitIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CLISection(title = "🔐 Crypto Scanner — RSA/ECDSA Detection") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Scan source code for RSA/ECDSA usage and get PQC migration urgency.",
                    color = C.OnSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.projectPath,
                        onValueChange = { onIntent(PQCToolkitIntent.UpdateProjectPath(it)) },
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
                        onClick = { onIntent(PQCToolkitIntent.StartScan) },
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
                            onClick = { onIntent(PQCToolkitIntent.CancelScan) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = C.Fail)
                        ) {
                            Text("Cancel", color = C.Fail, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
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
                                ScanPhase.SCANNING_KOTLIN -> "Scanning Kotlin... ${state.scannedFiles} files"
                                ScanPhase.SCANNING_JAVA -> "Scanning Java..."
                                ScanPhase.ANALYZING -> "Analyzing patterns..."
                                ScanPhase.GENERATING_REPORT -> "Generating report..."
                                ScanPhase.COMPLETED -> "Scan complete — ${state.criticalCount} critical, ${state.warningCount} warnings"
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

        // Readiness Level Banner
        if (state.readinessLevel != ReadinessLevel.UNKNOWN) {
            ReadinessBanner(level = state.readinessLevel)
        }

        // Summary Cards
        if (state.criticalCount > 0 || state.warningCount > 0 || state.infoCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.criticalCount > 0) SummaryChip("${state.criticalCount} Critical", C.Critical, Modifier.weight(1f))
                if (state.warningCount > 0) SummaryChip("${state.warningCount} Warnings", C.Warn, Modifier.weight(1f))
                if (state.infoCount > 0) SummaryChip("${state.infoCount} Info", C.Info, Modifier.weight(1f))
            }
        }

        // Findings
        if (state.findings.isNotEmpty()) {
            CLISection(title = "📋 Findings (${state.findings.size})") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.findings.forEach { finding ->
                        CryptoFindingCard(finding = finding)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onIntent(PQCToolkitIntent.ClearFindings) }) {
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
private fun ReadinessBanner(level: ReadinessLevel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(level.color.copy(alpha = 0.15f))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(level.emoji, fontSize = 24.sp)
            Column {
                Text(
                    text = "PQC Readiness: ${level.displayName}",
                    color = level.color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = when (level) {
                        ReadinessLevel.READY -> "App is quantum-safe! Maintain hybrid mode through 2029."
                        ReadinessLevel.WARNING -> "Partial migration done. Complete remaining items before 2029."
                        ReadinessLevel.NOT_READY -> "Critical PQC issues found. Start migration immediately!"
                        else -> "Run a scan to check PQC readiness."
                    },
                    color = level.color.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun SummaryChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun CryptoFindingCard(finding: CryptoFinding) {
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
                Text(finding.urgency.emoji, fontSize = 14.sp)
                Text(
                    finding.algorithm.displayName,
                    color = finding.urgency.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    "(${finding.usage.displayName})",
                    color = C.OnSurfaceVariant,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                "${finding.file}:${finding.line}",
                color = C.OnSurfaceVariant,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
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
// ML-DSA Generator Tab
// ─────────────────────────────────────────────────────────────────
@Composable
private fun MLDSAGeneratorTab(
    state: MLDSAGeneratorState,
    onIntent: (PQCToolkitIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CLISection(title = "🔑 ML-DSA Key Generator") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Generate quantum-safe ML-DSA keys for Android Keystore (Android 17+).",
                    color = C.OnSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Key Alias
                Text("Key Alias:", color = C.OnSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                OutlinedTextField(
                    value = state.keyAlias,
                    onValueChange = { onIntent(PQCToolkitIntent.UpdateKeyAlias(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = C.Primary,
                        unfocusedBorderColor = C.SurfaceVariant,
                        focusedTextColor = C.OnSurface,
                        unfocusedTextColor = C.OnSurface
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    singleLine = true
                )

                // Variant Selection
                Text("Variant:", color = C.OnSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MLDSAVariant.entries.forEach { variant ->
                        val isSelected = state.selectedVariant == variant
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
                                .clickable { onIntent(PQCToolkitIntent.SelectVariant(variant)) }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    variant.displayName,
                                    color = if (isSelected) C.Primary else C.OnSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    when (variant) {
                                        MLDSAVariant.ML_DSA_44 -> "Speed"
                                        MLDSAVariant.ML_DSA_65 -> "Balanced"
                                        MLDSAVariant.ML_DSA_87 -> "Security"
                                    },
                                    color = C.OnSurfaceVariant,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Generate Button
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onIntent(PQCToolkitIntent.GenerateKey) },
                        enabled = state.keyGenStatus != KeyGenStatus.GENERATING,
                        colors = ButtonDefaults.buttonColors(containerColor = C.Primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.keyGenStatus == KeyGenStatus.GENERATING) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = C.Background, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Key, contentDescription = null, tint = C.Background)
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (state.keyGenStatus == KeyGenStatus.GENERATING) "Generating..." else "Generate Key",
                            color = C.Background,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    OutlinedButton(
                        onClick = { onIntent(PQCToolkitIntent.GenerateCodeSnippet) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = C.Primary)
                        Spacer(Modifier.width(4.dp))
                        Text("Code Snippet", color = C.Primary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                if (state.keyGenStatus == KeyGenStatus.SUCCESS) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(C.Pass.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text("✅ Key generated successfully! Check Android Keystore.", color = C.Pass, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Code Snippet
        if (state.generatedCodeSnippet.isNotEmpty()) {
            CLISection(title = "📋 Usage Code Snippet") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(C.Background, RoundedCornerShape(6.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = state.generatedCodeSnippet,
                        color = C.OnSurface,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────
// Migration Guide Tab
// ─────────────────────────────────────────────────────────────────
@Composable
private fun MigrationGuideTab(
    state: MigrationGuideState,
    onIntent: (PQCToolkitIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CLISection(title = "📋 RSA/ECDSA → ML-DSA Migration Guide") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Step-by-step guide to migrate from classical cryptography to quantum-safe ML-DSA.",
                    color = C.OnSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Algorithm Selection
                Text("Algorithm to migrate:", color = C.OnSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(CryptoAlgorithm.RSA_2048, CryptoAlgorithm.ECDSA_P256, CryptoAlgorithm.RSA_3072).forEach { algo ->
                        val isSelected = state.selectedAlgorithm == algo
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
                                .clickable { onIntent(PQCToolkitIntent.SelectAlgorithm(algo)) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                algo.displayName,
                                color = if (isSelected) C.Primary else C.OnSurface,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Hybrid Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Hybrid Mode", color = C.OnSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(
                            "Sign with both RSA + ML-DSA for backward compatibility",
                            color = C.OnSurfaceVariant,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = state.hybridModeEnabled,
                        onCheckedChange = { onIntent(PQCToolkitIntent.ToggleHybridMode) },
                        colors = SwitchDefaults.colors(checkedThumbColor = C.Primary, checkedTrackColor = C.Primary.copy(alpha = 0.5f))
                    )
                }

                // Step Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onIntent(PQCToolkitIntent.PreviousStep) },
                        enabled = state.currentStep > 0
                    ) {
                        Text("← Prev", color = if (state.currentStep > 0) C.Primary else C.OnSurfaceVariant, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = "Step ${state.currentStep + 1} of ${state.totalSteps}",
                        color = C.OnSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    OutlinedButton(
                        onClick = { onIntent(PQCToolkitIntent.NextStep) },
                        enabled = state.currentStep < state.totalSteps - 1
                    ) {
                        Text("Next →", color = if (state.currentStep < state.totalSteps - 1) C.Primary else C.OnSurfaceVariant, fontFamily = FontFamily.Monospace)
                    }
                }

                // Migration Steps
                val steps = listOf(
                    "Assess" to "Inventory RSA/ECDSA usage in your codebase. Identify signature, key exchange, and certificate validation.",
                    "Plan" to "Schedule migration by 2027 (18 months before 2029 deadline). Prioritize production-critical crypto.",
                    "Implement" to if (state.hybridModeEnabled) "Add ML-DSA key generation + dual signature mode. Keep existing RSA/ECDSA for backward compat."
                                   else "Replace RSA/ECDSA with pure ML-DSA. Test thoroughly on Android 17+ devices.",
                    "Deploy" to "Roll out in phases: internal → beta → production. Monitor error rates during transition."
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.forEachIndexed { index, (title, desc) ->
                        val isActive = index == state.currentStep
                        val isPast = index < state.currentStep
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        isActive -> C.Primary.copy(alpha = 0.15f)
                                        isPast -> C.Pass.copy(alpha = 0.1f)
                                        else -> C.SurfaceVariant
                                    }
                                )
                                .padding(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                                Text(
                                    text = if (isPast) "✅" else "${index + 1}",
                                    fontSize = 14.sp,
                                    color = if (isPast) C.Pass else if (isActive) C.Primary else C.OnSurfaceVariant
                                )
                                Column {
                                    Text(
                                        title,
                                        color = if (isActive) C.Primary else C.OnSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        desc,
                                        color = C.OnSurfaceVariant,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Generate Diff Button
                OutlinedButton(
                    onClick = { onIntent(PQCToolkitIntent.GenerateCodeDiff) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Difference, contentDescription = null, tint = C.Primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate Code Diff", color = C.Primary, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Code Diff
        if (state.codeDiff.isNotEmpty()) {
            CLISection(title = "📝 Code Diff") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(C.Background, RoundedCornerShape(6.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = state.codeDiff,
                        color = C.OnSurface,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────
// Benchmark Tab
// ─────────────────────────────────────────────────────────────────
@Composable
private fun BenchmarkTab(
    state: BenchmarkState,
    onIntent: (PQCToolkitIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CLISection(title = "⚡ ML-DSA Performance Benchmark") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Compare ML-DSA vs RSA/ECDSA: signing speed, key size, verification time.",
                    color = C.OnSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Text("Device: ${state.deviceInfo}", color = C.OnSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onIntent(PQCToolkitIntent.RunBenchmark) },
                        enabled = !state.isRunning,
                        colors = ButtonDefaults.buttonColors(containerColor = C.Primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.isRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = C.Background, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = C.Background)
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(if (state.isRunning) "Running..." else "Run Benchmark", color = C.Background, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    OutlinedButton(
                        onClick = { onIntent(PQCToolkitIntent.ClearResults) },
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
                            text = state.currentOperation,
                            color = C.OnSurfaceVariant,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Results Table
        if (state.results.isNotEmpty()) {
            CLISection(title = "📊 Benchmark Results") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth().background(C.SurfaceVariant).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Operation", color = C.OnSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.5f))
                        Text("RSA-2048", color = C.OnSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        Text("ML-DSA-65", color = C.OnSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        Text("Delta", color = C.OnSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    }
                    state.results.forEach { result ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(result.operation, color = C.OnSurface, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.5f))
                            Text(result.rsa2048Value, color = C.Info, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                            Text(result.mlDsa65Value, color = C.Accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                            Text(result.delta, color = C.Warn, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider(color = C.SurfaceVariant, thickness = 0.5.dp)
                    }
                }
            }

            // Recommendation
            if (state.recommendation.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(C.Primary.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = state.recommendation,
                        color = C.Primary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────
// Play Signing Guide Tab
// ─────────────────────────────────────────────────────────────────
@Composable
private fun PlaySigningGuideTab(
    state: PlaySigningGuideState,
    onIntent: (PQCToolkitIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CLISection(title = "🎮 Google Play Signing PQC Guide") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Learn how Google Play Signing is transitioning to quantum-safe PQC keys.",
                    color = C.OnSurfaceVariant,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Topic Selection
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlaySigningTopic.entries.forEach { topic ->
                        val isSelected = state.selectedTopic == topic
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) C.Primary.copy(alpha = 0.15f) else C.SurfaceVariant)
                                .clickable { onIntent(PQCToolkitIntent.SelectTopic(topic)) }
                                .padding(10.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(topic.emoji, fontSize = 12.sp)
                                Text(topic.displayName, color = if (isSelected) C.Primary else C.OnSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // Topic Content
                val content = when (state.selectedTopic) {
                    PlaySigningTopic.OVERVIEW -> """
                        | 📖 Google Play Signing PQC Overview
                        |
                        | Google Play已经开始为新App和选择加入的现有App生成量子安全签名密钥（ML-DSA）。
                        |
                        | 🔑 Key Points:
                        | • Google Play使用ML-DSA为Play Signing密钥签名（Android 17+）
                        | • 现有App需要主动选择加入PQC签名
                        | • 2029年目标：完成所有App的PQC签名迁移
                        | • 签名链: App → RSA/ECDSA (dev) → ML-DSA (Play) → Users
                        |
                        | ℹ️ Impact:                        | Most apps don't need to change anything — Google handles PQC at the signing level.
                        | If your app uses Play Signing, you're already part of this transition.
                    """.trimMargin()
                    PlaySigningTopic.KEY_MIGRATION -> """
                        | 🔑 Play Signing Key Migration
                        |
                        | Steps to enable PQC signing in Play Console:
                        |
                        | 1. 📱 Go to Play Console → Release → Setup → App signing
                        | 2. ✅ Opt-in to PQC signing (if available for your app)
                        | 3. 📋 Review the new signing key certificate (shows ML-DSA)
                        | 4. 🧪 Test on Android 17+ devices
                        | 5. 🚀 Deploy — no app code changes needed
                        |
                        | ⚠️ Note: This only affects Play Signing keys, not your app's own crypto.
                    """.trimMargin()
                    PlaySigningTopic.TIMELINE -> """
                        | ⏰ 2029 Migration Timeline
                        |
                        | 📅 Key Milestones:
                        | • 2026: PQC signing available (opt-in)
                        | • 2027: New apps encouraged to use PQC
                        | • 2028: Legacy RSA signing deprecated
                        | • 2029: Full PQC mandatory for all apps
                        |
                        | 💡 Recommendation: Start PQC migration planning in 2026.
                        | Budget 18 months for thorough testing across all devices.
                    """.trimMargin()
                    PlaySigningTopic.FAQ -> """
                        | ❓ FAQ
                        |
                        | Q: Does this affect my app's crypto?
                        | A: No — this is about Play Signing keys, not your app's encryption.
                        |
                        | Q: Do I need to update my app for PQC?
                        | A: No for Play Signing. Yes if you use RSA/ECDSA for signatures.
                        |
                        | Q: Will my app work on old Android versions?
                        | A: Yes — PQC signing is transparent to apps.
                        |
                        | Q: What if I don't use Play Signing?
                        | A: Your app signing keys are your responsibility — start planning PQC migration.
                    """.trimMargin()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(C.Background, RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = content,
                        color = C.OnSurface,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────
// Gradle Plugin Tab
// ─────────────────────────────────────────────────────────────────
@Composable
private fun GradlePluginTab(
    state: GradlePluginState,
    onIntent: (PQCToolkitIntent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CLISection(title = "🔧 Gradle Plugin — CI PQC Compliance Check") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onIntent(PQCToolkitIntent.CheckCompliance) },
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

                // Check Result
                if (state.checkMessages.isNotEmpty()) {
                    val level = state.checkResult
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(level.color.copy(alpha = 0.15f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(level.emoji, fontSize = 18.sp)
                        Text(
                            text = "PQC Readiness: ${level.displayName}",
                            color = level.color,
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
                            val isInfo = msg.contains("[INFO]")
                            val msgColor = when {
                                isPass -> C.Pass
                                isFail -> C.Fail
                                isWarn -> C.Warn
                                isInfo -> C.Info
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

        // Config Snippet
        CLISection(title = "📋 Plugin Configuration") {
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
                    onClick = { onIntent(PQCToolkitIntent.GenerateConfigSnippet) },
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
