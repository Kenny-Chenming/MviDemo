package com.mvi.kenny.feature.lannetworktool

import com.mvi.kenny.base.TopBarAction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.feature.lannetworktool.ComplianceLevel
import com.mvi.kenny.feature.lannetworktool.MainTab
import com.mvi.kenny.feature.lannetworktool.ScanStep
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =============================================================
// LocalNetworkToolScreen — 局域网权限工具主屏幕
// =============================================================
/**
 * Local Network Permission Tool Main Screen / 局域网权限工具主屏幕
 *
 * Android 17 ACCESS_LOCAL_NETWORK compliance detection and migration toolkit.
 * Android 17 ACCESS_LOCAL_NETWORK 合规检测与迁移工具包。
 *
 * Four main sections:
 * 1. Dashboard — Compliance overview / 总览 — 合规概览
 * 2. Scanner — Guided scan flow / 扫描器 — 引导式扫描流程
 * 3. Report — Detailed compliance report / 报告 — 详细合规报告
 * 4. Migration — Manifest diff & permission code / 迁移 — 清单差异与权限代码
 *
 * @param state Current UI state / 当前 UI 状态
 * @param onIntent Send intent to ViewModel / 发送意图到 ViewModel
 * @param onUpdateTopBar Update parent TopBar config / 更新父级 TopBar 配置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalNetworkToolScreen(
    state: LocalNetworkToolState,
    onIntent: (LocalNetworkToolIntent) -> Unit,
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Update TopBar config whenever state changes / 状态变化时更新 TopBar 配置
    LaunchedEffect(state.activeTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "LAN Permission Tool / 局域网权限",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Clear / 清除",
                        onClick = { onIntent(LocalNetworkToolIntent.ClearResults) }
                    )
                )
            )
        )
    }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        // Effect collection would go here in production
        // 实际使用中会在此处收集副作用
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tab Row / 主 Tab 行
            MainTabRow(
                activeTab = state.activeTab,
                onTabSelected = { onIntent(LocalNetworkToolIntent.SelectTab(it)) },
                nonCompliantCount = state.nonCompliantCount
            )

            // Content based on active tab / 基于活跃 Tab 的内容
            when (state.activeTab) {
                MainTab.DASHBOARD -> DashboardContent(
                    state = state,
                    onIntent = onIntent
                )
                MainTab.SCANNER -> ScannerContent(
                    state = state,
                    onIntent = onIntent
                )
                MainTab.REPORT -> ReportContent(
                    state = state,
                    onIntent = onIntent
                )
                MainTab.MIGRATION -> MigrationContent(
                    state = state,
                    onIntent = onIntent
                )
            }
        }
    }
}

// =============================================================
// MainTabRow — 主 Tab 行
// =============================================================
/**
 * Main tab row with navigation items / 带导航项的主 Tab 行
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTabRow(
    activeTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    nonCompliantCount: Int
) {
    TabRow(
        selectedTabIndex = activeTab.ordinal,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        MainTab.entries.forEachIndexed { index, tab ->
            Tab(
                selected = activeTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (tab) {
                                MainTab.DASHBOARD -> "📊 Dashboard"
                                MainTab.SCANNER -> "🔍 Scanner"
                                MainTab.REPORT -> "📋 Report"
                                MainTab.MIGRATION -> "🔧 Migration"
                            }
                        )
                        if (tab == MainTab.MIGRATION && nonCompliantCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge { Text("$nonCompliantCount") }
                        }
                    }
                }
            )
        }
    }
}

// =============================================================
// DashboardContent — 总览内容
// =============================================================
/**
 * Dashboard tab content — compliance overview / 总览 Tab 内容 — 合规概览
 */
@Composable
private fun DashboardContent(
    state: LocalNetworkToolState,
    onIntent: (LocalNetworkToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header info card / 头部信息卡
        InfoCard(
            title = "Android 17 ACCESS_LOCAL_NETWORK / Android 17 局域网权限",
            description = "从 Android 17 (API 37) 起，targeting API 37+ 的应用访问局域网（RFC1918 私有 IP）必须显式声明 ACCESS_LOCAL_NETWORK 运行时权限。权限撤销后访问静默失败，不抛异常。",
            icon = Icons.Default.Security,
            iconColor = Color(0xFF6750A4)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // What requires this permission? / 什么需要此权限？
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚠️ 需要 ACCESS_LOCAL_NETWORK 的场景 / Requires This Permission",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                val scenarios = listOf(
                    "🏠 智能家居 App（访问本地 Hue/米家/Home Assistant）",
                    "📺 媒体串流 App（DLNA/Chromecast/NAS）",
                    "📁 文件传输 App（本地 AirDrop 替代）",
                    "🏢 企业内网 App（本地 VPN/内部系统）",
                    "🔍 网络扫描工具（mDNS/Bonjour）",
                    "📡 IoT 控制面板（访问本地物联网设备）",
                    "🖥️ SSH/VNC 远程桌面 App",
                    "🖨️ 本地打印 App"
                )
                scenarios.forEach { scenario ->
                    Text(
                        text = scenario,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RFC1918 IP ranges / RFC1918 IP 地址段
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🌐 RFC1918 私有地址段 / Private IP Ranges",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                val ipRanges = listOf(
                    Triple("10.0.0.0/8", "A 类私有 / Class A", listOf("10.0.0.1", "10.255.255.255")),
                    Triple("172.16.0.0/12", "B 类私有 / Class B", listOf("172.16.0.1", "172.31.255.255")),
                    Triple("192.168.0.0/16", "C 类私有 / Class C", listOf("192.168.0.1", "192.168.255.255")),
                    Triple("169.254.0.0/16", "链路本地 / Link-local", listOf("169.254.0.1")),
                    Triple("224.0.0.0/4", "多播 / Multicast", listOf("224.0.0.1"))
                )
                ipRanges.forEach { (cidr, label, examples) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = cidr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(
                            text = examples.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Not in scope / 不在范围内
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ℹ️ 不在范围内 / NOT in Scope",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 127.0.0.0/8 (Loopback / 回环地址)\n• 卫星互联网地址\n• 公网 IP（非 RFC1918）",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start scan button / 开始扫描按钮
        Button(
            onClick = { onIntent(LocalNetworkToolIntent.SelectTab(MainTab.SCANNER)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.NetworkCheck, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Scanning / 开始扫描")
        }
    }
}

// =============================================================
// ScannerContent — 扫描器内容
// =============================================================
/**
 * Scanner tab content — guided scan flow / 扫描器 Tab 内容 — 引导式扫描流程
 */
@Composable
private fun ScannerContent(
    state: LocalNetworkToolState,
    onIntent: (LocalNetworkToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Scan step indicator / 扫描步骤指示器
        ScanStepIndicator(
            currentStep = state.scanStep,
            onStepClick = { step ->
                // Only allow going back to completed steps / 只允许返回已完成的步骤
                if (step.stepNumber <= state.scanStep.stepNumber) {
                    onIntent(LocalNetworkToolIntent.SetScanStep(step))
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (state.scanStep) {
            ScanStep.PROJECT_SELECTION -> ProjectSelectionStep(
                projectPath = state.projectPath,
                isScanning = state.isScanning,
                onPathChange = { onIntent(LocalNetworkToolIntent.SetProjectPath(it)) },
                onStartScan = { onIntent(LocalNetworkToolIntent.StartScan) }
            )
            ScanStep.SCANNING -> ScanningStep(
                scanLogs = state.scanLogs,
                scanProgress = state.scanProgress,
                isScanning = state.isScanning,
                onCancel = { onIntent(LocalNetworkToolIntent.CancelScan) },
                onNext = { onIntent(LocalNetworkToolIntent.SetScanStep(ScanStep.RESULTS)) }
            )
            ScanStep.RESULTS -> ResultsStep(
                accessPaths = state.accessPaths,
                selectedPath = state.selectedAccessPath,
                onPathSelected = { onIntent(LocalNetworkToolIntent.SelectAccessPath(it)) },
                onNext = { onIntent(LocalNetworkToolIntent.SetScanStep(ScanStep.COMPLIANCE)) },
                onBack = { onIntent(LocalNetworkToolIntent.SetScanStep(ScanStep.SCANNING)) }
            )
            ScanStep.COMPLIANCE -> ComplianceStep(
                accessPaths = state.accessPaths,
                onNext = { onIntent(LocalNetworkToolIntent.SetScanStep(ScanStep.MIGRATION)) },
                onBack = { onIntent(LocalNetworkToolIntent.SetScanStep(ScanStep.RESULTS)) }
            )
            ScanStep.MIGRATION -> MigrationStep(
                state = state,
                onIntent = onIntent,
                onBack = { onIntent(LocalNetworkToolIntent.SetScanStep(ScanStep.COMPLIANCE)) }
            )
        }
    }
}

// =============================================================
// ScanStepIndicator — 扫描步骤指示器
// =============================================================
@Composable
private fun ScanStepIndicator(
    currentStep: ScanStep,
    onStepClick: (ScanStep) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ScanStep.entries.forEach { step ->
            val isActive = step.stepNumber == currentStep.stepNumber
            val isCompleted = step.stepNumber < currentStep.stepNumber

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onStepClick(step) }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> Color(0xFF4CAF50)
                                isActive -> Color(0xFF6750A4)
                                else -> Color(0xFFE0E0E0)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${step.stepNumber}",
                            color = if (isActive) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.title.substringBefore(" /"),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive || isCompleted) MaterialTheme.colorScheme.primary else Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            if (step != ScanStep.entries.last()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(top = 16.dp)
                        .background(
                            if (step.stepNumber < currentStep.stepNumber) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
                        )
                )
            }
        }
    }
}

// =============================================================
// ProjectSelectionStep — 项目选择步骤
// =============================================================
@Composable
private fun ProjectSelectionStep(
    projectPath: String,
    isScanning: Boolean,
    onPathChange: (String) -> Unit,
    onStartScan: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Step 1: Select Project / 步骤 1: 选择项目",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = projectPath,
            onValueChange = onPathChange,
            label = { Text("Project Path / 项目路径") },
            placeholder = { Text("e.g., /Users/kenny/WorkSpace/AndroidStudioProjects/MyApp") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isScanning,
            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick path buttons / 快捷路径按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quickPaths = listOf(
                "MyMviProject" to "/Users/kenny/WorkSpace/AndroidStudioProjects/MyMviProject"
            )
            quickPaths.forEach { (name, path) ->
                FilterChip(
                    selected = projectPath == path,
                    onClick = { onPathChange(path) },
                    label = { Text(name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info / 信息
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🔍 Scan Coverage / 扫描范围",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                val scanTargets = listOf(
                    "InetAddress.getByName() / getAllByName()",
                    "Socket / InetSocketAddress",
                    "HttpURLConnection / HttpsURLConnection",
                    "OkHttpClient / Retrofit baseUrl",
                    "AndroidManifest.xml permission declarations"
                )
                scanTargets.forEach { target ->
                    Text(
                        text = "• $target",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStartScan,
            modifier = Modifier.fillMaxWidth(),
            enabled = projectPath.isNotBlank() && !isScanning
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isScanning) "Scanning... / 扫描中..." else "Start Scan / 开始扫描")
        }
    }
}

// =============================================================
// ScanningStep — 扫描中步骤
// =============================================================
@Composable
private fun ScanningStep(
    scanLogs: List<ScanLogEntry>,
    scanProgress: Float,
    isScanning: Boolean,
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = scanProgress,
        label = "scan_progress"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Step 2: Scanning / 步骤 2: 扫描中",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Progress / 进度
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color(0xFF6750A4)
        )
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scan logs / 扫描日志
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                items(scanLogs) { entry ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = entry.level.prefix,
                            color = entry.level.color,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = entry.message,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons / 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = isScanning
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancel / 取消")
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = !isScanning && scanProgress >= 1f
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Results / 结果")
            }
        }
    }
}

// =============================================================
// ResultsStep — 扫描结果步骤
// =============================================================
@Composable
private fun ResultsStep(
    accessPaths: List<LanAccessPath>,
    selectedPath: LanAccessPath?,
    onPathSelected: (LanAccessPath) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Step 3: Results / 步骤 3: 扫描结果",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Summary cards / 摘要卡片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                title = "Total / 总计",
                value = "${accessPaths.size}",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Compliant / 合规",
                value = "${accessPaths.count { it.permissionStatus == PermissionStatus.DECLARED }}",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Non-Compliant / 不合规",
                value = "${accessPaths.count { it.permissionStatus == PermissionStatus.MISSING }}",
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (accessPaths.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "✅ No LAN access paths found / 未发现局域网访问路径",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Your app doesn't access local network / 您的应用未访问本地网络",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Access paths list / 访问路径列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(accessPaths) { path ->
                    AccessPathItem(
                        path = path,
                        isSelected = path == selectedPath,
                        onClick = { onPathSelected(path) }
                    )
                }
            }
        }

        // Detail view / 详情视图
        AnimatedVisibility(
            visible = selectedPath != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            selectedPath?.let { path ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📍 ${path.file.substringAfterLast("/")}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Line ${path.line} · ${path.method}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "IP Range: ${path.targetIpRange}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Status: ${path.permissionStatus.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = path.permissionStatus.color
                        )
                        if (path.codeSnippet.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = path.codeSnippet,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back / 返回")
            }
            Button(onClick = onNext, modifier = Modifier.weight(1f)) {
                Text("Compliance / 合规评估")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Security, contentDescription = null)
            }
        }
    }
}

// =============================================================
// ComplianceStep — 合规评估步骤
// =============================================================
@Composable
private fun ComplianceStep(
    accessPaths: List<LanAccessPath>,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val compliantCount = accessPaths.count { it.permissionStatus == PermissionStatus.DECLARED }
    val nonCompliantCount = accessPaths.count { it.permissionStatus == PermissionStatus.MISSING }
    val uncertainCount = accessPaths.count { it.permissionStatus == PermissionStatus.UNCERTAIN }
    val total = accessPaths.size
    val score = if (total == 0) 100 else ((compliantCount.toFloat() / total) * 100).toInt()

    val level = when {
        score >= 90 -> ComplianceLevel.FULL
        score >= 60 -> ComplianceLevel.PARTIAL
        else -> ComplianceLevel.NON_COMPLIANT
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Step 4: Compliance / 步骤 4: 合规评估",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Score circle / 评分圆
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.size(120.dp),
                strokeWidth = 12.dp,
                color = level.color,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = level.color
                )
                Text(
                    text = level.label.substringBefore(" /"),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Breakdown / 分解
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Compliant / 合规", value = "$compliantCount", color = Color(0xFF4CAF50))
            StatItem(label = "Missing / 缺失", value = "$nonCompliantCount", color = Color(0xFFF44336))
            StatItem(label = "Uncertain / 不确定", value = "$uncertainCount", color = Color(0xFFFF9800))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recommendation / 建议
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (level == ComplianceLevel.FULL) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = when (level) {
                        ComplianceLevel.FULL -> "✅ 完美！您的应用已正确声明 ACCESS_LOCAL_NETWORK 权限"
                        ComplianceLevel.PARTIAL -> "⚠️ 部分合规 — 建议补充缺失的权限声明"
                        ComplianceLevel.NON_COMPLIANT -> "🚨 不合规 — targetSDK 37+ 必须声明 ACCESS_LOCAL_NETWORK"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (level) {
                        ComplianceLevel.FULL -> "所有检测到的局域网访问路径都已正确声明权限"
                        ComplianceLevel.PARTIAL -> "仍有 $nonCompliantCount 个访问路径缺少权限声明"
                        ComplianceLevel.NON_COMPLIANT -> "您的应用访问局域网但未声明 ACCESS_LOCAL_NETWORK"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back / 返回")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = nonCompliantCount > 0
            ) {
                Text("Migration / 迁移")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }
}

// =============================================================
// MigrationStep — 迁移步骤
// =============================================================
@Composable
private fun MigrationStep(
    state: LocalNetworkToolState,
    onIntent: (LocalNetworkToolIntent) -> Unit,
    onBack: () -> Unit
) {
    var showDiffPreview by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Step 5: Migration / 步骤 5: 迁移",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Manifest diff / 清单差异
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📄 AndroidManifest.xml Diff",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (state.manifestDiff == null) {
                            Button(
                                onClick = { onIntent(LocalNetworkToolIntent.GenerateManifestDiff) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Diff / 生成差异")
                            }
                        } else {
                            // Show diff preview / 显示差异预览
                            if (showDiffPreview) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "--- ORIGINAL / 原始",
                                            color = Color(0xFFFF6B6B),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = state.manifestDiff.original,
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "+++ MODIFIED / 修改后",
                                            color = Color(0xFF4CAF50),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = state.manifestDiff.modified,
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showDiffPreview = !showDiffPreview },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (showDiffPreview) "Hide / 隐藏" else "Preview / 预览")
                                }
                                Button(
                                    onClick = { onIntent(LocalNetworkToolIntent.ApplyManifestDiff(false)) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Apply / 应用")
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Permission request template / 权限请求模板
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📝 Runtime Permission Request / 运行时权限请求",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val templates = listOf(
                            PermissionTemplate(
                                templateName = "Activity Result API (Recommended) / 推荐",
                                description = "Modern ActivityResultContract approach / 现代 ActivityResultContract 方式",
                                templateCode = """// Kotlin — ACCESS_LOCAL_NETWORK permission request
// ACCESS_LOCAL_NETWORK 权限请求
// @see https://developer.android.com/reference/android/Manifest.permission#ACCESS_LOCAL_NETWORK

private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted: Boolean ->
    if (isGranted) {
        // Permission granted — LAN access now allowed / 权限已授予 — 允许局域网访问
        Log.d(TAG, "ACCESS_LOCAL_NETWORK granted")
    } else {
        // Permission denied — graceful degradation / 权限被拒绝 — 优雅降级
        Log.w(TAG, "ACCESS_LOCAL_NETWORK denied — show guidance UI")
        showLanPermissionDeniedUI()
    }
}

// Check permission before LAN access / 访问局域网前检查权限
private fun accessLocalNetwork() {
    when {
        // Android 12 (API 31) and below with INTERNET permission / Android 12 及以下带 INTERNET 权限
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
            // Legacy implicit grant — no runtime request needed / 传统隐式授予 — 无需运行时请求
            performLanAccess()
        }
        // Android 13+ (API 33) with NEARBY_DEVICES permission group / Android 13+ 带 NEARBY_DEVICES 权限组
        checkPermission(Manifest.permission.ACCESS_LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED -> {
            performLanAccess()
        }
        // Should show rationale / 应该显示理由
        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_LOCAL_NETWORK) -> {
            showPermissionRationale()
        }
        else -> {
            // Request permission / 请求权限
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }
    }
}

// Graceful degradation when permission denied / 权限被拒绝时的优雅降级
private fun showLanPermissionDeniedUI() {
    // Show user-friendly message / 显示用户友好的消息
    // Guide user to Settings to enable permission / 引导用户到设置中启用权限
    AlertDialog.Builder(this)
        .setTitle("LAN Access Required / 需要局域网访问权限")
        .setMessage("This feature needs to access local devices. Please enable in Settings.")
        .setPositiveButton("Settings") { _, _ ->
            // Open app settings / 打开应用设置
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                startActivity(this)
            }
        }
        .setNegativeButton("Cancel", null)
        .show()
}
""".trimIndent()
                            ),
                            PermissionTemplate(
                                templateName = "Legacy Approach / 传统方式",
                                description = "Manual permission check with ContextCompat / 使用 ContextCompat 手动检查权限",
                                templateCode = """// Legacy approach / 传统方式
private fun requestLocalNetworkPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_LOCAL_NETWORK
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_LOCAL_NETWORK),
                REQUEST_CODE_LOCAL_NETWORK
            )
        } else {
            performLanAccess()
        }
    } else {
        // No runtime permission needed below API 31 / API 31 以下无需运行时权限
        performLanAccess()
    }
}

override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
        REQUEST_CODE_LOCAL_NETWORK -> {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performLanAccess()
            } else {
                // Handle denial gracefully / 优雅处理拒绝
                showLanPermissionDeniedUI()
            }
        }
    }
}
""".trimIndent()
                            )
                        )

                        templates.forEach { template ->
                            val isSelected = state.selectedTemplate == template
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onIntent(LocalNetworkToolIntent.SelectTemplate(template)) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4CAF50)) else null
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = template.templateName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = template.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onIntent(LocalNetworkToolIntent.GeneratePermissionCode) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.selectedTemplate != null
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Code / 生成代码")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Generated code display / 生成的代码显示
            if (state.generatedPermissionCode.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Generated Code / 生成的代码",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { onIntent(LocalNetworkToolIntent.CopyPermissionCode) }
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = "Copy / 复制",
                                        tint = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.generatedPermissionCode,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE0E0E0),
                                fontSize = 11.sp,
                                maxLines = 20,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back to Compliance / 返回合规评估")
        }
    }
}

// =============================================================
// ReportContent — 报告内容
// =============================================================
@Composable
private fun ReportContent(
    state: LocalNetworkToolState,
    onIntent: (LocalNetworkToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Compliance Report / 合规报告",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (state.complianceReport == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No report generated yet / 尚未生成报告",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Run a scan first to generate compliance report / 先运行扫描以生成合规报告",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onIntent(LocalNetworkToolIntent.SelectTab(MainTab.SCANNER)) }
                    ) {
                        Text("Go to Scanner / 前往扫描器")
                    }
                }
            }
        } else {
            val report = state.complianceReport

            // Score / 评分
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = report.complianceLevel.color.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${report.complianceScore}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = report.complianceLevel.color
                    )
                    Text(
                        text = report.complianceLevel.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = report.complianceLevel.color
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = report.summaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary stats / 摘要统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "Total / 总计",
                    value = "${report.totalPaths}",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Compliant / 合规",
                    value = "${report.compliantPaths}",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Non-Compliant / 不合规",
                    value = "${report.nonCompliantPaths}",
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export buttons / 导出按钮
            Text(
                text = "Export Report / 导出报告",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportFormat.entries.forEach { format ->
                    OutlinedButton(
                        onClick = { onIntent(LocalNetworkToolIntent.ExportReport(format)) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(format.label)
                    }
                }
            }
        }
    }
}

// =============================================================
// MigrationContent — 迁移内容（完整 Tab）
// =============================================================
@Composable
private fun MigrationContent(
    state: LocalNetworkToolState,
    onIntent: (LocalNetworkToolIntent) -> Unit
) {
    // Redirect to scanner migration step / 重定向到扫描器迁移步骤
    LaunchedEffect(Unit) {
        if (state.scanStep != ScanStep.MIGRATION) {
            // Auto-navigate to migration step / 自动导航到迁移步骤
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Migration / 迁移方案",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Migration guidance / 迁移指导
        InfoCard(
            title = "NEARBY_WIFI_DEVICES vs ACCESS_LOCAL_NETWORK",
            description = "临时 Wi-Fi 设备发现 → NEARBY_WIFI_DEVICES (Nearby Devices API)\n持久局域网访问 → ACCESS_LOCAL_NETWORK\n\nDon't confuse the two! / 不要混淆两者！",
            icon = Icons.Default.Info,
            iconColor = Color(0xFF2196F3)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick actions / 快捷操作
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onIntent(LocalNetworkToolIntent.GenerateManifestDiff) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Manifest Diff / 清单差异")
            }
            Button(
                onClick = { onIntent(LocalNetworkToolIntent.GeneratePermissionCode) },
                modifier = Modifier.weight(1f),
                enabled = state.selectedTemplate != null
            ) {
                Icon(Icons.Default.Code, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Permission Code / 权限代码")
            }
        }
    }
}

// =============================================================
// AccessPathItem — 访问路径列表项
// =============================================================
@Composable
private fun AccessPathItem(
    path: LanAccessPath,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon / 状态图标
            Icon(
                imageVector = when (path.permissionStatus) {
                    PermissionStatus.DECLARED -> Icons.Default.CheckCircle
                    PermissionStatus.MISSING -> Icons.Default.Error
                    PermissionStatus.UNCERTAIN -> Icons.Default.Warning
                    PermissionStatus.NOT_NEEDED -> Icons.Default.Info
                },
                contentDescription = null,
                tint = path.permissionStatus.color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = path.file.substringAfterLast("/"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Line ${path.line} · ${path.method}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = path.accessType.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6750A4)
                )
            }

            Text(
                text = path.permissionStatus.label.substringBefore(" /"),
                style = MaterialTheme.typography.labelSmall,
                color = path.permissionStatus.color
            )
        }
    }
}

// =============================================================
// StatItem — 统计项
// =============================================================
@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

// =============================================================
// StatCard — 统计卡片
// =============================================================
@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

// =============================================================
// SummaryCard — 摘要卡片
// =============================================================
@Composable
private fun SummaryCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

// =============================================================
// InfoCard — 信息卡片
// =============================================================
@Composable
private fun InfoCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
