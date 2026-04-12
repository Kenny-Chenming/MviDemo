package com.mvi.kenny.feature.bubbles.intro

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.bubbles.BubblesColors
import com.mvi.kenny.feature.bubbles.BubblesGradleWizardState
import com.mvi.kenny.feature.bubbles.BubblesIntent
import com.mvi.kenny.feature.bubbles.WizardStep

/**
 * ============================================================
 * BubblesGradleWizardScreen — Gradle 插件配置向导
 * ============================================================
 * 5 步向导，帮助开发者快速接入 App Bubbles。
 *
 * 步骤流程：
 * Step 1: 选择目标 Module
 * Step 2: 选择要浮动化的 Activity
 * Step 3: 配置 BubbleMetadata
 * Step 4: Manifest 声明预览
 * Step 5: 完成
 *
 * @param state Current wizard state / 当前向导状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
fun BubblesGradleWizardScreen(
    state: BubblesGradleWizardState,
    onIntent: (BubblesIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BubblesColors.Background)
    ) {
        // =============================================================
        // Step Progress Indicator / 步骤进度指示器
        // =============================================================
        WizardProgressBar(
            currentStep = state.currentStep,
            onStepClick = { step ->
                // Allow navigating to previous steps only / 只能返回上一步
                if (step.stepNumber < state.currentStep.stepNumber) {
                    when (step) {
                        WizardStep.MODULE_SELECT -> onIntent(BubblesIntent.PrevWizardStep(state.currentStep))
                        WizardStep.ACTIVITY_SELECT -> onIntent(BubblesIntent.PrevWizardStep(state.currentStep))
                        WizardStep.METADATA_CONFIG -> onIntent(BubblesIntent.PrevWizardStep(state.currentStep))
                        WizardStep.MANIFEST_PREVIEW -> onIntent(BubblesIntent.PrevWizardStep(state.currentStep))
                        else -> {}
                    }
                }
            }
        )

        // =============================================================
        // Step Content / 步骤内容
        // =============================================================
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                if (targetState.stepNumber > initialState.stepNumber) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "wizard_content"
        ) { step ->
            when (step) {
                WizardStep.MODULE_SELECT -> ModuleSelectStep(
                    availableModules = state.availableModules,
                    selectedModule = state.selectedModule,
                    onSelectModule = { onIntent(BubblesIntent.SelectModule(it)) },
                    onNext = { onIntent(BubblesIntent.NextWizardStep(step)) }
                )
                WizardStep.ACTIVITY_SELECT -> ActivitySelectStep(
                    availableActivities = state.availableActivities,
                    selectedActivities = state.selectedActivities,
                    selectedModule = state.selectedModule,
                    onToggleActivity = { onIntent(BubblesIntent.ToggleActivity(it)) },
                    onNext = { onIntent(BubblesIntent.NextWizardStep(step)) },
                    onBack = { onIntent(BubblesIntent.PrevWizardStep(step)) }
                )
                WizardStep.METADATA_CONFIG -> MetadataConfigStep(
                    metadata = state.bubbleMetadata,
                    selectedActivities = state.selectedActivities,
                    onUpdateMetadata = { onIntent(BubblesIntent.UpdateMetadata(it)) },
                    onNext = { onIntent(BubblesIntent.NextWizardStep(step)) },
                    onBack = { onIntent(BubblesIntent.PrevWizardStep(step)) }
                )
                WizardStep.MANIFEST_PREVIEW -> ManifestPreviewStep(
                    manifestXml = state.generatedManifestXml,
                    isApplying = state.isApplying,
                    onApply = { onIntent(BubblesIntent.ApplyGradleConfig(state)) },
                    onBack = { onIntent(BubblesIntent.PrevWizardStep(step)) }
                )
                WizardStep.DONE -> DoneStep(
                    result = state.applyResult,
                    onReset = { onIntent(BubblesIntent.ResetWizard) }
                )
            }
        }
    }
}

// =============================================================
// WizardProgressBar — 步骤进度条
// =============================================================
/**
 * Horizontal step progress bar.
 * 水平步骤进度条。
 *
 * @param currentStep Current wizard step / 当前向导步骤
 * @param onStepClick Callback when step is clicked / 步骤点击回调
 */
@Composable
private fun WizardProgressBar(
    currentStep: WizardStep,
    onStepClick: (WizardStep) -> Unit
) {
    val steps = WizardStep.entries.filter { it != WizardStep.DONE }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BubblesColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Progress bar / 进度条
            val progress = (currentStep.stepNumber - 1).toFloat() / (steps.size - 1)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = BubblesColors.BubblesActive,
                trackColor = Color.LightGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step indicators / 步骤指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEach { step ->
                    val isCompleted = step.stepNumber < currentStep.stepNumber
                    val isCurrent = step == currentStep

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = step.stepNumber < currentStep.stepNumber) {
                                onStepClick(step)
                            }
                    ) {
                        // Step circle / 步骤圆圈
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    when {
                                        isCompleted -> BubblesColors.BubblesActive
                                        isCurrent -> BubblesColors.Primary
                                        else -> Color.LightGray
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "${step.stepNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isCurrent) Color.White else Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Step label / 步骤标签
                        Text(
                            text = step.titleZh,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrent) BubblesColors.Primary else Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// ModuleSelectStep — Step 1: 选择 Module
// =============================================================
/**
 * Module selection step.
 * Module 选择步骤。
 */
@Composable
private fun ModuleSelectStep(
    availableModules: List<String>,
    selectedModule: String,
    onSelectModule: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        StepHeader(
            stepNumber = 1,
            title = "Select Target Module",
            titleZh = "选择目标 Module",
            description = "Choose the module where you want to enable Bubbles"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Module list / Module 列表
        availableModules.forEach { module ->
            ModuleCard(
                moduleName = module,
                isSelected = module == selectedModule,
                onClick = { onSelectModule(module) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next button / 下一步按钮
        Button(
            onClick = onNext,
            enabled = selectedModule.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BubblesColors.Primary)
        ) {
            Text("Next / 下一步")
        }
    }
}

/**
 * Module selection card.
 * Module 选择卡片。
 */
@Composable
private fun ModuleCard(
    moduleName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BubblesColors.Primary.copy(alpha = 0.1f)
                            else BubblesColors.Surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, BubblesColors.Primary)
                 else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = null,
                tint = if (isSelected) BubblesColors.Primary else Color.Gray
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = moduleName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ":app" + if (moduleName == "app") " (Main module)" else " (Library)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = BubblesColors.Primary
                )
            }
        }
    }
}

// =============================================================
// ActivitySelectStep — Step 2: 选择 Activity
// =============================================================
/**
 * Activity selection step.
 * Activity 选择步骤。
 */
@Composable
private fun ActivitySelectStep(
    availableActivities: List<String>,
    selectedActivities: List<String>,
    selectedModule: String,
    onToggleActivity: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        StepHeader(
            stepNumber = 2,
            title = "Select Activities to Bubbleify",
            titleZh = "选择要浮动化的 Activity",
            description = "Choose which activities can be displayed as bubbles in module: $selectedModule"
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableActivities) { activity ->
                ActivityCard(
                    activityName = activity,
                    isSelected = selectedActivities.contains(activity),
                    onClick = { onToggleActivity(activity) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation buttons / 导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }
            Button(
                onClick = onNext,
                enabled = selectedActivities.isNotEmpty(),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = BubblesColors.Primary)
            ) {
                Text("Next / 下一步")
            }
        }
    }
}

/**
 * Activity selection card.
 * Activity 选择卡片。
 */
@Composable
private fun ActivityCard(
    activityName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BubblesColors.Primary.copy(alpha = 0.1f)
                            else BubblesColors.Surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, BubblesColors.Primary)
                 else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isSelected) BubblesColors.Primary else Color.LightGray.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activityName.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) Color.White else Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = activityName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = BubblesColors.Primary
                )
            }
        }
    }
}

// =============================================================
// MetadataConfigStep — Step 3: 配置 Metadata
// =============================================================
/**
 * Metadata configuration step.
 * 元数据配置步骤。
 */
@Composable
private fun MetadataConfigStep(
    metadata: com.mvi.kenny.feature.bubbles.BubbleMetadataConfig,
    selectedActivities: List<String>,
    onUpdateMetadata: (com.mvi.kenny.feature.bubbles.BubbleMetadataConfig) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        StepHeader(
            stepNumber = 3,
            title = "Configure BubbleMetadata",
            titleZh = "配置 Bubble 元数据",
            description = "Set up the bubble appearance and behavior for selected activities"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Icon resource / 图标资源
        OutlinedTextField(
            value = metadata.iconResId,
            onValueChange = { onUpdateMetadata(metadata.copy(iconResId = it)) },
            label = { Text("Icon Resource ID / 图标资源 ID") },
            placeholder = { Text("e.g., @drawable/ic_bubble") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Title / 标题
        OutlinedTextField(
            value = metadata.title,
            onValueChange = { onUpdateMetadata(metadata.copy(title = it)) },
            label = { Text("Bubble Title / 浮动窗口标题") },
            placeholder = { Text("e.g., My Music Player") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Shortcut ID / 快捷方式 ID
        OutlinedTextField(
            value = metadata.shortcutId,
            onValueChange = { onUpdateMetadata(metadata.copy(shortcutId = it)) },
            label = { Text("Shortcut ID / 快捷方式 ID") },
            placeholder = { Text("e.g., shortcut_music") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Badge icon / 徽章图标
        OutlinedTextField(
            value = metadata.badgeIconResId,
            onValueChange = { onUpdateMetadata(metadata.copy(badgeIconResId = it)) },
            label = { Text("Badge Icon Resource ID / 徽章图标资源 ID") },
            placeholder = { Text("e.g., @drawable/ic_notification") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selected activities summary / 已选 Activity 摘要
        Card(
            colors = CardDefaults.cardColors(
                containerColor = BubblesColors.Background
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Selected Activities / 已选 Activity",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                selectedActivities.forEach { activity ->
                    Text(
                        text = "• $activity",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(16.dp))

        // Navigation buttons / 导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = BubblesColors.Primary)
            ) {
                Text("Preview Manifest / 预览 Manifest")
            }
        }
    }
}

// =============================================================
// ManifestPreviewStep — Step 4: Manifest 预览
// =============================================================
/**
 * Manifest preview step.
 * Manifest 预览步骤。
 */
@Composable
private fun ManifestPreviewStep(
    manifestXml: String,
    isApplying: Boolean,
    onApply: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        StepHeader(
            stepNumber = 4,
            title = "Preview Manifest",
            titleZh = "预览 Manifest",
            description = "Review and apply the generated AndroidManifest.xml changes"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Code preview / 代码预览
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AndroidManifest.xml",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = manifestXml,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color(0xFF9CDCFE),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Apply result / 应用结果
        AnimatedVisibility(visible = isApplying) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Applying changes... / 应用变更中...",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation buttons / 导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBack,
                enabled = !isApplying,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }
            Button(
                onClick = onApply,
                enabled = !isApplying,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BubblesColors.BubblesActive
                )
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply / 应用")
                }
            }
        }
    }
}

// =============================================================
// DoneStep — Step 5: 完成
// =============================================================
/**
 * Wizard completion step.
 * 向导完成步骤。
 */
@Composable
private fun DoneStep(
    result: com.mvi.kenny.feature.bubbles.ApplyResult?,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success icon / 成功图标
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    BubblesColors.BubblesActive.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = BubblesColors.BubblesActive,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Congratulations! / 恭喜！",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "App Bubbles has been configured successfully",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "浮动窗口配置成功！",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Result details / 结果详情
        if (result != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = BubblesColors.Background
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = result.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (result.manifestPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Modified: ${result.manifestPath}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = BubblesColors.Primary)
        ) {
            Text("Start Over / 重新开始")
        }
    }
}

// =============================================================
// StepHeader — 步骤头部
// =============================================================
/**
 * Reusable step header component.
 * 可复用的步骤头部组件。
 *
 * @param stepNumber Step number / 步骤编号
 * @param title Step title in English / 步骤标题（英文）
 * @param titleZh Step title in Chinese / 步骤标题（中文）
 * @param description Step description / 步骤描述
 */
@Composable
private fun StepHeader(
    stepNumber: Int,
    title: String,
    titleZh: String,
    description: String
) {
    Column {
        Text(
            text = "Step $stepNumber",
            style = MaterialTheme.typography.labelMedium,
            color = BubblesColors.Primary
        )
        Text(
            text = "$title / $titleZh",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
