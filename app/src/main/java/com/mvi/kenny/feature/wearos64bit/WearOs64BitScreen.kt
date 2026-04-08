package com.mvi.kenny.feature.wearos64bit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.base.TopBarAction
import kotlinx.coroutines.launch

// =============================================================
// WearOs64BitScreen — Wear OS 64位合规工具主界面
// =============================================================
/**
 * Wear OS 64-Bit Compliance Tool Main Screen / Wear OS 64位合规工具主界面
 *
 * Tab-based layout:
 * - Bottom tabs: Home / SDK Query / Migration Tracker / Settings
 * - Home tab has sub-tabs: APK Scan / Project Scan
 *
 * @param onUpdateTopBar TopBar configuration callback / TopBar 配置回调
 * @param onNavigateBack Navigation callback / 导航回调
 * @param viewModel ViewModel instance / ViewModel 实例
 *
 * @see WearOs64BitContract State/Intent/Effect definitions
 * @see WearOs64BitViewModel State management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearOs64BitScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: WearOs64BitViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Update TopBar based on active tab / 根据当前 Tab 更新 TopBar
    LaunchedEffect(state.activeTab) {
        val config = when (state.activeTab) {
            BottomTab.HOME -> TopBarConfig(
                title = "Wear OS 64-Bit 合规中心",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        onClick = { viewModel.sendIntent(WearOs64BitIntent.StartApkScan(Uri.EMPTY)) }
                    )
                )
            )
            BottomTab.SDK_QUERY -> TopBarConfig(title = "SDK 64位兼容性库")
            BottomTab.MIGRATION_TRACKER -> TopBarConfig(title = "迁移进度追踪")
            BottomTab.SETTINGS -> TopBarConfig(title = "设置")
        }
        onUpdateTopBar(config)
    }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WearOs64BitEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
                is WearOs64BitEffect.ShowError -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
                is WearOs64BitEffect.ShowExportSuccess -> {
                    scope.launch { snackbarHostState.showSnackbar("Report saved: ${effect.filePath}") }
                }
                is WearOs64BitEffect.ScrollToTop -> { /* Handled by LazyColumn */ }
                is WearOs64BitEffect.NavigateToSoDetail -> { /* Could open detail dialog */ }
                is WearOs64BitEffect.OpenFilePicker -> { /* Handled by file picker */ }
            }
        }
    }

    // File picker launcher / 文件选择器启动器
    // Error dismiss handler / 错误关闭处理
    val onDismissError: () -> Unit = {
        viewModel.sendIntent(WearOs64BitIntent.DismissError)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendIntent(WearOs64BitIntent.StartApkScan(it)) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                BottomTab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    BottomTab.HOME -> Icons.Default.Android
                                    BottomTab.SDK_QUERY -> Icons.Default.Search
                                    BottomTab.MIGRATION_TRACKER -> Icons.Default.Description
                                    BottomTab.SETTINGS -> Icons.Default.Settings
                                },
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        selected = state.activeTab == tab,
                        onClick = { viewModel.sendIntent(WearOs64BitIntent.SwitchTab(tab)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.activeTab) {
                BottomTab.HOME -> HomeTabContent(
                    state = state.homeState,
                    activeSubTab = state.activeHomeTab,
                    isLoading = state.isLoading,
                    errorMsg = state.error,
                    onSwitchSubTab = { viewModel.sendIntent(WearOs64BitIntent.SwitchHomeSubTab(it)) },
                    onUploadApk = { viewModel.sendIntent(WearOs64BitIntent.UploadApk(it)) },
                    onStartScan = { filePickerLauncher.launch("application/vnd.android.package-archive") },
                    onSelectSoDetail = { viewModel.sendIntent(WearOs64BitIntent.SelectSoDetail(it)) },
                    onClearSoDetail = { viewModel.sendIntent(WearOs64BitIntent.ClearSoDetail) },
                    onToggleFixSelection = { viewModel.sendIntent(WearOs64BitIntent.ToggleFixSoSelection(it)) },
                    onSetFixStrategy = { viewModel.sendIntent(WearOs64BitIntent.SetFixStrategy(it)) },
                    onStartFixWizard = { viewModel.sendIntent(WearOs64BitIntent.StartFixWizard) },
                    onNextWizardStep = { viewModel.sendIntent(WearOs64BitIntent.NextWizardStep) },
                    onPrevWizardStep = { viewModel.sendIntent(WearOs64BitIntent.PrevWizardStep) },
                    onExecuteFix = { viewModel.sendIntent(WearOs64BitIntent.ExecuteFix) },
                    onResetWizard = { viewModel.sendIntent(WearOs64BitIntent.ResetWizard) },
                    onToggleModule = { viewModel.sendIntent(WearOs64BitIntent.ToggleModuleSelection(it)) },
                    onStartProjectScan = { viewModel.sendIntent(WearOs64BitIntent.StartProjectScan(it)) },
                    onDismissError = { viewModel.sendIntent(WearOs64BitIntent.DismissError) }
                )
                BottomTab.SDK_QUERY -> SdkQueryTabContent(
                    state = state.sdkQueryState,
                    onSearch = { viewModel.sendIntent(WearOs64BitIntent.SearchSdk(it)) }
                )
                BottomTab.MIGRATION_TRACKER -> MigrationTrackerTabContent(
                    projects = viewModel.filteredProjects,
                    filter = state.trackerState.filter,
                    onFilterChange = { viewModel.sendIntent(WearOs64BitIntent.FilterTracker(it)) }
                )
                BottomTab.SETTINGS -> SettingsTabContent(
                    onExportReport = { viewModel.sendIntent(WearOs64BitIntent.ExportReport(it)) }
                )
            }
        }
    }
}

// =============================================================
// HomeTabContent — 首页 Tab 内容
// =============================================================
/**
 * Home tab content / 首页 Tab 内容
 *
 * Contains sub-tabs: APK Scan / Project Scan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTabContent(
    state: HomeState,
    activeSubTab: HomeSubTab,
    isLoading: Boolean,
    errorMsg: String?,
    onSwitchSubTab: (HomeSubTab) -> Unit,
    onUploadApk: (Uri) -> Unit,
    onStartScan: () -> Unit,
    onSelectSoDetail: (SoLibrary) -> Unit,
    onClearSoDetail: () -> Unit,
    onToggleFixSelection: (String) -> Unit,
    onSetFixStrategy: (FixStrategy) -> Unit,
    onStartFixWizard: () -> Unit,
    onNextWizardStep: () -> Unit,
    onPrevWizardStep: () -> Unit,
    onExecuteFix: () -> Unit,
    onResetWizard: () -> Unit,
    onToggleModule: (String) -> Unit,
    onStartProjectScan: (String) -> Unit,
    onDismissError: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-tab selector / 子 Tab 选择器
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            HomeSubTab.entries.forEachIndexed { index, subTab ->
                SegmentedButton(
                    selected = activeSubTab == subTab,
                    onClick = { onSwitchSubTab(subTab) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = HomeSubTab.entries.size
                    )
                ) {
                    Text(subTab.title)
                }
            }
        }

        // Sub-tab content / 子 Tab 内容
        when (activeSubTab) {
            HomeSubTab.APK_SCAN -> ApkScanContent(
                state = state,
                isLoading = isLoading,
                errorMsg = errorMsg,
                onStartScan = onStartScan,
                onSelectSoDetail = onSelectSoDetail,
                onToggleFixSelection = onToggleFixSelection,
                onSetFixStrategy = onSetFixStrategy,
                onStartFixWizard = onStartFixWizard,
                onNextWizardStep = onNextWizardStep,
                onPrevWizardStep = onPrevWizardStep,
                onExecuteFix = onExecuteFix,
                onResetWizard = onResetWizard,
                onDismissError = onDismissError
            )
            HomeSubTab.PROJECT_SCAN -> ProjectScanContent(
                state = state,
                isLoading = isLoading,
                onToggleModule = onToggleModule,
                onStartProjectScan = onStartProjectScan
            )
        }
    }
}

// =============================================================
// ApkScanContent — APK 扫描内容
// =============================================================
/**
 * APK scan content / APK 扫描内容
 *
 * States:
 * - Idle: Show upload zone
 * - Processing: Show progress
 * - Done: Show results + fix wizard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApkScanContent(
    state: HomeState,
    isLoading: Boolean,
    errorMsg: String?,
    onStartScan: () -> Unit,
    onSelectSoDetail: (SoLibrary) -> Unit,
    onToggleFixSelection: (String) -> Unit,
    onSetFixStrategy: (FixStrategy) -> Unit,
    onStartFixWizard: () -> Unit,
    onNextWizardStep: () -> Unit,
    onPrevWizardStep: () -> Unit,
    onExecuteFix: () -> Unit,
    onResetWizard: () -> Unit,
    onDismissError: () -> Unit
) {
    var showDetailDialog by remember { mutableStateOf(false) }

    // Compliance overview card / 合规概览卡片
    if (state.apkInfo != null && state.complianceResult != null) {
        ComplianceOverviewCard(
            score = state.complianceResult.score,
            grade = state.complianceResult.grade,
            apkInfo = state.apkInfo,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // So library list / .so 库列表
        if (state.wizardStep == FixWizardStep.SELECT) {
            // Normal list view / 正常列表视图
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Step indicator / 步骤指示器
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Native Libraries (.so)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = onStartFixWizard,
                            enabled = state.soLibraries.any { !it.has64Bit }
                        ) {
                            Text("Start Fix Wizard / 开始修复向导")
                        }
                    }
                }

                items(state.soLibraries) { library ->
                    SoLibraryItem(
                        library = library,
                        onClick = {
                            onSelectSoDetail(library)
                            showDetailDialog = true
                        }
                    )
                }

                // Export button / 导出按钮
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { /* Export report */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Report / 导出报告")
                    }
                }
            }
        } else {
            // Fix wizard content / 修复向导内容
            FixWizardContent(
                state = state,
                isLoading = isLoading,
                onToggleSelection = onToggleFixSelection,
                onSetStrategy = onSetFixStrategy,
                onNextStep = onNextWizardStep,
                onPrevStep = onPrevWizardStep,
                onExecute = onExecuteFix,
                onReset = onResetWizard
            )
        }
    } else {
        // Upload zone / 上传区域
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Recent scans / 最近扫描
                if (state.recentScans.isNotEmpty()) {
                    Text(
                        text = "Recent Scans / 最近扫描",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    state.recentScans.take(2).forEach { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = record.apkName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Score: ${record.result.score}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = record.result.grade.color
                                    )
                                }
                                Icon(
                                    imageVector = if (record.result.pass) Icons.Default.Check else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = record.result.grade.color
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Upload card / 上传卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .clickable { onStartScan() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Upload APK / AAB",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to select APK or AAB file\n点击选择 APK 或 AAB 文件",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Loading indicator / 加载指示器
                if (isLoading || state.uploadStatus == UploadStatus.PROCESSING) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Analyzing APK... / 正在分析 APK...",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (state.parseProgress > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { state.parseProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // So detail dialog / .so 详情对话框
    if (showDetailDialog && state.selectedSoDetail != null) {
        SoDetailDialog(
            library = state.selectedSoDetail,
            onDismiss = {
                showDetailDialog = false
            }
        )
    }

    // Error dialog / 错误对话框
    errorMsg?.let { err ->
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Error / 错误") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text("OK")
                }
            }
        )
    }
}

// =============================================================
// HomeTabContent — 首页 Tab 内容
// =============================================================

// =============================================================
// ComplianceOverviewCard — 合规概览卡片
// =============================================================
/**
 * Compliance overview card / 合规概览卡片
 *
 * Shows overall compliance score and grade.
 */
@Composable
private fun ComplianceOverviewCard(
    score: Int,
    grade: ComplianceGrade,
    apkInfo: ApkInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = grade.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Compliance Score / 合规分数",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = grade.color
                )
                Text(
                    text = grade.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = grade.color
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = apkInfo.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "v${apkInfo.versionName} (${apkInfo.versionCode})",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${apkInfo.soCount} .so files / .so 文件",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// =============================================================
// SoLibraryItem — .so 库列表项
// =============================================================
/**
 * .so library list item / .so 库列表项
 */
@Composable
private fun SoLibraryItem(
    library: SoLibrary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (library.has64Bit) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = library.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = library.source,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    items(library.supportedAbis) { abi ->
                        Text(
                            text = abi.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (abi.is64Bit) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                            modifier = Modifier
                                .background(
                                    color = if (abi.is64Bit) Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    else Color(0xFF9E9E9E).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Icon(
                imageVector = if (library.has64Bit) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = if (library.has64Bit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// =============================================================
// SoDetailDialog — .so 详情对话框
// =============================================================
/**
 * .so detail dialog / .so 详情对话框
 */
@Composable
private fun SoDetailDialog(
    library: SoLibrary,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = library.name,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                DetailRow("Source / 来源", library.source)
                DetailRow("Size / 大小", formatBytes(library.size))
                DetailRow("Third Party / 第三方", if (library.isFromThirdParty) "Yes / 是" else "No / 否")
                DetailRow("64-bit Support / 64位支持", if (library.has64Bit) "✅ Yes" else "❌ No")
                DetailRow("Recommended Fix / 推荐修复", library.recommendedFix.label)

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Supported ABIs / 支持的 ABI:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                library.supportedAbis.forEach { abi ->
                    Text(
                        text = "• ${abi.label} (${if (abi.is64Bit) "64-bit" else "32-bit"})",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close / 关闭")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// =============================================================
// FixWizardContent — 修复向导内容
// =============================================================
/**
 * Fix wizard content / 修复向导内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FixWizardContent(
    state: HomeState,
    isLoading: Boolean,
    onToggleSelection: (String) -> Unit,
    onSetStrategy: (FixStrategy) -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onExecute: () -> Unit,
    onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Step indicator / 步骤指示器
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FixWizardStep.entries.forEachIndexed { index, step ->
                val isActive = state.wizardStep.index == index
                val isCompleted = state.wizardStep.index > index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = when {
                                    isCompleted -> Color(0xFF4CAF50)
                                    isActive -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                color = if (isActive) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Step content / 步骤内容
        AnimatedContent(
            targetState = state.wizardStep,
            transitionSpec = {
                if (targetState.index > initialState.index) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                    slideOutHorizontally { it } + fadeOut()
                }
            },
            modifier = Modifier.weight(1f),
            label = "wizard_step"
        ) { step ->
            when (step) {
                FixWizardStep.SELECT -> FixStepSelect(
                    libraries = state.soLibraries.filter { !it.has64Bit },
                    selected = state.selectedLibrariesForFix,
                    onToggle = onToggleSelection
                )
                FixWizardStep.STRATEGY -> FixStepStrategy(
                    selectedStrategy = state.selectedFixStrategy,
                    onSetStrategy = onSetStrategy
                )
                FixWizardStep.PREVIEW -> FixStepPreview(
                    selectedLibraries = state.soLibraries.filter { it.name in state.selectedLibrariesForFix },
                    strategy = state.selectedFixStrategy
                )
                FixWizardStep.EXECUTE -> FixStepExecute(
                    logs = state.wizardLog,
                    isComplete = state.wizardComplete,
                    isLoading = isLoading
                )
            }
        }

        // Navigation buttons / 导航按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (!state.wizardComplete) {
                OutlinedButton(
                    onClick = if (state.wizardStep.index > 0) onPrevStep else onReset
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (state.wizardStep.index > 0) "Back / 上一步" else "Reset / 重置")
                }

                Button(
                    onClick = if (state.wizardStep == FixWizardStep.EXECUTE) onExecute else onNextStep,
                    enabled = !isLoading
                ) {
                    Text(
                        if (state.wizardStep == FixWizardStep.EXECUTE) "Execute / 执行" else "Next / 下一步"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (state.wizardStep == FixWizardStep.EXECUTE) Icons.Default.Check
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            } else {
                Button(
                    onClick = onReset,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done / 完成")
                }
            }
        }
    }
}

@Composable
private fun FixStepSelect(
    libraries: List<SoLibrary>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Select .so files to fix / 选择待修复的 .so 文件",
                style = MaterialTheme.typography.titleSmall
            )
        }
        items(libraries) { library ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(library.name) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = library.name in selected,
                        onCheckedChange = { onToggle(library.name) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(library.name, fontWeight = FontWeight.Medium)
                        Text(
                            library.source,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FixStepStrategy(
    selectedStrategy: FixStrategy,
    onSetStrategy: (FixStrategy) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Choose fix strategy / 选择修复策略",
                style = MaterialTheme.typography.titleSmall
            )
        }
        items(FixStrategy.entries.toList()) { strategy ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSetStrategy(strategy) },
                colors = CardDefaults.cardColors(
                    containerColor = if (strategy == selectedStrategy) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = strategy == selectedStrategy,
                        onClick = { onSetStrategy(strategy) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strategy.label, fontWeight = FontWeight.Medium)
                        Text(
                            when (strategy) {
                                FixStrategy.RECOMPILE -> "Recompile .so with 64-bit support using NDK / 使用 NDK 重新编译 .so 以支持64位"
                                FixStrategy.SUPPRESS -> "Suppress warning if .so is confirmed 64-bit capable / 如果确认 .so 支持64位则抑制警告"
                                FixStrategy.REPLACE_SDK -> "Replace third-party SDK with 64-bit version / 替换第三方 SDK 为64位版本"
                                FixStrategy.CUSTOM -> "Custom fix approach / 自定义修复方式"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FixStepPreview(
    selectedLibraries: List<SoLibrary>,
    strategy: FixStrategy
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Preview changes / 预览变更",
                style = MaterialTheme.typography.titleSmall
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Strategy: ${strategy.label}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (strategy) {
                            FixStrategy.RECOMPILE -> "# In app/build.gradle.kts:\nandroid {\n    defaultConfig {\n        ndk { abiFilters += listOf(\"arm64-v8a\", \"x86_64\") }\n    }\n}"
                            FixStrategy.REPLACE_SDK -> "# Update SDK version in build.gradle.kts:\nimplementation(\"com.vendor:sdk:最新版\")"
                            FixStrategy.SUPPRESS -> "# Add to AndroidManifest.xml:\n<meta-data\n    android:name=\"android.wearos.64bit.suppression\"\n    android:value=\"true\" />"
                            FixStrategy.CUSTOM -> "# Custom Gradle configuration required\n# 需要自定义 Gradle 配置"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
        item {
            Text(
                text = "${selectedLibraries.size} .so files selected / 已选择 ${selectedLibraries.size} 个 .so 文件",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun FixStepExecute(
    logs: List<String>,
    isComplete: Boolean,
    isLoading: Boolean
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = if (isComplete) "Fix completed / 修复完成" else "Executing fix... / 正在执行修复...",
                style = MaterialTheme.typography.titleSmall,
                color = if (isComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
            )
        }

        if (isLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Running... / 运行中...", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        items(logs) { log ->
            Text(
                text = log,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
            )
        }

        if (isComplete) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "All selected .so files have been marked for 64-bit support. Run ./gradlew :app:assemble to rebuild.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
// =============================================================
// ProjectScanContent — 项目扫描内容
// =============================================================
/**
 * Project scan content / 项目扫描内容
 *
 * Scan all modules in an Android project for .so compliance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectScanContent(
    state: HomeState,
    isLoading: Boolean,
    onToggleModule: (String) -> Unit,
    onStartProjectScan: (String) -> Unit
) {
    var projectPath by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Project path input / 项目路径输入
        OutlinedTextField(
            value = projectPath,
            onValueChange = { projectPath = it },
            label = { Text("Project Path / 项目路径") },
            placeholder = { Text("e.g., /Users/xxx/AndroidStudioProjects/MyApp") },
            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onStartProjectScan(projectPath) },
            enabled = projectPath.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Start Project Scan / 开始项目扫描")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading indicator / 加载指示器
        if (isLoading && state.parseProgress > 0) {
            LinearProgressIndicator(
                progress = { state.parseProgress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scanning modules... / 正在扫描模块...",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Module list / 模块列表
        if (state.projectModules.isNotEmpty()) {
            Text(
                text = "Modules / 模块",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.projectModules) { module ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = module.isSelected,
                                    onCheckedChange = { onToggleModule(module.name) }
                                )
                                Column {
                                    Text(
                                        text = module.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${module.soLibraries.size} .so files / .so 文件",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Compliance rate bar / 合规率进度条
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${(module.complianceRate * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = when {
                                        module.complianceRate >= 0.9f -> Color(0xFF4CAF50)
                                        module.complianceRate >= 0.6f -> Color(0xFFFF9800)
                                        else -> Color(0xFFF44336)
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(4.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(2.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(module.complianceRate)
                                            .height(4.dp)
                                            .background(
                                                when {
                                                    module.complianceRate >= 0.9f -> Color(0xFF4CAF50)
                                                    module.complianceRate >= 0.6f -> Color(0xFFFF9800)
                                                    else -> Color(0xFFF44336)
                                                },
                                                RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// SdkQueryTabContent — SDK 查询 Tab 内容
// =============================================================
/**
 * SDK query tab content / SDK 查询 Tab 内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SdkQueryTabContent(
    state: SdkQueryState,
    onSearch: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar / 搜索栏
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearch,
            label = { Text("Search SDK / 搜索 SDK") },
            placeholder = { Text("e.g., Stripe, Firebase...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // SDK count / SDK 数量
        Text(
            text = "${state.filteredSdkList.size} SDKs found / 找到 ${state.filteredSdkList.size} 个 SDK",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // SDK list / SDK 列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.filteredSdkList) { sdk ->
                SdkCompatibilityCard(sdk = sdk)
            }

            if (state.filteredSdkList.isEmpty() && state.searchQuery.isNotBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No SDK found for \"${state.searchQuery}\"\n未找到匹配 \"${state.searchQuery}\" 的 SDK",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// SdkCompatibilityCard — SDK 兼容性卡片
// =============================================================
/**
 * SDK compatibility card / SDK 兼容性卡片
 */
@Composable
private fun SdkCompatibilityCard(sdk: SdkInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sdk.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = sdk.vendor,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = sdk.status.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = sdk.status.color,
                    modifier = Modifier
                        .background(
                            sdk.status.color.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Version / 版本",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sdk.version,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "64-bit Compatible / 64位兼容",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sdk.compatibleVersion ?: "—",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (sdk.compatibleVersion != null) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (sdk.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sdk.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =============================================================
// MigrationTrackerTabContent — 迁移进度追踪 Tab 内容
// =============================================================
/**
 * Migration tracker tab content / 迁移进度追踪 Tab 内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MigrationTrackerTabContent(
    projects: List<MigrationProject>,
    filter: TrackerFilter,
    onFilterChange: (TrackerFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter chips / 筛选标签
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TrackerFilter.entries.toList()) { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { onFilterChange(f) },
                    label = { Text(f.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary card / 摘要卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val total = projects.size
                val completed = projects.count { it.status == MigrationStatus.COMPLETED }
                val inProgress = projects.count { it.status == MigrationStatus.IN_PROGRESS }

                StatItem("Total / 总数", total.toString())
                StatItem("Completed / 已完成", completed.toString(), Color(0xFF4CAF50))
                StatItem("In Progress / 进行中", inProgress.toString(), Color(0xFF2196F3))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Project list / 项目列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(projects) { project ->
                MigrationProjectCard(project = project)
            }

            if (projects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No projects match the filter\n没有匹配筛选条件的项目",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, valueColor: Color? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

// =============================================================
// MigrationProjectCard — 迁移项目卡片
// =============================================================
/**
 * Migration project card / 迁移项目卡片
 */
@Composable
private fun MigrationProjectCard(project: MigrationProject) {
    val progress = if (project.moduleCount > 0) {
        project.completedModules.toFloat() / project.moduleCount
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = project.status.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = project.status.color,
                    modifier = Modifier
                        .background(
                            project.status.color.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar / 进度条
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .background(
                                project.status.color,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${project.completedModules}/${project.moduleCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (project.lastScanTime > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Last scan: ${formatTimestamp(project.lastScanTime)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =============================================================
// SettingsTabContent — 设置 Tab 内容
// =============================================================
/**
 * Settings tab content / 设置 Tab 内容
 */
@Composable
private fun SettingsTabContent(
    onExportReport: (Boolean) -> Unit
) {
    var ndkVersion by remember { mutableStateOf("r26") }
    var exportFormat by remember { mutableStateOf("json") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "NDK Configuration / NDK 配置",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = ndkVersion,
                        onValueChange = { ndkVersion = it },
                        label = { Text("Default NDK Version / 默认 NDK 版本") },
                        placeholder = { Text("e.g., r26, r27") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Minimum required for 64-bit support: NDK r26+\n64位支持所需最低版本：NDK r26+",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Report Export / 报告导出",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Export Format / 导出格式",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("json" to "JSON", "md" to "Markdown", "pdf" to "PDF (Coming Soon)").forEach { (value, label) ->
                            FilterChip(
                                selected = exportFormat == value,
                                onClick = { if (value != "pdf") exportFormat = value },
                                label = { Text(label) },
                                enabled = value != "pdf"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onExportReport(exportFormat == "json") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Report / 导出报告")
                    }
                }
            }
        }

        item {
            Text(
                text = "About / 关于",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow("Tool Name / 工具名称", "Wear OS 64-Bit Compliance Tool")
                    DetailRow("Version / 版本", "1.0.0")
                    DetailRow("Google Deadline / Google 截止日期", "2026-09-15")
                    DetailRow("Supported ABIs / 支持的 ABI", "arm64-v8a, x86_64")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This tool helps you check if your Wear OS app includes 64-bit native libraries and provides automated fix guidance.\n此工具帮助您检查 Wear OS 应用是否包含 64 位 native 库，并提供自动化修复指导。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// =============================================================
// Helper Functions — 辅助函数
// =============================================================
/**
 * Format bytes to human-readable string / 将字节格式化为可读字符串
 *
 * @param bytes Size in bytes / 字节大小
 * @return Formatted string (e.g., "1.5 MB") / 格式化字符串
 */
private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> String.format("%.1f GB", bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> String.format("%.1f MB", bytes / 1_000_000.0)
        bytes >= 1_000 -> String.format("%.1f KB", bytes / 1_000.0)
        else -> "$bytes B"
    }
}

/**
 * Format timestamp to relative time string / 将时间戳格式化为相对时间字符串
 *
 * @param timestamp Timestamp in milliseconds / 毫秒时间戳
 * @return Relative time string / 相对时间字符串
 */
private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now / 刚刚"
        diff < 3600_000 -> "${diff / 60_000}m ago / ${diff / 60_000}分钟前"
        diff < 86400_000 -> "${diff / 3600_000}h ago / ${diff / 3600_000}小时前"
        else -> "${diff / 86400_000}d ago / ${diff / 86400_000}天前"
    }
}
