package com.mvi.kenny.feature.contactspicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * ============================================================
 * ContactsPickerScreen — Contacts Picker 工具主界面
 * ============================================================
 * Android 17 Contacts Picker API 迁移选择器与隐私合规工具
 *
 * 5-tab layout:
 * - SCANNER: 扫描 READ_CONTACTS 调用点
 * - LIBRARY: Contacts Picker 封装库使用指南
 * - REPORT: 隐私合规报告生成
 * - TEMPLATES: 集成模板市场
 * - COMPATIBILITY: 跨版本兼容性检测
 *
 * @param viewModel ViewModel instance / ViewModel 实例
 * @see ContactsPickerViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsPickerScreen(
    viewModel: ContactsPickerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for effects / 监听副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ContactsPickerEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is ContactsPickerEffect.ExportSuccess -> {
                    snackbarHostState.showSnackbar("导出成功: ${effect.filePath}")
                }
                is ContactsPickerEffect.ScanComplete -> {
                    // Already handled in snackbar
                }
                is ContactsPickerEffect.NavigateToTemplateDetail -> {
                    // Could navigate to detail screen
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts Picker 工具") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row / Tab 栏
            TabRow(
                selectedTabIndex = state.activeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                ToolTab.entries.forEach { tab ->
                    Tab(
                        selected = state.activeTab == tab,
                        onClick = { viewModel.sendIntent(ContactsPickerIntent.SwitchTab(tab)) },
                        text = {
                            Text(
                                text = tab.label,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Tab Content / Tab 内容
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (state.activeTab) {
                    ToolTab.SCANNER -> ScannerTabContent(
                        state = state,
                        onStartScan = { viewModel.sendIntent(ContactsPickerIntent.StartScan) },
                        onCancelScan = { viewModel.sendIntent(ContactsPickerIntent.CancelScan) },
                        onSelectCallSite = { viewModel.sendIntent(ContactsPickerIntent.SelectCallSite(it)) }
                    )
                    ToolTab.LIBRARY -> LibraryTabContent(
                        state = state,
                        onUpdateConfig = { viewModel.sendIntent(ContactsPickerIntent.UpdatePickerConfig(it)) }
                    )
                    ToolTab.REPORT -> ReportTabContent(
                        state = state,
                        onGenerateReport = { viewModel.sendIntent(ContactsPickerIntent.GenerateReport(it)) }
                    )
                    ToolTab.TEMPLATES -> TemplatesTabContent(
                        state = state,
                        onSelectTemplate = { viewModel.sendIntent(ContactsPickerIntent.SelectTemplate(it)) }
                    )
                    ToolTab.COMPATIBILITY -> CompatibilityTabContent(
                        state = state,
                        onCheckCompatibility = { viewModel.sendIntent(ContactsPickerIntent.CheckCompatibility) }
                    )
                }
            }
        }
    }
}

// =============================================================
// Scanner Tab Content / 扫描器 Tab 内容
// =============================================================
@Composable
private fun ScannerTabContent(
    state: ContactsPickerToolState,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit,
    onSelectCallSite: (PermissionCallSite) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Scan Control Card / 扫描控制卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "READ_CONTACTS 扫描器",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "扫描项目中所有 READ_CONTACTS 权限调用点，评估可迁移性",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar / 进度条
                if (state.scanStatus == ScanStatus.SCANNING) {
                    LinearProgressIndicator(
                        progress = { state.scanProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "扫描进度: ${(state.scanProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onCancelScan,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("取消扫描")
                    }
                } else {
                    OutlinedButton(
                        onClick = onStartScan,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Android, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (state.scanStatus == ScanStatus.COMPLETED) "重新扫描" else "开始扫描")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row / 统计行
        if (state.scanResults.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "P0",
                    count = state.p0Count,
                    color = Migrability.P0.color
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "P1",
                    count = state.p1Count,
                    color = Migrability.P1.color
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "P2",
                    count = state.p2Count,
                    color = Migrability.P2.color
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Results List / 结果列表
        if (state.scanResults.isNotEmpty()) {
            Text(
                text = "扫描结果 (${state.scanResults.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.scanResults) { callSite ->
                    CallSiteCard(
                        callSite = callSite,
                        onClick = { onSelectCallSite(callSite) }
                    )
                }
            }
        } else if (state.scanStatus == ScanStatus.IDLE) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Android,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "点击上方按钮开始扫描",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CallSiteCard(
    callSite: PermissionCallSite,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Migrability Chip / 可迁移性标签
            FilterChip(
                selected = false,
                onClick = onClick,
                label = {
                    Text(
                        text = callSite.migrability.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = callSite.migrability.color.copy(alpha = 0.15f),
                    labelColor = callSite.migrability.color
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = callSite.filePath.substringAfterLast("/"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Line ${callSite.lineNumber} · ${callSite.callChain}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

// =============================================================
// Library Tab Content / 封装库 Tab 内容
// =============================================================
@Composable
private fun LibraryTabContent(
    state: ContactsPickerToolState,
    onUpdateConfig: (PickerConfig) -> Unit
) {
    val config = state.pickerConfig
    var localFields by remember(config) { mutableStateOf(config.fields.toSet()) }
    var localLimit by remember(config) { mutableIntStateOf(config.selectionLimit) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // API Usage Card / API 使用卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Kotlin 封装库用法",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Code Preview / 代码预览
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF1E1E1E),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = """
                            |// 1. 定义 ActivityResultLauncher
                            |private val pickContactLauncher =
                            |    registerForActivityResult(
                            |        ActivityResultContracts.PickContact()
                            |    ) { uri ->
                            |        uri?.let { resolveContact(it) }
                            |    }
                            |
                            |// 2. 启动 Picker
                            |fun launchPicker() {
                            |    pickContactLauncher.launch(null)
                            |}
                        """.trimMargin(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFFD4D4D4)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Config Card / 配置卡片
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "字段请求配置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Field Selection / 字段选择
                Text(
                    text = "请求的联系人字段",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ContactField.entries.forEach { field ->
                        FilterChip(
                            selected = field in localFields,
                            onClick = {
                                localFields = if (field in localFields) {
                                    localFields - field
                                } else {
                                    localFields + field
                                }
                                onUpdateConfig(PickerConfig(
                                    fields = localFields.toList(),
                                    selectionLimit = localLimit
                                ))
                            },
                            label = { Text(field.label, fontSize = 12.sp) },
                            leadingIcon = if (field in localFields) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Selection Limit / 多选限制
                Text(
                    text = "多选数量限制: $localLimit",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 5, 10, 0).forEach { limit ->
                        FilterChip(
                            selected = localLimit == limit,
                            onClick = {
                                localLimit = limit
                                onUpdateConfig(PickerConfig(
                                    fields = localFields.toList(),
                                    selectionLimit = limit
                                ))
                            },
                            label = {
                                Text(
                                    if (limit == 0) "无限制" else limit.toString(),
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preview Card / 预览卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "配置预览",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "字段: ${localFields.joinToString { it.label }}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "多选: ${if (localLimit == 0) "无限制" else "$localLimit 个联系人"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// =============================================================
// Report Tab Content / 报告 Tab 内容
// =============================================================
@Composable
private fun ReportTabContent(
    state: ContactsPickerToolState,
    onGenerateReport: (ReportFormat) -> Unit
) {
    val reportConfig = state.reportConfig
    var includePermissionUsage by remember { mutableStateOf(reportConfig.includePermissionUsage) }
    var includeMigrationComparison by remember { mutableStateOf(reportConfig.includeMigrationComparison) }
    var selectedFormat by remember { mutableStateOf(ReportFormat.MARKDOWN) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Config Card / 配置卡片
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "报告配置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includePermissionUsage,
                        onCheckedChange = { includePermissionUsage = it }
                    )
                    Text(
                        text = "包含权限使用记录",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeMigrationComparison,
                        onCheckedChange = { includeMigrationComparison = it }
                    )
                    Text(
                        text = "包含迁移前后对比",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Format Selection / 格式选择
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "导出格式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportFormat.entries.forEach { format ->
                        FilterChip(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            label = { Text(format.name) },
                            leadingIcon = if (selectedFormat == format) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generate Button / 生成按钮
        OutlinedButton(
            onClick = { onGenerateReport(selectedFormat) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.scanResults.isNotEmpty() || state.generatedReport != null
        ) {
            Icon(Icons.Default.Description, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("生成报告")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Report Preview / 报告预览
        state.generatedReport?.let { report ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "报告预览 (${report.format.name})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { /* copy */ }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "复制")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = report.content,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// Templates Tab Content / 模板 Tab 内容
// =============================================================
@Composable
private fun TemplatesTabContent(
    state: ContactsPickerToolState,
    onSelectTemplate: (Template) -> Unit
) {
    val templates = remember {
        listOf(
            Template(
                id = "dial",
                name = "拨号场景",
                scenario = "用户选择联系人后直接拨打电话",
                code = """
                    |// Dial Template / 拨号模板
                    |val pickContactLauncher = registerForActivityResult(
                    |    ActivityResultContracts.PickContact()
                    |) { uri ->
                    |    uri?.let { contactUri ->
                    |        val phone = getPhoneNumber(contactUri)
                    |        val intent = Intent(Intent.ACTION_DIAL).apply {
                    |            data = Uri.parse("tel:${'$'}phone")
                    |        }
                    |        startActivity(intent)
                    |    }
                    |}
                    |
                    |private fun getPhoneNumber(uri: Uri): String? {
                    |    val projection = arrayOf(ContactsContract.PhoneLookup.NUMBER)
                    |    contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    |        if (cursor.moveToFirst()) {
                    |            return cursor.getString(0)
                    |        }
                    |    }
                    |    return null
                    |}
                """.trimMargin()
            ),
            Template(
                id = "sms",
                name = "短信场景",
                scenario = "用户选择联系人后打开短信编辑页面",
                code = """
                    |// SMS Template / 短信模板
                    |val pickContactLauncher = registerForActivityResult(
                    |    ActivityResultContracts.PickContact()
                    |) { uri ->
                    |    uri?.let { contactUri ->
                    |        val phone = getPhoneNumber(contactUri)
                    |        val intent = Intent(Intent.ACTION_SENDTO).apply {
                    |            data = Uri.parse("smsto:${'$'}phone")
                    |        }
                    |        startActivity(intent)
                    |    }
                    |}
                """.trimMargin()
            ),
            Template(
                id = "email",
                name = "邮件场景",
                scenario = "用户选择联系人后打开邮件应用",
                code = """
                    |// Email Template / 邮件模板
                    |val pickContactLauncher = registerForActivityResult(
                    |    ActivityResultContracts.PickContact()
                    |) { uri ->
                    |    uri?.let { contactUri ->
                    |        val email = getEmail(contactUri)
                    |        val intent = Intent(Intent.ACTION_SENDTO).apply {
                    |            data = Uri.parse("mailto:${'$'}email")
                    |        }
                    |        startActivity(intent)
                    |    }
                    |}
                """.trimMargin()
            ),
            Template(
                id = "social",
                name = "社交分享场景",
                scenario = "用户选择联系人后分享内容到社交应用",
                code = """
                    |// Social Share Template / 社交分享模板
                    |val pickContactLauncher = registerForActivityResult(
                    |    ActivityResultContracts.PickContact()
                    |) { uri ->
                    |    uri?.let { contactUri ->
                    |        val name = getDisplayName(contactUri)
                    |        // Share to social apps / 分享到社交应用
                    |        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    |            type = "text/plain"
                    |            putExtra(Intent.EXTRA_TEXT, "Hello ${'$'}name!")
                    |        }
                    |        startActivity(Intent.createChooser(shareIntent, "分享到"))
                    |    }
                    |}
                """.trimMargin()
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(templates) { template ->
            TemplateCard(
                template = template,
                onClick = { onSelectTemplate(template) }
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: Template,
    onClick: () -> Unit
) {
    val icon = when (template.id) {
        "dial" -> Icons.Default.Phone
        "sms" -> Icons.Default.Sms
        "email" -> Icons.Default.Email
        "social" -> Icons.Default.Share
        else -> Icons.Default.Person
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = template.scenario,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            IconButton(onClick = onClick) {
                Icon(Icons.Default.Download, contentDescription = "使用模板")
            }
        }
    }
}

// =============================================================
// Compatibility Tab Content / 兼容检测 Tab 内容
// =============================================================
@Composable
private fun CompatibilityTabContent(
    state: ContactsPickerToolState,
    onCheckCompatibility: () -> Unit
) {
    val result = state.compatibilityResult

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Check Button / 检测按钮
        OutlinedButton(
            onClick = onCheckCompatibility,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Android, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("检测 Android 版本兼容性")
        }

        Spacer(modifier = Modifier.height(24.dp))

        result?.let { compat ->
            // Device Info Card / 设备信息卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "设备信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        Text("当前 SDK 版本:", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Android ${compat.currentSdkInt - 27} (API ${compat.currentSdkInt})")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Result Card / 结果卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (compat.isCompatible) {
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                    } else {
                        Color(0xFFFB8C00).copy(alpha = 0.1f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (compat.isCompatible) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (compat.isCompatible) Color(0xFF4CAF50) else Color(0xFFFB8C00)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (compat.isCompatible) "支持 Contacts Picker API" else "不支持 Contacts Picker API",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (compat.isCompatible) Color(0xFF4CAF50) else Color(0xFFFB8C00)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "降级策略:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = compat.fallbackStrategy,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Features Card / 功能卡片
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "可用功能",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    compat.features.forEach { feature ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Android,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "点击上方按钮检测兼容性",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
