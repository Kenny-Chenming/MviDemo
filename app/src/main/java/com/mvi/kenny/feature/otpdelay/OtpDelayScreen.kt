package com.mvi.kenny.feature.otpdelay

// ================================================================
// OtpDelayScreen — Android 17 SMS OTP Delay 合规检测与迁移工具包 Screen
// ================================================================
// CLI-style Screen for OTP Delay compliance toolkit.
//
// PRD-186: Android 17 SMS OTP Delay 合规检测与迁移工具包
// Design Reference: memory/agency/designs/PRD-186-SMS-OTP-Delay-合规检测与迁移工具包.md
//
// This screen provides a terminal-like interface for:
//   - OTP Impact Scanner: Color-coded scan results
//   - Compliance Check: CI/CD integration status
//   - Hash Generator: SMS Retriever hash string generation
//   - Fallback Strategy: Migration guidance
//
// Visual Style:
//   - Dark terminal theme (monospace font, dark background)
//   - Color-coded output by severity (HIGH=red, MEDIUM=yellow, LOW=green)
//   - ANSI-style colored text
// ================================================================

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * OtpDelayScreen — Main Screen Composable
 * ================================================================
 * Terminal-style screen for OTP Delay compliance toolkit.
 *
 * @param viewModel OtpDelayViewModel instance
 * @param onNavigateToTemplate Callback for navigating to code templates
 */
@Composable
fun OtpDelayScreen(
    viewModel: OtpDelayViewModel = remember { OtpDelayViewModel() },
    onNavigateToTemplate: (String) -> Unit = {}
) {
    // Collect states from ViewModel
    // 从 ViewModel 收集状态
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val complianceState by viewModel.complianceState.collectAsStateWithLifecycle()
    val hashGeneratorState by viewModel.hashGeneratorState.collectAsStateWithLifecycle()

    // Local terminal output history
    // 本地终端输出历史
    var terminalOutput by remember { mutableStateOf<List<TerminalLine>>(emptyList()) }

    // Collect effects in LaunchedEffect
    // 在 LaunchedEffect 中收集副作用
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.scanEffects.collectLatest { effect ->
                when (effect) {
                    is OtpScanEffect.TerminalOutput -> {
                        terminalOutput = terminalOutput + TerminalLine(
                            text = effect.message,
                            severity = effect.severity,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                    is OtpScanEffect.ScanCompleted -> {
                        // Already handled via state update
                    }
                    is OtpScanEffect.ReportGenerated -> {
                        terminalOutput = terminalOutput + TerminalLine(
                            text = "📄 报告已生成: ${effect.filePath}",
                            severity = OtpSeverity.INFO,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                    is OtpScanEffect.Error -> {
                        terminalOutput = terminalOutput + TerminalLine(
                            text = "❌ 错误: ${effect.message}",
                            severity = OtpSeverity.HIGH,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                    is OtpScanEffect.Navigate -> {
                        // Handle navigation if needed
                    }
                }
            }
        }
    }

    // Main layout
    // 主布局
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))  // Dark terminal background
            .padding(16.dp)
    ) {
        // Header
        // 标题栏
        OtpDelayHeader()

        Spacer(modifier = Modifier.height(16.dp))

        // Tab navigation for different tools
        // 不同工具的 Tab 导航
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf(
            "🔍 Scanner" to "扫描器",
            "✅ Compliance" to "合规检测",
            "🔑 Hash Generator" to "Hash生成器",
            "📋 Fallback Strategy" to "降级策略"
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF2D2D2D),
            contentColor = Color.White
        ) {
            tabs.forEachIndexed { index, (en, zh) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(en, fontSize = 12.sp)
                            Text(zh, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab content
        // Tab 内容
        when (selectedTab) {
            0 -> ScannerTab(viewModel, scanState, terminalOutput)
            1 -> ComplianceTab(viewModel, complianceState)
            2 -> HashGeneratorTab(viewModel, hashGeneratorState)
            3 -> FallbackStrategyTab(onNavigateToTemplate)
        }
    }
}

/**
 * ============================================================
 * OtpDelayHeader — 标题栏组件
 * ================================================================
 */
@Composable
private fun OtpDelayHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "🔐 OTP Delay 合规检测工具包",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Android 17 SMS OTP 3-Hour Delay Migration Toolkit",
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { },
                label = { Text("Android 17", fontSize = 10.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFF3FB950).copy(alpha = 0.2f)
                )
            )
            AssistChip(
                onClick = { },
                label = { Text("API 37+", fontSize = 10.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFF58A6FF).copy(alpha = 0.2f)
                )
            )
        }
    }
}

/**
 * ============================================================
 * ScannerTab — OTP 影响扫描器 Tab
 * ================================================================
 * Provides OTP impact scanning functionality with terminal output.
 * 提供 OTP 影响扫描功能，带终端输出。
 */
@Composable
private fun ScannerTab(
    viewModel: OtpDelayViewModel,
    scanState: OtpScanState,
    terminalOutput: List<TerminalLine>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Scan controls
        // 扫描控制
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var sourceDir by remember { mutableStateOf("app/src/main") }
            var packageName by remember { mutableStateOf("com.example.myapp") }

            OutlinedTextField(
                value = sourceDir,
                onValueChange = { sourceDir = it },
                label = { Text("源码目录") },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF58A6FF),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF58A6FF),
                    unfocusedLabelColor = Color.Gray
                )
            )

            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text("包名") },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF58A6FF),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF58A6FF),
                    unfocusedLabelColor = Color.Gray
                )
            )

            Button(
                onClick = {
                    viewModel.processIntent(
                        OtpScanIntent.StartScan(sourceDir, packageName)
                    )
                },
                enabled = !scanState.isScanning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3FB950)
                )
            ) {
                if (scanState.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("▶ Run Scan")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        // 进度条
        if (scanState.isScanning) {
            LinearProgressIndicator(
                progress = { scanState.progress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF58A6FF),
                trackColor = Color(0xFF3D3D3D)
            )
            Text(
                text = "Scanning... ${scanState.progress}%",
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Terminal output area
        // 终端输出区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0D0D0D), RoundedCornerShape(8.dp))
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column {
                terminalOutput.forEach { line ->
                    TerminalLineDisplay(line)
                }

                // Scan summary when completed
                // 扫描完成时显示摘要
                if (scanState.phase == ScanPhase.COMPLETED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    SummaryCard(scanState)
                }
            }
        }
    }
}

/**
 * ============================================================
 * TerminalLine — 终端输出行数据类
 * ================================================================
 */
private data class TerminalLine(
    val text: String,
    val severity: OtpSeverity,
    val timestamp: Long
)

/**
 * ============================================================
 * TerminalLineDisplay — 终端行显示组件
 * ================================================================
 * Renders a single terminal output line with ANSI-style coloring.
 * 以 ANSI 风格着色渲染单个终端输出行。
 */
@Composable
private fun TerminalLineDisplay(line: TerminalLine) {
    val textColor = when (line.severity) {
        OtpSeverity.HIGH -> Color(0xFFF85149)
        OtpSeverity.MEDIUM -> Color(0xFFFFD700)
        OtpSeverity.LOW -> Color(0xFF3FB950)
        OtpSeverity.INFO -> Color(0xFF58A6FF)
    }

    Text(
        text = line.text,
        color = textColor,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(vertical = 1.dp)
    )
}

/**
 * ============================================================
 * SummaryCard — 扫描结果摘要卡片
 * ================================================================
 */
@Composable
private fun SummaryCard(scanState: OtpScanState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 Scan Results Summary",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    emoji = "🔴",
                    label = "HIGH",
                    count = scanState.highSeverityCount,
                    color = Color(0xFFF85149)
                )
                SummaryItem(
                    emoji = "🟡",
                    label = "MEDIUM",
                    count = scanState.mediumSeverityCount,
                    color = Color(0xFFFFD700)
                )
                SummaryItem(
                    emoji = "🟢",
                    label = "LOW",
                    count = scanState.lowSeverityCount,
                    color = Color(0xFF3FB950)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "⏱️  Duration: ${scanState.durationMs}ms | 📁 Files: ${scanState.scannedFilesCount}",
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * ============================================================
 * SummaryItem — 摘要项组件
 * ================================================================
 */
@Composable
private fun SummaryItem(
    emoji: String,
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 20.sp)
        Text(
            text = count.toString(),
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * ============================================================
 * ComplianceTab — CI 合规检测 Tab
 * ================================================================
 */
@Composable
private fun ComplianceTab(
    viewModel: OtpDelayViewModel,
    complianceState: OtpComplianceState
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Compliance status card
        // 合规状态卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (complianceState.complianceLevel) {
                    ComplianceLevel.COMPLIANT -> Color(0xFF3FB950).copy(alpha = 0.2f)
                    ComplianceLevel.NON_COMPLIANT -> Color(0xFFF85149).copy(alpha = 0.2f)
                    ComplianceLevel.WARNING -> Color(0xFFFFD700).copy(alpha = 0.2f)
                    ComplianceLevel.NOT_CHECKED -> Color(0xFF8B949E).copy(alpha = 0.2f)
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${complianceState.complianceLevel.emoji} ${complianceState.complianceLevel.displayName}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (complianceState.complianceLevel) {
                            ComplianceLevel.COMPLIANT -> "App 已使用 SMS Retriever/User Consent API"
                            ComplianceLevel.NON_COMPLIANT -> "App 未适配 Android 17 OTP 延迟"
                            ComplianceLevel.WARNING -> "部分场景需要处理"
                            ComplianceLevel.NOT_CHECKED -> "点击按钮进行检测"
                        },
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { /* Run compliance check */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF58A6FF)
                    )
                ) {
                    Text("▶ Run Check")
                }
            }
        }

        // CI/CD integration info
        // CI/CD 集成信息
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚙️ CI/CD Integration",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                CodeBlock("""
// settings.gradle.kts
pluginManagement {
    plugins {
        id("com.mvi.kenny.otpdelay") version "1.0.0"
    }
}

// app/build.gradle.kts
plugins {
    id("com.mvi.kenny.otpdelay")
}

otpdelay {
    strict.set(true)  // true = fail build, false = warning
}
                """.trimIndent())

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Task: checkOtpCompliance",
                        color = Color(0xFF3FB950),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Report section
        // 报告区域
        if (complianceState.reportPath != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("📄 Compliance Report", color = Color.White, fontSize = 14.sp)
                        Text(
                            complianceState.reportPath,
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = { /* Open report */ }) {
                        Icon(Icons.Default.OpenInNew, "Open", tint = Color(0xFF58A6FF))
                    }
                }
            }
        }
    }
}

/**
 * ============================================================
 * HashGeneratorTab — Hash 生成器 Tab
 * ================================================================
 */
@Composable
private fun HashGeneratorTab(
    viewModel: OtpDelayViewModel,
    hashState: HashGeneratorState
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Input fields
        // 输入字段
        OutlinedTextField(
            value = hashState.packageName,
            onValueChange = {
                viewModel.processHashIntent(HashGeneratorIntent.UpdatePackageName(it))
            },
            label = { Text("Package Name") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF58A6FF),
                unfocusedBorderColor = Color.Gray
            )
        )

        OutlinedTextField(
            value = hashState.keystorePath,
            onValueChange = {
                viewModel.processHashIntent(HashGeneratorIntent.UpdateKeystorePath(it))
            },
            label = { Text("Keystore Path (optional)") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF58A6FF),
                unfocusedBorderColor = Color.Gray
            )
        )

        OutlinedTextField(
            value = hashState.keyAlias,
            onValueChange = {
                viewModel.processHashIntent(HashGeneratorIntent.UpdateKeyAlias(it))
            },
            label = { Text("Key Alias (optional)") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF58A6FF),
                unfocusedBorderColor = Color.Gray
            )
        )

        // Generate button
        // 生成按钮
        Button(
            onClick = {
                viewModel.processHashIntent(HashGeneratorIntent.GenerateHash)
            },
            enabled = !hashState.isGenerating && hashState.packageName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3FB950))
        ) {
            if (hashState.isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (hashState.isGenerating) "Generating..." else "🔑 Generate Hash")
        }

        // Hash output
        // Hash 输出
        if (hashState.generatedHash != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✅ Generated Hash:", color = Color(0xFF3FB950), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = hashState.generatedHash,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 Add this hash to your SMS Retriever backend",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Error display
        // 错误显示
        if (hashState.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF85149).copy(alpha = 0.2f))
            ) {
                Text(
                    text = "❌ ${hashState.error}",
                    color = Color(0xFFF85149),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Instructions
        // 说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📝 How to get the hash:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = """
                    1. Use Play Console → App Signing → SMS Retriever hash
                    2. Or run Google's official Python script:
                       tools-scripts-java/sms_retriever_hash_v2.py
                    3. Or use the CLI tool in tools/hash-generator/
                    """.trimIndent(),
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * ============================================================
 * FallbackStrategyTab — 降级策略 Tab
 * ================================================================
 */
@Composable
private fun FallbackStrategyTab(onNavigateToTemplate: (String) -> Unit) {
    val strategies = FallbackStrategy.entries

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "当 App 无法读取 OTP 时的降级策略",
            color = Color.Gray,
            fontSize = 12.sp
        )

        strategies.forEach { strategy ->
            StrategyCard(
                strategy = strategy,
                onViewTemplate = { onNavigateToTemplate(strategy.name) }
            )
        }
    }
}

/**
 * ============================================================
 * StrategyCard — 策略卡片组件
 * ================================================================
 */
@Composable
private fun StrategyCard(
    strategy: FallbackStrategy,
    onViewTemplate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (strategy == FallbackStrategy.SMS_RETRIEVER)
                Color(0xFF3FB950).copy(alpha = 0.15f)
            else
                Color(0xFF2D2D2D)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strategy.displayName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (strategy == FallbackStrategy.SMS_RETRIEVER) {
                            Spacer(modifier = Modifier.width(8.dp))
                            AssistChip(
                                onClick = { },
                                label = { Text("Recommended", fontSize = 9.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFF3FB950).copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                    Text(
                        text = strategy.displayNameEn,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                TextButton(onClick = onViewTemplate) {
                    Text("View Template", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strategy.description,
                color = Color.Gray,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFF58A6FF)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "UX: ${strategy.userExperience}",
                    color = Color(0xFF58A6FF),
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * ============================================================
 * CodeBlock — 代码块组件
 * ================================================================
 */
@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D0D), RoundedCornerShape(4.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            color = Color(0xFF3FB950),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
