package com.mvi.kenny.feature.appfunctionstool

// ================================================================
// AppFunctionsToolScreen — Android AppFunctions 开发工具包主界面
// ================================================================
// Main screen for Android AppFunctions Development Toolkit.
//
// PRD-214: Android AppFunctions 开发工具包
// Features:
//   Tab 1: 策略扫描器 — 扫描源码 → 暴露建议报告
//   Tab 2: 能力描述器 — Kotlin 签名 → 自描述 JSON
//   Tab 3: 权限合规检测 — Gradle 插件配置 + 检测结果
//   Tab 4: 错误处理模板 — 4 种错误类型 → 代码模板
//   Tab 5: 决策树 + 调试面板 — 决策对比 + 模拟调用日志
// ================================================================

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Tab Definitions
// ================================================================

private data class AppFunctionsTab(
    val title: String,
    val emoji: String,
    val icon: ImageVector
)

private val APP_FUNCTIONS_TABS = listOf(
    AppFunctionsTab("策略扫描器", "🔍", Icons.Default.Search),
    AppFunctionsTab("能力描述器", "📝", Icons.Default.Info),
    AppFunctionsTab("权限合规", "🔒", Icons.Default.CheckCircle),
    AppFunctionsTab("错误模板", "⚠️", Icons.Default.Error),
    AppFunctionsTab("决策+调试", "🌲", Icons.Default.Terminal)
)

// ================================================================
// Main Screen Composable
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFunctionsToolScreen(
    viewModel: AppFunctionsToolViewModel,
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Update TopAppBar when tab changes
    LaunchedEffect(state.selectedTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "AppFunctions 工具台",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "重置",
                        onClick = { viewModel.sendIntent(AppFunctionsToolIntent.ResetAll) }
                    )
                )
            )
        )
    }

    // Listen for effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AppFunctionsToolEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is AppFunctionsToolEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(effect.label, effect.content)
                    clipboard.setPrimaryClip(clip)
                }
                is AppFunctionsToolEffect.ShowError -> {
                    snackbarHostState.showSnackbar("ERROR: ${effect.message}")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Tab Row ─────────────────────────────────────────────
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = Color(0xFF1E1E2E),
                contentColor = Color.White
            ) {
                APP_FUNCTIONS_TABS.forEachIndexed { index, tab ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.sendIntent(AppFunctionsToolIntent.SelectTab(index)) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tab.emoji, fontSize = 14.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(tab.title, fontSize = 11.sp)
                            }
                        },
                        selectedContentColor = Color(0xFFBB86FC),
                        unselectedContentColor = Color(0xFF8B949E)
                    )
                }
            }

            // ── Tab Content ──────────────────────────────────────────
            AnimatedContent(
                targetState = state.selectedTab,
                transitionSpec = {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                },
                label = "TabContentAnimation"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> Tab1_Scanner(viewModel, state)
                    1 -> Tab2_CapabilityDescriber(viewModel, state)
                    2 -> Tab3_ComplianceCheck(viewModel, state)
                    3 -> Tab4_ErrorTemplates(viewModel, state)
                    4 -> Tab5_DecisionTreeAndDebug(viewModel, state)
                    else -> Tab1_Scanner(viewModel, state)
                }
            }
        }
    }
}

// ================================================================
// Tab 1: 策略扫描器
// ================================================================

@Composable
private fun Tab1_Scanner(viewModel: AppFunctionsToolViewModel, state: AppFunctionsToolState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
            .padding(16.dp)
    ) {
        Text(
            text = "AppFunctions 暴露策略扫描器",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "扫描源码目录，识别适合暴露给 AI Agent 的函数",
            fontSize = 12.sp,
            color = Color(0xFF8B949E),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = state.sourcePath,
            onValueChange = { viewModel.sendIntent(AppFunctionsToolIntent.UpdateSourcePath(it)) },
            label = { Text("源码路径 (app/src/main/java)") },
            placeholder = { Text("例如: app/src/main/java/com/example/app") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.sendIntent(AppFunctionsToolIntent.StartScan(simulate = false)) },
                enabled = !state.isScanning,
                modifier = Modifier.weight(1f)
            ) {
                if (state.isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("扫描中...")
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("开始扫描")
                }
            }
            OutlinedButton(
                onClick = { viewModel.sendIntent(AppFunctionsToolIntent.StartScan(simulate = true)) },
                enabled = !state.isScanning
            ) {
                Text("模拟数据")
            }
        }

        if (state.isScanning) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { state.scanProgress },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFBB86FC)
            )
            Text(
                text = "正在分析代码结构... ${(state.scanProgress * 100).toInt()}%",
                fontSize = 11.sp,
                color = Color(0xFF8B949E)
            )
        }

        Spacer(Modifier.height(16.dp))

        if (state.scanResults.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "扫描结果 (${state.scanResults.size} 个函数)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                val highCount = state.scanResults.count { it.exposureLevel == ExposureLevel.HIGH }
                val medCount = state.scanResults.count { it.exposureLevel == ExposureLevel.MEDIUM }
                val lowCount = state.scanResults.count { it.exposureLevel == ExposureLevel.LOW }
                Text(
                    text = "HIGH:$highCount  MED:$medCount  LOW:$lowCount",
                    fontSize = 12.sp,
                    color = Color(0xFF8B949E)
                )
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.scanResults) { suggestion ->
                    ExposureSuggestionCard(
                        suggestion = suggestion,
                        onCopyTemplate = {
                            viewModel.sendIntent(
                                AppFunctionsToolIntent.CopyToClipboard(
                                    suggestion.appFunctionTemplate,
                                    "@AppFunction Template"
                                )
                            )
                        }
                    )
                }
            }
        } else if (!state.isScanning) {
            EmptyStateCard(
                emoji = "🔍",
                title = "尚未扫描",
                message = "输入源码路径并点击「开始扫描」来分析可暴露函数"
            )
        }
    }
}

@Composable
private fun ExposureSuggestionCard(
    suggestion: ExposureSuggestion,
    onCopyTemplate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D3F)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(suggestion.exposureLevel.color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${suggestion.exposureLevel.emoji} ${suggestion.functionName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Text(
                    text = suggestion.exposureLevel.displayName,
                    fontSize = 11.sp,
                    color = suggestion.exposureLevel.color
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = suggestion.filePath,
                fontSize = 10.sp,
                color = Color(0xFF8B949E),
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = suggestion.functionSignature,
                fontSize = 11.sp,
                color = Color(0xFFBB86FC),
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .background(Color(0xFF1E1E2E), RoundedCornerShape(4.dp))
                    .padding(4.dp)
            )

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                if (suggestion.exposureReasons.isNotEmpty()) {
                    Text("暴露理由:", fontSize = 11.sp, color = Color(0xFF6BCF7F))
                    suggestion.exposureReasons.forEach {
                        Text("  - $it", fontSize = 10.sp, color = Color(0xFF8B949E))
                    }
                }
                if (suggestion.cautionReasons.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("注意事项:", fontSize = 11.sp, color = Color(0xFFFFB347))
                    suggestion.cautionReasons.forEach {
                        Text("  - $it", fontSize = 10.sp, color = Color(0xFFFF6B6B))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("@AppFunction 模板:", fontSize = 11.sp, color = Color.White)
                Text(
                    text = suggestion.appFunctionTemplate.take(200) + if (suggestion.appFunctionTemplate.length > 200) "..." else "",
                    fontSize = 9.sp,
                    color = Color(0xFF8B949E),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(Color(0xFF1E1E2E), RoundedCornerShape(4.dp))
                        .padding(6.dp)
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onCopyTemplate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4A6A))
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("复制 @AppFunction 模板", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = if (expanded) "收起 ▲" else "展开详情 ▼",
                    fontSize = 11.sp,
                    color = Color(0xFFBB86FC)
                )
            }
        }
    }
}

// ================================================================
// Tab 2: 能力描述器
// ================================================================

@Composable
private fun Tab2_CapabilityDescriber(viewModel: AppFunctionsToolViewModel, state: AppFunctionsToolState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
            .padding(16.dp)
    ) {
        Text(
            text = "AppFunction 能力描述生成器",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "粘贴 Kotlin 函数签名，自动生成 AppFunction 自描述 JSON",
            fontSize = 12.sp,
            color = Color(0xFF8B949E),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = state.kotlinCode,
            onValueChange = { viewModel.sendIntent(AppFunctionsToolIntent.UpdateKotlinCode(it)) },
            label = { Text("Kotlin 函数代码") },
            placeholder = { Text("例如: fun sendMessage(recipientId: String, content: String): Boolean") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 8
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { viewModel.sendIntent(AppFunctionsToolIntent.GenerateJson) },
            enabled = !state.isGeneratingJson && state.kotlinCode.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isGeneratingJson) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("生成中...")
            } else {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("生成 JSON")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (state.generatedJson.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("生成的 AppFunction JSON:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                IconButton(onClick = {
                    viewModel.sendIntent(AppFunctionsToolIntent.CopyToClipboard(state.generatedJson, "JSON"))
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制", tint = Color(0xFFBB86FC))
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF2D2D3F), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = state.generatedJson,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6BCF7F)
                    )
                }
            }
        } else {
            EmptyStateCard(
                emoji = "📝",
                title = "尚未生成",
                message = "粘贴 Kotlin 函数代码并点击「生成 JSON」"
            )
        }
    }
}

// ================================================================
// Tab 3: 权限合规检测
// ================================================================

@Composable
private fun Tab3_ComplianceCheck(viewModel: AppFunctionsToolViewModel, state: AppFunctionsToolState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
            .padding(16.dp)
    ) {
        Text(
            text = "AppFunctions 权限合规检测",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "检测 build.gradle.kts 中的 AppFunctions 配置是否满足 Google Play 政策",
            fontSize = 12.sp,
            color = Color(0xFF8B949E),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = state.gradleConfig,
            onValueChange = { viewModel.sendIntent(AppFunctionsToolIntent.UpdateGradleConfig(it)) },
            label = { Text("build.gradle.kts 内容（可选）") },
            placeholder = { Text("粘贴 build.gradle.kts 内容以进行真实检测...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 6
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.sendIntent(AppFunctionsToolIntent.CheckCompliance(simulate = false)) },
                enabled = !state.isCheckingCompliance,
                modifier = Modifier.weight(1f)
            ) {
                if (state.isCheckingCompliance) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("检测中...")
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("开始检测")
                }
            }
            OutlinedButton(
                onClick = { viewModel.sendIntent(AppFunctionsToolIntent.CheckCompliance(simulate = true)) },
                enabled = !state.isCheckingCompliance
            ) {
                Text("模拟数据")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (state.complianceResults.isNotEmpty()) {
            val passCount = state.complianceResults.count { it.status == ComplianceStatus.PASS }
            val warnCount = state.complianceResults.count { it.status == ComplianceStatus.WARNING }
            val failCount = state.complianceResults.count { it.status == ComplianceStatus.VIOLATION }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip("PASS", passCount.toString(), Color(0xFF6BCF7F))
                StatChip("WARN", warnCount.toString(), Color(0xFFFFB347))
                StatChip("FAIL", failCount.toString(), Color(0xFFFF6B6B))
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.complianceResults) { result ->
                    ComplianceResultCard(result = result)
                }
            }
        } else if (!state.isCheckingCompliance) {
            EmptyStateCard(
                emoji = "🔒",
                title = "尚未检测",
                message = "粘贴 Gradle 配置并点击「开始检测」以检查合规性"
            )
        }
    }
}

@Composable
private fun StatChip(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = Color(0xFF8B949E))
    }
}

@Composable
private fun ComplianceResultCard(result: ComplianceResult) {
    val statusIcon: ImageVector = when (result.status) {
        ComplianceStatus.PASS -> Icons.Default.CheckCircle
        ComplianceStatus.WARNING -> Icons.Default.Warning
        ComplianceStatus.VIOLATION -> Icons.Default.Error
        ComplianceStatus.UNKNOWN -> Icons.Default.Info
    }
    val statusColor = when (result.status) {
        ComplianceStatus.PASS -> Color(0xFF6BCF7F)
        ComplianceStatus.WARNING -> Color(0xFFFFB347)
        ComplianceStatus.VIOLATION -> Color(0xFFFF6B6B)
        ComplianceStatus.UNKNOWN -> Color(0xFF8B949E)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D3F)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = result.rule, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text(text = result.message, fontSize = 11.sp, color = Color(0xFF8B949E), modifier = Modifier.padding(top = 2.dp))
                if (result.fixSuggestion.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("修复建议:", fontSize = 10.sp, color = Color(0xFFBB86FC))
                    Text(
                        text = result.fixSuggestion,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6BCF7F),
                        modifier = Modifier
                            .background(Color(0xFF1E1E2E), RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    )
                }
            }
            Text(text = result.status.emoji, fontSize = 16.sp)
        }
    }
}

// ================================================================
// Tab 4: 错误处理模板
// ================================================================

@Composable
private fun Tab4_ErrorTemplates(viewModel: AppFunctionsToolViewModel, state: AppFunctionsToolState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
            .padding(16.dp)
    ) {
        Text(
            text = "AppFunction 错误处理模板",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "选择错误类型，生成完整的 @AppFunction 错误处理代码模板",
            fontSize = 12.sp,
            color = Color(0xFF8B949E),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text("错误类型:", fontSize = 13.sp, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ErrorType.entries.forEach { type ->
                FilterChip(
                    selected = state.selectedErrorType == type,
                    onClick = { viewModel.sendIntent(AppFunctionsToolIntent.SelectErrorType(type)) },
                    label = { Text("${type.displayName} (${type.displayNameCn})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFBB86FC),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (state.generatedTemplate.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("代码模板:", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                IconButton(onClick = {
                    viewModel.sendIntent(AppFunctionsToolIntent.CopyToClipboard(state.generatedTemplate, "ErrorTemplate"))
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制", tint = Color(0xFFBB86FC))
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF2D2D3F), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = state.generatedTemplate,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6BCF7F),
                        lineHeight = 14.sp
                    )
                }
            }
        } else {
            EmptyStateCard(
                emoji = "⚠️",
                title = "尚未生成模板",
                message = "选择错误类型即可自动生成代码模板"
            )
        }
    }
}

// ================================================================
// Tab 5: 决策树 + 调试面板
// ================================================================

@Composable
private fun Tab5_DecisionTreeAndDebug(viewModel: AppFunctionsToolViewModel, state: AppFunctionsToolState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2E))
            .padding(16.dp)
    ) {
        Text(
            text = "AppFunctions vs Deep Links vs Shortcuts 决策树",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "三种「App 被触发」方式的场景对比和选型指南",
            fontSize = 12.sp,
            color = Color(0xFF8B949E),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Decision Matrix Table
        LazyColumn(
            modifier = Modifier.weight(0.45f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            // Header
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3D3D5C)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text("维度", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(0.9f))
                        Text("AppFunctions", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBB86FC), modifier = Modifier.weight(1f))
                        Text("DeepLinks", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6BCF7F), modifier = Modifier.weight(0.8f))
                        Text("Shortcuts", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB347), modifier = Modifier.weight(0.8f))
                    }
                }
            }
            items(DECISION_MATRIX) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D3F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(item.dimension, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF8B949E))
                        Spacer(Modifier.height(2.dp))
                        Text(item.appFunctions, fontSize = 9.sp, color = Color.White, modifier = Modifier.weight(0.9f))
                        Text(item.deepLinks, fontSize = 9.sp, color = Color(0xFF8B949E), modifier = Modifier.weight(0.8f))
                        Text(item.shortcuts, fontSize = 9.sp, color = Color(0xFF8B949E), modifier = Modifier.weight(0.8f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Debug Panel
        Text(
            text = "模拟 Agent 调用调试面板",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "模拟 AI Agent 调用 AppFunction，查看调用日志",
            fontSize = 12.sp,
            color = Color(0xFF8B949E),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = state.simulatedCallParams,
            onValueChange = { viewModel.sendIntent(AppFunctionsToolIntent.UpdateSimulatedParams(it)) },
            label = { Text("调用参数 (JSON)") },
            placeholder = { Text("""{"function": "send_message", "recipientId": "user_123"}""") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { viewModel.sendIntent(AppFunctionsToolIntent.SimulateCall) },
            enabled = !state.isSimulatingCall,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSimulatingCall) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("调用中...")
            } else {
                Icon(Icons.Default.Android, contentDescription = null)
                Spacer( Modifier.width(8.dp))
                Text("模拟 Agent 调用")
            }
        }

        Spacer(Modifier.height(8.dp))

        if (state.debugLogs.isNotEmpty()) {
            Text(
                text = "调用日志 (${state.debugLogs.size})",
                fontSize = 12.sp,
                color = Color(0xFF8B949E),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(0.55f)
            ) {
                items(state.debugLogs) { log ->
                    DebugLogCard(log = log)
                }
            }
        } else {
            EmptyStateCard(
                emoji = "🛠️",
                title = "尚无调用日志",
                message = "点击「模拟 Agent 调用」开始调试"
            )
        }
    }
}

@Composable
private fun DebugLogCard(log: DebugLogEntry) {
    val bgColor = if (log.status == "SUCCESS") Color(0xFF2D3D2D) else Color(0xFF3D2D2D)
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.timestamp,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF8B949E)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (log.status == "SUCCESS") "OK" else "FAIL",
                        fontSize = 12.sp,
                        color = if (log.status == "SUCCESS") Color(0xFF6BCF7F) else Color(0xFFFF6B6B)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${log.durationMs}ms",
                        fontSize = 9.sp,
                        color = Color(0xFF8B949E)
                    )
                }
                Text(
                    text = log.caller.take(50),
                    fontSize = 9.sp,
                    color = Color(0xFFBB86FC),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Params: ${log.params.take(60)}",
                    fontSize = 8.sp,
                    color = Color(0xFF6BCF7F),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = log.result.take(80),
                    fontSize = 8.sp,
                    color = if (log.status == "SUCCESS") Color(0xFF6BCF7F) else Color(0xFFFF6B6B),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ================================================================
// Shared Components
// ================================================================

@Composable
private fun EmptyStateCard(emoji: String, title: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D3F)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = Color(0xFF8B949E)
            )
        }
    }
}
