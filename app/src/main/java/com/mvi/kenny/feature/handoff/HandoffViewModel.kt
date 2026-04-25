package com.mvi.kenny.feature.handoff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// =============================================================
// HandoffViewModel — Android 17 Handoff API 跨设备连续性开发工具包
// ViewModel / 视图模型
// =============================================================
// PRD-153 | Android 17 Handoff API Cross-Device Continuity Dev Toolkit
//
// Responsibilities / 职责:
// - Manage HandoffState as single source of truth
// - Process HandoffIntent user intentions
// - Emit one-time HandoffEffect side effects
//
// Architecture / 架构:
// - MVI (Model-View-Intent) pattern
// - State: mutableStateOf ( Compose-friendly )
// - Effect: Channel for one-time events ( navigation, toast, clipboard, etc.)
// - ViewModelScope: viewModelScope.launch for coroutine-based async operations
//
// @see HandoffContract MVI contract definitions
// @see HandoffMainScreen Main screen with tab navigation

class HandoffViewModel : ViewModel() {

    // ============================================================
    // State — single source of truth for UI
    // ============================================================
    private val _state = MutableStateFlow(HandoffState())
    val state: StateFlow<HandoffState> = _state.asStateFlow()

    // ============================================================
    // Effect — one-time side effects via Channel
    // ============================================================
    private val _effect = Channel<HandoffEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // sendIntent — process user intentions
    // ============================================================
    /**
     * Process user intention / 处理用户意图
     *
     * Called from the View layer (Composable) when user performs an action.
     * Updates state accordingly and emits side effects if needed.
     *
     * @param intent User intention defined in HandoffIntent / 用户意图
     */
    fun sendIntent(intent: HandoffIntent) {
        viewModelScope.launch {
            when (intent) {
                is HandoffIntent.SelectTool -> handleSelectTool(intent.tool)
                is HandoffIntent.AddDataField -> handleAddDataField(intent.field)
                is HandoffIntent.RemoveDataField -> handleRemoveDataField(intent.index)
                is HandoffIntent.UpdateDataField -> handleUpdateDataField(intent.index, intent.field)
                is HandoffIntent.GenerateCode -> handleGenerateCode()
                is HandoffIntent.UpdateIntentPackage -> handleUpdateIntentPackage(intent.packageName)
                is HandoffIntent.UpdateIntentClass -> handleUpdateIntentClass(intent.className)
                is HandoffIntent.AddActivityInput -> handleAddActivityInput(intent.input)
                is HandoffIntent.RemoveActivityInput -> handleRemoveActivityInput(intent.id)
                is HandoffIntent.UpdateActivityInput -> handleUpdateActivityInput(intent.input)
                is HandoffIntent.AnalyzeActivities -> handleAnalyzeActivities()
                is HandoffIntent.AddDataType -> handleAddDataType(intent.dataType)
                is HandoffIntent.RemoveDataType -> handleRemoveDataType(intent.dataType)
                is HandoffIntent.GeneratePrivacyReport -> handleGeneratePrivacyReport(intent.dataTypes)
                is HandoffIntent.SelectSourceDevice -> handleSelectSourceDevice(intent.device)
                is HandoffIntent.SelectTargetDevice -> handleSelectTargetDevice(intent.device)
                is HandoffIntent.StartDebugSession -> handleStartDebugSession()
                is HandoffIntent.SelectDebugSession -> handleSelectDebugSession(intent.session)
                is HandoffIntent.UpdateSyncConfig -> handleUpdateSyncConfig(intent.config)
                is HandoffIntent.UpdateAppBundleConfig -> handleUpdateAppBundleConfig(intent.config)
                is HandoffIntent.SelectFallback -> handleSelectFallback(intent.type)
                is HandoffIntent.ClearError -> _state.value = _state.value.copy(error = null)
                is HandoffIntent.DismissLoading -> _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    // ============================================================
    // Tool Navigation
    // ============================================================
    private fun handleSelectTool(tool: HandoffTool) {
        _state.value = _state.value.copy(currentTool = tool)
    }

    // ============================================================
    // INTEGRATE_TEMPLATE handlers
    // ============================================================
    private fun handleAddDataField(field: DataField) {
        val currentFields = _state.value.activityDataFields.toMutableList()
        currentFields.add(field)
        _state.value = _state.value.copy(activityDataFields = currentFields)
    }

    private fun handleRemoveDataField(index: Int) {
        val currentFields = _state.value.activityDataFields.toMutableList()
        if (index in currentFields.indices) {
            currentFields.removeAt(index)
            _state.value = _state.value.copy(activityDataFields = currentFields)
        }
    }

    private fun handleUpdateDataField(index: Int, field: DataField) {
        val currentFields = _state.value.activityDataFields.toMutableList()
        if (index in currentFields.indices) {
            currentFields[index] = field
            _state.value = _state.value.copy(activityDataFields = currentFields)
        }
    }

    private suspend fun handleGenerateCode() {
        _state.value = _state.value.copy(isLoading = true)
        try {
            val fields = _state.value.activityDataFields
            val kotlinCode = generateHandoffKotlinCode(fields)
            val javaCode = generateHandoffJavaCode(fields)
            val templates = listOf(
                HandoffCodeTemplate(CodeLanguage.KOTLIN, kotlinCode, "Kotlin integration template"),
                HandoffCodeTemplate(CodeLanguage.JAVA, javaCode, "Java integration template")
            )
            _state.value = _state.value.copy(
                generatedCodeTemplates = templates,
                isLoading = false
            )
            _effect.send(HandoffEffect.ShowToast("代码已生成 / Code generated"))
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }

    /**
     * Generate Kotlin Handoff integration code / 生成 Kotlin Handoff 集成代码
     */
    private fun generateHandoffKotlinCode(fields: List<DataField>): String {
        val fieldLines = fields.joinToString("\n") { field ->
            val encrypted = if (field.isEncrypted) ",\n        encrypted = true" else ""
            "    \"${field.name}\" to ${field.type}::class.java$encrypted"
        }
        return """
package com.example.myapp.handoff

import android.app.Activity
import android.content.Intent
import android.os.HandoffActivityData
import androidx.activity.ComponentActivity

/**
 * Handoff integration template — Kotlin / Handoff 集成模板 — Kotlin
 * Generated by Handoff API Dev Toolkit / 由 Handoff API 开发工具包生成
 *
 * Implements onHandoffActivityRequested() for cross-device continuity.
 * 实现 onHandoffActivityRequested() 以支持跨设备连续性。
 */
class HandoffIntegrationHelper(private val activity: ComponentActivity) {

    /**
     * Called when system requests Handoff of this Activity.
     * 当系统请求此 Activity 的 Handoff 时调用。
     *
     * @return HandoffActivityData containing serialized Activity state
     *         包含序列化 Activity 状态的 HandoffActivityData
     */
    fun onHandoffActivityRequested(): HandoffActivityData {
        val data = HandoffActivityData.Builder(
            activity.intent,
            activity::class.java
        )

        // Register data fields to serialize / 注册需要序列化的数据字段
${fields.joinToString("\n") { field ->
            val comment = when {
                field.name.contains("user", ignoreCase = true) -> " // 用户数据 / User data"
                field.name.contains("token", ignoreCase = true) -> " // 认证令牌 / Auth token"
                field.name.contains("password", ignoreCase = true) -> " // ⚠️ 敏感数据，请加密 / Sensitive data, encrypt!"
                else -> ""
            }
            "        data.addField(\"${field.name}\", ${field.type}::class.java)$comment"
        }}

        return data.build()
    }

    /**
     * Receive Handoff from source device.
     * 从源设备接收 Handoff。
     *
     * @param intent Intent containing HandoffActivityData
     * @param intent 包含 HandoffActivityData 的 Intent
     */
    fun receiveHandoff(intent: Intent) {
        val handoffData = HandoffActivityData.fromIntent(intent)
        handoffData?.let { data ->
${fields.joinToString("\n") { field ->
            "            // Restore ${field.name} / 恢复 ${field.name}"
        }}
            // Navigate to target Activity / 导航到目标 Activity
            val targetIntent = Intent(activity, activity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            activity.startActivity(targetIntent)
        }
    }
}
""".trimIndent()
    }

    /**
     * Generate Java Handoff integration code / 生成 Java Handoff 集成代码
     */
    private fun generateHandoffJavaCode(fields: List<DataField>): String {
        return """
package com.example.myapp.handoff;

import android.app.Activity;
import android.content.Intent;
import android.os.HandoffActivityData;
import androidx.activity.ComponentActivity;

/**
 * Handoff integration template — Java / Handoff 集成模板 — Java
 * Generated by Handoff API Dev Toolkit / 由 Handoff API 开发工具包生成
 */
public class HandoffIntegrationHelper {

    private final ComponentActivity activity;

    public HandoffIntegrationHelper(ComponentActivity activity) {
        this.activity = activity;
    }

    /**
     * Called when system requests Handoff of this Activity.
     */
    public HandoffActivityData onHandoffActivityRequested() {
        HandoffActivityData.Builder builder = new HandoffActivityData.Builder(
            activity.getIntent(),
            activity.getClass()
        );

        // Register data fields to serialize
${fields.joinToString("\n") { field ->
            "        builder.addField(\"${field.name}\", ${field.type}.class);"
        }}

        return builder.build();
    }

    /**
     * Receive Handoff from source device.
     */
    public void receiveHandoff(Intent intent) {
        HandoffActivityData handoffData = HandoffActivityData.fromIntent(intent);
        if (handoffData != null) {
            // Restore state and navigate
            Intent targetIntent = new Intent(activity, activity.getClass());
            targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(targetIntent);
        }
    }
}
""".trimIndent()
    }

    // ============================================================
    // INTENT_BUILDER handlers
    // ============================================================
    private fun handleUpdateIntentPackage(packageName: String) {
        _state.value = _state.value.copy(intentPackageName = packageName)
    }

    private fun handleUpdateIntentClass(className: String) {
        _state.value = _state.value.copy(intentClassName = className)
    }

    // ============================================================
    // DECISION_ENGINE handlers
    // ============================================================
    private fun handleAddActivityInput(input: ActivityInput) {
        val currentInputs = _state.value.activityInputs.toMutableList()
        currentInputs.add(input)
        _state.value = _state.value.copy(activityInputs = currentInputs)
    }

    private fun handleRemoveActivityInput(id: String) {
        val currentInputs = _state.value.activityInputs.filter { it.id != id }
        _state.value = _state.value.copy(activityInputs = currentInputs)
    }

    private fun handleUpdateActivityInput(input: ActivityInput) {
        val currentInputs = _state.value.activityInputs.toMutableList()
        val index = currentInputs.indexOfFirst { it.id == input.id }
        if (index >= 0) {
            currentInputs[index] = input
            _state.value = _state.value.copy(activityInputs = currentInputs)
        }
    }

    private suspend fun handleAnalyzeActivities() {
        _state.value = _state.value.copy(isLoading = true)
        try {
            val results = _state.value.activityInputs.map { input ->
                analyzeActivitySuitability(input)
            }
            _state.value = _state.value.copy(
                decisionResults = results,
                isLoading = false
            )
            _effect.send(HandoffEffect.DecisionAnalysisCompleted(results))
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }

    /**
     * Analyze whether an Activity is suitable for Handoff.
     * 分析 Activity 是否适合 Handoff。
     *
     * Decision logic:
     * - NOT suitable: contains sensitive data (HIGH/CRITICAL) without encryption
     * - NOT suitable: requires real-time network data for critical operations
     * - Suitable: normal data, no extreme sensitivity
     */
    private fun analyzeActivitySuitability(input: ActivityInput): DecisionResult {
        val reasons = mutableListOf<String>()
        val risks = mutableListOf<String>()
        var suitable = true
        var confidence = 0.8f

        // Rule 1: Data sensitivity check / 数据敏感度检查
        when (input.dataSensitivity) {
            DataSensitivity.HIGH, DataSensitivity.CRITICAL -> {
                suitable = false
                risks.add("高敏感数据不建议跨设备传输 / High-sensitivity data not recommended for cross-device transfer")
                confidence -= 0.3f
            }
            DataSensitivity.MEDIUM -> {
                reasons.add("中等敏感数据可传输，建议加密 / Medium-sensitivity data transferable, encryption recommended")
                confidence -= 0.1f
            }
            DataSensitivity.LOW -> {
                reasons.add("低敏感数据适合 Handoff / Low-sensitivity data suitable for Handoff")
            }
        }

        // Rule 2: User data check / 用户数据检查
        if (input.hasUserData && input.dataSensitivity != DataSensitivity.LOW) {
            risks.add("包含用户数据，需要额外隐私合规检查 / Contains user data, requires additional privacy compliance")
            confidence -= 0.15f
        }

        // Rule 3: Network dependency check / 网络依赖检查
        if (input.hasNetworkData) {
            reasons.add("依赖网络数据的 Activity 在离线场景下 Handoff 可能失败 / Network-dependent Activities may fail Handoff offline")
            confidence -= 0.1f
        }

        confidence = confidence.coerceIn(0.1f, 1.0f)
        return DecisionResult(
            activityId = input.id,
            activityName = input.name.ifEmpty { "Unknown Activity" },
            suitable = suitable,
            confidence = confidence,
            reasons = reasons,
            risks = risks
        )
    }

    // ============================================================
    // PRIVACY_COMPLIANCE handlers
    // ============================================================
    private fun handleAddDataType(dataType: String) {
        val currentTypes = _state.value.dataTypesInput.toMutableList()
        if (dataType !in currentTypes) {
            currentTypes.add(dataType)
            _state.value = _state.value.copy(dataTypesInput = currentTypes)
        }
    }

    private fun handleRemoveDataType(dataType: String) {
        val currentTypes = _state.value.dataTypesInput.filter { it != dataType }
        _state.value = _state.value.copy(dataTypesInput = currentTypes)
    }

    private suspend fun handleGeneratePrivacyReport(dataTypes: List<String>) {
        _state.value = _state.value.copy(isLoading = true)
        try {
            val report = generatePrivacyReport(dataTypes)
            _state.value = _state.value.copy(privacyReport = report, isLoading = false)
            _effect.send(HandoffEffect.PrivacyReportGenerated(report))
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }

    /**
     * Generate privacy compliance report for given data types.
     * 为给定的数据类型生成隐私合规报告。
     */
    private fun generatePrivacyReport(dataTypes: List<String>): PrivacyReport {
        val violations = mutableListOf<PrivacyViolation>()
        val suggestions = mutableListOf<String>()
        var gdprCompliant = true
        var ccpCompliant = true

        dataTypes.forEach { dataType ->
            when {
                dataType.contains("location", ignoreCase = true) ||
                dataType.contains("gps", ignoreCase = true) -> {
                    violations.add(PrivacyViolation(
                        regulation = "GDPR Art. 9 / CCPA",
                        description = "位置数据传输需额外加密和用户同意 / Location data requires additional encryption and user consent",
                        severity = DataSensitivity.HIGH
                    ))
                    gdprCompliant = false
                    suggestions.add("对位置数据使用端到端加密 / Use end-to-end encryption for location data")
                }
                dataType.contains("health", ignoreCase = true) ||
                dataType.contains("medical", ignoreCase = true) -> {
                    violations.add(PrivacyViolation(
                        regulation = "GDPR Art. 9 / HIPAA",
                        description = "健康数据属于特殊类别数据，需明确法律依据 / Health data is special category, requires explicit legal basis",
                        severity = DataSensitivity.CRITICAL
                    ))
                    gdprCompliant = false
                    ccpCompliant = false
                    suggestions.add("健康数据不建议通过 Handoff 传输，建议使用专用安全通道 / Health data should not be transferred via Handoff, use dedicated secure channel")
                }
                dataType.contains("financial", ignoreCase = true) ||
                dataType.contains("payment", ignoreCase = true) -> {
                    violations.add(PrivacyViolation(
                        regulation = "PCI-DSS / CCPA",
                        description = "金融数据跨设备传输需额外安全措施 / Financial data cross-device transfer requires additional security measures",
                        severity = DataSensitivity.HIGH
                    ))
                    ccpCompliant = false
                    suggestions.add("金融数据应使用加密通道传输并在目标设备验证身份 / Encrypt financial data and verify identity on target device")
                }
            }
        }

        val crossDeviceItems = dataTypes.map { type ->
            CrossDeviceDataItem(
                name = type,
                sensitivity = when {
                    type.contains("health", ignoreCase = true) ||
                    type.contains("financial", ignoreCase = true) -> DataSensitivity.HIGH
                    type.contains("email", ignoreCase = true) ||
                    type.contains("phone", ignoreCase = true) -> DataSensitivity.MEDIUM
                    else -> DataSensitivity.LOW
                },
                requiresEncryption = !gdprCompliant || !ccpCompliant
            )
        }

        if (suggestions.isEmpty()) {
            suggestions.add("当前数据类型合规，建议对所有跨设备传输数据使用 TLS 加密 / Current data types are compliant, recommend TLS encryption for all cross-device transfers")
        }

        val overallStatus = when {
            !gdprCompliant || !ccpCompliant -> ComplianceStatus.FAIL
            dataTypes.size > 3 -> ComplianceStatus.WARN
            else -> ComplianceStatus.PASS
        }

        return PrivacyReport(
            overallStatus = overallStatus,
            gdprCompliant = gdprCompliant,
            ccpCompliant = ccpCompliant,
            crossDeviceDataItems = crossDeviceItems,
            violations = violations,
            suggestions = suggestions
        )
    }

    // ============================================================
    // DEBUG_PANEL handlers
    // ============================================================
    private fun handleSelectSourceDevice(device: DeviceType) {
        _state.value = _state.value.copy(selectedSourceDevice = device)
    }

    private fun handleSelectTargetDevice(device: DeviceType) {
        _state.value = _state.value.copy(selectedTargetDevice = device)
    }

    private suspend fun handleStartDebugSession() {
        val source = _state.value.selectedSourceDevice
        val target = _state.value.selectedTargetDevice

        _state.value = _state.value.copy(isLoading = true)

        val session = DebugSession(
            sourceDevice = source,
            targetDevice = target,
            status = SessionStatus.RUNNING,
            steps = listOf(
                HandoffStep(1, "初始化 / Initialize", "建立 Handoff 会话 / Establishing Handoff session", StepStatus.IN_PROGRESS),
                HandoffStep(2, "序列化状态 / Serialize State", "打包 Activity 状态 / Packing Activity state", StepStatus.PENDING),
                HandoffStep(3, "传输数据 / Transfer Data", "跨设备传输 HandoffActivityData / Cross-device transfer", StepStatus.PENDING),
                HandoffStep(4, "恢复状态 / Restore State", "在目标设备恢复 Activity / Restoring Activity on target", StepStatus.PENDING),
                HandoffStep(5, "完成 / Complete", "Handoff 流程完成 / Handoff flow complete", StepStatus.PENDING)
            )
        )

        val sessions = _state.value.debugSessions.toMutableList()
        sessions.add(0, session)
        _state.value = _state.value.copy(
            debugSessions = sessions,
            activeSession = session,
            isLoading = false
        )

        // Simulate step progression / 模拟步骤推进
        simulateSessionSteps(session.id)
    }

    private suspend fun simulateSessionSteps(sessionId: String) {
        kotlinx.coroutines.delay(500)
        val updatedSteps1 = listOf(
            HandoffStep(1, "初始化 / Initialize", "Handoff 会话已建立 / Session established", StepStatus.DONE),
            HandoffStep(2, "序列化状态 / Serialize State", "正在打包 Activity 状态 / Packing Activity state", StepStatus.IN_PROGRESS),
            HandoffStep(3, "传输数据 / Transfer Data", "跨设备传输 HandoffActivityData / Cross-device transfer", StepStatus.PENDING),
            HandoffStep(4, "恢复状态 / Restore State", "在目标设备恢复 Activity / Restoring Activity on target", StepStatus.PENDING),
            HandoffStep(5, "完成 / Complete", "Handoff 流程完成 / Handoff flow complete", StepStatus.PENDING)
        )
        updateSessionSteps(sessionId, updatedSteps1, SessionStatus.RUNNING)

        kotlinx.coroutines.delay(800)
        val updatedSteps2 = listOf(
            HandoffStep(1, "初始化 / Initialize", "Handoff 会话已建立 / Session established", StepStatus.DONE),
            HandoffStep(2, "序列化状态 / Serialize State", "Activity 状态已打包 / Activity state packed", StepStatus.DONE),
            HandoffStep(3, "传输数据 / Transfer Data", "正在跨设备传输 / Cross-device transfer in progress", StepStatus.IN_PROGRESS),
            HandoffStep(4, "恢复状态 / Restore State", "在目标设备恢复 Activity / Restoring Activity on target", StepStatus.PENDING),
            HandoffStep(5, "完成 / Complete", "Handoff 流程完成 / Handoff flow complete", StepStatus.PENDING)
        )
        updateSessionSteps(sessionId, updatedSteps2, SessionStatus.RUNNING)

        kotlinx.coroutines.delay(600)
        val updatedSteps3 = listOf(
            HandoffStep(1, "初始化 / Initialize", "Handoff 会话已建立 / Session established", StepStatus.DONE),
            HandoffStep(2, "序列化状态 / Serialize State", "Activity 状态已打包 / Activity state packed", StepStatus.DONE),
            HandoffStep(3, "传输数据 / Transfer Data", "数据传输完成 / Transfer complete", StepStatus.DONE),
            HandoffStep(4, "恢复状态 / Restore State", "正在目标设备恢复 / Restoring on target device", StepStatus.IN_PROGRESS),
            HandoffStep(5, "完成 / Complete", "Handoff 流程完成 / Handoff flow complete", StepStatus.PENDING)
        )
        updateSessionSteps(sessionId, updatedSteps3, SessionStatus.RUNNING)

        kotlinx.coroutines.delay(400)
        val finalSteps = listOf(
            HandoffStep(1, "初始化 / Initialize", "Handoff 会话已建立 / Session established", StepStatus.DONE),
            HandoffStep(2, "序列化状态 / Serialize State", "Activity 状态已打包 / Activity state packed", StepStatus.DONE),
            HandoffStep(3, "传输数据 / Transfer Data", "数据传输完成 / Transfer complete", StepStatus.DONE),
            HandoffStep(4, "恢复状态 / Restore State", "Activity 已在目标设备恢复 / Activity restored on target", StepStatus.DONE),
            HandoffStep(5, "完成 / Complete", "✅ Handoff 流程完成 / Handoff flow complete", StepStatus.DONE)
        )
        val completedSession = updateSessionSteps(sessionId, finalSteps, SessionStatus.SUCCESS)
        completedSession?.let {
            _effect.send(HandoffEffect.DebugSessionCompleted(it))
        }
    }

    private fun updateSessionSteps(
        sessionId: String,
        steps: List<HandoffStep>,
        status: SessionStatus
    ): DebugSession? {
        val sessions = _state.value.debugSessions.toMutableList()
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index < 0) return null
        val updated = sessions[index].copy(steps = steps, status = status)
        sessions[index] = updated
        _state.value = _state.value.copy(
            debugSessions = sessions,
            activeSession = if (_state.value.activeSession?.id == sessionId) updated else _state.value.activeSession
        )
        return updated
    }

    private fun handleSelectDebugSession(session: DebugSession) {
        _state.value = _state.value.copy(activeSession = session)
    }

    // ============================================================
    // SYNC_TEMPLATE handlers
    // ============================================================
    private fun handleUpdateSyncConfig(config: SyncConfig) {
        _state.value = _state.value.copy(syncConfig = config)
    }

    // ============================================================
    // APP_BUNDLE handlers
    // ============================================================
    private fun handleUpdateAppBundleConfig(config: AppBundleConfig) {
        _state.value = _state.value.copy(appBundleConfig = config)
    }

    // ============================================================
    // FALLBACK_STRATEGY handlers
    // ============================================================
    private fun handleSelectFallback(type: FallbackType) {
        _state.value = _state.value.copy(selectedFallback = type)
    }
}
