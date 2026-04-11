package com.mvi.kenny.feature.devverification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.devverification.components.AppVerificationListItem
import com.mvi.kenny.feature.devverification.components.ComplianceScoreCard
import com.mvi.kenny.feature.devverification.components.DeadlineCountdownChip
import kotlinx.coroutines.flow.collectLatest

/**
 * Sub-screen navigation state for internal routing within the Dev Verification feature.
 * 内部子屏幕导航状态，用于在功能模块内切换仪表盘/向导/MDM/设置
 */
private sealed class DevVerificationSubScreen {
    object Dashboard : DevVerificationSubScreen()
    object Wizard : DevVerificationSubScreen()
    object MDM : DevVerificationSubScreen()
    object Settings : DevVerificationSubScreen()
}

/**
 * PRD-078 | Android Developer Verification Compliance Toolkit
 * ComplianceDashboardScreen — Main entry point showing overall compliance health
 *
 * Design: Section 3.1 — ComplianceDashboardScreen
 * MVI: DashboardIntent / ComplianceDashboardState / DashboardEffect
 * Bilingual comments: CN + EN
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceDashboardScreen(
    viewModel: DevVerificationViewModel = viewModel(),
    onNavigateToWizard: () -> Unit = {},
    onNavigateToMDM: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf<DevVerificationSubScreen>(DevVerificationSubScreen.Dashboard) }
    val state by viewModel.dashboardState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect one-time effects / 收集一次性副作用
    LaunchedEffect(Unit) {
        viewModel.dashboardEffects.collectLatest { effect ->
            when (effect) {
                is DashboardEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is DashboardEffect.NavigateToWizard -> currentScreen = DevVerificationSubScreen.Wizard
                is DashboardEffect.NavigateToMDM -> currentScreen = DevVerificationSubScreen.MDM
                is DashboardEffect.NavigateToSettings -> currentScreen = DevVerificationSubScreen.Settings
                is DashboardEffect.OpenExternalUrl -> { /* Handle via Intent */ }
                is DashboardEffect.ExportReport -> snackbarHostState.showSnackbar("Report exported: ${effect.filePath}")
            }
        }
    }

    // Internal navigation: switch sub-screen based on state
    // 内部导航：根据状态切换子屏幕
    when (currentScreen) {
        DevVerificationSubScreen.Dashboard -> { /* dashboard content below */ }
        DevVerificationSubScreen.Wizard -> {
            VerificationWizardScreen(
                viewModel = viewModel,
                state = viewModel.wizardState.value,
                onIntent = viewModel::processWizardIntent,
                onNavigateBack = { currentScreen = DevVerificationSubScreen.Dashboard }
            )
            return
        }
        DevVerificationSubScreen.MDM -> {
            MDMComplianceScreen(
                viewModel = viewModel,
                state = viewModel.mdmState.value,
                onIntent = viewModel::processMDMIntent,
                onNavigateBack = { currentScreen = DevVerificationSubScreen.Dashboard }
            )
            return
        }
        DevVerificationSubScreen.Settings -> {
            SettingsScreen(
                viewModel = viewModel,
                state = viewModel.settingsState.value,
                onIntent = viewModel::processSettingsIntent,
                onNavigateBack = { currentScreen = DevVerificationSubScreen.Dashboard }
            )
            return
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dev Verification",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Settings button / 设置按钮
                    IconButton(onClick = { currentScreen = DevVerificationSubScreen.Settings }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ============ Compliance Score Card ============
            // ============ 合规评分卡片 ============
            item {
                ComplianceScoreCard(
                    score = state.overallScore,
                    totalApps = state.apps.size,
                    verifiedApps = state.apps.count { it.isVerified }
                )
            }

            // ============ Deadline Countdown ============
            // ============ 截止日期倒计时 ============
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Region.entries.take(2).forEach { region ->
                        DeadlineCountdownChip(
                            region = region,
                            daysRemaining = viewModel.daysUntilDeadline(region),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Region.entries.drop(2).forEach { region ->
                        DeadlineCountdownChip(
                            region = region,
                            daysRemaining = viewModel.daysUntilDeadline(region),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ============ Scan Progress Bar ============
            // ============ 扫描进度条 ============
            if (state.isScanning) {
                item {
                    Column {
                        Text(
                            text = "Scanning apps...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { state.scanProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            // ============ Quick Actions ============
            // ============ 快捷操作区 ============
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.processDashboardIntent(DashboardIntent.RefreshScan) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isScanning
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Scan")
                    }
                    Button(
                        onClick = { currentScreen = DevVerificationSubScreen.Wizard },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Verify")
                    }
                }
            }

            // ============ App List Header ============
            // ============ App 列表标题 ============
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Apps (${state.apps.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.apps.isNotEmpty()) {
                        TextButton(onClick = { viewModel.processDashboardIntent(DashboardIntent.GenerateReport) }) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Report")
                        }
                    }
                }
            }

            // ============ App List ============
            // ============ App 列表 ============
            if (state.apps.isEmpty() && !state.isScanning) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No apps scanned yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Scan' to detect apps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(state.apps, key = { it.packageName }) { app ->
                AppVerificationListItem(
                    app = app,
                    onSelect = { viewModel.processDashboardIntent(DashboardIntent.SelectApp(app.packageName)) },
                    onBatchVerify = { viewModel.processDashboardIntent(DashboardIntent.BatchVerify(listOf(app.packageName))) }
                )
            }

            // ============ MDM Compliance Shortcut ============
            // ============ MDM 合规快捷入口 ============
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedCard(
                    onClick = { currentScreen = DevVerificationSubScreen.MDM },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ManageAccounts,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "MDM Compliance",
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Enterprise app distribution check",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }
    }
}
