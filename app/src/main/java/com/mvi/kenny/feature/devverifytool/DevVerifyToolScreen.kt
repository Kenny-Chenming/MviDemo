package com.mvi.kenny.feature.devverifytool

import java.time.LocalDate

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collect

/**
 * ============================================================
 * DevVerifyToolScreen — PRD-209 Android 开发者验证合规工具包
 * ============================================================
 * Main entry point for Android Developer Verification Compliance Toolkit.
 * 5 功能 Tab：扫描器 / 批量注册 / 密钥管理 / 截止日期 / 仪表盘
 *
 * Architecture: MVI pattern with DevVerifyToolViewModel
 * Visual Style: Dark Terminal theme (参考 PRD-206 深色 Terminal 风格)
 *
 * Design: memory/agency/designs/PRD-209-Android-开发者验证合规工具包.md
 * Bilingual comments: CN + EN
 */

// ============ Terminal Theme Colors ============
// ============ 终端主题颜色 ============
private val TerminalBg = Color(0xFF0D1117)
private val TerminalSurface = Color(0xFF161B22)
private val TerminalPrimary = Color(0xFF3FB950)   // 合规绿 ✅
private val TerminalError = Color(0xFFF85149)      // 不合规红 ❌
private val TerminalWarning = Color(0xFFD29922)     // 待处理黄 ⚠️
private val TerminalOnSurface = Color(0xFFC9D1D9)
private val TerminalOutline = Color(0xFF30363D)
private val TerminalAccent = Color(0xFF79C0FF)    // Terminal accent / 终端强调色

// ============ Main Screen ============
// ============ 主屏幕 ============
@Composable
fun DevVerifyToolScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit = {},
    viewModel: DevVerifyToolViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Update parent TopBar when tab changes / Tab 变化时更新父组件 TopBar
    LaunchedEffect(state.selectedTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Dev验证合规 · ${state.selectedTab.title}"
            )
        )
    }

    // Listen for effects / 监听副作用
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            // Effect handling — snackerbar, navigation, etc.
            // 副作用处理 — snackbar、导航等
        }
    }

    Scaffold(
        containerColor = TerminalBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row / Tab 行
            TabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                containerColor = TerminalSurface,
                contentColor = TerminalOnSurface,
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .height(3.dp)
                            .fillMaxSize()
                    )
                }
            ) {
                VerificationTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.sendIntent(DevVerifyToolIntent.SelectTab(tab)) },
                        text = {
                            Text(
                                text = tab.title,
                                color = if (state.selectedTab == tab) TerminalAccent else TerminalOnSurface,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Tab Content / Tab 内容
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TerminalBg)
            ) {
                when (state.selectedTab) {
                    VerificationTab.SCANNER -> ScannerTab(state, viewModel)
                    VerificationTab.BATCH_REGISTER -> BatchRegisterTab(state, viewModel)
                    VerificationTab.KEY_MANAGEMENT -> KeyManagementTab(state, viewModel)
                    VerificationTab.DEADLINES -> DeadlinesTab(state, viewModel)
                    VerificationTab.DASHBOARD -> DashboardTab(state, viewModel)
                }
            }
        }
    }
}

// =============================================================
// Scanner Tab — App 验证状态扫描器
// =============================================================
@Composable
private fun ScannerTab(state: DevVerifyToolState, viewModel: DevVerifyToolViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with scan button / 带扫描按钮的头部
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📦 App Compliance Scanner",
                style = MaterialTheme.typography.titleMedium,
                color = TerminalOnSurface,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    if (state.isScanning) viewModel.sendIntent(DevVerifyToolIntent.CancelScan)
                    else viewModel.sendIntent(DevVerifyToolIntent.StartScan)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isScanning) TerminalError else TerminalPrimary
                )
            ) {
                if (state.isScanning) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cancel")
                } else {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Start Scan")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Progress bar / 进度条
        if (state.isScanning) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { state.scanProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = TerminalAccent,
                    trackColor = TerminalOutline
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${(state.scanProgress * 100).toInt()}%",
                    color = TerminalAccent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // Stats summary / 统计摘要
        if (state.scanResults.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    label = "Total",
                    value = "${state.scanResults.size}",
                    color = TerminalOnSurface
                )
                StatChip(
                    label = "✅ Verified",
                    value = "${state.scanResults.count { it.status == AppComplianceStatus.VERIFIED }}",
                    color = TerminalPrimary
                )
                StatChip(
                    label = "❌ Not Verified",
                    value = "${state.scanResults.count { it.status == AppComplianceStatus.NOT_VERIFIED }}",
                    color = TerminalError
                )
                StatChip(
                    label = "⚠️ Expiring",
                    value = "${state.scanResults.count { it.status == AppComplianceStatus.EXPIRING_SOON }}",
                    color = TerminalWarning
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // Results list / 结果列表
        if (state.scanResults.isEmpty() && !state.isScanning) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TerminalOutline
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No scan results yet",
                        color = TerminalOnSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "点击「Start Scan」开始扫描",
                        color = TerminalOnSurface.copy(alpha = 0.4f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.scanResults) { item ->
                    AppComplianceCard(item)
                }
            }
        }
    }
}

// =============================================================
// Batch Register Tab — 批量注册 CLI 工具
// =============================================================
@Composable
private fun BatchRegisterTab(state: DevVerifyToolState, viewModel: DevVerifyToolViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // CSV Import section / CSV 导入区
        Text(
            text = "📄 CSV Batch Registration",
            style = MaterialTheme.typography.titleMedium,
            color = TerminalOnSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "CSV format: app_package,app_name,signing_keystore_path",
            color = TerminalOnSurface.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(12.dp))

        // CSV path display + import button / CSV 路径显示 + 导入按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Path display / 路径显示
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(TerminalSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, TerminalOutline, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = state.csvPath ?: "No CSV file selected",
                    color = if (state.csvPath != null) TerminalPrimary else TerminalOnSurface.copy(alpha = 0.4f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Import button / 导入按钮
            Button(
                onClick = { viewModel.sendIntent(DevVerifyToolIntent.ImportCsv("/path/to/apps.csv")) },
                colors = ButtonDefaults.buttonColors(containerColor = TerminalAccent)
            ) {
                Text("Import CSV", color = TerminalBg)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Command preview / 命令预览
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurface, RoundedCornerShape(8.dp))
                .border(1.dp, TerminalOutline, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = if (state.csvPath != null)
                    "avd verify register-batch --csv ${state.csvPath}"
                else
                    "# avd verify register-batch --csv <csv_path>",
                color = TerminalAccent,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // Execute button / 执行按钮
        Button(
            onClick = { viewModel.sendIntent(DevVerifyToolIntent.ExecuteBatchRegister) },
            enabled = state.csvPath != null && !state.isRegistering,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = TerminalPrimary,
                disabledContainerColor = TerminalOutline
            )
        ) {
            if (state.isRegistering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = TerminalOnSurface,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Registering...")
            } else {
                Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Execute Batch Register")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Terminal output / 终端输出
        Text(
            text = "Terminal Output",
            color = TerminalOnSurface,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
                .border(1.dp, TerminalOutline, RoundedCornerShape(8.dp))
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = state.batchRegisterOutput.ifEmpty { "# Waiting for action...\n" },
                color = TerminalPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// =============================================================
// Key Management Tab — 签名密钥注册管理
// =============================================================
@Composable
private fun KeyManagementTab(state: DevVerifyToolState, viewModel: DevVerifyToolViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🔑 Signing Key Management",
            style = MaterialTheme.typography.titleMedium,
            color = TerminalOnSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Manage Android Developer Console signing key registrations",
            color = TerminalOnSurface.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(16.dp))

        // Mock key items / 模拟密钥数据
        val mockKeys = remember {
            listOf(
                KeyItem(
                    id = "1", appName = "My App", packageName = "com.example.myapp",
                    keyType = KeyType.GOOGLE_MANAGED, isRegistered = true,
                    expiresAt = LocalDate.now().plusYears(2)
                ),
                KeyItem(
                    id = "2", appName = "Old App", packageName = "com.example.oldapp",
                    keyType = KeyType.SELF_SIGNED, isRegistered = false,
                    expiresAt = LocalDate.now().plusMonths(3), isExpiringSoon = true
                ),
                KeyItem(
                    id = "3", appName = "New App", packageName = "com.example.newapp",
                    keyType = KeyType.UPLOAD_KEY, isRegistered = true,
                    expiresAt = LocalDate.now().plusMonths(1), isExpiringSoon = true
                )
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(mockKeys) { key ->
                KeyItemCard(key)
            }
        }
    }
}

// =============================================================
// Deadlines Tab — 地域截止日期倒计时
// =============================================================
@Composable
private fun DeadlinesTab(state: DevVerifyToolState, viewModel: DevVerifyToolViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "⏰ Regional Deadlines",
            style = MaterialTheme.typography.titleMedium,
            color = TerminalOnSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Android Developer Verification mandatory deadlines by region",
            color = TerminalOnSurface.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(16.dp))

        // Region filter chips / 地区筛选
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DevRegion.entries.forEach { region ->
                val isSelected = state.selectedRegions.contains(region)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.sendIntent(DevVerifyToolIntent.ToggleRegion(region)) },
                    label = { Text(region.displayNameZh, fontSize = 12.sp) },
                    leadingIcon = {
                        if (isSelected) Icon(Icons.Default.Check, null, Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = TerminalSurface,
                        labelColor = TerminalOnSurface,
                        selectedContainerColor = TerminalAccent.copy(alpha = 0.2f),
                        selectedLabelColor = TerminalAccent
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Deadline cards / 截止日期卡片
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(state.regionDeadlineItems.filter { it.isSelected }) { item ->
                DeadlineCard(item)
            }
        }
    }
}

// =============================================================
// Dashboard Tab — 团队合规状态仪表盘
// =============================================================
@Composable
private fun DashboardTab(state: DevVerifyToolState, viewModel: DevVerifyToolViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header / 头部
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 Compliance Dashboard",
                style = MaterialTheme.typography.titleMedium,
                color = TerminalOnSurface,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { viewModel.sendIntent(DevVerifyToolIntent.RefreshDashboard) },
                colors = ButtonDefaults.buttonColors(containerColor = TerminalSurface),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Refresh", fontSize = 12.sp, color = TerminalOnSurface)
            }
        }
        Spacer(Modifier.height(16.dp))

        // Stats cards row / 统计卡片行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                label = "Total",
                value = "${state.dashboardStats.totalApps}",
                color = TerminalOnSurface
            )
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                label = "Verified",
                value = "${state.dashboardStats.verifiedCount}",
                color = TerminalPrimary
            )
            DashboardStatCard(
                modifier = Modifier.weight(1f),
                label = "Not Verified",
                value = "${state.dashboardStats.notVerifiedCount}",
                color = TerminalError
            )
        }

        Spacer(Modifier.height(12.dp))

        // Compliance rate / 合规率
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Compliance Rate / 合规率",
                        color = TerminalOnSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(state.dashboardStats.complianceRate * 100).toInt()}%",
                        color = if (state.dashboardStats.complianceRate > 0.7f) TerminalPrimary
                               else if (state.dashboardStats.complianceRate > 0.4f) TerminalWarning
                               else TerminalError,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.dashboardStats.complianceRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (state.dashboardStats.complianceRate > 0.7f) TerminalPrimary
                           else if (state.dashboardStats.complianceRate > 0.4f) TerminalWarning
                           else TerminalError,
                    trackColor = TerminalOutline
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Expiring soon warning / 即将过期警告
        if (state.dashboardStats.expiringSoonCount > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = TerminalWarning.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = TerminalWarning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${state.dashboardStats.expiringSoonCount} apps expiring soon",
                            color = TerminalWarning,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Check「Scanner」tab for details / 查看「扫描器」标签页详情",
                            color = TerminalWarning.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Operation logs / 操作日志
        if (state.operationLogs.isNotEmpty()) {
            Text(
                text = "📋 Recent Operations / 最近操作",
                color = TerminalOnSurface,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            state.operationLogs.take(5).forEach { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (log.isSuccess) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (log.isSuccess) TerminalPrimary else TerminalError,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = log.action,
                        color = TerminalOnSurface,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = log.target,
                        color = TerminalOnSurface.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// =============================================================
// App Compliance Card — App 合规结果卡片
// =============================================================
@Composable
private fun AppComplianceCard(item: AppComplianceItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon / 状态图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(item.status.color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (item.status) {
                        AppComplianceStatus.VERIFIED -> "✅"
                        AppComplianceStatus.NOT_VERIFIED -> "❌"
                        AppComplianceStatus.EXPIRING_SOON -> "⚠️"
                        AppComplianceStatus.UNKNOWN -> "❓"
                    },
                    fontSize = 20.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.appName,
                    color = TerminalOnSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.packageName,
                    color = TerminalOnSurface.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (item.verificationDate != null) {
                    Text(
                        text = "Verified: ${item.verificationDate}",
                        color = TerminalOnSurface.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }
            }
            // Status chip / 状态 Chip
            Box(
                modifier = Modifier
                    .background(item.status.color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.status.labelZh,
                    color = item.status.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// =============================================================
// Key Item Card — 密钥管理卡片
// =============================================================
@Composable
private fun KeyItemCard(item: KeyItem) {
    val borderColor = if (item.isExpiringSoon) TerminalWarning else TerminalOutline
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Key,
                contentDescription = null,
                tint = if (item.isRegistered) TerminalPrimary else TerminalError,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.appName, color = TerminalOnSurface, fontWeight = FontWeight.Medium)
                Text(
                    text = item.packageName,
                    color = TerminalOnSurface.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.keyType.labelZh,
                        color = TerminalOnSurface.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                    if (item.expiresAt != null) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "⏱ ${item.expiresAt}",
                            color = if (item.isExpiringSoon) TerminalWarning else TerminalOnSurface.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .background(
                        if (item.isRegistered) TerminalPrimary.copy(alpha = 0.15f)
                        else TerminalError.copy(alpha = 0.15f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (item.isRegistered) "✅ Registered" else "❌ Not Registered",
                    color = if (item.isRegistered) TerminalPrimary else TerminalError,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// =============================================================
// Deadline Card — 截止日期倒计时卡片
// =============================================================
@Composable
private fun DeadlineCard(item: RegionDeadlineItem) {
    val urgency = when {
        item.daysRemaining < 30 -> TerminalError
        item.daysRemaining < 90 -> TerminalWarning
        else -> TerminalPrimary
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${item.region.displayName} (${item.region.displayNameZh})",
                        color = TerminalOnSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = item.region.deadline.toString(),
                        color = TerminalOnSurface.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${item.daysRemaining}",
                        color = urgency,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "days remaining",
                        color = TerminalOnSurface.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = urgency,
                trackColor = TerminalOutline
            )
        }
    }
}

// =============================================================
// Dashboard Stat Card — 仪表盘统计卡片
// =============================================================
@Composable
private fun DashboardStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = TerminalOnSurface.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}

// =============================================================
// Stat Chip — 统计 Chip
// =============================================================
@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .background(TerminalSurface, RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$label: ", color = TerminalOnSurface.copy(alpha = 0.6f), fontSize = 11.sp)
            Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}


