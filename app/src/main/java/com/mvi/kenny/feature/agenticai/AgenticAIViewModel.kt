package com.mvi.kenny.feature.agenticai

// ================================================================
// AgenticAIViewModel — Android Agentic AI AppFunctions MVI ViewModel
// ================================================================
// ViewModel for Android Agentic AI AppFunctions & UI Automation Framework.
//
// PRD-169: Android Agentic AI AppFunctions & UI Automation Framework 开发工具包
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Manage tab navigation state
//   - Calculate Google I/O 2026 countdown
//   - Provide AppFunctions templates and code examples
//   - Run CI compliance checks
//   - Manage design checklist state
//   - Expose one-time Effects (navigation, toast, copy)
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * ============================================================
 * AgenticAIViewModel — AgenticAI MVI ViewModel
 * ============================================================
 * Manages the AgenticUIState and processes AgenticIntent.
 *
 * In a real implementation, this would:
 *   - Integrate with actual AppFunctions APIs
 *   - Parse APK for AppFunctions annotations
 *   - Connect to Gemini for discovery
 *   - Run APK analysis for compliance checks
 *
 * Current implementation uses mock data for demonstration purposes.
 *
 * @see AgenticUIState
 * @see AgenticIntent
 * @see AgenticEffect
 */
class AgenticAIViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(AgenticUIState.Initial)
    val state: StateFlow<AgenticUIState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via SharedFlow
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<AgenticEffect>()
    val effect = _effect.asSharedFlow()

    // ─────────────────────────────────────────────────────────────
    // Google I/O 2026 Target Date
    // ─────────────────────────────────────────────────────────────
    companion object {
        // Google I/O 2026 target date: May 19, 2026
        private val IO_2026_TARGET = java.time.LocalDateTime.of(2026, 5, 19, 10, 0, 0)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
    }

    init {
        // Initialize countdown timer
        startCountdown()
        // Initialize mock data
        initializeMockData()
    }

    /**
     * ============================================================
     * sendIntent — Intent 处理入口
     * ============================================================
     * Entry point for all user intents. Called from UI layer.
     *
     * @param intent User intent
     * @see AgenticIntent
     */
    fun sendIntent(intent: AgenticIntent) {
        when (intent) {
            is AgenticIntent.SelectTab -> selectTab(intent.index)
            is AgenticIntent.SelectPathNode -> selectPathNode(intent.nodeId)
            is AgenticIntent.CopyCode -> copyCode(intent.code)
            is AgenticIntent.RunComplianceCheck -> runComplianceCheck(intent.packageName)
            is AgenticIntent.ExportReport -> exportReport(intent.format)
            is AgenticIntent.SelectTemplate -> selectTemplate(intent.template)
            is AgenticIntent.ToggleChecklistItem -> toggleChecklistItem(intent.itemId)
            is AgenticIntent.ToggleTheme -> toggleTheme(intent.isDark)
        }
    }

    // ================================================================
    // Intent Handlers
    // ================================================================

    /**
     * SelectTab — 切换 Tab
     *
     * @param index Tab index
     */
    private fun selectTab(index: Int) {
        _update { copy(selectedTab = index) }
    }

    /**
     * SelectPathNode — 选择路径图节点
     *
     * @param nodeId Node ID
     */
    private fun selectPathNode(nodeId: String) {
        val targetTab = when (nodeId) {
            "node_app_functions" -> AgenticTab.APP_FUNCTIONS
            "node_gemini" -> AgenticTab.GEMINI_INTEGRATION
            "node_ui_automation" -> AgenticTab.UI_AUTOMATION
            "node_a2a" -> AgenticTab.A2A_PROTOCOL
            "node_design" -> AgenticTab.DESIGN_STANDARDS
            "node_security" -> AgenticTab.SECURITY_PRIVACY
            "node_cli" -> AgenticTab.CLI_SKILLS
            "node_ci" -> AgenticTab.CI_COMPLIANCE
            else -> AgenticTab.HOME
        }
        _update { copy(pathMapSelectedNode = nodeId, selectedTab = targetTab.ordinal) }
        viewModelScope.launch {
            _effect.emit(AgenticEffect.NavigateToTab(targetTab.ordinal))
        }
    }

    /**
     * CopyCode — 复制代码到剪贴板
     *
     * @param code Code to copy
     */
    private fun copyCode(code: String) {
        viewModelScope.launch {
            _effect.emit(AgenticEffect.CodeCopied)
            _effect.emit(AgenticEffect.ShowToast("代码已复制到剪贴板"))
        }
    }

    /**
     * RunComplianceCheck — 运行合规检测
     *
     * @param packageName App package name
     */
    private fun runComplianceCheck(packageName: String) {
        viewModelScope.launch {
            _update { copy(isLoading = true) }
            try {
                // Simulate compliance check process
                delay(2000)
                val checks = createMockComplianceChecks()
                val score = (checks.count { it.status == ComplianceStatus.PASS } * 100) / checks.size
                _update {
                    copy(
                        isLoading = false,
                        complianceChecks = checks,
                        complianceScore = score
                    )
                }
                _effect.emit(AgenticEffect.ShowToast("合规检测完成，得分: $score%"))
            } catch (e: Exception) {
                _update { copy(isLoading = false) }
                _effect.emit(AgenticEffect.ShowToast("合规检测失败: ${e.message}"))
            }
        }
    }

    /**
     * ExportReport — 导出合规报告
     *
     * @param format Export format (pdf, json, md)
     */
    private fun exportReport(format: String) {
        viewModelScope.launch {
            try {
                delay(500)
                val filePath = "/tmp/agentic_compliance_report.$format"
                _effect.emit(AgenticEffect.ReportExported(filePath))
                _effect.emit(AgenticEffect.ShowToast("报告已导出至 $filePath"))
            } catch (e: Exception) {
                _effect.emit(AgenticEffect.ShowToast("导出失败: ${e.message}"))
            }
        }
    }

    /**
     * SelectTemplate — 选择模板
     *
     * @param template Selected template
     */
    private fun selectTemplate(template: AppFunctionTemplate) {
        _update { copy(currentTemplate = template) }
    }

    /**
     * ToggleChecklistItem — 切换设计清单条目勾选状态
     *
     * @param itemId Item ID
     */
    private fun toggleChecklistItem(itemId: String) {
        _update {
            copy(
                checklistItems = checklistItems.map { item ->
                    if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
                }
            )
        }
    }

    /**
     * ToggleTheme — 切换主题
     *
     * @param isDark Dark mode enabled
     */
    private fun toggleTheme(isDark: Boolean) {
        _update { copy(isDarkTheme = isDark) }
    }

    // ================================================================
    // Countdown Timer
    // ================================================================

    /**
     * startCountdown — 启动 I/O 2026 倒计时
     *
     * Updates countdown every second using LaunchedEffect in the UI layer.
     * This method updates the state with the latest countdown value.
     */
    private fun startCountdown() {
        viewModelScope.launch {
            while (isActive) {
                val now = java.time.Instant.now()
                val remaining = Duration.between(now, IO_2026_TARGET)

                if (remaining.isNegative) {
                    _update { copy(ioCountdown = "Google I/O 2026 已结束") }
                } else {
                    val countdown = formatCountdown(
                        days = remaining.toDays(),
                        hours = remaining.toHours() % 24,
                        minutes = remaining.toMinutes() % 60,
                        seconds = remaining.seconds % 60
                    )
                    _update { copy(ioCountdown = countdown) }
                }
                delay(1000)
            }
        }
    }

    // ================================================================
    // Private Helpers
    // ================================================================

    /**
     * update — State update helper using immutable copy
     */
    private inline fun _update(update: AgenticUIState.() -> AgenticUIState) {
        _state.update { it.update() }
    }

    // ================================================================
    // Mock Data Initializers
    // ================================================================

    /**
     * initializeMockData — 初始化模拟数据
     */
    private fun initializeMockData() {
        _update {
            copy(
                checklistItems = createMockChecklistItems(),
                agentCards = createMockAgentCards(),
                skillDefinitions = createMockSkillDefinitions(),
                complianceChecks = createMockComplianceChecks(),
                complianceScore = 72
            )
        }
    }

    /**
     * createMockChecklistItems — 创建模拟设计检查清单
     */
    private fun createMockChecklistItems(): List<DesignChecklistItem> = listOf(
        // 透明性原则
        DesignChecklistItem(
            id = "dc_001",
            title = "操作意图透明",
            description = "当 AI agent 代表用户操作 App 时，App 应向用户清晰展示正在执行的操作和原因",
            category = "透明性"
        ),
        DesignChecklistItem(
            id = "dc_002",
            title = "状态变更通知",
            description = "App 数据或状态变更时，应主动通知用户和 AI agent",
            category = "透明性"
        ),
        DesignChecklistItem(
            id = "dc_003",
            title = "能力边界披露",
            description = "通过 Agent Card 明确告知 AI agents 当前 App 支持哪些操作及限制",
            category = "透明性"
        ),
        // 用户授权原则
        DesignChecklistItem(
            id = "dc_004",
            title = "敏感操作二次确认",
            description = "支付、删除等敏感操作必须经用户明确授权后才能由 agent 执行",
            category = "授权"
        ),
        DesignChecklistItem(
            id = "dc_005",
            title = "权限撤回机制",
            description = "用户应能随时撤回已授予 AI agent 的操作权限",
            category = "授权"
        ),
        DesignChecklistItem(
            id = "dc_006",
            title = "最小权限原则",
            description = "只授予 AI agent 完成特定任务所需的最小权限集",
            category = "授权"
        ),
        // 可控性原则
        DesignChecklistItem(
            id = "dc_007",
            title = "操作可取消",
            description = "正在进行中的 agent 操作应支持用户随时取消",
            category = "可控性"
        ),
        DesignChecklistItem(
            id = "dc_008",
            title = "操作审计日志",
            description = "记录所有 AI agent 执行的操作，支持用户查看和导出",
            category = "可控性"
        ),
        DesignChecklistItem(
            id = "dc_009",
            title = "超时保护机制",
            description = "Agent 操作应设置合理的超时时间，避免无限等待",
            category = "可控性"
        ),
        DesignChecklistItem(
            id = "dc_010",
            title = "错误恢复策略",
            description = "操作失败时应提供清晰的错误信息，并支持重试或回退",
            category = "可控性"
        )
    )

    /**
     * createMockAgentCards — 创建模拟 Agent Cards
     */
    private fun createMockAgentCards(): List<AgentCard> = listOf(
        AgentCard(
            name = "ShoppingAssistant",
            description = "电商购物助手 Agent，能够搜索商品、下单、查询物流",
            version = "1.0.0",
            capabilities = listOf(
                "search_products",
                "place_order",
                "query_logistics",
                "cancel_order"
            ),
            skills = listOf(
                "product_search",
                "price_comparison",
                "order_management"
            )
        ),
        AgentCard(
            name = "NavigationAgent",
            description = "出行导航 Agent，支持路线规划、实时导航、行程管理",
            version = "1.2.0",
            capabilities = listOf(
                "route_planning",
                "realtime_navigation",
                "trip_management"
            ),
            skills = listOf(
                "location_services",
                "traffic_analysis",
                "eta_calculation"
            )
        ),
        AgentCard(
            name = "MusicAssistant",
            description = "音乐播放控制 Agent，支持播放控制、歌单管理、音乐推荐",
            version = "2.0.0",
            capabilities = listOf(
                "playback_control",
                "playlist_management",
                "music_recommendation"
            ),
            skills = listOf(
                "audio_control",
                "content_discovery",
                "user_preference_learning"
            )
        )
    )

    /**
     * createMockSkillDefinitions — 创建模拟 CLI Skill 定义
     */
    private fun createMockSkillDefinitions(): List<SkillDefinition> = listOf(
        SkillDefinition(
            id = "skill_001",
            name = "android/agent-app-functions",
            description = "将 App 的能力函数注册到 Android Agent Runtime",
            commands = listOf(
                "adb shell app_functions register <package>",
                "adb shell app_functions list",
                "adb shell app_functions unregister <function_id>"
            ),
            category = "AppFunctions"
        ),
        SkillDefinition(
            id = "skill_002",
            name = "android/agent-discovery",
            description = "在本地网络中查找支持 Agentic AI 的 App",
            commands = listOf(
                "adb shell agent_discovery scan",
                "adb shell agent_discovery list",
                "adb shell agent_discovery query <package>"
            ),
            category = "Discovery"
        ),
        SkillDefinition(
            id = "skill_003",
            name = "android/ui-automation",
            description = "UI Automation Framework 调试和测试命令",
            commands = listOf(
                "adb shell uiauto dump",
                "adb shell uiauto run <script>",
                "adb shell uiauto inject <actions>"
            ),
            category = "UI Automation"
        ),
        SkillDefinition(
            id = "skill_004",
            name = "android/a2a-server",
            description = "启动和管理 A2A Protocol Server",
            commands = listOf(
                "adb shell a2a_server start --port <port>",
                "adb shell a2a_server stop",
                "adb shell a2a_server status"
            ),
            category = "A2A Protocol"
        ),
        SkillDefinition(
            id = "skill_005",
            name = "android/agentic-cli",
            description = "Android Agentic AI 综合调试工具",
            commands = listOf(
                "adb shell agentic list-apps",
                "adb shell agentic invoke <app_id> <function>",
                "adb shell agentic audit <package>"
            ),
            category = "General"
        )
    )

    /**
     * createMockComplianceChecks — 创建模拟合规检测条目
     */
    private fun createMockComplianceChecks(): List<ComplianceCheckItem> = listOf(
        ComplianceCheckItem(
            id = "cc_001",
            title = "AppFunctions 注解声明",
            description = "检查 AppFunctions 是否使用 @AgentFunction 注解正确声明",
            status = ComplianceStatus.PASS,
            suggestion = "已正确使用 @AgentFunction 注解声明所有函数"
        ),
        ComplianceCheckItem(
            id = "cc_002",
            title = "Agent Card 完整性",
            description = "检查 Agent Card 是否包含完整的 name, description, version, capabilities",
            status = ComplianceStatus.PASS,
            suggestion = "Agent Card 格式完整，符合规范"
        ),
        ComplianceCheckItem(
            id = "cc_003",
            title = "敏感权限声明",
            description = "检查 AppFunctions 是否正确声明所需权限",
            status = ComplianceStatus.WARNING,
            suggestion = "部分函数缺少权限声明，建议补充 @RequiresPermission 注解"
        ),
        ComplianceCheckItem(
            id = "cc_004",
            title = "UI Automation 支持",
            description = "检查 App 是否实现了 UI Automation Framework 所需的 AccessibilityNodeInfo",
            status = ComplianceStatus.PASS,
            suggestion = "UI Automation 支持已正确实现"
        ),
        ComplianceCheckItem(
            id = "cc_005",
            title = "用户确认对话框",
            description = "检查敏感操作是否有对应的用户确认 UI",
            status = ComplianceStatus.FAIL,
            suggestion = "缺少敏感操作的确认对话框，建议实现 ConfirmationAgentInterface"
        ),
        ComplianceCheckItem(
            id = "cc_006",
            title = "操作审计日志",
            description = "检查 App 是否实现了完整的操作审计日志",
            status = ComplianceStatus.WARNING,
            suggestion = "审计日志不完整，建议增加操作类型、操作时间、Agent ID 等字段"
        ),
        ComplianceCheckItem(
            id = "cc_007",
            title = "A2A Protocol 支持",
            description = "检查 App 是否实现了 A2A Protocol 的 Agent 端点",
            status = ComplianceStatus.PASS,
            suggestion = "A2A Protocol 实现正确"
        ),
        ComplianceCheckItem(
            id = "cc_008",
            title = "超时错误处理",
            description = "检查 AppFunctions 是否正确处理超时错误",
            status = ComplianceStatus.FAIL,
            suggestion = "缺少超时处理逻辑，建议为每个函数设置合理的超时时间并实现 CancellationException 处理"
        ),
        ComplianceCheckItem(
            id = "cc_009",
            title = "SKILL.md 定义文件",
            description = "检查是否在 android/skills 仓库中定义了对应的 Skill",
            status = ComplianceStatus.SKIP,
            suggestion = "建议创建 SKILL.md 文件定义 App 的 CLI Skills"
        ),
        ComplianceCheckItem(
            id = "cc_010",
            title = "最小权限检查",
            description = "检查 AppFunctions 权限是否符合最小权限原则",
            status = ComplianceStatus.WARNING,
            suggestion = "部分函数权限过大，建议按最小权限原则重新评估"
        )
    )
}
