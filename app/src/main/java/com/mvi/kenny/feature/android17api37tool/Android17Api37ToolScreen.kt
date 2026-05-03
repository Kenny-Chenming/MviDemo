package com.mvi.kenny.feature.android17api37tool

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import com.mvi.kenny.base.TopBarConfig
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * Android17Api37ToolScreen — Android 17 API 37 综合迁移工具主界面
 * ============================================================
 * PRD-220 | Android 17 (API 37) 破坏性变更综合迁移工具包
 *
 * 五阶段流水线 Tab 布局：
 * — Tab 1: LNP 检测（Local Network Permission Scanner）
 * — Tab 2: 申请模板（Permission Template）
 * — Tab 3: CI 合规（CI Compliance）
 * — Tab 4: 行为变更（Behavior Changes）
 * — Tab 5: 隐私决策（Privacy & Decision）
 *
 * @param state UI 状态
 * @param onIntent 发送用户意图的回调
 * @param onUpdateTopBar TopBar 配置更新回调
 */
@Composable
fun Android17Api37ToolScreen(
    state: Android17Api37ToolState,
    onIntent: (Android17Api37ToolIntent) -> Unit,
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val context = LocalContext.current

    // Update TopBar config / 更新 TopBar 配置
    LaunchedEffect(state.currentTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Android 17 API 37 迁移工具"
            )
        )
    }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        // Effect collection handled via SharedFlow in ViewModel
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Android 17 Header / Android 17 头部
        Android17Header()

        // Tab Row / Tab 导航
        MainTabRow(
            currentTab = state.currentTab,
            onTabSelected = { onIntent(Android17Api37ToolIntent.SetTab(it)) }
        )

        // Tab Content / Tab 内容
        AnimatedContent(
            targetState = state.currentTab,
            modifier = Modifier.weight(1f),
            label = "TabContent"
        ) { tab ->
            when (tab) {
                0 -> LNPScannerTab(state, onIntent, context)
                1 -> PermissionTemplateTab(state, onIntent, context)
                2 -> CIComplianceTab(state, onIntent, context)
                3 -> BehaviorChangesTab(state, onIntent)
                4 -> PrivacyDecisionTab(state, onIntent, context)
                else -> LNPScannerTab(state, onIntent, context)
            }
        }
    }
}

// ================================================================
// Android 17 Header / Android 17 头部
// ================================================================

@Composable
private fun Android17Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Android Robot icon placeholder / Android 机器人图标占位
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Android17Green, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "17",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Android 17 API 37",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "破坏性变更综合迁移工具包",
                color = Color(0xFFB3B3B3),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // API Level badge / API Level 标签
        Box(
            modifier = Modifier
                .background(Color(0xFF2E2E2E), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "API 37",
                color = Android17Green,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ================================================================
// Main Tab Row / 主 Tab 导航
// ================================================================

@Composable
private fun MainTabRow(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = currentTab,
        containerColor = Color(0xFF1E1E1E),
        contentColor = Color.White,
        edgePadding = 0.dp
    ) {
        MainTab.entries.forEachIndexed { index, tab ->
            Tab(
                selected = currentTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = tab.titleZh,
                        fontSize = 12.sp,
                        fontWeight = if (currentTab == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentTab == index) Android17Green else Color(0xFFB3B3B3)
                    )
                }
            )
        }
    }
}

// ================================================================
// Tab 1: LNP Scanner / Tab 1: LNP 检测
// ================================================================

@Composable
private fun LNPScannerTab(
    state: Android17Api37ToolState,
    onIntent: (Android17Api37ToolIntent) -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // LNP 版本说明 / LNP version explanation
        LNPHeaderCard()

        Spacer(modifier = Modifier.height(12.dp))

        // Scan input / 扫描输入
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.scanPath,
                onValueChange = { onIntent(Android17Api37ToolIntent.SetScanPath(it)) },
                label = { Text("源码路径 / Source Path") },
                placeholder = { Text("/Users/.../MyProject") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onIntent(Android17Api37ToolIntent.StartScan) },
                enabled = !state.isScanning,
                colors = ButtonDefaults.buttonColors(containerColor = Android17Green)
            ) {
                if (state.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black)
                }
            }
        }

        // Scan progress / 扫描进度
        if (state.isScanning) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.scanProgress },
                modifier = Modifier.fillMaxWidth(),
                color = Android17Green,
                trackColor = Color(0xFF2E2E2E)
            )
            Text(
                text = "Scanning... ${(state.scanProgress * 100).toInt()}%",
                color = Color(0xFFB3B3B3),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scan results / 扫描结果
        if (state.scanResults.isNotEmpty()) {
            Text(
                text = "检测结果 (${state.scanResults.size}) / Scan Results",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.scanResults) { result ->
                    LNPScanResultCard(
                        result = result,
                        isSelected = state.selectedResult?.id == result.id,
                        onClick = {
                            onIntent(Android17Api37ToolIntent.SelectScanResult(
                                if (state.selectedResult?.id == result.id) null else result
                            ))
                        },
                        onCopy = {
                            copyToClipboard(context, result.manifestSuggestion)
                        }
                    )
                }
            }
        } else if (!state.isScanning && state.scanPath.isNotBlank()) {
            EmptyStateCard(
                message = "点击搜索按钮开始扫描 / Click search to start scanning"
            )
        } else {
            EmptyStateCard(
                message = "输入源码路径开始 LNP 检测 / Enter source path to start LNP scanning"
            )
        }
    }
}

@Composable
private fun LNPHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = SeverityWarning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Local Network Permission: Android 16 → 17",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VersionBadge(text = "Android 16: Opt-in", color = SeverityInfo)
                VersionBadge(text = "Android 17: Mandatory", color = SeverityCritical)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "从 opt-in 升级为强制要求。所有访问局域网的 App 必须显式申请并处理拒绝场景。",
                color = Color(0xFFB3B3B3),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun VersionBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LNPScanResultCard(
    result: LNPScanResult,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCopy: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Android17Green else Color(0xFF2E2E2E),
        label = "BorderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Severity badge / 严重等级标签
                Box(
                    modifier = Modifier
                        .background(result.severity.color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = result.severity.labelZh,
                        color = result.severity.color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = result.riskType,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color(0xFFB3B3B3),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = result.filePath,
                color = Color(0xFFB3B3B3),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (result.lineNumber != null) {
                Text(
                    text = "Line ${result.lineNumber}",
                    color = Android17Green,
                    fontSize = 10.sp
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFF2E2E2E))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "修复建议 / Fix Suggestion:",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2E2E2E), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = result.manifestSuggestion,
                        color = Android17Green,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ================================================================
// Tab 2: Permission Template / Tab 2: 权限申请模板
// ================================================================

@Composable
private fun PermissionTemplateTab(
    state: Android17Api37ToolState,
    onIntent: (Android17Api37ToolIntent) -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Scenario selector / 场景选择器
        Text(
            text = "选择场景 / Select Scenario",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LNPScenario.entries.forEach { scenario ->
                ScenarioChip(
                    scenario = scenario,
                    isSelected = state.selectedScenario == scenario,
                    onClick = { onIntent(Android17Api37ToolIntent.SelectScenario(scenario)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manifest diff / Manifest diff
        Text(
            text = "Manifest 声明 / Manifest Declaration",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        ManifestDiffCard(
            diff = state.manifestDiff,
            onCopy = { copyToClipboard(context, state.manifestDiff) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Code templates / 代码模板
        Text(
            text = "代码模板 / Code Templates",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filteredTemplates = state.codeTemplates.filter {
                it.scenario == state.selectedScenario
            }
            items(filteredTemplates) { template ->
                CodeTemplateCard(
                    template = template,
                    onCopy = { copyToClipboard(context, template.code) }
                )
            }

            if (filteredTemplates.isEmpty()) {
                item {
                    EmptyStateCard(
                        message = "该场景暂无模板 / No templates for this scenario yet"
                    )
                }
            }
        }
    }
}

@Composable
private fun ScenarioChip(
    scenario: LNPScenario,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Android17Green else Color(0xFF2E2E2E),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isSelected) Android17Green else Color(0xFF3E3E3E),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = scenario.titleZh,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ManifestDiffCard(diff: String, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = diff,
                    color = Android17Green,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }

            IconButton(onClick = onCopy) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color(0xFFB3B3B3)
                )
            }
        }
    }
}

@Composable
private fun CodeTemplateCard(template: CodeTemplate, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Android17Green,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0D0D), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = template.code,
                    color = Color(0xFFE0E0E0),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ================================================================
// Tab 3: CI Compliance / Tab 3: CI 合规
// ================================================================

@Composable
private fun CIComplianceTab(
    state: Android17Api37ToolState,
    onIntent: (Android17Api37ToolIntent) -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // CI Header / CI 头部
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CI 合规检测工具 / CI Compliance Checker",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Gradle 插件：com.android.tools:android17-compliance:1.0.0",
                    color = Android17Green,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "检测 App 是否已声明 ACCESS_LOCAL_NETWORK，阻塞 CI 直到合规。",
                    color = Color(0xFFB3B3B3),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Run check button / 运行检查按钮
        Button(
            onClick = { onIntent(Android17Api37ToolIntent.RunCIComplianceCheck) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Android17Green),
            enabled = state.ciComplianceStatus != ComplianceStatus.UNKNOWN ||
                      state.complianceReport == null
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "运行 CI 合规检测 / Run CI Compliance Check",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Compliance status / 合规状态
        if (state.complianceReport != null) {
            val report = state.complianceReport

            ComplianceStatusCard(report = report)

            Spacer(modifier = Modifier.height(16.dp))

            // Gradle plugin guide / Gradle 插件配置指南
            GradlePluginGuideCard(onCopy = {
                copyToClipboard(context, buildGradlePluginConfig())
            })
        } else {
            EmptyStateCard(
                message = "点击上方按钮运行 CI 合规检测 / Click the button above to run CI check"
            )
        }
    }
}

@Composable
private fun ComplianceStatusCard(report: ComplianceReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(report.status.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CI 合规状态: ${report.status.labelZh}",
                    color = report.status.color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ComplianceMetric(label = "已声明", value = "${report.declared}", color = Android17Green)
                ComplianceMetric(label = "未声明", value = "${report.missing}", color = SeverityCritical)
                ComplianceMetric(
                    label = "阻塞 CI",
                    value = if (report.blocking) "是" else "否",
                    color = if (report.blocking) SeverityCritical else Android17Green
                )
            }
        }
    }
}

@Composable
private fun ComplianceMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Text(
            text = label,
            color = Color(0xFFB3B3B3),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun GradlePluginGuideCard(onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gradle 插件配置 / Plugin Configuration",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCopy) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Android17Green
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0D0D), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = buildGradlePluginConfig(),
                    color = Color(0xFFE0E0E0),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

private fun buildGradlePluginConfig(): String = """
// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

// build.gradle.kts (project level)
plugins {
    id("com.android.tools.android17-compliance") version "1.0.0"
}

// build.gradle.kts (app level)
android {
    android17Compliance {
        checkLocalNetworkPermission = true
        blocking = true
        failOnNonCompliant = true
    }
}
""".trimIndent()

// ================================================================
// Tab 4: Behavior Changes / Tab 4: 行为变更
// ================================================================

@Composable
private fun BehaviorChangesTab(
    state: Android17Api37ToolState,
    onIntent: (Android17Api37ToolIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sub-tab row / 子 Tab 行
        ScrollableTabRow(
            selectedTabIndex = state.selectedBehaviorTab,
            containerColor = Color(0xFF1E1E1E),
            contentColor = Color.White,
            edgePadding = 0.dp
        ) {
            BehaviorSubTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = state.selectedBehaviorTab == index,
                    onClick = { onIntent(Android17Api37ToolIntent.SetBehaviorSubTab(index)) },
                    text = {
                        Text(
                            text = tab.titleZh,
                            fontSize = 12.sp,
                            color = if (state.selectedBehaviorTab == index) Android17Green else Color(0xFFB3B3B3)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Checklist / 检查清单
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.behaviorChecklist) { item ->
                BehaviorCheckCard(
                    item = item,
                    onToggle = { onIntent(Android17Api37ToolIntent.ToggleBehaviorCheck(item.id)) }
                )
            }
        }

        // Progress summary / 进度摘要
        val checkedCount = state.behaviorChecklist.count { it.isChecked }
        val totalCount = state.behaviorChecklist.size

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "检查进度 / Progress: $checkedCount / $totalCount",
                color = Color(0xFFB3B3B3),
                fontSize = 12.sp
            )

            val progress by animateFloatAsState(
                targetValue = if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f,
                label = "Progress"
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(100.dp)
                    .height(4.dp),
                color = Android17Green,
                trackColor = Color(0xFF2E2E2E)
            )
        }
    }
}

@Composable
private fun BehaviorCheckCard(
    item: BehaviorCheckItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox / 复选框
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (item.isChecked) Android17Green else Color(0xFF2E2E2E),
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        1.dp,
                        if (item.isChecked) Android17Green else Color(0xFF3E3E3E),
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.isChecked) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.titleZh,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    if (item.isBreaking) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(SeverityCritical.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Breaking",
                                color = SeverityCritical,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.descriptionZh,
                    color = Color(0xFFB3B3B3),
                    fontSize = 12.sp
                )
            }

            // Priority badge / 优先级标签
            Box(
                modifier = Modifier
                    .background(Color(0xFF2E2E2E), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "P${item.priority}",
                    color = when (item.priority) {
                        1 -> SeverityCritical
                        2 -> SeverityWarning
                        else -> SeverityInfo
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ================================================================
// Tab 5: Privacy & Decision / Tab 5: 隐私与决策
// ================================================================

@Composable
private fun PrivacyDecisionTab(
    state: Android17Api37ToolState,
    onIntent: (Android17Api37ToolIntent) -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Privacy Policy Guide / 隐私合规指南
            item {
                PrivacyPolicyGuideCard(
                    text = state.privacyGuideText,
                    onCopy = { copyToClipboard(context, state.privacyGuideText) }
                )
            }

            // Decision Tree / 决策树
            item {
                Text(
                    text = "迁移决策树 / Migration Decision Tree",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            items(state.decisionTreeNodes) { node ->
                DecisionTreeCard(
                    node = node,
                    depth = 0,
                    onToggle = { onIntent(Android17Api37ToolIntent.ToggleDecisionNode(it)) }
                )
            }
        }
    }
}

@Composable
private fun PrivacyPolicyGuideCard(text: String, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "隐私合规指南 /Privacy Decision / 隐私合规指南",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCopy) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Android17Green
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0D0D), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = text,
                    color = Color(0xFFE0E0E0),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun DecisionTreeCard(
    node: DecisionNode,
    depth: Int,
    onToggle: (String) -> Unit
) {
    val horizontalPadding = (depth * 16).dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = horizontalPadding),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Node question / 节点问题
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = { onToggle(node.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (node.isExpanded)
                            Icons.Default.KeyboardArrowDown
                        else
                            Icons.Default.KeyboardArrowRight,
                        contentDescription = "Toggle",
                        tint = Android17Green,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.questionZh,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = node.question,
                        color = Color(0xFF808080),
                        fontSize = 11.sp
                    )
                }
            }

            // Node action / 节点操作
            if (node.action.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Android17Green.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, Android17Green.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "✅ ${node.actionZh}",
                        color = Android17Green,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Children / 子节点
            if (node.isExpanded && node.children.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    node.children.forEach { child ->
                        DecisionTreeCard(
                            node = child,
                            depth = depth + 1,
                            onToggle = onToggle
                        )
                    }
                }
            }
        }
    }
}

// ================================================================
// Shared Components / 共享组件
// ================================================================

/**
 * Empty state card / 空状态卡片
 */
@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color(0xFF808080),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Copy text to clipboard / 复制文本到剪贴板
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Android 17 Tool", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "已复制 / Copied", Toast.LENGTH_SHORT).show()
}
