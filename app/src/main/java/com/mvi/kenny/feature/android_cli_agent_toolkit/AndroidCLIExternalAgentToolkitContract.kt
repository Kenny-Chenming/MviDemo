package com.mvi.kenny.feature.android_cli_agent_toolkit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class AgentType(val displayName: String, val displayNameCn: String, val logo: String) {
    CLAUDE_CODE("Claude Code", "Claude Code", "🦕"),
    CURSOR("Cursor", "Cursor AI", "Cursor"),
    GEMINI_CLI("Gemini CLI", "Gemini CLI", "✨")
}

data class AgentGuide(
    val id: String,
    val agentType: AgentType,
    val title: String,
    val titleCn: String,
    val subtitle: String,
    val subtitleCn: String,
    val installCommand: String,
    val setupSteps: List<Pair<String, String>>,
    val setupStepsCn: List<Pair<String, String>>,
    val typicalWorkflow: String,
    val typicalWorkflowCn: String,
    val codeExample: String,
    val url: String
)

enum class AgentRole(val roleName: String, val roleNameCn: String, val color: Long) {
    ORCHESTRATOR("Orchestrator", "编排器", 0xFF58A6FF),
    EXECUTOR("Executor", "执行器", 0xFF3FB950),
    REVIEWER("Reviewer", "审核器", 0xFFFFA657)
}

data class CollaborationStep(
    val stepIndex: Int,
    val role: AgentRole,
    val action: String,
    val actionCn: String,
    val cliCommand: String,
    val description: String,
    val descriptionCn: String
)

data class OrchestrationPattern(
    val id: String,
    val patternName: String,
    val patternNameCn: String,
    val description: String,
    val descriptionCn: String,
    val steps: List<CollaborationStep>,
    val useCases: List<String>,
    val useCasesCn: List<String>
)

data class ParsedCommand(
    val id: String,
    val originalCommand: String,
    val commandType: String,
    val commandTypeCn: String,
    val args: Map<String, String>,
    val outputFormat: String,
    val outputFormatCn: String,
    val exampleOutput: String
)

data class TriggerRule(
    val id: String,
    val taskPattern: String,
    val taskPatternCn: String,
    val recommendedSkill: String,
    val recommendedSkillCn: String,
    val autoInstallCommand: String,
    val description: String,
    val descriptionCn: String
)

data class SwiftExportGuide(
    val id: String,
    val kotlinVersion: String,
    val supportedPlatforms: List<String>,
    val kmpConfigSteps: List<Pair<String, String>>,
    val swiftExportCommand: String,
    val codeExample: String
)

data class QuailFeature(
    val id: String,
    val featureName: String,
    val featureNameCn: String,
    val description: String,
    val descriptionCn: String,
    val installCommand: String,
    val usageSteps: List<String>,
    val usageStepsCn: List<String>,
    val codeExample: String,
    val url: String
)

data class KbApiMethod(
    val id: String,
    val methodName: String,
    val methodNameCn: String,
    val endpoint: String,
    val description: String,
    val descriptionCn: String,
    val parameters: List<String>,
    val parametersCn: List<String>,
    val responseFormat: String,
    val codeExample: String
)

data class OrchestrationFramework(
    val id: String,
    val frameworkName: String,
    val frameworkNameCn: String,
    val description: String,
    val descriptionCn: String,
    val components: List<String>,
    val componentsCn: List<String>,
    val configExample: String,
    val url: String
)

data class AndroidCLIExternalAgentToolkitState(
    val selectedTab: Int = 0,
    val agentGuides: List<AgentGuide> = emptyList(),
    val expandedAgentGuideId: String? = null,
    val collaborationSteps: List<CollaborationStep> = emptyList(),
    val orchestrationPatterns: List<OrchestrationPattern> = emptyList(),
    val expandedPatternId: String? = null,
    val parsedCommands: List<ParsedCommand> = emptyList(),
    val triggerRules: List<TriggerRule> = emptyList(),
    val expandedCommandId: String? = null,
    val expandedTriggerId: String? = null,
    val swiftExportGuides: List<SwiftExportGuide> = emptyList(),
    val quailFeatures: List<QuailFeature> = emptyList(),
    val expandedSwiftExportId: String? = null,
    val expandedQuailFeatureId: String? = null,
    val kbApiMethods: List<KbApiMethod> = emptyList(),
    val orchestrationFrameworks: List<OrchestrationFramework> = emptyList(),
    val expandedKbApiId: String? = null,
    val expandedFrameworkId: String? = null,
    val copiedItemId: String? = null
)

sealed class AndroidCLIExternalAgentToolkitIntent {
    data class SelectTab(val index: Int) : AndroidCLIExternalAgentToolkitIntent()
    data class ToggleAgentGuideExpanded(val guideId: String) : AndroidCLIExternalAgentToolkitIntent()
    data class TogglePatternExpanded(val patternId: String) : AndroidCLIExternalAgentToolkitIntent()
    data class ToggleCommandExpanded(val commandId: String) : AndroidCLIExternalAgentToolkitIntent()
    data class ToggleTriggerExpanded(val triggerId: String) : AndroidCLIExternalAgentToolkitIntent()
    data class ToggleSwiftExportExpanded(val guideId: String) : AndroidCLIExternalAgentToolkitIntent()
    data class ToggleQuailFeatureExpanded(val featureId: String) : AndroidCLIExternalAgentToolkitIntent()
    data class ToggleKbApiExpanded(val apiId: String) : AndroidCLIExternalAgentToolkitIntent()
    data class ToggleFrameworkExpanded(val frameworkId: String) : AndroidCLIExternalAgentToolkitIntent()
    data class CopyCommand(val command: String, val itemId: String) : AndroidCLIExternalAgentToolkitIntent()
    data class OpenUrl(val url: String) : AndroidCLIExternalAgentToolkitIntent()
}

sealed class AndroidCLIExternalAgentToolkitEffect {
    data class ShowSnackbar(val message: String) : AndroidCLIExternalAgentToolkitEffect()
    data class CopyToClipboard(val text: String) : AndroidCLIExternalAgentToolkitEffect()
    data class OpenUrl(val url: String) : AndroidCLIExternalAgentToolkitEffect()
}
