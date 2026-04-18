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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.cos
import kotlin.math.sin

// ===== Color Palette — PQC Security Toolkit Theme =====
// ===== 配色方案 — PQC 安全工具包主题 =====

private val PQCPrimary = Color(0xFF6750A4)          // Purple — Security/Authority
private val PQCSecondary = Color(0xFF625B71)
private val PQCTertiary = Color(0xFF7D5260)
private val PQCSurface = Color(0xFF1C1B1F)          // Dark surface
private val PQCSurfaceVariant = Color(0xFF2D2D2D)
private val PQCRiskHigh = Color(0xFFB3261E)          // Red — HIGH risk / 高风险
private val PQCRiskMedium = Color(0xFFE8A317)        // Amber — MEDIUM risk / 中风险
private val PQCRiskLow = Color(0xFF2E7D32)           // Green — LOW/None risk / 低/无风险
private val PQCQuantumSafe = Color(0xFF00C853)      // Bright green — quantum safe / 量子安全
private val OnSurfaceLight = Color(0xFFE0E0E0)
private val OnSurfaceDim = Color(0xFF9E9E9E)
private val CodeBackground = Color(0xFF0D1117)

// ===== Risk Level Color Mapper =====
// ===== 风险级别颜色映射 =====

@Composable
private fun RiskColor(level: RiskLevel): Color = when (level) {
    RiskLevel.HIGH -> PQCRiskHigh
    RiskLevel.MEDIUM -> PQCRiskMedium
    RiskLevel.LOW -> PQCRiskLow
    RiskLevel.NONE -> PQCQuantumSafe
}

// ===== Signature Strength Color Mapper =====
// ===== 签名强度颜色映射 =====

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

/**
 * Custom Radar Chart for PQC Security Dashboard
 * PQC 安全仪表盘的自定义雷达图
 *
 * @param data Radar chart data with 6 dimensions
 * @param maxValue Maximum value for each axis (typically 100)
 */
@Composable
private fun PQCRadarChart(
    data: RadarChartData,
    maxValue: Int = 100,
    modifier: Modifier = Modifier
) {
    val dimensions = listOf(
        "Signature\nStrength" to data.signatureStrength,
        "Algorithm\nType" to data.algorithmType,
        "Key\nLength" to data.keyLength,
        "Keystore\nSupport" to data.keystoreSupport,
        "Play Signing\nCompliance" to data.playSigningCompliance,
        "Migration\nReadiness" to data.migrationReadiness
    )
    val dimCount = dimensions.size
    // Use fixed canvas size of 200.dp converted to pixels
    // 使用固定的 200.dp 转换为像素作为画布大小
    // Fixed pixel dimensions for radar chart (avoids density conversion issues)
    // 雷达图的固定像素尺寸（避免密度转换问题）
    val centerX = 100f
    val centerY = 100f
    val radius = 85f
    val angleStep = 360f / dimCount
    val labelRadius = radius * 1.15f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            // Draw grid circles / 绘制网格圆
            for (level in 1..5) {
                val levelRadius = radius * level / 5
                drawCircle(
                    color = Color(0xFF444444),
                    radius = levelRadius,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    style = Stroke(width = 0.5.dp.toPx())
                )
            }

            // Draw axis lines / 绘制轴线
            for (index in 0 until dimCount) {
                val angle = Math.toRadians((angleStep * index - 90).toDouble())
                val endX = centerX + radius * cos(angle).toFloat()
                val endY = centerY + radius * sin(angle).toFloat()
                drawLine(
                    color = Color(0xFF444444),
                    start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                    strokeWidth = 0.5.dp.toPx()
                )
            }

            // Draw data polygon / 绘制数据多边形
            val path = Path()
            for (index in 0 until dimCount) {
                val (_, value) = dimensions[index]
                val angle = Math.toRadians((angleStep * index - 90).toDouble())
                val normalizedValue = value.toFloat() / maxValue
                val pointRadius = radius * normalizedValue
                val px = centerX + pointRadius * cos(angle).toFloat()
                val py = centerY + pointRadius * sin(angle).toFloat()
                if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()

            // Fill polygon / 填充多边形
            drawPath(path = path, color = PQCPrimary.copy(alpha = 0.3f))

            // Stroke polygon / 描边多边形
            drawPath(path = path, color = PQCPrimary, style = Stroke(width = 2.dp.toPx()))

            // Draw data points / 绘制数据点
            for (index in 0 until dimCount) {
                val (_, value) = dimensions[index]
                val angle = Math.toRadians((angleStep * index - 90).toDouble())
                val normalizedValue = value.toFloat() / maxValue
                val pointRadius = radius * normalizedValue
                val px = centerX + pointRadius * cos(angle).toFloat()
                val py = centerY + pointRadius * sin(angle).toFloat()
                drawCircle(
                    color = PQCPrimary,
                    radius = 4.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(px, py)
                )
            }
        }

        // Dimension labels / 维度标签 (outside Canvas — use pre-computed values)
        for (index in 0 until dimCount) {
            val (label, _) = dimensions[index]
            val angle = Math.toRadians((angleStep * index - 90).toDouble())
            val lx = centerX + labelRadius * cos(angle).toFloat()
            val ly = centerY + labelRadius * sin(angle).toFloat()

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(x = (lx - 30f).dp, y = (ly - 8f).dp)
            )
        }
    }
}

// ===== Health Score Ring Component =====
// ===== 健康分环形图组件 =====

@Composable
private fun HealthScoreRing(
    score: Int,
    modifier: Modifier = Modifier
) {
    val scoreColor = when {
        score >= 80 -> PQCRiskLow
        score >= 50 -> PQCRiskMedium
        else -> PQCRiskHigh
    }

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(90.dp)) {
            // Background circle / 背景圆
            drawArc(
                color = Color(0xFF333333),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx())
            )
            // Score arc / 分数弧
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = score * 3.6f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx())
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = scoreColor
            )
            Text(
                text = "Health",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDim
            )
        }
    }
}

// ===== Tab Row Component =====
// ===== 标签页行组件 =====

@Composable
private fun PQCTabRow(
    selectedTab: PQCTab,
    onTabSelected: (PQCTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = PQCTab.entries

    ScrollableTabRow(
        selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
        containerColor = PQCSurface,
        contentColor = OnSurfaceLight,
        edgePadding = 8.dp,
        modifier = modifier
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = tab.labelZh,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selectedTab == tab) PQCPrimary else OnSurfaceDim
                        )
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedTab == tab) OnSurfaceLight else OnSurfaceDim
                        )
                    }
                }
            )
        }
    }
}

// ===== Dashboard Tab Content =====
// ===== 仪表盘标签页内容 =====

@Composable
private fun DashboardTabContent(
    state: PQCSecurityState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header / 标题
        Text(
            text = "PQC Security Dashboard / PQC 安全仪表盘",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurfaceLight
        )

        // Top metrics row / 顶部指标行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HealthScoreRing(score = state.overallHealthScore)

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            ) {
                MetricCard(
                    label = "Total APKs Scanned / 扫描 APK 总数",
                    value = "${state.totalApksScanned}",
                    icon = Icons.Default.Apps,
                    color = PQCPrimary
                )
                MetricCard(
                    label = "Quantum Safe / 量子安全",
                    value = "${state.quantumSafeCount}",
                    icon = Icons.Default.Shield,
                    color = PQCQuantumSafe
                )
                MetricCard(
                    label = "At Risk / 存在风险",
                    value = "${state.atRiskCount}",
                    icon = Icons.Default.Warning,
                    color = if (state.atRiskCount > 0) PQCRiskHigh else PQCRiskLow
                )
            }
        }

        // Radar Chart / 雷达图
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = PQCSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Security Radar / 安全雷达",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                PQCRadarChart(data = state.radarChartData)
            }
        }

        // Quick Actions / 快速操作
        Text(
            text = "Quick Actions / 快速操作",
            style = MaterialTheme.typography.titleMedium,
            color = OnSurfaceLight
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Scan APK / 扫描 APK",
                subtitle = "Analyze signature / 分析签名",
                icon = Icons.Default.Search,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Knowledge / 知识库",
                subtitle = "PQC Algorithms / PQC 算法",
                icon = Icons.Default.School,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Config Wizard / 配置向导",
                subtitle = "Play Signing Setup / Play 签名设置",
                icon = Icons.Default.Build,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Checklist / 清单",
                subtitle = "Migration Steps / 迁移步骤",
                icon = Icons.Default.Checklist,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = PQCSurfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDim
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = { },
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = PQCSurfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PQCPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = OnSurfaceLight
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDim
                )
            }
        }
    }
}

// ===== Scanner Tab Content =====
// ===== 签名检测器标签页内容 =====

@Composable
private fun ScannerTabContent(
    state: PQCSecurityState,
    onScanAPK: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "APK Signature Scanner / APK 签名扫描器",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurfaceLight
        )

        // Scan button / 扫描按钮
        if (state.scanStatus == ScanStatus.IDLE) {
            ElevatedButton(
                onClick = { /* Trigger file picker / 触发文件选择器 */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.elevatedButtonColors(containerColor = PQCPrimary)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select APK/AAB / 选择 APK/AAB")
            }
        }

        // Scanning progress / 扫描进度
        if (state.scanStatus == ScanStatus.SCANNING) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = PQCSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PQCPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Scanning... / 扫描中...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurfaceLight
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.scanProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = PQCPrimary
                    )
                    Text(
                        text = "${(state.scanProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDim
                    )
                }
            }
        }

        // Scan results / 扫描结果
        if (state.scannedApks.isNotEmpty()) {
            Text(
                text = "Scan Results (${state.scannedApks.size}) / 扫描结果 (${state.scannedApks.size})",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceLight
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.scannedApks) { apk ->
                    ApkResultCard(
                        apk = apk,
                        isSelected = state.selectedApk?.filePath == apk.filePath,
                        onClick = { onScanAPK(android.net.Uri.parse(apk.filePath)) }
                    )
                }
            }
        }

        // Selected APK details / 选中的 APK 详情
        state.selectedApk?.let { apk ->
            ApkDetailCard(apk = apk)
        }
    }
}

@Composable
private fun ApkResultCard(
    apk: ApkSignatureInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) PQCPrimary.copy(alpha = 0.2f) else PQCSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Risk badge / 风险徽章
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RiskColor(apk.riskLevel).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (apk.riskLevel) {
                        RiskLevel.HIGH -> Icons.Default.Error
                        RiskLevel.MEDIUM -> Icons.Default.Warning
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = RiskColor(apk.riskLevel),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = apk.fileName,
                    style = MaterialTheme.typography.labelLarge,
                    color = OnSurfaceLight
                )
                Text(
                    text = apk.packageName ?: "Unknown package / 未知包名",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDim
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    apk.signatureSchemes.forEach { scheme ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(scheme.label, style = MaterialTheme.typography.labelSmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = StrengthColor(apk.overallStrength).copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }

            // Quantum safe badge / 量子安全徽章
            if (apk.quantumSafe) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Quantum Safe / 量子安全",
                    tint = PQCQuantumSafe,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ApkDetailCard(
    apk: ApkSignatureInfo,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = PQCSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Details / 详情",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceLight
            )
            Spacer(modifier = Modifier.height(12.dp))

            DetailRow("Package / 包名", apk.packageName ?: "N/A")
            DetailRow("Version", "${apk.versionName} (${apk.versionCode})")
            DetailRow("File Size", formatFileSize(apk.fileSize))
            DetailRow("Risk Level / 风险级别", apk.riskLevel.label)
            DetailRow("Strength / 强度", apk.overallStrength.name)
            DetailRow("Quantum Safe / 量子安全", if (apk.quantumSafe) "Yes / 是" else "No / 否")
            DetailRow("Migration Urgency / 迁移紧迫度", "${apk.migrationUrgency.labelZh} (${apk.migrationUrgency.label})")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Algorithms / 算法:",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceLight
            )
            apk.algorithms.forEach { alg ->
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            "${alg.name} (${alg.keySize ?: "?"} bits) - ${alg.scheme.label}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = StrengthColor(alg.strength).copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceDim)
        Text(text = value, style = MaterialTheme.typography.labelMedium, color = OnSurfaceLight)
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> "%.2f GB".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> "%.2f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000 -> "%.2f KB".format(bytes / 1_000.0)
        else -> "$bytes B"
    }
}

// ===== Knowledge Tab Content =====
// ===== PQC 知识库标签页内容 =====

@Composable
private fun KnowledgeTabContent(
    state: PQCSecurityState,
    onSelectAlgorithm: (PQCAlgorithm?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "PQC Algorithm Knowledge Base / PQC 算法知识库",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurfaceLight
        )

        Text(
            text = "NIST Post-Quantum Cryptography Standards — Android 17 adopts these to protect against quantum computer attacks. / NIST 后量子密码学标准 — Android 17 采用这些标准来防御量子计算机攻击。",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceDim
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.pqcAlgorithms) { algorithm ->
                AlgorithmCard(
                    algorithm = algorithm,
                    onClick = { onSelectAlgorithm(algorithm) }
                )
            }
        }
    }
}

@Composable
private fun AlgorithmCard(
    algorithm: PQCAlgorithm,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = PQCSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = algorithm.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PQCPrimary
                    )
                    Text(
                        text = algorithm.fullName,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDim
                    )
                }

                // NIST Level badge / NIST 级别徽章
                SuggestionChip(
                    onClick = { },
                    label = { Text("Level ${algorithm.nistLevel}", style = MaterialTheme.typography.labelSmall) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = PQCPrimary.copy(alpha = 0.2f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = algorithm.description,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceLight,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(
                    onClick = { },
                    label = { Text(algorithm.type.label, style = MaterialTheme.typography.labelSmall) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = when (algorithm.type) {
                            PQCType.KEY_ENCAPSULATION -> Color(0xFF1565C0).copy(alpha = 0.2f)
                            PQCType.DIGITAL_SIGNATURE -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                            PQCType.HASH_BASED -> Color(0xFF6A1B9A).copy(alpha = 0.2f)
                        }
                    )
                )

                SuggestionChip(
                    onClick = { },
                    label = { Text(algorithm.androidSupport.label, style = MaterialTheme.typography.labelSmall) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (algorithm.androidSupport == AndroidPQCSupport.ANDROID_17_PLUS)
                            PQCQuantumSafe.copy(alpha = 0.2f) else OnSurfaceDim.copy(alpha = 0.2f)
                    )
                )

                Text(
                    text = "Key: ${algorithm.keySize}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDim,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

// ===== Config Wizard Tab Content =====
// ===== 配置向导标签页内容 =====

@Composable
private fun ConfigWizardTabContent(
    state: PQCSecurityState,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Play App Signing PQC Config Wizard / Play App Signing PQC 配置向导",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurfaceLight
        )

        // Step indicator / 步骤指示器
        StepperIndicator(
            currentStep = state.configWizardStep.step,
            totalSteps = 5
        )

        // Step content / 步骤内容
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = PQCSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Step ${state.configWizardStep.step + 1}: ${state.configWizardStep.titleZh}",
                    style = MaterialTheme.typography.titleMedium,
                    color = PQCPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val (description, details) = when (state.configWizardStep) {
                    ConfigWizardStep.ASSESS_CURRENT -> Pair(
                        "Assess your current app signing configuration / 评估您当前的应用签名配置",
                        listOf(
                            "• Use the Scanner tab to analyze your APK/AAB signatures / 使用扫描器标签分析您的 APK/AAB 签名",
                            "• Identify signature algorithms: ECDSA P-256/P-384 / 识别签名算法：ECDSA P-256/P-384",
                            "• Check if V1 (JAR) signatures are still in use / 检查 V1 (JAR) 签名是否仍在使用",
                            "• Evaluate migration urgency based on risk assessment / 根据风险评估确定迁移紧迫度"
                        )
                    )
                    ConfigWizardStep.SELECT_ALGORITHM -> Pair(
                        "Select your quantum-safe algorithm / 选择您的量子安全算法",
                        listOf(
                            "• ML-KEM-768: Recommended for most apps, Android 17+ / 推荐大多数应用使用，Android 17+",
                            "• LMS: Best for high-frequency signing (OTA updates) / 适合高频签名（OTA 更新）",
                            "• Consider dual-signing during transition period / 过渡期考虑双重签名"
                        )
                    )
                    ConfigWizardStep.GENERATE_KEYS -> Pair(
                        "Generate new quantum-safe keys in Play Console / 在 Play Console 中生成新的量子安全密钥",
                        listOf(
                            "• Go to Google Play Console → App Signing / 前往 Google Play Console → App Signing",
                            "• Click 'Request new keys' / 点击「请求新密钥」",
                            "• Select 'Quantum-safe (ML-KEM-768)' / 选择「量子安全（ML-KEM-768）」",
                            "• Google will generate keys in their HSM / Google 将在其 HSM 中生成密钥"
                        )
                    )
                    ConfigWizardStep.CONFIGURE_DUAL_SIGNING -> Pair(
                        "Configure dual-signing for transition / 配置过渡期双重签名",
                        listOf(
                            "• Enable both ECDSA (current) and ML-KEM (new) signatures / 同时启用 ECDSA（当前）和 ML-KEM（新）签名",
                            "• This ensures backward compatibility / 这确保向后兼容性",
                            "• Recommended transition period: 6-12 months / 建议过渡期：6-12 个月"
                        )
                    )
                    ConfigWizardStep.VERIFY_MIGRATION -> Pair(
                        "Verify migration and decommission old keys / 验证迁移并废弃旧密钥",
                        listOf(
                            "• Test all apps on Android 17+ devices / 在 Android 17+ 设备上测试所有应用",
                            "• Verify Google Play app delivery works correctly / 验证 Google Play 应用分发正常",
                            "• After transition period, decommission ECDSA keys per guidelines / 过渡期后按指南废弃 ECDSA 密钥"
                        )
                    )
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight
                )

                Spacer(modifier = Modifier.height(12.dp))

                details.forEach { detail ->
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons / 导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onPrevStep,
                enabled = state.configWizardStep.step > 0,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceLight)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous / 上一步")
            }

            Button(
                onClick = onNextStep,
                enabled = state.configWizardStep.step < 4,
                colors = ButtonDefaults.buttonColors(containerColor = PQCPrimary)
            ) {
                Text(if (state.configWizardStep.step == 4) "Complete / 完成" else "Next / 下一步")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun StepperIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 0 until totalSteps) {
            Box(
                modifier = Modifier
                    .size(if (step == currentStep) 32.dp else 24.dp)
                    .clip(CircleShape)
                    .background(
                        if (step < currentStep) PQCRiskLow
                        else if (step == currentStep) PQCPrimary
                        else OnSurfaceDim.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${step + 1}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (step == currentStep) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = Color.White
                )
            }

            if (step < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(2.dp)
                        .background(
                            if (step < currentStep) PQCRiskLow else OnSurfaceDim.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

// ===== Report Tab Content =====
// ===== 合规报告标签页内容 =====

@Composable
private fun ReportTabContent(
    state: PQCSecurityState,
    onGenerateReport: (ReportFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "PQC Compliance Report / PQC 合规报告",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurfaceLight
        )

        Text(
            text = "Generate a comprehensive PQC compliance report for your project. / 为您的项目生成全面的 PQC 合规报告。",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceDim
        )

        // Report format selection / 报告格式选择
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = PQCSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Report Contents / 报告内容:",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    "Current signature algorithm analysis / 当前签名算法分析",
                    "Risk level assessment (0-100) / 风险级别评估",
                    "PQC migration urgency rating / PQC 迁移紧迫度评级",
                    "Recommended quantum-safe algorithm / 推荐的量子安全算法",
                    "Step-by-step migration timeline / 分步迁移时间线",
                    "Google Play    Signatures compliance checklist / Play 签名合规清单"
                ).forEach { item ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PQCRiskLow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item, style = MaterialTheme.typography.bodySmall, color = OnSurfaceLight)
                    }
                }
            }
        }

        // Format selection / 格式选择
        Text(
            text = "Report Format / 报告格式:",
            style = MaterialTheme.typography.titleMedium,
            color = OnSurfaceLight
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReportFormat.entries.forEach { format ->
                FilterChip(
                    selected = state.selectedReportFormat == format,
                    onClick = { },
                    label = { Text(format.label) },
                    leadingIcon = {
                        if (state.selectedReportFormat == format) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PQCPrimary.copy(alpha = 0.3f)
                    )
                )
            }
        }

        // Generate button / 生成按钮
        if (state.reportGenerating) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = PQCSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PQCPrimary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Generating Report... / 正在生成报告...", color = OnSurfaceLight)
                }
            }
        } else if (state.reportGenerated) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = PQCRiskLow.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PQCRiskLow)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Report Generated / 报告已生成", color = OnSurfaceLight)
                        state.reportPath?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall, color = OnSurfaceDim)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Share, contentDescription = "Share / 分享", tint = PQCPrimary)
                    }
                }
            }
        } else {
            Button(
                onClick = { onGenerateReport(state.selectedReportFormat) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PQCPrimary),
                enabled = state.scannedApks.isNotEmpty()
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Report / 生成报告")
            }

            if (state.scannedApks.isEmpty()) {
                Text(
                    text = "Scan at least one APK first to generate a report. / 请先扫描至少一个 APK 再生成报告。",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDim
                )
            }
        }
    }
}

// ===== Device Tab Content =====
// ===== 设备检测标签页内容 =====

@Composable
private fun DeviceTabContent(
    state: PQCSecurityState,
    onDetect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Android Keystore PQC Capability Detection / Android Keystore PQC 能力检测",
            style = MaterialTheme.typography.titleLarge,
            color = OnSurfaceLight
        )

        Text(
            text = "Detect which PQC algorithms are supported by the device's Android Keystore. / 检测设备 Android Keystore 支持哪些 PQC 算法。",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceDim
        )

        Button(
            onClick = onDetect,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PQCPrimary),
            enabled = !state.isDetectingDevices
        ) {
            if (state.isDetectingDevices) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.PhoneAndroid, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (state.isDetectingDevices) "Detecting... / 检测中..." else "Detect Device Capabilities / 检测设备能力")
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.deviceCapabilities) { device ->
                DeviceCapabilityCard(device = device)
            }
        }
    }
}

@Composable
private fun DeviceCapabilityCard(
    device: KeystoreCapability,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = PQCSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (device.isQuantumSafe) Icons.Default.Shield else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (device.isQuantumSafe) PQCQuantumSafe else PQCRiskMedium,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(device.deviceModel, style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight)
                    Text("${device.androidVersion} (API ${device.apiLevel})", style = MaterialTheme.typography.labelSmall, color = OnSurfaceDim)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DetailRow("Keymaster Version / Keymaster 版本", device.keymasterVersion)
            DetailRow("Security Level / 安全级别", device.securityLevel)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Supported Algorithms / 支持的算法:", style = MaterialTheme.typography.labelMedium, color = OnSurfaceLight)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                device.supportedAlgorithms.forEach { alg ->
                    val isPQC = alg.startsWith("ML-KEM") || alg.startsWith("LMS") || alg.startsWith("Dilithium")
                    SuggestionChip(
                        onClick = { },
                        label = { Text(alg, style = MaterialTheme.typography.labelSmall) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isPQC) PQCQuantumSafe.copy(alpha = 0.2f) else OnSurfaceDim.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}

// ===== Checklist Tab Content =====
// ===== 迁移清单标签页内容 =====

@Composable
private fun ChecklistTabContent(
    state: PQCSecurityState,
    onToggleStep: (String) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PQC Migration Checklist / PQC 迁移清单",
                style = MaterialTheme.typography.titleLarge,
                color = OnSurfaceLight,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onReset) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset / 重置", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Progress ring / 进度环形图
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(90.dp)) {
                    drawArc(color = Color(0xFF333333), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 8.dp.toPx()))
                    drawArc(
                        color = PQCRiskLow,
                        startAngle = -90f,
                        sweepAngle = state.migrationProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx())
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${(state.migrationProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PQCRiskLow
                    )
                    Text("Complete / 完成", style = MaterialTheme.typography.labelSmall, color = OnSurfaceDim)
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.migrationSteps) { step ->
                MigrationStepCard(
                    step = step,
                    onToggle = { onToggleStep(step.id) }
                )
            }
        }
    }
}

@Composable
private fun MigrationStepCard(
    step: MigrationStep,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor) = StepStatusIcon(step.status)

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (step.isBlocking && step.status != MigrationStepStatus.COMPLETED)
                PQCRiskHigh.copy(alpha = 0.1f)
            else PQCSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = step.status != MigrationStepStatus.BLOCKED) { onToggle() }
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${step.stepNumber}.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PQCPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(step.title, style = MaterialTheme.typography.titleMedium, color = OnSurfaceLight)
                    if (step.isBlocking) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = { },
                            label = { Text("Blocking / 阻塞", style = MaterialTheme.typography.labelSmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = PQCRiskHigh.copy(alpha = 0.2f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    Text(
                        text = "⏱ ${step.estimatedTime}",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDim
                    )
                    step.referenceLink?.let {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "🔗 Reference / 参考",
                            style = MaterialTheme.typography.labelSmall,
                            color = PQCPrimary
                        )
                    }
                }
            }
        }
    }
}

// ===== Main Screen Composable =====
// ===== 主屏幕可组合函数 =====

@Composable
fun PQCSecurityScreen(
    viewModel: PQCSecurityViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Collect effects / 收集副作用
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PQCEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = if (effect.isError) SnackbarDuration.Long else SnackbarDuration.Short
                    )
                }
                is PQCEffect.ReportGenerated -> {
                    snackbarHostState.showSnackbar("Report: ${effect.filePath}")
                }
                is PQCEffect.ShareFile -> {
                    snackbarHostState.showSnackbar("Share: ${effect.filePath}")
                }
                is PQCEffect.NavigateToAlgorithmDetail -> {
                    // Navigate to algorithm detail / 导航到算法详情
                }
                is PQCEffect.NavigateToWizardStep -> {
                    // Navigate to wizard step / 导航到向导步骤
                }
                is PQCEffect.ScanComplete -> {
                    snackbarHostState.showSnackbar(
                        "Scanned ${effect.apkCount} APKs, ${effect.quantumSafeCount} quantum-safe / " +
                                "已扫描 ${effect.apkCount} 个 APK，${effect.quantumSafeCount} 个量子安全"
                    )
                }
                is PQCEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PQCSurface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row / 标签页行
            PQCTabRow(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.processIntent(PQCIntent.SelectTab(it)) }
            )

            // Tab Content / 标签页内容
            Box(modifier = Modifier.fillMaxSize()) {
                when (state.selectedTab) {
                    PQCTab.DASHBOARD -> DashboardTabContent(
                        state = state,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                    PQCTab.SCANNER -> ScannerTabContent(
                        state = state,
                        onScanAPK = { uri -> viewModel.processIntent(PQCIntent.ScanAPK(uri)) },
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                    PQCTab.KNOWLEDGE -> KnowledgeTabContent(
                        state = state,
                        onSelectAlgorithm = { viewModel.processIntent(PQCIntent.SelectAlgorithm(it)) },
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                    PQCTab.CONFIG_WIZARD -> ConfigWizardTabContent(
                        state = state,
                        onNextStep = { viewModel.processIntent(PQCIntent.NextWizardStep) },
                        onPrevStep = { viewModel.processIntent(PQCIntent.PrevWizardStep) },
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                    PQCTab.REPORT -> ReportTabContent(
                        state = state,
                        onGenerateReport = { format -> viewModel.processIntent(PQCIntent.GenerateReport(format)) },
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                    PQCTab.DEVICE -> DeviceTabContent(
                        state = state,
                        onDetect = { viewModel.processIntent(PQCIntent.DetectDeviceCapabilities) },
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                    PQCTab.CHECKLIST -> ChecklistTabContent(
                        state = state,
                        onToggleStep = { stepId -> viewModel.processIntent(PQCIntent.ToggleMigrationStep(stepId)) },
                        onReset = { viewModel.processIntent(PQCIntent.ResetChecklist) },
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}
