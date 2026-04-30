package com.mvi.kenny.feature.android17compliance

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig

// ================================================================
// Android17ComplianceScreen — Android 17 合规工具主屏幕
// ================================================================
// Main screen with 5 tabs:
//   1. 大屏扫描器 (Screen Scan)
//   2. 适配向导 (Migration Wizard)
//   3. 明文流量检测 (Network Scan)
//   4. Config 生成器 (Config Generator)
//   5. 合规报告 (Compliance Report)
//
// Architecture: MVI (Model-View-Intent)
//   Contract: Android17ComplianceContract
//   ViewModel: Android17ComplianceViewModel
//
// Design Reference: memory/agency/designs/PRD-206-Android-17-大屏适配-明文流量拦截-工具包.md
// ================================================================

// =============================================================
// Color Palette — 深色 Terminal 风格配色
// =============================================================
private object ComplianceColors {
    val Primary = Color(0xFF00C853)      // 安全绿
    val Warning = Color(0xFFFF6D00)       // 橙色警告
    val Danger = Color(0xFFFF1744)        // 红色危险
    val Background = Color(0xFF0D1117)    // 深黑背景
    val CardBackground = Color(0xFF161B22)
    val SurfaceVariant = Color(0xFF21262D)
    val TextPrimary = Color(0xFFE6EDF3)
    val TextSecondary = Color(0xFF8B949E)
    val CodeBackground = Color(0xFF0D1117)
    val TabIndicator = Color(0xFF00C853)
}

// =============================================================
// Tab Navigation — Tab 导航栏
// =============================================================
@Composable
private fun ComplianceTabNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = ComplianceTab.entries

    Column {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = ComplianceColors.Background,
            contentColor = ComplianceColors.Primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ComplianceColors.TabIndicator
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = tab.title,
                            color = if (selectedTab == index) ComplianceColors.Primary else ComplianceColors.TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                )
            }
        }
    }
}

// =============================================================
// Tab 1: Screen Scan / 大屏扫描器
// =============================================================
@Composable
private fun ScreenScanTab(
    state: Android17ComplianceState,
    onStartScan: () -> Unit,
    onTabSelected: (Int) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.screenScanState) {
        if (state.screenScanState == ScanState.SUCCESS) {
            snackbarHostState.showSnackbar("扫描完成")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.screenScanState != ScanState.SCANNING) {
                FloatingActionButton(
                    onClick = onStartScan,
                    containerColor = ComplianceColors.Primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "重新扫描",
                        tint = Color.White
                    )
                }
            }
        },
        containerColor = ComplianceColors.Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.screenScanState == ScanState.SCANNING -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🔍 正在扫描 AndroidManifest.xml...", color = ComplianceColors.TextPrimary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        LinearProgressIndicator(
                            progress = { state.screenScanProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = ComplianceColors.Primary,
                            trackColor = ComplianceColors.SurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${(state.screenScanProgress * 100).toInt()}%", color = ComplianceColors.TextSecondary, fontSize = 14.sp)
                    }
                }
                state.screenRisks.isEmpty() && state.screenScanState == ScanState.SUCCESS -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🛡️", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("未检测到风险配置 ✨", color = ComplianceColors.Primary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("AndroidManifest.xml 中没有发现方向锁定或可调整性限制", color = ComplianceColors.TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("发现 ${state.screenRisks.size} 个大屏适配风险", color = ComplianceColors.Warning, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }
                        items(state.screenRisks) { risk -> ScreenRiskCard(risk = risk) }
                    }
                }
            }
        }
    }
}

// =============================================================
// Screen Risk Card / 大屏风险卡片
// =============================================================
@Composable
private fun ScreenRiskCard(risk: ScreenRisk) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ComplianceColors.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(risk.riskLevel.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(risk.riskType, color = risk.riskLevel.color, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        Text(risk.riskLevel.labelZh, color = risk.riskLevel.color, fontSize = 12.sp)
                    }
                }
                Text("Line ${risk.line}", color = ComplianceColors.TextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(risk.description, color = ComplianceColors.TextPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().background(ComplianceColors.CodeBackground, RoundedCornerShape(8.dp)).padding(12.dp)
            ) {
                Text(risk.snippet, color = ComplianceColors.TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(risk.file, color = ComplianceColors.TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("💡 ${risk.suggestion}", color = ComplianceColors.Primary, fontSize = 12.sp)
            }
        }
    }
}

// =============================================================
// Tab 2: Migration Wizard / 适配向导
// =============================================================
@Composable
private fun MigrationWizardTab(
    state: Android17ComplianceState,
    onSetStep: (Int) -> Unit,
    onCompleteStep: (Int) -> Unit
) {
    val steps = state.migrationSteps
    val currentStep = state.migrationStep

    Column(modifier = Modifier.fillMaxSize().background(ComplianceColors.Background)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEachIndexed { index, step ->
                val isActive = index == currentStep
                val isCompleted = step.isCompleted
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isCompleted -> ComplianceColors.Primary
                                isActive -> ComplianceColors.Primary.copy(alpha = 0.2f)
                                else -> ComplianceColors.SurfaceVariant
                            }
                        )
                        .clickable { onSetStep(index) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCompleted) "✓ ${step.stepNumber}" else "${step.stepNumber}",
                        color = when {
                            isCompleted -> Color.White
                            isActive -> ComplianceColors.Primary
                            else -> ComplianceColors.TextSecondary
                        },
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }

        HorizontalDivider(color = ComplianceColors.SurfaceVariant)

        val step = steps.getOrNull(currentStep)
        if (step != null) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                Text("步骤 ${step.stepNumber}: ${step.title}", color = ComplianceColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(step.description, color = ComplianceColors.TextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Before → After", color = ComplianceColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("❌ 原代码", color = ComplianceColors.Danger, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                        Box(modifier = Modifier.fillMaxWidth().background(ComplianceColors.CodeBackground, RoundedCornerShape(8.dp)).padding(12.dp)) {
                            Text(step.originalCode, color = ComplianceColors.Danger.copy(alpha = 0.8f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("✅ 迁移后", color = ComplianceColors.Primary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                        Box(modifier = Modifier.fillMaxWidth().background(ComplianceColors.CodeBackground, RoundedCornerShape(8.dp)).padding(12.dp)) {
                            Text(step.migratedCode, color = ComplianceColors.Primary.copy(alpha = 0.8f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(
                        onClick = { if (currentStep > 0) onSetStep(currentStep - 1) },
                        enabled = currentStep > 0,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ComplianceColors.TextSecondary)
                    ) { Text("◀ 上一步") }
                    Button(
                        onClick = {
                            onCompleteStep(currentStep)
                            if (currentStep < steps.size - 1) onSetStep(currentStep + 1)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ComplianceColors.Primary)
                    ) { Text(if (currentStep == steps.size - 1) "✅ 完成" else "下一步 ▶") }
                }
            }
        }
    }
}

// =============================================================
// Tab 3: Network Scan / 明文流量检测
// =============================================================
@Composable
private fun NetworkScanTab(state: Android17ComplianceState, onStartScan: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.networkScanState) {
        if (state.networkScanState == ScanState.SUCCESS) snackbarHostState.showSnackbar("扫描完成")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.networkScanState != ScanState.SCANNING) {
                FloatingActionButton(onClick = onStartScan, containerColor = ComplianceColors.Primary) {
                    Icon(Icons.Default.Refresh, "重新扫描", tint = Color.White)
                }
            }
        },
        containerColor = ComplianceColors.Background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.networkScanState == ScanState.SCANNING -> {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("🌐 正在扫描明文流量...", color = ComplianceColors.TextPrimary, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        LinearProgressIndicator(progress = { state.networkScanProgress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = ComplianceColors.Primary, trackColor = ComplianceColors.SurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${(state.networkScanProgress * 100).toInt()}%", color = ComplianceColors.TextSecondary, fontSize = 14.sp)
                    }
                }
                state.networkRisks.isEmpty() && state.networkScanState == ScanState.SUCCESS -> {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("🛡️", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("未检测到明文流量 ✨", color = ComplianceColors.Primary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("所有网络请求均使用 HTTPS", color = ComplianceColors.TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item { Text("发现 ${state.networkRisks.size} 个明文流量风险", color = ComplianceColors.Warning, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp)) }
                        items(state.networkRisks) { risk -> NetworkRiskCard(risk = risk) }
                    }
                }
            }
        }
    }
}

// =============================================================
// Network Risk Card / 明文流量风险卡片
// =============================================================
@Composable
private fun NetworkRiskCard(risk: NetworkRisk) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ComplianceColors.CardBackground), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(risk.riskLevel.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(risk.url, color = risk.riskLevel.color, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1)
                        Text(risk.library, color = ComplianceColors.TextSecondary, fontSize = 12.sp)
                    }
                }
                Text("Line ${risk.line}", color = ComplianceColors.TextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().background(ComplianceColors.CodeBackground, RoundedCornerShape(8.dp)).padding(12.dp)) {
                Text(risk.snippet, color = ComplianceColors.TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, maxLines = 6)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(risk.file, color = ComplianceColors.TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("💡 ${risk.suggestion}", color = ComplianceColors.Primary, fontSize = 12.sp)
            }
        }
    }
}

// =============================================================
// Tab 4: Config Generator / Config 生成器
// =============================================================
@Composable
private fun ConfigGeneratorTab(
    state: Android17ComplianceState,
    onAddDomain: (String) -> Unit,
    onRemoveDomain: (String) -> Unit,
    onSetMode: (ConfigMode) -> Unit,
    onGenerate: () -> Unit,
    onCopy: () -> Unit
) {
    var domainInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(state.generatedConfig) {
        if (state.configDomains.isNotEmpty() && state.generatedConfig.isEmpty()) onGenerate()
    }

    Column(modifier = Modifier.fillMaxSize().background(ComplianceColors.Background).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("域名配置", color = ComplianceColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = domainInput,
                onValueChange = { domainInput = it },
                label = { Text("输入域名", color = ComplianceColors.TextSecondary) },
                placeholder = { Text("api.example.com", color = ComplianceColors.TextSecondary) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ComplianceColors.Primary,
                    unfocusedBorderColor = ComplianceColors.SurfaceVariant,
                    focusedTextColor = ComplianceColors.TextPrimary,
                    unfocusedTextColor = ComplianceColors.TextPrimary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { if (domainInput.isNotBlank()) { onAddDomain(domainInput.trim()); domainInput = "" } },
                modifier = Modifier.background(ComplianceColors.Primary, RoundedCornerShape(8.dp)).size(48.dp)
            ) { Icon(Icons.Default.Add, "添加域名", tint = Color.White) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.configDomains.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.configDomains.forEach { domain ->
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text(domain, color = ComplianceColors.TextPrimary) },
                        trailingIcon = { Icon(Icons.Default.Delete, "删除", tint = ComplianceColors.TextSecondary, modifier = Modifier.clickable { onRemoveDomain(domain) }) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            containerColor = ComplianceColors.SurfaceVariant,
                            selectedContainerColor = ComplianceColors.Primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = ComplianceColors.SurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Text("配置级别", color = ComplianceColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        ConfigMode.entries.forEach { mode ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onSetMode(mode) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = state.configMode == mode,
                    onClick = { onSetMode(mode) },
                    colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = ComplianceColors.Primary, unselectedColor = ComplianceColors.TextSecondary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(mode.label, color = ComplianceColors.TextPrimary, fontWeight = FontWeight.Medium)
                    Text(
                        if (mode == ConfigMode.DOMAIN_LEVEL) "只对指定域名禁用明文流量（推荐）" else "对所有域名禁用明文流量",
                        color = ComplianceColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ComplianceColors.Primary)) { Text("🔧 生成配置") }

        if (state.generatedConfig.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("network_security_config.xml", color = ComplianceColors.TextSecondary, fontSize = 12.sp)
                Row {
                    IconButton(onClick = onCopy) { Icon(Icons.Default.ContentCopy, "复制", tint = ComplianceColors.Primary) }
                    IconButton(onClick = {
                        val sendIntent = Intent().apply { action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_TEXT, state.generatedConfig); type = "text/plain" }
                        context.startActivity(Intent.createChooser(sendIntent, "分享配置"))
                    }) { Icon(Icons.Default.Share, "分享", tint = ComplianceColors.Primary) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(ComplianceColors.CodeBackground, RoundedCornerShape(12.dp)).padding(16.dp)) {
                Text(state.generatedConfig, color = ComplianceColors.Primary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.verticalScroll(rememberScrollState()))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("📋 将上述 XML 保存为 res/xml/network_security_config.xml，并在 AndroidManifest.xml 中引用", color = ComplianceColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

// =============================================================
// Tab 5: Compliance Report / 合规报告
// =============================================================
@Composable
private fun ComplianceReportTab(
    state: Android17ComplianceState,
    onRunCheck: () -> Unit,
    onExport: () -> Unit,
    onTabSelected: (Int) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ComplianceColors.Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Text("📊 Android 17 合规报告", color = ComplianceColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Android 17 (API 37) 强制适配检测", color = ComplianceColors.TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            val summary = state.complianceSummary
            val screenRisks = state.screenRisks
            val networkRisks = state.networkRisks

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ComplianceColors.CardBackground), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("检测状态", color = ComplianceColors.TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (screenRisks.isEmpty() && networkRisks.isEmpty()) {
                        Text("⚠️ 请先运行扫描", color = ComplianceColors.Warning, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRunCheck, colors = ButtonDefaults.buttonColors(containerColor = ComplianceColors.Primary)) { Text("🚀 运行完整检测") }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ReportStatCard(emoji = "🖥️", title = "大屏适配", total = screenRisks.size, fixed = summary?.screenRisksFixed ?: 0, color = ComplianceColors.Primary, onClick = { onTabSelected(0) })
                            ReportStatCard(emoji = "🌐", title = "明文流量", total = networkRisks.size, fixed = summary?.networkRisksFixed ?: 0, color = ComplianceColors.Warning, onClick = { onTabSelected(2) })
                        }
                    }
                }
            }

            if (screenRisks.isNotEmpty() || networkRisks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ComplianceColors.CardBackground), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("详细发现", color = ComplianceColors.TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (screenRisks.isNotEmpty()) {
                            Text("🖥️ 大屏适配 (${screenRisks.size} 项)", color = ComplianceColors.TextPrimary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            screenRisks.take(3).forEach { risk ->
                                Text("  ${risk.riskLevel.emoji} ${risk.riskType}: ${risk.description}", color = ComplianceColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                            if (screenRisks.size > 3) TextButton(onClick = { onTabSelected(0) }) { Text("查看全部 ${screenRisks.size} 项 →", color = ComplianceColors.Primary) }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (networkRisks.isNotEmpty()) {
                            Text("🌐 明文流量 (${networkRisks.size} 项)", color = ComplianceColors.TextPrimary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            networkRisks.take(3).forEach { risk ->
                                Text("  ${risk.riskLevel.emoji} ${risk.url}", color = ComplianceColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                            if (networkRisks.size > 3) TextButton(onClick = { onTabSelected(2) }) { Text("查看全部 ${networkRisks.size} 项 →", color = ComplianceColors.Primary) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onExport, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = ComplianceColors.Primary)) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("📤 导出合规报告")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRunCheck, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ComplianceColors.Primary)) { Text("🔄 重新检测") }
            }
        }
    }
}

// =============================================================
// Report Stat Card / 报告统计卡片
// =============================================================
@Composable
private fun ReportStatCard(emoji: String, title: String, total: Int, fixed: Int, color: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(ComplianceColors.SurfaceVariant).clickable(onClick = onClick).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = ComplianceColors.TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("$fixed / $total", color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

// =============================================================
// Main Screen Export / 主屏幕导出（供 MainScreen 调用）
// =============================================================
/**
 * Android17ComplianceScreen — 主屏幕
 *
 * @param viewModel ViewModel instance / ViewModel 实例
 * @param onUpdateTopBar TopBar configuration update callback / TopBar 更新回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Android17ComplianceScreen(
    viewModel: Android17ComplianceViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val tabTitles = ComplianceTab.entries.map { it.title }

    // Update top bar / 更新 TopBar
    LaunchedEffect(state.selectedTab) {
        onUpdateTopBar(TopBarConfig(title = "Android 17 合规工具"))
    }

    // Handle effects / 处理副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Android17ComplianceEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is Android17ComplianceEffect.ConfigCopied -> snackbarHostState.showSnackbar("配置已复制到剪贴板")
                is Android17ComplianceEffect.ReportExported -> { /* Share intent handled in ConfigGeneratorTab */ }
                is Android17ComplianceEffect.NavigateToTab -> viewModel.sendIntent(Android17ComplianceIntent.SelectTab(effect.tabIndex))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ComplianceColors.Background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ComplianceTabNavigation(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.sendIntent(Android17ComplianceIntent.SelectTab(it)) }
            )

            AnimatedContent(targetState = state.selectedTab, label = "tab_content") { tab ->
                when (tab) {
                    0 -> ScreenScanTab(state = state, onStartScan = { viewModel.sendIntent(Android17ComplianceIntent.StartScreenScan) }, onTabSelected = { viewModel.sendIntent(Android17ComplianceIntent.SelectTab(it)) })
                    1 -> MigrationWizardTab(state = state, onSetStep = { viewModel.sendIntent(Android17ComplianceIntent.SetMigrationStep(it)) }, onCompleteStep = { viewModel.sendIntent(Android17ComplianceIntent.CompleteMigrationStep(it)) })
                    2 -> NetworkScanTab(state = state, onStartScan = { viewModel.sendIntent(Android17ComplianceIntent.StartNetworkScan) })
                    3 -> ConfigGeneratorTab(state = state, onAddDomain = { viewModel.sendIntent(Android17ComplianceIntent.AddDomain(it)) }, onRemoveDomain = { viewModel.sendIntent(Android17ComplianceIntent.RemoveDomain(it)) }, onSetMode = { viewModel.sendIntent(Android17ComplianceIntent.SetConfigMode(it)) }, onGenerate = { viewModel.sendIntent(Android17ComplianceIntent.GenerateConfig) }, onCopy = { viewModel.sendIntent(Android17ComplianceIntent.CopyConfig) })
                    4 -> ComplianceReportTab(state = state, onRunCheck = { viewModel.sendIntent(Android17ComplianceIntent.RunFullComplianceCheck) }, onExport = { viewModel.sendIntent(Android17ComplianceIntent.ExportReport) }, onTabSelected = { viewModel.sendIntent(Android17ComplianceIntent.SelectTab(it)) })
                    else -> Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
