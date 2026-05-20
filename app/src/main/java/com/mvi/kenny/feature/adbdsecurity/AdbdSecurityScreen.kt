package com.mvi.kenny.feature.adbdsecurity

// ================================================================
// AdbdSecurityScreen — Android adbd CVE-2026-0073 Security Toolkit Screen
// ================================================================
// Compose UI screen for Android adbd CVE-2026-0073 Vulnerability Detection & Security Toolkit.
//
// PRD-264: Android adbd CVE-2026-0073 无线ADB漏洞检测与安全加固工具包
// Design Reference: memory/agency/designs/PRD-264-Android-adbd-CVE-2026-0073漏洞检测与安全加固工具包.md
//
// Four tool tabs:
//   1. Home (首页)      — One-click scan with dashboard results
//   2. CI Integration   — GitHub Actions / Jenkins YAML snippets
//   3. Security Guide   — Wireless ADB security best practices
//   4. About            — CVE details, references, and patch information
//
// Visual Style: Material Design 3 Security Dashboard
//   - Card-based information architecture
//   - Color-coded severity indicators (Critical/Patched/Vulnerable)
//   - Code blocks with dark theme (for CI snippets)
// ================================================================

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.*
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
private val C = AdbdSecurityColors

// ─────────────────────────────────────────────────────────────────
// Text Style Helpers
// ─────────────────────────────────────────────────────────────────
private val titleLargeStyle = TextStyle(
    fontSize = 22.sp,
    fontWeight = FontWeight.Medium,
    color = C.OnSurface
)
private val titleMediumStyle = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.Medium,
    color = C.OnSurface
)
private val bodyMediumStyle = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
    color = C.OnSurface
)
private val labelMediumStyle = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    color = C.OnSurfaceVariant
)
private val codeStyle = TextStyle(
    fontSize = 13.sp,
    fontFamily = FontFamily.Monospace,
    color = C.CodeBlockText
)

// ─────────────────────────────────────────────────────────────────
// AdbdSecurityScreen — Main Composable
// ─────────────────────────────────────────────────────────────────
@Composable
fun AdbdSecurityScreen(
    viewModel: AdbdSecurityViewModel = remember { AdbdSecurityViewModel() }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboardManager = LocalClipboardManager.current

    // Collect effects
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    is AdbdSecurityEffect.ScanCompleted -> { /* handled by state */ }
                    is AdbdSecurityEffect.ReportCopied -> { /* handled by toast */ }
                    is AdbdSecurityEffect.JsonExported -> { /* handled by state */ }
                    is AdbdSecurityEffect.ShowError -> { /* handled by UI */ }
                    is AdbdSecurityEffect.ShowToast -> { /* handled by UI */ }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C.Background)
    ) {
        // Tab content based on current tab
        when (state.currentTab) {
            BottomTab.HOME -> HomeTabContent(
                state = state,
                onStartScan = { viewModel.processIntent(AdbdSecurityIntent.StartScan) },
                onRetryScan = { viewModel.processIntent(AdbdSecurityIntent.RetryScan) },
                onExportJson = { viewModel.processIntent(AdbdSecurityIntent.ExportJson) },
                onCopyReport = { viewModel.processIntent(AdbdSecurityIntent.CopyReport) }
            )
            BottomTab.CI -> CITabContent(
                state = state,
                onFormatChange = { viewModel.processIntent(AdbdSecurityIntent.SetCIOutputFormat(it)) },
                onCopyYaml = { /* copy to clipboard */ }
            )
            BottomTab.GUIDE -> GuideTabContent(
                state = state,
                onToggleConfig = { viewModel.processIntent(AdbdSecurityIntent.SetExpandedSecurityConfig(it)) }
            )
            BottomTab.ABOUT -> AboutTabContent(state = state)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Tab Bar
        BottomTabBar(
            currentTab = state.currentTab,
            onTabSelect = { viewModel.processIntent(AdbdSecurityIntent.SelectTab(it)) }
        )
    }
}

// ================================================================
// Bottom Tab Bar — 底部导航栏
// ================================================================
@Composable
private fun BottomTabBar(
    currentTab: BottomTab,
    onTabSelect: (BottomTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = C.Surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomTab.entries.forEach { tab ->
                TabItem(
                    tab = tab,
                    isSelected = tab == currentTab,
                    onClick = { onTabSelect(tab) }
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: BottomTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) C.Primary else C.OnSurfaceVariant
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = tab.emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tab.displayName,
            style = labelMediumStyle.copy(color = color),
            fontSize = 11.sp
        )
    }
}

// ================================================================
// Home Tab — 扫描入口页
// ================================================================
@Composable
private fun HomeTabContent(
    state: AdbdSecurityState,
    onStartScan: () -> Unit,
    onRetryScan: () -> Unit,
    onExportJson: () -> Unit,
    onCopyReport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header with CVE badge
        HomeHeader()

        Spacer(modifier = Modifier.height(16.dp))

        // Scan Card — 主扫描卡片
        ScanCard(
            state = state,
            onStartScan = onStartScan,
            onRetryScan = onRetryScan
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Scan Result (if scan completed)
        if (state.scanStatus == ScanStatus.SUCCESS && state.latestScanResult != null) {
            ScanResultCard(
                result = state.latestScanResult,
                onExportJson = onExportJson,
                onCopyReport = onCopyReport
            )
        }

        // Quick Access Chips — 快捷入口
        Spacer(modifier = Modifier.height(12.dp))
        QuickAccessSection()
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Android adbd", style = titleLargeStyle)
            Text("CVE-2026-0073 Security Toolkit", style = titleMediumStyle.copy(color = C.OnSurfaceVariant))
        }
        CveSeverityBadge(level = SeverityLevel.CRITICAL)
    }
}

@Composable
private fun CveSeverityBadge(level: SeverityLevel) {
    val bgColor = when (level) {
        SeverityLevel.CRITICAL -> C.CriticalBg
        SeverityLevel.HIGH -> Color(0xFFFFF3E0)
        SeverityLevel.MEDIUM -> Color(0xFFFFFDE7)
        SeverityLevel.LOW, SeverityLevel.NONE -> C.PatchedBg
    }
    val borderColor = when (level) {
        SeverityLevel.CRITICAL -> C.CriticalBorder
        SeverityLevel.HIGH -> Color(0xFFE65100)
        SeverityLevel.MEDIUM -> Color(0xFFF9A825)
        SeverityLevel.LOW, SeverityLevel.NONE -> C.PatchedBorder
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "${level.emoji} ${level.displayName}",
            style = labelMediumStyle.copy(
                color = borderColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun ScanCard(
    state: AdbdSecurityState,
    onStartScan: () -> Unit,
    onRetryScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = C.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Vulnerability Scan", style = titleMediumStyle)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Device Info Row
            if (state.scanStatus != ScanStatus.IDLE || state.deviceInfo.adbVersion != "Unknown") {
                DeviceInfoRow(label = "ADB Version", value = state.deviceInfo.adbVersion)
                DeviceInfoRow(label = "Android", value = state.deviceInfo.androidVersion)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Scan Progress
            if (state.scanStatus == ScanStatus.SCANNING) {
                ScanProgressIndicator(progress = state.scanProgress, phase = state.scanPhase)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Action Button
            Button(
                onClick = if (state.scanStatus == ScanStatus.SCANNING) ({}) else onStartScan,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.scanStatus != ScanStatus.SCANNING,
                colors = ButtonDefaults.buttonColors(
                    containerColor = C.Primary,
                    disabledContainerColor = C.Primary.copy(alpha = 0.4f)
                )
            ) {
                if (state.scanStatus == ScanStatus.SCANNING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scanning...")
                } else {
                    Icon(Icons.Default.BugReport, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (state.scanStatus == ScanStatus.IDLE) "Start Scan" else "Rescan")
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = labelMediumStyle)
        Text(value, style = bodyMediumStyle.copy(fontWeight = FontWeight.Medium))
    }
}

@Composable
private fun ScanProgressIndicator(progress: Float, phase: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(phase, style = labelMediumStyle)
            Text("${(progress * 100).toInt()}%", style = labelMediumStyle)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = C.Primary,
            trackColor = C.Primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun ScanResultCard(
    result: ScanResult,
    onExportJson: () -> Unit,
    onCopyReport: () -> Unit
) {
    // Status Card
    val (bgColor, borderColor, statusText) = when (result.vulnerabilityStatus) {
        VulnerabilityStatus.PATCHED -> Triple(C.PatchedBg, C.PatchedBorder, "✅ Patched")
        VulnerabilityStatus.VULNERABLE -> Triple(C.CriticalBg, C.CriticalBorder, "⚠️ Vulnerable")
        VulnerabilityStatus.UNSUPPORTED -> Triple(Color(0xFFF5F5F5), Color(0xFFBDBDBD), "⚠️ Unsupported")
        VulnerabilityStatus.UNKNOWN -> Triple(Color(0xFFFFFDE7), Color(0xFFF9A825), "❓ Unknown")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(statusText, style = titleMediumStyle.copy(color = borderColor))
                        Text(
                            "Device: ${result.deviceInfo.deviceName}",
                            style = labelMediumStyle
                        )
                    }
                    CveSeverityBadge(level = result.severityLevel)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CVE Description
            Text("CVE-2026-0073", style = titleMediumStyle)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.cveDescription,
                style = bodyMediumStyle.copy(color = C.OnSurfaceVariant),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Details rows
            DetailRow(label = "Attack Vector", value = result.attackVector)
            DetailRow(label = "Affected", value = result.affectedVersions)
            DetailRow(label = "Exploit Status", value = result.exploitStatus)

            Spacer(modifier = Modifier.height(12.dp))

            // Fix suggestion
            FixSuggestionCard(result = result)

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopyReport,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Report", fontSize = 13.sp)
                }
                Button(
                    onClick = onExportJson,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = C.Primary)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export JSON", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = labelMediumStyle)
        Text(value, style = bodyMediumStyle, maxLines = 2)
    }
}

@Composable
private fun FixSuggestionCard(result: ScanResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, C.Primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = C.Primary.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = C.Primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recommended Actions", style = titleMediumStyle.copy(fontSize = 14.sp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Patch: ${result.patchMethod}",
                style = bodyMediumStyle.copy(fontSize = 13.sp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Workaround: ${result.workaround}",
                style = bodyMediumStyle.copy(fontSize = 13.sp, color = C.OnSurfaceVariant),
                maxLines = 3
            )
        }
    }
}

@Composable
private fun QuickAccessSection() {
    Column {
        Text("Quick Access", style = titleMediumStyle)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickAccessChip(text = "CI Integration", emoji = "⚙️")
            QuickAccessChip(text = "Security Guide", emoji = "🛡️")
            QuickAccessChip(text = "Patch Query", emoji = "🔎")
            QuickAccessChip(text = "About CVE", emoji = "📋")
        }
    }
}

@Composable
private fun QuickAccessChip(text: String, emoji: String) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
        color = C.Primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, style = labelMediumStyle.copy(color = C.Primary))
        }
    }
}

// ================================================================
// CI Tab — CI/CD集成页
// ================================================================
@Composable
private fun CITabContent(
    state: AdbdSecurityState,
    onFormatChange: (CIOutputFormat) -> Unit,
    onCopyYaml: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("CI/CD Integration", style = titleLargeStyle)
        Text(
            "Integrate CVE-2026-0073 detection into your CI/CD pipeline",
            style = bodyMediumStyle.copy(color = C.OnSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Format selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = C.Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Output Format", style = titleMediumStyle)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CIOutputFormat.entries.forEach { format ->
                        FilterChip(
                            selected = state.ciOutputFormat == format,
                            onClick = { onFormatChange(format) },
                            label = { Text(format.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = C.Primary.copy(alpha = 0.2f),
                                selectedLabelColor = C.Primary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // GitHub Actions
        CICard(
            title = "GitHub Actions",
            description = "Automated CVE detection on every pull request",
            yaml = GitHubActionsYaml,
            onCopy = onCopyYaml
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Jenkins Pipeline
        CICard(
            title = "Jenkins Pipeline",
            description = "Enterprise CI/CD integration with Jenkins",
            yaml = JenkinsYaml,
            onCopy = onCopyYaml
        )

        Spacer(modifier = Modifier.height(12.dp))

        // JSON output preview
        JsonOutputPreview(format = state.ciOutputFormat)
    }
}

@Composable
private fun CICard(
    title: String,
    description: String,
    yaml: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = titleMediumStyle)
                    Text(description, style = labelMediumStyle)
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = C.Primary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            CodeBlock(code = yaml)
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(C.CodeBlockBg)
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            text = code,
            style = codeStyle
        )
    }
}

@Composable
private fun JsonOutputPreview(format: CIOutputFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = C.Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Output Preview (${format.name})", style = titleMediumStyle)
            }
            Spacer(modifier = Modifier.height(8.dp))
            CodeBlock(
                code = if (format == CIOutputFormat.JSON) JsonOutputSample else HtmlOutputSample
            )
        }
    }
}

// ================================================================
// Guide Tab — 安全配置指南页
// ================================================================
@Composable
private fun GuideTabContent(
    state: AdbdSecurityState,
    onToggleConfig: (Int?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Wireless ADB Security Guide", style = titleLargeStyle)
        Text(
            "Best practices for securing your Android development environment",
            style = bodyMediumStyle.copy(color = C.OnSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Security Config List
        SecurityConfigList(
            configs = securityConfigs,
            expandedIndex = state.expandedSecurityConfig,
            onToggle = onToggleConfig
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Patch Status Query
        PatchQuerySection(state = state)
    }
}

private data class SecurityConfig(
    val title: String,
    val emoji: String,
    val description: String,
    val details: String,
    val riskLevel: String
)

private val securityConfigs = listOf(
    SecurityConfig(
        title = "Disable Wireless ADB",
        emoji = "🔒",
        description = "Turn off wireless ADB when not actively developing",
        details = "The most effective mitigation. Use `adb disable-verity` and reboot. " +
                "Re-enable only when needed for development.\n\n" +
                "Command: adb connect <device-ip>:5555\n" +
                "To disable: Settings > Developer Options > Wireless Debugging > OFF",
        riskLevel = "Risk: Critical if left enabled"
    ),
    SecurityConfig(
        title = "VPN + Dedicated Network",
        emoji = "🌐",
        description = "Use VPN or a dedicated development network for ADB",
        details = "Connect your development machine and Android device to the same VPN. " +
                "This prevents network-adjacent attackers from reaching your ADB port.\n\n" +
                "Recommended: WireGuard VPN (wireguard.com)\n" +
                "Alternative: Separate VLAN for development devices",
        riskLevel = "Risk: High if on shared network"
    ),
    SecurityConfig(
        title = "Firewall Rules",
        emoji = "🔥",
        description = "Block ADB ports (5555-5585) on untrusted networks",
        details = "Configure your firewall to block access to ADB ports from untrusted networks.\n\n" +
                "macOS: sudo pfctl -f /etc/pf.conf\n" +
                "Linux: sudo iptables -A INPUT -p tcp --dport 5555:5585 -j DROP\n" +
                "Windows: Windows Defender Firewall > Inbound Rules",
        riskLevel = "Risk: Medium if on trusted network only"
    ),
    SecurityConfig(
        title = "ADB Authorization Whitelist",
        emoji = "✅",
        description = "Only allow authorized RSA key pairs for ADB connections",
        details = "When ADB asks for authorization, always verify the computer's RSA key fingerprint. " +
                "Never authorize a computer you don't recognize.\n\n" +
                "Check authorized keys: ~/.android/adbkey.pub (Linux/Mac)\n" +
                "Revoke all: Settings > Developer Options > Revoke USB debugging authorizations",
        riskLevel = "Risk: Medium"
    )
)

@Composable
private fun SecurityConfigList(
    configs: List<SecurityConfig>,
    expandedIndex: Int?,
    onToggle: (Int?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        configs.forEachIndexed { index, config ->
            SecurityConfigItem(
                config = config,
                isExpanded = expandedIndex == index,
                onToggle = { onToggle(if (expandedIndex == index) null else index) }
            )
        }
    }
}

@Composable
private fun SecurityConfigItem(
    config: SecurityConfig,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(config.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(config.title, style = titleMediumStyle)
                        Text(config.description, style = labelMediumStyle)
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = C.OnSurfaceVariant
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = config.details,
                    style = bodyMediumStyle.copy(color = C.OnSurfaceVariant),
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = config.riskLevel,
                    style = labelMediumStyle.copy(
                        color = when {
                            config.riskLevel.contains("Critical") -> C.CriticalBorder
                            config.riskLevel.contains("High") -> C.VulnerableBorder
                            else -> C.OnSurfaceVariant
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun PatchQuerySection(state: AdbdSecurityState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Patch Status Query", style = titleMediumStyle)
            Text(
                "Check if a specific device configuration is affected",
                style = labelMediumStyle
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Query fields (simplified — in real app would have text inputs)
            var deviceModel by remember { mutableStateOf("Pixel 8 Pro") }
            var androidVersion by remember { mutableStateOf("Android 15 (API 35)") }
            var adbVersion by remember { mutableStateOf("1.0.39") }

            OutlinedTextField(
                value = deviceModel,
                onValueChange = { deviceModel = it },
                label = { Text("Device Model") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = androidVersion,
                onValueChange = { androidVersion = it },
                label = { Text("Android Version") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = adbVersion,
                onValueChange = { adbVersion = it },
                label = { Text("ADB Version (e.g. 1.0.40)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* trigger query */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isQueryingPatch
            ) {
                if (state.isQueryingPatch) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Querying...")
                } else {
                    Text("Check Patch Status")
                }
            }

            // Query result
            if (state.patchQueryResult != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val result = state.patchQueryResult!!
                val (bgColor, borderColor) = if (result.isAffected) {
                    C.CriticalBg to C.CriticalBorder
                } else {
                    C.PatchedBg to C.PatchedBorder
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = if (result.isAffected) "⚠️ Affected" else "✅ Not Affected",
                            style = titleMediumStyle.copy(color = borderColor)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(result.affectedReason, style = bodyMediumStyle, maxLines = 4)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Recommendation: ${result.recommendedAction}", style = labelMediumStyle, maxLines = 3)
                    }
                }
            }
        }
    }
}

// ================================================================
// About Tab — CVE详情页
// ================================================================
@Composable
private fun AboutTabContent(state: AdbdSecurityState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // CVE Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = C.CriticalBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🔴", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("CVE-2026-0073", style = titleLargeStyle.copy(fontSize = 28.sp))
                Text("Android adbd Zero-Click RCE", style = titleMediumStyle.copy(color = C.CriticalBorder))
                Spacer(modifier = Modifier.height(8.dp))
                Text("CVSS Score: 9.8 (Critical)", style = bodyMediumStyle.copy(fontWeight = FontWeight.Bold))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Technical Details
        AboutSection(title = "Technical Details") {
            AboutRow("Component", "adbd (Android Debug Bridge daemon)")
            AboutRow("Function", "adbd_tls_verify_cert (auth.cpp)")
            AboutRow("Vulnerability", "Logic error bypasses mutual TLS authentication")
            AboutRow("Impact", "Remote Code Execution as 'kabuk' user")
            AboutRow("User Interaction", "None required (zero-click)")
            AboutRow("Attack Vector", "Network-adjacent (same LAN)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Affected Versions
        AboutSection(title = "Affected Versions") {
            AboutRow("Android 14", "API 34 — Affected")
            AboutRow("Android 15", "API 35 — Affected")
            AboutRow("Android 16", "API 36 — Affected")
            AboutRow("Android 16-qpr2", "Quarterly Platform Release 2 — Affected")
            AboutRow("Android 13 and earlier", "Not Affected (different adbd)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Patch Information
        AboutSection(title = "Patch Information") {
            AboutRow("Patch Method", "Google Play System Update (Project Mainline)")
            AboutRow("Patch Version", "adb version >= 1.0.40 (May 2026)")
            AboutRow("System Update", "NOT required — Play System Update is sufficient")
            AboutRow("CISA Status", "Listed in Known Exploited Vulnerabilities (KEV) catalog")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // References
        AboutSection(title = "References") {
            ReferenceRow("Android May 2026 Security Bulletin", "source.android.com")
            ReferenceRow("CISA CVE-2026-0073", "cisa.gov")
            ReferenceRow("Google Security Blog", "security.google")
            ReferenceRow("Android Developers", "developer.android.com")
        }
    }
}

@Composable
private fun AboutSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = titleMediumStyle)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = labelMediumStyle, modifier = Modifier.weight(0.4f))
        Text(value, style = bodyMediumStyle.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun ReferenceRow(title: String, url: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(C.Background)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = bodyMediumStyle)
        Text(url, style = labelMediumStyle.copy(color = C.Primary))
    }
}

// ================================================================
// Static YAML Constants — CI配置YAML模板
// ================================================================
private const val GitHubActionsYaml = """
name: CVE-2026-0073 Adbd Security Check
on: [pull_request, push]

jobs:
  security-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Check ADB Version
        run: |
          adb version
          echo "CVE-2026-0073 Scan Complete"
      - name: Run Security Scan
        run: |
          echo "Scanning for CVE-2026-0073..."
          # Check security patch level
          getprop ro.build.version.security_patch
"""

private const val JenkinsYaml = """
pipeline {
    agent any
    stages {
        stage('CVE-2026-0073 Check') {
            steps {
                sh '''
                    echo "Checking Android Security Patch..."
                    adb shell getprop ro.build.version.security_patch
                    echo "Scan Complete"
                '''
            }
        }
    }
    post {
        failure {
            echo '⚠️ Security scan failed - vulnerable device detected'
        }
    }
}
"""

private const val JsonOutputSample = """
{
  "cve_id": "CVE-2026-0073",
  "status": "VULNERABLE",
  "severity": "CRITICAL",
  "cvss_score": 9.8,
  "device": {
    "model": "Pixel 8 Pro",
    "android_version": "Android 15 (API 35)",
    "adb_version": "1.0.39",
    "security_patch": "2026-04"
  },
  "affected_versions": ["API 34", "API 35", "API 36"],
  "recommendation": "Apply May 2026 security patch",
  "scanned_at": "2026-05-20T00:16:00Z"
}
"""

private const val HtmlOutputSample = """
<!DOCTYPE html>
<html>
<head><title>CVE-2026-0073 Scan Report</title></head>
<body>
<h1>⚠️ CVE-2026-0073: VULNERABLE</h1>
<p><strong>Device:</strong> Pixel 8 Pro</p>
<p><strong>ADB Version:</strong> 1.0.39 (outdated)</p>
<p><strong>Recommendation:</strong> Apply May 2026 security patch</p>
</body>
</html>
"""
