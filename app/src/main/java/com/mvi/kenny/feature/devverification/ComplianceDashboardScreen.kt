package com.mvi.kenny.feature.devverification

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.devverification.DevVerificationIntent.*
import com.mvi.kenny.feature.devverification.DevVerificationEffect
import kotlinx.coroutines.flow.collectLatest
import java.time.format.DateTimeFormatter

/**
 * ============================================================
 * PRD-224 | Android 开发者身份验证合规工具包
 * ComplianceDashboardScreen — Main entry point with 5-Tab MVI architecture
 * ============================================================
 * Design: Section 3 — 页面结构 (Page Structure)
 * 5 Tabs: 状态检测 | 注册指南 | 密钥工具 | CI合规 | 决策参考
 * MVI: DevVerificationIntent / DevVerificationState / DevVerificationEffect
 * Bilingual comments: CN + EN
 */

// ─────────────────────────────────────────────────────────────────
// Color System (from Design Spec Section 7)
// ─────────────────────────────────────────────────────────────────
private object DevVerifColors {
    val AndroidGreen = Color(0xFF3DDC84)
    val Warning = Color(0xFFFFB74D)
    val Danger = Color(0xFFEF5350)
    val Success = Color(0xFF66BB6A)
    val Surface = Color(0xFF1E1E1E)
    val SurfaceVariant = Color(0xFF2D2D2D)
    val OnSurface = Color(0xFFE0E0E0)
    val OnSurfaceVariant = Color(0xFF9E9E9E)
    val Outline = Color(0xFF424242)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceDashboardScreen(
    viewModel: DevVerificationViewModel = viewModel(),
    onNavigateToWizard: () -> Unit = {},
    onNavigateToMDM: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    // Collect one-time effects / 收集一次性副作用
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is DevVerificationEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
                is DevVerificationEffect.ShowError -> snackbarHostState.showSnackbar("❌ ${effect.message}")
                is DevVerificationEffect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.text))
                    snackbarHostState.showSnackbar("Copied to clipboard")
                }
                is DevVerificationEffect.OpenExternalUrl -> { /* Open URL via Intent */ }
                is DevVerificationEffect.ScanComplete -> snackbarHostState.showSnackbar("✅ Scan complete")
                is DevVerificationEffect.KeyProcessComplete -> { /* Handled by ShowToast */ }
                is DevVerificationEffect.ConfigApplied -> { /* Handled by ShowToast */ }
            }
        }
    }

    // ── Tab definitions ──
    val tabs = listOf(
        "状态检测" to Icons.Default.Search,
        "注册指南" to Icons.Default.MenuBook,
        "密钥工具" to Icons.Default.Key,
        "CI合规" to Icons.Default.Terminal,
        "决策参考" to Icons.Default.Help
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size }, initialPage = state.selectedTab)

    // Sync pager with state / 同步分页器与状态
    LaunchedEffect(state.selectedTab) {
        if (pagerState.currentPage != state.selectedTab) {
            pagerState.animateScrollToPage(state.selectedTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.selectedTab) {
            viewModel.processIntent(SelectTab(pagerState.currentPage))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = DevVerifColors.AndroidGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Dev 验证工具包",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DevVerifColors.Surface
                ),
                actions = {
                    // PRD badge / PRD 编号徽章
                    Surface(
                        color = DevVerifColors.AndroidGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "PRD-224",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = DevVerifColors.AndroidGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DevVerifColors.Surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Tab Row ──
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = DevVerifColors.SurfaceVariant,
                contentColor = DevVerifColors.OnSurface,
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = DevVerifColors.AndroidGreen
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            viewModel.processIntent(SelectTab(index))
                        },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.height(2.dp))
                                Text(title, fontSize = 11.sp, maxLines = 1)
                            }
                        },
                        selectedContentColor = DevVerifColors.AndroidGreen,
                        unselectedContentColor = DevVerifColors.OnSurfaceVariant
                    )
                }
            }

            // ── Tab Content ──
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> StatusDetectionTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    1 -> RegistrationGuideTab(
                        state = state,
                        steps = viewModel.getCurrentGuideSteps(),
                        onIntent = viewModel::processIntent
                    )
                    2 -> KeyToolsTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    3 -> CIComplianceTab(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    4 -> DecisionReferenceTab(
                        state = state,
                        faqItems = viewModel.getFaqItems(),
                        onIntent = viewModel::processIntent
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// Tab 0: 状态检测 (Verification Status)
// ══════════════════════════════════════════════════════════════

@Composable
private fun StatusDetectionTab(
    state: DevVerificationState,
    onIntent: (DevVerificationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DevVerifColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Input Mode Toggle / 输入模式切换
        Text(
            text = "APK 扫描工具",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DevVerifColors.OnSurface
        )
        Text(
            text = "扫描 APK 文件或输入包名，检测 App 是否已在 Android Developer Console 完成注册",
            style = MaterialTheme.typography.bodySmall,
            color = DevVerifColors.OnSurfaceVariant
        )

        // Input mode selector / 输入模式选择器
        var inputMode by remember { mutableStateOf("package") } // "apk" | "package"

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = inputMode == "package",
                onClick = {
                    inputMode = "package"
                    onIntent(SetScanInput(ScanInput.PackageName("")))
                },
                label = { Text("Package Name") },
                leadingIcon = if (inputMode == "package") {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DevVerifColors.AndroidGreen.copy(alpha = 0.2f),
                    selectedLabelColor = DevVerifColors.AndroidGreen
                )
            )
            FilterChip(
                selected = inputMode == "apk",
                onClick = {
                    inputMode = "apk"
                    onIntent(SetScanInput(ScanInput.ApkFile("", "Select APK...")))
                },
                label = { Text("APK File") },
                leadingIcon = if (inputMode == "apk") {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DevVerifColors.AndroidGreen.copy(alpha = 0.2f),
                    selectedLabelColor = DevVerifColors.AndroidGreen
                )
            )
        }

        // Input field / 输入字段
        when (val input = state.scanInput) {
            is ScanInput.PackageName -> {
                OutlinedTextField(
                    value = input.name,
                    onValueChange = { onIntent(SetScanInput(ScanInput.PackageName(it))) },
                    label = { Text("Package Name") },
                    placeholder = { Text("com.example.myapp") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Android, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DevVerifColors.AndroidGreen,
                        cursorColor = DevVerifColors.AndroidGreen
                    )
                )
            }
            is ScanInput.ApkFile -> {
                OutlinedCard(
                    onClick = { /* File picker would open here */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Android,
                            contentDescription = null,
                            tint = DevVerifColors.AndroidGreen,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("APK File", fontWeight = FontWeight.Medium)
                            Text(
                                input.fileName,
                                style = MaterialTheme.typography.bodySmall,
                                color = DevVerifColors.OnSurfaceVariant
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                    }
                }
            }
        }

        // Scan button / 扫描按钮
        Button(
            onClick = { onIntent(StartScan) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isScanning,
            colors = ButtonDefaults.buttonColors(
                containerColor = DevVerifColors.AndroidGreen
            )
        ) {
            if (state.isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("扫描中...", color = Color.Black)
            } else {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("开始扫描", color = Color.Black)
            }
        }

        // Error display / 错误展示
        state.scanError?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DevVerifColors.Danger.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = DevVerifColors.Danger)
                    Spacer(Modifier.width(8.dp))
                    Text(error, color = DevVerifColors.Danger, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Scan Result / 扫描结果
        state.scanResult?.let { result ->
            ScanResultCard(result = result)
        }

        // Status Legend / 状态说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DevVerifColors.SurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("状态说明", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                StatusLegendItem(color = DevVerifColors.Success, label = "已注册", desc = "App 已完成开发者验证注册")
                StatusLegendItem(color = DevVerifColors.Danger, label = "未注册", desc = "App 尚未完成验证注册")
                StatusLegendItem(color = DevVerifColors.Warning, label = "未知", desc = "无法自动检测，请登录 Console 确认")
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "⚠️ 注意：目前无官方公开 API 可自动查询注册状态，结果仅供参考",
            style = MaterialTheme.typography.bodySmall,
            color = DevVerifColors.OnSurfaceVariant
        )
    }
}

@Composable
private fun ScanResultCard(result: ApkScanResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifColors.SurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Android,
                        contentDescription = null,
                        tint = DevVerifColors.AndroidGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(result.appName, fontWeight = FontWeight.Bold)
                        Text(
                            result.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = DevVerifColors.OnSurfaceVariant
                        )
                    }
                }

                // Status Badge / 状态徽章
                StatusBadge(status = result.verificationStatus)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DevVerifColors.Outline)

            // Details / 详细信息
            Spacer(Modifier.height(12.dp))
            ResultRow("Version", "${result.versionName} (${result.versionCode})")
            ResultRow("Package", result.packageName)
            ResultRow("签名算法", result.signatureAlgorithm)
            result.validFrom?.let { from ->
                result.validUntil?.let { until ->
                    ResultRow("证书有效期", "${from} ~ ${until}")
                }
            }

            Spacer(Modifier.height(8.dp))

            // Fingerprint / 指纹
            Card(
                colors = CardDefaults.cardColors(containerColor = DevVerifColors.Surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SHA-256 Fingerprint", style = MaterialTheme.typography.labelSmall, color = DevVerifColors.OnSurfaceVariant)
                        IconButton(onClick = { /* Copy */ }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = DevVerifColors.OnSurfaceVariant, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(
                        text = result.signatureFingerprint,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = DevVerifColors.AndroidGreen
                    )
                }
            }

            // Match result / 匹配结果
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (result.signatureMatch) {
                        SignatureMatch.MATCHED -> Icons.Default.CheckCircle
                        SignatureMatch.MISMATCHED -> Icons.Default.Warning
                        SignatureMatch.NO_SIGNATURE -> Icons.Default.Help
                        SignatureMatch.UNKNOWN -> Icons.Default.HelpOutline
                    },
                    contentDescription = null,
                    tint = when (result.signatureMatch) {
                        SignatureMatch.MATCHED -> DevVerifColors.Success
                        SignatureMatch.MISMATCHED -> DevVerifColors.Warning
                        else -> DevVerifColors.OnSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    when (result.signatureMatch) {
                        SignatureMatch.MATCHED -> "签名与注册记录匹配"
                        SignatureMatch.MISMATCHED -> "签名与注册记录不匹配"
                        SignatureMatch.NO_SIGNATURE -> "无法获取 APK 签名"
                        SignatureMatch.UNKNOWN -> "签名状态未知"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = DevVerifColors.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: VerificationStatus) {
    val (color, label) = when (status) {
        VerificationStatus.REGISTERED -> DevVerifColors.Success to "已注册"
        VerificationStatus.UNREGISTERED -> DevVerifColors.Danger to "未注册"
        VerificationStatus.UNKNOWN -> DevVerifColors.Warning to "未知"
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = DevVerifColors.OnSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, color = DevVerifColors.OnSurface)
    }
}

@Composable
private fun StatusLegendItem(color: Color, label: String, desc: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text("$label — $desc", style = MaterialTheme.typography.bodySmall, color = DevVerifColors.OnSurfaceVariant)
    }
}

// ══════════════════════════════════════════════════════════════
// Tab 1: 注册指南 (Registration Guide)
// ══════════════════════════════════════════════════════════════

@Composable
private fun RegistrationGuideTab(
    state: DevVerificationState,
    steps: List<GuideStep>,
    onIntent: (DevVerificationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DevVerifColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "注册指南",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DevVerifColors.OnSurface
        )

        // Path selector / 路径选择器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GuidePath.entries.forEach { path ->
                FilterChip(
                    selected = state.selectedGuidePath == path,
                    onClick = { onIntent(SelectGuidePath(path)) },
                    label = { Text(path.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = if (state.selectedGuidePath == path) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DevVerifColors.AndroidGreen.copy(alpha = 0.2f),
                        selectedLabelColor = DevVerifColors.AndroidGreen
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // URL card / 链接卡片
        Card(
            onClick = { /* Open URL */ },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DevVerifColors.SurfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = DevVerifColors.AndroidGreen)
                Spacer(Modifier.width(8.dp))
                Text(
                    state.selectedGuidePath.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = DevVerifColors.AndroidGreen,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Accordion steps / 折叠步骤
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(steps) { step ->
                AccordionCard(
                    step = step,
                    isExpanded = state.expandedStep == step.index,
                    onToggle = { onIntent(ToggleStep(step.index)) }
                )
            }
        }
    }
}

@Composable
private fun AccordionCard(
    step: GuideStep,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                DevVerifColors.AndroidGreen.copy(alpha = 0.1f)
            else
                DevVerifColors.SurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isExpanded) DevVerifColors.AndroidGreen
                                else DevVerifColors.Surface
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${step.index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isExpanded) Color.Black else DevVerifColors.OnSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(step.title, fontWeight = FontWeight.Medium, color = DevVerifColors.OnSurface)
                        if (!isExpanded) {
                            Text(
                                step.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = DevVerifColors.OnSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = DevVerifColors.OnSurfaceVariant
                )
            }

            // Expanded content / 展开内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(start = 38.dp, top = 12.dp)) {
                    Text(
                        step.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = DevVerifColors.OnSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    step.details.forEach { detail ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", color = DevVerifColors.AndroidGreen)
                            Text(detail, style = MaterialTheme.typography.bodySmall, color = DevVerifColors.OnSurface)
                        }
                    }
                    step.url?.let { url ->
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = DevVerifColors.AndroidGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                url,
                                style = MaterialTheme.typography.bodySmall,
                                color = DevVerifColors.AndroidGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// Tab 2: 密钥工具 (Key Tools)
// ══════════════════════════════════════════════════════════════

@Composable
private fun KeyToolsTab(
    state: DevVerificationState,
    onIntent: (DevVerificationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DevVerifColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "签名密钥工具",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DevVerifColors.OnSurface
        )
        Text(
            text = "从 keystore 提取证书信息、计算指纹、生成注册文本",
            style = MaterialTheme.typography.bodySmall,
            color = DevVerifColors.OnSurfaceVariant
        )

        // Mode selector / 模式选择器
        ScrollableTabRow(
            selectedTabIndex = KeyToolMode.entries.indexOf(state.keyToolMode),
            containerColor = DevVerifColors.SurfaceVariant,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            KeyToolMode.entries.forEach { mode ->
                Tab(
                    selected = state.keyToolMode == mode,
                    onClick = { onIntent(SetKeyToolMode(mode)) },
                    text = { Text(mode.displayName, fontSize = 12.sp) },
                    selectedContentColor = DevVerifColors.AndroidGreen,
                    unselectedContentColor = DevVerifColors.OnSurfaceVariant
                )
            }
        }

        // Keystore file selector / Keystore 文件选择
        OutlinedCard(
            onClick = { /* File picker */ },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DevVerifColors.SurfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = DevVerifColors.AndroidGreen)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Keystore 文件", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        state.keystoreFile?.fileName ?: "选择 .jks 或 .keystore 文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = DevVerifColors.OnSurfaceVariant
                    )
                }
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = DevVerifColors.OnSurfaceVariant)
            }
        }

        // Password fields / 密码字段
        OutlinedTextField(
            value = state.keystorePassword,
            onValueChange = { onIntent(SetKeystorePassword(it)) },
            label = { Text("Keystore Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DevVerifColors.AndroidGreen,
                cursorColor = DevVerifColors.AndroidGreen
            )
        )

        OutlinedTextField(
            value = state.keyAlias,
            onValueChange = { onIntent(SetKeyAlias(it)) },
            label = { Text("Key Alias") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DevVerifColors.AndroidGreen,
                cursorColor = DevVerifColors.AndroidGreen
            )
        )

        OutlinedTextField(
            value = state.keyPassword,
            onValueChange = { onIntent(SetKeyPassword(it)) },
            label = { Text("Key Password (可选)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Password, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DevVerifColors.AndroidGreen,
                cursorColor = DevVerifColors.AndroidGreen
            )
        )

        // Process button / 处理按钮
        Button(
            onClick = { onIntent(ProcessKeystore) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isProcessingKey,
            colors = ButtonDefaults.buttonColors(containerColor = DevVerifColors.AndroidGreen)
        ) {
            if (state.isProcessingKey) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("处理中...", color = Color.Black)
            } else {
                Icon(Icons.Default.Build, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("处理密钥", color = Color.Black)
            }
        }

        // Error display / 错误展示
        state.keyProcessError?.let { error ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DevVerifColors.Danger.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = DevVerifColors.Danger)
                    Spacer(Modifier.width(8.dp))
                    Text(error, color = DevVerifColors.Danger, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Output / 输出结果
        state.keyToolOutput?.let { output ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                colors = CardDefaults.cardColors(containerColor = DevVerifColors.SurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("输出结果", fontWeight = FontWeight.Bold, color = DevVerifColors.AndroidGreen)
                        IconButton(onClick = { /* Copy */ }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = DevVerifColors.OnSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    output.certificateInfo?.let { cert ->
                        Text("证书信息", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(4.dp))
                        ResultRow("Subject", cert.subjectDN)
                        ResultRow("Issuer", cert.issuerDN)
                        ResultRow("算法", cert.signatureAlgorithm)
                        ResultRow("公钥", "${cert.publicKeyAlgorithm} ${cert.publicKeySize} bits")
                        ResultRow("有效期", "${cert.validFrom} ~ ${cert.validUntil}")
                        ResultRow("Serial", cert.serialNumber)
                    }

                    output.fingerprint?.let { fp ->
                        Spacer(Modifier.height(12.dp))
                        Text("SHA-256 Fingerprint", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(4.dp))
                        SelectionContainer {
                            Text(
                                fp,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = DevVerifColors.AndroidGreen
                            )
                        }
                    }

                    output.registrationInfo?.let { info ->
                        Spacer(Modifier.height(12.dp))
                        Text("注册信息（可直接粘贴）", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(4.dp))
                        SelectionContainer {
                            Text(
                                info,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = DevVerifColors.OnSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// Tab 3: CI 合规 (CI Compliance)
// ══════════════════════════════════════════════════════════════

@Composable
private fun CIComplianceTab(
    state: DevVerificationState,
    onIntent: (DevVerificationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DevVerifColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "CI 合规检测",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DevVerifColors.OnSurface
        )
        Text(
            text = "配置 Gradle 插件，阻塞未合规的 CI 构建",
            style = MaterialTheme.typography.bodySmall,
            color = DevVerifColors.OnSurfaceVariant
        )

        // Check button / 检查按钮
        Button(
            onClick = { onIntent(GenerateComplianceReport) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isCheckingCompliance,
            colors = ButtonDefaults.buttonColors(containerColor = DevVerifColors.AndroidGreen)
        ) {
            if (state.isCheckingCompliance) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("检查中...", color = Color.Black)
            } else {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("生成合规报告", color = Color.Black)
            }
        }

        // Compliance result / 合规结果
        state.complianceResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DevVerifColors.SurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("合规等级", fontWeight = FontWeight.Bold)
                        Surface(
                            color = when (result.complianceLevel) {
                                ComplianceLevel.COMPLIANT -> DevVerifColors.Success.copy(alpha = 0.15f)
                                ComplianceLevel.WARNING -> DevVerifColors.Warning.copy(alpha = 0.15f)
                                ComplianceLevel.NON_COMPLIANT -> DevVerifColors.Danger.copy(alpha = 0.15f)
                                ComplianceLevel.UNKNOWN -> DevVerifColors.OnSurfaceVariant.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = result.complianceLevel.label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = when (result.complianceLevel) {
                                    ComplianceLevel.COMPLIANT -> DevVerifColors.Success
                                    ComplianceLevel.WARNING -> DevVerifColors.Warning
                                    ComplianceLevel.NON_COMPLIANT -> DevVerifColors.Danger
                                    ComplianceLevel.UNKNOWN -> DevVerifColors.OnSurfaceVariant
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("验证检查", style = MaterialTheme.typography.labelSmall, color = DevVerifColors.OnSurfaceVariant)
                            Text(if (result.hasVerificationCheck) "已配置" else "未配置", color = if (result.hasVerificationCheck) DevVerifColors.Success else DevVerifColors.Danger)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("插件版本", style = MaterialTheme.typography.labelSmall, color = DevVerifColors.OnSurfaceVariant)
                            Text(result.gradlePluginVersion ?: "未检测到", color = DevVerifColors.OnSurface)
                        }
                    }

                    if (result.suggestions.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("建议", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
                        result.suggestions.forEach { suggestion ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", color = DevVerifColors.Warning)
                                Text(suggestion, style = MaterialTheme.typography.bodySmall, color = DevVerifColors.OnSurface)
                            }
                        }
                    }
                }
            }
        }

        // Gradle Config Code Block / Gradle 配置代码块
        Text("Gradle 配置", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "build.gradle.kts",
                        style = MaterialTheme.typography.labelSmall,
                        color = DevVerifColors.OnSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = { onIntent(ApplyGradleConfigToProject) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = DevVerifColors.AndroidGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                SelectionContainer {
                    Text(
                        state.gradleConfig,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = DevVerifColors.AndroidGreen
                    )
                }
            }
        }

        // Apply button / 应用按钮
        OutlinedButton(
            onClick = { onIntent(ApplyGradleConfigToProject) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("复制配置到剪贴板")
        }
    }
}

// ══════════════════════════════════════════════════════════════
// Tab 4: 决策参考 (Decision Reference)
// ══════════════════════════════════════════════════════════════

@Composable
private fun DecisionReferenceTab(
    state: DevVerificationState,
    faqItems: List<FaqItem>,
    onIntent: (DevVerificationIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DevVerifColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "决策参考",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DevVerifColors.OnSurface
        )
        Text(
            text = "密钥轮换 / 企业分发 / 多 APK 策略 + 常见开发者困惑 FAQ",
            style = MaterialTheme.typography.bodySmall,
            color = DevVerifColors.OnSurfaceVariant
        )

        // Decision module cards / 决策模块入口卡片
        Text("子模块", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DecisionModule.entries.forEach { module ->
                Card(
                    onClick = { onIntent(SelectDecisionModule(module)) },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.selectedDecisionModule == module)
                            DevVerifColors.AndroidGreen.copy(alpha = 0.15f)
                        else
                            DevVerifColors.SurfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            when (module) {
                                DecisionModule.KEY_ROTATION -> Icons.Default.Refresh
                                DecisionModule.ENTERPRISE_DISTRIBUTION -> Icons.Default.Business
                                DecisionModule.MULTI_APK -> Icons.Default.Widgets
                            },
                            contentDescription = null,
                            tint = if (state.selectedDecisionModule == module)
                                DevVerifColors.AndroidGreen
                            else
                                DevVerifColors.OnSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            module.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = DevVerifColors.OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Selected module detail / 选中模块详情
        state.selectedDecisionModule?.let { module ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DevVerifColors.SurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(module.displayName, fontWeight = FontWeight.Bold, color = DevVerifColors.AndroidGreen)
                    Spacer(Modifier.height(4.dp))
                    Text(module.description, style = MaterialTheme.typography.bodySmall, color = DevVerifColors.OnSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    when (module) {
                        DecisionModule.KEY_ROTATION -> {
                            KeyRotationGuide()
                        }
                        DecisionModule.ENTERPRISE_DISTRIBUTION -> {
                            EnterpriseDistributionGuide()
                        }
                        DecisionModule.MULTI_APK -> {
                            MultiApkGuide()
                        }
                    }
                }
            }
        }

        // FAQ Section / FAQ 区域
        Text("常见开发者困惑 FAQ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(faqItems) { faq ->
                FaqCard(
                    faq = faq,
                    isExpanded = faq.index in state.faqExpandedItems,
                    onToggle = { onIntent(ToggleFaqItem(faq.index)) }
                )
            }
        }
    }
}

@Composable
private fun KeyRotationGuide() {
    Column {
        GuideBullet("已注册密钥不可随意更换（合规要求）")
        GuideBullet("密钥泄露时：联系 Google Support 申请特殊处理")
        GuideBullet("计划轮换：提前6个月在 Console 注册新密钥")
        GuideBullet("临时方案：使用 keyAlias 切换到备用密钥（需重新注册）")
    }
}

@Composable
private fun EnterpriseDistributionGuide() {
    Column {
        GuideBullet("Google Endpoint Verification API 可验证企业设备上的 App")
        GuideBullet("企业签名的 App 需额外注册企业签名证书")
        GuideBullet("MDM 部署场景建议完成开发者验证以避免用户端警告")
        GuideBullet("联系 Google Enterprise Support 获取企业场景具体指引")
    }
}

@Composable
private fun MultiApkGuide() {
    Column {
        GuideBullet("每个签名密钥需单独注册")
        GuideBullet("列出项目中所有 APK variant (debug/release/arm64/x86)")
        GuideBullet("每个 package name 需独立注册（即使同一 App）")
        GuideBullet("不同 variant 使用不同密钥时，确保每个密钥都已注册")
    }
}

@Composable
private fun GuideBullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("• ", color = DevVerifColors.AndroidGreen)
        Text(text, style = MaterialTheme.typography.bodySmall, color = DevVerifColors.OnSurface)
    }
}

@Composable
private fun FaqCard(
    faq: FaqItem,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                DevVerifColors.AndroidGreen.copy(alpha = 0.08f)
            else
                DevVerifColors.SurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    "Q${faq.index + 1}. ${faq.question}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DevVerifColors.OnSurface,
                    fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = DevVerifColors.OnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodySmall,
                    color = DevVerifColors.OnSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// ── Animation Enter/Exit for scan result card ──
private val EnterTransition = fadeIn(animationSpec = tween(300)) +
    slideInVertically(animationSpec = tween(300)) { it / 3 }
private val ExitTransition = fadeOut(animationSpec = tween(300)) +
    slideOutVertically(animationSpec = tween(300)) { it / 3 }