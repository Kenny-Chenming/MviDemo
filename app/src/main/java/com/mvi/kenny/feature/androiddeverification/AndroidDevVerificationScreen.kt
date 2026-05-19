package com.mvi.kenny.feature.androiddeverification

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * AndroidDevVerificationScreen — Android 开发者验证合规与 CI 集成工具包主界面
 * AndroidDevVerificationScreen — Android Developer Verification Compliance & CI Toolkit Main Screen
 * ============================================================
 *
 * PRD-261 | Android 开发者验证合规与 CI 集成工具包
 *
 * Design: Compliance Professional Style
 * - Deep blue primary (#1565C0) — conveys trust/security/compliance
 * - Orange accent (#FF9800) — for warnings and risk indicators
 * - Risk color palette: Green (#4CAF50) / Yellow (#FF9800) / Red (#F44336)
 * - JetBrains Mono for code / 等宽字体显示代码
 *
 * 5 Tab Layout:
 * - Tab 0: 验证检测 — APK 验证状态检测 + CI 插件
 * - Tab 1: 政策解读 — 强制执行时间线 + 认证设备 + 违规后果
 * - Tab 2: 分发指南 — 有限分发账号 + 高级流程 UX
 * - Tab 3: 替代方案 — 未验证应用分发策略 + App Claim
 * - Tab 4: 集成工具 — Android Studio 集成 + IDE 工作流
 */

// ============================================================
// Color Palette / 配色方案
// ============================================================

private object DevVerifyColors {
    // Primary colors / 主色
    val Primary = Color(0xFF1565C0)        // Deep blue — trust & compliance / 深蓝色 — 信任与合规
    val PrimaryVariant = Color(0xFF0D47A1)
    val Secondary = Color(0xFF42A5F5)      // Light blue / 浅蓝色
    val Accent = Color(0xFFFF9800)         // Orange — warnings / 橙色 — 警示

    // Background / 背景
    val Background = Color(0xFFF5F7FA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFE8EEF4)

    // Risk colors / 风险色
    val RiskSafe = Color(0xFF4CAF50)       // Green / 绿色
    val RiskPending = Color(0xFFFF9800)    // Yellow-Orange / 橙黄色
    val RiskNonCompliant = Color(0xFFF44336) // Red / 红色
    val RiskUnknown = Color(0xFF9E9E9E)     // Gray / 灰色

    // Text colors / 文字色
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)
    val TextOnPrimary = Color(0xFFFFFFFF)

    // Code block / 代码块
    val CodeBackground = Color(0xFF1E1E1E)
    val CodeText = Color(0xFFE0E0E0)
    val CodeBorder = Color(0xFF3D3D3D)

    // Divider / 分割线
    val Divider = Color(0xFFE0E0E0)
}

private val tabTitles = listOf(
    "验证检测" to "Verification",
    "政策解读" to "Policy",
    "分发指南" to "Distribution",
    "替代方案" to "Alternatives",
    "集成工具" to "Integration"
)

// ============================================================
// Main Screen / 主界面
// ============================================================

/**
 * Android Developer Verification Toolkit Screen
 * Android 开发者验证工具箱主界面
 *
 * @param viewModel AndroidDevVerificationViewModel instance
 * @param onUpdateTopBar TopBar configuration callback / TopBar 配置回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidDevVerificationScreen(
    viewModel: AndroidDevVerificationViewModel = viewModel(),
    onUpdateTopBar: (com.mvi.kenny.base.TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Update parent TopBar / 更新父级 TopBar
    LaunchedEffect(state.selectedTab) {
        onUpdateTopBar(
            com.mvi.kenny.base.TopBarConfig(
                title = "Android 开发者验证工具箱"
            )
        )
    }

    // Collect side effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AndroidDevVerificationEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AndroidDevVerificationEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("AndroidDevVerification", effect.text)
                    clipboard.setPrimaryClip(clip)
                }
                is AndroidDevVerificationEffect.OpenUrl -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "无法打开链接 / Cannot open URL", Toast.LENGTH_SHORT).show()
                    }
                }
                is AndroidDevVerificationEffect.ShowReport -> {
                    // Report is shown via BottomSheet state / 报告通过 BottomSheet 状态显示
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Android 开发者验证工具箱",
                        color = DevVerifyColors.TextOnPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DevVerifyColors.Primary
                )
            )
        },
        containerColor = DevVerifyColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row / Tab 切换行
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = DevVerifyColors.Surface,
                contentColor = DevVerifyColors.Primary,
                indicator = { tabPositions ->
                    if (state.selectedTab < tabPositions.size) {
                        SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                            color = DevVerifyColors.Primary
                        )
                    }
                }
            ) {
                tabTitles.forEachIndexed { index, (titleZh, titleEn) ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.sendIntent(AndroidDevVerificationIntent.SelectTab(index)) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = titleZh,
                                    fontSize = 12.sp,
                                    fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = titleEn,
                                    fontSize = 9.sp,
                                    color = DevVerifyColors.TextSecondary
                                )
                            }
                        },
                        selectedContentColor = DevVerifyColors.Primary,
                        unselectedContentColor = DevVerifyColors.TextSecondary
                    )
                }
            }

            // Tab Content / Tab 内容
            Box(modifier = Modifier.fillMaxSize()) {
                when (state.selectedTab) {
                    0 -> VerificationDetectionTab(state, viewModel)
                    1 -> PolicyInterpretationTab(state, viewModel)
                    2 -> DistributionGuideTab(state, viewModel)
                    3 -> AlternativesTab(state, viewModel)
                    4 -> IntegrationToolsTab(state, viewModel)
                }

                // Compliance Report BottomSheet / 合规报告 BottomSheet
                if (state.isReportVisible) {
                    ComplianceReportBottomSheet(
                        reportContent = state.reportContent,
                        riskLevel = state.riskLevel,
                        onDismiss = { viewModel.sendIntent(AndroidDevVerificationIntent.DismissReport) },
                        onCopy = { viewModel.sendIntent(AndroidDevVerificationIntent.CopySnippet("report", state.reportContent)) }
                    )
                }
            }
        }
    }
}

// ============================================================
// Tab 0: Verification Detection / 验证检测
// ============================================================

@Composable
private fun VerificationDetectionTab(
    state: AndroidDevVerificationState,
    viewModel: AndroidDevVerificationViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header / 标题区
        item {
            Text(
                text = "// APK 验证状态检测",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "APK Verification Status Detection — 检测 APK 的开发者验证合规状态",
                color = DevVerifyColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DevVerifyColors.Divider)
        }

        // APK Path Input / APK 路径输入
        item {
            ApkInputCard(
                apkPath = state.apkPath,
                isScanning = state.isScanning,
                onPathChange = { viewModel.sendIntent(AndroidDevVerificationIntent.UpdateApkPath(it)) },
                onScan = { viewModel.sendIntent(AndroidDevVerificationIntent.ScanApk(state.apkPath)) }
            )
        }

        // Scan Result / 扫描结果
        item {
            ScanResultCard(state)
        }

        // Risk Badge / 风险标签
        state.scanResult?.let {
            item {
                RiskBadgeCard(state.riskLevel, it)
            }
        }

        // CI Plugin Configuration / CI 插件配置
        item {
            CiPluginCard(viewModel)
        }

        // Code Example: Gradle CI / Gradle CI 配置示例
        item {
            CodeExampleCard(
                id = "gradle_ci",
                title = "Gradle CI 配置示例 / Gradle CI Configuration",
                description = "在 build.gradle 中配置验证检查",
                language = "kotlin",
                content = """
// settings.gradle.kts — 开发者验证 CI 插件配置
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.developer-verification") version "1.0.0"
    }
}

// build.gradle.kts (app module)
plugins {
    id("com.android.developer-verification")
}

android {
    developerVerification {
        // 启用验证状态检查 / Enable verification check
        enforceVerification = true

        // 在验证失败时停止构建 / Fail build on verification failure
        failOnNonCompliance = true

        // 允许的分发渠道 / Allowed distribution channels
        allowedChannels = listOf(
            DistributionChannel.GOOGLE_PLAY,
            DistributionChannel.INTERNAL_TESTING,
            DistributionChannel.LIMITED
        )

        // CI 环境变量 / CI environment variables
        ciVerificationToken = System.getenv("DEV_VERIFICATION_TOKEN")
    }
}
                """.trimIndent(),
                isExpanded = "gradle_ci" in state.expandedSnippets,
                isCopied = state.copiedSnippetId == "gradle_ci",
                onToggle = { viewModel.sendIntent(AndroidDevVerificationIntent.ToggleSnippet("gradle_ci")) },
                onCopy = {
                    viewModel.sendIntent(
                        AndroidDevVerificationIntent.CopySnippet(
                            "gradle_ci",
                            """
// settings.gradle.kts — 开发者验证 CI 插件配置
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.developer-verification") version "1.0.0"
    }
}

// build.gradle.kts (app module)
plugins {
    id("com.android.developer-verification")
}

android {
    developerVerification {
        enforceVerification = true
        failOnNonCompliance = true
        allowedChannels = listOf(
            DistributionChannel.GOOGLE_PLAY,
            DistributionChannel.INTERNAL_TESTING,
            DistributionChannel.LIMITED
        )
        ciVerificationToken = System.getenv("DEV_VERIFICATION_TOKEN")
    }
}
                            """.trimIndent()
                        )
                    )
                }
            )
        }

        // Code Example: Fastlane CI / Fastlane CI 配置示例
        item {
            CodeExampleCard(
                id = "fastlane_ci",
                title = "Fastlane CI 配置示例 / Fastlane CI Configuration",
                description = "使用 Fastlane 自动化验证检查",
                language = "ruby",
                content = """
# Fastfile — 开发者验证 Fastlane 配置
# Developer Verification Fastlane Configuration

platform :android do
  desc "Verify developer compliance before build"
  lane :verify_developer do
    # 检查验证状态 / Check verification status
    verify_dev_status(
      api_token: ENV['DEV_VERIFICATION_API_TOKEN'],
      package_name: android_package_name,
      distribution: :limited  # or :direct, :google_play
    )

    # 如果验证失败则停止 / Abort if verification fails
    if !result[:verified]
      UI.error("❌ Developer not verified for #{android_package_name}")
      UI.error("💡 Complete verification: https://play.google.com/console/developers/verify")
      sh "exit 1"
    end

    # 继续构建 / Proceed with build
    gradle(task: "assembleRelease")
  end

  desc "Generate compliance report"
  lane :compliance_report do
    report = generate_compliance_report(
      package_name: android_package_name,
      output_format: :json
    )
    puts report
  end
end
                """.trimIndent(),
                isExpanded = "fastlane_ci" in state.expandedSnippets,
                isCopied = state.copiedSnippetId == "fastlane_ci",
                onToggle = { viewModel.sendIntent(AndroidDevVerificationIntent.ToggleSnippet("fastlane_ci")) },
                onCopy = {
                    viewModel.sendIntent(
                        AndroidDevVerificationIntent.CopySnippet(
                            "fastlane_ci",
                            """
# Fastfile — 开发者验证 Fastlane 配置
platform :android do
  desc "Verify developer compliance before build"
  lane :verify_developer do
    verify_dev_status(
      api_token: ENV['DEV_VERIFICATION_API_TOKEN'],
      package_name: android_package_name,
      distribution: :limited
    )
    if !result[:verified]
      UI.error("❌ Developer not verified for #{android_package_name}")
      sh "exit 1"
    end
    gradle(task: "assembleRelease")
  end
end
                            """.trimIndent()
                        )
                    )
                }
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun ApkInputCard(
    apkPath: String,
    isScanning: Boolean,
    onPathChange: (String) -> Unit,
    onScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// APK 文件路径 / APK File Path",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = apkPath,
                onValueChange = onPathChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("APK 路径 / APK Path") },
                placeholder = { Text("/path/to/your/app-release.apk") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = DevVerifyColors.Primary
                    )
                },
                singleLine = true,
                enabled = !isScanning
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onScan,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isScanning && apkPath.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DevVerifyColors.Primary)
            ) {
                if (isScanning) {
                    LinearProgressIndicator(
                        modifier = Modifier.width(20.dp).height(4.dp),
                        color = DevVerifyColors.TextOnPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("扫描中... / Scanning...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("检测 APK / Scan APK")
                }
            }
        }
    }
}

@Composable
private fun ScanResultCard(state: AndroidDevVerificationState) {
    val result = state.scanResult

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// 扫描结果 / Scan Result",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (result == null && !state.isScanning) {
                Text(
                    text = "尚未扫描 / No scan performed",
                    color = DevVerifyColors.TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                )
            } else if (result != null) {
                ResultRow("Package Name", result.packageName)
                ResultRow("Developer Verified", if (result.developerVerified) "✅ Yes / 是" else "❌ No / 否")
                ResultRow("Verification Date", result.verificationDate ?: "N/A")
                ResultRow("Distribution Type", result.distributionType)
                ResultRow("Compliance Status", result.complianceStatus)

                Spacer(modifier = Modifier.height(12.dp))

                // Generate Report Button / 生成报告按钮
                Button(
                    onClick = { /* handled via ViewModel */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DevVerifyColors.Accent)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("生成合规报告 / Generate Report")
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = DevVerifyColors.TextSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = DevVerifyColors.TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RiskBadgeCard(riskLevel: RiskLevel, result: VerificationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "// 风险评级 / Risk Level",
                    color = DevVerifyColors.Primary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "${riskLevel.emoji} ${riskLevel.label}",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(riskLevel.colorHex).copy(alpha = 0.15f),
                        labelColor = Color(riskLevel.colorHex)
                    )
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val statusText = when (riskLevel) {
                    RiskLevel.SAFE -> "已验证 / Verified"
                    RiskLevel.PENDING -> "待确认 / Pending"
                    RiskLevel.NON_COMPLIANT -> "未合规 / Non-compliant"
                    RiskLevel.UNKNOWN -> "未知 / Unknown"
                }
                Text(
                    text = statusText,
                    color = Color(riskLevel.colorHex),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun CiPluginCard(viewModel: AndroidDevVerificationViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Primary.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// CI 插件信息 / CI Plugin Info",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "开发者验证 CI 插件支持 Gradle 和 Fastlane，可在构建流水线中自动检测验证状态。",
                color = DevVerifyColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { },
                    label = { Text("Gradle", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = DevVerifyColors.Primary.copy(alpha = 0.1f),
                        labelColor = DevVerifyColors.Primary
                    )
                )
                AssistChip(
                    onClick = { },
                    label = { Text("Fastlane", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = DevVerifyColors.Primary.copy(alpha = 0.1f),
                        labelColor = DevVerifyColors.Primary
                    )
                )
                AssistChip(
                    onClick = { },
                    label = { Text("GitHub Actions", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = DevVerifyColors.Primary.copy(alpha = 0.1f),
                        labelColor = DevVerifyColors.Primary
                    )
                )
            }
        }
    }
}

// ============================================================
// Tab 1: Policy Interpretation / 政策解读
// ============================================================

@Composable
private fun PolicyInterpretationTab(
    state: AndroidDevVerificationState,
    viewModel: AndroidDevVerificationViewModel
) {
    val enforcementTimelines = rememberEnforcementTimelines()
    val policyItems = rememberPolicyItems()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "// 政策解读 / Policy Interpretation",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Android 开发者验证计划 — 各国强制执行时间线与合规要求",
                color = DevVerifyColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DevVerifyColors.Divider)
        }

        // Enforcement Timeline / 强制执行时间线
        item {
            EnforcementTimelineCard(enforcementTimelines)
        }

        // Certified Devices / 认证设备定义
        item {
            CertifiedDevicesCard()
        }

        // Violation Consequences / 违规后果
        item {
            ViolationConsequencesCard()
        }

        // Policy Details / 政策详情
        itemsIndexed(policyItems) { index, item ->
            PolicyItemCard(item)
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun EnforcementTimelineCard(timelines: List<EnforcementTimeline>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// 强制执行时间线 / Enforcement Timeline",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            timelines.forEach { timeline ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (timeline.isMandatory) DevVerifyColors.RiskSafe else DevVerifyColors.RiskPending)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timeline.country,
                            color = DevVerifyColors.TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = timeline.enforcementDate,
                            color = if (timeline.isMandatory) DevVerifyColors.RiskNonCompliant else DevVerifyColors.RiskPending,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (timeline.isMandatory) {
                            Text(
                                text = "强制执行",
                                color = DevVerifyColors.RiskSafe,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CertifiedDevicesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// 认证 Android 设备定义 / Certified Android Devices",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "认证 Android 设备 (Certified Android Devices) 是指通过 Google 兼容性测试 (CTS) 并获得 Google 移动服务 (GMS) 认证的设备。",
                color = DevVerifyColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            val criteria = listOf(
                "✅ 通过 Google CTS (Compatibility Test Suite) / Passed CTS",
                "✅ 预装 Google Play Store / Pre-installed Google Play Store",
                "✅ 符合 Google GMS 许可协议 / GMS Licensed",
                "❌ 非认证设备安装未验证开发者的 APK 将受限 / Non-certified: APK from unverified devs restricted"
            )
            criteria.forEach { item ->
                Text(
                    text = item,
                    color = DevVerifyColors.TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ViolationConsequencesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.RiskNonCompliant.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = DevVerifyColors.RiskNonCompliant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "// 违规后果 / Violation Consequences",
                    color = DevVerifyColors.RiskNonCompliant,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val consequences = listOf(
                "🔴 未验证开发者的 APK 在认证设备上无法安装 / Unverified dev APK blocked on certified devices",
                "🔴 应用被从分发列表中移除 / App removed from distribution",
                "🔴 开发者账号面临封禁风险 / Developer account suspension risk",
                "🟡 用户侧弹窗警告 / User-facing warning dialogs"
            )
            consequences.forEach { item ->
                Text(
                    text = item,
                    color = DevVerifyColors.TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 3.dp),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PolicyItemCard(item: PolicyItem) {
    val severityColor = when (item.severity) {
        "critical" -> DevVerifyColors.RiskNonCompliant
        "warning" -> DevVerifyColors.RiskPending
        else -> DevVerifyColors.Primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    color = severityColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = { },
                    label = { Text(item.severity.uppercase(), fontSize = 10.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = severityColor.copy(alpha = 0.15f),
                        labelColor = severityColor
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.description,
                color = DevVerifyColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun rememberEnforcementTimelines(): List<EnforcementTimeline> = listOf(
    EnforcementTimeline("🇧🇷 巴西 / Brazil", "2026-09-01", true, "强制执行第一阶段"),
    EnforcementTimeline("🇮🇩 印尼 / Indonesia", "2026-09-01", true, "强制执行第一阶段"),
    EnforcementTimeline("🇸🇬 新加坡 / Singapore", "2026-09-01", true, "强制执行第一阶段"),
    EnforcementTimeline("🇹🇭 泰国 / Thailand", "2026-09-01", true, "强制执行第一阶段"),
    EnforcementTimeline("🌍 全球 / Global", "2027-01-01", true, "2027 年全面强制执行"),
    EnforcementTimeline("🇺🇸 美国 / USA", "2027-06-01", false, "预计执行日期"),
    EnforcementTimeline("🇪🇺 欧盟 / EU", "2027-09-01", false, "预计执行日期"),
)

@Composable
private fun rememberPolicyItems(): List<PolicyItem> = listOf(
    PolicyItem(
        id = 0,
        title = "开发者必须在 2026 年 9 月前完成验证",
        description = "所有在 Google Play 以外分发 Android 应用的开发者必须在强制执行日期前完成开发者验证，否则应用将在认证设备上面临安装限制。",
        severity = "critical"
    ),
    PolicyItem(
        id = 1,
        title = "Google Play 分发不受影响",
        description = "通过 Google Play 分发的应用不受此验证要求限制。此要求仅针对 Google Play 以外的替代分发渠道。",
        severity = "info"
    ),
    PolicyItem(
        id = 2,
        title = "企业内部分发 (Enterprise) 需要单独账号",
        description = "企业内部分发应用需要申请「有限分发」(Limited Distribution) 开发者账号，并完成企业验证流程。",
        severity = "warning"
    ),
    PolicyItem(
        id = 3,
        title = "认证设备定义 / Certified Devices",
        description = "预装 Google Play Services 并通过 Google 兼容性测试 (CTS) 的设备为认证设备。未验证开发者的 APK 在此类设备上将被限制安装。",
        severity = "info"
    )
)
// ============================================================
// Tab 2: Distribution Guide / 分发指南
// ============================================================

@Composable
private fun DistributionGuideTab(
    state: AndroidDevVerificationState,
    viewModel: AndroidDevVerificationViewModel
) {
    val accountTypes = rememberAccountTypes()
    val uxFlows = rememberUxFlows()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "// 分发指南 / Distribution Guide",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "有限分发账号与高级流程用户体验分析",
                color = DevVerifyColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DevVerifyColors.Divider)
        }

        // Limited Distribution Account Types / 有限分发账号类型
        item {
            LimitedDistributionCard(accountTypes)
        }

        // Advanced Flow UX Analysis / 高级流程 UX 分析
        item {
            UxFlowAnalysisCard(uxFlows)
        }

        // Distribution Channel Decision / 分发渠道决策
        item {
            DistributionDecisionCard()
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun LimitedDistributionCard(accounts: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// 有限分发账号类型 / Limited Distribution Account Types",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            accounts.forEach { (type, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "▸",
                        color = DevVerifyColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = type,
                            color = DevVerifyColors.TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = desc,
                            color = DevVerifyColors.TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UxFlowAnalysisCard(flows: List<Triple<String, String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// 高级流程 UX 分析 / Advanced Flow UX Analysis",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            flows.forEach { (flow, description, recommendation) ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        text = flow,
                        color = DevVerifyColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        color = DevVerifyColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "💡 $recommendation",
                        color = DevVerifyColors.Accent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = DevVerifyColors.Divider)
                }
            }
        }
    }
}

@Composable
private fun DistributionDecisionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Primary.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// 分发渠道决策树 / Distribution Channel Decision",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            val decisions = listOf(
                "你的应用是否在 Google Play？",
                "  是 → 无需额外验证 / No additional verification needed",
                "  否 → 继续判断 ↓",
                "分发规模是否 > 100 用户/设备？",
                "  是 → 需要有限分发账号 / Limited Distribution account required",
                "  否 → 可用直接分发 / Direct distribution allowed",
                "应用是否面向企业客户？",
                "  是 → 企业有限分发 / Enterprise Limited Distribution",
                "  否 → 开发者验证 + 标准有限分发"
            )
            decisions.forEach { line ->
                Text(
                    text = line,
                    color = if (line.startsWith("  是") || line.startsWith("  否"))
                        DevVerifyColors.TextSecondary
                    else
                        DevVerifyColors.TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun rememberAccountTypes(): List<Pair<String, String>> = listOf(
    Pair("标准有限分发 (Standard Limited)", "适用于企业内部应用分发，需要完成开发者基础验证"),
    Pair("企业有限分发 (Enterprise Limited)", "适用于大型企业 OEM 预装，需要企业级验证和合同"),
    Pair("测试分发 (Testing)", "适用于 beta 测试，无需正式验证但有设备数量限制")
)

@Composable
private fun rememberUxFlows(): List<Triple<String, String, String>> = listOf(
    Triple(
        "开发者首次验证流程 / First Verification Flow",
        "用户在 Play Console 发起验证 → 填写信息 → 支付验证费用 → 完成验证",
        "建议：提前准备银行账户和身份证明材料，减少中途等待"
    ),
    Triple(
        "有限分发账号申请流程 / Limited Account Application",
        "申请表单 → 审核 (3-5 工作日) → 签署协议 → 账号激活",
        "建议：企业账号需准备公司注册证明，建议提前沟通"
    ),
    Triple(
        "应用上架审核流程 / App Review Process",
        "提交应用 → 政策合规检查 → 安全审核 → 上架",
        "建议：使用合规清单自检后再提交，减少被拒次数"
    )
)

// ============================================================
// Tab 3: Alternative Distribution / 替代方案
// ============================================================

@Composable
private fun AlternativesTab(
    state: AndroidDevVerificationState,
    viewModel: AndroidDevVerificationViewModel
) {
    val strategies = rememberAlternativeStrategies()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "// 替代分发方案 / Alternative Distribution",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "未验证应用替代分发策略 + App Claim 流程",
                color = DevVerifyColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DevVerifyColors.Divider)
        }

        // Alternative Strategies / 替代策略
        itemsIndexed(strategies) { index, strategy ->
            AlternativeStrategyCard(
                strategy = strategy,
                isExpanded = "alt_$index" in state.expandedSnippets,
                onToggle = { viewModel.sendIntent(AndroidDevVerificationIntent.ToggleSnippet("alt_$index")) },
                onCopy = { viewModel.sendIntent(AndroidDevVerificationIntent.CopySnippet("alt_$index", strategy.steps.joinToString("\n"))) }
            )
        }

        // App Claim Process / App Claim 流程
        item {
            AppClaimCard()
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun AlternativeStrategyCard(
    strategy: AlternativeStrategy,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strategy.title,
                        color = DevVerifyColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = strategy.description,
                        color = DevVerifyColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = DevVerifyColors.TextSecondary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Applicability / 适用性
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DevVerifyColors.Primary.copy(alpha = 0.08f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "适用场景: ${strategy.applicability}",
                            color = DevVerifyColors.Primary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Steps / 步骤
                    strategy.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}.",
                                color = DevVerifyColors.Accent,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = step,
                                color = DevVerifyColors.TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onCopy,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("复制步骤 / Copy Steps", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppClaimCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Accent.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = DevVerifyColors.Accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "// App Claim 流程 / App Claim Process",
                    color = DevVerifyColors.Accent,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "App Claim 允许经过验证的开发者「认领」未经验证开发者上传的应用所有权。适用于开发者变更或应用迁移场景。",
                color = DevVerifyColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            val claimSteps = listOf(
                "1. 验证开发者提交 App Claim 申请",
                "2. 提供原始开发者身份证明或授权文件",
                "3. Google 审核通过后完成应用所有权转移",
                "4. 新开发者完成验证后应用恢复正常分发"
            )
            claimSteps.forEach { step ->
                Text(
                    text = step,
                    color = DevVerifyColors.TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun rememberAlternativeStrategies(): List<AlternativeStrategy> = listOf(
    AlternativeStrategy(
        id = 0,
        title = "Google Play 分发 / Google Play Distribution",
        description = "将应用迁移至 Google Play 作为主要分发渠道，完全规避验证要求",
        applicability = "适用于有 Play Store 政策合规能力的开发者",
        steps = listOf(
            "准备应用商店上架材料（截图、描述、隐私政策）",
            "注册 Google Play 开发者账号（支付 $25 注册费）",
            "完成开发者账号验证（个人或企业）",
            "上传 APK/AAB 并提交审核",
            "审核通过后应用在 Google Play 上架"
        )
    ),
    AlternativeStrategy(
        id = 1,
        title = "企业内部分发 (Enterprise Internal) / Enterprise Internal",
        description = "通过企业账号进行内部应用分发，适用于企业自用应用",
        applicability = "适用于企业内部应用，不面向普通消费者",
        steps = listOf(
            "申请企业有限分发开发者账号",
            "完成企业身份验证（需公司注册文件）",
            "签署企业分发协议",
            "通过企业管理平台 (EMM) 分发应用",
            "配置设备策略和管理员控制"
        )
    ),
    AlternativeStrategy(
        id = 2,
        title = "直接下载分发 (Direct Download) / Sideloading",
        description = "通过官网直接提供 APK 下载，适用于技术用户群体",
        applicability = "适用于技术社区、有 root 需求的用户",
        steps = listOf(
            "在官网提供 APK 下载链接",
            "确保应用签名和完整性",
            "提供清晰的安装指引（设置→安全→未知来源）",
            "注意：认证设备会显示安全警告",
            "考虑配合网站安全证书增加信任度"
        )
    ),
    AlternativeStrategy(
        id = 3,
        title = "第三方应用商店 / Third-party App Stores",
        description = "通过 Amazon Appstore、APKMirror 等第三方平台分发",
        applicability = "适用于希望扩大分发覆盖但无 Play Store 资源的开发者",
        steps = listOf(
            "选择目标第三方商店（Amazon、Samsung 等）",
            "注册开发者账号并完成验证",
            "提交应用并通过平台审核",
            "管理各平台的合规要求和政策差异"
        )
    )
)

// ============================================================
// Tab 4: Integration Tools / 集成工具
// ============================================================

@Composable
private fun IntegrationToolsTab(
    state: AndroidDevVerificationState,
    viewModel: AndroidDevVerificationViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "// 集成工具 / Integration Tools",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Android Studio 验证状态集成 + IDE 工作流",
                color = DevVerifyColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DevVerifyColors.Divider)
        }

        // Android Studio Plugin / Android Studio 插件
        item {
            AndroidStudioPluginCard()
        }

        // IDE Workflow Guide / IDE 工作流指南
        item {
            IdeWorkflowCard()
        }

        // Code Example: Gradle Plugin / Gradle 插件配置
        item {
            CodeExampleCard(
                id = "android_studio_plugin",
                title = "Android Studio 验证插件配置 / Android Studio Plugin Config",
                description = "在 Android Studio 中配置开发者验证插件",
                language = "kotlin",
                content = """
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.developer-verification") version "1.0.0"
    }
}

// build.gradle.kts (project level)
plugins {
    id("com.android.developer-verification") version "1.0.0"
}

// build.gradle.kts (app level)
android {
    verification {
        // 自动检测验证状态 / Auto-detect verification status
        autoCheck = true

        // 在构建时检查 / Check during build
        checkOnBuild = true

        // 严重级别: error, warning, info
        severityLevel = SeverityLevel.ERROR

        // 排除特定变体 / Exclude specific variants
        excludeVariants = listOf("debug", "release")
    }
}
                """.trimIndent(),
                isExpanded = "android_studio_plugin" in state.expandedSnippets,
                isCopied = state.copiedSnippetId == "android_studio_plugin",
                onToggle = { viewModel.sendIntent(AndroidDevVerificationIntent.ToggleSnippet("android_studio_plugin")) },
                onCopy = {
                    viewModel.sendIntent(
                        AndroidDevVerificationIntent.CopySnippet("android_studio_plugin",
                            """
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.developer-verification") version "1.0.0"
    }
}
android {
    verification {
        autoCheck = true
        checkOnBuild = true
        severityLevel = SeverityLevel.ERROR
    }
}
                            """.trimIndent()
                        )
                    )
                }
            )
        }

        // CLI Command Reference / CLI 命令参考
        item {
            CliCommandCard()
        }

        // CI/CD Integration Guide / CI/CD 集成指南
        item {
            CiCdIntegrationCard(viewModel)
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun AndroidStudioPluginCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// Android Studio 验证插件 / Android Studio Verification Plugin",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Android Studio 验证插件可在 IDE 内直接显示应用的验证状态，无需切换到 Play Console。",
                color = DevVerifyColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            val features = listOf(
                "✅ 状态指示器 — 项目窗口实时显示验证状态",
                "✅ 构建拦截 — 未验证时阻止 Release 构建",
                "✅ 一键验证 — 内置验证流程引导",
                "✅ 合规报告 — 生成符合要求的合规文档"
            )
            features.forEach { feat ->
                Text(
                    text = feat,
                    color = DevVerifyColors.TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "安装方式: Settings → Plugins → Marketplace → 搜索「Developer Verification」",
                color = DevVerifyColors.Accent,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun IdeWorkflowCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// IDE 工作流集成 / IDE Workflow Integration",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            val workflows = listOf(
                Triple("开发阶段", "Development", "在开发时即可检测验证状态，提前发现合规问题"),
                Triple("构建阶段", "Build", "Release 构建前自动检查，阻止不合规构建"),
                Triple("发布阶段", "Release", "发布前生成合规报告，记录验证状态"),
                Triple("CI/CD 阶段", "CI/CD", "集成到 Gradle/Fastlane/GitHub Actions")
            )
            workflows.forEach { (zh, en, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$zh ($en)",
                            color = DevVerifyColors.Primary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = desc,
                            color = DevVerifyColors.TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                HorizontalDivider(color = DevVerifyColors.Divider, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun CliCommandCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.CodeBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// CLI 命令参考 / CLI Command Reference",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            val commands = listOf(
                "# 检查验证状态 / Check verification status",
                "verify-dev status --package=com.example.app",
                "",
                "# 触发验证流程 / Trigger verification",
                "verify-dev verify --package=com.example.app",
                "",
                "# 生成合规报告 / Generate compliance report",
                "verify-dev report --package=com.example.app --format=json",
                "",
                "# CI 环境验证 / CI environment verification",
                "verify-dev ci-check --token=\${'$'}DEV_VERIFICATION_TOKEN"
            )
            commands.forEach { cmd ->
                Text(
                    text = cmd,
                    color = if (cmd.startsWith("#"))
                        DevVerifyColors.TextSecondary
                    else
                        DevVerifyColors.CodeText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun CiCdIntegrationCard(viewModel: AndroidDevVerificationViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "// CI/CD 集成示例 / CI/CD Integration Examples",
                color = DevVerifyColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val ciExamples = listOf(
                "GitHub Actions" to """
name: Android Build with Verification
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Verify Developer
        run: |
          verify-dev ci-check \\
            --token=\\${'{'}secrets.DEV_VERIFICATION_TOKEN} \\
            --package=com.example.app
      - name: Build
        run: ./gradlew assembleRelease
                """.trimIndent(),
                "GitLab CI" to """
verify_dev:
  stage: pre-build
  script:
    - verify-dev ci-check \\
      --token=\${'$'}DEV_VERIFICATION_TOKEN \\
      --package=com.example.app
  rules:
    - if: '\${'$'}CI_COMMIT_BRANCH == "main"'
                """.trimIndent()
            )

            ciExamples.forEach { (name, content) ->
                Text(
                    text = "// $name",
                    color = DevVerifyColors.Primary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DevVerifyColors.CodeBackground)
                        .border(1.dp, DevVerifyColors.CodeBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = content,
                        color = DevVerifyColors.CodeText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ============================================================
// Code Example Card / 代码示例卡片
// ============================================================

@Composable
private fun CodeExampleCard(
    id: String,
    title: String,
    description: String,
    language: String,
    content: String,
    isExpanded: Boolean,
    isCopied: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = DevVerifyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = DevVerifyColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = description,
                        color = DevVerifyColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = DevVerifyColors.TextSecondary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DevVerifyColors.CodeBackground)
                            .border(1.dp, DevVerifyColors.CodeBorder, RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = content,
                            color = DevVerifyColors.CodeText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onCopy,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCopied) DevVerifyColors.RiskSafe else DevVerifyColors.Primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCopied) "已复制 ✓" else "复制 / Copy",
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Compliance Report BottomSheet / 合规报告 BottomSheet
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComplianceReportBottomSheet(
    reportContent: String,
    riskLevel: RiskLevel,
    onDismiss: () -> Unit,
    onCopy: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DevVerifyColors.Primary) },
        containerColor = DevVerifyColors.Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "// 合规报告 / Compliance Report",
                        color = DevVerifyColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "${riskLevel.emoji} ${riskLevel.label}",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(riskLevel.colorHex).copy(alpha = 0.15f),
                            labelColor = Color(riskLevel.colorHex)
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Text("✕", color = DevVerifyColors.TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DevVerifyColors.Divider)
            Spacer(modifier = Modifier.height(16.dp))

            // Report Content / 报告内容
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DevVerifyColors.CodeBackground)
                    .border(1.dp, DevVerifyColors.CodeBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = reportContent,
                    color = DevVerifyColors.CodeText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions / 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("关闭 / Close")
                }
                Button(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DevVerifyColors.Primary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制报告 / Copy Report")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
