// ================================================================
// AudioMigrationToolScreen — 音频迁移工具页面
// Audio Migration Tool Screen
// ================================================================
// PRD-306: Android 17 后台音频 Foreground Service 迁移检测与适配工具包
//
// 页面包含三个标签：
// 1. 音频 API 扫描报告
// 2. WIU Foreground Service 脚手架生成器
// 3. 兼容性验证套件
//
// @param viewModel ViewModel for state management
// @param onNavigateToLogin 退出登录回调（占位）
// @param onUpdateTopBar 向 MainScreen 上报 TopBar 配置
// ================================================================

package com.mvi.kenny.feature.audiobackgroundtool

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collect

// ==================== ANSI Colors (Terminal) ====================
// Critical: 红 / Warning: 黄 / Info: 青 / Success: 绿
private val ColorCritical = Color(0xFFF85149)
private val ColorWarning = Color(0xFFFFD700)
private val ColorInfo = Color(0xFF58A6FF)
private val ColorSuccess = Color(0xFF3FB950)

// ==================== Screen ====================

/**
 * 音频迁移工具主页面
 * Audio Migration Tool Main Screen
 */
@Composable
fun AudioMigrationToolScreen(
    viewModel: AudioMigrationToolViewModel = viewModel(),
    onNavigateToLogin: () -> Unit = {},
    onUpdateTopBar: (com.mvi.kenny.base.TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    // TopBar 配置
    val topBarConfig = remember(state.activeTab) {
        com.mvi.kenny.base.TopBarConfig(
            title = "Android 17 音频迁移工具",
            actions = listOfNotNull(
                com.mvi.kenny.base.TopBarActions.settings { }
            )
        )
    }

    LaunchedEffect(topBarConfig) {
        onUpdateTopBar(topBarConfig)
    }

    // Effect 收集
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AudioMigrationToolEffect.ShowToast -> {
                    // Toast 处理（简化）
                }
                is AudioMigrationToolEffect.ShowError -> {
                    // Error 处理
                }
                is AudioMigrationToolEffect.ShowSuccess -> {
                    // Success 处理
                }
                is AudioMigrationToolEffect.ScaffoldGenerated -> {
                    // Scaffold generated
                }
                is AudioMigrationToolEffect.OpenReport -> {
                    // Open report
                }
                is AudioMigrationToolEffect.TestCompleted -> {
                    // Test completed
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = state.activeTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = state.activeTab == 0,
                onClick = { viewModel.sendIntent(AudioMigrationToolIntent.SwitchTab(0)) },
                text = { Text("🔍 扫描报告") }
            )
            Tab(
                selected = state.activeTab == 1,
                onClick = { viewModel.sendIntent(AudioMigrationToolIntent.SwitchTab(1)) },
                text = { Text("📦 脚手架生成") }
            )
            Tab(
                selected = state.activeTab == 2,
                onClick = { viewModel.sendIntent(AudioMigrationToolIntent.SwitchTab(2)) },
                text = { Text("🧪 兼容性测试") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (state.activeTab) {
            0 -> ScanReportTab(
                state = state,
                onIntent = viewModel::sendIntent
            )
            1 -> ScaffoldGeneratorTab(
                state = state,
                onIntent = viewModel::sendIntent
            )
            2 -> CompatibilityTestTab(
                state = state,
                onIntent = viewModel::sendIntent
            )
        }
    }
}

// ==================== Tab 1: Scan Report ====================

@Composable
private fun ScanReportTab(
    state: AudioMigrationToolState,
    onIntent: (AudioMigrationToolIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Cards
        if (state.summary != null) {
            SummaryCards(summary = state.summary)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onIntent(AudioMigrationToolIntent.StartScan) },
                enabled = !state.isScanning
            ) {
                if (state.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("扫描中...")
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始扫描")
                }
            }

            OutlinedButton(
                onClick = { onIntent(AudioMigrationToolIntent.SelectAllViolations) }
            ) {
                Text("全选")
            }

            OutlinedButton(
                onClick = { onIntent(AudioMigrationToolIntent.DeselectAllViolations) }
            ) {
                Text("取消全选")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar
        if (state.isScanning) {
            LinearProgressIndicator(
                progress = { state.scanProgress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Violation List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.violations) { violation ->
                ViolationCard(
                    violation = violation,
                    isSelected = state.selectedViolationIds.contains(violation.id),
                    onToggleSelection = {
                        onIntent(AudioMigrationToolIntent.ToggleViolationSelection(violation.id))
                    }
                )
            }
        }
    }
}

@Composable
private fun SummaryCards(summary: AuditSummaryUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "总数",
            value = summary.total.toString(),
            color = Color.White
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "严重",
            value = summary.critical.toString(),
            color = ColorCritical
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "警告",
            value = summary.warning.toString(),
            color = ColorWarning
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "提示",
            value = summary.info.toString(),
            color = ColorInfo
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ViolationCard(
    violation: AudioViolationUi,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    val severityColor = when (violation.severity) {
        "CRITICAL" -> ColorCritical
        "WARNING" -> ColorWarning
        else -> ColorInfo
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelection() }
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    ColorSuccess,
                    RoundedCornerShape(12.dp)
                )
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Severity Badge
                    Box(
                        modifier = Modifier
                            .background(
                                severityColor.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = violation.severity,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = severityColor
                        )
                    }

                    // API Name
                    Text(
                        text = violation.apiName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // File Path
                Text(
                    text = "${violation.filePath}:${violation.line}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Suggested Fix
                Text(
                    text = "💡 ${violation.suggestedFix}",
                    fontSize = 12.sp,
                    color = Color(0xFFB0B0B0)
                )
            }
        }
    }
}

// ==================== Tab 2: Scaffold Generator ====================

@Composable
private fun ScaffoldGeneratorTab(
    state: AudioMigrationToolState,
    onIntent: (AudioMigrationToolIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Icon(
            Icons.Default.Build,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = ColorInfo
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "WIU Foreground Service 脚手架生成器",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "根据检测到的违规代码，自动生成合规的 Foreground Service 模板",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Selected Count
        if (state.selectedViolationIds.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D2D2D)
                )
            ) {
                Text(
                    text = "已选择 ${state.selectedViolationIds.size} 个违规",
                    modifier = Modifier.padding(16.dp),
                    color = ColorSuccess
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Generate Button
        Button(
            onClick = {
                val ids = if (state.selectedViolationIds.isNotEmpty()) {
                    state.selectedViolationIds.toList()
                } else {
                    state.violations.map { it.id }
                }
                onIntent(AudioMigrationToolIntent.GenerateScaffolding(ids))
            },
            enabled = !state.isGenerating && state.violations.isNotEmpty()
        ) {
            if (state.isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("生成中...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("生成脚手架")
            }
        }

        // Generated Files List
        if (state.generatedFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "✅ 已生成文件：",
                fontWeight = FontWeight.Bold,
                color = ColorSuccess
            )

            Spacer(modifier = Modifier.height(8.dp))

            state.generatedFiles.forEach { file ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = ColorInfo,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = file,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Info Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E3A5F)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📋 生成的内容包括：",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• AudioPlaybackService.kt — WIU Foreground Service", fontSize = 12.sp, color = Color(0xFFB0B0B0))
                Text("• AudioFocusManager.kt — AudioFocus 封装管理器", fontSize = 12.sp, color = Color(0xFFB0B0B0))
                Text("• AudioMigrationGuide.md — 人工审查清单", fontSize = 12.sp, color = Color(0xFFB0B0B0))
            }
        }
    }
}

// ==================== Tab 3: Compatibility Test ====================

@Composable
private fun CompatibilityTestTab(
    state: AudioMigrationToolState,
    onIntent: (AudioMigrationToolIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score Display
        val scoreColor = when {
            state.testScore >= 80 -> ColorSuccess
            state.testScore >= 60 -> ColorWarning
            else -> ColorCritical
        }

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(80.dp))
                .background(Color(0xFF2D2D2D)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${state.testScore}%",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
                Text(
                    text = if (state.testPassed) "✅ 通过" else "❌ 失败",
                    fontSize = 16.sp,
                    color = if (state.testPassed) ColorSuccess else ColorCritical
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Android 17 音频兼容性测试",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "模拟 Android 17 后台音频拦截行为，验证迁移效果",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Run Test Button
        Button(
            onClick = { onIntent(AudioMigrationToolIntent.RunCompatibilityTest) },
            enabled = !state.isVerifying
        ) {
            if (state.isVerifying) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("测试中...")
            } else {
                Icon(Icons.Default.Science, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("运行测试")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Test Scenarios Info
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2D2D2D)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🧪 测试场景：",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• WIU Service 中的 MediaPlayer.play()", fontSize = 12.sp, color = Color(0xFFB0B0B0))
                Text("• 后台调用 ExoPlayer.play() 应被拦截", fontSize = 12.sp, color = Color(0xFFB0B0B0))
                Text("• AudioFocus 申请与释放", fontSize = 12.sp, color = Color(0xFFB0B0B0))
                Text("• Android 16 vs Android 17 行为对比", fontSize = 12.sp, color = Color(0xFFB0B0B0))
            }
        }
    }
}
