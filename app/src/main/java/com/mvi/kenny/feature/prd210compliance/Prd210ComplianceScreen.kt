package com.mvi.kenny.feature.prd210compliance

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * ============================================================
 * Prd210ComplianceScreen — PRD-210 Google Play 2026年4月政策三连击合规工具包
 * ============================================================
 * Main entry point for Google Play April 2026 Policy Compliance Toolkit.
 * 5 功能 Tab：Contacts扫描器 / Location扫描器 / Account Transfer / 三合一清单 / 仪表盘
 *
 * Architecture: MVI pattern with Prd210ComplianceViewModel
 * Visual Style: Dark Terminal theme (Google Play Console 风格)
 *
 * Design: memory/agency/designs/PRD-210-Google-Play-政策三连击合规工具包.md
 * Bilingual comments: CN + EN
 */

// ============ Terminal Theme Colors ============
// ============ 终端主题颜色 ============
private val TerminalBg = Color(0xFF0D1117)
private val TerminalSurface = Color(0xFF161B22)
private val TerminalPrimary = Color(0xFF3FB950)   // 合规绿 ✅
private val TerminalError = Color(0xFFF85149)      // 不合规红 ❌
private val TerminalWarning = Color(0xFFFB8C00)    // 待处理黄 ⚠️
private val TerminalInfo = Color(0xFF79C0FF)       // 信息蓝 ℹ️
private val TerminalOnSurface = Color(0xFFC9D1D9)
private val TerminalOutline = Color(0xFF30363D)
private val TerminalAccent = Color(0xFF79C0FF)
private val P2Color = Color(0xFFFDD835)          // P2 黄色

// ============ Main Screen ============
// ============ 主屏幕 ============
@Composable
fun Prd210ComplianceScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit = {},
    viewModel: Prd210ComplianceViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Update top bar config / 更新 TopBar 配置
    LaunchedEffect(state.selectedTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Google Play 合规工具包"
            )
        )
    }

    // Handle effects / 处理副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Prd210ComplianceEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is Prd210ComplianceEffect.ShowError -> {
                    snackbarHostState.showSnackbar("❌ ${effect.message}")
                }
                is Prd210ComplianceEffect.ReportExported -> {
                    snackbarHostState.showSnackbar("Report exported as ${effect.format} / 报告已导出为 ${effect.format}")
                }
                is Prd210ComplianceEffect.ScanComplete -> {
                    snackbarHostState.showSnackbar("✅ Scan complete / 扫描完成")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = TerminalBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row / Tab 栏
            ComplianceTabRow(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.processIntent(Prd210ComplianceIntent.SelectTab(it)) }
            )

            // Tab Content / Tab 内容
            when (state.selectedTab) {
                ComplianceTab.CONTACTS_SCANNER -> ContactsScannerTab(
                    state = state,
                    onIntent = { viewModel.processIntent(it) }
                )
                ComplianceTab.LOCATION_SCANNER -> LocationScannerTab(
                    state = state,
                    onIntent = { viewModel.processIntent(it) }
                )
                ComplianceTab.ACCOUNT_TRANSFER -> AccountTransferTab(
                    state = state,
                    onIntent = { viewModel.processIntent(it) }
                )
                ComplianceTab.CHECKLIST -> ChecklistTab(
                    state = state,
                    onIntent = { viewModel.processIntent(it) }
                )
                ComplianceTab.DASHBOARD -> DashboardTab(
                    state = state,
                    onIntent = { viewModel.processIntent(it) }
                )
            }
        }
    }
}

// ============ Tab Row ============
// ============ Tab 栏 ============
@Composable
private fun ComplianceTabRow(
    selectedTab: ComplianceTab,
    onTabSelected: (ComplianceTab) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = TerminalSurface,
        contentColor = TerminalOnSurface,
        indicator = { /* Custom indicator handled by selected state */ }
    ) {
        ComplianceTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        color = if (selectedTab == tab) TerminalAccent else TerminalOnSurface,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = getTabIcon(tab),
                        contentDescription = tab.title,
                        tint = if (selectedTab == tab) TerminalAccent else TerminalOnSurface
                    )
                }
            )
        }
    }
}

/**
 * Get icon for tab / 获取 Tab 图标
 */
private fun getTabIcon(tab: ComplianceTab): ImageVector = when (tab) {
    ComplianceTab.CONTACTS_SCANNER -> Icons.Default.Person
    ComplianceTab.LOCATION_SCANNER -> Icons.Default.LocationOn
    ComplianceTab.ACCOUNT_TRANSFER -> Icons.Default.SwapHoriz
    ComplianceTab.CHECKLIST -> Icons.Default.CheckCircle
    ComplianceTab.DASHBOARD -> Icons.Default.Dashboard
}

// ============ Contacts Scanner Tab ============
// ============ Contacts 扫描器 Tab ============
@Composable
private fun ContactsScannerTab(
    state: Prd210ComplianceState,
    onIntent: (Prd210ComplianceIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(16.dp)
    ) {
        // Title / 标题
        Text(
            text = "� Contacts 权限扫描器",
            style = MaterialTheme.typography.titleLarge,
            color = TerminalOnSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Scan AndroidManifest.xml and source code for READ_CONTACTS usage / 扫描 AndroidManifest.xml 和源代码中的 READ_CONTACTS 使用",
            style = MaterialTheme.typography.bodySmall,
            color = TerminalOnSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Path Input / 路径输入
        OutlinedTextField(
            value = state.contactsProjectPath,
            onValueChange = { onIntent(Prd210ComplianceIntent.ContactsPathChanged(it)) },
            label = { Text("Project Path / 项目路径") },
            placeholder = { Text("e.g. /Users/name/Projects/MyApp") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.contactsScanning
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons / 操作按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onIntent(Prd210ComplianceIntent.StartContactsScan) },
                enabled = !state.contactsScanning && state.contactsProjectPath.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TerminalPrimary)
            ) {
                if (state.contactsScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TerminalBg,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Start Scan / 开始扫描")
            }

            Button(
                onClick = { onIntent(Prd210ComplianceIntent.ClearContactsScan) },
                enabled = !state.contactsScanning,
                colors = ButtonDefaults.buttonColors(containerColor = TerminalOutline)
            ) {
                Text("Clear / 清除")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Terminal Output / 终端输出
        if (state.contactsScanOutput.isNotBlank()) {
            TerminalOutputCard(output = state.contactsScanOutput)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Scan Results / 扫描结果
        if (state.contactsScanResults.isNotEmpty()) {
            Text(
                text = "📋 Scan Results (${state.contactsScanResults.size}) / 扫描结果",
                style = MaterialTheme.typography.titleMedium,
                color = TerminalOnSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.contactsScanResults) { item ->
                    ContactsScanResultCard(item = item)
                }
            }
        }
    }
}

/**
 * Contacts scan result card / Contacts 扫描结果卡片
 */
@Composable
private fun ContactsScanResultCard(item: ContactsScanItem) {
    val riskColor = when (item.riskLevel) {
        RiskLevel.P0 -> TerminalError
        RiskLevel.P1 -> TerminalWarning
        RiskLevel.P2 -> P2Color
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header / 头部
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiskBadge(level = item.riskLevel)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.permissionType.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = TerminalAccent
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // File path / 文件路径
            Text(
                text = "📄 ${item.filePath}:${item.lineNumber}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = TerminalOnSurface.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Usage context / 使用上下文
            Text(
                text = item.usageContext,
                style = MaterialTheme.typography.bodySmall,
                color = TerminalOnSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Migration guide / 迁移指南
            Card(
                colors = CardDefaults.cardColors(containerColor = TerminalBg)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "💡",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.migrationGuide,
                        style = MaterialTheme.typography.bodySmall,
                        color = TerminalPrimary
                    )
                }
            }
        }
    }
}

// ============ Location Scanner Tab ============
// ============ Location 扫描器 Tab ============
@Composable
private fun LocationScannerTab(
    state: Prd210ComplianceState,
    onIntent: (Prd210ComplianceIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(16.dp)
    ) {
        // Title / 标题
        Text(
            text = "📍 Location 权限扫描器",
            style = MaterialTheme.typography.titleLarge,
            color = TerminalOnSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Scan for ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION usage / 扫描 ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION 使用情况",
            style = MaterialTheme.typography.bodySmall,
            color = TerminalOnSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Path Input / 路径输入
        OutlinedTextField(
            value = state.locationProjectPath,
            onValueChange = { onIntent(Prd210ComplianceIntent.LocationPathChanged(it)) },
            label = { Text("Project Path / 项目路径") },
            placeholder = { Text("e.g. /Users/name/Projects/MyApp") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.locationScanning
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons / 操作按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onIntent(Prd210ComplianceIntent.StartLocationScan) },
                enabled = !state.locationScanning && state.locationProjectPath.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TerminalPrimary)
            ) {
                if (state.locationScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = TerminalBg,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Start Scan / 开始扫描")
            }

            Button(
                onClick = { onIntent(Prd210ComplianceIntent.ClearLocationScan) },
                enabled = !state.locationScanning,
                colors = ButtonDefaults.buttonColors(containerColor = TerminalOutline)
            ) {
                Text("Clear / 清除")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Terminal Output / 终端输出
        if (state.locationScanOutput.isNotBlank()) {
            TerminalOutputCard(output = state.locationScanOutput)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Scan Results / 扫描结果
        if (state.locationScanResults.isNotEmpty()) {
            Text(
                text = "📋 Scan Results (${state.locationScanResults.size}) / 扫描结果",
                style = MaterialTheme.typography.titleMedium,
                color = TerminalOnSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.locationScanResults) { item ->
                    LocationScanResultCard(item = item)
                }
            }
        }
    }
}

/**
 * Location scan result card / Location 扫描结果卡片
 */
@Composable
private fun LocationScanResultCard(item: LocationScanItem) {
    val riskColor = when (item.riskLevel) {
        RiskLevel.P0 -> TerminalError
        RiskLevel.P1 -> TerminalWarning
        RiskLevel.P2 -> P2Color
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header / 头部
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiskBadge(level = item.riskLevel)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.permissionType.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = TerminalAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (item.needsDeclaration) {
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text("Needs Declaration / 需声明", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = TerminalWarning.copy(alpha = 0.2f),
                            labelColor = TerminalWarning
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // File path / 文件路径
            Text(
                text = "📄 ${item.filePath}:${item.lineNumber}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = TerminalOnSurface.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Usage context / 使用上下文
            Text(
                text = item.usageContext,
                style = MaterialTheme.typography.bodySmall,
                color = TerminalOnSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// ============ Account Transfer Tab ============
// ============ Account Transfer Tab ============
@Composable
private fun AccountTransferTab(
    state: Prd210ComplianceState,
    onIntent: (Prd210ComplianceIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title / 标题
        Text(
            text = "🔄 Account Transfer 工具",
            style = MaterialTheme.typography.titleLarge,
            color = TerminalOnSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Guide for transferring app ownership via Play Console official workflow / 通过 Play Console 官方工作流转移应用所有权的引导",
            style = MaterialTheme.typography.bodySmall,
            color = TerminalOnSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Deadline notice / 截止日期提醒
        val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.of(2026, 5, 27))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (daysRemaining <= 0) TerminalError.copy(alpha = 0.2f)
                else TerminalWarning.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Deadline",
                    tint = if (daysRemaining <= 0) TerminalError else TerminalWarning
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⚠️ Account Transfer 政策已于 2026-05-27 生效！请尽快完成所有权确认。",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (daysRemaining <= 0) TerminalError else TerminalWarning
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App ID Input / App ID 输入
        OutlinedTextField(
            value = state.accountTransferAppId,
            onValueChange = { onIntent(Prd210ComplianceIntent.AccountTransferAppIdChanged(it)) },
            label = { Text("Google Play App ID / Google Play App ID") },
            placeholder = { Text("e.g. com.example.myapp") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.accountTransferGenerating
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Generate button / 生成按钮
        Button(
            onClick = { onIntent(Prd210ComplianceIntent.GenerateAccountTransferGuide) },
            enabled = !state.accountTransferGenerating && state.accountTransferAppId.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = TerminalPrimary)
        ) {
            if (state.accountTransferGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = TerminalBg,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Generate Guide / 生成引导")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transfer Steps / 转移步骤
        if (state.accountTransferGuide.isNotEmpty()) {
            Text(
                text = "📋 Transfer Steps / 转移步骤",
                style = MaterialTheme.typography.titleMedium,
                color = TerminalOnSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            state.accountTransferGuide.forEach { step ->
                TransferStepCard(
                    step = step,
                    onToggle = { onIntent(Prd210ComplianceIntent.MarkTransferStepCompleted(step.stepNumber)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Transfer step card / 转移步骤卡片
 */
@Composable
private fun TransferStepCard(
    step: TransferStep,
    onToggle: () -> Unit
) {
    val backgroundColor = if (step.isCompleted) TerminalPrimary.copy(alpha = 0.1f) else TerminalSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Step number / 步骤编号
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (step.isCompleted) TerminalPrimary else TerminalAccent),
                contentAlignment = Alignment.Center
            ) {
                if (step.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = TerminalBg,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "${step.stepNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TerminalBg,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TerminalOnSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TerminalOnSurface.copy(alpha = 0.8f)
                )
                if (step.screenshotRequired) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📸 Screenshot recommended / 建议截图",
                        style = MaterialTheme.typography.labelSmall,
                        color = TerminalInfo
                    )
                }
            }
        }
    }
}

// ============ Checklist Tab ============
// ============ 检查清单 Tab ============
@Composable
private fun ChecklistTab(
    state: Prd210ComplianceState,
    onIntent: (Prd210ComplianceIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(16.dp)
    ) {
        // Title / 标题
        Text(
            text = "✅ 三合一合规检查清单",
            style = MaterialTheme.typography.titleLarge,
            color = TerminalOnSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Triple policy checklist: Contacts + Location + Account Transfer / 三政策协同检查：Contacts + Location + Account Transfer",
            style = MaterialTheme.typography.bodySmall,
            color = TerminalOnSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Deadline summary cards / 截止日期摘要卡片
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeadlineCard(
                title = "Account Transfer",
                deadline = "2026-05-27",
                isEffective = true,
                modifier = Modifier.weight(1f)
            )
            DeadlineCard(
                title = "Contacts/Location",
                deadline = "2026-10-28",
                isEffective = false,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generate button / 生成按钮
        Button(
            onClick = { onIntent(Prd210ComplianceIntent.GenerateChecklist) },
            enabled = !state.isLoadingGeneral,
            colors = ButtonDefaults.buttonColors(containerColor = TerminalPrimary)
        ) {
            if (state.isLoadingGeneral) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = TerminalBg,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Generate Checklist / 生成清单")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Checklist Items / 检查清单项
        if (state.checklistItems.isNotEmpty()) {
            val completedCount = state.checklistItems.count { it.isCompleted }
            val totalCount = state.checklistItems.size

            // Progress / 进度
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { completedCount.toFloat() / totalCount },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = TerminalPrimary,
                    trackColor = TerminalOutline
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$completedCount / $totalCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = TerminalOnSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.checklistItems) { item ->
                    ChecklistItemCard(
                        item = item,
                        onToggle = { onIntent(Prd210ComplianceIntent.ToggleChecklistItem(item.id)) }
                    )
                }
            }
        }
    }
}

/**
 * Deadline summary card / 截止日期摘要卡片
 */
@Composable
private fun DeadlineCard(
    title: String,
    deadline: String,
    isEffective: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isEffective) TerminalError.copy(alpha = 0.1f)
            else TerminalSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEffective) "⚠️ 已生效" else "📅 截止日期",
                style = MaterialTheme.typography.labelSmall,
                color = if (isEffective) TerminalError else TerminalOnSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TerminalOnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = deadline,
                style = MaterialTheme.typography.bodySmall,
                color = TerminalAccent
            )
        }
    }
}

/**
 * Checklist item card / 检查清单项卡片
 */
@Composable
private fun ChecklistItemCard(
    item: ChecklistItem,
    onToggle: () -> Unit
) {
    val riskColor = when (item.riskLevel) {
        RiskLevel.P0 -> TerminalError
        RiskLevel.P1 -> TerminalWarning
        RiskLevel.P2 -> P2Color
    }
    val backgroundColor = if (item.isCompleted) TerminalPrimary.copy(alpha = 0.1f) else TerminalSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                colors = androidx.compose.material3.CheckboxDefaults.colors(
                    checkedColor = TerminalPrimary,
                    uncheckedColor = TerminalOnSurface.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.policyName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TerminalAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RiskBadge(level = item.riskLevel)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.daysRemaining}d left",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.daysRemaining <= 30) TerminalError else TerminalOnSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TerminalOnSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TerminalOnSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ============ Dashboard Tab ============
// ============ 仪表盘 Tab ============
@Composable
private fun DashboardTab(
    state: Prd210ComplianceState,
    onIntent: (Prd210ComplianceIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
            .padding(16.dp)
    ) {
        // Title / 标题
        Text(
            text = "📊 合规状态仪表盘",
            style = MaterialTheme.typography.titleLarge,
            color = TerminalOnSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Overview of all apps across 3 policies / 所有 App 在三条政策下的合规状态总览",
            style = MaterialTheme.typography.bodySmall,
            color = TerminalOnSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Load button / 加载按钮
        Button(
            onClick = { onIntent(Prd210ComplianceIntent.LoadDashboard) },
            enabled = !state.isLoadingGeneral,
            colors = ButtonDefaults.buttonColors(containerColor = TerminalPrimary)
        ) {
            if (state.isLoadingGeneral) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = TerminalBg,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Load Dashboard / 加载仪表盘")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.dashboardApps.isNotEmpty()) {
            // Stats Cards / 统计卡片
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Apps",
                    value = "${state.dashboardStats.totalApps}",
                    icon = Icons.Default.Description,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "P0 Issues",
                    value = "${state.dashboardStats.urgentCount}",
                    icon = Icons.Default.Warning,
                    color = TerminalError,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Contacts ✅",
                    value = "${state.dashboardStats.contactsCompliant}",
                    icon = Icons.Default.Person,
                    color = TerminalPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Location ✅",
                    value = "${state.dashboardStats.locationCompliant}",
                    icon = Icons.Default.LocationOn,
                    color = TerminalPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Transfer ✅",
                    value = "${state.dashboardStats.accountTransferCompliant}",
                    icon = Icons.Default.SwapHoriz,
                    color = TerminalPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App List / App 列表
            Text(
                text = "📱 App Status / App 状态",
                style = MaterialTheme.typography.titleMedium,
                color = TerminalOnSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.dashboardApps) { app ->
                    DashboardAppCard(app = app)
                }
            }
        }
    }
}

/**
 * Stat card / 统计卡片
 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = TerminalAccent
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TerminalOnSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Dashboard app card / 仪表盘 App 卡片
 */
@Composable
private fun DashboardAppCard(app: AppComplianceStatus) {
    val riskColor = when (app.overallRiskLevel) {
        RiskLevel.P0 -> TerminalError
        RiskLevel.P1 -> TerminalWarning
        RiskLevel.P2 -> P2Color
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header / 头部
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiskBadge(level = app.overallRiskLevel)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleSmall,
                        color = TerminalOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = TerminalOnSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Compliance status row / 合规状态行
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ComplianceStatusChip(
                    label = "Contacts",
                    status = app.contactsStatus
                )
                ComplianceStatusChip(
                    label = "Location",
                    status = app.locationStatus
                )
                ComplianceStatusChip(
                    label = "Transfer",
                    status = app.accountTransferStatus
                )
            }
        }
    }
}

/**
 * Compliance status chip / 合规状态芯片
 */
@Composable
private fun ComplianceStatusChip(
    label: String,
    status: ComplianceStatusType
) {
    val (bgColor, textColor) = when (status) {
        ComplianceStatusType.COMPLIANT -> TerminalPrimary.copy(alpha = 0.2f) to TerminalPrimary
        ComplianceStatusType.NON_COMPLIANT -> TerminalError.copy(alpha = 0.2f) to TerminalError
        ComplianceStatusType.NEEDS_REVIEW -> TerminalWarning.copy(alpha = 0.2f) to TerminalWarning
        ComplianceStatusType.NOT_APPLICABLE -> TerminalOutline to TerminalOnSurface.copy(alpha = 0.7f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "$label: ${status.labelZh}",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Terminal output card / 终端输出卡片
 */
@Composable
private fun TerminalOutputCard(output: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalBg),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = output,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = TerminalPrimary
            )
        }
    }
}

/**
 * Risk badge / 风险徽章
 */
@Composable
private fun RiskBadge(level: RiskLevel) {
    val color = when (level) {
        RiskLevel.P0 -> TerminalError
        RiskLevel.P1 -> TerminalWarning
        RiskLevel.P2 -> P2Color
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = level.label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Extension to check if list is not empty / 列表非空扩展
 */
private fun <T> List<T>.isNotEmpty(): Boolean = size > 0
