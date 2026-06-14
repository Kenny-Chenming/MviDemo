package com.mvi.kenny.feature.migrationtoolkit

// ================================================================
// MigrationToolkitScreen — Material Views → Compose 迁移工具包主界面
// ================================================================
// Main screen for MDC-Android Views → Compose Migration Toolkit.
//
// PRD-266: Material Views → Compose 迁移工具包
// Design Reference: memory/agency/designs/PRD-266-Material-Views-Compose迁移工具包.md
//
// Features:
//   - Homepage: 10 tool cards with Tab filtering (ALL/ASSESSMENT/CONVERTER/GUIDE)
//   - Assessment: XML layout scanning, complexity scoring, priority recommendations
//   - Converter: XML → Composable code conversion with preview
//   - Mapping: Searchable MDC Views → Compose component mapping table
//   - Theming: colors.xml/dimens.xml/strings.xml → Compose Theme migration guide
//   - Hybrid: Progressive migration checklist for mixed Views+Compose apps
//   - MDC Maintenance: MDC-Android maintenance mode explanation
//   - M2→M3: Material 2 → Material 3 migration guide
//   - M3 Expressive: M3 Expressive APIs migration guide
//   - Dynamic Color: Material You / Dynamic Color Compose integration
//   - Styles API: New Styles API Compose integration guide
// —————————————————————————————————————————————————————————————————

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ================================================================
// MigrationToolkitScreen — 主入口 Composable
// ================================================================

/**
 * MigrationToolkitScreen — 迁移工具包主页
 *
 * Top-level composable that sets up the MVI state collection
 * and passes intents to the ViewModel.
 *
 * @param currentRoute Current navigation route (determines which sub-page to show)
 * @param onNavigateBack Callback for navigation back
 * @param viewModel ViewModel instance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrationToolkitScreen(
    currentRoute: String = "migration_toolkit",
    onNavigateBack: (() -> Unit)? = null,
    viewModel: MigrationToolkitViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // ── Handle Effects ──────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MigrationToolkitEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is MigrationToolkitEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("code", effect.content))
                }
                is MigrationToolkitEffect.ShareReport -> {
                    // In production: use share intent
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("report", effect.content))
                    snackbarHostState.showSnackbar("报告已复制到剪贴板")
                }
                is MigrationToolkitEffect.NavigateToRoute -> {
                    // In production: use NavController
                }
                is MigrationToolkitEffect.ShowError -> {
                    snackbarHostState.showSnackbar("❌ ${effect.message}")
                }
            }
        }
    }

    // ── Route-based sub-page rendering ────────────────────────
    when (currentRoute) {
        "migration_toolkit_assessment" -> AssessmentScreen(
            state = state,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
        "migration_toolkit_converter" -> ConverterScreen(
            state = state,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
        "migration_toolkit_mapping" -> MappingScreen(
            state = state,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
        "migration_toolkit_theme" -> ThemingScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
        "migration_toolkit_hybrid" -> HybridScreen(
            onNavigateBack = onNavigateBack
        )
        "migration_toolkit_mdc_maintenance" -> MdcMaintenanceScreen(
            onNavigateBack = onNavigateBack
        )
        "migration_toolkit_m2_m3" -> M2M3Screen(
            onNavigateBack = onNavigateBack
        )
        else -> MigrationToolkitHomeScreen(
            state = state,
            viewModel = viewModel,
            snackbarHostState = snackbarHostState
        )
    }
}

// ================================================================
// MigrationToolkitHomeScreen — 迁移工具包首页
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MigrationToolkitHomeScreen(
    state: MigrationToolkitState,
    viewModel: MigrationToolkitViewModel,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MDC Views → Compose 迁移工具包", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Material Views 进入维护模式，迁移正当时",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(MigrationToolkitIntent.ResetAll) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Tab Filter Row ───────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MigrationTab.entries.forEach { tab ->
                    FilterChip(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.sendIntent(MigrationToolkitIntent.SelectTab(tab)) },
                        label = { Text("${tab.emoji} ${tab.title}") }
                    )
                }
            }

            // ── Tool Cards Grid ──────────────────────────────────
            val filteredTools = when (state.selectedTab) {
                MigrationTab.ALL -> state.tools
                MigrationTab.ASSESSMENT -> state.tools.filter { it.category == ToolCategory.ASSESSMENT }
                MigrationTab.CONVERTER -> state.tools.filter { it.category == ToolCategory.CONVERTER }
                MigrationTab.GUIDE -> state.tools.filter { it.category == ToolCategory.GUIDE }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTools, key = { it.id }) { tool ->
                    ToolCardItem(
                        tool = tool,
                        onClick = { viewModel.sendIntent(MigrationToolkitIntent.SelectTool(tool)) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    // ── MDC Maintenance Banner ────────────────────────
                    MdcMaintenanceBanner()
                }
            }
        }
    }
}

// ================================================================
// ToolCardItem — 工具卡片组件
// ================================================================

@Composable
private fun ToolCardItem(
    tool: ToolCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Icon ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tool.icon,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── Content ────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = tool.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    PriorityBadge(priority = tool.priority)
                    if (tool.status != ToolStatus.READY) {
                        StatusBadge(status = tool.status)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryChip(category = tool.category)
                    Text(
                        text = "⏱ ${tool.estimatedTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Arrow ──────────────────────────────────────────
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = 180f }
            )
        }
    }
}

// ================================================================
// AssessmentScreen — 评估工具页
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssessmentScreen(
    state: MigrationToolkitState,
    viewModel: MigrationToolkitViewModel,
    onNavigateBack: (() -> Unit)?
) {
    val context = LocalContext.current
    var hasSelectedFiles by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            hasSelectedFiles = true
            viewModel.sendIntent(MigrationToolkitIntent.StartAssessment(uris))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔍 迁移评估工具") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (!hasSelectedFiles || state.assessmentState.scanResult == null) {
                // ── File Selection ─────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "选择 XML Layout 文件",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "支持 AndroidManifest.xml 或 layouts/ 文件夹",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.Button(
                            onClick = { filePickerLauncher.launch(arrayOf("application/xml", "text/xml")) }
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("选择文件")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                // Use simulated data for demo
                                viewModel.sendIntent(MigrationToolkitIntent.StartAssessment(emptyList()))
                                hasSelectedFiles = true
                            }
                        ) {
                            Text("使用演示数据")
                        }
                    }
                }
            } else {
                // ── Scan Result ────────────────────────────────────
                AssessmentResultCard(result = state.assessmentState.scanResult!!)
            }
        }
    }
}

// ================================================================
// AssessmentResultCard — 评估结果卡片
// ================================================================

@Composable
private fun AssessmentResultCard(result: ScanResult) {
    // ── Summary Card ────────────────────────────────────────────
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📊 评估摘要", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Views", result.totalViews.toString())
                StatItem("ViewGroups", result.totalViewGroups.toString())
                StatItem("嵌套深度", result.maxNestingDepth.toString())
            }
            Spacer(modifier = Modifier.height(12.dp))
            ComplexityRating(score = result.complexityScore)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Component Stats ──────────────────────────────────────────
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📈 组件统计", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            result.componentStats.take(8).forEach { stat ->
                ComponentStatRow(stat = stat, maxCount = result.componentStats.maxOfOrNull { it.count } ?: 1)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Priority Recommendations ────────────────────────────────
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🎯 优先级迁移建议", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            result.priorityRecommendations.forEach { rec ->
                RecommendationCard(rec = rec)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ================================================================
// ConverterScreen — 转换器页
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConverterScreen(
    state: MigrationToolkitState,
    viewModel: MigrationToolkitViewModel,
    onNavigateBack: (() -> Unit)?
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔄 自动化转换工具") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (state.converterState.convertedCode.isNotEmpty()) {
                                viewModel.sendIntent(
                                    MigrationToolkitIntent.CopyCode(state.converterState.convertedCode)
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "复制代码")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // ── Source XML Input ────────────────────────────────────
            OutlinedTextField(
                value = state.converterState.sourceXml,
                onValueChange = { viewModel.sendIntent(MigrationToolkitIntent.InputXml(it)) },
                label = { Text("XML Layout 代码") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Convert Button ─────────────────────────────────────
            androidx.compose.material3.Button(
                onClick = {
                    viewModel.sendIntent(
                        MigrationToolkitIntent.ConvertXml(state.converterState.sourceXml)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.converterState.sourceXml.isNotEmpty() && !state.converterState.isConverting
            ) {
                if (state.converterState.isConverting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("转换为 Compose 代码")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Converted Code Output ──────────────────────────────
            if (state.converterState.convertedCode.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Generated Compose Code",
                                color = Color(0xFFD4D4D4),
                                style = MaterialTheme.typography.labelMedium
                            )
                            IconButton(
                                onClick = {
                                    viewModel.sendIntent(
                                        MigrationToolkitIntent.CopyCode(state.converterState.convertedCode)
                                    )
                                }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    tint = Color(0xFFD4D4D4)
                                )
                            }
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = state.converterState.convertedCode,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFFD4D4D4),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            readOnly = true
                        )
                    }
                }
            }
        }
    }
}

// ================================================================
// MappingScreen — 组件映射页
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MappingScreen(
    state: MigrationToolkitState,
    viewModel: MigrationToolkitViewModel,
    onNavigateBack: (() -> Unit)?
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredMappings = SIMULATED_COMPONENT_MAPPINGS.filter {
        searchQuery.isBlank() ||
        it.viewsName.contains(searchQuery, ignoreCase = true) ||
        it.composeImport.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 MDC Views → Compose 组件映射") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search Bar ─────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("搜索组件名称...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            // ── Mapping List ───────────────────────────────────────
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredMappings) { mapping ->
                    MappingCard(
                        mapping = mapping,
                        onCopyNote = {
                            viewModel.sendIntent(
                                MigrationToolkitIntent.CopyCode(mapping.migrationNote)
                            )
                        }
                    )
                }
            }
        }
    }
}

// ================================================================
// ThemingScreen — Theming 迁移指南页
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemingScreen(
    viewModel: MigrationToolkitViewModel,
    onNavigateBack: (() -> Unit)?
) {
    var selectedThemingItem by remember { mutableStateOf(THEMING_MIGRATION_GUIDE.first()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎨 Material Theming 迁移指南") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Category Tabs ──────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = THEMING_MIGRATION_GUIDE.indexOf(selectedThemingItem),
                modifier = Modifier.fillMaxWidth()
            ) {
                THEMING_MIGRATION_GUIDE.forEachIndexed { index, item ->
                    Tab(
                        selected = selectedThemingItem.id == item.id,
                        onClick = { selectedThemingItem = item },
                        text = {
                            Text(
                                when (item.id) {
                                    "colors" -> "🎨 colors.xml"
                                    "dimens" -> "📏 dimens.xml"
                                    "strings" -> "📝 strings.xml"
                                    "theme-xml" -> "🌈 themes.xml"
                                    else -> item.title
                                }
                            )
                        }
                    )
                }
            }

            // ── Code Comparison ────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = selectedThemingItem.title,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Before
                Text("【迁移前】❌", style = MaterialTheme.typography.labelLarge, color = Color(0xFFFF6B6B))
                Spacer(modifier = Modifier.height(8.dp))
                CodeBlock(code = selectedThemingItem.beforeCode)

                Spacer(modifier = Modifier.height(24.dp))

                // After
                Text("【迁移后】✅", style = MaterialTheme.typography.labelLarge, color = Color(0xFF4CAF50))
                Spacer(modifier = Modifier.height(8.dp))
                CodeBlock(code = selectedThemingItem.afterCode)
            }
        }
    }
}

// ================================================================
// HybridScreen — 混合 App 渐进迁移策略页
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HybridScreen(
    onNavigateBack: (() -> Unit)?
) {
    var expandedPhase by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏗️ 混合 App 渐进迁移策略") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "渐进迁移检查清单 — Views + Compose 共存期",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            HYBRID_MIGRATION_CHECKLIST.forEach { checkItem ->
                item {
                    HybridPhaseCard(
                        checkItem = checkItem,
                        isExpanded = expandedPhase == checkItem.phase,
                        onToggle = {
                            expandedPhase = if (expandedPhase == checkItem.phase) null else checkItem.phase
                        }
                    )
                }
            }
        }
    }
}

// ================================================================
// MdcMaintenanceScreen — MDC 维护模式解读页
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MdcMaintenanceScreen(
    onNavigateBack: (() -> Unit)?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 MDC-Android 维护模式解读") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ── Status Banner ──────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚠️ MDC-Android 已进入维护模式", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "自 2026年5月19日 Google I/O 2026 起，Material Views (MDC-Android) 正式不再开发新功能。",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Timeline ───────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📅 停更时间线预测", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    TimelineItem(
                        date = "2026-05-19",
                        title = "进入维护模式",
                        description = "MDC-Android 1.14.0 最终稳定版发布，Views 库不再开发新功能",
                        isPast = true
                    )
                    TimelineItem(
                        date = "2027-Q1",
                        title = "关键 Bug 修复终止（预测）",
                        description = "预计关键 Bug 修复 SLA 终止，只接收安全修复",
                        isPast = false
                    )
                    TimelineItem(
                        date = "2028-Q1",
                        title = "安全修复终止（预测）",
                        description = "预计安全修复也终止，MDC-Android 正式结束生命周期",
                        isPast = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── What This Means ────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔍 这意味着什么", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(
                        "✅ 现有 MDC Views 代码可以继续使用，不需要立即删除",
                        "⚠️ 新功能不会在 Views 库中实现，只会出现在 Compose",
                        "⚠️ Material 3 (M3) Expressive APIs 只在 Compose 中提供",
                        "✅ MDC-Android 1.14.0 是 LTS（长期支持）版本，可安全使用",
                        "✅ Google Play 不会强制下架使用 MDC Views 的 App",
                        "💡 建议：制定从 Views 迁移到 Compose 的计划，优先级适中"
                    ).forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(item, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// M2M3Screen — Material 2 → 3 迁移页
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun M2M3Screen(
    onNavigateBack: (() -> Unit)?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⬆️ Material 2 → 3 迁移指南") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Material 2 → 3 Breaking Changes",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📦 依赖变更", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeBlock(
                        code = """// build.gradle.kts
// ❌ Material 2 (deprecated)
implementation("com.google.android.material:material:1.11.0")

// ✅ Material 3 (Compose only after M3 Expressive)
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.material3:material3")"""
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔄 Theme API 变更", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Material 3 使用新的 colorScheme / typography / shape 参数，" +
                        "不再使用 MaterialTheme.colors / MaterialTheme.typography。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeBlock(
                        code = """// ✅ Material 3 Theme
MaterialTheme(
    colorScheme = MaterialTheme.colorScheme, // 使用 M3 colorScheme
    typography = Typography(),               // 使用 M3 Typography
    shapes = Shapes()                        // 使用 M3 Shapes
) { ... }"""
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎨 Dynamic Color (Material You)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeBlock(
                        code = """// ✅ Dynamic Color in M3
val colorScheme = when {
    dynamicColor -> dynamicDarkColorScheme(context)
    darkTheme -> darkColorScheme()
    else -> lightColorScheme()
}

MaterialTheme(colorScheme = colorScheme) { ... }"""
                    )
                }
            }
        }
    }
}

// ================================================================
// Shared UI Components
// ================================================================

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ComplexityRating(score: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("复杂度:", style = MaterialTheme.typography.labelMedium)
        repeat(5) { index ->
            Text(
                text = if (index < score) "⭐" else "☆",
                fontSize = 16.sp
            )
        }
        Text(
            text = " ($score/5)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PriorityBadge(priority: Priority) {
    Surface(
        color = priority.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = priority.label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = priority.color
        )
    }
}

@Composable
private fun StatusBadge(status: ToolStatus) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.emoji + " " + status.label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
private fun CategoryChip(category: ToolCategory) {
    val (emoji, label) = when (category) {
        ToolCategory.ASSESSMENT -> "🔍" to "评估"
        ToolCategory.CONVERTER -> "🔄" to "转换"
        ToolCategory.GUIDE -> "📖" to "指南"
    }
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "$emoji $label",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun ComponentStatRow(stat: ComponentStat, maxCount: Int) {
    val fraction = stat.count.toFloat() / maxCount.toFloat()
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stat.componentName, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${stat.count}x",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = stat.migrateEffort.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = stat.migrateEffort.label,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = stat.migrateEffort.color
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun RecommendationCard(rec: PriorityRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = rec.priority.color.copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(rec.priority.emoji, fontSize = 18.sp)
                Text(
                    rec.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "⏱ ${rec.estimatedHours}h",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                rec.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "涉及: ${rec.targetComponents.joinToString(", ")}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MappingCard(
    mapping: ComponentMapping,
    onCopyNote: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    mapping.viewsName + " → " + mapping.composeImport.substringAfterLast("."),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = mapping.effort.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = mapping.effort.label + "难度",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = mapping.effort.color
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                mapping.migrationNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                mapping.viewsImport,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            )
            Text(
                mapping.composeImport,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = onCopyNote) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("复制迁移说明", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF1E1E1E)
        )
    }
}

@Composable
private fun HybridPhaseCard(
    checkItem: HybridCheckItem,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    checkItem.phase,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.List else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    checkItem.tasks.forEach { task ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("✅", modifier = Modifier.padding(end = 8.dp))
                            Text(
                                task,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(
    date: String,
    title: String,
    description: String,
    isPast: Boolean
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            Text(
                date,
                style = MaterialTheme.typography.labelSmall,
                color = if (isPast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MdcMaintenanceBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠️", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "MDC-Android 维护模式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Google I/O 2026 正式宣布 Material Views 进入维护模式，Compose 是唯一新功能方向。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.Button(
                onClick = { /* Navigate to MDC maintenance page */ },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("查看详情", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}