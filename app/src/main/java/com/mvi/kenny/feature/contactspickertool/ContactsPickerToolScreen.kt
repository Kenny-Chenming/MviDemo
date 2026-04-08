package com.mvi.kenny.feature.contactspickertool

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

// =============================================================
// ContactsPickerToolScreen — Android 17 Contacts Picker API
// 迁移选择器与隐私合规工具包主界面
// =============================================================
/**
 * Main screen for Contacts Picker Tool / Contacts Picker 工具主界面
 *
 * Implements a dashboard + multi-tab interface as specified in the design doc:
 * - Left navigation: tool module switching
 * - Main content: tool-specific UI
 * - Tab bar: alternative navigation for mobile
 *
 * @param viewModel ViewModel instance / ViewModel 实例
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsPickerToolScreen(
    viewModel: ContactsPickerToolViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ContactsPickerToolEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is ContactsPickerToolEffect.ExportSuccess -> {
                    snackbarHostState.showSnackbar("Exported: ${effect.filePath}")
                }
                is ContactsPickerToolEffect.ScanComplete -> {
                    snackbarHostState.showSnackbar("Scan complete: ${state.scanResults.size} call sites found")
                }
                is ContactsPickerToolEffect.NavigateToTemplateDetail -> {
                    // Handled by selecting template / 通过选择模板处理
                }
                is ContactsPickerToolEffect.CopyToClipboard -> {
                    // Would copy to clipboard / 会复制到剪贴板
                    snackbarHostState.showSnackbar("Code copied to clipboard")
                }
                is ContactsPickerToolEffect.OpenFileExporter -> {
                    // Would open file exporter / 会打开文件导出器
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Contacts Picker Tool",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Refresh / 刷新
                    IconButton(
                        onClick = { viewModel.processIntent(ContactsPickerToolIntent.CheckCompatibility) }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                ToolTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    when (tab) {
                                        ToolTab.SCANNER -> {
                                            if (state.p0Count > 0) {
                                                Badge { Text("${state.p0Count}") }
                                            }
                                        }
                                        ToolTab.REPORT -> {
                                            if (state.generatedReport != null) {
                                                Badge { Icon(Icons.Default.Check, null) }
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            ) {
                                Icon(getTabIcon(tab), contentDescription = tab.title)
                            }
                        },
                        label = {
                            Text(
                                tab.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 10.sp
                            )
                        },
                        selected = state.activeTab == tab,
                        onClick = {
                            viewModel.processIntent(ContactsPickerToolIntent.SelectTab(tab))
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab content / Tab 内容
            when (state.activeTab) {
                ToolTab.SCANNER -> ScannerTabContent(
                    state = state,
                    onIntent = viewModel::processIntent
                )
                ToolTab.LIBRARY -> LibraryTabContent(
                    state = state,
                    onIntent = viewModel::processIntent
                )
                ToolTab.REPORT -> ReportTabContent(
                    state = state,
                    onIntent = viewModel::processIntent
                )
                ToolTab.TEMPLATES -> TemplatesTabContent(
                    state = state,
                    onIntent = viewModel::processIntent
                )
                ToolTab.COMPATIBILITY -> CompatibilityTabContent(
                    state = state,
                    onIntent = viewModel::processIntent
                )
            }
        }
    }

    // Call site detail dialog / 调用点详情对话框
    state.selectedCallSite?.let { callSite ->
        CallSiteDetailDialog(
            callSite = callSite,
            onDismiss = { viewModel.processIntent(ContactsPickerToolIntent.ClearCallSite) }
        )
    }
}

// =============================================================
// Scanner Tab Content — READ_CONTACTS 扫描器页面
// =============================================================
/**
 * Scanner tab content / 扫描器 Tab 内容
 *
 * Displays scan configuration, progress, and results.
 */
@Composable
private fun ScannerTabContent(
    state: ContactsPickerToolState,
    onIntent: (ContactsPickerToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Scan configuration card / 扫描配置卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "READ_CONTACTS Scan Configuration / READ_CONTACTS 扫描配置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.selectedModule ?: "MyMviProject",
                    onValueChange = {},
                    label = { Text("Project / Module Path") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.scanStatus != ScanStatus.SCANNING,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.scanStatus == ScanStatus.SCANNING) {
                        TextButton(
                            onClick = { onIntent(ContactsPickerToolIntent.CancelScan) }
                        ) {
                            Icon(Icons.Default.Close, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel / 取消")
                        }
                    } else {
                        TextButton(
                            onClick = {
                                onIntent(
                                    ContactsPickerToolIntent.StartScan(
                                        state.selectedModule ?: "MyMviProject"
                                    )
                                )
                            },
                            enabled = state.scanStatus != ScanStatus.SCANNING
                        ) {
                            Icon(Icons.Default.Search, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Scan / 开始扫描")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scan progress / 扫描进度
        if (state.scanStatus == ScanStatus.SCANNING) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Scanning... / 扫描中...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${state.scanPercentage}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.scanProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Scan results / 扫描结果
        if (state.scanStatus == ScanStatus.COMPLETED) {
            // Summary cards / 摘要卡片
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "P0 Critical / P0 严重",
                    count = state.p0Count,
                    color = Color(0xFFB3261E)
                )
                SummaryCard(
                    title = "P1 Important / P1 重要",
                    count = state.p1Count,
                    color = Color(0xFFF57C00)
                )
                SummaryCard(
                    title = "P2 Optional / P2 可选",
                    count = state.p2Count,
                    color = Color(0xFFF1C40F)
                )
                SummaryCard(
                    title = "Total / 总计",
                    count = state.scanResults.size,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Call Sites / 调用点",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            state.sortedScanResults.forEach { callSite ->
                CallSiteCard(
                    callSite = callSite,
                    onClick = {
                        onIntent(ContactsPickerToolIntent.SelectCallSite(callSite))
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Empty state / 空状态
        if (state.scanStatus == ScanStatus.IDLE && state.scanResults.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Search,
                title = "No scan results / 无扫描结果",
                description = "Click 'Start Scan' to scan for READ_CONTACTS usage / Click Start Scan to scan for READ_CONTACTS usage"
            )
        }
    }
}

// =============================================================
// Library Tab Content — Picker 封装库页面
// =============================================================
/**
 * Library tab content / Picker 封装库 Tab 内容
 *
 * Displays Contacts Picker Kotlin library code and configuration.
 */
@Composable
private fun LibraryTabContent(
    state: ContactsPickerToolState,
    onIntent: (ContactsPickerToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Contacts Picker Kotlin Library / Contacts Picker Kotlin 封装库",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Picker configuration / Picker 配置
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Picker Configuration / Picker 配置",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fields / 字段选择
                Text(
                    "Request Fields / 请求字段",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PickerField.ALL_FIELDS.forEach { field ->
                        FilterChip(
                            selected = field.key in state.pickerConfig.selectedFields,
                            onClick = {
                                val newFields = state.pickerConfig.selectedFields.toMutableSet().apply {
                                    if (field.key in this) remove(field.key) else add(field.key)
                                }
                                onIntent(
                                    ContactsPickerToolIntent.UpdatePickerConfig(
                                        state.pickerConfig.copy(selectedFields = newFields)
                                    )
                                )
                            },
                            label = { Text(field.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Multiple selection / 多选
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        onIntent(
                            ContactsPickerToolIntent.UpdatePickerConfig(
                                state.pickerConfig.copy(
                                    allowMultipleSelection = !state.pickerConfig.allowMultipleSelection
                                )
                            )
                        )
                    }
                ) {
                    Checkbox(
                        checked = state.pickerConfig.allowMultipleSelection,
                        onCheckedChange = {
                            onIntent(
                                ContactsPickerToolIntent.UpdatePickerConfig(
                                    state.pickerConfig.copy(allowMultipleSelection = it)
                                )
                            )
                        }
                    )
                    Text("Allow Multiple Selection / 允许多选")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Library code preview / 库代码预览
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E2E)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Library Code / 库代码",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            // Copy code to clipboard / 复制代码到剪贴板
                        }
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    getSamplePickerCode(state.pickerConfig),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFFE0E0E0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D1117))
                        .padding(12.dp)
                )
            }
        }
    }
}

// =============================================================
// Report Tab Content — 隐私合规报告页面
// =============================================================
/**
 * Report tab content / 报告 Tab 内容
 */
@Composable
private fun ReportTabContent(
    state: ContactsPickerToolState,
    onIntent: (ContactsPickerToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Privacy Compliance Report / 隐私合规报告",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Report config / 报告配置
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Report Configuration / 报告配置",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                ReportConfigCheckboxes(
                    config = state.reportConfig,
                    onConfigChange = { newConfig ->
                        onIntent(ContactsPickerToolIntent.UpdateReportConfig(newConfig))
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Format selection / 格式选择
                Text("Export Format / 导出格式", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportFormat.entries.forEach { format ->
                        FilterChip(
                            selected = state.reportConfig.format == format,
                            onClick = {
                                onIntent(
                                    ContactsPickerToolIntent.GenerateReport(format)
                                )
                            },
                            label = { Text(format.label) },
                            enabled = !state.isGeneratingReport
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (state.isGeneratingReport) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text("Generating... / 生成中...")
                    }
                } else {
                    TextButton(
                        onClick = {
                            onIntent(
                                ContactsPickerToolIntent.GenerateReport(
                                    state.reportConfig.format
                                )
                            )
                        },
                        enabled = state.scanResults.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Description, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Generate Report / 生成报告")
                    }
                }
            }
        }

        // Generated report preview / 生成的报告预览
        state.generatedReport?.let { report ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Generated Report / 生成的报告",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { onIntent(ContactsPickerToolIntent.ClearReport) }
                        ) {
                            Icon(Icons.Default.Close, "Clear")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        report.fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        report.filePath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        report.summary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// =============================================================
// Templates Tab Content — 集成模板页面
// =============================================================
/**
 * Templates tab content / 模板 Tab 内容
 */
@Composable
private fun TemplatesTabContent(
    state: ContactsPickerToolState,
    onIntent: (ContactsPickerToolIntent) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<TemplateCategory?>(null) }
    var showTemplateDetail by remember { mutableStateOf<Template?>(null) }

    val templates = remember(selectedCategory) {
        getSampleTemplates().filter {
            selectedCategory == null || it.category == selectedCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Integration Templates / 集成模板",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category filter / 分类过滤
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All / 全部") }
            )
            TemplateCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(templates) { template ->
                TemplateCard(
                    template = template,
                    onClick = { showTemplateDetail = template }
                )
            }
        }
    }

    // Template detail dialog / 模板详情对话框
    showTemplateDetail?.let { template ->
        TemplateDetailDialog(
            template = template,
            onDismiss = { showTemplateDetail = null },
            onExport = { targetPath ->
                onIntent(
                    ContactsPickerToolIntent.ExportTemplate(template, targetPath)
                )
                showTemplateDetail = null
            }
        )
    }
}

// =============================================================
// Compatibility Tab Content — 跨版本兼容页面
// =============================================================
/**
 * Compatibility tab content / 兼容性 Tab 内容
 */
@Composable
private fun CompatibilityTabContent(
    state: ContactsPickerToolState,
    onIntent: (ContactsPickerToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Cross-Version Compatibility / 跨版本兼容性",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Check button / 检测按钮
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (state.isCheckingCompatibility) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text("Checking... / 检测中...")
                    }
                } else {
                    TextButton(
                        onClick = { onIntent(ContactsPickerToolIntent.CheckCompatibility) }
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Check Device Compatibility / 检测设备兼容性")
                    }
                }
            }
        }

        // Compatibility result / 兼容性结果
        state.compatibilityResult?.let { result ->
            Spacer(modifier = Modifier.height(16.dp))

            // Status card / 状态卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.isCompatible)
                        Color(0xFF1B5E20).copy(alpha = 0.2f)
                    else
                        Color(0xFFB71C1C).copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        if (result.isCompatible) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (result.isCompatible) Color(0xFF4CAF50) else Color(0xFFE53935),
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            if (result.isCompatible) "Compatible / 兼容" else "Not Compatible / 不兼容",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Android ${result.deviceAndroidVersion}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Supported fields / 支持的字段
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Supported Fields / 支持的字段",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    result.supportedFields.forEach { field ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(field)
                        }
                    }
                }
            }

            // Fallback strategy / 降级策略
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Fallback Strategy / 降级策略",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        result.fallbackStrategy,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// =============================================================
// Reusable Components — 可复用组件
// =============================================================

/**
 * Summary card for scan statistics / 扫描统计摘要卡片
 */
@Composable
private fun SummaryCard(
    title: String,
    count: Int,
    color: Color
) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = color
            )
        }
    }
}

/**
 * Call site card / 调用点卡片
 */
@Composable
private fun CallSiteCard(
    callSite: PermissionCallSite,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = callSite.migrabilityRating.color,
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 2.dp,
                color = borderColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    callSite.className,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Badge(
                    containerColor = callSite.migrabilityRating.color
                ) {
                    Text(
                        callSite.migrabilityRating.label,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "${callSite.methodName}()",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )

            Text(
                "${callSite.filePath}:${callSite.lineNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                callSite.migrationSuggestion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Call site detail dialog / 调用点详情对话框
 */
@Composable
private fun CallSiteDetailDialog(
    callSite: PermissionCallSite,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(callSite.className)
                Badge(containerColor = callSite.migrabilityRating.color) {
                    Text(
                        callSite.migrabilityRating.label,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    "Call Chain / 调用链",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    callSite.callChain,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Location / 位置",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${callSite.filePath}:${callSite.lineNumber}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Migration Suggestion / 迁移建议",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(callSite.migrationSuggestion)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close / 关闭")
            }
        }
    )
}

/**
 * Report configuration checkboxes / 报告配置复选框
 */
@Composable
private fun ReportConfigCheckboxes(
    config: ReportConfig,
    onConfigChange: (ReportConfig) -> Unit
) {
    Column {
        ReportConfigCheckboxItem(
            label = "Permission Usage Records / 权限使用记录",
            checked = config.includePermissionUsage,
            onCheckedChange = {
                onConfigChange(config.copy(includePermissionUsage = it))
            }
        )
        ReportConfigCheckboxItem(
            label = "Before/After Comparison / 迁移前后对比",
            checked = config.includeBeforeAfterComparison,
            onCheckedChange = {
                onConfigChange(config.copy(includeBeforeAfterComparison = it))
            }
        )
        ReportConfigCheckboxItem(
            label = "Data Flow Diagram / 数据流向图",
            checked = config.includeDataFlowDiagram,
            onCheckedChange = {
                onConfigChange(config.copy(includeDataFlowDiagram = it))
            }
        )
        ReportConfigCheckboxItem(
            label = "Google Play Data Safety Mapping / Google Play Data Safety 映射",
            checked = config.includeGooglePlayDataSafety,
            onCheckedChange = {
                onConfigChange(config.copy(includeGooglePlayDataSafety = it))
            }
        )
    }
}

@Composable
private fun ReportConfigCheckboxItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * Template card / 模板卡片
 */
@Composable
private fun TemplateCard(
    template: Template,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                getTemplateIcon(template.category),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    template.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "~${template.lineCount} lines",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Template detail dialog / 模板详情对话框
 */
@Composable
private fun TemplateDetailDialog(
    template: Template,
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    var targetPath by remember { mutableStateOf("/tmp/${template.id}.kt") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(template.name) },
        text = {
            Column {
                Text(
                    template.code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D1117))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetPath,
                    onValueChange = { targetPath = it },
                    label = { Text("Export Path / 导出路径") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onExport(targetPath) }) {
                Icon(Icons.Default.Save, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export / 导出")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel / 取消")
            }
        }
    )
}

/**
 * Empty state card / 空状态卡片
 */
@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// =============================================================
// Helper Functions — 辅助函数
// =============================================================

/**
 * Get icon for a tool tab / 获取 Tab 图标
 */
private fun getTabIcon(tab: ToolTab): ImageVector {
    return when (tab) {
        ToolTab.SCANNER -> Icons.Default.Search
        ToolTab.LIBRARY -> Icons.Default.Description
        ToolTab.REPORT -> Icons.Default.Description
        ToolTab.TEMPLATES -> Icons.Default.Share
        ToolTab.COMPATIBILITY -> Icons.Default.Check
    }
}

/**
 * Get icon for a template category / 获取模板分类图标
 */
private fun getTemplateIcon(category: TemplateCategory): ImageVector {
    return when (category) {
        TemplateCategory.DIAL -> Icons.Default.Phone
        TemplateCategory.SMS -> Icons.Default.Sms
        TemplateCategory.EMAIL -> Icons.Default.Email
        TemplateCategory.SOCIAL -> Icons.Default.Share
    }
}

/**
 * Get sample picker library code / 获取示例 Picker 库代码
 *
 * In production this would be generated based on actual PickerConfig.
 * / 生产环境中这将基于实际 PickerConfig 生成。
 */
private fun getSamplePickerCode(config: PickerConfig): String {
    val fields = config.selectedFields.joinToString("\n    ") { field ->
        """ContentContract.Intents.Insert.$field"""
    }
    return """
package com.example.app.contacts

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/**
 * Contacts Picker launcher / Contacts Picker 启动器
 *
 * Usage:
 * @sample ContactsPickerSample
 */
class ContactsPickerLauncher {

    private var onResult: ((List<ContactResult>) -> Unit)? = null

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { /* Handle contact URI */ }
    }

    @Composable
    fun Launch(
        onResult: (List<ContactResult>) -> Unit
    ) {
        this.onResult = onResult
        pickerLauncher.launch(null)
    }
}

data class ContactResult(
    val uri: String,
    val displayName: String?,
    val phone: String?,
    val email: String?
)

// Configuration:
// Fields: ${config.selectedFields.joinToString(", ")}
// Selection Limit: ${if (config.selectionLimit == 0) "Unlimited" else config.selectionLimit}
// Multi-select: ${config.allowMultipleSelection}
    """.trimIndent()
}

/**
 * Get sample templates / 获取示例模板列表
 *
 * In production these would be loaded from actual template files.
 * / 生产环境中这些将从实际模板文件加载。
 */
private fun getSampleTemplates(): List<Template> = listOf(
    Template(
        id = "dial_template",
        category = TemplateCategory.DIAL,
        name = "Dial Contact Template / 拨号联系人模板",
        description = "Launches the system dialer with a contact's phone number / 启动系统拨号器并填入联系人电话号码",
        code = """
package com.example.app.contacts.templates

import android.content.Intent
import android.net.Uri

/**
 * Dial contact template / 拨号联系人模板
 *
 * Opens the dialer with a pre-filled phone number.
 * Opens system dialer with the contact's phone number pre-filled.
 *
 * @param phoneNumber Phone number to dial / 要拨打的电话号码
 */
fun dialContact(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:${'$'}phoneNumber")
    }
    // Launch with ActivityResultLauncher
}
        """.trimIndent(),
        lineCount = 18,
        tags = listOf("dial", "phone", "call")
    ),
    Template(
        id = "sms_template",
        category = TemplateCategory.SMS,
        name = "SMS Contact Template / 短信联系人模板",
        description = "Opens the messaging app with a contact's phone number / 打开发短信界面并填入联系人电话号码",
        code = """
package com.example.app.contacts.templates

import android.content.Intent
import android.net.Uri

/**
 * SMS contact template / 短信联系人模板
 *
 * Opens the default SMS app with the contact's phone number.
 *
 * @param phoneNumber Recipient phone number / 收件人电话号码
 * @param message Initial message text (optional) / 初始消息文本（可选）
 */
fun smsContact(phoneNumber: String, message: String = "") {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:${'$'}phoneNumber")
        if (message.isNotEmpty()) {
            putExtra("sms_body", message)
        }
    }
    // Launch with ActivityResultLauncher
}
        """.trimIndent(),
        lineCount = 20,
        tags = listOf("sms", "message", "text")
    ),
    Template(
        id = "email_template",
        category = TemplateCategory.EMAIL,
        name = "Email Contact Template / 邮件联系人模板",
        description = "Opens email client with contact's email address / 打开发邮件界面并填入联系人邮箱地址",
        code = """
package com.example.app.contacts.templates

import android.content.Intent
import android.net.Uri

/**
 * Email contact template / 邮件联系人模板
 *
 * Opens email client with contact's email address pre-filled.
 *
 * @param email Recipient email address / 收件人邮箱
 * @param subject Email subject (optional) / 邮件主题（可选）
 * @param body Email body (optional) / 邮件正文（可选）
 */
fun emailContact(email: String, subject: String = "", body: String = "") {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:${'$'}email")
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    // Launch with ActivityResultLauncher
}
        """.trimIndent(),
        lineCount = 20,
        tags = listOf("email", "mail", "compose")
    ),
    Template(
        id = "social_share_template",
        category = TemplateCategory.SOCIAL,
        name = "Social Share Template / 社交分享模板",
        description = "Shares content to selected contacts via social apps / 通过社交应用向选中的联系人分享内容",
        code = """
package com.example.app.contacts.templates

import android.content.Intent

/**
 * Social share template / 社交分享模板
 *
 * Shares text or media content to selected contacts.
 *
 * @param shareText Text to share / 要分享的文本
 * @param mimeType MIME type of content / 内容的 MIME 类型
 */
fun shareToContact(shareText: String, mimeType: String = "text/plain") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    // Use Contacts Picker to select contact, then share
    // Use ActivityResultContracts.PickContact() first
}
        """.trimIndent(),
        lineCount = 17,
        tags = listOf("share", "social", "intent")
    )
)
