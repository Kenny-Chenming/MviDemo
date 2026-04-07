package com.mvi.kenny.feature.page16kb

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.page16kb.Step as StepEnum

/**
 * ============================================================
 * Page16KbScreen — 16KB Page Size 迁移工具主界面
 * ============================================================
 * 4-step wizard: DETECT → ANALYZE → FIX → VERIFY
 *
 * @param viewModel ViewModel for state management / 状态管理的 ViewModel
 * @param onNavigateBack Callback when user wants to go back / 用户返回时的回调
 *
 * @see Page16KbContract State/Intent/Effect definitions
 * @see Page16KbViewModel ViewModel implementation
 */

// =============================================================
// Colors — 颜色常量
// =============================================================
private val ColorP0 = Color(0xFFD32F2F)      // Red — P0 critical issue
private val ColorP1 = Color(0xFFF57C00)      // Orange — P1 warning
private val ColorSuccess = Color(0xFF388E3C) // Green — passed
private val ColorSurface = Color(0xFFF5F5F5) // Light surface

// =============================================================
// Main Screen
// =============================================================
/**
 * 16KB Migration Tool Screen / 16KB 迁移工具主界面
 *
 * 4-step wizard layout with shared TopAppBar and step indicator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Page16KbScreen(
    viewModel: Page16KbViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Page16KbEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is Page16KbEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is Page16KbEffect.ShowExportSuccess -> {
                    snackbarHostState.showSnackbar("Report saved: ${effect.filePath}")
                }
                is Page16KbEffect.NavigateToSoDetail,
                is Page16KbEffect.ScrollToTop,
                is Page16KbEffect.OpenFilePicker -> {
                    // Handled by navigation or scroll state
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("16KB 迁移助手 / 16KB Migration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Step Progress Indicator / 步骤进度指示器
            StepProgressIndicator(
                currentStep = state.currentStep,
                modifier = Modifier.padding(16.dp)
            )

            // Main Content Area / 主内容区
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "step_animation"
            ) { step ->
                when (step) {
                    StepEnum.DETECT -> DetectStepContent(
                        state = state,
                        onUploadApk = { uri ->
                            viewModel.sendIntent(Page16KbIntent.UploadApk(uri))
                        }
                    )
                    StepEnum.ANALYZE -> AnalyzeStepContent(
                        state = state,
                        onFilterChange = { mode ->
                            viewModel.sendIntent(Page16KbIntent.FilterSoList(mode))
                        },
                        onSoClick = { soInfo ->
                            viewModel.sendIntent(Page16KbIntent.SelectSoDetail(soInfo))
                        },
                        onNextStep = { viewModel.sendIntent(Page16KbIntent.NextStep) }
                    )
                    StepEnum.FIX -> FixStepContent(
                        state = state,
                        onApplyFix = { rec ->
                            viewModel.sendIntent(Page16KbIntent.ApplyFix(rec))
                        },
                        onSearchSdk = { query ->
                            viewModel.sendIntent(Page16KbIntent.SearchSdkCompatibility(query))
                        },
                        onNextStep = { viewModel.sendIntent(Page16KbIntent.NextStep) },
                        onPreviousStep = { viewModel.sendIntent(Page16KbIntent.PreviousStep) }
                    )
                    StepEnum.VERIFY -> VerifyStepContent(
                        state = state,
                        onRunVerification = { uri ->
                            viewModel.sendIntent(Page16KbIntent.RunVerification(uri))
                        },
                        onExport = { isJson ->
                            viewModel.sendIntent(Page16KbIntent.ExportReport(isJson))
                        },
                        onPreviousStep = { viewModel.sendIntent(Page16KbIntent.PreviousStep) }
                    )
                }
            }
        }
    }

    // .so Detail Dialog / .so 详情弹窗
    state.selectedSoDetail?.let { soInfo ->
        SoDetailDialog(
            soFileInfo = soInfo,
            onDismiss = { viewModel.sendIntent(Page16KbIntent.ClearSoDetail) }
        )
    }

    // Error Dialog / 错误弹窗
    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(Page16KbIntent.DismissError) },
            title = { Text("Error / 错误") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.sendIntent(Page16KbIntent.DismissError) }) {
                    Text("OK")
                }
            }
        )
    }
}

// =============================================================
// Step Progress Indicator
// =============================================================
/**
 * Step progress indicator / 步骤进度指示器
 *
 * Visual indicator showing current step in the 4-step wizard.
 */
@Composable
private fun StepProgressIndicator(
    currentStep: StepEnum,
    modifier: Modifier = Modifier
) {
    val steps = StepEnum.entries
    val currentIndex = steps.indexOf(currentStep)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = index < currentIndex
            val isCurrent = index == currentIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Step circle / 步骤圆圈
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> ColorSuccess
                                isCurrent -> MaterialTheme.colorScheme.primary
                                else -> Color.Gray.copy(alpha = 0.3f)
                            }
                        )
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            color = if (isCurrent) Color.White else Color.Gray,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Step label / 步骤标签
                Text(
                    text = step.labelZh,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent || isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Gray,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )

                // Connector line / 连接线
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .height(2.dp)
                            .weight(1f)
                            .background(
                                if (isCompleted) ColorSuccess else Color.Gray.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

// =============================================================
// Step 1: Detect — APK Upload
// =============================================================
/**
 * Step 1: APK Upload / 步骤 1：APK 上传
 *
 * Drag-and-drop zone for APK file selection.
 */
@Composable
private fun DetectStepContent(
    state: Page16KbState,
    onUploadApk: (android.net.Uri) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Deadline Banner / 截止日横幅
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = ColorP0.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = ColorP0,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "⚠️ Google Play 截止日：2026-05-31",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorP0
                    )
                    Text(
                        "All updated APKs must support 16KB page size / 所有更新的 APK 必须支持 16KB page size",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorP0.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Drop Zone / 拖拽上传区域
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 2.dp,
                    color = if (state.isParsingApk)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Gray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(ColorSurface)
                .clickable(enabled = !state.isParsingApk) {
                    // In real app, trigger file picker
                    // 实际应用中触发文件选择器
                    // For demo, simulate with a placeholder URI
                    onUploadApk(android.net.Uri.EMPTY)
                }
        ) {
            if (state.isParsingApk) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        progress = { state.parseProgress },
                        modifier = Modifier.size(64.dp),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "解析 APK... ${(state.parseProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Parsing APK...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "拖拽 APK 文件到这里",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Drag & drop APK here",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "或点击选择文件 / or tap to select",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info text / 说明文字
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "16KB Page Size 是什么？",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Android 16KB page size (from 4KB) is a major memory management change. APKs with .so files not compiled for 16KB will SIGSEGV crash on 16KB devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    "16KB Page Size 是什么？",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    "Android 从 4KB 切换到 16KB page size 是重大底层变更。未针对 16KB 编译的 .so 文件将在 16KB 设备上 SIGSEGV 崩溃。",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// =============================================================
// Step 2: Analyze — Detection Results Dashboard
// =============================================================
/**
 * Step 2: Detection Results Dashboard / 步骤 2：检测结果仪表盘
 *
 * Shows .so alignment analysis with circular progress and filterable list.
 */
@Composable
private fun AnalyzeStepContent(
    state: Page16KbState,
    onFilterChange: (FilterMode) -> Unit,
    onSoClick: (SoFileInfo) -> Unit,
    onNextStep: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Dashboard Cards / 仪表盘卡片
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circular Progress / 环形进度图
            Card(
                modifier = Modifier.size(120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = state.issuePercentage,
                        animationSpec = tween(800),
                        label = "progress"
                    )

                    // Background circle / 背景圆
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(100.dp),
                        strokeWidth = 8.dp,
                        color = Color.Gray.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )

                    // Progress circle / 进度圆
                    CircularProgressIndicator(
                        progress = { 1f - animatedProgress },
                        modifier = Modifier.size(100.dp),
                        strokeWidth = 8.dp,
                        color = if (state.issueCount > 0) ColorP0 else ColorSuccess,
                        strokeCap = StrokeCap.Round
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${state.issueCount}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (state.issueCount > 0) ColorP0 else ColorSuccess
                        )
                        Text(
                            "问题 / Issues",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Stats Cards / 统计卡片
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "APK / APK 信息",
                    value = state.uploadedApkInfo?.packageName ?: "-",
                    subValue = "v${state.uploadedApkInfo?.versionName ?: "-"}",
                    icon = Icons.Default.Android,
                    iconColor = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    label = "总 .so / Total",
                    value = "${state.totalCount}",
                    subValue = "第三方: ${state.soList.count { it.isFromThirdParty }}",
                    icon = Icons.Default.Architecture,
                    iconColor = ColorSuccess
                )
            }
        }

        // Filter Chips / 过滤 Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterMode.entries.forEach { mode ->
                FilterChip(
                    selected = state.filterMode == mode,
                    onClick = { onFilterChange(mode) },
                    label = { Text(mode.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        // .so List / .so 列表
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.filteredSoList, key = { it.name }) { soInfo ->
                SoIssueCard(
                    soFileInfo = soInfo,
                    onClick = { onSoClick(soInfo) }
                )
            }
        }

        // Bottom Action Bar / 底部操作栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onNextStep,
                enabled = state.soList.isNotEmpty()
            ) {
                Text("下一步 / Next Step")
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

/**
 * Statistics card / 统计卡片
 */
@Composable
private fun StatCard(
    label: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subValue, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

// =============================================================
// Step 3: Fix — Fix Recommendations
// =============================================================
/**
 * Step 3: Fix Recommendations / 步骤 3：修复方案
 *
 * Shows fix cards for each problem .so with SDK compatibility search.
 */
@Composable
private fun FixStepContent(
    state: Page16KbState,
    onApplyFix: (FixRecommendation) -> Unit,
    onSearchSdk: (String) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit
) {
    var sdkQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Fix Summary Banner / 修复摘要横幅
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (state.issueCount > 0)
                    ColorP0.copy(alpha = 0.1f)
                else
                    ColorSuccess.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (state.issueCount > 0) Icons.Default.BugReport else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (state.issueCount > 0) ColorP0 else ColorSuccess,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        if (state.issueCount > 0)
                            "发现 ${state.issueCount} 个问题 .so / Found ${state.issueCount} problem .so files"
                        else
                            "所有 .so 均已对齐！/ All .so files are aligned!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (state.issueCount > 0)
                            "按照以下方案修复 / Follow the recommendations below to fix"
                        else
                            "Ready for verification / 准备验证",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        // SDK Compatibility Search / SDK 兼容性搜索
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "SDK 兼容性查询 / SDK Compatibility Search",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = sdkQuery,
                    onValueChange = {
                        sdkQuery = it
                        onSearchSdk(it)
                    },
                    placeholder = { Text("搜索 SDK 名称... / Search SDK name...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // SDK Search Results / SDK 搜索结果
                if (state.sdkCompatibilityResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    state.sdkCompatibilityResults.take(3).forEach { sdk ->
                        SdkCompatibilityItem(sdk)
                    }
                }
            }
        }

        // Fix Recommendations List / 修复建议列表
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.fixRecommendations, key = { it.soFileInfo.name }) { rec ->
                FixActionCard(
                    recommendation = rec,
                    onApplyFix = { onApplyFix(rec) }
                )
            }
        }

        // Bottom Action Bar / 底部操作栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onPreviousStep) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Text(" 上一步", modifier = Modifier.padding(start = 4.dp))
            }
            Button(onClick = onNextStep) {
                Text("验证 / Verify")
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

// =============================================================
// Step 4: Verify — Verification Results
// =============================================================
/**
 * Step 4: Verification Results / 步骤 4：验证结果
 *
 * Shows verification result banner and export options.
 */
@Composable
private fun VerifyStepContent(
    state: Page16KbState,
    onRunVerification: (android.net.Uri) -> Unit,
    onExport: (Boolean) -> Unit,
    onPreviousStep: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Verification Result Banner / 验证结果横幅
        state.verificationResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.isAllPassed)
                        ColorSuccess.copy(alpha = 0.1f)
                    else
                        ColorP0.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (result.isAllPassed) Icons.Default.Verified else Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = if (result.isAllPassed) ColorSuccess else ColorP0
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (result.isAllPassed) "✅ 验证通过 / VERIFIED" else "❌ 验证失败 / VERIFICATION FAILED",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isAllPassed) ColorSuccess else ColorP0,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${result.passedCount}/${result.totalCount} .so files 16KB aligned",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        result.verificationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } ?: run {
            // Not yet verified / 尚未验证
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "运行验证 / Run Verification",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "上传 APK 或点击验证按钮开始 / Upload APK or tap Verify to start",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onRunVerification(android.net.Uri.EMPTY) },
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                        }
                        Text(" 开始验证 / Start Verification", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Export Options / 导出选项
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "导出报告 / Export Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onExport(true) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Text(" JSON", modifier = Modifier.padding(start = 4.dp))
                    }
                    Button(
                        onClick = { onExport(false) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Text(" Markdown", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Back Button / 返回按钮
        TextButton(onClick = onPreviousStep) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Text(" 返回修复步骤 / Back to Fix", modifier = Modifier.padding(start = 4.dp))
        }
    }
}

// =============================================================
// SoIssueCard — .so 问题卡片
// =============================================================
/**
 * .so Issue Card / .so 问题卡片
 *
 * Displays a single .so file's alignment status.
 */
@Composable
private fun SoIssueCard(
    soFileInfo: SoFileInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when (soFileInfo.alignmentStatus) {
                SoAlignmentStatus.MISALIGNED -> ColorP0.copy(alpha = 0.05f)
                SoAlignmentStatus.ALIGNED -> ColorSuccess.copy(alpha = 0.05f)
                SoAlignmentStatus.UNKNOWN -> ColorSurface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon / 状态图标
            Icon(
                imageVector = when (soFileInfo.alignmentStatus) {
                    SoAlignmentStatus.ALIGNED -> Icons.Default.CheckCircle
                    SoAlignmentStatus.MISALIGNED -> Icons.Default.Error
                    SoAlignmentStatus.UNKNOWN -> Icons.Default.Info
                },
                contentDescription = null,
                tint = soFileInfo.alignmentStatus.color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    soFileInfo.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    soFileInfo.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        soFileInfo.architecture,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        " • ${formatFileSize(soFileInfo.size)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    soFileInfo.alignmentStatus.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = soFileInfo.alignmentStatus.color,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    soFileInfo.minNdkVersion,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// =============================================================
// FixActionCard — 修复建议卡片
// =============================================================
/**
 * Fix Action Card / 修复建议卡片
 *
 * Shows fix recommendation for a problem .so.
 */
@Composable
private fun FixActionCard(
    recommendation: FixRecommendation,
    onApplyFix: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        recommendation.soFileInfo.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        recommendation.fixType.name + " / " + when (recommendation.fixType) {
                            FixType.RECOMPILE -> "重新编译"
                            FixType.UPGRADE_SDK -> "升级 SDK"
                            FixType.SUPPRESS -> "Suppression"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Text(
                    recommendation.ndkVersion,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fix Command / 修复命令
            // Code block card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    recommendation.fixCommand,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Note / 备注
            Text(
                recommendation.suppressionNote,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Apply Fix Button / 应用修复按钮
            Button(
                onClick = onApplyFix,
                enabled = recommendation.isAutoFixable,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Build, contentDescription = null)
                Text(
                    if (recommendation.isAutoFixable) "应用修复 / Apply Fix" else "查看文档 / View Docs",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
// =============================================================
// SdkCompatibilityItem — SDK 兼容性项
// =============================================================
/**
 * SDK Compatibility Item / SDK 兼容性项
 *
 * Shows a single SDK's 16KB compatibility status.
 */
@Composable
private fun SdkCompatibilityItem(sdk: SdkCompatibility) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (sdk.isCompatible) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (sdk.isCompatible) ColorSuccess else ColorP1,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                sdk.sdkName + " " + sdk.version,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (sdk.isCompatible)
                    "Compatible / 兼容"
                else
                    "Upgrade to ${sdk.compatibleVersion} / 升级到 ${sdk.compatibleVersion}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

// =============================================================
// SoDetailDialog — .so 详情弹窗
// =============================================================
/**
 * .so Detail Dialog / .so 详情弹窗
 *
 * Shows detailed ELF header information for a .so file.
 */
@Composable
private fun SoDetailDialog(
    soFileInfo: SoFileInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                soFileInfo.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Status Badge / 状态徽章
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (soFileInfo.alignmentStatus) {
                            SoAlignmentStatus.ALIGNED -> Icons.Default.CheckCircle
                            SoAlignmentStatus.MISALIGNED -> Icons.Default.Error
                            SoAlignmentStatus.UNKNOWN -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = soFileInfo.alignmentStatus.color,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        soFileInfo.alignmentStatus.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = soFileInfo.alignmentStatus.color,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider()

                // Details / 详细信息
                DetailRow("Source / 来源", soFileInfo.source)
                DetailRow("Architecture / 架构", soFileInfo.architecture)
                DetailRow("Size / 大小", formatFileSize(soFileInfo.size))
                DetailRow("Min NDK / 最低 NDK", soFileInfo.minNdkVersion)
                DetailRow("Third Party / 第三方", if (soFileInfo.isFromThirdParty) "Yes / 是" else "No / 否")

                HorizontalDivider()

                // ELF Header / ELF 头信息
                Text(
                    "ELF Header / ELF 头信息",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    soFileInfo.elfHeaderInfo.ifEmpty { "N/A" },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                if (soFileInfo.alignmentStatus == SoAlignmentStatus.MISALIGNED) {
                    HorizontalDivider()
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ColorP0.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "⚠️ This .so will crash on 16KB devices",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = ColorP0
                            )
                            Text(
                                "此 .so 将在 16KB 设备上崩溃",
                                style = MaterialTheme.typography.bodySmall,
                                color = ColorP0.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

/**
 * Detail row / 详情行
 */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

// =============================================================
// Helper Functions / 辅助函数
// =============================================================
/**
 * Format file size in bytes to human-readable string / 将字节转换为人类可读的大小字符串
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> String.format("%.1f GB", bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> String.format("%.1f MB", bytes / 1_000_000.0)
        bytes >= 1_000 -> String.format("%.1f KB", bytes / 1_000.0)
        else -> "$bytes B"
    }
}
