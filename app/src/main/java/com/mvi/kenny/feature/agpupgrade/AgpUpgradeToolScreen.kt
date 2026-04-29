package com.mvi.kenny.feature.agpupgrade

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ============================================================
 * AGP Upgrade Tool Screen — PRD-200
 * AGP 升级工具主界面
 * ============================================================
 *
 * A developer toolkit for managing AGP 9.2 / Compose 1.12.0 / compileSdk 37 upgrades.
 * Contains 5 tabs: Scanner / Wizard / Breaking Changes / Diagnostics / Compliance
 *
 * 设计: 深色终端风格，强调信息密度和可扫描性
 */

// ============ Color Palette ============
// ============ 配色方案 ============
private object AgpToolColors {
    val Background = androidx.compose.ui.graphics.Color(0xFF1C1C1E)
    val CardBg = androidx.compose.ui.graphics.Color(0xFF2C2C2E)
    val Primary = androidx.compose.ui.graphics.Color(0xFF0A84FF)
    val P0Red = androidx.compose.ui.graphics.Color(0xFFFF453A)
    val P1Yellow = androidx.compose.ui.graphics.Color(0xFFFFD60A)
    val P2Green = androidx.compose.ui.graphics.Color(0xFF30D158)
    val CompliantGreen = androidx.compose.ui.graphics.Color(0xFF30D158)
    val NonCompliantRed = androidx.compose.ui.graphics.Color(0xFFFF453A)
    val PartialYellow = androidx.compose.ui.graphics.Color(0xFFFFD60A)
    val TextPrimary = androidx.compose.ui.graphics.Color.White
    val TextSecondary = androidx.compose.ui.graphics.Color(0xFF8E8E93)
    val CodeBg = androidx.compose.ui.graphics.Color(0xFF000000)
    val CodeText = androidx.compose.ui.graphics.Color(0xFFCCDDFF)
    val TabActive = androidx.compose.ui.graphics.Color(0xFF0A84FF)
    val TabInactive = androidx.compose.ui.graphics.Color(0xFF636366)
}

// ============ Main App Composable ============
// ============ 主应用组件 ============

@Composable
fun AgpUpgradeToolApp(
    state: AgpUpgradeToolState,
    onIntent: (AgpUpgradeToolIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AgpToolColors.Background)
    ) {
        // Top Summary Banner / 顶部汇总横幅
        SummaryBanner(state = state)

        // Tab Bar / Tab 栏
        TabBar(
            currentTab = state.currentTab,
            onTabSelected = { onIntent(AgpUpgradeToolIntent.SwitchTab(it)) }
        )

        // Tab Content / Tab 内容
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (state.currentTab) {
                ToolTab.Scanner -> ScannerTab(
                    state = state.scanner,
                    onIntent = onIntent
                )
                ToolTab.Wizard -> WizardTab(
                    state = state.wizard,
                    onIntent = onIntent
                )
                ToolTab.Changes -> ChangesTab(
                    state = state.changes,
                    onIntent = onIntent
                )
                ToolTab.Diagnostics -> DiagnosticsTab(
                    state = state.diagnostics,
                    onIntent = onIntent
                )
                ToolTab.Compliance -> ComplianceTab(
                    state = state.compliance,
                    onIntent = onIntent
                )
            }
        }
    }
}

// ============ Summary Banner ============
// ============ 顶部汇总横幅 ============

@Composable
private fun SummaryBanner(state: AgpUpgradeToolState) {
    val bannerColor = when {
        state.scanner.scanResult?.riskLevel == RiskLevel.P0 -> AgpToolColors.P0Red
        state.scanner.scanResult?.riskLevel == RiskLevel.P1 -> AgpToolColors.P1Yellow
        state.scanner.scanResult?.riskLevel == RiskLevel.Pass -> AgpToolColors.P2Green
        else -> AgpToolColors.CardBg
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bannerColor.copy(alpha = 0.2f))
            .border(1.dp, bannerColor.copy(alpha = 0.5f), RoundedCornerShape(0.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AGP 升级工具 / AGP Upgrade Tool",
                    color = AgpToolColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Compose 1.12.0 · compileSdk 37 · AGP 9.2.0",
                    color = AgpToolColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
            state.scanner.scanResult?.let { result ->
                RiskBadge(riskLevel = result.riskLevel)
            }
        }
    }
}

// ============ Tab Bar ============
// ============ Tab 栏 ============

@Composable
private fun TabBar(
    currentTab: ToolTab,
    onTabSelected: (ToolTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ToolTab.entries.forEach { tab ->
            val isActive = tab == currentTab
            FilterChip(
                selected = isActive,
                onClick = { onTabSelected(tab) },
                label = {
                    Text(
                        text = when (tab) {
                            ToolTab.Scanner -> "🔍 ${tab.titleZh}"
                            ToolTab.Wizard -> "🧙 ${tab.titleZh}"
                            ToolTab.Changes -> "📋 ${tab.titleZh}"
                            ToolTab.Diagnostics -> "🔧 ${tab.titleZh}"
                            ToolTab.Compliance -> "✅ ${tab.titleZh}"
                        },
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AgpToolColors.TabActive.copy(alpha = 0.3f),
                    selectedLabelColor = AgpToolColors.TabActive
                )
            )
        }
    }
}

// ============ Risk Badge ============
// ============ 风险等级徽章 ============

@Composable
fun RiskBadge(riskLevel: RiskLevel) {
    val (color, text) = when (riskLevel) {
        RiskLevel.P0 -> AgpToolColors.P0Red to "🔴 P0"
        RiskLevel.P1 -> AgpToolColors.P1Yellow to "🟡 P1"
        RiskLevel.P2 -> AgpToolColors.P2Green to "🟢 P2"
        RiskLevel.Pass -> AgpToolColors.P2Green to "✅ 最新"
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ============ Scanner Tab ============
// ============ 扫描器 Tab ============

@Composable
private fun ScannerTab(
    state: ScannerState,
    onIntent: (AgpUpgradeToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Title / 标题
        Text(
            text = "🔍 AGP 版本现状扫描器 / AGP Version Scanner",
            color = AgpToolColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // Path Input / 路径输入
        OutlinedTextField(
            value = state.projectPath,
            onValueChange = { onIntent(AgpUpgradeToolIntent.UpdateScannerPath(it)) },
            label = { Text("Project Path / 项目路径") },
            placeholder = { Text("/path/to/your/android/project") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgpToolColors.Primary,
                unfocusedBorderColor = AgpToolColors.TextSecondary
            )
        )

        // Scan Button / 扫描按钮
        Button(
            onClick = { onIntent(AgpUpgradeToolIntent.StartScan) },
            enabled = !state.isScanning,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AgpToolColors.Primary
            )
        ) {
            if (state.isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AgpToolColors.TextPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scanning... / 扫描中...")
            } else {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Scan / 开始扫描")
            }
        }

        // Error Display / 错误展示
        state.error?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = AgpToolColors.P0Red.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = "❌ $error",
                    color = AgpToolColors.P0Red,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Scan Result / 扫描结果
        state.scanResult?.let { result ->
            VersionStatusCard(result = result)

            if (result.blockingIssues.isNotEmpty()) {
                BlockingIssuesCard(issues = result.blockingIssues)
            }

            result.compatibleVersions?.let { compat ->
                CompatibleVersionsCard(compatible = compat)
            }

            // Clear Button / 清除按钮
            TextButton(
                onClick = { onIntent(AgpUpgradeToolIntent.ClearScanResult) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear Result / 清除结果", color = AgpToolColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun VersionStatusCard(result: ScanResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AgpToolColors.CardBg
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 版本现状 / Version Status",
                    color = AgpToolColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                RiskBadge(riskLevel = result.riskLevel)
            }

            HorizontalDivider(color = AgpToolColors.TextSecondary.copy(alpha = 0.3f))

            // Version Grid / 版本信息网格
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VersionRow("AGP", result.versionInfo.agpVersion, result.versionInfo.agpVersion < "9.2.0")
                VersionRow("Gradle", result.versionInfo.gradleVersion, false)
                VersionRow("Kotlin", result.versionInfo.kotlinVersion, result.versionInfo.kotlinVersion < "2.1.0")
                VersionRow("Compose", result.versionInfo.composeVersion, false)
                VersionRow("compileSdk", result.versionInfo.compileSdk.toString(), result.versionInfo.compileSdk < 37)
                VersionRow("targetSdk", result.versionInfo.targetSdk.toString(), false)
            }
        }
    }
}

@Composable
private fun VersionRow(label: String, value: String, isOutdated: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = AgpToolColors.TextSecondary,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = if (isOutdated) AgpToolColors.P1Yellow else AgpToolColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = if (isOutdated) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun BlockingIssuesCard(issues: List<String>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AgpToolColors.P0Red.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🚨 阻断项 / Blocking Issues",
                color = AgpToolColors.P0Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            issues.forEach { issue ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("•", color = AgpToolColors.P0Red)
                    Text(issue, color = AgpToolColors.TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CompatibleVersionsCard(compatible: CompatibleVersions) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AgpToolColors.P2Green.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "✅ 推荐版本组合 / Recommended Versions",
                color = AgpToolColors.P2Green,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "AGP: ${compatible.recommendedAGP}  |  Gradle: ${compatible.recommendedGradle}${compatible.recommendedKotlin?.let { "  |  Kotlin: $it" } ?: ""}",
                color = AgpToolColors.TextPrimary,
                fontSize = 14.sp
            )
        }
    }
}

// ============ Wizard Tab ============
// ============ 升级向导 Tab ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardTab(
    state: WizardState,
    onIntent: (AgpUpgradeToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Text(
                text = "🧙 AGP 升级向导 / Upgrade Wizard",
                color = AgpToolColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Step Indicator / 步骤指示器
            StepIndicator(currentStep = state.currentStep)

            // Step Content / 步骤内容
            when (state.currentStep) {
                WizardStep.SelectCurrent -> SelectVersionStepContent(
                    state = state,
                    onIntent = onIntent
                )
                WizardStep.ShowPath -> ShowPathStepContent(
                    state = state,
                    onIntent = onIntent
                )
                WizardStep.ShowConfig -> ShowConfigStep(
                    state = state,
                    onIntent = onIntent
                )
                WizardStep.CICommands -> CICommandsStep(
                    state = state,
                    onIntent = onIntent
                )
            }
        }

        // Navigation Buttons / 导航按钮
        WizardNavigationButtons(
            currentStep = state.currentStep,
            canGoBack = state.currentStep != WizardStep.SelectCurrent,
            canGoNext = when (state.currentStep) {
                WizardStep.SelectCurrent -> state.currentAGPVersion.isNotBlank()
                else -> true
            },
            onBack = { onIntent(AgpUpgradeToolIntent.PrevWizardStep) },
            onNext = { onIntent(AgpUpgradeToolIntent.NextWizardStep) }
        )
    }
}

@Composable
private fun WizardNavigationButtons(
    currentStep: WizardStep,
    canGoBack: Boolean,
    canGoNext: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            enabled = canGoBack,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AgpToolColors.TextPrimary,
                disabledContentColor = AgpToolColors.TextSecondary.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Text(" Back")
        }
        Button(
            onClick = onNext,
            enabled = canGoNext,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = AgpToolColors.Primary,
                disabledContainerColor = AgpToolColors.Primary.copy(alpha = 0.3f)
            )
        ) {
            Text("Next ")
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun StepIndicator(currentStep: WizardStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WizardStep.entries.forEach { step ->
            val isActive = step.step <= currentStep.step
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isActive) AgpToolColors.Primary else AgpToolColors.CardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${step.step}",
                        color = if (isActive) AgpToolColors.TextPrimary else AgpToolColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.titleZh,
                    color = if (isActive) AgpToolColors.Primary else AgpToolColors.TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectVersionStepContent(
    state: WizardState,
    onIntent: (AgpUpgradeToolIntent) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AgpToolColors.CardBg
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "第 1 步：选择当前 AGP 版本 / Step 1: Select Current AGP Version",
                color = AgpToolColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "选择项目当前使用的 AGP 版本 / Select the AGP version your project currently uses",
                color = AgpToolColors.TextSecondary,
                fontSize = 13.sp
            )

            // Version Dropdown / 版本下拉选择
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = state.currentAGPVersion.ifBlank { "Select version / 选择版本" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AgpToolColors.Primary
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    availableAGPVersionsForDisplay.forEach { version ->
                        DropdownMenuItem(
                            text = { Text(version) },
                            onClick = {
                                onIntent(AgpUpgradeToolIntent.SelectCurrentVersion(version))
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Target version / 目标版本
            Text(
                text = "目标版本 / Target Version: AGP ${state.targetAGPVersion}",
                color = AgpToolColors.TextPrimary,
                fontSize = 14.sp
            )
        }
    }
}

private val availableAGPVersionsForDisplay = listOf(
    "8.5.0", "8.6.0", "8.7.0", "8.8.0", "8.9.0",
    "9.0.0", "9.1.0", "9.2.0"
)

@Composable
private fun ShowPathStepContent(
    state: WizardState,
    onIntent: (AgpUpgradeToolIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = AgpToolColors.CardBg
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "第 2 步：升级路径 / Step 2: Upgrade Path",
                    color = AgpToolColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "从 AGP ${state.currentAGPVersion} → ${state.targetAGPVersion}",
                    color = AgpToolColors.Primary,
                    fontSize = 14.sp
                )
            }
        }

        // Upgrade Steps / 升级步骤列表
        state.upgradePath.forEach { step ->
            UpgradeStepCard(step = step)
        }
    }
}

@Composable
private fun UpgradeStepCard(step: UpgradeStep) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AgpToolColors.CardBg
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${step.fromVersion} → ${step.toVersion}",
                    color = AgpToolColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                RiskBadge(riskLevel = step.riskLevel)
            }
            Text(
                text = step.keyNotes,
                color = AgpToolColors.Primary,
                fontSize = 13.sp
            )
            step.breakingChanges.forEach { change ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("•", color = AgpToolColors.TextSecondary)
                    Text(change, color = AgpToolColors.TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ShowConfigStep(
    state: WizardState,
    onIntent: (AgpUpgradeToolIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = AgpToolColors.CardBg
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "第 3 步：生成配置 / Step 3: Generated Config",
                    color = AgpToolColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        CodeBlockCard(
            title = "gradle-wrapper.properties",
            code = state.generatedGradleWrapper
        )

        CodeBlockCard(
            title = "build.gradle.kts",
            code = state.generatedBuildGradle
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { onIntent(AgpUpgradeToolIntent.PrevWizardStep) },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AgpToolColors.TextPrimary
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Text("Back / 上一步")
        }
        Button(
            onClick = { onIntent(AgpUpgradeToolIntent.NextWizardStep) },
            colors = ButtonDefaults.buttonColors(
                containerColor = AgpToolColors.Primary
            )
        ) {
            Text("Next / 下一步")
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun CICommandsStep(
    state: WizardState,
    onIntent: (AgpUpgradeToolIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = AgpToolColors.CardBg
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "第 4 步：CI 验证命令 / Step 4: CI Validation Commands",
                    color = AgpToolColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "在 CI 环境中运行的验证脚本 / Validation script to run in CI environment",
                    color = AgpToolColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        CodeBlockCard(
            title = "CI Script",
            code = state.generatedCICommands,
            showCopyButton = true
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = { onIntent(AgpUpgradeToolIntent.PrevWizardStep) },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AgpToolColors.TextPrimary
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Text("Back / 上一步")
        }
        Button(
            onClick = { /* Wizard complete / 向导完成 */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = AgpToolColors.P2Green
            )
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Text("Complete / 完成")
        }
    }
}

@Composable
private fun CodeBlockCard(
    title: String,
    code: String,
    showCopyButton: Boolean = false
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = AgpToolColors.CodeBg
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = AgpToolColors.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (showCopyButton) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(code))
                            scope.launch {
                                // Toast would be shown via Effect / 通过 Effect 显示 Toast
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = AgpToolColors.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            SelectionContainer {
                Text(
                    text = code,
                    color = AgpToolColors.CodeText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ============ Breaking Changes Tab ============
// ============ 变更清单 Tab ============

@Composable
private fun ChangesTab(
    state: ChangesState,
    onIntent: (AgpUpgradeToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "📋 AGP 9.x 破坏性变更清单 / Breaking Changes",
            color = AgpToolColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // Search Bar / 搜索栏
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onIntent(AgpUpgradeToolIntent.UpdateSearchQuery(it)) },
            placeholder = { Text("Search / 搜索...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgpToolColors.Primary,
                unfocusedBorderColor = AgpToolColors.TextSecondary
            )
        )

        // Risk Filter Chips / 风险过滤标签
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.selectedRiskFilter == null,
                onClick = { onIntent(AgpUpgradeToolIntent.FilterByRisk(null)) },
                label = { Text("All / 全部") }
            )
            RiskLevel.entries.filter { it != RiskLevel.Pass }.forEach { risk ->
                FilterChip(
                    selected = state.selectedRiskFilter == risk,
                    onClick = { onIntent(AgpUpgradeToolIntent.FilterByRisk(risk)) },
                    label = {
                        Text(
                            when (risk) {
                                RiskLevel.P0 -> "🔴 P0"
                                RiskLevel.P1 -> "🟡 P1"
                                RiskLevel.P2 -> "🟢 P2"
                                else -> ""
                            }
                        )
                    }
                )
            }
        }

        // Category Filter / 分类过滤
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("AGP 9.0", "AGP 9.1", "AGP 9.2", "compileSdk 37").forEach { cat ->
                FilterChip(
                    selected = state.selectedCategoryFilter == cat,
                    onClick = {
                        onIntent(
                            AgpUpgradeToolIntent.FilterByCategory(
                                if (state.selectedCategoryFilter == cat) null else cat
                            )
                        )
                    },
                    label = { Text(cat) }
                )
            }
        }

        // Changes List / 变更列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.filteredChanges) { change ->
                ChangeCard(
                    change = change,
                    isExpanded = state.expandedChangeId == change.id,
                    onToggle = { onIntent(AgpUpgradeToolIntent.ToggleChangeExpanded(change.id)) }
                )
            }
        }
    }
}

@Composable
private fun ChangeCard(
    change: BreakingChangeItem,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val borderColor = when (change.riskLevel) {
        RiskLevel.P0 -> AgpToolColors.P0Red
        RiskLevel.P1 -> AgpToolColors.P1Yellow
        RiskLevel.P2 -> AgpToolColors.P2Green
        else -> AgpToolColors.CardBg
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = AgpToolColors.CardBg
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RiskBadge(riskLevel = change.riskLevel)
                    Surface(
                        color = AgpToolColors.Primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = change.category,
                            color = AgpToolColors.Primary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AgpToolColors.TextSecondary
                )
            }

            Text(
                text = change.titleZh,
                color = AgpToolColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = change.title,
                color = AgpToolColors.TextSecondary,
                fontSize = 12.sp
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = borderColor.copy(alpha = 0.3f))
                    Text(
                        text = change.descriptionZh,
                        color = AgpToolColors.TextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = change.description,
                        color = AgpToolColors.TextSecondary,
                        fontSize = 12.sp
                    )

                    change.codeBefore?.let { before ->
                        Text(
                            text = "Before / 之前:",
                            color = AgpToolColors.P0Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        CodeSnippet(code = before, isBefore = true)
                    }

                    change.codeAfter?.let { after ->
                        Text(
                            text = "After / 之后:",
                            color = AgpToolColors.P2Green,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        CodeSnippet(code = after, isBefore = false)
                    }

                    change.migrationGuide?.let { guide ->
                        Text(
                            text = "💡 $guide",
                            color = AgpToolColors.Primary,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = "影响范围: ${change.affectedScope}",
                        color = AgpToolColors.TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeSnippet(code: String, isBefore: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AgpToolColors.CodeBg
        )
    ) {
        Text(
            text = code,
            color = if (isBefore) AgpToolColors.P0Red.copy(alpha = 0.8f)
                    else AgpToolColors.P2Green.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(8.dp)
        )
    }
}

// ============ Diagnostics Tab ============
// ============ 诊断工具 Tab ============

@Composable
private fun DiagnosticsTab(
    state: DiagnosticsState,
    onIntent: (AgpUpgradeToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🔧 AGP 升级失败诊断工具 / Diagnostic Tool",
            color = AgpToolColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "粘贴 Gradle/AGP 错误日志，自动分析原因和修复建议 / Paste Gradle/AGP error logs for auto analysis",
            color = AgpToolColors.TextSecondary,
            fontSize = 13.sp
        )

        // Error Log Input / 错误日志输入
        OutlinedTextField(
            value = state.errorLog,
            onValueChange = { onIntent(AgpUpgradeToolIntent.UpdateErrorLog(it)) },
            label = { Text("Error Log / 错误日志") },
            placeholder = { Text("Paste your Gradle/AGP error here...\n在此粘贴 Gradle/AGP 错误...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgpToolColors.Primary,
                unfocusedBorderColor = AgpToolColors.TextSecondary
            )
        )

        // Analyze Button / 分析按钮
        Button(
            onClick = { onIntent(AgpUpgradeToolIntent.AnalyzeLog) },
            enabled = !state.isAnalyzing && state.errorLog.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AgpToolColors.Primary
            )
        ) {
            if (state.isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AgpToolColors.TextPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing... / 分析中...")
            } else {
                Icon(Icons.Default.Analytics, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze / 分析")
            }
        }

        // Clear Button / 清除按钮
        if (state.diagnosisResult != null) {
            TextButton(
                onClick = { onIntent(AgpUpgradeToolIntent.ClearDiagnosis) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear / 清除", color = AgpToolColors.TextSecondary)
            }
        }

        // Diagnosis Result / 诊断结果
        state.diagnosisResult?.let { result ->
            DiagnosisResultCard(result = result)
        }
    }
}

@Composable
private fun DiagnosisResultCard(result: DiagnosisResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AgpToolColors.CardBg
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Badge / 分类徽章
            val categoryColor = when (result.category) {
                DiagnosticCategory.AGP_VERSION_INCOMPATIBLE -> AgpToolColors.P0Red
                DiagnosticCategory.GRADLE_VERSION_MISMATCH -> AgpToolColors.P1Yellow
                DiagnosticCategory.COMPOSE_DEPENDENCY_CONFLICT -> AgpToolColors.P0Red
                DiagnosticCategory.COMPILE_SDK_TOO_LOW -> AgpToolColors.P0Red
                else -> AgpToolColors.TextSecondary
            }

            Surface(
                color = categoryColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = result.category.name.replace("_", " "),
                    color = categoryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Summary / 摘要
            Text(
                text = result.summaryZh,
                color = AgpToolColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = result.summary,
                color = AgpToolColors.TextSecondary,
                fontSize = 13.sp
            )

            HorizontalDivider(color = AgpToolColors.TextSecondary.copy(alpha = 0.3f))

            // Root Cause / 根本原因
            Text(
                text = "🔍 根本原因 / Root Cause",
                color = AgpToolColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = result.rootCauseZh,
                color = AgpToolColors.TextPrimary,
                fontSize = 13.sp
            )
            Text(
                text = result.rootCause,
                color = AgpToolColors.TextSecondary,
                fontSize = 12.sp
            )

            // Fix Suggestions / 修复建议
            Text(
                text = "🛠️ 修复建议 / Fix Suggestions",
                color = AgpToolColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            result.fixSuggestionsZh.forEachIndexed { index, suggestion ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${index + 1}.",
                        color = AgpToolColors.Primary,
                        fontSize = 13.sp
                    )
                    Column {
                        Text(
                            text = suggestion,
                            color = AgpToolColors.TextPrimary,
                            fontSize = 13.sp
                        )
                        if (index < result.fixSuggestions.size) {
                            Text(
                                text = result.fixSuggestions[index],
                                color = AgpToolColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Reference Links / 参考链接
            if (result.referenceLinks.isNotEmpty()) {
                Text(
                    text = "📚 参考文档 / Reference Docs",
                    color = AgpToolColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                result.referenceLinks.forEach { link ->
                    Text(
                        text = link,
                        color = AgpToolColors.Primary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ============ Compliance Tab ============
// ============ 合规检测 Tab ============

@Composable
private fun ComplianceTab(
    state: ComplianceState,
    onIntent: (AgpUpgradeToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "✅ CI 合规检测 / Compliance Check",
            color = AgpToolColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "检测项目是否满足 Compose 1.12.0 / AGP 9.2.0 升级要求 / Check if project meets Compose 1.12.0 / AGP 9.2.0 requirements",
            color = AgpToolColors.TextSecondary,
            fontSize = 13.sp
        )

        // Path Input / 路径输入
        OutlinedTextField(
            value = state.projectPath,
            onValueChange = { onIntent(AgpUpgradeToolIntent.UpdateCompliancePath(it)) },
            label = { Text("Project Path / 项目路径") },
            placeholder = { Text("/path/to/project") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgpToolColors.Primary,
                unfocusedBorderColor = AgpToolColors.TextSecondary
            )
        )

        // Run Check Button / 运行检测按钮
        Button(
            onClick = { onIntent(AgpUpgradeToolIntent.RunComplianceCheck) },
            enabled = !state.isChecking && state.projectPath.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AgpToolColors.Primary
            )
        ) {
            if (state.isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AgpToolColors.TextPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Checking... / 检测中...")
            } else {
                Icon(Icons.Default.VerifiedUser, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Run Compliance Check / 运行合规检测")
            }
        }

        // Log Output / 日志输出
        if (state.logOutput.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = AgpToolColors.CodeBg
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Log Output / 日志输出",
                        color = AgpToolColors.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectionContainer {
                        Text(
                            text = state.logOutput,
                            color = AgpToolColors.CodeText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Compliance Report / 合规报告
        state.report?.let { report ->
            ComplianceReportCard(
                report = report,
                onExport = { onIntent(AgpUpgradeToolIntent.ExportComplianceReport) }
            )
        }
    }
}

@Composable
private fun ComplianceReportCard(
    report: ComplianceReport,
    onExport: () -> Unit
) {
    val bannerColor = when (report.overallStatus) {
        ComplianceStatus.Compliant -> AgpToolColors.CompliantGreen
        ComplianceStatus.Partial -> AgpToolColors.PartialYellow
        ComplianceStatus.NonCompliant -> AgpToolColors.NonCompliantRed
    }

    val statusText = when (report.overallStatus) {
        ComplianceStatus.Compliant -> "✅ 完全合规 / Fully Compliant"
        ComplianceStatus.Partial -> "⚠️ 部分合规 / Partially Compliant"
        ComplianceStatus.NonCompliant -> "🚨 不合规 / Non-Compliant"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = AgpToolColors.CardBg
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Overall Status Banner / 整体状态横幅
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bannerColor.copy(alpha = 0.2f))
                    .border(1.dp, bannerColor, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusText,
                    color = bannerColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Check Items / 检测项列表
            Text(
                text = "📋 检测项详情 / Check Items Detail",
                color = AgpToolColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            report.checkItems.forEach { item ->
                ComplianceCheckItemRow(item = item)
            }

            // Export Button / 导出按钮
            OutlinedButton(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AgpToolColors.Primary
                )
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export JSON Report / 导出 JSON 报告")
            }
        }
    }
}

@Composable
private fun ComplianceCheckItemRow(item: ComplianceCheckItem) {
    val statusIcon = when (item.status) {
        ComplianceStatus.Compliant -> "✅"
        ComplianceStatus.Partial -> "⚠️"
        ComplianceStatus.NonCompliant -> "❌"
    }
    val statusColor = when (item.status) {
        ComplianceStatus.Compliant -> AgpToolColors.CompliantGreen
        ComplianceStatus.Partial -> AgpToolColors.PartialYellow
        ComplianceStatus.NonCompliant -> AgpToolColors.NonCompliantRed
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = statusIcon, fontSize = 14.sp)
                    Text(
                        text = item.nameZh,
                        color = AgpToolColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "实际: ${item.actualValue}  |  预期: ${item.expectedValue}",
                    color = AgpToolColors.TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = item.fixSuggestionZh,
                    color = statusColor,
                    fontSize = 11.sp
                )
            }
        }
    }
}
