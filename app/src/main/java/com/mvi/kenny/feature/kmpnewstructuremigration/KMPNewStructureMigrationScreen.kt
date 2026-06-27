package com.mvi.kenny.feature.kmpnewstructuremigration

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * ============================================================
 * KMP New Structure Migration — Screen
 * KMP 新默认项目结构迁移工具包 — 界面层
 * ============================================================
 *
 * PRD-299 | AGP 9.0 新 KMP 项目结构迁移工具包
 *
 * 提供交互式界面，帮助用户扫描 KMP 项目结构、预览迁移改动、
 * 执行自动化迁移，并验证迁移后的编译结果。
 */

// ─────────────────────────────────────────────
// Color Palette — 颜色常量
// ─────────────────────────────────────────────

private val ColorSuccess = Color(0xFF4CAF50)
private val ColorWarning = Color(0xFFFF9800)
private val ColorError = Color(0xFFF44336)
private val ColorInfo = Color(0xFF2196F3)
private val ColorBlocker = Color(0xFFD32F2F)

// ─────────────────────────────────────────────
// Main Screen — 主界面
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KMPNewStructureMigrationScreen(
    onNavigateBack: () -> Unit,
    viewModel: KMPNewStructureMigrationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is KMPNewStructureMigrationEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is KMPNewStructureMigrationEffect.NavigateBack -> onNavigateBack()
                is KMPNewStructureMigrationEffect.OpenFile -> { /* TODO */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "KMP AGP9 迁移工具",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.processIntent(KMPNewStructureMigrationIntent.Reset) }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "重置")
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
                .padding(16.dp)
        ) {
            // Phase indicator
            PhaseIndicator(currentPhase = state.phase)

            Spacer(modifier = Modifier.height(16.dp))

            // Main content area with phase-based display
            AnimatedContent(
                targetState = state.phase,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "phase_content"
            ) { phase ->
                when (phase) {
                    MigrationPhase.IDLE -> IdleContent(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    MigrationPhase.SCANNING -> ScanningContent()
                    MigrationPhase.SCAN_COMPLETE -> ScanCompleteContent(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    MigrationPhase.MIGRATING -> MigratingContent(
                        scanResult = state.scanResult
                    )
                    MigrationPhase.VERIFYING -> VerifyingContent()
                    MigrationPhase.COMPLETE -> CompleteContent(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    MigrationPhase.ERROR -> ErrorContent(
                        errorMessage = state.errorMessage ?: "未知错误",
                        onIntent = viewModel::processIntent
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Phase Indicator — 阶段指示器
// ─────────────────────────────────────────────

@Composable
private fun PhaseIndicator(currentPhase: MigrationPhase) {
    val phases = listOf(
        "输入" to setOf(MigrationPhase.IDLE),
        "扫描" to setOf(MigrationPhase.SCANNING),
        "预览" to setOf(MigrationPhase.SCAN_COMPLETE),
        "迁移" to setOf(MigrationPhase.MIGRATING),
        "验证" to setOf(MigrationPhase.VERIFYING),
        "完成" to setOf(MigrationPhase.COMPLETE, MigrationPhase.ERROR)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            phases.forEachIndexed { index, (label, activePhases) ->
                val isActive = currentPhase in activePhases
                val isPast = phases.take(index).any { it.second.contains(currentPhase) }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isActive -> MaterialTheme.colorScheme.primary
                                    isPast -> ColorSuccess
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPast && !isActive) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                color = if (isActive || isPast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (index < phases.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 4.dp)
                            .background(
                                if (isPast) ColorSuccess.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Idle Content — 初始输入界面
// ─────────────────────────────────────────────

@Composable
private fun IdleContent(
    state: KMPNewStructureMigrationState,
    onIntent: (KMPNewStructureMigrationIntent) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "KMP 新默认项目结构迁移工具包",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AGP 9.0 强制要求 Android 应用入口点与共享代码分离到独立模块。"
                            + " 本工具帮助您自动检测项目结构并完成合规迁移。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Project path input
        Text(
            text = "项目路径 / Project Path",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.projectPathInput,
            onValueChange = { onIntent(KMPNewStructureMigrationIntent.UpdateProjectPath(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例如: /Users/xxx/MyKmpProject") },
            leadingIcon = {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    onIntent(KMPNewStructureMigrationIntent.StartScan)
                }
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "提示：输入 KMP 项目的根目录路径，工具将自动分析其结构合规性。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onIntent(KMPNewStructureMigrationIntent.StartScan)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.projectPathInput.isNotBlank()
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("开始扫描 / Start Scan")
        }
    }
}

// ─────────────────────────────────────────────
// Scanning Content — 扫描中界面
// ─────────────────────────────────────────────

@Composable
private fun ScanningContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "正在扫描项目结构...",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Scanning project structure...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "分析 settings.gradle.kts, 模块结构, 入口文件位置...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────
// Scan Complete Content — 扫描完成界面
// ─────────────────────────────────────────────

@Composable
private fun ScanCompleteContent(
    state: KMPNewStructureMigrationState,
    onIntent: (KMPNewStructureMigrationIntent) -> Unit
) {
    val scanResult = state.scanResult ?: return

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Project info card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (scanResult.isCompliant)
                        ColorSuccess.copy(alpha = 0.1f)
                    else
                        ColorWarning.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = scanResult.projectName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = scanResult.projectPath,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${scanResult.complianceScore}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    scanResult.complianceScore >= 80 -> ColorSuccess
                                    scanResult.complianceScore >= 50 -> ColorWarning
                                    else -> ColorError
                                }
                            )
                            Text("合规分", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Compliance bar
                    LinearProgressIndicator(
                        progress = { scanResult.complianceScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            scanResult.complianceScore >= 80 -> ColorSuccess
                            scanResult.complianceScore >= 50 -> ColorWarning
                            else -> ColorError
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (scanResult.isCompliant) "✓ 项目结构符合 AGP 9.0 规范" else "⚠ 项目结构不符合 AGP 9.0 规范",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (scanResult.isCompliant) ColorSuccess else ColorWarning
                    )
                }
            }
        }

        // Structure overview
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "结构检测 / Structure Overview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val structure = scanResult.currentStructure
                    StructureCheckRow("androidApp 模块", structure.appModuleExists)
                    StructureCheckRow("composeApp 模块", structure.composeAppModuleExists)
                    StructureCheckRow("入口点已分离", structure.hasSeparatedEntry)
                    StructureInfoRow("AGP 版本", structure.agpVersion ?: "未检测到")
                    StructureInfoRow("Kotlin 版本", structure.kotlinVersion ?: "未检测到")
                }
            }
        }

        // Issues list
        if (scanResult.issues.isNotEmpty()) {
            item {
                Text(
                    text = "发现问题 (${scanResult.issues.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            items(scanResult.issues) { issue ->
                IssueCard(issue = issue)
            }
        }

        // Migration tasks
        if (scanResult.migrationTasks.isNotEmpty()) {
            item {
                Text(
                    text = "迁移任务 (${scanResult.migrationTasks.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            items(scanResult.migrationTasks) { task ->
                MigrationTaskCard(task = task)
            }
        }

        // Dry-run toggle and action button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = state.isDryRun,
                    onClick = { onIntent(KMPNewStructureMigrationIntent.SetDryRun(!state.isDryRun)) },
                    label = { Text("干跑模式 (Dry-run)") }
                )
            }
        }

        item {
            Button(
                onClick = { onIntent(KMPNewStructureMigrationIntent.StartMigration) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isDryRun) ColorInfo else ColorSuccess
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.isDryRun) "预览迁移 / Preview Migration" else "执行迁移 / Execute Migration")
            }
        }
    }
}

@Composable
private fun StructureCheckRow(label: String, isPresent: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = if (isPresent) "✓" else "✗",
            color = if (isPresent) ColorSuccess else ColorError,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StructureInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun IssueCard(issue: StructureIssue) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = when (issue.severity) {
                    IssueSeverity.BLOCKER -> ColorBlocker
                    IssueSeverity.WARNING -> ColorWarning
                    IssueSeverity.INFO -> ColorInfo
                },
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when (issue.severity) {
                    IssueSeverity.BLOCKER -> Icons.Default.Error
                    IssueSeverity.WARNING -> Icons.Default.Warning
                    IssueSeverity.INFO -> Icons.Default.Info
                },
                contentDescription = null,
                tint = when (issue.severity) {
                    IssueSeverity.BLOCKER -> ColorBlocker
                    IssueSeverity.WARNING -> ColorWarning
                    IssueSeverity.INFO -> ColorInfo
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "[${issue.severity.name}] ${issue.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = issue.descriptionEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "文件: ${issue.filePath}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MigrationTaskCard(task: MigrationTask) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = task.titleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TaskStatusBadge(status = task.status)
            }

            if (task.diffPreview != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = task.diffPreview,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFD4D4D4)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskStatusBadge(status: TaskStatus) {
    val (color, label) = when (status) {
        TaskStatus.PENDING -> ColorInfo to "待执行"
        TaskStatus.IN_PROGRESS -> ColorWarning to "执行中"
        TaskStatus.SUCCESS -> ColorSuccess to "成功"
        TaskStatus.FAILED -> ColorError to "失败"
        TaskStatus.SKIPPED -> MaterialTheme.colorScheme.outline to "跳过"
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────
// Migrating Content — 迁移中界面
// ─────────────────────────────────────────────

@Composable
private fun MigratingContent(scanResult: ScanResult?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "正在执行迁移...",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Executing migration tasks...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (scanResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "当前任务 / Current Task",
                        style = MaterialTheme.typography.labelMedium
                    )
                    scanResult.migrationTasks.forEachIndexed { index, task ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (task.status) {
                                TaskStatus.SUCCESS -> Icon(
                                    Icons.Default.CheckCircle, null,
                                    tint = ColorSuccess, modifier = Modifier.size(16.dp)
                                )
                                TaskStatus.IN_PROGRESS -> CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                                else -> Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            CircleShape
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (task.status == TaskStatus.IN_PROGRESS)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Verifying Content — 验证中界面
// ─────────────────────────────────────────────

@Composable
private fun VerifyingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "正在验证编译...",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Running ./gradlew :androidApp:compileDebugKotlin",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "> ./gradlew :androidApp:compileDebugKotlin",
                    color = Color(0xFFD4D4D4),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Text(
                    text = "Building...",
                    color = ColorWarning,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Complete Content — 完成界面
// ─────────────────────────────────────────────

@Composable
private fun CompleteContent(
    state: KMPNewStructureMigrationState,
    onIntent: (KMPNewStructureMigrationIntent) -> Unit
) {
    val result = state.migrationResult ?: return

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.verificationPassed)
                        ColorSuccess.copy(alpha = 0.1f)
                    else
                        ColorError.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (result.verificationPassed)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.Error,
                        contentDescription = null,
                        tint = if (result.verificationPassed) ColorSuccess else ColorError,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (result.verificationPassed) "迁移成功！" else "迁移完成（有警告）",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (result.verificationPassed) "Migration Successful!" else "Migration Completed with Warnings",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Summary stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard("总任务", result.totalTasks.toString(), ColorInfo)
                StatCard("成功", result.succeeded.toString(), ColorSuccess)
                StatCard("失败", result.failed.toString(), ColorError)
                StatCard("跳过", result.skipped.toString(), MaterialTheme.colorScheme.outline)
            }
        }

        // Compile output
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "编译输出 / Compile Output",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = result.compileOutput,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFD4D4D4)
                        )
                    }
                }
            }
        }

        // Warnings
        if (result.warnings.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ColorWarning.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "警告 / Warnings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ColorWarning
                        )
                        result.warnings.forEach { warning ->
                            Text(
                                text = "• $warning",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onIntent(KMPNewStructureMigrationIntent.Reset) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("新项目")
                }
                Button(
                    onClick = { onIntent(KMPNewStructureMigrationIntent.StartMigration) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("再次执行")
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────
// Error Content — 错误界面
// ─────────────────────────────────────────────

@Composable
private fun ErrorContent(
    errorMessage: String,
    onIntent: (KMPNewStructureMigrationIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            tint = ColorError,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "发生错误",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = ColorError.copy(alpha = 0.1f)
            )
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onIntent(KMPNewStructureMigrationIntent.DismissError) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("重试 / Retry")
        }
    }
}
