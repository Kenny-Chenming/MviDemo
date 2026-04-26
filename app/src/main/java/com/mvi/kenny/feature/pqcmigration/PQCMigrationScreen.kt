package com.mvi.kenny.feature.pqcmigration

// ============================================================
// PQCMigrationScreen — Android 17 PQC 迁移工具包主界面
// PRD-165 | Android 17 Post-Quantum Cryptography 迁移工具包
// ============================================================
/**
 * PQC 迁移工具包主界面 Composable。
 * PQC Migration Toolkit main screen composable.
 *
 * 架构说明：
 * - 5 个主要 Tab：Overview / KeyGen / KeyMigration / TLSCompliance / AppSigning
 * - 每个 Tab 对应一个子模块状态和独立 UI 区域
 * - 共享 PQCMigrationViewModel 提供统一状态管理
 *
 * 视觉设计规范（来自设计文档）：
 * - Primary: #6B4EFF（量子紫）
 * - Secondary: #00D9FF（科技蓝）
 * - Background: #0D1117（深空黑）
 * - Surface: #161B22
 * - Error: #FF6B6B
 * - Success: #4ADE80
 * - Warning: #FBBF24
 * - OnPrimary: #FFFFFF
 * - OnBackground: #E6EDF3
 *
 * @param state 页面状态（来自 ViewModel）/ Page state from ViewModel
 * @param keyGenState KeyGen 子模块状态 / KeyGen sub-module state
 * @param keyMigrationState KeyMigration 子模块状态 / KeyMigration sub-module state
 * @param tlsComplianceState TLSCompliance 子模块状态 / TLSCompliance sub-module state
 * @param appSigningState AppSigning 子模块状态 / AppSigning sub-module state
 * @param onIntent 发送 Intent 到 ViewModel / Send intent to ViewModel
 * @param onNavigateToSubmodule 导航到子模块 / Navigate to sub-module
 *
 * @see PQCMigrationViewModel
 * @see PQCMigrationContract
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow

// ============================================================
// Color Palette — PQC Theme Colors
// ============================================================
/**
 * PQC 工具包专用配色方案
 * PQC Toolkit dedicated color scheme
 *
 * From design document / 设计文档来源：
 * - Primary: Quantum Purple (#6B4EFF)
 * - Secondary: Tech Blue (#00D9FF)
 * - Background: Deep Space Black (#0D1117)
 * - Surface: Dark Surface (#161B22)
 * - Error: Alert Red (#FF6B6B)
 * - Success: Compliance Green (#4ADE80)
 * - Warning: Caution Yellow (#FBBF24)
 */
object PQCColors {
    val QuantumPurple = Color(0xFF6B4EFF)
    val TechBlue = Color(0xFF00D9FF)
    val DeepSpaceBlack = Color(0xFF0D1117)
    val DarkSurface = Color(0xFF161B22)
    val AlertRed = Color(0xFFFF6B6B)
    val ComplianceGreen = Color(0xFF4ADE80)
    val CautionYellow = Color(0xFFFBBF24)
    val OnBackground = Color(0xFFE6EDF3)
    val MutedGray = Color(0xFF6B7280)
    val CardBorder = Color(0xFF30363D)
}

// ============================================================
// Main Entry Point — PQCMigrationScreen
// ============================================================

/**
 * PQC 迁移工具包主界面
 * PQC Migration Toolkit main screen entry point.
 *
 * @param state 全局状态 / Global state
 * @param keyGenState KeyGen 子模块状态 / KeyGen sub-module state
 * @param keyMigrationState KeyMigration 子模块状态 / KeyMigration sub-module state
 * @param tlsComplianceState TLSCompliance 子模块状态 / TLSCompliance sub-module state
 * @param appSigningState AppSigning 子模块状态 / AppSigning sub-module state
 * @param effect 副作用流 / Effect flow
 * @param onIntent 发送 Intent / Send intent
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PQCMigrationScreen(
    state: PQCMigrationState,
    keyGenState: KeyGenState,
    keyMigrationState: KeyMigrationState,
    tlsComplianceState: TLSComplianceState,
    appSigningState: AppSigningState,
    effect: Flow<PQCMigrationEffect>,
    onIntent: (PQCMigrationIntent) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    // Handle effects / 处理副作用
    LaunchedEffect(effect) {
        effect.collect { e ->
            when (e) {
                is PQCMigrationEffect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(e.text))
                }
                is PQCMigrationEffect.ShowError -> {
                    snackbarHostState.showSnackbar(e.message)
                }
                is PQCMigrationEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(e.message)
                }
                is PQCMigrationEffect.ScanComplete -> {
                    snackbarHostState.showSnackbar("扫描完成 / Scan complete")
                }
                is PQCMigrationEffect.ReportExported -> {
                    snackbarHostState.showSnackbar("报告已导出: ${e.filePath}")
                }
                is PQCMigrationEffect.NavigateToModule -> {
                    onIntent(PQCMigrationIntent.SelectTab(e.module))
                }
            }
        }
    }

    // Tab indices / Tab 索引
    val tabIndex = remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        "概览" to "Overview",
        "密钥生成" to "KeyGen",
        "密钥迁移" to "Migration",
        "TLS检测" to "TLS",
        "签名合规" to "Signing"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PQCColors.DeepSpaceBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // —————————————————————————————————————————————
            // Tab Row — 顶部 Tab 导航
            // —————————————————————————————————————————————
            ScrollableTabRow(
                selectedTabIndex = tabIndex.intValue,
                containerColor = PQCColors.DarkSurface,
                contentColor = PQCColors.OnBackground,
                edgePadding = 12.dp,
                divider = {
                    androidx.compose.material3.HorizontalDivider(color = PQCColors.CardBorder)
                }
            ) {
                tabTitles.forEachIndexed { index, (titleCn, titleEn) ->
                    Tab(
                        selected = tabIndex.intValue == index,
                        onClick = {
                            tabIndex.intValue = index
                            val module = when (index) {
                                0 -> PQCToolModule.Overview
                                1 -> PQCToolModule.KeyGen
                                2 -> PQCToolModule.KeyMigration
                                3 -> PQCToolModule.TLSCompliance
                                4 -> PQCToolModule.AppSigning
                                else -> PQCToolModule.Overview
                            }
                            onIntent(PQCMigrationIntent.SelectTab(module))
                        },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = titleCn,
                                    fontSize = 13.sp,
                                    fontWeight = if (tabIndex.intValue == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (tabIndex.intValue == index) PQCColors.QuantumPurple else PQCColors.MutedGray
                                )
                                Text(
                                    text = titleEn,
                                    fontSize = 10.sp,
                                    color = PQCColors.MutedGray
                                )
                            }
                        }
                    )
                }
            }

            // —————————————————————————————————————————————
            // Tab Content — 各 Tab 内容区
            // —————————————————————————————————————————————
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PQCColors.DeepSpaceBlack)
            ) {
                when (tabIndex.intValue) {
                    0 -> OverviewTabContent(
                        state = state,
                        onIntent = onIntent
                    )
                    1 -> KeyGenTabContent(
                        state = keyGenState,
                        onIntent = onIntent
                    )
                    2 -> KeyMigrationTabContent(
                        state = keyMigrationState,
                        onIntent = onIntent
                    )
                    3 -> TLSComplianceTabContent(
                        state = tlsComplianceState,
                        onIntent = onIntent
                    )
                    4 -> AppSigningTabContent(
                        state = appSigningState,
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}

// ============================================================
// Tab 1: Overview — 首页概览
// ============================================================

/**
 * Overview Tab 内容区
 * Overview tab content area showing PQC migration dashboard.
 *
 * @param state Overview 状态 / Overview state
 * @param onIntent 发送 Intent / Send intent
 */
@Composable
private fun OverviewTabContent(
    state: PQCMigrationState,
    onIntent: (PQCMigrationIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // —————————————————————————————————————————————
        // Announcement Banner — 公告横幅
        // —————————————————————————————————————————————
        item {
            AnnouncementBanner(announcement = state.announcement)
        }

        // —————————————————————————————————————————————
        // Countdown Card — PQC 截止倒计时卡片
        // —————————————————————————————————————————————
        item {
            CountdownCard(countdownDays = state.countdownDays)
        }

        // —————————————————————————————————————————————
        // Tool Entry Cards — 工具入口卡片
        // —————————————————————————————————————————————
        item {
            Text(
                text = "工具模块 / Tool Modules",
                style = MaterialTheme.typography.titleMedium,
                color = PQCColors.OnBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(PQCToolModule.entries.filter { it != PQCToolModule.Overview }) { module ->
            ToolEntryCard(
                module = module,
                onClick = { onIntent(PQCMigrationIntent.SelectTab(module)) }
            )
        }
    }
}

/**
 * 公告横幅 / Announcement banner
 *
 * @param announcement 公告内容 / Announcement content
 */
@Composable
private fun AnnouncementBanner(announcement: String?) {
    announcement ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PQCColors.QuantumPurple.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = PQCColors.QuantumPurple,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = announcement,
                style = MaterialTheme.typography.bodyMedium,
                color = PQCColors.OnBackground
            )
        }
    }
}

/**
 * 倒计时卡片 / Countdown card showing days until PQC deadline
 *
 * @param countdownDays 距 2029 年天数 / Days until 2029
 */
@Composable
private fun CountdownCard(countdownDays: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🛡️ PQC 迁移截止倒计时",
                style = MaterialTheme.typography.titleMedium,
                color = PQCColors.OnBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = PQCColors.CautionYellow,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$countdownDays",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = PQCColors.CautionYellow
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "天 / days",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PQCColors.MutedGray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Google PQ 迁移截止日期: 2029 年 / Google PQ Migration Deadline: 2029",
                style = MaterialTheme.typography.bodySmall,
                color = PQCColors.MutedGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 工具入口卡片 / Tool entry card
 *
 * @param module 工具模块 / Tool module
 * @param onClick 点击回调 / Click callback
 */
@Composable
private fun ToolEntryCard(
    module: PQCToolModule,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / 图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        PQCColors.QuantumPurple.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = module.iconEmoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.titleCn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PQCColors.OnBackground
                )
                Text(
                    text = module.titleEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = PQCColors.MutedGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PQCColors.MutedGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "启动 / Launch",
                tint = PQCColors.QuantumPurple
            )
        }
    }
}

// ============================================================
// Tab 2: KeyGen — PQ 密钥生成
// ============================================================

/**
 * KeyGen Tab 内容区
 * KeyGen tab content area for generating PQC key code.
 *
 * @param state KeyGen 状态 / KeyGen state
 * @param onIntent 发送 Intent / Send intent
 */
@Composable
private fun KeyGenTabContent(
    state: KeyGenState,
    onIntent: (PQCMigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // —————————————————————————————————————————————
        // Header — 标题区
        // —————————————————————————————————————————————
        Text(
            text = "🗝️ Android Keystore PQ 密钥生成器",
            style = MaterialTheme.typography.titleLarge,
            color = PQCColors.OnBackground
        )
        Text(
            text = "选择算法和 API 级别，生成 KeyGenParameterSpec 完整代码",
            style = MaterialTheme.typography.bodyMedium,
            color = PQCColors.MutedGray
        )

        // —————————————————————————————————————————————
        // Algorithm Selection — 算法选择
        // —————————————————————————————————————————————
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "选择 PQ 算法 / Select PQC Algorithm",
                    style = MaterialTheme.typography.titleSmall,
                    color = PQCColors.OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                PqcAlgorithm.entries.forEach { algorithm ->
                    AlgorithmOption(
                        algorithm = algorithm,
                        isSelected = state.selectedAlgorithm == algorithm,
                        onSelect = { onIntent(PQCMigrationIntent.SelectAlgorithm(algorithm)) }
                    )
                }
            }
        }

        // —————————————————————————————————————————————
        // API Level Selection — API 级别选择
        // —————————————————————————————————————————————
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "API 级别 / API Level",
                    style = MaterialTheme.typography.titleSmall,
                    color = PQCColors.OnBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ML-KEM 在 Android Keystore 的最低要求: API 38 (Android 14)",
                    style = MaterialTheme.typography.bodySmall,
                    color = PQCColors.CautionYellow
                )
                Spacer(modifier = Modifier.height(12.dp))

                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("API ${state.apiLevel} / Android ${state.apiLevel - 27}")
                        Spacer(modifier = Modifier.weight(1f))
                        Text("▼", color = PQCColors.MutedGray)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf(38, 39, 40).forEach { level ->
                            DropdownMenuItem(
                                text = { Text("API $level / Android ${level - 27}") },
                                onClick = {
                                    onIntent(PQCMigrationIntent.SetApiLevel(level))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // —————————————————————————————————————————————
        // Generate Button — 生成按钮
        // —————————————————————————————————————————————
        Button(
            onClick = { onIntent(PQCMigrationIntent.GenerateKeyGenCode) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isGenerating,
            colors = ButtonDefaults.buttonColors(containerColor = PQCColors.QuantumPurple)
        ) {
            if (state.isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (state.isGenerating) "生成中... / Generating..." else "生成代码 / Generate Code"
            )
        }

        // —————————————————————————————————————————————
        // Generated Code — 生成的代码
        // —————————————————————————————————————————————
        AnimatedVisibility(
            visible = state.generatedCode != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            state.generatedCode?.let { code ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "生成的代码 / Generated Code",
                                style = MaterialTheme.typography.titleSmall,
                                color = PQCColors.OnBackground
                            )
                            Row {
                                IconButton(
                                    onClick = { onIntent(PQCMigrationIntent.CopyGeneratedCode) }
                                ) {
                                    Icon(
                                        imageVector = if (state.copiedToClipboard) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                        contentDescription = "复制 / Copy",
                                        tint = if (state.copiedToClipboard) PQCColors.ComplianceGreen else PQCColors.MutedGray
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PQCColors.DeepSpaceBlack, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = PQCColors.TechBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 算法选项 / Algorithm option radio button
 *
 * @param algorithm PQ 算法 / PQC algorithm
 * @param isSelected 是否选中 / Whether selected
 * @param onSelect 选择回调 / Select callback
 */
@Composable
private fun AlgorithmOption(
    algorithm: PqcAlgorithm,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                selectedColor = PQCColors.QuantumPurple
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = algorithm.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = PQCColors.OnBackground
            )
            Text(
                text = algorithm.description,
                style = MaterialTheme.typography.bodySmall,
                color = PQCColors.MutedGray
            )
        }
        Text(
            text = "API ${algorithm.apiLevelMin}+",
            style = MaterialTheme.typography.labelSmall,
            color = PQCColors.QuantumPurple
        )
    }
}

// ============================================================
// Tab 3: KeyMigration — 密钥迁移扫描
// ============================================================

/**
 * KeyMigration Tab 内容区
 * KeyMigration tab content area for scanning and migrating keys.
 *
 * @param state KeyMigration 状态 / KeyMigration state
 * @param onIntent 发送 Intent / Send intent
 */
@Composable
private fun KeyMigrationTabContent(
    state: KeyMigrationState,
    onIntent: (PQCMigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // —————————————————————————————————————————————
        // Header — 标题区
        // —————————————————————————————————————————————
        Text(
            text = "🔄 密钥迁移扫描器",
            style = MaterialTheme.typography.titleLarge,
            color = PQCColors.OnBackground
        )
        Text(
            text = "检测 App 中 RSA/EC 密钥用法，输出迁移到 PQ 算法的 Diff",
            style = MaterialTheme.typography.bodyMedium,
            color = PQCColors.MutedGray
        )

        // —————————————————————————————————————————————
        // Project Path Input — 项目路径输入
        // —————————————————————————————————————————————
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "项目路径 / Project Path",
                    style = MaterialTheme.typography.titleSmall,
                    color = PQCColors.OnBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.projectPath,
                    onValueChange = { onIntent(PQCMigrationIntent.SetProjectPath(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("例如: /path/to/your/project") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = PQCColors.MutedGray
                        )
                    },
                    enabled = !state.isScanning,
                    singleLine = true
                )
            }
        }

        // —————————————————————————————————————————————
        // Scan Button — 扫描按钮
        // —————————————————————————————————————————————
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onIntent(PQCMigrationIntent.StartKeyMigrationScan) },
                modifier = Modifier.weight(1f),
                enabled = !state.isScanning && state.projectPath.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PQCColors.QuantumPurple)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始扫描 / Start Scan")
            }

            if (state.isScanning) {
                OutlinedButton(
                    onClick = { onIntent(PQCMigrationIntent.CancelScan) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PQCColors.AlertRed)
                ) {
                    Text("取消 / Cancel")
                }
            }
        }

        // —————————————————————————————————————————————
        // Scan Progress — 扫描进度
        // —————————————————————————————————————————————
        AnimatedVisibility(visible = state.isScanning) {
            ScanProgressCard(
                phase = state.scanPhase,
                progress = state.progress
            )
        }

        // —————————————————————————————————————————————
        // Findings List — 发现列表
        // —————————————————————————————————————————————
        if (state.findings.isNotEmpty()) {
            Text(
                text = "发现 ${state.findings.size} 个不合规密钥用法 / Found ${state.findings.size} non-compliant key usages",
                style = MaterialTheme.typography.titleSmall,
                color = PQCColors.AlertRed
            )

            state.findings.forEach { finding ->
                FindingCard(
                    finding = finding,
                    onClick = { onIntent(PQCMigrationIntent.SelectFinding(finding)) }
                )
            }
        }

        // —————————————————————————————————————————————
        // Migration Diff — 迁移 Diff
        // —————————————————————————————————————————————
        if (state.migrationDiff != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📄 迁移 Diff / Migration Diff",
                            style = MaterialTheme.typography.titleSmall,
                            color = PQCColors.OnBackground
                        )
                        OutlinedButton(
                            onClick = { onIntent(PQCMigrationIntent.ExportMigrationReport) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导出 / Export")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(PQCColors.DeepSpaceBlack, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = state.migrationDiff,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = PQCColors.TechBlue
                        )
                    }
                }
            }
        }
    }
}

/**
 * 扫描进度卡片 / Scan progress card
 *
 * @param phase 当前阶段 / Current phase
 * @param progress 进度值 / Progress value
 */
@Composable
private fun ScanProgressCard(
    phase: ScanPhase,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "扫描中... / Scanning...",
                    style = MaterialTheme.typography.titleSmall,
                    color = PQCColors.QuantumPurple
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = PQCColors.QuantumPurple
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PQCColors.QuantumPurple,
                trackColor = PQCColors.DeepSpaceBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = phase.displayNameCn,
                style = MaterialTheme.typography.bodySmall,
                color = PQCColors.MutedGray
            )
        }
    }
}

/**
 * 发现项卡片 / Finding card
 *
 * @param finding 发现项 / Finding
 * @param onClick 点击回调 / Click callback
 */
@Composable
private fun FindingCard(
    finding: NonCompliantKeyFinding,
    onClick: () -> Unit
) {
    val severityColor = when (finding.severity) {
        "HIGH" -> PQCColors.AlertRed
        "MEDIUM" -> PQCColors.CautionYellow
        else -> PQCColors.MutedGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(severityColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = finding.keyType,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = finding.severity,
                        style = MaterialTheme.typography.labelSmall,
                        color = severityColor
                    )
                }
                Text(
                    text = "L${finding.lineNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = PQCColors.MutedGray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = finding.filePath,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = PQCColors.TechBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = finding.usageContext,
                style = MaterialTheme.typography.bodySmall,
                color = PQCColors.MutedGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "建议: ${finding.suggestedReplacement}",
                style = MaterialTheme.typography.bodySmall,
                color = PQCColors.ComplianceGreen
            )
        }
    }
}

// ============================================================
// Tab 4: TLSCompliance — TLS 合规检测
// ============================================================

/**
 * TLSCompliance Tab 内容区
 * TLSCompliance tab content area for TLS PQ compliance checking.
 *
 * @param state TLSCompliance 状态 / TLSCompliance state
 * @param onIntent 发送 Intent / Send intent
 */
@Composable
private fun TLSComplianceTabContent(
    state: TLSComplianceState,
    onIntent: (PQCMigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // —————————————————————————————————————————————
        // Header — 标题区
        // —————————————————————————————————————————————
        Text(
            text = "🔒 TLS Hybrid Mode 合规检测",
            style = MaterialTheme.typography.titleLarge,
            color = PQCColors.OnBackground
        )
        Text(
            text = "检测 App 的 TLS 配置是否启用 hybrid PQ 模式",
            style = MaterialTheme.typography.bodyMedium,
            color = PQCColors.MutedGray
        )

        // —————————————————————————————————————————————
        // Input Mode Selection — 输入模式选择
        // —————————————————————————————————————————————
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "输入模式 / Input Mode",
                    style = MaterialTheme.typography.titleSmall,
                    color = PQCColors.OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = state.inputMode == "SOURCE_PATH",
                        onClick = { onIntent(PQCMigrationIntent.SetTLSInputMode("SOURCE_PATH")) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = PQCColors.QuantumPurple.copy(alpha = 0.2f),
                            activeContentColor = PQCColors.QuantumPurple
                        )
                    ) {
                        Text("源码路径 / Source")
                    }
                    SegmentedButton(
                        selected = state.inputMode == "APK",
                        onClick = { onIntent(PQCMigrationIntent.SetTLSInputMode("APK")) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = PQCColors.QuantumPurple.copy(alpha = 0.2f),
                            activeContentColor = PQCColors.QuantumPurple
                        )
                    ) {
                        Text("APK / APK")
                    }
                }
            }
        }

        // —————————————————————————————————————————————
        // Input Path — 输入路径
        // —————————————————————————————————————————————
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (state.inputMode == "APK") "APK 路径 / APK Path" else "源码路径 / Source Path",
                    style = MaterialTheme.typography.titleSmall,
                    color = PQCColors.OnBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.inputPath,
                    onValueChange = { onIntent(PQCMigrationIntent.SetTLSInputPath(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (state.inputMode == "APK") "/path/to/app.apk"
                            else "/path/to/project"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = PQCColors.MutedGray
                        )
                    },
                    enabled = !state.isScanning,
                    singleLine = true
                )
            }
        }

        // —————————————————————————————————————————————
        // Scan Button — 扫描按钮
        // —————————————————————————————————————————————
        Button(
            onClick = { onIntent(PQCMigrationIntent.StartTLSScan) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isScanning && state.inputPath.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = PQCColors.QuantumPurple)
        ) {
            if (state.isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (state.isScanning) "检测中... / Scanning..." else "开始检测 / Start Scan"
            )
        }

        // —————————————————————————————————————————————
        // Compliance Score Card — 合规评分卡片
        // —————————————————————————————————————————————
        if (state.complianceScore != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TLS 合规评分 / TLS Compliance Score",
                        style = MaterialTheme.typography.titleSmall,
                        color = PQCColors.OnBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                Color(state.complianceLevel.color).copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.complianceScore}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(state.complianceLevel.color)
                            )
                            Text(
                                text = state.complianceLevel.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(state.complianceLevel.color)
                            )
                        }
                    }
                }
            }
        }

        // —————————————————————————————————————————————
        // TLS Findings — TLS 发现项
        // —————————————————————————————————————————————
        if (state.findings.isNotEmpty()) {
            Text(
                text = "发现 ${state.findings.size} 个合规问题 / Found ${state.findings.size} compliance issues",
                style = MaterialTheme.typography.titleSmall,
                color = PQCColors.AlertRed
            )

            state.findings.forEach { finding ->
                TLSFindingCard(finding = finding)
            }
        }
    }
}

/**
 * TLS 发现项卡片 / TLS finding card
 *
 * @param finding TLS 发现项 / TLS finding
 */
@Composable
private fun TLSFindingCard(finding: TLSFinding) {
    val severityColor = when (finding.severity) {
        "HIGH" -> PQCColors.AlertRed
        "MEDIUM" -> PQCColors.CautionYellow
        else -> PQCColors.MutedGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = finding.domain,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PQCColors.TechBlue
                )
                Box(
                    modifier = Modifier
                        .background(severityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = finding.severity,
                        style = MaterialTheme.typography.labelSmall,
                        color = severityColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = finding.issue,
                style = MaterialTheme.typography.bodyMedium,
                color = PQCColors.AlertRed
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "当前配置 / Current: ${finding.currentConfig}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = PQCColors.MutedGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "建议 / Suggested: ${finding.suggestedFix}",
                style = MaterialTheme.typography.bodySmall,
                color = PQCColors.ComplianceGreen
            )
        }
    }
}

// ============================================================
// Tab 5: AppSigning — App Signing PQ 合规
// ============================================================

/**
 * AppSigning Tab 内容区
 * AppSigning tab content area for APK signature PQ compliance checking.
 *
 * @param state AppSigning 状态 / AppSigning state
 * @param onIntent 发送 Intent / Send intent
 */
@Composable
private fun AppSigningTabContent(
    state: AppSigningState,
    onIntent: (PQCMigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // —————————————————————————————————————————————
        // Header — 标题区
        // —————————————————————————————————————————————
        Text(
            text = "📜 App Signing PQ 合规检测",
            style = MaterialTheme.typography.titleLarge,
            color = PQCColors.OnBackground
        )
        Text(
            text = "验证 App 签名是否满足 Google Play PQ 要求",
            style = MaterialTheme.typography.bodyMedium,
            color = PQCColors.MutedGray
        )

        // —————————————————————————————————————————————
        // APK Path Input — APK 路径输入
        // —————————————————————————————————————————————
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "APK 路径 / APK Path",
                    style = MaterialTheme.typography.titleSmall,
                    color = PQCColors.OnBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.apkPath,
                    onValueChange = { onIntent(PQCMigrationIntent.SetApkPath(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("/path/to/app-release.apk") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = PQCColors.MutedGray
                        )
                    },
                    enabled = !state.isAnalyzing,
                    singleLine = true
                )
            }
        }

        // —————————————————————————————————————————————
        // Analyze Button — 分析按钮
        // —————————————————————————————————————————————
        Button(
            onClick = { onIntent(PQCMigrationIntent.AnalyzeAppSigning) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isAnalyzing && state.apkPath.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = PQCColors.QuantumPurple)
        ) {
            if (state.isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (state.isAnalyzing) "分析中... / Analyzing..." else "分析签名 / Analyze Signature"
            )
        }

        // —————————————————————————————————————————————
        // Analysis Result — 分析结果
        // —————————————————————————————————————————————
        if (state.signatureScheme != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "签名方案 / Signature Scheme",
                            style = MaterialTheme.typography.titleSmall,
                            color = PQCColors.OnBackground
                        )
                        Icon(
                            imageVector = if (state.isPQCCompliant == true)
                                Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (state.isPQCCompliant == true)
                                PQCColors.ComplianceGreen else PQCColors.AlertRed,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(
                        label = "方案 / Scheme",
                        value = state.signatureScheme ?: "Unknown"
                    )
                    DetailRow(
                        label = "版本 / Version",
                        value = state.signatureVersion?.toString() ?: "Unknown"
                    )
                    DetailRow(
                        label = "PQ 合规 / PQ Compliant",
                        value = when (state.isPQCCompliant) {
                            true -> "✅ 是 / Yes"
                            false -> "❌ 否 / No"
                            else -> "未知 / Unknown"
                        },
                        valueColor = when (state.isPQCCompliant) {
                            true -> PQCColors.ComplianceGreen
                            false -> PQCColors.AlertRed
                            else -> PQCColors.MutedGray
                        }
                    )
                }
            }
        }

        // —————————————————————————————————————————————
        // Compliance Details — 合规详情
        // —————————————————————————————————————————————
        if (state.complianceDetails != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PQCColors.DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📋 合规详情 / Compliance Details",
                        style = MaterialTheme.typography.titleSmall,
                        color = PQCColors.OnBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PQCColors.DeepSpaceBlack, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = state.complianceDetails,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = PQCColors.OnBackground
                        )
                    }
                }
            }
        }

        // —————————————————————————————————————————————
        // Export Button — 导出按钮
        // —————————————————————————————————————————————
        if (state.signatureScheme != null) {
            OutlinedButton(
                onClick = { onIntent(PQCMigrationIntent.ExportSigningReport) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("导出报告 / Export Report")
            }
        }
    }
}

/**
 * 详情行 / Detail row for displaying label-value pairs
 *
 * @param label 标签 / Label
 * @param value 值 / Value
 * @param valueColor 值颜色（可选）/ Value color (optional)
 */
@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = PQCColors.OnBackground
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = PQCColors.MutedGray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
