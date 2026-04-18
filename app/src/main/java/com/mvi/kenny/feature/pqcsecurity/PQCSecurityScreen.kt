package com.mvi.kenny.feature.pqcsecurity

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// ===== Color Palette — PQC Security Toolkit Theme =====
// ===== 配色方案 — PQC 安全工具包主题 =====

private val PQCPrimary = Color(0xFF6750A4)
private val PQCSecondary = Color(0xFF625B71)
private val PQCTertiary = Color(0xFF7D5260)
private val PQCSurfaceVariant = Color(0xFF2D2D2D)
private val PQCRiskHigh = Color(0xFFB3261E)
private val PQCRiskMedium = Color(0xFFE8A317)
private val PQCRiskLow = Color(0xFF2E7D32)
private val PQCQuantumSafe = Color(0xFF00C853)
private val OnSurfaceLight = Color(0xFFE0E0E0)
private val OnSurfaceDim = Color(0xFF9E9E9E)

// ===== Risk Level Color Mapper =====
// ===== 风险级别颜色映射 =====

@Composable
private fun RiskColor(level: RiskLevel): Color = when (level) {
    RiskLevel.HIGH -> PQCRiskHigh
    RiskLevel.MEDIUM -> PQCRiskMedium
    RiskLevel.LOW -> PQCRiskLow
    RiskLevel.NONE -> PQCQuantumSafe
}

// ===== Signature Strength Color =====
// ===== 签名强度颜色 =====

@Composable
private fun StrengthColor(strength: SignatureStrength): Color = when (strength) {
    SignatureStrength.QUANTUM_SAFE -> PQCQuantumSafe
    SignatureStrength.TRANSITIONAL -> PQCRiskMedium
    SignatureStrength.LEGACY -> PQCRiskHigh
    SignatureStrength.UNKNOWN -> OnSurfaceDim
}

// ===== Migration Step Status Icon & Color =====
// ===== 迁移步骤状态图标和颜色 =====

@Composable
private fun StepStatusIcon(status: MigrationStepStatus): Pair<ImageVector, Color> = when (status) {
    MigrationStepStatus.COMPLETED -> Icons.Default.CheckCircle to PQCRiskLow
    MigrationStepStatus.IN_PROGRESS -> Icons.Default.Schedule to PQCRiskMedium
    MigrationStepStatus.PENDING -> Icons.Default.RadioButtonUnchecked to OnSurfaceDim
    MigrationStepStatus.BLOCKED -> Icons.Default.Block to PQCRiskHigh
}

// ===== Radar Chart Component =====
// ===== 雷达图组件 =====

@Composable
private fun PQCRadarChart(data: RadarChartData, maxValue: Int = 100, modifier: Modifier = Modifier) {
    val dimensions = listOf(
        "Signature Strength" to data.signatureStrength,
        "Algorithm Type" to data.algorithmType,
        "Key Length" to data.keyLength,
        "Keystore Support" to data.keystoreSupport,
        "Play Signing" to data.playSigningCompliance,
        "Migration Ready" to data.migrationReadiness
    )
    val angles = dimensions.mapIndexed { index, _ -> (index * 360.0 / dimensions.size) - 90.0 }

    Canvas(modifier = modifier.size(240.dp)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) * 0.75f

        // Draw grid rings / 绘制网格圆环
        for (level in 1..5) {
            val ringRadius = radius * level / 5f
            drawCircle(color = OnSurfaceDim.copy(alpha = 0.2f), radius = ringRadius, center = androidx.compose.ui.geometry.Offset(centerX, centerY), style = Stroke(width = 1.dp.toPx()))
        }

        // Draw axis lines / 绘制轴线
        dimensions.forEachIndexed { index, _ ->
            val angle = Math.toRadians(angles[index])
            val endX = centerX + (radius * cos(angle)).toFloat()
            val endY = centerY + (radius * sin(angle)).toFloat()
            drawLine(color = OnSurfaceDim.copy(alpha = 0.3f), start = androidx.compose.ui.geometry.Offset(centerX, centerY), end = androidx.compose.ui.geometry.Offset(endX, endY), strokeWidth = 1.dp.toPx())
        }

        // Draw data polygon / 绘制数据多边形
        val path = Path()
        dimensions.forEachIndexed { index, (_, value) ->
            val angle = Math.toRadians(angles[index])
            val normalizedValue = value.toFloat() / maxValue
            val px = centerX + (radius * normalizedValue * cos(angle)).toFloat()
            val py = centerY + (radius * normalizedValue * sin(angle)).toFloat()
            if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawPath(path = path, color = PQCPrimary.copy(alpha = 0.4f))
        drawPath(path = path, color = PQCPrimary, style = Stroke(width = 2.dp.toPx()))

        // Draw data points / 绘制数据点
        dimensions.forEachIndexed { index, (_, value) ->
            val angle = Math.toRadians(angles[index])
            val normalizedValue = value.toFloat() / maxValue
            val px = centerX + (radius * normalizedValue * cos(angle)).toFloat()
            val py = centerY + (radius * normalizedValue * sin(angle)).toFloat()
            drawCircle(color = PQCPrimary, radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(px, py()))
        }
    }
}

// ===== Main Screen =====
// ===== 主屏幕 =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PQCSecurityScreen(viewModel: PQCSecurityViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 7 })

    LaunchedEffect(state.selectedTab) {
        pagerState.animateScrollToPage(state.selectedTab.ordinal)
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.processIntent(PQCIntent.SelectTab(PQCTab.entries[pagerState.currentPage]))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PQC 安全工具包", fontWeight = FontWeight.Medium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PQCPrimary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.processIntent(PQCIntent.ClearScanResults) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Tab Row / 标签页行
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PQCPrimary,
                edgePadding = 8.dp
            ) {
                PQCTab.entries.forEach { tab ->
                    Tab(
                        selected = pagerState.currentPage == tab.ordinal,
                        onClick = { viewModel.processIntent(PQCIntent.SelectTab(tab)) },
                        text = {
                            Text(
                                text = tab.labelZh,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (pagerState.currentPage == tab.ordinal) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            val icon = when (tab) {
                                PQCTab.DASHBOARD -> Icons.Default.Dashboard
                                PQCTab.SCANNER -> Icons.Default.Search
                                PQCTab.KNOWLEDGE -> Icons.Default.School
                                PQCTab.CONFIG_WIZARD -> Icons.Default.Build
                                PQCTab.REPORT -> Icons.Default.Description
                                PQCTab.DEVICE -> Icons.Default.PhoneAndroid
                                PQCTab.CHECKLIST -> Icons.Default.Checklist
                            }
                            Icon(icon, contentDescription = tab.label)
                        }
                    )
                }
            }

            // Horizontal Pager / 水平分页器
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> DashboardTab(state, viewModel)
                    1 -> ScannerTab(state, viewModel)
                    2 -> KnowledgeTab(state, viewModel)
                    3 -> ConfigWizardTab(state, viewModel)
                    4 -> ReportTab(state, viewModel)
                    5 -> DeviceTab(state, viewModel)
                    6 -> ChecklistTab(state, viewModel)
                }
            }
        }
    }
}

// ===== Dashboard Tab =====
// ===== 仪表盘标签 =====

@Composable
private fun DashboardTab(state: PQCSecurityState, viewModel: PQCSecurityViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("安全健康度仪表盘", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("整体安全评分", style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${state.overallHealthScore}",
                        style = MaterialTheme.typography.displayMedium,
                        color = when {
                            state.overallHealthScore >= 80 -> PQCQuantumSafe
                            state.overallHealthScore >= 50 -> PQCRiskMedium
                            else -> PQCRiskHigh
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Text("总分 100", style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
                    Spacer(modifier = Modifier.height(16.dp))
                    PQCRadarChart(data = state.radarChartData, modifier = Modifier.size(200.dp))
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "已扫描 APK",
                    value = "${state.totalApksScanned}",
                    color = PQCPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "量子安全",
                    value = "${state.quantumSafeCount}",
                    color = PQCQuantumSafe,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "存在风险",
                    value = "${state.atRiskCount}",
                    color = PQCRiskHigh,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Text("雷达图维度说明", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        }
        items(
            listOf(
                "Signature Strength / 签名强度" to "当前签名算法抵抗量子攻击的能力",
                "Algorithm Type / 算法类型" to "所使用的签名算法类型（ECDSA/ML-KEM/LMS）",
                "Key Length / 密钥长度" to "签名密钥的长度指标",
                "Keystore Support / Keystore 支持" to "设备 Android Keystore 对 PQC 的支持程度",
                "Play Signing / Play 签名合规" to "Google Play App Signing 配置状态",
                "Migration Ready / 迁移就绪度" to "完成量子安全迁移的进度"
            )
        ) { (title, desc) ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("• ", color = PQCPrimary)
                Text(title, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceLight, fontWeight = FontWeight.Medium)
            }
            Text("  $desc", style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = OnSurfaceDim, textAlign = TextAlign.Center)
        }
    }
}

// ===== Scanner Tab =====
// ===== 签名检测器标签 =====

@Composable
private fun ScannerTab(state: PQCSecurityState, viewModel: PQCSecurityViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("APK/AAB 签名算法检测", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant),
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(android.content.Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                        putExtra(android.content.Intent.EXTRA_MIME_TYPES, arrayOf("application/vnd.android.package-archive", "application/octet-stream"))
                    }
                }
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = PQCPrimary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("点击选择 APK/AAB 文件", style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight)
                    Text("或拖拽文件到此处", style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            // Simulate scan with demo data
                            viewModel.processIntent(PQCIntent.ScanAPK(android.net.Uri.parse("content://demo.apk")))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PQCPrimary)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("扫描演示 APK")
                    }
                }
            }
        }
        if (state.scanStatus == ScanStatus.SCANNING) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("扫描中...", style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { state.scanProgress }, modifier = Modifier.fillMaxWidth(), color = PQCPrimary)
                        Text("${(state.scanProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
                    }
                }
            }
        }
        items(state.scannedApks) { apk ->
            SignatureScanCard(apk = apk, onSelect = { viewModel.processIntent(PQCIntent.SelectApkResult(apk)) })
        }
    }
}

@Composable
private fun SignatureScanCard(apk: ApkSignatureInfo, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(apk.fileName, style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight, fontWeight = FontWeight.Medium)
                    apk.packageName?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim) }
                }
                RiskBadge(riskLevel = apk.riskLevel)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                apk.signatureSchemes.forEach { scheme ->
                    AlgorithmChip(name = scheme.label, strength = apk.overallStrength)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("强度: ${apk.overallStrength.name}", style = MaterialTheme.typography.bodySmall, color = StrengthColor(apk.overallStrength))
                Text("迁移: ${apk.migrationUrgency.labelZh}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
                apk.versionName?.let { Text("v$it", style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim) }
            }
        }
    }
}

@Composable
private fun RiskBadge(riskLevel: RiskLevel) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = RiskColor(riskLevel).copy(alpha = 0.2f)
    ) {
        Text(
            text = riskLevel.labelZh,
            style = MaterialTheme.typography.labelSmall,
            color = RiskColor(riskLevel),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AlgorithmChip(name: String, strength: SignatureStrength) {
    Surface(shape = RoundedCornerShape(8.dp), color = StrengthColor(strength).copy(alpha = 0.2f)) {
        Text(text = name, style = MaterialTheme.typography.labelSmall, color = StrengthColor(strength), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

// ===== Knowledge Tab =====
// ===== PQC 知识库标签 =====

@Composable
private fun KnowledgeTab(state: PQCSecurityState, viewModel: PQCSecurityViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("量子安全算法知识库", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item {
            Text("NIST PQC 标准算法详解 — 了解 ML-KEM/LMS/XMSS/Dilithium/Falcon 的实现路径和适用场景", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceDim)
        }
        items(state.pqcAlgorithms) { algorithm ->
            AlgorithmDetailCard(algorithm = algorithm, onSelect = { viewModel.processIntent(PQCIntent.SelectAlgorithm(algorithm)) })
        }
    }
}

@Composable
private fun AlgorithmDetailCard(algorithm: PQCAlgorithm, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(algorithm.name, style = MaterialTheme.typography.titleLarge, color = OnSurfaceLight, fontWeight = FontWeight.Bold)
                    Text(algorithm.fullName, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = when (algorithm.androidSupport) {
                    AndroidPQCSupport.ANDROID_17_PLUS -> PQCQuantumSafe.copy(alpha = 0.2f)
                    AndroidPQCSupport.ANDROID_14_PLUS -> PQCRiskMedium.copy(alpha = 0.2f)
                    AndroidPQCSupport.PLANNED -> PQCPrimary.copy(alpha = 0.2f)
                    AndroidPQCSupport.NOT_SUPPORTED -> PQCRiskHigh.copy(alpha = 0.2f)
                }) {
                    Text(algorithm.androidSupport.labelZh, style = MaterialTheme.typography.labelSmall,
                        color = when (algorithm.androidSupport) {
                            AndroidPQCSupport.ANDROID_17_PLUS -> PQCQuantumSafe
                            AndroidPQCSupport.ANDROID_14_PLUS -> PQCRiskMedium
                            AndroidPQCSupport.PLANNED -> PQCPrimary
                            AndroidPQCSupport.NOT_SUPPORTED -> PQCRiskHigh
                        }, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(algorithm.description, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim, maxLines = 3)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("NIST Level: ${algorithm.nistLevel}", style = MaterialTheme.typography.labelSmall, color = PQCPrimary)
                Text("Key: ${algorithm.keySize}", style = MaterialTheme.typography.labelSmall, color = OnSurfaceDim)
                Text(algorithm.type.label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceDim)
            }
        }
    }
}

// ===== Config Wizard Tab =====
// ===== 配置引导器标签 =====

@Composable
private fun ConfigWizardTab(state: PQCSecurityState, viewModel: PQCSecurityViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Play App Signing PQC 配置引导", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item {
            // Stepper / 步骤条
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ConfigWizardStep.entries.forEachIndexed { index, step ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(
                                if (state.configWizardStep.step >= step.step) PQCPrimary else OnSurfaceDim
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${step.step + 1}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                        Text(step.titleZh, style = MaterialTheme.typography.labelSmall, color = OnSurfaceLight, textAlign = TextAlign.Center)
                    }
                    if (index < ConfigWizardStep.entries.size - 1) {
                        Box(modifier = Modifier.weight(0.5f).height(2.dp).padding(top = 15.dp).background(if (state.configWizardStep.step > step.step) PQCPrimary else OnSurfaceDim))
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val step = state.configWizardStep
                    Text(step.titleZh, style = MaterialTheme.typography.titleLarge, color = OnSurfaceLight, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    val description = when (step) {
                        ConfigWizardStep.ASSESS_CURRENT -> "使用签名检测器分析所有 APK/AAB 的当前签名算法。记录所有使用 ECDSA P-256/P-384 的应用。这些应用需要迁移到量子安全算法。\n\n使用本工具的 Scanner Tab 扫描您的 APK，获取详细的签名算法报告。"
                        ConfigWizardStep.SELECT_ALGORITHM -> "根据您的使用场景选择合适的量子安全算法：\n• ML-KEM-768 — Google Play App Signing 推荐，密钥封装\n• LMS — 高频签名场景，适合频繁更新的应用\n\n考虑因素：密钥大小、签名速度、Android 版本兼容性。"
                        ConfigWizardStep.GENERATE_KEYS -> "在 Google Play Console 中生成新的量子安全密钥：\n1. 打开 Play Console → App Signing\n2. 点击「Request new quantum-safe key」\n3. 选择 ML-KEM-768 或 LMS\n4. 等待 Google 在 HSM 中生成密钥（约 3-5 天）\n\n注意：此过程不可逆。"
                        ConfigWizardStep.CONFIGURE_DUAL_SIGNING -> "在过渡期内同时配置 ECDSA（当前）和 ML-KEM/LMS（新）签名：\n1. 在 Play Console 中启用双重签名\n2. 上传新的量子安全公钥证书\n3. 保留旧的 ECDSA 密钥用于向后兼容\n4. 建议过渡期：6-12 个月"
                        ConfigWizardStep.VERIFY_MIGRATION -> "完成双重签名配置后进行全面验证：\n1. 在 Android 17+ 设备上测试所有应用\n2. 验证 Play Store 发布流程正常\n3. 检查旧版本兼容性问题\n4. 确认无误后，按照 Google Play 指南废弃 ECDSA 旧密钥"
                    }
                    Text(description, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceDim)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.configWizardStep.step > 0) {
                    OutlinedButton(
                        onClick = { viewModel.processIntent(PQCIntent.PrevWizardStep) },
                        modifier = Modifier.weight(1f)
                    ) { Text("上一步") }
                }
                if (state.configWizardStep.step < ConfigWizardStep.entries.size - 1) {
                    Button(
                        onClick = { viewModel.processIntent(PQCIntent.NextWizardStep) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PQCPrimary)
                    ) { Text("下一步") }
                } else {
                    Button(
                        onClick = { viewModel.processIntent(PQCIntent.GenerateReport(ReportFormat.PDF)) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PQCQuantumSafe)
                    ) { Text("完成配置") }
                }
            }
        }
    }
}

// ===== Report Tab =====
// ===== 合规报告标签 =====

@Composable
private fun ReportTab(state: PQCSecurityState, viewModel: PQCSecurityViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("PQC 合规性报告", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("报告格式选择", style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight)
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportFormat.entries.forEach { format ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.processIntent(PQCIntent.SelectReportFormat(format)) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.selectedReportFormat == format,
                                onClick = { viewModel.processIntent(PQCIntent.SelectReportFormat(format)) },
                                colors = RadioButtonDefaults.colors(selectedColor = PQCPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(format.label, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceLight)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.processIntent(PQCIntent.GenerateReport(state.selectedReportFormat)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.reportGenerating,
                        colors = ButtonDefaults.buttonColors(containerColor = PQCPrimary)
                    ) {
                        if (state.reportGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("生成中...")
                        } else {
                            Icon(Icons.Default.Description, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("生成报告")
                        }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("报告内容预览", style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight)
                    Spacer(modifier = Modifier.height(8.dp))
                    val items = listOf(
                        "✓ 当前签名状态概览" to (if (state.scannedApks.isNotEmpty()) "已扫描 ${state.totalApksScanned} 个 APK" else "未扫描"),
                        "✓ 风险级别评估" to "高风险: ${state.atRiskCount} 个 / 量子安全: ${state.quantumSafeCount} 个",
                        "✓ 迁移建议" to "建议优先迁移 ECDSA P-256 应用",
                        "✓ 预估时间线" to "完整迁移预计 2-4 周",
                        "✓ 算法强度评分" to "${state.overallHealthScore}/100"
                    )
                    items.forEach { (title, value) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(title, style = MaterialTheme.typography.bodySmall, color = OnSurfaceLight, modifier = Modifier.weight(1f))
                            Text(value, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
                        }
                    }
                }
            }
        }
    }
}

// ===== Device Tab =====
// ===== 设备检测标签 =====

@Composable
private fun DeviceTab(state: PQCSecurityState, viewModel: PQCSecurityViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Android Keystore PQC 能力检测", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item {
            Button(
                onClick = { viewModel.processIntent(PQCIntent.DetectDeviceCapabilities) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isDetectingDevices,
                colors = ButtonDefaults.buttonColors(containerColor = PQCPrimary)
            ) {
                if (state.isDetectingDevices) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("检测中...")
                } else {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("检测设备 Keystore 能力")
                }
            }
        }
        items(state.deviceCapabilities) { device ->
            DeviceCapabilityCard(device = device)
        }
        if (state.deviceCapabilities.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = OnSurfaceDim, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("点击上方按钮检测设备 PQC 能力", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceDim, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCapabilityCard(device: KeystoreCapability) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(device.deviceModel, style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(8.dp), color = if (device.isQuantumSafe) PQCQuantumSafe.copy(alpha = 0.2f) else PQCRiskMedium.copy(alpha = 0.2f)) {
                    Text(if (device.isQuantumSafe) "量子安全" else "待升级", style = MaterialTheme.typography.labelSmall, color = if (device.isQuantumSafe) PQCQuantumSafe else PQCRiskMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Text("${device.androidVersion} (API ${device.apiLevel})", style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Keymaster: ${device.keymasterVersion} | Security: ${device.securityLevel}", style = MaterialTheme.typography.labelSmall, color = OnSurfaceDim)
            Spacer(modifier = Modifier.height(8.dp))
            device.supportedAlgorithms.take(3).forEach { algo ->
                Text("• $algo", style = MaterialTheme.typography.bodySmall, color = if (algo.contains("ML-KEM") || algo.contains("LMS")) PQCQuantumSafe else OnSurfaceDim)
            }
            if (device.supportedAlgorithms.size > 3) {
                Text("+${device.supportedAlgorithms.size - 3} more", style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
            }
        }
    }
}

// ===== Checklist Tab =====
// ===== 迁移清单标签 =====

@Composable
private fun ChecklistTab(state: PQCSecurityState, viewModel: PQCSecurityViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("App 签名流水线 PQC 迁移检查表", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                TextButton(onClick = { viewModel.processIntent(PQCIntent.ResetChecklist) }) { Text("重置") }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("迁移进度", style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.migrationProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = PQCQuantumSafe,
                        trackColor = OnSurfaceDim.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${(state.migrationProgress * 100).toInt()}% 完成", style = MaterialTheme.typography.bodySmall, color = OnSurfaceDim)
                }
            }
        }
        items(state.migrationSteps) { step ->
            MigrationStepCard(step = step, onToggle = { viewModel.processIntent(PQCIntent.ToggleMigrationStep(step.id)) })
        }
    }
}

@Composable
private fun MigrationStepCard(step: MigrationStep, onToggle: () -> Unit) {
    val (icon, iconColor) = StepStatusIcon(step.status)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = PQCSurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = step.status.labelZh, tint = iconColor,