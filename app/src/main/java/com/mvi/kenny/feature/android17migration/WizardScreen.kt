package com.mvi.kenny.feature.android17migration

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.R

/**
 * ============================================================
 * WizardScreen — 修复向导页
 * ============================================================
 * Step-by-step wizard for fixing migration issues.
 * 分步引导修复迁移问题的向导页面。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardScreen(
    issueId: String,
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit,
    viewModel: MigrationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val issue = state.issues.find { it.id == issueId }
    val currentStep = state.currentWizardStep

    LaunchedEffect(issueId) {
        if (issue == null) {
            onNavigateBack()
        }
    }

    if (issue == null) return

    val totalSteps = issue.wizardSteps

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Fix Wizard / 修复向导",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back / 返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Step Indicator
            WizardStepIndicator(
                currentStep = currentStep,
                totalSteps = totalSteps,
                stepTitles = when (issue.severity) {
                    Severity.CRITICAL -> listOf("Review Issue / 审视问题", "Apply Fix / 应用修复", "Verify / 验证结果")
                    Severity.WARNING -> listOf("Review / 审视", "Apply / 应用")
                    Severity.INFO -> listOf("Confirm / 确认")
                }
            )

            // Progress Bar
            LinearProgressIndicator(
                progress = { (currentStep + 1).toFloat() / totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                strokeCap = StrokeCap.Round
            )

            // Wizard Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "wizard_content"
            ) { step ->
                WizardContent(
                    step = step,
                    issue = issue,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            // Bottom Navigation
            WizardNavigationBar(
                currentStep = currentStep,
                totalSteps = totalSteps,
                onPrevClick = {
                    viewModel.sendIntent(MigrationIntent.PrevWizardStep(currentStep))
                },
                onNextClick = {
                    if (currentStep < totalSteps - 1) {
                        viewModel.sendIntent(MigrationIntent.NextWizardStep(currentStep))
                    }
                },
                onCompleteClick = {
                    viewModel.sendIntent(MigrationIntent.CompleteWizard(issue.id))
                    onComplete()
                }
            )
        }
    }
}

@Composable
private fun WizardStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    stepTitles: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepTitles.forEachIndexed { index, title ->
                val isCompleted = index < currentStep
                val isCurrent = index == currentStep

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = when {
                                    isCompleted -> Color(0xFF43A047)
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary
                                else if (isCompleted) Color(0xFF43A047)
                                else MaterialTheme.colorScheme.outline,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                }

                if (index < totalSteps - 1) {
                    Box(
                        modifier = Modifier
                            .weight(0.5f)
                            .height(2.dp)
                            .background(
                                if (index < currentStep) Color(0xFF43A047)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun WizardContent(
    step: Int,
    issue: MigrationIssue,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (step) {
            0 -> Step0ReviewIssue(issue)
            1 -> Step1ApplyFix(issue)
            2 -> Step2Verify(issue)
        }
    }
}

@Composable
private fun Step0ReviewIssue(issue: MigrationIssue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = issue.severity.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = issue.severity.color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = issue.titleZh,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = issue.severity.color
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = issue.descriptionZh,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip(label = "${issue.affectedFiles} files")
                InfoChip(label = "${issue.affectedMethods} methods")
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Affected Files / 受影响文件:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            issue.filePaths.forEach { path ->
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Important / 重要提示",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "This change affects ${issue.affectedFiles} file(s). Please review before applying. / 此变更影响 ${issue.affectedFiles} 个文件，请在应用修复前仔细审查。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun Step1ApplyFix(issue: MigrationIssue) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Code Changes / 代码变更",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CodeBlock(
                    title = "Before / 修复前",
                    code = "// Old deprecated code\nval pendingIntent = PendingIntent.getActivity(\n    context,\n    REQUEST_CODE,\n    intent,\n    0 // Missing mutability flag\n)",
                    isOld = true,
                    modifier = Modifier.weight(1f)
                )

                CodeBlock(
                    title = "After / 修复后",
                    code = issue.fixSuggestion,
                    isOld = false,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Files to Modify / 将被修改的文件:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            issue.filePaths.forEach { path ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = path,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ready to Apply / 准备应用",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Click 'Next' to preview or 'Complete' to apply fix directly. / 点击[下一步]预览变更，或点击[完成]直接应用修复。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun Step2Verify(issue: MigrationIssue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF43A047).copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF43A047), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Fix Applied! / 修复已应用！",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF43A047)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The issue has been fixed in ${issue.affectedFiles} file(s). / 问题已在 ${issue.affectedFiles} 个文件中修复。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Verification Checklist / 验证清单:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            VerificationItem(text = "Code syntax verified / 代码语法已验证", completed = true)
            VerificationItem(text = "Build passes / 编译通过", completed = true)
            VerificationItem(text = "Runtime behavior tested / 运行时行为已测试", completed = false)
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Next Steps / 后续步骤:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1. Run './gradlew assembleDebug' to verify build / 运行编译验证\n2. Test on Android 17 device / 在 Android 17 设备上测试\n3. Run full migration scan again / 重新运行完整迁移扫描",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CodeBlock(
    title: String,
    code: String,
    isOld: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isOld) MaterialTheme.colorScheme.error else Color(0xFF43A047)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D2D2D), RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    if (isOld) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    else Color(0xFF43A047).copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFD4D4D4),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun VerificationItem(text: String, completed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    if (completed) Color(0xFF43A047) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (completed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun InfoChip(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WizardNavigationBar(
    currentStep: Int,
    totalSteps: Int,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onPrevClick,
            enabled = currentStep > 0,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Previous")
        }

        if (currentStep < totalSteps - 1) {
            Button(
                onClick = onNextClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Next")
            }
        } else {
            Button(
                onClick = onCompleteClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Complete")
            }
        }
    }
}
